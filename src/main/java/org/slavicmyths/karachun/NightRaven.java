package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.*;
import org.slavicmyths.morozko.FrostWorld;
/** Bounded circling, staggered warning/dives and navigation around loaded obstacles. */
public final class NightRaven extends PathfinderMob {
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(NightRaven.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> ALLY=SynchedEntityData.defineId(NightRaven.class,EntityDataSerializers.BOOLEAN);
 public UUID owner,session;private int slot,life,age;private long nextDive;private Vec3 divePoint;private boolean hit;
 public NightRaven(EntityType<? extends NightRaven> t,net.minecraft.world.level.Level w){super(t,w);setNoGravity(true);moveControl=new net.minecraft.world.entity.ai.control.FlyingMoveControl(this,20,true);xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FLYING_SPEED,.7).add(Attributes.FOLLOW_RANGE,32).add(Attributes.ATTACK_DAMAGE,3);}
 @Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(net.minecraft.world.level.Level w){var n=new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this,w);n.setCanOpenDoors(false);n.setCanFloat(true);return n;}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,0);b.define(ALLY,false);}
 @Override protected void registerGoals(){}
 public int state(){return entityData.get(STATE);}public boolean allied(){return entityData.get(ALLY);}
 public void configure(Karachun boss,int index){owner=boss.getUUID();session=boss.session;slot=index;getAttribute(Attributes.MAX_HEALTH).setBaseValue(NightConfig.value("ravenHealth"));setHealth((float)NightConfig.value("ravenHealth"));nextDive=level().getGameTime()+60+index*25;}
 public void configure(ServerPlayer p,int index){owner=p.getUUID();slot=index;life=NightConfig.ticks("wingDuration");entityData.set(ALLY,true);nextDive=level().getGameTime()+20+index*30;}
 public LivingEntity master(){if(!(level() instanceof ServerLevel w)||owner==null)return null;var e=w.getEntity(owner);return e instanceof LivingEntity l?l:null;}
 public boolean allowed(LivingEntity t){var master=master();if(master==null||t==null||!org.slavicmyths.combat.CombatTargeting.canAttack(master,t,org.slavicmyths.combat.CombatTargeting.Context.PROJECTILE)||t==this||t==master||t instanceof NightRaven||!t.isAlive())return false;if(master instanceof Karachun b)return b.candidate(t);if(t instanceof net.minecraft.world.entity.player.Player&&master.getLastHurtMob()!=t&&master.getLastHurtByMob()!=t)return false;return FrostWorld.hostile(t,master)&&!(t instanceof org.slavicmyths.morozko.PlayerDouble)&&!(t instanceof org.slavicmyths.yaga.BabaYaga)&&!(t instanceof org.slavicmyths.bayun.BayunEntity);}
 private void state(int state){entityData.set(STATE,state);age=0;getNavigation().stop();}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();var master=master();if(master==null||!master.isAlive()||master instanceof ServerPlayer p&&(p.isSpectator()||p.hasDisconnected())||master instanceof Karachun b&&!b.fighting()&&b.phase()!=0||allied()&&--life<=0||distanceToSqr(master)>64*64){dispel();return;}age++;long now=w.getGameTime();if(tickCount%20==0){var t=getTarget();if(t==null||!allowed(t)||distanceToSqr(t)>1024){LivingEntity wanted=master.getLastHurtMob();if(!allowedNull(wanted))wanted=master.getLastHurtByMob();if(!allowedNull(wanted))wanted=w.getEntitiesOfClass(LivingEntity.class,master.getBoundingBox().inflate(28),this::allowed).stream().min(Comparator.comparingDouble(master::distanceToSqr)).orElse(null);setTarget(wanted);}}
  var target=getTarget();if(state()==0){double angle=now*.025+slot*Math.PI/2;Vec3 at=master.position().add(Math.cos(angle)*5,4+Math.sin(angle*.6),Math.sin(angle)*5);move(at,.8);if(target!=null&&allowed(target)&&now>=nextDive&&hasLineOfSight(target)){divePoint=target.getBoundingBox().getCenter();state(1);NightContent.sound(this,"raven_dive");}if(tickCount%70==slot*3)NightContent.sound(this,"raven_flap");if(tickCount%220==slot*7)NightContent.sound(this,"raven_call");}
  else if(state()==1){if(target==null||!allowed(target)){state(0);return;}getLookControl().setLookAt(target);if(age>=18){divePoint=target.getBoundingBox().getCenter();state(2);hit=false;}}
  else if(state()==2){if(target==null||!allowed(target)){state(3);return;}Vec3 delta=divePoint.subtract(position());Vec3 velocity=delta.normalize().scale(.8);var collision=w.clip(new net.minecraft.world.level.ClipContext(position(),position().add(velocity),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));if(collision.getType()!=HitResult.Type.MISS||horizontalCollision||verticalCollision){state(3);return;}setDeltaMovement(velocity);getLookControl().setLookAt(target);if(!hit&&getBoundingBox().expandTowards(velocity).inflate(.25).intersects(target.getBoundingBox())&&hasLineOfSight(target)){hit=true;boolean damaged=target.hurt(damageSources().mobAttack(this),(float)NightConfig.value("ravenDamage"));if(damaged){NightContent.sound(this,"raven_hit");if(master instanceof Karachun b){b.credit(FrostWorld.owner(target),0,true);NightStatus.add(target,b.session,0,8,false);}}state(3);}else if(age>=25||delta.length()<.6)state(3);}
  else {move(master.position().add(Math.cos(slot*2)*4,5,Math.sin(slot*2)*4),1);if(age>=30){nextDive=now+NightConfig.ticks("ravenCooldown");state(0);}}
 }
 private boolean allowedNull(LivingEntity t){return t!=null&&allowed(t);}
 private void move(Vec3 at,double speed){var w=(ServerLevel)level();var pos=net.minecraft.core.BlockPos.containing(at);if(!w.hasChunkAt(pos)||!w.getWorldBorder().isWithinBounds(pos))return;var hit=w.clip(new net.minecraft.world.level.ClipContext(position(),at,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));if(hit.getType()==HitResult.Type.MISS&&w.noCollision(getBoundingBox().move(at.subtract(position())))){getNavigation().stop();moveControl.setWantedPosition(at.x,at.y,at.z,speed);}else if(tickCount%10==0){Vec3 lower=hit.getLocation().subtract(at.subtract(position()).normalize().scale(.8));if(w.noCollision(getBoundingBox().move(lower.subtract(position()))))getNavigation().moveTo(lower.x,lower.y,lower.z,speed);else getNavigation().moveTo(master(),speed);}}
 private void dispel(){if(level() instanceof ServerLevel w)w.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,getX(),getY(),getZ(),6,.2,.2,.2,.02);discard();}
 @Override protected void dropAllDeathLoot(ServerLevel w,net.minecraft.world.damagesource.DamageSource s){}
 @Override public boolean causeFallDamage(float d,float m,net.minecraft.world.damagesource.DamageSource s){return false;}
 @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float damage){var master=master();if(master==null)return false;if(s.getEntity() instanceof LivingEntity l&&(l==master||l instanceof NightRaven||allied()&&!FrostWorld.hostile(l,master)))return false;return super.hurt(s,damage);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);discard();}
}
