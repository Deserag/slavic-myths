package org.slavicmyths.burningislands;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** One winner's persistent inbox; full inventory keeps the entire batch, never drops it in lava. */
@EventBusSubscriber(modid="slavicmyths")
public final class MagmaRewards {
 private static final String KEY="slavicmyths_magma_rewards";
 public static void queue(ServerPlayer p,int cores){
  var tag=p.getPersistentData();var pending=tag.getList(KEY,Tag.TAG_INT);pending.add(IntTag.valueOf(Math.clamp(cores,4,6)));tag.put(KEY,pending);deliver(p);
 }
 private static List<ItemStack> batch(int cores){
  var soul=new ItemStack(org.slavicmyths.rpg.RuneFoundation.item("soul"));
  var data=new CompoundTag();data.putBoolean("slavicmyths_infected_medium",true);
  soul.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
  soul.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.translatable("item.slavicmyths.infected_medium_soul"));
  return List.of(new ItemStack(org.slavicmyths.trophy.TrophyHeads.ITEMS.get(org.slavicmyths.trophy.TrophyHeads.Kind.MAGMA_LORD).get()),new ItemStack(IslandsContent.item("magma_lord_heart")),new ItemStack(IslandsContent.item("magma_core"),cores),soul);
 }
 private static void deliver(ServerPlayer p){
  var list=p.getPersistentData().getList(KEY,Tag.TAG_INT);if(list.isEmpty())return;
  var rewards=batch(list.getInt(0));
  // Simulate the whole inventory before changing any slot; do not partially deliver.
  var slots=new ArrayList<ItemStack>();for(int i=0;i<36;i++)slots.add(p.getInventory().getItem(i).copy());
  for(var reward:rewards){int remaining=reward.getCount();for(var slot:slots){if(ItemStack.isSameItemSameComponents(slot,reward)){int add=Math.min(remaining,Math.max(0,slot.getMaxStackSize()-slot.getCount()));slot.grow(add);remaining-=add;}}
   for(int i=0;i<36&&remaining>0;i++)if(slots.get(i).isEmpty()){int add=Math.min(remaining,reward.getMaxStackSize());slots.set(i,reward.copyWithCount(add));remaining-=add;}
   if(remaining>0)return;
  }
  for(int i=0;i<36;i++)p.getInventory().setItem(i,slots.get(i));
  list.remove(0);p.getPersistentData().put(KEY,list);p.getInventory().setChanged();p.containerMenu.broadcastChanges();
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%40==0&&p.getPersistentData().contains(KEY))deliver(p);}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)deliver(p);}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){if(e.getOriginal().getPersistentData().contains(KEY))e.getEntity().getPersistentData().put(KEY,e.getOriginal().getPersistentData().get(KEY).copy());}
 private MagmaRewards(){}
}
