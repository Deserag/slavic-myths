package org.slavicmyths.hellvisualqa;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths_hellvisualqa")
public final class BossFightContacts {
 @net.neoforged.bus.api.SubscribeEvent public static void damage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event){
  if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer&&event.getSource().getEntity() instanceof org.slavicmyths.netherboss.NetherSummonBoss boss)BossClientFight.recordDamage(boss,event.getNewDamage());
 }
}
