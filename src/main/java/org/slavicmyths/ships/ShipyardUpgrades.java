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


    /** All four raft blueprints use the same materials as progressive expansion. */
    public static int raftDeckCost(RaftSize size) {
        return switch (size) {
            case SMALL -> 9;
            case LONG -> 15;
            case WIDE -> 25;
            case CARGO -> 35;
        };
    }

    public static ItemStack buildRaft(Player player, RaftSize size) {
        if (player.level().isClientSide) return ItemStack.EMPTY;
        int sections = raftDeckCost(size);
        if (!player.getAbilities().instabuild
                && (player.getInventory().countItem(RaftContent.DECK_SECTION.get()) < sections
                    || player.getInventory().countItem(RaftContent.OAR_MODULE.get()) < 1))
            return ItemStack.EMPTY;
        if (!player.getAbilities().instabuild) {
            consume(player, RaftContent.DECK_SECTION.get(), sections);
            consume(player, RaftContent.OAR_MODULE.get(), 1);
        }
        return new ItemStack(RaftContent.raftItem(size));
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
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag module = modules.getCompound(i);
            int destination;
            try {
                destination = ShipyardLayout.remap(module.getInt("Tile"),
                        oldSize.width, oldSize.length, next.width, next.length);
            } catch (IllegalArgumentException invalid) {
                return ItemStack.EMPTY;
            }
            if (occupied[destination]) return ItemStack.EMPTY;
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
        // Keep all vanilla/custom components: names, lore, enchantments, etc.
        // Only the registered raft item and vessel-specific payload change.
        ItemStack upgraded = original.transmuteCopy(RaftContent.raftItem(next), 1);
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

    /**
     * Remove one approved part and return it to the player. The source raft
     * never disappears or loses cargo during reconfiguration.
     * Actions: 7 rails, 8 one sail, 9 helm, 10 extra oars.
     */
    public static ItemStack dismantle(Player player, ItemStack original, int action) {
        if (player.level().isClientSide || !(original.getItem() instanceof RaftItem)) return ItemStack.EMPTY;
        RaftCharacteristics.Info info = RaftCharacteristics.fromItem(original);
        if (info == null || info.invalid()) return ItemStack.EMPTY;
        Item part = switch (action) {
            case 7 -> RaftContent.RAIL_MODULE.get();
            case 8 -> RaftContent.SAIL_MODULE.get();
            case 9 -> RaftContent.HELM_MODULE.get();
            case 10 -> RaftContent.OAR_MODULE.get();
            default -> null;
        };
        if (part == null || (action == 7 && !info.rails()) ||
                (action == 8 && info.sails() == 0) ||
                (action == 9 && !info.helm()) ||
                (action == 10 && !info.extraOars())) return ItemStack.EMPTY;
        ItemStack refund = new ItemStack(part);
        if (!canReceive(player, refund)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Нужна свободная ячейка для снятой детали"), true);
            return ItemStack.EMPTY;
        }
        CompoundTag root = original.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag packed = root.getCompound("Raft2511Pack").copy();
        packed.putInt("RaftSails", info.sails() - (action == 8 ? 1 : 0));
        int flags = (info.rails() ? 1 : 0) | (info.helm() ? 2 : 0) | (info.extraOars() ? 4 : 0);
        flags &= ~switch (action) { case 7 -> 1; case 9 -> 2; case 10 -> 4; default -> 0; };
        packed.putInt("RaftFlags", flags);
        root.put("Raft2511Pack", packed);
        ItemStack changed = original.copy();
        changed.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        // All checks completed. Refund to the real player inventory, not the ship.
        if (!player.getInventory().add(refund)) player.drop(refund, false);
        return changed;
    }

    private static boolean canReceive(Player player, ItemStack part) {
        int end = Math.min(36, player.getInventory().getContainerSize());
        for (int i = 0; i < end; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || (ItemStack.isSameItemSameComponents(stack, part) &&
                    stack.getCount() < stack.getMaxStackSize())) return true;
        }
        return false;
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


    /**
     * A full ship is deliberately NOT a raft upgrade. Shipwright's bill:
     * 48 logs, 96 planks, 32 iron fasteners, 24 rope (string),
     * 32 deck sections, four bulwark kits and one helm.
     * Sailcloth and all sail tiers are separate equipment. All checks run before any item is taken.
     */
    public static boolean canBuildFirstShip(Player player) {
        return player.getAbilities().instabuild ||
                (countTag(player, net.minecraft.tags.ItemTags.LOGS) >= 48 &&
                 countTag(player, net.minecraft.tags.ItemTags.PLANKS) >= 96 &&
                 player.getInventory().countItem(net.minecraft.world.item.Items.IRON_INGOT) >= 32 &&
                 player.getInventory().countItem(net.minecraft.world.item.Items.STRING) >= 24 &&
                 player.getInventory().countItem(RaftContent.DECK_SECTION.get()) >= 32 &&
                 player.getInventory().countItem(RaftContent.RAIL_MODULE.get()) >= 4 &&
                 player.getInventory().countItem(RaftContent.HELM_MODULE.get()) >= 1);
    }

    public static ItemStack buildFirstShip(Player player) {
        if (player.level().isClientSide || !canBuildFirstShip(player)) return ItemStack.EMPTY;
        if (!player.getAbilities().instabuild) {
            consumeTag(player, net.minecraft.tags.ItemTags.LOGS, 48);
            consumeTag(player, net.minecraft.tags.ItemTags.PLANKS, 96);
            consume(player, net.minecraft.world.item.Items.IRON_INGOT, 32);
            consume(player, net.minecraft.world.item.Items.STRING, 24);
            consume(player, RaftContent.DECK_SECTION.get(), 32);
            consume(player, RaftContent.RAIL_MODULE.get(), 4);
            consume(player, RaftContent.HELM_MODULE.get(), 1);
        }
        return new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
    }


    /** Reuse the existing three-dimensional ship_helm_module model. All new
     * sail/mast installation is packed-item only and server-authoritative. */
    public static int shipRigLevel(ItemStack stack) {
        CompoundTag pack = shipPack(stack);
        return pack == null ? -1 : pack.isEmpty() ? 0
                : pack.getInt("Version") == 1 ? 1 : pack.getInt("RigLevel");
    }

    public static int shipFabric(ItemStack stack) {
        CompoundTag pack = shipPack(stack);
        return pack == null ? -1 : pack.isEmpty() ? 0
                : pack.getInt("Version") == 1 ? 1 : pack.getInt("FabricQuality");
    }

    private static CompoundTag shipPack(ItemStack stack) {
        if (!(stack.getItem() instanceof FirstShipItem)) return null;
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains("FirstShipPack", Tag.TAG_COMPOUND)) return new CompoundTag();
        CompoundTag pack = root.getCompound("FirstShipPack");
        if ((pack.getInt("Version") != 1 && pack.getInt("Version") != 2)
                || !pack.contains("Modules", Tag.TAG_LIST)) return null;
        int rig = pack.getInt("RigLevel");
        int fabric = pack.getInt("FabricQuality");
        if (pack.getInt("Version") == 1) { rig = 1; fabric = 1; }
        try { new ShipRigging(rig, fabric); }
        catch (IllegalArgumentException invalid) { return null; }
        return pack;
    }

    private static Item fabricModule(int quality) {
        return switch (quality) {
            case 1 -> RaftContent.SAIL_MODULE.get();
            case 2 -> RaftContent.SAIL_LINEN_MODULE.get();
            case 3 -> RaftContent.SAIL_REINFORCED_MODULE.get();
            default -> throw new IllegalArgumentException("Bad sail quality");
        };
    }

    private static void refund(Player player, Item item, int amount) {
        if (player.getAbilities().instabuild || amount <= 0) return;
        ItemStack left = new ItemStack(item, amount);
        player.getInventory().add(left);
        if (!left.isEmpty()) player.drop(left, false);
    }

    /**
     * Workshop-only rig transactions:
     * 20/21/22 = install/swap fabric, 23 = mast/yard +1,
     * 24 = mast/yard -1, 25 = completely remove rig.
     * Preserve every other data component, old saved geometry and block cargo.
     */
    public static ItemStack outfitFirstShip(Player player, ItemStack original, int action) {
        if (player.level().isClientSide || !(original.getItem() instanceof FirstShipItem))
            return ItemStack.EMPTY;
        CompoundTag pack = shipPack(original);
        if (pack == null) return ItemStack.EMPTY;
        // The real ship loader validates legacy cargo, module positions,
        // packed geometry and design before a single material is consumed.
        FirstShipEntity verifier = RaftContent.FIRST_SHIP.get().create(player.level());
        if (verifier == null || !verifier.loadPacked(original)) return ItemStack.EMPTY;
        int level = pack.isEmpty() ? 0 : pack.getInt("RigLevel");
        int fabric = pack.isEmpty() ? 0 : pack.getInt("FabricQuality");
        if (!pack.isEmpty() && pack.getInt("Version") == 1) { level = 1; fabric = 1; }
        try { new ShipRigging(level, fabric); }
        catch (IllegalArgumentException invalid) { return ItemStack.EMPTY; }
        int newLevel = level, newFabric = fabric;
        Item paid1 = null, paid2 = null, returned1 = null, returned2 = null;
        int amount1 = 0, amount2 = 0, back1 = 0, back2 = 0;
        if (action >= 20 && action <= 22) {
            newFabric = action - 19;
            if (fabric == newFabric) return ItemStack.EMPTY;
            newLevel = Math.max(1, level);
            paid1 = fabricModule(newFabric);
            amount1 = newLevel;
            if (level > 0) { returned1 = fabricModule(fabric); back1 = level; }
        } else if (action == 23) {
            if (level < 1 || level >= 4) return ItemStack.EMPTY;
            newLevel = level + 1;
            paid1 = RaftContent.SAIL_RIGGING_KIT.get();
            paid2 = fabricModule(fabric);
            amount1 = amount2 = 1;
        } else if (action == 24) {
            if (level < 2) return ItemStack.EMPTY;
            newLevel = level - 1;
            returned1 = RaftContent.SAIL_RIGGING_KIT.get();
            returned2 = fabricModule(fabric);
            back1 = back2 = 1;
        } else if (action == 25) {
            if (level < 1) return ItemStack.EMPTY;
            newLevel = newFabric = 0;
            returned1 = fabricModule(fabric);
            returned2 = RaftContent.SAIL_RIGGING_KIT.get();
            back1 = level;
            back2 = level - 1;
        } else return ItemStack.EMPTY;
        try { new ShipRigging(newLevel, newFabric); }
        catch (IllegalArgumentException invalid) { return ItemStack.EMPTY; }

        if (!player.getAbilities().instabuild) {
            if (paid1 != null && player.getInventory().countItem(paid1) < amount1)
                return ItemStack.EMPTY;
            if (paid2 != null && player.getInventory().countItem(paid2) < amount2)
                return ItemStack.EMPTY;
        }
        // No inventory writes until all validation has succeeded.
        CompoundTag root = original.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag out = pack.copy();
        out.putInt("Version", 2);
        out.putInt("RigLevel", newLevel);
        out.putInt("FabricQuality", newFabric);
        if (!out.contains("Modules", Tag.TAG_LIST)) out.put("Modules", new ListTag());
        if (!out.contains("GeometryVersion", Tag.TAG_INT))
            out.putInt("GeometryVersion", 8);
        else if (out.getInt("GeometryVersion") == 7 && action >= 20 && action <= 22)
            out.putInt("GeometryVersion", 8);
        root.put("FirstShipPack", out);
        ItemStack modified = original.copy();
        modified.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        if (!verifier.loadPacked(modified)) return ItemStack.EMPTY;
        if (!player.getAbilities().instabuild) {
            if (paid1 != null) consume(player, paid1, amount1);
            if (paid2 != null) consume(player, paid2, amount2);
        }
        if (returned1 != null) refund(player, returned1, back1);
        if (returned2 != null) refund(player, returned2, back2);
        return modified;
    }

    private static int countTag(Player player, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
        int sum = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(tag)) sum += stack.getCount();
        }
        return sum;
    }

    private static void consumeTag(Player player, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag, int total) {
        int left = total;
        for (int i = 0; i < player.getInventory().getContainerSize() && left > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(tag)) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        if (left != 0) throw new IllegalStateException("Shipyard tag ingredients changed during validated build");
    }

    private ShipyardUpgrades() {}
}
