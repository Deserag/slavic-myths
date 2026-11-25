import org.slavicmyths.desert.HalaRules;
import org.slavicmyths.desert.HalaGeometry;
public class RulesCheck {
 public static void main(String[] args){
  var g=HalaGeometry.INSTANCE;if(g.count!=2781||g.groups.length<12)throw new AssertionError("geometry count");
  for(int a=0;a<12;a++)for(int i=0;i<=20;i++){var v=g.sample(a,i/20F);if(v.length!=2781*24)throw new AssertionError("mesh");for(float f:v)if(!Float.isFinite(f))throw new AssertionError("nonfinite");}
  float[] idle=g.sample(0,0);float[] min={Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY},max={Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};for(int i=0;i<idle.length;i++){int k=i%3;min[k]=Math.min(min[k],idle[i]);max[k]=Math.max(max[k],idle[i]);}
  double[] lo={-98.39253678228063,0.05752092635576833,-40.77340386360786},hi={98.39253678228063,116.38317670246694,27.30820518199046};for(int i=0;i<3;i++)if(Math.abs(min[i]-lo[i]/14)>1e-5||Math.abs(max[i]-hi[i]/14)>1e-5)throw new AssertionError("approved V2 bounds changed");
  var collision=HalaGeometry.Collision.INSTANCE;
  if(!java.util.Arrays.equals(g.groups,collision.groups))throw new AssertionError("collision groups changed");
  float[] scratch=new float[g.count*24];long hullPoints=0,fullPoints=0;
  for(int a=0;a<HalaRules.LENGTH.length;a++)for(int tick=0;tick<=HalaRules.LENGTH[a];tick++){
   float progress=(float)tick/HalaRules.LENGTH[a];g.sampleInto(a,progress,scratch);
   var frame=collision.sample(a,progress);double[] tip=g.tip(scratch);
   if(Math.abs(tip[0]-frame.tipX())>1e-6||Math.abs(tip[1]-frame.tipY())>1e-6||Math.abs(tip[2]-frame.tipZ())>1e-6)throw new AssertionError("staff anchor drift");
   for(var hull:frame.hulls())hullPoints+=hull.xz().length/2;
   fullPoints+=g.count*8;
   for(double yaw:new double[]{0,.37,1.11,2.6}){
    double c=Math.cos(yaw),s=Math.sin(yaw);
    double[][] expected=new double[g.groups.length][6];
    for(double[] b:expected){java.util.Arrays.fill(b,0,3,Double.POSITIVE_INFINITY);java.util.Arrays.fill(b,3,6,Double.NEGATIVE_INFINITY);}
    for(int i=0;i<g.count;i++)for(int k=0;k<8;k++){
     int at=i*24+k*3;double x=scratch[at]*c-scratch[at+2]*s,y=scratch[at+1],z=scratch[at]*s+scratch[at+2]*c;
     double[] b=expected[g.part[i]];b[0]=Math.min(b[0],x);b[1]=Math.min(b[1],y);b[2]=Math.min(b[2],z);b[3]=Math.max(b[3],x);b[4]=Math.max(b[4],y);b[5]=Math.max(b[5],z);
    }
    for(int i=0;i<expected.length;i++){
     var hull=frame.hulls()[i];float[] xz=hull.xz();double lx=Double.POSITIVE_INFINITY,lz=lx,hx=-lx,hz=hx;
     for(int k=0;k<xz.length;k+=2){double x=xz[k]*c-xz[k+1]*s,z=xz[k]*s+xz[k+1]*c;lx=Math.min(lx,x);lz=Math.min(lz,z);hx=Math.max(hx,x);hz=Math.max(hz,z);}
     double[] actual={lx,hull.minY(),lz,hx,hull.maxY(),hz};
     for(int k=0;k<6;k++)if(Math.abs(expected[i][k]-actual[k])>2e-5)throw new AssertionError("hull drift: "+a+"/"+tick+"/"+i+"/"+k);
    }
   }
  }
  if(hullPoints>=fullPoints/4)throw new AssertionError("collision hulls failed to reduce geometry");
  System.out.println("Hala collision hull vertices: "+hullPoints+" vs "+fullPoints);
  if(HalaRules.stage(39)!=1||HalaRules.stage(40)!=2||HalaRules.stage(80)!=3||HalaRules.outgoing(3,.1F)!=0||HalaRules.cool(80,true)>=HalaRules.cool(80,false))throw new AssertionError("burn rules");
  if(java.util.Arrays.equals(g.sample(0,0),g.sample(0,.5F)))throw new AssertionError("idle animation lost");
  if(HalaRules.auraPulse(195)||!HalaRules.auraPulse(200)||HalaRules.auraPulse(215)||!HalaRules.auraPulse(220))throw new AssertionError("10 second aura timing");
  System.out.println("Hala V2 geometry and burn rules verified");
 }
}
