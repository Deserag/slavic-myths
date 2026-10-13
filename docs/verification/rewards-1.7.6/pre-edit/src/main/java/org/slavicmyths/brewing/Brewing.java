package org.slavicmyths.brewing;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.registry.*;
public final class Brewing {
 public static final Map<String,DeferredHolder<Item,? extends Item>> ITEMS=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<Block,? extends Block>> BLOCKS=new LinkedHashMap<>();
 public static final String[] BEERS={"light_beer","dark_beer","hopped_beer","honey_beer","forest_beer","thunder_beer","veles_dark_beer","witch_berry_beer"};
 public static final Map<String,String> BATCHES=new LinkedHashMap<>();
 public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"slavicmyths");
 // No copyOnDeath: respawn starts sober. Milk never writes this attachment.
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<CompoundTag>> LOAD=ATTACHMENTS.register("drink_overload",()->AttachmentType.builder(()->new CompoundTag()).serialize(CompoundTag.CODEC).build());
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BrewTile>> TYPE=ModTiles.TILES.register("brewing_equipment",()->BlockEntityType.Builder.of(BrewTile::new,BLOCKS.get("fruit_press").get(),BLOCKS.get("fermentation_vat").get(),BLOCKS.get("small_keg").get()).build(null));
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static Item item(String id){return ITEMS.get(id).get();}
 public static String key(ItemStack s){return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();}
 public static void init(net.neoforged.bus.api.IEventBus bus){
  for(String s:new String[]{"malted_barley","malt_sack","fruit_pomace","berry_mash","hops"})ITEMS.put(s,ModItems.ITEMS.register(s,()->new Item(new Item.Properties())));
  for(String s:new String[]{"honey_infusion","forest_herbal_infusion","thunder_crystal_powder"})ITEMS.put(s,ModItems.ITEMS.register(s,()->new Item(new Item.Properties().stacksTo(16))));
  for(String s:new String[]{"wooden_mug","dark_bottle","ceramic_pitcher","filled_pitcher","berry_mors_pitcher"})ITEMS.put(s,ModItems.ITEMS.register(s,()->new BeverageItem(s,new Item.Properties().stacksTo(s.endsWith("pitcher")&&!s.equals("ceramic_pitcher")?1:16))));
  for(String s:BEERS)drink(s+"_mug",s.contains("thunder")||s.contains("veles")||s.contains("witch")?8:16,0,0);
  drink("kvass_mug",16,2,.35F);drink("berry_mors_mug",16,2,.30F);drink("mead_bottle",16,0,0);drink("apple_cider_bottle",16,0,0);drink("apple_juice_bottle",16,2,.30F);drink("spoiled_brew",16,0,0);
  for(String s:BEERS)batch(s.equals("veles_dark_beer")?"veles_dark_wort":s.equals("witch_berry_beer")?"witch_berry_wort":s+"_wort",s);
  batch("kvass_wort","kvass");batch("mead_must","mead");batch("cider_must","apple_cider");
  for(String s:new String[]{"fruit_press","fermentation_vat","small_keg"}){var b=ModBlocks.BLOCKS.register(s,()->new BrewBlock(s));BLOCKS.put(s,b);ITEMS.put(s,ModItems.ITEMS.register(s,()->new BlockItem(b.get(),new Item.Properties().stacksTo(1))));}
  var hops=ModBlocks.BLOCKS.register("hops_crop",()->new HopsCrop(Block.Properties.ofFullCopy(Blocks.WHEAT)));BLOCKS.put("hops_crop",hops);ITEMS.put("hops_cutting",ModItems.ITEMS.register("hops_cutting",()->new ItemNameBlockItem(hops.get(),new Item.Properties())));
  VatMenu.TYPE.getId();VatRecipe.SERIALIZER.getId();ATTACHMENTS.register(bus);
 }
 private static void drink(String s,int max,int nutrition,float saturation){var p=new Item.Properties().stacksTo(max);if(nutrition>0)p.food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build());ITEMS.put(s,ModItems.ITEMS.register(s,()->new BeverageItem(s,p)));}
 private static void batch(String s,String beverage){BATCHES.put(s,beverage);ITEMS.put(s,ModItems.ITEMS.register(s,()->new Item(new Item.Properties().stacksTo(1))));}
 public static int category(String s){if(BATCHES.containsKey(s)||s.equals("filled_pitcher"))return -1;if(s.equals("hops")||s.equals("hops_cutting"))return 2;if(s.equals("thunder_crystal_powder"))return 7;if(BLOCKS.containsKey(s))return 6;if(s.endsWith("_mug")||s.endsWith("_bottle")||Set.of("wooden_mug","dark_bottle","ceramic_pitcher","filled_pitcher","berry_mors_pitcher","spoiled_brew").contains(s))return 3;return 1;}
 private Brewing(){}
}
