package org.slavicmyths.ships;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * First real NeoForge water raft test. Native boat motion and one controllable rower.
 * Four passengers are temporary vanilla seats: free walking on the moving deck
 * still needs a moving-block physics prototype (Sable or an independently proven adapter).
 * Never delete/recreate chest world blocks on motion: modules live in this entity's NBT.
 */
public final class RaftBoatEntity extends Boat {
    private static final EntityDataAccessor<Integer> MODULES = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SURFACE_Y = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.FLOAT);
    private static final float UNKNOWN_SURFACE = -10000F;
    private static final double FLOATING_DRAFT = 0.14D;
    private static final EntityDataAccessor<Integer> RAFT_WIDTH = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RAFT_LENGTH = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SAIL_COUNT = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOD_FLAGS = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> MODULE_GRID = SynchedEntityData.defineId(RaftBoatEntity.class, EntityDataSerializers.STRING);
    private static final int RAILS = 1, HELM = 2, OARS = 4;
    private final Map<Integer, SimpleContainer> inventories = new HashMap<>();
    private final Map<Integer, Integer> kinds = new HashMap<>();
    private final Map<Integer, SimpleContainerData> smelting = new HashMap<>();
    private boolean packing;

    public RaftBoatEntity(EntityType<? extends RaftBoatEntity> type, Level level) {
        super(type, level);
        setVariant(Boat.Type.OAK);
        setInvulnerable(true); // Regular punches, damage and collisions never dismantle the hull.
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MODULES, 0);
        builder.define(SURFACE_Y, UNKNOWN_SURFACE);
        builder.define(RAFT_WIDTH, 3);
        builder.define(RAFT_LENGTH, 3);
        builder.define(SAIL_COUNT, 0);
        builder.define(MOD_FLAGS, 0);
        builder.define(MODULE_GRID, "0".repeat(35));
    }

    /** Actual visual upper deck at entity height + 0.50 blocks, used for moving support. */
    public double deckTop() { return getY() + 0.5D; }
    public int deckWidth() { return entityData.get(RAFT_WIDTH); }
    public int deckLength() { return entityData.get(RAFT_LENGTH); }
    public int sailCount() { return entityData.get(SAIL_COUNT); }
    public boolean hasRails() { return (entityData.get(MOD_FLAGS) & RAILS) != 0; }
    public boolean hasHelm() { return (entityData.get(MOD_FLAGS) & HELM) != 0; }
    public boolean hasExtraOars() { return (entityData.get(MOD_FLAGS) & OARS) != 0; }
    public RaftSize raftSize() { return RaftSize.of(deckWidth(), deckLength()); }
    public void configure(RaftSize size) {
        entityData.set(RAFT_WIDTH, size.width);
        entityData.set(RAFT_LENGTH, size.length);
        syncKinds();
    }

    /*
     * IMPORTANT: Boat collision dimensions do not equal our visible raft.
     * Expanding the boat's collision square to 7x7 made water checks and player
     * push response fight the deck carrier each tick, causing violent jitter.
     * Keep native hull dimensions; deck walking uses a separate rectangle.
     */
    @Override public boolean canCollideWith(net.minecraft.world.entity.Entity other) {
        return !(other instanceof Player) && super.canCollideWith(other);
    }

    @Override protected int getMaxPassengers() { return 4; }

    /** Four non-overlapping temporary test seats; not free movement on deck. */
    @Override protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity passenger,
            net.minecraft.world.entity.EntityDimensions dimensions, float partialTick) {
        int index = Math.max(0, getPassengers().indexOf(passenger));
        return new Vec3((index % 2 == 0 ? -0.72D : 0.72D), 0.58D, (index < 2 ? -0.72D : 0.72D));
    }

    /** World water surface, not the block bottom. Set at placement and synced to clients. */
    public void setWaterSurface(double surface) {
        if (Double.isFinite(surface) && surface > level().getMinBuildHeight() && surface < level().getMaxBuildHeight() + 1) {
            entityData.set(SURFACE_Y, (float)surface);
        }
    }

    /**
     * Ordinary attacks, including Creative attacks, never dismantle an occupied raft.
     * Only shipyard-key interaction can safely turn it into a packed inventory item.
     */
    @Override public boolean hurt(DamageSource source, float amount) { return false; }

    private ListTag modulesForSaving() {
        ListTag modules = new ListTag();
        HolderLookup.Provider provider = registryAccess();
        for (var entry : inventories.entrySet()) {
            CompoundTag module = new CompoundTag();
            module.putInt("Tile", entry.getKey());
            module.putInt("Kind", kinds.getOrDefault(entry.getKey(), 0));
            module.put("Items", entry.getValue().createTag(provider));
            if (smelting.containsKey(entry.getKey())) {
                SimpleContainerData data = smelting.get(entry.getKey());
                module.putIntArray("FurnaceTicks", new int[]{data.get(0), data.get(1), data.get(2), data.get(3)});
            }
            modules.add(module);
        }
        return modules;
    }

    private void unpackModules(CompoundTag tag) {
        inventories.clear();
        kinds.clear();
        smelting.clear();
        ListTag modules = tag.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < modules.size() && i < deckWidth() * deckLength(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile"), kind = module.getInt("Kind");
            if (tile < 0 || tile >= deckWidth() * deckLength() || (kind < 1 || kind > 4) || kinds.containsKey(tile)) continue;
            SimpleContainer inventory = new SimpleContainer(kind == 4 ? 3 : 27);
            inventory.fromTag(module.getList("Items", Tag.TAG_COMPOUND), registryAccess());
            kinds.put(tile, kind);
            inventories.put(tile, inventory);
            if (kind == 4) {
                SimpleContainerData data = new SimpleContainerData(4);
                int[] ticks = module.getIntArray("FurnaceTicks");
                for (int j = 0; j < Math.min(4, ticks.length); j++) data.set(j, Math.max(0, ticks[j]));
                smelting.put(tile, data);
            }
        }
        syncKinds();
    }

    /** Pack only local equipment state; do not clone a world-positioned entity. */
    private ItemStack packedItem() {
        ItemStack result = new ItemStack(RaftContent.raftItem(raftSize()));
        CompoundTag packed = new CompoundTag();
        packed.putInt("Version", 2);
        packed.putInt("RaftWidth", deckWidth());
        packed.putInt("RaftLength", deckLength());
        packed.putInt("RaftSails", sailCount());
        packed.putInt("RaftFlags", entityData.get(MOD_FLAGS));
        packed.putUUID("VesselId", getUUID());
        packed.put("Raft2511Modules", modulesForSaving());
        CompoundTag custom = new CompoundTag();
        custom.put("Raft2511Pack", packed);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        return result;
    }

    /** Called by RaftItem after validation; restores real ItemStack inventories. */
    public boolean loadPackedItem(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return true;
        CompoundTag root = custom.copyTag();
        if (!root.contains("Raft2511Pack", Tag.TAG_COMPOUND)) return true;
        CompoundTag packed = root.getCompound("Raft2511Pack");
        int version = packed.getInt("Version");
        if ((version != 1 && version != 2) || !packed.contains("Raft2511Modules", Tag.TAG_LIST)) return false;
        // Version 1 was used only by the original 3×3 raft. Do not quietly
        // reinterpret an old nine-cell cargo layout as a large hull.
        if (version == 1 && raftSize() != RaftSize.SMALL) return false;
        if (version == 2 && (packed.getInt("RaftWidth") != deckWidth() || packed.getInt("RaftLength") != deckLength())) return false;
        int packedSails = version == 2 ? packed.getInt("RaftSails") : 0;
        int packedFlags = version == 2 ? packed.getInt("RaftFlags") : 0;
        if (packedSails < 0 || packedSails > raftSize().maxSails() || (packedFlags & ~7) != 0) return false;
        ListTag modules = packed.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        if (modules.size() > deckWidth() * deckLength()) return false;
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile"), kind = module.getInt("Kind");
            if (tile < 0 || tile >= deckWidth() * deckLength() || (kind < 1 || kind > 4) || !seen.add(tile) ||
                    !module.contains("Items", Tag.TAG_LIST) ||
                    module.getList("Items", Tag.TAG_COMPOUND).size() > (kind == 4 ? 3 : 27)) return false;
        }
        // Only transfer once the whole payload passed structural validation.
        // Validate rigging before loading any inventory, never after a partial transfer.
        try { unpackModules(packed); }
        catch (RuntimeException invalid) { return false; }
        if (version == 2) {
            entityData.set(SAIL_COUNT, packedSails);
            entityData.set(MOD_FLAGS, packedFlags);
        }
        return true;
    }

    /** Server-authoritative one-time transfer: vessel -> packed item -> inventory. */
    private InteractionResult packWithKey(Player player) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer) || packing || isRemoved() || isVehicle()) return InteractionResult.FAIL;
        if (getDeltaMovement().horizontalDistanceSqr() > .001D) {
            player.displayClientMessage(Component.literal("Остановите плот перед упаковкой"), true);
            return InteractionResult.FAIL;
        }
        ItemStack packed = packedItem();
        packing = true;
        // Discard before handing the complete item to avoid repeated interaction duplication.
        discard();
        if (!player.getInventory().add(packed)) player.drop(packed, false);
        player.displayClientMessage(Component.literal("Плот упакован вместе со всем грузом"), true);
        return InteractionResult.CONSUME;
    }

    /** The vanilla Boat pick and drop items are vanilla wooden boats unless overridden. */
    @Override public ItemStack getPickResult() { return new ItemStack(RaftContent.raftItem(raftSize())); }
    @Override public Item getDropItem() { return RaftContent.raftItem(raftSize()); }
    @Override protected Component getTypeName() {
        return Component.translatableWithFallback("entity.slavicmyths.raft_2511_test", "Плот");
    }

    private boolean waterUnder(double surface) {
        BlockPos below = BlockPos.containing(getX(), surface - FLOATING_DRAFT, getZ());
        return level().hasChunkAt(below) && level().getFluidState(below).is(FluidTags.WATER);
    }

    /**
     * One-time waterline recovery for old submerged prototype entities.
     * Never load chunks; only ascend a continuous column of water.
     */
    private double recoverWaterSurface() {
        BlockPos origin = blockPosition();
        int start = Math.max(level().getMinBuildHeight(), origin.getY() - 1);
        int stop = Math.min(level().getMaxBuildHeight() - 1, start + 96);
        boolean foundWater = false;
        double waterline = Double.NaN;
        for (int y = start; y <= stop; y++) {
            BlockPos pos = new BlockPos(origin.getX(), y, origin.getZ());
            if (!level().hasChunkAt(pos)) return Double.NaN;
            var fluid = level().getFluidState(pos);
            if (!fluid.is(FluidTags.WATER)) {
                if (foundWater) return waterline;
                if (y > start + 1) return Double.NaN;
                continue;
            }
            foundWater = true;
            waterline = y + fluid.getHeight(level(), pos);
        }
        return Double.NaN;
    }

    public int moduleKind(int x, int z) {
        if (x < 0 || x >= deckWidth() || z < 0 || z >= deckLength()) return 0;
        int index = z * deckWidth() + x;
        String grid = entityData.get(MODULE_GRID);
        return index < grid.length() ? Math.max(0, grid.charAt(index) - '0') : 0;
    }

    private void syncKinds() {
        char[] grid = "0".repeat(35).toCharArray();
        int bits = 0;
        for (var entry : kinds.entrySet()) {
            int tile = entry.getKey(), kind = entry.getValue();
            if (tile >= 0 && tile < deckWidth() * deckLength()) {
                grid[tile] = (char)('0' + kind);
                if (deckWidth() == 3 && deckLength() == 3) bits |= (kind & 3) << (tile * 2);
            }
        }
        entityData.set(MODULE_GRID, new String(grid));
        entityData.set(MODULES, bits);
    }

    /** Ray to the deck surface, transformed into 3x3 raft-local tile coordinates. */
    private int localTile(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getLookAngle();
        double t = view.y < -0.025 ? (getY() + 0.50 - eye.y) / view.y : -1;
        Vec3 point = t > 0 && t < 6 ? eye.add(view.scale(t)) : player.position();
        double dx = point.x - getX(), dz = point.z - getZ(), yaw = Math.toRadians(getYRot());
        double localX = dx * Math.cos(yaw) + dz * Math.sin(yaw);
        double localZ = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
        int x = (int)Math.floor(localX + deckWidth() / 2.0), z = (int)Math.floor(localZ + deckLength() / 2.0);
        return x >= 0 && x < deckWidth() && z >= 0 && z < deckLength() ? z * deckWidth() + x : -1;
    }

    @Override public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(RaftContent.SHIPYARD_KEY.get())) return packWithKey(player);
        // Normal click ALWAYS boards for reliable steering on every raft size.
        // To walk without boarding, jump onto the deck directly; shift-click
        // is reserved for ship modules and inventory.
        if (!player.isSecondaryUseActive()) return super.interact(player, hand);
        if (held.is(RaftContent.RAIL_MODULE.get()) || held.is(RaftContent.SAIL_MODULE.get())
                || held.is(RaftContent.HELM_MODULE.get()) || held.is(RaftContent.OAR_MODULE.get())) {
            if (level().isClientSide) return InteractionResult.SUCCESS;
            int flags = entityData.get(MOD_FLAGS);
            if (held.is(RaftContent.RAIL_MODULE.get())) {
                if ((flags & RAILS) != 0) return InteractionResult.FAIL;
                entityData.set(MOD_FLAGS, flags | RAILS);
            } else if (held.is(RaftContent.HELM_MODULE.get())) {
                if ((flags & HELM) != 0) return InteractionResult.FAIL;
                entityData.set(MOD_FLAGS, flags | HELM);
            } else if (held.is(RaftContent.OAR_MODULE.get())) {
                if ((flags & OARS) != 0 || deckLength() <= 3) return InteractionResult.FAIL;
                entityData.set(MOD_FLAGS, flags | OARS);
            } else {
                if (sailCount() >= raftSize().maxSails()) return InteractionResult.FAIL;
                entityData.set(SAIL_COUNT, sailCount() + 1);
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.displayClientMessage(Component.literal("Оснащение плота установлено"), true);
            return InteractionResult.CONSUME;
        }
        int tile = localTile(player);
        if (tile < 0) return InteractionResult.FAIL;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (held.is(Items.CHEST) || held.is(Items.BARREL) || held.is(Items.CRAFTING_TABLE) || held.is(Items.FURNACE)) {
            if (kinds.containsKey(tile)) return InteractionResult.FAIL;
            int kind = held.is(Items.CHEST) ? 1 : held.is(Items.BARREL) ? 2 : held.is(Items.CRAFTING_TABLE) ? 3 : 4;
            kinds.put(tile, kind);
            inventories.put(tile, new SimpleContainer(kind == 4 ? 3 : 27));
            if (kind == 4) smelting.put(tile, new SimpleContainerData(4));
            syncKinds();
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.CONSUME;
        }
        SimpleContainer inventory = inventories.get(tile);
        if (inventory != null && player instanceof ServerPlayer serverPlayer) {
            int kind = kinds.get(tile);
            Component title = Component.literal(switch (kind) {
                case 1 -> "Судовой сундук";
                case 2 -> "Судовая бочка";
                case 3 -> "Судовой верстак";
                default -> "Судовая печь";
            });
            if (kind == 3) {
                serverPlayer.openMenu(new SimpleMenuProvider((windowId, playerInventory, p) ->
                        new net.minecraft.world.inventory.CraftingMenu(windowId, playerInventory,
                                net.minecraft.world.inventory.ContainerLevelAccess.create(level(), blockPosition())) {
                            @Override public boolean stillValid(Player guest) {
                                return !RaftBoatEntity.this.isRemoved() && guest.distanceToSqr(RaftBoatEntity.this) < 64.0D;
                            }
                        }, title));
            } else if (kind == 4) {
                serverPlayer.openMenu(new SimpleMenuProvider((windowId, playerInventory, p) ->
                        new FurnaceMenu(windowId, playerInventory, inventory, smelting.get(tile)), title));
            } else {
                serverPlayer.openMenu(new SimpleMenuProvider((windowId, playerInventory, p) ->
                        ChestMenu.threeRows(windowId, playerInventory, inventory), title));
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    private static int fuelTicks(ItemStack item) {
        if (item.is(Items.COAL) || item.is(Items.CHARCOAL)) return 1600;
        if (item.is(Items.BLAZE_ROD)) return 2400;
        if (item.is(ItemTags.LOGS)) return 300;
        if (item.is(ItemTags.PLANKS)) return 300;
        if (item.is(Items.STICK)) return 100;
        return 0; // Only tested non-container fuels; no lava-bucket loss.
    }

    /** Fixed-slot vanilla SMELTING recipe logic; recipes and ItemStack data stay with vessel. */
    private void tickShipFurnaces() {
        for (var entry : smelting.entrySet()) {
            SimpleContainer slots = inventories.get(entry.getKey());
            if (slots == null || slots.getContainerSize() != 3) continue;
            SimpleContainerData state = entry.getValue();
            int lit = Math.max(0, state.get(0) - 1);
            if (lit > 0) state.set(0, lit);
            else state.set(0, 0);
            ItemStack source = slots.getItem(0), fuel = slots.getItem(1), output = slots.getItem(2);
            if (source.isEmpty()) { state.set(2, 0); continue; }
            var found = level().getRecipeManager().getRecipeFor(
                    RecipeType.SMELTING, new SingleRecipeInput(source), level());
            if (found.isEmpty()) { state.set(2, 0); continue; }
            SmeltingRecipe recipe = found.get().value();
            ItemStack result = recipe.assemble(new SingleRecipeInput(source), registryAccess());
            if (result.isEmpty() || (!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, result) ||
                    output.getCount() + result.getCount() > output.getMaxStackSize()))) {
                state.set(2, 0);
                continue;
            }
            if (lit == 0 && !fuel.isEmpty()) {
                int burn = fuelTicks(fuel);
                if (burn > 0) {
                    fuel.shrink(1);
                    state.set(0, burn);
                    state.set(1, burn);
                    lit = burn;
                }
            }
            if (lit > 0) {
                int progress = state.get(2) + 1;
                int total = Math.max(1, recipe.getCookingTime());
                state.set(3, total);
                if (progress >= total) {
                    source.shrink(1);
                    if (output.isEmpty()) slots.setItem(2, result.copy());
                    else output.grow(result.getCount());
                    state.set(2, 0);
                    slots.setChanged();
                } else state.set(2, progress);
            } else state.set(2, 0);
        }
    }

    @Override public void tick() {
        // Boat's private buoyancy/status algorithm is calibrated to a narrow vanilla boat.
        // A 2.95-wide custom boat can fall to UNDER_WATER and never resurface by itself.
        // Lock only vertical position while directly over water, keeping vanilla paddle
        // steering, inertia, collision and server/client horizontal movement.
        if (!level().isClientSide && entityData.get(SURFACE_Y) == UNKNOWN_SURFACE) {
            double recovered = recoverWaterSurface();
            if (Double.isFinite(recovered)) setWaterSurface(recovered);
        }
        double surface = entityData.get(SURFACE_Y);
        boolean afloat = surface != UNKNOWN_SURFACE && waterUnder(surface);
        if (afloat) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(velocity.x, 0D, velocity.z);
            setPos(getX(), surface - FLOATING_DRAFT, getZ());
        } else if (!level().isClientSide && !isInWaterOrBubble()) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(0D, velocity.y, 0D);
        }
        super.tick();
        if (!level().isClientSide) tickShipFurnaces();
        if (afloat && waterUnder(surface)) {
            Vec3 velocity = getDeltaMovement();
            if (hasHelm() && sailCount() > 0 && isVehicle()) {
                // The sail catches a deterministic, weather-dependent WORLD wind.
                // Turning straight into a headwind now removes sail propulsion;
                // rowing still works and may combine with a favorable breeze.
                RaftWind.Sample breeze = RaftWind.at(level().getGameTime(), getX(), getZ(),
                        level().isRaining(), level().isThundering());
                double thrust = RaftWind.thrust(breeze, getYRot(), sailCount());
                double yaw = Math.toRadians(getYRot());
                double headingX = -Math.sin(yaw), headingZ = Math.cos(yaw);
                double vx = velocity.x + headingX * thrust, vz = velocity.z + headingZ * thrust;
                boolean rowing = getPaddleState(0) || getPaddleState(1);
                double max = RaftCharacteristics.ONE_SAIL_LIMIT + RaftCharacteristics.SECOND_SAIL_BONUS * (sailCount() - 1) + (rowing ? RaftCharacteristics.ROWING_BONUS : 0D);
                double horizontal = Math.sqrt(vx * vx + vz * vz);
                if (horizontal > max) { vx *= max / horizontal; vz *= max / horizontal; }
                velocity = new Vec3(vx, 0D, vz);
            }
            setDeltaMovement(velocity.x, 0D, velocity.z);
            setPos(getX(), surface - FLOATING_DRAFT, getZ());
            fallDistance = 0F;
        } else if (!level().isClientSide && !isInWaterOrBubble()) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(0D, velocity.y, 0D);
        }
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Raft2511Modules", modulesForSaving());
        tag.putFloat("Raft2511SurfaceY", entityData.get(SURFACE_Y));
        tag.putInt("RaftWidth", deckWidth());
        tag.putInt("RaftLength", deckLength());
        tag.putInt("RaftSails", sailCount());
        tag.putInt("RaftFlags", entityData.get(MOD_FLAGS));
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RaftWidth", Tag.TAG_INT) && tag.contains("RaftLength", Tag.TAG_INT)) {
            try { configure(RaftSize.of(tag.getInt("RaftWidth"), tag.getInt("RaftLength"))); }
            catch (IllegalArgumentException invalid) { configure(RaftSize.SMALL); }
        }
        unpackModules(tag);
        entityData.set(SAIL_COUNT, Math.min(raftSize().maxSails(), Math.max(0, tag.getInt("RaftSails"))));
        entityData.set(MOD_FLAGS, tag.getInt("RaftFlags") & (RAILS | HELM | OARS));
        if (tag.contains("Raft2511SurfaceY", Tag.TAG_FLOAT)) setWaterSurface(tag.getFloat("Raft2511SurfaceY"));
    }
}
