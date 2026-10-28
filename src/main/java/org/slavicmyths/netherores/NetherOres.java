package org.slavicmyths.netherores;

import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Resource-only patch #324. No equipment, new furnace, XP-from-replaced-ore or healing. */
public final class NetherOres {
 public static final List<String> ORES=List.of("livingstone_ore","alatyrite_ore","runnik_ore","infernal_ore");
 public static final List<String> RAW=List.of("raw_livingstone","raw_alatyrite","raw_runnik","raw_infernal_shard");
 public static final List<String> ROASTED=List.of("roasted_livingstone","roasted_alatyrite","roasted_runnik","roasted_infernal_shard");
 public static final Map<String,DeferredHolder<Block,Block>> BLOCKS=new LinkedHashMap<>();
 static {
  var blocks=new ArrayList<>(ORES);blocks.add("sulfur_deposit");
  for(String id:blocks){var block=ModBlocks.BLOCKS.register(id,()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_QUARTZ_ORE).strength(id.equals("sulfur_deposit")?2:4,6).requiresCorrectToolForDrops()));BLOCKS.put(id,block);ModItems.ITEMS.register(id,()->new BlockItem(block.get(),new Item.Properties()));}
  var items=new ArrayList<>(RAW);items.addAll(ROASTED);items.add("sulfur");items.add("alatyr_ingot");
  for(String id:items)ModItems.ITEMS.register(id,()->new Item(new Item.Properties()){
   @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<net.minecraft.network.chat.Component> text,TooltipFlag flag){text.add(net.minecraft.network.chat.Component.translatable("netherores."+(id.startsWith("raw_")?"raw":id.equals("sulfur")?"sulfur":id.equals("alatyr_ingot")?"ingot":"roasted")));}
  });
 }
 public static void init(){NetherOreFeature.init();}
 private NetherOres(){}
}
