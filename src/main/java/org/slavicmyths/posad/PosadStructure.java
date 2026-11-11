package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Native planner: noise columns only, no FULL chunks, tickets, block writes or synchronous waits. */
public final class PosadStructure extends Structure {
    public static final MapCodec<PosadStructure> CODEC = simpleCodec(PosadStructure::new);
    public PosadStructure(StructureSettings settings) { super(settings); }
    @Override public StructureType<?> type() { return PosadWorldgen.STRUCTURE.get(); }
    private record PlanKey(Object generator,Object random,Object registries,Object templates,long seed,long chunk,int minY,int height) {}
    private final java.util.LinkedHashMap<PlanKey,Optional<PosadTerrain.Plan>> plans=new java.util.LinkedHashMap<>(128,.75F,true);
    private long cacheHits,cacheMisses;
    public record PlannerStats(long hits,long misses,int retained) {}
    public PlannerStats plannerStats(){synchronized(plans){return new PlannerStats(cacheHits,cacheMisses,plans.size());}}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        var key=new PlanKey(c.chunkGenerator(),c.randomState(),c.registryAccess(),c.structureTemplateManager(),c.seed(),c.chunkPos().toLong(),c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getHeight());
        Optional<PosadTerrain.Plan> result;
        synchronized(plans){result=plans.get(key);if(result!=null)cacheHits++;else cacheMisses++;}
        // The noise/registry plan is deterministic. Compute outside the lock so
        // parallel worldgen workers never wait on an expensive terrain survey.
        if(result==null){
            result=plan(c);
            synchronized(plans){plans.put(key,result);while(plans.size()>128)plans.remove(plans.keySet().iterator().next());}
        }
        return result.map(plan -> new GenerationStub(new BlockPos(plan.anchorX()+56,plan.deckY(),plan.anchorZ()+56),
                builder -> builder.addPiece(new PosadPiece(plan))));
    }
    private Optional<PosadTerrain.Plan> plan(GenerationContext c) {
        int ax=c.chunkPos().getMiddleBlockX()-56, az=c.chunkPos().getMiddleBlockZ()-56;
        Map<Long,NoiseColumn> columns=new HashMap<>();
        boolean[] dense={false};
        Map<Long,PosadNoiseBatch.Window> windows=new HashMap<>();
        PosadTerrain.Columns terrain=(x,y,z)-> {
            long key=((long)x<<32)^(z&0xffffffffL);
            var column=columns.get(key);
            var sampled=windows.get(key);
            if(column==null&&sampled==null&&dense[0]&&y>=32-PosadTerrain.MAX_SUPPORT_DEPTH&&y<=96+17
                    &&c.chunkGenerator() instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator noise
                    &&PosadNoiseBatch.fillWindow(noise,c.randomState(),c.heightAccessor(),x,z,32-PosadTerrain.MAX_SUPPORT_DEPTH,96+17,windows))sampled=windows.get(key);
            net.minecraft.world.level.block.state.BlockState state;
            if(column==null&&sampled!=null&&sampled.contains(y))state=sampled.state(y);
            else {if(column==null){column=c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState());columns.put(key,column);}state=column.getBlock(y);}
            if(state.isAir())return PosadTerrain.Material.AIR;
            if(state.is(Blocks.LAVA))return PosadTerrain.Material.LAVA;
            if(NetherBridgeTerrain.natural(state))return PosadTerrain.Material.NATURAL;
            return PosadTerrain.Material.PROTECTED;
        };
        int first=Math.floorMod((int)(c.seed()^c.chunkPos().toLong()),4);
        // Elevated Nether plateaus were excluded by the former hard Y=64 ceiling.
        // Every added height still goes through the full safety, shore and collision checks.
        for(int y=Math.min(96,c.heightAccessor().getMaxBuildHeight()-18);y>=32;y--) {
            boolean valid=true;
            // Cheap rejection before full-footprint inspection. All final checks remain dense.
            for(int z=0;z<113&&valid;z+=16)for(int x=0;x<113;x+=16) {
                if(!biomes().contains(c.biomeSource().getNoiseBiome((ax+x)>>2,y>>2,(az+z)>>2,c.randomState().sampler()))) {valid=false;break;}
                for(int dy=PosadTerrain.MAX_TERRAIN_CUT+1;dy<18;dy++)if(terrain.at(ax+x,y+dy,az+z)!=PosadTerrain.Material.AIR){valid=false;break;}
                if(!valid)break;
            }
            if(!valid)continue;
            dense[0]=true;
            var decision=PosadTerrain.inspect(terrain,ax,y,az,first);
            dense[0]=false;
            if(!decision.accepted())continue;
            var piece=new PosadPiece(decision.plan());
            var proposed=java.util.List.<StructurePiece>of(piece);
            if(!StructureCoverageService.referencesFit(c.chunkPos(),proposed))return Optional.empty();
            if(!StructureCandidates.clearAccepted(c,c.chunkPos(),id->id!=null&&id.toString().equals("slavicmyths:nether_posad")?0:1,proposed))return Optional.empty();
            return Optional.of(decision.plan());
        }
        return Optional.empty();
    }
}
