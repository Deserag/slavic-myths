package org.slavicmyths.desert.winds;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.slavicmyths.swamp.SwampStructures;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

public final class FourWindsStructure extends Structure {
    public static final MapCodec<FourWindsStructure> CODEC=simpleCodec(FourWindsStructure::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructureType<?>,StructureType<FourWindsStructure>> TYPE=
        SwampStructures.STRUCTURES.register("four_winds_palace",()->()->CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructurePieceType,StructurePieceType> PIECE=
        SwampStructures.PIECES.register("four_winds_palace",()->(ctx,n)->new FourWindsPiece(n));
    public static void init(){}
    public FourWindsStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return TYPE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        try(var samples=StructureCoverageService.sampleTerrain(c)){
            int entrance=z+FourWindsPlan.ENTRANCE_Z;
            int ground=height(c,x,entrance,Heightmap.Types.OCEAN_FLOOR_WG);
            if(height(c,x,entrance,Heightmap.Types.WORLD_SURFACE_WG)>ground)return Optional.empty();
            if(!c.biomeSource().getNoiseBiome(x>>2,ground>>2,entrance>>2,c.randomState().sampler()).is(Biomes.DESERT))return Optional.empty();
            int center=height(c,x,z,Heightmap.Types.OCEAN_FLOOR_WG);
            if(!c.biomeSource().getNoiseBiome(x>>2,center>>2,z>>2,c.randomState().sampler()).is(Biomes.DESERT))return Optional.empty();
            var y=FourWindsBuild.anchor((wx,wz)->height(c,wx,wz,Heightmap.Types.WORLD_SURFACE_WG),
                x,z,c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight());
            if(y.isEmpty())return Optional.empty();
            var origin=new BlockPos(x,y.getAsInt(),z);
            var piece=new FourWindsPiece(origin);
            if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
            if(!StructureCandidates.clear(c,c.chunkPos(),id->{
                if(id==null||id.toString().equals("slavicmyths:four_winds_palace"))return 0;
                if(id.getNamespace().equals("slavicmyths"))
                    return switch(id.getPath()){case "lost_oasis","sun_temple","desert_trial_dungeon"->10;default->6;};
                return StructureCandidates.vanilla(id,"village","desert_pyramid","pillager_outpost")?6:0;
            }))return Optional.empty();
            return Optional.of(new GenerationStub(origin,b->b.addPiece(piece)));
        }
    }
    private static int height(GenerationContext c,int x,int z,Heightmap.Types type){
        return StructureCoverageService.baseHeight(c.chunkGenerator(),x,z,type,c.heightAccessor(),c.randomState());
    }
}
