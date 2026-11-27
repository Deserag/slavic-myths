package org.slavicmyths.savanna;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class SteppeArmorEvents {
 private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath("slavicmyths","steppe_cloak_speed");
 @SubscribeEvent public static void crit(CriticalHitEvent event){
  var p=event.getEntity();if(p.level().isClientSide||!(event.getTarget() instanceof LivingEntity)||p.getAttackStrengthScale(.5F)<.9F)return;
  int pieces=SteppeArmor.count(p);boolean cloak=SteppeCloak.worn(p);
  boolean weapon=SteppeWeapons.isStandard(p.getMainHandItem())||SteppeWeapons.kind(p.getMainHandItem())==SteppeWeapons.Kind.JACKAL_DAGGERS;
  if(!event.isCriticalHit()&&(pieces>0||cloak||weapon)&&p.getRandom().nextFloat()<.05F*(pieces+(cloak?1:0)+(weapon?1:0))){event.setCriticalHit(true);event.setDamageMultiplier(1.5F);event.setDisableSweep(true);}
  if(pieces==4&&event.isCriticalHit())event.setDamageMultiplier(event.getDamageMultiplier()*1.25F);
 }
 @SubscribeEvent public static void visibility(LivingEvent.LivingVisibilityEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  if(SteppeArmor.full(p))event.modifyVisibility(.65);
  if(SteppeCloak.worn(p))event.modifyVisibility(.85);
 }
 @SubscribeEvent public static void dodge(LivingIncomingDamageEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p)||!SteppeArmor.full(p)||event.getAmount()<=0)return;
  var source=event.getSource();if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
  var direct=source.getDirectEntity();if(direct==null||(!(direct instanceof LivingEntity)&&!(direct instanceof Projectile)))return;
  if(p.getRandom().nextFloat()<.12F){event.setCanceled(true);p.displayClientMessage(net.minecraft.network.chat.Component.literal("Уклонение!"),true);}
 }
 @SubscribeEvent public static void damage(LivingDamageEvent.Pre event){
  if(event.getEntity().level().isClientSide||event.getNewDamage()<=0)return;
  if(event.getSource().getEntity() instanceof ServerPlayer p&&SteppeCloak.worn(p))event.setNewDamage(event.getNewDamage()*1.10F);
  else if(event.getSource().getEntity() instanceof LivingEntity e&&org.slavicmyths.artifact.ArtifactEvents.summoned(e)&&org.slavicmyths.artifact.ArtifactEvents.owner(e) instanceof ServerPlayer p&&SteppeCloak.worn(p))event.setNewDamage(event.getNewDamage()*1.15F);
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%10!=0)return;
  var a=p.getAttribute(Attributes.MOVEMENT_SPEED);if(a==null)return;
  boolean worn=SteppeCloak.worn(p);
  if(worn&&a.getModifier(SPEED)==null)a.addTransientModifier(new AttributeModifier(SPEED,.10,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
  else if(!worn&&a.getModifier(SPEED)!=null)a.removeModifier(SPEED);
 }
 private SteppeArmorEvents(){}
}
