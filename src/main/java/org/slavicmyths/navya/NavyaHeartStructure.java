package org.slavicmyths.navya;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;
public final class NavyaHeartStructure extends Structure {
    public static final MapCodec<NavyaHeartStructure> CODEC=simpleCodec(NavyaHeartStructure::new);
    public NavyaHeartStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return NavyaWorldgen.HEART.get();}
    /** NavyaHeartPiece's lowest bound is centerY-8; distant lava below it is not an obstruction. */
    public static boolean checksBasalFluid(int centerY){return centerY-8<=31;}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
        int x=context.chunkPos().getMiddleBlockX(),z=context.chunkPos().getMiddleBlockZ();
        var profile=NavyaProfile.at(context.seed(),x,z);int y=context.random().nextBoolean()?profile.upper():profile.middle();
        if(checksBasalFluid(y)&&!context.chunkGenerator().getBaseColumn(x,z,context.heightAccessor(),context.randomState()).getBlock(31).getFluidState().isEmpty())return Optional.empty();
        if(y+10>=context.heightAccessor().getMaxBuildHeight())return Optional.empty();
        var pos=new BlockPos(x,y,z);
        var piece=new NavyaHeartPiece(pos,context.seed());
        // Check actual accepted neighboring structure footprints before placement:
        // chunk-by-chunk postProcess must not demand that nine neighbors are loaded.
        if(!org.slavicmyths.worldgen.StructureCandidates.clearAccepted(context,context.chunkPos(),
                other->other!=null&&other.getNamespace().equals("minecraft")?8:0,java.util.List.of(piece)))return Optional.empty();
        return Optional.of(new GenerationStub(pos,builder->builder.addPiece(piece)));
    }
}
