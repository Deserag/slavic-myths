package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
/** Bounded entity-local scheduler; no world polling and no client damage. */
public abstract class WitchActor extends PathfinderMob {
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(WitchActor.class,EntityDataSerializers.INT),LENGTH=SynchedEntityData.defineId(WitchActor.class,EntityDataSerializers.INT),SEQUENCE=SynchedEntityData.defineId(WitchActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(WitchActor.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> FLINCH=SynchedEntityData.defineId(WitchActor.class,EntityDataSerializers.FLOAT);
    protected final Map<String,Long> cooldowns=new HashMap<>();
    protected WitchActor(EntityType<? extends PathfinderMob> t,Level w){super(t,w);setPersistenceRequired();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(LENGTH,0);b.define(SEQUENCE,0);b.define(START,0L);b.define(FLINCH,1F);}
    public float flinch(){return entityData.get(FLINCH);}
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide)entityData.set(FLINCH,amount>=12?2F:1F);return hit;}
    public WitchAction action(){return WitchAction.values()[Math.clamp(entityData.get(ACTION),0,WitchAction.values().length-1)];}
    public float age(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
    public int age(){return (int)age(0);}public int length(){return entityData.get(LENGTH);}public int sequence(){return entityData.get(SEQUENCE);}
    public void begin(WitchAction a){begin(a,a.ticks);}public void begin(WitchAction a,int ticks){entityData.set(ACTION,a.ordinal());entityData.set(START,level().getGameTime());entityData.set(LENGTH,ticks);entityData.set(SEQUENCE,sequence()+1);getNavigation().stop();}
    public boolean ready(String key){return level().getGameTime()>=cooldowns.getOrDefault(key,0L);}public void cooldown(String key,int ticks){cooldowns.put(key,level().getGameTime()+ticks);}
    @Override public boolean hasLineOfSight(Entity target){return target instanceof org.slavicmyths.poloz.GreatPoloz p?org.slavicmyths.poloz.PolozAim.sight(this,p):super.hasLineOfSight(target);}
    public Vec3 heading(){double a=Math.toRadians(getYRot());return new Vec3(-Math.sin(a),0,Math.cos(a));}
    public void face(LivingEntity e){double a=Math.atan2(org.slavicmyths.poloz.PolozAim.point(this,e).z-getZ(),org.slavicmyths.poloz.PolozAim.point(this,e).x-getX());setYRot((float)Math.toDegrees(a)-90);yBodyRot=getYRot();getLookControl().setLookAt(e,25,20);}
    public double edge(LivingEntity e){if(e instanceof org.slavicmyths.poloz.GreatPoloz)return org.slavicmyths.poloz.PolozAim.edge(this,e);return Math.max(0,Math.sqrt(e.getBoundingBox().distanceToSqr(position()))-getBbWidth()/2);}
    public boolean cone(LivingEntity e,double r,double angle){var d=org.slavicmyths.poloz.PolozAim.point(this,e).subtract(position()).multiply(1,0,1);return edge(e)<=r&&(d.lengthSqr()<.01||d.normalize().dot(heading())>=Math.cos(Math.toRadians(angle/2)));}
    public boolean line(Vec3 from,Vec3 to){for(int i=0;i<=16;i++)if(!level().hasChunkAt(net.minecraft.core.BlockPos.containing(from.lerp(to,i/16.0))))return false;return level().clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    public void cue(String name){if(!level().isClientSide)playSound(MarinkaContent.sound((this instanceof DobrynyaEntity?"dobrynya":"marinka")+"."+name),name.contains("step")?.5F:.9F,.97F+random.nextFloat()*.06F);}
    public void bark(int line){if(!(level() instanceof ServerLevel w))return;WitchActor speaker=this;UUID owner=null;String who="dobrynya";if(this instanceof DobrynyaEntity h)owner=h.owner;else if(this instanceof MarinkaEntity m){var root=m.root();if(root==null)return;speaker=root;owner=m.owner;who="marinka";}if(owner==null||!speaker.ready("voice_line"))return;var p=w.getServer().getPlayerList().getPlayer(owner);if(p==null||p.level()!=w||distanceToSqr(p)>1024)return;speaker.cooldown("voice_line",140);p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("dialogue.slavicmyths.172."+who+"_"+line));}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putString("WitchAction",action().name());n.putInt("WitchElapsed",age());n.putInt("WitchLength",length());var c=new CompoundTag();cooldowns.forEach(c::putLong);n.put("WitchCooldowns",c);n.putInt("WitchSequence",sequence());}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);var c=n.getCompound("WitchCooldowns");for(String k:c.getAllKeys())cooldowns.put(k,c.getLong(k));entityData.set(SEQUENCE,n.getInt("WitchSequence"));begin(WitchAction.IDLE);}
    protected void resume(CompoundTag n,WitchAction a){begin(a,n.getInt("WitchLength"));entityData.set(START,level().getGameTime()-Math.clamp(n.getInt("WitchElapsed"),0,length()));}
}
