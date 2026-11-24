package org.slavicmyths.winterboss;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;

/** Server segment collision; actual axes return and spectral volley copies expire. */
public final class ShipCaptainAxe extends Projectile {
    private static final EntityDataAccessor<Boolean> RETURNING=SynchedEntityData.defineId(ShipCaptainAxe.class,EntityDataSerializers.BOOLEAN);
    private UUID captain;
    private int slot=-1,age;
    private boolean volley;
    private final Set<UUID> struck=new HashSet<>();
    public ShipCaptainAxe(EntityType<? extends ShipCaptainAxe> t,Level level){super(t,level);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(RETURNING,false);}
    public boolean returning(){return entityData.get(RETURNING);}
    public void launch(ShipCaptain owner,int slot,Vec3 velocity,boolean volley){setOwner(owner);captain=owner.getUUID();this.slot=slot;this.volley=volley;setDeltaMovement(velocity);}
    @Override public void tick(){
        super.tick();
        if(!(level() instanceof ServerLevel w))return;
        if(!(w.getEntity(captain) instanceof ShipCaptain owner)||!owner.isAlive()||(!volley&&!owner.ownsAxe(slot,getUUID()))||++age>90){discard();return;}
        Vec3 from=position(),velocity=getDeltaMovement();
        if(!volley&&(age>=22||returning())){
            if(!returning()){entityData.set(RETURNING,true);struck.clear();}
            Vec3 delta=owner.hand(slot).subtract(from);
            if(delta.lengthSqr()<1){owner.returnAxe(slot,getUUID());discard();return;}
            velocity=delta.normalize().scale(1.6);
        }else if(volley&&age>20){discard();return;}
        Vec3 to=from.add(velocity);
        if(!w.hasChunkAt(BlockPos.containing(to))){discard();return;}
        var wall=w.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        double limit=wall.getType()==HitResult.Type.MISS?velocity.lengthSqr()+1:from.distanceToSqr(wall.getLocation());
        ServerPlayer closest=null;
        for(var p:w.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().expandTowards(velocity).inflate(.4),p->owner.eligible(p)&&!struck.contains(p.getUUID()))){
            var contact=p.getBoundingBox().inflate(.25).clip(from,to);
            if(contact.isPresent()){double d=from.distanceToSqr(contact.get());if(d<limit){limit=d;closest=p;}}
        }
        if(closest!=null){struck.add(closest.getUUID());owner.projectileHit(closest,volley);if(volley){discard();return;}if(!returning()){entityData.set(RETURNING,true);struck.clear();struck.add(closest.getUUID());return;}}
        if(wall.getType()!=HitResult.Type.MISS){if(volley||returning()){discard();return;}entityData.set(RETURNING,true);return;}
        setDeltaMovement(velocity);setPos(to);
        w.sendParticles(ParticleTypes.SNOWFLAKE,getX(),getY(),getZ(),2,.08,.08,.08,0);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);}
}
