package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slavicmyths.registry.ModItems;

/** Four approved berry growth states; harvesting regresses 3 -> 2 without breaking the bush. */
public final class NetherBerryBush extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final MapCodec<NetherBerryBush> CODEC = simpleCodec(NetherBerryBush::new);

    public NetherBerryBush(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override protected MapCodec<? extends NetherBerryBush> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(Blocks.CRIMSON_NYLIUM); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).is(Blocks.CRIMSON_NYLIUM); }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 3 && random.nextInt(12) == 0 && canSurvive(state, level, pos))
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(2, 0, 2, 14, 5 + state.getValue(AGE) * 3, 14);
    }

    private InteractionResult harvest(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 3 || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), player.getMainHandItem()))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(AGE, 2), 3);
            popResource(level, pos, new ItemStack(ModItems.HEAT_BERRIES.get(), 1 + level.random.nextInt(3)));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return harvest(state, level, pos, player, hit);
    }

    @Override public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.BONE_MEAL)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (state.getValue(AGE) != 3) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return harvest(state, level, pos, player, hit) == InteractionResult.PASS
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3 && canSurvive(state, level, pos);
    }

    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }

    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.is(this) && isValidBonemealTarget(level, pos, state))
            level.setBlock(pos, state.setValue(AGE, Math.min(3, state.getValue(AGE) + 1)), 3);
    }
}
