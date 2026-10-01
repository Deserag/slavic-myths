package org.slavicmyths.furniture;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.tileentity.*;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.*;
public final class Furniture {
 public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
 public static final RegistryObject<TileEntityType<RackTile>> RACK;
 public static final RegistryObject<EntityType<SeatEntity>> SEAT;
 public static final RegistryObject<Item> FEATHER;
 static {
  for(String wood:new String[]{"pine","linden"})for(String kind:new String[]{"table","chair","stool","bench","shelf","wardrobe","bedside_cabinet","weapon_rack"})add(wood+"_"+kind,kind);
  for(String kind:new String[]{"cloth_bag","wooden_crate","firewood_bundle","training_dummy","signal_bell"})add(kind,kind);
  RACK=ModTiles.TILES.register("weapon_rack",()->TileEntityType.Builder.of(RackTile::new,get("pine_weapon_rack"),get("linden_weapon_rack")).build(null));
  SEAT=ModEntities.ENTITIES.register("furniture_seat",()->EntityType.Builder.<SeatEntity>of(SeatEntity::new,EntityClassification.MISC).sized(.01F,.01F).clientTrackingRange(8).updateInterval(20).noSave().noSummon().build("slavicmyths:furniture_seat"));
  FEATHER=ModItems.ITEMS.register("mysterious_black_feather",()->new Item(new Item.Properties().tab(ModItemGroup.TAB)));
 }
 private static void add(String id,String kind){RegistryObject<Block>b=ModBlocks.BLOCKS.register(id,()->kind.equals("wardrobe")?new WardrobeBlock():new FurnitureBlock(kind));BLOCKS.put(id,b);ModItems.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties().tab(ModItemGroup.TAB)));}
 public static Block get(String id){return BLOCKS.get(id).get();}
 public static void init(){}
}
