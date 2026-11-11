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
    public enum TravelMode { WATER, AIR }
    private final FirstShipBlockHull.MotionSnapshot snapshot;
    private final ShipBuildConfiguration modular;
    private final int firstTurn;
    private final ListTag cargo;
    private final Map<BlockPos, BlockState> palette;
    private final int waterlineY;
    private final TravelMode mode;
    private final ShipRigidMotion motion;
    private boolean detached;
    private boolean parked;

    private ShipMovingContraption(FirstShipBlockHull.MotionSnapshot snapshot,
            int surfaceY, ShipRigidMotion motion, boolean detached) {
        this.snapshot = snapshot;
        this.modular = null;
        this.firstTurn = snapshot.turn();
        this.cargo = snapshot.cargo();
        this.waterlineY = surfaceY;
        this.mode = TravelMode.WATER;
        this.motion = motion;
        this.palette = FirstShipBlockHull.localMotionPalette(snapshot);
        this.detached = detached;
    }

    private ShipMovingContraption(ShipBuildConfiguration modular, ListTag cargo,
            int turns, int surfaceY, ShipRigidMotion motion, boolean detached) {
        this.snapshot = null;
        this.modular = modular;
        this.firstTurn = Math.floorMod(turns, 4);
        this.cargo = cargo.copy();
        this.waterlineY = surfaceY;
        this.mode = TravelMode.WATER;
        this.motion = motion;
        this.palette = ShipModularBlockHull.localMotionPalette(modular, cargo);
        if (this.palette.isEmpty()) throw new IllegalArgumentException("Invalid modular palette");
        this.detached = detached;
    }

    public static ShipMovingContraption prepare(ServerLevel level, BlockPos anchor,
            int turns, ShipBuildConfiguration configuration) {
        ListTag cargo = ShipModularBlockHull.pack(level, anchor, turns, configuration);
        if (cargo == null) return null;
        double radius = Math.hypot(configuration.width() / 2D + 1D,
                configuration.length() / 2D + 1D);
        ShipRigidMotion motion = new ShipRigidMotion(new ShipRigidMotion.Pose(
                anchor.getX() + .5D, anchor.getZ() + .5D, turns * 90D), radius);
        return new ShipMovingContraption(configuration, cargo, turns,
                anchor.getY(), motion, false);
    }

    public ShipBuildConfiguration modularConfiguration() { return modular; }
    public ListTag cargo() { return cargo.copy(); }

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
        if (modular != null) {
            ListTag actual = ShipModularBlockHull.pack(level, original, firstTurn, modular);
            if (actual == null || !actual.equals(cargo)
                    || !ShipModularBlockHull.remove(level, original, firstTurn, modular))
                return false;
        } else if (!FirstShipBlockHull.detachForMotion(level, original, snapshot)) return false;
        detached = true;
        return true;
    }

    public ShipRigidMotion.Frame tick(ServerLevel world, ShipRigidMotion.Control helm) {
        if (!detached || parked) throw new IllegalStateException("Cannot move an attached hull");
        if (mode != TravelMode.WATER)
            throw new IllegalStateException("Air travel requires a three-dimensional controller");
        ShipRigidMotion.Frame frame = motion.advance(helm,
                pose -> ShipMovingClearance.clearPose(world, pose, waterlineY, palette));
        if (!frame.blocked() && !frame.before().equals(frame.after()))
            ShipMovingClearance.clearSoftObstacles(world, frame.after(), waterlineY, palette);
        return frame;
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
        if (modular != null) {
            ListTag rotated = ShipModularBlockHull.rotateCargo(modular, cargo, firstTurn, turn);
            if (rotated == null || !ShipModularBlockHull.place(world, target, turn, modular, rotated))
                return false;
            cargo.clear();
            cargo.addAll(rotated);
        } else if (!FirstShipBlockHull.restoreFromMotion(world, target, turn, snapshot())) return false;
        parked = true;
        detached = false;
        return true;
    }

    public ShipRigidMotion.Pose pose() { return motion.pose(); }
    public TravelMode mode() { return mode; }
    public ShipRigidMotion.SpatialPose spatialPose() {
        ShipRigidMotion.Pose current = motion.pose();
        return new ShipRigidMotion.SpatialPose(current.x(), waterlineY,
                current.z(), current.yaw(), 0D, 0D);
    }
    public ShipRigidMotion motion() { return motion; }
    public FirstShipBlockHull.MotionSnapshot snapshot() {
        if (snapshot == null) return null;
        return new FirstShipBlockHull.MotionSnapshot(cargo, snapshot.geometry(),
                snapshot.rig(), snapshot.fabric(), snapshot.turn());
    }
    public int cargoCount() { return cargo.size(); }
    public CompoundTag cargoEntry(int index) {
        return index < 0 || index >= cargo.size() ? null : cargo.getCompound(index).copy();
    }
    public boolean updateCargoEntry(int index, CompoundTag blockEntity) {
        if (index < 0 || index >= cargo.size() || blockEntity == null) return false;
        CompoundTag entry = cargo.getCompound(index).copy();
        if (!entry.contains("BlockEntity", Tag.TAG_COMPOUND)) return false;
        entry.put("BlockEntity", blockEntity.copy());
        cargo.set(index, entry);
        return true;
    }
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
        tag.putString("TravelMode", mode().name());
        tag.putInt("Waterline", waterlineY);
        tag.putInt("Geometry", snapshot == null ? 9 : snapshot.geometry());
        tag.putInt("Rig", snapshot == null ? 0 : snapshot.rig());
        tag.putInt("Fabric", snapshot == null ? 0 : snapshot.fabric());
        tag.putInt("OriginalTurn", firstTurn);
        if (modular != null) tag.put(ShipBuildConfiguration.NBT_KEY, modular.save());
        tag.put("Cargo", cargo.copy());
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
                || tag.contains("TravelMode", Tag.TAG_STRING)
                    && !TravelMode.WATER.name().equals(tag.getString("TravelMode"))
                || !tag.contains("Cargo", Tag.TAG_LIST)
                || !tag.contains("X", Tag.TAG_DOUBLE)
                || !tag.contains("Z", Tag.TAG_DOUBLE)
                || !tag.contains("Yaw", Tag.TAG_DOUBLE)) return null;
        try {
            ShipRigidMotion.Pose pose = new ShipRigidMotion.Pose(
                    tag.getDouble("X"), tag.getDouble("Z"), tag.getDouble("Yaw"));
            boolean detached = tag.getBoolean("Detached") && !tag.getBoolean("Parked");
            if (tag.contains(ShipBuildConfiguration.NBT_KEY, Tag.TAG_COMPOUND)) {
                ShipBuildConfiguration config = ShipBuildConfiguration.load(
                        tag.getCompound(ShipBuildConfiguration.NBT_KEY));
                double radius = Math.hypot(config.width() / 2D + 1D,
                        config.length() / 2D + 1D);
                ShipRigidMotion modularMotion = new ShipRigidMotion(pose, radius);
                modularMotion.restore(pose, tag.getDouble("Speed"), tag.getDouble("TurnSpeed"));
                return new ShipMovingContraption(config, tag.getList("Cargo", Tag.TAG_COMPOUND),
                        tag.getInt("OriginalTurn"), tag.getInt("Waterline"), modularMotion, detached);
            }
            ShipRigidMotion motion = new ShipRigidMotion(pose);
            motion.restore(pose, tag.getDouble("Speed"), tag.getDouble("TurnSpeed"));
            FirstShipBlockHull.MotionSnapshot snapshot = new FirstShipBlockHull.MotionSnapshot(
                    tag.getList("Cargo", Tag.TAG_COMPOUND), tag.getInt("Geometry"),
                    tag.getInt("Rig"), tag.getInt("Fabric"), tag.getInt("OriginalTurn"));
            return new ShipMovingContraption(snapshot, tag.getInt("Waterline"), motion, detached);
        } catch (RuntimeException invalid) {
            return null;
        }
    }
}
