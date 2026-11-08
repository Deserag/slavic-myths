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
        refreshDimensions();
    }

    // Use a broad square collision envelope for the first large-raft test.
    // A future dedicated physics hull will replace this with a rectangular ship-local deck.
    @Override public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        if (this.entityData == null) return super.getDimensions(pose);
        return net.minecraft.world.entity.EntityDimensions.scalable(
                Math.max(deckWidth(), deckLength()) - .05F, .65F);
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor.equals(RAFT_WIDTH) || accessor.equals(RAFT_LENGTH)) refreshDimensions();
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
            modules.add(module);
        }
        return modules;
    }

    private void unpackModules(CompoundTag tag) {
        inventories.clear();
        kinds.clear();
        ListTag modules = tag.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < modules.size() && i < deckWidth() * deckLength(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile"), kind = module.getInt("Kind");
            if (tile < 0 || tile >= deckWidth() * deckLength() || (kind != 1 && kind != 2 && kind != 3) || kinds.containsKey(tile)) continue;
            SimpleContainer inventory = new SimpleContainer(27);
            inventory.fromTag(module.getList("Items", Tag.TAG_COMPOUND), registryAccess());
            kinds.put(tile, kind);
            inventories.put(tile, inventory);
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
        if (version == 2 && (packed.getInt("RaftWidth") != deckWidth() || packed.getInt("RaftLength") != deckLength())) return false;
        ListTag modules = packed.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        if (modules.size() > deckWidth() * deckLength()) return false;
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile"), kind = module.getInt("Kind");
            if (tile < 0 || tile >= deckWidth() * deckLength() || (kind != 1 && kind != 2 && kind != 3) || !seen.add(tile) ||
                    !module.contains("Items", Tag.TAG_LIST) ||
                    module.getList("Items", Tag.TAG_COMPOUND).size() > 27) return false;
        }
        // Only transfer once the whole payload passed structural validation.
        try { unpackModules(packed); }
        catch (RuntimeException invalid) { return false; }
        if (version == 2) {
            int sails = packed.getInt("RaftSails");
            if (sails < 0 || sails > raftSize().maxSails()) return false;
            entityData.set(SAIL_COUNT, sails);
            entityData.set(MOD_FLAGS, packed.getInt("RaftFlags") & (RAILS | HELM | OARS));
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
        double t = view.y < -0.025 ? (getY() + 0.28 - eye.y) / view.y : -1;
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
        // Captain enters at the stern; ordinary visitors can walk on the physical deck.
        if (!player.isSecondaryUseActive()) {
            int targeted = localTile(player);
            int x = targeted < 0 ? -1 : targeted % deckWidth();
            int z = targeted < 0 ? -1 : targeted / deckWidth();
            if (x == deckWidth() / 2 && z >= deckLength() - 2) return super.interact(player, hand);
            return InteractionResult.PASS;
        }
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
        if (held.is(Items.CHEST) || held.is(Items.BARREL)) {
            if (kinds.containsKey(tile)) return InteractionResult.FAIL;
            int kind = held.is(Items.CHEST) ? 1 : 2;
            kinds.put(tile, kind);
            inventories.put(tile, new SimpleContainer(27));
            syncKinds();
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.CONSUME;
        }
        SimpleContainer inventory = inventories.get(tile);
        if (inventory != null && player instanceof ServerPlayer serverPlayer) {
            Component title = Component.literal(kinds.get(tile) == 1 ? "Судовой сундук" : "Судовая бочка");
            serverPlayer.openMenu(new SimpleMenuProvider((windowId, playerInventory, p) ->
                    ChestMenu.threeRows(windowId, playerInventory, inventory), title));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
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
        if (afloat && waterUnder(surface)) {
            Vec3 velocity = getDeltaMovement();
            if (hasHelm() && sailCount() > 0 && isVehicle()) {
                // Deterministic non-industrial breeze: stronger than rowing alone;
                // paddling remains additive, and a cargo raft can carry two sails.
                double gust = .78D + .22D * Math.sin(level().getGameTime() / 150.0D + getX() / 160.0D);
                double thrust = (.011D + .004D * (sailCount() - 1)) * gust;
                double yaw = Math.toRadians(getYRot());
                double windX = -Math.sin(yaw), windZ = Math.cos(yaw);
                double vx = velocity.x + windX * thrust, vz = velocity.z + windZ * thrust;
                double max = .35D + .09D * (sailCount() - 1);
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
