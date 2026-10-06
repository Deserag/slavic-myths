package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
public final class PoludnitsaEntity extends LandSpiritEntity {
    private static final EntityDataAccessor<Integer> FORM=SynchedEntityData.defineId(PoludnitsaEntity.class,EntityDataSerializers.INT);
    private int watch;private long nextSweep;private long nextNoticeSound;
    public enum Behavior { DISTANT_WATCH, APPROACH_THRESHOLD, TRANSFORM, CHASE, SWEEP, HEAT_PRESSURE, RECOVERY, CALM }
    private final AttackTimeline attack=new AttackTimeline();private Behavior behavior=Behavior.DISTANT_WATCH;private boolean heat;
    public Behavior behavior(){return behavior;}
    public PoludnitsaEntity(EntityType<? extends PoludnitsaEntity> t,Level w){super(t,w);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(FORM,0);}
    public int form(){return entityData.get(FORM);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,60).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,4).add(Attributes.FOLLOW_RANGE,32);}
    @Override protected boolean meleeReady(){return false;}
    @Override protected AttackTimeline attackTimeline(){return attack;}
    @Override protected EntityDimensions getDefaultDimensions(Pose pose){return EntityDimensions.scalable(.6F,1.92F+.035F*form());}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> p){super.onSyncedDataUpdated(p);if(FORM.equals(p))refreshDimensions();}
    @Override public void provoke(Player p){super.provoke(p);if(form()==0){entityData.set(FORM,1);windup=20;getNavigation().stop();voice("transform",.65F);}}
    @Override protected void abilityTick(){
        if(form()>0 && form()<20){behavior=Behavior.TRANSFORM;windup=1;entityData.set(FORM,form()+1);getNavigation().stop();if(form()%4==0)particles(ParticleTypes.CLOUD,5);if(form()==20)windup=0;return;}
        if(getTarget()==null){attack.cancel();if(form()==20){entityData.set(FORM,0);behavior=Behavior.CALM;voice("transform",.4F);}return;}
        if(attack.ready()){
            if(level().getGameTime()>=nextPower&&attackable(getTarget(),7)){heat=true;attack.start(24,1,16,40);nextPower=level().getGameTime()+240;voice("power",.6F);particles(ParticleTypes.CLOUD,12);}
            else if(attackable(getTarget(),3)){heat=false;attack.start(20,1,16,30);voice("attack",.5F);}
            else{behavior=Behavior.CHASE;chase(1.2);return;}
        }
        if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();behavior=heat?Behavior.HEAT_PRESSURE:Behavior.SWEEP;}
        if(attack.tick()){
            if(heat){particles(ParticleTypes.CLOUD,20);for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(6),p->attackable(p,6)))player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60));}
            else{for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(3),p->attackable(p,3)&&p.position().subtract(position()).multiply(1,0,1).normalize().dot(getLookAngle().multiply(1,0,1).normalize())>.25))player.hurt(damageSources().mobAttack(this),7);}
        }
        if(attack.phase()==AttackTimeline.Phase.RECOVERY){behavior=Behavior.RECOVERY;getNavigation().stop();}
    }
    @Override protected void think(){
        if(form()>0 && form()<20)return;
        if(getTarget()!=null){
            return;
        }
        Player p=nearby(20);if(p==null){watch=0;wander();return;}
        getLookControl().setLookAt(p,30,30);double d=distanceToSqr(p);
        if(d<144 && hasLineOfSight(p)){if(watch==0 && level().getGameTime()>nextNoticeSound){voice("notice",.3F);nextNoticeSound=level().getGameTime()+400;}getNavigation().stop();watch+=20;if(watch>=20)provoke(p);}
        else{watch=0;behavior=d<256?Behavior.APPROACH_THRESHOLD:Behavior.DISTANT_WATCH;if(d<256)getNavigation().moveTo(p,.35);else getNavigation().stop();}
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("NoonForm",form());n.putLong("NextSweep",nextSweep);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);entityData.set(FORM,n.getInt("NoonForm")>0?20:0);nextSweep=n.getLong("NextSweep");}
    @Override public int getAmbientSoundInterval(){return 600;}
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level().isClientSide)voice("attack",.4F);return hit;}
}
