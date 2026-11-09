package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Actual player-vs-moving-block collisions on both logical sides.
 *
 * Create-style contraptions need moving collision geometry in the entity
 * collision pipeline, not a visual block palette plus post-tick teleports.
 * The voxel boxes here are transformed with the same rigid hull pose.
 * Rotated boxes use conservative world-axis envelopes, so this is a first
 * collision layer, not a complete oriented-box continuous solver.
 */
public final class ShipDynamicCollision {
    private record Volume(AABB bounds, VoxelShape shape) {}
    private record Cache(double x, double y, double z, float yaw, String key,
                         List<Volume> volumes) {}
    private static final Map<FirstShipEntity, Cache> CACHED = new WeakHashMap<>();

    public static void collect(Entity entity, Level level, AABB region,
                               List<VoxelShape> collisions) {
        if (!(entity instanceof Player player) || !player.isAlive() || player.isSpectator()
                || player.getAbilities().flying || player.isFallFlying() || player.isPassenger()) return;
        for (FirstShipEntity ship : level.getEntitiesOfClass(FirstShipEntity.class,
                region.inflate(12D, 15D, 12D))) {
            if (ship.isRemoved() || !ship.isMoving()) continue;
            Cache cache = cache(ship, level);
            for (Volume volume : cache.volumes())
                if (volume.bounds().intersects(region)) collisions.add(volume.shape());
        }
    }

    private static Cache cache(FirstShipEntity ship, Level level) {
        String key = ship.geometryVersion() + ":" + ship.rigLevel() + ":"
                + ship.fabricQuality() + ":" + ship.movingCargoView();
        synchronized (CACHED) {
            Cache current = CACHED.get(ship);
            if (current != null && current.x() == ship.getX() && current.y() == ship.getY()
                    && current.z() == ship.getZ() && current.yaw() == ship.getYRot()
                    && current.key().equals(key)) return current;
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
                        // Ignore invalid cosmetic tokens, never invent cargo.
                    }
                }
                try {
                    palette = FirstShipBlockHull.localMotionPalette(
                            new FirstShipBlockHull.MotionSnapshot(cargo,
                                    ship.geometryVersion(), ship.rigLevel(),
                                    ship.fabricQuality(), 0));
                } catch (RuntimeException invalid) {
                    palette = Map.of();
                }
            }
            List<Volume> volumes = new ArrayList<>();
            BlockPos sample = ship.blockPosition();
            double cos = Math.cos(Math.toRadians(ship.getYRot()));
            double sin = Math.sin(Math.toRadians(ship.getYRot()));
            for (var entry : palette.entrySet()) {
                BlockState state = entry.getValue();
                if (state.isAir()) continue;
                VoxelShape shape = state.getCollisionShape(level, sample, CollisionContext.empty());
                if (shape.isEmpty()) continue;
                BlockPos p = entry.getKey();
                for (AABB local : shape.toAabbs()) {
                    double minX = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
                    double maxX = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
                    for (double x : new double[]{p.getX() + local.minX - .5D,
                            p.getX() + local.maxX - .5D})
                        for (double z : new double[]{p.getZ() + local.minZ - .5D,
                                p.getZ() + local.maxZ - .5D}) {
                            double wx = ship.getX() + x * cos - z * sin;
                            double wz = ship.getZ() + x * sin + z * cos;
                            minX = Math.min(minX, wx); maxX = Math.max(maxX, wx);
                            minZ = Math.min(minZ, wz); maxZ = Math.max(maxZ, wz);
                        }
                    AABB worldBox = new AABB(minX, ship.getY() + p.getY() + local.minY,
                            minZ, maxX, ship.getY() + p.getY() + local.maxY, maxZ);
                    volumes.add(new Volume(worldBox, Shapes.create(worldBox)));
                }
            }
            Cache updated = new Cache(ship.getX(), ship.getY(), ship.getZ(),
                    ship.getYRot(), key, List.copyOf(volumes));
            CACHED.put(ship, updated);
            return updated;
        }
    }

    private ShipDynamicCollision() {}
}
