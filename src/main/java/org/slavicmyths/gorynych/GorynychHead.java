package org.slavicmyths.gorynych;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
public final class GorynychHead extends net.neoforged.neoforge.entity.PartEntity<Gorynych> {
 public final int head;private final EntityDimensions size;
 public GorynychHead(Gorynych boss,int head){super(boss);this.head=head;size=EntityDimensions.scalable(2.2f,1.8f);refreshDimensions();}
 @Override public EntityDimensions getDimensions(Pose pose){return size;}
 @Override public boolean isPickable(){return getParent().isAlive();}
 @Override public boolean is(Entity e){return e==this||e==getParent();}
 @Override public boolean hurt(DamageSource source,float amount){getParent().resistGrab();return getParent().hurt(source,amount);}
 @Override public net.minecraft.world.InteractionResult interact(net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand hand){return getParent().cleanse(p,hand,head);}
 @Override public net.minecraft.world.InteractionResult interactAt(net.minecraft.world.entity.player.Player p,net.minecraft.world.phys.Vec3 hit,net.minecraft.world.InteractionHand hand){return interact(p,hand);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override protected void addAdditionalSaveData(CompoundTag n){}
 @Override protected void readAdditionalSaveData(CompoundTag n){}
 @Override public boolean shouldBeSaved(){return false;}
}
