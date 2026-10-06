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
 private static final EntityDataAccessor<Integer> LENGTH=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> STYLE=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> MOTHER=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Boolean> FAWN=SynchedEntityData.defineId(WildlifeEntity.class,EntityDataSerializers.BOOLEAN);
 private final net.minecraft.server.level.ServerBossEvent motherBar=new net.minecraft.server.level.ServerBossEvent(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.mother_bear"),net.minecraft.world.BossEvent.BossBarColor.RED,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
 private static final class HerdGroup implements SpawnGroupData {
  final UUID id;UUID babyParent;int babies;
  HerdGroup(UUID id){this.id=id;}
 }
 private UUID herdId;private boolean familyCreated;
 private UUID parentId;private net.minecraft.core.BlockPos territory;private boolean rage;private int pendingFamily=-1;
 private final Kind kind;
 private int actionTicks,actionLength,recovery,alertTicks,fleeTicks,leapCooldown;
 private Vec3 chargeDirection=Vec3.ZERO;
 private LivingEntity danger;
 private long angerUntil;
 private int packRole,packSize;private Vec3 herdEscape=Vec3.ZERO;
 public float visualMove,visualMoveOld,visualRun,visualRunOld,visualStand,visualStandOld;
 public WildlifeEntity(EntityType<? extends WildlifeEntity> type,Level world,Kind kind){
  super(type,world);this.kind=kind;installGoals();motherBar.setVisible(false);getAttribute(Attributes.STEP_HEIGHT).setBaseValue(kind==Kind.BEAR?.9F:.6F);
 }
 public Kind kind(){return kind;}
 @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){
  SpawnGroupData result=super.finalizeSpawn(world,difficulty,reason,data);
  territory=blockPosition();
  if(kind==Kind.BEAR&&(reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION)){
   var plan=WildlifeFamilyPlan.bear(random,true);if(plan.babies()>0)pendingFamily=plan.babies();
  }
  if(kind==Kind.STAG||kind==Kind.DOE){
   HerdGroup group=data instanceof HerdGroup existing?existing:new HerdGroup(getUUID());herdId=group.id;
   if(kind==Kind.DOE&&(reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION)){
    if(data==null&&random.nextInt(10)==0){group.babyParent=getUUID();group.babies=random.nextInt(100)<35?2:1;familyCreated=true;}
    else if(group.babyParent!=null&&group.babies>0){fawn(group.babyParent);group.babies--;}
   }
   return group;
  }
  return result;
 }
 public boolean isMother(){return entityData.get(MOTHER);}
 public boolean isFawn(){return entityData.get(FAWN);}
 public UUID parentId(){return parentId;}
 @Override public boolean isBaby(){return kind==Kind.CUB||isFawn();}
 public void fawn(UUID mother){parentId=mother;entityData.set(FAWN,true);getAttribute(Attributes.MAX_HEALTH).setBaseValue(12);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0);setHealth(12);refreshDimensions();}
 @Override protected EntityDimensions getDefaultDimensions(Pose pose){var size=super.getDefaultDimensions(pose);return isFawn()?size.scale(.55F):size;}
 @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(FAWN.equals(key))refreshDimensions();}
 /** Prepare every child before adding any. Failed insertion rolls back every child. */
 public boolean family(int count){
  if(familyCreated||isFawn()||!(level() instanceof ServerLevel world)||count<1||count>2||kind!=Kind.BEAR&&kind!=Kind.DOE)return false;
  var children=new ArrayList<WildlifeEntity>();
  for(int i=0;i<count;i++){
   var child=(kind==Kind.BEAR?org.slavicmyths.registry.ModEntities.BEAR_CUB:org.slavicmyths.registry.ModEntities.DOE).get().create(world);if(child==null)return false;
   boolean found=false;
   for(int attempt=0;attempt<16;attempt++){
    double angle=attempt*Math.PI/8;double radius=2.2+i*.8;child.moveTo(getX()+Math.cos(angle)*radius,getY(),getZ()+Math.sin(angle)*radius,getYRot(),0);
    var feet=child.blockPosition();
    if(!world.hasChunkAt(feet)||!world.getBlockState(feet.below()).isSolid()||!world.getFluidState(feet).isEmpty()||!world.noCollision(child)||children.stream().anyMatch(c->c.getBoundingBox().intersects(child.getBoundingBox())))continue;
    found=true;break;
   }
   if(!found)return false;
   child.parentId=getUUID();child.territory=blockPosition();if(kind==Kind.DOE){if(herdId==null)herdId=getUUID();child.herdId=herdId;}if(kind==Kind.DOE)child.fawn(getUUID());children.add(child);
  }
  for(var child:children)if(!world.addFreshEntity(child)){children.forEach(Entity::discard);return false;}
  familyCreated=true;
  if(kind==Kind.BEAR){entityData.set(MOTHER,true);getAttribute(Attributes.MAX_HEALTH).setBaseValue(120);getAttribute(Attributes.ARMOR).setBaseValue(6);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8);setHealth(120);}
  return true;
 }
 public int motion(){return entityData.get(STATE);}
 public int attackStyle(){return entityData.get(STYLE);}
 public float actionProgress(float partial){int elapsed=(int)level().getGameTime()-entityData.get(CLOCK);return net.minecraft.util.Mth.clamp((elapsed+partial)/Math.max(1,entityData.get(LENGTH)),0,1);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(STATE,0);builder.define(CLOCK,0);builder.define(LENGTH,20);builder.define(STYLE,0);builder.define(MOTHER,false);builder.define(FAWN,false);}
 @Override protected void registerGoals(){} // Mob's constructor runs before kind is initialized.
 private void installGoals(){
  goalSelector.addGoal(0,new FloatGoal(this));
  goalSelector.addGoal(1,new EncounterGoal());
  goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,kind==Kind.WOLF?1:.72));
  goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,10));
  goalSelector.addGoal(7,new RandomLookAroundGoal(this));
 }
 public static AttributeSupplier.Builder attributes(Kind k){
  return createMobAttributes().add(Attributes.MAX_HEALTH,k==Kind.BEAR?50:k==Kind.CUB?12:k==Kind.WOLF?30:k==Kind.BOAR?36:k==Kind.STAG?26:22)
   .add(Attributes.MOVEMENT_SPEED,k==Kind.BEAR?.25:k==Kind.CUB?.29:k==Kind.WOLF?.32:k==Kind.BOAR?.28:.34)
   .add(Attributes.ARMOR,k==Kind.BEAR?3:k==Kind.WOLF?1:k==Kind.BOAR?2:0)
   .add(Attributes.ATTACK_DAMAGE,k==Kind.BEAR?7:k==Kind.WOLF?5:k==Kind.BOAR?4:k==Kind.STAG?4:0).add(Attributes.FOLLOW_RANGE,k==Kind.BOAR?20:24);
 }
 private void state(int s){if(motion()!=s){entityData.set(STATE,s);entityData.set(CLOCK,(int)level().getGameTime());}}
 private void action(int s,int ticks){state(s);actionTicks=actionLength=ticks;entityData.set(CLOCK,(int)level().getGameTime());entityData.set(LENGTH,ticks);getNavigation().stop();}
 private boolean valid(LivingEntity e){return e!=null&&e.isAlive()&&(!(e instanceof Player)||!((Player)e).isCreative()&&!e.isSpectator());}
 @Override public boolean hurt(DamageSource source,float amount){
  boolean hit=super.hurt(source,amount);
  if(hit&&!level().isClientSide&&source.getEntity() instanceof LivingEntity){
   LivingEntity attacker=(LivingEntity)source.getEntity();
   if(valid(attacker)){
    if(kind==Kind.CUB||kind==Kind.DOE||kind==Kind.STAG&&distanceToSqr(attacker)>9){danger=attacker;fleeTicks=180;setTarget(null);}
    else {setTarget(attacker);angerUntil=level().getGameTime()+400;}
    if(kind==Kind.CUB&&parentId!=null&&level() instanceof ServerLevel world&&world.getEntity(parentId) instanceof WildlifeEntity parent&&parent.isMother()&&distanceToSqr(parent)<=32*32){
     parent.setTarget(attacker);parent.angerUntil=level().getGameTime()+400;parent.rage=true;parent.action(STAND,20);
    }
    if(kind==Kind.WOLF||kind==Kind.CUB)for(WildlifeEntity ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a!=this&&(kind==Kind.CUB?a.kind==Kind.BEAR:a.kind==Kind.WOLF))){
     if(kind==Kind.CUB&&parentId!=null&&!parentId.equals(ally.getUUID()))continue;
     ally.setTarget(attacker);ally.angerUntil=level().getGameTime()+400;ally.rage=ally.isMother();
     if(kind==Kind.CUB)ally.action(STAND,20);
    }
    if(kind==Kind.STAG||kind==Kind.DOE)for(var ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a!=this&&(a.kind==Kind.STAG||a.kind==Kind.DOE))){ally.danger=attacker;ally.fleeTicks=160;ally.alertTicks=5+ally.getId()%10;}
    if(kind==Kind.WOLF&&getHealth()<getMaxHealth()*.35F){danger=attacker;fleeTicks=100;setTarget(null);}
   }
  }return hit;
 }
 private void inspect(){
  Player player=level().getNearestPlayer(this,isMother()?21:kind==Kind.BEAR?7:kind==Kind.BOAR?5:14);
  if(!valid(player))player=null;
  if((kind==Kind.CUB||isFawn())&&getTarget()==null&&fleeTicks==0){
   WildlifeEntity adult=parentId!=null&&level() instanceof ServerLevel w&&w.getEntity(parentId) instanceof WildlifeEntity a?a:null;
   if(adult!=null&&distanceToSqr(adult)>9)getNavigation().moveTo(adult,1.05);
   if(player!=null&&distanceToSqr(player)<49){danger=player;fleeTicks=100;}
   return;
  }
  if((kind==Kind.STAG||kind==Kind.DOE)&&herdId!=null&&player==null&&fleeTicks==0&&getTarget()==null){
   var herd=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(24),a->a!=this&&herdId.equals(a.herdId)&&!a.isFawn()&&a.isAlive());
   if(!herd.isEmpty()){
    Vec3 center=Vec3.ZERO;for(var member:herd)center=center.add(member.position());center=center.scale(1.0/herd.size());
    if(position().distanceToSqr(center)>36)getNavigation().moveTo(center.x,center.y,center.z,.85);
   }
  }
  if(kind==Kind.WOLF&&getTarget()==null&&(tickCount+getId())%100<20){
   LivingEntity prey=level().getEntitiesOfClass(net.minecraft.world.entity.animal.Sheep.class,getBoundingBox().inflate(16),a->a.isAlive()).stream().findFirst().orElse(null);
   if(prey!=null){setTarget(prey);angerUntil=level().getGameTime()+300;}
  }
  if(player==null||getTarget()!=null||fleeTicks>0)return;
  if(kind==Kind.DOE||kind==Kind.STAG){
   org.slavicmyths.item.FolkAccessoryItem.award(player,"wildlife_encounter");
   danger=player;alertTicks=20;fleeTicks=140;herdEscape=position().subtract(player.position()).multiply(1,0,1).normalize();
   for(var ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(12),a->a!=this&&(a.kind==Kind.STAG||a.kind==Kind.DOE))){ally.danger=player;ally.herdEscape=herdEscape;ally.alertTicks=5+ally.getId()%12;ally.fleeTicks=140;}
   voice("alert",.4F);return;
  }
  if(kind==Kind.BEAR||kind==Kind.BOAR){
   double threshold=kind==Kind.BEAR?3.2:2.5;
   var cubs=isMother()?level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a.kind==Kind.CUB&&getUUID().equals(a.parentId)):List.<WildlifeEntity>of();
   Player intruder=player;boolean cub=isMother()&&cubs.stream().anyMatch(a->a.distanceToSqr(intruder)<49);
   if(cub)threshold=14;
   if(!cub&&org.slavicmyths.item.FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.VELES_AMULET.get()))threshold*=.55;
   if(cub||distanceToSqr(player)<threshold*threshold){setTarget(player);angerUntil=level().getGameTime()+220;action(kind==Kind.BEAR?STAND:WINDUP,kind==Kind.BEAR?25:12);voice(kind==Kind.BEAR?"roar":"alert",.65F);}
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
  if(territory==null)territory=blockPosition();
  if(pendingFamily>0){int count=pendingFamily;pendingFamily=-1;family(count);}
  if(isMother()){
   motherBar.setVisible(getTarget()!=null);motherBar.setProgress(getHealth()/getMaxHealth());
   if(getHealth()<getMaxHealth()*.5F)rage=true;
  }
  if(org.slavicmyths.artifact.ArtifactEvents.calm(this)||org.slavicmyths.artifact.ArtifactEvents.summoned(this)){actionTicks=fleeTicks=alertTicks=0;Vec3 motion=getDeltaMovement();state(motion.x*motion.x+motion.z*motion.z>.001?WALK:IDLE);return;}
  if(recovery>0)recovery--;if(leapCooldown>0)leapCooldown--;
  if(getTarget()!=null&&(!valid(getTarget())||level().getGameTime()>angerUntil||distanceToSqr(getTarget())>24*24||territory.distSqr(blockPosition())>32*32)){
   setTarget(null);actionTicks=0;state(IDLE);getNavigation().moveTo(territory.getX()+.5,territory.getY(),territory.getZ()+.5,.9);
  }
  if((tickCount+getId())%20==0&&actionTicks==0)inspect();
  if(kind==Kind.WOLF&&getTarget()!=null&&(tickCount+getId())%20==0){
   var pack=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(16),a->a.kind==Kind.WOLF&&a.getTarget()==getTarget()&&a.isAlive());
   pack.sort(Comparator.comparingInt(Entity::getId));packRole=Math.max(0,pack.indexOf(this))%4;
   if(packSize>pack.size()&&random.nextBoolean()){danger=getTarget();fleeTicks=100;setTarget(null);}packSize=pack.size();
  }
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
  float damage=(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*multiplier;
  if(kind==Kind.BOAR&&motion()==CHARGE)damage=7;
  if(isMother())damage=motion()==CHARGE?10:rage?9:8;
  target.hurt(damageSources().mobAttack(this),damage);
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
  @Override public boolean requiresUpdateEveryTick(){return true;}
  @Override public boolean canUse(){return actionTicks>0||getTarget()!=null||fleeTicks>0;}
  @Override public boolean canContinueToUse(){return canUse();}
  @Override public void tick(){
   if(org.slavicmyths.artifact.ArtifactEvents.calm(WildlifeEntity.this)||org.slavicmyths.artifact.ArtifactEvents.summoned(WildlifeEntity.this))return;
   LivingEntity target=getTarget();
   if(fleeTicks>0&&valid(danger)){
    if(alertTicks>0){alertTicks--;state(ALERT);getNavigation().stop();getLookControl().setLookAt(danger,20,20);return;}
    fleeTicks--;state(FLEE);
    if(tickCount%10==0){Vec3 away=herdEscape.lengthSqr()>0?herdEscape:position().subtract(danger.position()).multiply(1,0,1).normalize();var flee=net.minecraft.world.entity.ai.util.LandRandomPos.getPosAway(WildlifeEntity.this,10,3,position().subtract(away.scale(8)));if(flee!=null)getNavigation().moveTo(flee.x,flee.y,flee.z,1.45);}
    if((kind==Kind.STAG||kind==Kind.DOE)&&onGround()&&!isInWater()&&leapCooldown==0&&distanceToSqr(danger)<64){
     setDeltaMovement(getDeltaMovement().add(0,.48,0));leapCooldown=70;
    }
    return;
   }else if(fleeTicks>0)fleeTicks=0;
   if(actionTicks>0){
    actionTicks--;
    if(motion()==CHARGE){
     if(horizontalCollision){action(STUN,35);voice("impact",.75F);soil(8);return;}
     double speed=.22+.46*(1-actionTicks/(double)actionLength);
     setDeltaMovement(chargeDirection.scale(speed).add(0,getDeltaMovement().y,0));
     if(tickCount%4==0)soil(3);
     if(valid(target)&&distanceToSqr(target)<3.5){strike(target,1.35F,1);action(STUN,20);return;}
    }else{
     getNavigation().stop();
     if(valid(target))getLookControl().setLookAt(target,20,20);
     if(motion()==WINDUP&&actionTicks%9==0)soil(3);
     if(kind==Kind.WOLF&&motion()==ATTACK&&actionTicks==actionLength/2+2&&valid(target)&&hasLineOfSight(target)&&onGround()&&!isInWater()){
      Vec3 lunge=target.position().subtract(position()).multiply(1,0,1).normalize().scale(.35);
      if(level().noCollision(WildlifeEntity.this,getBoundingBox().move(lunge)))setDeltaMovement(lunge.add(0,.05,0));
     }
     if(motion()==ATTACK&&actionTicks==actionLength/2){strike(target,kind==Kind.BEAR&&attackStyle()==1?1.35F:1,kind==Kind.STAG?.6F:.15F);voice("attack",.55F);}
    }
    if(actionTicks==0){
     if(motion()==WINDUP&&valid(target)){
      chargeDirection=target.position().subtract(position()).multiply(1,0,1).normalize();setYRot((float)(Math.atan2(chargeDirection.z,chargeDirection.x)*180/Math.PI)-90);setYHeadRot(getYRot());action(CHARGE,12);voice("attack",.7F);
     }else{recovery=kind==Kind.BEAR?(rage?25:30):kind==Kind.WOLF?14:20;state(IDLE);}
    }return;
   }
   if(!valid(target))return;
   if(recovery>0){
    if(kind==Kind.WOLF&&tickCount%8==0){Vec3 away=position().subtract(target.position()).normalize().scale(3);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,.9);}
    else getNavigation().stop();return;
   }
   double distance=distanceToSqr(target);
   if(kind==Kind.STAG&&distance>9){danger=target;fleeTicks=120;setTarget(null);return;}
   if(isMother()&&distance>=25){
    if(tickCount%8!=0&&getNavigation().isInProgress()){state(RUN);return;}
    var cub=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a.kind==Kind.CUB&&getUUID().equals(a.parentId)).stream().min(Comparator.comparingDouble(a->a.distanceToSqr(target))).orElse(null);
    if(cub!=null){Vec3 guard=cub.position().add(target.position()).scale(.5);if(getNavigation().moveTo(guard.x,guard.y,guard.z,1.2)){state(RUN);return;}}
   }
   if((kind==Kind.BOAR||kind==Kind.BEAR)&&distance<100&&distance>5&&!isInWater()){action(WINDUP,kind==Kind.BEAR?20:12);voice("alert",.65F);return;}
   if(kind==Kind.WOLF&&packRole==3&&distance>4){
    if(tickCount%20==0){getNavigation().stop();state(ALERT);}return;
   }
   if(distance<Math.pow(getBbWidth()+target.getBbWidth()+.8,2)&&hasLineOfSight(target)){
    entityData.set(STYLE,random.nextInt(3));action(ATTACK,kind==Kind.BEAR?30:20);
    if(kind==Kind.BEAR&&attackStyle()==2)setDeltaMovement(target.position().subtract(position()).normalize().scale(.25).add(0,.05,0));
   }else if(tickCount%8==0){
    double side=kind==Kind.WOLF&&distance>16?(packRole==1?-3:packRole==2?3:0):0;
    Vec3 delta=target.position().subtract(position()).normalize();
    getNavigation().moveTo(target.getX()-delta.z*side,target.getY(),target.getZ()+delta.x*side,1.3);state(RUN);
   }
  }
 }
 @Override protected int getBaseExperienceReward(){return kind==Kind.CUB||isFawn()?0:isMother()?45:kind==Kind.BEAR?8:kind==Kind.WOLF?5:2;}
 @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("WildlifeFamilyCreated",familyCreated);if(herdId!=null)tag.putUUID("WildlifeHerd",herdId);tag.putBoolean("MotherBear",isMother());tag.putBoolean("DeerFawn",isFawn());tag.putBoolean("DefensiveRage",rage);if(parentId!=null)tag.putUUID("WildlifeParent",parentId);if(territory!=null)tag.putLong("WildlifeHome",territory.asLong());tag.putInt("PendingWildlifeFamily",pendingFamily);}
 @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){super.readAdditionalSaveData(tag);familyCreated=tag.getBoolean("WildlifeFamilyCreated")||tag.getBoolean("MotherBear");herdId=tag.hasUUID("WildlifeHerd")?tag.getUUID("WildlifeHerd"):null;entityData.set(MOTHER,tag.getBoolean("MotherBear"));entityData.set(FAWN,tag.getBoolean("DeerFawn"));rage=tag.getBoolean("DefensiveRage");parentId=tag.hasUUID("WildlifeParent")?tag.getUUID("WildlifeParent"):null;territory=tag.contains("WildlifeHome")?net.minecraft.core.BlockPos.of(tag.getLong("WildlifeHome")):null;pendingFamily=tag.contains("PendingWildlifeFamily")?tag.getInt("PendingWildlifeFamily"):-1;actionTicks=recovery=0;setTarget(null);state(IDLE);}
 @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);motherBar.addPlayer(player);}
 @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);motherBar.removePlayer(player);}
 @Override public void remove(RemovalReason reason){motherBar.removeAllPlayers();super.remove(reason);}
 private SoundEvent sound(String suffix){return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",kind.name().toLowerCase(java.util.Locale.ROOT)+"_"+suffix));}
 private void voice(String suffix,float volume){SoundEvent s=sound(suffix);if(s!=null)playSound(s,volume,1);}
 @Override protected SoundEvent getAmbientSound(){return sound("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource source){return sound("hurt");}
 @Override protected SoundEvent getDeathSound(){return sound("death");}
 @Override public int getAmbientSoundInterval(){return kind==Kind.STAG||kind==Kind.DOE?700:500;}
 @Override protected float getSoundVolume(){return .45F;}
 @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!isInWater())voice("impact",kind==Kind.BEAR?.12F:.055F);}
 @Override protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","entities/"+(isFawn()?"cub":isMother()?"mother_bear":kind.name().toLowerCase(java.util.Locale.ROOT))));}
 @Override public int getMaxSpawnClusterSize(){return kind==Kind.WOLF?4:kind==Kind.STAG||kind==Kind.DOE?5:kind==Kind.CUB?1:3;}
 @Override public boolean canBeLeashed(){return false;}
}
