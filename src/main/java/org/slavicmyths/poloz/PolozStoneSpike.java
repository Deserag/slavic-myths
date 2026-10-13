package org.slavicmyths.poloz;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;
/** Short-lived visible stone projection, not a placed block or terrain edit. */
public final class PolozStoneSpike extends Entity {
 public PolozStoneSpike(EntityType<? extends PolozStoneSpike> t,Level w){super(t,w);noPhysics=true;}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>=24)discard();}
 @Override protected void addAdditionalSaveData(CompoundTag n){n.putInt("Age",tickCount);}
 @Override protected void readAdditionalSaveData(CompoundTag n){tickCount=Math.clamp(n.getInt("Age"),0,24);}
}
