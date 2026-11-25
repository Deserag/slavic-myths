import org.slavicmyths.desert.*;
import static org.slavicmyths.desert.IndrikRules.*;
public class GeometryCheck {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  var g=IndrikGeometry.INSTANCE;check(g.count==3669&&g.groups.length==23,"geometry count");float[] v=new float[24];int faces=0;for(int m:g.mask)faces+=Integer.bitCount(m);
  check(phase(1)==1&&phase(.7F)==2&&phase(.35F)==3,"phase thresholds");check(charges(3)==3&&charges(2)==2,"charge counts");check(logicalHealth(1024,1024)==1500,"logical HP");check(Math.abs(internalDamage(1500,1024)-1024)<.01,"damage scaling");
  for(int a=0;a<LENGTH.length;a++)for(int n=0;n<=100;n++){float t=n/100F;float[] p=g.sample(a,t);for(int i=0;i<g.count;i++){g.cube(i,p,v);for(float x:v)check(Float.isFinite(x),"non-finite vertex");}}
  int visible=0;float[] p=g.sample(BURROW,.5F);for(int i=0;i<g.count;i++){g.cube(i,p,v);if(g.visible(i,BURROW,.5F,v)){visible++;check((g.flags[i]&2)!=0,"non-spike above ground");}}
  check(visible>0&&visible<g.count/4,"burrow silhouette");
  System.out.printf("PASS: %d cuboids, %d bones, %d faces, %d visible underground; 13 x 101 pose samples finite%n",g.count,g.groups.length,faces,visible);
 }
}
