package org.slavicmyths.textile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Vanilla bed gameplay, authored static head/foot models instead of the vanilla bed renderer. */
public final class LinenBed extends BedBlock {
    private static final MapCodec<BedBlock> CODEC=simpleCodec(LinenBed::new);
    public LinenBed(){this(Properties.ofFullCopy(Blocks.RED_BED));}
    private LinenBed(Properties p){super(DyeColor.RED,p);}
    @Override public MapCodec<BedBlock> codec(){return CODEC;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return null;}
}
