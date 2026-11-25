import org.slavicmyths.desert.AntGeometry;
import static org.slavicmyths.desert.AntRules.*;
public class AntGeometryCheck {
 public static void main(String[] args){
  int[] counts={546,142,190};
  for(int k=0;k<3;k++)for(int a=0;a<=LAND;a++)for(int f=0;f<=16;f++){
   var b=AntGeometry.pose(k,a,f/16.0);
   if(b.size()!=counts[k])throw new AssertionError("cuboids "+k+" "+b.size()+" expected "+counts[k]);
   for(var x:b)for(float n:x.vertices())if(!Float.isFinite(n))throw new AssertionError("nonfinite");
  }
  if(phase(.65F)!=2||phase(.30F)!=3||cap(false,3)+cap(true,3)!=55)throw new AssertionError("phase");
  var start=AntGeometry.pose(QUEEN,DEATH,0);var end=AntGeometry.pose(QUEEN,DEATH,1);
  double[] top={0,0},width={0,0};
  for(int i=0;i<2;i++)for(var box:i==0?start:end){float[] v=box.vertices();for(int j=0;j<v.length;j+=3){if(box.group()==2)top[i]=Math.max(top[i],v[j+1]);if(box.group()>=4)width[i]=Math.max(width[i],Math.abs(v[j]));}}
  if(top[1]>=top[0]-.30||width[1]<=width[0]+.5)throw new AssertionError("queen death must lower abdomen and spread feet");
  System.out.println("Ant geometry and rules passed");
 }
}
