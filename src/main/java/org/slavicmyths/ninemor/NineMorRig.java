package org.slavicmyths.ninemor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
/** Shared server/client articulation: all joints rotate at fixed attachment points. */
public final class NineMorRig {
 public static final float MOUTH_DEPTH=-6.3F;
 public static final float[][] HEAD_POS={
  {-17F,-32F,27F},{0,-35F,28.5F},{17F,-32F,27F},
  {-14F,-25F,32.5F},{0,-28F,34F},{14F,-25F,32.5F},
  {-8.8F,-19.5F,37F},{0,-22F,39F},{8.8F,-19.5F,37F}}; public record HeadPose(float yaw,float pitch,float jaw){}
 public static HeadPose pose(NineMorSerpent e,int head){
  float yaw=Mth.clamp(e.headYaw(head)*Mth.DEG_TO_RAD,-2.1F,2.1F);
  float pitch=Mth.clamp(e.headPitch(head)*Mth.DEG_TO_RAD,-.65F,1.15F);
  int mode=e.headMode(head);float pulse=mode==0?0:Mth.sin(Mth.PI*Mth.clamp(e.headAge(head)/38F,0,1));
  return new HeadPose(yaw,pitch+(mode==NineMorSerpent.GRAB?-.08F:pulse*.025F),mode==0?.06F:(mode==NineMorSerpent.GRAB?.65F:.42F)*pulse);
 }
 public static float bodyY(float age,int phase){return 24F+Mth.sin(age*.055F)*(phase==2?.35F:.08F);}
 public static float bodyPitch(int phase){return phase==2?-.045F:0;}
 public static float[] neckPoint(int head,float t){
  var h=HEAD_POS[head];int row=head/3,lane=head%3-1;float eased=t*t*(3-2*t);
  float x= Mth.lerp(eased,lane*2.85F,h[0])+.6F*Mth.sin(t*Mth.TWO_PI+head*.67F)*Mth.sin(t*Mth.PI);
  float z=Mth.lerp(t,-10.7F+row*.65F,h[1]+2.4F)-.9F*Mth.sin(t*Mth.PI);
  float height=Mth.lerp(t,18F+row*.4F,h[2]-.8F)+.5F*Mth.sin(t*Mth.PI);
  return new float[]{x,-height,z};
 }
 private static void joint(Matrix4f m,float[] p,float[] parent,float pitch,float yaw){m.translate((p[0]-parent[0])/16F,(p[1]-parent[1])/16F,(p[2]-parent[2])/16F).rotateZYX(0,yaw,pitch);}
 public static Vec3 mouth(NineMorSerpent e,int head){
  var pose=pose(e,head);var a=neckPoint(head,0);var b=neckPoint(head,6F/17);var c=neckPoint(head,12F/17);var h=HEAD_POS[head];
  var matrix=new Matrix4f();
  // Keep large world coordinates in doubles: apply the local transform first.
  matrix.rotateY((180-e.getYRot())*Mth.DEG_TO_RAD).scale(-4,-4,4).translate(0,-1.501F,0)
   .translate(0,bodyY(e.tickCount,e.flightPhase())/16F,0).rotateX(bodyPitch(e.flightPhase()));
  joint(matrix,a,new float[]{0,0,0},pose.pitch()*.12F,pose.yaw()*.16F);
  joint(matrix,b,a,pose.pitch()*.22F,pose.yaw()*.24F);
  joint(matrix,c,b,pose.pitch()*.26F,pose.yaw()*.28F);
  joint(matrix,new float[]{h[0],-h[2],h[1]},c,pose.pitch()*.40F,pose.yaw()*.32F);
  var local=matrix.transformPosition(new Vector3f(0,0,MOUTH_DEPTH/16F));
  return e.position().add(local.x,local.y,local.z);
 }
 private NineMorRig(){}
}