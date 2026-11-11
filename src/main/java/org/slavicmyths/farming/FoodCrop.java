package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import java.util.function.Supplier;

/** Four visible stages for the approved 2.2.9-C Overworld vegetables. */
public final class FoodCrop extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final MapCodec<FoodCrop> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(), BuiltInRegistries.ITEM.byNameCodec().fieldOf("seed").forGetter(c -> c.plantingItem.get())).apply(i, (p, s) -> new FoodCrop(p, () -> s)));
    private final Supplier<Item> plantingItem;
    public FoodCrop(Properties properties, Supplier<Item> plantingItem) { super(properties); this.plantingItem = plantingItem; }
    @Override public MapCodec<FoodCrop> codec() { return CODEC; }
    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return 3; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected ItemLike getBaseSeedId() { return plantingItem.get(); }
    @Override protected int getBonemealAgeIncrease(Level level) { return 1; }

    // Explicit vanilla bone meal contract: only living immature crops accept a charge.
    // The server changes exactly one age and sends the new state to clients (flag 3).
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.is(this) && getAge(state) < getMaxAge() && canSurvive(state, level, pos);
    }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isValidBonemealTarget(level, pos, state))
            level.setBlock(pos, getStateForAge(getAge(state) + 1), 3);
    }
}
