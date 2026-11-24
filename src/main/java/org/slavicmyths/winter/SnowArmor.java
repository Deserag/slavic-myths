package org.slavicmyths.winter;

import java.util.*;
import net.minecraft.ChatFormatting;
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

public final class SnowArmor {
    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> MATERIAL=ModGear.ARMOR_MATERIALS.register("snow_armor",()->new ArmorMaterial(
        Map.of(ArmorItem.Type.HELMET,4,ArmorItem.Type.CHESTPLATE,9,ArmorItem.Type.LEGGINGS,7,ArmorItem.Type.BOOTS,4),
        18,SoundEvents.ARMOR_EQUIP_NETHERITE,()->Ingredient.of(ModItems.WINTER_STEEL_INGOT.get()),
        List.of(new ArmorMaterial.Layer(id("snow_armor"))),4F,.10F));
    public static final Map<ArmorItem.Type,DeferredHolder<Item,Item>> ITEMS=new EnumMap<>(ArmorItem.Type.class);
    static {
        for(var type:List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS))
            ITEMS.put(type,ModItems.ITEMS.register(itemId(type),()->new Piece(type)));
    }
    public static void init(){}
    public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
    public static String itemId(ArmorItem.Type type){return switch(type){case HELMET->"snow_helmet";case CHESTPLATE->"snow_chestplate";case LEGGINGS->"snow_leggings";case BOOTS->"snow_boots";default->throw new IllegalArgumentException("Not a player armor slot");};}
    public static int durability(ArmorItem.Type type){return switch(type){case HELMET->500;case CHESTPLATE->750;case LEGGINGS->700;case BOOTS->600;default->0;};}
    public static int hp(ArmorItem.Type type){return switch(type){case HELMET->3;case CHESTPLATE->6;case LEGGINGS->5;case BOOTS->2;default->0;};}
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    public static int count(LivingEntity e){int count=0;for(var slot:SLOTS){var stack=e.getItemBySlot(slot);if(stack.getItem() instanceof Piece piece&&piece.getEquipmentSlot()==slot)count++;}return count;}
    public static boolean full(LivingEntity e){return e.isAlive()&&count(e)==4;}
    public static final class Piece extends ArmorItem {
        public Piece(Type type){super(MATERIAL,type,new Item.Properties().durability(SnowArmor.durability(type)).rarity(Rarity.EPIC).fireResistant());}
        @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack){
            return super.getDefaultAttributeModifiers(stack).withModifierAdded(Attributes.MAX_HEALTH,
                    new AttributeModifier(id(itemId(getType())+"_hp"),hp(getType()),AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.bySlot(getEquipmentSlot()));
        }
        @Override public boolean canWalkOnPowderedSnow(ItemStack stack,LivingEntity wearer){return getType()==Type.BOOTS&&full(wearer);}
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
            super.appendHoverText(stack,context,lines,flag);
            lines.add(Component.translatable("armor.slavicmyths.snow.piece").withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("armor.slavicmyths.snow.sockets",org.slavicmyths.rpg.Runes.slots(stack),3).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("armor.slavicmyths.snow.set").withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("armor.slavicmyths.snow.aura").withStyle(ChatFormatting.GRAY));
        }
    }
    private SnowArmor(){}
}
