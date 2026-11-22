package org.slavicmyths.rewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Three successful charged melee hits prepare a +3 fourth hit; waves never feed the combo. */
@EventBusSubscriber(modid="slavicmyths")
public final class KalinSabreCombo {
 private static boolean valid(net.minecraft.world.damagesource.DamageSource source){return source.is(DamageTypes.PLAYER_ATTACK)&&source.getDirectEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(RewardContent.item("trophy_kalin_sabre"))&&p.getPersistentData().getFloat("RareAttackCharge")>=.9F;}
 @SubscribeEvent public static void damage(LivingDamageEvent.Pre event){
  if(event.getEntity().level().isClientSide||event.getNewDamage()<=0||!valid(event.getSource()))return;
  var p=(ServerPlayer)event.getSource().getDirectEntity();var data=p.getPersistentData();
  if(data.getLong("KalinSabreComboUntil")>p.level().getGameTime()&&data.getInt("KalinSabreCombo")>=3)event.setNewDamage(event.getNewDamage()+3);
 }
 @SubscribeEvent public static void hit(LivingDamageEvent.Post event){
  if(event.getEntity().level().isClientSide||event.getNewDamage()<=0||!valid(event.getSource()))return;
  var p=(ServerPlayer)event.getSource().getDirectEntity();var data=p.getPersistentData();long now=p.level().getGameTime();
  int count=data.getLong("KalinSabreComboUntil")>now?data.getInt("KalinSabreCombo"):0;
  data.putInt("KalinSabreCombo",count>=3?0:count+1);data.putLong("KalinSabreComboUntil",now+100);
 }
 private KalinSabreCombo(){}
}
