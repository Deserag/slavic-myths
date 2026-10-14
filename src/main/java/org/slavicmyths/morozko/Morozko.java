package org.slavicmyths.morozko;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
public class Morozko extends PathfinderMob implements Enemy {
 protected static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(Morozko.class,EntityDataSerializers.INT),ACTION=SynchedEntityData.defineId(Morozko.class,EntityDataSerializers.INT),AGE=SynchedEntityData.defineId(Morozko.class,EntityDataSerializers.INT);
 public UUID session,owner;public Vec3 anchor=Vec3.ZERO;protected int battleTicks,absent;private int stageAge;protected final long[] ready=new long[10];private boolean finalStorm;
 private int stormTicks,enhancedTicks;private Vec3 stormCenter;private final Set<UUID> children=new HashSet<>();
 private final ServerBossEvent bar=new ServerBossEvent(FrostContent.text("boss"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
 public Morozko(EntityType<? extends Morozko> t,net.minecraft.world.level.Level w){super(t,w);setPersistenceRequired();xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,FrostRules.BOSS_HP).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.ARMOR,8).add(Attributes.ATTACK_DAMAGE,FrostRules.MELEE).add(Attributes.FOLLOW_RANGE,28).add(Attributes.KNOCKBACK_RESISTANCE,.8);}
 /** Species-local ceiling; IceDouble retains vanilla capped attributes. No new wire fields. */
 @Override public float getMaxHealth(){return this instanceof IceDouble?super.getMaxHealth():(float)org.slavicmyths.combat.CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
 @Override public double getAttributeValue(net.minecraft.core.Holder<Attribute> attribute){return !(this instanceof IceDouble)&&attribute.equals(Attributes.MAX_HEALTH)?org.slavicmyths.combat.CreatureHealthBalance.uncappedMaximum(getAttribute(attribute)):super.getAttributeValue(attribute);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PHASE,0);b.define(ACTION,0);b.define(AGE,0);}
 public int phase(){return entityData.get(PHASE);}public int action(){return entityData.get(ACTION);}public int age(){return entityData.get(AGE);}
 protected void action(int a){entityData.set(ACTION,a);entityData.set(AGE,0);getNavigation().stop();if(a>0){String cue=switch(a){case 1,2->null;case 3->"blizzard_start";case 4->"spike_telegraph";case 5->"breath";case 6->"wave";case 7->"clone_spawn";case 8->"blizzard_start";default->"yield";};if(cue!=null)FrostContent.sound(this,cue);}}
 public boolean fighting(){return phase()>=1&&phase()<=3&&session!=null;}
 public boolean candidate(LivingEntity e){return fighting()&&e.isAlive()&&!(e instanceof Morozko)&&FrostWorld.owner(e)!=null&&e.position().distanceToSqr(anchor)<=1600&&(!(e instanceof ServerPlayer p)||!p.isSpectator()&&(!p.isCreative()||p.getUUID().equals(owner)))&&(!(e instanceof Mob)||e instanceof PlayerDouble||org.slavicmyths.bogatyr.HeroThreats.availableHero(e));}
 public void credit(UUID id,float damage){if(id==null||!(level() instanceof ServerLevel w)||session==null)return;var d=FrostLedger.get(w);var s=d.session(session);var n=s.getCompound("Participants");var row=n.getCompound(id.toString());row.putDouble("Damage",row.getDouble("Damage")+damage);n.put(id.toString(),row);s.put("Participants",n);d.setDirty();}
 public void attacked(LivingEntity e){UUID id=FrostWorld.owner(e);if(id==null||session==null)return;var d=FrostLedger.get((ServerLevel)level());var s=d.session(session);var n=s.getCompound("Participants");var row=n.getCompound(id.toString());row.putBoolean("Attacked",true);n.put(id.toString(),row);s.put("Participants",n);d.setDirty();}
 protected List<LivingEntity> targets(){return level().getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(anchor,anchor).inflate(40),this::candidate);}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();if(session==null||FrostLedger.get(w).session(session).getBoolean("Cancelled")){cleanup();discard();return;}
 if(this instanceof IceDouble){doubleTick();return;}
 if(tickCount%12==0&&getDeltaMovement().horizontalDistanceSqr()>.0005&&onGround())FrostContent.sound(this,"step");
 entityData.set(AGE,age()+1);stageAge++;
 if(phase()==0){if(tickCount%4==0)FrostVisuals.ring(w,anchor,1.8,.05,12);if(stageAge>=60){entityData.set(PHASE,1);stageAge=0;}return;}
 if(phase()>=4){getNavigation().stop();setTarget(null);if(phase()==4&&stageAge>=110){entityData.set(PHASE,5);FrostLedger.get(w).complete(w,session);stageAge=0;}else if(phase()==5){entityData.set(PHASE,6);stageAge=0;}else if(phase()==6){FrostVisuals.burst(w,position().add(0,1.5,0),6);if(stageAge>=26){cleanup();discard();}}return;}
 if(getHealth()<=FrostRules.floor(getMaxHealth())+.001){yieldTrial();return;}
 battleTicks++;int next=FrostRules.phase(getHealth(),getMaxHealth());entityData.set(PHASE,next);bar.setProgress(getHealth()/getMaxHealth());
 if(tickCount%20==0)FrostVisuals.ring(w,position(),8,.08,16);
 if(tickCount%4==0){var targets=targets();var d=FrostLedger.get(w);var parts=d.session(session).getCompound("Participants");boolean present=false;for(var e:targets){if(e instanceof ServerPlayer p){present=true;bar.addPlayer(p);var row=parts.getCompound(p.getUUID().toString());row.putInt("Active",row.getInt("Active")+4);parts.put(p.getUUID().toString(),row);}double distance=Math.sqrt(FrostWorld.bboxDistance(position().add(0,1,0),e.getBoundingBox()));double rate=FrostRules.rate(distance)*(enhancedTicks>0?1.5:1)*(e instanceof ServerPlayer?1:.5);if(rate>0)FrostCold.aura(e,rate*(hasLineOfSight(e)?1:.5)/5,this);if(stormTicks>0&&e.position().distanceToSqr(stormCenter)<=144&&hasLineOfSight(e))FrostCold.aura(e,(e instanceof ServerPlayer?.4:.2),this);}
 d.session(session).put("Participants",parts);d.setDirty();for(var p:new ArrayList<>(bar.getPlayers()))if(!candidate(p))bar.removePlayer(p);if(present)absent=0;else absent+=4;if(absent>600){for(var p:bar.getPlayers())p.sendSystemMessage(FrostContent.text("cancel"));FrostLedger.get(w).session(session).putBoolean("Cancelled",true);cleanup();discard();return;}
 var t=targets.stream().filter(this::hasLineOfSight).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(t);}
 if(enhancedTicks>0)enhancedTicks--;
 if(stormTicks>0){stormTicks--;if(tickCount%4==0)FrostVisuals.storm(w,stormCenter,12);if(stormTicks%80==0)FrostContent.sound(this,"blizzard_loop");}
 if(action()!=0){castTick();return;}
 if(next==3&&!finalStorm){finalStorm=true;action(8);return;}
 var t=getTarget();if(t==null)return;getLookControl().setLookAt(t,8,8);if(tickCount%4!=0)return;
 long now=w.getGameTime();children.removeIf(id->!(w.getEntity(id) instanceof IceDouble c)||!c.isAlive());
 if(battleTicks>=400&&children.size()<3&&now>=ready[7]){action(7);ready[7]=now+(next==1?700:560)+40;}
 else if(battleTicks>=240&&stormTicks==0&&now>=ready[3]){action(3);ready[3]=now+50+240+560;}
 else if(battleTicks>=160&&now>=ready[4]){action(4);ready[4]=now+(next>=2?221:260)+23;}
 else if(battleTicks>=100&&now>=ready[5]){action(5);ready[5]=now+(next>=2?306:360)+63;}
 else if(battleTicks>=200&&now>=ready[6]){action(6);ready[6]=now+440+42;}
 else if(distanceToSqr(t)<16&&now>=ready[1]){action(1);ready[1]=now+40;}
 else if(distanceToSqr(t)>=16&&distanceToSqr(t)<=400&&now>=ready[2]){action(2);ready[2]=now+50;}
 else if(distanceToSqr(t)>9&&position().distanceToSqr(anchor)<35*35)getNavigation().moveTo(t,1);
 if(tickCount%220==0)FrostContent.sound(this,"ambient");}
 protected void castTick(){var w=(ServerLevel)level();int a=action(),age=age();var t=getTarget();if(a==1&&age==10){FrostContent.sound(this,"staff_hit");for(var e:targets())if(FrostWorld.cone(this,e,3.5,55))hit(e,FrostRules.MELEE,8);}
 if(a==2&&age==13&&t!=null&&hasLineOfSight(t)){FrostContent.sound(this,"ice_bolt");FrostShard.launch(this,t.getBoundingBox().getCenter(),FrostRules.BOLT,10,session);}
 if(a==3&&age==50){stormTicks=240;stormCenter=position();FrostContent.sound(this,"blizzard_loop");}
 if(a==4&&age==5){var spots=new ArrayList<Vec3>();for(var e:targets()){if(spots.size()>=5)break;var p=FrostWorld.safe(w,e.position(),.8f,2.5f,1);if(p!=null)spots.add(p);}for(int i=0;spots.size()<3&&i<8;i++){double theta=i*Math.PI/4;var p=FrostWorld.safe(w,position().add(Math.cos(theta)*5,0,Math.sin(theta)*5),.8f,2.5f,1);if(p!=null&&spots.stream().noneMatch(v->v.distanceToSqr(p)<1))spots.add(p);}for(var p:spots)FrostHazard.spike(this,p,18,7,12,session);}
 if(a==5&&age>=16&&age<=56&&(age-16)%14==0){for(var e:targets())if(FrostWorld.cone(this,e,9,25))hit(e,4,15);FrostVisuals.cone(w,this,9);}
 if(a==6&&age==24)FrostHazard.wave(this,7,5,8,session);
 if(a==7&&age==7){for(int i=0;i<3&&children.size()<3;i++){double theta=i*Math.PI*2/3;var p=FrostWorld.safe(w,position().add(Math.cos(theta)*4,0,Math.sin(theta)*4),1.35f,3.15f,2);if(p==null)continue;var c=FrostContent.DOUBLE.get().create(w);if(c!=null){c.session=session;c.owner=getUUID();c.anchor=anchor;c.setPos(p);w.addFreshEntity(c);children.add(c.getUUID());}}var list=new ListTag();for(var id:children)list.add(StringTag.valueOf(id.toString()));FrostLedger.get(w).session(session).put("Clones",list);FrostLedger.get(w).setDirty();}
 if(a==8&&age==48){enhancedTicks=160;stormTicks=160;stormCenter=position();FrostContent.sound(this,"blizzard_loop");}
 int duration=switch(a){case 1->18;case 2->21;case 3->50;case 4->23;case 5->63;case 6->42;case 7->40;case 8->48;default->0;};if(age>=duration)action(0);}
 protected void doubleTick(){throw new IllegalStateException("Only IceDouble uses this controller");}
 protected void hit(LivingEntity e,float damage,int cold){if(!candidate(e))return;if(e.hurt(org.slavicmyths.combat.MythDamageSources.caused("morozko_frost",this),damage)){FrostCold.add(e,cold);attacked(e);}}
 public void yieldTrial(){if(!fighting()||this instanceof IceDouble)return;setHealth(FrostRules.floor(getMaxHealth()));entityData.set(PHASE,4);stageAge=0;action(9);bar.setProgress(.05f);stormTicks=0;cleanup();var w=(ServerLevel)level();var parts=FrostLedger.get(w).session(session).getCompound("Participants");for(String key:parts.getAllKeys()){var p=w.getServer().getPlayerList().getPlayer(UUID.fromString(key));if(p!=null){FrostCold.stop(p);p.sendSystemMessage(FrostContent.text("success"));}}}
 protected void cleanup(){if(phase()<4||phase()>=6||isRemoved())bar.removeAllPlayers();if(level() instanceof ServerLevel w){for(var id:children){var e=w.getEntity(id);if(e instanceof IceDouble c)c.dissolve();else if(e!=null)e.discard();}children.clear();for(var e:w.getEntitiesOfClass(Entity.class,getBoundingBox().inflate(80),e->e instanceof FrostShard s&&Objects.equals(s.session,session)||e instanceof FrostHazard h&&Objects.equals(h.session,session)||e instanceof IceDouble c&&Objects.equals(c.session,session))){if(e instanceof IceDouble c)c.dissolve();else e.discard();}for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(40),e->FrostWorld.owner(e)!=null))FrostCold.stop(e);}}
 @Override public boolean hurt(DamageSource source,float amount){if(!(this instanceof IceDouble)&&source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)&&level() instanceof ServerLevel w){if(session!=null){FrostLedger.get(w).session(session).putBoolean("Cancelled",true);FrostLedger.get(w).setDirty();}discard();return true;}if(!(this instanceof IceDouble)&&!fighting()||source.getEntity() instanceof Morozko||source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING))return false;return super.hurt(source,amount);}
 @Override public boolean canFreeze(){return false;}
 /** Last line of defence after other damage subscribers; vanilla never sees lethal health. */
 @Override public void setHealth(float hp){if(!(this instanceof IceDouble)&&session!=null&&phase()>0&&phase()<6)hp=Math.max(hp,FrostRules.floor(getMaxHealth()));super.setHealth(hp);}
 @Override public void remove(RemovalReason r){if(!level().isClientSide&&!(this instanceof IceDouble)){bar.removeAllPlayers();cleanup();}super.remove(r);}
 @Override public void die(DamageSource s){if(this instanceof IceDouble){FrostContent.sound(this,"clone_break");super.die(s);}else if(level() instanceof ServerLevel)yieldTrial();}
 @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(!(this instanceof IceDouble))org.slavicmyths.combat.CreatureHealthBalance.mark(n);if(session!=null)n.putUUID("FrostSession",session);if(owner!=null)n.putUUID("FrostOwner",owner);n.putLong("FrostAnchor",net.minecraft.core.BlockPos.containing(anchor).asLong());}
 @Override public void readAdditionalSaveData(CompoundTag n){if(!(this instanceof IceDouble)){double oldBase=org.slavicmyths.combat.CreatureHealthBalance.savedBase(n,1000);n=org.slavicmyths.combat.CreatureHealthBalance.prepare(n,oldBase==1000?FrostRules.BOSS_HP:oldBase,1000,true);}super.readAdditionalSaveData(n);if(n.hasUUID("FrostSession"))session=n.getUUID("FrostSession");if(n.hasUUID("FrostOwner"))owner=n.getUUID("FrostOwner");anchor=Vec3.atBottomCenterOf(net.minecraft.core.BlockPos.of(n.getLong("FrostAnchor")));if(level() instanceof ServerLevel w&&session!=null){FrostLedger.get(w).session(session).putBoolean("Cancelled",true);FrostLedger.get(w).setDirty();}}
}
