package org.slavicmyths.furniture;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public final class SeatEntity extends Entity {
 private BlockPos chair=BlockPos.ZERO;
 public SeatEntity(EntityType<?>type,Level w){super(type,w);noPhysics=true;setNoGravity(true);}
 public static boolean sit(Level w,BlockPos pos,LivingEntity rider){
  if(rider.isPassenger()||!w.getEntitiesOfClass(SeatEntity.class,new AABB(pos)).isEmpty()||!w.isEmptyBlock(pos.above()))return false;
  SeatEntity seat=Furniture.SEAT.get().create(w);seat.chair=pos;seat.setPos(pos.getX()+.5,pos.getY()+.35,pos.getZ()+.5);w.addFreshEntity(seat);if(!rider.startRiding(seat,true)){seat.discard();return false;}return true;
 }
 @Override public void tick(){super.tick();if(!level().isClientSide&&(getPassengers().isEmpty()||!(level().getBlockState(chair).getBlock()instanceof FurnitureBlock)||!((FurnitureBlock)level().getBlockState(chair).getBlock()).seat())){ejectPassengers();discard();}}
 @Override public Vec3 getPassengerRidingPosition(Entity passenger){return position().add(0,.05,0);}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){for(net.minecraft.core.Direction d:net.minecraft.core.Direction.Plane.HORIZONTAL){BlockPos p=chair.relative(d);if(level().isEmptyBlock(p)&&level().isEmptyBlock(p.above())&&level().getBlockState(p.below()).isSolid())return Vec3.atBottomCenterOf(p);}return position().add(0,1,0);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){}
 @Override protected void readAdditionalSaveData(CompoundTag n){}
 @Override protected void addAdditionalSaveData(CompoundTag n){}

}
