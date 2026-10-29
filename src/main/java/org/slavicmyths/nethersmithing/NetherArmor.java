package org.slavicmyths.nethersmithing;
import java.util.*;import net.minecraft.core.*;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.Ingredient;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.*;import net.minecraft.world.item.component.ItemAttributeModifiers;import net.minecraft.network.chat.Component;
import org.slavicmyths.registry.ModItems;import org.slavicmyths.item.ModGear;
public final class NetherArmor {
 public static final Map<String,net.neoforged.neoforge.registries.DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static final net.neoforged.neoforge.registries.DeferredHolder<ArmorMaterial,ArmorMaterial> ZHARA=material(false),KING=material(true);
 private static net.neoforged.neoforge.registries.DeferredHolder<ArmorMaterial,ArmorMaterial> material(boolean king){
  return ModGear.ARMOR_MATERIALS.register(king?"infernal_king":"zhara",()->new ArmorMaterial(
   Map.of(ArmorItem.Type.HELMET,king?4:3,ArmorItem.Type.CHESTPLATE,king?10:8,ArmorItem.Type.LEGGINGS,king?8:6,ArmorItem.Type.BOOTS,king?4:3),18,net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE,
   ()->Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","alatyr_ingot"))),
   List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("slavicmyths",king?"infernal_king":"zhara"))),king?4:3,king?0:.05F));
 }
 static {for(boolean king:new boolean[]{false,true})for(var type:List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS)){String id=NetherArmorRules.id(king,type);ITEMS.put(id,ModItems.ITEMS.register(id,()->new Piece(king,type)));}}
 public static void init(){}
 public static final class Piece extends ArmorItem {
  public final boolean king;public final String id;
  public Piece(boolean king,Type type){super((king?KING:ZHARA),type,new Properties().durability(NetherArmorRules.durability(king,type)).rarity(king?Rarity.EPIC:Rarity.RARE).fireResistant());this.king=king;this.id=NetherArmorRules.id(king,type);}
  @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack){var a=super.getDefaultAttributeModifiers(stack);var group=EquipmentSlotGroup.bySlot(getEquipmentSlot());a=a.withModifierAdded(Attributes.MAX_HEALTH,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths",id+"_hp"),NetherArmorRules.hp(king,getType()),AttributeModifier.Operation.ADD_VALUE),group);
   if(king&&getType()==Type.LEGGINGS)a=a.withModifierAdded(Attributes.MOVEMENT_SPEED,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","king_leggings_speed"),.15,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),group);
   if(king&&getType()==Type.BOOTS)a=a.withModifierAdded(Attributes.JUMP_STRENGTH,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","king_boots_jump"),.30,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),group);return a;}
  @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/nether/"+id+".png");}
  @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flags){lines.add(Component.translatable("netherarmor.hp",NetherArmorRules.hp(king,getType())/2));lines.add(Component.translatable("netherarmor.effect."+id));lines.add(Component.translatable("netherarmor.sockets",org.slavicmyths.rpg.Runes.slots(stack),NetherArmorRules.sockets(king,getType())));}
 }
 public static boolean king(LivingEntity e,EquipmentSlot slot){var stack=e.getItemBySlot(slot);return stack.getItem() instanceof Piece p&&p.king;}
 public static boolean full(LivingEntity e){return e.isAlive()&&king(e,EquipmentSlot.HEAD)&&king(e,EquipmentSlot.CHEST)&&king(e,EquipmentSlot.LEGS)&&king(e,EquipmentSlot.FEET);}
 private NetherArmor(){}
}
