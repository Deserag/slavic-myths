package org.slavicmyths.marine;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.buyan.BuyanTrades;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.village.VillageRoles;

/** Full merchant offers in a real NeoForge world: role gate, six tokens, paid seal and dedup. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarinePrinceGameTests {
    private static Villager villager(GameTestHelper t, String role) {
        Villager villager = EntityType.VILLAGER.create(t.getLevel());
        t.assertTrue(villager != null, "Could not create test villager");
        villager.setNoAi(true);
        villager.setData(VillageRoles.BUYAN_ROLE, role);
        villager.moveTo(t.absolutePos(new BlockPos(2, 2, 2)), 0F, 0F);
        t.assertTrue(t.getLevel().addFreshEntity(villager), "Could not spawn test villager");
        t.assertTrue(BuyanTrades.populate(villager), "Buyan role rejected: " + role);
        return villager;
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void princelyTokensPayExactCoinsOnlyOnce(GameTestHelper t) {
        var prince = villager(t, "knyaz");
        Map<Item, Integer> prices = new LinkedHashMap<>();
        prices.put(MarineRewards.PHARAOHKA_TOKEN.get(), 18);
        prices.put(MarineRewards.DEEP_SERPENT_TOKEN.get(), 16);
        prices.put(MarineRewards.OCEAN_ZMEY_TOKEN.get(), 22);
        prices.put(MarineRewards.STRATIM_TOKEN.get(), 20);
        prices.put(MarineRewards.TSAR_PIKE_TOKEN.get(), 12);
        prices.put(MarineRewards.ZHELVA_TOKEN.get(), 22);
        BuyanTrades.populate(prince);
        for (var itemPrice : prices.entrySet()) {
            var matches = prince.getOffers().stream()
                .filter(offer -> offer.getBaseCostA().is(itemPrice.getKey())
                    && offer.getResult().is(ModItems.ANCIENT_COIN.get()))
                .toList();
            t.assertTrue(matches.size() == 1,
                "Duplicate or missing boss-token trade");
            MerchantOffer trade = matches.getFirst();
            t.assertTrue(trade.getBaseCostA().getCount() == 1
                && trade.getResult().getCount() == itemPrice.getValue(),
                "Token exchange generated incorrect coin count");
        }
        prince.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void princelySealCostsEightCoinsAndCannotMintCoins(GameTestHelper t) {
        var prince = villager(t, "knyaz");
        BuyanTrades.populate(prince);
        var matches = prince.getOffers().stream()
            .filter(offer -> offer.getBaseCostA().is(ModItems.PRINCELY_SEAL.get()))
            .toList();
        t.assertTrue(matches.size() == 1, "Duplicate or missing seal trade");
        var offer = matches.getFirst();
        t.assertTrue(offer.getBaseCostA().getCount() == 1
            && offer.getCostB().is(ModItems.ANCIENT_COIN.get())
            && offer.getCostB().getCount() == 8
            && offer.getResult().is(MarineArtifacts.COMPASS.get())
            && offer.getResult().getCount() == 1,
            "The seal must buy one compass with eight coins, never mint coins");
        prince.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void ordinaryBuyanVillagerCannotBuyBossTokens(GameTestHelper t) {
        var villager = villager(t, "obychny");
        t.assertTrue(villager.getOffers().stream().noneMatch(
            offer -> offer.getResult().is(ModItems.ANCIENT_COIN.get())
                && (offer.getBaseCostA().is(MarineRewards.PHARAOHKA_TOKEN.get())
                    || offer.getBaseCostA().is(MarineRewards.DEEP_SERPENT_TOKEN.get())
                    || offer.getBaseCostA().is(MarineRewards.OCEAN_ZMEY_TOKEN.get())
                    || offer.getBaseCostA().is(MarineRewards.STRATIM_TOKEN.get())
                    || offer.getBaseCostA().is(MarineRewards.TSAR_PIKE_TOKEN.get())
                    || offer.getBaseCostA().is(MarineRewards.ZHELVA_TOKEN.get()))),
            "Ordinary villager illegally accepted ocean boss tokens");
        villager.discard();
        t.succeed();
    }
}
