package org.slavicmyths.guardians;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import org.slavicmyths.sanctuary.SanctuaryBlock;
public final class GuardianNodeBlock extends BaseEntityBlock {
 private final boolean valve;
 public GuardianNodeBlock(boolean valve){super(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS).strength(3.5f,12).noOcclusion().lightLevel(s->s.getValue(SanctuaryBlock.LIT)?8:2));this.valve=valve;registerDefaultState(stateDefinition.any().setValue(SanctuaryBlock.FACING,Direction.NORTH).setValue(SanctuaryBlock.LIT,false));}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(SanctuaryBlock.FACING,SanctuaryBlock.LIT);}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GuardianNode(p,s);}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(SanctuaryBlock.FACING,r.rotate(s.getValue(SanctuaryBlock.FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(SanctuaryBlock.FACING)));}
 private void use(Level w,BlockPos at,Player p){if(w.getBlockEntity(at) instanceof GuardianNode node)node.use(p);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player p,BlockHitResult hit){use(w,at,p);return InteractionResult.sidedSuccess(w.isClientSide);}
 @Override protected ItemInteractionResult useItemOn(ItemStack st,BlockState s,Level w,BlockPos at,Player p,InteractionHand hand,BlockHitResult hit){if(hand==InteractionHand.MAIN_HAND)use(w,at,p);return ItemInteractionResult.sidedSuccess(w.isClientSide);}
}
