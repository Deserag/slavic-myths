package org.slavicmyths.ships;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Actual player-vs-moving-block collisions on both logical sides.
 *
 * Create-style contraptions need moving collision geometry in the entity
 * collision pipeline, not a visual block palette plus post-tick teleports.
 * The voxel boxes here are transformed with the same rigid hull pose.
 * Local block shapes are indexed by cell. A player queries only nearby cells;
 * rotated boxes are subdivided before conversion to vanilla axis-aligned
 * shapes, reducing the invisible corners of a whole-block envelope.
 */
public final class ShipDynamicCollision {
    private record Geometry(String key, Map<BlockPos, List<AABB>> cells) {}
    private static final Map<FirstShipEntity, Geometry> GEOMETRY = new WeakHashMap<>();
    /** The carried player must collide with mast, ladder backing and rails too. */
    public static void carryWithWorldCollision(Player player, FirstShipEntity ship, Vec3 delta) {
        player.move(MoverType.SELF, delta);
    }

    /** Avoid allocating a copied vanilla collision list on unrelated entity moves. */
    public static boolean hasNearbyMovingShip(Entity entity, Level level, AABB region) {
        if (!(entity instanceof Player player) || !player.isAlive() || player.isSpectator()
                || player.getAbilities().flying || player.isFallFlying() || player.isPassenger())
            return false;
        for (FirstShipEntity ship : level.getEntitiesOfClass(FirstShipEntity.class,
                region.inflate(36D, 25D, 36D)))
            if (!ship.isRemoved() && ship.isMoving())
                return true;
        return false;
    }

    private static boolean shipLadder(FirstShipEntity ship, double x, double z, double y) {
        var config = ship.modularConfiguration();
        if (config == null) return FirstShipHull.onLadder(x, z)
                && y >= .9D && y <= 5.3D;
        if (Math.abs(x + 2D) < .49D && Math.abs(z - 1D) < .49D
                && y >= .9D && y <= 4D + (config.decks() - 1) * 4D + .9D) return true;
        if (config.bridge()) {
            int side = Math.min(3, config.width() / 2 - 2);
            double ladderX = -side + 1D, ladderZ = -config.length() / 2D + 6D;
            return Math.abs(x - ladderX) < .49D
                    && Math.abs(z - ladderZ) < .49D && y >= 4.9D && y <= 9.9D;
        }
        return false;
    }

    public static boolean onMovingLadder(Player player) {
        if (!player.isAlive() || player.isPassenger() || player.isSpectator()) return false;
        for (FirstShipEntity ship : player.level().getEntitiesOfClass(FirstShipEntity.class,
                player.getBoundingBox().inflate(36D, 25D, 36D))) {
            if (ship.isRemoved() || !ship.isMoving()) continue;
            FirstShipHull.Point local = FirstShipHull.toLocal(player.getX(), player.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            double y = player.getY() - ship.getY();
            if (shipLadder(ship, local.x(), local.z(), y))
                return true;
        }
        return false;
    }

    public static void collect(Entity entity, Level level, AABB region,
                               List<VoxelShape> collisions) {
        if (!(entity instanceof Player player) || !player.isAlive() || player.isSpectator()
                || player.getAbilities().flying || player.isFallFlying() || player.isPassenger()) return;
        for (FirstShipEntity ship : level.getEntitiesOfClass(FirstShipEntity.class,
                region.inflate(36D, 25D, 36D))) {
            if (ship.isRemoved() || !ship.isMoving()) continue;
            Geometry geometry = geometry(ship, level);
            AABB local = localBounds(region, ship);
            double angle = Math.toRadians(ship.getYRot());
            double cos = Math.cos(angle), sin = Math.sin(angle);
            double remainder = Math.abs(ship.getYRot()) % 90D;
            int subdivisions = remainder < .01D || remainder > 89.99D ? 1 : 4;
            for (int y = (int)Math.floor(local.minY) - 1;
                    y <= (int)Math.ceil(local.maxY) + 1; y++)
                for (int x = (int)Math.floor(local.minX) - 1;
                        x <= (int)Math.ceil(local.maxX) + 1; x++)
                    for (int z = (int)Math.floor(local.minZ) - 1;
                            z <= (int)Math.ceil(local.maxZ) + 1; z++) {
                        // The real hatch is absent from the palette. Do not
                        // discard its solid neighboring roof and backing cells.
                        List<AABB> shapes = geometry.cells().get(new BlockPos(x, y, z));
                        if (shapes == null) continue;
                        for (AABB shape : shapes) {
                            double x0 = x + shape.minX - .5D, x1 = x + shape.maxX - .5D;
                            double z0 = z + shape.minZ - .5D, z1 = z + shape.maxZ - .5D;
                            double y0 = ship.getY() + y + shape.minY;
                            double y1 = ship.getY() + y + shape.maxY;
                            if (x1 < local.minX || x0 > local.maxX
                                    || z1 < local.minZ || z0 > local.maxZ
                                    || y1 < region.minY || y0 > region.maxY) continue;
                            for (int sx = 0; sx < subdivisions; sx++)
                                for (int sz = 0; sz < subdivisions; sz++) {
                                    double ax = x0 + (x1 - x0) * sx / subdivisions;
                                    double bx = x0 + (x1 - x0) * (sx + 1) / subdivisions;
                                    double az = z0 + (z1 - z0) * sz / subdivisions;
                                    double bz = z0 + (z1 - z0) * (sz + 1) / subdivisions;
                                    AABB box = transformed(ship, ax, bx, az, bz,
                                            y0, y1, cos, sin);
                                    if (box.intersects(region)) collisions.add(Shapes.create(box));
                                }
                        }
                    }
        }
    }



    private static AABB localBounds(AABB world, FirstShipEntity ship) {
        double minX = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (double x : new double[]{world.minX, world.maxX})
            for (double z : new double[]{world.minZ, world.maxZ}) {
                FirstShipHull.Point p = FirstShipHull.toLocal(x, z,
                        ship.getX(), ship.getZ(), ship.getYRot());
                minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
                minZ = Math.min(minZ, p.z()); maxZ = Math.max(maxZ, p.z());
            }
        return new AABB(minX, world.minY - ship.getY(), minZ,
                maxX, world.maxY - ship.getY(), maxZ);
    }

    private static AABB transformed(FirstShipEntity ship, double x0, double x1,
            double z0, double z1, double y0, double y1, double cos, double sin) {
        double minX = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (double x : new double[]{x0, x1})
            for (double z : new double[]{z0, z1}) {
                double wx = ship.getX() + x * cos - z * sin;
                double wz = ship.getZ() + x * sin + z * cos;
                minX = Math.min(minX, wx); maxX = Math.max(maxX, wx);
                minZ = Math.min(minZ, wz); maxZ = Math.max(maxZ, wz);
            }
        return new AABB(minX, y0, minZ, maxX, y1, maxZ);
    }

    private static Geometry geometry(FirstShipEntity ship, Level level) {
        var configuration = ship.modularConfiguration();
        String key = ship.geometryVersion() + ":" + ship.rigLevel() + ":"
                + ship.fabricQuality() + ":" + ship.movingCargoView()
                + ":" + (configuration == null ? "" : configuration.wire());
        synchronized (GEOMETRY) {
            Geometry existing = GEOMETRY.get(ship);
            if (existing != null && existing.key().equals(key)) return existing;
            Map<BlockPos, BlockState> palette = ship.serverMovingPalette();
            if (palette == null) {
                ListTag cargo = new ListTag();
                String types = ship.movingCargoView();
                if (!types.isBlank()) for (String token : types.split(";")) {
                    String[] fields = token.split(",");
                    if (fields.length != 4) continue;
                    try {
                        CompoundTag entry = new CompoundTag();
                        entry.putInt("X", Integer.parseInt(fields[0]));
                        entry.putInt("Y", Integer.parseInt(fields[1]));
                        entry.putInt("Z", Integer.parseInt(fields[2]));
                        entry.putInt("Kind", Integer.parseInt(fields[3]));
                        entry.put("BlockEntity", new CompoundTag());
                        cargo.add(entry);
                    } catch (NumberFormatException invalid) {
                        // Ignore malformed cosmetic tokens.
                    }
                }
                try {
                    palette = configuration == null
                            ? FirstShipBlockHull.localMotionPalette(
                                new FirstShipBlockHull.MotionSnapshot(cargo,
                                        ship.geometryVersion(), ship.rigLevel(),
                                        ship.fabricQuality(), 0))
                            : ShipModularBlockHull.localMotionPalette(configuration, cargo);
                } catch (RuntimeException invalid) {
                    palette = Map.of();
                }
            }
            Map<BlockPos, List<AABB>> cells = new HashMap<>();
            BlockPos sample = ship.blockPosition();
            for (var entry : palette.entrySet()) {
                BlockState state = entry.getValue();
                if (state.isAir()) continue;
                List<AABB> boxes = state.getCollisionShape(level, sample,
                        CollisionContext.empty()).toAabbs();
                if (!boxes.isEmpty()) cells.put(entry.getKey(), List.copyOf(boxes));
            }
            Geometry updated = new Geometry(key, Map.copyOf(cells));
            GEOMETRY.put(ship, updated);
            return updated;
        }
    }

    private ShipDynamicCollision() {}
}
