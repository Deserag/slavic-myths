package org.slavicmyths.netherboss;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.core.registries.Registries;

/** One shared soul, one scroll, exactly one guaranteed pair item plus the missing second. */
public final class NetherBossRewards {
 public static double secondChance(int looting){return Math.min(.5,.2+.1*Math.max(0,looting));}
 public static List<String> roll(boolean morova,int looting,RandomSource random){
  String first=morova?"morova_silence_pendant":"fallen_druzhina_horn",second=morova?"rune_morova_lingering":"rune_trizna_ember_ward";
  if(random.nextBoolean()){var temp=first;first=second;second=temp;}
  return random.nextDouble()<secondChance(looting)?List.of("legendary_soul",morova?"scroll_of_waning":"scroll_of_last_call",first,second):List.of("legendary_soul",morova?"scroll_of_waning":"scroll_of_last_call",first);
 }
 public static List<ItemStack> batch(boolean morova,int pairMask){
  if(pairMask<1||pairMask>3)throw new IllegalArgumentException("Invalid legendary pair mask");
  var items=new java.util.ArrayList<ItemStack>();items.add(new ItemStack(NetherBossItems.item("legendary_soul")));items.add(new ItemStack(NetherBossItems.item(morova?"scroll_of_waning":"scroll_of_last_call")));
  if((pairMask&1)!=0)items.add(new ItemStack(NetherBossItems.item(morova?"morova_silence_pendant":"fallen_druzhina_horn")));
  if((pairMask&2)!=0)items.add(new ItemStack(NetherBossItems.item(morova?"rune_morova_lingering":"rune_trizna_ember_ward")));
  return List.copyOf(items);
 }
 public static void grant(NetherSummonBoss boss,ServerLevel world,DamageSource source){
  String id=boss.getPersistentData().getString("RiteEncounter");if(id.isEmpty())return;
  var ledger=NetherBossLedger.get(world);var row=ledger.records.getCompound(id);
  if(!row.hasUUID("Boss")||!row.getUUID("Boss").equals(boss.getUUID())||row.getBoolean("Rewarded"))return;
  LivingEntity killer=source.getEntity() instanceof LivingEntity living?living:boss.getKillCredit();
  java.util.UUID recipient=killer instanceof net.minecraft.server.level.ServerPlayer player?player.getUUID():
   killer instanceof NetherRetainer retainer&&retainer.playerOwned()?retainer.owner():row.hasUUID("Owner")?row.getUUID("Owner"):null;
  if(recipient==null)return;
  int looting=killer==null?0:EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING),killer.getMainHandItem());
  int mask=0;for(String item:roll(boss.isMorova(),looting,boss.getRandom())){if(item.equals("morova_silence_pendant")||item.equals("fallen_druzhina_horn"))mask|=1;if(item.startsWith("rune_"))mask|=2;}
  String key="nether_21x:"+world.dimension().location()+":"+id;
  var pin=org.slavicmyths.losthalls.BossRewardInbox.prepare(world.getServer(),key,boss.isMorova()?"morova_21x":"trizna_21x",recipient,mask,world.getServer().overworld().getGameTime());
  if(pin==null)return;
  row.putBoolean("Rewarded",true);row.putString("State","ENDED");ledger.flush(world);
  var player=world.getServer().getPlayerList().getPlayer(pin.recipient());if(player!=null)org.slavicmyths.losthalls.BossRewardInbox.reconcile(player);
 }
 private NetherBossRewards(){}
}
