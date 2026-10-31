package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ItemLike;
import java.util.function.Supplier;

/** Four visible stages for the approved 2.2.9-C Overworld vegetables. */
public final class FoodCrop extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final MapCodec<FoodCrop> CODEC = simpleCodec(FoodCrop::new);
    private final Supplier<Item> plantingItem;
    public FoodCrop(Properties properties) { this(properties, () -> net.minecraft.world.item.Items.WHEAT_SEEDS); }
    public FoodCrop(Properties properties, Supplier<Item> plantingItem) { super(properties); this.plantingItem = plantingItem; }
    @Override protected MapCodec<? extends CropBlock> codec() { return CODEC; }
    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return 3; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected ItemLike getBaseSeedId() { return plantingItem.get(); }
    @Override protected int getBonemealAgeIncrease(Level level) { return 1; }
}
