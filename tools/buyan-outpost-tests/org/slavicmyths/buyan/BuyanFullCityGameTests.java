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
        // Reuse the completed geological and natural passes on later runs.
        // Invoking the same path twice also tests in-process idempotence.
        BuyanPreSpawn.constructOrResume(helper.getLevel());
        BuyanPreSpawn.constructOrResume(helper.getLevel());
        BuyanState.Site island = new BuyanState.Site(0, 0, BuyanTerrain.MAX_RADIUS, 63);
        if (!BuyanNatureState.get(helper.getLevel()).finishedAt(island))
            throw new IllegalStateException("End-to-end Buyan: nature not marked complete");
        if (!BuyanShrine.hasBuilt(helper.getLevel(), island))
            throw new IllegalStateException("End-to-end Buyan: sanctuary missing");
        if (!BuyanCityPlan.hasBuilt(helper.getLevel(), island))
            throw new IllegalStateException("End-to-end Buyan: central city axis missing");
        // Regression for the previously failed (-2, 73, -90) write:
        // laying pavement removes plants above their support automatically,
        // so the next planned AIR operation must accept an already-AIR block.
        int x = -2, z = -90;
        int natural = BuyanNature.top(helper.getLevel().getSeed(),
            island.x(), island.z(), island.radius(), x, z, island.waterLevel());
        boolean paved = false;
        for (int y = natural; y <= natural + 2; y++) {
            net.minecraft.core.BlockPos surface = new net.minecraft.core.BlockPos(x,y,z);
            if (helper.getLevel().getBlockState(surface).is(
                    net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ)) {
                for (int dy = 1; dy <= 3; dy++)
                    if (!helper.getLevel().getBlockState(surface.above(dy)).isAir())
                        throw new IllegalStateException(
                            "End-to-end Buyan: street headroom obstructed at "
                                + surface.above(dy));
                paved = true;
            }
        }
        if (!paved)
            throw new IllegalStateException("End-to-end Buyan: missing street at -2,-90");
        helper.succeed();
    }
}
