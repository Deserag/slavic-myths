package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class SteppeResources {
 public static final Map<String,DeferredHolder<Block,Block>> ORES=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 static {
  ore("sulfur_ore","Серная руда",Blocks.COAL_ORE);
  ore("deepslate_sulfur_ore","Глубинносланцевая серная руда",Blocks.DEEPSLATE_COAL_ORE);
  ore("fire_coal_ore","Руда огненного угля",Blocks.COAL_ORE);
  item("fire_coal","Огненный уголь");
  item("sky_coal","Небесный уголь");
  item("steppe_ingot","Слиток степи");
 }
 private static void ore(String id,String name,Block template){var b=ModBlocks.BLOCKS.register(id,()->new Block(BlockBehaviour.Properties.ofFullCopy(template).requiresCorrectToolForDrops()));ORES.put(id,b);ITEMS.put(id,ModItems.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties()){@Override public Component getName(ItemStack s){return Component.translatableWithFallback(getDescriptionId(),name);}}));}
 private static void item(String id,String name){ITEMS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties()){@Override public Component getName(ItemStack s){return Component.translatableWithFallback(getDescriptionId(),name);}}));}
 public static void init(){}
 private SteppeResources(){}
}
