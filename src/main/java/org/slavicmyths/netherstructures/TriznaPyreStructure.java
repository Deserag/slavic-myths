package org.slavicmyths.netherstructures;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.*;
public final class TriznaPyreStructure extends Structure {
    public static final MapCodec<TriznaPyreStructure> CODEC=simpleCodec(TriznaPyreStructure::new);
    public TriznaPyreStructure(StructureSettings s){super(s);}
    @Override public StructureType<?> type(){return TriznaPyreWorldgen.STRUCTURE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        if(!biomes().contains(c.chunkGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2,c.randomState().sampler())))return Optional.empty();
        if(!StructureCandidates.clear(c,c.chunkPos(),id->id==null||id.equals(TriznaPyreWorldgen.id("trizna_pyre"))?0:id.getNamespace().equals("minecraft")?4:3))return Optional.empty();
        var plan=TriznaPyreTerrain.find(c);if(plan==null)return Optional.empty();
        var piece=new TriznaPyrePiece(plan);
        if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
        return Optional.of(new GenerationStub(plan.center(),b->b.addPiece(piece)));
    }
}
