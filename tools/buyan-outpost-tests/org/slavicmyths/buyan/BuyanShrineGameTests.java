package org.slavicmyths.buyan;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Full non-destructive sanctuary blueprint against the reported island seed. */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanShrineGameTests {
    private BuyanShrineGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void shrineRoofOakChainsGateAndAlatyrReservation(GameTestHelper helper) {
        BuyanShrine.verifyBlueprintFixture(helper.getLevel());
        helper.succeed();
    }
}
