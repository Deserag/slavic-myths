package org.slavicmyths.entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.syncher.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
/** All combat timing and target decisions live on the server. Render interpolation is per entity. */
public final class WildlifeEntity extends PathfinderMob {
 public enum Kind { BEAR,CUB,WOLF,BOAR,STAG,DOE }
 public static final int IDLE=0,WALK=1,TROT=2,RUN=3,ALERT=4,ATTACK=5,STAND=6,SWIM=7,WINDUP=8,CHARGE=9,STUN=10,FLEE=11;
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> CLOCK=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> STYLE=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.INT);
 private final Kind kind;
 private int actionTicks,actionLength,recovery,alertTicks,fleeTicks,leapCooldown;
 private Vec3 chargeDirection=Vec3.ZERO;
 private LivingEntity danger;
 private long angerUntil;
 public float visualMove,visualMoveOld,visualRun,visualRunOld,visualStand,visualStandOld;
 public WildlifeEntity(EntityType<? extends WildlifeEntity> type,Level world,Kind kind){
  super(type,world);this.kind=kind;installGoals();getAttribute(Attributes.STEP_HEIGHT).setBaseValue(kind==Kind.BEAR?.9F:.6F);
 }
 public Kind kind(){return kind;}
 @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){
  SpawnGroupData result=super.finalizeSpawn(world,difficulty,reason,data);
  if(kind==Kind.BEAR&&(reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION)&&random.nextInt(3)==0){
   WildlifeEntity cub=org.slavicmyths.registry.ModEntities.BEAR_CUB.get().create(world.getLevel());
   if(cub!=null){cub.moveTo(getX()+1,getY(),getZ(),getYRot(),0);if(world.noCollision(cub))world.addFreshEntity(cub);}
  }
  return result;
 }
 public int motion(){return entityData.get(STATE);}
 public int attackStyle(){return entityData.get(STYLE);}
 public float actionProgress(float partial){return Math.min(1,(tickCount+partial-entityData.get(CLOCK))/(motion()==WINDUP?28F:24F));}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(STATE,0);builder.define(CLOCK,0);builder.define(STYLE,0);}
 @Override protected void registerGoals(){} // Mob's constructor runs before kind is initialized.
 private void installGoals(){
  goalSelector.addGoal(0,new FloatGoal(this));
  goalSelector.addGoal(1,new EncounterGoal());
  goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,kind==Kind.WOLF?1:.72));
  goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,10));
  goalSelector.addGoal(7,new RandomLookAroundGoal(this));
 }
 public static AttributeSupplier.Builder attributes(Kind k){
  return createMobAttributes().add(Attributes.MAX_HEALTH,k==Kind.BEAR?48:k==Kind.CUB?16:k==Kind.WOLF?24:k==Kind.BOAR?30:k==Kind.STAG?28:22)
   .add(Attributes.MOVEMENT_SPEED,k==Kind.BEAR?.25:k==Kind.CUB?.29:k==Kind.WOLF?.32:k==Kind.BOAR?.29:.34)
   .add(Attributes.ATTACK_DAMAGE,k==Kind.BEAR?7:k==Kind.WOLF?4:k==Kind.BOAR?6:k==Kind.STAG?5:1).add(Attributes.FOLLOW_RANGE,24);
 }
 private void state(int s){if(motion()!=s){entityData.set(STATE,s);entityData.set(CLOCK,tickCount);}}
 private void action(int s,int ticks){state(s);actionTicks=actionLength=ticks;getNavigation().stop();}
 private boolean valid(LivingEntity e){return e!=null&&e.isAlive()&&(!(e instanceof Player)||!((Player)e).isCreative()&&!e.isSpectator());}
 @Override public boolean hurt(DamageSource source,float amount){
  boolean hit=super.hurt(source,amount);
  if(hit&&!level().isClientSide&&source.getEntity() instanceof LivingEntity){
   LivingEntity attacker=(LivingEntity)source.getEntity();
   if(valid(attacker)){
    if(kind==Kind.CUB||kind==Kind.DOE){danger=attacker;fleeTicks=180;}
    else {setTarget(attacker);angerUntil=level().getGameTime()+400;}
    if(kind==Kind.WOLF||kind==Kind.CUB)for(WildlifeEntity ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a!=this&&(kind==Kind.CUB?a.kind==Kind.BEAR:a.kind==Kind.WOLF))){
     ally.setTarget(attacker);ally.angerUntil=level().getGameTime()+400;
    }
   }
  }return hit;
 }
 private void inspect(){
  Player player=level().getNearestPlayer(this,kind==Kind.BEAR?7:kind==Kind.BOAR?5:14);
  if(!valid(player))player=null;
  if(kind==Kind.CUB&&getTarget()==null&&fleeTicks==0){
   WildlifeEntity adult=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a.kind==Kind.BEAR).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
   if(adult!=null&&distanceToSqr(adult)>9)getNavigation().moveTo(adult,1.05);
  }
  if(kind==Kind.WOLF&&getTarget()==null&&(tickCount+getId())%100<20){
   LivingEntity prey=level().getEntitiesOfClass(net.minecraft.world.entity.animal.Sheep.class,getBoundingBox().inflate(16),a->a.isAlive()).stream().findFirst().orElse(null);
   if(prey!=null){setTarget(prey);angerUntil=level().getGameTime()+300;}
  }
  if(player==null||getTarget()!=null||fleeTicks>0)return;
  if(kind==Kind.DOE||kind==Kind.STAG){
   org.slavicmyths.item.FolkAccessoryItem.award(player,"wildlife_encounter");
   danger=player;alertTicks=20;fleeTicks=140;voice("alert",.4F);return;
  }
  if(kind==Kind.BEAR||kind==Kind.BOAR){
   double threshold=kind==Kind.BEAR?3.2:2.5;
   boolean cub=kind==Kind.BEAR&&!level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(8),a->a.kind==Kind.CUB).isEmpty();
   if(cub)threshold=6;
   if(!cub&&org.slavicmyths.item.FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.VELES_AMULET.get()))threshold*=.55;
   if(distanceToSqr(player)<threshold*threshold){setTarget(player);angerUntil=level().getGameTime()+220;action(kind==Kind.BEAR?STAND:WINDUP,kind==Kind.BEAR?45:28);voice(kind==Kind.BEAR?"roar":"alert",.65F);}
  }
 }
 @Override public void aiStep(){
  super.aiStep();
  if(level().isClientSide){
   visualMoveOld=visualMove;visualRunOld=visualRun;visualStandOld=visualStand;
   float target=Math.min(1,walkAnimation.speed()*2);
   visualMove+=(target-visualMove)*.18F;
   visualRun+=((motion()==RUN||motion()==CHARGE||motion()==FLEE?1:0)-visualRun)*.14F;
   visualStand+=((motion()==STAND?1:0)-visualStand)*.1F;
   return;
  }
  if(org.slavicmyths.artifact.ArtifactEvents.calm(this)||org.slavicmyths.artifact.ArtifactEvents.summoned(this)){actionTicks=fleeTicks=alertTicks=0;Vec3 motion=getDeltaMovement();state(motion.x*motion.x+motion.z*motion.z>.001?WALK:IDLE);return;}
  if(recovery>0)recovery--;if(leapCooldown>0)leapCooldown--;
  if(getTarget()!=null&&(!valid(getTarget())||level().getGameTime()>angerUntil||distanceToSqr(getTarget())>900))setTarget(null);
  if((tickCount+getId())%20==0&&actionTicks==0)inspect();
  if(actionTicks==0&&getTarget()==null&&fleeTicks==0){
   double speed=getDeltaMovement().x*getDeltaMovement().x+getDeltaMovement().z*getDeltaMovement().z;
   state(isInWaterOrBubble()?SWIM:speed>.045?RUN:speed>.007?TROT:speed>.0003?WALK:IDLE);
  }
 }
 private void soil(int count){
  if(level() instanceof ServerLevel)((ServerLevel)level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,level().getBlockState(blockPosition().below())),getX(),getY()+.12,getZ(),count,.3,.05,.3,.03);
 }
 private void strike(LivingEntity target,float multiplier,float knock){
  if(!valid(target)||distanceToSqr(target)>Math.pow(getBbWidth()+target.getBbWidth()+1,2)||!hasLineOfSight(target))return;
  int blockedBefore=target instanceof net.minecraft.server.level.ServerPlayer?((net.minecraft.server.level.ServerPlayer)target).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD)):0;
  target.hurt(damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*multiplier);
  if(knock>0)target.knockback(knock,getX()-target.getX(),getZ()-target.getZ());
  boolean blocking=target instanceof net.minecraft.server.level.ServerPlayer&&((net.minecraft.server.level.ServerPlayer)target).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD))>blockedBefore;
  if(blocking&&kind==Kind.BOAR&&target instanceof Player){
   org.slavicmyths.item.FolkAccessoryItem.award(target,"boar_charge_block");
   Player p=(Player)target;
   p.getCooldowns().addCooldown(p.getUseItem().getItem(),30);p.stopUsingItem();voice("impact",.75F);
  }
 }
 private class EncounterGoal extends Goal {
  EncounterGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){return actionTicks>0||getTarget()!=null||fleeTicks>0;}
  @Override public boolean canContinueToUse(){return canUse();}
  @Override public void tick(){
   if(org.slavicmyths.artifact.ArtifactEvents.calm(WildlifeEntity.this)||org.slavicmyths.artifact.ArtifactEvents.summoned(WildlifeEntity.this))return;
   LivingEntity target=getTarget();
   if(fleeTicks>0&&valid(danger)){
    if(alertTicks>0){alertTicks--;state(ALERT);getNavigation().stop();getLookControl().setLookAt(danger,20,20);return;}
    fleeTicks--;state(FLEE);
    if(tickCount%10==0){Vec3 away=position().subtract(danger.position()).normalize().scale(10);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,1.45);}
    if((kind==Kind.STAG||kind==Kind.DOE)&&onGround()&&!isInWater()&&leapCooldown==0&&distanceToSqr(danger)<64){
     setDeltaMovement(getDeltaMovement().add(0,.48,0));leapCooldown=70;
    }
    return;
   }else if(fleeTicks>0)fleeTicks=0;
   if(actionTicks>0){
    actionTicks--;
    if(motion()==CHARGE){
     if(horizontalCollision){action(STUN,35);voice("impact",.75F);soil(8);return;}
     double speed=.22+.46*(1-actionTicks/24.0);
     setDeltaMovement(chargeDirection.scale(speed).add(0,getDeltaMovement().y,0));
     if(tickCount%4==0)soil(3);
     if(valid(target)&&distanceToSqr(target)<3.5){strike(target,1.35F,1);action(STUN,20);return;}
    }else{
     getNavigation().stop();
     if(valid(target))getLookControl().setLookAt(target,20,20);
     if(motion()==WINDUP&&actionTicks%9==0)soil(3);
     if(motion()==ATTACK&&actionTicks==actionLength/2){strike(target,kind==Kind.BEAR&&attackStyle()==1?1.35F:1,kind==Kind.STAG?.6F:.15F);voice("attack",.55F);}
    }
    if(actionTicks==0){
     if(motion()==WINDUP&&valid(target)){
      chargeDirection=target.position().subtract(position()).multiply(1,0,1).normalize();action(CHARGE,24);voice("attack",.7F);
     }else{recovery=kind==Kind.BEAR?30:20;state(IDLE);}
    }return;
   }
   if(!valid(target))return;
   if(recovery>0){
    if(kind==Kind.WOLF&&tickCount%8==0){Vec3 away=position().subtract(target.position()).normalize().scale(3);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,.9);}
    else getNavigation().stop();return;
   }
   double distance=distanceToSqr(target);
   if(kind==Kind.BOAR&&distance<100&&distance>5&&!isInWater()){action(WINDUP,28);voice("alert",.65F);return;}
   if(distance<Math.pow(getBbWidth()+target.getBbWidth()+.8,2)&&hasLineOfSight(target)){
    entityData.set(STYLE,random.nextInt(3));action(ATTACK,kind==Kind.BEAR?30:20);
    if(kind==Kind.BEAR&&attackStyle()==2)setDeltaMovement(target.position().subtract(position()).normalize().scale(.25).add(0,.05,0));
   }else if(tickCount%8==0){
    double side=kind==Kind.WOLF?(getId()%3-1)*2.5:0;
    Vec3 delta=target.position().subtract(position()).normalize();
    getNavigation().moveTo(target.getX()-delta.z*side,target.getY(),target.getZ()+delta.x*side,1.3);state(RUN);
   }
  }
 }
 private SoundEvent sound(String suffix){return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",kind.name().toLowerCase(java.util.Locale.ROOT)+"_"+suffix));}
 private void voice(String suffix,float volume){SoundEvent s=sound(suffix);if(s!=null)playSound(s,volume,1);}
 @Override protected SoundEvent getAmbientSound(){return sound("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource source){return sound("hurt");}
 @Override protected SoundEvent getDeathSound(){return sound("death");}
 @Override public int getAmbientSoundInterval(){return kind==Kind.STAG||kind==Kind.DOE?700:500;}
 @Override protected float getSoundVolume(){return .45F;}
 @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!isInWater())voice("impact",kind==Kind.BEAR?.12F:.055F);}
 @Override protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","entities/"+kind.name().toLowerCase(java.util.Locale.ROOT)));}
 @Override public int getMaxSpawnClusterSize(){return kind==Kind.WOLF?4:kind==Kind.CUB?1:3;}
 @Override public boolean canBeLeashed(){return false;}
}
