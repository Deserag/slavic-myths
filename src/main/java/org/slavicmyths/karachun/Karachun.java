package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.phys.*;
import org.slavicmyths.morozko.FrostWorld;
public final class Karachun extends PathfinderMob implements Enemy {
 private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(Karachun.class,EntityDataSerializers.INT),ACTION=SynchedEntityData.defineId(Karachun.class,EntityDataSerializers.INT),AGE=SynchedEntityData.defineId(Karachun.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> MAX=SynchedEntityData.defineId(Karachun.class,EntityDataSerializers.FLOAT);
 public UUID session,owner;public Vec3 anchor=Vec3.ZERO;private int absent,stageAge,battleTicks,nightTicks;private final long[] ready=new long[10];private final Set<UUID> birds=new HashSet<>(),affected=new HashSet<>();private Vec3 marker;private int markerAge;private int combo;
 private final ServerBossEvent bar=new ServerBossEvent(NightContent.text("boss"),net.minecraft.world.BossEvent.BossBarColor.PURPLE,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
 public Karachun(EntityType<? extends Karachun> t,net.minecraft.world.level.Level w){super(t,w);setPersistenceRequired();xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.MOVEMENT_SPEED,.2).add(Attributes.ARMOR,10).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.9);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PHASE,0);b.define(ACTION,0);b.define(AGE,0);b.define(MAX,1600f);}
 /** Like the existing Poloz, own health ceiling avoids vanilla's 1024 attribute clamp. */
 @Override public float getMaxHealth(){return entityData==null?1600:entityData.get(MAX);}
 public void configureHealth(float health){entityData.set(MAX,health);setHealth(health);}
 public int phase(){return entityData.get(PHASE);}public int action(){return entityData.get(ACTION);}public int age(){return entityData.get(AGE);}public boolean fighting(){return session!=null&&phase()>=1&&phase()<=3;}
 public boolean candidate(LivingEntity e){return fighting()&&e.isAlive()&&NightOwners.owner(e)!=null&&e.position().distanceToSqr(anchor)<=Math.pow(NightConfig.value("arena"),2)&&(!(e instanceof ServerPlayer p)||!p.isSpectator()&&!p.isCreative())&&(!(e instanceof Mob)||e instanceof org.slavicmyths.morozko.PlayerDouble||e instanceof org.slavicmyths.quest185.WolfSupport||e instanceof net.minecraft.world.entity.TamableAnimal||org.slavicmyths.bogatyr.HeroThreats.availableHero(e));}
 private List<LivingEntity> targets(){return level().getEntitiesOfClass(LivingEntity.class,new AABB(anchor,anchor).inflate(NightConfig.value("arena")),this::candidate);}
 public void credit(UUID uuid,float damage,boolean attacked){if(uuid==null||session==null||!(level() instanceof ServerLevel w))return;var d=NightLedger.get(w);var s=d.session(session);var parts=s.getCompound("Participants");var n=parts.getCompound(uuid.toString());n.putDouble("Damage",n.getDouble("Damage")+damage);if(attacked)n.putBoolean("Attacked",true);parts.put(uuid.toString(),n);s.put("Participants",parts);d.setDirty();}
 private void action(int a){entityData.set(ACTION,a);entityData.set(AGE,0);getNavigation().stop();if(a>0)NightContent.sound(this,switch(a){case 1,2,3->"scythe_windup";case 4->"touch_hunger";case 5->"touch_frost";case 6->"touch_death";case 7->"long_night_cast";case 8->"voice_whisper";case 9->"last_hour_mark";default->"yield";});}
 public void cancelPeacefully(){if(level() instanceof ServerLevel w){if(session!=null){var d=NightLedger.get(w);var row=d.session(session);if(!row.getBoolean("Complete")){row.putBoolean("Cancelled",true);if(row.hasUUID("Owner"))d.player(row.getUUID("Owner")).putLong("TrialReady",d.now(w));d.setDirty();}}cleanup();bar.removeAllPlayers();discard();}}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();if(session==null||NightLedger.get(w).session(session).getBoolean("Cancelled")){discard();return;}entityData.set(AGE,age()+1);stageAge++;
  if(phase()==0){if(stageAge==20)ravens();if(stageAge>=60){entityData.set(PHASE,1);stageAge=0;}return;}
  if(phase()>=4){if(stageAge==40)NightLedger.get(w).complete(w,session);if(stageAge>=80)discard();return;}
  if(getHealth()<=NightRules.floor(getMaxHealth())+.001){yieldTrial();return;}entityData.set(PHASE,NightRules.phase(getHealth(),getMaxHealth()));battleTicks++;bar.setName(NightContent.text("boss_phase",phase()));bar.setProgress(getHealth()/getMaxHealth());if(nightTicks>0)nightTicks--;
  if(tickCount%4==0){var ts=targets();boolean present=false;var d=NightLedger.get(w);var parts=d.session(session).getCompound("Participants");for(var t:ts){affected.add(t.getUUID());if(t instanceof ServerPlayer p){present=true;bar.addPlayer(p);var row=parts.getCompound(p.getUUID().toString());row.putInt("Active",row.getInt("Active")+4);parts.put(p.getUUID().toString(),row);}double distance=Math.sqrt(FrostWorld.bboxDistance(position().add(0,1,0),t.getBoundingBox()));double radius=NightConfig.value("aura"),rate=distance<4?NightConfig.value("nearGain"):distance<7?NightConfig.value("midGain"):distance<=radius?NightConfig.value("farGain"):0;if(rate>0&&hasLineOfSight(t))NightStatus.add(t,session,rate/5*(t instanceof ServerPlayer?1:.5)*(nightTicks>0?1.4:1),rate/5*.6+(nightTicks>0?.6:phase()==3?.2:0),nightTicks>0);else if(nightTicks>0)NightStatus.add(t,session,0,.6,true);}
   d.session(session).put("Participants",parts);d.setDirty();for(var p:new ArrayList<>(bar.getPlayers()))if(!candidate(p))bar.removePlayer(p);absent=present?0:absent+4;if(absent>=600){d.session(session).putBoolean("Cancelled",true);d.setDirty();discard();return;}setTarget(ts.stream().filter(this::hasLineOfSight).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));}
  if(marker!=null){markerAge++;if(markerAge%20==0)ring(marker,3,24);if(markerAge==40||markerAge==80)NightContent.sound(this,"last_hour_mark");if(markerAge>=NightConfig.ticks("hourWindup")){NightContent.sound(this,"last_hour_impact");for(var t:targets())if(FrostWorld.bboxDistance(marker,t.getBoundingBox())<=9&&hasLineOfSight(t))hit(t,16,10,10);burst(marker,40);marker=null;}}
  if(tickCount%40==0){ring(position(),NightConfig.value("aura"),18);if(nightTicks>0)NightContent.sound(this,"dark_frost");}if(tickCount%240==0)NightContent.sound(this,"ambient_low_wind");
  birds.removeIf(id->!(w.getEntity(id) instanceof NightRaven r)||!r.isAlive());long now=w.getGameTime();if(birds.size()<4&&now>=ready[0]){ravens();ready[0]=now+NightConfig.ticks("resummonCooldown");}
  if(action()!=0){castTick();return;}var t=getTarget();if(t==null)return;getLookControl().setLookAt(t,12,12);if(tickCount%4!=0)return;
  if(battleTicks>120&&marker==null&&now>=ready[9]){marker=t.position();markerAge=0;action(9);ready[9]=now+NightConfig.ticks("hourCooldown");}
  else if(battleTicks>80&&nightTicks==0&&now>=ready[7]){action(7);ready[7]=now+NightConfig.ticks("nightCooldown");}
  else if(battleTicks>180&&now>=ready[8]){action(8);ready[8]=now+NightConfig.ticks("fearCooldown");}
  else if(now>=ready[4]&&distanceToSqr(t)>16){action(4);ready[4]=now+NightConfig.ticks("hungerCooldown");}
  else if(distanceToSqr(t)<81&&now>=ready[5]){action(5);ready[5]=now+NightConfig.ticks("frostCooldown");}
  else if(distanceToSqr(t)<25&&now>=ready[6]){action(6);ready[6]=now+NightConfig.ticks("deathCooldown");}
  else if(distanceToSqr(t)<36&&now>=ready[1]){action(1+combo++%3);ready[1]=now+NightConfig.ticks("sweepCooldown")/(phase()==3?2:1);}
  else if(distanceToSqr(t)>9&&position().distanceToSqr(anchor)<Math.pow(NightConfig.value("arena")-5,2))getNavigation().moveTo(t,1);
 }
 private void castTick(){int a=action(),age=age();var t=getTarget();if(a<=3&&age==(a==3?14:10)){for(var e:targets())if(FrostWorld.cone(this,e,4,70)){hit(e,12,4,6);e.knockback(.6,Math.sin(Math.toRadians(getYRot())), -Math.cos(Math.toRadians(getYRot())));}NightContent.sound(this,"scythe_hit");}
  if(a==4&&age==16&&t!=null)NightBlade.hunger(this,t.getBoundingBox().getCenter());
  if(a==5&&age==20){for(var e:targets())if(FrostWorld.cone(this,e,8,30)){hit(e,6,20,4);NightEffects.add(e,session,MobEffects.WEAKNESS,80,0);}ring(position(),8,32);}
  if(a==6&&age==16)for(var e:targets())if(FrostWorld.cone(this,e,3.5,40)){if(hit(e,8,10,10))NightEffects.add(e,session,MobEffects.WITHER,100,1);}
  if(a==7&&age==24)nightTicks=NightConfig.ticks("nightDuration");
  if(a==8&&age==24)for(var e:targets()){affected.add(e.getUUID());NightStatus.add(e,session,0,30,false);}
  if(age>=switch(a){case 1,2->28;case 3->40;case 4,6->32;case 5->36;case 7,8->40;case 9->26;default->1;})action(0);
 }
 public boolean hit(LivingEntity t,float damage,int waning,int fear){if(!candidate(t))return false;boolean success=t.hurt(damageSources().mobAttack(this),damage);if(success){affected.add(t.getUUID());NightStatus.add(t,session,waning*(t instanceof ServerPlayer?1:.5),fear,false);credit(NightOwners.owner(t),0,true);}return success;}
 private void ravens(){var w=(ServerLevel)level();for(int i=0;i<4&&birds.size()<4;i++){var r=NightContent.RAVEN.get().create(w);if(r==null)continue;double angle=i*Math.PI/2;var at=position().add(Math.cos(angle)*2,Math.min(4,2+i*.4),Math.sin(angle)*2);if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(at))||!w.noCollision(r.getBoundingBox().move(at.subtract(r.position()))))continue;r.configure(this,i);r.setPos(at);if(w.addFreshEntity(r))birds.add(r.getUUID());}}
 public void yieldTrial(){if(!fighting())return;entityData.set(PHASE,4);setHealth(NightRules.floor(getMaxHealth()));action(10);stageAge=0;cleanup();getRandom().nextFloat();/* Preserve the duplicate cue pitch draw; action(10) already plays yield. */for(var p:bar.getPlayers())p.sendSystemMessage(NightContent.text("success"));bar.setName(NightContent.text("success"));bar.setProgress(.05f);}
 private void cleanup(){nightTicks=0;marker=null;if(level() instanceof ServerLevel w){for(UUID id:birds){var r=w.getEntity(id);if(r!=null)r.discard();}birds.clear();for(UUID id:affected){var t=w.getEntity(id);if(t instanceof LivingEntity l){NightStatus.clear(l,session);NightEffects.clear(l,session);}}affected.clear();for(var e:w.getEntitiesOfClass(NightBlade.class,getBoundingBox().inflate(NightConfig.value("arena")+10),b->Objects.equals(b.session,session)))e.discard();}}
 private void ring(Vec3 at,double radius,int count){var w=(ServerLevel)level();for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.3f,.18f,.48f),.8f),at.x+Math.cos(a)*radius,at.y+.1,at.z+Math.sin(a)*radius,1,0,.05,0,0);}}
 private void burst(Vec3 at,int count){((ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,at.x,at.y+.5,at.z,count,1,.5,1,.05);}
 @Override public boolean hurt(DamageSource s,float n){if(s.is(DamageTypes.GENERIC_KILL)&&level() instanceof ServerLevel w){if(session!=null){NightLedger.get(w).session(session).putBoolean("Cancelled",true);NightLedger.get(w).setDirty();}discard();return true;}if(!fighting()||s.getEntity() instanceof Karachun||s.getEntity() instanceof NightRaven)return false;return super.hurt(s,n);}
 @Override public void setHealth(float hp){if(session!=null&&phase()<4&&hp<=NightRules.floor(getMaxHealth()))hp=NightRules.floor(getMaxHealth());super.setHealth(hp);}
 @Override public void die(DamageSource s){if(!level().isClientSide)yieldTrial();}
 @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
 @Override public void remove(RemovalReason r){if(!level().isClientSide){cleanup();bar.removeAllPlayers();if(session!=null&&phase()<4){var d=NightLedger.get((ServerLevel)level());d.session(session).putBoolean("Cancelled",true);d.setDirty();}}super.remove(r);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);org.slavicmyths.combat.CreatureHealthBalance.mark(n);n.putFloat("NightMax",getMaxHealth());if(session!=null)n.putUUID("NightSession",session);}
 @Override public void readAdditionalSaveData(CompoundTag n){float oldMax=n.contains("NightMax")?n.getFloat("NightMax"):1200;
  if(!Float.isFinite(oldMax)||oldMax<=0)oldMax=1200;
  float newMax=oldMax;
  if(n.getInt(org.slavicmyths.combat.CreatureHealthBalance.KEY)<1){
   newMax=oldMax==1200?(float)Math.max(100,NightConfig.value("health")):oldMax;
   var migrated=n.copy();if(n.contains("Health"))migrated.putFloat("Health",org.slavicmyths.combat.CreatureHealthBalance.proportional(n.getFloat("Health"),oldMax,newMax));
   org.slavicmyths.combat.CreatureHealthBalance.mark(migrated);n=migrated;
  }
  session=null;entityData.set(MAX,newMax);super.readAdditionalSaveData(n);if(n.hasUUID("NightSession")){session=n.getUUID("NightSession");if(level() instanceof ServerLevel w){NightLedger.get(w).session(session).putBoolean("Cancelled",true);NightLedger.get(w).setDirty();}}}
}
