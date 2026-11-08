package org.slavicmyths.ships.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.ships.RaftBoatEntity;

/**
 * One movement writer: the LOCAL client's player, never both client and server.
 * Retains local position when the hull moves/rotates. Server validates fall
 * support separately and receives normal vanilla client position packets.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class RaftDeckClient {
    private record Contact(int entityId, double x, double z, float yaw) {}
    private static final Map<Player, Contact> LAST = new WeakHashMap<>();

    @SubscribeEvent
    public static void walk(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = event.getEntity();
        if (mc.player != player || player.isPassenger() || !player.isAlive() ||
                player.isSpectator() || player.isFallFlying() || mc.screen != null) {
            LAST.remove(player);
            return;
        }
        if (player.getDeltaMovement().y > .09D) {
            LAST.remove(player);
            return;
        }
        Contact prev = LAST.get(player);
        for (RaftBoatEntity raft : player.level().getEntitiesOfClass(
                RaftBoatEntity.class, player.getBoundingBox().inflate(8D, 1.4D, 8D))) {
            if (raft.isRemoved()) continue;
            double surface = raft.deckTop(), feet = player.getY();
            boolean linked = prev != null && prev.entityId == raft.getId();
            if (feet < surface - (linked ? .33D : .28D) ||
                    feet > surface + (linked ? .40D : .30D)) continue;
            double dx = player.getX() - raft.getX(), dz = player.getZ() - raft.getZ();
            if (linked) {
                // Player walk delta remains in the prior ship-local frame.
                double px = player.getX() - prev.x, pz = player.getZ() - prev.z;
                double turn = Math.toRadians(Mth.wrapDegrees(raft.getYRot() - prev.yaw));
                dx = px * Math.cos(turn) - pz * Math.sin(turn);
                dz = px * Math.sin(turn) + pz * Math.cos(turn);
            }
            double yaw = Math.toRadians(raft.getYRot());
            double lx = dx * Math.cos(yaw) + dz * Math.sin(yaw);
            double lz = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
            double halfW = raft.deckWidth() / 2D, halfL = raft.deckLength() / 2D;
            if (Math.abs(lx) > halfW + .08D || Math.abs(lz) > halfL + .08D) {
                if (linked) LAST.remove(player);
                continue;
            }
            if (raft.hasRails()) {
                lx = Mth.clamp(lx, -halfW + .30D, halfW - .30D);
                lz = Mth.clamp(lz, -halfL + .30D, halfL - .30D);
                dx = lx * Math.cos(yaw) - lz * Math.sin(yaw);
                dz = lx * Math.sin(yaw) + lz * Math.cos(yaw);
            }
            double nextX = raft.getX() + dx, nextZ = raft.getZ() + dz;
            if (Math.abs(nextX - player.getX()) > .001D ||
                    Math.abs(nextZ - player.getZ()) > .001D ||
                    Math.abs(surface - feet) > .015D)
                player.setPos(nextX, surface, nextZ);
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < 0D) player.setDeltaMovement(motion.x, 0D, motion.z);
            player.setOnGround(true);
            player.fallDistance = 0F;
            LAST.put(player, new Contact(raft.getId(), raft.getX(), raft.getZ(), raft.getYRot()));
            return;
        }
        LAST.remove(player);
    }
    private RaftDeckClient() {}
}
