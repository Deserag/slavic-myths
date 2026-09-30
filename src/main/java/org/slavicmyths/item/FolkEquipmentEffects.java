package org.slavicmyths.item;
import java.util.UUID;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class FolkEquipmentEffects {
 private static final UUID SPEED=UUID.fromString("e058a1ec-73b8-4309-92d7-e917d4fdbf52");
 public static boolean wears(LivingEntity p,net.minecraft.item.Item item){return !FolkAccessoryItem.equipped(p,item).isEmpty();}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
  PlayerEntity p=event.player;if(event.phase!=TickEvent.Phase.END||p.level.isClientSide)return;
  CompoundNBT data=p.getPersistentData();ModifiableAttributeInstance speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
  boolean boots=p.getItemBySlot(EquipmentSlotType.FEET).getItem()==ModItems.SEVEN_LEAGUE_BOOTS.get();
  int ramp=data.getInt("SlavicBootRamp");
  if(boots&&p.isSprinting()&&p.isOnGround()&&!p.isInWater()&&Math.abs(MathHelper.wrapDegrees(p.yRot-data.getFloat("SlavicBootYaw")))<18&&p.getDeltaMovement().lengthSqr()>.006)ramp=Math.min(60,ramp+1);else ramp=0;
  if(ramp!=data.getInt("SlavicBootRamp")){
   speed.removeModifier(SPEED);if(ramp>0)speed.addTransientModifier(new AttributeModifier(SPEED,"Seven league stride",.3*ramp/60,AttributeModifier.Operation.MULTIPLY_TOTAL));
   data.putInt("SlavicBootRamp",ramp);
  }else if(!boots&&speed.getModifier(SPEED)!=null)speed.removeModifier(SPEED);
  if(boots)data.putFloat("SlavicBootYaw",p.yRot);
  if(ramp>30&&p.tickCount%8==0)((ServerWorld)p.level).sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.1,p.getZ(),1,.08,.02,.08,.005);
 }
 @SubscribeEvent public static void hurt(LivingHurtEvent event){
  LivingEntity victim=event.getEntityLiving();if(victim.level.isClientSide)return;
  if(event.getSource()==DamageSource.LIGHTNING_BOLT&&wears(victim,ModItems.PERUN_RING.get()))event.setAmount(event.getAmount()*.65F);
  Entity source=event.getSource().getEntity();
  if(source instanceof LivingEntity&&source!=victim){
   LivingEntity attacker=(LivingEntity)source;
   if(event.getSource().getDirectEntity()==source&&wears(attacker,ModItems.PERUN_RING.get())){
    long now=attacker.level.getGameTime();CompoundNBT d=attacker.getPersistentData();
    if(now>=d.getLong("SlavicPerunReady")){event.setAmount(event.getAmount()+2);d.putLong("SlavicPerunReady",now+60);((ServerWorld)victim.level).sendParticles(ParticleTypes.CRIT,victim.getX(),victim.getY()+1,victim.getZ(),6,.2,.3,.2,.1);}
   }
   if(event.getSource().isProjectile()&&victim instanceof org.slavicmyths.entity.WildlifeEntity&&wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.1F);
   attacker.getPersistentData().putLong("SlavicCapBroken",attacker.level.getGameTime()+100);
  }
  victim.getPersistentData().putLong("SlavicCapBroken",victim.level.getGameTime()+100);
 }
 @SubscribeEvent public static void death(LivingDeathEvent event){
  LivingEntity e=event.getEntityLiving();if(e.level.isClientSide||event.getSource()==DamageSource.OUT_OF_WORLD)return;
  ItemStack charm=FolkAccessoryItem.equipped(e,ModItems.RETRIBUTION_CHARM.get());if(charm.isEmpty())return;
  charm.shrink(1);event.setCanceled(true);e.setHealth(4);e.addEffect(new EffectInstance(Effects.REGENERATION,100,1));e.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,60,1));
  ((ServerWorld)e.level).sendParticles(ParticleTypes.TOTEM_OF_UNDYING,e.getX(),e.getY()+1,e.getZ(),25,.5,.5,.5,.1);
  Entity enemy=event.getSource().getEntity();if(enemy instanceof LivingEntity&&enemy.isAlive()&&e.distanceToSqr(enemy)<36)((LivingEntity)enemy).hurt(DamageSource.thorns(e),6);
 }
 private FolkEquipmentEffects(){}
}
