package org.slavicmyths.verify;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void validateAll142CatalogStacks(GameTestHelper t) {
        int checked = 0;
        for (JsonObject entry : PosadTradeCatalog.catalog()) {
            JsonObject active = entry;
            if (!registered(active) && entry.has("fallback")) active = entry.getAsJsonObject("fallback");
            if (!registered(active)) {
                t.assertTrue(entry.has("deferred") && entry.get("deferred").getAsBoolean(), "Missing non-deferred trade item: " + entry.get("id"));
                checked++;
                continue;
            }
            int repetitions = 1;
            for (String slot : new String[]{"a", "b", "out"}) {
                if (!active.has(slot)) continue;
                JsonObject spec = active.getAsJsonObject(slot);
                int count = spec.get("count").getAsInt();
                int max = item(spec).getDefaultMaxStackSize();
                t.assertTrue(count > 0, "Nonpositive stack: " + entry.get("id") + "/" + slot);
                if (count > max) {
                    t.assertTrue(max == 1, "Oversized stackable item: " + entry.get("id") + "/" + slot);
                    t.assertTrue(repetitions == 1 || repetitions == count, "Incompatible nonstackable batch: " + entry.get("id"));
                    repetitions = count;
                }
            }
            for (String slot : new String[]{"a", "b", "out"}) {
                if (!active.has(slot)) continue;
                JsonObject spec = active.getAsJsonObject(slot);
                int count = spec.get("count").getAsInt();
                t.assertTrue(count % repetitions == 0 && count / repetitions >= 1 && count / repetitions <= item(spec).getDefaultMaxStackSize(), "Invalid normalized stack: " + entry.get("id") + "/" + slot);
            }
            checked++;
        }
        t.assertTrue(checked == 142, "Catalog validation did not cover all 142 offers: " + checked);
        t.succeed();
    }
    private static Item item(JsonObject spec) {
        ResourceLocation key = ResourceLocation.parse(spec.get("id").getAsString());
        return BuiltInRegistries.ITEM.containsKey(key) ? BuiltInRegistries.ITEM.get(key) : Items.AIR;
    }
    private static boolean registered(JsonObject row) {
        for (String slot : new String[]{"a", "b", "out"}) if (row.has(slot) && item(row.getAsJsonObject(slot)) == Items.AIR) return false;
        return true;
    }

}
