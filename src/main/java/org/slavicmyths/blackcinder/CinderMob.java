package org.slavicmyths.blackcinder;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.blackcinder.CinderContent.Kind;

/** Authoritative finite attacks; entity-local work, never world/chunk searches. */
public final class CinderMob extends PathfinderMob {
 public enum Action { IDLE,BITE,CLAW,COAL,SMOKE,PULSE,RING,SPIKES,SHIFT,VORTEX,PHASE }
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(CinderMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(CinderMob.class,EntityDataSerializers.LONG);
 private static final EntityDataAccessor<Boolean> PHASE=SynchedEntityData.defineId(CinderMob.class,EntityDataSerializers.BOOLEAN);
 public final Kind kind;
 private int age,cooldown,anger,sequence;
 private Vec3 aim=Vec3.ZERO;
 private Vec3 smokeVelocity=Vec3.ZERO;
 private BlockPos home;
 private boolean rewarded;
 private UUID provoker;
 private boolean journalRegistered;
 private final Set<UUID> struck=new HashSet<>();
 private final ServerBossEvent bar=new ServerBossEvent(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.zharenitsa"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 public CinderMob(EntityType<? extends CinderMob> type,Level level,Kind kind){
  super(type,level);this.kind=kind;xpReward=kind==Kind.SPARKLING?0:kind==Kind.ZHARENITSA?60:12;
  getAttribute(Attributes.MAX_HEALTH).setBaseValue(CinderConfig.health(kind));setHealth(getMaxHealth());
  if(flying()){setNoGravity(true);moveControl=new FlyingMoveControl(this,15,true);navigation=new FlyingPathNavigation(this,level);}
 }
 public static AttributeSupplier.Builder attributes(Kind k){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,CinderConfig.health(k)).add(Attributes.ATTACK_DAMAGE,switch(k){case COAL_CRAWLER->9;case ASH_SPIRIT->6;case PEKELNIK->11;case SPARKLING->0;case ZHARENITSA->14;}).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FLYING_SPEED,.3).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,k==Kind.ZHARENITSA?.8:.2);}
 public boolean flying(){return kind==Kind.ASH_SPIRIT||kind==Kind.SPARKLING||kind==Kind.ZHARENITSA;}
 public boolean second(){return entityData.get(PHASE);}
 public Action action(){return Action.values()[Math.clamp(entityData.get(ACTION),0,Action.values().length-1)];}
 public float actionTime(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
 public void home(BlockPos pos){home=pos.immutable();setPersistenceRequired();}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(START,0L);b.define(PHASE,false);}
 @Override protected void registerGoals(){
  goalSelector.addGoal(1,new FloatGoal(this));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.65));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,12));goalSelector.addGoal(7,new RandomLookAroundGoal(this));
  goalSelector.addGoal(0,new Goal(){
   {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
   @Override public boolean canUse(){return kind!=Kind.SPARKLING&&getTarget()!=null&&getTarget().isAlive();}
   @Override public void tick(){var target=getTarget();if(target!=null)getLookControl().setLookAt(target,25,25);if(action()!=Action.IDLE)navigation.stop();}
  });
  targetSelector.addGoal(1,new HurtByTargetGoal(this));
  targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,10,true,false,p->kind!=Kind.SPARKLING&&kind!=Kind.ASH_SPIRIT));
 }
 @Override public void setTarget(LivingEntity target){super.setTarget(kind==Kind.SPARKLING?null:target);}
 @Override public boolean canAttack(LivingEntity target){return kind!=Kind.SPARKLING&&!(target instanceof CinderMob)&&!target.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)&&super.canAttack(target);}
 @Override public boolean isAlliedTo(Entity entity){return entity instanceof CinderMob||super.isAlliedTo(entity);}
 @Override public boolean hurt(DamageSource source,float amount){boolean result=super.hurt(source,amount);if(result&&!level().isClientSide&&kind==Kind.ASH_SPIRIT&&source.getEntity() instanceof LivingEntity attacker&&canAttack(attacker)){anger=400;provoker=attacker.getUUID();setTarget(attacker);}return result;}
 public static int windup(Action a){return switch(a){case RING,SPIKES,VORTEX->36;case PHASE->40;case BITE->22;case CLAW,COAL->26;case SHIFT,SMOKE->16;case PULSE->24;default->0;};}
 private int duration(Action a){return windup(a)+switch(a){case RING->34;case VORTEX->70;case BITE->15;default->18;};}
 private void begin(Action a){entityData.set(ACTION,a.ordinal());entityData.set(START,level().getGameTime());age=0;struck.clear();setDeltaMovement(Vec3.ZERO);smokeVelocity=Vec3.ZERO;aim=getTarget()==null?position():getTarget().position();navigation.stop();playSound(CinderSounds.get(switch(a){case RING->"ring";case SPIKES->"spikes";case VORTEX->"vortex";case SHIFT,SMOKE->"shift";case PHASE->"phase";default->kind==Kind.COAL_CRAWLER?"crawler_warn":kind==Kind.ASH_SPIRIT?"spirit_warn":"pekelnik_warn";}),.65F,1);}
 @Override public void tick(){
  super.tick();if(level().isClientSide){if(kind==Kind.SPARKLING&&tickCount%6==0)level().addParticle(ParticleTypes.SMALL_FLAME,getX()+(random.nextDouble()-.5)*.5,getY()+.3,getZ()+(random.nextDouble()-.5)*.5,0,.015,0);return;}
  if(kind==Kind.ZHARENITSA&&home!=null&&!journalRegistered&&level() instanceof ServerLevel server){journalRegistered=true;if(!CinderEncounters.get(server).register(home.asLong(),getUUID())){discard();return;}}
  if(kind==Kind.SPARKLING){setTarget(null);if(tickCount%80==0&&random.nextInt(4)==0)playSound(CinderSounds.get("spark_idle"),.12F,1);}
  if(kind==Kind.ASH_SPIRIT&&anger>0&&--anger==0)setTarget(null);
  if(kind==Kind.ASH_SPIRIT&&anger==0)setTarget(null);
  if(kind==Kind.ASH_SPIRIT&&anger>0&&getTarget()==null&&provoker!=null&&level() instanceof ServerLevel server&&server.getEntity(provoker) instanceof LivingEntity attacker&&attacker.isAlive()&&distanceToSqr(attacker)<32*32&&canAttack(attacker))setTarget(attacker);
  if(flying()){
   setNoGravity(true);
   if(action()==Action.IDLE && getTarget()==null&&tickCount%60==0){var p=position().add(random.nextDouble()*6-3,random.nextDouble()*2-1,random.nextDouble()*6-3);if(safe(p,false))moveControl.setWantedPosition(p.x,p.y,p.z,.5);}
   // A boss remains close to the arena floor rather than drifting out of reach.
   if(kind==Kind.ZHARENITSA&&home!=null&&getY()>home.getY()+3)setDeltaMovement(getDeltaMovement().add(0,-.08,0));
  }
  if(kind==Kind.ZHARENITSA){bar.setProgress(getHealth()/750F);if(!second()&&getHealth()<=375&&isAlive()){entityData.set(PHASE,true);begin(Action.PHASE);}if(home!=null&&distanceToSqr(Vec3.atCenterOf(home))>48*48){setTarget(null);navigation.moveTo(home.getX()+.5,home.getY()+1,home.getZ()+.5,1);}}
  if(cooldown>0)cooldown--;
  if(action()!=Action.IDLE){
   navigation.stop();age++;telegraph();
   if(age==windup(action()))impact();
   if(action()==Action.RING&&age>=windup(action()))ring();
   if(action()==Action.VORTEX&&age>=windup(action())&&age%10==0)vortex();
   if(action()==Action.BITE&&age>windup(action())&&age<windup(action())+8){if(horizontalCollision||!safe(position().add(getDeltaMovement()),true))setDeltaMovement(Vec3.ZERO);else bite();}
   if(action()==Action.SMOKE&&age>windup(action())){if(age<windup(action())+8&&safe(position().add(smokeVelocity),false)){setDeltaMovement(smokeVelocity);hurtMarked=true;}else setDeltaMovement(Vec3.ZERO);}
   if(age>=duration(action())){entityData.set(ACTION,0);cooldown=second()?24:40;}
   return;
  }
  var target=getTarget();if(target==null||!target.isAlive()||!canAttack(target)){setTarget(null);return;}
  if(target instanceof Player p&&(p.isCreative()||p.isSpectator())){setTarget(null);return;}
  getLookControl().setLookAt(target,25,25);
  double d=distanceToSqr(target);if(tickCount%10==0)navigation.moveTo(target,1.05);
  if(cooldown>0||!hasLineOfSight(target)||d>32*32)return;
  switch(kind){
   case COAL_CRAWLER->{if(d<100)begin(Action.BITE);}
   case ASH_SPIRIT->begin(sequence++%2==0?Action.SMOKE:Action.PULSE);
   case PEKELNIK->begin(d<12?Action.CLAW:Action.COAL);
   case ZHARENITSA->{Action[] cycle=second()?new Action[]{Action.RING,Action.SPIKES,Action.SHIFT,Action.VORTEX}:new Action[]{Action.RING,Action.SPIKES,Action.SHIFT};begin(cycle[Math.floorMod(sequence++,cycle.length)]);}
   default->{}
  }
 }
 private void telegraph(){if(!(level() instanceof ServerLevel s)||age%4!=0||age>windup(action()))return;
  if(action()==Action.SPIKES)for(int i=0;i<5;i++){double a=i*Math.PI*2/5;s.sendParticles(ParticleTypes.ASH,aim.x+Math.cos(a)*3,aim.y+.15,aim.z+Math.sin(a)*3,5,.25,.05,.25,.01);}
  else if(action()==Action.RING)for(int i=0;i<24;i++){double a=i*Math.PI/12;s.sendParticles(ParticleTypes.SMALL_FLAME,getX()+Math.cos(a)*2,floorY()+.1,getZ()+Math.sin(a)*2,1,0,0,0,0);}
  else s.sendParticles(ParticleTypes.ASH,getX(),getY()+1,getZ(),5,.5,.5,.5,.02);
 }
 private boolean safe(Vec3 p,boolean ground){var b=BlockPos.containing(p);var box=getBoundingBox().move(p.subtract(position()));for(int cx=net.minecraft.util.Mth.floor(box.minX)>>4;cx<=(net.minecraft.util.Mth.floor(box.maxX)>>4);cx++)for(int cz=net.minecraft.util.Mth.floor(box.minZ)>>4;cz<=(net.minecraft.util.Mth.floor(box.maxZ)>>4);cz++)if(!level().hasChunk(cx,cz))return false;return level().getWorldBorder().isWithinBounds(b)&&level().getFluidState(b).isEmpty()&&level().noCollision(this,box)&&(!ground||level().getBlockState(b.below()).isFaceSturdy(level(),b.below(),Direction.UP));}
 private double floorY(){var base=blockPosition();for(int d=0;d<12;d++){var q=base.below(d);if(level().getBlockState(q.below()).isFaceSturdy(level(),q.below(),Direction.UP))return q.getY();}return home!=null?home.getY():getY();}
 private boolean pathClear(Vec3 p){for(int step=1;step<=10;step++)if(!safe(position().lerp(p,step/10.0),false))return false;return true;}
 private void shift(){for(int i=0;i<8;i++){var p=position().add(random.nextInt(11)-5,0,random.nextInt(11)-5);if(kind==Kind.ZHARENITSA){var b=BlockPos.containing(p);if(!level().hasChunkAt(b))continue;for(int d=0;d<7;d++)if(level().getBlockState(b.below(d+1)).isFaceSturdy(level(),b.below(d+1),Direction.UP)){p=new Vec3(p.x,b.getY()-d,p.z);break;}}if((home==null||p.distanceToSqr(Vec3.atCenterOf(home))<24*24)&&safe(p,kind==Kind.ZHARENITSA)&&pathClear(p)){if(action()==Action.SMOKE){smokeVelocity=p.subtract(position()).normalize().scale(.65);setDeltaMovement(smokeVelocity);hurtMarked=true;}else teleportTo(p.x,p.y,p.z);return;}}}
 private void impact(){switch(action()){
  case BITE->{var direction=aim.subtract(position()).multiply(1,0,1).normalize().scale(.85);if(safe(position().add(direction),true))setDeltaMovement(direction.add(0,.08,0));bite();}
  case CLAW->area(position(),3.2,11,true);
  case PULSE->{area(position(),3.5,6,false);playSound(CinderSounds.get("spirit_pulse"),.6F,1);}
  case SMOKE,SHIFT->shift();
  case COAL->{if(getTarget()!=null&&hasLineOfSight(getTarget())&&level() instanceof ServerLevel s){var shot=new CinderCoal(CinderWorldgen.COAL.get(),s);shot.setOwner(this);shot.setPos(getX(),getEyeY()-.2,getZ());var d=getTarget().getEyePosition().subtract(shot.position());shot.shoot(d.x,d.y,d.z,.85F,0);s.addFreshEntity(shot);playSound(CinderSounds.get("pekelnik_throw"),.7F,1);}}
  case SPIKES->{if(level() instanceof ServerLevel s)for(int i=0;i<5;i++){double a=i*Math.PI*2/5;var p=aim.add(Math.cos(a)*3,0,Math.sin(a)*3);var b=BlockPos.containing(p);if(!s.hasChunkAt(b))continue;for(int dy=2;dy>=-3;dy--){var q=b.above(dy);if(s.isEmptyBlock(q)&&s.getBlockState(q.below()).isFaceSturdy(s,q.below(),Direction.UP)){var spike=new CinderSpike(CinderWorldgen.SPIKE.get(),s);spike.owner=getUUID();spike.setPos(q.getX()+.5,q.getY(),q.getZ()+.5);s.addFreshEntity(spike);break;}}}}
  default->{}
 }}
 private void bite(){if(age==windup(Action.BITE))playSound(CinderSounds.get("crawler_hit"),.55F,1);area(position().add(getLookAngle().multiply(1,0,1).normalize().scale(1.1)),1.8,9,true);}
 private void area(Vec3 center,double radius,float damage,boolean front){for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius,1,radius),this::validVictim)){var d=e.position().subtract(position());if(e.position().distanceToSqr(center)>radius*radius||front&&getLookAngle().multiply(1,0,1).normalize().dot(d.multiply(1,0,1).normalize())<.1)continue;if(struck.add(e.getUUID())&&e.hurt(damageSources().mobAttack(this),damage))e.knockback(.5,-d.x,-d.z);}}
 private boolean validVictim(LivingEntity e){return e!=this&&canAttack(e)&&!isAlliedTo(e)&&hasLineOfSight(e)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()));}
 private void ring(){double radius=2+(age-windup(Action.RING))*.42;if(level() instanceof ServerLevel s){for(int i=0;i<40;i++){double a=i*Math.PI/20;s.sendParticles(ParticleTypes.FLAME,getX()+Math.cos(a)*radius,floorY()+.15,getZ()+Math.sin(a)*radius,1,0,0,0,0);}for(LivingEntity e:s.getEntitiesOfClass(LivingEntity.class,new AABB(getX()-17,floorY()-1,getZ()-17,getX()+17,floorY()+2,getZ()+17),this::validVictim)){double d=e.position().subtract(position()).horizontalDistance();if(Math.abs(d-radius)<.7&&Math.abs(e.getY()-floorY())<.65&&struck.add(e.getUUID()))e.hurt(CinderDamage.heat(this,this),12);}}}
 private void vortex(){if(!(level() instanceof ServerLevel s)||!second())return;for(int i=0;i<24;i++){double a=i*.8+age*.2;double r=2+i*.25;s.sendParticles(ParticleTypes.ASH,getX()+Math.cos(a)*r,getY()+i*.12,getZ()+Math.sin(a)*r,1,0,0,0,0);}for(LivingEntity e:s.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(8,3,8),this::validVictim)){var d=position().subtract(e.position());if(d.lengthSqr()>64)continue;e.push(d.normalize().x*.06,0,d.normalize().z*.06);e.hurt(CinderDamage.heat(this,this),3);}}
 @Override public boolean doHurtTarget(Entity entity){return false;}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(kind==Kind.ZHARENITSA)bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s){return CinderSounds.get("hurt");}
 @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return CinderSounds.get("death");}
 @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean hit){
  super.dropCustomDeathLoot(level,source,hit);if(rewarded||kind==Kind.SPARKLING)return;rewarded=true;
  if(kind==Kind.ZHARENITSA){if(home==null||!CinderEncounters.get(level).canClaim(home.asLong(),getUUID())){
    if(source.getEntity() instanceof ServerPlayer player)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("cinder.boss.no_arena_reward"),false);
    return;
   }
   ServerPlayer winner=source.getEntity() instanceof ServerPlayer p?p:getKillCredit() instanceof ServerPlayer p?p:null;
   if(winner!=null){var pin=org.slavicmyths.losthalls.BossRewardInbox.prepare(level.getServer(),"zharenitsa:"+level.dimension().location()+":"+home.asLong(),"zharenitsa_head",winner.getUUID(),0,level.getServer().overworld().getGameTime());if(pin==null)return;}
   if(!CinderEncounters.get(level).claim(home.asLong(),getUUID()))return;if(winner==null)spawnAtLocation(CinderBlocks.get("zharenitsa_head").asItem());spawnAtLocation(new ItemStack(ZharenitsaSeals.SEAL.get(),4+random.nextInt(3)));spawnAtLocation(CinderContent.RUNE.get());
   var soul=new ItemStack(org.slavicmyths.rpg.RuneFoundation.item("soul"));var tag=new CompoundTag();tag.putBoolean("slavicmyths_infected_medium",true);soul.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));soul.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.translatable("item.slavicmyths.infected_medium_soul"));spawnAtLocation(soul);
  }
 }
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putBoolean("CinderSecond",second());t.putBoolean("CinderRewarded",rewarded);t.putInt("CinderSequence",sequence);t.putInt("CinderAnger",anger);if(provoker!=null)t.putUUID("CinderProvoker",provoker);if(home!=null)t.putLong("CinderHome",home.asLong());}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(PHASE,t.getBoolean("CinderSecond"));rewarded=t.getBoolean("CinderRewarded");sequence=Math.max(0,t.getInt("CinderSequence"));anger=Math.clamp(t.getInt("CinderAnger"),0,400);provoker=t.hasUUID("CinderProvoker")?t.getUUID("CinderProvoker"):null;if(t.contains("CinderHome"))home=BlockPos.of(t.getLong("CinderHome"));entityData.set(ACTION,0);setDeltaMovement(Vec3.ZERO);smokeVelocity=Vec3.ZERO;cooldown=40;}
}
