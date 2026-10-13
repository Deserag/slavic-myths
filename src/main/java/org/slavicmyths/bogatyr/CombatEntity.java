package org.slavicmyths.bogatyr;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.bogatyr.BogatyrRules.State;

public abstract class CombatEntity extends net.minecraft.world.entity.PathfinderMob {
    private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(CombatEntity.class,EntityDataSerializers.INT),DURATION=SynchedEntityData.defineId(CombatEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(CombatEntity.class,EntityDataSerializers.LONG);
    public final Map<String,Long> cooldowns=new HashMap<>();
    protected CombatEntity(EntityType<? extends PathfinderMob> type,Level level){super(type,level);setPersistenceRequired();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,State.IDLE.ordinal());b.define(START,0L);b.define(DURATION,0);}
    public State state(){return State.values()[Math.clamp(entityData.get(STATE),0,State.values().length-1)];}
    public float age(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
    public int elapsed(){return (int)age(0);}
    public int duration(){return entityData.get(DURATION);}
    public void resume(State s,int total,int elapsed){begin(s,total);entityData.set(START,level().getGameTime()-Math.clamp(elapsed,0,total));}
    public void begin(State s,int duration){entityData.set(STATE,s.ordinal());entityData.set(START,level().getGameTime());entityData.set(DURATION,duration);getNavigation().stop();}
    public boolean ready(String key){return level().getGameTime()>=cooldowns.getOrDefault(key,0L);}
    public void cooldown(String key,int ticks){cooldowns.put(key,level().getGameTime()+ticks);}
    public double edge(LivingEntity target){if(target instanceof org.slavicmyths.poloz.GreatPoloz)return org.slavicmyths.poloz.PolozAim.edge(this,target);return Math.sqrt(target.getBoundingBox().distanceToSqr(position()))-getBbWidth()/2;}
    public boolean cone(LivingEntity t,double reach,double degrees,Vec3 heading){var d=org.slavicmyths.poloz.PolozAim.point(this,t).subtract(position()).multiply(1,0,1);return edge(t)<=reach&&(d.lengthSqr()<.001||d.normalize().dot(heading)>=Math.cos(Math.toRadians(degrees/2)));}
    @Override public boolean hasLineOfSight(Entity target){return target instanceof org.slavicmyths.poloz.GreatPoloz p?org.slavicmyths.poloz.PolozAim.sight(this,p):super.hasLineOfSight(target);}
    public Vec3 heading(){double a=Math.toRadians(getYRot());return new Vec3(-Math.sin(a),0,Math.cos(a));}
    public void face(LivingEntity t){getLookControl().setLookAt(t,30,25);double a=Math.atan2(org.slavicmyths.poloz.PolozAim.point(this,t).z-getZ(),org.slavicmyths.poloz.PolozAim.point(this,t).x-getX());setYRot((float)Math.toDegrees(a)-90);yBodyRot=getYRot();}
    public void cue(String cue){if(!level().isClientSide)playSound(BogatyrContent.sound(this instanceof IlyaEntity?"ilya_muromets":"idolishche_poganoe",cue),cue.contains("step")?.5F:1,1);}
    public void dust(int count){if(level() instanceof ServerLevel w)w.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,getX(),getY()+.2,getZ(),Math.min(count,BogatyrConfig.PARTICLES.get()),1,.15,1,.03);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putString("CombatState",state().name());n.putInt("CombatDuration",duration());n.putInt("CombatElapsed",elapsed());n.putInt("CombatRemaining",Math.max(0,duration()-elapsed()));var cds=new CompoundTag();cooldowns.forEach((k,v)->cds.putLong(k,v));n.put("CombatCooldowns",cds);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);var cds=n.getCompound("CombatCooldowns");for(String k:cds.getAllKeys())cooldowns.put(k,cds.getLong(k));begin(State.IDLE,0);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));}
}
