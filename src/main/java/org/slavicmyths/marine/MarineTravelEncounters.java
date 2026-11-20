package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Server-driven open-ocean encounters, independent of vanilla MONSTER darkness
 * checks. Only existing loaded chunks are read; this never force-loads or
 * generates terrain. Creative players can observe encounters without combat.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class MarineTravelEncounters {
    private static final String NEXT = "SlavicMarineTravelNextAttempt";
    private static final String LAST_X = "SlavicMarineTravelLastX";
    private static final String LAST_Z = "SlavicMarineTravelLastZ";
    private static final int CHECK_TICKS = 100;
    private static final int ENCOUNTER_COOLDOWN = 800;
    private static final int FAILED_RETRY = 100;
    private static final int TRAVEL_DISTANCE = 80;
    private static final int MAX_DEPTH_SCAN = 80;
    private static final int LOCATION_ATTEMPTS = 80;

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || level.getDifficulty() == Difficulty.PEACEFUL
                || level.getGameTime() % CHECK_TICKS != 0) return;

        int sea = level.getSeaLevel();
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            BlockPos here = player.blockPosition();
            BlockPos water = new BlockPos(here.getX(), sea - 2, here.getZ());
            if (level.getChunkSource().getChunkNow(water.getX() >> 4, water.getZ() >> 4) == null
                    || !level.getBiome(water).is(BiomeTags.IS_OCEAN)
                    || !level.getFluidState(water).is(FluidTags.WATER)) continue;

            CompoundTag data = player.getPersistentData();
            long now = level.getGameTime();
            if (!data.contains(LAST_X) || !data.contains(LAST_Z)) {
                data.putInt(LAST_X, here.getX());
                data.putInt(LAST_Z, here.getZ());
                data.putLong(NEXT, now + CHECK_TICKS);
                continue;
            }
            if (now < data.getLong(NEXT)) continue;
            long dx = (long) here.getX() - data.getInt(LAST_X);
            long dz = (long) here.getZ() - data.getInt(LAST_Z);
            if (dx * dx + dz * dz < (long) TRAVEL_DISTANCE * TRAVEL_DISTANCE) continue;

            // Bosses can be far below a player in creative flight. Use sea level
            // for the cap check rather than the player's Y coordinate.
            if (hasBoss(level, new BlockPos(here.getX(), sea - 4, here.getZ()))) {
                data.putLong(NEXT, now + FAILED_RETRY);
                continue;
            }
            if (spawnAround(level, player)) {
                data.putInt(LAST_X, here.getX());
                data.putInt(LAST_Z, here.getZ());
                data.putLong(NEXT, now + ENCOUNTER_COOLDOWN);
                level.playSound(null, player.blockPosition(),
                    SoundEvents.CONDUIT_ACTIVATE, SoundSource.AMBIENT, 1F, .9F);
            } else {
                // Do not lock out the player for a minute after a failed random
                // sample, unloaded chunk, reef or unsuitable sea floor.
                data.putLong(NEXT, now + FAILED_RETRY);
            }
        }
    }

    private static boolean hasBoss(ServerLevel level, BlockPos center) {
        return !level.getEntitiesOfClass(Mob.class, new AABB(center).inflate(144),
                mob -> mob.getType() == DeepSeaSerpentContent.BOSS.get()
                    || mob.getType() == OceanZmeyContent.BOSS.get()
                    || mob.getType() == StratimBirdContent.BOSS.get()
                    || mob.getType() == TsarPikeContent.BOSS.get()
                    || mob.getType() == ZhelvaContent.BOSS.get()
                    || mob.getType() == PharaohkaContent.LEADER.get()).isEmpty();
    }

    private static boolean spawnAround(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        int sea = level.getSeaLevel();
        int firstBoss = level.random.nextInt(5);

        for (int attempt = 0; attempt < LOCATION_ATTEMPTS; attempt++) {
            int x = origin.getX() + level.random.nextInt(89) - 44;
            int z = origin.getZ() + level.random.nextInt(89) - 44;
            long dx = (long) x - origin.getX();
            long dz = (long) z - origin.getZ();
            if (dx * dx + dz * dz < 20L * 20L || dx * dx + dz * dz > 48L * 48L) continue;
            if (level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) continue;

            BlockPos water = new BlockPos(x, sea - 2, z);
            if (!level.getBiome(water).is(BiomeTags.IS_OCEAN)
                    || !level.getFluidState(water).is(FluidTags.WATER)) continue;

            // Scan the *entire* column to find the real seabed. The old 30-block
            // limit made deep-ocean Zhelva encounters almost impossible.
            int depth = 0;
            int bottom = sea - 1;
            for (int y = sea - 1; y >= sea - MAX_DEPTH_SCAN; y--) {
                BlockPos at = new BlockPos(x, y, z);
                if (!level.getFluidState(at).is(FluidTags.WATER)) break;
                depth++;
                bottom = y;
            }
            if (depth < 9) continue;

            for (int offset = 0; offset < 5; offset++) {
                int selected = (firstBoss + offset) % 5;
                if (selected == 0 && depth < 12) continue;
                if (selected == 1 && depth < 14) continue;
                if (selected == 4 && (depth < 17
                        || !level.getBlockState(new BlockPos(x, bottom - 1, z))
                            .isSolidRender(level, new BlockPos(x, bottom - 1, z)))) continue;

                int y = switch (selected) {
                    case 2 -> sea + 3; // Stratim is a *surface* bird, not an underwater mob.
                    case 4 -> bottom;  // Zhelva stays on the actual solid ocean floor.
                    case 1 -> sea - 6; // The large sea zmey can be seen from the surface.
                    case 0 -> sea - 5;
                    default -> sea - 4;
                };
                BlockPos pos = new BlockPos(x, y, z);
                if (selected == 2) {
                    if (!level.canSeeSky(pos)
                            || !level.getFluidState(pos.below(4)).is(FluidTags.WATER)) continue;
                } else {
                    if (!level.getFluidState(pos).is(FluidTags.WATER)
                            || !level.getFluidState(pos.above()).is(FluidTags.WATER)) continue;
                }

                Mob boss = switch (selected) {
                    case 0 -> DeepSeaSerpentContent.BOSS.get().create(level);
                    case 1 -> OceanZmeyContent.BOSS.get().create(level);
                    case 2 -> StratimBirdContent.BOSS.get().create(level);
                    case 3 -> TsarPikeContent.BOSS.get().create(level);
                    default -> ZhelvaContent.BOSS.get().create(level);
                };
                if (boss == null) continue;
                boss.moveTo(x + .5D, y + .1D, z + .5D,
                    level.random.nextFloat() * 360F, 0F);
                if (!level.noCollision(boss)) continue;
                boss.setPersistenceRequired();
                if (level.addFreshEntity(boss)) return true;
            }
        }
        return false;
    }

    private MarineTravelEncounters() {}
}
