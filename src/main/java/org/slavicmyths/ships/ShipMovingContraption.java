package org.slavicmyths.ships;

import java.util.Map;
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
    private final FirstShipBlockHull.MotionSnapshot snapshot;
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
        if (Math.abs(pose.x() - (target.getX() + .5D)) > .20D
                || Math.abs(pose.z() - (target.getZ() + .5D)) > .20D) return false;
        if (!FirstShipBlockHull.restoreFromMotion(world, target, turn, snapshot)) return false;
        parked = true;
        detached = false;
        return true;
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
