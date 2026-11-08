package org.slavicmyths.ships;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Experimental deck-attachment physics for *non-passenger* players.
 *
 * Boats have no walkable block collision, so we solve support at the deck plane
 * and apply the vessel's translation/rotation to a walker's LOCAL offset.
 * Requires client + dedicated-server gameplay validation; toggle using
 * -Dslavicmyths.raft.deckWalk=false if it interferes with other mods.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class RaftDeckWalk {
    private record Anchor(UUID raftId, double x, double z, float yaw) {}
    private static final Map<Player, Anchor> ANCHORS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("slavicmyths.raft.deckWalk", "true"));

    @SubscribeEvent public static void afterPlayerTick(PlayerTickEvent.Post event) {
        if (!ENABLED) return;
        Player player = event.getEntity();
        if (!player.isAlive() || player.isPassenger() || player.isSpectator()) {
            ANCHORS.remove(player);
            return;
        }
        var nearby = player.level().getEntitiesOfClass(RaftBoatEntity.class, player.getBoundingBox().inflate(6D),
                raft -> !raft.isRemoved());
        RaftBoatEntity chosen = null;
        Anchor old = ANCHORS.get(player);
        if (old != null) for (RaftBoatEntity raft : nearby) {
            if (raft.getUUID().equals(old.raftId())) { chosen = raft; break; }
        }
        if (chosen == null) {
            for (RaftBoatEntity raft : nearby) {
                if (overDeck(player, raft, .65D)) { chosen = raft; break; }
            }
        }
        if (chosen == null) { ANCHORS.remove(player); return; }
        double deck = chosen.getY() + .50D;
        if (old != null && old.raftId().equals(chosen.getUUID())) {
            // Preserve the player's position relative to the *previous* raft pose,
            // including his own WASD movement, and rotate that offset with the hull.
            double prevAngle = Math.toRadians(old.yaw());
            double dx = player.getX() - old.x(), dz = player.getZ() - old.z();
            double localX = dx * Math.cos(prevAngle) + dz * Math.sin(prevAngle);
            double localZ = -dx * Math.sin(prevAngle) + dz * Math.cos(prevAngle);
            // A fitted railing is a real gameplay boundary, not merely a visual prop.
            // Players may still jump over it: only walking height is constrained.
            if (chosen.hasUpgrade(1) && player.getY() < deck + .72D) {
                double boundX = chosen.raftSize().width / 2D - .45D;
                double boundZ = chosen.raftSize().length / 2D - .45D;
                localX = Math.clamp(localX, -boundX, boundX);
                localZ = Math.clamp(localZ, -boundZ, boundZ);
            }
            double nowAngle = Math.toRadians(chosen.getYRot());
            double x = chosen.getX() + localX * Math.cos(nowAngle) - localZ * Math.sin(nowAngle);
            double z = chosen.getZ() + localX * Math.sin(nowAngle) + localZ * Math.cos(nowAngle);
            player.setPos(x, player.getY(), z);
        }
        if (overDeck(player, chosen, .85D)) {
            // An upward jump is voluntary; do not pull a jumper back down.
            if (player.getDeltaMovement().y <= .05D && player.getY() < deck) {
                player.setPos(player.getX(), deck, player.getZ());
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x, 0D, velocity.z);
                player.setOnGround(true);
                player.fallDistance = 0F;
            }
            ANCHORS.put(player, new Anchor(chosen.getUUID(), chosen.getX(), chosen.getZ(), chosen.getYRot()));
        } else ANCHORS.remove(player);
    }

    private static boolean overDeck(Player player, RaftBoatEntity raft, double tolerance) {
        double deck = raft.getY() + .50D;
        if (player.getY() < deck - tolerance || player.getY() > deck + 1.1D) return false;
        double yaw = Math.toRadians(raft.getYRot()), dx = player.getX() - raft.getX(), dz = player.getZ() - raft.getZ();
        double x = dx * Math.cos(yaw) + dz * Math.sin(yaw);
        double z = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
        return Math.abs(x) <= raft.raftSize().width / 2D - .28D &&
               Math.abs(z) <= raft.raftSize().length / 2D - .28D;
    }

    private RaftDeckWalk() {}
}
