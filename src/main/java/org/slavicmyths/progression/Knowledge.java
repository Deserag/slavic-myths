package org.slavicmyths.progression;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

/** Vanilla advancements are the persisted, per-player discovery IDs. No duplicate player store. */
public final class Knowledge {
    public static final String[] ENTRIES = {"meet_domovoy", "meet_leshy", "shrine", "altar", "first_ritual", "new_catch", "net_catch", "meet_vodyanoy", "meet_rusalka", "depth_clue", "find_deep_pool", "defeat_depth_master", "depth_gift", "depth_gift", "explore_swamp_hut", "explore_abandoned_settlement", "explore_bog_causeway", "explore_flooded_shrine", "explore_fishing_camp", "explore_underwater_ruins", "bad_people", "find_small_camp", "find_medium_camp", "without_ataman", "learn_armorer", "woodlands", "black_feather", "meet_nightingale"};
    public static void award(Player player, String id) {
        if (!(player instanceof ServerPlayer)) return;
        ServerPlayer server = (ServerPlayer) player;
        AdvancementHolder advancement = server.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("slavicmyths", id));
        if (advancement != null) {
            for (String criterion : server.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria())
                server.getAdvancements().award(advancement, criterion);
        }
    }
    public static boolean knows(ServerPlayer player, String id) {
        AdvancementHolder a = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("slavicmyths", id));
        return a != null && player.getAdvancements().getOrStartProgress(a).isDone();
    }
    public static int mask(ServerPlayer player) {
        int mask = 0;
        // Inventory-derived discoveries also work for chest loot and gifts from other players.
        if (knows(player, "ancient_find")) award(player, "shrine");
        if (knows(player, "leshy_heart")) award(player, "meet_leshy");
        for (int i = 0; i < ENTRIES.length; i++) if (knows(player, ENTRIES[i])) mask |= 1 << i;
        if (Integer.bitCount(mask) >= 3) award(player, "lore_keeper");
        return mask;
    }
    private Knowledge() { }
}
