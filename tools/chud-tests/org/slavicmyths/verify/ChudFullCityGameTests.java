package org.slavicmyths.verify;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths_chud_full") @PrefixGameTestTemplate(false)
public final class ChudFullCityGameTests {
    @GameTest(template="empty",timeoutTicks=2400)
    public static void fullCityNativePlacement(GameTestHelper test)throws Exception { ChudGameTests.fullCityNativePlacement(test); }
}
