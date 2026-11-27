package org.slavicmyths.savanna;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
public final class PolkanMissile extends Projectile {
    private static final EntityDataAccessor<Boolean> SPEAR=SynchedEntityData.defineId(PolkanMissile.class,EntityDataSerializers.BOOLEAN);
    private boolean returning;private float damage;private int age;
    public PolkanMissile(EntityType<? extends PolkanMissile> t,Level w){super(t,w);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(SPEAR,false);}
    public boolean spear(){return entityData.get(SPEAR);}
    void init(Polkan p,boolean spear,float damage,Vec3 at,Vec3 velocity){setOwner(p);entityData.set(SPEAR,spear);this.damage=damage;setPos(at.x,at.y,at.z);setDeltaMovement(velocity);rotate(velocity);}
    private void rotate(Vec3 v){setYRot((float)(Mth.atan2(v.x,v.z)*180/Math.PI));setXRot((float)(Mth.atan2(v.y,v.horizontalDistance())*180/Math.PI));}
    @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;if(!(getOwner() instanceof Polkan p)||!p.isAlive()||++age>100||spear()&&!p.ownsSpear(getUUID())||!spear()&&age>26){discard();return;}
        Vec3 from=position(),v=getDeltaMovement();
        if(returning){Vec3 d=p.handPosition().subtract(from);if(d.length()<2){p.spearReturned();discard();return;}v=d.normalize().scale(2.6);}
        else{Vec3 to=from.add(v);HitResult block=w.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));Vec3 limit=block.getType()==HitResult.Type.MISS?to:block.getLocation();
            LivingEntity target=null;double best=from.distanceToSqr(limit);
            for(LivingEntity x:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().expandTowards(v).inflate(.6),p::hostileTo)){var at=x.getBoundingBox().inflate(.25).clip(from,limit);if(at.isPresent()&&from.distanceToSqr(at.get())<best){best=from.distanceToSqr(at.get());target=x;}}
            if(target!=null){if(target.hurt(damageSources().mobProjectile(this,p),damage)&&spear())target.knockback(1.5,p.getX()-target.getX(),p.getZ()-target.getZ());if(spear())returning=true;else{discard();return;}}
            else if(block.getType()!=HitResult.Type.MISS||age>=24&&spear()){if(spear())returning=true;else{discard();return;}}
            if(!spear())v=v.add(0,-.035,0);if(returning)v=p.handPosition().subtract(from).normalize().scale(2.6);
        }
        setDeltaMovement(v);setPos(from.x+v.x,from.y+v.y,from.z+v.z);rotate(v);if(spear()||damage>=24)w.sendParticles(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),2,.06,.06,.06,.01);
    }
    @Override protected void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putBoolean("Spear",spear());n.putBoolean("Returning",returning);n.putFloat("Damage",damage);n.putInt("Age",age);}
    @Override protected void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);entityData.set(SPEAR,n.getBoolean("Spear"));returning=n.getBoolean("Returning");damage=n.getFloat("Damage");age=n.getInt("Age");}
}