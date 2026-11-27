package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.item.ModGear;
import org.slavicmyths.registry.ModItems;
public final class SteppeArmor {
 public static final Map<String,DeferredHolder<Item,Item>> MATERIALS=new LinkedHashMap<>();
 static {
  material("jackal_hide","Шкура степного шакала");
  material("jackal_fang","Клык степного шакала");
  material("black_hyena_hide","Шкура чёрной гиены");
  material("black_hyena_fang","Клык чёрной гиены");
  material("veteran_hyena_hide","Шкура матерой гиены");
  material("veteran_hyena_fang","Клык матерой гиены");
 }
 private static void material(String id,String name){MATERIALS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties().rarity(Rarity.RARE)){
  @Override public Component getName(ItemStack stack){return Component.translatableWithFallback(getDescriptionId(),name);}
 }));}
 public static final DeferredHolder<ArmorMaterial,ArmorMaterial> MATERIAL=ModGear.ARMOR_MATERIALS.register("steppe_armor",()->new ArmorMaterial(
 Map.of(ArmorItem.Type.HELMET,3,ArmorItem.Type.CHESTPLATE,6,ArmorItem.Type.LEGGINGS,5,ArmorItem.Type.BOOTS,3),
 18,SoundEvents.ARMOR_EQUIP_LEATHER,()->Ingredient.of(MATERIALS.get("jackal_hide").get(),MATERIALS.get("black_hyena_hide").get(),MATERIALS.get("veteran_hyena_hide").get()),
 List.of(new ArmorMaterial.Layer(id("steppe_armor"))),0F,0F));
 public static final Map<ArmorItem.Type,DeferredHolder<Item,Item>> ITEMS=new EnumMap<>(ArmorItem.Type.class);
 static {for(var type:List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS))ITEMS.put(type,ModItems.ITEMS.register(itemId(type),()->new Piece(type)));}
 public static void init(){}
 static ResourceLocation id(String key){return ResourceLocation.fromNamespaceAndPath("slavicmyths",key);}
 static String itemId(ArmorItem.Type type){return "steppe_"+switch(type){case HELMET->"helmet";case CHESTPLATE->"chestplate";case LEGGINGS->"leggings";case BOOTS->"boots";default->throw new IllegalArgumentException();};}
 public static int count(LivingEntity e){int n=0;for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))if(e.getItemBySlot(slot).getItem() instanceof Piece p&&p.getEquipmentSlot()==slot)n++;return n;}
 public static boolean full(LivingEntity e){return count(e)==4;}
 public static final class Piece extends ArmorItem{
  Piece(Type t){super(MATERIAL,t,new Item.Properties().durability(switch(t){case HELMET->180;case CHESTPLATE->260;case LEGGINGS->240;case BOOTS->200;default->1;}).rarity(Rarity.RARE));}
  @Override public Component getName(ItemStack stack){return Component.translatableWithFallback(getDescriptionId(),switch(getType()){case HELMET->"Капюшон князя степей";case CHESTPLATE->"Нагрудник князя степей";case LEGGINGS->"Поножи князя степей";case BOOTS->"Сапоги князя степей";default->"Броня князя степей";});}
  @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack){
   var slot=EquipmentSlotGroup.bySlot(getEquipmentSlot());
   var key=itemId(getType());
   var mods=super.getDefaultAttributeModifiers(stack).withModifierAdded(Attributes.MOVEMENT_SPEED,new AttributeModifier(id(key+"_speed"),getType()==Type.BOOTS?.10:.05,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),slot);
   if(getType()!=Type.BOOTS)mods=mods.withModifierAdded(Attributes.MAX_HEALTH,new AttributeModifier(id(key+"_hp"),2,AttributeModifier.Operation.ADD_VALUE),slot);
   double damage=switch(getType()){case HELMET->.03;case CHESTPLATE->.07;case LEGGINGS->.05;default->0;};
   if(damage>0)mods=mods.withModifierAdded(Attributes.ATTACK_DAMAGE,new AttributeModifier(id(key+"_damage"),damage,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),slot);
   return mods;
  }
  @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity e,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return id("textures/armor/steppe/steppe_atlas.png");}
 }
 private SteppeArmor(){}
}
