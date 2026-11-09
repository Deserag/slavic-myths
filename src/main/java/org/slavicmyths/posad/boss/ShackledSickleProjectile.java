package org.slavicmyths.posad.boss;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.registry.ModItems;

public final class ShackledSickleProjectile extends ThrowableItemProjectile {
    private UUID targetId;
    private boolean returning;
    private int pullTicks;
    public ShackledSickleProjectile(EntityType<? extends ShackledSickleProjectile> type,Level level){super(type,level);}
    @Override protected Item getDefaultItem(){return ModItems.MYTAR_SHACKLED_SICKLE.get();}
    @Override protected double getDefaultGravity(){return 0.012;}
    @Override protected boolean canHitEntity(Entity target){
        return targetId==null && !returning && target instanceof LivingEntity living && getOwner()!=target && (getOwner()==null||!getOwner().isAlliedTo(target)) && super.canHitEntity(target);
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        if(level().isClientSide||!(getOwner() instanceof ServerPlayer owner) || !(hit.getEntity() instanceof LivingEntity target)){returning=true;return;}
        target.hurt(level().damageSources().thrown(this,owner),4F);
        if(target.getBbHeight()<=4F && target.getBbWidth()<=2.5F && !target.isAlliedTo(owner)
            && !(target instanceof Player p&&(p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))) {
            targetId=target.getUUID();
            pullTicks=26;
            setDeltaMovement(Vec3.ZERO);
        } else returning=true;
    }
    @Override protected void onHitBlock(BlockHitResult hit){returning=true;}
    @Override public void tick(){
        if(!(level() instanceof ServerLevel world)){super.tick();return;}
        if(!(getOwner() instanceof ServerPlayer owner)||!owner.isAlive()||owner.level()!=level()){discard();return;}
        if(targetId!=null){
            Entity found=world.getEntity(targetId);
            if(!(found instanceof LivingEntity target)||!target.isAlive()||pullTicks--<=0){targetId=null;returning=true;}
            else {
                setPos(target.getX(),target.getY()+target.getBbHeight()*.58,target.getZ());
                Vec3 delta=owner.position().subtract(target.position()).multiply(1,0,1);
                if(delta.lengthSqr()>2.5 && target.getBbHeight()<=4F){
                    Vec3 step=delta.normalize().scale(.19);
                    if(world.noCollision(target,target.getBoundingBox().move(step)))target.setDeltaMovement(target.getDeltaMovement().add(step));
                }else {targetId=null;returning=true;}
            }
        }
        if(returning){
            Vec3 destination=owner.getEyePosition().add(owner.getLookAngle().scale(.4));
            Vec3 delta=destination.subtract(position());
            if(delta.lengthSqr()<1.5){discard();return;}
            setDeltaMovement(delta.normalize().scale(1.1));
        }
        super.tick();
        if(tickCount>70){discard();return;}
        Vec3 hand=owner.getEyePosition().subtract(0,.35,0),blade=position();
        double distance=blade.distanceTo(hand);
        for(int i=1;i<=Math.min(24,(int)(distance*3));i++){
            Vec3 spot=hand.lerp(blade,i/(Math.min(24,(int)(distance*3))+1.0));
            world.sendParticles(ParticleTypes.ASH,spot.x,spot.y,spot.z,1,0,0,0,0);
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        if(targetId!=null)tag.putUUID("ChainTarget",targetId);
        tag.putBoolean("ChainReturning",returning);
        tag.putInt("ChainPullTicks",pullTicks);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        targetId=tag.hasUUID("ChainTarget")?tag.getUUID("ChainTarget"):null;
        returning=tag.getBoolean("ChainReturning");
        pullTicks=tag.getInt("ChainPullTicks");
    }
}
