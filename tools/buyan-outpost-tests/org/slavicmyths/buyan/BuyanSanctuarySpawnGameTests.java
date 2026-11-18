package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Pure boundary and spawn-cause regression tests for the Buyan protection. */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanSanctuarySpawnGameTests {
    private BuyanSanctuarySpawnGameTests() {}

    @GameTest(template = "empty")
    public static void naturalMonstersOnlyOnIslandLand(GameTestHelper helper) {
        long seed=helper.getLevel().getSeed();
        BuyanState.Site site=new BuyanState.Site(0,0,BuyanTerrain.MAX_RADIUS,63);
        if (!BuyanSanctuarySpawns.blocksNaturalSpawn(site,seed,
                MobCategory.MONSTER,MobSpawnType.NATURAL,0,0))
            throw new IllegalStateException("Naturally spawned zombie allowed on Buyan");
        if (BuyanSanctuarySpawns.blocksNaturalSpawn(site,seed,
                MobCategory.CREATURE,MobSpawnType.NATURAL,0,0))
            throw new IllegalStateException("Passive animals blocked on Buyan");
        if (BuyanSanctuarySpawns.blocksNaturalSpawn(site,seed,
                MobCategory.MONSTER,MobSpawnType.NATURAL,site.radius()+100,0))
            throw new IllegalStateException("Monster spawning blocked beyond Buyan");
        if (BuyanSanctuarySpawns.blocksNaturalSpawn(site,seed,
                MobCategory.MONSTER,MobSpawnType.COMMAND,0,0))
            throw new IllegalStateException("Command summons blocked on Buyan");
        if (BuyanSanctuarySpawns.blocksNaturalSpawn(site,seed,
                MobCategory.MONSTER,MobSpawnType.SPAWNER,0,0))
            throw new IllegalStateException("Spawner/boss spawning blocked on Buyan");
        helper.succeed();
    }
}
