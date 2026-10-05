package org.slavicmyths.hunt;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.vector.Vector3d;
/** One shared launch gate: turn body and head before locking heading. */
public final class DashFacing {
 public static float wrap(float angle){angle%=360;if(angle>=180)angle-=360;if(angle<-180)angle+=360;return angle;}
 public static float yaw(double x,double z){return (float)Math.toDegrees(Math.atan2(z,x))-90;}
 public static float step(float current,float desired,float limit){float delta=wrap(desired-current);return current+Math.max(-limit,Math.min(limit,delta));}
 public static boolean aligned(float current,float desired,float threshold){return Math.abs(wrap(desired-current))<=threshold;}
 public static boolean turn(MobEntity e,Vector3d direction,float threshold){float desired=yaw(direction.x,direction.z);e.yRot=step(e.yRot,desired,12);e.yBodyRot=e.yRot;e.setYHeadRot(e.yRot);return aligned(e.yRot,desired,threshold);}
 public static void lock(MobEntity e,Vector3d direction){e.yRot=yaw(direction.x,direction.z);e.yBodyRot=e.yRot;e.setYHeadRot(e.yRot);}
 private DashFacing(){}
}
