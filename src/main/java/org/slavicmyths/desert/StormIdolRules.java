package org.slavicmyths.desert;

import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

/** All coordinates are in blocks, +Z faces the target. Shared by server hitboxes and renderer. */
public final class StormIdolRules {
 public static final int IDLE=0,COMBO=1,LAUNCH=2,SLAM=3,UPDRAFT=4,STORM=5;
 public static final int[] LENGTH={1,76,72,66,64,120};
 public static final int[] COOLDOWN={0,70,105,180,120,360};
 public static final float HP=1500, INTERNAL_HP=1024, SCALE=.075F;
 public static final int FISTS=4;
 public static int phase(float f){return f>.70F?1:f>.35F?2:3;}
 public static double smooth(double t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
 public static double envelope(double t,double a,double b,double c,double d){return smooth((t-a)/(b-a))*(1-smooth((t-c)/(d-c)));}
 public static Vec3 rest(int i,float time){
  int s=i%2==0?-1:1;return new Vec3(s*28*SCALE,(i<2?48:29)*SCALE+Math.sin(time*.15+i*1.5)*.10,4*SCALE+Math.cos(time*.13+i)*.06);
 }
 public static double bob(float time){return .09*Math.sin(time*.13);}
 public static Vec3 fist(int a,int i,float t,float time,Vec3 target){
  Vec3 c=rest(i,time);int s=i%2==0?-1:1;Vec3 hit=target.add(s*.18,0,0);
  switch(a){
   case COMBO,LAUNCH:{
    if(a==LAUNCH&&i>=2)return c;
    double q=t-(a==COMBO?3+i*14:6+i*10);
    double prep=a==COMBO?5:7,flight=a==COMBO?5:12,hold=3,back=a==COMBO?7:19;
    Vec3 windup=c.add(s*.32,.28,-.55);
    if(q<0)return c;
    if(q<prep)return c.lerp(windup,smooth(q/prep));
    if(q<prep+flight)return windup.lerp(hit,smooth((q-prep)/flight));
    if(q<prep+flight+hold)return hit;
    double u=(q-prep-flight-hold)/back;
    if(u<1)return hit.lerp(c,smooth(u)).add(s*(a==LAUNCH?.65:.15)*Math.sin(Math.PI*u),.45*Math.sin(Math.PI*u),0);
    return c;
   }
   case SLAM:{
    double q=t-3-i*1.5;Vec3 low=hit.add(s*(i<2?.65:1.3),-.55,i<2?-.65:.65);
    Vec3 high=low.add(0,4.7,0);
    if(q<0)return c;
    if(q<15)return c.lerp(high,smooth(q/15));
    if(q<25)return high;
    if(q<31)return high.lerp(low,smooth((q-25)/6));
    if(q<35)return low;
    if(q<57)return low.lerp(c,smooth((q-35)/22)).add(0,.5*Math.sin(Math.PI*(q-35)/22),0);
    return c;
   }
   case UPDRAFT:{double e=envelope(t,3,15,32,58);return c.add(s*.25*e,.85*e,.2*e);}
   case STORM:{
    double e=envelope(t,4,22,91,118),ang=i*Math.PI/2+t*.12;
    return c.lerp(new Vec3(3.2*Math.cos(ang),2.4+.5*Math.sin(ang*2),3.2*Math.sin(ang)),e);
   }
   default:return c;
  }
 }
 public static boolean active(int a,int i,int t){
  if(a==COMBO||a==LAUNCH){if(a==LAUNCH&&i>=2)return false;int q=t-(a==COMBO?3+i*14:6+i*10);return q>=(a==COMBO?5:7)&&q<=(a==COMBO?13:22);}
  if(a==SLAM){double q=t-3-i*1.5;return q>=25&&q<=35;}
  return a==STORM&&t>=25&&t<=91;
 }
 private StormIdolRules(){}
}
