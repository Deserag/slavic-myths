package org.slavicmyths.progression;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

/** Vanilla advancements are the persisted, per-player discovery IDs. No duplicate player store. */
public final class Knowledge {
    public static final String[] ENTRIES = {"meet_domovoy", "meet_leshy", "shrine", "altar", "first_ritual", "new_catch", "net_catch", "meet_vodyanoy", "meet_rusalka", "depth_clue", "find_deep_pool", "defeat_depth_master", "depth_gift", "depth_gift", "explore_swamp_hut", "explore_abandoned_settlement", "explore_bog_causeway", "explore_flooded_shrine", "explore_fishing_camp", "explore_underwater_ruins", "bad_people", "find_small_camp", "find_medium_camp", "without_ataman", "learn_armorer", "woodlands", "black_feather"};
    public static void award(PlayerEntity player, String id) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity server = (ServerPlayerEntity) player;
        Advancement advancement = server.server.getAdvancements().getAdvancement(new ResourceLocation("slavicmyths", id));
        if (advancement != null) {
            for (String criterion : server.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria())
                server.getAdvancements().award(advancement, criterion);
        }
    }
    public static boolean knows(ServerPlayerEntity player, String id) {
        Advancement a = player.server.getAdvancements().getAdvancement(new ResourceLocation("slavicmyths", id));
        return a != null && player.getAdvancements().getOrStartProgress(a).isDone();
    }
    public static int mask(ServerPlayerEntity player) {
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
