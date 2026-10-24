package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.bogatyr.*;
import org.slavicmyths.skim.HeroCalls;
@GameTestHolder("slavicmyths_chud_kurgan") @PrefixGameTestTemplate(false)
public final class HeroCoopTests {
 @GameTest(template="empty",timeoutTicks=200) public static void combat_contribution_threshold(GameTestHelper t){
  var w=t.getLevel();var a=ChudKurganTests.player(t);var b=ChudKurganTests.player(t);var low=ChudKurganTests.player(t);
  for(var p:List.of(a,b,low))HeroIntroductions.state(p,"ilya_muromets").putBoolean("Met",true);
  var boss=BogatyrContent.IDOL.get().create(w);boss.moveTo(a.position().add(5,0,0));boss.debug=false;boss.setNoAi(true);boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);boss.setHealth(100);w.addFreshEntity(boss);
  boss.hurt(w.damageSources().playerAttack(a),20);boss.invulnerableTime=0;boss.hurt(w.damageSources().playerAttack(b),15);boss.invulnerableTime=0;boss.hurt(w.damageSources().playerAttack(low),14);
  ChudKurganTests.check(t,!HeroIntroductions.unlocked(a,"ilya_muromets"),"damage alone before victory gives no unlock");boss.invulnerableTime=0;boss.hurt(w.damageSources().generic(),1000);
  ChudKurganTests.check(t,!boss.isAlive(),"native enemy death");
  ChudKurganTests.check(t,HeroIntroductions.unlocked(a,"ilya_muromets")&&HeroIntroductions.unlocked(b,"ilya_muromets")&&!HeroIntroductions.unlocked(low,"ilya_muromets"),"native damage and death: 20 and 15 percent qualify, 14 percent does not");
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=300) public static void shared_help_four_heroes(GameTestHelper t){
  var a=ChudKurganTests.player(t);var b=ChudKurganTests.player(t);var spectator=ChudKurganTests.player(t);var w=t.getLevel();
  org.slavicmyths.yaga.YagaData.get(w).progress(a.getUUID()).stage=1;org.slavicmyths.yaga.YagaData.get(w).progress(b.getUUID()).stage=1;org.slavicmyths.yaga.YagaData.get(w).progress(spectator.getUUID()).stage=1;
  for(String hero:HeroIntroductions.IDS){
   ChudKurganTests.check(t,!HeroCalls.toggle(a,hero)&&!HeroCalls.toggle(b,hero),hero+" locked before help for both players");
   ChudKurganTests.check(t,HeroIntroductions.offer(a,"yaga",hero)&&HeroIntroductions.offer(b,"yaga",hero),hero+" both request meeting");
   HeroIntroductions.offer(spectator,"yaga",hero);var qa=HeroIntroductions.state(a,hero);var qb=HeroIntroductions.state(b,hero);ChudKurganTests.check(t,qa.getUUID("Npc").equals(qb.getUUID("Npc")),hero+" shared NPC without duplicate");
   var npc=(Mob)w.getEntity(qa.getUUID("Npc"));a.teleportTo(npc.getX()+2,npc.getY(),npc.getZ());b.teleportTo(npc.getX()+2,npc.getY(),npc.getZ());spectator.teleportTo(npc.getX()+2,npc.getY(),npc.getZ());
   HeroIntroductions.interact(npc,a,InteractionHand.MAIN_HAND);HeroIntroductions.interact(npc,b,InteractionHand.MAIN_HAND);HeroIntroductions.interact(npc,spectator,InteractionHand.MAIN_HAND);spectator.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BREAD));HeroIntroductions.interact(npc,spectator,InteractionHand.MAIN_HAND);
   b.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BREAD,2));HeroIntroductions.interact(npc,b,InteractionHand.MAIN_HAND);
   ChudKurganTests.check(t,!HeroIntroductions.unlocked(b,hero),hero+" partial help waits for task completion");
   a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.getInventory().add(new ItemStack(Items.BREAD,3));a.getInventory().add(new ItemStack(Items.HONEY_BOTTLE));a.getInventory().add(new ItemStack(Items.IRON_INGOT,4));a.getInventory().selected=8;HeroIntroductions.interact(npc,a,InteractionHand.MAIN_HAND);
   ChudKurganTests.check(t,HeroIntroductions.unlocked(a,hero)&&HeroIntroductions.unlocked(b,hero)&&!HeroIntroductions.unlocked(spectator,hero)&&!HeroIntroductions.state(spectator,hero).hasUUID("Npc"),hero+" qualifying contributors unlock; 1/11 receives no unlock and can request a new meeting");
   HeroIntroductions.offer(b,"yaga",hero);a.getInventory().clearContent();b.getInventory().clearContent();spectator.getInventory().clearContent();
  }
  // Canonical global reservations cover other owners and all four hero types.
  var ids=new ArrayList<UUID>();try{int before=HeroCalls.serverCount(w);while(HeroCalls.serverCount(w)<2){var owner=UUID.randomUUID();ids.add(owner);HeroCalls.map(w,"finist").put(owner,UUID.randomUUID());}
   ChudKurganTests.check(t,!HeroCalls.toggle(a,"finist")&&!BogatyrLedger.call(b),"global cap blocks a third hero across different players and direct call route");
  }finally{for(var id:ids)HeroCalls.map(w,"finist").remove(id);}
  t.succeed();
 }
}
