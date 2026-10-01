import java.util.*;
import org.slavicmyths.wood.TreeShape;
public final class TreeShapeCheck {
 public static void main(String[] args){
  for(String species:new String[]{"linden","rowan","willow","pine"}){
   int minH=100,maxH=0,minW=100,maxW=0;Set<String>forms=new HashSet<>();Set<Integer>variants=new HashSet<>();int curtains=0;
   for(int seed=0;seed<256;seed++){
    TreeShape t=new TreeShape(species,new Random(seed*173L+81));variants.add(t.variant);
    Set<TreeShape.Cell> reached=new HashSet<>();ArrayDeque<TreeShape.Cell>q=new ArrayDeque<>();q.add(new TreeShape.Cell(0,0,0));
    while(!q.isEmpty()){TreeShape.Cell c=q.remove();if(!t.logs.containsKey(c)||!reached.add(c))continue;for(int[]d:TreeShape.DIRS)q.add(c.offset(d[0],d[1],d[2]));}
    if(!reached.equals(t.logs.keySet()))throw new AssertionError(species+" disconnected logs");
    int top=0,loX=0,hiX=0,loZ=0,hiZ=0;
    Set<TreeShape.Cell>all=new HashSet<>(t.logs.keySet());all.addAll(t.leaves.keySet());
    for(TreeShape.Cell c:all){top=Math.max(top,c.y+1);loX=Math.min(loX,c.x);hiX=Math.max(hiX,c.x);loZ=Math.min(loZ,c.z);hiZ=Math.max(hiZ,c.z);}
    for(Map.Entry<TreeShape.Cell,Integer>e:t.leaves.entrySet()){
     if(e.getValue()<1||e.getValue()>6||t.logs.containsKey(e.getKey()))throw new AssertionError("unsupported leaf");
     boolean support=false;for(int[]d:TreeShape.DIRS){TreeShape.Cell n=e.getKey().offset(d[0],d[1],d[2]);if(t.logs.containsKey(n)||t.leaves.getOrDefault(n,7)<e.getValue())support=true;}
     if(!support)throw new AssertionError("leaf has no path to wood");
    }
    for(TreeShape.Cell c:t.hanging){TreeShape.Cell above=c.offset(0,1,0);if(!t.leaves.containsKey(above)&&!t.hanging.contains(above))throw new AssertionError("unsupported hanging leaf");if(t.logs.containsKey(c)||t.leaves.containsKey(c))throw new AssertionError("overlap");}
    if(species.equals("pine"))for(TreeShape.Cell c:t.leaves.keySet())if(c.y<5)throw new AssertionError("closed pine trunk");
    int width=Math.max(hiX-loX,hiZ-loZ)+1;minH=Math.min(minH,top);maxH=Math.max(maxH,top);minW=Math.min(minW,width);maxW=Math.max(maxW,width);curtains+=t.hanging.size();forms.add(t.logs.keySet().toString()+t.leaves.size());
   }
   if(variants.size()!=3||forms.size()<20)throw new AssertionError("insufficient variants");
   if(species.equals("willow")&&curtains==0)throw new AssertionError("no curtains");
   System.out.println("PASS "+species+": 256 seeds, 3 variants, connected wood/supported leaves; height="+minH+".."+maxH+", width="+minW+".."+maxW+", hanging="+curtains);
  }
 }
}
