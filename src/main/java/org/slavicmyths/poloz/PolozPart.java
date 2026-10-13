package org.slavicmyths.poloz;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
public final class PolozPart extends net.neoforged.neoforge.entity.PartEntity<GreatPoloz> {
 public final int segment;public final boolean scar;private final EntityDimensions dimensions;
 public PolozPart(GreatPoloz boss,int index,boolean scar,float width,float height){super(boss);segment=index;this.scar=scar;dimensions=EntityDimensions.scalable(width,height);refreshDimensions();}
 @Override public EntityDimensions getDimensions(Pose pose){return dimensions;}
 @Override public boolean isPickable(){return getParent().isAlive();}
 @Override public boolean is(Entity e){return e==this||e==getParent();}
 @Override public boolean hurt(DamageSource s,float amount){return getParent().hitPart(this,s,amount);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override protected void addAdditionalSaveData(CompoundTag n){}
 @Override protected void readAdditionalSaveData(CompoundTag n){}
 @Override public boolean shouldBeSaved(){return false;}
}
