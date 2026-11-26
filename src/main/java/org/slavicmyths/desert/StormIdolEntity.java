package org.slavicmyths.desert;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.PartEntity;
import org.slavicmyths.combat.*;
import org.slavicmyths.desert.winds.FourWindsRecords;
import static org.slavicmyths.desert.StormIdolRules.*;

public final class StormIdolEntity extends Monster implements LivingThreatBoss {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(StormIdolEntity.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(StormIdolEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> TX=SynchedEntityData.defineId(StormIdolEntity.class,EntityDataSerializers.FLOAT),TY=SynchedEntityData.defineId(StormIdolEntity.class,EntityDataSerializers.FLOAT),TZ=SynchedEntityData.defineId(StormIdolEntity.class,EntityDataSerializers.FLOAT);
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.storm_idol","Проклятый идол"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
 private final Part[] parts=new Part[6];
 private final int[] cooldown=new int[LENGTH.length];
 private final List<Set<UUID>> hits=new ArrayList<>();
 private final Vec3[] previous=new Vec3[FISTS];
 private Vec3 home; private UUID locked; private float lockedYaw; private int recovery,sequence,lastTick=-1;
 public StormIdolEntity(EntityType<? extends StormIdolEntity> type,Level w){super(type,w);xpReward=260;setPersistenceRequired();setNoGravity(true);for(int i=0;i<parts.length;i++)parts[i]=new Part(this,i);for(int i=0;i<FISTS;i++)hits.add(new HashSet<>());}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,INTERNAL_HP).add(Attributes.ATTACK_DAMAGE,24).add(Attributes.ARMOR,24).add(Attributes.ARMOR_TOUGHNESS,6).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,64).add(Attributes.MOVEMENT_SPEED,.28);}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(START,0);b.define(TX,0F);b.define(TY,0F);b.define(TZ,0F);}
 public int action(){return entityData.get(ACTION);}
 public int age(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
 public Vec3 aim(){return new Vec3(entityData.get(TX),entityData.get(TY),entityData.get(TZ));}
 public float animationTime(float partial){return (float)(level().getGameTime()%24000L)+partial;}
 public Vec3 fistLocal(int i,float partial){return fist(action(),i,age()+partial,animationTime(partial),aim());}
 public Vec3 localToWorld(Vec3 p){return position().add(p.yRot(-getYRot()*Mth.DEG_TO_RAD));}
 private void change(int a){entityData.set(START,(int)level().getGameTime());entityData.set(ACTION,a);lastTick=-1;for(var h:hits)h.clear();Arrays.fill(previous,null);}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PartEntity<?>[] getParts(){return parts;}
 @Override public boolean isPickable(){return false;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected SoundEvent getAmbientSound(){return StormIdolSounds.get("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource s){return StormIdolSounds.get("hurt");}
 @Override protected SoundEvent getDeathSound(){return StormIdolSounds.get("death");}
 @Override public boolean hurt(DamageSource s,float a){return super.hurt(s,a*getMaxHealth()/HP);}
 public static final class Part extends PartEntity<StormIdolEntity>{
  final int index;
  Part(StormIdolEntity p,int i){super(p);index=i;noPhysics=true;refreshDimensions();}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override protected void addAdditionalSaveData(CompoundTag n){}
  @Override protected void readAdditionalSaveData(CompoundTag n){}
  @Override public EntityDimensions getDimensions(Pose p){return EntityDimensions.scalable(1,1);}
  @Override public boolean shouldBeSaved(){return false;}
  @Override public boolean isPickable(){return getParent().isAlive();}
  @Override public boolean is(Entity e){return e==this||e==getParent();}
  @Override public boolean hurt(DamageSource s,float a){return getParent().hurt(s,a*(index==1?1.15F:1));}
 }
 private void parts(){
  double b=bob(animationTime(0));
  place(parts[0],localToWorld(new Vec3(0,2.85+b,0)),.76,.83);
  place(parts[1],localToWorld(new Vec3(0,4.02+b,0)),.45,.55);
  for(int i=0;i<FISTS;i++)place(parts[2+i],localToWorld(fistLocal(i,0).add(0,b,0)),.42,.38);
 }
 private static void place(Part p,Vec3 center,double width,double height){p.setPos(center.x,center.y-height,center.z);p.setBoundingBox(new AABB(center.x-width,center.y-height,center.z-width,center.x+width,center.y+height,center.z+width));}
 @Override public boolean canPursue(LivingEntity e){return e!=null&&!(e instanceof StormIdolEntity)&&e.isAlive()&&e.level()==level()&&FourWindsRecords.allows(this,e)&&distanceToSqr(e)<64*64&&(home==null||home.distanceToSqr(e.position())<48*48)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.DIRECT);}
 @Override public boolean validThreatTarget(LivingEntity e){return canPursue(e)&&(e==getTarget()||CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE)||BossThreat.recordedAttacker(this,e));}
 @Override public boolean acceptThreatTarget(LivingEntity e){if(threatAttackLocked()||!canPursue(e))return false;setTarget(e);return getTarget()==e;}
 @Override public boolean threatAttackLocked(){return action()!=IDLE;}
 @Override public void setTarget(LivingEntity e){if(entityData!=null&&action()!=IDLE&&e!=getTarget())return;super.setTarget(e);}
 private boolean hostile(LivingEntity e){return validThreatTarget(e);}
 private void turn(Vec3 p){Vec3 d=p.subtract(position());if(d.horizontalDistanceSqr()>.001){float y=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);setYRot(y);setYBodyRot(y);setYHeadRot(y);}}
 @Override public void tick(){
  setNoGravity(true);super.tick();
  if(!isAlive())return;
  if(level() instanceof ServerLevel w){
   if(home==null)home=position();
   bar.setName(getName().copy().append("  "+Math.round(getHealth()/getMaxHealth()*HP)+" / "+Math.round(HP)+" HP"));bar.setProgress(getHealth()/getMaxHealth());
   if(!isNoAi()){
    for(int i=0;i<cooldown.length;i++)if(cooldown[i]>0)cooldown[i]--;if(recovery>0)recovery--;
    if(action()==IDLE)think(w);else execute(w);
   }
  }
  parts();
 }
 private void fly(Vec3 dest,double speed){
  dest=FourWindsRecords.constrain(this,dest);
  Vec3 d=dest.subtract(position());Vec3 v=d.lengthSqr()<.16?Vec3.ZERO:d.normalize().scale(Math.min(speed,d.length()*.16));
  if(level().clip(new ClipContext(position().add(0,1,0),position().add(0,1,0).add(v.scale(3)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)v=Vec3.ZERO;
  setDeltaMovement(getDeltaMovement().scale(.55).add(v.scale(.45)));hasImpulse=true;
 }
 private void think(ServerLevel w){
  if(home.distanceToSqr(position())>44*44){setTarget(null);fly(home,.28);return;}
  BossThreat.selectLivingBoss(this);LivingEntity t=getTarget();
  if(!validThreatTarget(t)){setTarget(null);if(tickCount%10==0){t=w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(32),e->validThreatTarget(e)&&hasLineOfSight(e)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);if(t!=null)setTarget(t);}}
  t=getTarget();if(!validThreatTarget(t)){fly(home,.2);return;}
  turn(t.position());double d=distanceTo(t);int phase=phase(getHealth()/getMaxHealth());
  if(recovery==0&&hasLineOfSight(t)){
   int a=IDLE;
   if(phase==3&&d<10&&cooldown[STORM]==0&&sequence%3==0)a=STORM;
   else if(phase>=2&&d<14&&cooldown[SLAM]==0&&sequence%3==1)a=SLAM;
   else if(d<14&&cooldown[UPDRAFT]==0&&sequence%3==2)a=UPDRAFT;
   else if(d<5&&cooldown[COMBO]==0)a=COMBO;
   else if(d<17&&cooldown[LAUNCH]==0)a=LAUNCH;
   else if(d<14&&cooldown[UPDRAFT]==0)a=UPDRAFT;
   if(a!=IDLE){begin(a,t);return;}
  }
  Vec3 away=position().subtract(t.position());Vec3 flat=new Vec3(away.x,0,away.z).normalize();
  Vec3 dest=t.position().add(flat.scale(4)).add(0,.35,0);fly(dest,.28);
 }
 void begin(int a,LivingEntity t){
  setTarget(t);turn(t.position());lockedYaw=getYRot();locked=t.getUUID();
  Vec3 q=t.getBoundingBox().getCenter().subtract(position()).yRot(lockedYaw*Mth.DEG_TO_RAD);
  entityData.set(TX,(float)q.x);entityData.set(TY,(float)q.y);entityData.set(TZ,(float)q.z);
  cooldown[a]=COOLDOWN[a];sequence++;change(a);setDeltaMovement(Vec3.ZERO);playSound(StormIdolSounds.get("charge"),1.3F,.8F);
 }
 private void finish(){change(IDLE);locked=null;recovery=18;setDeltaMovement(Vec3.ZERO);}
 private void execute(ServerLevel w){
  LivingEntity t=getTarget();if(!canPursue(t)||!t.getUUID().equals(locked)){finish();return;}
  setYRot(lockedYaw);setYHeadRot(lockedYaw);setYBodyRot(lockedYaw);setDeltaMovement(Vec3.ZERO);
  int a=action(),n=age();if(n>=LENGTH[a]){finish();return;}
  if(n==lastTick)return;Vec3 target=localToWorld(aim());
  if(a==UPDRAFT&&n<17&&n%4==0||a==SLAM&&n<28&&n%5==0)ring(w,target,1.6);
  if(a==UPDRAFT&&lastTick<17&&n>=17){
   for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(target,target).inflate(1.7,2.5,1.7),this::hostile)){
    if(!hasLineOfSight(e)||!CombatTargeting.canControl(this,e))continue;
    e.setDeltaMovement(e.getDeltaMovement().x,1.15,e.getDeltaMovement().z);e.hasImpulse=true;e.hurtMarked=true;e.hurt(damageSources().mobAttack(this),12);
   }playSound(StormIdolSounds.get("updraft"),1.6F,1F);
  }
  if(a==UPDRAFT&&n>=17&&n<37&&n%3==0)w.sendParticles(ParticleTypes.CLOUD,target.x,target.y,target.z,12,.65,1.5,.65,.06);
  double b=bob(animationTime(0));
  for(int i=0;i<FISTS;i++){
   Vec3 p=localToWorld(fistLocal(i,0).add(0,b,0)),prev=previous[i]==null?p:previous[i];previous[i]=p;
   if(!active(a,i,n))continue;
   if(a==STORM&&n%20==0)hits.get(i).clear();
   if(!active(a,i,lastTick)){playSound(StormIdolSounds.get("launch"),1.15F,.9F+i*.07F);}
   if(!w.isLoaded(BlockPos.containing(p)))continue;
   var clip=w.clip(new ClipContext(prev,p,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
   Vec3 end=clip.getType()==HitResult.Type.MISS?p:clip.getLocation();
   // Abort on a solid obstruction, rather than allow an invisible fist hit through a wall.
   if(clip.getType()!=HitResult.Type.MISS){finish();return;}
   for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(prev,end).inflate(.46),this::hostile)){
    AABB box=e.getBoundingBox().inflate(.36);
    if(!box.contains(prev)&&!box.contains(end)&&box.clip(prev,end).isEmpty())continue;
    if(!hasLineOfSight(e)||!hits.get(i).add(e.getUUID()))continue;
    if(e.hurt(damageSources().mobAttack(this),a==LAUNCH?32:a==SLAM?35:a==STORM?20:22)){
     e.knockback(1.1,getX()-e.getX(),getZ()-e.getZ());playSound(StormIdolSounds.get("impact"),1.5F,.8F);
    }
   }
   if(n%2==0)w.sendParticles(ParticleTypes.CLOUD,p.x,p.y,p.z,3,.19,.19,.19,.03);
  }
  if(a==SLAM&&lastTick<40&&n>=40){ring(w,target,3);for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(target,target).inflate(3,1.7,3),this::hostile)){if(e.position().subtract(target).horizontalDistanceSqr()>9||!e.onGround()||!hasLineOfSight(e)||hits.stream().anyMatch(h->h.contains(e.getUUID())))continue;e.hurt(damageSources().mobAttack(this),18);e.knockback(.9,target.x-e.getX(),target.z-e.getZ());}playSound(StormIdolSounds.get("impact"),1.7F,.7F);}
  if(a==STORM&&n>=22&&n<=96&&n%5==0){
   ring(w,position().add(0,.3,0),5);
   for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(5,1,5),this::hostile)){
    if(distanceToSqr(e)>36||!hasLineOfSight(e)||!CombatTargeting.canControl(this,e))continue;
    Vec3 d=position().subtract(e.position());Vec3 v=new Vec3(d.x,0,d.z).normalize().scale(.16);
    e.setDeltaMovement(e.getDeltaMovement().add(v));e.hasImpulse=true;e.hurtMarked=true;
   }
   if(n==25)playSound(StormIdolSounds.get("storm"),2,.75F);
  }
  lastTick=n;
 }
 private void ring(ServerLevel w,Vec3 p,double r){for(int i=0;i<16;i++){double a=i*Math.PI/8;w.sendParticles(ParticleTypes.CLOUD,p.x+r*Math.cos(a),p.y-.45,p.z+r*Math.sin(a),1,.05,.1,.05,.015);}}
 @Override public void die(DamageSource s){super.die(s);bar.removeAllPlayers();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putIntArray("StormCooldown",cooldown);n.putInt("StormSequence",sequence);if(home!=null){n.putDouble("StormX",home.x);n.putDouble("StormY",home.y);n.putDouble("StormZ",home.z);}}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);int[] saved=n.getIntArray("StormCooldown");for(int i=0;i<Math.min(saved.length,cooldown.length);i++)cooldown[i]=Math.max(0,saved[i]);sequence=n.getInt("StormSequence");if(n.contains("StormX"))home=new Vec3(n.getDouble("StormX"),n.getDouble("StormY"),n.getDouble("StormZ"));change(IDLE);}
}
