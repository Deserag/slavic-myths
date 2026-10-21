package org.slavicmyths.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.poloz.PolozRewards;
import org.slavicmyths.gorynych.GorynychContent;

/** Equipment-event refresh only; transient modifiers never heal or accumulate on re-equip. */
public final class ArmorSetHealth {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static boolean perunite(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PERUNITE_HELMET.get())
            && entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.PERUNITE_CHESTPLATE.get())
            && entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.PERUNITE_LEGGINGS.get())
            && entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.PERUNITE_BOOTS.get());
    }
    public static boolean dragon(LivingEntity entity) {
        for (var slot : SLOTS) if (!entity.getItemBySlot(slot).is(GorynychContent.ARMOR.get(slot).get())) return false;
        return true;
    }
    public static void refresh(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        var health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        // Keep the existing Poloz modifier ID so existing saved characters cannot gain both bonuses.
        set(health, "poloz_set_health", PolozRewards.full(entity) ? 14 : 0);
        set(health, "perunite_set_health", perunite(entity) ? 5 : 0);
        set(health, "dragon_set_health", dragon(entity) ? 20 : 0);
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }
    private static void set(net.minecraft.world.entity.ai.attributes.AttributeInstance health, String name, double value) {
        var id = ResourceLocation.fromNamespaceAndPath("slavicmyths", name);
        var previous = health.getModifier(id);
        if (previous != null && previous.amount() == value && previous.operation() == AttributeModifier.Operation.ADD_VALUE) return;
        health.removeModifier(id);
        if (value > 0) health.addTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
    }
    public static String tooltip(ItemStack stack) {
        for (var slot : SLOTS) {
            if (stack.is(GorynychContent.ARMOR.get(slot).get())) return "armor_set_health.dragon";
        }
        if (stack.is(ModItems.PERUNITE_HELMET.get()) || stack.is(ModItems.PERUNITE_CHESTPLATE.get())
            || stack.is(ModItems.PERUNITE_LEGGINGS.get()) || stack.is(ModItems.PERUNITE_BOOTS.get())) return "armor_set_health.perunite";
        return null; // Poloz already supplies its existing full-set tooltip.
    }
    private ArmorSetHealth() { }
}
