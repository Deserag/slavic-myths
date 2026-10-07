package org.slavicmyths.kitchen;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
import org.slavicmyths.furniture.Furniture;
public final class KitchenII {
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<KitchenTile>> TILE=ModTiles.TILES.register("kitchen_table",()->BlockEntityType.Builder.of(KitchenTile::new,ModBlocks.KITCHEN_TABLE.get()).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TableTile>> TABLE=ModTiles.TILES.register("table_display",()->BlockEntityType.Builder.of(TableTile::new,Furniture.get("pine_table"),Furniture.get("linden_table")).build(null));
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static TagKey<Item> tag(String s){return TagKey.create(Registries.ITEM,id(s));}
 public static Item item(String s){return BuiltInRegistries.ITEM.get(id(s));}
 private static void food(String s,int n,float sat,int count,boolean bowl){ITEMS.put(s,ModItems.ITEMS.register(s,()->{var b=new FoodProperties.Builder().nutrition(n).saturationModifier(sat);if(bowl)b.usingConvertsTo(Items.BOWL);return new Item(new Item.Properties().stacksTo(count).food(b.build()));}));}
 public static void init(IEventBus bus){
  for(String s:List.of("wheat_flour","rye_flour","oat_groats","barley_groats","dough"))ITEMS.put(s,ModItems.ITEMS.register(s,()->new Item(new Item.Properties())));
  food("rye_bread",6,.70F,64,false);food("baked_turnip",4,.50F,64,false);
  food("stewed_turnip",6,.65F,1,true);food("stewed_cabbage",6,.70F,1,true);food("oat_porridge",6,.65F,1,true);food("barley_porridge",6,.65F,1,true);food("berry_porridge",7,.75F,1,true);
  food("vegetable_stew",7,.70F,1,true);food("meat_stew",9,.85F,1,true);food("pea_soup",7,.75F,1,true);food("mushroom_stew_slavic",6,.70F,1,true);
  food("bliny",6,.65F,16,false);food("berry_bliny",8,.80F,16,false);food("meat_bliny",9,.85F,16,false);food("apple_bliny",8,.78F,16,false);food("honey_bliny",8,.82F,16,false);
  food("apple_pie",8,.80F,16,false);food("meat_pie",10,.90F,16,false);food("cabbage_pie",8,.80F,16,false);
 }
 private KitchenII(){}
}
