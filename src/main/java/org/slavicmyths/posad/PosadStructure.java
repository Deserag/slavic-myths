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
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        int ax=c.chunkPos().getMiddleBlockX()-56, az=c.chunkPos().getMiddleBlockZ()-56;
        Map<Long,NoiseColumn> columns=new HashMap<>();
        PosadTerrain.Columns terrain=(x,y,z)-> {
            long key=((long)x<<32)^(z&0xffffffffL);
            var column=columns.computeIfAbsent(key,k->c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState()));
            var state=column.getBlock(y);
            if(state.isAir())return PosadTerrain.Material.AIR;
            if(state.is(Blocks.LAVA))return PosadTerrain.Material.LAVA;
            if(NetherBridgeTerrain.natural(state))return PosadTerrain.Material.NATURAL;
            return PosadTerrain.Material.PROTECTED;
        };
        int first=Math.floorMod((int)(c.seed()^c.chunkPos().toLong()),4);
        for(int y=64;y>=32;y-=4) {
            boolean valid=true;
            // Cheap rejection before full-footprint inspection. All final checks remain dense.
            for(int z=0;z<113&&valid;z+=16)for(int x=0;x<113;x+=16) {
                if(!biomes().contains(c.biomeSource().getNoiseBiome((ax+x)>>2,y>>2,(az+z)>>2,c.randomState().sampler()))) {valid=false;break;}
                for(int dy=1;dy<18;dy++)if(terrain.at(ax+x,y+dy,az+z)!=PosadTerrain.Material.AIR){valid=false;break;}
                if(!valid)break;
            }
            if(!valid)continue;
            var decision=PosadTerrain.inspect(terrain,ax,y,az,first);
            if(!decision.accepted())continue;
            var piece=new PosadPiece(decision.plan());
            var proposed=java.util.List.<StructurePiece>of(piece);
            if(!StructureCoverageService.referencesFit(c.chunkPos(),proposed))return Optional.empty();
            if(!StructureCandidates.clearAccepted(c,c.chunkPos(),id->id!=null&&id.toString().equals("slavicmyths:nether_posad")?0:1,proposed))return Optional.empty();
            return Optional.of(new GenerationStub(new BlockPos(ax+56,y,az+56),builder->builder.addPiece(piece)));
        }
        return Optional.empty();
    }
}
