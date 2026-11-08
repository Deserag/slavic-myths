package org.slavicmyths.ships;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Server-side shipyard operations with no item replication or blind metadata resets. */
public final class ShipyardUpgrades {
    public static int sectionCost(RaftSize from) {
        return switch (from) {
            case SMALL -> 6;
            case LONG, WIDE -> 10;
            case CARGO -> 0;
        };
    }

    public static boolean canBuild(Player player) {
        return player.getAbilities().instabuild ||
                (player.getInventory().countItem(RaftContent.DECK_SECTION.get()) >= 9 &&
                 player.getInventory().countItem(RaftContent.OAR_MODULE.get()) >= 1);
    }

    public static ItemStack build(Player player) {
        if (player.level().isClientSide || !canBuild(player)) return ItemStack.EMPTY;
        // Validate all ingredients before changing any slot.
        if (!player.getAbilities().instabuild) {
            consume(player, RaftContent.DECK_SECTION.get(), 9);
            consume(player, RaftContent.OAR_MODULE.get(), 1);
        }
        return new ItemStack(RaftContent.RAFT_ITEM.get());
    }

    public static boolean canUpgrade(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof RaftItem raft) || raft.size().next() == null) return false;
        RaftCharacteristics.Info info = RaftCharacteristics.fromItem(stack);
        if (info == null || info.invalid()) return false;
        return player.getAbilities().instabuild ||
                player.getInventory().countItem(RaftContent.DECK_SECTION.get()) >= sectionCost(raft.size());
    }

    /** Does not mutate input until the upgraded vessel and cargo metadata are fully validated. */
    public static ItemStack upgrade(Player player, ItemStack original) {
        if (player.level().isClientSide || !canUpgrade(original, player)) return ItemStack.EMPTY;
        RaftSize oldSize = ((RaftItem)original.getItem()).size();
        RaftSize next = oldSize.next();
        if (next == null) return ItemStack.EMPTY;
        CompoundTag root = original.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean packedAlready = root.contains("Raft2511Pack", Tag.TAG_COMPOUND);
        CompoundTag packed = packedAlready ? root.getCompound("Raft2511Pack").copy() : new CompoundTag();
        ListTag modules = packed.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        if (modules.size() > oldSize.width * oldSize.length) return ItemStack.EMPTY;
        boolean[] occupied = new boolean[next.width * next.length];
        int offsetX = (next.width - oldSize.width) / 2;
        int offsetZ = (next.length - oldSize.length) / 2;
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile");
            if (tile < 0 || tile >= oldSize.width * oldSize.length) return ItemStack.EMPTY;
            int x = tile % oldSize.width + offsetX, z = tile / oldSize.width + offsetZ;
            int destination = z * next.width + x;
            if (destination < 0 || destination >= occupied.length || occupied[destination]) return ItemStack.EMPTY;
            occupied[destination] = true;
            module.putInt("Tile", destination);
        }
        packed.putInt("Version", 2);
        packed.putInt("RaftWidth", next.width);
        packed.putInt("RaftLength", next.length);
        if (!packedAlready) {
            packed.putInt("RaftSails", 0);
            packed.putInt("RaftFlags", 0);
        }
        packed.put("Raft2511Modules", modules);
        root.put("Raft2511Pack", packed);
        ItemStack upgraded = new ItemStack(RaftContent.raftItem(next));
        upgraded.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        if (!player.getAbilities().instabuild) consume(player, RaftContent.DECK_SECTION.get(), sectionCost(oldSize));
        return upgraded;
    }

    /**
     * Install one piece of equipment on the packed vessel at the workbench.
     * action: 3 rails, 4 sail, 5 helm, 6 extra oars.
     * The original stack is replaced in the same menu slot, never duplicated.
     */
    public static ItemStack equip(Player player, ItemStack original, int action) {
        if (player.level().isClientSide || !(original.getItem() instanceof RaftItem raft)) return ItemStack.EMPTY;
        RaftCharacteristics.Info info = RaftCharacteristics.fromItem(original);
        if (info == null || info.invalid()) return ItemStack.EMPTY;
        Item part = switch (action) {
            case 3 -> RaftContent.RAIL_MODULE.get();
            case 4 -> RaftContent.SAIL_MODULE.get();
            case 5 -> RaftContent.HELM_MODULE.get();
            case 6 -> RaftContent.OAR_MODULE.get();
            default -> null;
        };
        if (part == null || (!player.getAbilities().instabuild &&
                player.getInventory().countItem(part) < 1)) return ItemStack.EMPTY;
        if ((action == 3 && info.rails()) || (action == 4 && info.sails() >= raft.size().maxSails()) ||
                (action == 5 && info.helm()) || (action == 6 &&
                (info.extraOars() || raft.size().length <= 3))) return ItemStack.EMPTY;
        CompoundTag root = original.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag packed = root.contains("Raft2511Pack", Tag.TAG_COMPOUND)
                ? root.getCompound("Raft2511Pack").copy() : new CompoundTag();
        packed.putInt("Version", 2);
        packed.putInt("RaftWidth", raft.size().width);
        packed.putInt("RaftLength", raft.size().length);
        packed.putInt("RaftSails", info.sails() + (action == 4 ? 1 : 0));
        int flags = (info.rails() ? 1 : 0) | (info.helm() ? 2 : 0) | (info.extraOars() ? 4 : 0);
        flags |= switch (action) { case 3 -> 1; case 5 -> 2; case 6 -> 4; default -> 0; };
        packed.putInt("RaftFlags", flags);
        if (!packed.contains("Raft2511Modules", Tag.TAG_LIST)) packed.put("Raft2511Modules", new ListTag());
        root.put("Raft2511Pack", packed);
        ItemStack result = original.copy();
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        if (!player.getAbilities().instabuild) consume(player, part, 1);
        return result;
    }

    private static void consume(Player player, Item item, int total) {
        int remaining = total;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack current = player.getInventory().getItem(i);
            if (!current.is(item)) continue;
            int taken = Math.min(remaining, current.getCount());
            current.shrink(taken);
            remaining -= taken;
        }
        if (remaining != 0) throw new IllegalStateException("Shipyard resources changed during validated operation");
    }

    private ShipyardUpgrades() {}
}
