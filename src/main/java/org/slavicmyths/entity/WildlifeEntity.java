package org.slavicmyths.entity;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.particles.*;
import net.minecraftforge.registries.ForgeRegistries;
/** All combat timing and target decisions live on the server. Render interpolation is per entity. */
public final class WildlifeEntity extends CreatureEntity {
 public enum Kind { BEAR,CUB,WOLF,BOAR,STAG,DOE }
 public static final int IDLE=0,WALK=1,TROT=2,RUN=3,ALERT=4,ATTACK=5,STAND=6,SWIM=7,WINDUP=8,CHARGE=9,STUN=10,FLEE=11;
 private static final DataParameter<Integer> STATE=EntityDataManager.defineId(WildlifeEntity.class,DataSerializers.INT);
 private static final DataParameter<Integer> CLOCK=EntityDataManager.defineId(WildlifeEntity.class,DataSerializers.INT);
 private static final DataParameter<Integer> STYLE=EntityDataManager.defineId(WildlifeEntity.class,DataSerializers.INT);
 private final Kind kind;
 private int actionTicks,actionLength,recovery,alertTicks,fleeTicks,leapCooldown;
 private Vector3d chargeDirection=Vector3d.ZERO;
 private LivingEntity danger;
 private long angerUntil;
 public float visualMove,visualMoveOld,visualRun,visualRunOld,visualStand,visualStandOld;
 public WildlifeEntity(EntityType<? extends WildlifeEntity> type,World world,Kind kind){
  super(type,world);this.kind=kind;installGoals();maxUpStep=kind==Kind.BEAR?.9F:.6F;
 }
 public Kind kind(){return kind;}
 @Override public ILivingEntityData finalizeSpawn(net.minecraft.world.IServerWorld world,net.minecraft.world.DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,net.minecraft.nbt.CompoundNBT nbt){
  ILivingEntityData result=super.finalizeSpawn(world,difficulty,reason,data,nbt);
  if(kind==Kind.BEAR&&(reason==SpawnReason.NATURAL||reason==SpawnReason.CHUNK_GENERATION)&&random.nextInt(3)==0){
   WildlifeEntity cub=org.slavicmyths.registry.ModEntities.BEAR_CUB.get().create(world.getLevel());
   if(cub!=null){cub.moveTo(getX()+1,getY(),getZ(),yRot,0);if(world.noCollision(cub))world.addFreshEntity(cub);}
  }
  return result;
 }
 public int motion(){return entityData.get(STATE);}
 public int attackStyle(){return entityData.get(STYLE);}
 public float actionProgress(float partial){return Math.min(1,(tickCount+partial-entityData.get(CLOCK))/(motion()==WINDUP?28F:24F));}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(STATE,0);entityData.define(CLOCK,0);entityData.define(STYLE,0);}
 @Override protected void registerGoals(){} // Mob's constructor runs before kind is initialized.
 private void installGoals(){
  goalSelector.addGoal(0,new SwimGoal(this));
  goalSelector.addGoal(1,new EncounterGoal());
  goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,kind==Kind.WOLF?1:.72));
  goalSelector.addGoal(6,new LookAtGoal(this,PlayerEntity.class,10));
  goalSelector.addGoal(7,new LookRandomlyGoal(this));
 }
 public static AttributeModifierMap.MutableAttribute attributes(Kind k){
  return createMobAttributes().add(Attributes.MAX_HEALTH,k==Kind.BEAR?48:k==Kind.CUB?16:k==Kind.WOLF?24:k==Kind.BOAR?30:k==Kind.STAG?28:22)
   .add(Attributes.MOVEMENT_SPEED,k==Kind.BEAR?.25:k==Kind.CUB?.29:k==Kind.WOLF?.32:k==Kind.BOAR?.29:.34)
   .add(Attributes.ATTACK_DAMAGE,k==Kind.BEAR?7:k==Kind.WOLF?4:k==Kind.BOAR?6:k==Kind.STAG?5:1).add(Attributes.FOLLOW_RANGE,24);
 }
 private void state(int s){if(motion()!=s){entityData.set(STATE,s);entityData.set(CLOCK,tickCount);}}
 private void action(int s,int ticks){state(s);actionTicks=actionLength=ticks;getNavigation().stop();}
 private boolean valid(LivingEntity e){return e!=null&&e.isAlive()&&(!(e instanceof PlayerEntity)||!((PlayerEntity)e).isCreative()&&!e.isSpectator());}
 @Override public boolean hurt(DamageSource source,float amount){
  boolean hit=super.hurt(source,amount);
  if(hit&&!level.isClientSide&&source.getEntity() instanceof LivingEntity){
   LivingEntity attacker=(LivingEntity)source.getEntity();
   if(valid(attacker)){
    if(kind==Kind.CUB||kind==Kind.DOE){danger=attacker;fleeTicks=180;}
    else {setTarget(attacker);angerUntil=level.getGameTime()+400;}
    if(kind==Kind.WOLF||kind==Kind.CUB)for(WildlifeEntity ally:level.getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a!=this&&(kind==Kind.CUB?a.kind==Kind.BEAR:a.kind==Kind.WOLF))){
     ally.setTarget(attacker);ally.angerUntil=level.getGameTime()+400;
    }
   }
  }return hit;
 }
 private void inspect(){
  PlayerEntity player=level.getNearestPlayer(this,kind==Kind.BEAR?7:kind==Kind.BOAR?5:14);
  if(!valid(player))player=null;
  if(kind==Kind.CUB&&getTarget()==null&&fleeTicks==0){
   WildlifeEntity adult=level.getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a.kind==Kind.BEAR).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
   if(adult!=null&&distanceToSqr(adult)>9)getNavigation().moveTo(adult,1.05);
  }
  if(kind==Kind.WOLF&&getTarget()==null&&(tickCount+getId())%100<20){
   LivingEntity prey=level.getEntitiesOfClass(net.minecraft.entity.passive.SheepEntity.class,getBoundingBox().inflate(16),a->a.isAlive()).stream().findFirst().orElse(null);
   if(prey!=null){setTarget(prey);angerUntil=level.getGameTime()+300;}
  }
  if(player==null||getTarget()!=null||fleeTicks>0)return;
  if(kind==Kind.DOE||kind==Kind.STAG){
   org.slavicmyths.item.FolkAccessoryItem.award(player,"wildlife_encounter");
   danger=player;alertTicks=20;fleeTicks=140;voice("alert",.4F);return;
  }
  if(kind==Kind.BEAR||kind==Kind.BOAR){
   double threshold=kind==Kind.BEAR?3.2:2.5;
   boolean cub=kind==Kind.BEAR&&!level.getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(8),a->a.kind==Kind.CUB).isEmpty();
   if(cub)threshold=6;
   if(!cub&&org.slavicmyths.item.FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.VELES_AMULET.get()))threshold*=.55;
   if(distanceToSqr(player)<threshold*threshold){setTarget(player);angerUntil=level.getGameTime()+220;action(kind==Kind.BEAR?STAND:WINDUP,kind==Kind.BEAR?45:28);voice(kind==Kind.BEAR?"roar":"alert",.65F);}
  }
 }
 @Override public void aiStep(){
  super.aiStep();
  if(level.isClientSide){
   visualMoveOld=visualMove;visualRunOld=visualRun;visualStandOld=visualStand;
   float target=Math.min(1,animationSpeed*2);
   visualMove+=(target-visualMove)*.18F;
   visualRun+=((motion()==RUN||motion()==CHARGE||motion()==FLEE?1:0)-visualRun)*.14F;
   visualStand+=((motion()==STAND?1:0)-visualStand)*.1F;
   return;
  }
  if(recovery>0)recovery--;if(leapCooldown>0)leapCooldown--;
  if(getTarget()!=null&&(!valid(getTarget())||level.getGameTime()>angerUntil||distanceToSqr(getTarget())>900))setTarget(null);
  if((tickCount+getId())%20==0&&actionTicks==0)inspect();
  if(actionTicks==0&&getTarget()==null&&fleeTicks==0){
   double speed=getDeltaMovement().x*getDeltaMovement().x+getDeltaMovement().z*getDeltaMovement().z;
   state(isInWaterOrBubble()?SWIM:speed>.045?RUN:speed>.007?TROT:speed>.0003?WALK:IDLE);
  }
 }
 private void soil(int count){
  if(level instanceof ServerWorld)((ServerWorld)level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK,level.getBlockState(blockPosition().below())),getX(),getY()+.12,getZ(),count,.3,.05,.3,.03);
 }
 private void strike(LivingEntity target,float multiplier,float knock){
  if(!valid(target)||distanceToSqr(target)>Math.pow(getBbWidth()+target.getBbWidth()+1,2)||!canSee(target))return;
  int blockedBefore=target instanceof net.minecraft.entity.player.ServerPlayerEntity?((net.minecraft.entity.player.ServerPlayerEntity)target).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD)):0;
  target.hurt(DamageSource.mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*multiplier);
  if(knock>0)target.knockback(knock,getX()-target.getX(),getZ()-target.getZ());
  boolean blocking=target instanceof net.minecraft.entity.player.ServerPlayerEntity&&((net.minecraft.entity.player.ServerPlayerEntity)target).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD))>blockedBefore;
  if(blocking&&kind==Kind.BOAR&&target instanceof PlayerEntity){
   org.slavicmyths.item.FolkAccessoryItem.award(target,"boar_charge_block");
   PlayerEntity p=(PlayerEntity)target;
   p.getCooldowns().addCooldown(p.getUseItem().getItem(),30);p.stopUsingItem();voice("impact",.75F);
  }
 }
 private class EncounterGoal extends Goal {
  EncounterGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){return actionTicks>0||getTarget()!=null||fleeTicks>0;}
  @Override public boolean canContinueToUse(){return canUse();}
  @Override public void tick(){
   LivingEntity target=getTarget();
   if(fleeTicks>0&&valid(danger)){
    if(alertTicks>0){alertTicks--;state(ALERT);getNavigation().stop();getLookControl().setLookAt(danger,20,20);return;}
    fleeTicks--;state(FLEE);
    if(tickCount%10==0){Vector3d away=position().subtract(danger.position()).normalize().scale(10);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,1.45);}
    if((kind==Kind.STAG||kind==Kind.DOE)&&isOnGround()&&!isInWater()&&leapCooldown==0&&distanceToSqr(danger)<64){
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
    if(kind==Kind.WOLF&&tickCount%8==0){Vector3d away=position().subtract(target.position()).normalize().scale(3);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,.9);}
    else getNavigation().stop();return;
   }
   double distance=distanceToSqr(target);
   if(kind==Kind.BOAR&&distance<100&&distance>5&&!isInWater()){action(WINDUP,28);voice("alert",.65F);return;}
   if(distance<Math.pow(getBbWidth()+target.getBbWidth()+.8,2)&&canSee(target)){
    entityData.set(STYLE,random.nextInt(3));action(ATTACK,kind==Kind.BEAR?30:20);
    if(kind==Kind.BEAR&&attackStyle()==2)setDeltaMovement(target.position().subtract(position()).normalize().scale(.25).add(0,.05,0));
   }else if(tickCount%8==0){
    double side=kind==Kind.WOLF?(getId()%3-1)*2.5:0;
    Vector3d delta=target.position().subtract(position()).normalize();
    getNavigation().moveTo(target.getX()-delta.z*side,target.getY(),target.getZ()+delta.x*side,1.3);state(RUN);
   }
  }
 }
 private SoundEvent sound(String suffix){return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("slavicmyths",kind.name().toLowerCase(java.util.Locale.ROOT)+"_"+suffix));}
 private void voice(String suffix,float volume){SoundEvent s=sound(suffix);if(s!=null)playSound(s,volume,1);}
 @Override protected SoundEvent getAmbientSound(){return sound("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource source){return sound("hurt");}
 @Override protected SoundEvent getDeathSound(){return sound("death");}
 @Override public int getAmbientSoundInterval(){return kind==Kind.STAG||kind==Kind.DOE?700:500;}
 @Override protected float getSoundVolume(){return .45F;}
 @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState block){if(!isInWater())voice("impact",kind==Kind.BEAR?.12F:.055F);}
 @Override protected ResourceLocation getDefaultLootTable(){return new ResourceLocation("slavicmyths","entities/"+kind.name().toLowerCase(java.util.Locale.ROOT));}
 @Override public int getMaxSpawnClusterSize(){return kind==Kind.WOLF?4:kind==Kind.CUB?1:3;}
 @Override public boolean canBeLeashed(PlayerEntity player){return false;}
}
