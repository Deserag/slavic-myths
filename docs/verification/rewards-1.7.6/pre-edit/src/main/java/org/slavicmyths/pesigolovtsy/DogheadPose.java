package org.slavicmyths.pesigolovtsy;
import org.slavicmyths.pesigolovtsy.DogheadRules.*;
import net.minecraft.world.phys.Vec3;
/** Model-space weapon anchors shared by the renderer and physical arrow release. Y down, front -Z. */
public final class DogheadPose {
 public record Weapon(float x,float y,float z,float pitch,float yaw,float roll){}
 public static float smooth(float t,float a,float b){float u=Math.clamp((t-a)/(b-a),0,1);return u*u*(3-2*u);}
 public static float key(float t,float[] times,float[] values){if(t<=times[0])return values[0];for(int i=1;i<times.length;i++)if(t<=times[i])return values[i-1]+(values[i]-values[i-1])*smooth(t,times[i-1],times[i]);return values[values.length-1];}
 public static float stride(Role r,boolean run){return (run?.85F:.65F)*(r==Role.SCOUT?.88F:r==Role.LEADER?1.12F:1);}
 public static float shoulder(Role r){return r==Role.SCOUT?23:r==Role.HUNTER?25:27;}
 public static float sample(Clip c,float t,int column,float fallback){var a=DogheadKeyframes.samples(c);if(a.length==0)return fallback;if(t<=a[0][0])return a[0][column];for(int i=1;i<a.length;i++)if(t<=a[i][0])return a[i-1][column]+(a[i][column]-a[i-1][column])*smooth(t,a[i-1][0],a[i][0]);return a[a.length-1][column];}
 public static float pitch(Role r,Clip c,float t){return (float)Math.toRadians(sample(c,t,1,r==Role.SCOUT?12:r==Role.HUNTER?8:6));}
 public static Weapon weapon(Role r,Clip c,float t){
  if(r==Role.SCOUT)return new Weapon(4,-1,-7,0,0,0);
  if(r==Role.HUNTER){float thrust=c==Clip.THRUST?key(t,new float[]{0,3,6,8,10,12,16,20,24},new float[]{0,3,3,0,-2,-3,1,0,0}):0;float yaw=c==Clip.SWEEP?key(t,new float[]{0,5,9,13,17,22,28},new float[]{-.85F,-1.3F,-.6F,.9F,1.3F,.3F,-.85F}):c==Clip.THRUST?key(t,new float[]{0,3,6,8,10,16,24},new float[]{.85F,.6F,.25F,.05F,0,.25F,.85F}):.85F;return new Weapon(c==Clip.SWEEP?2:-1.5F,2,c==Clip.SWEEP?-5:-8+thrust,c==Clip.THRUST?-pitch(r,c,t)-.55F*(1-smooth(t,5,10)+smooth(t,16,24)):c==Clip.SWEEP?-.20F:-.75F,yaw-(float)Math.toRadians(sample(c,t,2,0)),-.14F);}
  if(c==Clip.HOWL){float lower=smooth(t,0,4)*(1-smooth(t,23,28));return new Weapon(-2,4+3*lower,-8+2*lower,-.32F+1.22F*lower,-(float)Math.toRadians(sample(c,t,2,0)),-.15F);}
  if(c==Clip.CHOP){float p=key(t,new float[]{0,5,10,14,17,21,24,29,34,38},new float[]{-.32F,-1.2F,-2.6F,-2.6F,-1.8F,-.2F,.5F,.2F,-.2F,-.32F});float lift=key(t,new float[]{0,5,10,14,17,21,24,29,38},new float[]{0,-2,-8,-8,-5,-2,4,3,0});float forward=key(t,new float[]{0,5,10,14,17,21,24,29,38},new float[]{-8,-5,-3,-3,-5,-12.2F,-8,-8,-8});return new Weapon(-2,4+lift,forward,p,-(float)Math.toRadians(sample(c,t,2,0)),-.15F);}
  if(c==Clip.CLEAVE){float yaw=key(t,new float[]{0,6,11,15,19,22,28,34},new float[]{0,-1.1F,-1.4F,-.3F,1,1.3F,.3F,0});return new Weapon(2,4,-5,-.32F,yaw,-.15F);}
  return new Weapon(-2,4,-8,-.32F,0,-.15F);
 }
 public static Weapon blendedWeapon(Role r,Clip c,float t){var posed=weapon(r,c,t);if(c!=Clip.SWEEP&&c!=Clip.CLEAVE)return posed;var ready=weapon(r,Clip.READY,0);float blend=smooth(t,0,4)*(1-smooth(t,c.duration-5,c.duration));return new Weapon(ready.x+(posed.x-ready.x)*blend,ready.y+(posed.y-ready.y)*blend,ready.z+(posed.z-ready.z)*blend,ready.pitch+(posed.pitch-ready.pitch)*blend,ready.yaw+(posed.yaw-ready.yaw)*blend,ready.roll+(posed.roll-ready.roll)*blend);}
 public static Vec3 nock(Role r,Clip c,float t,float bodyYaw){var a=weapon(r,c,t);var pos=new org.joml.Matrix4f().rotateY((float)Math.toRadians(180-bodyYaw)).scale(-1F/16,-1F/16,1F/16).translate(0,-shoulder(r),0).rotateY((float)Math.toRadians(sample(c,t,2,0))).rotateX(pitch(r,c,t)).translate(a.x,a.y,a.z).rotateZYX(a.roll,a.yaw,a.pitch).transformPosition(new org.joml.Vector3f(0,0,0));return new Vec3(pos.x,pos.y,pos.z);}
 private DogheadPose(){}
}
