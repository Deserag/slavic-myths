package org.slavicmyths.desert;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.slavicmyths.desert.temple.SunTemplePlan;

@net.neoforged.neoforge.gametest.GameTestHolder("slavicmyths_accessories278")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class SunTempleGameTests {
    @GameTest(template="empty")
    public static void sunTemplePlanAndArena(GameTestHelper h){
        var p=SunTemplePlan.INSTANCE;
        h.assertTrue(p.at(55,29,96)!=SunTemplePlan.AIR,"arena floor missing");
        for(int y=30;y<50;y++)h.assertTrue(p.at(55,y,96)==SunTemplePlan.AIR,"arena vertical clearance");
        for(int z=29;z<=44;z++){
            int y=8+z-29;
            h.assertTrue(p.at(55,y,z)==SunTemplePlan.STAIR,"main staircase missing");
            h.assertTrue(p.at(55,y+1,z)==SunTemplePlan.AIR,"main staircase blocked");
        }
        int water=0,solid=0;
        for(int x=0;x<SunTemplePlan.W;x++)for(int z=0;z<SunTemplePlan.D;z++){
            if(p.at(x,29,z)==SunTemplePlan.WATER)water++;
            for(int y=0;y<SunTemplePlan.H;y++)if(p.at(x,y,z)!=SunTemplePlan.AIR)solid++;
        }
        h.assertTrue(water==100,"four 5x5 cooling basins");
        h.assertTrue(solid>150000,"temple geometry incomplete");
        h.assertTrue(SunTemplePlan.arena(55,30,96)&&!SunTemplePlan.arena(55,30,10),"arena bounds");
        h.succeed();
    }
}
