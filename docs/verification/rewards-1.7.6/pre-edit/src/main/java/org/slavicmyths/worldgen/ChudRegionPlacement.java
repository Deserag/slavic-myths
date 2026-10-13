package org.slavicmyths.worldgen;

import java.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.*;

/** Several possible owning chunks, one seed-planned root per cell. Original slot stays first. */
public final class ChudRegionPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<ChudRegionPlacement> CODEC=RecordCodecBuilder.mapCodec(i->placementCodec(i)
        .and(i.group(Codec.intRange(256,256).fieldOf("spacing").forGetter(ChudRegionPlacement::spacing),
            Codec.intRange(160,160).fieldOf("separation").forGetter(ChudRegionPlacement::separation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type",RandomSpreadType.LINEAR).forGetter(ChudRegionPlacement::spreadType)))
        .apply(i,ChudRegionPlacement::new));
    public ChudRegionPlacement(Vec3i offset,FrequencyReductionMethod reduction,float frequency,int salt,
                               Optional<ExclusionZone> exclusion,int spacing,int separation,RandomSpreadType spread) {
        super(offset,reduction,frequency,salt,exclusion,spacing,separation,spread);
    }
    public List<ChunkPos> candidates(long seed,int chunkX,int chunkZ) {
        var original=super.getPotentialStructureChunk(seed,chunkX,chunkZ);
        return org.slavicmyths.chud.ChudSitePolicy.candidates(chunkX,chunkZ,original.x,original.z).stream()
            .map(p->new ChunkPos(p.x(),p.z())).toList();
    }
    @Override protected boolean isPlacementChunk(ChunkGeneratorStructureState state,int x,int z) {
        return candidates(state.getLevelSeed(),x,z).contains(new ChunkPos(x,z));
    }
    @Override public StructurePlacementType<?> type() { return ChudRegionPlacements.TYPE.get(); }
}
