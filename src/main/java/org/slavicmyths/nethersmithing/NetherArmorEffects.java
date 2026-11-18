package org.slavicmyths.nethersmithing;
import net.minecraft.resources.ResourceLocation;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.*;import net.minecraft.world.entity.player.Player;import net.minecraft.tags.DamageTypeTags;import net.minecraft.world.damagesource.DamageTypes;import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.event.entity.living.*;
@EventBusSubscriber(modid="slavicmyths")
public final class NetherArmorEffects {
 private static final ResourceLocation SPEED=id("king_set_speed"),KB=id("king_set_kb"),FALL=id("king_soft_fall");
 private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 private static void modifier(LivingEntity e,net.minecraft.core.Holder<Attribute> key,ResourceLocation id,double value,AttributeModifier.Operation op,boolean on){var a=e.getAttribute(key);if(a==null)return;if(!on){a.removeModifier(id);return;}if(a.getModifier(id)==null)a.addTransientModifier(new AttributeModifier(id,value,op));}
 public static void refresh(LivingEntity e){if(e.level().isClientSide)return;boolean full=NetherArmor.full(e);modifier(e,Attributes.MOVEMENT_SPEED,SPEED,.05,AttributeModifier.Operation.ADD_MULTIPLIED_BASE,full);modifier(e,Attributes.KNOCKBACK_RESISTANCE,KB,.25,AttributeModifier.Operation.ADD_VALUE,full);if(!NetherArmor.king(e,EquipmentSlot.FEET)||!e.isAlive())modifier(e,Attributes.GRAVITY,FALL,0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,false);}
 @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void death(LivingDeathEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){var p=event.getEntity();if(p.level().isClientSide)return;boolean boots=NetherArmor.king(p,EquipmentSlot.FEET);boolean falling=boots&&p.isAlive()&&!p.onGround()&&!p.isFallFlying()&&!p.getAbilities().flying&&!p.isInWaterOrBubble()&&p.getDeltaMovement().y<0;var gravity=p.getAttribute(Attributes.GRAVITY);if(boots||gravity!=null&&gravity.getModifier(FALL)!=null){modifier(p,Attributes.GRAVITY,FALL,-.875,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,falling);if(falling)p.fallDistance=0;}if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());}
 @SubscribeEvent public static void fire(LivingIncomingDamageEvent e){if(!e.getEntity().level().isClientSide&&NetherArmor.full(e.getEntity())&&e.getEntity().level().dimension().equals(Level.NETHER)&&e.getSource().is(DamageTypeTags.IS_FIRE)&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)){e.getEntity().clearFire();e.setCanceled(true);}}
 @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;var source=e.getSource();if(NetherArmor.king(e.getEntity(),EquipmentSlot.CHEST)&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)&&!source.is(DamageTypeTags.BYPASSES_ARMOR))e.setNewDamage(e.getNewDamage()*.75F);
  if(source.getEntity() instanceof Player p&&NetherArmor.king(p,EquipmentSlot.HEAD)&&!source.is(DamageTypes.THORNS)&&!source.is(DamageTypeTags.IS_FIRE)&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)&&(source.getDirectEntity()==p||source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile&&projectile.getOwner()==p))e.setNewDamage(e.getNewDamage()*1.2F);
 }
 private NetherArmorEffects(){}
}
