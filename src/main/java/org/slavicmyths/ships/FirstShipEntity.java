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
    private static final float NO_SURFACE = -10000F;
    private static final double DRAFT = 0.14D;
    private static final int CELLS = FirstShipHull.WIDTH * FirstShipHull.LENGTH;
    private final Map<Integer, SimpleContainer> inventory = new HashMap<>();
    private final Map<Integer, Integer> kind = new HashMap<>();
    private final Map<Integer, SimpleContainerData> furnaces = new HashMap<>();
    private boolean packing;
    /** Malformed saved inventories are retained byte-for-byte; packing is blocked. */
    private ListTag quarantinedModules;

    public FirstShipEntity(EntityType<? extends FirstShipEntity> type, Level world) {
        super(type, world);
        setVariant(Type.SPRUCE);
        setInvulnerable(true);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SURFACE, NO_SURFACE);
        builder.define(MODULES, "0".repeat(CELLS * 2));
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

    private InteractionResult packWithKey(Player player) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer) || quarantinedModules != null || packing || isRemoved() || isVehicle()
                || hasWalkers() || getDeltaMovement().horizontalDistanceSqr() > .0005D) {
            player.displayClientMessage(Component.literal("Судно должно стоять без экипажа и пассажиров"), true);
            return InteractionResult.FAIL;
        }
        ItemStack stack = packed();
        packing = true;
        discard(); // The vessel leaves the world before the returned item is granted.
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
        FirstShipHull.Point wheel = FirstShipHull.toWorld(0D, FirstShipHull.HELM_Z,
                getX(), getZ(), getYRot());
        Vec3 aim = new Vec3(wheel.x(), deckY() + 1.50D, wheel.z()).subtract(player.getEyePosition());
        if (aim.lengthSqr() < .09D || aim.lengthSqr() > 25D
                || aim.normalize().dot(player.getLookAngle()) < .76D) return false;
        return player.startRiding(this);
    }

    /**
     * A single explicit boarding step from water to the open deck.
     * Once on deck the player is NOT mounted or moved between fake seats.
     */
    public boolean tryBoard(ServerPlayer player) {
        if (isVehicle() || player.isPassenger() || player.isSpectator()
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
            if (!level().isClientSide) player.startRiding(this);
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

    @Override public void tick() {
        boolean water = afloat();
        double surface = entityData.get(SURFACE);
        double oldX = getX(), oldZ = getZ();
        float oldYaw = getYRot();
        boolean blocked = false;
        if (water) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(velocity.x, 0D, velocity.z);
            setPos(getX(), surface - DRAFT, getZ());
        }
        super.tick();
        if (!level().isClientSide) tickShipFurnaces();
        if (!level().isClientSide && water &&
                ((getX() - oldX) * (getX() - oldX) + (getZ() - oldZ) * (getZ() - oldZ) > .000001D
                        || Math.abs(net.minecraft.util.Mth.wrapDegrees(getYRot() - oldYaw)) > .05F) &&
                !FirstShipNavigation.clear(level(), getX(), getZ(), surface, getYRot())) {
            // Ship hull is much larger than native boat hitbox. Never allow a
            // 7x11 visible bow or the lower hold to enter solid terrain.
            setPos(oldX, surface - DRAFT, oldZ);
            setYRot(oldYaw);
            setDeltaMovement(Vec3.ZERO);
            blocked = true;
        }
        if (water && afloat()) {
            Vec3 velocity = getDeltaMovement();
            if (isVehicle() && !blocked) {
                RaftWind.Sample wind = RaftWind.at(level().getGameTime(), getX(), getZ(),
                        level().isRaining(), level().isThundering());
                double thrust = RaftWind.thrust(wind, getYRot(), 1) * .72D;
                double heading = Math.toRadians(getYRot());
                double vx = velocity.x - Math.sin(heading) * thrust;
                double vz = velocity.z + Math.cos(heading) * thrust;
                double speed = Math.hypot(vx, vz);
                if (speed > .37D) { vx *= .37D / speed; vz *= .37D / speed; }
                velocity = new Vec3(vx, 0D, vz);
            }
            setDeltaMovement(velocity.x, 0D, velocity.z);
            setPos(getX(), surface - DRAFT, getZ());
            fallDistance = 0F;
        } else if (!level().isClientSide && !isInWaterOrBubble()) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(0D, velocity.y, 0D);
        }
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("FirstShipSurface", entityData.get(SURFACE));
        tag.put("FirstShipModules", quarantinedModules != null ? quarantinedModules.copy() : storeModules(registryAccess()));
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
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
