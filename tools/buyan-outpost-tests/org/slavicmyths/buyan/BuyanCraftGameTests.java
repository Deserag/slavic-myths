package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Isolated design fixture; it never writes a craft quarter into the test world. */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanCraftGameTests {
    private BuyanCraftGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void craftForgeRoofsStairsChimneyBedroomAndLoot(GameTestHelper helper) {
        BuyanCraftQuarter.verifyBlueprintFixture(helper.getLevel());
        helper.succeed();
    }
}
