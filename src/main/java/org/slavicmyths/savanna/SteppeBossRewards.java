package org.slavicmyths.savanna;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.slavicmyths.artifact.ArtifactEvents;
import org.slavicmyths.savanna.SteppeWeapons.Kind;
@EventBusSubscriber(modid="slavicmyths")
public final class SteppeBossRewards{
 @SubscribeEvent public static void drops(LivingDropsEvent e){
  LivingEntity x=e.getEntity();if(x.level().isClientSide||!(x instanceof Polkan||x instanceof Div||x instanceof Skarapeya)||ArtifactEvents.summoned(x))return;
  var killer=e.getSource().getEntity();if(!(killer instanceof Player)&&!(killer instanceof LivingEntity l&&ArtifactEvents.owner(l)!=null))return;
  if(!x.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)||x.getPersistentData().getBoolean("SteppeBossRewardsClaimed"))return;x.getPersistentData().putBoolean("SteppeBossRewardsClaimed",true);
  if(x instanceof Polkan){weapon(e,x.getRandom().nextBoolean()?Kind.POLKAN_SPEAR:Kind.POLKAN_BOW);material(e,"polkan_seal",2);}
  else if(x instanceof Div){weapon(e,Kind.DIV_CLAWS);material(e,"div_feather",4+x.getRandom().nextInt(3));material(e,"div_eye",2);}
  else {weapon(e,x.getRandom().nextBoolean()?Kind.SKARAPEYA_BLADE:Kind.SKARAPEYA_GUSLI);material(e,"skarapeya_scale",5+x.getRandom().nextInt(3));material(e,"skarapeya_gland",2);material(e,"skarapeya_fang",2);}
 }
 private static void weapon(LivingDropsEvent e,Kind k){give(e,new ItemStack(SteppeWeapons.item(k)));}
 private static void material(LivingDropsEvent e,String id,int n){give(e,new ItemStack(SteppeWeapons.MATERIALS.get(id).get(),n));}
 private static void give(LivingDropsEvent e,ItemStack s){var x=e.getEntity();var drop=new ItemEntity(x.level(),x.getX(),x.getY()+.5,x.getZ(),s);drop.setDefaultPickUpDelay();e.getDrops().add(drop);}
 private SteppeBossRewards(){}
}
