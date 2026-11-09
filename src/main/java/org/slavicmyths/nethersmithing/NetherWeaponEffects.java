package org.slavicmyths.nethersmithing;
import net.minecraft.resources.ResourceLocation;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.*;import net.minecraft.world.entity.projectile.AbstractArrow;import net.minecraft.world.effect.MobEffects;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="slavicmyths")
public final class NetherWeaponEffects {
 private static final ResourceLocation ONE_HAND=ResourceLocation.fromNamespaceAndPath("slavicmyths","ashen_one_hand"),ARMOR=ResourceLocation.fromNamespaceAndPath("slavicmyths","shadow_knife_armor");
 public static void hands(LivingEntity e){if(e.level().isClientSide)return;var a=e.getAttribute(Attributes.ATTACK_DAMAGE);if(a==null)return;boolean penalty=e.getMainHandItem().getItem() instanceof NetherWeapons.Greatsword&&!e.getOffhandItem().isEmpty();if(penalty){if(a.getModifier(ONE_HAND)==null)a.addTransientModifier(new AttributeModifier(ONE_HAND,-6,AttributeModifier.Operation.ADD_VALUE));}else a.removeModifier(ONE_HAND);}
 public static void armorDebuff(LivingEntity e,boolean on){var a=e.getAttribute(Attributes.ARMOR);if(a==null)return;if(on){if(a.getModifier(ARMOR)==null)a.addTransientModifier(new AttributeModifier(ARMOR,-2,AttributeModifier.Operation.ADD_VALUE));}else{a.removeModifier(ARMOR);e.getPersistentData().remove("NetherKnifeArmorUntil");}}
 @SubscribeEvent public static void equipment(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent e){hands(e.getEntity());}
 @SubscribeEvent public static void attack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent e){hands(e.getEntity());}
 @SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){hands(e.getEntity());}
 @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e){hands(e.getEntity());}
 @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){hands(e.getEntity());}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){if(!(e.getEntity() instanceof LivingEntity living)||living.level().isClientSide||!living.getPersistentData().contains("NetherKnifeArmorUntil"))return;long now=((net.minecraft.server.level.ServerLevel)living.level()).getServer().overworld().getGameTime();armorDebuff(living,living.isAlive()&&living.getPersistentData().getLong("NetherKnifeArmorUntil")>now);}
 private static boolean legalArrow(ServerPlayer owner,LivingEntity target){
  return target!=owner&&target.isAlive()&&!target.isSpectator()&&!owner.isAlliedTo(target)&&org.slavicmyths.rpg.Abilities.canHit(owner,target);
 }
 @SubscribeEvent public static void arrow(net.neoforged.neoforge.event.entity.ProjectileImpactEvent event){
  if(!(event.getProjectile() instanceof AbstractArrow arrow)||arrow.level().isClientSide||arrow.getPersistentData().getFloat("InfernalRaw")<=0)return;
  if(!(event.getRayTraceResult() instanceof EntityHitResult hit)||!(hit.getEntity() instanceof LivingEntity target))return;
  if(arrow.getOwner() instanceof ServerPlayer owner && (target==owner||owner.isAlliedTo(target)||target.isSpectator())){
   event.setCanceled(true);arrow.discard();
  }
 }
 @SubscribeEvent public static void arrowHit(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){
  if(e.getNewDamage()<=0||!(e.getSource().getDirectEntity() instanceof AbstractArrow arrow)||arrow.level().isClientSide||arrow.getPersistentData().getFloat("InfernalRaw")<=0||!(arrow.getOwner() instanceof ServerPlayer p))return;
  boolean empowered=arrow.getPersistentData().getBoolean("InfernalEmpowered"),special=arrow.getPersistentData().getBoolean("InfernalSkill");
  int count=arrow.getPersistentData().getInt("InfernalHitCount");
  if(count>=(empowered&&special?2:1))return;
  var target=e.getEntity();if(!legalArrow(p,target))return;
  arrow.getPersistentData().putInt("InfernalHitCount",count+1);
  if(!target.fireImmune()&&!target.hasEffect(MobEffects.FIRE_RESISTANCE)){
   target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(),special?160:100));
   if(special){p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,target.getX(),target.getY()+.5,target.getZ(),6,.2,.2,.2,.01);org.slavicmyths.burningislands.TemporaryTerrain.fire(p,target);}
  }
  if(empowered&&special){
   var nearby=p.serverLevel().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(2.5),
      other->other!=target&&other.isAlive()&&legalArrow(p,other)&&p.hasLineOfSight(other));
   nearby.sort(java.util.Comparator.comparingDouble(other->other.distanceToSqr(target)));
   if(!nearby.isEmpty()){
    LivingEntity other=nearby.getFirst();
    if(other.hurt(p.damageSources().indirectMagic(p,p),Math.min(6F,e.getNewDamage()*.55F))){
     if(!other.fireImmune()&&!other.hasEffect(MobEffects.FIRE_RESISTANCE))other.setRemainingFireTicks(Math.max(other.getRemainingFireTicks(),100));
     other.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),p);
    }
   }
  }
  if(empowered&&target.isAlive()){
   target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),p);
   p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,target.getX(),target.getY()+.6,target.getZ(),8,.2,.3,.2,.01);
  }
 }
 @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){if(!(e.getItemStack().getItem() instanceof InfernalBow)&&NetherWeapons.owns(e.getItemStack())){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem()).getPath();e.getToolTip().add(net.minecraft.network.chat.Component.translatable("netherweapons.tip."+id).withStyle(net.minecraft.ChatFormatting.GRAY));}}
 private NetherWeaponEffects(){}
}
