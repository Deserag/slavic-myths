package org.slavicmyths.guardians;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.karachun.NightOwners;
/** Two entity-owned encounter controllers; no global tick polling. */
public abstract class GuardianEntity extends PathfinderMob implements Enemy {
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.INT),AGE=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.INT),PATTERN=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> SCENE=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<BlockPos> ORIGIN=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.BLOCK_POS),MIN=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.BLOCK_POS),MAX=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.BLOCK_POS);
 private static final EntityDataAccessor<Integer> ROTATION=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> SCENE_AGE=SynchedEntityData.defineId(GuardianEntity.class,EntityDataSerializers.INT);
 public UUID instance;protected CompoundTag layout=new CompoundTag();protected int absent,hitArm;protected long meleeReady;private boolean scene;private int sceneAge;
 protected final ServerBossEvent bar;protected final boolean water;
 public GuardianEntity(EntityType<? extends GuardianEntity> t,Level w,boolean water){super(t,w);this.water=water;xpReward=0;bar=new ServerBossEvent(GuardianContent.text(water?"water_boss":"fire_boss"),water?net.minecraft.world.BossEvent.BossBarColor.BLUE:net.minecraft.world.BossEvent.BossBarColor.RED,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);setPersistenceRequired();}
 public static AttributeSupplier.Builder attributes(boolean water){return createMobAttributes().add(Attributes.MAX_HEALTH,GuardianRules.maximum(water)).add(Attributes.ARMOR,water?GuardianRules.WATER_ARMOR:GuardianRules.FIRE_ARMOR).add(Attributes.MOVEMENT_SPEED,water?GuardianRules.WATER_SPEED:GuardianRules.FIRE_SPEED).add(Attributes.ATTACK_DAMAGE,water?GuardianRules.WATER_MELEE:GuardianRules.FIRE_MELEE).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.8);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,GuardianRules.IDLE);b.define(AGE,0);b.define(PATTERN,0);b.define(SCENE,false);b.define(ORIGIN,BlockPos.ZERO);b.define(MIN,BlockPos.ZERO);b.define(MAX,BlockPos.ZERO);b.define(ROTATION,0);b.define(SCENE_AGE,0);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new Goal(){ {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));} @Override public boolean canUse(){return fighting()&&state()==GuardianRules.COMBAT&&getTarget()!=null;}@Override public boolean canContinueToUse(){return canUse()&&candidate(getTarget());}@Override public void tick(){var t=getTarget();getLookControl().setLookAt(t,30,30);if(tickCount%10==0&&arena().contains(t.position()))getNavigation().moveTo(t,1);}@Override public void stop(){getNavigation().stop();}});}
 public int state(){return entityData.get(STATE);}public int age(){return entityData.get(AGE);}public int pattern(){return entityData.get(PATTERN);}public boolean scene(){return entityData.get(SCENE);}public boolean water(){return water;}
 public int sceneAge(){return entityData.get(SCENE_AGE);}
 public boolean fighting(){return instance!=null&&state()!=GuardianRules.IDLE&&state()!=GuardianRules.AWAKEN&&state()<GuardianRules.PURIFY;}
 @Override public void move(MoverType type,Vec3 delta){if(instance!=null&&!level().isClientSide){var b=arena();double radius=getBbWidth()/2+.05;double x=Math.clamp(getX()+delta.x,b.minX+radius,b.maxX-radius),z=Math.clamp(getZ()+delta.z,b.minZ+radius,b.maxZ-radius);delta=new Vec3(x-getX(),delta.y,z-getZ());}super.move(type,delta);}
 protected void state(int state){entityData.set(STATE,state);entityData.set(AGE,0);getNavigation().stop();setDeltaMovement(getDeltaMovement().multiply(0,1,0));}
 protected void pattern(int n){entityData.set(PATTERN,n);}
 public BlockPos origin(){return entityData.get(ORIGIN);}
 public net.minecraft.world.level.block.Rotation rotation(){return net.minecraft.world.level.block.Rotation.values()[Math.clamp(entityData.get(ROTATION),0,3)];}
 private void syncLayout(){entityData.set(ORIGIN,BlockPos.of(layout.getLong("Origin")));entityData.set(ROTATION,GuardianWorld.rotation(layout).ordinal());int[] b=layout.getIntArray("ArenaBounds");if(b.length==6){entityData.set(MIN,new BlockPos(b[0],b[1],b[2]));entityData.set(MAX,new BlockPos(b[3],b[4],b[5]));}}
 public AABB arena(){return level().isClientSide?new AABB(Vec3.atLowerCornerOf(entityData.get(MIN)),Vec3.atLowerCornerOf(entityData.get(MAX).offset(1,1,1))):GuardianWorld.arena(layout);}
 public void configure(UUID id,CompoundTag n){if(GuardianRules.migrateRecord(n)&&level() instanceof ServerLevel w)GuardianLedger.get(w).setDirty();instance=id;layout=n.copy();syncLayout();super.setHealth(n.contains("Health")?Math.max(GuardianRules.threshold(water),n.getFloat("Health")):getMaxHealth());if(n.getString("Stage").equals("CLEARED")){super.setHealth(getMaxHealth());state(GuardianRules.CLEANSED);}else if(n.getString("Stage").equals("PURIFICATION"))state(GuardianRules.PURIFY);else state(GuardianRules.IDLE);}
 public boolean candidate(LivingEntity e){if(e==null||!e.isAlive()||e==this||e.level()!=level()||!arena().contains(e.position())||NightOwners.owner(e)==null)return false;if(e instanceof ServerPlayer p)return !p.isSpectator()&&!p.hasDisconnected();return org.slavicmyths.bogatyr.HeroThreats.availableHero(e)||e instanceof org.slavicmyths.morozko.PlayerDouble||e instanceof org.slavicmyths.quest185.WolfSupport||e instanceof net.minecraft.world.entity.TamableAnimal||e instanceof org.slavicmyths.karachun.NightRaven r&&r.allied();}
 protected List<LivingEntity> targets(){return level().getEntitiesOfClass(LivingEntity.class,arena(),this::candidate);}
 protected boolean hit(LivingEntity t,float damage){return hit(t,damage,damageSources().mobAttack(this));}
 protected boolean hit(LivingEntity t,float damage,DamageSource source){if(!fighting()||!candidate(t)||!hasLineOfSight(t)||damage<=0)return false;boolean hit=t.hurt(source,damage);if(hit)credit(t);return hit;}
 protected void credit(Entity e){if(e==null)return;UUID owner=NightOwners.owner(e);if(owner!=null&&level() instanceof ServerLevel w&&instance!=null)GuardianLedger.get(w).participate(instance,owner);}
 protected void sound(String key){GuardianContent.sound(level(),blockPosition(),key);}
 protected void particle(net.minecraft.core.particles.ParticleOptions kind,Vec3 p,int amount){if(level() instanceof ServerLevel w)w.sendParticles(kind,p.x,p.y,p.z,amount,.06,.04,.06,.01);}
 protected void ring(Vec3 center,double radius,net.minecraft.core.particles.ParticleOptions kind){for(int i=0;i<16;i++){double a=i*Math.PI/8;var p=center.add(Math.cos(a)*radius,.1,Math.sin(a)*radius);if(arena().contains(p))particle(kind,p,1);}}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();if(w.dimension()!=Level.OVERWORLD){discard();return;}if(instance==null){standalone(w);}var d=GuardianLedger.get(w);var n=d.row(instance);if(n.getString("Stage").equals("CLEARED")&&state()!=GuardianRules.CLEANSED){clearHazards();state(GuardianRules.CLEANSED);super.setHealth(getMaxHealth());}else if(state()==GuardianRules.CLEANSED&&!n.getString("Stage").equals("CLEARED"))d.stage(instance,"CLEARED");
  // No navigation or hazard can carry the guardian beyond its own physical room.
  if(!arena().contains(position())){var back=Vec3.atBottomCenterOf(BlockPos.of(layout.getLong("SpawnAnchor")));if(w.hasChunkAt(BlockPos.containing(back))){setPos(back);getNavigation().stop();}}
  entityData.set(AGE,age()+1);if(tickCount%20==0){n.putUUID("Boss",getUUID());n.putLong("LastPosition",blockPosition().asLong());n.putFloat("Health",getHealth());org.slavicmyths.combat.CreatureHealthBalance.mark(n);d.setDirty();GuardianWorld.install(w,instance,n);var ts=targets();boolean any=ts.stream().anyMatch(t->t instanceof ServerPlayer);absent=any?0:absent+20;for(var old:new ArrayList<>(bar.getPlayers()))if(!candidate(old)||state()>=GuardianRules.PURIFY||state()==GuardianRules.IDLE)bar.removePlayer(old);if(state()>GuardianRules.IDLE&&state()<GuardianRules.PURIFY)for(var t:ts)if(t instanceof ServerPlayer p){bar.addPlayer(p);d.participate(instance,p.getUUID());}
   if(state()==GuardianRules.IDLE&&any&&!n.getString("Stage").equals("SEALED")){state(GuardianRules.AWAKEN);sound(water?"spring_keeper_emerge":"fire_guardian_awaken");}
   if(absent>=GuardianRules.ABSENT_RESET&&fighting()){clearHazards();state(GuardianRules.IDLE);d.stage(instance,"READY");bar.removeAllPlayers();setTarget(null);}
   if(fighting()){var choices=ts.stream().filter(this::hasLineOfSight).sorted(Comparator.comparingDouble(this::distanceToSqr)).toList();var selected=choices.isEmpty()?null:choices.getFirst();if(choices.size()>1&&w.getGameTime()%120==0)selected=choices.get(1);setTarget(selected);}
  }
  bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));if(getTarget()!=null&&!candidate(getTarget())){setTarget(null);getNavigation().stop();}
  if(state()==GuardianRules.AWAKEN&&age()>=GuardianRules.AWAKEN_TICKS){state(GuardianRules.COMBAT);d.stage(instance,"ACTIVE");}
  if(state()==GuardianRules.PURIFY){bar.removeAllPlayers();if(scene){sceneAge++;entityData.set(SCENE_AGE,sceneAge);entityData.set(SCENE,true);if(tickCount%4==0)particle(water?net.minecraft.core.particles.ParticleTypes.GLOW:net.minecraft.core.particles.ParticleTypes.FLAME,position().add(0,1.8,0),4);if(sceneAge>=GuardianRules.PURIFY_SCENE)complete();}return;}
  if(state()==GuardianRules.CLEANSED){setTarget(null);bar.removeAllPlayers();if(tickCount%240==0)sound(water?"spring_keeper_idle":"fire_guardian_idle");return;}
  if(fighting()){if(tickCount%160==0)sound(water?"spring_keeper_idle":"fire_guardian_idle");combat();}
 }
 /** Spawn eggs and /summon get a real local encounter rather than disappearing. */
 private void standalone(ServerLevel w){configure(getUUID(),GuardianLedger.get(w).standalone(getUUID(),blockPosition(),water));}
 @Override protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand hand){if(instance!=null&&layout.getBoolean("Standalone")&&state()==GuardianRules.PURIFY){if(p instanceof ServerPlayer server)finishPurification(server);return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);}return super.mobInteract(p,hand);}
 public void cancelPeacefully(){if(level() instanceof ServerLevel w&&instance!=null){var d=GuardianLedger.get(w);var n=d.row(instance);n.putFloat("Health",getHealth());if(state()>GuardianRules.IDLE&&state()<GuardianRules.PURIFY)d.stage(instance,"READY");d.setDirty();}clearHazards();bar.removeAllPlayers();discard();}
 protected abstract void combat();
 protected void clearHazards(){}
 protected boolean melee(){var t=getTarget();if(t==null||distanceToSqr(t)>12.25||!hasLineOfSight(t))return false;if(state()==GuardianRules.COMBAT&&level().getGameTime()>=meleeReady){hitArm=1-hitArm;pattern(hitArm);state(GuardianRules.MELEE);sound(water?"spring_whip_windup":"fire_guardian_swing");return true;}return false;}
 protected void meleeTick(){if(age()==GuardianRules.MELEE_WINDUP&&getTarget()!=null&&distanceToSqr(getTarget())<=12.25)hit(getTarget(),water?GuardianRules.WATER_MELEE:GuardianRules.FIRE_MELEE);if(age()>=GuardianRules.MELEE_WINDUP+GuardianRules.MELEE_RECOVERY){meleeReady=level().getGameTime()+30;state(GuardianRules.COMBAT);}}
 protected void beginPurification(){super.setHealth(GuardianRules.threshold(water));clearHazards();state(GuardianRules.PURIFY);setTarget(null);bar.removeAllPlayers();GuardianLedger.get((ServerLevel)level()).stage(instance,"PURIFICATION");for(var t:targets())if(t instanceof ServerPlayer p)p.sendSystemMessage(GuardianContent.text(water?"purify_water_hint":"purify_fire_hint"));}
 public void finishPurification(ServerPlayer p){if(state()!=GuardianRules.PURIFY||scene)return;scene=true;sceneAge=0;credit(p);sound(water?"spring_keeper_purify":"fire_guardian_purify");}
 private void complete(){state(GuardianRules.CLEANSED);scene=false;entityData.set(SCENE,false);setHealth(getMaxHealth());var w=(ServerLevel)level();var d=GuardianLedger.get(w);d.stage(instance,"CLEARED");var n=d.row(instance);n.putInt("Valves",3);n.putFloat("Health",getHealth());GuardianWorld.install(w,instance,n);w.getServer().overworld().getDataStorage().save();for(var t:targets())if(t instanceof ServerPlayer p){org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:"+(water?"cleansed_spring_keeper":"cleansed_flame_guardian"),4,true);p.sendSystemMessage(GuardianContent.text(water?"water_cleansed":"fire_cleansed"));}clearHazards();}
 @Override public boolean hurt(DamageSource source,float amount){if(level().isClientSide||instance==null)return false;if(source.is(DamageTypes.GENERIC_KILL)){clearHazards();bar.removeAllPlayers();discard();return true;}if(!fighting()||state()==GuardianRules.LOCKDOWN||source.getEntity() instanceof GuardianEntity||source.getEntity() instanceof WaterEcho)return false;if(source.getEntity() instanceof LivingEntity attacker&&!candidate(attacker))return false;credit(source.getEntity());return super.hurt(source,amount);}
 @Override public void setHealth(float health){if(instance!=null&&state()<GuardianRules.PURIFY&&health<=GuardianRules.threshold(water)&&!level().isClientSide){beginPurification();return;}super.setHealth(health);}
 @Override public boolean isAlliedTo(Entity e){return state()>=GuardianRules.PURIFY||super.isAlliedTo(e);}
 @Override public boolean canBeLeashed(){return false;}
 @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return GuardianContent.SOUNDS.get(water?"spring_keeper_hurt":"fire_guardian_hurt").get();}
 @Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){if(tickCount%8==0)sound(water?"spring_keeper_step":"fire_guardian_step");}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);org.slavicmyths.combat.CreatureHealthBalance.mark(n);if(instance!=null)n.putUUID("GuardianInstance",instance);n.put("GuardianLayout",layout);n.putInt("GuardianState",state());}
 @Override public void readAdditionalSaveData(CompoundTag n){instance=null;n=org.slavicmyths.combat.CreatureHealthBalance.prepare(n,GuardianRules.maximum(water),water?450:500,false);super.readAdditionalSaveData(n);instance=n.hasUUID("GuardianInstance")?n.getUUID("GuardianInstance"):null;layout=n.getCompound("GuardianLayout");syncLayout();int saved=n.getInt("GuardianState");state(saved>=GuardianRules.PURIFY?saved:GuardianRules.IDLE);clearHazards();}
 @Override public void remove(RemovalReason why){if(!level().isClientSide){bar.removeAllPlayers();clearHazards();}super.remove(why);}
}
