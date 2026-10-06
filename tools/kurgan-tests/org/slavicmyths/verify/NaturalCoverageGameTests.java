package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
/** Separate expensive release gate; core regressions do not replace this measurement. */
@GameTestHolder("slavicmyths_coverage") @PrefixGameTestTemplate(false)
public final class NaturalCoverageGameTests {
 @GameTest(template="port_empty",timeoutTicks=6000)
 public static void acceptedNaturalNetworks(GameTestHelper test)throws Exception { CoverageGameTests.measure(test,true); }
}
