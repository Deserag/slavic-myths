package org.slavicmyths.item;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import org.slavicmyths.registry.ModItems;

public final class ModGear {
    public static final IItemTier PERUNITE = new IItemTier() {
        public int getUses() { return 850; }
        public float getSpeed() { return 7.0F; }
        public float getAttackDamageBonus() { return 2.5F; }
        public int getLevel() { return 2; }
        public int getEnchantmentValue() { return 13; }
        public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.PERUNITE.get()); }
    };
    public static final IItemTier SILVER = new IItemTier() {
        public int getUses() { return 220; }
        public float getSpeed() { return 6.0F; }
        public float getAttackDamageBonus() { return 2.0F; }
        public int getLevel() { return 2; }
        public int getEnchantmentValue() { return 18; }
        public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.SILVER_INGOT.get()); }
    };
    public static final IItemTier BONE = new IItemTier() {
        public int getUses() { return 75; }
        public float getSpeed() { return 4.0F; }
        public float getAttackDamageBonus() { return 0.0F; }
        public int getLevel() { return 0; }
        public int getEnchantmentValue() { return 5; }
        public Ingredient getRepairIngredient() { return Ingredient.of(net.minecraft.item.Items.BONE); }
    };
    public static final IArmorMaterial PERUNITE_ARMOR = new IArmorMaterial() {
        private final int[] defense = {2, 5, 6, 2};
        public int getDurabilityForSlot(EquipmentSlotType slot) { return new int[]{13, 15, 16, 11}[slot.getIndex()] * 23; }
        public int getDefenseForSlot(EquipmentSlotType slot) { return defense[slot.getIndex()]; }
        public int getEnchantmentValue() { return 13; }
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
        public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.PERUNITE.get()); }
        public String getName() { return "slavicmyths:perunite"; }
        public float getToughness() { return 0.5F; }
        public float getKnockbackResistance() { return 0; }
    };
    private ModGear() { }
}
