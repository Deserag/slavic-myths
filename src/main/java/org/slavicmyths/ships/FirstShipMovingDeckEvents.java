package org.slavicmyths.ships;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Moving-contraption deck support for independently walking players.
 * A dynamic hull is NOT a normal collidable chunk floor. Carry people in
 * both logical worlds, applying only the delta between the old and new
 * ship frames and preserving their free walking/jumping displacement.
 * The previous native-block deck carrier must ignore voyaging ships.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class FirstShipMovingDeckEvents {
    private record Contact(int shipId, double x, double z, float yaw) {}
    private static final Map<Player, Contact> PREVIOUS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.getAbilities().flying || player.isFallFlying()) {
            PREVIOUS.remove(player);
            return;
        }
        Contact prior = PREVIOUS.get(player);
        FirstShipEntity found = null;
        double nearest = Double.POSITIVE_INFINITY;
        AABB box = player.getBoundingBox().inflate(13D, 9D, 13D);
        for (FirstShipEntity ship : player.level().getEntitiesOfClass(FirstShipEntity.class, box)) {
            if (!ship.isVoyaging() || ship.isRemoved()) continue;
            double distance = ship.distanceToSqr(player);
            if (distance < nearest) {
                FirstShipHull.Point local = FirstShipHull.toLocal(player.getX(), player.getZ(),
                        ship.getX(), ship.getZ(), ship.shipYaw());
                if (FirstShipHull.withinHull(local.x(), local.z(), .02D)
                        || prior != null && prior.shipId == ship.getId()) {
                    found = ship;
                    nearest = distance;
                }
            }
        }
        if (found == null) { PREVIOUS.remove(player); return; }
        FirstShipEntity ship = found;
        Vec3 feet = player.position();
        if (prior != null && prior.shipId == ship.getId()) {
            FirstShipHull.Point carried = FirstShipHull.transfer(feet.x, feet.z,
                    prior.x, prior.z, prior.yaw, ship.getX(), ship.getZ(), ship.shipYaw());
            if (Math.hypot(feet.x - carried.x(), feet.z - carried.z()) > .00001D)
                player.setPos(carried.x(), feet.y, carried.z());
        }
        FirstShipHull.Point local = FirstShipHull.toLocal(player.getX(), player.getZ(),
                ship.getX(), ship.getZ(), ship.shipYaw());
        boolean lower = player.getY() < ship.getY() + 3.0D;
        double floor = lower ? ship.holdY() : ship.deckY();
        boolean supported = lower
                ? FirstShipHull.holdSupports(local.x(), local.z())
                : FirstShipHull.mainDeckSupports(local.x(), local.z());
        if (!supported) {
            // Open central hatch allows climbing or dropping to the hold.
            if (!FirstShipHull.withinHull(local.x(), local.z(), -.05D)) {
                PREVIOUS.remove(player);
                return;
            }
            if (!lower && FirstShipHull.hatch(local.x(), local.z())) {
                PREVIOUS.put(player, new Contact(ship.getId(),
                        ship.getX(), ship.getZ(), ship.shipYaw()));
                return;
            }
            FirstShipHull.Point limited = lower
                    ? FirstShipHull.confineHold(local.x(), local.z())
                    : FirstShipHull.confineDeck(local.x(), local.z());
            FirstShipHull.Point target = FirstShipHull.toWorld(limited.x(), limited.z(),
                    ship.getX(), ship.getZ(), ship.shipYaw());
            player.setPos(target.x(), player.getY(), target.z());
        }
        double feetY = player.getY();
        if (feetY >= floor - .55D && feetY <= floor + .20D
                && player.getDeltaMovement().y <= .09D) {
            player.setPos(player.getX(), floor + .015D, player.getZ());
            Vec3 velocity = player.getDeltaMovement();
            if (velocity.y < 0D)
                player.setDeltaMovement(velocity.x, 0D, velocity.z);
            player.setOnGround(true);
            player.fallDistance = 0F;
        }
        PREVIOUS.put(player, new Contact(ship.getId(),
                ship.getX(), ship.getZ(), ship.shipYaw()));
    }

    private FirstShipMovingDeckEvents() {}
}
