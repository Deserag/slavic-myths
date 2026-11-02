package org.slavicmyths.hellqa;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.burningislands.*;

/** Bounded native biome selection and physical chunks; diagnostic, not survival exploration. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class NetherBiomeQaTests {
    @GameTest(template="empty", timeoutTicks=1200)
    public static void fourBiomesPhysicalChunks(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var source=w.getChunkSource().getGenerator().getBiomeSource();
        var sampler=w.getChunkSource().randomState().sampler();
        Map<String,BlockPos> found=new TreeMap<>();
        for(int x=-4096;x<=4096;x+=256) for(int z=-4096;z<=4096;z+=256)
            for(int y:new int[]{36,64,100}) {
                String id=source.getNoiseBiome(x>>2,y>>2,z>>2,sampler).unwrapKey().orElseThrow().location().toString();
                found.putIfAbsent(id,new BlockPos(x,y,z));
            }
        for(String id:List.of("minecraft:nether_wastes","minecraft:soul_sand_valley","minecraft:crimson_forest","minecraft:warped_forest","minecraft:basalt_deltas",
                "slavicmyths:navya_grove","slavicmyths:black_cinder","slavicmyths:lost_halls")) {
            HellQaTests.check(found.containsKey(id),"Missing native biome "+id+" seed="+w.getSeed());
            var pos=found.get(id);
            var chunk=w.getChunkAt(pos);
            HellQaTests.check(chunk.getNoiseBiome(pos.getX()>>2,pos.getY()>>2,pos.getZ()>>2).unwrapKey().orElseThrow().location().toString().equals(id),"Stored quart biome mismatch "+id+" at "+pos);
            // Level.getBiome uses BiomeManager's Voronoi zoom, not the quart at floor(X/4).
            // Verify an actual block position as well, without assuming the cell boundary maps to itself.
            BlockPos physical=null;
            search:for(int dx=-4;dx<=4;dx+=2) for(int dy=-4;dy<=4;dy+=2) for(int dz=-4;dz<=4;dz+=2) {
                var q=pos.offset(dx,dy,dz);
                if(w.getBiome(q).unwrapKey().orElseThrow().location().toString().equals(id)){physical=q;break search;}
            }
            HellQaTests.check(physical!=null,"No matching physical biome near stored quart "+id+" at "+pos);
            System.out.println("HELLQA_BIOME seed="+w.getSeed()+" id="+id+" quartSample="+pos+" physicalXYZ="+physical+" actualChunk="+chunk.getPos());
        }
        var ocean=IslandsWorldgen.context(w);
        OceanTerrain.Plan chosen=null;
        outer:for(int ring=0;ring<=12;ring++) for(int cx=-ring;cx<=ring;cx++) for(int cz=-ring;cz<=ring;cz++) {
            if(Math.max(Math.abs(cx),Math.abs(cz))!=ring)continue;
            var plan=ocean.plan(cx*512+256,cz*512+256);
            if(ocean.region(plan)){chosen=plan;break outer;}
        }
        HellQaTests.check(chosen!=null,"No ocean region in bounded search seed="+w.getSeed());
        var home=chosen.home();
        w.getChunk(home);
        HellQaTests.check(w.getBiome(home).is(IslandsContent.BIOME),"Physical islands biome mismatch "+home);
        var floor=w.getBlockState(home.below());
        HellQaTests.check(floor.is(Blocks.BLACKSTONE)||floor.is(Blocks.BASALT)||floor.is(Blocks.OBSIDIAN),"Missing physical arena floor "+home+" "+floor);
        HellQaTests.check(w.isEmptyBlock(home)&&w.isEmptyBlock(home.above()),"Blocked physical arena "+home);
        System.out.println("HELLQA_BIOME seed="+w.getSeed()+" id=slavicmyths:burning_islands XYZ="+home+" physical arena floor="+floor+"; bounded diagnostic chunk requests, not survival/natural spawn proof");
        t.succeed();
    }
}
