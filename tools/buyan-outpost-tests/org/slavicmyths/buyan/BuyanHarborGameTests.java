package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanHarborGameTests {
    private BuyanHarborGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void integratedClientCrashSeedPortPreservesPalace(GameTestHelper helper) {
        BuyanHarbor.verifyHarborPalaceCrashSeedFixture(helper.getLevel());
        helper.succeed();
    }
}
