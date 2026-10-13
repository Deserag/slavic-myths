package org.slavicmyths.equipment;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.ModGear;

public final class UniqueEquipment {
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WIND_MATERIAL = material("wind", 2, 0, 0, Items.LEATHER);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BOGATYR_MATERIAL = material("bogatyr", 8, 3, .20F, null);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SIGHT_MATERIAL = material("clear_sight", 3, 1, 0, null);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LEGS_MATERIAL = material("indomitability", 6, 2, .10F, null);
    public static final DeferredHolder<Item, Item> HARDENED_IRON = ModItems.ITEMS.register("hardened_iron", () -> new Item(new Item.Properties()) {
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("unique.slavicmyths.iron_source").withStyle(ChatFormatting.GRAY));}
    });
    public static final DeferredHolder<Item, Item> WIND_BOOTS = armor("wind_boots", WIND_MATERIAL, ArmorItem.Type.BOOTS, 260);
    public static final DeferredHolder<Item, Item> BOGATYR_CHESTPLATE = armor("bogatyr_chestplate", BOGATYR_MATERIAL, ArmorItem.Type.CHESTPLATE, 640);
    public static final DeferredHolder<Item, Item> CLEAR_SIGHT_HELMET = armor("clear_sight_helmet", SIGHT_MATERIAL, ArmorItem.Type.HELMET, 330);
    public static final DeferredHolder<Item, Item> INDOMITABILITY_LEGGINGS = armor("indomitability_leggings", LEGS_MATERIAL, ArmorItem.Type.LEGGINGS, 600);
    public static final DeferredHolder<Item, Item> WARD_SHIELD = ModItems.ITEMS.register("ward_shield", () -> new WardShield());
    private static DeferredHolder<ArmorMaterial,ArmorMaterial> material(String id,int defense,float toughness,float kb,Item repair) {
        return ModGear.ARMOR_MATERIALS.register("unique_"+id, () -> new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET,defense,ArmorItem.Type.CHESTPLATE,defense,ArmorItem.Type.LEGGINGS,defense,ArmorItem.Type.BOOTS,defense),
            15, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(repair == null ? HARDENED_IRON.get() : repair),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("slavicmyths","unique_"+id))),toughness,kb));
    }
    private static DeferredHolder<Item,Item> armor(String id,Holder<ArmorMaterial> material,ArmorItem.Type type,int durability) {
        return ModItems.ITEMS.register(id, () -> new UniqueArmor(material,type,durability));
    }
    public static boolean unique(ItemStack stack) { return stack.getItem() instanceof UniqueArmor || stack.getItem() instanceof WardShield; }
    public static void init() { }
    private static void tooltip(Item item,List<Component> lines) {
        lines.add(Component.translatable(item.getDescriptionId()+".base").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable(item.getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(item.getDescriptionId()+".detail").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(item.getDescriptionId()+".extra").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("unique.slavicmyths.sockets").withStyle(ChatFormatting.DARK_AQUA));
    }
    public static final class UniqueArmor extends ArmorItem {
        UniqueArmor(Holder<ArmorMaterial> material,Type type,int durability) { super(material,type,new Properties().durability(durability).rarity(Rarity.RARE)); }
        @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
            var base=super.getDefaultAttributeModifiers(stack);
            if(getType()!=Type.BOOTS)return base;
            return base.withModifierAdded(Attributes.MOVEMENT_SPEED,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","wind_boots_speed"),UniqueEquipmentRules.SPEED,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),net.minecraft.world.entity.EquipmentSlotGroup.FEET)
                .withModifierAdded(Attributes.JUMP_STRENGTH,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","wind_boots_jump"),UniqueEquipmentRules.JUMP,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),net.minecraft.world.entity.EquipmentSlotGroup.FEET);
        }
        @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner) {
            String id=switch(getType()){case BOOTS->"wind";case CHESTPLATE->"bogatyr";case HELMET->"clear_sight";default->"indomitability";};
            return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/unique_"+id+ (inner ? "_layer_2.png" : "_layer_1.png"));
        }
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag) { tooltip(this,lines); }
    }
    public static final class WardShield extends ShieldItem {
        WardShield() { super(new Properties().durability(672).rarity(Rarity.RARE)); }
        @Override public boolean isValidRepairItem(ItemStack stack,ItemStack ingredient) { return ingredient.is(HARDENED_IRON.get()); }
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag) { super.appendHoverText(stack,context,lines,flag);tooltip(this,lines); }
    }
    private UniqueEquipment() { }
}
