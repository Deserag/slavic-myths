package org.slavicmyths.dungeon;

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

/** Natural well and underground complex are one structure start. */
public final class DungeonStructure extends Structure {
    public static final MapCodec<DungeonStructure> CODEC=simpleCodec(DungeonStructure::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructureType<?>,StructureType<DungeonStructure>> TYPE=
        SwampStructures.STRUCTURES.register("desert_trial_dungeon",()->()->CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructurePieceType,StructurePieceType> PIECE=
        SwampStructures.PIECES.register("trial_dungeon",()->(ctx,tag)->new DungeonPiece(tag));
    public static void init(){}
    public DungeonStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return TYPE.get();}
    private int height(GenerationContext c,int x,int z){
        return c.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());
    }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        long seed=c.random().nextLong();
        for(int count=4;count>=3;count--){
            DungeonPlan p=DungeonPlan.create(seed,count);var entry=p.entrance();
            int wx=x+entry.x(),wz=z+entry.z(),surface=height(c,wx,wz);
            if(!c.biomeSource().getNoiseBiome(wx>>2,surface>>2,wz>>2,c.randomState().sampler()).is(Biomes.DESERT))return Optional.empty();
            int water=c.chunkGenerator().getBaseHeight(wx,wz,Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState());
            if(water!=surface)return Optional.empty();
            int low=surface;
            for(var b:p.boxes)if(b.floor()==0){
                for(int dx=b.x0();dx<=b.x1();dx+=8)for(int dz=b.z0();dz<=b.z1();dz+=8)low=Math.min(low,height(c,x+dx,z+dz));
            }
            int y;try{y=p.anchorY(low,c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight());}catch(IllegalArgumentException invalid){continue;}
            if(y>=low||surface+4>=c.heightAccessor().getMaxBuildHeight())continue;
            BlockPos origin=new BlockPos(x,y,z),well=new BlockPos(wx,surface,wz);
            DungeonPiece piece=new DungeonPiece(p,origin,surface-y);
            if(!StructureCoverageService.referencesFit(c.chunkPos(),List.of(piece)))continue;
            if(!StructureCandidates.clear(c,c.chunkPos(),id->id!=null&&(StructureCandidates.vanilla(id,"village","desert_pyramid","pillager_outpost")||id.getNamespace().equals("slavicmyths")&&!id.getPath().equals("desert_trial_dungeon"))?8:0))return Optional.empty();
            return Optional.of(new GenerationStub(well,b->b.addPiece(piece)));
        }
        return Optional.empty();
    }
}
