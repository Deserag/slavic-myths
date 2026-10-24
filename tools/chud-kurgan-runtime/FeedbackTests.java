package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.bogatyr.*;
import org.slavicmyths.yaga.*;
import org.slavicmyths.registry.ModEntities;
@GameTestHolder("slavicmyths_chud_kurgan") @PrefixGameTestTemplate(false)
public final class FeedbackTests {
 static void check(GameTestHelper t,boolean value,String why){ChudKurganTests.check(t,value,why);}
 @GameTest(template="empty",timeoutTicks=200) public static void bayun_authenticated_ilya_intro(GameTestHelper t){
  var p=ChudKurganTests.player(t);var w=t.getLevel();var cat=org.slavicmyths.bayun.BayunContent.ENTITY.get().create(w);cat.home=p.blockPosition();cat.moveTo(p.position().add(1,0,0));cat.setNoAi(true);cat.setNoGravity(true);w.addFreshEntity(cat);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));cat.interact(p,InteractionHand.MAIN_HAND);cat.bond(p).putInt("Trust",25);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);cat.interact(p,InteractionHand.MAIN_HAND);org.slavicmyths.bayun.BayunNetwork.open(p,cat);
  var n=p.getPersistentData();check(t,n.hasUUID("BayunMenu"),"real cat SAFE dialogue opens");org.slavicmyths.bayun.BayunNetwork.choose(p,new org.slavicmyths.bayun.BayunNetwork.Choice(cat.getId(),UUID.randomUUID(),1,10));check(t,!IlyaIntroduction.state(p).hasUUID("Npc"),"forged nonce cannot reserve Ilya");org.slavicmyths.bayun.BayunNetwork.choose(p,new org.slavicmyths.bayun.BayunNetwork.Choice(cat.getId(),n.getUUID("BayunMenu"),1,10));check(t,IlyaIntroduction.state(p).hasUUID("Npc"),"authenticated Bayun option10 offers actual Ilya meeting");
  var npc=(IlyaEntity)w.getEntity(IlyaIntroduction.state(p).getUUID("Npc"));var other=ChudKurganTests.player(t);IlyaIntroduction.interact(npc,other,InteractionHand.MAIN_HAND);check(t,!IlyaIntroduction.state(other).getBoolean("Met")&&!IlyaIntroduction.unlocked(other),"other player cannot steal introduction");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=300) public static void ilya_full_intro_and_legacy(GameTestHelper t){
  var p=ChudKurganTests.player(t);var w=t.getLevel();var token=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","ilya_call_token"));p.getInventory().add(new ItemStack(token));
  check(t,!BogatyrLedger.call(p)&&!org.slavicmyths.skim.HeroCalls.toggle(p,"ilya_muromets"),"token ownership does not bypass Ilya intro via either call route");
  var crafting=new CraftingMenu(8,p.getInventory(),ContainerLevelAccess.create(w,p.blockPosition()));p.containerMenu=crafting;crafting.getSlot(2).set(new ItemStack(Items.IRON_INGOT));crafting.getSlot(5).set(new ItemStack(Items.RED_DYE));crafting.getSlot(8).set(new ItemStack(Items.PAPER));check(t,crafting.getSlot(0).getItem().isEmpty(),"native survival recipe result locked before help");p.containerMenu=p.inventoryMenu;
  var yaga=ModEntities.BABA_YAGA.get().create(w);yaga.moveTo(p.position().add(1,0,0));yaga.finalizeSpawn(w,w.getCurrentDifficultyAt(yaga.blockPosition()),MobSpawnType.SPAWN_EGG,null);w.addFreshEntity(yaga);var progress=YagaData.get(w).progress(p.getUUID());progress.stage=1;var menu=new YagaMenu(9,p.getInventory(),yaga.getId());p.containerMenu=menu;
  check(t,menu.clickMenuButton(p,707),"real Yaga menu offers Ilya meeting");var q=IlyaIntroduction.state(p);var id=q.getUUID("Npc");check(t,menu.clickMenuButton(p,707)&&q.getUUID("Npc").equals(id),"repeat request reuses same Ilya reservation");var npc=(IlyaEntity)w.getEntity(id);p.teleportTo(npc.getX()+2,npc.getY(),npc.getZ());p.containerMenu=p.inventoryMenu;
  IlyaIntroduction.interact(npc,p,InteractionHand.MAIN_HAND);check(t,q.getBoolean("Met")&&!IlyaIntroduction.unlocked(p),"meeting alone does not unlock");p.getInventory().add(new ItemStack(Items.BREAD,6));p.getInventory().add(new ItemStack(Items.HONEY_BOTTLE));p.getInventory().add(new ItemStack(Items.IRON_INGOT,4));p.getInventory().selected=8;IlyaIntroduction.interact(npc,p,InteractionHand.MAIN_HAND);
  check(t,IlyaIntroduction.unlocked(p)&&org.slavicmyths.progression.Knowledge.knows(p,"ilya_friendship"),"help commits permanent unlock and actual friendship advancement");
  check(t,org.slavicmyths.rpg.Runes.inventoryCount(p,Items.BREAD)==0&&org.slavicmyths.rpg.Runes.inventoryCount(p,Items.IRON_INGOT)==0&&org.slavicmyths.rpg.Runes.inventoryCount(p,Items.GLASS_BOTTLE)==1&&org.slavicmyths.rpg.Runes.inventoryCount(p,token)==1,"supplies consumed once, bottle returned, existing token not duplicated");
  IlyaIntroduction.interact(npc,p,InteractionHand.MAIN_HAND);check(t,org.slavicmyths.rpg.Runes.inventoryCount(p,Items.GLASS_BOTTLE)==1,"repeat hand-in cannot duplicate rewards");
  p.containerMenu=crafting;crafting.getSlot(5).set(new ItemStack(Items.RED_DYE));check(t,crafting.getSlot(0).getItem().is(token),"native survival crafting unlocked after help");p.containerMenu=p.inventoryMenu;check(t,BogatyrLedger.call(p),"actual Ilya ally can be summoned after help");
  var ally=BogatyrLedger.hero(p);if(ally!=null)ally.discard();BogatyrLedger.get(w).heroes.remove(p.getUUID());
  var legacy=ChudKurganTests.player(t);BogatyrLedger.get(w).heroes.put(legacy.getUUID(),UUID.randomUUID());check(t,!IlyaIntroduction.unlocked(legacy),"mere old summon history cannot substitute for help");BogatyrLedger.get(w).heroes.remove(legacy.getUUID());t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200) public static void yaga_reputation_and_broom(GameTestHelper t){
  var p=ChudKurganTests.player(t);var w=t.getLevel();var y=ModEntities.BABA_YAGA.get().create(w);y.moveTo(p.position().add(1,0,0));y.finalizeSpawn(w,w.getCurrentDifficultyAt(y.blockPosition()),MobSpawnType.SPAWN_EGG,null);w.addFreshEntity(y);var progress=YagaData.get(w).progress(p.getUUID());var menu=new YagaMenu(7,p.getInventory(),y.getId());p.containerMenu=menu;
  for(int i=0;i<3;i++){
   for(var need:YagaServices.MAIN[i].inputs)p.getInventory().add(need.stack());if(i==2){org.slavicmyths.progression.Knowledge.award(p,"name_your_misfortune");org.slavicmyths.progression.Knowledge.award(p,"steppe_champion");}
   check(t,menu.clickMenuButton(p,100+i)&&menu.clickMenuButton(p,200+i),"actual Yaga main quest completion stage"+i);
  }
  check(t,progress.stage==3&&org.slavicmyths.quest185.Quest185Story.yagaTrust(p)==3&&YagaServices.killed(p,"guest_of_yaga")&&YagaServices.killed(p,"trusted_by_yaga"),"native reputation and guest/trusted advancements agree");
  int index=0;while(!YagaServices.EXCHANGES[index].id.equals("flying_broom"))index++;var offer=YagaServices.EXCHANGES[index];for(var need:offer.inputs)p.getInventory().add(need.stack());check(t,menu.clickMenuButton(p,400+index)&&org.slavicmyths.rpg.Runes.inventoryCount(p,offer.output().getItem())==1,"earned reputation permits real broom trade");var broom=ModEntities.FLYING_BROOM.get().create(w);broom.fromItem(offer.output(),p.getUUID());broom.moveTo(p.position());w.setBlock(broom.blockPosition().below(),Blocks.STONE.defaultBlockState(),3);w.addFreshEntity(broom);broom.interact(p,InteractionHand.MAIN_HAND);broom.setOnGround(true);broom.input(p,0,0,1,p.getYRot(),false);broom.tick();check(t,org.slavicmyths.progression.Knowledge.knows(p,"first_flight"),"actual broom takeoff grants native first_flight advancement");p.stopRiding();broom.discard();t.succeed();
 }
}
