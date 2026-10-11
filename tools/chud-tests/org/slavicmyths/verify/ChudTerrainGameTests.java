package org.slavicmyths.verify;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths_chud_terrain") @PrefixGameTestTemplate(false)
public final class ChudTerrainGameTests {
    @GameTest(template="empty",timeoutTicks=2400)
    public static void naturalTenSeedCandidates(GameTestHelper test)throws Exception { ChudGameTests.naturalTerrainCandidates(test); }
}
