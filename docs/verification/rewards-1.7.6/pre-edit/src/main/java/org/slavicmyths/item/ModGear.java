package org.slavicmyths.item;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slavicmyths.registry.ModItems;

/** Original tool/armor values, using target registries and incorrect-drop tags. */
public final class ModGear {
    public static final Tier PERUNITE = tier(850, 7, 2.5F, BlockTags.INCORRECT_FOR_IRON_TOOL, 13, () -> Ingredient.of(ModItems.PERUNITE.get()));
    public static final Tier SILVER = tier(220, 6, 2, BlockTags.INCORRECT_FOR_IRON_TOOL, 18, () -> Ingredient.of(ModItems.SILVER_INGOT.get()));
    public static final Tier BONE = tier(75, 4, 0, BlockTags.INCORRECT_FOR_WOODEN_TOOL, 5, () -> Ingredient.of(Items.BONE));
    private static Tier tier(int uses, float speed, float attack, TagKey<Block> incorrect, int enchantment, Supplier<Ingredient> repair) {
        return new Tier() {
            public int getUses() { return uses; }
            public float getSpeed() { return speed; }
            public float getAttackDamageBonus() { return attack; }
            public TagKey<Block> getIncorrectBlocksForDrops() { return incorrect; }
            public int getEnchantmentValue() { return enchantment; }
            public Ingredient getRepairIngredient() { return repair.get(); }
        };
    }
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "slavicmyths");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PERUNITE_ARMOR = ARMOR_MATERIALS.register("perunite", () -> new ArmorMaterial(
            Map.of(ArmorItem.Type.BOOTS, 2, ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.CHESTPLATE, 6, ArmorItem.Type.HELMET, 2),
            13, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ModItems.PERUNITE.get()),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("slavicmyths", "perunite"))), .5F, 0));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAMBESON = armor("gambeson", 4, 0, false);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PLATE = armor("plate", 7, 1, true);
    private static DeferredHolder<ArmorMaterial, ArmorMaterial> armor(String id, int defense, float toughness, boolean plate) {
        return ARMOR_MATERIALS.register(id, () -> new ArmorMaterial(
                Map.of(ArmorItem.Type.HELMET, defense, ArmorItem.Type.CHESTPLATE, defense, ArmorItem.Type.LEGGINGS, defense, ArmorItem.Type.BOOTS, defense),
                9, plate ? SoundEvents.ARMOR_EQUIP_IRON : SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(plate ? Items.IRON_INGOT : Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("slavicmyths", id))), toughness, plate ? .05F : 0));
    }
    private ModGear() { }
}
