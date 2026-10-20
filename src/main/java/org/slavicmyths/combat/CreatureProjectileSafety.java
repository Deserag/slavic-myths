package org.slavicmyths.combat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
/** Delayed projectile hits retain their real owner's team/PvP/ally checks. Native hurt/encounter gates stay authoritative. */
@EventBusSubscriber(modid="slavicmyths")
public final class CreatureProjectileSafety {
 @SubscribeEvent(priority=EventPriority.HIGH) public static void hit(LivingIncomingDamageEvent e){
  if(e.getEntity().level().isClientSide||!(e.getSource().getDirectEntity() instanceof Projectile shot)||!(e.getSource().getEntity() instanceof LivingEntity owner))return;
  boolean ours=BuiltInRegistries.ENTITY_TYPE.getKey(shot.getType()).getNamespace().equals("slavicmyths")||BuiltInRegistries.ENTITY_TYPE.getKey(owner.getType()).getNamespace().equals("slavicmyths")||owner instanceof Villager v&&org.slavicmyths.military.Military.guard(v);
  if(ours&&!CombatTargeting.canAttack(owner,e.getEntity(),CombatTargeting.Context.NATIVE_DAMAGE))e.setCanceled(true);
 }
 private CreatureProjectileSafety(){}
}
