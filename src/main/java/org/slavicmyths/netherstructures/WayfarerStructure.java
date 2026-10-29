package org.slavicmyths.netherstructures;

import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Native structure: no server ticks, commands, or manual placement involved. */
public final class WayfarerStructure extends Structure {
    public static final MapCodec<WayfarerStructure> CODEC=simpleCodec(WayfarerStructure::new);
    public WayfarerStructure(StructureSettings settings) {super(settings);}
    @Override public StructureType<?> type() {return WayfarerWorldgen.STRUCTURE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        var biome=c.chunkGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2,c.randomState().sampler());
        if(!biomes().contains(biome))return Optional.empty();
        if(!StructureCandidates.clear(c,c.chunkPos(),id-> {
            if(id==null||id.equals(WayfarerWorldgen.id("wayfarer_camp")))return 0;
            return id.getNamespace().equals("minecraft")?4:3;
        }))return Optional.empty();
        var plan=WayfarerTerrain.find(c);
        if(plan==null)return Optional.empty();
        var piece=new WayfarerPiece(plan);
        if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
        return Optional.of(new GenerationStub(plan.center(),b->b.addPiece(piece)));
    }
}
