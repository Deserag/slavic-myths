package org.slavicmyths.client;
import net.minecraft.client.model.geom.ModelPart;
/** Local two-link foot target, with separate stance/swing. No world/tick searches. */
public final class MobMotion {
 public record Step(float forward,float lift,boolean swing){}
 public static Step step(float phase,float duty,float stride,float lift){float cycle=(float)(phase/(Math.PI*2)-Math.floor(phase/(Math.PI*2)));if(cycle<duty)return new Step(stride*(.5F-cycle/duty),0,false);float s=(cycle-duty)/(1-duty);float smooth=s*s*(3-2*s);return new Step(stride*(-.5F+smooth),(float)Math.sin(s*Math.PI)*lift,true);}
 public static void leg(ModelPart yaw,ModelPart upper,ModelPart knee,ModelPart foot,int side,float reach,float drop,float a,float b,float phase,float amount,float duty,float attack){
  Step s=step(phase,duty,7,3.5F);float z=s.forward*amount;float lift=s.lift*amount+attack*4;
  float horizontal=(float)Math.hypot(reach,z),vertical=drop-lift;
  float cos=Math.clamp((horizontal*horizontal+vertical*vertical-a*a-b*b)/(2*a*b),-.995F,.995F);float bend=(float)Math.acos(cos);
  float angle=(float)Math.atan2(vertical,horizontal)-(float)Math.atan2(b*Math.sin(bend),a+b*Math.cos(bend));
  yaw.yRot=(float)Math.atan2(-side*z,reach);upper.zRot=side*angle;knee.zRot=side*(bend-(float)Math.PI/2);
  foot.zRot=-side*(angle+bend-(float)Math.PI/2); // Ground-parallel sole during stance.
 }
 public static float envelope(float time,float release,float duration){if(time<0||time>=duration)return 0;float x=time<release?time/Math.max(1,release):1-(time-release)/Math.max(1,duration-release);return x*x*(3-2*x);}
 private MobMotion(){}
}
