package org.slavicmyths.posad;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Exact owner-approved 2.2.12 prices; no quests, trust, new currency, or villager trade hooks. */
public final class PosadTradeCatalog {
    private static final String RESOURCE = "/data/slavicmyths/posad/trades.json";
    private static final int[] XP_THRESHOLDS = {0, 10, 70, 150, 250};
    private static final int[] XP_PER_TIER = {0, 2, 5, 10, 15, 30};
    private static List<JsonObject> catalog;

    public static int tier(PosadPopulationPlan.Role role, int xp) {
        if (fixed(role)) return 1;
        int tier = 1;
        for (int i = 1; i < XP_THRESHOLDS.length; i++) if (xp >= XP_THRESHOLDS[i]) tier = i + 1;
        return tier;
    }
    public static boolean fixed(PosadPopulationPlan.Role role) {
        return role == PosadPopulationPlan.Role.POSADNIK || role == PosadPopulationPlan.Role.CIVILIAN;
    }
    public static synchronized List<JsonObject> catalog() {
        if (catalog != null) return catalog;
        List<JsonObject> entries = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try (var input = PosadTradeCatalog.class.getResourceAsStream(RESOURCE)) {
            if (input == null) throw new IllegalStateException("Posad trade resource is missing: " + RESOURCE);
            JsonObject file = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            if (file.get("schema").getAsInt() != 1 || !file.get("currency").getAsString().equals("slavicmyths:ancient_coin")) throw new IllegalStateException("Unexpected trade schema/currency");
            for (JsonElement raw : file.getAsJsonArray("offers")) {
                JsonObject entry = raw.getAsJsonObject();
                String id = entry.get("id").getAsString();
                PosadPopulationPlan.Role.valueOf(entry.get("role").getAsString());
                int tier = entry.get("tier").getAsInt();
                if (!ids.add(id) || tier < 1 || tier > 5) throw new IllegalStateException("Invalid trade id or tier " + id);
                if (fixed(PosadPopulationPlan.Role.valueOf(entry.get("role").getAsString())) && tier != 1) throw new IllegalStateException("Fixed role must not level " + id);
                entries.add(entry);
            }
        } catch (java.io.IOException error) {
            throw new java.io.UncheckedIOException(error);
        }
        catalog = List.copyOf(entries);
        return catalog;
    }
    private static Item item(JsonObject stack) {
        ResourceLocation key = ResourceLocation.parse(stack.get("id").getAsString());
        if (!BuiltInRegistries.ITEM.containsKey(key)) return null;
        Item item = BuiltInRegistries.ITEM.get(key);
        return item == Items.AIR ? null : item;
    }
    private static boolean available(JsonObject row) {
        for (String key : List.of("a", "b", "out")) {
            if (row.has(key) && item(row.getAsJsonObject(key)) == null) return false;
        }
        return true;
    }
    private static ItemStack result(JsonObject spec) {
        Item item = item(spec);
        int count = spec.get("count").getAsInt();
        if (item == null || count < 1 || count > item.getDefaultMaxStackSize()) throw new IllegalStateException("Invalid output item/stack: " + spec);
        return new ItemStack(item, count);
    }
    private static ItemCost input(JsonObject spec, boolean pristine) {
        Item item = item(spec);
        int count = spec.get("count").getAsInt();
        if (item == null || count < 1 || count > item.getDefaultMaxStackSize()) throw new IllegalStateException("Invalid input item/stack: " + spec);
        ItemCost cost = new ItemCost(item, count);
        if (pristine) cost = cost.withComponents(builder -> builder.expect(DataComponents.DAMAGE, 0).expect(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        return cost;
    }
    private static boolean equipment(String id) {
        return id.contains("spear") || id.contains("sulitsa") || id.contains("sword") || id.contains("helmet")
                || id.contains("chestplate") || id.contains("leggings") || id.contains("boots") || id.contains("shield")
                || id.contains("crossbow") || id.contains("dagger") || id.contains("battle_axe") || id.contains("mace");
    }
    private static int maxUses(JsonObject entry, JsonObject actual) {
        String kind = entry.get("type").getAsString();
        if (kind.equals("buy")) return 8;
        if (kind.equals("barter")) return 6;
        if (entry.get("tier").getAsInt() == 5 && !fixed(PosadPopulationPlan.Role.valueOf(entry.get("role").getAsString()))) return 2;
        return equipment(actual.getAsJsonObject("out").get("id").getAsString()) ? 3 : 10;
    }
    public static MerchantOffers create(PosadPopulationPlan.Role role, int tier, Map<String,Integer> used, IdentityHashMap<MerchantOffer,String> identities, RegistryAccess registries) {
        MerchantOffers offers = new MerchantOffers();
        identities.clear();
        for (JsonObject original : catalog()) {
            if (!original.get("role").getAsString().equals(role.name()) || original.get("tier").getAsInt() > tier) continue;
            JsonObject actual = original;
            if (!available(actual)) {
                if (!original.has("fallback")) {
                    // Only items explicitly staged in future patches may be absent. No AIR substitutions.
                    String name = original.get("id").getAsString();
                    if (!original.has("deferred") || !original.get("deferred").getAsBoolean()) throw new IllegalStateException("Missing live posad trade item in " + name);
                    continue;
                }
                actual = original.getAsJsonObject("fallback");
                if (!available(actual)) throw new IllegalStateException("Invalid fallback for " + original.get("id").getAsString());
            }
            String id = original.get("id").getAsString();
            boolean pristine = original.has("pristine") && original.get("pristine").getAsBoolean();
            ItemCost a = input(actual.getAsJsonObject("a"), pristine);
            Optional<ItemCost> b = actual.has("b") ? Optional.of(input(actual.getAsJsonObject("b"), false)) : Optional.empty();
            ItemStack result = result(actual.getAsJsonObject("out"));
            if (original.has("enchant") && actual == original) {
                ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(original.get("enchant").getAsString()));
                var holder = registries.registryOrThrow(Registries.ENCHANTMENT).getHolder(key).orElseThrow(() -> new IllegalStateException("Missing posad enchantment: " + key));
                result.enchant(holder, 1);
            }
            int xp = fixed(role) ? 0 : XP_PER_TIER[original.get("tier").getAsInt()];
            MerchantOffer offer = new MerchantOffer(a, b, result, maxUses(original, actual), xp, 0.0F);
            int spent = Math.min(offer.getMaxUses(), Math.max(0, used.getOrDefault(id, 0)));
            for (int n = 0; n < spent; n++) offer.increaseUses();
            offers.add(offer);
            identities.put(offer, id);
        }
        return offers;
    }
    private PosadTradeCatalog() {}
}
