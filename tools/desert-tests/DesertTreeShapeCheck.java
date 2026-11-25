import java.util.*;
import org.slavicmyths.wood.TreeShape;
public final class DesertTreeShapeCheck {
    public static void main(String[] args) {
        for(String species:List.of("date_palm","tamarisk")) for(int seed=0;seed<500;seed++){
            var s=new TreeShape(species,new Random(seed));
            if(s.logs.isEmpty()||s.leaves.size()<20)throw new AssertionError("empty tree "+species+" "+seed);
            for(var c:s.logs.keySet())if(s.leaves.containsKey(c)||s.hanging.contains(c))throw new AssertionError("overlap");
            for(var c:s.leaves.entrySet())if(c.getValue()<1||c.getValue()>6)throw new AssertionError("leaf distance");
            for(var c:s.hanging){
                if(s.leaves.containsKey(c)||c.y<1)throw new AssertionError("hanging overlap");
                var above=c.offset(0,1,0);
                if(!s.leaves.containsKey(above)&&!s.hanging.contains(above))throw new AssertionError("unsupported frond");
            }
            Set<TreeShape.Cell> seen=new HashSet<>();ArrayDeque<TreeShape.Cell> q=new ArrayDeque<>();
            var root=new TreeShape.Cell(0,0,0);seen.add(root);q.add(root);
            while(!q.isEmpty()){
                var c=q.remove();for(var dir:TreeShape.DIRS){
                    var n=c.offset(dir[0],dir[1],dir[2]);
                    if(s.logs.containsKey(n)&&seen.add(n))q.add(n);
                }
            }
            if(seen.size()!=s.logs.size())throw new AssertionError("disconnected trunk");
        }
        System.out.println("1000 desert tree geometry cases passed");
    }
}
