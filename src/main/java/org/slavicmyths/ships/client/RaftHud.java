package org.slavicmyths.ships.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slavicmyths.ships.RaftBoatEntity;
import org.slavicmyths.ships.RaftCharacteristics;

/**
 * Compact, read-only ship info while aiming at a raft or operating it.
 * Only synced entity fields are read: do not expose or replicate cargo contents.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class RaftHud {
    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || mc.options.hideGui) return;
        RaftBoatEntity raft = null;
        if (mc.hitResult instanceof EntityHitResult aimed && aimed.getEntity() instanceof RaftBoatEntity candidate) {
            raft = candidate;
        } else if (mc.player.getVehicle() instanceof RaftBoatEntity riding) {
            raft = riding;
        }
        if (raft == null) raft = aimedAtDeck(mc);
        if (raft == null || raft.isRemoved()) return;
        RaftCharacteristics.Info info = RaftCharacteristics.fromEntity(raft);
        List<Component> rows = RaftCharacteristics.tooltip(info);
        var graphics = event.getGuiGraphics();
        int x = Math.max(5, graphics.guiWidth() - 210);
        int y = Math.max(5, graphics.guiHeight() / 2 - 42);
        int height = 43 + rows.size() * 10;
        graphics.fill(x - 5, y - 7, x + 201, y + height, 0xc4221a12);
        graphics.fill(x - 3, y - 5, x + 199, y - 3, 0xffb99463);
        graphics.drawString(mc.font, "ОСНАЩЕНИЕ СУДНА", x, y, 0xffffdd94, false);
        y += 14;
        for (Component row : rows) {
            String text = mc.font.plainSubstrByWidth(row.getString(), 192);
            graphics.drawString(mc.font, text, x, y, 0xfff2e4cb, false);
            y += 10;
        }
        org.slavicmyths.ships.RaftWind.Sample breeze = org.slavicmyths.ships.RaftWind.at(
                mc.level.getGameTime(), raft.getX(), raft.getZ(),
                mc.level.isRaining(), mc.level.isThundering());
        String wind = String.format(java.util.Locale.ROOT,
                "Ветер к %s • сила %.0f%% • парус %.0f%%",
                breeze.direction(), breeze.strength() * 100D,
                breeze.sailFactor(raft.getYRot()) * 100D);
        graphics.drawString(mc.font, mc.font.plainSubstrByWidth(wind, 192),
                x, y + 2, 0xff9ec9dd, false);
        double speed = raft.getDeltaMovement().horizontalDistance() * 20.0D;
        graphics.drawString(mc.font, String.format(java.util.Locale.ROOT,
                "Ход: %.1f блок/с", speed), x, y + 13, 0xffbadcaa, false);
    }
    /**
     * Bigger hulls have deliberately retained the vanilla boat's small collision box.
     * Intersect the visual deck rectangle so looking at a 5x7 deck edge still reveals
     * its properties without making an invisible 7x7 collision obstacle.
     */
    private static RaftBoatEntity aimedAtDeck(Minecraft mc) {
        var player = mc.player;
        if (player == null) return null;
        net.minecraft.world.phys.Vec3 eye = player.getEyePosition();
        net.minecraft.world.phys.Vec3 direction = player.getLookAngle();
        if (Math.abs(direction.y) < 0.015D) return null;
        RaftBoatEntity closest = null;
        double nearest = 6D;
        for (RaftBoatEntity raft : player.level().getEntitiesOfClass(
                RaftBoatEntity.class, player.getBoundingBox().inflate(8.0D))) {
            if (raft.isRemoved()) continue;
            double t = (raft.deckTop() - eye.y) / direction.y;
            if (t <= 0D || t >= nearest) continue;
            net.minecraft.world.phys.Vec3 point = eye.add(direction.scale(t));
            double dx = point.x - raft.getX(), dz = point.z - raft.getZ();
            double radians = Math.toRadians(raft.getYRot());
            double lx = dx * Math.cos(radians) + dz * Math.sin(radians);
            double lz = -dx * Math.sin(radians) + dz * Math.cos(radians);
            if (Math.abs(lx) <= raft.deckWidth() / 2.0D &&
                    Math.abs(lz) <= raft.deckLength() / 2.0D) {
                closest = raft;
                nearest = t;
            }
        }
        return closest;
    }

    private RaftHud() {}
}
