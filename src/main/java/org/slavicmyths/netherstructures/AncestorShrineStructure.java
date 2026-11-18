package org.slavicmyths.netherstructures;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

/** 2.1.5 natural Nether one-chunk house. /locate is never a build operation. */
public final class AncestorShrineStructure extends Structure {
    public static final MapCodec<AncestorShrineStructure> CODEC=simpleCodec(AncestorShrineStructure::new);
    public AncestorShrineStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return AncestorShrineWorldgen.STRUCTURE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        var biome=c.chunkGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2,c.randomState().sampler());
        if(!biomes().contains(biome))return Optional.empty();
        if(!StructureCandidates.clear(c,c.chunkPos(),id->{
            if(id==null||id.equals(AncestorShrineWorldgen.id("ancestor_shrine")))return 0;
            return id.getNamespace().equals("minecraft")?4:3;
        }))return Optional.empty();
        var plan=AncestorShrineTerrain.find(c);
        if(plan==null)return Optional.empty();
        var piece=new AncestorShrinePiece(plan);
        if(!StructureCoverageService.referencesFit(c.chunkPos(),java.util.List.of(piece)))return Optional.empty();
        return Optional.of(new GenerationStub(plan.center(),builder->builder.addPiece(piece)));
    }
}
