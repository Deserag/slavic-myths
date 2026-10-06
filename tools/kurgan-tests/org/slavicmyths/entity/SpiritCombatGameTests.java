package org.slavicmyths.entity;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.ModEntities;

@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class SpiritCombatGameTests {
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void attackWindows(GameTestHelper test){
  var clock=new AttackTimeline();test.assertTrue(clock.start(12,3,20,40),"Clock does not start");
  int openings=0;var phases=EnumSet.noneOf(AttackTimeline.Phase.class);
  for(int tick=0;tick<75;tick++){phases.add(clock.phase());if(clock.tick())openings++;}
  phases.add(clock.phase());
  test.assertTrue(openings==1&&clock.ready()&&phases.size()==5,"Clock loses recovery/cooldown or repeats active entry");
  var world=test.getLevel();var origin=test.absolutePos(new BlockPos(20000,160,20000));world.getChunkAt(origin);
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=-1;y<=4;y++)world.setBlock(origin.offset(x,y,z),(y==-1?Blocks.GRASS_BLOCK:Blocks.AIR).defaultBlockState(),2);
  var types=List.of(ModEntities.KIKIMORA.get(),ModEntities.POLUDNITSA.get(),ModEntities.POLEVIK.get(),ModEntities.BANNIK.get(),ModEntities.IGOSHA.get(),ModEntities.LESHY.get());
  int[] hp={36,60,44,80,30,100};int checked=0;
  for(var type:types){
   var mob=(LandSpiritEntity)type.create(world);test.assertTrue(mob.getMaxHealth()==hp[checked],"Wrong HP "+type);
   test.assertTrue(!mob.meleeReady(),"Generic melee still competes with stateful attacks "+type);
   var player=test.makeMockPlayer(GameType.SURVIVAL);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);player.setHealth(500);
   mob.moveTo(origin.getX()+.5,origin.getY(),origin.getZ()+.5,0,0);player.moveTo(mob.getX(),mob.getY(),mob.getZ()+1.5,180,0);
   mob.provoke(player);boolean started=false,recovered=false;int damageEvents=0;
   for(int tick=0;tick<180;tick++){
    player.invulnerableTime=0;float before=player.getHealth();var prior=mob.attackTimeline().phase();mob.abilityTick();var after=mob.attackTimeline().phase();
    if(after==AttackTimeline.Phase.TELEGRAPH)started=true;
    if(player.getHealth()<before){damageEvents++;test.assertTrue(prior==AttackTimeline.Phase.ACTIVE||after==AttackTimeline.Phase.ACTIVE,"Damage outside active window "+type);}
    if(after==AttackTimeline.Phase.RECOVERY)recovered=true;
    if(started&&mob.attackTimeline().ready())break;
   }
   test.assertTrue(started&&recovered,"No complete telegraph/recovery "+type);
   test.assertTrue(damageEvents<=(type==ModEntities.KIKIMORA.get()?2:1),"Repeated swing damage "+type);
   mob.attackTimeline().start(12,3,20,40);mob.aiStep();
   test.assertTrue(mob.attackPhase()==mob.attackTimeline().phase(),"Server attack phase is not synchronized");
   int age=mob.tickCount;float visual=mob.attackVisualTime(age+.5F);mob.tickCount+=100000;
   test.assertTrue(Math.abs(mob.attackVisualTime(mob.tickCount+.5F)-visual)<.01F,"Spirit animation depends on local entity age");mob.tickCount=age;
   var tag=new net.minecraft.nbt.CompoundTag();mob.saveWithoutId(tag);var reloaded=(LandSpiritEntity)type.create(world);reloaded.load(tag);
   test.assertTrue(reloaded.attackTimeline().ready()&&!reloaded.isInvisible(),"Reload replays attack/vanish "+type);checked++;
  }
  test.assertTrue(ModEntities.ATAMAN.get().create(world).getMaxHealth()==150&&ModEntities.NIGHTINGALE.get().create(world).getMaxHealth()==250,"Mandatory bandit boss HP");
  System.out.println("SPIRIT_RUNTIME_ATTACK_WINDOWS_PASS mobs="+checked+" telegraphActiveRecoveryCooldown=true reloadCancels=true animationUsesWorldClock=true atamanHP=150 soloveyHP=250");test.succeed();
 }
}
