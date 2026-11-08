package org.slavicmyths.ships;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Explicit client->server module action: an entity with a small hitbox cannot
 * receive vanilla entity interaction from the bow/port/stern of a 7x11 hull.
 * The client does not modify inventories; all mutations happen after validated
 * server-side position and cell checks in FirstShipNetwork/FirstShipEntity.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipModuleEvents {
    @SubscribeEvent
    public static void shipInteract(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isPassenger() || !player.isShiftKeyDown()) return;
        ItemStack stack = event.getItemStack();
        boolean placing = stack.is(Items.CHEST) || stack.is(Items.BARREL)
                || stack.is(Items.CRAFTING_TABLE) || stack.is(Items.FURNACE);
        for (FirstShipEntity ship : player.level().getEntitiesOfClass(
                FirstShipEntity.class, player.getBoundingBox().inflate(11D, 4D, 11D))) {
            if (ship.isRemoved()) continue;
            FirstShipHull.Point local = FirstShipHull.toLocal(
                    player.getX(), player.getZ(), ship.getX(), ship.getZ(), ship.getYRot());
            int floor = player.getY() > ship.getY() + 1.8D ? 1 : 0;
            int tile = FirstShipEntity.tileAt(local.x(), local.z(), floor);
            if (tile < 0 || Math.abs(player.getY() - (floor == 1 ? ship.deckY() : ship.holdY())) > .7D)
                continue;
            int ix = tile % FirstShipHull.WIDTH;
            int iz = (tile % (FirstShipHull.WIDTH * FirstShipHull.LENGTH)) / FirstShipHull.WIDTH;
            if (!placing && ship.moduleKind(floor, ix, iz) == 0) continue;
            PacketDistributor.sendToServer(new FirstShipNetwork.Command(ship.getId(),
                    event.getHand() == InteractionHand.OFF_HAND
                            ? FirstShipNetwork.MODULE_OFF : FirstShipNetwork.MODULE_MAIN));
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
    }

    private FirstShipModuleEvents() {}
}
