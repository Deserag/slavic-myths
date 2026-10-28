package org.slavicmyths.losthalls;

import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.losthalls.PostenEncounters.Phase;

/** One controller/body and four stable journal-backed clones, never five health pools. */
public final class PostenMob extends PathfinderMob {
 public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<PostenMob>> TYPE=ModEntities.ENTITIES.register("posten",()->EntityType.Builder.<PostenMob>of(PostenMob::new,MobCategory.MONSTER).sized(1.45F,3.9F).fireImmune().clientTrackingRange(14).updateInterval(2).build("slavicmyths:posten"));
 private static final EntityDataAccessor<Integer> MASK=SynchedEntityData.defineId(PostenMob.class,EntityDataSerializers.INT),CAST=SynchedEntityData.defineId(PostenMob.class,EntityDataSerializers.INT),STAGE=SynchedEntityData.defineId(PostenMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(PostenMob.class,EntityDataSerializers.LONG);
 private UUID arena,body,lastPlayer;private BlockPos home;private int clone=-1,cooldown=50,castAge,sequence,ending;private boolean registered;private Vec3 transit,castAim=Vec3.ZERO,poisonCenter=Vec3.ZERO;private long poisonUntil;
 private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.posten"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
 public PostenMob(EntityType<? extends PostenMob> t,Level w){super(t,w);xpReward=0;}
 public static void init(net.neoforged.bus.api.IEventBus bus){ModItems.ITEMS.register("posten_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(TYPE,0x242032,0x6ac2cf,new Item.Properties()));bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(TYPE.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,650).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.8).build()));}
 public boolean controller(){return clone<0;}public int mask(){return entityData.get(MASK);}public int stage(){return entityData.get(STAGE);}public int cast(){return entityData.get(CAST);}public float castTime(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
 public void bind(UUID id,BlockPos at){arena=id;body=getUUID();home=at.immutable();setPersistenceRequired();}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MASK,0);b.define(CAST,0);b.define(STAGE,0);b.define(START,0L);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new LookAtPlayerGoal(this,Player.class,24));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
 @Override protected boolean shouldDespawnInPeaceful(){return true;}
 @Override protected boolean shouldDropLoot(){return false;}
 @Override public boolean doHurtTarget(Entity e){return false;}
 @Override public boolean canAttack(LivingEntity e){return !(e instanceof PostenMob)&&!(e instanceof HallsMob)&&!(e instanceof TamableAnimal a&&a.isTame())&&!e.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()))&&super.canAttack(e);}
 @Override public boolean isAlliedTo(Entity e){return e instanceof PostenMob||e instanceof HallsMob||super.isAlliedTo(e);}
 @Override public boolean isPickable(){return !(controller()&&stage()==1)&&super.isPickable();}
 @Override public void remove(Entity.RemovalReason r){if(!level().isClientSide)bar.removeAllPlayers();super.remove(r);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(controller()&&level().getDifficulty()!=Difficulty.PEACEFUL)bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 @Override protected void actuallyHurt(DamageSource source,float amount){
  if(!(level() instanceof ServerLevel s)||arena==null||controller()&&stage()!=0||!controller()&&stage()!=1)return;
  var journal=PostenEncounters.get(s);var before=journal.snapshot(arena).orElse(null);if(before==null)return;
  float hp=getHealth();super.actuallyHurt(source,amount);float damage=Math.max(0,hp-getHealth());
  var applied=controller()?journal.damageBody(arena,getUUID(),damage):journal.damageCopy(arena,getUUID(),damage);
  if(source.getEntity() instanceof ServerPlayer player){lastPlayer=player.getUUID();if(body!=null&&s.getEntity(body) instanceof PostenMob owner)owner.lastPlayer=lastPlayer;}
  var state=journal.snapshot(arena).orElseThrow();
  if(controller()){setHealth(state.health());if(applied.splitNow()){entityData.set(STAGE,1);setInvisible(true);setTarget(null);entityData.set(CAST,0);}}
  else {float health=state.copies().stream().filter(c->c.entity().equals(getUUID())).findFirst().orElseThrow().health();setHealth(health);}
 }
 @Override public void tick(){
  if(!level().isClientSide&&(level().getDifficulty()==Difficulty.PEACEFUL||!level().dimension().equals(Level.NETHER))){discard();return;}
  super.tick();if(level().isClientSide){if(cast()>0&&cast()<5&&tickCount%4==0){double radius=mask()==2?3:1.5;for(int i=0;i<12;i++){double a=i*Math.PI/6;level().addParticle(mask()==1?net.minecraft.core.particles.ParticleTypes.FLAME:mask()==2?net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR:mask()==3?net.minecraft.core.particles.ParticleTypes.WITCH:net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,getX()+Math.cos(a)*radius,getY()+.1,getZ()+Math.sin(a)*radius,0,.015,0);}}return;}if(isRemoved()||!(level() instanceof ServerLevel s))return;
  if(arena==null){BlockPos at=blockPosition();bind(UUID.nameUUIDFromBytes(("posten:"+s.getSeed()+":"+at.asLong()).getBytes(StandardCharsets.UTF_8)),at);}
  if(home==null||body==null){discard();return;}
  var journal=PostenEncounters.get(s);
  if(controller()&&!registered){registered=true;if(!journal.register(arena,getUUID())&&journal.snapshot(arena).filter(e->e.body().equals(getUUID())).isEmpty()){discard();return;}}
  var state=journal.snapshot(arena).orElse(null);if(state==null||!body.equals(state.body())){discard();return;}
  if(controller()){
   entityData.set(STAGE,state.phase().ordinal());bar.setProgress(state.health()/650F);setInvisible(state.phase()==Phase.COPIES);
   if(state.phase()==Phase.DEFEATED){bar.removeAllPlayers();if(cast()!=6)begin(6);if(++ending>=80)deliver(s,journal);return;}
   if(state.phase()==Phase.COPIES){if(tickCount%20==0)reconcile(s,state);return;}
  }else{
   entityData.set(STAGE,1);var own=state.copies().stream().filter(c->c.entity().equals(getUUID())).findFirst().orElse(null);
   if(state.phase()!=Phase.COPIES||own==null||own.health()<=0){discard();return;}setHealth(own.health());
  }
  if(poisonUntil>s.getGameTime()&&tickCount%5==0){for(var e:s.getEntitiesOfClass(LivingEntity.class,new AABB(poisonCenter,poisonCenter).inflate(3,1.5,3),e->canAttack(e)&&!isAlliedTo(e)&&hasLineOfSight(e)))e.addEffect(new MobEffectInstance(MobEffects.POISON,40),this);s.sendParticles(net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR,poisonCenter.x,poisonCenter.y+.2,poisonCenter.z,5,2,.1,2,0);}
  if(cooldown>0)cooldown--;LivingEntity target=getTarget();
  if(target==null||!target.isAlive()||!canAttack(target)||distanceToSqr(target)>1024){setTarget(null);entityData.set(CAST,0);return;}
  if(lastPlayer==null&&target instanceof ServerPlayer player){lastPlayer=player.getUUID();if(body!=null&&s.getEntity(body) instanceof PostenMob owner&&owner.lastPlayer==null)owner.lastPlayer=lastPlayer;}
  getLookControl().setLookAt(target,25,25);
  if(home!=null&&distanceToSqr(Vec3.atCenterOf(home))>32*32){navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}
  if(cast()!=0){navigation.stop();castAge++;int release=mask()==0?24:mask()==1?20:28;
   if(cast()==5){if(castAge==16&&transit!=null&&safe(s,transit)){setPos(transit);transit=null;}if(castAge>=32){entityData.set(CAST,0);cooldown=60;}return;}
   if(castAge==release&&hasLineOfSight(target)&&distanceToSqr(target)<24*24)attack(s,target);
   if(castAge>=release+20){entityData.set(CAST,0);cooldown=50;}return;
  }
  if(cooldown==0&&hasLineOfSight(target)){
   if(controller()&&++sequence%4==0){transit=findWall(s);if(transit!=null){begin(5);return;}}
   if(controller())entityData.set(MASK,(mask()+1)%4);begin(mask()+1);
  }else if(tickCount%10==0&&distanceToSqr(target)>18*18)navigation.moveTo(target,.8);
 }
 private void begin(int action){if(!level().isClientSide&&action>=1&&action<=4)playSound(switch(action){case 1->net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE;case 2->net.minecraft.sounds.SoundEvents.FIRECHARGE_USE;case 3->net.minecraft.sounds.SoundEvents.SCULK_BLOCK_SPREAD;default->net.minecraft.sounds.SoundEvents.DEEPSLATE_BREAK;},.65F,action==1?.65F:action==4?.55F:1F);if(getTarget()!=null)castAim=getTarget().position();entityData.set(CAST,action);entityData.set(START,level().getGameTime());castAge=0;}
 private void attack(ServerLevel s,LivingEntity t){
  if(mask()==2){poisonCenter=castAim;poisonUntil=s.getGameTime()+40;return;}
  if(mask()==1){for(int i=-1;i<=1;i++){var b=HallsBolt.TYPE.get().create(s);if(b==null)continue;b.setOwner(this);b.mode(6);b.setPos(getX(),getY()+2.5,getZ());var d=t.getEyePosition().subtract(b.position()).normalize();b.setDeltaMovement(d.x*.45+i*.05,d.y*.45,d.z*.45-i*.05);s.addFreshEntity(b);}return;}
  for(var e:s.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(mask()==2?5:10,3,mask()==2?5:10),e->e!=this&&canAttack(e)&&!isAlliedTo(e)&&hasLineOfSight(e))){
   if(mask()==0)e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,0),this);
   if(mask()==2&&e.distanceToSqr(t)<9)e.addEffect(new MobEffectInstance(MobEffects.POISON,60,0),this);
   if(mask()==3&&e.position().subtract(position()).normalize().dot(getLookAngle())>.8&&e.hurt(damageSources().indirectMagic(this,this),6))e.addEffect(new MobEffectInstance(MobEffects.WITHER,40,0),this);
  }
  s.sendParticles(mask()==2?net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR:net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,t.getX(),t.getY()+1,t.getZ(),12,1,.4,1,.01);
 }
 private boolean safe(ServerLevel s,Vec3 at){BlockPos feet=BlockPos.containing(at);AABB box=getBoundingBox().move(at.subtract(position()));for(BlockPos p:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))if(!s.hasChunkAt(p)||!s.getWorldBorder().isWithinBounds(p)||!s.getFluidState(p).isEmpty())return false;return s.getBlockState(feet.below()).isFaceSturdy(s,feet.below(),Direction.UP)&&s.noCollision(this,box);}
 private Vec3 findWall(ServerLevel s){for(Direction d:Direction.Plane.HORIZONTAL)for(int distance=20;distance<=31;distance++){BlockPos p=home.relative(d,distance);if(!s.hasChunkAt(p)||!s.hasChunkAt(p.relative(d,2)))continue;Vec3 v=Vec3.atBottomCenterOf(p);if(!s.getBlockState(p.relative(d,2).above()).isAir()&&safe(s,v)&&distanceToSqr(v)>36)return v;}return null;}
 private void reconcile(ServerLevel s,PostenEncounters.Snapshot state){
  int cx=home.getX()>>4,cz=home.getZ()>>4;for(int x=cx-2;x<=cx+2;x++)for(int z=cz-2;z<=cz+2;z++)if(!s.hasChunk(x,z)||!s.areEntitiesLoaded(ChunkPos.asLong(x,z)))return;
  int i=0;for(var c:state.copies()){if(c.health()<=0||s.getEntity(c.entity())!=null){i++;continue;}BlockPos feet=home.offset(i%2==0?-4:4,0,i<2?-4:4);i++;Vec3 at=null;for(int dy:new int[]{0,1,-1,2,-2,3,-3}){Vec3 candidate=Vec3.atBottomCenterOf(feet.above(dy));if(safe(s,candidate)){at=candidate;break;}}if(at==null)continue;var copy=TYPE.get().create(s);if(copy==null)continue;copy.setUUID(c.entity());copy.arena=arena;copy.body=getUUID();copy.home=home;copy.clone=c.mask().ordinal();copy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(65);copy.entityData.set(MASK,copy.clone);copy.entityData.set(STAGE,1);copy.setHealth(c.health());copy.setPersistenceRequired();copy.moveTo(at.x,at.y,at.z,0,0);s.addFreshEntity(copy);}
 }
 private void deliver(ServerLevel s,PostenEncounters journal){
  var state=journal.snapshot(arena).orElseThrow();if(state.rewardsClaimed()){discard();return;}if(lastPlayer==null)return;
  var pin=BossRewardInbox.prepare(s.getServer(),"posten:"+arena,"posten",lastPlayer,0,s.getServer().overworld().getGameTime());if(pin==null)return;
  if(journal.claimRewards(arena,getUUID())){var player=s.getServer().getPlayerList().getPlayer(pin.recipient());if(player!=null)BossRewardInbox.reconcile(player);}discard();
 }
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(arena!=null)t.putUUID("PostenArena",arena);if(body!=null)t.putUUID("PostenBody",body);if(home!=null)t.putLong("PostenHome",home.asLong());if(lastPlayer!=null)t.putUUID("PostenWinner",lastPlayer);t.putInt("PostenClone",clone);t.putInt("PostenMask",mask());t.putInt("PostenEnding",ending);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);arena=t.hasUUID("PostenArena")?t.getUUID("PostenArena"):null;body=t.hasUUID("PostenBody")?t.getUUID("PostenBody"):null;home=t.contains("PostenHome")?BlockPos.of(t.getLong("PostenHome")):null;lastPlayer=t.hasUUID("PostenWinner")?t.getUUID("PostenWinner"):null;clone=t.contains("PostenClone")?Math.clamp(t.getInt("PostenClone"),-1,3):-1;if(clone>=0)getAttribute(Attributes.MAX_HEALTH).setBaseValue(65);entityData.set(MASK,Math.clamp(t.getInt("PostenMask"),0,3));entityData.set(STAGE,controller()?0:1);ending=Math.clamp(t.getInt("PostenEnding"),0,80);registered=false;}
}
