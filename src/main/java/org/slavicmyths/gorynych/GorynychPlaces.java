package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.*;
/** Authored roomy nodes. Underground components never pretend narrow stairs fit a dragon. */
public final class GorynychPlaces {
 public static final int[][] NODES={{62,81,58},{61,81,72},{74,81,72},{53,81,39},{53,81,43},{67,5,90},{67,5,92},{99,3,85},{99,3,89}};
 public static Rotation rotation(CompoundTag n){try{return Rotation.valueOf(n.getString("Rotation"));}catch(IllegalArgumentException bad){return Rotation.NONE;}}
 public static Vec3 node(CompoundTag n,int i){if(n.getBoolean("Standalone"))return Vec3.atBottomCenterOf(BlockPos.of(n.getLong("Origin"))).add((i%3-1)*8,0,(i/3-1)*8);var p=NODES[Math.floorMod(i,NODES.length)];return Vec3.atBottomCenterOf(BlockPos.of(n.getLong("Origin")).offset(new BlockPos(p[0],p[1],p[2]).rotate(rotation(n))));}
 public static AABB bounds(CompoundTag n){var o=BlockPos.of(n.getLong("Origin"));if(n.getBoolean("Standalone"))return new AABB(Vec3.atLowerCornerOf(o.offset(-32,-4,-32)),Vec3.atLowerCornerOf(o.offset(33,40,33)));return AABB.of(org.slavicmyths.serpenthill.HillGeometry.bounds(o,rotation(n)));}
 public static boolean loaded(ServerLevel w,AABB b){for(int x=((int)Math.floor(b.minX))>>4;x<=((int)Math.floor(b.maxX))>>4;x++)for(int z=((int)Math.floor(b.minZ))>>4;z<=((int)Math.floor(b.maxZ))>>4;z++)if(w.getChunkSource().getChunkNow(x,z)==null)return false;return true;}
 public static boolean safe(ServerLevel w,Gorynych e,Vec3 pos){var box=e.getBoundingBox().move(pos.subtract(e.position()));return loaded(w,box.inflate(1))&&w.getWorldBorder().isWithinBounds(box)&&w.noCollision(e,box)&&!w.getBlockState(BlockPos.containing(pos).below()).isAir();}
 private GorynychPlaces(){}
}
