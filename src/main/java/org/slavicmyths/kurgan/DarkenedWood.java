package org.slavicmyths.kurgan;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.*;
/** A processed material, not a fifth living tree species. */
public final class DarkenedWood {
 public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
 static {
  add("log",()->new RotatedPillarBlock(AbstractBlock.Properties.copy(Blocks.OAK_LOG)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}
   @Override public BlockState getToolModifiedState(BlockState s,net.minecraft.world.World w,net.minecraft.util.math.BlockPos p,net.minecraft.entity.player.PlayerEntity player,ItemStack stack,net.minecraftforge.common.ToolType tool){return tool==net.minecraftforge.common.ToolType.AXE?get("stripped_log").defaultBlockState().setValue(AXIS,s.getValue(AXIS)):super.getToolModifiedState(s,w,p,player,stack,tool);}
  });
  add("stripped_log",()->new RotatedPillarBlock(AbstractBlock.Properties.copy(Blocks.STRIPPED_OAK_LOG)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("planks",()->new Block(AbstractBlock.Properties.copy(Blocks.OAK_PLANKS)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("stairs",()->new StairsBlock(()->get("planks").defaultBlockState(),AbstractBlock.Properties.copy(Blocks.OAK_STAIRS)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("slab",()->new SlabBlock(AbstractBlock.Properties.copy(Blocks.OAK_SLAB)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("fence",()->new FenceBlock(AbstractBlock.Properties.copy(Blocks.OAK_FENCE)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("fence_gate",()->new FenceGateBlock(AbstractBlock.Properties.copy(Blocks.OAK_FENCE_GATE)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("door",()->new DoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_DOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
  add("trapdoor",()->new TrapDoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_TRAPDOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
 }
 private static void add(String n,Supplier<Block> factory){RegistryObject<Block>b=ModBlocks.BLOCKS.register("darkened_"+n,factory);BLOCKS.put(n,b);ModItems.ITEMS.register("darkened_"+n,()->new BlockItem(b.get(),new Item.Properties().tab(ModItemGroup.TAB)));}
 public static Block get(String n){return BLOCKS.get(n).get();}
 public static void init(){}
}
