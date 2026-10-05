package org.slavicmyths.furniture;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class Furniture {
 public static final Map<String,DeferredHolder<Block, Block>> BLOCKS=new LinkedHashMap<>();
 public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RackTile>> RACK;
 public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT;
 public static final DeferredHolder<Item, Item> FEATHER;
 static {
  for(String wood:new String[]{"pine","linden"})for(String kind:new String[]{"table","chair","stool","bench","shelf","wardrobe","bedside_cabinet","weapon_rack"})add(wood+"_"+kind,kind);
  for(String kind:new String[]{"cloth_bag","wooden_crate","firewood_bundle","training_dummy","signal_bell"})add(kind,kind);
  RACK=ModTiles.TILES.register("weapon_rack",()->BlockEntityType.Builder.of(RackTile::new,get("pine_weapon_rack"),get("linden_weapon_rack")).build(null));
  SEAT=ModEntities.ENTITIES.register("furniture_seat",()->EntityType.Builder.<SeatEntity>of(SeatEntity::new,MobCategory.MISC).sized(.01F,.01F).clientTrackingRange(8).updateInterval(20).noSave().noSummon().build("slavicmyths:furniture_seat"));
  FEATHER=ModItems.ITEMS.register("mysterious_black_feather",()->new Item(new Item.Properties()));
 }
 private static void add(String id,String kind){DeferredHolder<Block, Block>b=ModBlocks.BLOCKS.register(id,()->kind.equals("wardrobe")?new WardrobeBlock():new FurnitureBlock(kind));BLOCKS.put(id,b);ModItems.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties()));}
 public static Block get(String id){return BLOCKS.get(id).get();}
 public static void init(){}
}
