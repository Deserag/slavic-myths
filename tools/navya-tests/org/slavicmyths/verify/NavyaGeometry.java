package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import org.slavicmyths.navya.NavyaTreePlan;
import org.slavicmyths.navya.NavyaTreePlan.Cell;

/** Independent topology/size gate against the installed FallingTree limits. */
public final class NavyaGeometry {
    public static void main(String[] args) throws Exception {
        int[] min={999,999,999},max={0,0,0};
        for(int size=0;size<3;size++) for(int seed=0;seed<500;seed++) {
            var plan=new NavyaTreePlan(size,seed);
            min[size]=Math.min(min[size],plan.logs.size());max[size]=Math.max(max[size],plan.logs.size());
            if(plan.logs.size()>100)throw new AssertionError("FallingTree maxSize=100; size="+size+" seed="+seed+" logs="+plan.logs.size());
            var seen=new HashSet<Cell>();var pending=new ArrayDeque<Cell>();pending.add(new Cell(0,0,0));
            while(!pending.isEmpty()) {
                Cell at=pending.removeFirst();if(!seen.add(at))continue;
                for(int[] dir:new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}}) {
                    Cell next=new Cell(at.x()+dir[0],at.y()+dir[1],at.z()+dir[2]);
                    if(plan.logs.containsKey(next)&&!seen.contains(next))pending.addLast(next);
                }
            }
            if(seen.size()!=plan.logs.size())throw new AssertionError("Disconnected wooden skeleton "+size+" "+seed);
            if(plan.foliage.isEmpty() || plan.foliage.size()+plan.logs.size()>500)throw new AssertionError("FallingTree scan/leaf bounds");
            for(Cell leaf:plan.foliage) {
                boolean adjacent=false;
                for(Cell log:plan.logs.keySet())if(Math.abs(log.x()-leaf.x())+Math.abs(log.y()-leaf.y())+Math.abs(log.z()-leaf.z())==1)adjacent=true;
                if(!adjacent)throw new AssertionError("Unsupported distant foliage");
            }
        }
        String result="Tree topology PASS: 1500 plans, all face-connected; logs small="+min[0]+".."+max[0]+", normal="+min[1]+".."+max[1]+", giant="+min[2]+".."+max[2]+"; maxSize100/maxScan500. Actual FallingTree gameplay NOT_RUN.\n";
        var out=Path.of("docs/implementation/navya-grove/tree-topology.txt");Files.createDirectories(out.getParent());Files.writeString(out,result);System.out.print(result);
    }
}
