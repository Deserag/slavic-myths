package org.slavicmyths.desert;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.desert.indrik.*;
import static org.slavicmyths.desert.indrik.IndrikLairPlan.*;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class IndrikLairGameTests {
    @GameTest(template="empty") public static void indrikLairPlan(GameTestHelper h){
        var cells=IndrikLairPlan.cells();
        h.assertTrue(cells.size()<=LIMIT,"bounded operations");
        h.assertTrue(cells.stream().allMatch(c->c.x()>=MIN_X&&c.x()<=MAX_X&&c.z()>=MIN_Z&&c.z()<=MAX_Z),"no outside edits");
        h.assertTrue(anchor((x,z)->64,(x,z)->64,0,0,-64,320).orElseThrow()==64,"flat terrain");
        h.assertTrue(anchor((x,z)->64,(x,z)->63,0,0,-64,320).isEmpty(),"water rejection");
        h.assertTrue(anchor((x,z)->z<-10?64:80,(x,z)->z<-10?64:80,0,0,-64,320).isEmpty(),"cliff rejection");
        var at=new java.util.HashMap<String,Kind>();
        for(var c:cells)at.put(c.x()+":"+c.y()+":"+c.z(),c.kind());
        for(int z=ENTRANCE_Z;z<=-6;z++)for(int x=-2;x<=2;x++){
            int y=-1-(z-ENTRANCE_Z);
            h.assertTrue(at.get(x+":"+y+":"+z)==Kind.STAIR,"continuous stairs");
            for(int dy=1;dy<=4;dy++)h.assertTrue(at.get(x+":"+(y+dy)+":"+z)==Kind.AIR,"clear staircase");
        }
        for(int x=-7;x<=7;x++)for(int z=-2;z<=11;z++)if(arena(x,z))
            for(int y=FLOOR+1;y<=-4;y++)h.assertTrue(at.get(x+":"+y+":"+z)==Kind.AIR,"clear arena center");
        h.assertTrue(IndrikLairStructure.TYPE.get()!=null&&IndrikLairStructure.PIECE.get()!=null,"registered");
        h.succeed();
    }
}
