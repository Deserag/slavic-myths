package org.slavicmyths.kurgan;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
public final class BurialCoffinBlock extends HorizontalBlock {
 public static final EnumProperty<BedPart> PART=net.minecraft.state.properties.BlockStateProperties.BED_PART;
 public BurialCoffinBlock(){super(Properties.copy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,BedPart.FOOT));}
 @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState>b){b.add(FACING,PART);}
 @Override public BlockState getStateForPlacement(BlockItemUseContext c){Direction d=c.getHorizontalDirection();BlockPos other=c.getClickedPos().relative(d);return c.getLevel().getBlockState(other).canBeReplaced(c)&&c.getLevel().getWorldBorder().isWithinBounds(other)?defaultBlockState().setValue(FACING,d):null;}
 @Override public void setPlacedBy(World w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){if(!w.isClientSide){w.setBlock(p.relative(s.getValue(FACING)),s.setValue(PART,BedPart.HEAD),3);if(w.getBlockEntity(p)instanceof BurialCoffinTile)((BurialCoffinTile)w.getBlockEntity(p)).makeDomestic();}}
 public static BlockPos foot(BlockState s,BlockPos p){return s.getValue(PART)==BedPart.FOOT?p:p.relative(s.getValue(FACING).getOpposite());}
 @Override public boolean hasTileEntity(BlockState s){return s.getValue(PART)==BedPart.FOOT;}
 @Override public TileEntity createTileEntity(BlockState s,IBlockReader w){return new BurialCoffinTile();}
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){return box(1,0,1,15,13,15);}
 @Override public VoxelShape getOcclusionShape(BlockState s,IBlockReader w,BlockPos p){return VoxelShapes.empty();}
 @Override public BlockRenderType getRenderShape(BlockState s){return BlockRenderType.ENTITYBLOCK_ANIMATED;}
 @Override public PushReaction getPistonPushReaction(BlockState s){return PushReaction.BLOCK;}
 @Override public ActionResultType use(BlockState s,World w,BlockPos p,PlayerEntity player,Hand hand,BlockRayTraceResult hit){BlockPos base=foot(s,p);if(!w.isClientSide&&w.getBlockEntity(base)instanceof BurialCoffinTile)NetworkHooks.openGui((ServerPlayerEntity)player,(BurialCoffinTile)w.getBlockEntity(base),base);return ActionResultType.sidedSuccess(w.isClientSide);}
 @Override public void playerWillDestroy(World w,BlockPos p,BlockState s,PlayerEntity player){if(!w.isClientSide&&player.isCreative()&&s.getValue(PART)==BedPart.HEAD){BlockPos base=foot(s,p);if(w.getBlockState(base).is(this))w.removeBlock(base,false);}super.playerWillDestroy(w,p,s,player);}
 @Override public void onRemove(BlockState s,World w,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())){
  if(s.getValue(PART)==BedPart.FOOT){TileEntity t=w.getBlockEntity(p);if(t instanceof BurialCoffinTile)InventoryHelper.dropContents(w,p,(BurialCoffinTile)t);BlockPos other=p.relative(s.getValue(FACING));if(w.getBlockState(other).is(this)&&w.getBlockState(other).getValue(PART)==BedPart.HEAD&&w.getBlockState(other).getValue(FACING)==s.getValue(FACING))w.removeBlock(other,false);}
  else {BlockPos base=foot(s,p);if(w.getBlockState(base).is(this)&&w.getBlockState(base).getValue(PART)==BedPart.FOOT&&w.getBlockState(base).getValue(FACING)==s.getValue(FACING))w.destroyBlock(base,true);}
 }super.onRemove(s,w,p,next,moving);}
}
