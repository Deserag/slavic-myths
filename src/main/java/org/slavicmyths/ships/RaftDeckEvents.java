package org.slavicmyths.ships;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Prototype blockless deck contact. Runs independently for client/server players.
 * Unlike the previous implementation, it remembers the previous ship transform:
 * no unconditional 20 Hz teleport to a changing boat position and no oversized
 * boat entity hitbox fighting against the player's movement.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class RaftDeckEvents {
    private record Contact(int raftId, double x, double z, float yaw) {}
    // Integrated single-player uses separate render and server threads.
    // A weak-key map avoids retaining disconnected Player instances and is synchronized
    // so the two sides cannot corrupt the shared map.
    private static final Map<Player, Contact> LAST =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void walk(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isPassenger() || player.isSpectator() || player.isFallFlying()) {
            LAST.remove(player);
            return;
        }
        // Positive Y velocity means an intentional jump. Do not snap a jumper to the deck.
        if (player.getDeltaMovement().y > .09D) {
            LAST.remove(player);
            return;
        }
        Contact previous = LAST.get(player);
        AABB search = player.getBoundingBox().inflate(8D, 1.35D, 8D);
        for (RaftBoatEntity raft : player.level().getEntitiesOfClass(RaftBoatEntity.class, search)) {
            // Native Boat status can remain UNDER_WATER with our custom locked waterline;
            // it must not prevent landing on the visibly dry raft deck.
            if (raft.isRemoved()) continue;
            double deckY = raft.deckTop();
            double feet = player.getY();
            // A player must land on TOP of the deck, not be swept upward from water underneath.
            boolean continuing = previous != null && previous.raftId == raft.getId();
            if (feet < deckY - (continuing ? .25D : .12D) ||
                    feet > deckY + (continuing ? .35D : .28D)) continue;

            double dx = player.getX() - raft.getX(), dz = player.getZ() - raft.getZ();
            if (continuing) {
                // Preserve the player's own walked offset, then apply only ship translation+turn.
                double turn = Math.toRadians(Mth.wrapDegrees(raft.getYRot() - previous.yaw));
                double previousDx = player.getX() - previous.x;
                double previousDz = player.getZ() - previous.z;
                dx = previousDx * Math.cos(turn) - previousDz * Math.sin(turn);
                dz = previousDx * Math.sin(turn) + previousDz * Math.cos(turn);
            }
            double heading = Math.toRadians(raft.getYRot());
            double localX = dx * Math.cos(heading) + dz * Math.sin(heading);
            double localZ = -dx * Math.sin(heading) + dz * Math.cos(heading);
            double halfW = raft.deckWidth() / 2D, halfL = raft.deckLength() / 2D;
            if (Math.abs(localX) > halfW + .14D || Math.abs(localZ) > halfL + .14D) {
                if (continuing) LAST.remove(player);
                continue;
            }
            if (raft.hasRails()) {
                // Only people standing on deck are limited by side rails. Jumping remains free.
                localX = Mth.clamp(localX, -.0D - halfW + .27D, halfW - .27D);
                localZ = Mth.clamp(localZ, -.0D - halfL + .27D, halfL - .27D);
                dx = localX * Math.cos(heading) - localZ * Math.sin(heading);
                dz = localX * Math.sin(heading) + localZ * Math.cos(heading);
            }
            double nextX = raft.getX() + dx, nextZ = raft.getZ() + dz;
            if (Math.abs(nextX - player.getX()) > .015D || Math.abs(nextZ - player.getZ()) > .015D ||
                    Math.abs(feet - deckY) > .025D) {
                player.setPos(nextX, deckY, nextZ);
            }
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < 0D) player.setDeltaMovement(motion.x, 0D, motion.z);
            player.setOnGround(true);
            player.fallDistance = 0F;
            LAST.put(player, new Contact(raft.getId(), raft.getX(), raft.getZ(), raft.getYRot()));
            return;
        }
        LAST.remove(player);
    }

    private RaftDeckEvents() {}
}
