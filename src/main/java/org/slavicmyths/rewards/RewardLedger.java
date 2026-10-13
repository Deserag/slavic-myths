package org.slavicmyths.rewards;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;

/** Save-bound, player-bound inbox. History is read; absent victories are never manufactured. */
public final class RewardLedger extends SavedData {
 public static final String[] BOSSES={"idolishche_poganoe","marinka","skim_beast","kartaus","kalin"};
 public static final String[] MATERIAL={"mat_bone_plates","mat_mirror_shards","mat_skim_fang","mat_banner_remnant","mat_royal_iron"};
 public static final String[] GIFTS={"gift_ilya_shield","gift_dobrynya_ring","gift_alyosha_belt","gift_finist_amulet"};
 public static final int[] FIRST={4,4,3,4,5};
 public UUID saveId=UUID.randomUUID();
 public final Map<UUID,CompoundTag> players=new HashMap<>();
 public static RewardLedger get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(RewardLedger::new,RewardLedger::load),"slavicmyths_rewards_176");}
 public CompoundTag player(UUID id){return players.computeIfAbsent(id,k->new CompoundTag());}
 public static CompoundTag state(ServerPlayer p){var n=p.getPersistentData();String key="slavicmyths.reward_claims176";if(!n.contains(key))n.put(key,new CompoundTag());var state=n.getCompound(key);get(p.serverLevel()).players.put(p.getUUID(),state);return state;}
 public String claimId(UUID player,int boss){return saveId+":"+player+":slavicmyths:"+BOSSES[boss]+":1.7.6";}
 public static CompoundTag history(ServerPlayer p,int index){var w=p.server.getLevel(net.minecraft.world.level.Level.OVERWORLD);return switch(index){
  case 0->org.slavicmyths.bogatyr.BogatyrLedger.get(w).victories.getOrDefault(p.getUUID(),new CompoundTag());
  case 1->org.slavicmyths.marinka.WitchYard.get(w).progress.getOrDefault(p.getUUID(),new CompoundTag());
  case 2->org.slavicmyths.skim.SkimValley.get(w).progress.getOrDefault(p.getUUID(),new CompoundTag());
  case 3->p.getPersistentData();
  default->org.slavicmyths.kalin.KalinEncounter.get(w).progress.getOrDefault(p.getUUID(),new CompoundTag());};}
 public static int count(ServerPlayer p,int i){var n=history(p,i);return switch(i){case 0->n.getBoolean("participationFlag")?n.getInt("defeatCount"):0;case 1->n.getBoolean("marinka_defeated")?n.getInt("DefeatCount"):0;case 2->n.getBoolean("skim_beast_defeated")?n.getInt("defeat_count"):0;case 3->n.getBoolean("slavicmyths.story.finist_kartaus.victory")?n.getInt("slavicmyths.story.finist_kartaus.clear_count"):0;default->n.getBoolean("kalin_battle_won")?n.getInt("times_kalin_defeated"):0;};}
 public static boolean giftEligible(ServerPlayer p,int i){if(i<0||i>=4||count(p,i)<=0)return false;var n=history(p,i);return switch(i){case 0->n.getList("usedHeroIds",Tag.TAG_STRING).contains(StringTag.valueOf("ilya_muromets"));case 1->n.getBoolean("dobrynya_released");case 2->n.getBoolean("ally_participation_alyosha");default->n.getBoolean("slavicmyths.story.finist_kartaus.finist_participated");};}
 /** Only new authenticated completion callbacks roll repeat loot. Login only reconciles first claims. */
 public static void victory(ServerPlayer p,int i){var d=get(p.serverLevel());var state=state(p);int count=count(p,i);String key="Seen"+i;if(count<=state.getInt(key))return;state.putInt(key,count);if(count>1){var pending=state.getList("Pending",Tag.TAG_COMPOUND);append(pending,MATERIAL[i],(i==4?2:1)+p.getRandom().nextInt(2));if(p.getRandom().nextFloat()<.15F)append(pending,"rune_"+RewardContent.RUNES.get(i),1);state.put("Pending",pending);}d.setDirty();p.sendSystemMessage(Component.translatable("reward176.available"));}
 public static void reconcile(ServerPlayer p){var d=get(p.serverLevel());var n=state(p);for(int i=0;i<5;i++)if(!n.contains("Seen"+i))n.putInt("Seen"+i,count(p,i));d.setDirty();}
 private static void append(ListTag list,String item,int count){var n=new CompoundTag();n.putString("Item",item);n.putInt("Count",count);list.add(n);}
 /** Full bundle admission on copies: no partial delivery, no unbound drop on failure. */
 public static boolean deliver(ServerPlayer p,ListTag bundle){List<ItemStack> slots=new ArrayList<>();for(int i=0;i<36;i++)slots.add(p.getInventory().getItem(i).copy());for(var t:bundle){var n=(CompoundTag)t;ItemStack reward=new ItemStack(RewardContent.item(n.getString("Item")),n.getInt("Count"));for(var s:slots)if(ItemStack.isSameItemSameComponents(s,reward)){int take=Math.min(reward.getCount(),s.getMaxStackSize()-s.getCount());s.grow(take);reward.shrink(take);}for(int i=0;i<36&&!reward.isEmpty();i++)if(slots.get(i).isEmpty()){int take=Math.min(reward.getCount(),reward.getMaxStackSize());slots.set(i,reward.copyWithCount(take));reward.shrink(take);}if(!reward.isEmpty())return false;}for(int i=0;i<36;i++)p.getInventory().setItem(i,slots.get(i));p.getInventory().setChanged();return true;}
 public static boolean claimFirst(ServerPlayer p,int i){if(i<0||i>=5||count(p,i)<=0)return false;var d=get(p.serverLevel());var n=state(p);String key="First"+i;if(n.getBoolean(key))return false;var list=new ListTag();append(list,"rune_"+RewardContent.RUNES.get(i),1);append(list,MATERIAL[i],FIRST[i]);if(!deliver(p,list)){p.sendSystemMessage(Component.translatable("reward176.full"));return false;}n.putBoolean(key,true);RewardEvents.fx(p,"reward_claim_success");n.putString("Claim"+i,d.claimId(p.getUUID(),i));d.setDirty();return true;}
 public static boolean claimGift(ServerPlayer p,int i){if(!giftEligible(p,i))return false;var d=get(p.serverLevel());var n=state(p);String key="Gift"+i;if(n.getBoolean(key))return false;var list=new ListTag();append(list,GIFTS[i],1);if(!deliver(p,list)){p.sendSystemMessage(Component.translatable("reward176.full"));return false;}n.putBoolean(key,true);RewardEvents.fx(p,"reward_claim_success");n.putString("GiftClaim"+i,d.claimId(p.getUUID(),i)+":hero");d.setDirty();p.sendSystemMessage(Component.translatable("reward176.gift_received"));return true;}
 public static void claimInbox(ServerPlayer p){reconcile(p);for(int i=0;i<5;i++)claimFirst(p,i);var d=get(p.serverLevel());var n=state(p);var pending=n.getList("Pending",Tag.TAG_COMPOUND);while(!pending.isEmpty()){var one=new ListTag();one.add(pending.get(0).copy());if(!deliver(p,one))break;pending.remove(0);}n.put("Pending",pending);d.setDirty();org.slavicmyths.tales.TaleNetwork.send(p,org.slavicmyths.tales.TaleService.snapshot(p),false,true);}
 public static int available(ServerPlayer p){var d=get(p.serverLevel());var n=state(p);int count=n.getList("Pending",Tag.TAG_COMPOUND).size();for(int i=0;i<5;i++)if(count(p,i)>0&&!n.getBoolean("First"+i))count++;return count;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider p){n.putUUID("Save",saveId);var list=new ListTag();players.forEach((id,state)->{var t=state.copy();t.putUUID("Player",id);list.add(t);});n.put("Players",list);return n;}
 public static RewardLedger load(CompoundTag n,HolderLookup.Provider p){var d=new RewardLedger();if(n.hasUUID("Save"))d.saveId=n.getUUID("Save");for(var t:n.getList("Players",Tag.TAG_COMPOUND)){var a=(CompoundTag)t;if(a.hasUUID("Player"))d.players.put(a.getUUID("Player"),a.copy());}return d;}
}
