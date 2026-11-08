package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;

/**
 * One *real* vessel inspection slot, player inventory and validated server actions.
 * Source item never remains in both inventory and workbench: it lives in the
 * transient menu container and is returned on close.
 */
public final class ShipyardMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>, MenuType<ShipyardMenu>> TYPE =
            RpgMenu.MENUS.register("shipyard_inspection",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ShipyardMenu(id, inv, buf.readBlockPos())));
    private final BlockPos pos;
    private final SimpleContainer input = new SimpleContainer(1);
    private final Player owner;

    public static void register() {}

    public ShipyardMenu(int id, Inventory inventory, BlockPos position) {
        super(TYPE.get(), id);
        pos = position.immutable();
        owner = inventory.player;
        addSlot(new Slot(input, 0, 31, 45) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof RaftItem && !RaftCharacteristics.fromItem(stack).invalid();
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col, 12 + col * 18, 155 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 12 + col * 18, 213));
        }
    }

    public ItemStack inspected() { return input.getItem(0); }

    @Override public boolean stillValid(Player player) {
        return player.isAlive() && !player.isSpectator() &&
                player.level().getBlockState(pos).is(RaftContent.SHIPYARD_BENCH.get()) &&
                player.distanceToSqr(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D) <= 64D;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack held = source.getItem(), original = held.copy();
        if (index == 0) {
            if (!moveItemStackTo(held, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!(held.getItem() instanceof RaftItem) || !moveItemStackTo(held, 0, 1, false))
                return ItemStack.EMPTY;
        }
        if (held.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, held);
        return original;
    }

    @Override public boolean clickMenuButton(Player player, int action) {
        if (!(player instanceof ServerPlayer) || !stillValid(player)) return false;
        if (action == 1) {
            ItemStack current = input.getItem(0);
            ItemStack upgraded = ShipyardUpgrades.upgrade(player, current);
            if (upgraded.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        current.isEmpty() ? "Поместите предмет плота в ячейку" :
                        "Невозможно улучшить: проверьте секции, размер и сохранённые данные"), true);
                return false;
            }
            input.setItem(0, upgraded);
            player.displayClientMessage(Component.literal("Судно улучшено — оборудование и груз сохранены"), true);
        } else if (action == 2) {
            if (!input.isEmpty()) {
                player.displayClientMessage(Component.literal("Сначала освободите ячейку судна"), true);
                return false;
            }
            ItemStack created = ShipyardUpgrades.build(player);
            if (created.isEmpty()) {
                player.displayClientMessage(Component.literal("Нужно 9 палубных секций и 1 комплект вёсел"), true);
                return false;
            }
            input.setItem(0, created);
            player.displayClientMessage(Component.literal("Малый плот 3×3 готов"), true);
        } else if (action >= 3 && action <= 6) {
            ItemStack current = input.getItem(0);
            ItemStack updated = ShipyardUpgrades.equip(player, current, action);
            if (updated.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "Улучшение недоступно: проверьте детали, размер или оснащение"), true);
                return false;
            }
            input.setItem(0, updated);
            player.displayClientMessage(Component.literal("Модификация установлена; оборудование и груз не изменены"), true);
        } else return false;
        broadcastChanges();
        return true;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, input);
    }
}
