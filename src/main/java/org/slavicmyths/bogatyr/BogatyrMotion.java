package org.slavicmyths.bogatyr;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.bogatyr.BogatyrRules.State;
/** Shared wrist kinematics: server multipart target and client cuboids use identical pivots. */
public final class BogatyrMotion {
    public record Arm(float chestX,float chestY,float shoulder,float forearm,float bellyScale){}
    private static float ramp(float t,float a,float b){float u=Math.clamp((t-a)/(b-a),0,1);return u*u*(3-2*u);}
    private static float key(float t,float[] ts,float[] vs){if(t<=ts[0])return vs[0];for(int i=1;i<ts.length;i++)if(t<=ts[i])return vs[i-1]+(vs[i]-vs[i-1])*ramp(t,ts[i-1],ts[i]);return vs[vs.length-1];}
    private static float rad(float d){return (float)Math.toRadians(d);}
    public static Arm arm(State s,float t,float age){float chest=(float)Math.sin(age*.08)*.02F,cy=0,shoulder=0,forearm=0,belly=1+(float)Math.sin(age*.07)*.009F;
        switch(s){case FIST_WINDUP->{shoulder=rad(key(t,new float[]{0,6,15,22},new float[]{0,-35,-65,-95}));forearm=rad(65)*ramp(t,6,15);cy=-rad(12)*ramp(t,0,15);}
            case FIST_SLAM->{shoulder=rad(key(t,new float[]{0,5,8,12,17},new float[]{-95,82,95,45,0}));chest=rad(14)*(ramp(t,0,8)-ramp(t,8,17));}
            case GRAB_START,GRAB_HOLD,GRAB_THROW->{shoulder=-rad(s==State.GRAB_THROW?key(t,new float[]{0,8,12,24},new float[]{70,75,15,0}):s==State.GRAB_HOLD?key(t,new float[]{0,8,18},new float[]{45,65,90}):45*ramp(t,0,25));forearm=-rad(38);}
            case FEED_GRAB,FEED_CHEW->{float k=s==State.FEED_GRAB?ramp(t,0,24):1-ramp(t,40,52);shoulder=-rad(112)*k;forearm=-rad(55)*k;}
            case FEED_SUMMON->chest=rad(20)*(ramp(t,0,20)-ramp(t,29,50));
            case BREATH_WINDUP,BREATH_ACTIVE->{float k=s==State.BREATH_WINDUP?ramp(t,0,25):1-ramp(t,55,60);chest=-rad(8)*k;belly+=.07F*k;}
            case CHARGE_WINDUP,CHARGE_RUNNING,CONTEST->{chest=rad(s==State.CHARGE_WINDUP?18*ramp(t,0,28):23);if(s==State.CONTEST)shoulder=-rad(45);}
            case STUNNED->chest=rad(8)+(float)Math.sin(t*.12)*.05F;
            case RAGE_TRANSITION->{chest=rad(key(t,new float[]{0,15,25,40,65},new float[]{0,35,35,-8,0}));shoulder=rad(45)*(ramp(t,0,15)-ramp(t,40,65));}
            default->{}
        }return new Arm(chest,cy,shoulder,forearm,belly);
    }
    public static Vec3 wrist(State s,float t,float age,float yaw){var a=arm(s,t,age);var m=new org.joml.Matrix4f().translate(0,-28,0).translate(0,-6,-1).scale(1,1,a.bellyScale).translate(0,-28,1).rotateZYX(0,a.chestY,a.chestX).translate(-37,-26,0).rotateX(a.shoulder).translate(0,11,0).translate(0,24,0).rotateX(a.forearm).translate(0,22,-2);var v=m.transformPosition(new org.joml.Vector3f());double x=v.x/16,y=(24-v.y)/16,z=-v.z/16,angle=Math.toRadians(yaw);return new Vec3(x*Math.cos(angle)-z*Math.sin(angle),y,x*Math.sin(angle)+z*Math.cos(angle));}
    private BogatyrMotion(){}
    public static Vec3 leftWrist(State s,float t,float age,float yaw){var a=arm(s,t,age);float shoulder=s==State.FEED_SUMMON?(float)Math.toRadians(65)*(ramp(t,0,20)-ramp(t,29,50)):0;var m=new org.joml.Matrix4f().translate(0,-28,0).translate(0,-6,-1).scale(1,1,a.bellyScale).translate(0,-28,1).rotateZYX(0,a.chestY,a.chestX).translate(37,-26,0).rotateX(shoulder).translate(0,11,0).translate(0,24,0).translate(0,22,-2);var v=m.transformPosition(new org.joml.Vector3f());double x=v.x/16,y=(24-v.y)/16,z=-v.z/16,angle=Math.toRadians(yaw);return new Vec3(x*Math.cos(angle)-z*Math.sin(angle),y,x*Math.sin(angle)+z*Math.cos(angle));}
}
