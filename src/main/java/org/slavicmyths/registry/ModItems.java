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
    public static final RegistryObject<Item> MACE = ITEMS.register("mace", () -> new org.slavicmyths.combat.MaceItem(ItemTier.IRON, 8, .9F, .35F, .45F, properties()));
    public static final RegistryObject<Item> PERUNITE_MACE = ITEMS.register("perunite_mace", () -> new org.slavicmyths.combat.MaceItem(ModGear.PERUNITE, 9.5F, .6F, .4F, .4F, properties().rarity(Rarity.RARE)));
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
    public static final RegistryObject<Item> RAW_BEAR_MEAT=food("raw_bear_meat",4,.25F);
    public static final RegistryObject<Item> COOKED_BEAR_MEAT=food("cooked_bear_meat",9,.8F);
    public static final RegistryObject<Item> RAW_VENISON=food("raw_venison",3,.25F);
    public static final RegistryObject<Item> COOKED_VENISON=food("cooked_venison",8,.75F);
    public static final RegistryObject<Item> RAW_BOAR_MEAT=food("raw_boar_meat",3,.3F);
    public static final RegistryObject<Item> COOKED_BOAR_MEAT=food("cooked_boar_meat",8,.8F);
    public static final RegistryObject<Item> LARGE_ANIMAL_BONE=ITEMS.register("large_animal_bone",()->new Item(properties()));
    public static final RegistryObject<Item> BONE_ARROW=ITEMS.register("bone_arrow",()->new ArrowItem(properties()));
    public static final RegistryObject<Item> FLOUR=ITEMS.register("flour",()->new Item(properties()));
    public static final RegistryObject<Item> RASPBERRY=ITEMS.register("raspberry",()->new org.slavicmyths.item.FolkBerryItem(properties().food(new Food.Builder().nutrition(2).saturationMod(.15F).build()),true));
    public static final RegistryObject<Item> BLUEBERRY=ITEMS.register("blueberry",()->new org.slavicmyths.item.FolkBerryItem(properties().food(new Food.Builder().nutrition(2).saturationMod(.15F).build()),false));
    public static final RegistryObject<Item> PANCAKES=food("pancakes",5,.5F);
    public static final RegistryObject<Item> RASPBERRY_PANCAKES=food("raspberry_pancakes",7,.65F);
    public static final RegistryObject<Item> BLUEBERRY_PANCAKES=food("blueberry_pancakes",7,.65F);
    public static final RegistryObject<Item> MEAT_PANCAKES=food("meat_pancakes",9,.8F);
    public static final RegistryObject<Item> KARAVAI=food("karavai",10,.9F);
    public static final RegistryObject<Item> BERRY_PIE=food("berry_pie",8,.7F);
    public static final RegistryObject<Item> BAKED_APPLE=food("baked_apple",6,.55F);
    public static final RegistryObject<Item> MUSHROOM_STEW=stew("mushroom_stew",7,.6F);
    public static final RegistryObject<Item> BEEF_STEW=stew("beef_stew",10,.9F);
    public static final RegistryObject<Item> PORK_STEW=stew("pork_stew",10,.9F);
    public static final RegistryObject<Item> VENISON_STEW=stew("venison_stew",10,.9F);
    public static final RegistryObject<Item> BEAR_STEW=stew("bear_stew",11,1F);
    public static final RegistryObject<Item> BROWN_BEAR_SPAWN_EGG=egg("brown_bear_spawn_egg",ModEntities.BROWN_BEAR,0x4b3427,0xb28a62);
    public static final RegistryObject<Item> BEAR_CUB_SPAWN_EGG=egg("bear_cub_spawn_egg",ModEntities.BEAR_CUB,0x6b4a35,0xcda578);
    public static final RegistryObject<Item> FOREST_WOLF_SPAWN_EGG=egg("forest_wolf_spawn_egg",ModEntities.FOREST_WOLF,0x292b2e,0xb2a98f);
    public static final RegistryObject<Item> BOAR_SPAWN_EGG=egg("boar_spawn_egg",ModEntities.BOAR,0x493a32,0xd5c09c);
    public static final RegistryObject<Item> STAG_SPAWN_EGG=egg("stag_spawn_egg",ModEntities.STAG,0x76533b,0xd1ad7b);
    public static final RegistryObject<Item> DOE_SPAWN_EGG=egg("doe_spawn_egg",ModEntities.DOE,0x9a7653,0xe0c29c);
    private static RegistryObject<Item> food(String id,int nutrition,float saturation){return ITEMS.register(id,()->new Item(properties().food(new Food.Builder().nutrition(nutrition).saturationMod(saturation).build())));}
    private static RegistryObject<Item> stew(String id,int nutrition,float saturation){return ITEMS.register(id,()->new SoupItem(properties().stacksTo(1).food(new Food.Builder().nutrition(nutrition).saturationMod(saturation).build())));}
    private static RegistryObject<Item> egg(String id,RegistryObject<? extends net.minecraft.entity.EntityType<?>> type,int primary,int secondary){return ITEMS.register(id,()->new net.minecraftforge.common.ForgeSpawnEggItem(type,primary,secondary,properties()));}
    public static final RegistryObject<Item> INVISIBILITY_CAP=ITEMS.register("invisibility_cap",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.RARE),"head"));
    public static final RegistryObject<Item> RETRIBUTION_CHARM=ITEMS.register("retribution_charm",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.RARE),"charm"));
    public static final RegistryObject<Item> PERUN_RING=ITEMS.register("perun_ring",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.RARE),"ring"));
    public static final RegistryObject<Item> VELES_AMULET=ITEMS.register("veles_amulet",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.UNCOMMON),"necklace"));
    public static final RegistryObject<Item> HUNTER_BELT=ITEMS.register("hunter_belt",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.UNCOMMON),"belt"));
    public static final RegistryObject<Item> RESIN_RING=ITEMS.register("resin_ring",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.UNCOMMON),"ring"));
    public static final RegistryObject<Item> SEVEN_LEAGUE_BOOTS=ITEMS.register("seven_league_boots",()->new org.slavicmyths.item.SevenLeagueBoots(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> KITCHEN_TABLE=ITEMS.register("kitchen_table",()->new BlockItem(ModBlocks.KITCHEN_TABLE.get(),properties()));
    public static final RegistryObject<Item> ROLLING_PIN=ITEMS.register("rolling_pin",()->new Item(properties().durability(128)));
    public static final RegistryObject<Item> METAL_POT=ITEMS.register("metal_pot",()->new Item(properties().durability(256)));
    public static final RegistryObject<Item> FLYING_BROOM=ITEMS.register("flying_broom",()->new org.slavicmyths.flight.FlightItem(properties().rarity(Rarity.RARE),false));
    public static final RegistryObject<Item> FLYING_MORTAR=ITEMS.register("flying_mortar",()->new org.slavicmyths.flight.FlightItem(properties().rarity(Rarity.RARE),true));
    public static final RegistryObject<Item> PESTLE=ITEMS.register("pestle",()->new Item(properties().stacksTo(1)));
    public static final RegistryObject<Item> GUSLI=ITEMS.register("gusli",()->new org.slavicmyths.artifact.GusliItem(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SKATERT=ITEMS.register("skatert",()->new org.slavicmyths.artifact.SkatertItem(properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BADNYAK=ITEMS.register("badnyak",()->new org.slavicmyths.artifact.BadnyakItem(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> VELES_STAFF=ITEMS.register("veles_staff",()->new org.slavicmyths.artifact.VelesStaffItem(properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> RAW_PIKE=food("raw_pike",3,.2F),COOKED_PIKE=food("cooked_pike",7,.7F),RAW_CARP=food("raw_carp",3,.2F),COOKED_CARP=food("cooked_carp",6,.65F),SMOKED_CARP=food("smoked_carp",8,.8F),RAW_CRAYFISH=food("raw_crayfish",2,.1F),COOKED_CRAYFISH=food("cooked_crayfish",5,.6F),UKHA=stew("ukha",10,.8F);
    public static final RegistryObject<Item> OLD_HOOK=ITEMS.register("old_hook",()->new Item(properties()));
    public static final RegistryObject<Item> PEARL_FRAGMENT=ITEMS.register("pearl_fragment",()->new Item(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PIKE_SPAWN_EGG=egg("pike_spawn_egg",ModEntities.PIKE,0x53623b,0xbaad78),CARP_SPAWN_EGG=egg("carp_spawn_egg",ModEntities.CARP,0x9b753a,0xd0ae64),CRAYFISH_SPAWN_EGG=egg("crayfish_spawn_egg",ModEntities.CRAYFISH,0x414333,0x777750);
    public static final RegistryObject<Item> FISHING_NET=ITEMS.register("fishing_net",()->new BlockItem(ModBlocks.FISHING_NET.get(),properties().durability(32)));
    public static final RegistryObject<Item> REED=ITEMS.register("reed",()->new BlockItem(ModBlocks.REED.get(),properties()));
    public static final RegistryObject<Item> WATER_GRASS=ITEMS.register("water_grass",()->new BlockItem(ModBlocks.WATER_GRASS.get(),properties()));
    public static final RegistryObject<Item> WHITE_LILY=ITEMS.register("white_lily",()->new net.minecraft.item.LilyPadItem(ModBlocks.WHITE_LILY.get(),properties()));
    public static final RegistryObject<Item> VODYANOY_SPAWN_EGG=egg("vodyanoy_spawn_egg",ModEntities.VODYANOY,0x59695d,0x8f9364),RUSALKA_SPAWN_EGG=egg("rusalka_spawn_egg",ModEntities.RUSALKA,0xc3d1c7,0x3e5749);
    public static final RegistryObject<Item> POOL_PEARL=ITEMS.register("pool_pearl",()->new Item(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ANCIENT_WATER_SIGN=ITEMS.register("ancient_water_sign",()->new org.slavicmyths.depth.WaterSignItem(properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> POOL_SPEAR=ITEMS.register("pool_spear",()->new org.slavicmyths.depth.PoolSpear(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DEPTH_AMULET=ITEMS.register("depth_amulet",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.RARE),"necklace"));
    public static final RegistryObject<Item> VODYANOY_NET=ITEMS.register("vodyanoy_net",()->new org.slavicmyths.depth.VodyanoyNetItem(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> POOL_STONE=ITEMS.register("pool_stone",()->new BlockItem(ModBlocks.POOL_STONE.get(),properties()));
    public static final RegistryObject<Item> ELDER_VODYANOY_SPAWN_EGG=egg("elder_vodyanoy_spawn_egg",ModEntities.ELDER_VODYANOY,0x3f5149,0x889273);
    public static final RegistryObject<Item> ARMORER_TABLE=ITEMS.register("armorer_table",()->new BlockItem(ModBlocks.ARMORER_TABLE.get(),properties()));
    public static final RegistryObject<Item> IRON_RINGS=ITEMS.register("iron_rings",()->new Item(properties()));
    public static final RegistryObject<Item> WOODEN_MACE=ITEMS.register("wooden_mace",()->new org.slavicmyths.combat.MaceItem(ItemTier.WOOD,5,1.05F,.2F,.3F,properties()));
    public static final RegistryObject<Item> STONE_MACE=ITEMS.register("stone_mace",()->new org.slavicmyths.combat.MaceItem(ItemTier.STONE,7,.85F,.3F,.4F,properties()));
    public static final RegistryObject<Item> SILVER_MACE=ITEMS.register("silver_mace",()->new org.slavicmyths.combat.MaceItem(ModGear.SILVER,7,1F,.3F,.4F,properties()));
    public static final RegistryObject<Item> FLAIL=ITEMS.register("flail",()->new org.slavicmyths.combat.FlailItem(properties()));
    public static final RegistryObject<Item> GAMBESON=ITEMS.register("gambeson",()->new ArmorItem(org.slavicmyths.combat.CombatEquipment.GAMBESON,EquipmentSlotType.CHEST,properties()));
    public static final RegistryObject<Item> PLATE_CUIRASS=ITEMS.register("plate_cuirass",()->new org.slavicmyths.combat.CombatEquipment.PlateArmor(properties()));
    public static final RegistryObject<Item> BANDIT_FIGHTER_EGG=egg("bandit_fighter_spawn_egg",ModEntities.BANDIT_FIGHTER,0x574434,0xb39978);
    public static final RegistryObject<Item> BANDIT_ARCHER_EGG=egg("bandit_archer_spawn_egg",ModEntities.BANDIT_ARCHER,0x485343,0xb39978);
    public static final RegistryObject<Item> BANDIT_HEAVY_EGG=egg("bandit_heavy_spawn_egg",ModEntities.BANDIT_HEAVY,0x474a49,0xb39978);
    public static final RegistryObject<Item> BANDIT_SENIOR_EGG=egg("bandit_senior_spawn_egg",ModEntities.BANDIT_SENIOR,0x66503b,0xb39978);
    public static final RegistryObject<Item> ATAMAN_EGG=egg("ataman_spawn_egg",ModEntities.ATAMAN,0x653d36,0xa5a199);
    public static final RegistryObject<Item> NIGHTINGALE_MARK=ITEMS.register("nightingale_mark",()->new Item(properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> NIGHTINGALE_LOCK=ITEMS.register("nightingale_lock",()->new Item(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> NIGHTINGALE_DAGGER=ITEMS.register("nightingale_dagger",()->new org.slavicmyths.bandit.NightingaleDagger(properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> NIGHTINGALE_WHISTLE=ITEMS.register("nightingale_whistle",()->new org.slavicmyths.bandit.NightingaleWhistle(properties().stacksTo(1).rarity(Rarity.RARE),false));
    public static final RegistryObject<Item> BANDIT_HORN=ITEMS.register("bandit_horn",()->new org.slavicmyths.bandit.NightingaleWhistle(properties().stacksTo(1),true));
    public static final RegistryObject<Item> BURIAL_LOG_COFFIN=ITEMS.register("burial_log_coffin",()->new BlockItem(ModBlocks.BURIAL_COFFIN.get(),properties()));
    public static final RegistryObject<Item> ANCIENT_CAROLINGIAN_SWORD=ITEMS.register("ancient_carolingian_sword",()->new org.slavicmyths.kurgan.BurialWeapon(false,true,properties()));
    public static final RegistryObject<Item> RESTORED_CAROLINGIAN_SWORD=ITEMS.register("restored_carolingian_sword",()->new org.slavicmyths.kurgan.BurialWeapon(false,false,properties()));
    public static final RegistryObject<Item> ANCIENT_SPEAR=ITEMS.register("ancient_spear",()->new org.slavicmyths.kurgan.BurialWeapon(true,true,properties()));
    public static final RegistryObject<Item> RESTORED_SPEAR=ITEMS.register("restored_spear",()->new org.slavicmyths.kurgan.BurialWeapon(true,false,properties()));
    public static final RegistryObject<Item> ANCIENT_CHEKAN=ITEMS.register("ancient_chekan",()->new org.slavicmyths.combat.MaceItem(ItemTier.STONE,5,1.2F,.22F,.12F,properties().durability(90)));
    public static final RegistryObject<Item> CHEKAN=ITEMS.register("chekan",()->new org.slavicmyths.combat.MaceItem(ItemTier.IRON,6,1.3F,.3F,.15F,properties().durability(400)));
    public static final RegistryObject<Item> LUNULA=ITEMS.register("lunula",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.UNCOMMON),"necklace"));
    public static final RegistryObject<Item> GRIVNA=ITEMS.register("grivna",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.UNCOMMON),"necklace"));
    public static final RegistryObject<Item> GRAVE_WARD=ITEMS.register("grave_ward",()->new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.UNCOMMON),"charm"));
    public static final RegistryObject<Item> ANCIENT_FIBULA=ITEMS.register("ancient_fibula",()->new Item(properties()));
    public static final RegistryObject<Item> ANCIENT_COMB=ITEMS.register("ancient_comb",()->new Item(properties()));
    public static final RegistryObject<Item> ANCIENT_BEADS=ITEMS.register("ancient_beads",()->new Item(properties()));
    public static final RegistryObject<Item> OLD_BUCKLE=ITEMS.register("old_buckle",()->new Item(properties()));
    public static final RegistryObject<Item> POTTERY_FRAGMENT=ITEMS.register("pottery_fragment",()->new Item(properties()));
    public static final RegistryObject<Item> OLD_ARROWHEAD=ITEMS.register("old_arrowhead",()->new Item(properties()));
    private ModItems() { }
}
