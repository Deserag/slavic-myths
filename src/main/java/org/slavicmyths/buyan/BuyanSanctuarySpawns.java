package org.slavicmyths.buyan;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

/**
 * Sacred Buyan only blocks natural monster spawning on generated island land.
 * Summons, spawners, custom bosses and wildlife remain governed by normal rules.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class BuyanSanctuarySpawns {
    private BuyanSanctuarySpawns() {}

    static boolean blocksNaturalSpawn(BuyanState.Site site, long seed,
            MobCategory category, MobSpawnType reason, int x, int z) {
        if (category != MobCategory.MONSTER || reason != MobSpawnType.NATURAL)
            return false;
        int surface = BuyanNature.top(seed,site.x(),site.z(),site.radius(),
            x,z,site.waterLevel());
        return surface != Integer.MIN_VALUE && surface > site.waterLevel();
    }

    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL
                || event.getEntity().getType().getCategory() != MobCategory.MONSTER)
            return;
        ServerLevel level = event.getLevel().getLevel();
        if (level.dimension() != Level.OVERWORLD) return;
        BlockPos position = BlockPos.containing(event.getX(),event.getY(),event.getZ());
        for (BuyanState.Site site : BuyanState.get(level).completedSites()) {
            if (blocksNaturalSpawn(site,level.getSeed(),
                    event.getEntity().getType().getCategory(),event.getSpawnType(),
                    position.getX(),position.getZ())) {
                event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                return;
            }
        }
    }
}
