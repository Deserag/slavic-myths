package org.slavicmyths.ships.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.ShipBuildConfiguration;

/** Rebuild the former physical hull's chunk mesh after bulk detachment. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class ShipHullRenderRefreshClient {
    private record Refresh(int stage, long due) {}
    private static final Map<FirstShipEntity, Refresh> PENDING = new WeakHashMap<>();
    private static ClientLevel previousLevel;

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != previousLevel) {
            PENDING.clear();
            previousLevel = mc.level;
        }
        if (mc.level == null || mc.player == null
                || mc.level.getGameTime() % 5L != 0L) return;
        long now = mc.level.getGameTime();
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class,
                mc.player.getBoundingBox().inflate(110D, 55D, 110D))) {
            if (!ship.isMoving() || ship.isRemoved()) {
                PENDING.remove(ship);
                continue;
            }
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (config == null) continue;
            Refresh refresh = PENDING.get(ship);
            if (refresh != null && (refresh.stage() >= 3 || now < refresh.due()))
                continue;
            // The sail/bowsprit reaches beyond the IV hull's nominal length.
            int radius = Math.max(config.length() / 2 + 20,
                    config.width() / 2 + 3);
            BlockPos anchor = ship.blockAnchor();
            mc.levelRenderer.setBlocksDirty(anchor.getX() - radius,
                    anchor.getY() - 2, anchor.getZ() - radius,
                    anchor.getX() + radius, anchor.getY() + 49,
                    anchor.getZ() + radius);
            int stage = refresh == null ? 1 : refresh.stage() + 1;
            PENDING.put(ship, new Refresh(stage,
                    now + (stage == 1 ? 30L : 70L)));
        }
    }

    private ShipHullRenderRefreshClient() {}
}
