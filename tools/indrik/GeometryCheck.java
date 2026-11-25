import org.slavicmyths.desert.*;
import static org.slavicmyths.desert.IndrikRules.*;
public class GeometryCheck {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  var g=IndrikGeometry.INSTANCE;check(g.count==3669&&g.groups.length==23,"geometry count");float[] v=new float[24];int faces=0;for(int m:g.mask)faces+=Integer.bitCount(m);
  check(phase(1)==1&&phase(.7F)==2&&phase(.35F)==3,"phase thresholds");check(charges(3)==3&&charges(2)==2,"charge counts");check(logicalHealth(1024,1024)==1500,"logical HP");check(Math.abs(internalDamage(1500,1024)-1024)<.01,"damage scaling");
  for(int a=0;a<LENGTH.length;a++)for(int n=0;n<=100;n++){float t=n/100F;float[] p=g.sample(a,t);for(int i=0;i<g.count;i++){g.cube(i,p,v);for(float x:v)check(Float.isFinite(x),"non-finite vertex");}}
  double[][] lo=new double[g.groups.length][3],hi=new double[g.groups.length][3],refLo=new double[g.groups.length][3],refHi=new double[g.groups.length][3];
  float[] pose=new float[g.groups.length*12];
  for(int a=0;a<LENGTH.length;a++)for(int tick=0;tick<=LENGTH[a];tick++){
   float t=(float)tick/LENGTH[a];g.sampleInto(a,t,pose);
   for(double yaw:new double[]{0,.37,1.11,2.6}){
    double c=Math.cos(yaw),s=Math.sin(yaw);
    for(int i=0;i<refLo.length;i++){java.util.Arrays.fill(refLo[i],Double.POSITIVE_INFINITY);java.util.Arrays.fill(refHi[i],Double.NEGATIVE_INFINITY);}
    for(int i=0;i<g.count;i++){g.cube(i,pose,v);if(!g.visible(i,a,t,v))continue;int b=g.part[i];for(int k=0;k<24;k+=3){
     double x=v[k]*c-v[k+2]*s,y=a==BURROW?Math.max(0,v[k+1]):v[k+1],z=v[k]*s+v[k+2]*c;
     refLo[b][0]=Math.min(refLo[b][0],x);refLo[b][1]=Math.min(refLo[b][1],y);refLo[b][2]=Math.min(refLo[b][2],z);
     refHi[b][0]=Math.max(refHi[b][0],x);refHi[b][1]=Math.max(refHi[b][1],y);refHi[b][2]=Math.max(refHi[b][2],z);
    }}
    g.bounds(a,t,pose,v,0,0,0,c,s,lo,hi);
    for(int i=0;i<lo.length;i++)for(int k=0;k<3;k++){
     check(lo[i][k]==refLo[i][k]||Math.abs(lo[i][k]-refLo[i][k])<2e-5,"collision minimum drift");
     check(hi[i][k]==refHi[i][k]||Math.abs(hi[i][k]-refHi[i][k])<2e-5,"collision maximum drift");
    }
   }
  }
  check(g.supportCount()<g.count*8/2,"collision support did not reduce vertices");
  System.out.println("Indrik collision support: "+g.supportCount()+" vs "+g.count*8);
  int visible=0;float[] p=g.sample(BURROW,.5F);for(int i=0;i<g.count;i++){g.cube(i,p,v);if(g.visible(i,BURROW,.5F,v)){visible++;check((g.flags[i]&2)!=0,"non-spike above ground");}}
  check(visible>0&&visible<g.count/4,"burrow silhouette");
  System.out.printf("PASS: %d cuboids, %d bones, %d faces, %d visible underground; 13 x 101 pose samples finite%n",g.count,g.groups.length,faces,visible);
 }
}
