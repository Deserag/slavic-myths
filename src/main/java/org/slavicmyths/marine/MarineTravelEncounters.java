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
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Daylight-safe travel encounters. Vanilla monster spawn attempts often choose
 * the ocean surface, which cannot satisfy the deep-water placement predicates.
 * This only considers loaded chunks around players; it never generates chunks.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class MarineTravelEncounters {
    private static final String NEXT = "SlavicMarineTravelNextAttempt";
    private static final String LAST_X = "SlavicMarineTravelLastX";
    private static final String LAST_Z = "SlavicMarineTravelLastZ";

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || level.getDifficulty() == Difficulty.PEACEFUL
                || level.getGameTime() % 100 != 0) return;

        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            BlockPos here = player.blockPosition();
            if (!level.getBiome(new BlockPos(here.getX(), level.getSeaLevel() - 6, here.getZ()))
                    .is(BiomeTags.IS_OCEAN)) continue;

            CompoundTag state = player.getPersistentData();
            long now = level.getGameTime();
            if (now < state.getLong(NEXT)) continue;
            if (state.contains(LAST_X) && state.contains(LAST_Z)) {
                long dx = (long) here.getX() - state.getInt(LAST_X);
                long dz = (long) here.getZ() - state.getInt(LAST_Z);
                if (dx * dx + dz * dz < 180L * 180L) continue;
            }
            state.putInt(LAST_X, here.getX());
            state.putInt(LAST_Z, here.getZ());
            state.putLong(NEXT, now + 1200);
            if (level.random.nextFloat() >= 0.70F || hasBoss(level, here)) continue;
            spawnAround(level, here);
        }
    }

    private static boolean hasBoss(ServerLevel level, BlockPos center) {
        return !level.getEntitiesOfClass(Mob.class, new AABB(center).inflate(224),
                mob -> mob.getType() == DeepSeaSerpentContent.BOSS.get()
                    || mob.getType() == OceanZmeyContent.BOSS.get()
                    || mob.getType() == StratimBirdContent.BOSS.get()
                    || mob.getType() == TsarPikeContent.BOSS.get()
                    || mob.getType() == ZhelvaContent.BOSS.get()
                    || mob.getType() == PharaohkaContent.LEADER.get()).isEmpty();
    }

    private static void spawnAround(ServerLevel level, BlockPos origin) {
        int sea = level.getSeaLevel();
        int choice = level.random.nextInt(5);
        for (int attempt = 0; attempt < 16; attempt++) {
            int x = origin.getX() + level.random.nextInt(145) - 72;
            int z = origin.getZ() + level.random.nextInt(145) - 72;
            long dx = (long) x - origin.getX(), dz = (long) z - origin.getZ();
            if (dx * dx + dz * dz < 35L * 35L || dx * dx + dz * dz > 100L * 100L) continue;
            if (level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) continue;
            if (!level.getBiome(new BlockPos(x, sea - 6, z)).is(BiomeTags.IS_OCEAN)) continue;

            int waterDepth = 0;
            for (int y = sea - 1; y >= sea - 30; y--) {
                BlockPos at = new BlockPos(x, y, z);
                if (!level.getFluidState(at).is(FluidTags.WATER)) break;
                waterDepth++;
            }
            if (waterDepth < 8) continue;

            for (int step = 0; step < 5; step++) {
                int selected = (choice + step) % 5;
                if (selected == 0 && waterDepth < 14) continue;
                if (selected == 1 && waterDepth < 13) continue;
                if (selected == 4 && waterDepth < 16) continue;
                Mob boss = switch (selected) {
                    case 0 -> DeepSeaSerpentContent.BOSS.get().create(level);
                    case 1 -> OceanZmeyContent.BOSS.get().create(level);
                    case 2 -> StratimBirdContent.BOSS.get().create(level);
                    case 3 -> TsarPikeContent.BOSS.get().create(level);
                    default -> ZhelvaContent.BOSS.get().create(level);
                };
                if (boss == null) continue;
                int y = selected == 2 ? sea + 3 : selected == 4 ? sea - waterDepth + 1 : sea - Math.min(7, waterDepth / 2);
                BlockPos pos = new BlockPos(x, y, z);
                if (selected == 2) {
                    if (!level.canSeeSky(pos)
                            || !level.getFluidState(pos.below(4)).is(FluidTags.WATER)) continue;
                } else if (!level.getFluidState(pos).is(FluidTags.WATER)
                        || !level.getFluidState(pos.above()).is(FluidTags.WATER)) continue;
                if (selected == 4 && !level.getBlockState(pos.below()).isSolidRender(level, pos.below()))
                    continue;
                boss.moveTo(x + .5D, y + .15D, z + .5D, level.random.nextFloat() * 360, 0);
                if (!level.noCollision(boss)) continue;
                boss.setPersistenceRequired();
                if (level.addFreshEntity(boss)) return;
            }
        }
    }

    private MarineTravelEncounters() {}
}
