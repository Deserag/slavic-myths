import org.slavicmyths.desert.HalaRules;
import org.slavicmyths.desert.HalaGeometry;
public class RulesCheck {
 public static void main(String[] args){
  var g=HalaGeometry.INSTANCE;if(g.count!=2781||g.groups.length<12)throw new AssertionError("geometry count");
  for(int a=0;a<12;a++)for(int i=0;i<=20;i++){var v=g.sample(a,i/20F);if(v.length!=2781*24)throw new AssertionError("mesh");for(float f:v)if(!Float.isFinite(f))throw new AssertionError("nonfinite");}
  if(HalaRules.stage(39)!=1||HalaRules.stage(40)!=2||HalaRules.stage(80)!=3||HalaRules.outgoing(3,.1F)!=0||HalaRules.cool(80,true)>=HalaRules.cool(80,false))throw new AssertionError("burn rules");
  System.out.println("Hala V2 geometry and burn rules verified");
 }
}
