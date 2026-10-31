package org.slavicmyths.verify;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.posad.PosadPopulationPlan;
import org.slavicmyths.posad.PosadTradeCatalog;

/** Exercise actual NeoForge item registration, which the JSON-only audit cannot validate. */
@GameTestHolder("slavicmyths_posad")
@PrefixGameTestTemplate(false)
public final class PosadMerchantGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void allRoleOffersUseVanillaStacks(GameTestHelper t) {
        int offersChecked = 0;
        Map<String, Integer> normalized = new HashMap<>();
        for (PosadPopulationPlan.Role role : PosadPopulationPlan.Role.values()) {
            IdentityHashMap<MerchantOffer, String> identities = new IdentityHashMap<>();
            MerchantOffers offers = PosadTradeCatalog.create(role, 5, new HashMap<>(), identities, t.getLevel().registryAccess());
            t.assertTrue(!offers.isEmpty(), "Merchant has no usable offers: " + role);
            for (MerchantOffer offer : offers) {
                t.assertTrue(offer.getResult().getCount() > 0 && offer.getResult().getCount() <= offer.getResult().getMaxStackSize(), "Invalid vanilla result stack for " + role);
                String id = identities.get(offer);
                t.assertTrue(id != null, "Merchant offer identity is missing: " + role);
                if (id.equals("weaponsmith_sale_1_1") || id.equals("sotnik_sale_3_2") || id.equals("weaponsmith_sale_4_1")) {
                    t.assertTrue(offer.getResult().getCount() == 1, "Non-stackable weapon must be sold individually: " + id);
                    normalized.merge(id, 1, Integer::sum);
                }
                offersChecked++;
            }
        }
        t.assertTrue(normalized.size() == 3, "Missing normalized non-stackable weapons: " + normalized);
        t.assertTrue(offersChecked >= 120, "Too many unavailable trade offers: " + offersChecked);
        t.succeed();
    }
}
