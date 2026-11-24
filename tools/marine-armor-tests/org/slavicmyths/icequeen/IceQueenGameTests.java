package org.slavicmyths.icequeen;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class IceQueenGameTests {
 @GameTest(template="empty") public static void queenRegistrationAndSummonStats(GameTestHelper t){
  var q=QueenContent.BOSS.get().create(t.getLevel());
  t.assertTrue(q!=null&&q.getMaxHealth()==3000F&&q.getHealth()==3000F&&q.phase()==1&&!q.active(),"Queen registration or real HP");
  t.assertTrue(QueenContent.EGG.get() instanceof SpawnEggItem,"Missing test egg");
  int[] expected={20,40,15,30,40,100,200,150};
  for(var k:QueenContent.Kind.values()){var m=QueenContent.MINIONS.get(k).get().create(t.getLevel());t.assertTrue(m!=null&&m.getMaxHealth()==expected[k.ordinal()],"Incorrect minion HP: "+k);m.discard();}
  q.discard();t.succeed();
 }
 @GameTest(template="empty") public static void queenSoundRegistry(GameTestHelper t){
  var ids=new java.util.HashSet<net.minecraft.resources.ResourceLocation>();
  for(var cue:QueenSounds.Cue.values()){
   var id=net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(QueenSounds.event(cue));
   t.assertTrue(id!=null&&id.equals(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",cue.id))&&ids.add(id),"Missing or duplicate queen sound: "+cue.id);
  }
  t.assertTrue(ids.size()==17,"Incomplete queen cue registry");t.succeed();
 }
 @GameTest(template="empty") public static void queenTrialFloorAndCooldownRules(GameTestHelper t){
  t.assertTrue(QueenRules.health(-5)==100&&QueenRules.health(100)==100&&QueenRules.health(104)==104,"Health floor");
  t.assertTrue(QueenRules.nextTrial(1000)==3601000&&!QueenRules.ready(3600999,3601000)&&QueenRules.ready(3601000,3601000),"Real time cooldown boundary");
  t.assertTrue(QueenRules.consumeShield(5)==4&&QueenRules.consumeShield(0)==0&&!QueenRules.blocksProjectile(.30)&&QueenRules.blocksProjectile(.299),"Shield rules");
  int[] counts=new int[6];for(int i=0;i<100;i++)counts[QueenRules.summonKind(i)]++;t.assertTrue(java.util.Arrays.equals(counts,new int[]{25,15,15,20,20,5}),"Summon distribution");
  var q=QueenContent.BOSS.get().create(t.getLevel());var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"QueenTrialQA"));
  q.setPos(t.absolutePos(new BlockPos(2,2,2)).getCenter());p.setPos(q.position().add(1,0,0));q.startTrial(p);
  q.setHealth(-5);t.assertTrue(q.getHealth()==100&&q.isAlive(),"Native entity health floor");q.discard();
  var saved=new CompoundTag();saved.putLong("QueenReadyAt",3601000);var restored=QueenContent.BOSS.get().create(t.getLevel());restored.readAdditionalSaveData(saved);t.assertTrue(restored.readyAt()==3601000,"Cooldown lost on load");restored.discard();t.succeed();
 }
}
