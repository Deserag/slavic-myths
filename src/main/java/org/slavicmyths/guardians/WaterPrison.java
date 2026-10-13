package org.slavicmyths.guardians;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
/** Attackable temporary shell, no water blocks, collision or suffocation. */
public final class WaterPrison extends PathfinderMob implements net.minecraft.world.entity.monster.Enemy {
 private static final EntityDataAccessor<Integer> HITS=SynchedEntityData.defineId(WaterPrison.class,EntityDataSerializers.INT);
 public UUID keeper,target;private long nextHit;private int life;
 public WaterPrison(EntityType<? extends WaterPrison> t,Level w){super(t,w);setNoGravity(true);setNoAi(true);noPhysics=true;xpReward=0;}
 public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return createMobAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,3).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,0);}
 @Override protected void registerGoals(){}
 public void configure(SpringKeeper b,ServerPlayer p){keeper=b.getUUID();target=p.getUUID();setPos(p.position());}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(HITS,0);}
 public int hits(){return entityData.get(HITS);}
 @Override public boolean isPickable(){return true;}
 @Override public boolean isPushable(){return false;}
 @Override public boolean canBeCollidedWith(){return false;}
 @Override public boolean hurt(DamageSource s,float amount){if(s.getEntity() instanceof LivingEntity e&&org.slavicmyths.karachun.NightOwners.owner(e)!=null)return action(e);return false;}
 public boolean action(LivingEntity e){if(!(level() instanceof ServerLevel w)||w.getGameTime()<nextHit||distanceToSqr(e)>25||!e.hasLineOfSight(this))return false;nextHit=w.getGameTime()+6;int hits=GuardianRules.prisonHits(hits());entityData.set(HITS,hits);if(hits>=3){GuardianContent.sound(w,blockPosition(),"spring_prison_break");w.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,getX(),getY()+1,getZ(),18,1,1,1,.03);discard();}return true;}
 @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(p instanceof ServerPlayer server)action(server);return InteractionResult.sidedSuccess(level().isClientSide);}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;var e=target==null?null:w.getEntity(target);var boss=keeper==null?null:w.getEntity(keeper);if(!(boss instanceof SpringKeeper b)||!b.fighting()||!(e instanceof ServerPlayer p)||!b.candidate(p)||++life>GuardianRules.PRISON_MAX||distanceToSqr(p)>4){discard();return;}p.setDeltaMovement(p.getDeltaMovement().multiply(GuardianRules.PRISON_DRAG,1,GuardianRules.PRISON_DRAG));p.hurtMarked=true;}
}
