package org.slavicmyths.village;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * Appends JSON-defined offers to the five vanilla villager levels.
 * Neither trade counts nor profession counts are represented as fixed-size arrays.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class VillageTrades {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 5;

    private VillageTrades() {}

    @SubscribeEvent
    public static void trades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();
        ResourceLocation id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
        if (id == null || !"slavicmyths".equals(id.getNamespace()) || !VillageRoles.ROLES.containsKey(id.getPath())) return;
        if ("druzhinnik".equals(id.getPath())) return;

        String resource = "/data/slavicmyths/village_trades/" + id.getPath() + ".json";
        JsonObject document = load(resource);
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        for (Map.Entry<String, JsonElement> entry : document.entrySet()) {
            int level = tradeLevel(entry.getKey(), resource);
            JsonElement section = entry.getValue();
            if (!section.isJsonArray()) throw new IllegalStateException("Trade level is not a list: " + resource + " / " + level);
            JsonArray rows = section.getAsJsonArray();
            List<VillagerTrades.ItemListing> additions = new ArrayList<>(rows.size());
            for (JsonElement element : rows) {
                if (!element.isJsonObject()) throw new IllegalStateException("Trade row is not an object: " + resource + " / " + level);
                additions.add(parseListing(element.getAsJsonObject(), resource, level));
            }
            // A vanilla level remains a single key irrespective of how many offers it has.
            trades.computeIfAbsent(level, ignored -> new ArrayList<>()).addAll(additions);
        }
    }

    private static JsonObject load(String resource) {
        try (var stream = VillageTrades.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing trade pool " + resource);
            JsonElement root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) throw new IllegalStateException("Trade pool must contain an object: " + resource);
            return root.getAsJsonObject();
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot read trade pool " + resource, ex);
        }
    }

    private static int tradeLevel(String key, String resource) {
        int level;
        try {
            level = Integer.parseInt(key);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid trade level " + key + " in " + resource, ex);
        }
        if (level < MIN_LEVEL || level > MAX_LEVEL) throw new IllegalStateException("Trade level outside 1-5: " + level + " in " + resource);
        return level;
    }

    private static VillagerTrades.ItemListing parseListing(JsonObject row, String resource, int level) {
        try {
            Item input = item(row.get("input").getAsString());
            Item output = item(row.get("output").getAsString());
            int cost = row.get("cost").getAsInt();
            int count = row.get("count").getAsInt();
            int uses = row.get("max_uses").getAsInt();
            int xp = row.get("xp").getAsInt();
            float multiplier = row.get("price_multiplier").getAsFloat();
            if (cost < 1 || cost > input.getDefaultMaxStackSize() || count < 1 || count > output.getDefaultMaxStackSize() || uses < 1 || xp < 0 || !Float.isFinite(multiplier) || multiplier < 0) {
                throw new IllegalArgumentException("Trade quantities or multiplier out of bounds");
            }
            return (entity, random) -> new MerchantOffer(new ItemCost(input, cost), new ItemStack(output, count), uses, xp, multiplier);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Invalid trade in " + resource + " at level " + level + ": " + row, ex);
        }
    }

    private static Item item(String name) {
        ResourceLocation key = ResourceLocation.parse(name);
        if (!BuiltInRegistries.ITEM.containsKey(key)) throw new IllegalArgumentException("Unknown trade item " + name);
        Item item = BuiltInRegistries.ITEM.get(key);
        if (item == Items.AIR) throw new IllegalArgumentException("AIR cannot be traded: " + name);
        return item;
    }
}
