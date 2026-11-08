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
 * Experimental BLOCKLESS moving multi-floor support for the first ship.
 *
 * The previous ship pose is the only translation/rotation applied; the
 * player's movement relative to that previous pose is NEVER erased.
 * Each world/player side keeps separate contact state. Helm riders are excluded.
 *
 * This is a physics adapter, not proof of multiplayer interpolation or network
 * prediction. Those require a real integrated client + dedicated-server test.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class FirstShipDeckEvents {
    private record Contact(int id, double x, double z, float yaw, int floor, boolean climbing) {}
    private static final Map<Player, Contact> CONTACT =
            Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.isFallFlying() || player.getAbilities().flying) {
            CONTACT.remove(player);
            return;
        }
        Contact last = CONTACT.get(player);
        AABB search = player.getBoundingBox().inflate(12D, 4D, 12D);
        for (FirstShipEntity ship : player.level().getEntitiesOfClass(FirstShipEntity.class, search)) {
            if (ship.isRemoved()) continue;
            boolean continuing = last != null && last.id() == ship.getId();
            double px = player.getX(), pz = player.getZ(), py = player.getY();
            if (continuing) {
                FirstShipHull.Point carried = FirstShipHull.transfer(px, pz,
                        last.x(), last.z(), last.yaw(), ship.getX(), ship.getZ(), ship.getYRot());
                px = carried.x(); pz = carried.z();
            }
            FirstShipHull.Point local = FirstShipHull.toLocal(px, pz,
                    ship.getX(), ship.getZ(), ship.getYRot());
            double x = local.x(), z = local.z();
            boolean within = FirstShipHull.withinHull(x, z, continuing ? 0D : .24D);
            if (!within && !(continuing && last.floor() == 0)) continue;

            double upper = ship.deckY(), lower = ship.holdY();
            int floor = continuing ? last.floor() : -1;
            if (!continuing) {
                if (FirstShipHull.mainDeckSupports(x, z) && py >= upper - .22D && py <= upper + .28D) floor = 1;
                else if (FirstShipHull.holdSupports(x, z) && py >= lower - .25D && py <= lower + .28D) floor = 0;
            }
            if (floor < 0) continue;

            boolean hatch = FirstShipHull.hatch(x, z);
            boolean ladder = FirstShipHull.onLadder(x, z);
            boolean climbing = continuing && last.climbing();

            // A jump beside the actual wooden ladder starts a climb, which ends
            // above the hatch. Sneaking in the hatch allows a controlled descent.
            if (floor == 0 && ladder && player.getDeltaMovement().y > .09D) climbing = true;
            if (climbing && !ladder) climbing = false;
            if (climbing) {
                py = Math.min(upper + .02D, Math.max(py, lower) + .19D);
                if (py >= upper) { floor = 1; climbing = false; }
            } else if (floor == 1 && hatch && player.isShiftKeyDown()) {
                // No invisible upper floor across the hatch opening.
                py = Math.max(lower + .02D, py - .17D);
                if (py <= lower + .06D) floor = 0;
            } else if (floor == 1 && hatch && py < upper - .38D) {
                floor = 0;
            }

            // The lower hold is bounded by the hull, not by a single boat AABB.
            if (floor == 0) {
                FirstShipHull.Point confined = FirstShipHull.confineHold(x, z);
                x = confined.x(); z = confined.z();
                if (!FirstShipHull.hatch(x, z) && py > upper - 1.81D) {
                    py = upper - 1.81D; // head cannot clip through the upper deck
                    if (player.getDeltaMovement().y > 0D)
                        player.setDeltaMovement(player.getDeltaMovement().multiply(1D, 0D, 1D));
                }
            }

            // The player is free to jump or leap OVER the upper gunwale.
            // Merely walking cannot cross its reinforced wooden side.
            if (floor == 1 && !hatch && py <= upper + .26D) {
                FirstShipHull.Point confined = FirstShipHull.confineHold(x, z);
                x = confined.x(); z = confined.z();
            }

            if (climbing || (floor == 1 && hatch && player.isShiftKeyDown())) {
                Vec3 speed = player.getDeltaMovement();
                player.setDeltaMovement(speed.x, 0D, speed.z);
                player.fallDistance = 0F;
            } else {
                double target = floor == 1 ? upper : lower;
                boolean supporting = floor == 0
                        ? FirstShipHull.holdSupports(x, z)
                        : FirstShipHull.mainDeckSupports(x, z);
                // Never snap an intentional jumper down to a floor.
                if (supporting && py <= target + .15D
                        && py >= target - (continuing ? .75D : .24D)
                        && player.getDeltaMovement().y <= .09D) {
                    py = target;
                    Vec3 speed = player.getDeltaMovement();
                    if (speed.y < 0D) player.setDeltaMovement(speed.x, 0D, speed.z);
                    player.setOnGround(true);
                    player.fallDistance = 0F;
                }
            }

            FirstShipHull.Point world = FirstShipHull.toWorld(x, z,
                    ship.getX(), ship.getZ(), ship.getYRot());
            if (Math.abs(world.x() - player.getX()) > .002D
                    || Math.abs(world.z() - player.getZ()) > .002D
                    || Math.abs(py - player.getY()) > .002D) {
                player.setPos(world.x(), py, world.z());
            }
            CONTACT.put(player, new Contact(ship.getId(), ship.getX(),
                    ship.getZ(), ship.getYRot(), floor, climbing));
            return;
        }
        CONTACT.remove(player);
    }

    private FirstShipDeckEvents() {}
}
