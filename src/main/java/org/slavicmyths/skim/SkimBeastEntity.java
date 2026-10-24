package org.slavicmyths.skim;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
/** Finite attacks, encounter-local threat, locked leap and resumable fatigue. No passive contact damage. */
public final class SkimBeastEntity extends SkimActor {
 private static final EntityDataAccessor<BlockPos> LANDING=SynchedEntityData.defineId(SkimBeastEntity.class,EntityDataSerializers.BLOCK_POS);
 private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(SkimBeastEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> REAR=SynchedEntityData.defineId(SkimBeastEntity.class,EntityDataSerializers.BOOLEAN);
 private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.skim_beast"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 public UUID arena,owner,encounter=UUID.randomUUID(),victim;public int stamina=100,phase=1;public boolean rageUsed,recorded;
 public final Map<UUID,Double> threat=new HashMap<>();public final Map<UUID,Integer> attendance=new HashMap<>();public final Set<UUID> contributors=new HashSet<>();
 private final Deque<Long> misses=new ArrayDeque<>();private long lastTarget,lastValid,adaptUntil,tauntUntil,exhaustLock,majorAt;private UUID taunter;private Vec3 leapFrom,locked,flank;private float rescue;private int deathAge;
 public SkimBeastEntity(EntityType<? extends SkimBeastEntity> t,Level w){super(t,w);}
 @Override protected AABB makeBoundingBox(){var h=heading();double x=Math.abs(h.x)*2.4+Math.abs(h.z)*1.05,z=Math.abs(h.z)*2.4+Math.abs(h.x)*1.05;return new AABB(getX()+h.x*.55-x,getY(),getZ()+h.z*.55-z,getX()+h.x*.55+x,getY()+2.95,getZ()+h.z*.55+z);}
 @Override public void face(LivingEntity e){super.face(e);setBoundingBox(makeBoundingBox());}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(LANDING,BlockPos.ZERO);b.define(PHASE,1);b.define(REAR,true);}
 public boolean canRear(){return entityData.get(REAR);}
 public BlockPos landing(){return entityData.get(LANDING);}public int syncedPhase(){return entityData.get(PHASE);}
 public SkimValley.Entry valley(){return arena!=null&&level() instanceof ServerLevel w?SkimValley.get(w).valleys.get(arena):null;}
 public boolean active(){var e=valley();return e!=null&&(e.status==SkimValley.Status.ACTIVE||e.status==SkimValley.Status.STARTING);}
 public boolean heavy(){return action()==SkimAction.LEAP||action()==SkimAction.ROAR||action()==SkimAction.FLANK||action()==SkimAction.CLAWS||action()==SkimAction.GRIP;}
 public void addThreat(LivingEntity e,double value){if(valid(e))threat.merge(e.getUUID(),value,Double::sum);}
 public void taunt(AlyoshaEntity h){addThreat(h,30);taunter=h.getUUID();tauntUntil=level().getGameTime()+90;}
 public void fatigue(int value){if(ready("allyFatigue")){stamina=Math.max(0,stamina-value);cooldown("allyFatigue",100);}}
 public void miss(){long now=level().getGameTime();misses.add(now);while(!misses.isEmpty()&&misses.peek()<now-200)misses.remove();if(misses.size()>=2)adaptUntil=now+160;}
 public boolean valid(LivingEntity e){if(e==null||!e.isAlive()||e.level()!=level())return false;var v=valley();if(arena==null&&org.slavicmyths.bogatyr.HeroThreats.availableHero(e))return distanceToSqr(e)<=36*36;if(e instanceof ServerPlayer p)return !p.isCreative()&&!p.isSpectator()&&(v==null||v.inside(p.position(),32));return e instanceof AlyoshaEntity h&&h.owner!=null&&level() instanceof ServerLevel sw&&sw.getServer().getPlayerList().getPlayer(h.owner) instanceof ServerPlayer allyOwner&&allyOwner.isAlive()&&allyOwner.level()==level()&&!allyOwner.isCreative()&&!allyOwner.isSpectator()&&!h.recovering()&&(v==null||h.owner.equals(owner))&&distanceToSqr(h)<=36*36;}
 private List<LivingEntity> candidates(ServerLevel w){return w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(36),this::valid);}
 private void target(ServerLevel w){long now=w.getGameTime();var list=candidates(w);for(var e:list)threat.putIfAbsent(e.getUUID(),e instanceof ServerPlayer?12D:8D);
  if(valid(getTarget())&&now-lastTarget<30&&distanceToSqr(getTarget())<=324)return;
  var next=list.stream().max(Comparator.comparingDouble(e->{double t=threat.getOrDefault(e.getUUID(),1D);if(e.getUUID().equals(taunter)&&now<tauntUntil)t*=1.8;if(e instanceof ServerPlayer&&now<adaptUntil)t=1.3*(t+threat.values().stream().mapToDouble(Double::doubleValue).max().orElse(0));return t;})).orElse(null);if(next!=getTarget()){setTarget(next);lastTarget=now;}
 }
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w)||!isAlive())return;long now=w.getGameTime();var v=valley();if(arena!=null&&(v==null||!getUUID().equals(v.boss)||!active())){release();discard();return;}
  if(tickCount%20==0){bar.setProgress(getHealth()/750);var players=w.getPlayers(p->!p.isSpectator()&&distanceToSqr(p)<6400&&(v==null||v.inside(p.position(),36)));for(var p:new ArrayList<>(bar.getPlayers()))if(!players.contains(p))bar.removePlayer(p);for(var p:players)bar.addPlayer(p);if(v!=null)for(var p:players)if(!p.isCreative()&&v.inside(p.position(),32)){attendance.merge(p.getUUID(),20,Integer::sum);v.participants.add(p.getUUID());SkimValley.get(w).setDirty();}}
  if(v!=null){boolean player=w.getPlayers(p->!p.isCreative()&&!p.isSpectator()&&v.inside(p.position(),32)).size()>0;if(player){lastValid=now;v.lastPlayerAt=now;}if(tickCount%40==0){v.bossPos=blockPosition();SkimValley.get(w).setDirty();}else if(now-lastValid>1200){SkimValley.abort(w,v);return;}else if(now-lastValid>600){release();setTarget(null);getNavigation().moveTo(v.at(0,1,-20).getX()+.5,v.center.getY()+1,v.at(0,1,-20).getZ()+.5,1);return;}}
  if(tickCount%20==0){var list=candidates(w);threat.entrySet().removeIf(e->list.stream().noneMatch(t->t.getUUID().equals(e.getKey())));threat.replaceAll((id,value)->getTarget()!=null&&id.equals(getTarget().getUUID())?value:value*.9);}
  if(tickCount%15==0)target(w);if(!valid(getTarget())){release();setTarget(null);}if(action()==SkimAction.AWAKEN&&age()>=60){if(v!=null){v.status=SkimValley.Status.ACTIVE;SkimValley.get(w).setDirty();}begin(SkimAction.IDLE);}
  phase=Math.max(phase,getHealth()<225?3:getHealth()<=525?2:1);entityData.set(PHASE,phase);
  if(phase==3&&!rageUsed&&(action()==SkimAction.IDLE||action()==SkimAction.RECOVER)){rageUsed=true;release();begin(SkimAction.RAGE);cue("skim.rage");}
  step(w);if(action().ticks>0&&age()>=action().ticks)finish();if(action()!=SkimAction.IDLE)return;
  if(tickCount%20==0)stamina=Math.min(100,stamina+(getNavigation().isDone()?2:1));if(tickCount%10!=0)return;var t=getTarget();if(!valid(t))return;face(t);double d=edge(t);
  if(phase>=2&&d<2.5&&ready("grip")&&now>=majorAt){attack(SkimAction.GRIP,"grip",260);return;}
  if(phase>=2&&ready("whistle")&&now>=majorAt&&d<18&&hasLineOfSight(t)){attack(SkimAction.WHISTLE,"whistle",210);return;}
  if(phase>=2&&ready("roar")&&now>=majorAt&&d<7){attack(SkimAction.ROAR,"roar",190);return;}
  if(ready("leap")&&now>=majorAt&&d>=4&&d<=14){attack(SkimAction.LEAP,"leap",150);return;}
  if(phase>=2&&ready("flank")&&now>=majorAt&&d>=4&&d<=9){attack(SkimAction.FLANK,"flank",165);flank=heading().scale(.34).add(new Vec3(heading().z,0,-heading().x).scale(random.nextBoolean()?.28:-.28));return;}
  if(d<=3.3&&ready("claws")){attack(SkimAction.CLAWS,"claws",84);return;}getNavigation().moveTo(t,phase==3?1.32:1.18);
  if(tickCount%14==0&&getDeltaMovement().horizontalDistanceSqr()>.001)cue("skim.step_"+(tickCount%28==0?1:2));if(ready("ambient")){cooldown("ambient",180+random.nextInt(141));cue("skim.ambient_"+(random.nextBoolean()?1:2));}
 }
 public void attack(SkimAction a,String k,int cd){cooldown(k,(int)Math.ceil(cd*(phase==3?.87:1)));if(a==SkimAction.ROAR)entityData.set(REAR,level().noCollision(this,new AABB(getX()-1.4,getY()+2.95,getZ()-2.6,getX()+1.4,getY()+4.7,getZ()+2.6)));if(a!=SkimAction.CLAWS)majorAt=level().getGameTime()+(phase==1?140:20)+a.ticks;begin(a);}
 public DamageSource damage(String k){return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.DAMAGE_TYPE,SkimContent.id("skim_"+k))),this);}
 public static float raw(double net){return (float)((-.2+Math.sqrt(.04+.04*net))/.02);}
 private void hit(LivingEntity e,String kind,double net){if(!valid(e)||!hasLineOfSight(e))return;e.invulnerableTime=0;e.hurt(damage(kind),raw(net));}
 private int strike(ServerLevel w,double reach,double angle,String k,double net){int hits=0;for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(reach),this::valid))if(cone(e,reach,angle)&&hasLineOfSight(e)){hit(e,k,net);hits++;}if(hits>0)cue("skim.claw_hit");return hits;}
 private void step(ServerLevel w){int t=age();var target=getTarget();switch(action()){
  case CLAWS->{if(t==5)cue("skim.claw_windup");if(t==11||t==19)strike(w,3.3,125,"claw",2.5);if(t==30){int n=strike(w,3.8,125,"claw",6.1);if(n==0)miss();}}
  case LEAP->{if(t==5)cue("skim.leap_charge");if(t==12&&valid(target)){locked=target.position();leapFrom=position();entityData.set(LANDING,BlockPos.containing(locked));}
   if(t>=13&&t<=28&&locked!=null&&leapFrom!=null){double f=(t-12)/16D;var desired=leapFrom.lerp(locked,f).add(0,Math.sin(Math.PI*f)*3.2,0);var delta=desired.subtract(position());boolean rock=false;var v=valley();if(v!=null)for(int i=1;i<=4;i++){var q=BlockPos.containing(position().add(delta.scale(i/4D)));if(v.rocks.contains(q.asLong())&&!w.getBlockState(q).isAir()){rock=true;break;}}
    if(rock){release();begin(SkimAction.STAGGER);cooldown("stagger",80);cue("skim.stone_collision");return;}move(MoverType.SELF,delta);setDeltaMovement(Vec3.ZERO);fallDistance=0;
   }if(t==29&&locked!=null){int n=0;for(var e:w.getEntitiesOfClass(LivingEntity.class,new AABB(locked.add(-2.2,-1,-2.2),locked.add(2.2,4,2.2)),this::valid)){double d=e.position().multiply(1,0,1).distanceTo(locked.multiply(1,0,1));if(d<=2.2){hit(e,"leap",d<.9?7:3+(7-3)*(2.2-d)/1.3);n++;}}if(n==0)miss();cue("skim.leap_land");}}
  case WHISTLE->{if(t==9)cue("skim.whistle_charge");if(t==22){for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(18),this::valid))if(cone(e,18,75)&&hasLineOfSight(e)){hit(e,"whistle",1);e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60));e.addEffect(new MobEffectInstance(MobEffects.CONFUSION,30));}cue("skim.whistle_release");}}
  case ROAR->{if(t==26)cue("skim.roar");if(t==28){for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(7),this::valid))if(hasLineOfSight(e)&&edge(e)<=7){if(cone(e,4,115))hit(e,"roar",6.1);else hit(e,"roar",3);e.knockback(.45,getX()-e.getX(),getZ()-e.getZ());}}}
  case FLANK->{if(t>=11&&t<=22&&flank!=null){if(w.noCollision(this,getBoundingBox().move(flank)))move(MoverType.SELF,flank);if(valid(target))face(target);}if(t==24&&strike(w,3.8,90,"flank",6.1)==0)miss();}
  case GRIP->{if(t==16&&valid(target)&&cone(target,2.5,70)&&hasLineOfSight(target)){victim=target.getUUID();rescue=0;cue("skim.grab");}var held=held();if(t>=18&&t<=44&&held!=null){var at=position().add(heading().scale(1.7)).add(0,.55,0);var shift=at.subtract(held.position());if(shift.lengthSqr()>36||!w.noCollision(held,held.getBoundingBox().move(shift)))release();else{held.setDeltaMovement(Vec3.ZERO);held.teleportTo(at.x,at.y,at.z);held.fallDistance=0;held.hurtMarked=true;}}if(held!=null&&(t==21||t==31||t==41))hit(held,"grip",t==21?3:2);if(t==30)cue("skim.grip_hold");if(t==45){var e=held();if(e!=null){hit(e,"grip",4);e.setDeltaMovement(heading().scale(.4).add(0,.1,0));e.hurtMarked=true;e.fallDistance=0;}release();cue("skim.throw");}}
  case EXHAUSTED->{if(t==15||t==44)cue("skim.exhausted");}
  default->{}
 }}
 private void finish(){var old=action();release();if(old==SkimAction.EXHAUSTED){stamina=56;exhaustLock=level().getGameTime()+140;begin(SkimAction.IDLE);return;}if(old==SkimAction.RECOVER||old==SkimAction.STAGGER||old==SkimAction.RAGE){if(stamina<=15&&level().getGameTime()>=exhaustLock){begin(SkimAction.EXHAUSTED);if(level() instanceof ServerLevel w)for(var p:bar.getPlayers())p.sendSystemMessage(Component.translatable("message.slavicmyths.skim.exhausted"));}else begin(SkimAction.IDLE);return;}
  int cost=switch(old){case CLAWS->14;case LEAP,GRIP->25;case ROAR->20;case FLANK->18;case WHISTLE->10;default->0;};stamina=Math.max(0,stamina-(int)Math.ceil(cost*(phase==3?1.25:1)));begin(old==SkimAction.AWAKEN?SkimAction.IDLE:SkimAction.RECOVER);
 }
 public LivingEntity held(){if(victim==null||!(level() instanceof ServerLevel w))return null;var e=w.getEntity(victim);if(e instanceof LivingEntity l&&valid(l)&&distanceToSqr(l)<36)return l;release();return null;}
 public void release(){victim=null;rescue=0;}
 public void rescue(float amount){if(held()==null)return;rescue+=amount;if(rescue>=16){var e=held();release();if(e!=null){e.setDeltaMovement(heading().scale(.3).add(0,.1,0));e.hurtMarked=true;}begin(SkimAction.RECOVER);cooldown("stagger",80);}}
 public void registeredHit(DamageSource s,float raw,float actual){if(actual<=0)return;if(s.getEntity() instanceof ServerPlayer p&&!p.isCreative()&&!p.isSpectator()&&valid(p)){contributors.add(p.getUUID());addThreat(p,s.getDirectEntity()==p?8:5);}else if(s.getEntity() instanceof AlyoshaEntity a)addThreat(a,s.getDirectEntity()==a?6:4);var direct=s.getDirectEntity();if(held()!=null&&direct!=null&&direct.getY()>=getY()&&direct.position().distanceToSqr(position().add(heading().scale(2.5)).add(0,1.4,0))<16)rescue(raw);}
 @Override public boolean hurt(DamageSource s,float amount){boolean hit=super.hurt(s,amount);if(hit&&ready("hurt")){cooldown("hurt",14);cue("skim.hurt_"+(random.nextBoolean()?1:2));}return hit;
 }
 @Override public void die(DamageSource s){boolean advancementWasDead=dead;org.slavicmyths.progression.AdvancementMilestones.beginDeath(this);if(!recorded&&level() instanceof ServerLevel w){recorded=true;release();if(s.getEntity() instanceof ServerPlayer hitter&&!hitter.isCreative()&&!hitter.isSpectator()&&valid(hitter))contributors.add(hitter.getUUID());if(active()&&!s.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)&&((s.getEntity() instanceof ServerPlayer p&&!p.isCreative())||(s.getEntity() instanceof AlyoshaEntity a&&owner!=null&&owner.equals(a.owner))))SkimValley.win(w,valley(),this);else if(active())SkimValley.abort(w,valley());bar.removeAllPlayers();begin(SkimAction.DEATH);cue("skim.death");}super.die(s);org.slavicmyths.progression.AdvancementMilestones.endDeath(this,!advancementWasDead&&dead,s);}
 @Override protected void tickDeath(){if(++deathAge>=75)remove(RemovalReason.KILLED);}
 @Override public void onRemovedFromLevel(){bar.setVisible(false);bar.removeAllPlayers();super.onRemovedFromLevel();}
 @Override public void remove(RemovalReason r){release();bar.removeAllPlayers();super.remove(r);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(arena!=null)n.putUUID("SkimArena",arena);if(owner!=null)n.putUUID("SkimOwner",owner);n.putUUID("SkimEncounter",encounter);n.putInt("Stamina",stamina);n.putInt("Phase",phase);n.putBoolean("RageUsed",rageUsed);n.putLong("ExhaustLock",exhaustLock);n.putLong("MajorAt",majorAt);n.putBoolean("Recorded",recorded);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.hasUUID("SkimArena"))arena=n.getUUID("SkimArena");if(n.hasUUID("SkimOwner"))owner=n.getUUID("SkimOwner");if(n.hasUUID("SkimEncounter"))encounter=n.getUUID("SkimEncounter");stamina=n.contains("Stamina")?Math.clamp(n.getInt("Stamina"),0,100):100;phase=Math.max(1,n.getInt("Phase"));rageUsed=n.getBoolean("RageUsed");exhaustLock=n.getLong("ExhaustLock");majorAt=n.getLong("MajorAt");recorded=n.getBoolean("Recorded");lastValid=level().getGameTime();release();}
}
