package org.slavicmyths.storage;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class Household {
 public enum Kind {SACK("storage_sack",4,"sack_items"),BASKET("basket",4,"basket_items"),LARGE("large_basket",9,"large_basket_items"),CRATE("produce_crate",9,"crate_items"),CHEST("household_chest",27,""),BARREL("wooden_barrel",9,"barrel_items"),WALL("wall_shelf",3,""),SHELF("storage_shelf",6,""),DRY("drying_rack",4,""),HAY("haystack",0,"");
  public final String id,tag;public final int capacity;Kind(String id,int n,String tag){this.id=id;capacity=n;this.tag=tag;}public boolean multi(){return this==SHELF||this==DRY||this==HAY;}public boolean display(){return this==WALL||this==SHELF;}}
 public static final Map<Kind,DeferredHolder<Block,? extends Block>> BLOCKS=new EnumMap<>(Kind.class);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StorageTile>> TYPE=ModTiles.TILES.register("household_storage",()->BlockEntityType.Builder.of(StorageTile::new,BLOCKS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null));
 public static void init(){for(Kind k:Kind.values()){var b=ModBlocks.BLOCKS.register(k.id,()->k==Kind.SACK?new StorageBlock.Sack():k==Kind.CHEST?new StorageBlock.Chest():k==Kind.HAY?new StorageBlock.Hay():k==Kind.SHELF?new StorageBlock.Vertical(k):k==Kind.DRY?new StorageBlock.Multi(k):new StorageBlock(k));BLOCKS.put(k,b);ModItems.ITEMS.register(k.id,()->new BlockItem(b.get(),new Item.Properties()));}
  for(String grain:List.of("rye","barley","oat")){var b=ModBlocks.BLOCKS.register(grain+"_sheaf",SheafBlock::new);ModItems.ITEMS.register(grain+"_sheaf",()->new BlockItem(b.get(),new Item.Properties()));}
  ModItems.ITEMS.register("dried_berries",()->new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(.35F).build())));ModItems.ITEMS.register("dried_mushrooms",()->new Item(new Item.Properties()));DryingRecipe.SERIALIZER.getId();
 }
 private Household(){}
}
