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
        if (raft == null || raft.isRemoved()) return;
        RaftCharacteristics.Info info = RaftCharacteristics.fromEntity(raft);
        List<Component> rows = RaftCharacteristics.tooltip(info);
        var graphics = event.getGuiGraphics();
        int x = Math.max(5, graphics.guiWidth() - 210);
        int y = Math.max(5, graphics.guiHeight() / 2 - 42);
        int height = 24 + rows.size() * 10;
        graphics.fill(x - 5, y - 7, x + 201, y + height, 0xc4221a12);
        graphics.fill(x - 3, y - 5, x + 199, y - 3, 0xffb99463);
        graphics.drawString(mc.font, "ОСНАЩЕНИЕ СУДНА", x, y, 0xffffdd94, false);
        y += 14;
        for (Component row : rows) {
            String text = mc.font.plainSubstrByWidth(row.getString(), 192);
            graphics.drawString(mc.font, text, x, y, 0xfff2e4cb, false);
            y += 10;
        }
        if (mc.player.getVehicle() == raft) {
            double speed = raft.getDeltaMovement().horizontalDistance() * 20.0D;
            graphics.drawString(mc.font, String.format(java.util.Locale.ROOT,
                    "Ход: %.1f блок/с", speed), x, y + 2, 0xffbadcaa, false);
        }
    }
    private RaftHud() {}
}
