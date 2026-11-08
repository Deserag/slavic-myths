package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * One source of truth for item descriptions, shipyard inspection and live raft HUD.
 * Never decode an inventory from the item just to display its cargo summary.
 */
public final class RaftCharacteristics {
    public static final int CREW_CAPACITY = 4;
    public static final double ONE_SAIL_LIMIT = 0.50D;
    public static final double SECOND_SAIL_BONUS = 0.15D;
    public static final double ROWING_BONUS = 0.12D;

    public record Info(RaftSize size, int sails, boolean rails, boolean helm, boolean extraOars,
                       int chests, int barrels, int craftingTables, int furnaces, int storedStacks,
                       boolean packed, boolean invalid) {
        public int inventorySlots() { return 27 * (chests + barrels) + 3 * furnaces; }
        public int usedCells() { return chests + barrels + craftingTables + furnaces; }
        public int cells() { return size.width * size.length; }
        public String mode() { return sails > 0 && helm ? "Парусный + гребной" : "Гребной"; }
        public double sailLimit() { return sails == 0 || !helm ? 0D : ONE_SAIL_LIMIT + (sails - 1) * SECOND_SAIL_BONUS; }
        public double combinedLimit() { return sailLimit() + (sails > 0 && helm ? ROWING_BONUS : 0D); }
    }

    public static Info empty(RaftSize size) {
        return new Info(size, 0, false, false, false, 0, 0, 0, 0, 0, false, false);
    }

    public static Info fromItem(ItemStack stack) {
        if (!(stack.getItem() instanceof RaftItem item)) return null;
        RaftSize size = item.size();
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return empty(size);
        CompoundTag root = data.copyTag();
        if (!root.contains("Raft2511Pack", Tag.TAG_COMPOUND)) return empty(size);
        CompoundTag packed = root.getCompound("Raft2511Pack");
        int version = packed.getInt("Version");
        boolean invalid = version != 1 && version != 2;
        if (version == 2 && (packed.getInt("RaftWidth") != size.width ||
                packed.getInt("RaftLength") != size.length)) invalid = true;
        int sails = version == 2 ? packed.getInt("RaftSails") : 0;
        if (sails < 0 || sails > size.maxSails()) invalid = true;
        int flags = version == 2 ? packed.getInt("RaftFlags") : 0;
        ListTag modules = packed.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        if (modules.size() > size.width * size.length) invalid = true;
        boolean[] taken = new boolean[size.width * size.length];
        int[] counts = new int[5];
        int items = 0;
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile"), kind = module.getInt("Kind");
            if (tile < 0 || tile >= taken.length || kind < 1 || kind > 4 || taken[tile]) {
                invalid = true;
                continue;
            }
            taken[tile] = true;
            counts[kind]++;
            ListTag stacks = module.getList("Items", Tag.TAG_COMPOUND);
            if (stacks.size() > (kind == 4 ? 3 : 27)) invalid = true;
            items += stacks.size();
        }
        return new Info(size, Math.max(0, Math.min(size.maxSails(), sails)),
                (flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0,
                counts[1], counts[2], counts[3], counts[4], items, true, invalid);
    }

    public static Info fromEntity(RaftBoatEntity raft) {
        int[] counts = new int[5];
        for (int z = 0; z < raft.deckLength(); z++) {
            for (int x = 0; x < raft.deckWidth(); x++) {
                int kind = raft.moduleKind(x, z);
                if (kind > 0 && kind < counts.length) counts[kind]++;
            }
        }
        return new Info(raft.raftSize(), raft.sailCount(), raft.hasRails(), raft.hasHelm(),
                raft.hasExtraOars(), counts[1], counts[2], counts[3], counts[4],
                -1, false, false);
    }

    public static List<Component> tooltip(Info info) {
        List<Component> lines = new ArrayList<>();
        if (info == null) return lines;
        lines.add(Component.literal("Палуба " + info.size.width + "×" + info.size.length +
                " • экипаж до " + CREW_CAPACITY).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Движение: " + info.mode()).withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Паруса: " + info.sails + "/" + info.size.maxSails() +
                " • руль " + (info.helm ? "есть" : "нет")).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Ограждения: " + (info.rails ? "есть" : "нет") +
                " • доп. вёсла: " + (info.extraOars ? "есть" : "нет")).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Оборудование: " + info.usedCells() + "/" + info.cells() +
                " клеток • хранение " + info.inventorySlots() + " слотов").withStyle(ChatFormatting.GRAY));
        if (info.sailLimit() > 0) {
            lines.add(Component.literal("Лимит парусной тяги: ≈" +
                    String.format(java.util.Locale.ROOT, "%.1f", info.sailLimit() * 20D) + " блок/с")
                    .withStyle(ChatFormatting.GREEN));
            lines.add(Component.literal("С греблей: до ≈" +
                    String.format(java.util.Locale.ROOT, "%.1f", info.combinedLimit() * 20D) + " блок/с")
                    .withStyle(ChatFormatting.GREEN));
        } else lines.add(Component.literal("Гребля: физика обычной лодки; скорость зависит от условий")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (info.packed) lines.add(Component.literal("Упакованное судно" +
                (info.storedStacks >= 0 ? " • предметных стопок: " + info.storedStacks : ""))
                .withStyle(ChatFormatting.GOLD));
        if (info.invalid) lines.add(Component.literal("ВНИМАНИЕ: повреждённые данные судна")
                .withStyle(ChatFormatting.RED));
        return lines;
    }

    private RaftCharacteristics() {}
}
