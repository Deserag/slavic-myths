package org.slavicmyths.nochnitsa;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import org.slavicmyths.underground.OminousAura;
import org.slavicmyths.nochnitsa.NochnitsaRules.State;
import org.slavicmyths.nochnitsa.NochnitsaRules.Event;

public final class NochnitsaEntity extends Monster {
    private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(NochnitsaEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(NochnitsaEntity.class,EntityDataSerializers.LONG);
    public long screamVisualStart=Long.MIN_VALUE/2;
    @Override public void handleEntityEvent(byte event){if(event==61)screamVisualStart=level().getGameTime();else super.handleEntityEvent(event);}
    private final NochnitsaRules.Clock clock=new NochnitsaRules.Clock();private UUID tracked,charging;private int seenTicks,lostSight,idleIn=140,ambientIn=320,breathIn=100,clothIn=80;private boolean nightOverride;
    private final org.slf4j.Logger logger=com.mojang.logging.LogUtils.getLogger();
    public NochnitsaEntity(EntityType<? extends Monster> type,Level level){super(type,level);xpReward=8;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,State.WANDER.ordinal());b.define(START,0L);}
    public State state(){return State.values()[entityData.get(STATE)];}public float age(float partial){return (float)(level().getGameTime()-entityData.get(START))+partial;}public boolean debugNight(){return nightOverride&&NochnitsaConfig.debug;}
    public void debug(String event){if(NochnitsaConfig.debug)logger.info("Nochnitsa {} t={} {}",getUUID(),level().getGameTime(),event);}
    private void sync(){if(entityData.get(STATE)!=clock.state.ordinal()||clock.age==0){entityData.set(STATE,clock.state.ordinal());entityData.set(START,level().getGameTime()-clock.age);}}
    private void begin(State s){clock.begin(s);sync();if(clock.locked())getNavigation().stop();}
    private long secondaryVoiceAfter,hurtVoiceAfter;
    private void cue(String name,float volume){
        float pitch=1+(random.nextFloat()-.5F)*.10F; // Preserve the old draw even when an audio cue is suppressed.
        long now=level().getGameTime();
        if((name.equals("ambient")||name.equals("breath"))&&now<secondaryVoiceAfter)return;
        if(name.equals("hurt")&&now<hurtVoiceAfter)return;
        int ticks=switch(name){case "ambient"->20;case "breath"->21;case "target_spotted","scream_release"->22;case "scream_inhale"->14;case "scream_interrupt"->9;case "hurt"->18;case "death"->30;default->0;};
        if(ticks>0)secondaryVoiceAfter=now+ticks;
        if(name.equals("hurt"))hurtVoiceAfter=now+18;
        playSound(NochnitsaContent.sound(name),volume,pitch);
    }
    public boolean valid(LivingEntity target){if(clock.state==State.DAWN||clock.state==State.DEATH||target==null||!target.isAlive()||target.level()!=level()||isAlliedTo(target))return false;
        if(target instanceof Player p)return !p.isCreative()&&!p.isSpectator()&&!OminousAura.ignores(this,p);
        return target instanceof Villager||target==getLastHurtByMob()&&target instanceof net.minecraft.world.entity.animal.IronGolem;
    }
    public boolean mayScreamAt(LivingEntity target){return valid(target)&&(target instanceof Player||target instanceof Villager)&&!(target instanceof Player p&&OminousAura.active(p)>=2)&&hasLineOfSight(target)&&distanceToSqr(target)<=24*24;}
    @Override public boolean canAttack(LivingEntity target){return valid(target)&&super.canAttack(target);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}public boolean canUse(){return isAlive()&&(valid(getTarget())||state()!=State.WANDER);}public boolean requiresUpdateEveryTick(){return true;}public void tick(){var t=getTarget();if(valid(t))getLookControl().setLookAt(t,25,15);if(state()==State.PURSUE&&valid(t)){if(tickCount%12==0)getNavigation().moveTo(t,1.2);}else getNavigation().stop();}});
        goalSelector.addGoal(1,new FloatGoal(this));goalSelector.addGoal(4,new RandomStrollGoal(this,.55));goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(0,new HurtByTargetGoal(this));targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,10,true,false,this::valid));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Villager.class,10,true,false,t->valid(t)&&distanceToSqr(t)<=400));
    }
    @Override public void aiStep(){if(!level().isClientSide&&isAlive()&&!debugNight()&&!NochnitsaRules.night(level().getDayTime())&&clock.state!=State.DAWN){setTarget(null);charging=null;begin(State.DAWN);cue("sunrise_dissolve",.75F);debug("dawn_dissolve");}super.aiStep();if(level().isClientSide||!isAlive())return;
        var target=getTarget();if(target!=null&&(!valid(target)||distanceToSqr(target)>40*40)){setTarget(null);target=null;debug("target_clear_invalid_or_protected");}
        if(clock.state==State.DAWN){setTarget(null);getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);if(clock.age%3==0)((ServerLevel)level()).sendParticles(ParticleTypes.SMOKE,getX(),getY()+1.1,getZ(),3,.3,.8,.3,.01);if(clock.tick()==Event.DAWN_REMOVE){discard();return;}sync();return;}
        if(clock.state==State.WINDUP&&(target==null||charging==null||!charging.equals(target.getUUID())||!mayScreamAt(target))){clock.abort();charging=null;sync();debug("scream_abort target_invalid_or_protected");}
        if(target!=null){if(hasLineOfSight(target))lostSight=0;else if(++lostSight>120){setTarget(null);target=null;} }
        if(target==null){tracked=null;seenTicks=0;if(!clock.locked()&&clock.state!=State.WANDER)begin(State.WANDER);}else{if(!target.getUUID().equals(tracked)){tracked=target.getUUID();seenTicks=0;if(!clock.locked()){begin(distanceToSqr(target)<=2.15*2.15?State.PURSUE:State.SPOT);if(random.nextFloat()<.35)cue("target_spotted",.65F);cue("breath",.45F);debug("target_acquired");}}seenTicks++;if(!clock.locked()&&clock.state==State.WANDER)begin(State.PURSUE);}
        if(clock.state==State.PURSUE&&target!=null){if(distanceToSqr(target)<=2.15*2.15&&hasLineOfSight(target)&&clock.attackCooldown==0){clock.claw();sync();getNavigation().stop();}else if(clock.mayScream()&&seenTicks>=20&&distanceToSqr(target)>2.5*2.5&&mayScreamAt(target)){charging=target.getUUID();begin(State.WINDUP);debug("scream_start");}}
        if(clock.state==State.WANDER&&--idleIn<=0){idleIn=140+random.nextInt(80);begin(State.HEAD_SNAP);}
        if(--ambientIn<=0){ambientIn=260+random.nextInt(120);if(clock.state==State.WANDER)cue("ambient",.5F);}if(--breathIn<=0){breathIn=100+random.nextInt(100);if(clock.state==State.PURSUE)cue("breath",.42F);}if(--clothIn<=0){clothIn=80+random.nextInt(80);if(clock.state==State.PURSUE)cue("robes_rustle",.3F);}
        if((clock.state==State.CLAW_LEFT||clock.state==State.CLAW_RIGHT)&&clock.age==5)cue("claw_swing",.65F);
        if(clock.state==State.WINDUP&&clock.age>=12&&clock.age%5==0)((ServerLevel)level()).sendParticles(ParticleTypes.SMOKE,getX(),getY()+1.7,getZ(),1,.1,.1,.1,.005);
        State before=clock.state;Event event=clock.tick();if(event==Event.INHALE)cue("scream_inhale",.9F);if(event==Event.HIT&&valid(target)&&distanceToSqr(target)<=2.15*2.15&&hasLineOfSight(target))target.hurt(damageSources().mobAttack(this),6);
        if(event==Event.SCREAM){if(charging!=null&&target!=null&&charging.equals(target.getUUID())&&mayScreamAt(target)){NochnitsaHorde.release(this,target);cue("scream_release",2);level().broadcastEntityEvent(this,(byte)61);}else{clock.cooldown=NochnitsaRules.RETRY;clock.begin(State.PURSUE);}charging=null;}
        if(before!=clock.state||clock.age==0)sync();
    }
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!level().isClientSide&&onGround()&&!clock.locked())cue("step",.32F);}
    @Override public boolean hurt(DamageSource source,float amount){boolean accepted=super.hurt(source,amount);if(accepted&&isAlive()&&!level().isClientSide&&clock.state!=State.DAWN){boolean interrupted=clock.hurt();charging=null;sync();getNavigation().stop();cue(interrupted?"scream_interrupt":"hurt",.7F);if(interrupted)debug("scream_abort damage");}return accepted;}
    @Override public void die(DamageSource source){begin(State.DEATH);cue("death",.8F);super.die(source);}
    @Override protected void tickDeath(){deathTime++;if(deathTime>=24&&!level().isClientSide)remove(RemovalReason.KILLED);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("NochnitsaScreamCooldown",clock.cooldown);n.putInt("NochnitsaAttackCooldown",clock.attackCooldown);n.putBoolean("NochnitsaLeft",clock.left);if(debugNight())n.putBoolean("NochnitsaDebugNightOverride",true);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);clock.cooldown=Math.clamp(n.getInt("NochnitsaScreamCooldown"),0,600);clock.attackCooldown=Math.clamp(n.getInt("NochnitsaAttackCooldown"),0,24);clock.left=!n.contains("NochnitsaLeft")||n.getBoolean("NochnitsaLeft");nightOverride=NochnitsaConfig.debug&&n.getBoolean("NochnitsaDebugNightOverride");clock.cooldown=Math.max(clock.cooldown,NochnitsaRules.RETRY);clock.begin(State.WANDER);tracked=charging=null;sync();}
}
