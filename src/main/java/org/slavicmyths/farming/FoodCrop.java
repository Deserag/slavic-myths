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
}
