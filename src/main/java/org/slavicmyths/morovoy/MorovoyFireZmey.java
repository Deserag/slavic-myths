package org.slavicmyths.morovoy;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** 2-head / 2-tail independent telegraph scheduler; all hits are server-authoritative. */
public final class MorovoyFireZmey extends Monster {
 public static final float EFFECTIVE_HP=MorovoyZmeyRules.EFFECTIVE_HP;public static final int SLEEPING=0,WAKING=1,PHASE_I=2,PHASE_II=3,DYING=4;
 public static final int IDLE=0,MOR_BOLT=1,FIRE_BOLT=2,MOR_BREATH=3,FIRE_BREATH=4,DOUBLE_BOLT=5,MOR_TAIL=6,FIRE_TAIL=7,BITE_MOR=8,BITE_FIRE=9,CLAW=10,DIVE=11,WING_STREAK=12;
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> STATE_AGE=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> ACTION_AGE=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> AIRBORNE=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.BOOLEAN);
 private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.morovoy_fire_zmey"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_20);
 private Vec3 anchor;private int idleTicks=60,orbitTicks,grabTicks;private UUID grabbed;private int turn=0;
 public MorovoyFireZmey(EntityType<? extends MorovoyFireZmey> type,Level level){super(type,level);moveControl=new net.minecraft.world.entity.ai.control.FlyingMoveControl(this,20,true);setPersistenceRequired();xpReward=0;}
 @Override public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,net.minecraft.world.entity.MobSpawnType reason,net.minecraft.world.entity.SpawnGroupData data){var result=super.finalizeSpawn(world,difficulty,reason,data);if(reason==net.minecraft.world.entity.MobSpawnType.SPAWN_EGG){anchor=position();nextState(WAKING);}return result;}
 @Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level){net.minecraft.world.entity.ai.navigation.FlyingPathNavigation nav=new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this,level);nav.setCanOpenDoors(false);nav.setCanFloat(true);return nav;}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.ARMOR,14).add(Attributes.ARMOR_TOUGHNESS,8).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.FLYING_SPEED,.32).add(Attributes.ATTACK_DAMAGE,25).add(Attributes.FOLLOW_RANGE,88).add(Attributes.KNOCKBACK_RESISTANCE,1);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(STATE,SLEEPING);builder.define(STATE_AGE,0);builder.define(ACTION,IDLE);builder.define(ACTION_AGE,0);builder.define(AIRBORNE,false);}
 public int state(){return entityData.get(STATE);}public int stateAge(){return entityData.get(STATE_AGE);}public int action(){return entityData.get(ACTION);}public int actionAge(){return entityData.get(ACTION_AGE);}public boolean flying(){return entityData.get(AIRBORNE);}
 public boolean canDamage(LivingEntity target){return target!=this&&target.isAlive()&&target.level()==level()&&!(target instanceof MorovoyFireZmey)&&(!(target instanceof Player p)||!p.isCreative()&&!p.isSpectator())&&!isAlliedTo(target);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 private void nextState(int value){entityData.set(STATE,value);entityData.set(STATE_AGE,0);entityData.set(ACTION,IDLE);entityData.set(ACTION_AGE,0);grabbed=null;grabTicks=0;if(value==WAKING||value==DYING){setDeltaMovement(Vec3.ZERO);entityData.set(AIRBORNE,false);setNoGravity(false);}}
 private void act(int value){entityData.set(ACTION,value);entityData.set(ACTION_AGE,0);}
 @Override public boolean hurt(DamageSource source,float amount){if(state()==DYING)return false;if(state()==SLEEPING&&source.getEntity() instanceof Player)nextState(WAKING);return super.hurt(source,amount*getMaxHealth()/EFFECTIVE_HP);}
 @Override public void die(DamageSource source){if(state()!=DYING){nextState(DYING);setHealth(1);bar.setProgress(0);return;}super.die(source);}
 @Override public void tick(){super.tick();if(level().isClientSide)return;
  if(anchor==null)anchor=position();int age=stateAge()+1;entityData.set(STATE_AGE,age);
  bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0f,1f));
  if(state()==DYING){setDeltaMovement(Vec3.ZERO);if(age>=65)super.die(damageSources().genericKill());return;}
  if(state()==SLEEPING){setDeltaMovement(Vec3.ZERO);setNoGravity(false);if(tickCount%30==0){passiveMor();for(ServerPlayer p:((ServerLevel)level()).players())if(p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&MorovoyZmeyRules.shouldWake(p.distanceToSqr(position().add(0,1,0)))){nextState(WAKING);break;}}return;}
  if(state()==WAKING){setDeltaMovement(Vec3.ZERO);if(age==10)particles(0,position().add(0,2,0),24);if(age==40)particles(1,position().add(0,3,0),32);if(age>=65){nextState(PHASE_I);idleTicks=45;setDeltaMovement(0,.35,0);setNoGravity(true);entityData.set(AIRBORNE,true);}return;}
  if(state()==PHASE_I&&MorovoyZmeyRules.secondPhase(getHealth(),getMaxHealth())){nextState(PHASE_II);particles(1,position().add(0,2,0),90);idleTicks=55;return;}
  if(tickCount%10==0){LivingEntity target=selectTarget();setTarget(target);if(target==null){act(IDLE);moveInAir(null,false);return;}}
  LivingEntity target=getTarget();if(target==null||!canDamage(target)){moveInAir(null,false);return;}
  float desired=(float)Math.toDegrees(Math.atan2(target.getZ()-getZ(),target.getX()-getX()))-90;yBodyRot=Mth.rotLerp(.22f,yBodyRot,desired);setYRot(yBodyRot);
  int old=actionAge();entityData.set(ACTION_AGE,old+1);
  if(grabTicks>0)tickGrab();
  if(action()==IDLE){if(--idleTicks<=0){chooseAttack(target);idleTicks=state()==PHASE_II?16:29;}else moveInAir(target,false);return;}
  int a=actionAge();
  if(action()!=DIVE&&action()!=WING_STREAK)moveInAir(target,false);
  executeAttackWindow(target,a);
  if(a>=duration(action()))act(IDLE);
 }
 private void executeAttackWindow(LivingEntity target,int age) {
  switch(action()) {
   case MOR_BOLT -> {if(age==18)projectile(target,0);}
   case FIRE_BOLT -> {if(age==18)projectile(target,1);}
   case DOUBLE_BOLT -> {if(age==20)projectile(target,0);if(age==30)projectile(target,1);}
   case MOR_BREATH,FIRE_BREATH -> {if(age>=18&&age<=55&&age%4==0)breath(target,action()==MOR_BREATH?0:1);}
   case MOR_TAIL -> {if(age==22)strike(target,0,true);}
   case FIRE_TAIL -> {if(age==24)strike(target,1,true);}
   case BITE_MOR,BITE_FIRE -> {
    if(age==20){int element=action()==BITE_MOR?0:1;boolean struck=strike(target,element,false);
     if(struck&&!target.isBlocking()&&target.distanceToSqr(this)<=18&&hasLineOfSight(target)&&canDamage(target)){grabbed=target.getUUID();grabTicks=14;}
    }
   }
   case CLAW -> {if(age==17)strike(target,1,false);}
   case DIVE -> {if(age>=12&&age<=42)moveInAir(target,true);if(age==35)strike(target,1,false);}
   case WING_STREAK -> {if(age>=12&&age<=70){moveInAir(target,false);if(age%10==0)MorovoyZmeyField.spawn((ServerLevel)level(),this,(age/10)%2,position().add(0,-Math.min(6,getY()-anchor.y),0),50);}}
   default -> {}
  }
 }
 private LivingEntity selectTarget(){List<ServerPlayer> list=((ServerLevel)level()).getPlayers(p->canDamage(p)&&p.distanceToSqr(anchor)<=90*90&&p.distanceToSqr(this)<=82*82);return list.stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);}
 private int duration(int a){return switch(a){case MOR_BREATH,FIRE_BREATH->65;case WING_STREAK->80;case DIVE->55;case DOUBLE_BOLT->45;default->38;};}
 private void chooseAttack(LivingEntity target){double d=distanceToSqr(target);int[] far=state()==PHASE_II?new int[]{DOUBLE_BOLT,MOR_BREATH,FIRE_BREATH,DIVE,WING_STREAK,MOR_BOLT,FIRE_BOLT}:new int[]{MOR_BOLT,FIRE_BOLT,MOR_BREATH,FIRE_BREATH,DOUBLE_BOLT,DIVE};int[] close={MOR_TAIL,FIRE_TAIL,BITE_MOR,BITE_FIRE,CLAW,MOR_BREATH,FIRE_BREATH};int[] choices=d<64?close:far;act(choices[Math.floorMod(turn++,choices.length)]);}
 private Vec3 mouth(int element){float a=getYRot()*(float)Math.PI/180f;double right=element==0?-1.15:1.15;return position().add(-Math.sin(a)*2+Math.cos(a)*right,2.1,-Math.cos(a)*2-Math.sin(a)*right);}
 private void projectile(LivingEntity target,int element){if(!hasLineOfSight(target)||!(level() instanceof ServerLevel world))return;MorovoyZmeyBolt bolt=MorovoyFireZmeyContent.BOLT.get().create(world);if(bolt==null)return;Vec3 from=mouth(element),aim=target.getEyePosition().subtract(from).normalize();bolt.configure(this,element);bolt.setPos(from);bolt.shoot(aim.x,aim.y,aim.z,element==0?1.1f:1.4f,.6f);world.addFreshEntity(bolt);particles(element,from,15);}
 private void breath(LivingEntity target,int element){if(!(level() instanceof ServerLevel world))return;Vec3 from=mouth(element),to=target.getEyePosition().subtract(from).normalize();java.util.Set<UUID> damaged=new java.util.HashSet<>();for(int i=1;i<=8;i++){Vec3 point=from.add(to.scale(i*1.5));if(!world.hasChunkAt(BlockPos.containing(point)))break;if(!world.getBlockState(BlockPos.containing(point)).isAir())break;if(i%2==0)particles(element,point,5);for(LivingEntity victim:world.getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(1.2),this::canDamage))if(damaged.add(victim.getUUID())&&hasLineOfSight(victim)){victim.hurt(damageSources().mobAttack(this),element==0?5.0f:6.25f);if(element==0)victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,45));}}
  if(actionAge()%12==0&&target.distanceToSqr(this)<225){MorovoyZmeyField.spawn(world,this,element,target.position(),65);if(element==1)VioletFireBlock.place(world,target.blockPosition());}
 }
 private boolean strike(LivingEntity target,int element,boolean tail){if(!hasLineOfSight(target))return false;float yaw=getYRot()*(float)Math.PI/180f;Vec3 center=tail?position().add(Math.sin(yaw)*3+Math.cos(yaw)*(element==0?-1:1),1.1,Math.cos(yaw)*3-Math.sin(yaw)*(element==0?-1:1)):mouth(element);double range=tail?6.5:3.5;if(target.distanceToSqr(center)>range*range)return false;boolean hit=target.hurt(damageSources().mobAttack(this),tail?14:25);if(hit){if(tail)target.knockback(1.15D,getX()-target.getX(),getZ()-target.getZ());if(element==0)target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,75));else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35));}return hit;}
 private void tickGrab(){grabTicks--;if(!(level() instanceof ServerLevel world)||grabbed==null)return;net.minecraft.world.entity.Entity entity=world.getEntity(grabbed);if(!(entity instanceof LivingEntity victim)||!canDamage(victim)||!hasLineOfSight(victim)||distanceToSqr(victim)>36){grabTicks=0;grabbed=null;return;}if(grabTicks%5==0){Vec3 pull=mouth(action()==BITE_MOR?0:1).subtract(victim.position());victim.setDeltaMovement(victim.getDeltaMovement().add(pull.normalize().scale(.22)));victim.hurt(damageSources().mobAttack(this),2);}}
 private void passiveMor(){if(!(level() instanceof ServerLevel world))return;particles(0,position().add(0,.25,0),5);if(tickCount%90==0)MorovoyZmeyField.spawn(world,this,0,anchor,45);}
 private void particles(int element,Vec3 pos,int count){if(!(level() instanceof ServerLevel world))return;if(element!=0){PurpleFlameVisuals.burst(world,pos,count);return;}world.sendParticles(new DustParticleOptions(new Vector3f(.12f,.85f,.18f),1.1f),pos.x,pos.y,pos.z,count,.75,.45,.75,.04);}
 private void moveInAir(LivingEntity target,boolean dive) {
  if(!(level() instanceof ServerLevel world))return;
  if(anchor==null)anchor=position();
  entityData.set(AIRBORNE,true);
  setNoGravity(true);
  orbitTicks++;
  double angle=orbitTicks*.022+(getId()%9),radius=dive&&target!=null?3.5:13;
  Vec3 focus=dive&&target!=null?target.position().add(0,2,0):anchor.add(Math.cos(angle)*radius,state()==PHASE_II?12:9,Math.sin(angle)*radius);
  Vec3 offset=focus.subtract(position());
  Vec3 desired=offset.lengthSqr()>1?offset.normalize().scale(dive?.47:.26):Vec3.ZERO;
  Vec3 velocity=getDeltaMovement().scale(.84).add(desired.scale(.16));
  if(velocity.lengthSqr()>.25)velocity=velocity.normalize().scale(.5);
  Vec3 next=position().add(velocity);
  if(next.distanceToSqr(anchor)>70*70||!world.hasChunkAt(BlockPos.containing(next)))velocity=Vec3.ZERO;
  if(!world.noCollision(this,getBoundingBox().move(velocity))){
   Vec3 lift=new Vec3(0,.24,0);
   velocity=world.noCollision(this,getBoundingBox().move(lift))?lift:Vec3.ZERO;
  }
  float desiredYaw=(float)Math.toDegrees(Math.atan2(velocity.z,velocity.x))-90;
  if(velocity.horizontalDistanceSqr()>.0001)setYRot(Mth.rotLerp(.12f,getYRot(),desiredYaw));
  setDeltaMovement(velocity);
  hasImpulse=true;
 }

 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("BossState",state());tag.putInt("StateAge",stateAge());tag.putInt("Action",action());tag.putInt("ActionAge",actionAge());tag.putInt("Turn",turn);tag.putInt("Rest",idleTicks);tag.putInt("Orbit",orbitTicks);if(anchor!=null){tag.putDouble("AnchorX",anchor.x);tag.putDouble("AnchorY",anchor.y);tag.putDouble("AnchorZ",anchor.z);}}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(STATE,Math.clamp(tag.getInt("BossState"),SLEEPING,DYING));entityData.set(STATE_AGE,Math.max(0,tag.getInt("StateAge")));entityData.set(ACTION,Math.clamp(tag.getInt("Action"),IDLE,WING_STREAK));entityData.set(ACTION_AGE,Math.max(0,tag.getInt("ActionAge")));turn=Math.max(0,tag.getInt("Turn"));idleTicks=Math.max(10,tag.getInt("Rest"));orbitTicks=Math.max(0,tag.getInt("Orbit"));if(tag.contains("AnchorX"))anchor=new Vec3(tag.getDouble("AnchorX"),tag.getDouble("AnchorY"),tag.getDouble("AnchorZ"));grabbed=null;grabTicks=0;entityData.set(AIRBORNE,false);setNoGravity(false);}
 @Override public boolean removeWhenFarAway(double distance){return false;}
}
