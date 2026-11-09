package org.slavicmyths.ships;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server state of a DETACHED, moving rigid ship. This class is intentionally
 * independent from FirstShipEntity so the existing native ship is never
 * silently converted into a half-implemented moving mode.
 *
 * The caller must persist save() with its owning entity before detaching the
 * source blocks. The saved cargo never lives inside the world's chunks while
 * sailing. Unloaded chunks are treated as solid, not teleported through.
 */
public final class ShipMovingContraption {
    private FirstShipBlockHull.MotionSnapshot snapshot;
    /** Item inventories are materialized only for a chest/barrel actually opened. */
    private final Map<String, SimpleContainer> openCargo = new HashMap<>();
    private final Set<UUID> cargoViewers = new HashSet<>();
    private final Map<BlockPos, BlockState> palette;
    private final int waterlineY;
    private final ShipRigidMotion motion;
    private boolean detached;
    private boolean parked;

    private ShipMovingContraption(FirstShipBlockHull.MotionSnapshot snapshot,
            int surfaceY, ShipRigidMotion motion, boolean detached) {
        this.snapshot = snapshot;
        this.waterlineY = surfaceY;
        this.motion = motion;
        this.palette = FirstShipBlockHull.localMotionPalette(snapshot);
        this.detached = detached;
    }

    /** Validate but DO NOT remove the real ship. */
    public static ShipMovingContraption prepare(ServerLevel level, BlockPos anchor,
            int turns, int rig, int fabric, int geometry) {
        FirstShipBlockHull.MotionSnapshot source =
                FirstShipBlockHull.captureForMotion(level, anchor, turns, rig, fabric, geometry);
        if (source == null) return null;
        ShipRigidMotion motion = new ShipRigidMotion(new ShipRigidMotion.Pose(
                anchor.getX() + .5D, anchor.getZ() + .5D, turns * 90D));
        return new ShipMovingContraption(source, anchor.getY(), motion, false);
    }

    /** Stage 2 of assemble: the caller must already own a durable checkpoint. */
    public boolean detach(ServerLevel level, BlockPos original) {
        if (detached || parked) return false;
        if (Math.abs(motion.pose().x() - (original.getX() + .5D)) > 1E-4D
                || Math.abs(motion.pose().z() - (original.getZ() + .5D)) > 1E-4D) return false;
        if (!FirstShipBlockHull.detachForMotion(level, original, snapshot)) return false;
        detached = true;
        return true;
    }

    public ShipRigidMotion.Frame tick(ServerLevel world, ShipRigidMotion.Control helm) {
        if (!detached || parked) throw new IllegalStateException("Cannot move an attached hull");
        return motion.advance(helm,
                pose -> ShipMovingClearance.clearPose(world, pose, waterlineY, palette));
    }

    /**
     * Restores ordinary physical blocks only at a safe integer anchor and a
     * cardinal heading. Between angles the ship remains a moving contraption;
     * it is never silently rounded ninety degrees while underway.
     */
    public boolean park(ServerLevel world) {
        if (!detached || parked || Math.abs(motion.speed()) > .006D
                || Math.abs(motion.angularVelocity()) > .12D) return false;
        ShipRigidMotion.Pose pose = motion.pose();
        int turn = Math.floorMod((int)Math.round(pose.yaw() / 90D), 4);
        if (Math.abs(ShipRigidMotion.wrapDegrees(pose.yaw() - turn * 90D)) > 1.25D)
            return false;
        BlockPos target = BlockPos.containing(pose.x(), waterlineY, pose.z());
        // Only while STOPPED: align at most half a block to Minecraft's
        // grid when reconstructing actual vanilla blocks. No block-snapping
        // is ever applied to an underway ship.
        if (Math.abs(pose.x() - (target.getX() + .5D)) > .495D
                || Math.abs(pose.z() - (target.getZ() + .5D)) > .495D) return false;
        if (!FirstShipBlockHull.restoreFromMotion(world, target, turn, snapshot)) return false;
        parked = true;
        detached = false;
        return true;
    }

    public void registerCargoViewer(ServerPlayer player) {
        cargoViewers.add(player.getUUID());
    }

    /** Prevent packing/parking while any remote player still has one of this
     * ship's detached inventories open. Distance-only checks can be evaded. */
    public boolean hasOpenCargoMenus(ServerLevel world) {
        for (ServerPlayer player : world.players())
            if (cargoViewers.contains(player.getUUID())
                    && player.containerMenu != player.inventoryMenu) return true;
        return false;
    }

    private static String cargoKey(CompoundTag entry) {
        return entry.getInt("X") + ":" + entry.getInt("Y") + ":" + entry.getInt("Z");
    }

    /**
     * A moving chest has no chunk BlockEntity; deserialize the original
     * inventory from the untouched BlockEntity NBT instead. Only chests
     * and barrels are live in this first phase; furnaces are parked-only.
     */
    public SimpleContainer cargoInventory(int x, int y, int z, HolderLookup.Provider registries) {
        if (!detached || parked) return null;
        ListTag entries = snapshot.cargo();
        for (int n = 0; n < entries.size(); n++) {
            CompoundTag entry = entries.getCompound(n);
            if (entry.getInt("X") != x || entry.getInt("Y") != y
                    || entry.getInt("Z") != z) continue;
            if (entry.getInt("Kind") != 1 && entry.getInt("Kind") != 2) return null;
            String key = cargoKey(entry);
            SimpleContainer existing = openCargo.get(key);
            if (existing != null) return existing;
            CompoundTag contents = entry.getCompound("BlockEntity");
            // Loot-table containers require vanilla block-entity generation
            // on landing; refusing a live menu avoids destroying pending loot.
            if (contents.contains("LootTable", Tag.TAG_STRING)) return null;
            SimpleContainer created = new SimpleContainer(27);
            created.fromTag(contents.getList("Items", Tag.TAG_COMPOUND), registries);
            openCargo.put(key, created);
            return created;
        }
        return null;
    }

    /**
     * Transfer live moving-chest changes back to the immutable NBT snapshot
     * before ship save, journal checkpoint, or conversion to an item.
     * Never replace unrelated data: custom names, locks and mod metadata stay.
     */
    public void synchronizeCargo(HolderLookup.Provider registries) {
        if (openCargo.isEmpty()) return;
        ListTag entries = snapshot.cargo();
        for (int n = 0; n < entries.size(); n++) {
            CompoundTag entry = entries.getCompound(n);
            SimpleContainer inventory = openCargo.get(cargoKey(entry));
            if (inventory == null) continue;
            CompoundTag contents = entry.getCompound("BlockEntity").copy();
            contents.put("Items", inventory.createTag(registries));
            entry.put("BlockEntity", contents);
        }
        snapshot = new FirstShipBlockHull.MotionSnapshot(entries,
                snapshot.geometry(), snapshot.rig(), snapshot.fabric(), snapshot.turn());
    }

    public ShipRigidMotion.Pose pose() { return motion.pose(); }
    public ShipRigidMotion motion() { return motion; }
    public FirstShipBlockHull.MotionSnapshot snapshot() { return snapshot; }
    public Map<BlockPos, BlockState> palette() { return palette; }
    public int waterlineY() { return waterlineY; }
    public boolean detached() { return detached; }
    public boolean parked() { return parked; }

    /**
     * Entity-save payload; all numerical motion state and every container NBT
     * field survive reload. A missing/malformed payload is rejected rather than
     * silently manufacturing an empty vessel.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.putInt("Waterline", waterlineY);
        tag.putInt("Geometry", snapshot.geometry());
        tag.putInt("Rig", snapshot.rig());
        tag.putInt("Fabric", snapshot.fabric());
        tag.putInt("OriginalTurn", snapshot.turn());
        tag.put("Cargo", snapshot.cargo());
        tag.putDouble("X", motion.pose().x());
        tag.putDouble("Z", motion.pose().z());
        tag.putDouble("Yaw", motion.pose().yaw());
        tag.putDouble("Speed", motion.speed());
        tag.putDouble("TurnSpeed", motion.angularVelocity());
        tag.putBoolean("Detached", detached);
        tag.putBoolean("Parked", parked);
        return tag;
    }

    public static ShipMovingContraption load(CompoundTag tag) {
        if (tag == null || tag.getInt("Version") != 1
                || !tag.contains("Cargo", Tag.TAG_LIST)
                || !tag.contains("X", Tag.TAG_DOUBLE)
                || !tag.contains("Z", Tag.TAG_DOUBLE)
                || !tag.contains("Yaw", Tag.TAG_DOUBLE)) return null;
        try {
            FirstShipBlockHull.MotionSnapshot snapshot = new FirstShipBlockHull.MotionSnapshot(
                    tag.getList("Cargo", Tag.TAG_COMPOUND), tag.getInt("Geometry"),
                    tag.getInt("Rig"), tag.getInt("Fabric"), tag.getInt("OriginalTurn"));
            ShipRigidMotion motion = new ShipRigidMotion(new ShipRigidMotion.Pose(
                    tag.getDouble("X"), tag.getDouble("Z"), tag.getDouble("Yaw")));
            motion.restore(motion.pose(), tag.getDouble("Speed"), tag.getDouble("TurnSpeed"));
            return new ShipMovingContraption(snapshot, tag.getInt("Waterline"), motion,
                    tag.getBoolean("Detached") && !tag.getBoolean("Parked"));
        } catch (RuntimeException invalid) {
            return null;
        }
    }
}
