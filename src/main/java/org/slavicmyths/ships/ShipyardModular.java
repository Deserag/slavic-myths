package org.slavicmyths.ships;

import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/**
 * Server-checked material settlement for new modular ships and packed-item
 * upgrades. The bill comes from the actual changed voxel palette, not a
 * constant number of "ship sections" per click.
 */
public final class ShipyardModular {
    public record Materials(int logs, int planks, int iron, int string,
            int deckSections, int railKits, int helms,
            int basicSails, int linenSails, int reinforcedSails) {
        public boolean empty() {
            return logs + planks + iron + string + deckSections + railKits
                    + helms + basicSails + linenSails + reinforcedSails == 0;
        }
    }

    public static Materials costs(ShipBuildConfiguration original,
            ShipBuildConfiguration next) {
        if (next == null) throw new IllegalArgumentException("Missing destination ship");
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> before = original == null
                ? Map.of() : ShipHullVoxelLayout.generate(original.structure(), original.layoutVersion());
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> after =
                ShipHullVoxelLayout.generate(next.structure(), next.layoutVersion());
        Map<ShipHullVoxelLayout.Part, Integer> delta =
                ShipHullVoxelLayout.additions(before, after);
        int keel = delta.getOrDefault(ShipHullVoxelLayout.Part.KEEL, 0);
        int frame = delta.getOrDefault(ShipHullVoxelLayout.Part.FRAME, 0);
        int mast = delta.getOrDefault(ShipHullVoxelLayout.Part.MAST, 0);
        int yard = delta.getOrDefault(ShipHullVoxelLayout.Part.YARD, 0);
        int walls = delta.getOrDefault(ShipHullVoxelLayout.Part.CABIN_WALL, 0);
        int hull = delta.getOrDefault(ShipHullVoxelLayout.Part.HULL, 0);
        int deck = delta.getOrDefault(ShipHullVoxelLayout.Part.DECK, 0)
                + delta.getOrDefault(ShipHullVoxelLayout.Part.BRIDGE, 0);
        int railing = delta.getOrDefault(ShipHullVoxelLayout.Part.RAIL, 0);
        int sail = delta.getOrDefault(ShipHullVoxelLayout.Part.SAIL, 0)
                + delta.getOrDefault(ShipHullVoxelLayout.Part.SAIL_TRIM, 0);
        int rigging = delta.getOrDefault(ShipHullVoxelLayout.Part.RIGGING, 0);
        int bowsprit = delta.getOrDefault(ShipHullVoxelLayout.Part.BOWSPRIT, 0);
        int light = delta.getOrDefault(ShipHullVoxelLayout.Part.LIGHT, 0)
                + delta.getOrDefault(ShipHullVoxelLayout.Part.STANDING_LANTERN, 0);
        int ladders = delta.getOrDefault(ShipHullVoxelLayout.Part.LADDER, 0);
        int windows = delta.getOrDefault(ShipHullVoxelLayout.Part.WINDOW, 0);
        int helm = delta.getOrDefault(ShipHullVoxelLayout.Part.HELM, 0);
        int basic = 0, linen = 0, reinforced = 0;
        for (int i = 0; i < next.sailTiers().size(); i++) {
            int tier = next.sailTiers().get(i);
            int previousTier = original != null && i < original.sailTiers().size()
                    ? original.sailTiers().get(i) : 0;
            if (tier > previousTier) {
                if (tier == 1) basic++;
                if (tier == 2) linen++;
                if (tier == 3) reinforced++;
            }
        }
        return new Materials(divRoundUp(keel + frame + mast + yard + bowsprit + walls, 4),
                divRoundUp(hull + deck, 3),
                divRoundUp(keel + frame + mast + yard + windows + ladders, 15) + light * 2,
                divRoundUp(sail, 4) + divRoundUp(rigging, 3),
                divRoundUp(deck, 24),
                divRoundUp(railing, 16),
                helm > 0 ? 1 : 0,
                basic, linen, reinforced);
    }

    private static int divRoundUp(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static int count(Player player, TagKey<Item> tag) {
        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(tag)) found += stack.getCount();
        }
        return found;
    }

    private static void take(Player player, TagKey<Item> tag, int required) {
        int remaining = required;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(tag)) continue;
            int amount = Math.min(stack.getCount(), remaining);
            stack.shrink(amount);
            remaining -= amount;
        }
        if (remaining != 0) throw new IllegalStateException("Shipyard tag inventory changed");
    }

    private static void take(Player player, Item item, int required) {
        int remaining = required;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(item)) continue;
            int amount = Math.min(stack.getCount(), remaining);
            stack.shrink(amount);
            remaining -= amount;
        }
        if (remaining != 0) throw new IllegalStateException("Shipyard item inventory changed");
    }

    public static boolean canPay(Player player, Materials cost) {
        return player.getAbilities().instabuild
                || count(player, ItemTags.LOGS) >= cost.logs()
                && count(player, ItemTags.PLANKS) >= cost.planks()
                && player.getInventory().countItem(Items.IRON_INGOT) >= cost.iron()
                && player.getInventory().countItem(Items.STRING) >= cost.string()
                && player.getInventory().countItem(RaftContent.DECK_SECTION.get()) >= cost.deckSections()
                && player.getInventory().countItem(RaftContent.RAIL_MODULE.get()) >= cost.railKits()
                && player.getInventory().countItem(RaftContent.HELM_MODULE.get()) >= cost.helms()
                && player.getInventory().countItem(RaftContent.SAIL_MODULE.get()) >= cost.basicSails()
                && player.getInventory().countItem(RaftContent.SAIL_LINEN_MODULE.get()) >= cost.linenSails()
                && player.getInventory().countItem(RaftContent.SAIL_REINFORCED_MODULE.get()) >= cost.reinforcedSails();
    }

    static void pay(Player player, Materials cost) {
        if (player.getAbilities().instabuild) return;
        if (!canPay(player, cost)) throw new IllegalStateException("Shipyard insufficient inventory");
        take(player, ItemTags.LOGS, cost.logs());
        take(player, ItemTags.PLANKS, cost.planks());
        take(player, Items.IRON_INGOT, cost.iron());
        take(player, Items.STRING, cost.string());
        take(player, RaftContent.DECK_SECTION.get(), cost.deckSections());
        take(player, RaftContent.RAIL_MODULE.get(), cost.railKits());
        take(player, RaftContent.HELM_MODULE.get(), cost.helms());
        take(player, RaftContent.SAIL_MODULE.get(), cost.basicSails());
        take(player, RaftContent.SAIL_LINEN_MODULE.get(), cost.linenSails());
        take(player, RaftContent.SAIL_REINFORCED_MODULE.get(), cost.reinforcedSails());
    }

    public static ShipBuildConfiguration configuration(ItemStack stack) {
        if (!(stack.getItem() instanceof FirstShipItem)) return null;
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains("FirstShipPack", Tag.TAG_COMPOUND)) return null;
        CompoundTag pack = root.getCompound("FirstShipPack");
        if (!pack.contains(ShipBuildConfiguration.NBT_KEY, Tag.TAG_COMPOUND)) return null;
        try { return ShipBuildConfiguration.load(pack.getCompound(ShipBuildConfiguration.NBT_KEY)); }
        catch (RuntimeException invalid) { return null; }
    }

    public static ItemStack build(Player player, ShipBuildConfiguration config) {
        if (player.level().isClientSide || config == null) return ItemStack.EMPTY;
        Materials price = costs(null, config);
        if (!canPay(player, price)) return ItemStack.EMPTY;
        ItemStack item = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 2);
        pack.putInt("GeometryVersion", 9);
        pack.putInt("RigLevel", 0);
        pack.putInt("FabricQuality", 0);
        pack.put("Modules", new ListTag());
        pack.put(ShipBuildConfiguration.NBT_KEY, config.save());
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        FirstShipEntity inspector = RaftContent.FIRST_SHIP.get().create(player.level());
        if (inspector == null || !inspector.loadPacked(item)) return ItemStack.EMPTY;
        pay(player, price);
        return item;
    }

    public static boolean compatibleUpgrade(ShipBuildConfiguration old,
            ShipBuildConfiguration next) {
        if (old == null || next == null || old.shipClass() != next.shipClass()
                || old.layoutVersion() != next.layoutVersion()
                || next.extraLength() < old.extraLength()
                || next.extraWidth() < old.extraWidth()
                || next.decks() < old.decks()
                || old.bridge() && !next.bridge()
                || old.cabin() && !next.cabin()
                || next.sailTiers().size() < old.sailTiers().size()) return false;
        for (int i = 0; i < old.sailTiers().size(); i++)
            if (next.sailTiers().get(i) < old.sailTiers().get(i)) return false;
        return true;
    }

    public static ItemStack modify(Player player, ItemStack original,
            ShipBuildConfiguration next) {
        if (player.level().isClientSide || original.isEmpty() || next == null)
            return ItemStack.EMPTY;
        ShipBuildConfiguration old = configuration(original);
        if (!compatibleUpgrade(old, next)) return ItemStack.EMPTY;
        CompoundTag root = original.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag packed = root.getCompound("FirstShipPack").copy();
        packed.put(ShipBuildConfiguration.NBT_KEY, next.save());
        root.put("FirstShipPack", packed);
        ItemStack result = original.copy();
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        FirstShipEntity verifier = RaftContent.FIRST_SHIP.get().create(player.level());
        if (verifier == null || !verifier.loadPacked(result)) return ItemStack.EMPTY;
        // Every preserved chest position must be valid in the new layout.
        ListTag cargo = packed.getList("BlockCargo", Tag.TAG_COMPOUND);
        if (ShipModularBlockHull.localMotionPalette(next, cargo).isEmpty())
            return ItemStack.EMPTY;
        Materials price = costs(old, next);
        if (!canPay(player, price)) return ItemStack.EMPTY;
        pay(player, price);
        return result;
    }

    private ShipyardModular() {}
}
