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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slavicmyths.registry.ModItems;

/** Fruiting is fixed per branch at generation: no new fruit nodes can appear from bone meal or harvesting. */
public final class CoalPlumLeaves extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    public static final BooleanProperty FRUITING = BooleanProperty.create("fruiting");
    private static final MapCodec<CoalPlumLeaves> CODEC = simpleCodec(CoalPlumLeaves::new);

    public CoalPlumLeaves(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(FRUITING, false));
    }

    @Override public MapCodec<CoalPlumLeaves> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE, FRUITING); }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(FRUITING) && state.getValue(AGE) < 3 && random.nextInt(24) == 0)
            level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 3);
    }

    private InteractionResult harvest(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(FRUITING) || state.getValue(AGE) != 3 ||
                !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), player.getMainHandItem()))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(AGE, 0), 3);
            popResource(level, pos, new ItemStack(ModItems.COAL_PLUM.get(), 2 + level.random.nextInt(3)));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 0.8F, 0.9F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return harvest(state, level, pos, player, hit);
    }

    @Override public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.BONE_MEAL)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (!state.getValue(FRUITING) || state.getValue(AGE) != 3) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return harvest(state, level, pos, player, hit) == InteractionResult.PASS
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(FRUITING) && state.getValue(AGE) < 3;
    }

    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }

    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.is(this) && isValidBonemealTarget(level, pos, state))
            level.setBlock(pos, state.setValue(AGE, Math.min(3, state.getValue(AGE) + 1)), 3);
    }
}
