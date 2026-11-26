package org.slavicmyths.desert;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.desert.oasis.*;
import static org.slavicmyths.desert.oasis.LostOasisPlan.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class LostOasisGameTests {
    @GameTest(template="empty")
    public static void lostOasisPlan(GameTestHelper h){
        var p=INSTANCE;int water=0,fire=0,lantern=0,solid=0;
        for(int x=0;x<W;x++)for(int z=0;z<D;z++){
            for(int y=0;y<H;y++){int m=p.at(x,y,z);if(m!=AIR)solid++;if(m==WATER||m==WATER2){water++;if(y==2)h.assertTrue(p.at(x,y-1,z)!=AIR,"water has a bottom");}if(m==LANTERN){lantern++;h.assertTrue(p.at(x,y-1,z)!=AIR,"lantern support");}if(m==FIRE&&radius(x,z)<19)fire++;}
        }
        h.assertTrue(solid>70000&&solid<90000,"approved V1 volume");
        h.assertTrue(water>1000&&lantern>80&&fire==12,"oasis water and lights");
        for(int x=CX-12;x<=CX+12;x++)for(int z=CZ-12;z<=CZ+12;z++){
            h.assertTrue(p.at(x,FLOOR,z)!=AIR,"arena floor");for(int y=FLOOR+1;y<=FLOOR+16;y++)h.assertTrue(p.at(x,y,z)==AIR,"open arena");
        }
        for(int z=CZ-57;z<=CZ;z++)for(int x=CX-4;x<=CX+4;x++){h.assertTrue(p.at(x,FLOOR,z)!=AIR,"main access floor");for(int y=FLOOR+1;y<=FLOOR+2;y++)h.assertTrue(p.at(x,y,z)==AIR,"main access clearance");}
        h.assertTrue(p.at(CX,LOJA_FLOOR,CZ+29)!=AIR,"loggia floor");
        for(int y=LOJA_FLOOR+1;y<=LOJA_FLOOR+8;y++)h.assertTrue(p.at(CX,y,CZ+29)==AIR,"boss balcony clearance");
        h.assertTrue(arena(CX,FLOOR+1,CZ)&&!arena(CX,FLOOR+1,CZ+31),"fight trigger");
        h.assertTrue(LostOasisStructure.TYPE.get()!=null&&LostOasisStructure.PIECE.get()!=null,"native registration");
        h.succeed();
    }
}
