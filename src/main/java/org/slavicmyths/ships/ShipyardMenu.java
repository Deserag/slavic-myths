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
        addSlot(new Slot(input, 0, 153, 65) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FirstShipItem ||
                        (stack.getItem() instanceof RaftItem && !RaftCharacteristics.fromItem(stack).invalid());
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col, 198 + col * 18, 211 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 198 + col * 18, 268));
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
            if (!(held.getItem() instanceof RaftItem) && !(held.getItem() instanceof FirstShipItem) ||
                    !moveItemStackTo(held, 0, 1, false))
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
        } else if (action >= 30 && action <= 32) {
            if (!input.isEmpty()) {
                player.displayClientMessage(Component.literal("Сначала заберите готовое судно"), true);
                return false;
            }
            RaftSize size = switch (action) {
                case 30 -> RaftSize.LONG;
                case 31 -> RaftSize.WIDE;
                default -> RaftSize.CARGO;
            };
            ItemStack created = ShipyardUpgrades.buildRaft(player, size);
            if (created.isEmpty()) {
                player.displayClientMessage(Component.literal("Не хватает палубных секций или комплекта вёсел"), true);
                return false;
            }
            input.setItem(0, created);
        } else if (action >= 34 && action <= 37) {
            if (!input.isEmpty()) return false;
            ShipSectionPlan.ShipClass type =
                    ShipSectionPlan.ShipClass.values()[action - 34];
            ItemStack created = ShipyardUpgrades.buildVoxelShip(player, type);
            if (created.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "Не хватает ресурсов для выбранного корпуса или чертёж недоступен"), true);
                return false;
            }
            input.setItem(0, created);
            player.displayClientMessage(Component.literal(
                    "Собран корпус " + type.name() + "; установите его на открытой воде"), true);
        } else if (action >= 40 && action <= 58) {
            ItemStack updated = ShipyardUpgrades.modifyVoxelShip(player, input.getItem(0), action);
            if (updated.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "Модификация недоступна: класс, габариты, груз или материалы"), true);
                return false;
            }
            input.setItem(0, updated);
            player.displayClientMessage(Component.literal(
                    "Чертёж изменён. Постройка появится после установки упакованного корабля"), true);
        } else if (action >= 20 && action <= 25) {
            ItemStack updated = ShipyardUpgrades.outfitFirstShip(player, input.getItem(0), action);
            if (updated.isEmpty()) {
                player.displayClientMessage(Component.literal("Оснащение не выполнено: проверьте судно, детали и текущий уровень"), true);
                return false;
            }
            input.setItem(0, updated);
            player.displayClientMessage(Component.literal("Оснащение подготовлено — заберите и установите корабль"), true);
        } else if (action == 11) {
            if (!input.isEmpty()) {
                player.displayClientMessage(Component.literal("Заберите судно из ячейки перед постройкой"), true);
                return false;
            }
            ItemStack created = ShipyardUpgrades.buildFirstShip(player);
            if (created.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "Корабль 7×11: брёвна 48, доски 96, железо 32, нити 24, секции 32, борта 4, руль 1. Паруса отдельно"), true);
                return false;
            }
            input.setItem(0, created);
            player.displayClientMessage(Component.literal("Деревянный двухпалубный корабль построен"), true);
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
        } else if (action >= 7 && action <= 10) {
            ItemStack current = input.getItem(0);
            ItemStack changed = ShipyardUpgrades.dismantle(player, current, action);
            if (changed.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "Снять деталь нельзя: проверьте оснащение и место в инвентаре"), true);
                return false;
            }
            input.setItem(0, changed);
            player.displayClientMessage(Component.literal("Деталь снята и возвращена в инвентарь"), true);
        } else return false;
        broadcastChanges();
        return true;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, input);
    }
}
