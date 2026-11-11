package org.slavicmyths.farming;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ItemLike;
/** Issue363 R2: edible planting unit, crimson nylium, no water or daylight gate. */
public final class CrimsonPepperCrop extends CropBlock {
 public static final IntegerProperty AGE=IntegerProperty.create("age",0,3);
 public static final MapCodec<CrimsonPepperCrop> CODEC=simpleCodec(CrimsonPepperCrop::new);
 public CrimsonPepperCrop(Properties p){super(p);}
 @Override public MapCodec<CrimsonPepperCrop> codec(){return CODEC;}
 @Override public IntegerProperty getAgeProperty(){return AGE;}
 @Override public int getMaxAge(){return 3;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(AGE);}
 @Override protected ItemLike getBaseSeedId(){return org.slavicmyths.registry.ModItems.CRIMSON_PEPPER.get();}
 @Override protected boolean mayPlaceOn(BlockState s,BlockGetter w,BlockPos p){return s.is(Blocks.CRIMSON_NYLIUM);}
 @Override public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){return w.getBlockState(p.below()).is(Blocks.CRIMSON_NYLIUM);}
 @Override public void randomTick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){if(getAge(s)<3&&r.nextInt(12)==0)w.setBlock(p,getStateForAge(getAge(s)+1),2);}
 @Override protected int getBonemealAgeIncrease(Level w){return 1;}

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
