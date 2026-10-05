package org.slavicmyths.hunt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
/** One shared launch gate: turn body and head before locking heading. */
public final class DashFacing {
 public static float wrap(float angle){angle%=360;if(angle>=180)angle-=360;if(angle<-180)angle+=360;return angle;}
 public static float yaw(double x,double z){return (float)Math.toDegrees(Math.atan2(z,x))-90;}
 public static float step(float current,float desired,float limit){float delta=wrap(desired-current);return current+Math.max(-limit,Math.min(limit,delta));}
 public static boolean aligned(float current,float desired,float threshold){return Math.abs(wrap(desired-current))<=threshold;}
 public static boolean turn(Mob e,Vec3 direction,float threshold){float desired=yaw(direction.x,direction.z);e.setYRot(step(e.getYRot(),desired,12));e.yBodyRot=e.getYRot();e.setYHeadRot(e.getYRot());return aligned(e.getYRot(),desired,threshold);}
 public static void lock(Mob e,Vec3 direction){e.setYRot(yaw(direction.x,direction.z));e.yBodyRot=e.getYRot();e.setYHeadRot(e.getYRot());}
 private DashFacing(){}
}
