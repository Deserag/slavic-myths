package org.slavicmyths.combat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
/** Swept body volume: checks the whole route without loading chunks or changing blocks. */
public final class LargeMobClearance {
 public static boolean clear(Entity entity,Vec3 movement){
  var box=entity.getBoundingBox().expandTowards(movement).deflate(.001);
  var world=entity.level();
  for(double x:new double[]{box.minX,box.maxX})for(double z:new double[]{box.minZ,box.maxZ})
   if(!world.hasChunkAt(BlockPos.containing(x,entity.getY(),z)))return false;
  return world.getWorldBorder().isWithinBounds(box)&&world.noCollision(entity,box);
 }
 public static Vec3 steer(Entity entity,Vec3 desired){
  if(clear(entity,desired.scale(3)))return desired;
  for(Vec3 candidate:new Vec3[]{new Vec3(-desired.z,desired.y,desired.x).scale(.6),new Vec3(desired.z,desired.y,-desired.x).scale(.6),new Vec3(0,desired.y,0)})
   if(candidate.lengthSqr()>.00001&&clear(entity,candidate.scale(3)))return candidate;
  return Vec3.ZERO;
 }
 private LargeMobClearance(){}
}