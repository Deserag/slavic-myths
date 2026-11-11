package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Coal plum sapling grows only on crimson nylium with the complete tree volume free. */
public final class CoalPlumSapling extends BushBlock implements BonemealableBlock {
    private static final MapCodec<CoalPlumSapling> CODEC = simpleCodec(CoalPlumSapling::new);

    public CoalPlumSapling(Properties properties) { super(properties); }
    @Override public MapCodec<CoalPlumSapling> codec() { return CODEC; }
    @Override protected boolean mayPlaceOn(BlockState below, BlockGetter level, BlockPos pos) { return below.is(Blocks.CRIMSON_NYLIUM); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).is(Blocks.CRIMSON_NYLIUM); }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this) && random.nextInt(18) == 0) CoalPlumTree.grow(level, pos, random, false);
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.is(this) && CoalPlumTree.canGrow(level, pos);
    }

    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }

    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.is(this) && isValidBonemealTarget(level, pos, state)) CoalPlumTree.grow(level, pos, random, false);
    }
}
