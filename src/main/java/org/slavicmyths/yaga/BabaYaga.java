package org.slavicmyths.yaga;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
/** Persistent home NPC. Player harm triggers refusal, never a hostile-boss lifecycle. */
public final class BabaYaga extends PathfinderMob {
 private static final EntityDataAccessor<Integer> GESTURE=SynchedEntityData.defineId(BabaYaga.class,EntityDataSerializers.INT),SINCE=SynchedEntityData.defineId(BabaYaga.class,EntityDataSerializers.INT);
 public BlockPos home;private boolean eggSummoned;
 public BabaYaga(EntityType<? extends BabaYaga> t,Level w){super(t,w);setPersistenceRequired();xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,12);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(GESTURE,0);builder.define(SINCE,0);}
 public int gesture(){return entityData.get(GESTURE);}public float gestureTime(float partial){return Math.max(0,(int)level().getGameTime()-entityData.get(SINCE)+partial);}
 public void gesture(int id){if(!level().isClientSide){entityData.set(GESTURE,id);entityData.set(SINCE,(int)level().getGameTime());}}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE));}public boolean canUse(){return home!=null&&distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>9;}public void tick(){if(tickCount%20==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.65);}});goalSelector.addGoal(2,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(3,new RandomLookAroundGoal(this));}
 @Override public boolean removeWhenFarAway(double distance){return false;}@Override public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to){return false;}@Override public boolean causeFallDamage(float d,float mult,net.minecraft.world.damagesource.DamageSource source){return false;}@Override public void checkDespawn(){}
 @Override public void tick(){super.tick();if(!level().isClientSide){if(home==null)home=blockPosition();if(gesture()!=0&&gestureTime(0)>36)gesture(0);if(getHealth()<40)setHealth(40);}}
 public boolean isEggSummoned(){return eggSummoned;}
 public boolean canonical(){if(level().isClientSide)return true;YagaData d=YagaData.get((ServerLevel)level());return level().dimension()==Level.OVERWORLD&&(eggSummoned||d.placed&&getUUID().equals(d.npc));}
 @Override protected InteractionResult mobInteract(Player player,InteractionHand hand){if(level().isClientSide)return InteractionResult.SUCCESS;if(!(player instanceof ServerPlayer)||player.isSpectator()||!canonical())return InteractionResult.PASS;ServerPlayer p=(ServerPlayer)player;YagaData d=YagaData.get(p.serverLevel());YagaData.Progress progress=d.progress(p.getUUID());long now=level().getGameTime();if(progress.blocked(now)){YagaServices.fail(p,"uninvited");gesture(3);return InteractionResult.FAIL;}
  if(!eggSummoned&&progress.intro==0){progress.intro=1;d.setDirty();YagaServices.award(p,"find_the_hut");p.displayClientMessage(Component.translatable("yaga.intro.wait"),false);playSound(org.slavicmyths.registry.ModSounds.YAGA_HUT_CREAK.get(),.7F,.5F);gesture(1);return InteractionResult.CONSUME;}
  if(!eggSummoned&&progress.intro==1){progress.intro=2;d.setDirty();p.displayClientMessage(Component.translatable("yaga.intro.welcome"),false);playSound(org.slavicmyths.registry.ModSounds.YAGA_HUT_STEP.get(),.8F,.55F);((ServerLevel)level()).sendParticles(ParticleTypes.CLOUD,getX(),getY()-2,getZ(),14,2,.1,2,.03);}
  YagaMenu.open(p,this,0);gesture(1);playSound(org.slavicmyths.registry.ModSounds.YAGA_OPEN.get(),.45F,.55F);return InteractionResult.CONSUME;
 }
 @Override public boolean hurt(DamageSource source,float amount){if(!level().isClientSide&&source.getEntity() instanceof ServerPlayer&&canonical()){ServerPlayer p=(ServerPlayer)source.getEntity();YagaData d=YagaData.get((ServerLevel)level());boolean expelled=d.progress(p.getUUID()).attack(level().getGameTime(),amount>=10);d.setDirty();gesture(3);p.displayClientMessage(Component.translatable(expelled?"yaga.warning.expelled":"yaga.warning.first"),false);playSound(expelled?org.slavicmyths.registry.ModSounds.YAGA_WARN.get():org.slavicmyths.registry.ModSounds.YAGA_HURT.get(),.8F,.55F);if(expelled){Vec3 away=p.position().subtract(position()).multiply(1,0,1).normalize();p.knockback(1.6F,-away.x,-away.z);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,100));}if(p.containerMenu instanceof YagaMenu)p.closeContainer();}return false;}
 @Override public void die(DamageSource source){setHealth(40);}
 @Override protected SoundEvent getAmbientSound(){return org.slavicmyths.registry.ModSounds.YAGA_AMBIENT.get();}@Override public float getVoicePitch(){return .6F;}
 @Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(org.slavicmyths.registry.ModSounds.YAGA_STEP.get(),.15F,.65F);}
 @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor w,DifficultyInstance diff,MobSpawnType reason,SpawnGroupData data){SpawnGroupData result=super.finalizeSpawn(w,diff,reason,data);if(reason==MobSpawnType.SPAWN_EGG){eggSummoned=true;home=blockPosition();}return result;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putBoolean("YagaEggSummoned",eggSummoned);if(home!=null)n.putLong("YagaHome",home.asLong());}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);eggSummoned=n.getBoolean("YagaEggSummoned");home=n.contains("YagaHome")?BlockPos.of(n.getLong("YagaHome")):null;setPersistenceRequired();getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23);setHealth(40);}
}
