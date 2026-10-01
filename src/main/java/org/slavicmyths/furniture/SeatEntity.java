package org.slavicmyths.furniture;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
public final class SeatEntity extends Entity {
 private BlockPos chair=BlockPos.ZERO;
 public SeatEntity(EntityType<?>type,World w){super(type,w);noPhysics=true;setNoGravity(true);}
 public static boolean sit(World w,BlockPos pos,LivingEntity rider){
  if(rider.isPassenger()||!w.getEntitiesOfClass(SeatEntity.class,new AxisAlignedBB(pos)).isEmpty()||!w.isEmptyBlock(pos.above()))return false;
  SeatEntity seat=Furniture.SEAT.get().create(w);seat.chair=pos;seat.setPos(pos.getX()+.5,pos.getY()+.35,pos.getZ()+.5);w.addFreshEntity(seat);if(!rider.startRiding(seat,true)){seat.remove();return false;}return true;
 }
 @Override public void tick(){super.tick();if(!level.isClientSide&&(getPassengers().isEmpty()||!(level.getBlockState(chair).getBlock()instanceof FurnitureBlock)||!((FurnitureBlock)level.getBlockState(chair).getBlock()).seat())){ejectPassengers();remove();}}
 @Override public double getPassengersRidingOffset(){return .05;}
 @Override public Vector3d getDismountLocationForPassenger(LivingEntity passenger){for(net.minecraft.util.Direction d:net.minecraft.util.Direction.Plane.HORIZONTAL){BlockPos p=chair.relative(d);if(level.isEmptyBlock(p)&&level.isEmptyBlock(p.above())&&level.getBlockState(p.below()).getMaterial().isSolid())return Vector3d.atBottomCenterOf(p);}return position().add(0,1,0);}
 @Override protected void defineSynchedData(){}
 @Override protected void readAdditionalSaveData(CompoundNBT n){}
 @Override protected void addAdditionalSaveData(CompoundNBT n){}
 @Override public IPacket<?> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
