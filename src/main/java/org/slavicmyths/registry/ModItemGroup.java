package org.slavicmyths.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.garden.Gardens;
import org.slavicmyths.rpg.RuneFoundation;

/** Standard NeoForge tabs. Existing tab IDs and category indices remain compatible. */
public final class ModItemGroup {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "slavicmyths");
    public static final String[] IDS = CreativeCatalog.TABS.toArray(String[]::new);
    private static final int[] DISPLAY_ORDER = {0, 1, 2, 3, 9, 4, 5, 6, 7, 8};
    public static final List<DeferredHolder<CreativeModeTab, CreativeModeTab>> THEMATIC = new ArrayList<>();
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB;
    private static final Set<String> REPORTED_FALLBACKS = ConcurrentHashMap.newKeySet();
    static {
        for (int index = 0; index < IDS.length; index++) {
            int category = index;
            int position = 0;
            while (DISPLAY_ORDER[position] != category) position++;
            int slot = position;
            THEMATIC.add(TABS.register(IDS[category], () -> {
                var builder = CreativeModeTab.builder(slot < 5 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM, slot % 5)
                        .title(Component.translatable("itemGroup.slavicmyths." + IDS[category]))
                        .icon(() -> new ItemStack(icon(category)))
                        .displayItems((parameters, output) -> stacks(category).forEach(output::accept));
                builder.withTabsBefore(slot == 0 ? ResourceLocation.fromNamespaceAndPath("minecraft", "spawn_eggs")
                        : ResourceLocation.fromNamespaceAndPath("slavicmyths", IDS[DISPLAY_ORDER[slot - 1]]));
                if (slot + 1 < DISPLAY_ORDER.length)
                    builder.withTabsAfter(ResourceLocation.fromNamespaceAndPath("slavicmyths", IDS[DISPLAY_ORDER[slot + 1]]));
                return builder.build();
            }));
        }
        TAB = THEMATIC.getFirst();
    }

    private static Item icon(int category) {
        return switch (category) {
            case 0 -> ModItems.LORE_BOOK.get();
            case 1 -> ModItems.SILVER_INGOT.get();
            case 2 -> ModItems.RYE_SEEDS.get();
            case 3 -> ModItems.KARAVAI.get();
            case 4 -> ModItems.SICKLE.get();
            case 5 -> ModItems.PERUNITE_CHESTPLATE.get();
            case 6 -> Gardens.ITEMS.get("apple_planks").get();
            case 7 -> ModItems.THUNDER_STONE.get();
            case 8 -> ModItems.DOMOVOY_SPAWN_EGG.get();
            case 9 -> ModItems.PERUNITE_SWORD.get();
            default -> throw new IllegalArgumentException("Unknown creative category " + category);
        };
    }

    public static int category(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        if (!key.getNamespace().equals("slavicmyths")) return -1;
        String path = key.getPath();
        if (CreativeCatalog.entry(path) != null) return CreativeCatalog.category(path);
        if (REPORTED_FALLBACKS.add(path))
            System.getLogger("slavicmyths.creative").log(System.Logger.Level.WARNING,
                    "Uncatalogued item {0}; add its category and progression to creative_catalog.tsv", key);
        return fallback(item);
    }

    private static int fallback(Item item) {
        if (item instanceof SpawnEggItem) return 8;
        if (item instanceof ArmorItem || item instanceof ShieldItem
                || item instanceof org.slavicmyths.textile.ClothingItem) return 5;
        if (item instanceof SwordItem || item instanceof BowItem || item instanceof CrossbowItem) return 9;
        if (item instanceof TieredItem) return 4;
        if (item instanceof ItemNameBlockItem) return 2;
        if (item.components().has(net.minecraft.core.component.DataComponents.FOOD)) return 3;
        if (item instanceof BlockItem || item instanceof SignItem || item instanceof HangingSignItem) return 6;
        return 0;
    }

    public static List<Item> items(int category) {
        if (category < 0 || category >= IDS.length) return List.of();
        // Include actual namespace entries from every registration module.
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("slavicmyths"))
                .filter(item -> category(item) == category)
                .sorted(Comparator.<Item>comparingInt(item -> CreativeCatalog.order(BuiltInRegistries.ITEM.getKey(item).getPath()))
                        .thenComparing(item -> BuiltInRegistries.ITEM.getKey(item).getPath()))
                .toList();
    }

    public static List<ItemStack> stacks(int category) {
        var result = new ArrayList<ItemStack>();
        // Preserve typed blank substrates beside their own tier, without duplicate default stacks.
        var blanks = category == 7 ? RuneFoundation.creative().stream()
                .filter(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().startsWith("blank_rune_"))
                .toList() : List.<ItemStack>of();
        for (var item : items(category)) {
            var variants = blanks.stream().filter(stack -> stack.getItem() == item).toList();
            if (variants.isEmpty()) result.add(new ItemStack(item));
            else variants.forEach(stack -> result.add(stack.copy()));
        }
        return result;
    }
    private ModItemGroup() { }
}
