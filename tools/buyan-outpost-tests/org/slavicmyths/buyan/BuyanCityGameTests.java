package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanCityGameTests {
    private BuyanCityGameTests() {}
    @GameTest(template="empty",timeoutTicks=1200)
    public static void wideAxisContinuousPlazaReserved(GameTestHelper helper) {
        BuyanCityPlan.verifyBlueprintFixture(helper.getLevel());
        helper.succeed();
    }
}
