package org.slavicmyths.ships;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The SERVER never teleports a deck walker each tick.
 * Its former PlayerTickEvent.Post setPos fought against client movement packets
 * and was the primary cause of rubber-banding and frame-to-frame shaking.
 * Only fall support is performed here, while local client applies ship transform.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class RaftDeckEvents {
    @SubscribeEvent public static void support(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() ||
                player.isPassenger() || player.isSpectator() || player.isFallFlying()) return;
        if (player.getDeltaMovement().y > .09D) return;
        for (RaftBoatEntity raft : player.level().getEntitiesOfClass(
                RaftBoatEntity.class, player.getBoundingBox().inflate(7D, 1.15D, 7D))) {
            if (raft.isRemoved()) continue;
            double deck = raft.deckTop();
            if (player.getY() < deck - .32D || player.getY() > deck + .25D) continue;
            RaftTransforms.Flat local = RaftTransforms.toLocal(
                    player.getX() - raft.getX(), player.getZ() - raft.getZ(), raft.getYRot());
            if (Math.abs(local.x()) > raft.deckWidth() / 2D ||
                    Math.abs(local.z()) > raft.deckLength() / 2D) continue;
            Vec3 velocity = player.getDeltaMovement();
            if (velocity.y < 0D) player.setDeltaMovement(velocity.x, 0D, velocity.z);
            player.setOnGround(true);
            player.fallDistance = 0F;
            return;
        }
    }
    private RaftDeckEvents() {}
}
