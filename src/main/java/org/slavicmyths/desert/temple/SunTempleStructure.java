package org.slavicmyths.desert.temple;

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

public final class SunTempleStructure extends Structure {
    public static final MapCodec<SunTempleStructure> CODEC=simpleCodec(SunTempleStructure::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructureType<?>,StructureType<SunTempleStructure>> TYPE=SwampStructures.STRUCTURES.register("sun_temple",()->()->CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("sun_temple",()->(ctx,n)->new SunTemplePiece(n));
    public static void init(){}
    public SunTempleStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return TYPE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        int min=c.heightAccessor().getMinBuildHeight(),max=c.heightAccessor().getMaxBuildHeight();
        if(max-min<SunTemplePlan.H+5)return Optional.empty();
        int surface=c.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());
        if(!c.biomeSource().getNoiseBiome(x>>2,surface>>2,z>>2,c.randomState().sampler()).is(Biomes.DESERT))return Optional.empty();
        // No slope rejection: use the median terrain elevation and level the footprint.
        int[] heights=new int[25];int i=0;
        for(int dx=-32;dx<=32;dx+=16)for(int dz=-40;dz<=40;dz+=20)
            heights[i++]=c.chunkGenerator().getBaseHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());
        Arrays.sort(heights);
        BlockPos origin=new BlockPos(x-SunTemplePlan.W/2,SunTempleBuild.anchor(heights[12],min,max),z-SunTemplePlan.D/2);
        SunTemplePiece piece=new SunTemplePiece(origin,min+1,max-1);
        if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))return Optional.empty();
        if(!StructureCandidates.clear(c,c.chunkPos(),id->id==null?0:id.toString().equals("slavicmyths:desert_trial_dungeon")?8:(StructureCandidates.vanilla(id,"village","desert_pyramid","pillager_outpost")||id.getNamespace().equals("slavicmyths")&&!id.getPath().equals("sun_temple"))?6:0))return Optional.empty();
        return Optional.of(new GenerationStub(new BlockPos(x,origin.getY(),z),b->b.addPiece(piece)));
    }
}
