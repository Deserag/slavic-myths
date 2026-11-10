package org.slavicmyths.ninemor;

import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid="slavicmyths")
public final class NineMorRewardAbilities {
 private static final String LAST="SlavicNineMorLastVictim";
 private static final String BARRIER="SlavicNineMorBarrierReady";
 private static final String DOME="SlavicNineMorDomeReady";
 private static final String NINTH_LIFE="SlavicNineMorNinthLifeReady";
 private static final DustParticleOptions MOR=new DustParticleOptions(new Vector3f(.44F,.13F,.72F),1.25F);

 @SubscribeEvent public static void critical(CriticalHitEvent event){
  Player player=event.getEntity();
  if(!(player instanceof ServerPlayer)||!player.getMainHandItem().is(NineMorContent.SPEAR.get()))return;
  event.setCriticalHit(true);
  event.setDamageMultiplier(Math.max(1.5F,event.getDamageMultiplier()));
 }

 @SubscribeEvent public static void remember(LivingDamageEvent.Post event){
  if(!(event.getSource().getEntity() instanceof ServerPlayer player)||!event.getSource().is(DamageTypes.PLAYER_ATTACK)
      ||!player.getMainHandItem().is(NineMorContent.SPEAR.get())||event.getEntity()==player)return;
  if(event.getEntity().isAlive())player.getPersistentData().putUUID(LAST,event.getEntity().getUUID());
 }

 private static boolean eligible(ServerPlayer player,LivingEntity victim){
  return victim!=player&&victim.isAlive()&&!victim.isAlliedTo(player)&&!player.isAlliedTo(victim)
      &&!victim.getPersistentData().getBoolean("SlavicViySummoned")
      &&!(victim instanceof Player p&&(p.isCreative()||p.isSpectator()||!player.canHarmPlayer(p)));
 }

 public static InteractionResultHolder<ItemStack> spear(Level world,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(!(player instanceof ServerPlayer server))return InteractionResultHolder.consume(stack);
  if(server.getCooldowns().isOnCooldown(stack.getItem()))return InteractionResultHolder.fail(stack);
  if(server.isShiftKeyDown()){
   int hit=0;
   for(LivingEntity target:server.serverLevel().getEntitiesOfClass(LivingEntity.class,server.getBoundingBox().inflate(4.3D),e->eligible(server,e)&&e.distanceToSqr(server)<=20D)){
    if(target.hurt(server.damageSources().playerAttack(server),10F)){
     NineMorSerpent.applyElement(target,(hit++%9));
     Vec3 delta=target.position().subtract(server.position());
     if(delta.horizontalDistanceSqr()>.01D)target.knockback(.55D,-delta.x,-delta.z);
    }
    if(hit>=12)break;
   }
   if(hit==0)return InteractionResultHolder.fail(stack);
   server.getCooldowns().addCooldown(stack.getItem(),125);
   server.serverLevel().sendParticles(MOR,server.getX(),server.getY()+1D,server.getZ(),32,2D,.55D,2D,.025D);
   server.playSound(SoundEvents.PLAYER_ATTACK_SWEEP,1.2F,.65F);
   return InteractionResultHolder.consume(stack);
  }
  if(!server.getPersistentData().hasUUID(LAST))return InteractionResultHolder.fail(stack);
  UUID id=server.getPersistentData().getUUID(LAST);
  if(!(server.serverLevel().getEntity(id) instanceof LivingEntity target)||!eligible(server,target)||target.distanceToSqr(server)>36D*36D||!server.hasLineOfSight(target))return InteractionResultHolder.fail(stack);
  Vec3 look=server.getLookAngle(),right=new Vec3(-look.z,0,look.x).normalize();
  if(right.lengthSqr()<.01D)right=new Vec3(1,0,0);
  for(int i=0;i<4;i++){
   Vec3 from=server.position().add(0,2D,0).subtract(look.scale(1.8D)).add(right.scale((i-1.5D)*.75D));
   Vec3 aim=target.getEyePosition().subtract(from);
   NineMorShot shot=new NineMorShot(server.level(),server,9);
   shot.setPos(from.x,from.y,from.z);
   shot.shoot(aim.x,aim.y,aim.z,1.7F,.08F);
   server.serverLevel().addFreshEntity(shot);
   server.serverLevel().sendParticles(MOR,from.x,from.y,from.z,4,.2,.2,.2,.01);
  }
  server.getCooldowns().addCooldown(stack.getItem(),185);
  server.playSound(SoundEvents.TRIDENT_THROW,1.2F,.7F);
  return InteractionResultHolder.consume(stack);
 }

 public static InteractionResultHolder<ItemStack> axe(Level world,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(!(player instanceof ServerPlayer server))return InteractionResultHolder.consume(stack);
  if(server.getCooldowns().isOnCooldown(stack.getItem()))return InteractionResultHolder.fail(stack);
  Vec3 look=server.getLookAngle().multiply(1D,0D,1D);
  if(look.lengthSqr()<.01D)return InteractionResultHolder.fail(stack);
  look=look.normalize();
  if(server.isShiftKeyDown()){
   LivingEntity best=null;double bestDistance=Double.MAX_VALUE;
   for(LivingEntity target:server.serverLevel().getEntitiesOfClass(LivingEntity.class,server.getBoundingBox().inflate(8D),e->eligible(server,e))){
    Vec3 delta=target.position().subtract(server.position());
    double forward=delta.x*look.x+delta.z*look.z,side=Math.abs(delta.x*look.z-delta.z*look.x);
    if(forward<0||forward>8D||side>1.8D||!server.hasLineOfSight(target)||forward>=bestDistance)continue;
    best=target;bestDistance=forward;
   }
   if(best==null||best.getHealth()>best.getMaxHealth()*.35F)return InteractionResultHolder.fail(stack);
   best.hurt(server.damageSources().playerAttack(server),Math.min(55F,best.getMaxHealth()*.28F));
   best.addEffect(new MobEffectInstance(MobEffects.WITHER,90,1));
   server.serverLevel().sendParticles(MOR,best.getX(),best.getY()+1,best.getZ(),36,.9D,1D,.9D,.055D);
   server.getCooldowns().addCooldown(stack.getItem(),300);
   server.playSound(SoundEvents.WITHER_BREAK_BLOCK,1.0F,.65F);
   return InteractionResultHolder.consume(stack);
  }
  int hit=0;
  for(LivingEntity target:server.serverLevel().getEntitiesOfClass(LivingEntity.class,server.getBoundingBox().inflate(12D),e->eligible(server,e))){
   Vec3 delta=target.position().subtract(server.position());
   double forward=delta.x*look.x+delta.z*look.z,side=Math.abs(delta.x*look.z-delta.z*look.x);
   if(forward<1D||forward>11D||side>1.8D||!server.hasLineOfSight(target))continue;
   if(target.hurt(server.damageSources().playerAttack(server),14F)){
    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1));
    if(++hit>=12)break;
   }
  }
  Vec3 start=server.position().add(0,.15D,0);
  for(int k=1;k<=11;k++){
   Vec3 point=start.add(look.scale(k));
   server.serverLevel().sendParticles(MOR,point.x,point.y,point.z,4,.9D,.15D,.9D,.02D);
  }
  server.getCooldowns().addCooldown(stack.getItem(),175);
  server.playSound(SoundEvents.WITHER_SHOOT,1F,.7F);
  return InteractionResultHolder.consume(stack);
 }

 public static InteractionResultHolder<ItemStack> shield(Level world,Player player,InteractionHand hand){
  ItemStack stack=player.getItemInHand(hand);
  if(!(player instanceof ServerPlayer server)||!server.isShiftKeyDown())return null;
  long now=server.serverLevel().getGameTime();
  if(now<server.getPersistentData().getLong(DOME))return InteractionResultHolder.fail(stack);
  server.getPersistentData().putLong(DOME,now+300L);
  for(LivingEntity ally:server.serverLevel().getEntitiesOfClass(LivingEntity.class,server.getBoundingBox().inflate(5D),e->e==server||e instanceof Player p&&server.isAlliedTo(p))){
   ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,120,1));
   ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,120,1));
  }
  server.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,120,1));
  server.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,120,1));
  for(int i=0;i<28;i++){
   double angle=i*Math.PI*2/28D;
   server.serverLevel().sendParticles(MOR,server.getX()+Math.cos(angle)*4D,server.getY()+.9D,server.getZ()+Math.sin(angle)*4D,2,0,.7D,0,.008D);
  }
  server.playSound(SoundEvents.SHIELD_BLOCK,1.1F,.85F);
  return InteractionResultHolder.consume(stack);
 }

 @SubscribeEvent public static void ninthLife(LivingDamageEvent.Pre event){
  if(!(event.getEntity() instanceof ServerPlayer player)||player.isCreative()||player.isSpectator()||event.getNewDamage()<=0)return;
  if(event.getNewDamage()<player.getHealth()+player.getAbsorptionAmount())return;
  long now=player.server.getLevel(net.minecraft.world.level.Level.OVERWORLD).getGameTime();
  if(now<player.getPersistentData().getLong(NINTH_LIFE))return;
  boolean equipped=CuriosApi.getCuriosInventory(player)
      .map(inv->inv.findCurios(stack->stack.is(NineMorContent.AMULET.get())).stream()
          .anyMatch(curio->"necklace".equals(curio.slotContext().identifier()))).orElse(false);
  if(!equipped)return;
  player.getPersistentData().putLong(NINTH_LIFE,now+6000L);
  event.setNewDamage(0F);
  player.setHealth(Math.max(1F,player.getHealth()));
  player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,1));
  player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,120,1));
  player.serverLevel().sendParticles(MOR,player.getX(),player.getEyeY(),player.getZ(),65,.7D,1.2D,.7D,.05D);
  player.playSound(SoundEvents.TOTEM_USE,1F,.7F);
 }

 @SubscribeEvent public static void barrier(LivingShieldBlockEvent event){
  if(!event.getOriginalBlock()||!(event.getEntity() instanceof ServerPlayer player)
     ||!player.getUseItem().is(NineMorContent.SHIELD.get()))return;
  long now=player.serverLevel().getGameTime();
  if(now<player.getPersistentData().getLong(BARRIER))return;
  player.getPersistentData().putLong(BARRIER,now+120L);
  player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,65,0));
  player.serverLevel().sendParticles(MOR,player.getX(),player.getEyeY(),player.getZ(),16,.65D,.6D,.65D,.012D);
 }

 private NineMorRewardAbilities(){}
}
