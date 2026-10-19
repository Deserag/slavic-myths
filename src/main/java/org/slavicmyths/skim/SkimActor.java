package org.slavicmyths.skim;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
/** Shared native animation clock. Callbacks are exclusively server-side and cancel on reload. */
public abstract class SkimActor extends PathfinderMob {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(SkimActor.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(SkimActor.class,EntityDataSerializers.LONG);
 private static final EntityDataAccessor<Integer> LENGTH=SynchedEntityData.defineId(SkimActor.class,EntityDataSerializers.INT);
 protected final Map<String,Long> cooldowns=new HashMap<>();
 protected SkimActor(EntityType<? extends PathfinderMob> t,Level w){super(t,w);setPersistenceRequired();}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(START,0L);b.define(LENGTH,0);}
 public SkimAction action(){return SkimAction.values()[Math.clamp(entityData.get(ACTION),0,SkimAction.values().length-1)];}
 public float age(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}public int age(){return (int)age(0);}
 public int duration(){return entityData.get(LENGTH);}public float clipAge(float partial){return age(partial)*(duration()>0?action().ticks/(float)duration():1);}
 public boolean frame(int f){double scale=duration()>0?action().ticks/(double)duration():1;return age()*scale>=f&&(age()-1)*scale<f;}
 public void begin(SkimAction a){begin(a,a.ticks);}public void begin(SkimAction a,int ticks){entityData.set(ACTION,a.ordinal());entityData.set(START,level().getGameTime());entityData.set(LENGTH,ticks);getNavigation().stop();}
 public boolean ready(String k){return level().getGameTime()>=cooldowns.getOrDefault(k,0L);}public void cooldown(String k,int t){cooldowns.put(k,level().getGameTime()+t);}
 @Override public boolean hasLineOfSight(Entity target){return target instanceof org.slavicmyths.poloz.GreatPoloz p?org.slavicmyths.poloz.PolozAim.sight(this,p):super.hasLineOfSight(target);}
    public Vec3 heading(){double a=Math.toRadians(getYRot());return new Vec3(-Math.sin(a),0,Math.cos(a));}
 public void face(LivingEntity e){setYRot((float)Math.toDegrees(Math.atan2(org.slavicmyths.poloz.PolozAim.point(this,e).z-getZ(),org.slavicmyths.poloz.PolozAim.point(this,e).x-getX()))-90);yBodyRot=getYRot();getLookControl().setLookAt(e,25,20);}
 public double edge(LivingEntity e){if(e instanceof org.slavicmyths.poloz.GreatPoloz)return org.slavicmyths.poloz.PolozAim.edge(this,e);return Math.max(0,Math.sqrt(e.getBoundingBox().distanceToSqr(position()))-getBbWidth()/2);}
 public boolean cone(LivingEntity e,double r,double angle){var d=org.slavicmyths.poloz.PolozAim.point(this,e).subtract(position()).multiply(1,0,1);return edge(e)<=r&&(d.lengthSqr()<.01||d.normalize().dot(heading())>=Math.cos(Math.toRadians(angle/2)));}
 public void cue(String k){if(!level().isClientSide){float pitch=.96F+random.nextFloat()*.08F;playSound(SkimContent.sound(k),.8F,k.equals("skim.roar")?1:pitch);}}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);var c=new CompoundTag();cooldowns.forEach(c::putLong);n.put("SkimCooldowns",c);n.putString("SkimAction",action().name());}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);cooldowns.clear();var c=n.getCompound("SkimCooldowns");for(var k:c.getAllKeys())cooldowns.put(k,c.getLong(k));begin(SkimAction.IDLE);}
}
