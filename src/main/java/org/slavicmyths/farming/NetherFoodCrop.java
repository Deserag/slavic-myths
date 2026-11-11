package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Reusable four-stage Nether crop for issue #480.
 * The first implementation stage does not register an item or add artwork:
 * each actual species must supply its approved planting item and substrate.
 */
public final class NetherFoodCrop extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final MapCodec<NetherFoodCrop> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            propertiesCodec(),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("planting_item").forGetter(c -> c.plantingItem.get()),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("substrate").forGetter(c -> c.substrate)
    ).apply(i, (properties, item, substrate) -> new NetherFoodCrop(properties, () -> item, substrate)));

    private final Supplier<Item> plantingItem;
    private final Block substrate;

    public NetherFoodCrop(Properties properties, Supplier<Item> plantingItem, Block substrate) {
        super(properties);
        this.plantingItem = plantingItem;
        this.substrate = substrate;
    }

    @Override public MapCodec<NetherFoodCrop> codec() { return CODEC; }
    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return 3; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected ItemLike getBaseSeedId() { return plantingItem.get(); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(substrate); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return level.getBlockState(pos.below()).is(substrate); }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (getAge(state) < getMaxAge() && random.nextInt(12) == 0) level.setBlock(pos, getStateForAge(getAge(state) + 1), 2);
    }
    @Override protected int getBonemealAgeIncrease(Level level) { return 1; }
}
