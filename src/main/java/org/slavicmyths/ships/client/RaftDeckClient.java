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
import org.slavicmyths.ships.RaftTransforms;

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
            // Use exactly the same 180-yaw model transform as RaftRenderer.
            // Ship motion/turn is applied ONCE, while walking input remains local.
            RaftTransforms.Flat local = linked ?
                    RaftTransforms.toLocal(player.getX() - prev.x,
                            player.getZ() - prev.z, prev.yaw) :
                    RaftTransforms.toLocal(player.getX() - raft.getX(),
                            player.getZ() - raft.getZ(), raft.getYRot());
            double lx = local.x(), lz = local.z();
            double halfW = raft.deckWidth() / 2D, halfL = raft.deckLength() / 2D;
            if (Math.abs(lx) > halfW + .08D || Math.abs(lz) > halfL + .08D) {
                if (linked) LAST.remove(player);
                continue;
            }
            if (raft.hasRails()) {
                lx = Mth.clamp(lx, -halfW + .30D, halfW - .30D);
                lz = Mth.clamp(lz, -halfL + .30D, halfL - .30D);
            }
            RaftTransforms.Flat next = RaftTransforms.toWorld(lx, lz, raft.getYRot());
            double nextX = raft.getX() + next.x(), nextZ = raft.getZ() + next.z();
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
