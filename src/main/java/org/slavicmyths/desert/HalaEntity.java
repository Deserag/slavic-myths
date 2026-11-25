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
import static org.slavicmyths.desert.HalaRules.*;

public final class HalaEntity extends Monster implements LivingThreatBoss {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(HalaEntity.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(HalaEntity.class,EntityDataSerializers.INT);
 private static final HalaGeometry GEO=HalaGeometry.INSTANCE;
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.hala","Хала"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 private final Part[] parts=new Part[GEO.groups.length];
 private final int[] cd=new int[12];
 private final Map<UUID,Integer> exposure=new HashMap<>();
 private final Set<UUID> waveHits=new HashSet<>();
 private final List<Shot> shots=new ArrayList<>();
 private final List<HeatPatch> patches=new ArrayList<>();
 private record HeatPatch(Vec3 center,long until){}
 private Vec3 home,lockedPoint,lockedDirection;
 private float lockedYaw;
 private int recovery,sequence,lastExecuted=-1,flightStart=-1,lockedPhase;
 private boolean landed;
 private static final class Shot {Vec3 pos,velocity;final double radius;final float damage;final int heat;int life;Shot(Vec3 p,Vec3 v,double r,float d,int h,int l){pos=p;velocity=v;radius=r;damage=d;heat=h;life=l;}}
 public HalaEntity(EntityType<? extends HalaEntity> type,Level world){super(type,world);xpReward=180;setPersistenceRequired();for(int i=0;i<parts.length;i++)parts[i]=new Part(this,i);}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,HP).add(Attributes.ATTACK_DAMAGE,14).add(Attributes.ARMOR,16).add(Attributes.ARMOR_TOUGHNESS,5).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,64).add(Attributes.MOVEMENT_SPEED,.23);}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(START,0);}
 public int action(){return entityData.get(ACTION);}
 void action(int a){entityData.set(ACTION,a);entityData.set(START,(int)level().getGameTime());lastExecuted=-1;}
 public int age(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
 public int battlePhase(){return phase(getHealth()/getMaxHealth());}
 public int visualAction(){if(isDeadOrDying())return DEATH;if(action()!=IDLE)return action();if(hurtTime>0)return HURT;return getDeltaMovement().horizontalDistanceSqr()>.0007?WALK:IDLE;}
 public float visualProgress(float partial){int a=visualAction();if(a==DEATH)return Mth.clamp((deathTime+partial)/20F,0,1);if(a==HURT)return Mth.clamp((10-hurtTime+partial)/10F,0,1);if(a==WALK)return ((tickCount+partial)%LENGTH[WALK])/LENGTH[WALK];return a==IDLE?0:Mth.clamp((age()+partial)/LENGTH[a],0,1);}
 public float[] pose(float partial){
  int a=visualAction();float p=visualProgress(partial);float[] v=GEO.sample(a,p);
  // V2 contains root elevation during the two airborne poses. Move the actual entity instead of doubling it.
  if(a==DIVE||a==ULTIMATE){float dy=(float)(23*Math.sin(Math.PI*p)/14);for(int i=1;i<v.length;i+=3)v[i]-=dy;}
  return v;
 }
 @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback("entity.slavicmyths.hala","Хала");}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PartEntity<?>[] getParts(){return parts;}
 @Override public boolean isPickable(){return false;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected SoundEvent getAmbientSound(){return HalaSounds.get("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource s){return HalaSounds.get("hurt");}
 @Override protected SoundEvent getDeathSound(){return HalaSounds.get("death");}
 @Override protected void playStepSound(BlockPos p,BlockState s){playSound(HalaSounds.get("step"),.85F,.8F+random.nextFloat()*.2F);}
 @Override public boolean canBeAffected(MobEffectInstance e){return !e.getEffect().equals(org.slavicmyths.registry.ModEffects.SOLAR_BURN)&&super.canBeAffected(e);}
 public static final class Part extends PartEntity<HalaEntity>{
  final int index;Part(HalaEntity p,int i){super(p);index=i;noPhysics=true;refreshDimensions();}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override protected void addAdditionalSaveData(CompoundTag n){}
  @Override protected void readAdditionalSaveData(CompoundTag n){}
  @Override public EntityDimensions getDimensions(Pose p){return EntityDimensions.scalable(1,1);}
  @Override public boolean shouldBeSaved(){return false;}
  @Override public boolean isPickable(){return getParent().isAlive();}
  @Override public boolean is(Entity e){return e==this||e==getParent();}
  @Override public boolean hurt(DamageSource s,float a){String g=GEO.groups[index];return getParent().hurt(s,a*(g.equals("head")?1.15F:g.startsWith("wing")?.75F:g.equals("staff")?.5F:1));}
 }
 private Vec3 world(double x,double y,double z){double a=Math.toRadians(getYRot());return position().add(x*Math.cos(a)-z*Math.sin(a),y,x*Math.sin(a)+z*Math.cos(a));}
 private Vec3 staff(){double[] p=GEO.tip(pose(0));return world(p[0],p[1],p[2]);}
 private void updateParts(){
  if(parts==null)return;float[] v=pose(0);int n=parts.length;double[][] lo=new double[n][3],hi=new double[n][3];
  for(int i=0;i<n;i++){Arrays.fill(lo[i],Double.POSITIVE_INFINITY);Arrays.fill(hi[i],Double.NEGATIVE_INFINITY);}
  double a=Math.toRadians(getYRot()),c=Math.cos(a),s=Math.sin(a);
  for(int i=0;i<GEO.count;i++){int g=GEO.part[i];for(int k=0;k<8;k++){int p=i*24+k*3;double x=getX()+v[p]*c-v[p+2]*s,y=getY()+v[p+1],z=getZ()+v[p]*s+v[p+2]*c;lo[g][0]=Math.min(lo[g][0],x);lo[g][1]=Math.min(lo[g][1],y);lo[g][2]=Math.min(lo[g][2],z);hi[g][0]=Math.max(hi[g][0],x);hi[g][1]=Math.max(hi[g][1],y);hi[g][2]=Math.max(hi[g][2],z);}}
  for(int i=0;i<n;i++){double[] l=lo[i],h=hi[i];if(!Double.isFinite(l[0]))continue;parts[i].setPos((l[0]+h[0])*.5,l[1],(l[2]+h[2])*.5);parts[i].setBoundingBox(new AABB(l[0],l[1],l[2],h[0],h[1],h[2]).inflate(.025));}
 }
 public boolean canPursue(LivingEntity e){return e!=null&&!(e instanceof HalaEntity)&&e.isAlive()&&e.level()==level()&&distanceToSqr(e)<=96*96&&(home==null||home.distanceToSqr(e.position())<=80*80)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.DIRECT);}
 public boolean validThreatTarget(LivingEntity e){return canPursue(e)&&(e==getTarget()||CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE)||BossThreat.recordedAttacker(this,e));}
 public boolean acceptThreatTarget(LivingEntity e){if(threatAttackLocked()||!validThreatTarget(e))return false;setTarget(e);return getTarget()==e;}
 public boolean threatAttackLocked(){return action()!=IDLE;}
 @Override public void setTarget(LivingEntity e){if(entityData!=null&&action()!=IDLE&&e!=getTarget())return;super.setTarget(e);}
 private boolean hostile(LivingEntity e){return canPursue(e)&&(e==getTarget()||BossThreat.recordedAttacker(this,e)||CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE));}
 @Override public void tick(){
  super.tick();if(!isAlive()){updateParts();return;}
  if(level() instanceof ServerLevel w){
   if(home==null)home=position();bar.setProgress(getHealth()/getMaxHealth());bar.setColor(BossEvent.BossBarColor.RED);
   for(int i=0;i<cd.length;i++)if(cd[i]>0)cd[i]--;if(recovery>0)recovery--;
   updateShots(w);if(tickCount%20==0)updatePatches(w);if(action()==IDLE)think(w);else execute(w);
   if(tickCount%5==0)aura(w);
  }updateParts();
 }
 private void turn(Vec3 point){Vec3 d=point.subtract(position());if(d.horizontalDistanceSqr()<.001)return;float yaw=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);setYRot(yaw);setYBodyRot(yaw);setYHeadRot(yaw);}
 private void think(ServerLevel w){
  if(home.distanceToSqr(position())>80*80){setTarget(null);if(tickCount%10==0)getNavigation().moveTo(home.x,home.y,home.z,1);return;}
  BossThreat.selectLivingBoss(this);LivingEntity t=getTarget();
  if(!validThreatTarget(t)){setTarget(null);if(tickCount%10==0){t=w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(32),p->validThreatTarget(p)&&hasLineOfSight(p)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);if(t!=null)setTarget(t);}}
  t=getTarget();if(!validThreatTarget(t)){if(tickCount%10==0&&home.distanceToSqr(position())>4)getNavigation().moveTo(home.x,home.y,home.z,1);return;}
  double dist=distanceTo(t);turn(t.position());boolean sight=hasLineOfSight(t);int phase=battlePhase();
  if(recovery==0&&sight){
   int a=IDLE;
   if(dist<=5&&cd[CLAW]==0)a=CLAW;
   else if(dist<=12&&cd[FLASH]==0)a=FLASH;
   else if(phase==3&&dist<=30&&cd[ULTIMATE]==0&&sequence>=3&&sequence%5==0)a=ULTIMATE;
   else if(phase>=2&&dist>=7&&dist<=12&&cd[DIVE]==0&&sequence>=4&&sequence%6==0&&onGround())a=DIVE;
   else {int[] spells={VOLLEY,BEAM,PILLARS,ORB};for(int i=0;i<spells.length;i++){int k=spells[(sequence+i)%spells.length];if(cd[k]==0&&dist<= (k==BEAM?40:32)){a=k;break;}}}
   if(a!=IDLE){begin(a,t);return;}
  }
  if(!sight||dist>26){if(tickCount%8==0)getNavigation().moveTo(t,1);}
  else getNavigation().stop();
 }
 void begin(int a,LivingEntity target){
  turn(target.position());lockedYaw=getYRot();lockedPoint=target.position();lockedPhase=battlePhase();
  double[] tip=GEO.tip(GEO.sample(a,(float)RELEASE[a]/LENGTH[a]));double y=tip[1];if(a==DIVE||a==ULTIMATE)y-=23*Math.sin(Math.PI*RELEASE[a]/LENGTH[a])/14;
  lockedDirection=target.getEyePosition().subtract(world(tip[0],y,tip[2])).normalize();sequence++;
  cd[a]=COOLDOWN[a];action(a);waveHits.clear();flightStart=-1;landed=false;getNavigation().stop();playSound(HalaSounds.get("charge"),1.4F,a==ULTIMATE?.7F:1F);
 }
 private Vec3 forward(){return Vec3.directionFromRotation(0,lockedYaw);}
 private boolean reached(int t){return lastExecuted<t&&age()>=t;}
 private void execute(ServerLevel w){
  int a=action(),t=age();getNavigation().stop();setYRot(lockedYaw);setYBodyRot(lockedYaw);setYHeadRot(lockedYaw);
  if(lockedPoint==null||lockedDirection==null){finish();return;}
  if(a!=DIVE||flightStart<0||landed)setDeltaMovement(0,getDeltaMovement().y,0);
  int release=RELEASE[a];
  if(t<release&&t%4==0){Vec3 p=staff();w.sendParticles(ParticleTypes.FLAME,p.x,p.y,p.z,12,.35,.35,.35,.015);if(a==BEAM)ray(w,p,p.add(lockedDirection.scale(40)),false);}
  if(a==PILLARS&&t<release&&t%4==0)for(Vec3 p:marks())ring(w,p,3);
  if(a==ULTIMATE&&t<release&&t%4==0){ring(w,lockedPoint,8);Vec3 sun=ultimateOrigin(w);w.sendParticles(ParticleTypes.FLAME,sun.x,sun.y,sun.z,24,1.2,1.2,1.2,.02);}
  if(a==ULTIMATE&&reached(release-25)&&onGround()){setDeltaMovement(0,.7,0);hasImpulse=true;}
  if(a==DIVE&&t<release&&t%4==0)ring(w,lockedPoint,5);
  if(a==FLASH&&t<release&&t%4==0)ring(w,position(),12);
  if(reached(release)){playSound(HalaSounds.get(switch(a){case BEAM->"beam";case VOLLEY->"volley";case ORB->"orb";case PILLARS->"pillars";case FLASH->"flash";case DIVE->"dive";case CLAW->"claw";default->"ultimate";}),1.7F,1);
   switch(a){
    case ORB->launch(w,staff(),lockedPoint.add(0,1,0),.32,6,22,34,100);
    case PILLARS->{for(Vec3 p:marks())pillar(w,p);}
    case CLAW->{updateParts();for(Part p:parts)if(GEO.groups[p.index].equals("arm_L"))for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(.6),this::hostile))if(hasLineOfSight(e)&&waveHits.add(e.getUUID())&&e.hurt(damageSources().mobAttack(this),16))SolarBurnEffect.apply(e,10);}
    case DIVE->{if(onGround()){flightStart=t;Vec3 d=lockedPoint.subtract(position()).multiply(1,0,1);Vec3 dir=d.lengthSqr()<.01?forward():d.normalize();setDeltaMovement(dir.scale(Math.min(.65,d.length()/18)).add(0,.78,0));hasImpulse=true;}else{landed=true;land(w);}}
    case ULTIMATE->{Vec3 p=ultimateOrigin(w);launch(w,p,lockedPoint,.55,8,32,42,60);}
    default->{}
   }
  }
  if(a==BEAM&&t>=release&&t<=release+45&&t%10==release%10){Vec3 p=staff();ray(w,p,p.add(lockedDirection.scale(40)),true);}
  if(a==VOLLEY)for(int i=0;i<5;i++){int when=release+i*5;if(reached(when)){Vec3 p=staff(),side=new Vec3(-lockedDirection.z,0,lockedDirection.x).normalize();Vec3 dir=lockedDirection.add(side.scale((i-2)*.075)).normalize();launch(w,p,p.add(dir.scale(32)),.7,2.1,12,12,55);}}
  if(a==FLASH&&t>=release&&t<=release+22){double radius=Math.min(12,(t-release+1)*.6);ring(w,position(),radius);area(w,position(),radius,16,24,1.4,true);}
  if(a==DIVE&&flightStart>=0&&!landed&&t>flightStart+3){
   if(onGround()||t>flightStart+35){landed=true;land(w);}
   else{Vec3 d=lockedPoint.subtract(position()).multiply(1,0,1);Vec3 v=getDeltaMovement();double speed=horizontalCollision?0:Math.min(.65,d.length());setDeltaMovement(d.normalize().scale(speed).add(0,v.y,0));}
  }
  lastExecuted=t;if(t>=LENGTH[a])finish();
 }
 private void finish(){action(IDLE);recovery=18;lockedPoint=null;lockedDirection=null;setDeltaMovement(0,getDeltaMovement().y,0);}
 private List<Vec3> marks(){Vec3 side=new Vec3(-lockedDirection.z,0,lockedDirection.x).normalize(),front=forward();return lockedPhase==3?List.of(lockedPoint,lockedPoint.add(side.scale(5)),lockedPoint.add(side.scale(-5)),lockedPoint.add(front.scale(5)),lockedPoint.add(front.scale(-5))):List.of(lockedPoint,lockedPoint.add(side.scale(5)),lockedPoint.add(side.scale(-5)));}
 private boolean visible(ServerLevel w,Vec3 a,Vec3 b){return w.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
 private void ray(ServerLevel w,Vec3 a,Vec3 b,boolean hit){
  var block=w.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));Vec3 end=block.getType()==HitResult.Type.MISS?b:block.getLocation();Vec3 delta=end.subtract(a);int steps=Math.max(1,(int)(delta.length()*(hit?2:1)));
  for(int i=0;i<=steps;i++){Vec3 p=a.add(delta.scale((double)i/steps));w.sendParticles(hit?ParticleTypes.FLAME:ParticleTypes.END_ROD,p.x,p.y,p.z,hit?2:1,hit?.10:0,hit?.10:0,hit?.10:0,0);}
  if(hit)for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(a,end).inflate(.7),this::hostile)){AABB box=e.getBoundingBox().inflate(.45);if(box.contains(a)||box.clip(a,end).isPresent())solar(e,10,14);}
 }
 private void launch(ServerLevel w,Vec3 start,Vec3 point,double speed,double radius,float damage,int heat,int life){
  Vec3 d=point.subtract(start);if(d.lengthSqr()<.001)d=forward();if(shots.size()>=16)shots.removeFirst();shots.add(new Shot(start,d.normalize().scale(speed),radius,damage,heat,life));
  w.sendParticles(ParticleTypes.FLAME,start.x,start.y,start.z,20,.45,.45,.45,.02);
 }
 private void updateShots(ServerLevel w){
  for(var it=shots.iterator();it.hasNext();){Shot shot=it.next();Vec3 start=shot.pos,end=start.add(shot.velocity);
   if(!w.isLoaded(BlockPos.containing(end))){it.remove();continue;}
   var block=w.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));double best=block.getType()==HitResult.Type.MISS?Double.POSITIVE_INFINITY:start.distanceToSqr(block.getLocation());Vec3 impact=block.getType()==HitResult.Type.MISS?null:block.getLocation();
   for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(.5),this::hostile)){AABB box=e.getBoundingBox().inflate(.4);var hit=box.clip(start,end);double d=box.contains(start)?0:hit.map(start::distanceToSqr).orElse(Double.POSITIVE_INFINITY);if(d<best){best=d;impact=box.contains(start)?start:hit.orElse(start);}}
   if(impact!=null||--shot.life<=0){Vec3 p=impact==null?end:impact;w.sendParticles(ParticleTypes.EXPLOSION,p.x,p.y,p.z,1,0,0,0,0);
    if(shot.radius<=2.1){for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(shot.radius),this::hostile))if(e.getBoundingBox().getCenter().distanceToSqr(p)<=shot.radius*shot.radius&&visible(w,start,e.getBoundingBox().getCenter())&&solar(e,shot.damage,shot.heat))e.setRemainingFireTicks(Math.max(e.getRemainingFireTicks(),60));}
    else area(w,p,shot.radius,shot.damage,shot.heat,.45,false);it.remove();continue;}
   if(shot.radius>3&&shot.life%10==0)area(w,end,2.5,4,5,0,false);
   shot.pos=end;double spread=shot.radius>3?.75:.16;w.sendParticles(ParticleTypes.FLAME,end.x,end.y,end.z,shot.radius>3?16:5,spread,spread,spread,.01);w.sendParticles(ParticleTypes.END_ROD,end.x,end.y,end.z,2,spread,spread,spread,0);
  }
 }
 private Vec3 ultimateOrigin(ServerLevel w){
  Vec3 base=lockedPoint.add(0,.5,0),top=base.add(0,14,0);var hit=w.clip(new ClipContext(base,top,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
  if(hit.getType()==HitResult.Type.MISS)return top;
  Vec3 p=hit.getLocation().add(0,-.6,0);return p.y-base.y<2?staff():p;
 }
 private void pillar(ServerLevel w,Vec3 p){
  Vec3 ground=p.add(0,.15,0),top=ground.add(0,20,0);var hit=w.clip(new ClipContext(ground,top,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
  if(hit.getType()!=HitResult.Type.MISS)top=hit.getLocation();
  for(double y=ground.y;y<=top.y;y+=.5)w.sendParticles(ParticleTypes.END_ROD,p.x,y,p.z,4,.55,.2,.55,.01);
  for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(ground,top).inflate(3,.1,3),this::hostile)){
   Vec3 c=e.getBoundingBox().getCenter();if(c.y>=ground.y-.5&&c.y<=top.y&&c.subtract(p).horizontalDistanceSqr()<=9&&visible(w,new Vec3(p.x,c.y,p.z),c))solar(e,18,22);
  }
 }
 private void land(ServerLevel w){
  ring(w,position(),5);area(w,position(),5,24,32,1.4,false);
  if(patches.size()>=3)patches.removeFirst();patches.add(new HeatPatch(position(),w.getGameTime()+100));
 }
 private void updatePatches(ServerLevel w){
  patches.removeIf(p->p.until<=w.getGameTime());for(var p:patches){ring(w,p.center,5);area(w,p.center,5,3,5,0,false);}
 }
 private void ring(ServerLevel w,Vec3 p,double radius){for(int i=0;i<24;i++){double a=i*Math.PI/12;w.sendParticles(ParticleTypes.FLAME,p.x+radius*Math.cos(a),p.y+.22,p.z+radius*Math.sin(a),1,0,.04,0,0);}}
 private void area(ServerLevel w,Vec3 p,double radius,float damage,int heat,double knock,boolean wave){
  for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(radius,Math.max(3,radius*.7),radius),this::hostile)){
   Vec3 center=e.getBoundingBox().getCenter();if(center.distanceToSqr(p.add(0,.7,0))>radius*radius||!visible(w,p.add(0,.7,0),center)||(wave&&!waveHits.add(e.getUUID())))continue;
   if(solar(e,damage,heat)&&knock>0){Vec3 d=e.position().subtract(p);e.knockback(knock,-d.x,-d.z);if(wave)e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,50,0),this);}
  }
 }
 private boolean solar(LivingEntity e,float amount,int heat){if(e.hasEffect(MobEffects.FIRE_RESISTANCE))amount*=.65F;amount*=HalaRules.vulnerability(SolarBurnEffect.stage(e));if(!e.hurt(MythDamageSources.caused("solar_magic",this),amount))return false;SolarBurnEffect.apply(e,heat);return true;}
 private void aura(ServerLevel w){
  if(!validThreatTarget(getTarget())){exposure.clear();return;}
  Set<UUID> near=new HashSet<>();for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(AURA_RADIUS),this::hostile)){
   if(distanceToSqr(e)>AURA_RADIUS*AURA_RADIUS||!hasLineOfSight(e))continue;UUID id=e.getUUID();near.add(id);int t=exposure.compute(id,(key,old)->old==null?0:Math.min(1000000,old+5));
   if(t>=100&&t%20==0)w.sendParticles(ParticleTypes.FLAME,e.getX(),e.getY()+1,e.getZ(),5,.25,.35,.25,.01);
   if(t>=200&&(t-200)%20==0)solar(e,2+battlePhase(),6+battlePhase()*2);
  }exposure.keySet().retainAll(near);
 }
 @Override public void die(DamageSource s){super.die(s);bar.removeAllPlayers();shots.clear();exposure.clear();patches.clear();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putIntArray("HalaCooldowns",cd);n.putInt("HalaSequence",sequence);if(home!=null){n.putDouble("HalaHomeX",home.x);n.putDouble("HalaHomeY",home.y);n.putDouble("HalaHomeZ",home.z);}}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);int[] old=n.getIntArray("HalaCooldowns");for(int i=0;i<Math.min(old.length,cd.length);i++)cd[i]=Math.max(0,old[i]);sequence=n.getInt("HalaSequence");if(n.contains("HalaHomeX"))home=new Vec3(n.getDouble("HalaHomeX"),n.getDouble("HalaHomeY"),n.getDouble("HalaHomeZ"));action(IDLE);}
}
