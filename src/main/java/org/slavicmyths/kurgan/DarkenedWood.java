package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import java.util.function.Supplier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
/** A processed material, not a fifth living tree species. */
public final class DarkenedWood {
 public static final Map<String,DeferredHolder<Block, Block>> BLOCKS=new LinkedHashMap<>();
 static {
  add("log",()->new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}
   @Override public BlockState getToolModifiedState(BlockState s,net.minecraft.world.item.context.UseOnContext context,net.neoforged.neoforge.common.ItemAbility ability,boolean simulate){return ability==net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP&&context.getItemInHand().canPerformAction(ability)?get("stripped_log").defaultBlockState().setValue(AXIS,s.getValue(AXIS)):super.getToolModifiedState(s,context,ability,simulate);}
  });
  add("stripped_log",()->new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("planks",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("stairs",()->new StairBlock(get("planks").defaultBlockState(),BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("slab",()->new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("fence",()->new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("fence_gate",()->new FenceGateBlock(WoodType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("door",()->new DoorBlock(BlockSetType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
  add("trapdoor",()->new TrapDoorBlock(BlockSetType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 10;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
 }
 private static void add(String n,Supplier<Block> factory){DeferredHolder<Block, Block>b=ModBlocks.BLOCKS.register("darkened_"+n,factory);BLOCKS.put(n,b);ModItems.ITEMS.register("darkened_"+n,()->new BlockItem(b.get(),new Item.Properties()));}
 public static Block get(String n){return BLOCKS.get(n).get();}
 public static void init(){}
}
