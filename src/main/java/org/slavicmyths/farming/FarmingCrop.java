package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Seven-stage farmland crops; vanilla light, moisture and growth hooks remain authoritative. */
public final class FarmingCrop extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 6);
    public static final MapCodec<FarmingCrop> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            propertiesCodec(), BuiltInRegistries.ITEM.byNameCodec().fieldOf("seed").forGetter(c -> c.seed.get()),
            com.mojang.serialization.Codec.floatRange(0, 1).fieldOf("growth_rate").forGetter(c -> c.rate)
    ).apply(i, (p, s, r) -> new FarmingCrop(p, () -> s, r)));
    private final Supplier<Item> seed;
    private final float rate;
    public FarmingCrop(Properties properties, Supplier<Item> seed, float rate) {
        super(properties); this.seed = seed; this.rate = rate;
    }
    @Override public MapCodec<FarmingCrop> codec() { return CODEC; }
    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return 6; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected ItemLike getBaseSeedId() { return seed.get(); }
    @Override protected int getBonemealAgeIncrease(Level level) { return 1 + level.random.nextInt(2); }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.FARMLAND) && hasSufficientLight(level, pos);
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (rate >= 1 || random.nextFloat() < rate) super.randomTick(state, level, pos, random);
    }
}
