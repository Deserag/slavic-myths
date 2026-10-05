package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.*;
import org.slavicmyths.entity.LandSpiritEntity;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.hunt.HuntRules.Move;

/** Shares spirit save/guardian integration, replaces the old schedule with one exclusive fight goal. */
public abstract class HuntMob extends LandSpiritEntity {
 private static final DataParameter<Integer> ACTION=EntityDataManager.defineId(HuntMob.class,DataSerializers.INT),START=EntityDataManager.defineId(HuntMob.class,DataSerializers.INT);
 private static final DataParameter<Boolean> RAGE=EntityDataManager.defineId(HuntMob.class,DataSerializers.BOOLEAN);
 public UUID owner;private final EnumMap<Move,Integer> timers=new EnumMap<>(Move.class);
 private final ServerBossInfo bar=new ServerBossInfo(new net.minecraft.util.text.StringTextComponent(""),BossInfo.Color.RED,BossInfo.Overlay.PROGRESS);
 private int actionTick,basicWait,memory,unreachable,alignWait,alignRecovery;private boolean dashHit;private BlockPos lastSeen;private Vector3d aim=Vector3d.ZERO,sidePoint;
 private final Map<UUID,Long> prey=new HashMap<>(),trailHits=new HashMap<>();private final List<Trail> trail=new ArrayList<>();
 private static final class Trail{final Vector3d pos;final long until;Trail(Vector3d p,long until){pos=p;this.until=until;}}
 protected HuntMob(EntityType<? extends HuntMob> t,World w){super(t,w);xpReward=50;bar.setName(getDisplayName());}
 public abstract boolean oven();
 protected boolean miniBoss(){return true;}
 protected boolean showsHuntBar(){return true;}
 public HuntTarget huntTarget(){return oven()?HuntTarget.OVINNIK:HuntTarget.VOLKOLAK;}
 public static AttributeModifierMap.MutableAttribute attributes(boolean o){return createMobAttributes().add(Attributes.MAX_HEALTH,o?175:165).add(Attributes.ATTACK_DAMAGE,o?10:9).add(Attributes.MOVEMENT_SPEED,o?.245:.335).add(Attributes.ARMOR,o?10:7).add(Attributes.ARMOR_TOUGHNESS,o?2:0).add(Attributes.KNOCKBACK_RESISTANCE,o?.55:.25).add(Attributes.FOLLOW_RANGE,o?32:36);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ACTION,0);entityData.define(START,0);entityData.define(RAGE,false);}
 public Move action(){return Move.values()[entityData.get(ACTION)];}public boolean rage(){return entityData.get(RAGE);}public float elapsed(float partial){return Math.max(0,(int)level.getGameTime()-entityData.get(START)+partial);}
 @Override protected boolean usesSpiritSchedule(){return false;}
 @Override protected void think(){} // Combat is wholly scheduled by Fight below, never by two goals.
 @Override protected void registerGoals(){goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(4,new LookAtGoal(this,PlayerEntity.class,12));goalSelector.addGoal(5,new LookRandomlyGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<PlayerEntity>(this,PlayerEntity.class,true).setUnseenMemoryTicks(oven()?200:240));}
 @Override public void startSeenByPlayer(ServerPlayerEntity p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayerEntity p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override public void onRemovedFromWorld(){bar.removeAllPlayers();super.onRemovedFromWorld();}
 @Override public boolean removeWhenFarAway(double distance){return false;}
 @Override public boolean canChangeDimensions(){return owner==null&&super.canChangeDimensions();}
 @Override public boolean fireImmune(){return oven()||super.fireImmune();}
 @Override public void checkDespawn(){if(!level.isClientSide&&level.getDifficulty()==Difficulty.PEACEFUL&&owner!=null)HuntRecords.get((ServerWorld)level).finish(owner,getUUID(),HuntRecords.Status.FAILED);super.checkDespawn();}
 public boolean wetRain(){return level.isRainingAt(blockPosition().above());}
 @Override protected void playStepSound(BlockPos p,net.minecraft.block.BlockState b){playSound(oven()?SoundEvents.WOOD_STEP:SoundEvents.WOLF_STEP,.35F,.65F);}
 @Override protected SoundEvent getAmbientSound(){return oven()?super.getAmbientSound():random.nextBoolean()?SoundEvents.WOLF_GROWL:SoundEvents.WOLF_PANT;}
 @Override protected SoundEvent getHurtSound(DamageSource source){return oven()?super.getHurtSound(source):SoundEvents.WOLF_HURT;}
 @Override protected SoundEvent getDeathSound(){return oven()?super.getDeathSound():SoundEvents.WOLF_DEATH;}
 @Override protected float getVoicePitch(){return .65F;}
 @Override public boolean hurt(DamageSource source,float amount){if(oven()&&source.isFire())return false;return super.hurt(source,amount);}
 private void cue(SoundEvent sound){playSound(sound,.85F,.65F);}
 @Override public void tick(){super.tick();if(level.isClientSide)return;if(home==null){home=blockPosition();setPersistenceRequired();}bar.setVisible(showsHuntBar()&&getTarget()!=null);bar.setPercent(getHealth()/getMaxHealth());
  if(owner!=null&&isAlive()){HuntRecords.Hunt h=HuntRecords.get((ServerWorld)level).hunts.get(owner);if(h==null||h.status!=HuntRecords.Status.ACTIVE||!h.target.equals(getUUID())){remove();return;}}
  if(miniBoss()&&!(this instanceof ElementHuntMob)&&isAlive()&&!rage()&&getHealth()<=getMaxHealth()*.5){entityData.set(RAGE,true);cue(SoundEvents.RAVAGER_ROAR);}
  if(oven()&&isInWater()&&tickCount%20==0)hurt(DamageSource.DROWN,2);
  if(tickCount%5==0){prey.entrySet().removeIf(e->e.getValue()<=level.getGameTime());trail.removeIf(t->t.until<=level.getGameTime());trailHits.entrySet().removeIf(e->e.getValue()+40<level.getGameTime());
   for(Trail t:trail){((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,t.pos.x,t.pos.y+.1,t.pos.z,1,.1,0,.1,.01);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,new AxisAlignedBB(t.pos.x-.6,t.pos.y-.2,t.pos.z-.6,t.pos.x+.6,t.pos.y+.5,t.pos.z+.6)))if(!p.isCreative()&&!p.isSpectator()&&level.getGameTime()>=trailHits.getOrDefault(p.getUUID(),0L)&&clearLine(t.pos.add(0,.2,0),p.position().add(0,.2,0))){p.hurt(new EntityDamageSource("ovinnik_trail",this).setIsFire(),wetRain()?1.7F:2);trailHits.put(p.getUUID(),level.getGameTime()+20);}}
  }
 }
 private boolean ready(Move m){return timers.getOrDefault(m,0)<=0;}
 private void begin(Move move,LivingEntity target){entityData.set(ACTION,move.ordinal());entityData.set(START,(int)level.getGameTime());actionTick=0;alignWait=0;dashHit=false;navigation.stop();aim=target.position().subtract(position()).multiply(1,0,1).normalize();timers.put(move,HuntRules.cooldown(move.cooldown,rage()&&(oven()||move==Move.POUNCE)));if(move==Move.DODGE){Vector3d side=new Vector3d(-aim.z,0,aim.x).scale(random.nextBoolean()?2.5:-2.5);sidePoint=position().add(side);}
  cue(move==Move.HOWL?SoundEvents.RAVAGER_ROAR:move==Move.ASH?SoundEvents.FIRE_EXTINGUISH:oven()?SoundEvents.BLAZE_AMBIENT:SoundEvents.WOLF_GROWL);
 }
 public boolean clearLine(Vector3d a,Vector3d b){return level.clip(new RayTraceContext(a,b,RayTraceContext.BlockMode.COLLIDER,RayTraceContext.FluidMode.NONE,this)).getType()==RayTraceResult.Type.MISS;}
 private boolean strike(LivingEntity target,float damage,double reach,double cone,float knockback){if(!target.isAlive()||distanceTo(target)>reach||!getSensing().canSee(target)||aim.dot(target.position().subtract(position()).multiply(1,0,1).normalize())<cone)return false;
  // Six-tick combo spacing needs its own follow-up hits to pass vanilla's ten-tick damage window.
  if(action()==Move.COMBO&&actionTick>Move.COMBO.windup&&target.getLastHurtByMob()==this)target.invulnerableTime=Math.min(target.invulnerableTime,10);
  boolean hit=target.hurt(DamageSource.mobAttack(this),damage);if(knockback>0){Vector3d d=target.position().subtract(position()).normalize();target.knockback(knockback,-d.x,-d.z);}return hit;}
 private Move choose(LivingEntity t,boolean sight){double d=distanceTo(t);if(!sight)return Move.IDLE;
  if(oven()){if(d<=2.7&&basicWait==0)return Move.MELEE;if(d>=2&&d<=5&&ready(Move.SLAM))return Move.SLAM;if(d<=6&&ready(Move.ASH))return Move.ASH;if(!isInWater()&&d>=7&&d<=14&&ready(Move.TRAIL))return Move.TRAIL;if((d>10||unreachable>60)&&ready(Move.EMBER))return Move.EMBER;}
  else{if(d<3&&ready(Move.COMBO))return Move.COMBO;if(d>=5&&d<=10&&ready(Move.POUNCE))return Move.POUNCE;if(d<12&&ready(Move.HOWL))return Move.HOWL;Vector3d toMe=position().subtract(t.position()).normalize();if(d>=3&&d<=7&&ready(Move.DODGE)&&t.getLookAngle().dot(toMe)>.8&&random.nextInt(12)==0)return Move.DODGE;if(d<=2.4&&basicWait==0)return Move.CLAW;}return Move.IDLE;
 }
 private void release(LivingEntity target){Move m=action();switch(m){
  case MELEE:case CLAW:strike(target,oven()?10:9,oven()?2.7:2.4,-.1,0);basicWait=(oven()?(rage()?18:22):(rage()?16:18))-m.windup-1;break;
  case SLAM:particles(ParticleTypes.SMOKE,18);cue(SoundEvents.GENERIC_EXPLODE);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(4)))if(distanceTo(p)<=4&&getSensing().canSee(p)){boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.4;if(!shield)p.hurt(DamageSource.mobAttack(this),8);else p.hurt(new EntityDamageSource("ovinnik_slam",this){@Override public Vector3d getSourcePosition(){return null;}},3);Vector3d d=p.position().subtract(position()).normalize();p.knockback(shield?.35F:.7F,-d.x,-d.z);}break;
  case ASH:particles(ParticleTypes.SMOKE,18);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(6)))if(distanceTo(p)<=6&&getSensing().canSee(p)&&aim.dot(p.position().subtract(position()).multiply(1,0,1).normalize())>=.819){p.hurt(new DamageSource("ovinnik_ash"),4);p.addEffect(new EffectInstance(Effects.WEAKNESS,80));}break;
  case EMBER:EmberClump e=new EmberClump(ModEntities.EMBER_CLUMP.get(),level);e.setOwner(this);e.setPos(getX(),getEyeY()-.2,getZ());Vector3d delta=target.position().add(0,1,0).subtract(e.position());e.shoot(delta.x,delta.y,delta.z,.9F,1);level.addFreshEntity(e);break;
  case POUNCE:setDeltaMovement(aim.scale(.85).add(0,.42,0));hasImpulse=true;break;
  case COMBO:strike(target,4,2.7,-.1,0);break;
  case HOWL:for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(12)))if(distanceTo(p)<=12&&getSensing().canSee(p)&&!p.isCreative()&&!p.isSpectator()){p.addEffect(new EffectInstance(Effects.WEAKNESS,80));prey.put(p.getUUID(),level.getGameTime()+120);}break;
  default:break;
 }}
 private final class Fight extends Goal{
  Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){return getTarget()!=null&&getTarget().isAlive()||home!=null&&distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>40*40;}
  @Override public void stop(){navigation.stop();entityData.set(ACTION,0);}
  @Override public void tick(){LivingEntity t=getTarget();if(t!=null&&(!t.isAlive()||t instanceof PlayerEntity&&(((PlayerEntity)t).isCreative()||t.isSpectator()))){setTarget(null);t=null;}
   if(home!=null&&((distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>56*56&&(t==null||distanceTo(t)>16))||(t!=null&&distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>40*40&&t.distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>56*56))){entityData.set(ACTION,0);if(tickCount%10==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}if(t==null){if(home!=null&&tickCount%10==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}
   if(!oven()||!wetRain()||tickCount%5!=0)for(Move m:new ArrayList<>(timers.keySet()))timers.put(m,Math.max(0,timers.get(m)-1));basicWait=Math.max(0,basicWait-1);
   if(alignRecovery>0){alignRecovery--;navigation.stop();return;}boolean sight=getSensing().canSee(t);if(sight){lastSeen=t.blockPosition();memory=oven()?200:240;}else if(--memory<=0){setTarget(null);return;}
   getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(HuntRules.nightSpeed(oven(),rage(),!level.isDay(),level.getMoonPhase()==0,prey.containsKey(t.getUUID())));
   if(action()!=Move.IDLE){actionTick++;Move m=action();if(actionTick<=m.windup||(m!=Move.TRAIL&&m!=Move.DODGE&&!(m==Move.COMBO&&actionTick>m.windup+13)))navigation.stop();if((m==Move.POUNCE||m==Move.TRAIL)&&actionTick<=m.windup){aim=t.position().subtract(position()).multiply(1,0,1).normalize();boolean facing=DashFacing.turn(HuntMob.this,aim,m==Move.POUNCE?25:18);if(actionTick==m.windup&&!facing){if(++alignWait<=20){actionTick--;return;}entityData.set(ACTION,0);alignRecovery=10;return;}if(actionTick==m.windup)DashFacing.lock(HuntMob.this,aim);}if(actionTick==m.windup)release(t);
    if(m==Move.POUNCE&&actionTick>=m.windup&&actionTick<m.windup+10&&!dashHit&&distanceTo(t)<2.6&&getSensing().canSee(t)){strike(t,8,2.6,.2,.5F);dashHit=true;}
    if(m==Move.TRAIL&&actionTick>m.windup&&!isInWater()){if(actionTick==m.windup+1||actionTick%10==0)navigation.moveTo(getX()+aim.x*10,getY(),getZ()+aim.z*10,1.55);if(actionTick%4==0){trail.add(new Trail(position(),level.getGameTime()+40));particles(ParticleTypes.FLAME,2);}}
    if(m==Move.COMBO&&(actionTick==m.windup+6||actionTick==m.windup+12))strike(t,actionTick==m.windup+12?7:4,2.7,-.1,actionTick==m.windup+12?.7F:0);
    if(m==Move.COMBO&&actionTick==m.windup+14)navigation.moveTo(getX()-aim.x*2.5,getY(),getZ()-aim.z*2.5,1.15);
    if(m==Move.DODGE&&sidePoint!=null&&actionTick==2)navigation.moveTo(sidePoint.x,sidePoint.y,sidePoint.z,1.4);
    if(actionTick>=m.windup+m.recovery){entityData.set(ACTION,0);navigation.stop();}return;
   }
   lookControl.setLookAt(t,30,30);Move selected=choose(t,sight);if(selected!=Move.IDLE){begin(selected,t);return;}
   if(tickCount%10==0){Vector3d dest=lastSeen==null?t.position():new Vector3d(lastSeen.getX()+.5,lastSeen.getY(),lastSeen.getZ()+.5);if(!oven()&&sight&&distanceTo(t)<7){Vector3d flank=new Vector3d(-aim.z,0,aim.x);if(flank.lengthSqr()<.1)flank=new Vector3d(-(t.getZ()-getZ()),0,t.getX()-getX()).normalize();dest=dest.add(flank.scale(Math.sin(tickCount*.07)*2));}boolean path=navigation.moveTo(dest.x,dest.y,dest.z,1);unreachable=path?0:unreachable+10;}
  }
 }
 @Override public void die(DamageSource source){boolean before=dead;super.die(source);if(!before&&dead&&!level.isClientSide){cue(oven()?SoundEvents.WOOD_BREAK:SoundEvents.GENERIC_BIG_FALL);if(oven())cue(SoundEvents.FIRE_EXTINGUISH);if(owner!=null)HuntRecords.get((ServerWorld)level).finish(owner,getUUID(),HuntRecords.Status.CLEARED);if(miniBoss()&&!(this instanceof ElementHuntMob)&&source.getEntity() instanceof PlayerEntity&&(oven()||org.slavicmyths.item.SilverCombat.silverStrike(source)))org.slavicmyths.item.FolkAccessoryItem.award((LivingEntity)source.getEntity(),oven()?"extinguish_the_barn":"silver_remedy");}}
 @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("HuntOwner",owner);n.putBoolean("HuntRage",rage());CompoundNBT cd=new CompoundNBT();for(Map.Entry<Move,Integer> e:timers.entrySet())cd.putInt(e.getKey().name(),e.getValue());n.put("HuntTimers",cd);}
 @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);owner=n.hasUUID("HuntOwner")?n.getUUID("HuntOwner"):null;entityData.set(RAGE,n.getBoolean("HuntRage")||n.getBoolean("Enraged"));CompoundNBT cd=n.getCompound("HuntTimers");for(Move m:Move.values())timers.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));entityData.set(ACTION,0);
  if(!miniBoss())return;
  // Saved 0.8.x attributes must not silently retain the old 140-HP Ovinnik profile.
  getAttribute(Attributes.MAX_HEALTH).setBaseValue(oven()?175:165);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(oven()?10:9);getAttribute(Attributes.ARMOR).setBaseValue(oven()?10:7);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(oven()?2:0);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(oven()?.55:.25);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(oven()?32:36);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(HuntRules.nightSpeed(oven(),rage(),!level.isDay(),level.getMoonPhase()==0,false));
 }
}
