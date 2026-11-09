package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;

/**
 * Carries the LOCAL walking player exactly once on each authoritative native
 * hull anchor/heading update. The server remains responsible for position
 * packets and validation; this is the client-side motion the old block-only
 * deck adapter omitted. Ordinary walks/jumps are never overwritten each tick.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipDeckClient {
    private static int lastShipId = -1;
    private static BlockPos lastAnchor;
    private static int lastTurns;
    private static Vec3 lastFeet;

    private static void clear() {
        lastShipId = -1;
        lastAnchor = null;
        lastFeet = null;
    }

    private static boolean aboard(Vec3 feet, BlockPos anchor, int turn) {
        FirstShipHull.Point local = FirstShipHull.toLocal(feet.x, feet.z,
                anchor.getX() + .5D, anchor.getZ() + .5D, turn * 90D);
        if (!FirstShipHull.withinHull(local.x(), local.z(), .12D)) return false;
        double y = feet.y - anchor.getY();
        return (y >= FirstShipHull.HOLD_FLOOR - .3D
                        && y <= FirstShipHull.HOLD_FLOOR + 2.85D)
                || (y >= FirstShipHull.MAIN_DECK - .3D
                        && y <= FirstShipHull.MAIN_DECK + 2.3D);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || !minecraft.player.isAlive() || minecraft.player.isPassenger()
                || minecraft.player.isSpectator() || minecraft.player.getAbilities().flying
                || minecraft.player.isFallFlying()) {
            clear();
            return;
        }
        Vec3 feet = minecraft.player.position();
        FirstShipEntity ship = null;
        double nearest = Double.POSITIVE_INFINITY;
        for (FirstShipEntity candidate : minecraft.level.getEntitiesOfClass(
                FirstShipEntity.class, minecraft.player.getBoundingBox().inflate(12D, 9D, 12D))) {
            if (candidate.isRemoved() || !candidate.isNativeHull()) continue;
            BlockPos anchor = candidate.blockAnchor();
            boolean same = lastShipId == candidate.getId() && lastAnchor != null
                    && lastFeet != null && aboard(lastFeet, lastAnchor, lastTurns);
            if (!same && !aboard(feet, anchor, candidate.quarterTurns())) continue;
            double distance = candidate.distanceToSqr(minecraft.player);
            if (distance < nearest) { ship = candidate; nearest = distance; }
        }
        if (ship == null) { clear(); return; }

        BlockPos anchor = ship.blockAnchor();
        int turns = ship.quarterTurns();
        if (ship.getId() == lastShipId && lastAnchor != null && lastFeet != null
                && (!anchor.equals(lastAnchor) || turns != lastTurns)
                && aboard(lastFeet, lastAnchor, lastTurns)) {
            double oldX = lastAnchor.getX() + .5D, oldZ = lastAnchor.getZ() + .5D;
            double newX = anchor.getX() + .5D, newZ = anchor.getZ() + .5D;
            FirstShipHull.Point expected = FirstShipHull.transfer(
                    lastFeet.x, lastFeet.z, oldX, oldZ, lastTurns * 90D,
                    newX, newZ, turns * 90D);
            // A server teleport can arrive before this synced anchor packet.
            // Never apply the same transform twice in that ordering.
            double alreadyMoved = Math.hypot(feet.x - expected.x(), feet.z - expected.z());
            double notMoved = Math.hypot(feet.x - lastFeet.x, feet.z - lastFeet.z);
            if (alreadyMoved > .35D && alreadyMoved > notMoved) {
                FirstShipHull.Point updated = FirstShipHull.transfer(
                        feet.x, feet.z, oldX, oldZ, lastTurns * 90D,
                        newX, newZ, turns * 90D);
                minecraft.player.setPos(updated.x(), feet.y, updated.z());
                minecraft.player.fallDistance = 0F;
                feet = minecraft.player.position();
            }
        }
        if (!aboard(feet, anchor, turns)) { clear(); return; }
        lastShipId = ship.getId();
        lastAnchor = anchor;
        lastTurns = turns;
        lastFeet = feet;
    }

    private FirstShipDeckClient() {}
}
