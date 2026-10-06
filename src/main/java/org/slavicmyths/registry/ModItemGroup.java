package org.slavicmyths.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItemGroup {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "slavicmyths");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("slavicmyths", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.slavicmyths"))
            .icon(() -> new ItemStack(ModItems.BIRCH_BARK_SCROLL.get()))
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().stream()
                    .filter(entry -> entry != ModItems.BEAR_CUB_SPAWN_EGG && entry != ModItems.DOE_SPAWN_EGG)
                    .forEach(entry -> output.accept(entry.get())))
            .build());
    private ModItemGroup() { }
}
