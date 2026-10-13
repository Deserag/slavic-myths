package org.slavicmyths.chud;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.StructureCoverageService;
/** Shared native candidate cells: accepted A takes priority, B can use a rejected A cell. */
public final class AbandonedStructure extends Structure {
    public static final MapCodec<AbandonedStructure> CODEC=simpleCodec(AbandonedStructure::new);
    public AbandonedStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return AbandonedStructures.TYPE.get();}
    public static Optional<AbandonedPlan> attached(GenerationContext c,ChudPlan city){
        var random=new Random(city.seed^0x425f534d414c4cL);if(random.nextDouble()>=AbandonedConfig.ATTACHED_CHANCE.get())return Optional.empty();
        int draw=random.nextInt(100),flavor=draw<55?0:draw<85?1:2;
        var p=new AbandonedPlan(city.seed,city.anchor,city.rotation,AbandonedPlan.Kind.ATTACHED_SMALL,city.entranceFloor,city.cityId(),flavor);
        return suitable(c,p)?Optional.of(p):Optional.empty();
    }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        if(!ChudRegionPlanner.raw()&&ChudRegionPlanner.placement(c)!=null)return ChudRegionPlanner.find(c,false);
        var city=c.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        if(city!=null&&StructureCoverageService.neighborPlan(c,city,c.chunkPos()).isValid())return Optional.empty();
        boolean great=new Random(c.seed()^c.chunkPos().toLong()^0x4142414e444f4eL).nextInt(100)>=65;
        var piece=ChudSimplePlacement.abandoned(c,false,great);
        if(piece==null&&great)piece=ChudSimplePlacement.abandoned(c,false,false);
        if(piece==null)return Optional.empty();
        var accepted=piece;
        return Optional.of(new GenerationStub(accepted.plan.anchor,b->b.addPiece(accepted)));
    }
    public static boolean suitable(GenerationContext c,AbandonedPlan p){return suitable(new ChudStructure.Terrain(c),p);}
    private static boolean suitable(ChudStructure.Terrain terrain,AbandonedPlan p){
        return p.anchor.getY()+ChudTerrainPolicy.bottom(p.kind==AbandonedPlan.Kind.ATTACHED_SMALL,p.kind==AbandonedPlan.Kind.INDEPENDENT_GREAT,p.shaftProfile)>=terrain.context.heightAccessor().getMinBuildHeight()+1&&p.nativeBounds().maxY()<terrain.context.heightAccessor().getMaxBuildHeight();
    }
}
