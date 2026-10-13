package org.slavicmyths.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Forest master: neutral watch, warned snare, local vanish and recoverable strike. */
public final class LeshyEntity extends LandSpiritEntity {
 public enum Behavior { WATCH, WARN, VANISH, REPOSITION, ROOT_SNARE, WOODLAND_STRIKE, FALSE_PRESENCE, CHASE, DISENGAGE }
 private final AttackTimeline attack=new AttackTimeline();
 private Behavior behavior=Behavior.WATCH;
 private int vanished;private long nextEscape;private boolean roots;
 private final java.util.Map<java.util.UUID,Integer> disturbances=new java.util.LinkedHashMap<>();
 public LeshyEntity(EntityType<? extends LeshyEntity> type,Level world){super(type,world);}
 public Behavior behavior(){return behavior;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.ARMOR,8).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.FOLLOW_RANGE,36).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.KNOCKBACK_RESISTANCE,.25);}
 @Override protected boolean meleeReady(){return false;}
 @Override protected AttackTimeline attackTimeline(){return attack;}
 @Override public boolean hurt(DamageSource source,float amount){
  if(vanished>0&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return false;
  boolean hit=super.hurt(source,amount);if(hit&&source.getEntity() instanceof Player player)org.slavicmyths.progression.Knowledge.award(player,"meet_leshy");return hit;
 }
 public void disturb(Player player){
  if(!disturbances.containsKey(player.getUUID())&&disturbances.size()>=16)disturbances.remove(disturbances.keySet().iterator().next());
  int count=disturbances.merge(player.getUUID(),1,Integer::sum);behavior=Behavior.WARN;voice("angry",.5F);if(count>=4)provoke(player);
 }
 @Override protected void think(){
  if(home==null)home=blockPosition();
  if(getTarget()!=null)return;
  behavior=Behavior.WATCH;var player=nearby(24);if(player!=null)getLookControl().setLookAt(player,30,30);else wander();
 }
 @Override protected void abilityTick(){
  if(vanished>0){getNavigation().stop();if(--vanished==0){setInvisible(false);behavior=Behavior.REPOSITION;}return;}
  if(getTarget()==null){attack.cancel();setInvisible(false);return;}
  if(attack.ready()){
   if(level().getGameTime()>=nextEscape&&attackable(getTarget(),20)){
    roots=true;attack.start(16,1,20,40);behavior=Behavior.ROOT_SNARE;voice("angry",.6F);particles(ParticleTypes.HAPPY_VILLAGER,10);nextEscape=level().getGameTime()+280;
   }else if(attackable(getTarget(),3)){roots=false;attack.start(18,1,20,35);behavior=Behavior.WOODLAND_STRIKE;voice("angry",.55F);}
   else{behavior=Behavior.CHASE;chase(1.05);return;}
  }
  if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();
   if(roots&&level() instanceof net.minecraft.server.level.ServerLevel server&&tickCount%4==0)server.sendParticles(ParticleTypes.COMPOSTER,getTarget().getX(),getTarget().getY()+.1,getTarget().getZ(),4,.6,.05,.6,0);
  }
  if(attack.tick()){
   if(roots&&attackable(getTarget(),20)){getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40));behavior=Behavior.FALSE_PRESENCE;
    // Two brief positional sound/particle decoys, no helper entities to leak.
    for(int i=0;i<2;i++){var p=getTarget().blockPosition().offset(i==0?-4:4,0,i==0?3:-3);if(level().hasChunkAt(p)){level().playSound(null,p,org.slavicmyths.registry.ModSounds.LESHY_AMBIENT.get(),net.minecraft.sounds.SoundSource.HOSTILE,.45F,.8F);if(level() instanceof net.minecraft.server.level.ServerLevel server)server.sendParticles(ParticleTypes.POOF,p.getX()+.5,p.getY()+1,p.getZ()+.5,8,.3,.6,.3,0);}}
   }else if(!roots)strikeTarget(8,3,.5F);
  }
  if(roots&&attack.phase()==AttackTimeline.Phase.RECOVERY&&attack.elapsed()==0){
   behavior=Behavior.VANISH;particles(ParticleTypes.POOF,12);setInvisible(true);vanished=16;
   for(int i=0;i<8;i++){double angle=random.nextDouble()*Math.PI*2;int radius=8+random.nextInt(9);var p=blockPosition().offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius));
    if(!level().hasChunkAt(p)||!level().getFluidState(p).isEmpty()||!level().getBlockState(p.below()).isSolid()||home!=null&&home.distSqr(p)>24*24)continue;
    if(randomTeleport(p.getX()+.5,p.getY(),p.getZ()+.5,true))break;
   }
  }
  if(attack.phase()==AttackTimeline.Phase.RECOVERY)getNavigation().stop();
 }
 @Override protected InteractionResult mobInteract(Player player,InteractionHand hand){org.slavicmyths.progression.Knowledge.award(player,"meet_leshy");return player.getItemInHand(hand).isEmpty()?InteractionResult.sidedSuccess(level().isClientSide):super.mobInteract(player,hand);}
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putLong("NextEscape",nextEscape);}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);nextEscape=tag.getLong("NextEscape");attack.cancel();vanished=0;setInvisible(false);}
 @Override protected int getBaseExperienceReward(){return 20;}
 @Override public int getAmbientSoundInterval(){return 400;}
}
