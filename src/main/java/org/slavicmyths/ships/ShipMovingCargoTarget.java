package org.slavicmyths.ships;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Raycast against the ship's DETACHED local palette. Vanilla level raycasts
 * cannot hit these blocks because the physical chunks are empty while sailing.
 * Both client selection and the server's reach/line-of-sight validation use
 * this identical local-space intersection test.
 */
public final class ShipMovingCargoTarget {
    private ShipMovingCargoTarget() {}

    public record Hit(BlockPos cell, Direction face) {}

    public static BlockPos target(FirstShipEntity ship, Vec3 worldEye,
            Vec3 worldEnd, Map<BlockPos, BlockState> palette) {
        Hit hit = hit(ship, worldEye, worldEnd, palette);
        return hit == null ? null : hit.cell().relative(hit.face());
    }

    public static Hit hit(FirstShipEntity ship, Vec3 worldEye,
            Vec3 worldEnd, Map<BlockPos, BlockState> palette) {
        if (ship == null || worldEye == null || worldEnd == null
                || palette == null || palette.isEmpty()
                || worldEye.distanceToSqr(worldEnd) > 36.01D) return null;
        FirstShipHull.Point start = FirstShipHull.toLocal(worldEye.x, worldEye.z,
                ship.getX(), ship.getZ(), ship.shipYaw());
        FirstShipHull.Point finish = FirstShipHull.toLocal(worldEnd.x, worldEnd.z,
                ship.getX(), ship.getZ(), ship.shipYaw());
        Vec3 a = new Vec3(start.x(), worldEye.y - ship.getY(), start.z());
        Vec3 b = new Vec3(finish.x(), worldEnd.y - ship.getY(), finish.z());
        int minX = (int)Math.floor(Math.min(a.x, b.x) - 1D);
        int maxX = (int)Math.ceil(Math.max(a.x, b.x) + 1D);
        int minY = (int)Math.floor(Math.min(a.y, b.y) - 1D);
        int maxY = (int)Math.ceil(Math.max(a.y, b.y) + 1D);
        int minZ = (int)Math.floor(Math.min(a.z, b.z) - 1D);
        int maxZ = (int)Math.ceil(Math.max(a.z, b.z) + 1D);
        double nearest = Double.POSITIVE_INFINITY;
        Hit selected = null;
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos cell = new BlockPos(x, y, z);
                    BlockState state = palette.get(cell);
                    if (state == null || state.isAir()) continue;
                    var hit = new AABB(x - .5D, y, z - .5D,
                            x + .5D, y + 1D, z + .5D).clip(a, b);
                    if (hit.isEmpty()) continue;
                    Vec3 point = hit.get();
                    double distance = point.distanceToSqr(a);
                    if (distance >= nearest) continue;
                    double faceDistance = Math.abs(point.x - (x - .5D));
                    Direction face = Direction.WEST;
                    double d = Math.abs(point.x - (x + .5D));
                    if (d < faceDistance) { faceDistance = d; face = Direction.EAST; }
                    d = Math.abs(point.y - y);
                    if (d < faceDistance) { faceDistance = d; face = Direction.DOWN; }
                    d = Math.abs(point.y - (y + 1D));
                    if (d < faceDistance) { faceDistance = d; face = Direction.UP; }
                    d = Math.abs(point.z - (z - .5D));
                    if (d < faceDistance) { faceDistance = d; face = Direction.NORTH; }
                    d = Math.abs(point.z - (z + .5D));
                    if (d < faceDistance) face = Direction.SOUTH;
                    nearest = distance;
                    selected = new Hit(cell, face);
                }
        return selected;
    }
}
