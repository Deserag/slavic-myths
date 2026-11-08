package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.RaftBoatEntity;
import org.slavicmyths.ships.RaftNetwork;

/**
 * E while riding opens the actual server-owned ship menu.
 * Shift + RMB over any deck tile installs equipment even when the target lies
 * outside the native Boat's intentionally small collision hitbox.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class RaftControlsClient {
    @SubscribeEvent
    public static void onInventoryOpening(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null ||
                !(mc.player.getVehicle() instanceof RaftBoatEntity)) return;
        if (!(event.getNewScreen() instanceof InventoryScreen) &&
                !(event.getNewScreen() instanceof CreativeModeInventoryScreen)) return;
        event.setCanceled(true);
        PacketDistributor.sendToServer(new RaftNetwork.OpenShip());
    }

    @SubscribeEvent
    public static void onDeckUse(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null ||
                !mc.player.isShiftKeyDown() || event.getKeyMapping() != mc.options.keyUse) return;
        ItemStack held = mc.player.getMainHandItem();
        RaftBoatEntity closest = null;
        int cell = -1;
        double best = 36D;
        for (RaftBoatEntity raft : mc.level.getEntitiesOfClass(RaftBoatEntity.class,
                mc.player.getBoundingBox().inflate(7D))) {
            if (!raft.supportsEquipment(held) || raft.isRemoved()) continue;
            int candidate = raft.targetTile(mc.player);
            if (candidate < 0) continue;
            double distance = mc.player.distanceToSqr(raft);
            if (distance < best) { best = distance; closest = raft; cell = candidate; }
        }
        if (closest == null) return;
        event.setCanceled(true);
        PacketDistributor.sendToServer(new RaftNetwork.PlaceEquipment(closest.getId(), cell));
    }

    private RaftControlsClient() {}
}
