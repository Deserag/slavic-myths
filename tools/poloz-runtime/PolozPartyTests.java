package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.poloz.*;
import org.slavicmyths.skim.HeroCalls;
@GameTestHolder("slavicmyths_poloz") @PrefixGameTestTemplate(false)
public final class PolozPartyTests {
 @GameTest(template="empty",timeoutTicks=30) public static void thirdHeroOnlyDuringEvent(GameTestHelper t){var p=MorExposureTests.player(t);var w=t.getLevel();var d=PolozEvent.get(w);var previous=d.state;HeroCalls.map(w,"ilya_muromets").put(p.getUUID(),java.util.UUID.randomUUID());HeroCalls.map(w,"dobrynya_nikitich").put(p.getUUID(),java.util.UUID.randomUUID());try{d.state=PolozEvent.State.IDLE;t.assertTrue(!org.slavicmyths.marinka.StoryPolicy.canSummon(p,"alyosha_popovich"),"previous two-hero limit");d.state=PolozEvent.State.ACTIVE;t.assertTrue(org.slavicmyths.marinka.StoryPolicy.canSummon(p,"alyosha_popovich"),"existing policy allows third core hero");t.assertTrue(!org.slavicmyths.marinka.StoryPolicy.canSummon(p,"finist"),"Finist is not granted a new party slot");}finally{d.state=previous;HeroCalls.map(w,"ilya_muromets").remove(p.getUUID());HeroCalls.map(w,"dobrynya_nikitich").remove(p.getUUID());HeroCalls.dirty(w);p.discard();}t.succeed();}
}
