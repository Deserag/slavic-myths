package org.slavicmyths.marine;

import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.item.ModGear;
import org.slavicmyths.registry.ModItems;

/** Netherite armor geometry, vanilla enchantments, four bounded ocean bonuses. */
public final class MarineArmor {
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MATERIAL =
        ModGear.ARMOR_MATERIALS.register("marine_armor", () -> new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 3, ArmorItem.Type.CHESTPLATE, 8,
                ArmorItem.Type.LEGGINGS, 6, ArmorItem.Type.BOOTS, 3),
            18, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(MarineRewards.ZHELVA_MATERIAL.get()),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("slavicmyths", "marine_armor"))),
            2.5F, 0.05F));

    public static final DeferredHolder<Item, Item> HELMET = ModItems.ITEMS.register("tide_helmet",
        () -> new Piece(ArmorItem.Type.HELMET, 407));
    public static final DeferredHolder<Item, Item> CHESTPLATE = ModItems.ITEMS.register("abyss_chestplate",
        () -> new Piece(ArmorItem.Type.CHESTPLATE, 592));
    public static final DeferredHolder<Item, Item> LEGGINGS = ModItems.ITEMS.register("storm_leggings",
        () -> new Piece(ArmorItem.Type.LEGGINGS, 555));
    public static final DeferredHolder<Item, Item> BOOTS = ModItems.ITEMS.register("waterwalk_boots",
        () -> new Piece(ArmorItem.Type.BOOTS, 481));
    public static void init() {}

    public static boolean full(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(HELMET.get())
            && entity.getItemBySlot(EquipmentSlot.CHEST).is(CHESTPLATE.get())
            && entity.getItemBySlot(EquipmentSlot.LEGS).is(LEGGINGS.get())
            && entity.getItemBySlot(EquipmentSlot.FEET).is(BOOTS.get());
    }

    public static final class Piece extends ArmorItem {
        private Piece(Type type, int durability) {
            super(MATERIAL, type, new Item.Properties().durability(durability).rarity(Rarity.RARE).fireResistant());
        }
        @Override public ResourceLocation getArmorTexture(ItemStack stack, Entity entity,
                EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
            return ResourceLocation.fromNamespaceAndPath("slavicmyths",
                "textures/armor/marine/marine_armor_layer_" + (inner ? "2" : "1") + ".png");
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            lines.add(Component.translatable("item.slavicmyths.marine_armor.health").withStyle(ChatFormatting.AQUA));
            if(getType()==Type.CHESTPLATE)lines.add(Component.translatable("item.slavicmyths.marine_armor.regeneration").withStyle(ChatFormatting.AQUA));
            if(getType()==Type.LEGGINGS)lines.add(Component.translatable("item.slavicmyths.marine_armor.strength").withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("item.slavicmyths.marine_armor.set")
                .withStyle(ChatFormatting.DARK_AQUA));
            lines.add(Component.translatable("item.slavicmyths.marine_armor.bonus")
                .withStyle(ChatFormatting.AQUA));
        }
    }
    private MarineArmor() {}
}
