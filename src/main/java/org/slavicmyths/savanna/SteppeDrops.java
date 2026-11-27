package org.slavicmyths.savanna;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
final class SteppeDrops {
 static void drop(SavannaAnimal a,DamageSource source){
  if(a.level().isClientSide||a.packId==null||a.packSlot<0||!(source.getEntity() instanceof Player)||!a.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))return;
  var tag=a.getPersistentData();if(tag.getBoolean("SteppeRewardClaimed"))return;tag.putBoolean("SteppeRewardClaimed",true);
  if(a.isPackBoss()){
   int rank=Math.floorMod(a.packSlot+Math.floorMod(a.packId.hashCode(),9),9);
   if(rank<5)give(a,"jackal_hide",1);
   if(Math.floorMod(rank+3,9)<4)give(a,"jackal_fang",1);
  }else if(a.hyenaRole()==SavannaAnimal.HyenaRole.LEADER){give(a,"black_hyena_hide",2+a.getRandom().nextInt(2));give(a,"black_hyena_fang",1+a.getRandom().nextInt(2));}
  else if(a.hyenaRole()==SavannaAnimal.HyenaRole.GUARD){give(a,"veteran_hyena_hide",1);give(a,"veteran_hyena_fang",1);}
 }
 private static void give(SavannaAnimal a,String key,int count){a.spawnAtLocation(new ItemStack(SteppeArmor.MATERIALS.get(key).get(),count));}
 private SteppeDrops(){}
}
