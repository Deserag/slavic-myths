package org.slavicmyths.ships;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Dynamic deck and hold contact on a MOVING ship: its physical vanilla planks
 * have temporarily become an entity-owned palette, so normal world collision
 * cannot support a walker until the ship parks.
 *
 * Both logical sides apply only each ship TRANSFORM DELTA, not a teleport to a
 * fixed local offset every tick. Real walking input is kept; jumping is
 * allowed; the synthetic floor cancels falling only at the deck/hold surface.
 * This does not affect any stationary ship or existing raft.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class FirstShipMovingDeckEvents {
    private record Contact(int shipId, double x, double z, float yaw,
                           int floor, Vec3 previousFeet) {}
    private static final Map<Player, Contact> LAST =
            Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || player.isFallFlying()) {
            LAST.remove(player);
            return;
        }
        Contact last = LAST.get(player);
        for (FirstShipEntity ship : player.level().getEntitiesOfClass(
                FirstShipEntity.class, player.getBoundingBox().inflate(12D, 9D, 12D))) {
            if (ship.isRemoved() || !ship.isMoving()) continue;
            boolean continuing = last != null && last.shipId() == ship.getId();
            double oldX = continuing ? last.x() : ship.getX();
            double oldZ = continuing ? last.z() : ship.getZ();
            double oldYaw = continuing ? last.yaw() : ship.getYRot();
            Vec3 feet = player.position();
            FirstShipHull.Point relative = FirstShipHull.toLocal(
                    feet.x, feet.z, oldX, oldZ, oldYaw);
            boolean deck = FirstShipHull.mainDeckSupports(relative.x(), relative.z());
            boolean hold = FirstShipHull.holdSupports(relative.x(), relative.z());
            if (!deck && !hold) continue;

            double deckY = ship.deckY(), holdY = ship.holdY();
            int floor;
            if (continuing) floor = last.floor();
            else if (deck && feet.y >= deckY - .25D && feet.y <= deckY + .42D) floor = 1;
            else if (hold && feet.y >= holdY - .25D && feet.y <= holdY + .42D) floor = 0;
            else continue;
            double surface = floor == 1 ? deckY : holdY;
            if ((floor == 1 && !deck) || (floor == 0 && !hold)
                    || feet.y < surface - .56D || feet.y > surface + 2.6D) continue;

            FirstShipHull.Point moved = FirstShipHull.transfer(
                    feet.x, feet.z, oldX, oldZ, oldYaw,
                    ship.getX(), ship.getZ(), ship.getYRot());
            boolean alreadyMoved = false;
            if (continuing && last.previousFeet() != null) {
                FirstShipHull.Point expected = FirstShipHull.transfer(
                        last.previousFeet().x, last.previousFeet().z,
                        oldX, oldZ, oldYaw, ship.getX(), ship.getZ(), ship.getYRot());
                // Server correction arrived before the ship entity packet:
                // do not transfer an already-carried player twice.
                double distExpected = Math.hypot(feet.x - expected.x(), feet.z - expected.z());
                double distOld = Math.hypot(feet.x - last.previousFeet().x,
                        feet.z - last.previousFeet().z);
                alreadyMoved = distExpected < .055D && distExpected < distOld;
            }
            // The server already carries walking crew in FirstShipEntity.tick.
            // The client predicts that same transform for its LOCAL player.
            // Applying both server carriers would double a movement step when
            // entity and PlayerTick order differs between integrated/dedicated.
            boolean clientPrediction = player.level().isClientSide;
            double targetX = !clientPrediction || alreadyMoved ? feet.x : moved.x();
            double targetZ = !clientPrediction || alreadyMoved ? feet.z : moved.z();
            boolean grounded = feet.y <= surface + .19D
                    && player.getDeltaMovement().y <= .09D;
            double targetY = grounded ? surface + .02D : feet.y;
            if (Math.abs(targetX - feet.x) > 1E-4D
                    || Math.abs(targetZ - feet.z) > 1E-4D
                    || Math.abs(targetY - feet.y) > 1E-4D)
                player.setPos(targetX, targetY, targetZ);
            if (grounded) {
                Vec3 velocity = player.getDeltaMovement();
                if (velocity.y < 0D)
                    player.setDeltaMovement(velocity.x, 0D, velocity.z);
                player.setOnGround(true);
                player.fallDistance = 0F;
            }
            LAST.put(player, new Contact(ship.getId(), ship.getX(), ship.getZ(),
                    ship.getYRot(), floor, player.position()));
            return;
        }
        LAST.remove(player);
    }

    private FirstShipMovingDeckEvents() {}
}
