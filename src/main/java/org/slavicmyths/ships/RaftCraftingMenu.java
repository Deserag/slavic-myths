package org.slavicmyths.ships;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/** Standard vanilla 3x3 crafting recipes on a moving raft; verifies the real vessel instead of a world crafting-table block. */
public final class RaftCraftingMenu extends CraftingMenu {
    private final UUID vesselId;
    public RaftCraftingMenu(int windowId, Inventory inventory, RaftBoatEntity raft) {
        super(windowId, inventory, ContainerLevelAccess.create(raft.level(), raft.blockPosition()));
        vesselId = raft.getUUID();
    }
    @Override public boolean stillValid(Player player) {
        if (player.level().isClientSide) return true;
        if (!(player.level() instanceof ServerLevel level)) return false;
        Entity entity = level.getEntity(vesselId);
        return entity instanceof RaftBoatEntity raft && !raft.isRemoved() && player.distanceToSqr(raft) <= 64D;
    }
}
