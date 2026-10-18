package org.slavicmyths.water;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.entity.AttackTimeline;
import org.slavicmyths.progression.Knowledge;

/** Existing identity/rewards/charm, dedicated synchronized visual actions and bounded water movement. */
public final class RusalkaEntity extends WaterSpirit {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(RusalkaEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> ACTION_CLOCK=SynchedEntityData.defineId(RusalkaEntity.class,EntityDataSerializers.INT);
 private final AttackTimeline attack=new AttackTimeline();
 private int watching,diveTicks;private double diveRemaining;private long silentUntil,nextDive,nextDiveProbe;private Vec3 diveDirection=Vec3.ZERO;
 public RusalkaEntity(EntityType<? extends RusalkaEntity> type,Level level){super(type,level);}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,28).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.FOLLOW_RANGE,20);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(ACTION_CLOCK,0);}
 public int visualAction(){return entityData.get(ACTION);}
 public float actionTime(float age){return Math.max(0,(int)level().getGameTime()-entityData.get(ACTION_CLOCK)+Math.clamp(age-tickCount,0,1));}
 private void action(int value){if(visualAction()!=value){entityData.set(ACTION,value);entityData.set(ACTION_CLOCK,(int)level().getGameTime());}}
 @Override protected AttackTimeline attackTimeline(){return attack;}
 @Override protected void registerGoals(){
  goalSelector.addGoal(1,new Goal(){
   {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));}
   public boolean canUse(){return getTarget()!=null&&getTarget().isAlive();}
   public boolean requiresUpdateEveryTick(){return true;}
   public void tick(){faceTarget();if(attack.ready()&&diveTicks==0&&visualAction()!=RusalkaActions.BREAK)chase(1.1);else getNavigation().stop();}
   public void stop(){getNavigation().stop();}
  });
  goalSelector.addGoal(4,new LookAtPlayerGoal(this,Player.class,18));goalSelector.addGoal(5,new RandomLookAroundGoal(this));
 }
 public boolean singing(){return isAlive()&&morState()==0&&(state()==2||state()==4);}
 @Override public void setTarget(LivingEntity target){
  boolean was=singing();super.setTarget(target);
  if(!level().isClientSide&&getTarget()==null&&attack!=null){attack.cancel();if(diveTicks>0)endDive();action(RusalkaActions.IDLE);}
  if(!level().isClientSide&&getTarget()!=null&&was){state(3);silentUntil=level().getGameTime()+80;watching=0;action(RusalkaActions.BREAK);attack.cancel();getNavigation().stop();}
 }
 @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return singing()?null:super.getAmbientSound();}
 @Override protected void think(){
  if(home==null)home=blockPosition();if(getTarget()!=null){state(3);return;}
  Player p=nearby(20);if(p==null){watching=0;state(0);if(diveTicks==0)action(RusalkaActions.IDLE);wander();return;}
  Knowledge.award(p,"meet_rusalka");getLookControl().setLookAt(p,15,15);
  if(level().getGameTime()<silentUntil){state(1);if(visualAction()!=RusalkaActions.BREAK)action(RusalkaActions.OBSERVE);return;}
  watching=Math.min(100,watching+20);if(watching<100){state(1);action(RusalkaActions.OBSERVE);return;}
  boolean began=!singing();state(2);if(began)action(RusalkaActions.SONG_START);boolean lured=false;
  for(Player listener:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(20),q->q.isAlive()&&!q.isCreative()&&!q.isSpectator()&&distanceToSqr(q)<=400)){
   RusalkaSong.expose(listener,this);if(listener.getPersistentData().getFloat("SlavicWaterCharm")>=50)lured=true;
   if(distanceToSqr(listener)<6.25&&hasLineOfSight(listener)){provoke(listener);return;}
  }
  if(lured)state(4);
 }
 @Override protected void abilityTick(){
  if(!isAlive())return;
  if(visualAction()==RusalkaActions.BREAK&&actionTime(tickCount)<12){getNavigation().stop();return;}
  if(getTarget()==null){attack.cancel();diveTicks=0;if(singing()){if(actionTime(tickCount)>=24)action(RusalkaActions.LURE);}else if(visualAction()==RusalkaActions.BREAK)action(RusalkaActions.OBSERVE);return;}
  state(3);long now=level().getGameTime();
  if(diveTicks>0){
   getNavigation().stop();int elapsed=RusalkaActions.DIVE_WARN+RusalkaActions.DIVE_MOVE-diveTicks;
   if(!attackable(getTarget(),20)||!isInWater()){endDive();return;}
   if(elapsed>=RusalkaActions.DIVE_WARN){
    Vec3 step=diveDirection.scale(Math.min(.38,diveRemaining));
    if(diveRemaining<=.001||!waterSpace(getBoundingBox().move(step))||distanceToSqr(getTarget())<6.25){endDive();return;}
    // Actual physical displacement is performed and bounded in travel(), never teleportation.
   }
   if(tickCount%2==0&&level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.BUBBLE,getX(),getY()+.4,getZ(),3,.2,.15,.2,.01);
   if(--diveTicks==0)endDive();return;
  }
  if(attack.ready()){
   if(now>=nextDive&&now>=nextDiveProbe&&RusalkaActions.diveDistance(distanceToSqr(getTarget()))){nextDiveProbe=now+20;if(beginDive())return;}
   if(attackable(getTarget(),2)){attack.start(RusalkaActions.TELEGRAPH,RusalkaActions.ACTIVE,RusalkaActions.RECOVER,1);action(RusalkaActions.WINDUP);getNavigation().stop();}
   else{if(visualAction()!=RusalkaActions.RECOVERY||actionTime(tickCount)>=7)action(RusalkaActions.OBSERVE);return;}
  }
  if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();}
  if(attack.tick()){
   action(RusalkaActions.GRAB);
   if(attackable(getTarget(),2))doHurtTarget(getTarget());
  }else if(attack.phase()==AttackTimeline.Phase.RECOVERY)action(RusalkaActions.RECOVERY);
 }
 private void endDive(){diveTicks=0;diveDirection=Vec3.ZERO;setDeltaMovement(Vec3.ZERO);action(RusalkaActions.RECOVERY);}
 private boolean beginDive(){
  Vec3 direction=getTarget().position().subtract(position()).multiply(1,0,1);if(direction.lengthSqr()<.01)return false;direction=direction.normalize();
  Vec3 delta=direction;double length=Math.min(3.8,Math.sqrt(distanceToSqr(getTarget()))-2.5);
  if(!waterSpace(getBoundingBox())||!RusalkaActions.safeDivePath(length,d->waterSpace(getBoundingBox().move(delta.scale(d)))))return false;
  setDeltaMovement(Vec3.ZERO);diveDirection=direction;diveRemaining=length;diveTicks=RusalkaActions.DIVE_WARN+RusalkaActions.DIVE_MOVE;nextDive=level().getGameTime()+RusalkaActions.DIVE_COOLDOWN;
  attack.cancel();action(RusalkaActions.DIVE);getNavigation().stop();return true;
 }
 @Override public void travel(Vec3 input){
  if(isEffectiveAi()&&diveTicks>0){
   int elapsed=RusalkaActions.DIVE_WARN+RusalkaActions.DIVE_MOVE-diveTicks;
   setDeltaMovement(Vec3.ZERO);
   if(elapsed<RusalkaActions.DIVE_WARN)return;
   Vec3 step=diveDirection.scale(Math.min(.38,diveRemaining));
   if(getTarget()==null||!attackable(getTarget(),20)||diveRemaining<=.001||distanceToSqr(getTarget())<6.25||!waterSpace(getBoundingBox().move(step))){endDive();return;}
   Vec3 before=position();move(MoverType.SELF,step);diveRemaining=Math.max(0,diveRemaining-position().distanceTo(before));hurtMarked=true;return;
  }
  super.travel(input);
 }
 /** Only loaded, sufficiently deep contiguous water. Never teleports or forces chunks. */
 private boolean waterSpace(AABB body){
  AABB volume=new AABB(body.minX,body.minY-.3,body.minZ,body.maxX,body.maxY+.2,body.maxZ);
  if(!level().getWorldBorder().isWithinBounds(volume))return false;
  for(BlockPos p:BlockPos.betweenClosed(BlockPos.containing(volume.minX,volume.minY,volume.minZ),BlockPos.containing(volume.maxX,volume.maxY,volume.maxZ))){
   if(!level().hasChunkAt(p)||!level().getFluidState(p).is(FluidTags.WATER))return false;
  }
  return level().noCollision(this,body);
 }
 @Override public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide){silentUntil=level().getGameTime()+120;watching=0;state(3);attack.cancel();diveTicks=0;diveDirection=Vec3.ZERO;setDeltaMovement(Vec3.ZERO);action(RusalkaActions.BREAK);}return hit;}
 @Override public boolean doHurtTarget(Entity target){
  if(level().isClientSide||attack.phase()!=AttackTimeline.Phase.ACTIVE)return false;
  boolean hit=super.doHurtTarget(target);
  if(hit&&isInWater()&&level().getGameTime()>=nextPower){nextPower=level().getGameTime()+100;target.setDeltaMovement(target.getDeltaMovement().add(0,-.07,0));target.hurtMarked=true;
   if(level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.SPLASH,target.getX(),target.getY()+.5,target.getZ(),6,.3,.15,.3,0);
  }return hit;
 }
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);RusalkaActions.writeMemory(tag,silentUntil,nextDive,watching);}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);var memory=RusalkaActions.readMemory(tag,level().getGameTime());silentUntil=memory.silentUntil();nextDive=memory.nextDive();watching=memory.watching();attack.cancel();diveTicks=0;diveDirection=Vec3.ZERO;action(RusalkaActions.IDLE);state(0);}
}
