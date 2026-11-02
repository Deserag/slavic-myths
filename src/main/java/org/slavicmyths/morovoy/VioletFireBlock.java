package org.slavicmyths.morovoy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Temporary violet fire: never spreads; removed when no living Morovoy Fire Zmey is nearby. */
public final class VioletFireBlock extends BaseFireBlock {
    public static final MapCodec<VioletFireBlock> CODEC = simpleCodec(VioletFireBlock::new);
    private static final int CHECK_TICKS = 100;
    public VioletFireBlock(BlockBehaviour.Properties properties) { super(properties, 5.0F); }
    @Override protected MapCodec<? extends BaseFireBlock> codec() { return CODEC; }
    @Override protected boolean canBurn(BlockState state) { return false; }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide && !oldState.is(this)) level.scheduleTick(pos, this, CHECK_TICKS);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof LivingEntity living) || living instanceof MorovoyFireZmey || entity.tickCount % 4 != 0) return;
        if (living.hurt(server.damageSources().magic(), 5.0F)) living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1));
    }
    public static boolean place(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.below()).isSolid()) return false;
        return level.setBlock(pos, org.slavicmyths.registry.ModBlocks.VIOLET_FIRE.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
