package org.slavicmyths.marine;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.*;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.rpg.Abilities;
/** Server authority for the approved sea-weapon abilities. Durations are game ticks. */
@EventBusSubscriber(modid="slavicmyths")
public final class MarineWeaponAbilities {
 static final String PREFIX="MarineWeapon_";
 static void wear(ServerPlayer p,int amount){p.getMainHandItem().hurtAndBreak(amount,p,EquipmentSlot.MAINHAND);}
 static boolean ready(ServerPlayer p,String name,int cooldown){var data=p.getPersistentData();long now=p.serverLevel().getGameTime();if(data.getLong(PREFIX+name)>now)return false;data.putLong(PREFIX+name,now+cooldown);return true;}
 static LivingEntity aim(ServerPlayer p,double range){
  Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(range));LivingEntity best=null;double distance=range*range;
  for(var candidate:p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(eye,end).inflate(1),e->e!=p&&e.isAlive()&&Abilities.canHit(p,e)&&p.hasLineOfSight(e))){
   var hit=candidate.getBoundingBox().inflate(.4).clip(eye,end);if(hit.isPresent()&&eye.distanceToSqr(hit.get())<distance){best=candidate;distance=eye.distanceToSqr(hit.get());}
  }return best;
 }
 static void hit(ServerPlayer p,LivingEntity target,float amount){if(Abilities.canHit(p,target))target.hurt(new MarineWeaponRules.AbilitySource(p,p.getMainHandItem()),amount);}
 static boolean anchorPullable(LivingEntity target){return target.getBbHeight()<=4F;}
 static boolean large(LivingEntity target){return target.getBbWidth()>=1.4F||target.getBbHeight()>=2.7F||CombatTargeting.controlResistant(target);}
 static void stun(ServerPlayer owner,LivingEntity target){
  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1),owner);
  if(!CombatTargeting.controlResistant(target)){
   target.getPersistentData().putLong("RareStunUntil",RareCombat.now(target)+30);
   target.getPersistentData().putLong(PREFIX+"anchorStunUntil",target.level().getGameTime()+30);
   target.addEffect(new MobEffectInstance(ModEffects.DISORIENTATION,30,0),owner);
  }
 }
 public static boolean bubble(ServerPlayer p){
  if(!p.getMainHandItem().is(MarineWeapons.STAFF.get())||!ready(p,"bubble",400))return false;
  var data=p.getPersistentData();data.putFloat(PREFIX+"absorptionBefore",p.getAbsorptionAmount());data.putLong(PREFIX+"bubbleUntil",p.serverLevel().getGameTime()+120);
  var absorption=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION);
  if(absorption!=null){var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","marine_bubble");absorption.removeModifier(id);absorption.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id,6,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));}
  wear(p,2);p.setAbsorptionAmount(p.getAbsorptionAmount()+6);p.serverLevel().sendParticles(ParticleTypes.BUBBLE_POP,p.getX(),p.getY()+1,p.getZ(),36,.7,1,.7,.03);return true;
 }
 @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem event){
  if(!(event.getEntity() instanceof ServerPlayer p)||event.getHand()!=InteractionHand.MAIN_HAND)return;
  var stack=event.getItemStack();boolean staff=stack.is(MarineWeapons.STAFF.get()),anchor=stack.is(MarineWeapons.ANCHOR.get()),trident=stack.is(PharaohkaTrident.ITEM.get());
  if(!staff&&!anchor&&!(trident&&p.isShiftKeyDown()))return;
  event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
  if(trident){if(ready(p,"wave",240)){wear(p,2);for(var target:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(8),e->e!=p&&e.isAlive()&&Abilities.canHit(p,e)&&p.hasLineOfSight(e))){var delta=target.position().subtract(p.position());if(delta.lengthSqr()>0&&delta.lengthSqr()<=64&&delta.normalize().dot(p.getLookAngle())>.65){hit(p,target,6);if(!CombatTargeting.controlResistant(target))target.push(delta.normalize().x*.8,.15,delta.normalize().z*.8);}}p.serverLevel().sendParticles(ParticleTypes.SPLASH,p.getX(),p.getY()+1,p.getZ(),60,3,1,3,.15);}return;}
  if(anchor){
   if(p.isShiftKeyDown()){if(ready(p,"slam",240)){wear(p,2);for(var target:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(3),e->e!=p&&e.isAlive()&&p.distanceToSqr(e)<=9&&Abilities.canHit(p,e)&&p.hasLineOfSight(e))){hit(p,target,5);stun(p,target);}p.serverLevel().sendParticles(ParticleTypes.SPLASH,p.getX(),p.getY()+.2,p.getZ(),40,2,.2,2,.1);}return;}
   if(!ready(p,"anchor",80))return;wear(p,1);double range=12*org.slavicmyths.combat.RangeEnchantment.multiplier(p.level(),stack);p.getPersistentData().putDouble(PREFIX+"anchorRange",range);var target=aim(p,range);
   MarineAnchorVisuals.show(p,target==null?p.getEyePosition().add(p.getLookAngle().scale(range)):target.getEyePosition());
   if(target!=null){p.getPersistentData().putUUID(PREFIX+"anchorTarget",target.getUUID());p.getPersistentData().putLong(PREFIX+"anchorImpact",p.serverLevel().getGameTime()+7);}
   return;
  }  if(!p.isShiftKeyDown()){
   if(ready(p,"staffShot",40)){wear(p,1);var d=p.getPersistentData();Vec3 at=p.getEyePosition(),v=p.getLookAngle().scale(1.2);
    d.putInt(PREFIX+"shotTicks",15);d.putDouble(PREFIX+"sx",at.x);d.putDouble(PREFIX+"sy",at.y);d.putDouble(PREFIX+"sz",at.z);d.putDouble(PREFIX+"sdx",v.x);d.putDouble(PREFIX+"sdy",v.y);d.putDouble(PREFIX+"sdz",v.z);
   }return;
  }
  boolean water=false;for(var pos:BlockPos.betweenClosed(p.blockPosition().offset(-6,-6,-6),p.blockPosition().offset(6,6,6)))if(pos.distSqr(p.blockPosition())<=36&&p.level().getFluidState(pos).is(FluidTags.WATER)){water=true;break;}
  if(!water)return;var target=aim(p,24);if(target==null)target=p.getLastHurtMob();if(target==null||!target.isAlive()||p.distanceToSqr(target)>32*32||!Abilities.canHit(p,target)||!p.hasLineOfSight(target))return;
  if(p.getPersistentData().hasUUID(PREFIX+"vortexTarget")||!ready(p,"vortex",500))return;wear(p,3);var data=p.getPersistentData();data.putUUID(PREFIX+"vortexTarget",target.getUUID());data.putLong(PREFIX+"vortexTravelEnd",p.serverLevel().getGameTime()+80);data.putLong(PREFIX+"vortexUntil",0);Vec3 at=p.getEyePosition();data.putDouble(PREFIX+"vx",at.x);data.putDouble(PREFIX+"vy",at.y);data.putDouble(PREFIX+"vz",at.z);
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;var data=p.getPersistentData();long now=p.serverLevel().getGameTime();
  int shotTicks=data.getInt(PREFIX+"shotTicks");
  if(shotTicks>0){
   Vec3 at=new Vec3(data.getDouble(PREFIX+"sx"),data.getDouble(PREFIX+"sy"),data.getDouble(PREFIX+"sz"));
   Vec3 next=at.add(data.getDouble(PREFIX+"sdx"),data.getDouble(PREFIX+"sdy"),data.getDouble(PREFIX+"sdz"));
   var wall=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(at,next,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
   Vec3 end=wall.getType()==HitResult.Type.MISS?next:wall.getLocation();LivingEntity victim=null;double nearest=at.distanceToSqr(end)+.001;
   for(var candidate:p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(at,end).inflate(.3),e->e!=p&&e.isAlive()&&Abilities.canHit(p,e))){var crossing=candidate.getBoundingBox().inflate(.2).clip(at,end);if(crossing.isPresent()&&at.distanceToSqr(crossing.get())<nearest){nearest=at.distanceToSqr(crossing.get());victim=candidate;}}
   if(victim!=null){victim.hurt(new MarineWeaponRules.AbilitySource(p,new net.minecraft.world.item.ItemStack(MarineWeapons.STAFF.get())),5);victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0),p);shotTicks=1;}
   if(wall.getType()!=HitResult.Type.MISS)shotTicks=1;
   for(int j=0;j<5;j++){Vec3 dot=at.lerp(end,j/4D);p.serverLevel().sendParticles(ParticleTypes.SPLASH,dot.x,dot.y,dot.z,2,.05,.05,.05,0);}
   data.putDouble(PREFIX+"sx",next.x);data.putDouble(PREFIX+"sy",next.y);data.putDouble(PREFIX+"sz",next.z);data.putInt(PREFIX+"shotTicks",shotTicks-1);
  }
  var held=p.getMainHandItem();if(MarineWeaponRules.weapon(held)){
   var tag=held.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
   for(String key:new String[]{"slam","anchor","wave","vortex","bubble","staffShot"})tag.putLong(PREFIX+key,data.getLong(PREFIX+key));
   tag.putInt("MarineHarpoonHits",data.getInt("MarineHarpoonHits"));tag.putLong("MarineHarpoonReady",data.getLong("MarineHarpoonReady"));
   held.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
  }
  if(data.hasUUID(PREFIX+"anchorTarget")&&now>=data.getLong(PREFIX+"anchorImpact")){
   var anchorTarget=p.serverLevel().getEntity(data.getUUID(PREFIX+"anchorTarget"));data.remove(PREFIX+"anchorTarget");
   if(anchorTarget instanceof LivingEntity victim&&victim.isAlive()&&p.distanceToSqr(victim)<=Math.pow(Math.max(12,data.getDouble(PREFIX+"anchorRange"))+2,2)&&Abilities.canHit(p,victim)&&p.hasLineOfSight(victim)){
    victim.hurt(new MarineWeaponRules.AbilitySource(p,new net.minecraft.world.item.ItemStack(MarineWeapons.ANCHOR.get())),7);
    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,large(victim)?2:1),p);
    if(anchorPullable(victim)){
     data.putUUID(PREFIX+"anchorPullTarget",victim.getUUID());
     data.putLong(PREFIX+"anchorPullUntil",now+12);
    }
   }
  }
  if(data.hasUUID(PREFIX+"anchorPullTarget")){
   var pulled=p.serverLevel().getEntity(data.getUUID(PREFIX+"anchorPullTarget"));
   if(!p.isAlive()||now>=data.getLong(PREFIX+"anchorPullUntil")||!(pulled instanceof LivingEntity victim)||!victim.isAlive()||!anchorPullable(victim)||!Abilities.canHit(p,victim)||!p.hasLineOfSight(victim)||p.distanceToSqr(victim)>Math.pow(Math.max(12,data.getDouble(PREFIX+"anchorRange"))+2,2)){
    data.remove(PREFIX+"anchorPullTarget");data.remove(PREFIX+"anchorPullUntil");
   }else{
    Vec3 delta=p.position().subtract(victim.position());
    double stop=p.getBbWidth()*.5+victim.getBbWidth()*.5+.5;
    if(delta.horizontalDistance()<=stop){data.remove(PREFIX+"anchorPullTarget");}
    else{
     Vec3 velocity=delta.normalize().scale(Math.min(1.1,delta.length()*.18));
     // Normal entity movement resolves block collisions; do not teleport through walls.
     victim.setDeltaMovement(velocity.x,Math.clamp(velocity.y+.12,-.3,.35),velocity.z);
     victim.hurtMarked=true;
    }
   }
  }
  if(data.getLong(PREFIX+"bubbleUntil")>0&&now>=data.getLong(PREFIX+"bubbleUntil")){p.setAbsorptionAmount(Math.min(p.getAbsorptionAmount(),data.getFloat(PREFIX+"absorptionBefore")));data.remove(PREFIX+"bubbleUntil");var absorption=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION);if(absorption!=null)absorption.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","marine_bubble"));}
  if(!data.hasUUID(PREFIX+"vortexTarget"))return;var entity=p.serverLevel().getEntity(data.getUUID(PREFIX+"vortexTarget"));
  if(!p.isAlive()||!(entity instanceof LivingEntity target)||!target.isAlive()||!Abilities.canHit(p,target)||p.distanceToSqr(target)>48*48){data.remove(PREFIX+"vortexTarget");return;}
  Vec3 at=new Vec3(data.getDouble(PREFIX+"vx"),data.getDouble(PREFIX+"vy"),data.getDouble(PREFIX+"vz"));Vec3 destination=target.position().add(0,target.getBbHeight()*.5,0);
  long until=data.getLong(PREFIX+"vortexUntil");
  if(until==0){Vec3 delta=destination.subtract(at);if(delta.length()<1.3){until=now+160;data.putLong(PREFIX+"vortexUntil",until);}else {Vec3 next=at.add(delta.normalize().scale(.9));if(p.serverLevel().clip(new net.minecraft.world.level.ClipContext(at,next,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS||now>data.getLong(PREFIX+"vortexTravelEnd")){data.remove(PREFIX+"vortexTarget");return;}at=next;}}
  else {if(now>=until){data.remove(PREFIX+"vortexTarget");return;}at=destination;if(now%10==0)target.hurt(new MarineWeaponRules.AbilitySource(p,new net.minecraft.world.item.ItemStack(MarineWeapons.STAFF.get())),2);}
  data.putDouble(PREFIX+"vx",at.x);data.putDouble(PREFIX+"vy",at.y);data.putDouble(PREFIX+"vz",at.z);
  if(now%2==0)for(int i=0;i<10;i++){double a=now*.5+i*.9;double radius=.2+i*.045;p.serverLevel().sendParticles(ParticleTypes.CLOUD,at.x+Math.cos(a)*radius,at.y-.7+i*.15,at.z+Math.sin(a)*radius,3,.08,.04,.08,0);}
 }
 @SubscribeEvent public static void perfectBlock(LivingIncomingDamageEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p)||!p.isBlocking()||!p.getUseItem().is(ZhelvaShield.ITEM.get())||event.getAmount()<=0)return;
  Vec3 source=event.getSource().getSourcePosition();if(source==null)return;Vec3 toward=source.subtract(p.position()).multiply(1,0,1);if(toward.lengthSqr()<.001||toward.normalize().dot(p.getLookAngle().multiply(1,0,1).normalize())<=0)return;
  if(ready(p,"perfectBlock",240)){event.setCanceled(true);p.serverLevel().sendParticles(ParticleTypes.NAUTILUS,p.getX(),p.getY()+1,p.getZ(),24,.5,.5,.5,.04);}
 }
 @SubscribeEvent public static void preventStunnedAttacks(LivingIncomingDamageEvent event){
  if(event.getSource().getEntity() instanceof LivingEntity attacker&&DisorientationEffect.stunned(attacker)&&attacker.getPersistentData().getLong(PREFIX+"anchorStunUntil")>attacker.level().getGameTime())event.setCanceled(true);
 }
}