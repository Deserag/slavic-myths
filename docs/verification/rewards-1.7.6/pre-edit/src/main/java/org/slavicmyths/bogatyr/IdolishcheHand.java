package org.slavicmyths.bogatyr;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
/** Unsaved multipart wrist target, forwarding damage once through the parent pipeline. */
public final class IdolishcheHand extends net.neoforged.neoforge.entity.PartEntity<IdolishcheEntity> {
    private final EntityDimensions size=EntityDimensions.scalable(1.6F,1.5F);
    private final boolean right;
    public IdolishcheHand(IdolishcheEntity parent,boolean right){super(parent);this.right=right;refreshDimensions();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
    @Override protected void readAdditionalSaveData(CompoundTag n){}
    @Override protected void addAdditionalSaveData(CompoundTag n){}
    @Override public EntityDimensions getDimensions(Pose p){return size==null?EntityDimensions.scalable(1.6F,1.5F):size;}
    @Override public boolean isPickable(){return getParent().isAlive();}
    @Override public boolean canBeCollidedWith(){return getParent().isAlive();}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public boolean is(Entity e){return e==this||e==getParent();}
    @Override public boolean hurt(DamageSource s,float amount){var b=getParent();b.handHit=right;try{return b.hurt(s,amount);}finally{b.handHit=false;}}
}
