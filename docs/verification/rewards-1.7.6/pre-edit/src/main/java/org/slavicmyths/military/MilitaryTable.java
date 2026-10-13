package org.slavicmyths.military;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class MilitaryTable extends HorizontalDirectionalBlock implements EntityBlock {
 public static final MapCodec<MilitaryTable> CODEC=simpleCodec(p->new MilitaryTable());
 public MilitaryTable(){super(Properties.of().strength(3).sound(SoundType.WOOD).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return Block.box(0,0,0,16,16,16);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new MilitaryTableTile(p,s);}
 protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){if(!w.isClientSide&&w.getBlockEntity(pos) instanceof MilitaryTableTile tile){p.openMenu(tile,pos);if(p instanceof net.minecraft.server.level.ServerPlayer sp)MilitaryNetwork.sync(sp,pos);}return InteractionResult.sidedSuccess(w.isClientSide);}
}
