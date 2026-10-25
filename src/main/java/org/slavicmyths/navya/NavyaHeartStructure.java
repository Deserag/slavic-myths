package org.slavicmyths.navya;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;
public final class NavyaHeartStructure extends Structure {
    public static final MapCodec<NavyaHeartStructure> CODEC=simpleCodec(NavyaHeartStructure::new);
    public NavyaHeartStructure(StructureSettings settings){super(settings);}
    @Override public StructureType<?> type(){return NavyaWorldgen.HEART.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
        int x=context.chunkPos().getMiddleBlockX(),z=context.chunkPos().getMiddleBlockZ();
        var nativeColumn=context.chunkGenerator().getBaseColumn(x,z,context.heightAccessor(),context.randomState());
        if(!nativeColumn.getBlock(31).getFluidState().isEmpty())return Optional.empty();
        var profile=NavyaProfile.at(context.seed(),x,z);int y=context.random().nextBoolean()?profile.upper():profile.middle();
        if(y+10>=context.heightAccessor().getMaxBuildHeight())return Optional.empty();
        var pos=new BlockPos(x,y,z);
        return Optional.of(new GenerationStub(pos,builder->builder.addPiece(new NavyaHeartPiece(pos,context.seed()))));
    }
}
