package org.slavicmyths.posad;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Native new-chunk planner: noise only; never obtains FULL chunks or writes blocks. */
public final class PosadStructure extends Structure {
    public static final MapCodec<PosadStructure> CODEC = simpleCodec(PosadStructure::new);
    public PosadStructure(StructureSettings settings) { super(settings); }
    @Override public StructureType<?> type() { return PosadWorldgen.STRUCTURE.get(); }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        if (!StructureCandidates.clear(c,c.chunkPos(),id -> id!=null && id.toString().equals("slavicmyths:nether_posad")?0:12))
            return Optional.empty();
        int ax=c.chunkPos().getMiddleBlockX()-56, az=c.chunkPos().getMiddleBlockZ()-56;
        // Conservative land-only stage. Reject rather than leave unsupported lava platforms.
        for(int y=64;y>=32;y-=4) {
            boolean valid=true;
            for(int z=0;z<113 && valid;z+=8) for(int x=0;x<113;x+=8) {
                var column=c.chunkGenerator().getBaseColumn(ax+x,az+z,c.heightAccessor(),c.randomState());
                if(!biomes().contains(c.biomeSource().getNoiseBiome((ax+x)>>2,y>>2,(az+z)>>2,c.randomState().sampler()))) {valid=false;break;}
                if(!NetherBridgeTerrain.natural(column.getBlock(y-3))) {valid=false;break;}
                for(int dy=1;dy<18;dy++) if(!column.getBlock(y+dy).isAir()) {valid=false;break;}
                if(!valid)break;
            }
            if(!valid)continue;
            var piece=new PosadPiece(new BlockPos(ax,y,az),c.random().nextInt(4));
            if(!StructureCoverageService.referencesFit(c.chunkPos(),java.util.List.of(piece))) return Optional.empty();
            return Optional.of(new GenerationStub(new BlockPos(ax+56,y,az+56),builder->builder.addPiece(piece)));
        }
        return Optional.empty();
    }
}
