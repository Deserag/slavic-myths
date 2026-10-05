package org.slavicmyths.wood;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
public final class HangingLeaves extends Block {
 public HangingLeaves(){super(BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).randomTicks().noCollission());}
 @Override public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){BlockState above=w.getBlockState(p.above());return above.is(this)||above.is(Woodlands.SETS.get("willow").get("leaves"));}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos q){if(d==Direction.UP&&!canSurvive(s,w,p))w.scheduleTick(p,this,1);return super.updateShape(s,d,other,w,p,q);}
 @Override public void tick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){if(!canSurvive(s,w,p))w.destroyBlock(p,true);}
 @Override public void randomTick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){if(!canSurvive(s,w,p))w.destroyBlock(p,true);}
@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 60;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 30;}
}
