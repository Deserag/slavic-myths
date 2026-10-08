package org.slavicmyths.village;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Compact workstation only; existing flour recipes remain the production mechanic. */
public final class MillstoneBlock extends Block {
    public static final MapCodec<MillstoneBlock> CODEC=simpleCodec(p->new MillstoneBlock());
    private static final VoxelShape SHAPE=Shapes.or(box(1,0,1,15,7,15),box(2,7,2,14,12,14));
    public MillstoneBlock(){super(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext ctx){return SHAPE;}
}
