package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.PartEntity;
import org.slavicmyths.combat.*;
import static org.slavicmyths.desert.IndrikRules.*;

public final class IndrikEntity extends Monster implements LivingThreatBoss {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(IndrikEntity.class,EntityDataSerializers.INT), START=SynchedEntityData.defineId(IndrikEntity.class,EntityDataSerializers.INT);
 private static final IndrikGeometry GEO=IndrikGeometry.INSTANCE;
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.indrik_zmey","Индрик-змей"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 private final Part[] parts=new Part[GEO.groups.length];
 private final int[] cd=new int[LENGTH.length];
 private final Set<UUID> hits=new HashSet<>();
 private Vec3 home,lockedDirection;
 private UUID lockedTarget;
 private float lockedYaw;
 private int recovery,lastExecuted=-1,dashes,sequence;
 private final List<Shard> shards=new ArrayList<>();
 private record Shard(Vec3 pos,Vec3 vel,int ttl){}
 public IndrikEntity(EntityType<? extends IndrikEntity> type,Level w){super(type,w);xpReward=220;setPersistenceRequired();for(int i=0;i<parts.length;i++)parts[i]=new Part(this,i);}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,INTERNAL_HP).add(Attributes.ATTACK_DAMAGE,18).add(Attributes.ARMOR,18).add(Attributes.ARMOR_TOUGHNESS,6).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,64).add(Attributes.MOVEMENT_SPEED,.25);}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(START,0);}
 public int action(){return entityData.get(ACTION);}
 private void action(int a){entityData.set(ACTION,a);entityData.set(START,(int)level().getGameTime());lastExecuted=-1;hits.clear();}
 public int age(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
 public int battlePhase(){return phase(getHealth()/getMaxHealth());}
 public int visualAction(){if(isDeadOrDying())return DEATH;if(action()!=IDLE)return action();if(hurtTime>0)return HURT;if(getDeltaMovement().horizontalDistanceSqr()>.0007)return getDeltaMovement().horizontalDistanceSqr()>.12?RUN:WALK;return battlePhase()==3?ENRAGED:IDLE;}
 public float visualProgress(float partial){int a=visualAction();if(a==DEATH)return Mth.clamp((deathTime+partial)/20F,0,1);if(a==HURT)return Mth.clamp((10-hurtTime+partial)/10F,0,1);if(a==WALK||a==RUN||a==IDLE||a==ENRAGED)return ((tickCount+partial)%LENGTH[a])/LENGTH[a];return Mth.clamp((age()+partial)/LENGTH[a],0,1);}
 public boolean isUnderground(){return action()==BURROW&&underground((float)age()/LENGTH[BURROW]);}
 @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback("entity.slavicmyths.indrik_zmey","Индрик-змей");}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PartEntity<?>[] getParts(){return parts;}
 @Override public boolean isPickable(){return false;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected SoundEvent getAmbientSound(){return SoundEvents.RAVAGER_AMBIENT;}
 @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.RAVAGER_HURT;}
 @Override protected SoundEvent getDeathSound(){return SoundEvents.RAVAGER_DEATH;}
 @Override protected void playStepSound(BlockPos p,BlockState s){playSound(SoundEvents.RAVAGER_STEP,.9F,.7F);}
 @Override public boolean hurt(DamageSource s,float amount){return super.hurt(s,internalDamage(isUnderground()?amount*.35F:amount,getMaxHealth()));}
 public static final class Part extends PartEntity<IndrikEntity>{
  final int index;boolean active;Part(IndrikEntity p,int i){super(p);index=i;noPhysics=true;refreshDimensions();}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override protected void addAdditionalSaveData(CompoundTag n){}
  @Override protected void readAdditionalSaveData(CompoundTag n){}
  @Override public EntityDimensions getDimensions(Pose p){return EntityDimensions.scalable(1,1);}
  @Override public boolean shouldBeSaved(){return false;}
  @Override public boolean isPickable(){return active&&getParent().isAlive();}
  @Override public boolean is(Entity e){return e==this||e==getParent();}
  @Override public boolean hurt(DamageSource s,float a){return active&&getParent().hurt(s,a*(GEO.groups[index].equals("head")?1.15F:1));}
 }
 private void updateParts(){
  if(parts==null)return;int a=visualAction();float progress=visualProgress(0);float[] p=GEO.sample(a,progress),v=new float[24];int n=parts.length;double[][] lo=new double[n][3],hi=new double[n][3];
  for(int i=0;i<n;i++){Arrays.fill(lo[i],Double.POSITIVE_INFINITY);Arrays.fill(hi[i],Double.NEGATIVE_INFINITY);}
  double angle=Math.toRadians(getYRot()),c=Math.cos(angle),s=Math.sin(angle);
  for(int i=0;i<GEO.count;i++){GEO.cube(i,p,v);if(!GEO.visible(i,a,progress,v))continue;int g=GEO.part[i];for(int k=0;k<24;k+=3){double x=getX()+v[k]*c-v[k+2]*s,y=getY()+v[k+1],z=getZ()+v[k]*s+v[k+2]*c;if(a==BURROW)y=Math.max(getY(),y);lo[g][0]=Math.min(lo[g][0],x);lo[g][1]=Math.min(lo[g][1],y);lo[g][2]=Math.min(lo[g][2],z);hi[g][0]=Math.max(hi[g][0],x);hi[g][1]=Math.max(hi[g][1],y);hi[g][2]=Math.max(hi[g][2],z);}}
  for(int i=0;i<n;i++){double[] l=lo[i],h=hi[i];Part part=parts[i];part.active=Double.isFinite(l[0]);if(!part.active){part.setPos(getX(),getY()-8,getZ());part.setBoundingBox(new AABB(part.position(),part.position()));continue;}part.setPos((l[0]+h[0])*.5,l[1],(l[2]+h[2])*.5);part.setBoundingBox(new AABB(l[0],l[1],l[2],h[0],h[1],h[2]).inflate(.035));}
 }
 @Override public boolean canPursue(LivingEntity e){return e!=null&&!(e instanceof IndrikEntity)&&e.isAlive()&&e.level()==level()&&distanceToSqr(e)<=96*96&&(home==null||home.distanceToSqr(e.position())<=80*80)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.DIRECT);}
 @Override public boolean validThreatTarget(LivingEntity e){return canPursue(e)&&(e==getTarget()||CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE)||BossThreat.recordedAttacker(this,e));}
 @Override public boolean acceptThreatTarget(LivingEntity e){if(threatAttackLocked()||!validThreatTarget(e))return false;setTarget(e);return getTarget()==e;}
 @Override public boolean threatAttackLocked(){return action()!=IDLE;}
 @Override public void setTarget(LivingEntity e){if(entityData!=null&&action()!=IDLE&&e!=getTarget())return;super.setTarget(e);}
 private boolean hostile(LivingEntity e){return canPursue(e)&&(e==getTarget()||BossThreat.recordedAttacker(this,e)||CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE));}
 private void turn(Vec3 p){Vec3 d=p.subtract(position());if(d.horizontalDistanceSqr()<.001)return;float y=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);setYRot(y);setYBodyRot(y);setYHeadRot(y);}
 private void holdYaw(){setYRot(lockedYaw);setYBodyRot(lockedYaw);setYHeadRot(lockedYaw);}
 @Override public void tick(){super.tick();if(!isAlive()){updateParts();return;}if(level() instanceof ServerLevel w){if(home==null)home=position();bar.setName(getName().copy().append("  "+Math.round(logicalHealth(getHealth(),getMaxHealth()))+" / "+Math.round(HP)+" HP"));bar.setProgress(getHealth()/getMaxHealth());for(int i=0;i<cd.length;i++)if(cd[i]>0)cd[i]--;if(recovery>0)recovery--;updateShards(w);if(action()==IDLE)think(w);else execute(w);}updateParts();}
 private void think(ServerLevel w){
  if(home.distanceToSqr(position())>80*80){setTarget(null);if(tickCount%10==0)getNavigation().moveTo(home.x,home.y,home.z,1);return;}
  BossThreat.selectLivingBoss(this);LivingEntity t=getTarget();if(!validThreatTarget(t)){setTarget(null);if(tickCount%10==0){t=w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(32),p->validThreatTarget(p)&&hasLineOfSight(p)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);if(t!=null)setTarget(t);}}
  t=getTarget();if(!validThreatTarget(t)){if(tickCount%10==0&&home.distanceToSqr(position())>4)getNavigation().moveTo(home.x,home.y,home.z,1);return;}
  double d=distanceTo(t);turn(t.position());if(recovery==0&&hasLineOfSight(t)){
   int a=IDLE;if(battlePhase()>=2&&d<=20&&cd[BURROW]==0&&sequence%4==0)a=BURROW;else if(battlePhase()>=2&&d<=10&&cd[QUAKE]==0&&sequence%3==0)a=QUAKE;else if(d<=6&&cd[CLAW]==0)a=CLAW;else if(d<=9&&cd[TAIL]==0)a=TAIL;else if(d>=5&&d<=24&&cd[CHARGE]==0)a=CHARGE;else if(battlePhase()==3&&d<=12&&cd[ROAR]==0&&sequence%4==2)a=ROAR;else if(d<=24&&cd[MINERAL]==0)a=MINERAL;
   if(a!=IDLE){begin(a,t);return;}}
  if(d>4){if(tickCount%8==0)getNavigation().moveTo(t,1);}else getNavigation().stop();
 }
 void begin(int a,LivingEntity target){turn(target.position());lockedYaw=getYRot();lockedDirection=Vec3.directionFromRotation(0,lockedYaw);lockedTarget=target.getUUID();dashes=a==CHARGE?charges(battlePhase()):1;cd[a]=COOLDOWN[a];sequence++;action(a);getNavigation().stop();playSound(SoundEvents.RAVAGER_ROAR,1.5F,.65F);}
 private boolean reached(int t){return lastExecuted<t&&age()>=t;}
 private void execute(ServerLevel w){
  int a=action(),t=age();LivingEntity target=getTarget();if(target==null||!target.getUUID().equals(lockedTarget)||!canPursue(target)){finish();return;}
  if(a==BURROW&&isUnderground()){
   turn(target.position());lockedYaw=getYRot();if(tickCount%5==0)getNavigation().moveTo(target,1.8);if(t%20==0)hits.clear();if(t%4==0){updateParts();for(Part p:parts)if(p.active)hitBox(w,p.getBoundingBox().inflate(.2),12,1.0);dust(w,position(),2.5);}
  }else{
   getNavigation().stop();holdYaw();if(a==CHARGE&&t>=15&&t<=32){if(!horizontalCollision){setDeltaMovement(lockedDirection.scale(.74).add(0,getDeltaMovement().y,0));hasImpulse=true;}updateParts();for(Part p:parts)if(p.active)hitBox(w,p.getBoundingBox().inflate(.2),28,1.5);}
   else setDeltaMovement(0,getDeltaMovement().y,0);
  }
  if(t<RELEASE[a]&&t%5==0&&(a==QUAKE||a==BURROW||a==CHARGE))dust(w,position(),a==QUAKE?7:3);
  if(a==CLAW&&t>=17&&t<=24){updateParts();for(Part p:parts)if(p.active&&GEO.groups[p.index].startsWith("FR"))hitBox(w,p.getBoundingBox().inflate(.2),22,1.1);}
  if(a==TAIL&&t>=18&&t<=34){updateParts();for(Part p:parts)if(p.active&&GEO.groups[p.index].startsWith("tail"))hitBox(w,p.getBoundingBox().inflate(.3),24,1.5);}
  if(a==QUAKE&&t>=37&&t<=56){double radius=1+(t-37)*.55;area(w,position(),radius,30,1.4,true);if(t%3==1)dust(w,position(),radius);}
  if(a==BURROW&&reached(RELEASE[BURROW])){hits.clear();area(w,position(),5,30,1.6,false);dust(w,position(),5);playSound(SoundEvents.RAVAGER_ATTACK,1.8F,.6F);}
  if(a==MINERAL&&reached(RELEASE[MINERAL])){Vec3 start=position().add(0,3.5,0).add(lockedDirection.scale(3));for(int i=-2;i<=2;i++){Vec3 dir=target.getEyePosition().subtract(start).normalize().yRot(i*.14F);if(shards.size()>=24)shards.removeFirst();shards.add(new Shard(start,dir.scale(.6),40));}}
  if(a==ROAR&&reached(RELEASE[ROAR])){area(w,position(),10,10,1.0,true);playSound(SoundEvents.RAVAGER_ROAR,2,.55F);}
  if(reached(RELEASE[a])&&a!=BURROW)playSound(SoundEvents.RAVAGER_ATTACK,1.5F,.7F);
  lastExecuted=t;
  if(t>=LENGTH[a]){if(a==CHARGE&&--dashes>0){turn(target.position());lockedYaw=getYRot();lockedDirection=Vec3.directionFromRotation(0,lockedYaw);action(CHARGE);}else finish();}
 }
 private void finish(){action(IDLE);recovery=20;lockedTarget=null;lockedDirection=null;getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);}
 private void hitBox(ServerLevel w,AABB box,float amount,double knock){for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,box,this::hostile))if(hasLineOfSight(e))damage(e,amount,knock);}
 private boolean damage(LivingEntity e,float amount,double knock){if(!hits.add(e.getUUID()))return false;if(!e.hurt(damageSources().mobAttack(this),amount))return false;if(knock>0){Vec3 d=e.position().subtract(position());e.knockback(knock,-d.x,-d.z);}return true;}
 private void area(ServerLevel w,Vec3 p,double r,float amount,double knock,boolean grounded){for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(r,3,r),this::hostile)){if(e.position().subtract(p).horizontalDistanceSqr()>r*r||!hasLineOfSight(e)||(grounded&&!e.onGround()))continue;if(damage(e,amount,knock)&&grounded)e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,1),this);}}
 private void dust(ServerLevel w,Vec3 p,double r){for(int i=0;i<16;i++){double a=i*Math.PI/8;w.sendParticles(ParticleTypes.CLOUD,p.x+r*Math.cos(a),p.y+.15,p.z+r*Math.sin(a),1,.1,.08,.1,.01);}}
 private void updateShards(ServerLevel w){for(var it=shards.listIterator();it.hasNext();){Shard shard=it.next();Vec3 start=shard.pos(),end=start.add(shard.vel());if(shard.ttl()<=0||!w.isLoaded(BlockPos.containing(end))){it.remove();continue;}var block=w.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));double best=block.getType()==HitResult.Type.MISS?Double.POSITIVE_INFINITY:start.distanceToSqr(block.getLocation());LivingEntity struck=null;for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(.4),this::hostile)){var box=e.getBoundingBox().inflate(.25);double d=box.contains(start)?0:box.clip(start,end).map(start::distanceToSqr).orElse(Double.POSITIVE_INFINITY);if(d<best){best=d;struck=e;}}if(struck!=null){struck.hurt(damageSources().mobAttack(this),14);it.remove();continue;}if(block.getType()!=HitResult.Type.MISS){it.remove();continue;}w.sendParticles(ParticleTypes.END_ROD,end.x,end.y,end.z,3,.06,.06,.06,0);it.set(new Shard(end,shard.vel(),shard.ttl()-1));}}
 @Override public void die(DamageSource s){super.die(s);bar.removeAllPlayers();shards.clear();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putIntArray("IndrikCooldowns",cd);n.putInt("IndrikSequence",sequence);if(home!=null){n.putDouble("IndrikHomeX",home.x);n.putDouble("IndrikHomeY",home.y);n.putDouble("IndrikHomeZ",home.z);}}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);int[] saved=n.getIntArray("IndrikCooldowns");for(int i=0;i<Math.min(saved.length,cd.length);i++)cd[i]=Math.max(0,saved[i]);sequence=n.getInt("IndrikSequence");if(n.contains("IndrikHomeX"))home=new Vec3(n.getDouble("IndrikHomeX"),n.getDouble("IndrikHomeY"),n.getDouble("IndrikHomeZ"));action(IDLE);}
}
