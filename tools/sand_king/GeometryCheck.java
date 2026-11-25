import org.slavicmyths.desert.SandKingGeometry;
public class GeometryCheck {
 public static void main(String[] args){
  var g=SandKingGeometry.INSTANCE;float[] v=g.sample(0,0,false);float[] lo={99,99,99},hi={-99,-99,-99};
  for(int i=0;i<v.length;i++){lo[i%3]=Math.min(lo[i%3],v[i]);hi[i%3]=Math.max(hi[i%3],v[i]);}
  for(int i=0;i<3;i++)if(Math.abs(hi[i]-lo[i]-new int[]{9,7,12}[i])>.001)throw new AssertionError("Bounds");
  for(int a=0;a<7;a++)for(int t=0;t<=100;t++)for(float x:g.sample(a,t/100f,false))if(!Float.isFinite(x))throw new AssertionError("Animation");
  float[] scratch=new float[g.count*24];double[] tip=new double[3];
  for(int a=0;a<7;a++)for(int t=0;t<=100;t++)for(boolean mirror:new boolean[]{false,true}){
   float progress=t/100F;float[] original=g.sample(a,progress,mirror);g.sampleInto(a,progress,mirror,scratch);
   if(!java.util.Arrays.equals(original,scratch))throw new AssertionError("reused pose changed");
   g.tipInto(a,progress,mirror,tip);if(!java.util.Arrays.equals(g.tip(original),tip))throw new AssertionError("stinger anchor changed");
  }
  if(g.tip(g.sample(2,.5f,false))[1]>.12)throw new AssertionError("Stinger cannot reach ground");
  System.out.println(g.count+" cuboids / "+g.groups.length+" hit groups / 707 sampled poses / dimensions 9x12x7: PASS");
 }
}
