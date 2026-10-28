package org.slavicmyths.losthalls;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import org.slavicmyths.losthalls.HallsContent.Kind;

/** Five distinct roles; all decisions/inventory changes are server owned. */
public final class HallsMob extends TamableAnimal {
 private static final EntityDataAccessor<Integer> VARIANT=SynchedEntityData.defineId(HallsMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> CAST=SynchedEntityData.defineId(HallsMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(HallsMob.class,EntityDataSerializers.LONG);
 public final Kind kind;
 private int feedPulse;public int feedPulse(){return feedPulse;}
 @Override public void handleEntityEvent(byte event){if(event==73)feedPulse=12;else super.handleEntityEvent(event);}
 private int cooldown,anger,castAge,sequence;private long nextHarvest,echoUntil;private int spores;private net.minecraft.world.phys.Vec3 charge=net.minecraft.world.phys.Vec3.ZERO;private boolean struck;
 public HallsMob(EntityType<? extends HallsMob> t,Level w,Kind k){super(t,w);kind=k;xpReward=k==Kind.SHEPTUN||k==Kind.OSKOLNIK?10:0;if(k==Kind.GULOKRYL){moveControl=new FlyingMoveControl(this,20,true);navigation=new FlyingPathNavigation(this,w);setNoGravity(true);}}
 public static AttributeSupplier.Builder attributes(Kind k){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,k==Kind.OSKOLNIK?90:k==Kind.GULOKRYL?30:60).add(Attributes.MOVEMENT_SPEED,k==Kind.GRIBOSHMYG?.22:.25).add(Attributes.FLYING_SPEED,.38).add(Attributes.ATTACK_DAMAGE,k==Kind.OSKOLNIK?7:5).add(Attributes.FOLLOW_RANGE,24);}
 public int variant(){return entityData.get(VARIANT);}public int cast(){return entityData.get(CAST);}public float castTime(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(VARIANT,0);b.define(CAST,0);b.define(START,0L);}
 @Override protected void registerGoals(){
  goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new SitWhenOrderedToGoal(this));
  goalSelector.addGoal(2,new FollowOwnerGoal(this,1.1,5,2));
  goalSelector.addGoal(3,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));} public boolean canUse(){return getTarget()!=null&&!isOrderedToSit();}public void tick(){var t=getTarget();if(t==null)return;getLookControl().setLookAt(t,30,30);if(cast()!=0)navigation.stop();else if(distanceToSqr(t)>100)navigation.moveTo(t,1);else navigation.stop();}});
  goalSelector.addGoal(4,new PanicGoal(this,1.3){@Override public boolean canUse(){return kind==Kind.GRIBOSHMYG&&super.canUse();}});
  goalSelector.addGoal(5,new WaterAvoidingRandomFlyingGoal(this,.8){@Override public boolean canUse(){return kind==Kind.GULOKRYL&&!isOrderedToSit()&&super.canUse();}});
  goalSelector.addGoal(6,new WaterAvoidingRandomStrollGoal(this,.8){@Override public boolean canUse(){return kind!=Kind.GULOKRYL&&super.canUse();}});
  goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
  targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this));targetSelector.addGoal(2,new OwnerHurtTargetGoal(this));
  targetSelector.addGoal(3,new HurtByTargetGoal(this){@Override public boolean canUse(){return kind!=Kind.GRIBOSHMYG&&super.canUse();}});
  targetSelector.addGoal(4,new NearestAttackableTargetGoal<>(this,Player.class,true){@Override public boolean canUse(){return (kind==Kind.SHEPTUN||kind==Kind.OSKOLNIK)&&super.canUse();}});
 }
 @Override public boolean canAttack(LivingEntity e){return kind!=Kind.GRIBOSHMYG&&e!=getOwner()&&!(e instanceof HallsMob)&&!(e instanceof TamableAnimal a&&a.isTame())&&!e.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()||isTame()))&&super.canAttack(e);}
 @Override public boolean isAlliedTo(Entity e){return e instanceof HallsMob||super.isAlliedTo(e);}
 @Override protected boolean shouldDespawnInPeaceful(){return kind==Kind.SHEPTUN||kind==Kind.OSKOLNIK;}
 @Override public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide&&source.getEntity() instanceof LivingEntity a&&canAttack(a)){anger=600;setTarget(a);}return hit;}
 @Override public boolean doHurtTarget(Entity e){return false;}
 private void begin(int cast){entityData.set(CAST,cast);entityData.set(START,level().getGameTime());castAge=0;struck=false;charge=getTarget()==null?net.minecraft.world.phys.Vec3.ZERO:getTarget().position().subtract(position()).multiply(1,0,1).normalize();navigation.stop();}
 @Override public void tick(){
  super.tick();if(level().isClientSide){if(feedPulse>0)feedPulse--;if(kind==Kind.EKHOVIK&&cast()!=0&&tickCount%3==0){double radius=castTime(0)<16?1:Math.min(8,(castTime(0)-16)*.65);for(int i=0;i<12;i++){double a=i*Math.PI/6;level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,getX()+Math.cos(a)*radius,getY()+.15,getZ()+Math.sin(a)*radius,0,0,0);}}return;}if(!isAlive())return;if(cooldown>0)cooldown--;
  if(kind==Kind.GULOKRYL)setNoGravity(true);
  if(kind==Kind.EKHOVIK&&anger>0&&--anger==0){setTarget(null);setLastHurtByMob(null);}
  var target=getTarget();if(target!=null&&(!target.isAlive()||!canAttack(target)||distanceToSqr(target)>32*32)){setTarget(null);target=null;}
  if(kind==Kind.EKHOVIK&&target==null&&tickCount%200==0){echoUntil=level().getGameTime()+80;begin(4);playSound(SoundEvents.AMETHYST_BLOCK_RESONATE,.6F,.65F);}
  if(kind==Kind.GRIBOSHMYG&&tickCount%140==0&&level() instanceof ServerLevel s)s.sendParticles(net.minecraft.core.particles.ParticleTypes.WARPED_SPORE,getX(),getY()+.6,getZ(),3,.4,.2,.4,0);
  if(cast()!=0){castAge++;if(castAge==16&&target!=null&&hasLineOfSight(target)){
    if(kind==Kind.EKHOVIK&&cast()==2){for(var victim:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(8,2,8),e->e==getTarget()&&canAttack(e)&&hasLineOfSight(e))){victim.hurt(damageSources().mobAttack(this),6);var d=victim.position().subtract(position());victim.knockback(.65,-d.x,-d.z);}}
    else if(kind!=Kind.GRIBOSHMYG&&kind!=Kind.EKHOVIK){shoot(target,kind==Kind.SHEPTUN?variant():kind==Kind.GULOKRYL?4:5,0);if(kind==Kind.OSKOLNIK&&sequence%2==0)shoot(target,5,.13);}
   }
   if(kind==Kind.EKHOVIK&&cast()==3&&castAge>16&&castAge<25){var next=position().add(charge.scale(.55));boolean safe=true;for(int i=1;i<=4;i++){BlockPos q=BlockPos.containing(position().lerp(next,i/4.0));if(!level().hasChunkAt(q)||!level().getWorldBorder().isWithinBounds(q)||!level().getFluidState(q).isEmpty()||!level().getFluidState(q.below()).isEmpty()||level().getBlockState(q.below()).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||!level().getBlockState(q.below()).isFaceSturdy(level(),q.below(),Direction.UP)){safe=false;break;}}if(safe&&level().noCollision(this,getBoundingBox().move(next.subtract(position())))){setDeltaMovement(charge.scale(.55));if(target!=null&&!struck&&distanceToSqr(target)<9&&hasLineOfSight(target)){target.hurt(damageSources().mobAttack(this),7);struck=true;}}else setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
   if(kind==Kind.EKHOVIK&&cast()==3&&(castAge>=25||target==null))setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
   if(castAge>=30){if(kind==Kind.EKHOVIK)setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);entityData.set(CAST,0);cooldown=kind==Kind.OSKOLNIK&&sequence%2==0?80:kind==Kind.GULOKRYL?65:50;}return;
  }
  if(target!=null&&cooldown==0&&!isOrderedToSit()&&distanceToSqr(target)<=256&&hasLineOfSight(target)){sequence++;begin(kind==Kind.EKHOVIK?sequence%2==0?3:2:1);}
 }
 private void shoot(LivingEntity target,int mode,double side){var bolt=HallsBolt.TYPE.get().create(level());if(bolt==null)return;bolt.setOwner(this);bolt.mode(mode);bolt.setPos(getX(),getY()+getBbHeight()*.65,getZ());var d=target.getEyePosition().subtract(bolt.position()).normalize();bolt.setDeltaMovement(d.x*.55-side,d.y*.55,d.z*.55+side);level().addFreshEntity(bolt);}
 @Override public InteractionResult mobInteract(Player p,InteractionHand h){ItemStack held=p.getItemInHand(h);long now=level().getGameTime();
  if(kind==Kind.GULOKRYL){
   if(!isTame()&&held.is(Items.BLAZE_POWDER)){if(!level().isClientSide){held.consume(1,p);if(random.nextInt(4)==0){tame(p);setPersistenceRequired();navigation.stop();}level().broadcastEntityEvent(this,(byte)(isTame()?7:6));}return InteractionResult.sidedSuccess(level().isClientSide);}
   if(isTame()&&isOwnedBy(p)&&held.is(Items.MAGMA_CREAM)&&getHealth()<getMaxHealth()){if(!level().isClientSide){heal(6);held.consume(1,p);}return InteractionResult.sidedSuccess(level().isClientSide);}
   if(isTame()&&isOwnedBy(p)&&held.isEmpty()){if(!level().isClientSide){setOrderedToSit(!isOrderedToSit());setTarget(null);navigation.stop();}return InteractionResult.sidedSuccess(level().isClientSide);}
  }
  if(kind==Kind.EKHOVIK&&getTarget()==null&&held.is(Items.GLASS_BOTTLE)&&now<echoUntil&&now>=nextHarvest){if(!level().isClientSide){p.setItemInHand(h,ItemUtils.createFilledResult(held,p,new ItemStack(HallsContent.item("echo_vial"))));nextHarvest=now+6000;echoUntil=0;setPersistenceRequired();}return InteractionResult.sidedSuccess(level().isClientSide);}
  if(kind==Kind.GRIBOSHMYG){
   if(spores>0&&now>=nextHarvest){if(!level().isClientSide){ItemStack reward=new ItemStack(HallsContent.item("spore_pouch"),spores);if(!p.addItem(reward))p.drop(reward,false);spores=0;nextHarvest=now+3600;}return InteractionResult.sidedSuccess(level().isClientSide);}
   if(spores==0&&now>=nextHarvest&&(held.is(Items.WARPED_FUNGUS)||held.is(Items.CRIMSON_FUNGUS))){if(!level().isClientSide){held.consume(1,p);spores=1+random.nextInt(2);setPersistenceRequired();level().broadcastEntityEvent(this,(byte)73);}return InteractionResult.sidedSuccess(level().isClientSide);}
  }return InteractionResult.PASS;
 }
 @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor w,DifficultyInstance d,MobSpawnType r,SpawnGroupData data){entityData.set(VARIANT,random.nextInt(4));return super.finalizeSpawn(w,d,r,data);}
 @Override public AgeableMob getBreedOffspring(ServerLevel w,AgeableMob other){return null;}
 @Override public boolean isFood(ItemStack s){return false;}
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("HallsVariant",variant());t.putInt("HallsCooldown",cooldown);t.putInt("HallsAnger",anger);t.putLong("HallsHarvest",nextHarvest);t.putLong("HallsEcho",echoUntil);t.putInt("HallsSpores",spores);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(VARIANT,Math.clamp(t.getInt("HallsVariant"),0,3));cooldown=Math.clamp(t.getInt("HallsCooldown"),30,100);anger=Math.clamp(t.getInt("HallsAnger"),0,600);nextHarvest=Math.max(0,t.getLong("HallsHarvest"));echoUntil=Math.max(0,t.getLong("HallsEcho"));spores=Math.clamp(t.getInt("HallsSpores"),0,2);entityData.set(CAST,0);setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
 @Override protected SoundEvent getHurtSound(DamageSource s){return kind==Kind.OSKOLNIK||kind==Kind.EKHOVIK?SoundEvents.DEEPSLATE_BREAK:SoundEvents.AMETHYST_BLOCK_HIT;}
 @Override protected SoundEvent getDeathSound(){return SoundEvents.SCULK_SHRIEKER_BREAK;}
}
