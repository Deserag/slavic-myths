package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Server-side daylight ocean encounters, independent of vanilla hostile mob
 * light-level rolls. Uses loaded chunks only and never force-loads terrain.
 * Shipwreck Pharaohka leaders remain exclusive to the shipwreck system.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class MarineTravelEncounters {
    private static final String NEXT = "SlavicMarineTravelNextAttempt";
    private static final String LAST_X = "SlavicMarineTravelLastX";
    private static final String LAST_Z = "SlavicMarineTravelLastZ";
    private static final int CHECK_INTERVAL = 100;
    private static final int SUCCESS_COOLDOWN = 1000;
    private static final int FAILED_COOLDOWN = 100;
    private static final int TRAVEL_DISTANCE = 120;
    private static final int NEARBY_BOSS_RADIUS = 192;

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || level.getDifficulty() == Difficulty.PEACEFUL
                || level.getGameTime() % CHECK_INTERVAL != 0) return;

        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            BlockPos here = player.blockPosition();
            int sea = level.getSeaLevel();
            if (!level.getBiome(new BlockPos(here.getX(), sea - 6, here.getZ()))
                    .is(BiomeTags.IS_OCEAN)) continue;

            CompoundTag data = player.getPersistentData();
            long now = level.getGameTime();
            if (now < data.getLong(NEXT)) continue;
            if (data.contains(LAST_X) && data.contains(LAST_Z)) {
                long dx = (long) here.getX() - data.getInt(LAST_X);
                long dz = (long) here.getZ() - data.getInt(LAST_Z);
                if (dx * dx + dz * dz < (long) TRAVEL_DISTANCE * TRAVEL_DISTANCE) continue;
            }

            if (hasBoss(level, here)) {
                data.putLong(NEXT, now + SUCCESS_COOLDOWN);
                continue;
            }

            if (spawnAround(level, here)) {
                data.putInt(LAST_X, here.getX());
                data.putInt(LAST_Z, here.getZ());
                data.putLong(NEXT, now + SUCCESS_COOLDOWN);
            } else {
                // A failed roll must not permanently suppress encounters.
                data.putLong(NEXT, now + FAILED_COOLDOWN);
            }
        }
    }

    private static boolean hasBoss(ServerLevel level, BlockPos center) {
        return !level.getEntitiesOfClass(Mob.class,
                new AABB(center.getX() - NEARBY_BOSS_RADIUS, level.getMinBuildHeight(),
                         center.getZ() - NEARBY_BOSS_RADIUS,
                         center.getX() + NEARBY_BOSS_RADIUS, level.getMaxBuildHeight(),
                         center.getZ() + NEARBY_BOSS_RADIUS),
                mob -> mob.getType() == DeepSeaSerpentContent.BOSS.get()
                    || mob.getType() == OceanZmeyContent.BOSS.get()
                    || mob.getType() == StratimBirdContent.BOSS.get()
                    || mob.getType() == TsarPikeContent.BOSS.get()
                    || mob.getType() == ZhelvaContent.BOSS.get()
                    || mob.getType() == PharaohkaContent.LEADER.get()).isEmpty();
    }

    private static boolean spawnAround(ServerLevel level, BlockPos origin) {
        int sea = level.getSeaLevel();
        int first = level.random.nextInt(5);
        for (int attempt = 0; attempt < 40; attempt++) {
            int x = origin.getX() + level.random.nextInt(145) - 72;
            int z = origin.getZ() + level.random.nextInt(145) - 72;
            long dx = (long) x - origin.getX();
            long dz = (long) z - origin.getZ();
            if (dx * dx + dz * dz < 32L * 32 || dx * dx + dz * dz > 72L * 72) continue;
            if (level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) continue;
            if (!level.getBiome(new BlockPos(x, sea - 6, z)).is(BiomeTags.IS_OCEAN)) continue;
            if (!water(level, new BlockPos(x, sea - 1, z))) continue;

            // Heightmap returns the Y immediately ABOVE the top of the sea floor.
            // A fixed 30-block scan misses deep oceans and could put Zhelva
            // in free water rather than on its required solid seabed.
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            int depth = sea - floor;
            if (depth < 8) continue;

            for (int step = 0; step < 5; step++) {
                int kind = (first + step) % 5;
                if ((kind == 0 && depth < 13)
                        || (kind == 1 && depth < 15)
                        || (kind == 4 && depth < 16)) continue;
                int y = switch (kind) {
                    case 2 -> sea + 3; // Stratim patrols just above the surface.
                    case 4 -> floor; // Zhelva sits on the actual ocean floor.
                    case 0, 1 -> sea - 5;
                    default -> sea - 3;
                };
                BlockPos pos = new BlockPos(x, y, z);
                if (!clear(level, pos, kind)) continue;
                Mob boss = switch (kind) {
                    case 0 -> DeepSeaSerpentContent.BOSS.get().create(level);
                    case 1 -> OceanZmeyContent.BOSS.get().create(level);
                    case 2 -> StratimBirdContent.BOSS.get().create(level);
                    case 3 -> TsarPikeContent.BOSS.get().create(level);
                    default -> ZhelvaContent.BOSS.get().create(level);
                };
                if (boss == null) continue;
                boss.moveTo(x + .5D, y + .15D, z + .5D, level.random.nextFloat() * 360F, 0F);
                if (!level.noCollision(boss)) continue;
                boss.setPersistenceRequired();
                if (level.addFreshEntity(boss)) {
                    System.getLogger("slavicmyths.marine").log(System.Logger.Level.INFO,
                        "Ocean encounter spawned {0} at {1}, {2}, {3}",
                        boss.getType().toString(), x, y, z);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean clear(ServerLevel level, BlockPos pos, int kind) {
        if (kind == 2) {
            if (!level.canSeeSky(pos) || !water(level, pos.below(4))) return false;
            // A large flying bird needs enough air above the sea to unfold.
            for (int y = 0; y <= 5; y++) {
                BlockPos p = pos.above(y);
                if (!loaded(level, p) || !level.getFluidState(p).isEmpty()
                        || !level.getBlockState(p).getCollisionShape(level, p).isEmpty())
                    return false;
            }
            return true;
        }
        if (!water(level, pos) || !water(level, pos.above())) return false;
        if (kind == 4) {
            BlockPos below = pos.below();
            if (!loaded(level, below) || !level.getBlockState(below).isSolidRender(level, below))
                return false;
        }
        int radius = kind == 1 || kind == 4 ? 3 : 2;
        int height = kind == 1 || kind == 4 ? 4 : 3;
        for (int x = -radius; x <= radius; x += radius)
            for (int z = -radius; z <= radius; z += radius)
                for (int y = 0; y <= height; y += 2) {
                    if (!water(level, pos.offset(x, y, z))) return false;
                }
        return true;
    }

    private static boolean loaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    private static boolean water(ServerLevel level, BlockPos pos) {
        return loaded(level, pos) && level.getFluidState(pos).is(FluidTags.WATER)
            && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private MarineTravelEncounters() {}
}
