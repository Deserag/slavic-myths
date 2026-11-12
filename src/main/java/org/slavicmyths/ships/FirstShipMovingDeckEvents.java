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
    private record Contact(int shipId, int floor, long poseFrame, double feetX, double feetZ) {}
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
                FirstShipEntity.class, player.getBoundingBox().inflate(36D, 20D, 36D))) {
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
            if (floor < 0 || !ShipDeckContact.supports(ship,
                    relative.x(), relative.z(), floor)) continue;
            double surface = ship.getY() + ShipDeckContact.height(floor);
            if (feet.y < surface - .56D || feet.y > surface + 2.6D) continue;

            FirstShipHull.Point moved = FirstShipHull.transfer(feet.x, feet.z,
                    oldX, oldZ, oldYaw, ship.getX(), ship.getZ(), ship.getYRot());
            // On the server the ship tick is the sole owner of horizontal
            // transport. PlayerTick may run on either side of that tick.
            boolean alreadyCarried = newClientFrame && last != null
                    && ship.clientPoseFrame() - last.poseFrame() == 1L
                    && FirstShipHull.likelyAlreadyCarried(
                            new FirstShipHull.Point(last.feetX(), last.feetZ()),
                            new FirstShipHull.Point(feet.x, feet.z),
                            FirstShipHull.transfer(last.feetX(), last.feetZ(),
                                    oldX, oldZ, oldYaw, ship.getX(), ship.getZ(), ship.getYRot()));
            double targetX = newClientFrame && !alreadyCarried ? moved.x() : feet.x;
            double targetZ = newClientFrame && !alreadyCarried ? moved.z() : feet.z;
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
            LAST.put(player, new Contact(ship.getId(), floor, ship.clientPoseFrame(), player.getX(), player.getZ()));
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
        for (Player player : server.getEntitiesOfClass(Player.class,
                new net.minecraft.world.phys.AABB(oldX - 30D, ship.getY() - 1D, oldZ - 30D,
                        oldX + 30D, ship.getY() + 18D, oldZ + 30D))) {
            if (!player.isAlive() || player.isPassenger() || player.isSpectator()
                    || player.getAbilities().flying || player.isFallFlying()) continue;
            Vec3 feet = player.position();
            FirstShipHull.Point local = FirstShipHull.toLocal(feet.x, feet.z,
                    oldX, oldZ, oldYaw);
            int floor = -1;
            Contact contact = LAST.get(player);
            if (contact != null && contact.shipId() == ship.getId()
                    && ShipDeckContact.supports(ship, local.x(), local.z(), contact.floor()))
                floor = contact.floor();
            if (floor < 0) {
                for (int candidate = 4; candidate >= 0; candidate--) {
                    double candidateY = ship.getY() + ShipDeckContact.height(candidate);
                    if (feet.y >= candidateY - .56D && feet.y <= candidateY + 2.6D
                            && ShipDeckContact.supports(ship, local.x(), local.z(), candidate)) {
                        floor = candidate;
                        break;
                    }
                }
            }
            if (floor < 0) continue;
            FirstShipHull.Point moved = FirstShipHull.toWorld(local.x(), local.z(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            double surface = ship.getY() + ShipDeckContact.height(floor);
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
            LAST.put(player, new Contact(ship.getId(), floor, ship.clientPoseFrame(), player.getX(), player.getZ()));
        }
    }

    private FirstShipMovingDeckEvents() {}
}
