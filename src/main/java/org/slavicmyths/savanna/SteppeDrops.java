package org.slavicmyths.savanna;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class SteppeDrops {
 @net.neoforged.bus.api.SubscribeEvent public static void onDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event){if(event.getEntity() instanceof SavannaAnimal a&&!org.slavicmyths.artifact.ArtifactEvents.summoned(a))drop(a,event.getSource(),event.getDrops());}
 static void drop(SavannaAnimal a,DamageSource source,java.util.Collection<net.minecraft.world.entity.item.ItemEntity> out){
  if(a.level().isClientSide||a.packId==null||a.packSlot<0||!(source.getEntity() instanceof Player)||!a.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))return;
  var tag=a.getPersistentData();if(tag.getBoolean("SteppeRewardClaimed"))return;tag.putBoolean("SteppeRewardClaimed",true);
  if(a.isPackBoss()){
   int rank=Math.floorMod(a.packSlot+Math.floorMod(a.packId.hashCode(),9),9);
   if(rank<5)give(a,out,"jackal_hide",1);
   if(Math.floorMod(rank+3,9)<4)give(a,out,"jackal_fang",1);
  }else if(a.hyenaRole()==SavannaAnimal.HyenaRole.LEADER){give(a,out,"black_hyena_hide",2+a.getRandom().nextInt(2));give(a,out,"black_hyena_fang",1+a.getRandom().nextInt(2));}
  else if(a.hyenaRole()==SavannaAnimal.HyenaRole.GUARD){give(a,out,"veteran_hyena_hide",1);give(a,out,"veteran_hyena_fang",1);}
 }
 private static void give(SavannaAnimal a,java.util.Collection<net.minecraft.world.entity.item.ItemEntity> out,String key,int count){out.add(new net.minecraft.world.entity.item.ItemEntity(a.level(),a.getX(),a.getY(),a.getZ(),new ItemStack(SteppeArmor.MATERIALS.get(key).get(),count)));}
 private SteppeDrops(){}
}
