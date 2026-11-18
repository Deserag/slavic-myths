package org.slavicmyths.chud;

import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.*;

/** Bounded terrain sampling, conservative competing-candidate exclusion, standard structure lifecycle. */
public final class ChudStructure extends Structure {
    public static final MapCodec<ChudStructure> CODEC=simpleCodec(ChudStructure::new);
    public ChudStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return ChudStructures.TYPE.get();}
    static final class Terrain {
        final GenerationContext context;final Map<Long,net.minecraft.world.level.NoiseColumn> columns=new HashMap<>();
        Terrain(GenerationContext c){context=c;}
        net.minecraft.world.level.NoiseColumn column(int x,int z){ChudSearchBudget.check();long key=BlockPos.asLong(x,0,z);var saved=columns.get(key);if(saved!=null)return saved;var result=ChudRegionPlanner.column(context,x,z);if(columns.size()<8192)columns.put(key,result);return result;}
        int surface(int x,int z){return ChudRegionPlanner.surface(context,x,z);}
    }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        if(!ChudRegionPlanner.raw()&&ChudRegionPlanner.placement(c)!=null)return ChudRegionPlanner.find(c,true);
        boolean attached=new Random(c.seed()^c.chunkPos().toLong()^0x425f534d414c4cL).nextDouble()<AbandonedConfig.ATTACHED_CHANCE.get();
        var piece=ChudSimplePlacement.city(c,false,attached);
        if(piece==null&&attached)piece=ChudSimplePlacement.city(c,false,false);
        if(piece==null)return Optional.empty();
        var accepted=piece;
        return Optional.of(new GenerationStub(accepted.plan.anchor,b->{b.addPiece(accepted);if(accepted.access!=null)b.addPiece(new ChudAccessPiece(accepted.access));}));
    }
    public static boolean suitable(GenerationContext c,ChudPlan plan){
        return suitable(new Terrain(c),plan);
    }
    private static boolean suitable(Terrain terrain,ChudPlan plan){
        return plan.anchor.getY()-44>=terrain.context.heightAccessor().getMinBuildHeight()+1&&plan.anchor.getY()+73<terrain.context.heightAccessor().getMaxBuildHeight();
    }
}
