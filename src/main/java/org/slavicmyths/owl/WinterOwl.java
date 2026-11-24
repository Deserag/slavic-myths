package org.slavicmyths.owl;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
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
import org.slavicmyths.combat.*;
import org.slavicmyths.morozko.*;

public final class WinterOwl extends Monster {
 public static final int ORBIT=0,LOW=1,LAND=2,GROUND=3,TAKEOFF=4,TRAIL=5,ICICLES=6,DIVE=7,FEATHERS=8,GUST=9,RUNES=10,SCREAM=11,GRAB=12,BLIZZARD=13,SUMMON=14,TORNADO=15,MULTI_DIVE=16,AWAKEN=17;
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(WinterOwl.class,EntityDataSerializers.INT),FRAME=SynchedEntityData.defineId(WinterOwl.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> BANK=SynchedEntityData.defineId(WinterOwl.class,EntityDataSerializers.FLOAT);
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatable("entity.slavicmyths.winter_owl"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
 private static final int[] FIRST={TRAIL,FEATHERS,DIVE,ICICLES,GUST,RUNES,SCREAM,GRAB},SECOND={BLIZZARD,TRAIL,SUMMON,ICICLES,TORNADO,DIVE,AWAKEN,FEATHERS,MULTI_DIVE,GUST,RUNES,SCREAM,GRAB};
 private final List<Patch> patches=new ArrayList<>();
 private final Set<UUID> spirits=new HashSet<>(),struck=new HashSet<>();
 private record Patch(Vec3 pos,long until){}
 private UUID parent,grabbed;
 private int life=600,combatAge,nextAttack=35,cursor,groundDue=260,lowDue=130;
 private Vec3 aim=Vec3.ZERO,landing;
 public WinterOwl(EntityType<? extends WinterOwl> type,Level level){super(type,level);setPersistenceRequired();setNoGravity(true);setHealth(getMaxHealth());xpReward=0;}
 public static AttributeSupplier.Builder attributes(boolean spirit){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,spirit?35:2000).add(Attributes.ARMOR,spirit?0:10).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FLYING_SPEED,.55).add(Attributes.FOLLOW_RANGE,64).add(Attributes.ATTACK_DAMAGE,spirit?5:16).add(Attributes.KNOCKBACK_RESISTANCE,spirit?0:.8);}
 @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
 public boolean spirit(){return getType()==WinterOwlContent.SPIRIT.get();}
 public boolean secondPhase(){return !spirit()&&getHealth()<=getMaxHealth()*.5F;}
 public int action(){return entityData.get(ACTION);}
 public int frame(){return entityData.get(FRAME);}
 public float bank(){return entityData.get(BANK);}
 public int belly(){return action()==TRAIL?1:action()==ICICLES?2:0;}
 public static int attackAt(boolean phase,int index){int[] a=phase?SECOND:FIRST;return a[Math.floorMod(index,a.length)];}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,ORBIT);b.define(FRAME,0);b.define(BANK,0F);}
 @Override protected void registerGoals(){}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(!spirit())bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override public void onRemovedFromLevel(){bar.removeAllPlayers();release();cleanup();super.onRemovedFromLevel();}
 @Override public void die(DamageSource s){release();cleanup();bar.removeAllPlayers();super.die(s);}
 @Override public boolean isAlliedTo(Entity e){return e instanceof WinterOwl||super.isAlliedTo(e);}
 private boolean valid(LivingEntity p){return p instanceof Player player&&!player.isCreative()&&!player.isSpectator()&&CombatTargeting.canAttack(this,p,CombatTargeting.Context.NATIVE_DAMAGE);}
 private List<Player> players(ServerLevel w,Vec3 p,double r,double h){return w.getEntitiesOfClass(Player.class,new AABB(p,p).inflate(r,h,r),e->valid(e)&&e.position().subtract(p).horizontalDistanceSqr()<=r*r);}
 private void begin(int a,LivingEntity p){entityData.set(ACTION,a);entityData.set(FRAME,0);struck.clear();if(p!=null)aim=p.position();if(a!=GROUND&&a!=LAND)setNoGravity(true);}
 private void finish(){release();begin(ORBIT,getTarget());nextAttack=combatAge+(secondPhase()?25:45);}
 private void release(){if(grabbed!=null&&level() instanceof ServerLevel w&&w.getEntity(grabbed) instanceof LivingEntity p&&p.getVehicle()==this)p.stopRiding();grabbed=null;}
 private void cleanup(){patches.clear();if(level() instanceof ServerLevel w)for(UUID id:spirits){Entity e=w.getEntity(id);if(e!=null)e.discard();}spirits.clear();}
 private void face(Vec3 d){if(d.horizontalDistanceSqr()<.0001)return;float desired=(float)Math.toDegrees(Math.atan2(-d.x,d.z));float turn=Mth.clamp(Mth.wrapDegrees(desired-getYRot()),-12F,12F);setYRot(getYRot()+turn);setYBodyRot(getYRot());setYHeadRot(getYRot());entityData.set(BANK,turn*.035F);}
 private boolean clear(ServerLevel w,Vec3 step){BlockPos p=BlockPos.containing(position().add(step));return w.hasChunkAt(p)&&w.getWorldBorder().isWithinBounds(p)&&w.noCollision(this,getBoundingBox().move(step))&&!w.containsAnyLiquid(getBoundingBox().move(step));}
 private void steer(ServerLevel w,Vec3 dest,double speed){Vec3 d=dest.subtract(position());Vec3 desired=d.lengthSqr()<.08?Vec3.ZERO:d.normalize().scale(Math.min(speed,d.length()));Vec3 v=getDeltaMovement().scale(.45).add(desired.scale(.55));if(!clear(w,v)){Vec3 up=new Vec3(0,.25,0);v=clear(w,up)&&action()!=LAND&&action()!=LOW?up:Vec3.ZERO;}setDeltaMovement(v);face(v);}
 private Vec3 floor(ServerLevel w,Vec3 p,double below){if(!w.hasChunkAt(BlockPos.containing(p)))return null;var h=w.clip(new ClipContext(p,p.add(0,-below,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));return h.getType()==HitResult.Type.BLOCK?h.getLocation().add(0,.05,0):null;}
 private Vec3 findLanding(ServerLevel w,LivingEntity p){for(int i=0;i<9;i++){double a=i*Math.PI/4;Vec3 at=p.position().add(i==0?0:Math.cos(a)*4,0,i==0?0:Math.sin(a)*4);Vec3 safe=FrostWorld.safe(w,at,getBbWidth(),getBbHeight(),2);if(safe!=null)return safe;}return null;}
 private void orbit(ServerLevel w,LivingEntity p,double height,double radius,double speed){double angle=combatAge*.035+(getUUID().getLeastSignificantBits()&7);steer(w,p.position().add(Math.cos(angle)*radius,height,Math.sin(angle)*radius),speed);}
 private void hit(LivingEntity p,float damage,int cold){if(valid(p)&&p.hurt(damageSources().mobAttack(this),damage))FrostCold.add(p,cold);}
 private void pulse(ServerLevel w,Vec3 pos,double r,double h,float damage,boolean push){for(Player p:players(w,pos,r,h)){if(!hasLineOfSight(p))continue;hit(p,damage,12);if(push&&CombatTargeting.canControl(this,p)){Vec3 v=p.position().subtract(pos).multiply(1,0,1).normalize();p.push(v.x*1.2,.28,v.z*1.2);}}}
 private void trail(ServerLevel w){Vec3 pos=floor(w,position(),30);if(pos==null)return;if(patches.size()>=80)patches.removeFirst();patches.add(new Patch(pos,w.getGameTime()+100));}
 private void tickPatches(ServerLevel w){long now=w.getGameTime();patches.removeIf(p->p.until<=now||!w.hasChunkAt(BlockPos.containing(p.pos)));if(tickCount%10!=0)return;Set<UUID> affected=new HashSet<>();for(Patch patch:patches){FrostVisuals.ring(w,patch.pos,2.3,.05,8);for(Player p:players(w,patch.pos,2.3,.8))if(affected.add(p.getUUID())&&p.onGround()&&!p.hasEffect(org.slavicmyths.registry.ModEffects.AUKA_FROST_BLOOD)){p.addEffect(new MobEffectInstance(org.slavicmyths.registry.ModEffects.AUKA_FROST,80),this);FrostCold.add(p,4);}}}
 private void icicle(ServerLevel w){double a=random.nextDouble()*Math.PI*2,r=1.5+random.nextDouble()*3;Vec3 at=position().add(Math.cos(a)*r,1.6,Math.sin(a)*r);Vec3 ground=floor(w,at,35);if(ground!=null&&w.noCollision(new AABB(at,at).inflate(.25)))FrostHazard.falling(this,at,10,ground.y);}
 private void summon(ServerLevel w,LivingEntity p){spirits.removeIf(id->w.getEntity(id)==null);for(int i=0;i<3&&spirits.size()<3;i++){double a=i*Math.PI*2/3;Vec3 at=position().add(Math.cos(a)*3,0,Math.sin(a)*3);if(!w.hasChunkAt(BlockPos.containing(at))||!w.noCollision(new AABB(at,at.add(.7,1.4,.7))))continue;WinterOwl s=WinterOwlContent.SPIRIT.get().create(w);if(s==null)continue;s.parent=getUUID();s.moveTo(at);s.setTarget(p);if(w.addFreshEntity(s))spirits.add(s.getUUID());}}
 private void spiritTick(ServerLevel w){if(--life<=0||parent==null||!(w.getEntity(parent) instanceof WinterOwl boss)||!boss.isAlive()){discard();return;}LivingEntity p=boss.getTarget();if(!valid(p)){steer(w,boss.position().add(0,2,0),.4);return;}combatAge++;orbit(w,p,1.1,2.5,.63);if(tickCount%25==0&&distanceToSqr(p)<16&&hasLineOfSight(p))hit(p,5,5);if(distanceToSqr(boss)>1600)discard();}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w)||!isAlive())return;if(isNoAi()){setDeltaMovement(Vec3.ZERO);return;}if(spirit()){spiritTick(w);return;}bar.setProgress(Mth.clamp(getHealth()/getMaxHealth(),0,1));LivingEntity p=getTarget();if(!valid(p)||distanceToSqr(p)>6400){p=players(w,position(),64,40).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(p);}
 if(!valid(p)){release();patches.clear();if(action()!=GROUND)begin(GROUND,null);setNoGravity(false);setDeltaMovement(getDeltaMovement().multiply(.6,1,.6));return;}
 combatAge++;tickPatches(w);int act=action(),f=frame()+1;entityData.set(FRAME,f);
 if(act!=GROUND&&act!=LAND&&tickCount%28==0)playSound(SoundEvents.PHANTOM_FLAP,1.7F,.75F);
 if(act==GROUND){setNoGravity(false);face(p.position().subtract(position()));if(f%35==20&&distanceToSqr(p)<36)pulse(w,position(),5,3,13,true);if(f>=140){begin(TAKEOFF,p);groundDue=combatAge+260;lowDue=combatAge+125;}return;}
 if(act==TAKEOFF){steer(w,p.position().add(0,9,0),.48);if(f>=35)finish();return;}
 if(act==LAND){if(landing==null){begin(LOW,p);return;}setNoGravity(true);steer(w,landing,.38);if(position().distanceToSqr(landing)<.4||onGround()){begin(GROUND,p);setNoGravity(false);setDeltaMovement(Vec3.ZERO);}else if(f>=90){landing=null;begin(LOW,p);}return;}
 if(act==LOW){orbit(w,p,.8,2.7,.23);if(f%35==22)pulse(w,position(),4,3,10,false);if(f>=100){landing=findLanding(w,p);if(landing!=null&&combatAge>=groundDue)begin(LAND,p);else{groundDue=combatAge+110;lowDue=combatAge+120;finish();}}return;}
 if(act==ORBIT){if(combatAge>=groundDue){landing=findLanding(w,p);begin(landing==null?LOW:LAND,p);return;}if(combatAge>=lowDue){lowDue=combatAge+180;begin(LOW,p);return;}orbit(w,p,9,10,.5);if(combatAge>=nextAttack){begin(attackAt(secondPhase(),cursor++),p);playSound(SoundEvents.AMETHYST_BLOCK_CHIME,1,.7F);}return;}
 switch(act){
 case TRAIL->{orbit(w,p,5,6,.48);if(f>=18&&f%5==0)trail(w);if(f>=110)finish();}
 case ICICLES->{orbit(w,p,9,7,.46);if(f>=18&&f%7==0)icicle(w);if(f>=105)finish();}
 case DIVE,MULTI_DIVE,GRAB->{int cycle=act==MULTI_DIVE?(f-1)%42+1:f;if(cycle<=14){aim=p.position();steer(w,p.position().add(0,7,0),.42);if(cycle==14)struck.clear();}else if(cycle<=32){steer(w,aim.add(0,.6,0),.8);if(getBoundingBox().inflate(.7).intersects(p.getBoundingBox())&&hasLineOfSight(p)&&struck.add(p.getUUID())){hit(p,act==GRAB?10:20,8);if(act==GRAB&&CombatTargeting.canControl(this,p)&&!p.isPassenger()&&p.startRiding(this,true))grabbed=p.getUUID();}}else steer(w,p.position().add(0,8,0),.52);if(act==GRAB&&grabbed!=null&&f>=35){steer(w,aim.add(0,9,0),.48);if(f==65)release();}if(f>=(act==MULTI_DIVE?126:act==GRAB?78:48)){release();landing=findLanding(w,p);begin(landing!=null?LAND:LOW,p);}}
 case FEATHERS->{steer(w,p.position().add(0,6,0),.33);if(f==18||f==30||f==42){Vec3 side=p.position().subtract(position()).multiply(1,0,1).normalize().yRot(Mth.HALF_PI);for(int i=-2;i<=2;i++)FrostShard.launch(this,p.getEyePosition().add(side.scale(i*1.6)),10,7,null);}if(f>=54)finish();}
 case GUST,SCREAM->{steer(w,p.position().add(0,1.5,0),.26);if(f==22){playSound(act==GUST?SoundEvents.PHANTOM_FLAP:SoundEvents.PHANTOM_AMBIENT,2,.65F);FrostVisuals.ring(w,position(),8,1,32);pulse(w,position(),8,5,act==GUST?12:15,act==GUST);if(act==SCREAM)for(Player v:players(w,position(),8,5))if(hasLineOfSight(v))v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,1));}if(f>=44)finish();}
 case RUNES->{orbit(w,p,6,6,.28);if(f==18||f==40)for(Player v:players(w,p.position(),14,8)){Vec3 at=FrostWorld.safe(w,v.position(),.7F,2.5F,1);if(at!=null)FrostHazard.spike(this,at,24,14,12,null);}if(f>=82)finish();}
 case BLIZZARD->{orbit(w,p,8,9,.38);if(f>=20&&f%10==0){FrostVisuals.storm(w,aim,14);for(Player v:players(w,aim,14,8)){FrostCold.add(v,6);v.addEffect(new MobEffectInstance(MobEffects.DARKNESS,25));}}if(f>=150)finish();}
 case SUMMON->{steer(w,p.position().add(0,6,0),.25);if(f==30)summon(w,p);if(f>=55)finish();}
 case TORNADO->{orbit(w,p,6,8,.35);if(f>=18&&f%4==0)for(int i=0;i<3;i++){double a=i*Math.PI*2/3+f*.035;Vec3 c=aim.add(Math.cos(a)*5,0,Math.sin(a)*5);FrostVisuals.storm(w,c,2);for(Player v:players(w,c,3,3)){if(!CombatTargeting.canControl(this,v)||!hasLineOfSight(v))continue;Vec3 pull=c.subtract(v.position()).multiply(1,0,1).normalize();v.push(pull.x*.18,.04,pull.z*.18);if(f%20==0)hit(v,7,9);}}if(f>=110)finish();}
 case AWAKEN->{steer(w,p.position().add(0,1.5,0),.25);if(f<35&&f%5==0)FrostVisuals.ring(w,position(),4+f*.15,.2,24);if(f==36){FrostHazard.wave(this,13,22,20,null);pulse(w,position(),6,4,14,true);}if(f>=58){landing=findLanding(w,p);begin(landing!=null?LAND:LOW,p);}}
 default->finish();
 }}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("OwlCursor",cursor);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);cursor=n.getInt("OwlCursor");}
}