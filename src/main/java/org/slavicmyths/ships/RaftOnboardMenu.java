package org.slavicmyths.ships;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;

/**
 * Real onboard inventory, not a client-side copy of cargo.
 * The server owns all ItemStack storage; the 27 visible slots switch
 * between equipment modules using a synchronized page number.
 */
public final class RaftOnboardMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>, MenuType<RaftOnboardMenu>> TYPE =
            RpgMenu.MENUS.register("raft_onboard",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new RaftOnboardMenu(id, inv, buf.readVarInt())));
    private final RaftBoatEntity raft;
    private final SimpleContainerData view = new SimpleContainerData(2);
    private final Container cargo = new OnboardSlots();

    public static void register() {}

    public RaftOnboardMenu(int id, Inventory inventory, int entityId) {
        this(id, inventory,
                inventory.player.level().getEntity(entityId) instanceof RaftBoatEntity candidate ?
                        candidate : null);
    }

    public RaftOnboardMenu(int id, Inventory inventory, RaftBoatEntity vessel) {
        super(TYPE.get(), id);
        raft = vessel;
        addDataSlots(view);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(cargo, row * 9 + col, 12 + col * 18, 46 + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) {
                        return editableCargo() && super.mayPlace(stack);
                    }
                    @Override public boolean mayPickup(Player player) {
                        return editableCargo() && super.mayPickup(player);
                    }
                });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inventory, 9 + row * 9 + col, 12 + col * 18, 137 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col, 12 + col * 18, 195));
        syncPage();
    }

    public RaftBoatEntity raft() { return raft; }
    public int currentPage() { return view.get(0); }
    public int currentKind() { return view.get(1); }
    public int moduleCount() { return raft == null ? 0 : raft.equippedTiles().size(); }

    private int tile() {
        if (raft == null) return -1;
        List<Integer> indices = raft.equippedTiles();
        return view.get(0) >= 0 && view.get(0) < indices.size() ? indices.get(view.get(0)) : -1;
    }

    private Container actualCargo() {
        return raft == null ? null : raft.onboardInventory(tile());
    }

    private boolean editableCargo() { return currentKind() == 1 || currentKind() == 2; }

    private void syncPage() {
        if (raft == null) { view.set(0, 0); view.set(1, 0); return; }
        List<Integer> indices = raft.equippedTiles();
        view.set(0, indices.isEmpty() ? 0 : Math.min(view.get(0), indices.size() - 1));
        int index = tile();
        view.set(1, index < 0 ? 0 : raft.moduleKind(index % raft.deckWidth(), index / raft.deckWidth()));
    }

    @Override public boolean stillValid(Player player) {
        return raft != null && !raft.isRemoved() && player.isAlive() &&
                (player.getVehicle() == raft || player.distanceToSqr(raft) <= 64D);
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || button < 1 || button > 2) return false;
        int count = moduleCount();
        if (count == 0) return false;
        view.set(0, Math.max(0, Math.min(count - 1, view.get(0) + (button == 1 ? -1 : 1))));
        syncPage();
        broadcastChanges();
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (!stillValid(player) || slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        if (slotIndex < 27 && !editableCargo()) return ItemStack.EMPTY;
        ItemStack in = slot.getItem(), saved = in.copy();
        if (slotIndex < 27) {
            if (!moveItemStackTo(in, 27, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!editableCargo() || !moveItemStackTo(in, 0, 27, false)) return ItemStack.EMPTY;
        if (in.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, in);
        return saved;
    }

    private final class OnboardSlots implements Container {
        @Override public int getContainerSize() { return 27; }
        @Override public boolean isEmpty() {
            for (int i = 0; i < 27; i++) if (!getItem(i).isEmpty()) return false;
            return true;
        }
        @Override public ItemStack getItem(int index) {
            Container active = actualCargo();
            return active == null || index < 0 || index >= active.getContainerSize() ?
                    ItemStack.EMPTY : active.getItem(index);
        }
        @Override public ItemStack removeItem(int index, int amount) {
            Container active = actualCargo();
            return active == null || !editableCargo() || index < 0 || index >= active.getContainerSize() ?
                    ItemStack.EMPTY : active.removeItem(index, amount);
        }
        @Override public ItemStack removeItemNoUpdate(int index) {
            Container active = actualCargo();
            return active == null || !editableCargo() || index < 0 || index >= active.getContainerSize() ?
                    ItemStack.EMPTY : active.removeItemNoUpdate(index);
        }
        @Override public void setItem(int index, ItemStack stack) {
            Container active = actualCargo();
            if (active != null && editableCargo() && index >= 0 && index < active.getContainerSize())
                active.setItem(index, stack);
        }
        @Override public void setChanged() {
            Container active = actualCargo();
            if (active != null) active.setChanged();
        }
        @Override public boolean stillValid(Player player) { return RaftOnboardMenu.this.stillValid(player); }
        @Override public void clearContent() {
            // Never erase ship cargo just because a menu is closed.
        }
    }
}
