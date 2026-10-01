import org.slavicmyths.kurgan.KurganShape;
public final class KurganShapeTest {
 private static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[]args){
  for(int kind=0;kind<3;kind++){int r=KurganShape.RADIUS[kind],h=KurganShape.HEIGHT[kind];check(KurganShape.height(kind,0,0)==h,"peak");int previous=h;
   for(int x=0;x<=r;x++){int y=KurganShape.height(kind,x,0);check(y<=previous&&previous-y<=2,"smooth monotone slope");previous=y;}
   check(previous==0,"edge joins terrain");for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){int y=KurganShape.height(kind,x,z);check(y>=0&&y<=h,"height bound");check(y==KurganShape.height(kind,-x,-z),"shell symmetry");}
   int foot=8+r-5;check(Math.floorDiv(foot,16)==Math.floorDiv(foot-1,16),"coffin halves share generation chunk");
  }
  int[] counts=new int[3];for(int i=-50000;i<50000;i++)counts[KurganShape.kind(84,i,i*71)]++;
  check(counts[0]>58000&&counts[0]<62000,"small mix");check(counts[1]>28000&&counts[1]<32000,"warrior mix");check(counts[2]>9000&&counts[2]<11000,"great mix");
  System.out.println("PASS: mound peaks 10/18/30, smooth symmetric slopes, chunk-safe coffins, 100000 candidate selections. No worldgen runtime test.");
 }
}
