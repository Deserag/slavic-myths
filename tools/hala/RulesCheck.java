import org.slavicmyths.desert.HalaRules;
import org.slavicmyths.desert.HalaGeometry;
public class RulesCheck {
 public static void main(String[] args){
  var g=HalaGeometry.INSTANCE;if(g.count!=2781||g.groups.length<12)throw new AssertionError("geometry count");
  for(int a=0;a<12;a++)for(int i=0;i<=20;i++){var v=g.sample(a,i/20F);if(v.length!=2781*24)throw new AssertionError("mesh");for(float f:v)if(!Float.isFinite(f))throw new AssertionError("nonfinite");}
  float[] idle=g.sample(0,0);float[] min={Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY},max={Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};for(int i=0;i<idle.length;i++){int k=i%3;min[k]=Math.min(min[k],idle[i]);max[k]=Math.max(max[k],idle[i]);}
  double[] lo={-98.39253678228063,0.05752092635576833,-40.77340386360786},hi={98.39253678228063,116.38317670246694,27.30820518199046};for(int i=0;i<3;i++)if(Math.abs(min[i]-lo[i]/14)>1e-5||Math.abs(max[i]-hi[i]/14)>1e-5)throw new AssertionError("approved V2 bounds changed");
  if(HalaRules.stage(39)!=1||HalaRules.stage(40)!=2||HalaRules.stage(80)!=3||HalaRules.outgoing(3,.1F)!=0||HalaRules.cool(80,true)>=HalaRules.cool(80,false))throw new AssertionError("burn rules");
  System.out.println("Hala V2 geometry and burn rules verified");
 }
}
