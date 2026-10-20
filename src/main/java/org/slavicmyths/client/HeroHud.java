package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slavicmyths.bogatyr.IlyaEntity;
import org.slavicmyths.marinka.DobrynyaEntity;
import org.slavicmyths.skim.AlyoshaEntity;
import org.slavicmyths.finist.FinistEntity;

/** All loaded owned companions in a compact stack; no server timers or expanded scans. */
@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT)
public final class HeroHud {
    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || mc.options.hideGui
                || !org.slavicmyths.bogatyr.BogatyrConfig.HUD.get()) return;
        var loaded = new ArrayList<HeroHudView.Entry>();
        for (var entity : mc.level.entitiesForRendering()) {
            UUID owner = entity instanceof IlyaEntity h ? h.syncedOwner() : entity instanceof DobrynyaEntity h ? h.syncedOwner()
                    : entity instanceof AlyoshaEntity h ? h.syncedOwner() : entity instanceof FinistEntity h ? h.syncedOwner() : null;
            if (!mc.player.getUUID().equals(owner) || !(entity instanceof LivingEntity h)
                    || !h.isAlive() || mc.player.distanceToSqr(h) > 64 * 64) continue;
            boolean resting = org.slavicmyths.kalin.KalinParty.recovering(h);
            String key = retreating(h) ? "hero_hud.slavicmyths.retreating"
                    : resting ? "gui.slavicmyths.174.recovery" : "hero_hud.slavicmyths.active";
            if (h instanceof FinistEntity f && f.bird() == 4) key = "hero_hud.slavicmyths.cursed";
            loaded.add(new HeroHudView.Entry(h.getUUID(), owner,
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(h.getType()).toString(),
                    h.getName().getString(), h.getHealth(), h.getMaxHealth(),
                    Component.translatable(key).getString(), resting, mc.player.distanceToSqr(h), h.isAlive()));
        }
        var heroes = HeroHudView.select(mc.player.getUUID(), loaded);
        if (heroes.isEmpty()) return;
        var graphics = event.getGuiGraphics();
        var slots = HeroHudView.layout(graphics.guiWidth(), graphics.guiHeight(), heroes.size(),
                org.slavicmyths.bogatyr.HeroHudConfig.X.get(), org.slavicmyths.bogatyr.HeroHudConfig.Y.get());
        graphics.pose().pushPose();
        try {
            graphics.pose().scale(HeroHudView.SCALE, HeroHudView.SCALE, 1);
            HeroHudView.draw(new HeroHudView.Canvas() {
                public int width(String text) { return mc.font.width(text); }
                public String trim(String text, int width) { return mc.font.plainSubstrByWidth(text, width); }
                public void fill(int x, int y, int right, int bottom, int color) { graphics.fill(x, y, right, bottom, color); }
                public void text(String text, int x, int y, int color) { graphics.drawString(mc.font, text, x, y, color); }
            }, heroes, slots);
        } finally {
            graphics.pose().popPose();
        }
    }
    private static boolean retreating(LivingEntity hero) {
        if (hero instanceof IlyaEntity h) return h.state() == org.slavicmyths.bogatyr.BogatyrRules.State.RETREAT_START
                || h.state() == org.slavicmyths.bogatyr.BogatyrRules.State.RETREAT_MOVING;
        if (hero instanceof DobrynyaEntity h) return h.action() == org.slavicmyths.marinka.WitchAction.RETREAT;
        if (hero instanceof AlyoshaEntity h) return h.action() == org.slavicmyths.skim.SkimAction.RETREAT;
        return hero instanceof FinistEntity h && h.action() == org.slavicmyths.finist.FalconAction.RETREAT;
    }
    private HeroHud() { }
}
