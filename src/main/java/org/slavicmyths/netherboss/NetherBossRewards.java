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
 public static void grant(NetherSummonBoss boss,ServerLevel world,DamageSource source){
  String id=boss.getPersistentData().getString("RiteEncounter");if(id.isEmpty())return; // OP summons are never free legendary farms.
  var ledger=NetherBossLedger.get(world);var row=ledger.records.getCompound(id);
  if(!row.hasUUID("Boss")||!row.getUUID("Boss").equals(boss.getUUID())||row.getBoolean("Rewarded"))return;
  row.putBoolean("Rewarded",true);row.putString("State","ENDED");ledger.flush(world);
  LivingEntity killer=source.getEntity() instanceof LivingEntity living?living:boss.getKillCredit();
  int looting=killer==null?0:EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING),killer.getMainHandItem());
  for(String item:roll(boss.isMorova(),looting,boss.getRandom()))boss.spawnAtLocation(new ItemStack(NetherBossItems.item(item)));
 }
 private NetherBossRewards(){}
}
