package org.slavicmyths.ships;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;

/**
 * Real server-authoritative shipyard menu. Buttons use vanilla menu packets:
 * a ship item in slot 0 is upgraded without spawning/destroying any world entity.
 * Its packed cargo stays in CustomData during the size/item conversion.
 */
public final class ShipyardMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>, MenuType<ShipyardMenu>> TYPE =
            RpgMenu.MENUS.register("shipyard", () -> IMenuTypeExtension.create(
                    (id, inventory, buf) -> new ShipyardMenu(id, inventory, buf.readBlockPos())));

    public final BlockPos pos;
    public final SimpleContainer input = new SimpleContainer(1);
    private final Player owner;

    public static void register() {}

    public ShipyardMenu(int id, Inventory inventory, BlockPos pos) {
        super(TYPE.get(), id);
        this.owner = inventory.player;
        this.pos = pos;
        addSlot(new Slot(input, 0, 119, 34) {
            @Override public boolean mayPlace(ItemStack item) { return item.getItem() instanceof RaftItem; }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int y = 0; y < 3; y++) for (int x = 0; x < 9; x++)
            addSlot(new Slot(inventory, x + y * 9 + 9, 40 + x * 18, 205 + y * 18));
        for (int x = 0; x < 9; x++) addSlot(new Slot(inventory, x, 40 + x * 18, 263));
    }

    @Override public boolean stillValid(Player p) {
        return p.isAlive() && p.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64 &&
                p.level().getBlockState(pos).is(RaftContent.SHIPYARD.get());
    }

    @Override public ItemStack quickMoveStack(Player p, int index) {
        if (!stillValid(p) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!(stack.getItem() instanceof RaftItem) || !moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    private boolean canPay(Item ingredient, int count) {
        if (owner.getAbilities().instabuild) return true;
        int total = 0;
        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack stack = owner.getInventory().getItem(i);
            if (stack.is(ingredient)) total += stack.getCount();
        }
        return total >= count;
    }

    private void pay(Item ingredient, int count) {
        if (owner.getAbilities().instabuild) return;
        for (int i = 0; i < owner.getInventory().getContainerSize() && count > 0; i++) {
            ItemStack stack = owner.getInventory().getItem(i);
            if (!stack.is(ingredient)) continue;
            int n = Math.min(count, stack.getCount());
            stack.shrink(n);
            count -= n;
        }
    }

    private void message(Player player, String text) { player.displayClientMessage(Component.literal(text), true); }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer) || !stillValid(player)) return false;
        ItemStack held = input.getItem(0);
        if (button == 0) {
            if (!held.isEmpty()) return false;
            if (!canPay(RaftContent.DECK_SECTION.get(), 9) || !canPay(RaftContent.OARS.get(), 1) ||
                    !canPay(RaftContent.ROPE.get(), 1)) {
                message(player, "Нужно 9 секций, пара вёсел и канат"); return false;
            }
            pay(RaftContent.DECK_SECTION.get(), 9);
            pay(RaftContent.OARS.get(), 1);
            pay(RaftContent.ROPE.get(), 1);
            input.setItem(0, new ItemStack(RaftContent.RAFT_ITEM.get()));
            broadcastChanges();
            return true;
        }
        if (!(held.getItem() instanceof RaftItem raftItem)) {
            message(player, "Поместите плот в ячейку верстака"); return false;
        }
        if (button >= 1 && button <= 3) {
            RaftSize target = RaftSize.byIndex(button);
            RaftSize current = raftItem.size();
            if (target.ordinal() <= current.ordinal()) return false;
            int deckCost = target.width * target.length - current.width * current.length;
            if (!canPay(RaftContent.DECK_SECTION.get(), deckCost) || !canPay(RaftContent.ROPE.get(), 1)) {
                message(player, "Нужны палубные секции (" + deckCost + ") и канат"); return false;
            }
            pay(RaftContent.DECK_SECTION.get(), deckCost);
            pay(RaftContent.ROPE.get(), 1);
            ItemStack next = new ItemStack(RaftContent.sizeItem(target));
            CustomData old = held.get(DataComponents.CUSTOM_DATA);
            if (old != null) {
                CompoundTag data = old.copyTag();
                if (data.contains("Raft2511Pack")) {
                    CompoundTag packed = data.getCompound("Raft2511Pack");
                    // Cargo positions are local x/z; stride changes when the hull grows.
                    ListTag modules = packed.getList("Raft2511Modules", net.minecraft.nbt.Tag.TAG_COMPOUND);
                    for (int i = 0; i < modules.size(); i++) {
                        CompoundTag module = modules.getCompound(i);
                        int tile = module.getInt("Tile");
                        int x = tile % current.width, z = tile / current.width;
                        module.putInt("Tile", z * target.width + x);
                    }
                    packed.putInt("RaftSize", target.ordinal());
                }
                next.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            }
            input.setItem(0, next);
            broadcastChanges();
            message(player, "Плот увеличен до " + target.width + "×" + target.length);
            return true;
        }
        if (button < 10 || button > 17) return false;
        Item ingredient = switch (button) {
            case 10 -> RaftContent.RAILING.get();
            case 11, 12 -> RaftContent.SMALL_SAIL.get();
            case 13 -> RaftContent.HELM.get();
            case 14 -> RaftContent.BALLAST.get();
            case 15 -> RaftContent.LANTERN.get();
            case 16 -> RaftContent.REINFORCEMENT.get();
            case 17 -> RaftContent.OARS.get();
            default -> throw new IllegalStateException();
        };
        int bit = switch (button) {
            case 10 -> 1;
            case 11 -> 2;
            case 12 -> 4;
            case 13 -> 8;
            case 14 -> 16;
            case 15 -> 32;
            case 16 -> 64;
            case 17 -> 128;
            default -> 0;
        };
        if (button == 12 && raftItem.size().ordinal() < RaftSize.WIDE.ordinal()) {
            message(player, "Второй парус доступен с широкого плота 5×5"); return false;
        }
        CompoundTag root = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag packed = root.contains("Raft2511Pack") ? root.getCompound("Raft2511Pack") : new CompoundTag();
        int flags = packed.getInt("RaftUpgrades");
        if ((flags & bit) != 0) return false;
        if (button == 12 && (flags & 2) == 0) {
            message(player, "Сначала установите первый парус"); return false;
        }
        if (!canPay(ingredient, 1)) {
            message(player, "Не хватает детали: " + ingredient.getDescription().getString()); return false;
        }
        pay(ingredient, 1);
        packed.putInt("Version", 1);
        packed.putInt("RaftSize", raftItem.size().ordinal());
        if (!packed.contains("Raft2511Modules")) packed.put("Raft2511Modules", new ListTag());
        packed.putInt("RaftUpgrades", flags | bit);
        root.put("Raft2511Pack", packed);
        ItemStack updated = held.copy();
        updated.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        input.setItem(0, updated);
        broadcastChanges();
        message(player, "Модификация установлена");
        return true;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, input);
    }
}
