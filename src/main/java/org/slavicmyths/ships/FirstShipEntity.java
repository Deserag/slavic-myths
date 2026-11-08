package org.slavicmyths.ships;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * First independent two-deck ship. Does NOT change the four saved raft formats.
 * A native-sized collision hull supplies boat motion; separate local geometry
 * supplies the floor, below-deck walls and distinct helmsman's attachment.
 *
 * Runtime multiplayer acceptance is required before enabling this on a release.
 */
public final class FirstShipEntity extends Boat {
    private static final EntityDataAccessor<Float> SURFACE = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> MODULES = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> NATIVE = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TURNS = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.INT);
    private static final float NO_SURFACE = -10000F;
    private static final double DRAFT = 0.14D;
    private static final int CELLS = FirstShipHull.WIDTH * FirstShipHull.LENGTH;
    private final Map<Integer, SimpleContainer> inventory = new HashMap<>();
    private final Map<Integer, Integer> kind = new HashMap<>();
    private final Map<Integer, SimpleContainerData> furnaces = new HashMap<>();
    private boolean packing;
    /** Malformed saved inventories are retained byte-for-byte; packing is blocked. */
    private ListTag quarantinedModules;
    private ListTag pendingBlockCargo = new ListTag();
    private BlockPos blockAnchor = BlockPos.ZERO;
    private int quarterTurns;
    private boolean nativeHull;
    private long lastHelmAction = -100L;
    private boolean legacyConversionTried;

    public BlockPos blockAnchor() {
        return level().isClientSide
                ? BlockPos.containing(getX(), getY(), getZ()) : blockAnchor;
    }
    public int quarterTurns() { return entityData.get(TURNS); }
    public boolean isNativeHull() { return entityData.get(NATIVE); }
    public ListTag pendingBlockCargo() { return pendingBlockCargo.copy(); }

    public void initializeNativeHull(BlockPos anchor, int turns) {
        blockAnchor = anchor.immutable();
        quarterTurns = Math.floorMod(turns, 4);
        nativeHull = true;
        entityData.set(NATIVE, true);
        entityData.set(TURNS, quarterTurns);
        setYRot(quarterTurns * 90F);
        setPos(anchor.getX() + .5D, anchor.getY(), anchor.getZ() + .5D);
        setDeltaMovement(Vec3.ZERO);
    }

    public FirstShipEntity(EntityType<? extends FirstShipEntity> type, Level world) {
        super(type, world);
        setVariant(Type.SPRUCE);
        setInvulnerable(true);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SURFACE, NO_SURFACE);
        builder.define(MODULES, "0".repeat(CELLS * 2));
        builder.define(NATIVE, false);
        builder.define(TURNS, 0);
    }

    public double deckY() { return getY() + FirstShipHull.MAIN_DECK; }
    public double holdY() { return getY() + FirstShipHull.HOLD_FLOOR; }
    public int moduleKind(int floor, int x, int z) {
        int tile = tileId(floor, x, z);
        if (tile < 0) return 0;
        String state = entityData.get(MODULES);
        return tile < state.length() ? state.charAt(tile) - '0' : 0;
    }

    private static int tileId(int floor, int x, int z) {
        return floor < 0 || floor > 1 || x < 0 || x >= FirstShipHull.WIDTH
                || z < 0 || z >= FirstShipHull.LENGTH ? -1 : floor * CELLS + z * FirstShipHull.WIDTH + x;
    }

    public static int tileAt(double localX, double localZ, int floor) {
        int x = (int)Math.floor(localX + FirstShipHull.WIDTH / 2D);
        int z = (int)Math.floor(localZ + FirstShipHull.LENGTH / 2D);
        if (floor < 0 || floor > 1 || !FirstShipHull.withinHull(localX, localZ, 0.46D)) return -1;
        double centerX = x + .5D - FirstShipHull.WIDTH / 2D;
        double centerZ = z + .5D - FirstShipHull.LENGTH / 2D;
        // Cargo must be placed on a REAL rendered plank cell, never in a hatch,
        // outside the tapered hold or on the missing end of the curved bow.
        if (floor == 0 ? !FirstShipHull.holdSupports(localX, localZ)
                || !FirstShipHull.holdSupports(centerX, centerZ)
                : !FirstShipHull.mainDeckSupports(localX, localZ)
                || !FirstShipHull.mainDeckSupports(centerX, centerZ)) return -1;
        return tileId(floor, x, z);
    }

    private static boolean structuralCell(int tile) {
        int floor = tile / CELLS;
        int x = tile % FirstShipHull.WIDTH;
        int z = (tile % CELLS) / FirstShipHull.WIDTH;
        double cx = x + .5D - FirstShipHull.WIDTH / 2D;
        double cz = z + .5D - FirstShipHull.LENGTH / 2D;
        if (floor == 0) return FirstShipHull.onLadder(cx, cz);
        return (Math.abs(cx) < .80D && Math.abs(cz - FirstShipHull.MAST_Z) < .8D)
                || (Math.abs(cx) < 1.0D && Math.abs(cz - FirstShipHull.HELM_Z) < 1.0D);
    }

    private void syncModules() {
        char[] buffer = "0".repeat(CELLS * 2).toCharArray();
        for (var entry : kind.entrySet()) if (entry.getKey() >= 0 && entry.getKey() < buffer.length) {
            buffer[entry.getKey()] = (char)('0' + entry.getValue());
        }
        entityData.set(MODULES, new String(buffer));
    }

    public void setWaterSurface(double y) {
        if (Double.isFinite(y) && y > level().getMinBuildHeight() && y < level().getMaxBuildHeight()) {
            entityData.set(SURFACE, (float)y);
        }
    }

    private boolean afloat() {
        float surface = entityData.get(SURFACE);
        if (surface == NO_SURFACE) return false;
        BlockPos pos = BlockPos.containing(getX(), surface - DRAFT, getZ());
        return level().hasChunkAt(pos) && level().getFluidState(pos).is(FluidTags.WATER);
    }

    @Override protected int getMaxPassengers() { return 1; }

    /** The one genuine helm seat is on the STERN, not a fake free-walking passenger. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return new Vec3(0D, FirstShipHull.MAIN_DECK + 0.08D, FirstShipHull.HELM_Z);
    }

    /**
     * Boat has a special rider offset of its own. Native ships must instead use
     * the real helm position in WORLD space, rotating around the block anchor.
     * Repositioning happens when mounting and after an accepted voxel step,
     * never as a forced player teleport on every tick.
     */
    @Override protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!nativeHull) {
            super.positionRider(passenger, move);
            return;
        }
        if (!hasPassenger(passenger)) return;
        FirstShipHull.Point seat = FirstShipHull.toWorld(0D, FirstShipHull.HELM_Z,
                getX(), getZ(), getYRot());
        move.accept(passenger, seat.x(), deckY() + 0.08D, seat.z());
    }

    /**
     * Vanilla boats dismount next to their little waterline hitbox: on a ship
     * that is INSIDE the hold. Select a clear supported deck cell instead.
     */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (!nativeHull) return super.getDismountLocationForPassenger(passenger);
        double[][] safeCells = {{0D, -2D}, {1D, -2D}, {-1D, -2D},
                {1D, -3D}, {-1D, -3D}, {0D, -3D},
                {2D, -2D}, {-2D, -2D}, {0D, 0D}, {1D, 0D}, {-1D, 0D}};
        for (double[] cell : safeCells) {
            if (!FirstShipHull.mainDeckSupports(cell[0], cell[1])) continue;
            FirstShipHull.Point point = FirstShipHull.toWorld(cell[0], cell[1],
                    getX(), getZ(), getYRot());
            Vec3 target = new Vec3(point.x(), deckY() + 0.05D, point.z());
            BlockPos floor = BlockPos.containing(point.x(), deckY() - .15D, point.z());
            if (!level().hasChunkAt(floor) || level().getBlockState(floor).isAir()) continue;
            AABB area = passenger.getBoundingBox().move(target.subtract(passenger.position()));
            if (level().noCollision(passenger, area)) return target;
        }
        // Never return the entity's waterline centre inside solid hold blocks.
        // A high, unobstructed point above the deck is safer than suffocation.
        FirstShipHull.Point stern = FirstShipHull.toWorld(0D, -2D,
                getX(), getZ(), getYRot());
        for (int above = 1; above <= 8; above++) {
            Vec3 fallback = new Vec3(stern.x(), deckY() + above + .05D, stern.z());
            if (level().noCollision(passenger,
                    passenger.getBoundingBox().move(fallback.subtract(passenger.position()))))
                return fallback;
        }
        return new Vec3(stern.x(), deckY() + 16D, stern.z());
    }

    @Override protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (nativeHull) lastHelmAction = -100L;
    }

    @Override public boolean canCollideWith(Entity other) {
        return !(other instanceof Player) && super.canCollideWith(other);
    }

    @Override public boolean hurt(DamageSource source, float damage) { return false; }
    @Override public ItemStack getPickResult() { return new ItemStack(RaftContent.FIRST_SHIP_ITEM.get()); }
    @Override public Item getDropItem() { return RaftContent.FIRST_SHIP_ITEM.get(); }
    @Override protected Component getTypeName() { return Component.literal("Деревянный корабль 7×11"); }

    private ListTag storeModules(HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (var entry : kind.entrySet()) {
            int tile = entry.getKey(), type = entry.getValue();
            SimpleContainer slots = inventory.get(tile);
            if (slots == null) continue;
            CompoundTag data = new CompoundTag();
            data.putInt("Cell", tile);
            data.putInt("Kind", type);
            data.put("Items", slots.createTag(registries));
            SimpleContainerData heat = furnaces.get(tile);
            if (heat != null) data.putIntArray("Heat", new int[]{heat.get(0), heat.get(1), heat.get(2), heat.get(3)});
            list.add(data);
        }
        return list;
    }

    /** Validate the complete payload before changing a live entity or consuming an item. */
    public boolean loadPacked(ItemStack item) {
        CustomData custom = item.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return true;
        CompoundTag root = custom.copyTag();
        if (!root.contains("FirstShipPack", Tag.TAG_COMPOUND)) return true;
        CompoundTag pack = root.getCompound("FirstShipPack");
        if (pack.getInt("Version") != 1 || !pack.contains("Modules", Tag.TAG_LIST)) return false;
        ListTag list = pack.getList("Modules", Tag.TAG_COMPOUND);
        if (list.size() > CELLS * 2) return false;
        Map<Integer, SimpleContainer> loaded = new HashMap<>();
        Map<Integer, Integer> types = new HashMap<>();
        Map<Integer, SimpleContainerData> heatMap = new HashMap<>();
        Set<Integer> seen = new HashSet<>();
        try {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag value = list.getCompound(i);
                int tile = value.getInt("Cell"), type = value.getInt("Kind");
                if (tile < 0 || tile >= CELLS * 2 || type < 1 || type > 4 || !seen.add(tile)
                        || !value.contains("Items", Tag.TAG_LIST)) return false;
                int x = tile % FirstShipHull.WIDTH, z = (tile % CELLS) / FirstShipHull.WIDTH;
                double localX = x + .5D - FirstShipHull.WIDTH / 2D;
                double localZ = z + .5D - FirstShipHull.LENGTH / 2D;
                if (tileAt(localX, localZ, tile / CELLS) != tile) return false;
                ListTag stored = value.getList("Items", Tag.TAG_COMPOUND);
                if (stored.size() > (type == 4 ? 3 : 27)) return false;
                SimpleContainer slots = new SimpleContainer(type == 4 ? 3 : 27);
                slots.fromTag(stored, registryAccess());
                if (!stored.equals(slots.createTag(registryAccess()))) return false;
                loaded.put(tile, slots);
                types.put(tile, type);
                if (type == 4) {
                    SimpleContainerData heat = new SimpleContainerData(4);
                    int[] ticks = value.getIntArray("Heat");
                    for (int n = 0; n < Math.min(4, ticks.length); n++) heat.set(n, Math.max(0, ticks[n]));
                    heatMap.put(tile, heat);
                }
            }
        } catch (RuntimeException malformed) { return false; }
        if (pack.contains("BlockCargo", Tag.TAG_LIST)) {
            pendingBlockCargo = pack.getList("BlockCargo", Tag.TAG_COMPOUND).copy();
            if (pendingBlockCargo.size() > CELLS * 2) return false;
        } else pendingBlockCargo = new ListTag();
        quarantinedModules = null;
        inventory.clear(); inventory.putAll(loaded);
        kind.clear(); kind.putAll(types);
        furnaces.clear(); furnaces.putAll(heatMap);
        syncModules();
        return true;
    }

    private ItemStack packed() {
        ItemStack stack = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 1);
        pack.putUUID("VesselId", getUUID());
        pack.put("Modules", storeModules(registryAccess()));
        if (nativeHull && level() instanceof net.minecraft.server.level.ServerLevel server) {
            ListTag cargo = FirstShipBlockHull.pack(server, blockAnchor, quarterTurns);
            if (cargo == null) return ItemStack.EMPTY;
            pack.put("BlockCargo", cargo);
        }
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return stack;
    }

    /** No packing while anybody stands in the hold or on the deck. */
    private boolean hasWalkers() {
        AABB area = getBoundingBox().inflate(8D, 5D, 8D);
        for (Player player : level().getEntitiesOfClass(Player.class, area)) {
            if (player.isPassenger()) continue;
            FirstShipHull.Point local = FirstShipHull.toLocal(
                    player.getX(), player.getZ(), getX(), getZ(), getYRot());
            if (FirstShipHull.withinHull(local.x(), local.z(), .0D)
                    && player.getY() >= holdY() - 0.55D
                    && player.getY() <= deckY() + 2.2D) return true;
        }
        return false;
    }

    public InteractionResult packWithKey(Player player) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer) || quarantinedModules != null || packing || isRemoved() || isVehicle()
                || hasWalkers() || getDeltaMovement().horizontalDistanceSqr() > .0005D) {
            player.displayClientMessage(Component.literal("Судно должно стоять без экипажа и пассажиров"), true);
            return InteractionResult.FAIL;
        }
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    getBoundingBox().inflate(10D, 8D, 10D))) {
                if (nearby.containerMenu != nearby.inventoryMenu) {
                    player.displayClientMessage(Component.literal(
                            "Перед упаковкой все грузовые окна должны быть закрыты"), true);
                    return InteractionResult.FAIL;
                }
            }
        }
        ItemStack stack = packed();
        if (stack.isEmpty()) return InteractionResult.FAIL;
        if (nativeHull) {
            if (!(level() instanceof net.minecraft.server.level.ServerLevel server)
                    || !FirstShipBlockHull.remove(server, blockAnchor, quarterTurns)) {
                player.displayClientMessage(Component.literal("Корпус изменён или груз повреждён: упаковка заблокирована"), true);
                return InteractionResult.FAIL;
            }
        }
        packing = true;
        discard(); // Remove real blocks and ship before granting an inventory item.
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.displayClientMessage(Component.literal("Корабль упакован вместе с грузом"), true);
        return InteractionResult.CONSUME;
    }


    /**
     * The visible helm is ~3.6 blocks aft of the entity center; vanilla Boat
     * interaction range targets only its ~3-block core. A validated action
     * allows the actual painted helm to be used from its stern position.
     */
    public boolean tryHelm(ServerPlayer player) {
        if (isVehicle() || player.isPassenger() || player.isSpectator()) return false;
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        if (Math.abs(local.x()) > 1.65D
                || Math.abs(local.z() - FirstShipHull.HELM_Z) > 1.7D
                || Math.abs(player.getY() - deckY()) > .9D) return false;
        FirstShipHull.Point wheel = FirstShipHull.toWorld(0D, -4D,
                getX(), getZ(), getYRot());
        Vec3 aim = new Vec3(wheel.x(), deckY() + 1.25D, wheel.z()).subtract(player.getEyePosition());
        if (aim.lengthSqr() < .09D || aim.lengthSqr() > 25D
                || aim.normalize().dot(player.getLookAngle()) < .76D) return false;
        boolean mounted = player.startRiding(this);
        if (mounted) positionRider(player);
        return mounted;
    }

    /**
     * A single explicit boarding step from water to the open deck.
     * Once on deck the player is NOT mounted or moved between fake seats.
     */
    public boolean tryBoard(ServerPlayer player) {
        if (player.isPassenger() || player.isSpectator()
                || getDeltaMovement().horizontalDistanceSqr() > .0025D
                || player.getY() >= deckY() - 1.1D) return false;
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        double z = FirstShipHull.clamp(local.z(), -4.7D, 4.7D);
        double half = FirstShipHull.halfBeam(z);
        if (Math.abs(local.z()) > 6.25D || Math.abs(local.x()) > half + 1.60D
                || Math.abs(local.x()) < half - 1.1D && Math.abs(local.z()) < 4.9D) return false;
        double landingX = FirstShipHull.clamp(local.x(), -half + .82D, half - .82D);
        if (!FirstShipHull.mainDeckSupports(landingX, z)) return false;
        FirstShipHull.Point target = FirstShipHull.toWorld(landingX, z,
                getX(), getZ(), getYRot());
        Vec3 delta = new Vec3(target.x() - player.getX(), deckY() - player.getY(),
                target.z() - player.getZ());
        if (!player.level().noCollision(player, player.getBoundingBox().move(delta))) return false;
        player.teleportTo(target.x(), deckY() + .01D, target.z());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0F;
        return true;
    }

    @Override public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(RaftContent.SHIPYARD_KEY.get())) return packWithKey(player);
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        // Helm can only be activated at the stern, on the MAIN deck.
        if (!player.isSecondaryUseActive() && Math.abs(local.x()) < 1.35D
                && Math.abs(local.z() - FirstShipHull.HELM_Z) < 1.65D
                && Math.abs(player.getY() - deckY()) < 1.3D) {
            if (!level().isClientSide && player.startRiding(this)) positionRider(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
        return interactFromDeck(player, hand);
    }

    /** Also called by air-right-click while standing on any part of this large ship. */
    public InteractionResult interactFromDeck(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        int floor = player.getY() > getY() + 1.8D ? 1 : 0;
        int tile = tileAt(local.x(), local.z(), floor);
        if (tile < 0 || Math.abs(player.getY() - (floor == 1 ? deckY() : holdY())) > .72D)
            return InteractionResult.FAIL;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (held.is(Items.CHEST) || held.is(Items.BARREL)
                || held.is(Items.CRAFTING_TABLE) || held.is(Items.FURNACE)) {
            if (kind.containsKey(tile) || structuralCell(tile)) return InteractionResult.FAIL;
            int type = held.is(Items.CHEST) ? 1 : held.is(Items.BARREL) ? 2
                    : held.is(Items.CRAFTING_TABLE) ? 3 : 4;
            kind.put(tile, type);
            inventory.put(tile, new SimpleContainer(type == 4 ? 3 : 27));
            if (type == 4) furnaces.put(tile, new SimpleContainerData(4));
            syncModules();
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.CONSUME;
        }
        SimpleContainer slots = inventory.get(tile);
        if (slots == null || !(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        int type = kind.get(tile);
        Component label = Component.literal(type == 1 ? "Корабельный сундук" :
                type == 2 ? "Корабельная бочка" : type == 3 ? "Верстак" : "Корабельная печь");
        if (type == 3) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    new net.minecraft.world.inventory.CraftingMenu(id, inv,
                            net.minecraft.world.inventory.ContainerLevelAccess.create(level(), blockPosition())) {
                        @Override public boolean stillValid(Player viewer) {
                            return !FirstShipEntity.this.isRemoved() && viewer.distanceToSqr(FirstShipEntity.this) < 100D;
                        }
                    }, label));
        } else if (type == 4) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    new FurnaceMenu(id, inv, slots, furnaces.get(tile)), label));
        } else {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    ChestMenu.threeRows(id, inv, slots), label));
        }
        return InteractionResult.CONSUME;
    }


    /** Same conservative, three-slot smelting semantics as the existing rafts. */
    private static int fuelTicks(ItemStack fuel) {
        if (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL)) return 1600;
        if (fuel.is(Items.BLAZE_ROD)) return 2400;
        if (fuel.is(net.minecraft.tags.ItemTags.LOGS)
                || fuel.is(net.minecraft.tags.ItemTags.PLANKS)) return 300;
        if (fuel.is(Items.STICK)) return 100;
        return 0; // no untested container fuel, no disappearing buckets
    }

    private void tickShipFurnaces() {
        for (var entry : furnaces.entrySet()) {
            SimpleContainer slots = inventory.get(entry.getKey());
            if (slots == null || slots.getContainerSize() != 3) continue;
            SimpleContainerData heat = entry.getValue();
            int lit = Math.max(0, heat.get(0) - 1);
            heat.set(0, lit);
            ItemStack source = slots.getItem(0), fuel = slots.getItem(1), output = slots.getItem(2);
            if (source.isEmpty()) { heat.set(2, 0); continue; }
            var match = level().getRecipeManager().getRecipeFor(
                    net.minecraft.world.item.crafting.RecipeType.SMELTING,
                    new net.minecraft.world.item.crafting.SingleRecipeInput(source), level());
            if (match.isEmpty()) { heat.set(2, 0); continue; }
            net.minecraft.world.item.crafting.SmeltingRecipe recipe = match.get().value();
            ItemStack result = recipe.assemble(
                    new net.minecraft.world.item.crafting.SingleRecipeInput(source), registryAccess());
            if (result.isEmpty() || (!output.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(output, result)
                    || output.getCount() + result.getCount() > output.getMaxStackSize()))) {
                heat.set(2, 0);
                continue;
            }
            if (lit == 0 && !fuel.isEmpty()) {
                int burn = fuelTicks(fuel);
                if (burn > 0) {
                    fuel.shrink(1);
                    heat.set(0, burn);
                    heat.set(1, burn);
                    lit = burn;
                }
            }
            if (lit > 0) {
                int progress = heat.get(2) + 1;
                int required = Math.max(1, recipe.getCookingTime());
                heat.set(3, required);
                if (progress >= required) {
                    source.shrink(1);
                    if (output.isEmpty()) slots.setItem(2, result.copy());
                    else output.grow(result.getCount());
                    heat.set(2, 0);
                    slots.setChanged();
                } else heat.set(2, progress);
            } else heat.set(2, 0);
        }
    }

    /**
     * Native ship is a STATIC world-block structure between helm commands.
     * Never invoke Boat.tick() movement/float code: it was responsible for
     * dragging people off the fake floors. Vanilla blocks provide real collision.
     */
    @Override public void tick() {
        // Worlds created with the rejected fake-entity ship contain no world
        // blocks. Try a one-time safe upgrade if the former module grid is
        // empty and the 7x11 water site is still unobstructed. Never overwrite
        // land, delete NBT cargo, or silently claim an unsafe conversion.
        if (!isNativeHull() && !level().isClientSide && !legacyConversionTried) {
            legacyConversionTried = true;
            if (!isVehicle() && kind.isEmpty() && quarantinedModules == null
                    && level() instanceof net.minecraft.server.level.ServerLevel server) {
                BlockPos proposed = BlockPos.containing(getX(), getY(), getZ());
                int turn = FirstShipBlockHull.rotationFromYaw(getYRot());
                if (FirstShipBlockHull.place(server, proposed, turn, new ListTag())) {
                    initializeNativeHull(proposed, turn);
                    setCustomName(null);
                    setCustomNameVisible(false);
                }
            }
            if (!isNativeHull()) {
                setCustomName(Component.literal("СТАРЫЙ КОРАБЛЬ — УПАКУЙТЕ КЛЮЧОМ И ПОСТАВЬТЕ ЗАНОВО"));
                setCustomNameVisible(true);
            }
        }
        if (isNativeHull()) {
            baseTick();
            setDeltaMovement(Vec3.ZERO);
            if (!level().isClientSide)
                setPos(blockAnchor.getX() + .5D, blockAnchor.getY(), blockAnchor.getZ() + .5D);
        } else {
            // Legacy experimental entities remain identifiable for safe packing;
            // they are deliberately not silently converted mid-save.
            super.tick();
        }
        if (!level().isClientSide) tickShipFurnaces();
    }

    /**
     * One step per helm command. Validates an actual rider, cooldown, source
     * integrity and destination BEFORE moving any vanilla block.
     */
    public boolean helmStep(ServerPlayer driver, int action) {
        if (!nativeHull || driver.getVehicle() != this || getFirstPassenger() != driver
                || !(level() instanceof net.minecraft.server.level.ServerLevel server)
                || action < FirstShipNetwork.FORWARD || action > FirstShipNetwork.RIGHT
                || level().getGameTime() - lastHelmAction < 10L) return false;
        int nextTurn = quarterTurns;
        BlockPos nextAnchor = blockAnchor;
        if (action == FirstShipNetwork.LEFT) nextTurn = Math.floorMod(quarterTurns - 1, 4);
        else if (action == FirstShipNetwork.RIGHT) nextTurn = (quarterTurns + 1) % 4;
        else {
            int signed = action == FirstShipNetwork.FORWARD ? 1 : -1;
            int dx = quarterTurns == 1 ? -signed : quarterTurns == 3 ? signed : 0;
            int dz = quarterTurns == 0 ? signed : quarterTurns == 2 ? -signed : 0;
            nextAnchor = blockAnchor.offset(dx, 0, dz);
        }
        if (nextTurn == quarterTurns && nextAnchor.equals(blockAnchor)) return false;
        BlockPos oldAnchor = blockAnchor;
        int oldTurn = quarterTurns;
        java.util.List<ServerPlayer> walkers = new java.util.ArrayList<>();
        java.util.Map<ServerPlayer, FirstShipHull.Point> localPositions = new java.util.HashMap<>();
        AABB nearby = new AABB(oldAnchor).inflate(9D, 9D, 9D);
        for (ServerPlayer p : server.getEntitiesOfClass(ServerPlayer.class, nearby)) {
            if (p.containerMenu != p.inventoryMenu) {
                driver.displayClientMessage(Component.literal(
                        "Закройте все окна сундуков/печей перед перемещением корабля"), true);
                return false;
            }
            if (p == driver || p.isPassenger() || p.isSpectator() || p.getAbilities().flying) continue;
            FirstShipHull.Point local = FirstShipHull.toLocal(p.getX(), p.getZ(),
                    getX(), getZ(), getYRot());
            boolean onShip = FirstShipHull.withinHull(local.x(), local.z(), .1D)
                    && ((p.getY() >= holdY() - .25D && p.getY() <= holdY() + 2.9D)
                    || (p.getY() >= deckY() - .25D && p.getY() <= deckY() + 2.3D));
            if (onShip) { walkers.add(p); localPositions.put(p, local); }
        }
        if (!FirstShipBlockHull.move(server, oldAnchor, oldTurn, nextAnchor, nextTurn)) {
            driver.displayClientMessage(Component.literal(
                    "Ход заблокирован: мелководье, препятствие, изменённый корпус или груз"), true);
            lastHelmAction = level().getGameTime();
            return false;
        }
        initializeNativeHull(nextAnchor, nextTurn);
        // The captain was intentionally excluded from free-walker transfers;
        // update the MOUNTED seat explicitly to keep the captain on deck.
        positionRider(driver);
        lastHelmAction = level().getGameTime();
        for (ServerPlayer p : walkers) {
            FirstShipHull.Point oldLocal = localPositions.get(p);
            FirstShipHull.Point target = FirstShipHull.toWorld(
                    oldLocal.x(), oldLocal.z(), getX(), getZ(), getYRot());
            p.teleportTo(target.x(), p.getY(), target.z());
            p.fallDistance = 0F;
        }
        return true;
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("FirstShipSurface", entityData.get(SURFACE));
        tag.putBoolean("FirstShipNative", nativeHull);
        if (nativeHull) {
            tag.putLong("FirstShipAnchor", blockAnchor.asLong());
            tag.putInt("FirstShipTurns", quarterTurns);
        }
        tag.put("FirstShipModules", quarantinedModules != null ? quarantinedModules.copy() : storeModules(registryAccess()));
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        nativeHull = tag.getBoolean("FirstShipNative");
        if (nativeHull) initializeNativeHull(BlockPos.of(tag.getLong("FirstShipAnchor")),
                tag.getInt("FirstShipTurns"));
        if (tag.contains("FirstShipSurface", Tag.TAG_FLOAT)) setWaterSurface(tag.getFloat("FirstShipSurface"));
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 1);
        pack.put("Modules", tag.getList("FirstShipModules", Tag.TAG_COMPOUND));
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        ItemStack temporary = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        temporary.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        if (!loadPacked(temporary)) {
            // Preserve malformed raw ItemStack tags during autosave. The ship
            // stays in-world but the key must never pack a partial empty copy.
            quarantinedModules = tag.getList("FirstShipModules", Tag.TAG_COMPOUND).copy();
            inventory.clear();
            kind.clear();
            furnaces.clear();
            syncModules();
        }
    }
}
