package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.PartEntity;
import org.slavicmyths.combat.*;
import org.slavicmyths.desert.oasis.LostOasisRecords;
import static org.slavicmyths.desert.LamyaRules.*;

public final class LamyaEntity extends Monster implements LivingThreatBoss {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(LamyaEntity.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(LamyaEntity.class,EntityDataSerializers.INT),STAGGER=SynchedEntityData.defineId(LamyaEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> PREVIOUS=SynchedEntityData.defineId(LamyaEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> PREVIOUS_PROGRESS=SynchedEntityData.defineId(LamyaEntity.class,EntityDataSerializers.FLOAT);
 private static final LamyaGeometry GEO=LamyaGeometry.INSTANCE;
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.lamya","Ламя"),BossEvent.BossBarColor.WHITE,BossEvent.BossBarOverlay.PROGRESS);
 private final Part[] parts=new Part[GEO.groups.length];
 private final float[] currentBounds=new float[GEO.groups.length*6+9],previousBounds=new float[GEO.groups.length*6+9];
 private final int[] cooldown=new int[LENGTH.length],stun=new int[3];
 private final float[] pressure=new float[3];
 private final Set<UUID> hits=new HashSet<>();
 private final List<Zone> zones=new ArrayList<>();
 private record Zone(Vec3 p,int kind,long expiry){}
 private Vec3 home,aim=Vec3.ZERO;private UUID locked;private float lockedYaw;
 private int recovery,sequence,lastTick=-1,seenPhase=1;
 public LamyaEntity(EntityType<? extends LamyaEntity> type,Level w){super(type,w);xpReward=350;setPersistenceRequired();for(int i=0;i<parts.length;i++)parts[i]=new Part(this,i);}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,INTERNAL_HP).add(Attributes.ATTACK_DAMAGE,32).add(Attributes.ARMOR,28).add(Attributes.ARMOR_TOUGHNESS,8).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,64).add(Attributes.MOVEMENT_SPEED,.29);}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(START,0);b.define(STAGGER,0);b.define(PREVIOUS,IDLE);b.define(PREVIOUS_PROGRESS,0F);}
 public int action(){return entityData.get(ACTION);}
 public int stagger(){return entityData.get(STAGGER);}
 public int age(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
 public int visualAction(){if(isDeadOrDying())return DEATH;if(action()!=IDLE)return action();if(hurtTime>0)return HURT;return IDLE;}
 public float visualProgress(float partial){int a=visualAction();if(a==DEATH)return Math.clamp((deathTime+partial)/20F,0,1);if(a==HURT)return Math.clamp((10-hurtTime+partial)/10F,0,1);if(a==IDLE||a==WALK||a==RUN)return ((age()+partial)%LENGTH[a])/LENGTH[a];return Math.clamp((age()+partial)/LENGTH[a],0,1);}
 public int previousAction(){return entityData.get(PREVIOUS);}
 public float previousProgress(){return entityData.get(PREVIOUS_PROGRESS);}
 public float blend(float partial){int a=visualAction();if(a==SPAWN||a==DEATH||a==HURT)return 1;
  float t=Math.clamp((age()+partial)/12F,0,1);return t*t*(3-2*t);
 }
 private void change(int a){entityData.set(PREVIOUS,visualAction());entityData.set(PREVIOUS_PROGRESS,visualProgress(0));entityData.set(ACTION,a);entityData.set(START,(int)level().getGameTime());lastTick=-1;hits.clear();}
 private boolean free(){return action()==IDLE||action()==WALK||action()==RUN;}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PartEntity<?>[] getParts(){return parts;}
 @Override public boolean isPickable(){return false;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(!LostOasisRecords.bound(this)||LostOasisRecords.barVisible(this,p))bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected SoundEvent getAmbientSound(){return LamyaSounds.get("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource s){return LamyaSounds.get("hurt");}
 @Override protected SoundEvent getDeathSound(){return LamyaSounds.get("death");}
 @Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState s){playSound(LamyaSounds.get("crawl"),.85F,.85F);}
 @Override public boolean hurt(DamageSource s,float a){return super.hurt(s,a*getMaxHealth()/HP);}
 public static final class Part extends PartEntity<LamyaEntity>{
  final int index;Part(LamyaEntity p,int i){super(p);index=i;noPhysics=true;refreshDimensions();}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override protected void addAdditionalSaveData(CompoundTag n){}
  @Override protected void readAdditionalSaveData(CompoundTag n){}
  @Override public EntityDimensions getDimensions(Pose p){return EntityDimensions.scalable(1,1);}
  @Override public boolean shouldBeSaved(){return false;}
  @Override public boolean isPickable(){return getParent().isAlive();}
  @Override public boolean is(Entity e){return e==this||e==getParent();}
  @Override public boolean hurt(DamageSource s,float a){return getParent().hitPart(index,s,a);}
 }
 private boolean hitPart(int index,DamageSource s,float a){
  int head=index>=7&&index<=9?index-7:-1;boolean damaged=hurt(s,a*(head>=0?1.1F:1));
  if(damaged&&!level().isClientSide&&head>=0&&stun[head]==0){pressure[head]+=a;if(pressure[head]>=110){pressure[head]=0;stun[head]=100;playSound(LamyaSounds.get("stagger"),1.5F,.8F);}}
  return damaged;
 }
 public Vec3 localToWorld(Vec3 p){return position().add(p.yRot(-getYRot()*Mth.DEG_TO_RAD));}
 private float[] bounds(){
  float[] b=currentBounds;GEO.sampleInto(visualAction(),visualProgress(0),b);float t=blend(0);
  if(t<1){GEO.sampleInto(previousAction(),previousProgress(),previousBounds);for(int k=0;k<b.length;k++)b[k]=previousBounds[k]+(b[k]-previousBounds[k])*t;}return b;
 }
 private void updateParts(){
  float[] b=bounds();for(int i=0;i<parts.length;i++){
   int k=i*6;double lx=Double.POSITIVE_INFINITY,ly=lx,lz=lx,hx=-lx,hy=hx,hz=hx;
   for(int j=0;j<8;j++){Vec3 p=localToWorld(new Vec3(b[k+((j&1)==0?0:3)],b[k+((j&2)==0?1:4)],b[k+((j&4)==0?2:5)]));lx=Math.min(lx,p.x);ly=Math.min(ly,p.y);lz=Math.min(lz,p.z);hx=Math.max(hx,p.x);hy=Math.max(hy,p.y);hz=Math.max(hz,p.z);}
   parts[i].setPos((lx+hx)*.5,ly,(lz+hz)*.5);parts[i].setBoundingBox(new AABB(lx,ly,lz,hx,hy,hz).inflate(.03));
  }
 }
 Vec3 mouth(int head){float[] b=bounds();int k=GEO.groups.length*6+head*3;return localToWorld(new Vec3(b[k],b[k+1],b[k+2]));}
 @Override public boolean canPursue(LivingEntity e){return e!=null&&LostOasisRecords.allows(this,e)&&!(e instanceof LamyaEntity)&&e.isAlive()&&e.level()==level()&&distanceToSqr(e)<4096&&(home==null||home.distanceToSqr(e.position())<2304)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.DIRECT);}
 @Override public boolean validThreatTarget(LivingEntity e){return canPursue(e)&&(e==getTarget()||CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE)||BossThreat.recordedAttacker(this,e));}
 @Override public boolean acceptThreatTarget(LivingEntity e){if(threatAttackLocked()||!canPursue(e))return false;setTarget(e);return getTarget()==e;}
 @Override public boolean threatAttackLocked(){return !free();}
 @Override public void setTarget(LivingEntity e){if(entityData!=null&&threatAttackLocked()&&e!=getTarget())return;super.setTarget(e);}
 private void turn(Vec3 p){Vec3 d=p.subtract(position());if(d.horizontalDistanceSqr()>.001){float y=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);setYRot(y);setYBodyRot(y);setYHeadRot(y);}}
 private boolean faceGradually(Vec3 p){
  Vec3 d=p.subtract(position());if(d.horizontalDistanceSqr()<.001)return true;
  float wanted=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG),delta=Mth.wrapDegrees(wanted-getYRot()),y=getYRot()+Math.clamp(delta,-12F,12F);
  setYRot(y);setYBodyRot(y);setYHeadRot(y);return Math.abs(delta)<=12;
 }
 private void stop(){getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);}
 @Override public void tick(){
  super.tick();if(!isAlive())return;
  if(level() instanceof ServerLevel w){
   if(home==null){home=position();if(free()&&!isNoAi()){change(SPAWN);playSound(LamyaSounds.get("spawn"),2,.9F);}}
   bar.setName(getName().copy().append("  "+Math.round(getHealth()/getMaxHealth()*HP)+" / "+Math.round(HP)+" HP"));bar.setProgress(getHealth()/getMaxHealth());
   if(LostOasisRecords.bound(this)&&tickCount%10==0){for(ServerPlayer p:w.players()){if(LostOasisRecords.barVisible(this,p))bar.addPlayer(p);else bar.removePlayer(p);}}
   if(!isNoAi()){
    int mask=0;for(int i=0;i<3;i++){if(stun[i]>0){stun[i]--;if(stun[i]>0)mask|=1<<i;}else pressure[i]=Math.max(0,pressure[i]-.2F);}
    entityData.set(STAGGER,mask);if(mask!=0&&tickCount%5==0)for(int i=0;i<3;i++)if((mask&(1<<i))!=0){Vec3 p=mouth(i);w.sendParticles(ParticleTypes.SMOKE,p.x,p.y,p.z,8,.3,.3,.3,.02);}for(int i=0;i<cooldown.length;i++)if(cooldown[i]>0)cooldown[i]--;if(recovery>0)recovery--;
    if(free()){int p=phase(getHealth()/getMaxHealth());if(p>seenPhase){seenPhase=p;change(PHASE);playSound(LamyaSounds.get("phase"),2,.9F);stop();}else think(w);}
    else if(action()==SPAWN||action()==PHASE){stop();if(age()>=LENGTH[action()])change(IDLE);}
    else execute(w);
    tickZones(w);
   }
  }
  updateParts();
 }
 public void releaseFromOasis(BlockPos at){
  if(!LostOasisRecords.bound(this)||!isNoAi()||level().isClientSide)return;
  moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);home=null;locked=null;stop();change(IDLE);setInvulnerable(false);setNoAi(false);
 }
 private void walk(Vec3 dest,boolean run){
  dest=LostOasisRecords.clampMovement(this,dest);
  double d=position().distanceTo(dest);if(d<1.5){stop();if(action()!=IDLE)change(IDLE);return;}
  int a=run?RUN:WALK;if(action()!=a)change(a);faceGradually(dest);getNavigation().moveTo(dest.x,dest.y,dest.z,run?1.35:1.0);
 }
 private void think(ServerLevel w){
  if(home.distanceToSqr(position())>44*44){setTarget(null);walk(home,false);return;}
  BossThreat.selectLivingBoss(this);LivingEntity t=getTarget();
  if(!validThreatTarget(t)){setTarget(null);if(tickCount%10==0){t=w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(34),e->validThreatTarget(e)&&hasLineOfSight(e)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);if(t!=null)setTarget(t);}}
  t=getTarget();if(!validThreatTarget(t)){walk(home,false);return;}
  double d=distanceTo(t);int p=phase(getHealth()/getMaxHealth());
  if(recovery==0&&hasLineOfSight(t)&&d<19){
   if(!faceGradually(t.position())){stop();if(action()!=IDLE)change(IDLE);return;}
   int[] choices=p==3?new int[]{ULTIMATE,FIRE_WIND,SUN_WIND,FIRE_SUN,DROUGHT,FIRE,SOLAR,WIND,CLAW,SWEEP,STING}:p==2?new int[]{FIRE_WIND,SUN_WIND,FIRE_SUN,DROUGHT,FIRE,SOLAR,WIND,CLAW,SWEEP,STING}:new int[]{FIRE,SOLAR,WIND,CLAW,SWEEP,STING};
   for(int i=0;i<choices.length;i++){int a=choices[(sequence+i)%choices.length];if(cooldown[a]>0||(mask(a)&stagger())!=0||((a==CLAW||a==STING)&&d>3.5)||(a==SWEEP&&d>5))continue;begin(a,t);return;}
  }
  if(d>2.4)walk(t.position(),d>14);else{stop();faceGradually(t.position());if(action()!=IDLE)change(IDLE);}
 }
 void begin(int a,LivingEntity t){
  if(!attack(a)||!free()||(mask(a)&stagger())!=0)return;setTarget(t);if(getTarget()!=t)return;
  turn(t.position());lockedYaw=getYRot();locked=t.getUUID();aim=t.position();cooldown[a]=COOLDOWN[a];sequence++;change(a);stop();playSound(LamyaSounds.get("prepare_"+SOUND[a]),1.6F,.95F);
 }
 private void finish(){change(IDLE);locked=null;recovery=22;stop();}
 private boolean clear(Vec3 from,Vec3 to){return level().hasChunkAt(BlockPos.containing(from))&&level().hasChunkAt(BlockPos.containing(to))&&level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
 private Vec3 ground(ServerLevel w,Vec3 p){
  var hit=w.clip(new ClipContext(p.add(0,3,0),p.add(0,-6,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
  return hit.getType()==HitResult.Type.MISS?p:hit.getLocation().add(0,.15,0);
 }
 private void execute(ServerLevel w){
  LivingEntity t=getTarget();int a=action(),n=age();
  if(!canPursue(t)||locked==null||!locked.equals(t.getUUID())||(mask(a)&stagger())!=0||n>=LENGTH[a]){finish();return;}
  stop();setYRot(lockedYaw);setYHeadRot(lockedYaw);setYBodyRot(lockedYaw);
  if(n==lastTick)return;
  if(n<release(a)&&n%4==0){
   ring(w,ground(w,aim),a==ULTIMATE?7:a==DROUGHT?5:2.2,ParticleTypes.END_ROD);
   for(int i=0;i<3;i++)if((mask(a)&(1<<i))!=0){Vec3 p=mouth(i);w.sendParticles(i==2?ParticleTypes.CLOUD:i==1?ParticleTypes.END_ROD:ParticleTypes.FLAME,p.x,p.y,p.z,6,.17,.17,.17,.02);}
  }
  if(lastTick<release(a)&&n>=release(a)){playSound(LamyaSounds.get(SOUND[a]),2,1);hits.clear();}
  if(active(a,n)){
   int elapsed=n-release(a);
   if(a==CLAW||a==SWEEP||a==STING){
    updateParts();int[] ids=a==CLAW?new int[]{2,3,7,8}:a==STING?new int[]{19}:new int[]{14,15,16,17,18,19};
    for(int id:ids)for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,parts[id].getBoundingBox().inflate(.15),this::validThreatTarget)){
     if(!clear(parts[id].getBoundingBox().getCenter(),e.getBoundingBox().getCenter())||!hits.add(e.getUUID()))continue;
     if(e.hurt(damageSources().mobAttack(this),a==STING?38:30)&&CombatTargeting.canControl(this,e)){
      playSound(LamyaSounds.get("impact"),1.3F,.8F);
      Vec3 d=e.position().subtract(position()).normalize();e.setDeltaMovement(e.getDeltaMovement().add(d.scale(.65)).add(0,.35,0));e.hasImpulse=true;e.hurtMarked=true;
     }
    }
    if(a==STING&&elapsed==0){
     Vec3 tip=parts[19].getBoundingBox().getCenter(),floor=ground(w,tip);
     if(tip.distanceTo(floor)<1.4){ring(w,floor,2.3,ParticleTypes.CLOUD);for(LivingEntity v:w.getEntitiesOfClass(LivingEntity.class,new AABB(floor,floor).inflate(2.3,1.4,2.3),this::validThreatTarget)){
      if(v.position().subtract(floor).horizontalDistanceSqr()>5.29||!v.onGround()||!clear(floor.add(0,.3,0),v.getBoundingBox().getCenter())||!hits.add(v.getUUID()))continue;
      v.hurt(damageSources().mobAttack(this),20);
     }}
    }
   }else if(elapsed%8==0){
    if(a==FIRE||a==FIRE_SUN)beam(w,mouth(0),aim.add(0,1,0),ParticleTypes.FLAME,16,true,false,1.05);
    if(a==WIND)beam(w,mouth(2),aim.add(0,1,0),ParticleTypes.CLOUD,10,false,true,1.5);
    if(a==SOLAR||a==FIRE_SUN||a==ULTIMATE){
     Vec3 origin=localToWorld(new Vec3(0,6.1,-1.18));
     for(int i=-1;i<=1;i++){Vec3 q=ground(w,aim.add(i*2.8,0,Math.abs(i)*1.5));beam(w,origin,q,ParticleTypes.END_ROD,20,true,false,.65);if(elapsed==0&&clear(origin,q))addZone(w,q,FIRE_SUN,80);}
    }
    if(a==DROUGHT&&elapsed==0&&clear(mouth(1),aim.add(0,1,0)))addZone(w,ground(w,aim),DROUGHT,120);
    if(a==FIRE&&elapsed==0&&clear(mouth(0),aim.add(0,1,0)))addZone(w,ground(w,aim),FIRE,80);
    if(a==FIRE_WIND||a==SUN_WIND||a==ULTIMATE){
     Vec3 center=ground(w,aim.add(Math.sin(elapsed*.09)*2,0,0));double r=a==ULTIMATE?7:3.5;
     ring(w,center,r,a==SUN_WIND?ParticleTypes.CLOUD:ParticleTypes.FLAME);
     for(int k=0;k<14;k++){double angle=k*Math.PI/7+elapsed*.15;w.sendParticles(a==SUN_WIND?ParticleTypes.CLOUD:ParticleTypes.FLAME,center.x+r*.65*Math.cos(angle),center.y+(k%5)*.65,center.z+r*.65*Math.sin(angle),2,.1,.2,.1,.025);}
     for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(r,3,r),this::validThreatTarget)){
      if(e.position().subtract(center).horizontalDistanceSqr()>r*r||!clear(center.add(0,1,0),e.getBoundingBox().getCenter())||!hasLineOfSight(e))continue;
      e.hurt(damageSources().mobAttack(this),(a==ULTIMATE?20:14)*(a==SUN_WIND?1:LamyaDroughtEffect.fireMultiplier(e)));if(a!=SUN_WIND)e.igniteForSeconds(3);
      if(CombatTargeting.canControl(this,e)){Vec3 d=center.subtract(e.position()).multiply(1,0,1).normalize().scale(.38);e.setDeltaMovement(e.getDeltaMovement().add(d).add(0,.14,0));e.hasImpulse=true;e.hurtMarked=true;}
     }
    }
   }
  }
  lastTick=n;
 }
 private void beam(ServerLevel w,Vec3 from,Vec3 target,SimpleParticleType particle,float damage,boolean fire,boolean wind,double radius){
  Vec3 d=target.subtract(from);if(d.length()>24)d=d.normalize().scale(24);Vec3 to=from.add(d);
  var clip=w.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(clip.getType()!=HitResult.Type.MISS)to=clip.getLocation();
  int count=Math.max(1,(int)(from.distanceTo(to)*5));for(int i=0;i<=count;i++){Vec3 p=from.lerp(to,i/(double)count);w.sendParticles(particle,p.x,p.y,p.z,1,.09,.09,.09,.01);}
  for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(from,to).inflate(radius),this::validThreatTarget)){
   AABB b=e.getBoundingBox().inflate(radius);if(!b.contains(from)&&!b.contains(to)&&b.clip(from,to).isEmpty())continue;
   if(!clear(from,e.getBoundingBox().getCenter()))continue;
   e.hurt(damageSources().mobAttack(this),damage*(fire?LamyaDroughtEffect.fireMultiplier(e):1));if(fire)e.igniteForSeconds(3);
   if(wind&&CombatTargeting.canControl(this,e)){e.setDeltaMovement(e.getDeltaMovement().add(d.normalize().scale(.55)).add(0,.34,0));e.hasImpulse=true;e.hurtMarked=true;}
  }
 }
 private void addZone(ServerLevel w,Vec3 p,int kind,int ttl){if(zones.size()<12&&w.hasChunkAt(BlockPos.containing(p)))zones.add(new Zone(p,kind,w.getGameTime()+ttl));}
 private void tickZones(ServerLevel w){
  zones.removeIf(z->w.getGameTime()>=z.expiry()||!w.hasChunkAt(BlockPos.containing(z.p())));
  if(tickCount%10!=0)return;
  for(Zone z:zones){Vec3 p=z.p();double r=z.kind()==DROUGHT?5:2.5;ring(w,p,r,z.kind()==DROUGHT?ParticleTypes.SMOKE:ParticleTypes.FLAME);
   for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(r,1.8,r),this::validThreatTarget)){
    if(e.position().subtract(p).horizontalDistanceSqr()>r*r||!clear(p.add(0,.5,0),e.getBoundingBox().getCenter()))continue;
    if(z.kind()==DROUGHT){LamyaDroughtEffect.apply(e);}else{e.hurt(damageSources().mobAttack(this),6*LamyaDroughtEffect.fireMultiplier(e));e.igniteForSeconds(2);}
   }
  }
 }
 private void ring(ServerLevel w,Vec3 p,double r,SimpleParticleType type){for(int i=0;i<20;i++){double a=i*Math.PI/10;w.sendParticles(type,p.x+r*Math.cos(a),p.y+.12,p.z+r*Math.sin(a),1,.02,.04,.02,0);}}
 @Override public void die(DamageSource s){zones.clear();super.die(s);bar.removeAllPlayers();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putIntArray("LamyaCooldown",cooldown);n.putIntArray("LamyaStun",stun);n.putInt("LamyaSequence",sequence);n.putInt("LamyaPhase",seenPhase);if(home!=null){n.putDouble("LamyaX",home.x);n.putDouble("LamyaY",home.y);n.putDouble("LamyaZ",home.z);}}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);int[] a=n.getIntArray("LamyaCooldown"),b=n.getIntArray("LamyaStun");for(int i=0;i<Math.min(a.length,cooldown.length);i++)cooldown[i]=Math.max(0,a[i]);for(int i=0;i<Math.min(b.length,3);i++)stun[i]=Math.clamp(b[i],0,100);sequence=Math.max(0,n.getInt("LamyaSequence"));seenPhase=Math.clamp(n.getInt("LamyaPhase"),1,3);if(n.contains("LamyaX"))home=new Vec3(n.getDouble("LamyaX"),n.getDouble("LamyaY"),n.getDouble("LamyaZ"));zones.clear();locked=null;change(IDLE);}
}
