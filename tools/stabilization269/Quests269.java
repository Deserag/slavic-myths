package org.slavicmyths.verify;
import net.minecraft.world.item.*;
import org.slavicmyths.yaga.*;
import static org.slavicmyths.verify.Stabilization269.*;
/** Native transaction/credit integration; supplied inventory and one-hit bosses are not Survival combat. */
final class Quests269 {
 static void inputs(YagaServices.Entry entry){p.getInventory().clearContent();for(var need:entry.inputs)p.getInventory().add(need.stack());}
 static void run(){
  current="yaga_quest_transactions";var data=YagaData.get(w);var npc=(BabaYaga)w.getEntity(data.npc);check(npc!=null,"saved canonical NPC present for real quest menu");if(npc==null)return;
  p.teleportTo(w,npc.getX()+1,npc.getY(),npc.getZ(),0,0);var progress=data.progress(p.getUUID());progress.stage=0;progress.active="";progress.intro=2;progress.blockedUntil=0;progress.contractReady=0;
  var menu=new YagaMenu(1,p.getInventory(),npc.getId());
  for(int i=0;i<2;i++){var entry=YagaServices.MAIN[i];p.getInventory().clearContent();check(menu.clickMenuButton(p,100+i),"accept real main quest "+entry.id);check(!menu.clickMenuButton(p,200+i),"main rejects missing ingredients "+entry.id);inputs(entry);check(menu.clickMenuButton(p,200+i),"complete real main quest "+entry.id);check(progress.stage==i+1&&progress.active.isEmpty(),"main advances one stage "+entry.id);check(p.getInventory().countItem(entry.output().getItem())==entry.amount,"main reward exact quantity "+entry.id);check(!menu.clickMenuButton(p,200+i),"main reward cannot repeat "+entry.id);}
  check(menu.clickMenuButton(p,102),"accept strength quest");check(!menu.clickMenuButton(p,202),"strength rejects absent two boss credits");
  var types=java.util.List.of(org.slavicmyths.registry.ModEntities.LIKHO_ONE_EYED,org.slavicmyths.registry.ModEntities.TUGARIN_ZMEY);String[] advances={"name_your_misfortune","steppe_champion"};
  for(int i=0;i<types.size();i++){var boss=types.get(i).get().create(w);boss.moveTo(p.getX()+5,p.getY(),p.getZ()+5);w.addFreshEntity(boss);boss.setHealth(1);boss.hurt(w.damageSources().playerAttack(p),10000);check(!boss.isAlive()&&YagaServices.killed(p,advances[i]),"actual boss death awards player credit "+advances[i]);boss.discard();}
  check(menu.clickMenuButton(p,202),"strength completes with real boss death credits");check(progress.stage==3,"trusted stage earned through menu chain");check(!menu.clickMenuButton(p,202),"strength reward cannot repeat");
  for(int slot=0;slot<YagaServices.POOL.length;slot++){progress.active="";progress.contract=slot;progress.contractReady=0;var entry=YagaServices.CONTRACTS[YagaServices.POOL[slot]];p.getInventory().clearContent();check(menu.clickMenuButton(p,120),"accept contract pool slot "+slot);check(!menu.clickMenuButton(p,220),"contract missing inputs rejected slot "+slot);inputs(entry);check(menu.clickMenuButton(p,220),"complete contract slot "+slot);check(progress.contractReady==w.getGameTime()+24000,"contract real day cooldown slot "+slot);check(!menu.clickMenuButton(p,120)&&!menu.clickMenuButton(p,220),"contract cannot immediately repeat slot "+slot);}
  menu.removed(p);data.setDirty();
 }
}
