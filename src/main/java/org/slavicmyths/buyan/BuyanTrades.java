package org.slavicmyths.buyan;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.marine.MarineRewards;
import org.slavicmyths.village.VillageRoles;

/** Additional island-only listings. Invoked at the same points as native Villager.updateTrades. */
public final class BuyanTrades {
    private BuyanTrades() {}

    public static boolean populate(Villager villager) {
        String role = villager.getData(VillageRoles.BUYAN_ROLE);
        if (role.isEmpty()) return false;
        int level = Math.max(1, Math.min(5, villager.getVillagerData().getLevel()));
        MerchantOffers offers = villager.getOffers();
        switch (role) {
            case "zemledelec" -> farmer(offers, level);
            case "rybak" -> fisherman(offers, level);
            case "kuznec" -> smith(offers, level);
            case "tkach" -> weaver(offers, level);
            case "travnik" -> herbalist(offers, level);
            case "kupec" -> merchant(offers, level);
            case "knyaz" -> prince(offers, level);
            case "obychny" -> { }
            default -> { return false; }
        }
        return true;
    }

    private static void buy(MerchantOffers offers, Item item, int items, int coins, int level) {
        offers.add(new MerchantOffer(new ItemCost(item, items),
            new ItemStack(ModItems.ANCIENT_COIN.get(), coins), level >= 4 ? 8 : 16,
            xp(level), 0.05F));
    }

    private static void sell(MerchantOffers offers, Item item, int count, int coins, int level) {
        offers.add(new MerchantOffer(new ItemCost(ModItems.ANCIENT_COIN.get(), coins),
            new ItemStack(item, count), level >= 4 ? 6 : 12,
            xp(level), 0.05F));
    }

    private static int xp(int level) {
        return switch (level) { case 1 -> 2; case 2 -> 5; case 3 -> 10; case 4 -> 15; default -> 30; };
    }

    private static void farmer(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.WHEAT, 18, 2, l); sell(o, Items.BREAD, 4, 1, l); }
            case 2 -> { buy(o, Items.POTATO, 16, 2, l); sell(o, Items.PUMPKIN_PIE, 3, 2, l); }
            case 3 -> { buy(o, Items.CARROT, 16, 2, l); sell(o, Items.HAY_BLOCK, 5, 3, l); }
            case 4 -> { buy(o, Items.BEETROOT, 20, 3, l); sell(o, Items.GOLDEN_CARROT, 4, 5, l); }
            case 5 -> { buy(o, Items.MELON, 16, 3, l); sell(o, Items.GOLDEN_APPLE, 1, 18, l); }
        }
    }

    private static void fisherman(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.COD, 14, 2, l); sell(o, Items.COOKED_COD, 4, 1, l); }
            case 2 -> { buy(o, Items.SALMON, 12, 2, l); sell(o, Items.COOKED_SALMON, 4, 2, l); }
            case 3 -> { buy(o, Items.INK_SAC, 10, 3, l); sell(o, Items.FISHING_ROD, 1, 4, l); }
            case 4 -> { buy(o, Items.NAUTILUS_SHELL, 2, 4, l); sell(o, Items.TURTLE_SCUTE, 2, 8, l); }
            case 5 -> { buy(o, Items.HEART_OF_THE_SEA, 1, 24, l); sell(o, Items.NAME_TAG, 1, 8, l); }
        }
    }

    private static void smith(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.IRON_INGOT, 6, 2, l); sell(o, Items.IRON_PICKAXE, 1, 5, l); }
            case 2 -> { buy(o, Items.COAL, 24, 2, l); sell(o, Items.IRON_SWORD, 1, 5, l); }
            case 3 -> { buy(o, Items.COPPER_INGOT, 20, 2, l); sell(o, Items.SHIELD, 1, 7, l); }
            case 4 -> { buy(o, Items.GOLD_INGOT, 8, 5, l); sell(o, Items.IRON_CHESTPLATE, 1, 11, l); }
            case 5 -> { buy(o, Items.DIAMOND, 2, 8, l); sell(o, Items.DIAMOND_PICKAXE, 1, 24, l); }
        }
    }

    private static void weaver(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.WHITE_WOOL, 12, 2, l); sell(o, Items.WHITE_CARPET, 8, 1, l); }
            case 2 -> { buy(o, Items.STRING, 18, 2, l); sell(o, Items.LOOM, 1, 4, l); }
            case 3 -> { buy(o, Items.BLUE_WOOL, 12, 3, l); sell(o, Items.RED_BANNER, 1, 4, l); }
            case 4 -> { buy(o, Items.LEATHER, 12, 3, l); sell(o, Items.PAINTING, 2, 4, l); }
            case 5 -> { buy(o, Items.RABBIT_HIDE, 16, 5, l); sell(o, Items.BLUE_BED, 1, 7, l); }
        }
    }

    private static void herbalist(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.RED_MUSHROOM, 12, 2, l); sell(o, Items.GLASS_BOTTLE, 8, 1, l); }
            case 2 -> { buy(o, Items.BROWN_MUSHROOM, 12, 2, l); sell(o, Items.HONEY_BOTTLE, 2, 3, l); }
            case 3 -> { buy(o, Items.NETHER_WART, 12, 3, l); sell(o, Items.GLOWSTONE_DUST, 6, 4, l); }
            case 4 -> { buy(o, Items.FERMENTED_SPIDER_EYE, 8, 4, l); sell(o, Items.GHAST_TEAR, 1, 12, l); }
            case 5 -> { buy(o, Items.BLAZE_ROD, 8, 5, l); sell(o, Items.GOLDEN_APPLE, 1, 20, l); }
        }
    }

    private static void merchant(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.PAPER, 24, 2, l); sell(o, Items.COMPASS, 1, 5, l); }
            case 2 -> { buy(o, Items.LEATHER, 12, 3, l); sell(o, Items.CLOCK, 1, 5, l); }
            case 3 -> { buy(o, Items.LAPIS_LAZULI, 16, 3, l); sell(o, Items.BOOKSHELF, 2, 6, l); }
            case 4 -> { buy(o, Items.GOLD_INGOT, 8, 5, l); sell(o, Items.ENDER_PEARL, 2, 10, l); }
            case 5 -> { buy(o, Items.DIAMOND, 3, 10, l); sell(o, Items.NAME_TAG, 2, 12, l); }
        }
    }

    private static void princeToken(MerchantOffers offers, Item token, int coins, int level) {
        // updateTrades may be invoked again as the villager levels up.
        for (MerchantOffer offer : offers) {
            if (offer.getBaseCostA().is(token)
                && offer.getResult().is(ModItems.ANCIENT_COIN.get())) return;
        }
        buy(offers, token, 1, coins, level);
    }

    /** The existing princely seal is spent together with coins; this never mints coins. */
    private static void princeSeal(MerchantOffers offers, int level) {
        for (MerchantOffer offer : offers) {
            if (offer.getBaseCostA().is(ModItems.PRINCELY_SEAL.get())
                    && offer.getResult().is(org.slavicmyths.marine.MarineArtifacts.COMPASS.get())) return;
        }
        offers.add(new MerchantOffer(
            new ItemCost(ModItems.PRINCELY_SEAL.get(), 1),
            java.util.Optional.of(new ItemCost(ModItems.ANCIENT_COIN.get(), 8)),
            new ItemStack(org.slavicmyths.marine.MarineArtifacts.COMPASS.get()),
            4, xp(level), 0.05F));
    }

    private static void prince(MerchantOffers o, int l) {
        switch (l) {
            case 1 -> { buy(o, Items.GOLD_INGOT, 6, 4, l); sell(o, Items.BOOK, 3, 2, l); }
            case 2 -> { buy(o, Items.EMERALD, 8, 5, l); sell(o, Items.COMPASS, 1, 4, l); }
            case 3 -> { buy(o, Items.DIAMOND, 2, 8, l); sell(o, ModItems.BIRCH_BARK_SCROLL.get(), 2, 6, l); }
            case 4 -> { buy(o, Items.GOLD_BLOCK, 2, 12, l); sell(o, Items.GOLDEN_APPLE, 1, 18, l); }
            case 5 -> { buy(o, Items.NETHERITE_SCRAP, 1, 18, l); sell(o, Items.TOTEM_OF_UNDYING, 1, 48, l); }
        }
        princeToken(o, MarineRewards.PHARAOHKA_TOKEN.get(), 18, l);
        princeToken(o, MarineRewards.DEEP_SERPENT_TOKEN.get(), 16, l);
        princeToken(o, MarineRewards.OCEAN_ZMEY_TOKEN.get(), 22, l);
        princeToken(o, MarineRewards.STRATIM_TOKEN.get(), 20, l);
        princeToken(o, MarineRewards.TSAR_PIKE_TOKEN.get(), 12, l);
        princeToken(o, MarineRewards.ZHELVA_TOKEN.get(), 22, l);
        princeSeal(o, l);
    }
}
