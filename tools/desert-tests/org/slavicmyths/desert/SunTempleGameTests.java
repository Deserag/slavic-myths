package org.slavicmyths.desert;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.desert.temple.SunTemplePlan;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class SunTempleGameTests {
    @GameTest(template="empty")
    public static void sunTemplePlanAndArena(GameTestHelper h){
        var p=SunTemplePlan.INSTANCE;
        h.assertTrue(SunTemplePlan.W==81&&SunTemplePlan.D==91&&SunTemplePlan.H==53,"compact envelope");
        int cx=SunTemplePlan.CX,cz=SunTemplePlan.CZ,f=SunTemplePlan.FLOOR;
        for(int x=cx-10;x<=cx+10;x++)for(int z=cz-10;z<=cz+10;z++){
            h.assertTrue(p.at(x,f,z)!=SunTemplePlan.AIR,"arena floor missing");
            for(int y=f+1;y<=f+20;y++)h.assertTrue(p.at(x,y,z)==SunTemplePlan.AIR,"arena vertical clearance");
        }
        for(int z=0;z<=40;z++){
            int y=z<=3?z:z<30?3:z<=35?4+z-30:9;
            for(int x=cx-5;x<=cx+5;x++){
                h.assertTrue(p.at(x,y,z)!=SunTemplePlan.AIR,"continuous route floor");
                if(z<=3||z>=30&&z<=35)h.assertTrue(p.at(x,y,z)==SunTemplePlan.STAIR,"main staircase missing");
                for(int dy=1;dy<=3;dy++)h.assertTrue(p.at(x,y+dy,z)==SunTemplePlan.AIR,"main approach obstructed");
            }
        }
        for(int z=13;z<=40;z++)for(int y=10;y<SunTemplePlan.H;y++)
            h.assertTrue(p.at(cx,y,z)==SunTemplePlan.AIR,"courtyard must be open to sky");
        int water=0,solid=0;
        for(int x=0;x<SunTemplePlan.W;x++)for(int z=0;z<SunTemplePlan.D;z++){
            if(p.at(x,f,z)==SunTemplePlan.WATER){
                water++;h.assertTrue(p.at(x,f-1,z)==SunTemplePlan.SMOOTH,"basin liner missing");
            }
            for(int y=0;y<SunTemplePlan.H;y++)if(p.at(x,y,z)!=SunTemplePlan.AIR)solid++;
        }
        h.assertTrue(water==36,"four 3x3 cooling basins");
        h.assertTrue(solid>30000&&solid<120000,"compact geometry volume");
        h.assertTrue(SunTemplePlan.arena(cx,f+1,cz)&&!SunTemplePlan.arena(cx,f+1,10),"arena bounds");
        for(int x=18;x<=62;x++)for(int z=79;z<=81;z++){
            h.assertTrue(p.at(x,39,z)!=SunTemplePlan.AIR,"bridge floor");
            for(int y=40;y<=42;y++)h.assertTrue(p.at(x,y,z)==SunTemplePlan.AIR,"bridge corridor");
        }
        h.succeed();
    }
}
