package org.slavicmyths.morovoy;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
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
 private static final EntityDataAccessor<Float> MOR_YAW=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Float> MOR_PITCH=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Float> FIRE_YAW=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Float> FIRE_PITCH=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Float> BANK=SynchedEntityData.defineId(MorovoyFireZmey.class,EntityDataSerializers.FLOAT);
 private final Set<UUID> morTailHits=new HashSet<>();
 private final Set<UUID> fireTailHits=new HashSet<>();
 private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.morovoy_fire_zmey"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_20);
 private Vec3 anchor;private int idleTicks=60,orbitTicks,grabTicks;private UUID grabbed;private int turn=0;
 public MorovoyFireZmey(EntityType<? extends MorovoyFireZmey> type,Level level){super(type,level);setPersistenceRequired();xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.ARMOR,14).add(Attributes.ARMOR_TOUGHNESS,8).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.FLYING_SPEED,.32).add(Attributes.FOLLOW_RANGE,88).add(Attributes.KNOCKBACK_RESISTANCE,1);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(STATE,SLEEPING);builder.define(STATE_AGE,0);builder.define(ACTION,IDLE);builder.define(ACTION_AGE,0);builder.define(AIRBORNE,false);builder.define(MOR_YAW,0f);builder.define(MOR_PITCH,0f);builder.define(FIRE_YAW,0f);builder.define(FIRE_PITCH,0f);builder.define(BANK,0f);}
 public int state(){return entityData.get(STATE);}public int stateAge(){return entityData.get(STATE_AGE);}public int action(){return entityData.get(ACTION);}public int actionAge(){return entityData.get(ACTION_AGE);}public boolean flying(){return entityData.get(AIRBORNE);}public float morHeadYaw(){return entityData.get(MOR_YAW);}public float morHeadPitch(){return entityData.get(MOR_PITCH);}public float fireHeadYaw(){return entityData.get(FIRE_YAW);}public float fireHeadPitch(){return entityData.get(FIRE_PITCH);}public float bank(){return entityData.get(BANK);}
 public boolean canDamage(LivingEntity target){return target!=this&&target.isAlive()&&target.level()==level()&&!(target instanceof MorovoyFireZmey)&&(!(target instanceof Player p)||!p.isCreative()&&!p.isSpectator())&&!isAlliedTo(target);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 private void nextState(int value){entityData.set(STATE,value);entityData.set(STATE_AGE,0);entityData.set(ACTION,IDLE);entityData.set(ACTION_AGE,0);grabbed=null;grabTicks=0;if(value==WAKING||value==DYING){setDeltaMovement(Vec3.ZERO);entityData.set(AIRBORNE,false);setNoGravity(false);}}
 private void act(int value){entityData.set(ACTION,value);entityData.set(ACTION_AGE,0);morTailHits.clear();fireTailHits.clear();}
 @Override public boolean hurt(DamageSource source,float amount){if(state()==DYING)return false;if(state()==SLEEPING&&source.getEntity() instanceof Player)nextState(WAKING);return super.hurt(source,amount*getMaxHealth()/EFFECTIVE_HP);}
 @Override public void die(DamageSource source){if(state()!=DYING){nextState(DYING);setHealth(1);bar.setProgress(0);return;}super.die(source);}
 @Override public void tick(){super.tick();if(level().isClientSide)return;
  if(anchor==null)anchor=position();int age=stateAge()+1;entityData.set(STATE_AGE,age);
  bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0f,1f));
  if(state()==DYING){setDeltaMovement(Vec3.ZERO);if(age>=65)super.die(damageSources().genericKill());return;}
  if(state()==SLEEPING){setDeltaMovement(Vec3.ZERO);setNoGravity(false);if(tickCount%30==0)passiveMor();if(tickCount%5==0)for(ServerPlayer p:((ServerLevel)level()).players())if(p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&nearSleepingBody(p)){nextState(WAKING);break;}return;}
  if(state()==WAKING){setDeltaMovement(Vec3.ZERO);if(age==10)particles(0,position().add(0,2,0),24);if(age==40)particles(1,position().add(0,3,0),32);if(age>=65){nextState(PHASE_I);idleTicks=45;}return;}
  if(state()==PHASE_I&&MorovoyZmeyRules.secondPhase(getHealth(),getMaxHealth())){nextState(PHASE_II);particles(1,position().add(0,2,0),90);idleTicks=55;return;}
  if(tickCount%10==0)setTarget(selectTarget());
  LivingEntity target=getTarget();if(target!=null&&!canDamage(target))target=null;
  LivingEntity mor=sideTarget(0,target),fire=sideTarget(1,target);
  if(mor!=null&&mor==fire){LivingEntity alternative=otherTarget(mor);if(alternative!=null)fire=alternative;}
  updateHeadAim(mor,fire);
  moveInAir(target,action()==DIVE&&actionAge()>=12&&actionAge()<=42);
  if(target==null){if(action()!=IDLE)act(IDLE);return;}
  int old=actionAge();entityData.set(ACTION_AGE,old+1);
  if(grabTicks>0)tickGrab();
  if(action()==IDLE){if(--idleTicks<=0){chooseAttack(target);idleTicks=state()==PHASE_II?16:29;}return;}
  int a=actionAge();
  if(action()==MOR_BOLT&&a==18&&mor!=null)projectile(mor,0);
  if(action()==FIRE_BOLT&&a==18&&fire!=null)projectile(fire,1);
  if(action()==DOUBLE_BOLT){if(a==20&&mor!=null)projectile(mor,0);if(a==30&&fire!=null)projectile(fire,1);}
  if(action()==MOR_BREATH||action()==FIRE_BREATH){int element=action()==MOR_BREATH?0:1;if(a>=18&&a<=55&&a%4==0&&(element==0?mor:fire)!=null)breath(element==0?mor:fire,element);}
  if(action()==MOR_TAIL&&a>=20&&a<=27)tailSweep(0,a);
  if(action()==FIRE_TAIL&&a>=23&&a<=30)tailSweep(1,a);
  if((action()==BITE_MOR||action()==BITE_FIRE)&&a==20){int element=action()==BITE_MOR?0:1;LivingEntity victim=element==0?mor:fire;boolean struck=victim!=null&&strike(victim,element,false);if(struck&&!victim.isBlocking()&&victim.distanceToSqr(this)<=75&&hasLineOfSight(victim)&&canDamage(victim)){grabbed=victim.getUUID();grabTicks=14;}}
  if(action()==CLAW&&a==17)strike(target,1,false);
  if(action()==DIVE&&a==35&&fire!=null)strike(fire,1,false);
  if(action()==WING_STREAK&&a>=12&&a<=70){if(a%10==0){MorovoyZmeyField.spawn((ServerLevel)level(),this,(a/10)%2,position().add(0,-Math.min(6,getY()-anchor.y),0),50);}}
  if(a>=duration(action()))act(IDLE);
 }
 private LivingEntity selectTarget(){List<ServerPlayer> list=((ServerLevel)level()).getPlayers(p->canDamage(p)&&p.distanceToSqr(anchor)<=90*90&&p.distanceToSqr(this)<=82*82);return list.stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);}
 private int duration(int a){return switch(a){case MOR_BREATH,FIRE_BREATH->65;case WING_STREAK->80;case DIVE->55;case DOUBLE_BOLT->45;default->38;};}
 private void chooseAttack(LivingEntity target){double d=distanceToSqr(target);int[] far=state()==PHASE_II?new int[]{DOUBLE_BOLT,MOR_BREATH,FIRE_BREATH,DIVE,WING_STREAK,MOR_BOLT,FIRE_BOLT}:new int[]{MOR_BOLT,FIRE_BOLT,MOR_BREATH,FIRE_BREATH,DOUBLE_BOLT,DIVE};int[] close={MOR_TAIL,FIRE_TAIL,BITE_MOR,BITE_FIRE,CLAW,MOR_BREATH,FIRE_BREATH};int[] choices=d<64?close:far;act(choices[Math.floorMod(turn++,choices.length)]);}
 private Vec3 mouth(int element){float a=getYRot()*(float)Math.PI/180f;double right=element==0?-1.18:1.18;return position().add(-Math.sin(a)*6.5+Math.cos(a)*right,3.15,-Math.cos(a)*6.5-Math.sin(a)*right);}
 private void projectile(LivingEntity target,int element){if(!hasLineOfSight(target)||!(level() instanceof ServerLevel world))return;MorovoyZmeyBolt bolt=MorovoyFireZmeyContent.BOLT.get().create(world);if(bolt==null)return;Vec3 from=mouth(element),aim=target.getEyePosition().subtract(from).normalize();bolt.configure(this,element);bolt.setPos(from);bolt.shoot(aim.x,aim.y,aim.z,element==0?1.1f:1.4f,.6f);world.addFreshEntity(bolt);particles(element,from,15);}
 private void breath(LivingEntity target,int element){if(!(level() instanceof ServerLevel world))return;Vec3 from=mouth(element),to=target.getEyePosition().subtract(from).normalize();for(int i=1;i<=8;i++){Vec3 point=from.add(to.scale(i*1.5));if(!world.hasChunkAt(BlockPos.containing(point)))break;if(!world.getBlockState(BlockPos.containing(point)).isAir())break;if(i%2==0)particles(element,point,5);if(i%3==0)for(LivingEntity victim:world.getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(1.2),this::canDamage))if(hasLineOfSight(victim)){victim.hurt(damageSources().mobAttack(this),element==0?2.0f:2.5f);if(element==0)victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,45));}}
  if(actionAge()%12==0&&target.distanceToSqr(this)<225)MorovoyZmeyField.spawn(world,this,element,target.position(),65);
 }
 private boolean strike(LivingEntity target,int element,boolean tail){if(!hasLineOfSight(target))return false;float yaw=getYRot()*(float)Math.PI/180f;Vec3 center=tail?position().add(Math.sin(yaw)*3+Math.cos(yaw)*(element==0?-1:1),1.1,Math.cos(yaw)*3-Math.sin(yaw)*(element==0?-1:1)):mouth(element);double range=tail?6.5:3.5;if(target.distanceToSqr(center)>range*range)return false;boolean hit=target.hurt(damageSources().mobAttack(this),tail?14:10);if(hit){if(element==0)target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,75));else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35));}return hit;}
 private void tickGrab(){grabTicks--;if(!(level() instanceof ServerLevel world)||grabbed==null)return;net.minecraft.world.entity.Entity entity=world.getEntity(grabbed);if(!(entity instanceof LivingEntity victim)||!canDamage(victim)||!hasLineOfSight(victim)||distanceToSqr(victim)>36){grabTicks=0;grabbed=null;return;}if(grabTicks%5==0){Vec3 pull=mouth(action()==BITE_MOR?0:1).subtract(victim.position());victim.setDeltaMovement(victim.getDeltaMovement().add(pull.normalize().scale(.22)));victim.hurt(damageSources().mobAttack(this),2);}}
 private void passiveMor(){if(!(level() instanceof ServerLevel world))return;particles(0,position().add(0,.25,0),5);if(tickCount%90==0)MorovoyZmeyField.spawn(world,this,0,anchor,45);}
 private void particles(int element,Vec3 pos,int count){if(!(level() instanceof ServerLevel world))return;if(element!=0){PurpleFlameVisuals.burst(world,pos,count);return;}world.sendParticles(new DustParticleOptions(new Vector3f(.12f,.85f,.18f),1.1f),pos.x,pos.y,pos.z,count,.75,.45,.75,.04);}
 private boolean nearSleepingBody(ServerPlayer player){
  Vec3 relative=player.position().subtract(position());
  double radians=Math.toRadians(getYRot()),fore=-(relative.x*Math.sin(radians)+relative.z*Math.cos(radians)),side=relative.x*Math.cos(radians)-relative.z*Math.sin(radians);
  double longitudinal=fore-Mth.clamp(fore,-4.2,8.0),lateral=side-Mth.clamp(side,-1.15,1.15);
  double vertical=Math.max(0,Math.abs(player.getY()-(getY()+.95))-.95);
  return longitudinal*longitudinal+lateral*lateral+vertical*vertical<=49.0;
 }
 private LivingEntity otherTarget(LivingEntity avoid){
  if(!(level() instanceof ServerLevel world)||anchor==null)return null;
  return world.getPlayers(p->p!=avoid&&canDamage(p)&&p.distanceToSqr(anchor)<90*90&&p.distanceToSqr(this)<86*86).stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
 }
 private LivingEntity sideTarget(int element,LivingEntity fallback){
  if(!(level() instanceof ServerLevel world)||anchor==null)return fallback;
  float radians=getYRot()*Mth.DEG_TO_RAD;double rightX=Math.cos(radians),rightZ=-Math.sin(radians);
  LivingEntity choice=null;double best=Double.POSITIVE_INFINITY;
  for(ServerPlayer player:world.getPlayers(p->canDamage(p)&&p.distanceToSqr(anchor)<90*90&&p.distanceToSqr(this)<86*86)){
   double lateral=(player.getX()-getX())*rightX+(player.getZ()-getZ())*rightZ;
   double wrongSide=element==0?Math.max(0,lateral):Math.max(0,-lateral);
   double score=player.distanceToSqr(this)+wrongSide*wrongSide*4.5;
   if(score<best){best=score;choice=player;}
  }
  return choice!=null?choice:fallback;
 }
 private void updateHeadAim(LivingEntity mor,LivingEntity fire){
  aimHead(0,mor,MOR_YAW,MOR_PITCH);aimHead(1,fire,FIRE_YAW,FIRE_PITCH);
 }
 private void aimHead(int element,LivingEntity target,EntityDataAccessor<Float> yawData,EntityDataAccessor<Float> pitchData){
  if(target==null){entityData.set(yawData,Mth.lerp(.10f,entityData.get(yawData),0f));entityData.set(pitchData,Mth.lerp(.10f,entityData.get(pitchData),0f));return;}
  Vec3 origin=mouth(element),direction=target.getEyePosition().subtract(origin);
  float absYaw=(float)Math.toDegrees(Math.atan2(-direction.x,-direction.z));
  float yaw=Mth.clamp(Mth.wrapDegrees(absYaw-getYRot()),-67f,67f);
  float pitch=Mth.clamp(-(float)Math.toDegrees(Math.atan2(direction.y,Math.sqrt(direction.x*direction.x+direction.z*direction.z))),-38f,38f);
  entityData.set(yawData,Mth.lerp(.24f,entityData.get(yawData),yaw));
  entityData.set(pitchData,Mth.lerp(.24f,entityData.get(pitchData),pitch));
 }
 private void tailSweep(int element,int age){
  if(!(level() instanceof ServerLevel world))return;
  Set<UUID> hits=element==0?morTailHits:fireTailHits;
  double phase=Mth.clamp((age-(element==0?20:23))/7.0,0,1);
  double a=Math.toRadians(getYRot()),sign=element==0?-1:1;
  Vec3 rear=new Vec3(Math.sin(a),0,Math.cos(a)),side=new Vec3(Math.cos(a),0,-Math.sin(a));
  Vec3 start=position().add(rear.scale(3.2)).add(side.scale(sign*.65)).add(0,1.2,0);
  Vec3 end=start.add(rear.scale(5.6)).add(side.scale(sign*(1.4+4.2*Math.sin(phase*Math.PI*.9)))).add(0,.35+Math.sin(phase*Math.PI)*.65,0);
  AABB window=new AABB(start,end).inflate(1.5);
  for(LivingEntity victim:world.getEntitiesOfClass(LivingEntity.class,window,this::canDamage)){
   if(hits.contains(victim.getUUID())||!hasLineOfSight(victim))continue;
   Vec3 p=victim.position().add(0,victim.getBbHeight()*.5,0),v=end.subtract(start);
   double t=Mth.clamp(p.subtract(start).dot(v)/v.lengthSqr(),0,1);
   if(p.distanceToSqr(start.add(v.scale(t)))>2.0*2.0)continue;
   if(victim.hurt(damageSources().mobAttack(this),element==0?13:15)){
    hits.add(victim.getUUID());
    if(element==0)victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,75));
    else victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35));
   }
  }
 }
 private void moveInAir(LivingEntity target,boolean dive){
  if(!(level() instanceof ServerLevel world)||anchor==null)return;
  setNoGravity(true);entityData.set(AIRBORNE,true);orbitTicks++;
  double angle=orbitTicks*.028+(getId()%7)*.27,radius=16.0;
  double wave=2.8*Math.sin(angle*.75)+1.2*Math.sin(angle*1.8);
  Vec3 orbit=anchor.add(Math.cos(angle)*radius,10.5+wave,Math.sin(angle)*radius);
  Vec3 goal=dive&&target!=null?target.position().add(0,2.3,0):orbit;
  Vec3 delta=goal.subtract(position()),velocity=getDeltaMovement().scale(.72).add(delta.lengthSqr()>1e-4?delta.normalize().scale(dive?.22:.13):Vec3.ZERO);
  double limit=dive?.75:.46;if(velocity.length()>limit)velocity=velocity.normalize().scale(limit);
  if(position().y<anchor.y+3.5&&velocity.y<.15)velocity=new Vec3(velocity.x,.15,velocity.z);
  Vec3 next=position().add(velocity);
  if(next.distanceToSqr(anchor)>67*67||!world.hasChunkAt(BlockPos.containing(next))){setDeltaMovement(Vec3.ZERO);return;}
  AABB clearance=getBoundingBox().move(velocity).inflate(position().y>anchor.y+5?3.8:1.1,.24,position().y>anchor.y+5?3.8:1.1);
  if(!world.noCollision(this,clearance)){
   Vec3 up=new Vec3(velocity.x*.2,.24,velocity.z*.2);
   if(world.noCollision(this,getBoundingBox().move(up).inflate(1.1,.1,1.1)))velocity=up;else{setDeltaMovement(Vec3.ZERO);return;}
  }
  if(velocity.horizontalDistanceSqr()>.005){
   float desiredYaw=(float)Math.toDegrees(Math.atan2(-velocity.x,-velocity.z));
   float change=Mth.wrapDegrees(desiredYaw-getYRot());
   entityData.set(BANK,Mth.lerp(.20f,entityData.get(BANK),Mth.clamp(change*.56f,-27f,27f)));
   setYRot(Mth.rotLerp(.19f,getYRot(),desiredYaw));yBodyRot=getYRot();yHeadRot=getYRot();
  }
  setDeltaMovement(velocity);hasImpulse=true;
 }
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("BossState",state());tag.putInt("StateAge",stateAge());tag.putInt("Action",action());tag.putInt("ActionAge",actionAge());tag.putInt("Turn",turn);tag.putInt("Rest",idleTicks);tag.putInt("Orbit",orbitTicks);tag.putFloat("FlightBank",bank());if(anchor!=null){tag.putDouble("AnchorX",anchor.x);tag.putDouble("AnchorY",anchor.y);tag.putDouble("AnchorZ",anchor.z);}}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(STATE,Math.clamp(tag.getInt("BossState"),SLEEPING,DYING));entityData.set(STATE_AGE,Math.max(0,tag.getInt("StateAge")));entityData.set(ACTION,Math.clamp(tag.getInt("Action"),IDLE,WING_STREAK));entityData.set(ACTION_AGE,Math.max(0,tag.getInt("ActionAge")));turn=Math.max(0,tag.getInt("Turn"));idleTicks=Math.max(10,tag.getInt("Rest"));orbitTicks=Math.max(0,tag.getInt("Orbit"));entityData.set(BANK,tag.getFloat("FlightBank"));if(tag.contains("AnchorX"))anchor=new Vec3(tag.getDouble("AnchorX"),tag.getDouble("AnchorY"),tag.getDouble("AnchorZ"));grabbed=null;grabTicks=0;entityData.set(AIRBORNE,false);setNoGravity(false);}
 @Override public boolean removeWhenFarAway(double distance){return false;}
}
