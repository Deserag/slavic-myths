package org.slavicmyths.burningislands;

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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.burningislands.IslandsContent.Kind;
import org.slavicmyths.burningislands.MagmaCombat.Action;

/** Neutral until actually hurt. Entity-local, server-owned casts; no terrain editing or summons. */
public final class IslandsMob extends PathfinderMob {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(IslandsMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(IslandsMob.class,EntityDataSerializers.LONG);
 private static final EntityDataAccessor<Boolean> SECOND=SynchedEntityData.defineId(IslandsMob.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Integer> EXPOSED=SynchedEntityData.defineId(IslandsMob.class,EntityDataSerializers.INT);
 public final Kind kind;
 private int age,cooldown,anger,alarmCooldown,sequence;
 private double castReach=18;
 private UUID provoker,lastPlayer;
 private BlockPos home;
 private Vec3 origin=Vec3.ZERO,forward=new Vec3(0,0,1);
 private final Set<UUID> struck=new HashSet<>(),participants=new HashSet<>();
 private boolean rewarded,journalChecked;
 public void bindNatural(net.minecraft.core.BlockPos at){home=at.immutable();setPersistenceRequired();}
 private final ServerBossEvent bar=new ServerBossEvent(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.magma_lord"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 public IslandsMob(EntityType<? extends IslandsMob> type,Level level,Kind kind){super(type,level);this.kind=kind;xpReward=kind==Kind.MAGMA_LORD?60:kind==Kind.VENT_GOLEM?15:5;}
 public static AttributeSupplier.Builder attributes(Kind k){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,k==Kind.MAGMA_LORD?850:k==Kind.VENT_GOLEM?150:55).add(Attributes.ATTACK_DAMAGE,switch(k){case HEAT_SALAMANDER->6;case SLAG_CRAB->7;case VENT_GOLEM->12;case MAGMA_LORD->14;}).add(Attributes.MOVEMENT_SPEED,switch(k){case HEAT_SALAMANDER->.24;case SLAG_CRAB->.20;case VENT_GOLEM->.18;case MAGMA_LORD->.23;}).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,k==Kind.MAGMA_LORD?.85:k==Kind.VENT_GOLEM?.65:.15);}
 public boolean boss(){return kind==Kind.MAGMA_LORD;}
 @Override protected boolean shouldDespawnInPeaceful(){return boss();}
 /** Category alone does not remove a PathfinderMob. Keep ordinary neutral island fauna. */
 private boolean peacefulBoss(){return boss()&&level().getDifficulty()==Difficulty.PEACEFUL;}
 @Override public void remove(Entity.RemovalReason reason){
  // Includes discard/checkDespawn, unload, dimension removal and the normal death path.
  if(!level().isClientSide)bar.removeAllPlayers();
  super.remove(reason);
 }
 public boolean second(){return entityData.get(SECOND);}
 public boolean exposed(){return entityData.get(EXPOSED)>0;}
 public Action action(){return Action.values()[Math.clamp(entityData.get(ACTION),0,Action.values().length-1)];}
 public float actionTime(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
 /** Future natural-arena generator must bind this before adding the entity. No command creates terrain. */
 public void bindArena(BlockPos at){home=at.immutable();setPersistenceRequired();}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(START,0L);b.define(SECOND,false);b.define(EXPOSED,0);}
 @Override protected void registerGoals(){
  goalSelector.addGoal(0,new Goal(){
   {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
   @Override public boolean canUse(){return action()!=Action.IDLE||getTarget()!=null;}
   @Override public void tick(){if(getTarget()!=null)getLookControl().setLookAt(getTarget(),25,25);if(action()!=Action.IDLE)navigation.stop();}
  });
  goalSelector.addGoal(1,new FloatGoal(this));
  goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.65));
  goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,10));
  goalSelector.addGoal(7,new RandomLookAroundGoal(this));
 }
 @Override public boolean canAttack(LivingEntity e){return !(e instanceof IslandsMob)&&!e.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()))&&super.canAttack(e);}
 @Override public boolean isAlliedTo(Entity e){return e instanceof IslandsMob||super.isAlliedTo(e);}
 private void remember(LivingEntity attacker){provoker=attacker.getUUID();anger=600;if(participants.size()<16)participants.add(provoker);setTarget(attacker);}
 @Override public boolean hurt(DamageSource source,float amount){
  if(!level().isClientSide&&peacefulBoss()){discard();return false;}
  // Chest is a real spatial weak zone: projectile impact or adjacent melee eye ray through core height.
  if(boss()&&exposed()&&source.getEntity() instanceof LivingEntity a&&coreHit(source,a))amount*=1.35F;
  boolean hit=super.hurt(source,amount);
  if(!hit||level().isClientSide||!isAlive()||!(source.getEntity() instanceof LivingEntity attacker)||!canAttack(attacker)||isAlliedTo(attacker))return hit;
  boolean first=anger==0;remember(attacker);if(attacker instanceof ServerPlayer player)lastPlayer=player.getUUID();
  if(first&&action()==Action.IDLE)begin(boss()?Action.WAKE:kind==Kind.VENT_GOLEM?Action.IDLE:Action.WARN);
  if((kind==Kind.HEAT_SALAMANDER||kind==Kind.SLAG_CRAB)&&alarmCooldown==0){
   alarmCooldown=200;
   // A loaded-entity query only. Recipients never call hurt()/alarm: no recursive cascade.
   int accepted=0;
   for(var neighbor:level().getEntitiesOfClass(IslandsMob.class,getBoundingBox().inflate(16),m->m!=this&&m.getType()==getType()&&m.isAlive()&&m.distanceToSqr(this)<=256&&m.canAttack(attacker)&&!m.isAlliedTo(attacker))){
    if(accepted++>=32)break;
    if(neighbor.anger==0){neighbor.remember(attacker);neighbor.alarmCooldown=200;neighbor.begin(Action.WARN);}
   }
  }
  return hit;
 }
 private boolean coreHit(DamageSource source,LivingEntity attacker){
  if(source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile p){Vec3 v=p.position().subtract(position());return v.y>2.5&&v.y<3.6&&Math.abs(v.x)<.7&&Math.abs(v.z)<.7;}
  Vec3 start=attacker.getEyePosition(),end=start.add(attacker.getLookAngle().scale(4.5));return new AABB(getX()-.65,getY()+2.5,getZ()-.65,getX()+.65,getY()+3.6,getZ()+.65).clip(start,end).isPresent();
 }
 private void begin(Action a){if(boss()){
   String cue=a==Action.PHASE?"phase":a==Action.WAKE||a==Action.WARN?"wake":a==Action.IDLE?null:a.name().toLowerCase(java.util.Locale.ROOT)+"_charge";
   if(cue!=null)playSound(IslandsSounds.get(cue),.65F,a==Action.PHASE?.8F:1F);
  }entityData.set(ACTION,a.ordinal());entityData.set(START,level().getGameTime());age=0;struck.clear();origin=position();castReach=getTarget()==null?18:Math.clamp(distanceTo(getTarget()),18,128);forward=getTarget()==null?getLookAngle().multiply(1,0,1).normalize():getTarget().position().subtract(origin).multiply(1,0,1).normalize();navigation.stop();}
 @Override public void tick(){
  // Run before super.tick(): no goal, cast, phase, encounter registration or death loot in Peaceful.
  if(!level().isClientSide&&peacefulBoss()){discard();return;}
  super.tick();if(level().isClientSide||!isAlive())return;
  if(alarmCooldown>0)alarmCooldown--;if(cooldown>0)cooldown--;
  if(entityData.get(EXPOSED)>0)entityData.set(EXPOSED,entityData.get(EXPOSED)-1);
  if(boss()){
   // A spawned debug entity has a stable spawn anchor; this does not create or claim a natural arena.
   if(home==null)bindArena(blockPosition());
   bar.setProgress(Math.clamp(getHealth()/850F,0,1));
   if(home!=null&&!journalChecked&&level() instanceof ServerLevel s){journalChecked=true;if(!MagmaEncounters.get(s).register(home.asLong(),getUUID())){discard();return;}}
   if(!second()&&getHealth()<=425&&isAlive()){entityData.set(SECOND,true);begin(Action.PHASE);}
  }
  if(anger>0)anger--;
  if(anger==0){setTarget(null);provoker=null;participants.clear();}
  if(getTarget()==null&&anger>0&&provoker!=null&&level() instanceof ServerLevel s&&s.getEntity(provoker) instanceof LivingEntity a&&a.isAlive()&&distanceToSqr(a)<=(boss()?128*128:32*32)&&canAttack(a))setTarget(a);
  var target=getTarget();
  if(target!=null&&(!target.isAlive()||!canAttack(target)||distanceToSqr(target)>(boss()?128*128:32*32))){setTarget(null);target=null;}
  if(home!=null&&distanceToSqr(Vec3.atCenterOf(home))>32*32){setTarget(null);cancelCast();if(tickCount%20==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}
  if(action()!=Action.IDLE){
   navigation.stop();age++;
   telegraph();
   // Loss of target cancels hostile casts; phase/warning poses may finish harmlessly.
   if(target==null&&action()!=Action.PHASE&&action()!=Action.WARN){cancelCast();return;}
   if(boss()&&age==action().release&&action()!=Action.PHASE&&action()!=Action.WARN&&action()!=Action.WAKE&&action()!=Action.IDLE)playSound(IslandsSounds.get(action().name().toLowerCase(java.util.Locale.ROOT)+"_release"),.75F,1);
   perform();
   if(age>=action().duration){boolean heavy=action().heavy();cancelCast();cooldown=boss()?40:30;if(boss()&&second()&&heavy)entityData.set(EXPOSED,60);}
   return;
  }
  if(target==null)return;
  if(tickCount%10==0&&(!boss()||distanceToSqr(target)<=32*32))navigation.moveTo(target,1);
  if(boss()&&distanceToSqr(target)>32*32)navigation.stop();
  if(cooldown>0||!loadedSight(target))return;
  double distance=distanceTo(target);
  if(boss()&&distance<=128)begin(MagmaCombat.choose(sequence++,second(),distance));
  else if(!boss()&&distance<=getBbWidth()+target.getBbWidth()+1)begin(Action.CLAWS);
 }
 @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return boss()?IslandsSounds.get("idle"):null;}
 @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return boss()?IslandsSounds.get("hurt"):super.getHurtSound(source);}
 @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return boss()?IslandsSounds.get("death"):super.getDeathSound();}
 @Override public int getAmbientSoundInterval(){return 240;}
 private void cancelCast(){entityData.set(ACTION,0);age=0;struck.clear();}
 private Vec3 point(double f,double side){return origin.add(forward.scale(f)).add(-forward.z*side,0,forward.x*side);}
 private void mark(ServerLevel s,Vec3 p){BlockPos b=BlockPos.containing(p);if(s.hasChunkAt(b))s.sendParticles(ParticleTypes.SMALL_FLAME,p.x,p.y+.1,p.z,1,0,0,0,0);}
 private void telegraph(){
  if(!(level() instanceof ServerLevel s)||age%4!=0)return;
  if(action()==Action.RING&&age>=37&&age<=68){for(int i=0;i<48;i++){double a=i*Math.PI/24;double r=MagmaCombat.ring(age+4);mark(s,origin.add(Math.cos(a)*r,0,Math.sin(a)*r));}return;}
  if(age>=action().release)return;
  switch(action()){
   case FRACTURE->{for(double a:new double[]{-.35,0,.35})for(int f=2;f<=22;f+=2)mark(s,point(f,Math.tan(a)*f));}
   case ERUPTION->{for(int pulse=0;pulse<3;pulse++)for(int i=0;i<12;i++){double a=i*Math.PI/6;mark(s,point(5+6*pulse+Math.cos(a)*2.2,Math.sin(a)*2.2));}}
   case RING->{for(int i=0;i<32;i++){double a=i*Math.PI/16;mark(s,origin.add(Math.cos(a)*2,0,Math.sin(a)*2));}}
   case PULL->{for(int i=0;i<16;i++){double a=i*Math.PI/8;mark(s,origin.add(Math.cos(a)*10,0,Math.sin(a)*10));}}
   case HANDS->{for(int f=2;f<=castReach;f+=2)mark(s,point(f,0));}
   case CLAWS,WARN,WAKE,PHASE->s.sendParticles(ParticleTypes.ASH,getX(),getY()+getBbHeight()*.65,getZ(),4,.6,.3,.6,.01);
   default->{}
  }
 }
 private boolean victim(LivingEntity e){return e!=this&&e.isAlive()&&participants.contains(e.getUUID())&&canAttack(e)&&!isAlliedTo(e)&&loadedSight(e);}
 private boolean loadedSight(LivingEntity target){
  double distance=distanceTo(target);if(distance>128)return false;
  // Conservative loaded rectangle includes every ray voxel, including diagonal chunk corners.
  for(int cx=net.minecraft.util.Mth.floor(Math.min(getX(),target.getX()))>>4;cx<=(net.minecraft.util.Mth.floor(Math.max(getX(),target.getX()))>>4);cx++)
   for(int cz=net.minecraft.util.Mth.floor(Math.min(getZ(),target.getZ()))>>4;cz<=(net.minecraft.util.Mth.floor(Math.max(getZ(),target.getZ()))>>4);cz++)if(!level().hasChunk(cx,cz))return false;
  return hasLineOfSight(target);
 }
 private List<LivingEntity> castVictims(){
  var result=new ArrayList<LivingEntity>();if(!(level() instanceof ServerLevel server))return result;
  double radius=boss()?128:5;
  for(UUID id:participants)if(server.getEntity(id) instanceof LivingEntity e&&e.position().distanceToSqr(origin)<=radius*radius&&victim(e))result.add(e);
  return result;
 }
 private void perform(){
  Action a=action();boolean hit=age==a.release||a==Action.ERUPTION&&(age==39||age==47)||a==Action.RING&&age>=37&&age<=68;
  if(a==Action.PULL&&age>=27&&age<=34){pull();return;}
  if(!hit||a.damage==0)return;
  for(var e:castVictims()){
   Vec3 d=e.position().subtract(origin);double f=d.dot(forward),side=d.x*(-forward.z)+d.z*forward.x;
   if(Math.abs(d.y)>2.5)continue;
   boolean in=switch(a){
    case CLAWS->d.horizontalDistance()<(boss()?5:getBbWidth()+e.getBbWidth()+1)&&f>0;
    case FRACTURE->MagmaCombat.fracture(f,side);
    case HANDS->f>1&&f<castReach+1&&Math.abs(side)<1;
    case ERUPTION->MagmaCombat.eruption(f,side,(age-31)/8);
    case RING->Math.abs(d.horizontalDistance()-MagmaCombat.ring(age))<.7&&Math.abs(d.y)<.65;
    default->false;
   };
   // Each cast can hit a participant once, including the three eruption pulses.
   if(in&&struck.add(e.getUUID())){
    float raw=boss()?a.damage:(float)getAttributeValue(Attributes.ATTACK_DAMAGE);
    // Vanilla mobAttack scales difficulty once in Player.hurt, then applies armor.
    e.hurt(damageSources().mobAttack(this),raw);
   }
  }
 }
 private void pull(){for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(10,2,10),this::victim)){
  Vec3 delta=origin.subtract(e.position()).multiply(1,0,1);if(delta.lengthSqr()<9||delta.lengthSqr()>100)continue;
  Vec3 force=delta.normalize().scale(.04),next=e.position().add(e.getDeltaMovement().multiply(6,0,6)).add(force.scale(6));BlockPos b=BlockPos.containing(next);
  // Check every traversed quarter-block, not only an endpoint across a lava gap.
  boolean safe=true;for(int i=1;i<=4;i++){BlockPos q=BlockPos.containing(e.position().lerp(next,i/4.0));if(!level().hasChunkAt(q)||!level().getWorldBorder().isWithinBounds(q)||!level().getFluidState(q).isEmpty()||!level().getFluidState(q.below()).isEmpty()||!level().getBlockState(q.below()).isFaceSturdy(level(),q.below(),Direction.UP)){safe=false;break;}}
  if(safe){for(int dx=-2;dx<=2&&safe;dx++)for(int dz=-2;dz<=2;dz++){var q=b.offset(dx,0,dz);if(!level().hasChunkAt(q)||!level().getFluidState(q.below()).isEmpty()||!level().getBlockState(q.below()).isFaceSturdy(level(),q.below(),Direction.UP)){safe=false;break;}}}
  if(safe&&level().noCollision(e,e.getBoundingBox().move(next.subtract(e.position()))))e.push(force.x,0,force.z);
 }}
 @Override protected void tickDeath(){
  if(!boss()){super.tickDeath();return;}
  deathTime++;if(!level().isClientSide)bar.removeAllPlayers();
  if(deathTime>=80&&!level().isClientSide){if(lastPlayer!=null&&level() instanceof ServerLevel s){var winner=s.getServer().getPlayerList().getPlayer(lastPlayer);if(winner!=null)MagmaRewards.reconcile(winner);}level().broadcastEntityEvent(this,(byte)60);remove(Entity.RemovalReason.KILLED);}
 }
 @Override public boolean doHurtTarget(Entity target){return false;}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(boss()&&!peacefulBoss()&&!isRemoved())bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected void dropCustomDeathLoot(ServerLevel s,DamageSource source,boolean recentlyHit){
  super.dropCustomDeathLoot(s,source,recentlyHit);
  if(!boss()||rewarded||home==null)return;
  UUID winner=source.getEntity() instanceof ServerPlayer p?p.getUUID():lastPlayer;
  if(winner==null||!MagmaEncounters.get(s).canClaim(home.asLong(),getUUID()))return;
  var pin=org.slavicmyths.losthalls.BossRewardInbox.prepare(s.getServer(),s.dimension().location()+":"+home.asLong(),"magma",winner,4+random.nextInt(3),s.getServer().overworld().getGameTime()+80);
  if(pin==null||!MagmaEncounters.get(s).claim(home.asLong(),getUUID(),pin.recipient(),pin.cores(),s.getGameTime()+80))return;winner=pin.recipient();
  rewarded=true;lastPlayer=winner;
  var player=s.getServer().getPlayerList().getPlayer(winner);if(player!=null)MagmaRewards.expect(player);
 }
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putBoolean("MagmaSecond",second());t.putBoolean("MagmaRewarded",rewarded);t.putInt("MagmaAnger",anger);t.putInt("MagmaAlarm",alarmCooldown);t.putInt("MagmaSequence",sequence);t.putInt("MagmaCooldown",cooldown);if(provoker!=null)t.putUUID("MagmaProvoker",provoker);if(lastPlayer!=null)t.putUUID("MagmaLastPlayer",lastPlayer);if(home!=null)t.putLong("MagmaHome",home.asLong());}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(SECOND,t.getBoolean("MagmaSecond")||boss()&&getHealth()<=425);rewarded=t.getBoolean("MagmaRewarded");anger=Math.clamp(t.getInt("MagmaAnger"),0,600);alarmCooldown=Math.clamp(t.getInt("MagmaAlarm"),0,200);sequence=Math.max(0,t.getInt("MagmaSequence"));cooldown=Math.max(40,Math.clamp(t.getInt("MagmaCooldown"),0,200));lastPlayer=t.hasUUID("MagmaLastPlayer")?t.getUUID("MagmaLastPlayer"):null;provoker=t.hasUUID("MagmaProvoker")?t.getUUID("MagmaProvoker"):null;if(provoker!=null)participants.add(provoker);home=t.contains("MagmaHome")?BlockPos.of(t.getLong("MagmaHome")):null;cancelCast();entityData.set(EXPOSED,0);journalChecked=false;}
}
