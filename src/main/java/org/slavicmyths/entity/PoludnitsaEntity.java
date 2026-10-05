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
    public PoludnitsaEntity(EntityType<? extends PoludnitsaEntity> t,Level w){super(t,w);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(FORM,0);}
    public int form(){return entityData.get(FORM);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,46).add(Attributes.MOVEMENT_SPEED,.31).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.ARMOR,2).add(Attributes.FOLLOW_RANGE,24);}
    @Override protected EntityDimensions getDefaultDimensions(Pose pose){return EntityDimensions.scalable(.6F,1.92F+.035F*form());}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> p){super.onSyncedDataUpdated(p);if(FORM.equals(p))refreshDimensions();}
    @Override public void provoke(Player p){super.provoke(p);if(form()==0){entityData.set(FORM,1);windup=20;getNavigation().stop();voice("transform",.65F);}}
    @Override protected void abilityTick(){
        if(form()>0 && form()<20){windup=1;entityData.set(FORM,form()+1);getNavigation().stop();if(form()%4==0)particles(ParticleTypes.CLOUD,5);if(form()==20)windup=0;}
    }
    @Override protected void think(){
        if(form()>0 && form()<20)return;
        if(getTarget()!=null){
            if(level().getGameTime()>nextPower && distanceToSqr(getTarget())<64){for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(7),p->!p.isCreative() && !p.isSpectator() && hasLineOfSight(p)))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,70));nextPower=level().getGameTime()+360;voice("power",.6F);particles(ParticleTypes.CLOUD,15);}
            if(level().getGameTime()>nextSweep && distanceToSqr(getTarget())<9){swing(net.minecraft.world.InteractionHand.MAIN_HAND);voice("attack",.45F);for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(2.7),p->!p.isCreative() && !p.isSpectator() && hasLineOfSight(p))){p.hurt(damageSources().mobAttack(this),5);p.knockback(.3F,getX()-p.getX(),getZ()-p.getZ());}nextSweep=level().getGameTime()+160;}
            return;
        }
        Player p=nearby(20);if(p==null){watch=0;wander();return;}
        getLookControl().setLookAt(p,30,30);double d=distanceToSqr(p);
        if(d<36 && hasLineOfSight(p)){if(watch==0 && level().getGameTime()>nextNoticeSound){voice("notice",.3F);nextNoticeSound=level().getGameTime()+400;}getNavigation().stop();watch+=20;if(watch>=40)provoke(p);}
        else{watch=0;if(d<100)getNavigation().moveTo(p,.35);else getNavigation().stop();}
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("NoonForm",form());n.putLong("NextSweep",nextSweep);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);entityData.set(FORM,n.getInt("NoonForm")>0?20:0);nextSweep=n.getLong("NextSweep");}
    @Override public int getAmbientSoundInterval(){return 600;}
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level().isClientSide)voice("attack",.4F);return hit;}
}
