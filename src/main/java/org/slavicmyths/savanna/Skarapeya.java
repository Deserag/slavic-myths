package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import org.slavicmyths.combat.*;
import org.slavicmyths.registry.ModEffects;
import static org.slavicmyths.savanna.SkarapeyaRules.*;

public final class Skarapeya extends Monster {
 private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(Skarapeya.class,EntityDataSerializers.INT),ACTION=SynchedEntityData.defineId(Skarapeya.class,EntityDataSerializers.INT),AGE=SynchedEntityData.defineId(Skarapeya.class,EntityDataSerializers.INT),FIRST=SynchedEntityData.defineId(Skarapeya.class,EntityDataSerializers.INT),SECOND=SynchedEntityData.defineId(Skarapeya.class,EntityDataSerializers.INT);
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.skarapeya","Скарапея"),BossEvent.BossBarColor.WHITE,BossEvent.BossBarOverlay.PROGRESS);
 private final long[] ready=new long[LENGTH.length];
 private final List<Zone> zones=new ArrayList<>();
 private final List<Wave> waves=new ArrayList<>();
 private final List<Bolt> bolts=new ArrayList<>();
 private final Map<UUID,Long> exposed=new HashMap<>();
 private record Zone(Vec3 at,double radius,long start,long end,int a,int b,float burst){}
 private static final class Wave {final Vec3 origin,dir;final int a,b;final float damage;final double limit;final boolean radial;double r;final Set<UUID> hit=new HashSet<>();Wave(Vec3 p,Vec3 d,int a,int b,float dmg,double limit,boolean radial){origin=p;dir=d;this.a=a;this.b=b;damage=dmg;this.limit=limit;this.radial=radial;}}
 private record Bolt(Vec3 at,Vec3 velocity,int life,int a,int b,float damage){}
 private Vec3 aim=Vec3.ZERO,home,lastTrail;
 private UUID locked,grabbed;
 private float escapeDamage;
 private int recovery,melodyStep,sequence;
 private long nextMode,trailAt;
 public Skarapeya(EntityType<? extends Skarapeya> type,Level w){super(type,w);setPersistenceRequired();xpReward=400;}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,HP).add(Attributes.ATTACK_DAMAGE,30).add(Attributes.ARMOR,20).add(Attributes.ARMOR_TOUGHNESS,6).add(Attributes.KNOCKBACK_RESISTANCE,.95).add(Attributes.MOVEMENT_SPEED,.29).add(Attributes.FOLLOW_RANGE,64);}
 @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
 @Override public double getAttributeValue(net.minecraft.core.Holder<Attribute> a){return a.equals(Attributes.MAX_HEALTH)?CreatureHealthBalance.uncappedMaximum(getAttribute(a)):super.getAttributeValue(a);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PHASE,1);b.define(ACTION,0);b.define(AGE,0);b.define(FIRST,0);b.define(SECOND,-1);}
 public int phase(){return entityData.get(PHASE);}public int action(){return entityData.get(ACTION);}public int age(){return entityData.get(AGE);}public int first(){return entityData.get(FIRST);}public int second(){return entityData.get(SECOND);}
 public int weapon(){return phase()==1?0:phase()==3?3:split(action())?2:blade(action())?1:3;}
 @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback(getType().getDescriptionId(),"Скарапея");}
 @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor w,DifficultyInstance d,MobSpawnType reason,SpawnGroupData data){var result=super.finalizeSpawn(w,d,reason,data);setHealth(HP);return result;}
 @Override public boolean canBeAffected(MobEffectInstance e){var t=e.getEffect();return !t.equals(MobEffects.POISON)&&!t.equals(MobEffects.WITHER)&&!t.equals(MobEffects.MOVEMENT_SLOWDOWN)&&!t.equals(MobEffects.WEAKNESS)&&!t.equals(MobEffects.CONFUSION)&&!t.equals(MobEffects.BLINDNESS)&&!t.equals(ModEffects.SKARAPEYA_STONE)&&!t.equals(ModEffects.SKARAPEYA_BLOOD)&&super.canBeAffected(e);}
 private boolean valid(LivingEntity x){return x!=null&&x!=this&&x.level()==level()&&SkarapeyaPoison.valid(x)&&!isAlliedTo(x)&&canAttack(x);}
 @Override public boolean hurt(DamageSource s,float amount){float before=getHealth();boolean hit=super.hurt(s,amount);if(hit&&!level().isClientSide){if(s.getEntity() instanceof LivingEntity x&&valid(x))setTarget(x);if(grabbed!=null){escapeDamage+=Math.max(0,before-getHealth());if(escapeDamage>=60)grabbed=null;}}return hit;}
 private Vec3 front(){double y=Math.toRadians(getYRot());return new Vec3(-Math.sin(y),0,Math.cos(y));}
 private static Vec3 flat(Vec3 v){return v.multiply(1,0,1).normalize();}
 private static double horizontal(Vec3 a,Vec3 b){return Math.hypot(a.x-b.x,a.z-b.z);}
 private void face(Vec3 p){Vec3 d=p.subtract(position());float y=(float)Math.toDegrees(Math.atan2(-d.x,d.z));setYRot(y);yBodyRot=y;setYHeadRot(y);}
 private List<LivingEntity> targets(ServerLevel w){return w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(64),x->valid(x)&&(x instanceof Player||x==getTarget())&&distanceToSqr(x)<=4096);}
 private LivingEntity lookup(ServerLevel w,UUID id){return id!=null&&w.getEntity(id) instanceof LivingEntity x&&valid(x)?x:null;}
 private boolean near(LivingEntity x,Vec3 p,double r){return horizontal(x.position(),p)<=r&&Math.abs(x.getY()-p.y)<4;}
 private boolean hit(LivingEntity x,float damage,int a,int b,double knock){if(!valid(x)||!hasLineOfSight(x)||!x.hurt(damageSources().mobAttack(this),damage))return false;if(knock>0)x.knockback(knock,getX()-x.getX(),getZ()-x.getZ());poison(x,a,b);return true;}
 private void poison(LivingEntity x,int a,int b){if(a>=0)SkarapeyaPoison.apply(x,a);if(b>=0&&b!=a)SkarapeyaPoison.apply(x,b);}
 private void area(List<LivingEntity> xs,double r,double dot,float damage,int a,int b,double kb){for(var x:xs)if(near(x,position(),r)&&front().dot(flat(x.position().subtract(position())))>=dot)hit(x,damage,a,b,kb);}
 private void melody(ServerLevel w,int a,int step){if(a<0)return;int[][] notes={{12,16,19,16},{0,3,0,5},{12,12,7,12},{7,5,3,0},{11,18,10,17},{2,2,9,2},{0,7,0,7}};int n=notes[a][step%4];float pitch=(float)Math.pow(2,(n-12)/12D);w.playSound(null,blockPosition(),SoundEvents.NOTE_BLOCK_HARP.value(),SoundSource.HOSTILE,1.8F,pitch);}
 private void switchPoisons(ServerLevel w,boolean pair){int a=(first()+1+random.nextInt(6))%7,b=-1;if(pair){do{b=random.nextInt(7);}while(!compatible(a,b));}entityData.set(FIRST,a);entityData.set(SECOND,b);melodyStep=0;nextMode=w.getGameTime()+(phase()==3?120:240);}
 private void note(ServerLevel w){melody(w,first(),melodyStep);if(second()>=0)melody(w,second(),melodyStep);melodyStep++;}
 private void dust(ServerLevel w,Vec3 p,int a,int n){int c=COLOR[Math.clamp(a,0,6)];w.sendParticles(new DustParticleOptions(new Vector3f(((c>>16)&255)/255F,((c>>8)&255)/255F,(c&255)/255F),1.25F),p.x,p.y,p.z,n,.08,.08,.08,0);}
 private void ring(ServerLevel w,Vec3 p,double r,int a){for(int i=0;i<24;i++){double t=i*Math.PI/12;dust(w,p.add(Math.cos(t)*r,.16,Math.sin(t)*r),a,1);}}
 private void zone(ServerLevel w,Vec3 p,double r,int life,int a,int b,float burst){if(zones.size()>=90)zones.remove(0);long now=w.getGameTime();zones.add(new Zone(p,r,now+30,now+30+life,a,b,burst));ring(w,p,r,a);}
 private void tickZones(ServerLevel w,List<LivingEntity> xs){long now=w.getGameTime();zones.removeIf(z->now>=z.end||!w.hasChunkAt(BlockPos.containing(z.at)));exposed.entrySet().removeIf(e->e.getValue()<=now);for(var z:zones){if(now%10==0)ring(w,z.at,z.radius,z.a);if(now==z.start&&z.burst>0)for(var x:xs)if(near(x,z.at,z.radius))hit(x,z.burst,z.a,z.b,0);if(now>=z.start&&now%10==0)for(var x:xs)if(near(x,z.at,z.radius)&&!exposed.containsKey(x.getUUID())){poison(x,z.a,z.b);exposed.put(x.getUUID(),now+40);}}}
 private void wave(Vec3 p,Vec3 d,float damage,int a,int b,double range,boolean radial){if(waves.size()<48)waves.add(new Wave(p,d,a,b,damage,range,radial));}
 private void tickWaves(ServerLevel w,List<LivingEntity> xs){var it=waves.iterator();while(it.hasNext()){var q=it.next();double old=q.r;q.r+=.72;if(q.r>q.limit){it.remove();continue;}Vec3 center=q.origin.add(q.dir.scale(q.r));if(q.radial)ring(w,q.origin,q.r,q.a);else for(int i=-5;i<=5;i++)dust(w,center.add(q.dir.z*i*.9,.4,-q.dir.x*i*.9),q.a,1);for(var x:xs){if(Math.abs(x.getY()-q.origin.y)>2.2||q.hit.contains(x.getUUID()))continue;Vec3 d=x.position().subtract(q.origin);double along=d.dot(q.dir),perp=Math.abs(d.x*q.dir.z-d.z*q.dir.x);boolean inside=q.radial?Math.abs(horizontal(x.position(),q.origin)-q.r)<1.8&&x.getY()<q.origin.y+1.15:along>=old-1&&along<=q.r+1&&perp<5;if(inside&&hit(x,q.damage,q.a,q.b,.6))q.hit.add(x.getUUID());}}}
 private void bolt(Vec3 from,Vec3 toward,int a,int b,float damage){if(bolts.size()<48)bolts.add(new Bolt(from,toward.subtract(from).normalize().scale(1.25),45,a,b,damage));}
 private void tickBolts(ServerLevel w,List<LivingEntity> xs){var next=new ArrayList<Bolt>();for(var q:bolts){Vec3 end=q.at.add(q.velocity);if(!w.hasChunkAt(BlockPos.containing(end)))continue;var block=w.clip(new ClipContext(q.at,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));Vec3 limit=block.getType()==HitResult.Type.MISS?end:block.getLocation();LivingEntity victim=null;double best=Double.MAX_VALUE;for(var x:xs){var point=x.getBoundingBox().inflate(.35).clip(q.at,limit);if(point.isPresent()){double d=q.at.distanceToSqr(point.get());if(d<best){victim=x;best=d;}}}dust(w,limit,q.a,3);if(victim!=null){hit(victim,q.damage,q.a,q.b,0);zone(w,victim.position(),2.5,120,q.a,q.b,0);}else if(block.getType()!=HitResult.Type.MISS)zone(w,limit,2.5,120,q.a,q.b,0);else if(q.life>1)next.add(new Bolt(end,q.velocity,q.life-1,q.a,q.b,q.damage));}bolts.clear();bolts.addAll(next);}
 private void start(int a,LivingEntity t){entityData.set(ACTION,a);entityData.set(AGE,0);locked=t==null?null:t.getUUID();aim=t==null?position():t.position();if(t!=null)face(aim);getNavigation().stop();ready[a]=level().getGameTime()+CD[a];escapeDamage=0;}
 private void finish(){entityData.set(ACTION,IDLE);entityData.set(AGE,0);grabbed=null;locked=null;recovery=10;}
 private int choose(LivingEntity t){double d=horizontal(position(),t.position());int[] options=phase()==1?(d<8?new int[]{GRAB,COIL,TAIL,RING,BITE}:new int[]{CHARGE,SPIT}):phase()==3?new int[]{HOROVOD,DOUBLE,PULSE,GROUND,RAIN,GAZE,BLOOD}:d<7?new int[]{SLASH,SPIN,DUAL,THROW}:new int[]{BLADE_WAVE,THROW,HOROVOD,DOUBLE,PULSE,GROUND,RAIN,GAZE,BLOOD};if(phase()==3&&ready[ULTIMATE]<=level().getGameTime())return ULTIMATE;for(int i=0;i<options.length;i++){int a=options[Math.floorMod(sequence+i,options.length)];if(ready[a]<=level().getGameTime()){sequence++;return a;}}return IDLE;}
 private void retreat(LivingEntity t,double speed){Vec3 away=flat(position().subtract(t.position()));if(away.lengthSqr()<.001)away=front().scale(-1);Vec3 p=position().add(away.scale(7));if(home!=null&&horizontal(home,p)>48)p=home.add(flat(position().subtract(home)).scale(40));getNavigation().moveTo(p.x,p.y,p.z,speed);}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w)||!isAlive())return;if(w.getDifficulty()==Difficulty.PEACEFUL){discard();return;}if(home==null)home=position();if(horizontal(home,position())>64){finish();zones.clear();waves.clear();bolts.clear();getNavigation().moveTo(home.x,home.y,home.z,1.2);return;}
  int next=SkarapeyaRules.phase(getHealth());if(next>phase()){int old=phase();entityData.set(PHASE,next);finish();switchPoisons(w,next==3);if(old==1)start(TRANSFORM,getTarget());}
  bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));var viewers=w.getPlayers(p->p.isAlive()&&distanceToSqr(p)<4096);for(var p:List.copyOf(bar.getPlayers()))if(!viewers.contains(p))bar.removePlayer(p);for(var p:viewers)if(!bar.getPlayers().contains(p))bar.addPlayer(p);
  var xs=targets(w);tickZones(w,xs);tickWaves(w,xs);tickBolts(w,xs);LivingEntity t=getTarget();if(!valid(t)||distanceToSqr(t)>4096){t=xs.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(t);}
  if(phase()==1&&t!=null&&w.getGameTime()>=trailAt&&(lastTrail==null||horizontal(lastTrail,position())>=1.5)){lastTrail=position();trailAt=w.getGameTime()+10;zone(w,position(),1.5,160,0,-1,0);}
  if(phase()>1&&t!=null&&action()!=ULTIMATE){if(w.getGameTime()>=nextMode&&action()==IDLE)switchPoisons(w,phase()==3||random.nextBoolean());if((phase()==3||action()>=HOROVOD||blade(action())&&age()<20)&&tickCount%8==0)note(w);}
  if(action()!=IDLE){advance(w,lookup(w,locked),xs);return;}if(recovery>0)recovery--;if(t==null){getNavigation().stop();return;}face(t.position());if(recovery==0){int a=choose(t);if(a!=IDLE){start(a,t);return;}}
  double d=horizontal(position(),t.position());if(tickCount%8==0){if(phase()==1)getNavigation().moveTo(t,1.3);else if(d<(phase()==3?25:18))retreat(t,phase()==3?1.65:1.3);else if(d>(phase()==3?40:30))getNavigation().moveTo(t,1);else getNavigation().stop();}
 }
 private void advance(ServerLevel w,LivingEntity t,List<LivingEntity> xs){int a=action(),n=age()+1;entityData.set(AGE,n);if(a==TRANSFORM){if(n%8==0)ring(w,position(),4,0);if(n>=LENGTH[a])finish();return;}if(t==null){finish();return;}if(n<=12){aim=t.position();face(aim);}int p=phase()==1?0:first(),q=phase()==1?-1:second();if(phase()==3&&a!=ULTIMATE&&n%8==0)retreat(t,1.65);switch(a){
 case BITE->{if(n==18)area(xs,7,.2,30,0,-1,.3);}
 case GRAB,COIL->{if(n==22&&near(t,position(),7)&&hasLineOfSight(t)){grabbed=t.getUUID();escapeDamage=0;}if(grabbed!=null){var x=lookup(w,grabbed);if(x==null||!near(x,position(),9)){grabbed=null;break;}Vec3 at=position().add(front().scale(3));if(w.noCollision(x,x.getBoundingBox().move(at.subtract(x.position())))){x.teleportTo(at.x,at.y,at.z);x.setDeltaMovement(Vec3.ZERO);x.fallDistance=0;}else grabbed=null;if(grabbed!=null&&a==COIL&&n>=25&&n<=85&&(n-25)%20==0)hit(x,12,0,-1,0);if(grabbed!=null&&a==GRAB&&n==45){hit(x,42,0,-1,1.8);grabbed=null;}if(n==85)grabbed=null;}}
 case TAIL->{if(n==22)area(xs,10,-1,36,0,-1,1.8);}
 case CHARGE->{if(n>=16&&n<=32)getNavigation().moveTo(aim.x,aim.y,aim.z,1.8);if(n==33)area(xs,8,-.4,40,0,-1,1.3);}
 case RING->{if(n==10)ring(w,aim,9,0);if(n==55)for(var x:xs)if(near(x,aim,9)&&front().dot(flat(x.position().subtract(aim)))<.75)hit(x,44,0,-1,1);}
 case SPIT->{if(n==28){Vec3 from=position().add(front().scale(2)).add(0,4.5,0);for(int i=-2;i<=2;i++)bolt(from,aim.add(i*1.5,1,0),0,-1,22);}}
 case SLASH->{if(n==24)area(xs,8,.1,34,p,q,1.1);}
 case SPIN->{if(n==24||n==44)area(xs,10,-1,24,p,q,1.2);}
 case BLADE_WAVE->{if(n==30)wave(position(),front(),42,p,q,35,false);}
 case DUAL->{if(n==22)area(xs,8,0,23,p,-1,.3);if(n==38)area(xs,8,0,23,q<0?p:q,-1,.5);}
 case THROW->{if(n==25||n==46)bolt(position().add(0,4,0),aim.add(0,1,0),n==25||q<0?p:q,-1,28);}
 case HOROVOD->{if(n>=40&&n<=180&&n%20==0){double r=6+(n-40)*.14;for(int i=0;i<6;i++){double th=i*Math.PI/3+n*.035;Vec3 at=position().add(r*Math.cos(th),0,r*Math.sin(th));zone(w,at,2.2,140,i%2==0||q<0?p:q,-1,0);for(var x:xs)if(near(x,at,2.2))hit(x,30,p,q,0);}}}
 case DOUBLE->{if(n>=40&&n<=115&&(n-40)%15==0)wave(position(),front(),24,((n-40)/15)%2==0||q<0?p:q,-1,35,false);}
 case PULSE->{if(n==35)wave(position(),front(),42,p,q,30,false);}
 case GROUND->{if(n==40)for(int i=0;i<7;i++){double th=i*2*Math.PI/7;zone(w,aim.add(Math.cos(th)*9,0,Math.sin(th)*9),4,phase()==3?360:280,(p+i)%7,-1,28);}}
 case RAIN->{if(n>=40&&n<148&&(n-40)%6==0){double th=random.nextDouble()*2*Math.PI,r=2+random.nextDouble()*18;zone(w,aim.add(Math.cos(th)*r,0,Math.sin(th)*r),2.3,120,1,-1,20);}}
 case GAZE->{if(n==42)area(xs,25,.75,28,5,-1,0);}
 case BLOOD->{if(n==10)ring(w,aim,5,6);if(n==45)for(var x:xs)if(near(x,aim,5))hit(x,40,6,-1,.5);}
 case ULTIMATE->{if(n<=420&&n%8==0){int m=Math.min(6,n/60);entityData.set(FIRST,m);entityData.set(SECOND,(m+3)%7);note(w);}if(n>=60&&n<=420&&n%60==0){int m=(n/60-1)%7;wave(position(),front(),24,m,(m+3)%7,40,true);}if(n>=60&&n<=420&&n%30==0){double th=n*.04;zone(w,aim.add(8*Math.cos(th),0,8*Math.sin(th)),4,100,first(),second(),0);}if(n>420)getNavigation().stop();}
 default->{}
 }if(n>=LENGTH[a])finish();}
 @Override public void die(DamageSource s){super.die(s);bar.removeAllPlayers();zones.clear();waves.clear();bolts.clear();grabbed=null;}
 @Override public void remove(RemovalReason r){bar.removeAllPlayers();super.remove(r);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putLongArray("SkarapeyaReady",ready);if(home!=null){n.putDouble("SkarapeyaHomeX",home.x);n.putDouble("SkarapeyaHomeY",home.y);n.putDouble("SkarapeyaHomeZ",home.z);}n.putInt("SkarapeyaPhase",phase());n.putInt("SkarapeyaFirst",first());n.putInt("SkarapeyaSecond",second());}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);long[] a=n.getLongArray("SkarapeyaReady");System.arraycopy(a,0,ready,0,Math.min(a.length,ready.length));if(n.contains("SkarapeyaHomeX"))home=new Vec3(n.getDouble("SkarapeyaHomeX"),n.getDouble("SkarapeyaHomeY"),n.getDouble("SkarapeyaHomeZ"));entityData.set(PHASE,Math.max(SkarapeyaRules.phase(getHealth()),Math.clamp(n.getInt("SkarapeyaPhase"),1,3)));entityData.set(FIRST,Math.clamp(n.getInt("SkarapeyaFirst"),0,6));entityData.set(SECOND,n.contains("SkarapeyaSecond")?Math.clamp(n.getInt("SkarapeyaSecond"),-1,6):-1);finish();}
}
