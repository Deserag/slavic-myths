package org.slavicmyths.hellqa;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.burningislands.*;

/** Actual server boss-event memberships; not a client HUD, combat-phase or relog test. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class HellBossBarQaTests {
 static ServerBossEvent bar(IslandsMob boss)throws Exception {var f=IslandsMob.class.getDeclaredField("bar");f.setAccessible(true);return (ServerBossEvent)f.get(boss);}
 @GameTest(template="empty") public static void peacefulBossBarsAndNeutralFauna(GameTestHelper t)throws Exception {
  var server=t.getLevel().getServer();var old=server.getWorldData().getDifficulty();var player=HellQaTests.player(t);
  var spawned=new ArrayList<IslandsMob>();
  try {for(int attempt=0;attempt<3;attempt++){
   server.setDifficulty(Difficulty.NORMAL,true);
   var bosses=new ArrayList<IslandsMob>();
   for(int i=0;i<2;i++){
    var boss=IslandsContent.TYPES.get(IslandsContent.Kind.MAGMA_LORD).get().create(t.getLevel());
    boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(t.absoluteVec(new net.minecraft.world.phys.Vec3(2+i*4,2,2)));t.getLevel().addFreshEntity(boss);
    boss.startSeenByPlayer(player);boss.startSeenByPlayer(player);
    HellQaTests.check(bar(boss).getPlayers().size()==1,"One actor registered duplicate boss-event membership");bosses.add(boss);spawned.add(boss);
   }
   HellQaTests.check(!bar(bosses.get(0)).getId().equals(bar(bosses.get(1)).getId()),"Two bosses share a boss event ID");
   var neutrals=new ArrayList<IslandsMob>();
   for(var kind:List.of(IslandsContent.Kind.HEAT_SALAMANDER,IslandsContent.Kind.SLAG_CRAB,IslandsContent.Kind.VENT_GOLEM)){
    var mob=IslandsContent.TYPES.get(kind).get().create(t.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.moveTo(player.position());t.getLevel().addFreshEntity(mob);neutrals.add(mob);spawned.add(mob);
   }
   server.setDifficulty(Difficulty.PEACEFUL,true);
   for(var boss:bosses){boss.tick();HellQaTests.check(boss.isRemoved()&&bar(boss).getPlayers().isEmpty(),"Peaceful left body or orphan boss-event membership");boss.startSeenByPlayer(player);HellQaTests.check(bar(boss).getPlayers().isEmpty(),"Removed boss re-added player to boss event");}
   for(var mob:neutrals){mob.tick();HellQaTests.check(!mob.isRemoved(),"Peaceful removed neutral island fauna "+mob.kind);mob.discard();}
   System.out.println("HELLQA_PASS magma bar attempt="+attempt+" one actor/one event membership; two actors/distinct IDs; Peaceful removes bodies+memberships; all3 neutral fauna retained; removed actor cannot re-add viewer");
  }t.succeed();}finally{for(var mob:spawned)if(!mob.isRemoved())mob.discard();server.setDifficulty(old,true);}
 }
}
