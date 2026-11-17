package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Opt-in NeoForge server verification; never packaged in the production mod.
 * The fixture reuses the repository's shared empty GameTest structure.
 */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanOutpostGameTests {
    private BuyanOutpostGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void outpostPathsRoofsLaddersAndLoot(GameTestHelper helper) {
        BuyanOutpost.verifyGeometryFixture(helper.getLevel());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void integratedClientCrashSeedOutpostPreservesPalace(GameTestHelper helper) {
        BuyanOutpost.verifyPalaceFirstCrashSeedFixture(helper.getLevel());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void outpostReportedSeedMaintainsElevenBuildingsAndTraversableRoads(GameTestHelper helper) {
        BuyanOutpost.verifyReportedSeedFixture(helper.getLevel());
        helper.succeed();
    }
}
