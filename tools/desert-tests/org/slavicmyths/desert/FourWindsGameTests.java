package org.slavicmyths.desert;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.desert.winds.*;
import static org.slavicmyths.desert.winds.FourWindsPlan.*;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class FourWindsGameTests {
    @GameTest(template="empty") public static void fourWindsGeometry(GameTestHelper h){
        int count=0;
        for(int x=MIN_X;x<=MAX_X;x++)for(int z=MIN_Z;z<=MAX_Z;z++)for(int y=0;y<H;y++)if(at(x,y,z)!=AIR)count++;
        h.assertTrue(count==COUNT&&COUNT==13930,"approved geometry volume");
        for(int i=0;i<34;i++)for(int x=-2;x<=2;x++){
            int y=2+i,z=ENTRANCE_Z+i;
            h.assertTrue(at(x,y,z)==STAIR&&at(x,y-1,z)!=AIR&&at(x,y+1,z)==AIR&&at(x,y+2,z)==AIR,"walkable stairs");
        }
        for(int x=-23;x<=23;x++)for(int z=-23;z<=23;z++){
            if(x*x+z*z<=23*23)h.assertTrue(at(x,FLOOR,z)!=AIR&&at(x,FLOOR-1,z)!=AIR,"continuous floor");
            if(x*x+z*z<=12*12)for(int y=FLOOR+1;y<H;y++)h.assertTrue(at(x,y,z)==AIR,"clear arena");
        }
        h.assertTrue(FourWindsStructure.TYPE.get()!=null&&FourWindsStructure.PIECE.get()!=null,"native structure registry");
        BlockPos p=h.absolutePos(new BlockPos(2,2,2));BlockPos o=p.below(FLOOR);
        FourWindsBuild.render(h.getLevel(),o,new BoundingBox(p.getX(),p.getY(),p.getZ(),p.getX(),p.getY(),p.getZ()));
        h.assertTrue(h.getLevel().getBlockState(p).is(Blocks.SMOOTH_SANDSTONE),"real block placement");
        h.succeed();
    }
    @GameTest(template="empty") public static void fourWindsBossBounds(GameTestHelper h){
        var w=h.getLevel();var q=StormIdolContent.BOSS.get().create(w);var t=EntityType.ZOMBIE.create(w);
        h.assertTrue(q!=null&&t!=null,"entity registration");
        BlockPos o=h.absolutePos(new BlockPos(2,2,2)).below(FLOOR+1);
        q.moveTo(o.getX()+.5,o.getY()+FLOOR+1,o.getZ()+.5,0,0);
        t.moveTo(q.position().add(2,0,0));FourWindsRecords.bind(q,o);
        h.assertTrue(FourWindsRecords.allows(q,t),"target on deck");
        t.moveTo(q.position().add(0,-30,0));
        h.assertTrue(!FourWindsRecords.allows(q,t),"no target below arena");
        Vec3 clamped=FourWindsRecords.constrain(q,q.position().add(50,-20,0));
        h.assertTrue(Math.abs(clamped.x-q.getX()-15)<.01&&clamped.y==q.getY(),"flight remains above deck");
        q.discard();t.discard();h.succeed();
    }
}
