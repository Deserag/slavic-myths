package org.slavicmyths.sanctuary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
public final class SanctuaryGeometry {
 public static String id(boolean spring){return spring?"abode_pure_spring":"sanctuary_unfading_fire";}
 public static int width(boolean spring){return spring?96:80;}public static int depth(boolean spring){return spring?86:76;}
 public static BlockPos rotate(int x,int y,int z,Rotation r){return new BlockPos(x,y,z).rotate(r);}
 public static BlockPos root(BlockPos min,boolean spring,Rotation r){int w=width(spring)-1,d=depth(spring)-1;return min.offset(r==Rotation.CLOCKWISE_90?d:r==Rotation.CLOCKWISE_180?w:0,0,r==Rotation.COUNTERCLOCKWISE_90?w:r==Rotation.CLOCKWISE_180?d:0);}
 public static BoundingBox box(BlockPos origin,Rotation r,int x0,int y0,int z0,int x1,int y1,int z1){var a=origin.offset(rotate(x0,y0,z0,r));var b=origin.offset(rotate(x1,y1,z1,r));return BoundingBox.fromCorners(a,b);}
 public static BoundingBox guardian(BlockPos origin,boolean spring,Rotation r){return spring?box(origin,r,69,1,8,92,10,28):box(origin,r,63,1,33,78,10,54);}
 public static BoundingBox arena(BlockPos origin,boolean spring,Rotation r){return spring?box(origin,r,72,1,13,86,8,27):box(origin,r,64,2,39,77,8,52);}
 public static BlockPos pedestal(BlockPos origin,boolean spring,Rotation r){return origin.offset(rotate(spring?81:71,spring?2:3,spring?12:37,r));}
 public static BlockPos spawnAnchor(BlockPos origin,boolean spring,Rotation r){return origin.offset(rotate(spring?81:71,spring?1:2,spring?21:47,r));}
 public static BlockPos door(BlockPos origin,boolean spring,Rotation r){return origin.offset(rotate(spring?81:61,2,spring?29:43,r));}
 private SanctuaryGeometry(){}
}
