package org.slavicmyths.navya;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;

/** Ghost glob, bone thorn and non-griefing 4.5-block thrown trunk. */
public final class NavyaShot extends Projectile {
    private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(NavyaShot.class,EntityDataSerializers.INT);
    public NavyaShot(EntityType<? extends NavyaShot> type,Level world) { super(type,world); }
    public int mode() { return entityData.get(MODE); }
    public void mode(int mode) { entityData.set(MODE,Math.max(0,Math.min(2,mode))); }
    @Override public net.minecraft.sounds.SoundSource getSoundSource() { return net.minecraft.sounds.SoundSource.HOSTILE; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(MODE,0); }
    @Override protected boolean canHitEntity(Entity entity) { return super.canHitEntity(entity) && !(entity instanceof NavyaMob) && !(entity instanceof NavyaShot) && !(entity instanceof NavyaRoots); }
    @Override public void tick() {
        super.tick();
        Vec3 velocity=getDeltaMovement();
        if(!level().isClientSide) {
            if(tickCount>100 || !level().hasChunkAt(blockPosition())) {discard();return;}
            if(velocity.lengthSqr()>16) {discard();return;}
            var sweep=new AABB(position(),position().add(velocity)).inflate(mode()==2?2.7:.5);
            for(int cx=net.minecraft.util.Mth.floor(sweep.minX)>>4;cx<=(net.minecraft.util.Mth.floor(sweep.maxX)>>4);cx++)
                for(int cz=net.minecraft.util.Mth.floor(sweep.minZ)>>4;cz<=(net.minecraft.util.Mth.floor(sweep.maxZ)>>4);cz++)
                    if(!level().hasChunk(cx,cz)){discard();return;}
            HitResult hit=ProjectileUtil.getHitResultOnMoveVector(this,this::canHitEntity);
            // The trunk's visible long axis has a swept collision, not a point hit.
            if(mode()==2 && hit.getType()==HitResult.Type.MISS) {
                Vec3 axis=velocity.normalize();
                for(double offset:new double[]{-2.25,-1.1,1.1,2.25}) {
                    Vec3 start=position().add(axis.scale(offset)),end=start.add(velocity);
                    var block=level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
                    if(block.getType()!=HitResult.Type.MISS) {hit=block;break;}
                    var entity=ProjectileUtil.getEntityHitResult(level(),this,start,end,new AABB(start,end).inflate(.45),this::canHitEntity);
                    if(entity!=null) {hit=entity;break;}
                }
            }
            if(hit.getType()!=HitResult.Type.MISS) {onHit(hit);if(isRemoved())return;}
        }
        setPos(position().add(velocity));
        updateRotation();
        setDeltaMovement(velocity.scale(.995).add(0,mode()==2?-.025:mode()==1?-.015:0,0));
    }
    @Override protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if(level().isClientSide || !(result.getEntity() instanceof LivingEntity target))return;
        if(target.hurt(damageSources().thrown(this,getOwner()),mode()==2?18:mode()==1?9:7)) {
            if(mode()==0)target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,0),getOwner());
            if(mode()==2) {Vec3 velocity=getDeltaMovement();target.knockback(1,-velocity.x,-velocity.z);}
        }
    }
    @Override protected void onHit(HitResult result) {
        super.onHit(result);
        if(!level().isClientSide) {playSound(NavyaSounds.get(mode()==2?"trunk_hit":"shot_hit"),.65F,1);discard();}
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag);tag.putInt("NavyaMode",mode());tag.putInt("NavyaAge",tickCount); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag);mode(tag.getInt("NavyaMode"));tickCount=Math.max(0,Math.min(100,tag.getInt("NavyaAge"))); }
}
