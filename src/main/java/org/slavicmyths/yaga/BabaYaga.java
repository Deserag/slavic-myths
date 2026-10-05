package org.slavicmyths.yaga;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
/** Persistent home NPC. Player harm triggers refusal, never a hostile-boss lifecycle. */
public final class BabaYaga extends CreatureEntity {
 private static final DataParameter<Integer> GESTURE=EntityDataManager.defineId(BabaYaga.class,DataSerializers.INT),SINCE=EntityDataManager.defineId(BabaYaga.class,DataSerializers.INT);
 public BlockPos home;
 public BabaYaga(EntityType<? extends BabaYaga> t,World w){super(t,w);setPersistenceRequired();xpReward=0;}
 public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,12);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(GESTURE,0);entityData.define(SINCE,0);}
 public int gesture(){return entityData.get(GESTURE);}public float gestureTime(float partial){return Math.max(0,(int)level.getGameTime()-entityData.get(SINCE)+partial);}
 public void gesture(int id){if(!level.isClientSide){entityData.set(GESTURE,id);entityData.set(SINCE,(int)level.getGameTime());}}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE));}public boolean canUse(){return home!=null&&distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>9;}public void tick(){if(tickCount%20==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.65);}});goalSelector.addGoal(2,new LookAtGoal(this,PlayerEntity.class,8));goalSelector.addGoal(3,new LookRandomlyGoal(this));}
 @Override public boolean removeWhenFarAway(double distance){return false;}@Override public boolean canChangeDimensions(){return false;}@Override public boolean causeFallDamage(float d,float mult){return false;}@Override public void checkDespawn(){}
 @Override public void tick(){super.tick();if(!level.isClientSide){if(home==null)home=blockPosition();if(gesture()!=0&&gestureTime(0)>36)gesture(0);if(getHealth()<40)setHealth(40);}}
 public boolean canonical(){if(level.isClientSide)return true;YagaData d=YagaData.get((ServerWorld)level);return level.dimension()==World.OVERWORLD&&d.placed&&getUUID().equals(d.npc);}
 @Override protected ActionResultType mobInteract(PlayerEntity player,Hand hand){if(level.isClientSide)return ActionResultType.SUCCESS;if(!(player instanceof ServerPlayerEntity)||player.isSpectator()||!canonical())return ActionResultType.PASS;ServerPlayerEntity p=(ServerPlayerEntity)player;YagaData d=YagaData.get(p.getLevel());YagaData.Progress progress=d.progress(p.getUUID());long now=level.getGameTime();if(progress.blocked(now)){YagaServices.fail(p,"uninvited");gesture(3);return ActionResultType.FAIL;}
  if(progress.intro==0){progress.intro=1;d.setDirty();YagaServices.award(p,"find_the_hut");p.displayClientMessage(new TranslationTextComponent("yaga.intro.wait"),false);playSound(org.slavicmyths.registry.ModSounds.YAGA_HUT_CREAK.get(),.7F,.5F);gesture(1);return ActionResultType.CONSUME;}
  if(progress.intro==1){progress.intro=2;d.setDirty();p.displayClientMessage(new TranslationTextComponent("yaga.intro.welcome"),false);playSound(org.slavicmyths.registry.ModSounds.YAGA_HUT_STEP.get(),.8F,.55F);((ServerWorld)level).sendParticles(ParticleTypes.CLOUD,getX(),getY()-2,getZ(),14,2,.1,2,.03);}
  YagaMenu.open(p,this,0);gesture(1);playSound(org.slavicmyths.registry.ModSounds.YAGA_OPEN.get(),.45F,.55F);return ActionResultType.CONSUME;
 }
 @Override public boolean hurt(DamageSource source,float amount){if(!level.isClientSide&&source.getEntity() instanceof ServerPlayerEntity&&canonical()){ServerPlayerEntity p=(ServerPlayerEntity)source.getEntity();YagaData d=YagaData.get((ServerWorld)level);boolean expelled=d.progress(p.getUUID()).attack(level.getGameTime(),amount>=10);d.setDirty();gesture(3);p.displayClientMessage(new TranslationTextComponent(expelled?"yaga.warning.expelled":"yaga.warning.first"),false);playSound(expelled?org.slavicmyths.registry.ModSounds.YAGA_WARN.get():org.slavicmyths.registry.ModSounds.YAGA_HURT.get(),.8F,.55F);if(expelled){Vector3d away=p.position().subtract(position()).multiply(1,0,1).normalize();p.knockback(1.6F,-away.x,-away.z);p.addEffect(new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.MOVEMENT_SLOWDOWN,100));}if(p.containerMenu instanceof YagaMenu)p.closeContainer();}return false;}
 @Override public void die(DamageSource source){setHealth(40);}
 @Override protected SoundEvent getAmbientSound(){return org.slavicmyths.registry.ModSounds.YAGA_AMBIENT.get();}@Override protected float getVoicePitch(){return .6F;}
 @Override protected void playStepSound(BlockPos pos,net.minecraft.block.BlockState state){playSound(org.slavicmyths.registry.ModSounds.YAGA_STEP.get(),.15F,.65F);}
 @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);if(home!=null)n.putLong("YagaHome",home.asLong());}
 @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);home=n.contains("YagaHome")?BlockPos.of(n.getLong("YagaHome")):null;setPersistenceRequired();getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23);setHealth(40);}
}
