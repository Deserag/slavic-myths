package org.slavicmyths.sanctuary;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
public final class SanctuaryBlock extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;public static final BooleanProperty LIT=BlockStateProperties.LIT;
 public final String kind;
 public SanctuaryBlock(String kind){super(BlockBehaviour.Properties.ofFullCopy(kind.startsWith("carved_fire")||kind.startsWith("fire_idol")?Blocks.OAK_LOG:Blocks.STONE_BRICKS).mapColor(kind.startsWith("carved_fire")||kind.startsWith("fire_idol")?net.minecraft.world.level.material.MapColor.WOOD:net.minecraft.world.level.material.MapColor.STONE).strength(3.5f,10).noOcclusion().lightLevel(s->s.getValue(LIT)?kind.contains("spring")?9:15:kind.equals("unfading_brazier")?3:0));this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LIT,false));}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LIT);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return kind.equals("unfading_brazier")||kind.equals("pure_spring_heart")||kind.equals("tainted_spring_node")?new SanctuaryHeart(p,s):null;}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> t){return w.isClientSide?null:createTickerHelper(t,SanctuaryContent.HEART.get(),SanctuaryHeart::tick);}
 private boolean action(Level w,BlockPos at,Player p,InteractionHand hand){if(w.getBlockEntity(at) instanceof SanctuaryHeart heart)return heart.interact(p,hand);if(kind.startsWith("seal_")){if(p instanceof ServerPlayer server&&server.tickCount%10==0){server.displayClientMessage(SanctuaryContent.text("sealed"),true);SanctuaryContent.sound(w,at,"door_blocked");}return true;}return false;}
 @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level w,BlockPos at,Player p,InteractionHand hand,BlockHitResult hit){return action(w,at,p,hand)?ItemInteractionResult.sidedSuccess(w.isClientSide):ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
 @Override protected InteractionResult useWithoutItem(BlockState state,Level w,BlockPos at,Player p,BlockHitResult hit){return action(w,at,p,InteractionHand.MAIN_HAND)?InteractionResult.sidedSuccess(w.isClientSide):InteractionResult.PASS;}
 @Override public void animateTick(BlockState s,Level w,BlockPos p,net.minecraft.util.RandomSource r){if(kind.equals("unfading_brazier")&&r.nextInt(s.getValue(LIT)?8:30)==0)w.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,p.getX()+.5,p.getY()+.8,p.getZ()+.5,0,.015,0);if(kind.equals("pure_spring_heart")&&r.nextInt(25)==0)w.addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,p.getX()+.5,p.getY()+1,p.getZ()+.5,0,.03,0);}
}
