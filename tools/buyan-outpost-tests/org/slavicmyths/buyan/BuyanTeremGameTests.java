package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Server-side blueprint audit; does not alter or retrofit a world. */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanTeremGameTests {
    private BuyanTeremGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 1600)
    public static void threeStoreysTowersRoofsStairsAndLoot(GameTestHelper helper) {
        BuyanTerem.verifyBlueprintFixture(helper.getLevel());
        helper.succeed();
    }
}
