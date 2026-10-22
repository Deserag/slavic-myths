package org.slavicmyths.trophy;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.slavicmyths.hunt.*;

@EventBusSubscriber(modid="slavicmyths")
public final class TrophyHeadDrops {
 /** Pure rule, shared with the offline death/form matrix. Null means excluded. */
 public static TrophyHeads.Kind kind(String entity,boolean mor,boolean serpent){return switch(entity){
  case "tugarin_zmey"->serpent?TrophyHeads.Kind.TUGARIN:null;
  case "skim_beast"->TrophyHeads.Kind.SKIM;
  case "cursed_volkolak"->TrophyHeads.Kind.VOLKOLAK;
  case "likho_one_eyed"->mor?TrophyHeads.Kind.MOR_LIKHO:TrophyHeads.Kind.LIKHO;
  case "large_podzemnik"->TrophyHeads.Kind.PODZEMNIK;
  default->null;
 };}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void drops(LivingDropsEvent e){
  var mob=e.getEntity();if(mob.level().isClientSide||mob.isAlive())return;
  var id=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());if(!id.getNamespace().equals("slavicmyths"))return;
  WorldBoss boss=mob instanceof WorldBoss b?b:null;
  var kind=kind(id.getPath(),boss!=null&&boss.morState()!=0,boss!=null&&boss.tugarinState()==WorldBoss.TugarinState.PHASE_TWO);
  if(kind==null)return;
  var item=TrophyHeads.ITEMS.get(kind).get();
  // This subscriber owns this new drop; enforce one even if another loot layer supplies it.
  e.getDrops().removeIf(drop->drop.getItem().is(item));
  e.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY()+.5,mob.getZ(),new ItemStack(item)));
 }
 private TrophyHeadDrops(){}
}
