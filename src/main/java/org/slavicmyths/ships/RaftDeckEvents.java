package org.slavicmyths.ships;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * First native moving-platform support without Create.
 * Keeps a non-mounted walking player at the deck plane as a moving raft translates
 * and turns, while allowing voluntary jumps and stepping over an unrailed edge.
 * Client and server run the same bounded calculation; dedicated-server QA required.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class RaftDeckEvents {
    @SubscribeEvent
    public static void walk(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isPassenger() || player.isSpectator() || player.isFallFlying()) return;
        AABB search = player.getBoundingBox().inflate(7.5D, 1.8D, 7.5D);
        for (RaftBoatEntity raft : player.level().getEntitiesOfClass(RaftBoatEntity.class, search)) {
            if (raft.isRemoved()) continue;
            double deckY = raft.deckTop(), feet = player.getY();
            if (feet < deckY - .26D || feet > deckY + .52D || player.getDeltaMovement().y > .13D) continue;
            // Rotate player position into the ship's rectangle.
            double dx = player.getX() - raft.getX(), dz = player.getZ() - raft.getZ();
            double heading = Math.toRadians(raft.getYRot());
            double localX = dx * Math.cos(heading) + dz * Math.sin(heading);
            double localZ = -dx * Math.sin(heading) + dz * Math.cos(heading);
            double halfW = raft.deckWidth() / 2D, halfL = raft.deckLength() / 2D;
            if (Math.abs(localX) > halfW + .12D || Math.abs(localZ) > halfL + .12D) continue;
            // No trampoline: falling naturally or jumping away overrides support.
            double movementX = raft.getX() - raft.xo, movementZ = raft.getZ() - raft.zo;
            if (Math.abs(movementX) > 1.0D || Math.abs(movementZ) > 1.0D) continue;
            double turning = Math.toRadians(net.minecraft.util.Mth.wrapDegrees(raft.getYRot() - raft.yRotO));
            double nx = dx * Math.cos(turning) - dz * Math.sin(turning);
            double nz = dx * Math.sin(turning) + dz * Math.cos(turning);
            double targetX = raft.getX() + nx, targetZ = raft.getZ() + nz;
            if (raft.hasRails()) {
                // Rail collision is ship-local and permits exiting at the stern via a gap.
                double newX = nx * Math.cos(heading) + nz * Math.sin(heading);
                double newZ = -nx * Math.sin(heading) + nz * Math.cos(heading);
                if (Math.abs(newX) > halfW - .2D) targetX = raft.getX() + Math.copySign(halfW - .2D, newX) * Math.cos(heading) - newZ * Math.sin(heading);
                if (newZ < -(halfL - .2D)) targetZ = raft.getZ() + newX * Math.sin(heading) - (halfL - .2D) * Math.cos(heading);
            }
            player.setPos(targetX, deckY, targetZ);
            Vec3 velocity = player.getDeltaMovement();
            if (velocity.y < 0D) player.setDeltaMovement(velocity.x, 0D, velocity.z);
            player.setOnGround(true);
            player.fallDistance = 0;
            return;
        }
    }
    private RaftDeckEvents() {}
}
