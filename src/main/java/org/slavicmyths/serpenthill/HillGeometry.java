package org.slavicmyths.serpenthill;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
public final class HillGeometry {
 public static BlockPos origin(BlockPos min,Rotation r){return min.offset(r==Rotation.CLOCKWISE_90||r==Rotation.CLOCKWISE_180?127:0,0,r==Rotation.COUNTERCLOCKWISE_90||r==Rotation.CLOCKWISE_180?127:0);}
 public static BoundingBox bounds(BlockPos origin,Rotation r){return BoundingBox.fromCorners(origin,origin.offset(new BlockPos(127,143,127).rotate(r)));}
 public static BlockPos local(BlockPos origin,Rotation r,BlockPos world){Rotation inverse=r==Rotation.CLOCKWISE_90?Rotation.COUNTERCLOCKWISE_90:r==Rotation.COUNTERCLOCKWISE_90?Rotation.CLOCKWISE_90:r;return world.subtract(origin).rotate(inverse);}
 private HillGeometry(){}
}
