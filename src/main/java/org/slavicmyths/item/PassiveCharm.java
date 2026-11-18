package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Existing damage protection, available with a shield in the offhand. */
public interface PassiveCharm extends ICurioItem {
    @Override
    default boolean canEquip(SlotContext context, ItemStack stack) {
        return context.identifier().equals("charm");
    }

    static boolean protects(LivingEntity entity, Item item) {
        if (entity.getOffhandItem().is(item)) return true;
        return !equipped(entity, item, "charm").isEmpty();
    }

    /** Read the current bounded slot, avoiding Curios' per-game-tick item search cache. */
    static ItemStack equipped(LivingEntity entity, Item item, String slot) {
        var inventory = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (inventory == null) return ItemStack.EMPTY;
        var handler = inventory.getStacksHandler(slot).orElse(null);
        if (handler == null) return ItemStack.EMPTY;
        var stacks = handler.getStacks();
        for (int index = 0; index < stacks.getSlots(); index++) {
            var stack = stacks.getStackInSlot(index);
            if (inventory.isSlotActive(slot, index) && stack.is(item)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
