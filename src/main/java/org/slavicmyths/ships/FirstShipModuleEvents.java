package org.slavicmyths.ships;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * A 7x11 vessel is much larger than the native boat's 2.95-wide hitbox.
 * Shift+right-click IN AIR while standing on a deck works even near the bow.
 * Handles only this ship and only valid ship-local cells; normal block and
 * entity interactions outside the ship remain untouched.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class FirstShipModuleEvents {
    @SubscribeEvent
    public static void shipInteract(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isPassenger() || !player.isShiftKeyDown()) return;
        ItemStack stack = event.getItemStack();
        boolean installing = stack.is(Items.CHEST) || stack.is(Items.BARREL)
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
            if (!installing && ship.moduleKind(floor, ix, iz) == 0) continue;
            InteractionResult result = ship.interactFromDeck(player, event.getHand());
            event.setCancellationResult(result == InteractionResult.FAIL ? InteractionResult.FAIL : InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
    }

    private FirstShipModuleEvents() {}
}
