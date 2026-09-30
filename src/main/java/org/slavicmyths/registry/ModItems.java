package org.slavicmyths.registry;

import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Rarity;
import net.minecraft.item.*;
import net.minecraft.inventory.EquipmentSlotType;
import org.slavicmyths.item.ModGear;
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
    public static final RegistryObject<Item> ANCIENT_COIN = ITEMS.register("ancient_coin", () -> new org.slavicmyths.item.AncientCoinItem(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> IDOL_FRAGMENT = ITEMS.register("idol_fragment", () -> new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ANCIENT_SIGN = ITEMS.register("ancient_sign", () -> new org.slavicmyths.item.AncientSignItem(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LORE_BOOK = ITEMS.register("lore_book", () -> new org.slavicmyths.item.LoreBookItem(properties().stacksTo(1)));

    public static final RegistryObject<Item> SILVER_ORE = ITEMS.register("silver_ore", () -> new BlockItem(ModBlocks.SILVER_ORE.get(), properties()));
    public static final RegistryObject<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(properties()));
    public static final RegistryObject<Item> SILVER_NUGGET = ITEMS.register("silver_nugget", () -> new Item(properties()));
    public static final RegistryObject<Item> PERUNITE_SWORD = ITEMS.register("perunite_sword", () -> new SwordItem(ModGear.PERUNITE, 3, -2.4F, properties()));
    public static final RegistryObject<Item> PERUNITE_AXE = ITEMS.register("perunite_axe", () -> new AxeItem(ModGear.PERUNITE, 5.0F, -3.1F, properties()));
    public static final RegistryObject<Item> PERUNITE_PICKAXE = ITEMS.register("perunite_pickaxe", () -> new PickaxeItem(ModGear.PERUNITE, 1, -2.8F, properties()));
    public static final RegistryObject<Item> PERUNITE_SHOVEL = ITEMS.register("perunite_shovel", () -> new ShovelItem(ModGear.PERUNITE, 1.5F, -3.0F, properties()));
    public static final RegistryObject<Item> PERUNITE_HOE = ITEMS.register("perunite_hoe", () -> new HoeItem(ModGear.PERUNITE, -2, -1.0F, properties()));
    public static final RegistryObject<Item> PERUNITE_HELMET = ITEMS.register("perunite_helmet", () -> new ArmorItem(ModGear.PERUNITE_ARMOR, EquipmentSlotType.HEAD, properties()));
    public static final RegistryObject<Item> PERUNITE_CHESTPLATE = ITEMS.register("perunite_chestplate", () -> new ArmorItem(ModGear.PERUNITE_ARMOR, EquipmentSlotType.CHEST, properties()));
    public static final RegistryObject<Item> PERUNITE_LEGGINGS = ITEMS.register("perunite_leggings", () -> new ArmorItem(ModGear.PERUNITE_ARMOR, EquipmentSlotType.LEGS, properties()));
    public static final RegistryObject<Item> PERUNITE_BOOTS = ITEMS.register("perunite_boots", () -> new ArmorItem(ModGear.PERUNITE_ARMOR, EquipmentSlotType.FEET, properties()));
    public static final RegistryObject<Item> SILVER_SWORD = ITEMS.register("silver_sword", () -> new SwordItem(ModGear.SILVER, 3, -2.4F, properties()));
    public static final RegistryObject<Item> SILVER_DAGGER = ITEMS.register("silver_dagger", () -> new SwordItem(ModGear.SILVER, 1, -1.5F, properties()));
    public static final RegistryObject<Item> BONE_KNIFE = ITEMS.register("bone_knife", () -> new SwordItem(ModGear.BONE, 2, -1.5F, properties()));
    public static final RegistryObject<Item> RITUAL_KNIFE = ITEMS.register("ritual_knife", () -> new SwordItem(ModGear.SILVER, 1, -1.7F, properties()));
    public static final RegistryObject<Item> LINEN_CLOTH = ITEMS.register("linen_cloth", () -> new Item(properties()));
    public static final RegistryObject<Item> HERB_POUCH = ITEMS.register("herb_pouch", () -> new Item(properties()));
    public static final RegistryObject<Item> ST_JOHNS_WORT = ITEMS.register("st_johns_wort", () -> new BlockItem(ModBlocks.ST_JOHNS_WORT.get(), properties()));
    public static final RegistryObject<Item> NETTLE = ITEMS.register("nettle", () -> new BlockItem(ModBlocks.NETTLE.get(), properties()));
    public static final RegistryObject<Item> FIREWEED = ITEMS.register("fireweed", () -> new BlockItem(ModBlocks.FIREWEED.get(), properties()));
    public static final RegistryObject<Item> JUNIPER_BERRIES = ITEMS.register("juniper_berries", () -> new BlockItem(ModBlocks.JUNIPER_BERRIES.get(), properties()));
    public static final RegistryObject<Item> GROUND_WORMWOOD = ITEMS.register("ground_wormwood", () -> new Item(properties()));
    public static final RegistryObject<Item> GROUND_ST_JOHNS_WORT = ITEMS.register("ground_st_johns_wort", () -> new Item(properties()));
    public static final RegistryObject<Item> JUNIPER_BLEND = ITEMS.register("juniper_blend", () -> new Item(properties()));
    public static final RegistryObject<Item> HONEY_BREAD = ITEMS.register("honey_bread", () -> new Item(properties().food(new Food.Builder().nutrition(7).saturationMod(0.6F).build())));
    public static final RegistryObject<Item> HONEY_BAKED_APPLE = ITEMS.register("honey_baked_apple", () -> new Item(properties().food(new Food.Builder().nutrition(6).saturationMod(0.5F).build())));
    public static final RegistryObject<Item> RITUAL_BOWL = ITEMS.register("ritual_bowl", () -> new Item(properties()));
    public static final RegistryObject<Item> THUNDER_AXE = ITEMS.register("thunder_axe", () -> new org.slavicmyths.item.ThunderAxeItem(ModGear.PERUNITE, properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PERUN_CHARM = ITEMS.register("perun_charm", () -> new org.slavicmyths.item.PerunCharmItem(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CARVED_OAK_PILLAR = ITEMS.register("carved_oak_pillar", () -> new BlockItem(ModBlocks.CARVED_OAK_PILLAR.get(), properties()));
    public static final RegistryObject<Item> CARVED_BIRCH_PLANKS = ITEMS.register("carved_birch_planks", () -> new BlockItem(ModBlocks.CARVED_BIRCH_PLANKS.get(), properties()));
    public static final RegistryObject<Item> CARVED_OAK_BLOCK = ITEMS.register("carved_oak_block", () -> new BlockItem(ModBlocks.CARVED_OAK_BLOCK.get(), properties()));
    public static final RegistryObject<Item> STRAW_BLOCK = ITEMS.register("straw_block", () -> new BlockItem(ModBlocks.STRAW_BLOCK.get(), properties()));
    public static final RegistryObject<Item> AMBER = ITEMS.register("amber", () -> new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> AMBER_BLOCK = ITEMS.register("amber_block", () -> new BlockItem(ModBlocks.AMBER_BLOCK.get(), properties()));
    public static final RegistryObject<Item> AMBER_CHARM = ITEMS.register("amber_charm", () -> new org.slavicmyths.item.AmberCharmItem(properties()));
    public static final RegistryObject<Item> CARVED_STAFF = ITEMS.register("carved_staff", () -> new SwordItem(ItemTier.WOOD, 2, -2.8F, properties()));
    public static final RegistryObject<Item> STORM_STAFF = ITEMS.register("storm_staff", () -> new org.slavicmyths.item.StormStaffItem(properties().stacksTo(1).durability(240).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CLUB = ITEMS.register("club", () -> new org.slavicmyths.item.ClubItem(properties()));
    public static final RegistryObject<Item> BATTLE_AXE = ITEMS.register("battle_axe", () -> new AxeItem(ItemTier.IRON, 7.0F, -3.3F, properties()));
    public static final RegistryObject<Item> SPEAR = ITEMS.register("spear", () -> new org.slavicmyths.item.SpearItem(ItemTier.STONE, properties()));
    public static final RegistryObject<Item> SILVER_SPEAR = ITEMS.register("silver_spear", () -> new org.slavicmyths.item.SpearItem(ModGear.SILVER, properties()));
    public static final RegistryObject<Item> RITUAL_CHARCOAL = ITEMS.register("ritual_charcoal", () -> new Item(properties()));
    public static final RegistryObject<Item> RITUAL_CANDLE = ITEMS.register("ritual_candle", () -> new BlockItem(ModBlocks.RITUAL_CANDLE.get(), properties()));
    public static final RegistryObject<Item> HONEY_FLATBREAD = ITEMS.register("honey_flatbread", () -> new Item(properties().food(new Food.Builder().nutrition(6).saturationMod(0.7F).build())));
    public static final RegistryObject<Item> BERRY_MORS = ITEMS.register("berry_mors", () -> new org.slavicmyths.item.BerryMorsItem(properties().stacksTo(1).food(new Food.Builder().nutrition(3).saturationMod(0.3F).alwaysEat().build())));
    public static final RegistryObject<Item> FOREST_MIX = ITEMS.register("forest_mix", () -> new Item(properties().food(new Food.Builder().nutrition(4).saturationMod(0.3F).build())));
    public static final RegistryObject<Item> WALL_CARVING = ITEMS.register("wall_carving", () -> new BlockItem(ModBlocks.WALL_CARVING.get(), properties()));
    public static final RegistryObject<Item> FOREST_CHARM = ITEMS.register("forest_charm", () -> new org.slavicmyths.item.SimpleCharmItem("forest_charm", properties()));
    public static final RegistryObject<Item> HUNTER_CHARM = ITEMS.register("hunter_charm", () -> new org.slavicmyths.item.SimpleCharmItem("hunter_charm", properties()));
    public static final RegistryObject<Item> TRAVELER_CHARM = ITEMS.register("traveler_charm", () -> new org.slavicmyths.item.SimpleCharmItem("traveler_charm", properties()));
    public static final RegistryObject<Item> RETAINER_SHIELD = ITEMS.register("retainer_shield", () -> new org.slavicmyths.item.MythShieldItem(properties().durability(280)));
    public static final RegistryObject<Item> PERUNITE_SHIELD = ITEMS.register("perunite_shield", () -> new org.slavicmyths.item.MythShieldItem(properties().durability(420).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THUNDER_SPEAR = ITEMS.register("thunder_spear", () -> new org.slavicmyths.item.HeavyWeaponItem(ModGear.PERUNITE, 4, -2.9F, 0.18, properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MACE = ITEMS.register("mace", () -> new org.slavicmyths.item.HeavyWeaponItem(ItemTier.IRON, 5, -3.2F, 0.35, properties()));
    public static final RegistryObject<Item> PERUNITE_MACE = ITEMS.register("perunite_mace", () -> new org.slavicmyths.item.HeavyWeaponItem(ModGear.PERUNITE, 6, -3.4F, 0.4, properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BERDYSH = ITEMS.register("berdysh", () -> new org.slavicmyths.item.HeavyWeaponItem(ItemTier.IRON, 6, -3.3F, 0, properties()));
    public static final RegistryObject<Item> WEAPON_WRAP = ITEMS.register("weapon_wrap", () -> new Item(properties()));
    public static final RegistryObject<Item> SILVER_FITTING = ITEMS.register("silver_fitting", () -> new Item(properties()));

    public static final RegistryObject<Item> KIKIMORA_LOCK = ITEMS.register("kikimora_lock", () -> new Item(properties()));
    public static final RegistryObject<Item> NOON_EAR = ITEMS.register("noon_ear", () -> new Item(properties()));
    public static final RegistryObject<Item> FIELD_BUNDLE = ITEMS.register("field_bundle", () -> new Item(properties()));
    public static final RegistryObject<Item> BATH_BROOM = ITEMS.register("bath_broom", () -> new Item(properties()));
    public static final RegistryObject<Item> BATH_STONE = ITEMS.register("bath_stone", () -> new Item(properties()));
    public static final RegistryObject<Item> OLD_BUTTON = ITEMS.register("old_button", () -> new Item(properties()));
    public static final RegistryObject<Item> OVINNIK_CLAW = ITEMS.register("ovinnik_claw", () -> new Item(properties()));
    public static final RegistryObject<Item> EMBER_HEART = ITEMS.register("ember_heart", () -> new Item(properties()));
    public static final RegistryObject<Item> NOON_SICKLE = ITEMS.register("noon_sickle", () -> new SwordItem(ModGear.SILVER, 3, -2.5F, properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> KIKIMORA_SPAWN_EGG = ITEMS.register("kikimora_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.KIKIMORA, 0x494137, 0xbca981, properties()));
    public static final RegistryObject<Item> POLUDNITSA_SPAWN_EGG = ITEMS.register("poludnitsa_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.POLUDNITSA, 0x494137, 0xbca981, properties()));
    public static final RegistryObject<Item> POLEVIK_SPAWN_EGG = ITEMS.register("polevik_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.POLEVIK, 0x494137, 0xbca981, properties()));
    public static final RegistryObject<Item> BANNIK_SPAWN_EGG = ITEMS.register("bannik_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.BANNIK, 0x494137, 0xbca981, properties()));
    public static final RegistryObject<Item> IGOSHA_SPAWN_EGG = ITEMS.register("igosha_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.IGOSHA, 0x494137, 0xbca981, properties()));
    public static final RegistryObject<Item> OVINNIK_SPAWN_EGG = ITEMS.register("ovinnik_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.OVINNIK, 0x494137, 0xbca981, properties()));
    public static final RegistryObject<Item> BATH_STOVE = ITEMS.register("bath_stove", () -> new BlockItem(ModBlocks.BATH_STOVE.get(), properties()));
    public static final RegistryObject<Item> WOODEN_TUB = ITEMS.register("wooden_tub", () -> new BlockItem(ModBlocks.WOODEN_TUB.get(), properties()));

    public static final RegistryObject<Item> PATH_STONE=ITEMS.register("path_stone",()->new BlockItem(ModBlocks.PATH_STONE.get(),properties()));
    public static final RegistryObject<Item> RUNIC_ANVIL=ITEMS.register("runic_anvil",()->new BlockItem(ModBlocks.RUNIC_ANVIL.get(),properties()));
    public static final RegistryObject<Item> RUNE_THUNDER=ITEMS.register("rune_thunder",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_HEAT=ITEMS.register("rune_heat",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_FOREST=ITEMS.register("rune_forest",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_MIDDAY=ITEMS.register("rune_midday",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_SHADOW=ITEMS.register("rune_shadow",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_PROTECTION=ITEMS.register("rune_protection",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_LIFE=ITEMS.register("rune_life",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUNE_WIND=ITEMS.register("rune_wind",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    private ModItems() { }
}
