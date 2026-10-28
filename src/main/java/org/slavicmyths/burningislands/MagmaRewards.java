package org.slavicmyths.burningislands;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** One winner's constant-size aggregate inbox; full inventory keeps the entire batch, never drops it in lava. */
@EventBusSubscriber(modid="slavicmyths")
public final class MagmaRewards {
 private static final String KEY="slavicmyths_magma_rewards";
 private static final String CLAIMS="slavicmyths_magma_receipts",RECONCILE="slavicmyths_magma_reconcile_until";
 public static boolean queueData(CompoundTag tag,String claim,int cores){
  var receipts=tag.getCompound(CLAIMS);if(receipts.getBoolean(claim))return false;
  if(claim==null||claim.isBlank()||cores<4||cores>6)return false;
  var pending=tag.getCompound(KEY);long wins=pending.getLong("Wins"),oldCores=pending.getLong("Cores");
  if(wins<0||wins>=Long.MAX_VALUE/6||oldCores<0||oldCores>Long.MAX_VALUE-6||wins==0&&oldCores!=0||wins>0&&MagmaRewardRules.nextCores(wins,oldCores)==0)return false;
  pending.putLong("Wins",wins+1);pending.putLong("Cores",oldCores+cores);receipts.putBoolean(claim,true);
  // Both counters and receipt serialize in the SAME player file as inventory.
  tag.put(KEY,pending);tag.put(CLAIMS,receipts);return true;
 }
 public static void reconcile(ServerPlayer p){
  for(var level:p.getServer().getAllLevels())for(var delivery:MagmaEncounters.get(level).deliveries(p.getUUID(),level.getGameTime()))
   queueData(p.getPersistentData(),level.dimension().location()+":"+delivery.anchor(),delivery.cores());
  deliver(p);
 }
 public static void expect(ServerPlayer p){p.getPersistentData().putLong(RECONCILE,p.getServer().overworld().getGameTime()+160);}
 private static List<ItemStack> batch(int cores){
  var soul=new ItemStack(org.slavicmyths.rpg.RuneFoundation.item("soul"));
  var data=new CompoundTag();data.putBoolean("slavicmyths_infected_medium",true);
  soul.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
  soul.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.translatable("item.slavicmyths.infected_medium_soul"));
  return List.of(new ItemStack(org.slavicmyths.trophy.TrophyHeads.ITEMS.get(org.slavicmyths.trophy.TrophyHeads.Kind.MAGMA_LORD).get()),new ItemStack(IslandsContent.item("magma_lord_heart")),new ItemStack(IslandsContent.item("magma_core"),cores),soul);
 }
 private static void deliver(ServerPlayer p){
  var list=p.getPersistentData().getCompound(KEY);long wins=list.getLong("Wins"),cores=list.getLong("Cores");int count=MagmaRewardRules.nextCores(wins,cores);if(count==0)return;
  var source=new ArrayList<ItemStack>();for(int i=0;i<36;i++)source.add(p.getInventory().getItem(i));
  var slots=inventoryPlan(source,batch(count));if(slots==null)return;
  for(int i=0;i<36;i++)p.getInventory().setItem(i,slots.get(i));
  list.putLong("Wins",wins-1);list.putLong("Cores",cores-count);p.getPersistentData().put(KEY,list);p.getInventory().setChanged();p.containerMenu.broadcastChanges();
 }
 /** Pure admission on native stack copies, shared with offline full-inventory/metadata tests. */
 public static List<ItemStack> inventoryPlan(List<ItemStack> source,List<ItemStack> rewards){
  if(source.size()!=36)return null;
  var slots=new ArrayList<ItemStack>();source.forEach(s->slots.add(s.copy()));
  for(var reward:rewards){int remaining=reward.getCount();for(var slot:slots){if(ItemStack.isSameItemSameComponents(slot,reward)){int add=Math.min(remaining,Math.max(0,slot.getMaxStackSize()-slot.getCount()));slot.grow(add);remaining-=add;}}
   for(int i=0;i<36&&remaining>0;i++)if(slots.get(i).isEmpty()){int add=Math.min(remaining,reward.getMaxStackSize());slots.set(i,reward.copyWithCount(add));remaining-=add;}
   if(remaining>0)return null;
  }
  return slots;
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%40==0){if(p.getPersistentData().getLong(RECONCILE)>p.getServer().overworld().getGameTime())reconcile(p);else if(p.getPersistentData().contains(KEY))deliver(p);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){expect(p);reconcile(p);}}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){for(String key:List.of(KEY,CLAIMS,RECONCILE))if(e.getOriginal().getPersistentData().contains(key))e.getEntity().getPersistentData().put(key,e.getOriginal().getPersistentData().get(key).copy());}
 private MagmaRewards(){}
}
