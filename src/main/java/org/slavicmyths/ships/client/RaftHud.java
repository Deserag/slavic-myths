package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slavicmyths.ships.RaftBoatEntity;
import org.slavicmyths.ships.RaftWind;

/**
 * Full vessel dossier now lives in the shipyard or the captain's E screen.
 * The HUD must not cover the helm, centre of deck, or crosshair while sailing.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class RaftHud {
    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof RaftBoatEntity raft)) return;
        RaftWind.Sample wind = RaftWind.at(mc.level.getGameTime(),
                raft.getX(), raft.getZ(), mc.level.isRaining(), mc.level.isThundering());
        double speed = raft.getDeltaMovement().horizontalDistance() * 20D;
        var gg = event.getGuiGraphics();
        int x = Math.max(8, gg.guiWidth() - 163), y = Math.max(8, gg.guiHeight() - 61);
        gg.fill(x - 5, y - 4, x + 158, y + 51, 0xaf2d241a);
        gg.drawString(mc.font, "СУДНО • E — ОСМОТР", x, y, 0xffffd89c, false);
        gg.drawString(mc.font, String.format(java.util.Locale.ROOT,
                "Ход %.1f блок/с", speed), x, y + 14, 0xffb3dfb4, false);
        gg.drawString(mc.font, String.format(java.util.Locale.ROOT,
                "Ветер к %s  •  парус %.0f%%",
                wind.direction(), wind.sailFactor(raft.getYRot()) * 100D),
                x, y + 28, 0xffa7cddd, false);
    }
    private RaftHud() {}
}
