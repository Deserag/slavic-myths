package org.slavicmyths.entity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
public final class PoludnitsaEntity extends LandSpiritEntity {
    private static final DataParameter<Integer> FORM=EntityDataManager.defineId(PoludnitsaEntity.class,DataSerializers.INT);
    private int watch;private long nextSweep;private long nextNoticeSound;
    public PoludnitsaEntity(EntityType<? extends PoludnitsaEntity> t,World w){super(t,w);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(FORM,0);}
    public int form(){return entityData.get(FORM);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,46).add(Attributes.MOVEMENT_SPEED,.31).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.ARMOR,2).add(Attributes.FOLLOW_RANGE,24);}
    @Override public EntitySize getDimensions(Pose pose){return EntitySize.scalable(.6F,1.92F+.035F*form());}
    @Override public void onSyncedDataUpdated(DataParameter<?> p){super.onSyncedDataUpdated(p);if(FORM.equals(p))refreshDimensions();}
    @Override public void provoke(PlayerEntity p){super.provoke(p);if(form()==0){entityData.set(FORM,1);windup=20;getNavigation().stop();voice("transform",.65F);}}
    @Override protected void abilityTick(){
        if(form()>0 && form()<20){windup=1;entityData.set(FORM,form()+1);getNavigation().stop();if(form()%4==0)particles(ParticleTypes.CLOUD,5);if(form()==20)windup=0;}
    }
    @Override protected void think(){
        if(form()>0 && form()<20)return;
        if(getTarget()!=null){
            if(level.getGameTime()>nextPower && distanceToSqr(getTarget())<64){for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(7),p->!p.isCreative() && !p.isSpectator() && canSee(p)))p.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,70));nextPower=level.getGameTime()+360;voice("power",.6F);particles(ParticleTypes.CLOUD,15);}
            if(level.getGameTime()>nextSweep && distanceToSqr(getTarget())<9){swing(net.minecraft.util.Hand.MAIN_HAND);voice("attack",.45F);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(2.7),p->!p.isCreative() && !p.isSpectator() && canSee(p))){p.hurt(DamageSource.mobAttack(this),5);p.knockback(.3F,getX()-p.getX(),getZ()-p.getZ());}nextSweep=level.getGameTime()+160;}
            return;
        }
        PlayerEntity p=nearby(20);if(p==null){watch=0;wander();return;}
        getLookControl().setLookAt(p,30,30);double d=distanceToSqr(p);
        if(d<36 && canSee(p)){if(watch==0 && level.getGameTime()>nextNoticeSound){voice("notice",.3F);nextNoticeSound=level.getGameTime()+400;}getNavigation().stop();watch+=20;if(watch>=40)provoke(p);}
        else{watch=0;if(d<100)getNavigation().moveTo(p,.35);else getNavigation().stop();}
    }
    @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);n.putInt("NoonForm",form());n.putLong("NextSweep",nextSweep);}
    @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);entityData.set(FORM,n.getInt("NoonForm")>0?20:0);nextSweep=n.getLong("NextSweep");}
    @Override public int getAmbientSoundInterval(){return 600;}
    @Override public boolean doHurtTarget(net.minecraft.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level.isClientSide)voice("attack",.4F);return hit;}
}
