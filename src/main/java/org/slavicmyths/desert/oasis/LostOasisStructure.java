package org.slavicmyths.desert.oasis;

import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.slavicmyths.swamp.SwampStructures;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.worldgen.StructureCoverageService;

public final class LostOasisStructure extends Structure {
    public static final MapCodec<LostOasisStructure> CODEC=simpleCodec(LostOasisStructure::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructureType<?>,StructureType<LostOasisStructure>> TYPE=SwampStructures.STRUCTURES.register("lost_oasis",()->()->CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("lost_oasis",()->(ctx,n)->new LostOasisPiece(n));
    public static void init(){}
    public LostOasisStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return TYPE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        int min=c.heightAccessor().getMinBuildHeight(),max=c.heightAccessor().getMaxBuildHeight();
        if(max-min<LostOasisPlan.H+5)return Optional.empty();
        int surface=c.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());
        if(!c.biomeSource().getNoiseBiome(x>>2,surface>>2,z>>2,c.randomState().sampler()).is(Biomes.DESERT))return Optional.empty();
        // No slope rejection: use the median terrain elevation and level the footprint.
        int[] heights=new int[25];int i=0;
        for(int dx=-48;dx<=48;dx+=24)for(int dz=-52;dz<=52;dz+=26)
            heights[i++]=c.chunkGenerator().getBaseHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());
        Arrays.sort(heights);
        BlockPos origin=new BlockPos(x-LostOasisPlan.CX,LostOasisBuild.anchor(heights[12],min,max),z-LostOasisPlan.CZ);
        LostOasisPiece piece=new LostOasisPiece(origin,min+1,max-1);
        if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
        if(!StructureCandidates.clear(c,c.chunkPos(),id->id==null?0:id.toString().equals("slavicmyths:desert_trial_dungeon")||id.toString().equals("slavicmyths:sun_temple")?10:(StructureCandidates.vanilla(id,"village","desert_pyramid","pillager_outpost")||id.getNamespace().equals("slavicmyths")&&!id.getPath().equals("lost_oasis"))?8:0))return Optional.empty();
        return Optional.of(new GenerationStub(new BlockPos(x,origin.getY(),z),b->b.addPiece(piece)));
    }
}
