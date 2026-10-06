package org.slavicmyths.verify;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.kurgan.KurganCreature;
import org.slavicmyths.hunt.HuntMob;

@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class CombatPolishGameTests {
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void exclusiveCombatClocks(GameTestHelper test){
  int checked=0;
  for(var type:BuiltInRegistries.ENTITY_TYPE){
   var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);if(!id.getNamespace().equals("slavicmyths"))continue;
   var entity=type.create(test.getLevel());if(!(entity instanceof KurganCreature||entity instanceof HuntMob))continue;
   var mob=(Mob)entity;
   var fights=mob.goalSelector.getAvailableGoals().stream().map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal).filter(goal->goal.getClass().getSimpleName().equals("Fight")).toList();
   test.assertTrue(fights.size()==1,"Competing/missing fight goals "+id);
   test.assertTrue(fights.getFirst().requiresUpdateEveryTick(),"Combat clock runs at half server tick rate "+id);
   var flags=fights.getFirst().getFlags();test.assertTrue(flags.contains(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE)&&flags.contains(net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK),"Fight does not own navigation/facing "+id);
   test.assertTrue(mob.getMaxHealth()>0,"Invalid existing boss stats "+id);checked++;
  }
  test.assertTrue(checked>=10,"Incomplete existing fighter coverage "+checked);
  System.out.println("COMBAT_POLISH_CLOCK_PASS registeredFighters="+checked+" exclusiveGoal=true everyServerTick=true");test.succeed();
 }
}
