package org.slavicmyths.combat;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** Special weapons reuse the ordinary melee attributes and saved item components. */
public final class RareWeapons {
    public static final DeferredHolder<Item, Item> TUGARIN = ModItems.ITEMS.register("tugarin_sword", () -> new SwordItem(Tiers.DIAMOND,
        new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 2, -2.2F))));
    public static final DeferredHolder<Item, Item> LIKHO = ModItems.ITEMS.register("likho_staff", LikhoStaffItem::new);
    public static final DeferredHolder<Item, Item> ATAMAN = ModItems.ITEMS.register("ataman_flail", () -> new MaceItem(Tiers.DIAMOND, 9, .7F, .22F, .65F, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> VICTOR = ModItems.ITEMS.register("victor_spear", () -> new org.slavicmyths.item.SpearItem(Tiers.DIAMOND, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> SIGN = ModItems.ITEMS.register("ataman_sign", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static void init() { }
    private RareWeapons() { }
}
