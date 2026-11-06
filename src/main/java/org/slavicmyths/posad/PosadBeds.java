package org.slavicmyths.posad;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
/** Two-part fireproof resident cot. Nether interaction never invokes vanilla bed explosion. */
public final class PosadBeds {
 public static final net.neoforged.neoforge.registries.DeferredHolder<Block,Cot> COT=org.slavicmyths.registry.ModBlocks.BLOCKS.register("posad_cot",Cot::new);
 static {org.slavicmyths.registry.ModItems.ITEMS.register("posad_cot",()->new BedItem(COT.get(),new Item.Properties().stacksTo(1)));}
 public static void init(){}
 public static final class Cot extends BedBlock {
  public Cot(){super(DyeColor.RED,Properties.of().strength(1.5F).sound(SoundType.STONE).noOcclusion());}
  @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState state){return null;}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level world,BlockPos pos,Player player,BlockHitResult hit){
   if(!world.dimensionType().bedWorks()){if(!world.isClientSide)player.displayClientMessage(Component.translatable("posad.cot.nether"),true);return InteractionResult.sidedSuccess(world.isClientSide);}
   return super.useWithoutItem(state,world,pos,player,hit);
  }
 }
 private static final int[][] BEDS={{18,1,18,1},{30,1,18,1},{18,1,25,1},{30,1,25,1},{18,7,18,1},{30,7,18,1},{18,7,25,1},{30,7,25,1},{16,1,25,1},{32,1,25,1},{16,7,18,1},{32,7,18,1},{16,7,25,1},{32,7,25,1},{15,1,82,1},{33,1,82,1},{15,7,82,1},{33,7,82,1}};
 static BlockState voxel(int x,int y,int z,boolean expanded){
  for(int i=expanded?0:8;i<BEDS.length;i++){var b=BEDS[i];if(x==b[0]&&y==b[1]&&(z==b[2]||z==b[2]+b[3]))return COT.get().defaultBlockState().setValue(BedBlock.FACING,b[3]>0?Direction.SOUTH:Direction.NORTH).setValue(BedBlock.PART,z==b[2]?BedPart.FOOT:BedPart.HEAD);}
  return null;
 }
 private PosadBeds(){}
}
