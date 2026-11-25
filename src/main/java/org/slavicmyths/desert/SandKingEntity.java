package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
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
import org.joml.Vector3f;
import org.slavicmyths.combat.*;
import static org.slavicmyths.desert.SandKingRules.*;
public final class SandKingEntity extends Monster {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(SandKingEntity.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(SandKingEntity.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(SandKingEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> MIRROR=SynchedEntityData.defineId(SandKingEntity.class,EntityDataSerializers.BOOLEAN),LANDED=SynchedEntityData.defineId(SandKingEntity.class,EntityDataSerializers.BOOLEAN);
 private static final SandKingGeometry GEO=SandKingGeometry.INSTANCE;
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.sand_king","Царь песков"),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS);
 private final Part[] parts=new Part[GEO.groups.length];
 private final int[] cooldowns=new int[7];
 private final Set<UUID> shockHits=new HashSet<>();
 private final List<PoisonPatch> poison=new ArrayList<>();
 private Vec3 home,lockedPoint,lastKnown,previousTip,jumpDirection,shockCenter;
 private float lockedYaw;
 private int recovery,unseen,noTarget,flightStart=-1,landingTick=-1;
 private boolean stingHit,retreating;
 private record PoisonPatch(Vec3 at,long expires){}
 public static final class Part extends PartEntity<SandKingEntity>{
  final int index;
  Part(SandKingEntity parent,int index){super(parent);this.index=index;noPhysics=true;refreshDimensions();}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override protected void addAdditionalSaveData(CompoundTag n){}
  @Override protected void readAdditionalSaveData(CompoundTag n){}
  @Override public EntityDimensions getDimensions(Pose pose){return EntityDimensions.scalable(1,1);}
  @Override public boolean shouldBeSaved(){return false;}
  @Override public boolean isPickable(){return true;}
  @Override public boolean is(Entity e){return e==this||e==getParent();}
  @Override public boolean hurt(DamageSource source,float amount){return getParent().hurtPart(source,amount,GEO.multiplier(index));}
 }
 public SandKingEntity(EntityType<? extends SandKingEntity> type,Level level){super(type,level);xpReward=180;setPersistenceRequired();for(int i=0;i<parts.length;i++)parts[i]=new Part(this,i);}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,800).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.ARMOR,20).add(Attributes.ARMOR_TOUGHNESS,8).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,48).add(Attributes.MOVEMENT_SPEED,.27);}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(START,0);b.define(PHASE,1);b.define(MIRROR,false);b.define(LANDED,false);}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PartEntity<?>[] getParts(){return parts;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public boolean removeWhenFarAway(double distance){return false;}
 @Override public boolean canBeAffected(MobEffectInstance effect){return !effect.getEffect().equals(MobEffects.POISON)&&super.canBeAffected(effect);}
 @Override public boolean isPickable(){return false;}
 @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback("entity.slavicmyths.sand_king","Царь песков");}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected SoundEvent getAmbientSound(){return SandKingSounds.AMBIENT.get();}
 @Override protected SoundEvent getHurtSound(DamageSource s){return SandKingSounds.HURT.get();}
 @Override protected SoundEvent getDeathSound(){return SandKingSounds.DEATH.get();}
 @Override protected void playStepSound(BlockPos pos,BlockState state){playSound(SandKingSounds.STEP.get(),.65F,.85F+random.nextFloat()*.2F);}
 public int action(){return entityData.get(ACTION);}
 public int battlePhase(){return entityData.get(PHASE);}
 public int actionTicks(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
 public boolean mirrored(){return entityData.get(MIRROR);}
 public int visualAction(){return action()==IDLE&&getDeltaMovement().horizontalDistanceSqr()>.0008?WALK:action();}
 public float visualProgress(float partial){int a=visualAction();float age=actionTicks()+partial;if(a==WALK)return ((tickCount+partial)%16)/16F;if(a==JUMP){if(entityData.get(LANDED))return 1;return age<10?.12F*age/10:Mth.clamp(.12F+.76F*(age-10)/24F,.12F,.88F);}return a==IDLE?0:Mth.clamp(age/duration(a),0,1);}
 public float[] pose(float partial){return GEO.sample(visualAction(),visualProgress(partial),mirrored());}
 private Vec3 forward(){return Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();}
 private Vec3 localToWorld(double x,double y,double z){double a=getYRot()*Math.PI/180;return position().add(x*Math.cos(a)-z*Math.sin(a),y,x*Math.sin(a)+z*Math.cos(a));}
 private Vec3 tip(float[] vertices){double[] p=GEO.tip(vertices);return localToWorld(p[0],p[1],p[2]);}
 private void updateParts(){
  float[] v=pose(0);int n=parts.length;double[][] lo=new double[n][3],hi=new double[n][3];
  for(int j=0;j<n;j++){Arrays.fill(lo[j],Double.POSITIVE_INFINITY);Arrays.fill(hi[j],Double.NEGATIVE_INFINITY);}
  for(int i=0;i<GEO.count;i++){int p=GEO.groupForSide(GEO.part[i],mirrored());for(int k=0;k<8;k++){int ix=i*24+k*3;Vec3 q=localToWorld(v[ix],v[ix+1],v[ix+2]);double[] xyz={q.x,q.y,q.z};for(int d=0;d<3;d++){lo[p][d]=Math.min(lo[p][d],xyz[d]);hi[p][d]=Math.max(hi[p][d],xyz[d]);}}}
  for(int i=0;i<n;i++){Part p=parts[i];double[] l=lo[i],h=hi[i];if(!Double.isFinite(l[0]))continue;p.setPos((l[0]+h[0])*.5,l[1],(l[2]+h[2])*.5);p.setBoundingBox(new AABB(l[0],l[1],l[2],h[0],h[1],h[2]).inflate(.035));}
 }
 /** A recorded aggressor may remain a valid target even when its native goal changes. */
 public boolean canPursue(LivingEntity e){return e!=null&&!(e instanceof SandKingEntity)&&e.isAlive()&&!e.isRemoved()&&e.level()==level()&&distanceToSqr(e)<=96*96&&(home==null||home.distanceToSqr(e.position())<=80*80)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.DIRECT);}
 public boolean validThreatTarget(LivingEntity e){return canPursue(e)&&(CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE)||BossThreat.recordedAttacker(this,e));}
 private boolean valid(LivingEntity e){return validThreatTarget(e);}
 public boolean acceptThreatTarget(LivingEntity e){
  if(action()!=IDLE||!valid(e))return false;
  if(getTarget()==e)return true;
  boolean wake=!valid(getTarget());
  setTarget(e);if(getTarget()!=e)return false;
  lastKnown=e.position();unseen=0;retreating=false;noTarget=0;
  if(wake)playSound(SandKingSounds.ALERT.get(),1.6F,1);
  return true;
 }
 @Override public boolean hurt(DamageSource s,float amount){return hurtPart(s,amount,1);}
 private boolean hurtPart(DamageSource s,float amount,float multiplier){
  return super.hurt(s,amount*multiplier*(s.is(DamageTypeTags.IS_PROJECTILE)?.6F:1F));
 }
 @Override public void tick(){
  super.tick();if(!isAlive())return;
  if(level() instanceof ServerLevel w){if(home==null)home=position();int p=phase(getHealth()/getMaxHealth());if(p!=battlePhase()){entityData.set(PHASE,p);playSound(SandKingSounds.RAGE.get(),1.6F,p==3?.8F:1F);}
   bar.setProgress(getHealth()/getMaxHealth());bar.setColor(p==3?BossEvent.BossBarColor.RED:p==2?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.YELLOW);
   for(int i=0;i<cooldowns.length;i++)if(cooldowns[i]>0)cooldowns[i]--;if(recovery>0)recovery--;updatePoison(w);if(action()==IDLE)think(w);else execute(w);
  }updateParts();
 }
 private void think(ServerLevel w){
  LivingEntity target=getTarget();
  if(!valid(target)||distanceToSqr(target)>96*96||home.distanceToSqr(target.position())>80*80){setTarget(null);target=null;}
  if(!retreating)BossThreat.selectSandKing(this);target=getTarget();
  if(!valid(target)&&tickCount%10==0&&!retreating){double best=24*24;Player chosen=null;for(Player p:w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(24),this::valid))if(distanceToSqr(p)<best&&hasLineOfSight(p)){chosen=p;best=distanceToSqr(p);}if(chosen!=null){setTarget(chosen);target=chosen;playSound(SandKingSounds.ALERT.get(),1.5F,1);}}
  if(!valid(target)){if(++noTarget>100){retreating=true;if(position().distanceToSqr(home)>4){if(tickCount%10==0)getNavigation().moveTo(home.x,home.y,home.z,1);}else{getNavigation().stop();retreating=false;noTarget=0;}}return;}
  noTarget=0;retreating=false;if(hasLineOfSight(target)){lastKnown=target.position();unseen=0;}else unseen++;
  if(unseen>160){setTarget(null);getNavigation().stop();return;}
  Vec3 point=lastKnown==null?target.position():lastKnown;
  if(position().distanceToSqr(home)>80*80){setTarget(null);retreating=true;return;}
  double dist=position().distanceTo(target.position());turnTo(point);boolean sight=hasLineOfSight(target);
  if(recovery==0&&sight){int near=0;for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(9),this::attackable))if(position().distanceToSqr(e.position())<=81)near++;
   if(battlePhase()==3&&dist<=10&&ready(SLAM)&&(near>=2||random.nextInt(5)==0)){begin(SLAM,target);return;}
   if(dist>6.5&&dist<=16&&ready(JUMP)){begin(JUMP,target);return;}
   if(dist<=6&&battlePhase()>=2&&ready(COMBO)&&(near>1||random.nextInt(4)==0)){begin(COMBO,target);return;}
   if(dist<=4.4&&ready(STING)){begin(STING,target);return;}
   if(dist<=6&&ready(CLAW)){begin(CLAW,target);return;}
  }
  var speed=getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null)speed.setBaseValue(speed(battlePhase()));if(recovery>0&&dist<=5){getNavigation().stop();return;}if(tickCount%8==0)getNavigation().moveTo(point.x,point.y,point.z,recovery>0?.65:1);
 }
 private boolean ready(int a){return cooldowns[a]==0&&canUse(a,battlePhase());}
 private void turnTo(Vec3 p){Vec3 d=p.subtract(position());if(d.horizontalDistanceSqr()<.001)return;float goal=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);float yaw=getYRot()+Mth.clamp(Mth.wrapDegrees(goal-getYRot()),-18,18);setYRot(yaw);setYHeadRot(yaw);setYBodyRot(yaw);}
 private void begin(int a,LivingEntity t){
  Vec3 d=t.position().subtract(position());float yaw=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);Vec3 f=forward();boolean left=f.x*d.z-f.z*d.x>0;
  entityData.set(MIRROR,a==CLAW&&left);setYRot(yaw);setYBodyRot(yaw);setYHeadRot(yaw);lockedYaw=yaw;entityData.set(ACTION,a);entityData.set(START,(int)level().getGameTime());entityData.set(LANDED,false);
  lockedPoint=t.position();cooldowns[a]=cooldown(a,battlePhase());getNavigation().stop();previousTip=tip(pose(0));stingHit=false;flightStart=-1;landingTick=-1;shockHits.clear();shockCenter=null;
  if(a==SLAM)playSound(SandKingSounds.SLAM_CHARGE.get(),1.5F,1);if(a==STING)playSound(SandKingSounds.STING.get(),1.3F,1);if(a==JUMP)playSound(SandKingSounds.JUMP.get(),1.4F,1);
 }
 private void execute(ServerLevel w){
  int a=action(),t=actionTicks();getNavigation().stop();setYRot(lockedYaw);setYBodyRot(lockedYaw);setYHeadRot(lockedYaw);
  if(a!=JUMP&&a!=COMBO)setDeltaMovement(0,getDeltaMovement().y,0);
  if(a==STING){if(t>=7&&t<=22&&!stingHit)sting(w);else previousTip=tip(pose(0));}
  if(a==CLAW&&t==12)claw(w,mirrored()?-1:1,12,1.0);
  if(a==COMBO){if(t<48&&onGround())setDeltaMovement(forward().scale(.08).add(0,getDeltaMovement().y,0));for(int i=0;i<COMBO_TICKS.length;i++)if(t==COMBO_TICKS[i])claw(w,i%2==0?1:-1,i==5?10:5,i==5?1.15:.12);}
  if(a==JUMP){jump(w,t);if(landingTick>=0&&t>=landingTick+14){finish();return;}if(t>=90){finish();return;}return;}
  if(a==SLAM&&t>=50){if(shockCenter==null){shockCenter=position();playSound(SandKingSounds.SLAM.get(),2.5F,.9F);w.sendParticles(ParticleTypes.EXPLOSION,getX(),getY()+.3,getZ(),1,0,0,0,0);}shockwave(w,Math.min(12,(t-49)*.6));}
  if(t>=duration(a))finish();
 }
 private void finish(){int a=action();recovery=recovery(a);entityData.set(ACTION,IDLE);entityData.set(MIRROR,false);entityData.set(LANDED,false);previousTip=null;lockedPoint=null;shockCenter=null;setDeltaMovement(0,getDeltaMovement().y,0);}
 private boolean attackable(LivingEntity e){return !(e instanceof SandKingEntity)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.AREA);}
 private void sting(ServerLevel w){
  Vec3 end=tip(pose(0)),start=previousTip==null?end:previousTip;previousTip=end;BlockHitResult block=w.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
  double best=block.getType()==HitResult.Type.MISS?Double.POSITIVE_INFINITY:start.distanceToSqr(block.getLocation());LivingEntity victim=null;
  for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(.4),this::attackable)){var box=e.getBoundingBox().inflate(.2);var hit=box.clip(start,end);double distance=box.contains(start)?0:hit.map(start::distanceToSqr).orElse(Double.POSITIVE_INFINITY);if(distance<best){best=distance;victim=e;}}
  if(victim!=null){if(victim.hurt(damageSources().mobAttack(this),16))victim.addEffect(new MobEffectInstance(MobEffects.POISON,120,1),this);stingHit=true;playSound(SandKingSounds.CLAW.get(),1,1.3F);}
  else if(block.getType()!=HitResult.Type.MISS){poisonAt(w,block.getLocation());stingHit=true;}
 }
 private void poisonAt(ServerLevel w,Vec3 pos){if(poison.size()>=12)poison.removeFirst();poison.add(new PoisonPatch(pos,w.getGameTime()+160));playSound(SandKingSounds.POISON.get(),1.2F,1);}
 private void updatePoison(ServerLevel w){if(tickCount%5!=0)return;long now=w.getGameTime();poison.removeIf(p->p.expires<=now);for(var p:poison){for(int i=0;i<12;i++){double a=random.nextDouble()*Math.PI*2,r=3*Math.sqrt(random.nextDouble());w.sendParticles(new DustParticleOptions(new Vector3f(.29F,.66F,.10F),1.2F),p.at.x+Math.cos(a)*r,p.at.y+.2+random.nextDouble()*.7,p.at.z+Math.sin(a)*r,1,0,0,0,0);}for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(p.at,p.at).inflate(3),this::attackable))if(e.position().distanceToSqr(p.at)<=9)e.addEffect(new MobEffectInstance(MobEffects.POISON,60,0),this);}}
 private void claw(ServerLevel w,int side,float damage,double knock){
  updateParts();Part hit=null;String key=side==1?"claw1":"claw-1";for(Part p:parts)if(GEO.groups[p.index].equals(key)){hit=p;break;}if(hit==null)return;
  AABB box=hit.getBoundingBox().inflate(.35);Vec3 origin=hit.position();playSound(SandKingSounds.CLAW.get(),1.4F,.88F+random.nextFloat()*.24F);for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,box,this::attackable))if(hasLineOfSight(e))damage(e,damage,knock,origin,0,action()==COMBO);
 }
 private void jump(ServerLevel w,int t){
  if(t<10){setDeltaMovement(0,getDeltaMovement().y,0);return;}
  if(t==10){Vec3 diff=lockedPoint.subtract(position()).multiply(1,0,1);jumpDirection=diff.lengthSqr()<.01?forward():diff.normalize();double speed=Math.min(16,diff.length())/21;flightStart=t;setDeltaMovement(jumpDirection.scale(speed).add(0,.93,0));hasImpulse=true;return;}
  if(flightStart<0||landingTick>=0)return;
  if(onGround()&&t>flightStart+3){landingTick=t;entityData.set(LANDED,true);setDeltaMovement(0,0,0);playSound(SandKingSounds.LAND.get(),2,1);SandKingQuicksand.create(w,position());area(w,position(),4,20,1.0,.35);dust(w,position(),4,65);return;}
  double remaining=lockedPoint.subtract(position()).dot(jumpDirection);Vec3 motion=getDeltaMovement();double horizontal=horizontalCollision?0:Math.min(Math.max(0,remaining),.8);setDeltaMovement(jumpDirection.scale(horizontal).add(0,motion.y,0));
 }
 private void area(ServerLevel w,Vec3 center,double radius,float amount,double knock,double lift){for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius,3,radius),this::attackable))if(e.position().subtract(center).horizontalDistanceSqr()<=radius*radius&&hasLineOfSight(e))damage(e,amount,knock,center,lift,false);}
 private void shockwave(ServerLevel w,double radius){if(shockCenter==null)return;for(int i=0;i<32;i++){double a=i*Math.PI/16;Vec3 p=shockCenter.add(Math.cos(a)*radius,.22,Math.sin(a)*radius);w.sendParticles(ParticleTypes.POOF,p.x,p.y,p.z,1,.15,.16,.15,.03);}for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(shockCenter,shockCenter).inflate(radius,3,radius),this::attackable))if(e.position().subtract(shockCenter).horizontalDistanceSqr()<=radius*radius&&hasLineOfSight(e)&&shockHits.add(e.getUUID()))damage(e,28,1.6,shockCenter,.8,false);}
 private void damage(LivingEntity e,float amount,double knock,Vec3 from,double lift,boolean rapid){DamageSource source=rapid?SandKingDamage.combo(this):damageSources().mobAttack(this);if(!e.hurt(source,amount))return;Vec3 d=e.position().subtract(from).multiply(1,0,1);if(d.lengthSqr()<.001)d=forward();e.knockback(knock,-d.x,-d.z);if(lift>0){e.push(0,lift,0);e.hurtMarked=true;}}
 private void dust(ServerLevel w,Vec3 center,double radius,int n){w.sendParticles(ParticleTypes.POOF,center.x,center.y+.3,center.z,n,radius*.55,.5,radius*.55,.04);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(home!=null){n.putDouble("HomeX",home.x);n.putDouble("HomeY",home.y);n.putDouble("HomeZ",home.z);}n.putIntArray("SandKingCooldowns",cooldowns);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.contains("HomeX"))home=new Vec3(n.getDouble("HomeX"),n.getDouble("HomeY"),n.getDouble("HomeZ"));int[] saved=n.getIntArray("SandKingCooldowns");for(int i=0;i<Math.min(saved.length,cooldowns.length);i++)cooldowns[i]=Math.max(0,saved[i]);entityData.set(ACTION,IDLE);}
}
