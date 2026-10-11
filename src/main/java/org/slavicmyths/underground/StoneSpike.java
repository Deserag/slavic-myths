package org.slavicmyths.underground;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
public final class StoneSpike extends Entity {
 public UUID owner;
 public StoneSpike(EntityType<?> t,Level l){super(t,l);noPhysics=true;}
 protected void defineSynchedData(SynchedEntityData.Builder b){}
 public boolean shouldBeSaved(){return false;}
 protected void readAdditionalSaveData(CompoundTag n){if(n.hasUUID("Owner"))owner=n.getUUID("Owner");tickCount=11;}
 protected void addAdditionalSaveData(CompoundTag n){if(owner!=null)n.putUUID("Owner",owner);}
 public void tick(){super.tick();if(!level().isClientSide&&(tickCount>10||owner!=null&&level() instanceof net.minecraft.server.level.ServerLevel s&&(s.getEntity(owner)==null||!s.getEntity(owner).isAlive())))discard();}
}
