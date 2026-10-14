package org.slavicmyths.gorynych;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Shared authored-model kinematics. Coordinates follow ModelPart (pixels, Y down). */
public final class GorynychPose {
 public static float lane(int head,float yaw){return head==0?Mth.clamp(yaw,0,40):head==2?Mth.clamp(yaw,-40,0):Mth.clamp(yaw,-12,12);}
 public static float bend(int head,float time,boolean folded,boolean duck){return folded?1.3f:duck?.8f:(float)Math.sin(time*.04+head)*.035f;}
 public static float rootPitch(int head,float pitch,float time,boolean folded,boolean duck){return bend(head,time,folded,duck)+(folded||duck?0:(float)Math.toRadians(Mth.clamp(pitch,-35,65)*.7));}
 private static float smooth(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
 public static float extension(int action,float age){if(action==3)return 32;if(action==4)return 32*(1-smooth(age/18));if(action==0)return 0;return 32*smooth((age-24)/6)*(1-smooth((age-34)/18));}
 public static boolean contact(int action,int age){return (action==1||action==2)&&age>=30&&age<=34;}
 public static Vec3 offset(int head,float bodyYaw,float relativeYaw,float pitch,float time,boolean folded,boolean duck,boolean flying,int action,float age){
  Matrix4f m=new Matrix4f().translate(0,-20+(float)Math.sin(time*.04)*.6f,12).rotateX(flying?-.12f:0).translate((head-1)*30,4,-39).rotateY((float)Math.toRadians(folded?0:lane(head,relativeYaw))).rotateX(rootPitch(head,pitch,time,folded,duck));
  for(int i=1;i<5;i++)m.translate(0,-15,-5).rotateX(folded||duck?0:(float)Math.toRadians(pitch*.18));
  m.translate(0,-11,-8-(head==2?extension(action,age):0));
  // Center beneath the visible skull, toward the jaw; PartEntity uses its bottom center.
  Vector3f v=m.transformPosition(new Vector3f(0,6,-10));double x=v.x/16.,y=1.5-v.y/16.,forward=-v.z/16.,r=Math.toRadians(bodyYaw);
  return new Vec3(Math.cos(r)*x-Math.sin(r)*forward,y,Math.sin(r)*x+Math.cos(r)*forward);
 }
 private GorynychPose(){}
}
