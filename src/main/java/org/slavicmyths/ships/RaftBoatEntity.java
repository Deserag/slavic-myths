package org.slavicmyths.ships;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
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
    private final Map<Integer, SimpleContainer> inventories = new HashMap<>();
    private final Map<Integer, Integer> kinds = new HashMap<>();

    public RaftBoatEntity(EntityType<? extends RaftBoatEntity> type, Level level) {
        super(type, level);
        setVariant(Boat.Type.OAK);
        setInvulnerable(true); // Regular punches, damage and collisions never dismantle the hull.
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MODULES, 0);
    }

    @Override protected int getMaxPassengers() { return 4; }

    /** Four non-overlapping temporary test seats; not free movement on deck. */
    @Override protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity passenger,
            net.minecraft.world.entity.EntityDimensions dimensions, float partialTick) {
        int index = Math.max(0, getPassengers().indexOf(passenger));
        return new Vec3((index % 2 == 0 ? -0.72D : 0.72D), 0.34D, (index < 2 ? -0.72D : 0.72D));
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
        if (!player.isSecondaryUseActive()) return super.interact(player, hand);
        ItemStack held = player.getItemInHand(hand);
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
        if (!level().isClientSide && !isInWaterOrBubble()) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(0, velocity.y, 0);
        }
        super.tick();
        if (!level().isClientSide && !isInWaterOrBubble()) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(0, velocity.y, 0);
        }
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag modules = new ListTag();
        HolderLookup.Provider provider = registryAccess();
        for (var entry : inventories.entrySet()) {
            CompoundTag module = new CompoundTag();
            module.putInt("Tile", entry.getKey());
            module.putInt("Kind", kinds.getOrDefault(entry.getKey(), 0));
            module.put("Items", entry.getValue().createTag(provider));
            modules.add(module);
        }
        tag.put("Raft2511Modules", modules);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
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
}
