package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Real end-to-end Buyan construction in the disposable isolated test world.
 * Blueprint-only checks cannot catch a build order that leaves the island bare.
 */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanFullCityGameTests {
    private BuyanFullCityGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 3000)
    public static void fullBuyanCityBuildsInSingleWorld(GameTestHelper helper) {
        BuyanPreSpawn.construct(helper.getLevel());
        BuyanState.Site island = new BuyanState.Site(0, 0, BuyanTerrain.MAX_RADIUS, 63);
        if (!BuyanNatureState.get(helper.getLevel()).finishedAt(island))
            throw new IllegalStateException("End-to-end Buyan: nature not marked complete");
        if (!BuyanCityPlan.hasBuilt(helper.getLevel(), island))
            throw new IllegalStateException("End-to-end Buyan: central city axis missing");
        helper.succeed();
    }
}
