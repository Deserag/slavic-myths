package org.slavicmyths.icequeen;
import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.morozko.*;

public final class IceQueen extends net.minecraft.world.entity.monster.Monster {
 public static final int IDLE=0,SHOT=1,VOLLEY=2,SUMMON=3,STORM=4,THRUST=5,SWEEP=6,SPIN=7,COMBO=8,GUARD=9,TRANSFORM=10,YIELD=11;
 private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.INT), ACTION=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.INT), FRAME=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.INT), SHIELD=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.INT), REACTIVE=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> ACTIVE=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Float> IMPACT_YAW=SynchedEntityData.defineId(IceQueen.class,EntityDataSerializers.FLOAT);
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.ice_queen","Ледяная королева"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_20);
 private long readyAt,nextAttack,nextSummon,nextStorm,nextGuard,nextCopy,stormUntil;
 private final Set<UUID> children=new HashSet<>(), participants=new HashSet<>();
 private Vec3 anchor; private int absent;
 private boolean inTrial;
 public IceQueen(EntityType<? extends IceQueen> type,Level level){super(type,level);setPersistenceRequired();setHealth(getMaxHealth());xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,QueenRules.HP).add(Attributes.ATTACK_DAMAGE,36).add(Attributes.ARMOR,10).add(Attributes.ARMOR_TOUGHNESS,4).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.FOLLOW_RANGE,64);}
 @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PHASE,1);b.define(ACTION,IDLE);b.define(FRAME,0);b.define(SHIELD,0);b.define(REACTIVE,0);b.define(ACTIVE,false);b.define(IMPACT_YAW,0F);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
 public int phase(){return entityData.get(PHASE);} public int action(){return entityData.get(ACTION);} public int frame(){return entityData.get(FRAME);} public int shields(){return entityData.get(SHIELD);} public int reactive(){return entityData.get(REACTIVE);} public float impactYaw(){return entityData.get(IMPACT_YAW);} public boolean active(){return entityData.get(ACTIVE);}
 @Override public EntityDimensions getDimensions(Pose pose){return super.getDimensions(pose).scale(entityData!=null&&phase()==2?1.48F:1F,entityData!=null&&phase()==2?1.58F:1F);}
 @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(PHASE.equals(key))refreshDimensions();}
 private void phase(int p){entityData.set(PHASE,p);getAttribute(Attributes.ARMOR).setBaseValue(p==2?18:10);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(p==2?6:4);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(p==2?1:.8);refreshDimensions();}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean isAlliedTo(Entity e){return e instanceof QueenMinion m&&m.ownedBy(this)||super.isAlliedTo(e);}
 public boolean valid(Player p){return p!=null&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&p.level()==level()&&anchor!=null&&p.position().distanceToSqr(anchor)<=QueenRules.ARENA_RADIUS*QueenRules.ARENA_RADIUS;}
 public boolean participant(Player p){return valid(p)&&participants.contains(p.getUUID());}
 public Player victim(){return getTarget() instanceof Player p&&participant(p)?p:null;}
 @Override protected InteractionResult mobInteract(Player p,InteractionHand h){
  if(level().isClientSide)return InteractionResult.SUCCESS;
  if(!(p instanceof ServerPlayer)||p.isSpectator()||p.isCreative())return InteractionResult.PASS;
  if(active()){if(valid(p))participants.add(p.getUUID());return InteractionResult.CONSUME;}
  long now=System.currentTimeMillis();
  if(!QueenRules.ready(now,readyAt)){p.displayClientMessage(Component.translatableWithFallback("message.slavicmyths.ice_queen.cooldown","Новое испытание доступно через %s мин.",Math.max(1,(readyAt-now+59999)/60000)),true);return InteractionResult.CONSUME;}
  if(action()==YIELD)return InteractionResult.CONSUME;
  anchor=position();participants.clear();participants.add(p.getUUID());inTrial=true;entityData.set(ACTIVE,true);phase(1);setHealth(getMaxHealth());setTarget(p);
  long t=level().getGameTime();nextAttack=t+30;nextSummon=t;nextStorm=t+200;nextGuard=t;nextCopy=t;absent=0;
  p.sendSystemMessage(Component.translatableWithFallback("message.slavicmyths.ice_queen.start","Докажи свою силу. Испытание началось!"));playSound(SoundEvents.AMETHYST_BLOCK_CHIME,2,.55F);return InteractionResult.CONSUME;
 }
 private void begin(int a){entityData.set(ACTION,a);entityData.set(FRAME,0);getNavigation().stop();
  switch(a){
   case SHOT,VOLLEY -> {playSound(SoundEvents.AMETHYST_BLOCK_CHIME,1.5F,a==VOLLEY?.7F:1.1F);FrostContent.sound(this,"spike_telegraph");}
   case SUMMON -> {playSound(SoundEvents.EVOKER_PREPARE_SUMMON,1.5F,.7F);FrostContent.sound(this,"clone_spawn");}
   case STORM -> {playSound(SoundEvents.POWDER_SNOW_BREAK,2,.5F);FrostContent.sound(this,"blizzard_start");}
   case THRUST,SWEEP,SPIN,COMBO -> playSound(SoundEvents.PLAYER_ATTACK_SWEEP,1.4F,.65F);
   case GUARD,TRANSFORM -> playSound(SoundEvents.GLASS_PLACE,2,.6F);
   case YIELD -> {playSound(SoundEvents.AMETHYST_BLOCK_CHIME,2,.8F);FrostContent.sound(this,"yield");}
  }}
 @Override public void setHealth(float hp){super.setHealth(inTrial?Math.max(QueenRules.VICTORY,hp):hp);}
 @Override public boolean hurt(DamageSource source,float amount){
  if(!(level() instanceof ServerLevel w)||!active()||amount<=0||source.is(DamageTypeTags.IS_FREEZING)||source.getEntity() instanceof QueenMinion)return false;
  if(!(source.getEntity() instanceof Player p)||!valid(p))return false;
  participants.add(p.getUUID());
  if(action()==TRANSFORM)return false;
  if(shields()>0){entityData.set(SHIELD,shields()-1);playSound(SoundEvents.GLASS_BREAK,1.7F,1.1F);FrostVisuals.burst(w,getEyePosition(),16);return false;}
  if(phase()==2&&(source.getDirectEntity() instanceof Projectile||source.is(DamageTypeTags.IS_PROJECTILE))&&random.nextFloat()<QueenRules.PROJECTILE_BLOCK){
   Vec3 from=source.getSourcePosition();if(from==null)from=p.position();Vec3 d=from.subtract(position());
   entityData.set(IMPACT_YAW,(float)Math.toDegrees(Math.atan2(-d.x,d.z)));entityData.set(REACTIVE,8);playSound(SoundEvents.SHIELD_BLOCK,1.6F,1.4F);FrostVisuals.burst(w,getEyePosition(),8);return false;
  }
  boolean hit=super.hurt(source,amount);
  if(getHealth()<=QueenRules.VICTORY&&phase()==2){win(w);return true;}
  if(getHealth()<=QueenRules.PHASE_TWO&&phase()==1){setHealth(QueenRules.PHASE_TWO);cleanup(w);phase(2);begin(TRANSFORM);nextCopy=w.getGameTime()+65;nextGuard=w.getGameTime()+100;nextAttack=w.getGameTime()+65;}
  return hit;
 }
 private void cleanup(ServerLevel w){for(UUID id:children){Entity e=w.getEntity(id);if(e instanceof QueenMinion m&&m.ownedBy(this)){FrostVisuals.burst(w,e.position().add(0,.8,0),12);e.discard();}}children.clear();entityData.set(SHIELD,0);entityData.set(REACTIVE,0);stormUntil=0;}
 private void win(ServerLevel w){if(!active())return;setHealth(QueenRules.VICTORY);inTrial=false;entityData.set(ACTIVE,false);readyAt=QueenRules.nextTrial(System.currentTimeMillis());cleanup(w);setTarget(null);begin(YIELD);for(UUID id:participants){var p=w.getServer().getPlayerList().getPlayer(id);if(p!=null){FrostCold.stop(p);p.sendSystemMessage(Component.translatableWithFallback("message.slavicmyths.ice_queen.victory","Ты доказал свою силу. Испытание завершено."));}}participants.clear();}
 private void abort(ServerLevel w){cleanup(w);inTrial=false;entityData.set(ACTIVE,false);phase(1);setHealth(getMaxHealth());setTarget(null);begin(IDLE);participants.clear();}
 private List<ServerPlayer> players(ServerLevel w){return w.getPlayers(this::participant);}
 private void face(Player p){getLookControl().setLookAt(p,45,45);Vec3 d=p.position().subtract(position());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));setYRot(yaw);setYBodyRot(yaw);setYHeadRot(yaw);}
 private void launch(Player p,int count){Vec3 aim=p.getEyePosition();Vec3 side=p.position().subtract(position()).multiply(1,0,1).normalize().yRot((float)Math.PI/2);
  for(int i=0;i<count;i++)FrostShard.launch(this,aim.add(side.scale((i-(count-1)/2F)*1.8)),count==1?(phase()==1?14:20):(phase()==1?10:14),phase()==1?8:12,null);
  playSound(SoundEvents.SNOWBALL_THROW,2,.55F);FrostContent.sound(this,"ice_bolt");FrostVisuals.burst((ServerLevel)level(),getEyePosition(),12);
 }
 private void melee(ServerLevel w,float damage,double reach,double angle){for(ServerPlayer p:players(w)){if(FrostWorld.cone(this,p,reach,angle)&&p.hurt(damageSources().mobAttack(this),damage))FrostCold.add(p,12);}playSound(SoundEvents.PLAYER_ATTACK_SWEEP,1.7F,.7F);FrostContent.sound(this,"staff_hit");FrostVisuals.ring(w,position(),reach,.7,18);}
 private void summon(ServerLevel w,boolean copies){
  children.removeIf(id->!(w.getEntity(id) instanceof QueenMinion m)||!m.isAlive());
  int cap=copies?QueenRules.MAX_COPIES:QueenRules.MAX_MINIONS;
  QueenContent.Kind kind=copies?QueenContent.Kind.MELEE_COPY:QueenContent.Kind.values()[QueenRules.summonKind(random.nextInt(100))];
  if(kind==QueenContent.Kind.LARGE_GOLEM&&children.stream().anyMatch(id->w.getEntity(id) instanceof QueenMinion m&&m.kind()==kind))return;
  int count=copies?2+random.nextInt(2):kind==QueenContent.Kind.LARGE_GOLEM?1:2;
  for(int i=0;i<count&&children.size()<cap;i++){
   QueenContent.Kind k=copies?(i%2==0?QueenContent.Kind.MELEE_COPY:QueenContent.Kind.RANGED_COPY):kind;
   double a=random.nextDouble()*Math.PI*2;Vec3 at=FrostWorld.safe(w,position().add(Math.cos(a)*4,0,Math.sin(a)*4),k.width,k.height,4);if(at==null)continue;
   QueenMinion m=QueenContent.MINIONS.get(k).get().create(w);if(m==null)continue;m.bind(getUUID());m.moveTo(at);if(w.addFreshEntity(m)){children.add(m.getUUID());FrostVisuals.burst(w,at.add(0,1,0),24);}
  }playSound(SoundEvents.EVOKER_CAST_SPELL,1.8F,.8F);
 }
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;long now=w.getGameTime();bar.setProgress(getHealth()/getMaxHealth());if(reactive()>0)entityData.set(REACTIVE,reactive()-1);
  if(!active()){if(tickCount%240==0)FrostContent.sound(this,"ambient");getNavigation().stop();if(action()==YIELD){entityData.set(FRAME,frame()+1);if(frame()>=50){phase(1);begin(IDLE);}}return;}
  if(anchor==null||position().distanceToSqr(anchor)>QueenRules.ARENA_RADIUS*QueenRules.ARENA_RADIUS){abort(w);return;}
  Player p=victim();if(p==null){p=players(w).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(p);}
  if(p==null){if(++absent>=200)abort(w);return;}absent=0;face(p);
  if(stormUntil>now&&phase()==1&&tickCount%10==0){for(ServerPlayer v:players(w)){w.sendParticles(ParticleTypes.SNOWFLAKE,v.getX(),v.getY()+3,v.getZ(),45,6,4,6,.1);v.addEffect(new MobEffectInstance(MobEffects.DARKNESS,40,0,false,false));FrostCold.add(v,1.5);}if(tickCount%80==0)FrostContent.sound(this,"blizzard_loop");}
  if(action()!=IDLE){
   int f=frame()+1;entityData.set(FRAME,f);int a=action();
   if(f==12){switch(a){case SHOT->launch(p,1);case VOLLEY->launch(p,phase()==1?5:7);case SUMMON->summon(w,phase()==2);case STORM->{stormUntil=now+1000;FrostVisuals.ring(w,position(),6,.1,32);}case GUARD->{entityData.set(SHIELD,QueenRules.SHIELDS);playSound(SoundEvents.GLASS_PLACE,1.5F,.75F);}case THRUST->melee(w,36,5,30);case SWEEP->melee(w,30,5.5,95);case SPIN->melee(w,32,5.5,180);case COMBO->melee(w,22,5,45);}}
   if(a==COMBO&&(f==22||f==32))melee(w,22,5,45);
   if(a==TRANSFORM&&f%5==0)FrostVisuals.burst(w,getEyePosition(),20);
   if(f>=(a==TRANSFORM?55:a==COMBO?40:25))begin(IDLE);
   return;
  }
  if(phase()==1&&now>=nextSummon){nextSummon=now+QueenRules.SUMMON_INTERVAL;begin(SUMMON);return;}
  if(phase()==1&&now>=nextStorm){nextStorm=now+1800;begin(STORM);return;}
  if(phase()==2&&now>=nextGuard&&shields()==0){nextGuard=now+600;begin(GUARD);return;}
  if(phase()==2&&now>=nextCopy){nextCopy=now+900;begin(SUMMON);return;}
  if(now>=nextAttack){double d=distanceToSqr(p);int a=phase()==2&&d<=25?new int[]{THRUST,SWEEP,SPIN,COMBO}[random.nextInt(4)]:random.nextInt(3)==0?VOLLEY:SHOT;
   nextAttack=now+switch(a){case THRUST->50;case SWEEP->100;case SPIN->160;case COMBO->240;case VOLLEY->140;default->60;};begin(a);return;}
  if(phase()==2&&distanceToSqr(p)>16)getNavigation().moveTo(p,1.1);else if(phase()==1&&distanceToSqr(p)<25){Vec3 away=position().subtract(p.position()).multiply(1,0,1).normalize().scale(4);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,1);}else getNavigation().stop();
 }
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putLong("QueenReadyAt",readyAt);n.putBoolean("QueenInterrupted",active());}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);readyAt=n.getLong("QueenReadyAt");inTrial=false;entityData.set(ACTIVE,false);phase(1);if(n.getBoolean("QueenInterrupted"))setHealth(getMaxHealth());}
}
