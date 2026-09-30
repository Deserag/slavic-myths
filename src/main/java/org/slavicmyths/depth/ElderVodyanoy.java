package org.slavicmyths.depth;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.datasync.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.BossInfo;
import org.slavicmyths.water.WaterSpirit;
import org.slavicmyths.registry.ModSounds;
public final class ElderVodyanoy extends WaterSpirit {
 private static final DataParameter<Integer> PHASE=EntityDataManager.defineId(ElderVodyanoy.class,DataSerializers.INT);
 private static final DataParameter<Integer> START=EntityDataManager.defineId(ElderVodyanoy.class,DataSerializers.INT);
 private final ServerBossInfo bar=new ServerBossInfo(new net.minecraft.util.text.TranslationTextComponent("entity.slavicmyths.elder_vodyanoy"),BossInfo.Color.BLUE,BossInfo.Overlay.PROGRESS);
 private int recovery,waveTicks,dashTicks;private final java.util.Set<java.util.UUID> waveHit=new java.util.HashSet<>();private Vector3d dash=Vector3d.ZERO;private boolean resolved;
 public ElderVodyanoy(EntityType<? extends ElderVodyanoy> t,World w){super(t,w);setPersistenceRequired();xpReward=35;}
 public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,150).add(Attributes.MOVEMENT_SPEED,.19).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.FOLLOW_RANGE,24);}
 protected void defineSynchedData(){super.defineSynchedData();entityData.define(PHASE,1);entityData.define(START,0);}
 protected void registerGoals(){} // Encounter owns attack timing; no instant vanilla melee goal.
 public int phase(){return entityData.get(PHASE);}public float preparation(float partial){return Math.min(1,(((int)level.getGameTime()-entityData.get(START))+partial)/(state()==6?40F:state()==1?26F:32F));}
 public void provoke(PlayerEntity p){if(!p.isCreative()&&!p.isSpectator()){setTarget(p);angryUntil=level.getGameTime()+1200;}}
 protected void think(){if(home==null)home=blockPosition();if(getTarget()==null){PlayerEntity p=nearby(18);if(p!=null)provoke(p);}LivingEntity target=getTarget();if(waveTicks>0){getNavigation().stop();return;}if(target==null||windup>0||dashTicks>0)return;if(recovery>0){getNavigation().moveTo(target,isInWater()?1.6:.8);return;}double distance=distanceToSqr(target);int action=distance<16?(random.nextBoolean()?1:2):isInWater()&&random.nextBoolean()?3:4;if(phase()>=2&&random.nextInt(3)==0)action=random.nextBoolean()?5:6;state(action);windup=action==6?40:action==1?26:32;entityData.set(START,(int)level.getGameTime());getNavigation().stop();getLookControl().setLookAt(target,40,40);Vector3d toward=target.position().subtract(position()).normalize();yRot=(float)(Math.atan2(toward.z,toward.x)*180/Math.PI)-90;dash=toward;voiceAction(action);}
 protected void abilityTick(){bar.setPercent(getHealth()/getMaxHealth());int phase=getHealth()/getMaxHealth()>.65F?1:getHealth()/getMaxHealth()>.3F?2:3;if(phase!=phase()){entityData.set(PHASE,phase);playSound(ModSounds.ELDER_PHASE.get(),.9F,.75F);recovery=30;if(phase==3&&home!=null&&level.getBlockEntity(home) instanceof PoolStoneTile)((PoolStoneTile)level.getBlockEntity(home)).flood();}if(recovery>0)recovery--;
  if(windup>0){getNavigation().stop();if(tickCount%8==0)particles(ParticleTypes.SPLASH,4);if(--windup==0){resolve(state());state(0);recovery=phase()==3?30:60;}}
  if(dashTicks>0){dashTicks--;setDeltaMovement(dash.scale(.65).add(0,isInWater()?0:-.03,0));if(dashTicks==5)hitArea(3.5F,6,.45F,false);if(horizontalCollision)dashTicks=0;}
  if(waveTicks>0){float radius=(30-waveTicks)*.35F;waveTicks--;if(tickCount%3==0){ServerWorld w=(ServerWorld)level;for(int i=0;i<12;i++){double a=i*Math.PI/6;w.sendParticles(ParticleTypes.SPLASH,getX()+Math.cos(a)*radius,getY()+.35,getZ()+Math.sin(a)*radius,1,0,.05,0,.02);}}for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(radius+1,2,radius+1),this::validPlayer)){double distance=Math.sqrt(distanceToSqr(p));if(Math.abs(distance-radius)<1.2&&canSee(p)&&waveHit.add(p.getUUID())){p.hurt(DamageSource.mobAttack(this),3);p.knockback(.9F,getX()-p.getX(),getZ()-p.getZ());p.hurtMarked=true;}}}
 }
 private boolean validPlayer(PlayerEntity p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator();}
 private void resolve(int action){if(action==3&&isInWater()){dashTicks=12;return;}if(action==6){waveTicks=30;waveHit.clear();return;}if(action==5){for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(10),this::validPlayer)){if(!canSee(p)||(!p.isInWater()&&!level.getFluidState(p.blockPosition().below()).is(net.minecraft.tags.FluidTags.WATER)))continue;double strength=org.slavicmyths.item.FolkEquipmentEffects.wears(p,org.slavicmyths.registry.ModItems.DEPTH_AMULET.get())?.25:.4;p.setDeltaMovement(p.getDeltaMovement().add(position().subtract(p.position()).normalize().scale(strength)));p.hurtMarked=true;}return;}hitArea(action==4?6:4,action==1?9:action==2?5:3,action==1?.3F:.75F,true);}
 private void hitArea(float radius,float damage,float knock,boolean front){Vector3d facing=new Vector3d(-Math.sin(Math.toRadians(yRot)),0,Math.cos(Math.toRadians(yRot)));for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(radius,2,radius),this::validPlayer)){if(distanceToSqr(p)>radius*radius||!canSee(p)||(front&&p.position().subtract(position()).normalize().dot(facing)<.1))continue;p.hurt(DamageSource.mobAttack(this),damage);p.knockback(knock,getX()-p.getX(),getZ()-p.getZ());p.hurtMarked=true;}}
 private void voiceAction(int a){playSound(a==1?ModSounds.ELDER_HEAVY.get():a==3?ModSounds.ELDER_DASH.get():a==5?ModSounds.ELDER_PULL.get():a==6?ModSounds.ELDER_WAVE.get():ModSounds.ELDER_ATTACK.get(),.9F,.8F);}
 public boolean hurt(DamageSource s,float amount){return super.hurt(s,s==DamageSource.OUT_OF_WORLD?amount:Math.min(20,amount));}
 protected SoundEvent getAmbientSound(){return ModSounds.ELDER_IDLE.get();}protected SoundEvent getHurtSound(DamageSource s){return ModSounds.ELDER_HURT.get();}protected SoundEvent getDeathSound(){return ModSounds.ELDER_DEATH.get();}
 public void startSeenByPlayer(ServerPlayerEntity p){super.startSeenByPlayer(p);bar.addPlayer(p);}public void stopSeenByPlayer(ServerPlayerEntity p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 public void die(DamageSource s){if(!level.isClientSide&&!resolved){resolved=true;bar.removeAllPlayers();waveTicks=dashTicks=windup=0;if(home!=null&&level.getBlockEntity(home) instanceof PoolStoneTile)((PoolStoneTile)level.getBlockEntity(home)).finish(getUUID());for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(24)))org.slavicmyths.progression.Knowledge.award(p,"defeat_depth_master");particles(ParticleTypes.SPLASH,12);}super.die(s);}
 public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);n.putInt("ElderPhase",phase());n.putBoolean("Resolved",resolved);}
 public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);entityData.set(PHASE,Math.max(1,n.getInt("ElderPhase")));resolved=n.getBoolean("Resolved");state(0);recovery=40;}
}
