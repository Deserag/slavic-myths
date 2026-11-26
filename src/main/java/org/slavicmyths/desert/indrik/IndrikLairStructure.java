package org.slavicmyths.desert.indrik;

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

public final class IndrikLairStructure extends Structure {
    public static final MapCodec<IndrikLairStructure> CODEC=simpleCodec(IndrikLairStructure::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructureType<?>,StructureType<IndrikLairStructure>> TYPE=
        SwampStructures.STRUCTURES.register("indrik_lair",()->()->CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructurePieceType,StructurePieceType> PIECE=
        SwampStructures.PIECES.register("indrik_lair",()->(ctx,n)->new IndrikLairPiece(n));
    public static void init(){}
    public IndrikLairStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return TYPE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        try(var samples=StructureCoverageService.sampleTerrain(c)){
            var y=IndrikLairPlan.anchor((wx,wz)->height(c,wx,wz,Heightmap.Types.WORLD_SURFACE_WG),
                (wx,wz)->height(c,wx,wz,Heightmap.Types.OCEAN_FLOOR_WG),x,z,
                c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight());
            if(y.isEmpty())return Optional.empty();
            for(int dx=-12;dx<=12;dx+=12)for(int dz=-12;dz<=12;dz+=12)
                if(!c.biomeSource().getNoiseBiome((x+dx)>>2,y.getAsInt()>>2,(z+dz)>>2,c.randomState().sampler()).is(Biomes.DESERT))return Optional.empty();
            BlockPos origin=new BlockPos(x,y.getAsInt(),z);
            var piece=new IndrikLairPiece(origin);
            if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
            if(!StructureCandidates.clear(c,c.chunkPos(),id->{
                if(id==null||id.toString().equals("slavicmyths:indrik_lair"))return 0;
                if(id.getNamespace().equals("slavicmyths"))return switch(id.getPath()){
                    case "four_winds_palace","lost_oasis","sun_temple","desert_trial_dungeon"->10;default->6;};
                return StructureCandidates.vanilla(id,"village","desert_pyramid","pillager_outpost")?6:0;
            }))return Optional.empty();
            return Optional.of(new GenerationStub(origin,b->b.addPiece(piece)));
        }
    }
    private static int height(GenerationContext c,int x,int z,Heightmap.Types type){
        return StructureCoverageService.baseHeight(c.chunkGenerator(),x,z,type,c.heightAccessor(),c.randomState());
    }
}
