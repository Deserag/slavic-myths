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
        for (int i = 0; i < modules.size() && i < 9; i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile"), kind = module.getInt("Kind");
            if (tile < 0 || tile >= 9 || (kind != 1 && kind != 2) || kinds.containsKey(tile)) continue;
            SimpleContainer inventory = new SimpleContainer(27);
            inventory.fromTag(module.getList("Items", Tag.TAG_COMPOUND), registryAccess());
            kinds.put(tile, kind);
            inventories.put(tile, inventory);
        }
        syncKinds();
    }

    /** Pack only local equipment state; do not clone a world-positioned entity. */
    private ItemStack packedItem() {
        ItemStack result = new ItemStack(RaftContent.RAFT_ITEM.get());
        CompoundTag packed = new CompoundTag();
        packed.putInt("Version", 1);
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
        if (packed.getInt("Version") != 1 || !packed.contains("Raft2511Modules", Tag.TAG_LIST)) return false;
        unpackModules(packed);
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
    @Override public ItemStack getPickResult() { return new ItemStack(RaftContent.RAFT_ITEM.get()); }
    @Override public Item getDropItem() { return RaftContent.RAFT_ITEM.get(); }
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
        if (x < 0 || x >= 3 || z < 0 || z >= 3) return 0;
        return (entityData.get(MODULES) >>> ((z * 3 + x) * 2)) & 3;
    }

    private void syncKinds() {
        int bits = 0;
        for (var entry : kinds.entrySet()) bits |= (entry.getValue() & 3) << (entry.getKey() * 2);
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
        int x = (int)Math.floor(localX + 1.5), z = (int)Math.floor(localZ + 1.5);
        return x >= 0 && x < 3 && z >= 0 && z < 3 ? z * 3 + x : -1;
    }

    @Override public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(RaftContent.SHIPYARD_KEY.get())) return packWithKey(player);
        if (!player.isSecondaryUseActive()) return super.interact(player, hand);
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
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        unpackModules(tag);
        if (tag.contains("Raft2511SurfaceY", Tag.TAG_FLOAT)) setWaterSurface(tag.getFloat("Raft2511SurfaceY"));
    }
}
