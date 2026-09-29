package org.slavicmyths.registry;

import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;

/** Content registration only; future player mechanics belong to separate systems. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SlavicMyths.MOD_ID);

    public static final RegistryObject<Item> BIRCH_BARK_SCROLL = ITEMS.register(
            "birch_bark_scroll", () -> new Item(properties()));
    public static final RegistryObject<Item> THUNDER_STONE = ITEMS.register(
            "thunder_stone", () -> new Item(properties()
                    .rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WARDING_CHARM = ITEMS.register(
            "warding_charm", () -> new Item(properties()));

    public static final RegistryObject<Item> BIRCH_BARK = ITEMS.register("birch_bark", () -> new Item(properties()));
    public static final RegistryObject<Item> LINEN_THREAD = ITEMS.register("linen_thread", () -> new Item(properties()));
    public static final RegistryObject<Item> PERUNITE = ITEMS.register("perunite", () -> new Item(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FERN_FLOWER = ITEMS.register("fern_flower", () -> new Item(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FLAX = ITEMS.register("flax", () -> new BlockItem(ModBlocks.FLAX.get(), properties()));
    public static final RegistryObject<Item> WORMWOOD = ITEMS.register("wormwood", () -> new BlockItem(ModBlocks.WORMWOOD.get(), properties()));
    public static final RegistryObject<Item> PERUNITE_ORE = ITEMS.register("perunite_ore", () -> new BlockItem(ModBlocks.PERUNITE_ORE.get(), properties()));
    public static final RegistryObject<Item> LESHY_HEART = ITEMS.register("leshy_heart", () -> new Item(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DOMOVOY_SPAWN_EGG = ITEMS.register("domovoy_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.DOMOVOY, 0x77563D, 0xD2C0A2, properties()));
    public static final RegistryObject<Item> LESHY_SPAWN_EGG = ITEMS.register("leshy_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.LESHY, 0x343F2B, 0xC5DC76, properties()));

    private static Item.Properties properties() {
        return new Item.Properties().tab(ModItemGroup.TAB);
    }

    public static final RegistryObject<Item> HEARTH = ITEMS.register("hearth", () -> new BlockItem(ModBlocks.HEARTH.get(), properties()));
    public static final RegistryObject<Item> ANCIENT_IDOL = ITEMS.register("ancient_idol", () -> new BlockItem(ModBlocks.ANCIENT_IDOL.get(), properties()));
    public static final RegistryObject<Item> ALTAR = ITEMS.register("altar", () -> new BlockItem(ModBlocks.ALTAR.get(), properties()));
    public static final RegistryObject<Item> ANCIENT_COIN = ITEMS.register("ancient_coin", () -> new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> IDOL_FRAGMENT = ITEMS.register("idol_fragment", () -> new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ANCIENT_SIGN = ITEMS.register("ancient_sign", () -> new org.slavicmyths.item.AncientSignItem(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LORE_BOOK = ITEMS.register("lore_book", () -> new org.slavicmyths.item.LoreBookItem(properties().stacksTo(1)));

    private ModItems() { }
}
