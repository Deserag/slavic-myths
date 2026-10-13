package org.slavicmyths.volkolak;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;

/** Independent registry entity: the old hunt Volkolak is deliberately preserved. */
public final class CursedVolkolak extends Monster {
 public enum Move {IDLE(0,0),ALERT(16,0),RISE(16,0),DROP(12,0),CLAW_L(18,18),CLAW_R(20,18),DOUBLE(30,52),TRIPLE(40,85),BITE(21,25),POUNCE(36,90),FLANK(28,55),GRAB(60,260),STAGGER(20,0),ROAR(26,0),SNIFF(18,0),LISTEN(24,0),SNARL(25,0),HOWL_SHORT(42,0),ESCAPE(131,0);
  public final int length,cooldown;Move(int length,int cooldown){this.length=length;this.cooldown=cooldown;}}
 private static final EntityDataAccessor<Integer> MOVE=SynchedEntityData.defineId(CursedVolkolak.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(CursedVolkolak.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(CursedVolkolak.class,EntityDataSerializers.INT),HELD=SynchedEntityData.defineId(CursedVolkolak.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> UPRIGHT=SynchedEntityData.defineId(CursedVolkolak.class,EntityDataSerializers.BOOLEAN);
 private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
 private final ServerBossEvent bossbar=new ServerBossEvent(Component.translatable("entity.slavicmyths.cursed_volkolak"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 public UUID encounter;public BlockPos home;
 private UUID lockedTarget,alertedTarget;private Vec3 aim=Vec3.ZERO,flank=Vec3.ZERO,launchOrigin=Vec3.ZERO;private double launchRange;private int hitMask;private int heldTicks,cycles,outside,unseen,recovery,stanceLockedUntil;private boolean connected,escapeCommitted;private long nextAmbient,nextHurt,nextIdle;
 public CursedVolkolak(EntityType<? extends CursedVolkolak> type,Level level){super(type,level);xpReward=0;setPersistenceRequired();}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,400).add(Attributes.ATTACK_DAMAGE,15).add(Attributes.MOVEMENT_SPEED,.42).add(Attributes.ARMOR,7).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.6).add(Attributes.STEP_HEIGHT,1);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MOVE,0);b.define(START,0);b.define(PHASE,1);b.define(HELD,-1);b.define(UPRIGHT,false);}
 public Move move(){return Move.values()[Math.clamp(entityData.get(MOVE),0,Move.values().length-1)];}
 public float animationTicks(float partial){return Math.max(0,(int)level().getGameTime()-entityData.get(START)+partial);}
 public int phase(){return entityData.get(PHASE);}public boolean upright(){return entityData.get(UPRIGHT);}public int heldEntity(){return entityData.get(HELD);}
 @Override protected EntityDimensions getDefaultDimensions(Pose pose){return EntityDimensions.scalable(1.65F,upright()?2.8F:1.9F).withEyeHeight(upright()?2.35F:1.55F);}
 @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key.equals(UPRIGHT))refreshDimensions();}
 private boolean canStand(){return !level().getBlockCollisions(this,new AABB(getX()-.825,getY(),getZ()-.825,getX()+.825,getY()+2.9,getZ()+.825)).iterator().hasNext();}
 private void stance(boolean value){if(value&&!canStand())return;entityData.set(UPRIGHT,value);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.42*(value?.85:1)*(phase()==3?1.12:1));}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(4,new RandomStrollGoal(this,.6));goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,12));goalSelector.addGoal(6,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,1,true,false,p->p instanceof Player player&&!player.isCreative()&&!p.isSpectator()&&distanceToSqr(p)<=400));}
 @Override public boolean doHurtTarget(Entity target){return false;}
 @Override public boolean removeWhenFarAway(double distance){return false;}
 @Override public boolean canChangeDimensions(Level from,Level to){return encounter==null&&super.canChangeDimensions(from,to);}
 @Override protected SoundEvent getAmbientSound(){return null;}
 @Override protected SoundEvent getHurtSound(DamageSource source){return null;}
 @Override protected SoundEvent getDeathSound(){return VolkolakSounds.get("fake_fall");}
 public void cue(String name,float volume){playSound(VolkolakSounds.get(name),volume,1+(random.nextFloat()-.5F)*.1F);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bossbar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bossbar.removePlayer(p);}
 @Override public void onRemovedFromLevel(){release();bossbar.removeAllPlayers();super.onRemovedFromLevel();}
 @Override public void die(DamageSource source){release();super.die(source);}
 private boolean valid(LivingEntity target){return target!=null&&target.isAlive()&&target.level()==level()&&(!(target instanceof Player p)||!p.isCreative()&&!p.isSpectator())&&!isAlliedTo(target);}
 private LivingEntity locked(){return lockedTarget!=null&&level() instanceof ServerLevel w&&w.getEntity(lockedTarget) instanceof LivingEntity e?e:null;}
 private void begin(Move m,LivingEntity target){release();navigation.stop();entityData.set(MOVE,m.ordinal());entityData.set(START,(int)level().getGameTime());lockedTarget=target==null?null:target.getUUID();connected=false;hitMask=0;heldTicks=0;getPersistentData().remove("VolkolakLandCue");if(target!=null)aim=target.position().subtract(position()).multiply(1,0,1).normalize();cooldowns.put(m,m.cooldown);if(m==Move.CLAW_L||m==Move.CLAW_R){cooldowns.put(Move.CLAW_L,18);cooldowns.put(Move.CLAW_R,18);}if(m==Move.FLANK)flank=position().add(new Vec3(-aim.z,0,aim.x).scale(random.nextBoolean()?3.5:-3.5));if(m==Move.GRAB)cue("grab_prepare",.85F);if(m==Move.ALERT)cue("alert",.7F);if(m==Move.ESCAPE)cue("fake_fall",.8F);if(m!=Move.RISE&&m!=Move.DROP&&m!=Move.ALERT&&m!=Move.ROAR&&m!=Move.STAGGER)cycles++;}
 private void finish(){release();entityData.set(MOVE,0);entityData.set(START,(int)level().getGameTime());lockedTarget=null;navigation.stop();}
 private void turn(LivingEntity target){Vec3 d=target.position().subtract(position());float desired=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;float yaw=Mth.approachDegrees(getYRot(),desired,22);setYRot(yaw);setYHeadRot(yaw);yBodyRot=yaw;}
 private boolean shield(LivingEntity target){return target.isDamageSourceBlocked(damageSources().mobAttack(this));}
 private boolean strike(LivingEntity target,float damage,double reach,double degrees,boolean bleed){if(!valid(target)||distanceTo(target)>reach||!hasLineOfSight(target))return false;Vec3 d=target.position().subtract(position()).multiply(1,0,1).normalize();if(aim.dot(d)<Math.cos(Math.toRadians(degrees*.5)))return false;float before=target.getHealth()+target.getAbsorptionAmount();boolean result=target.hurt(damageSources().mobAttack(this),Math.max(0,damage+(float)getAttributeValue(Attributes.ATTACK_DAMAGE)-15+(phase()==3?2:0)));boolean actual=result&&target.getHealth()+target.getAbsorptionAmount()<before;if(actual&&bleed&&target instanceof ServerPlayer p)org.slavicmyths.wolf.WolfBleeding.apply(this,p);return actual;}
 private boolean dashRoom(){return !horizontalCollision&&level().noCollision(this,getBoundingBox().move(aim.scale(.45)));}
 private void launch(double horizontal,double vertical){if(dashRoom()){setDeltaMovement(aim.scale(horizontal).add(0,vertical,0));hasImpulse=true;}}
 private Move choose(LivingEntity t){
  double d=distanceTo(t);
  if(d<=3.1){
   if(upright()&&phase()==3&&ready(Move.TRIPLE)&&cycles%3==0)return Move.TRIPLE;
   if(upright()&&ready(Move.GRAB)&&cycles%5==4&&t instanceof ServerPlayer)return Move.GRAB;
   if(upright()&&ready(Move.DOUBLE)&&cycles%3==0)return Move.DOUBLE;
   if(!upright()&&ready(Move.BITE))return Move.BITE;
   if(ready(cycles%2==0?Move.CLAW_L:Move.CLAW_R))return cycles%2==0?Move.CLAW_L:Move.CLAW_R;
   if(ready(Move.BITE))return Move.BITE;
  }
  if(d>3.1&&d<=7&&ready(Move.POUNCE)&&!upright())return Move.POUNCE;
  if(d>3.1&&d<=5&&ready(Move.FLANK))return Move.FLANK;
  return Move.IDLE;
 }
 private void pursue(LivingEntity t){
  if(tickCount%6==0)navigation.moveTo(t,upright()?1.2:1.35);
  // Large ground navigators can finish a path before reaching their target's centre.
  // Continue the last few steps with native MoveControl and collision checks.
  if(navigation.isDone()&&distanceTo(t)>2.2&&distanceTo(t)<6&&!horizontalCollision){
   Vec3 d=t.position().subtract(position()).multiply(1,0,1).normalize();
   if(level().noCollision(this,getBoundingBox().move(d.scale(.25))))getMoveControl().setWantedPosition(t.getX(),t.getY(),t.getZ(),upright()?1.2:1.35);
  }
 }
 private boolean ready(Move m){return cooldowns.getOrDefault(m,0)==0;}
 private Vec3 heldFeet(){return position().add(aim.scale(.7)).add(0,.18,0);}
 @Override protected void positionRider(Entity passenger,Entity.MoveFunction move){if(passenger.getId()==heldEntity()){Vec3 anchor=heldFeet();move.accept(passenger,anchor.x,anchor.y,anchor.z);}else super.positionRider(passenger,move);}
 @Override protected boolean canAddPassenger(Entity p){return move()==Move.GRAB&&getPassengers().isEmpty()&&p instanceof ServerPlayer&&lockedTarget!=null&&lockedTarget.equals(p.getUUID());}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){return passenger.position();}
 @Override protected void removePassenger(Entity passenger){super.removePassenger(passenger);var data=passenger.getPersistentData();if(data.hasUUID("VolkolakHolder")&&data.getUUID("VolkolakHolder").equals(getUUID())){data.remove("VolkolakHolder");data.remove("VolkolakHeldUntil");}if(passenger.getId()==heldEntity())entityData.set(HELD,-1);}
 private void capture(LivingEntity t){if(!(t instanceof ServerPlayer p)||!valid(t)||distanceTo(t)>1.65||!hasLineOfSight(t)||!canStand()||p.isPassenger()||p.getPersistentData().getLong("VolkolakHeldUntil")>level().getGameTime())return;
  if(shield(t)){t.hurt(damageSources().mobAttack(this),16);begin(Move.STAGGER,null);return;}
  Vec3 anchor=heldFeet();if(!level().noCollision(p,p.getBoundingBox().move(anchor.subtract(p.position()))))return;stance(true);
  if(p.startRiding(this,true)){entityData.set(HELD,p.getId());positionRider(p);p.getPersistentData().putUUID("VolkolakHolder",getUUID());p.getPersistentData().putLong("VolkolakHeldUntil",level().getGameTime()+20);connected=true;heldTicks=0;p.displayClientMessage(Component.translatable("volkolak.slavicmyths.held"),true);}
 }
 private void release(){release(false);}
 private void release(boolean push){for(var passenger:List.copyOf(getPassengers())){var d=passenger.getPersistentData();if(d.hasUUID("VolkolakHolder")&&d.getUUID("VolkolakHolder").equals(getUUID())){d.remove("VolkolakHolder");d.remove("VolkolakHeldUntil");}passenger.stopRiding();if(push&&(hitMask&1)!=0&&passenger instanceof LivingEntity living&&living.isAlive()&&level().noCollision(living,living.getBoundingBox().move(aim.scale(.35)))){living.knockback(.35,-aim.x,-aim.z);living.hurtMarked=true;}}entityData.set(HELD,-1);heldTicks=0;}
 public void escape(){if(escapeCommitted)return;escapeCommitted=true;setHealth(Math.max(1,getHealth()));setTarget(null);begin(Move.ESCAPE,null);stance(false);}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(home==null)home=blockPosition();cooldowns.replaceAll((m,t)->Math.max(0,t-1));recovery=Math.max(0,recovery-1);if(move()!=Move.GRAB&&!getPassengers().isEmpty())release();
  if(heldEntity()>=0){var t=locked();if(!valid(t)||t.getVehicle()!=this||!canStand())release();else if(++heldTicks>=20)release(true);}
  int next=getHealth()<=100?3:getHealth()<=250?2:1;if(next>phase()&&move()!=Move.ESCAPE){entityData.set(PHASE,next);stance(upright());begin(Move.ROAR,null);}
  bossbar.setVisible(getTarget()!=null&&move()!=Move.ESCAPE);bossbar.setProgress(Mth.clamp(getHealth()/400,0,1));
  if(getTarget()==null&&move()==Move.IDLE&&level().getGameTime()>=nextIdle&&!bossbar.getPlayers().isEmpty()){int choice=random.nextInt(10);begin(choice<5?Move.SNIFF:choice<8?Move.LISTEN:choice==8?Move.SNARL:Move.HOWL_SHORT,null);nextIdle=level().getGameTime()+180+random.nextInt(181);}
  if(getTarget()==null&&move()==Move.IDLE&&level().getGameTime()>=nextAmbient&&!bossbar.getPlayers().isEmpty()){cue("ambient",.45F);nextAmbient=level().getGameTime()+100+random.nextInt(141);}
  if(move()==Move.ESCAPE){int t=(int)animationTicks(0);setTarget(null);if(t<87)navigation.stop();if(t==49)cue("curse_revive",.7F);if(t>=49&&t<87&&t%4==0)((ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,getX(),getY()+.9,getZ(),2,.3,.5,.3,.01);if(t==87){cue("escape_howl",.8F);aim=getLookAngle().multiply(1,0,1).normalize();}if(t>=87&&t%10==7)navigation.moveTo(getX()+aim.x*10,getY(),getZ()+aim.z*10,1.5);if(t>=130){VolkolakStory.escaped(this);discard();}}
 }
 @Override public boolean hurt(DamageSource source,float amount){if(move()==Move.ESCAPE)return false;boolean result=super.hurt(source,amount);if(result&&source.getEntity() instanceof LivingEntity attacker&&valid(attacker))setTarget(attacker);if(result&&level() instanceof ServerLevel w&&w.getGameTime()>=nextHurt){cue("hurt",.65F);nextHurt=w.getGameTime()+12;}return result;}
 private final class Fight extends Goal {
  Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean requiresUpdateEveryTick(){return true;}
  @Override public boolean canUse(){return move()!=Move.IDLE||getTarget()!=null||home!=null&&home.distToCenterSqr(position())>400;}
  @Override public void stop(){if(move()!=Move.ESCAPE)finish();}
  @Override public void tick(){if(move()==Move.ESCAPE)return;LivingEntity t=getTarget();if(!valid(t)){setTarget(null);t=null;}
   if(home!=null&&(home.distToCenterSqr(position())>55*55||t!=null&&home.distToCenterSqr(t.position())>55*55)){outside++;if(move()!=Move.IDLE)finish();if(outside>=400){setTarget(null);finish();}if(tickCount%10==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}outside=0;
   if(t!=null&&!t.getUUID().equals(alertedTarget)){alertedTarget=t.getUUID();cycles=Math.max(1,cycles);begin(Move.ALERT,t);return;}if(t==null)alertedTarget=null;
   if(move()!=Move.IDLE){act();return;}if(t==null){if(upright())begin(Move.DROP,null);else if(home!=null&&tickCount%20==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);return;}
   if(!hasLineOfSight(t)){if(++unseen>=100){setTarget(null);return;}pursue(t);return;}unseen=0;turn(t);if(recovery>0){pursue(t);return;}
   boolean desired=canStand()&&distanceTo(t)<=3.1&&(phase()==1?cycles%8>=4:cycles%8>=2);
   if(tickCount>=stanceLockedUntil&&desired!=upright()){stanceLockedUntil=tickCount+60;begin(desired?Move.RISE:Move.DROP,t);return;}
   Move chosen=choose(t);if(chosen!=Move.IDLE){begin(chosen,t);return;}pursue(t);
  }
 }
 private void act(){int tick=(int)animationTicks(0);Move m=move();LivingEntity t=locked();if(m!=Move.RISE&&m!=Move.DROP&&m!=Move.ROAR&&m!=Move.STAGGER&&m!=Move.SNIFF&&m!=Move.LISTEN&&m!=Move.SNARL&&m!=Move.HOWL_SHORT&&!valid(t)){finish();recovery=15;return;}
  if(m!=Move.FLANK||tick<7)navigation.stop();if(t!=null&&tick<8){turn(t);aim=t.position().subtract(position()).multiply(1,0,1).normalize();}
  if(m==Move.SNARL&&tick==3)cue("snarl",.5F);else if(m==Move.HOWL_SHORT&&tick==12)cue("howl_short",.55F);else if(m==Move.RISE){if(tick==8){stance(true);cue("stance_shift",.6F);}}else if(m==Move.DROP){if(tick==6){stance(false);cue("land",.35F);}}else if(m==Move.ROAR){if(tick==12)cue("phase_roar",.8F);}
  else if(m==Move.CLAW_L||m==Move.CLAW_R){if(tick==3)cue("snarl",.55F);int hit=m==Move.CLAW_L?10:12;if(tick==hit-3)cue("claw_windup",.65F);if(tick>=hit&&(hitMask&1)==0){hitMask|=1;cue("claw_swing",.7F);strike(t,15,3.1,90,true);}}
  else if(m==Move.DOUBLE||m==Move.TRIPLE){if(tick==7||tick==17||m==Move.TRIPLE&&tick==27)cue("claw_windup",.65F);for(int hit=0;hit<(m==Move.TRIPLE?3:2);hit++)if(tick>=10+hit*10&&(hitMask&(1<<hit))==0){hitMask|=1<<hit;cue("claw_swing",.7F);strike(t,hit==2?18:13,3.1,hit==2?100:90,true);}}
  else if(m==Move.BITE&&tick>=13&&(hitMask&1)==0){hitMask|=1;cue("bite",.7F);strike(t,17,3.05,80,true);}
  else if(m==Move.POUNCE){
   if(tick==14){launchOrigin=position();launchRange=Math.max(.5,position().subtract(t.position()).multiply(1,0,1).length()-1);cue("pounce_grunt",.7F);launch(.72,.39);}
   if(tick>14&&tick<=32){if(position().subtract(launchOrigin).multiply(1,0,1).lengthSqr()>=launchRange*launchRange||horizontalCollision)setDeltaMovement(0,getDeltaMovement().y,0);if(tick>=16&&!connected)connected=strike(t,20,2.8,90,true);}
   if(tick>=28&&tick<=32&&onGround()&&!getPersistentData().getBoolean("VolkolakLandCue")){cue("land",.65F);getPersistentData().putBoolean("VolkolakLandCue",true);}
  }
  else if(m==Move.FLANK){if(tick==7)navigation.moveTo(flank.x,flank.y,flank.z,1.3);if(tick>7&&tick<23){turn(t);if(tick%5==0)navigation.moveTo(flank.x,flank.y,flank.z,1.3);}if(tick==24){aim=t.position().subtract(position()).multiply(1,0,1).normalize();cue("bite",.7F);strike(t,16,2.4,55,true);}}
  else if(m==Move.GRAB){if(tick==14)launch(.6,.12);if(tick>=22&&tick<=25&&!connected)capture(t);if(tick==28&&(hitMask&1)==0&&heldEntity()==t.getId()&&t.getVehicle()==this){hitMask|=1;cue("grab_bite",.85F);if(strike(t,24,2.4,100,true)){BadBlood.apply(t,this);cue("bad_blood_apply",.45F);}}if(tick==45)release(true);if(tick==49)cue("grab_release",.6F);if(tick==26&&!connected){finish();recovery=18;return;}}
  if(tick>=m.length){finish();getPersistentData().remove("VolkolakLandCue");if(m==Move.POUNCE&&!connected)recovery=20;}
 }
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(home!=null)tag.putLong("VolkolakHome",home.asLong());if(encounter!=null)tag.putUUID("VolkolakEncounter",encounter);tag.putBoolean("VolkolakEscaped",escapeCommitted);tag.putInt("VolkolakPhase",phase());var cd=new CompoundTag();cooldowns.forEach((m,t)->cd.putInt(m.name(),t));tag.put("VolkolakCooldowns",cd);}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);home=tag.contains("VolkolakHome")?BlockPos.of(tag.getLong("VolkolakHome")):null;encounter=tag.hasUUID("VolkolakEncounter")?tag.getUUID("VolkolakEncounter"):null;escapeCommitted=tag.getBoolean("VolkolakEscaped");entityData.set(PHASE,Math.clamp(tag.getInt("VolkolakPhase"),1,3));for(Move m:Move.values())cooldowns.put(m,Math.clamp(tag.getCompound("VolkolakCooldowns").getInt(m.name()),0,m.cooldown));finish();stance(false);if(escapeCommitted)discard();}
}
