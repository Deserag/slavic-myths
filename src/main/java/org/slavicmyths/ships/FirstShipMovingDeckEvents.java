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
    private record Contact(int shipId, int floor, long poseFrame) {}
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
                FirstShipEntity.class, player.getBoundingBox().inflate(46D, 50D, 46D))) {
            if (ship.isRemoved() || !ship.isMoving()) continue;
            boolean continuing = last != null && last.shipId() == ship.getId();
            // A newly dismounted player has already been placed at the
            // CURRENT hull pose by vanilla. Reapplying the previous hull
            // transform on that first free tick caused a visible snap.
            boolean newClientFrame = player.level().isClientSide && continuing
                    && ship.clientPoseFrame() != last.poseFrame();
            double oldX = newClientFrame ? ship.clientPreviousX() : ship.getX();
            double oldZ = newClientFrame ? ship.clientPreviousZ() : ship.getZ();
            double oldYaw = newClientFrame ? ship.clientPreviousYaw() : ship.getYRot();
            Vec3 feet = player.position();
            FirstShipHull.Point relative = FirstShipHull.toLocal(
                    feet.x, feet.z, oldX, oldZ, oldYaw);
            int floor = continuing ? last.floor()
                    : ShipDeckContact.contactFloor(ship, relative.x(), relative.z(), feet.y);
            double stairY = ShipDeckContact.stairSurfaceHeight(
                    ship.modularConfiguration(), relative.x(), relative.z(),
                    feet.y - ship.getY());
            boolean deckSupported = floor >= 0 && floor != 5
                    && ShipDeckContact.supports(ship, relative.x(), relative.z(), floor);
            if (!deckSupported && Double.isFinite(stairY)) floor = 5;
            else if (!deckSupported) continue;
            double surface = ship.getY() + (floor == 5
                    ? stairY : ShipDeckContact.height(ship.modularConfiguration(), floor));
            if (feet.y < surface - .56D || feet.y > surface + 2.6D) continue;

            FirstShipHull.Point moved = FirstShipHull.transfer(feet.x, feet.z,
                    oldX, oldZ, oldYaw, ship.getX(), ship.getZ(), ship.getYRot());
            // On the server the ship tick is the sole owner of horizontal
            // transport. PlayerTick may run on either side of that tick.
            double targetX = newClientFrame ? moved.x() : feet.x;
            double targetZ = newClientFrame ? moved.z() : feet.z;
            boolean grounded = feet.y <= surface + .19D
                    && player.getDeltaMovement().y <= .09D;
            double targetY = grounded ? surface + .02D : feet.y;
            if (Math.abs(targetX - feet.x) > 1E-4D
                    || Math.abs(targetZ - feet.z) > 1E-4D)
                ShipDynamicCollision.carryWithWorldCollision(player, ship,
                        new Vec3(targetX - feet.x, 0D, targetZ - feet.z));
            if (Math.abs(targetY - player.getY()) > 1E-4D)
                player.setPos(player.getX(), targetY, player.getZ());
            if (grounded) {
                Vec3 velocity = player.getDeltaMovement();
                if (velocity.y < 0D)
                    player.setDeltaMovement(velocity.x, 0D, velocity.z);
                player.setOnGround(true);
                player.fallDistance = 0F;
            }
            LAST.put(player, new Contact(ship.getId(), floor, ship.clientPoseFrame()));
            return;
        }
        LAST.remove(player);
    }


    /**
     * The ship owns the authoritative transform of crew at every physics step.
     * PlayerTick.Post alone is insufficient: server players (including test
     * connections) may tick before the hull, or not tick in the expected order.
     * Re-evaluate each player's own current position in the OLD ship frame,
     * so normal walking input remains part of the resulting local position.
     */
    public static void carryServerCrew(FirstShipEntity ship, double oldX, double oldZ,
                                       float oldYaw) {
        if (!(ship.level() instanceof net.minecraft.server.level.ServerLevel server)) return;
        double reach = ship.modularConfiguration() != null
                && ship.modularConfiguration().shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && ship.modularConfiguration().hullRevision() == 4 ? 46D : 30D;
        for (Player player : server.getEntitiesOfClass(Player.class,
                new net.minecraft.world.phys.AABB(oldX - reach, ship.getY() - 1D, oldZ - reach,
                        oldX + reach, ship.getY() + 30D, oldZ + reach))) {
            if (!player.isAlive() || player.isPassenger() || player.isSpectator()
                    || player.getAbilities().flying || player.isFallFlying()) continue;
            Vec3 feet = player.position();
            FirstShipHull.Point local = FirstShipHull.toLocal(feet.x, feet.z,
                    oldX, oldZ, oldYaw);
            int floor = -1;
            Contact contact = LAST.get(player);
            if (contact != null && contact.shipId() == ship.getId()
                    && contact.floor() != 5
                    && ShipDeckContact.supports(ship, local.x(), local.z(), contact.floor()))
                floor = contact.floor();
            if (floor < 0) {
                for (int candidate = 4; candidate >= 0; candidate--) {
                    double candidateY = ship.getY() + ShipDeckContact.height(ship.modularConfiguration(), candidate);
                    if (feet.y >= candidateY - .56D && feet.y <= candidateY + 2.6D
                            && ShipDeckContact.supports(ship, local.x(), local.z(), candidate)) {
                        floor = candidate;
                        break;
                    }
                }
            }
            double stairY = ShipDeckContact.stairSurfaceHeight(
                    ship.modularConfiguration(), local.x(), local.z(),
                    feet.y - ship.getY());
            if (floor < 0 && Double.isFinite(stairY)) floor = 5;
            if (floor < 0) continue;
            FirstShipHull.Point moved = FirstShipHull.toWorld(local.x(), local.z(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            double surface = ship.getY() + (floor == 5
                    ? stairY : ShipDeckContact.height(ship.modularConfiguration(), floor));
            boolean grounded = feet.y <= surface + .19D
                    && player.getDeltaMovement().y <= .09D;
            double targetY = grounded ? surface + .02D : feet.y;
            double dx = moved.x() - feet.x, dz = moved.z() - feet.z;
            if (Math.abs(dx) > 1E-4D || Math.abs(dz) > 1E-4D)
                ShipDynamicCollision.carryWithWorldCollision(player, ship,
                        new Vec3(dx, 0D, dz));
            if (Math.abs(targetY - player.getY()) > 1E-4D)
                player.setPos(player.getX(), targetY, player.getZ());
            if (grounded) {
                Vec3 velocity = player.getDeltaMovement();
                if (velocity.y < 0D) player.setDeltaMovement(velocity.x, 0D, velocity.z);
                player.setOnGround(true);
                player.fallDistance = 0F;
            }
            LAST.put(player, new Contact(ship.getId(), floor, ship.clientPoseFrame()));
        }
    }

    private FirstShipMovingDeckEvents() {}
}
