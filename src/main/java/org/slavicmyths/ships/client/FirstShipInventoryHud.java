package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;

/**
 * E opens the NORMAL player inventory plus a read-only ship overview.
 * Never swaps the inventory menu (which would break slots and recipes).
 * Data is limited to synchronized module types; cargo ItemStacks remain server-only.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipInventoryHud {
    @SubscribeEvent
    public static void inventory(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        Player player = mc.player;
        FirstShipEntity ship = null;
        if (player.getVehicle() instanceof FirstShipEntity steering) ship = steering;
        if (ship == null) {
            for (FirstShipEntity nearby : mc.level.getEntitiesOfClass(
                    FirstShipEntity.class, player.getBoundingBox().inflate(10D, 5D, 10D))) {
                if (nearby.isRemoved()) continue;
                FirstShipHull.Point p = FirstShipHull.toLocal(
                        player.getX(), player.getZ(), nearby.getX(), nearby.getZ(), nearby.getYRot());
                if (FirstShipHull.withinHull(p.x(), p.z(), .20D)
                        && player.getY() >= nearby.holdY() - .3D
                        && player.getY() <= nearby.deckY() + 1.7D) {
                    ship = nearby;
                    break;
                }
            }
        }
        if (ship == null) return;
        GuiGraphics g = event.getGuiGraphics();
        int width = g.guiWidth(), height = g.guiHeight();
        int x = Math.min(width - 194, width / 2 + 92);
        int y = Math.max(8, height / 2 - 93);
        g.fill(x, y, x + 190, y + 198, 0xef2c2016);
        g.fill(x + 2, y + 2, x + 188, y + 4, 0xffbb9656);
        g.drawString(mc.font, "КОРАБЛЬ 7×11", x + 10, y + 10, 0xffffd99d, false);
        g.drawString(mc.font, "ПАЛУБА", x + 15, y + 25, 0xffe2c6a2, false);
        g.drawString(mc.font, "ТРЮМ", x + 111, y + 25, 0xffe2c6a2, false);
        int[] count = new int[4];
        for (int floor = 0; floor < 2; floor++) {
            int ox = x + (floor == 0 ? 103 : 9);
            for (int z = 0; z < FirstShipHull.LENGTH; z++)
                for (int ix = 0; ix < FirstShipHull.WIDTH; ix++) {
                    double cx = ix + .5D - FirstShipHull.WIDTH / 2D;
                    double cz = z + .5D - FirstShipHull.LENGTH / 2D;
                    boolean within = FirstShipHull.withinHull(cx, cz, .31D);
                    boolean deck = floor == 0
                            ? FirstShipHull.mainDeckSupports(cx, cz)
                            : FirstShipHull.holdSupports(cx, cz);
                    int type = ship.moduleKind(floor == 0 ? 1 : 0, ix, z);
                    if (type >= 1 && type <= 4) count[type - 1]++;
                    int color = !within ? 0xff29231d : !deck ? 0xff090b0c : switch (type) {
                        case 1 -> 0xffe0b45e;
                        case 2 -> 0xffa7764d;
                        case 3 -> 0xff81aab1;
                        case 4 -> 0xffaf8686;
                        default -> 0xff755a3d;
                    };
                    g.fill(ox + ix * 10, y + 40 + (FirstShipHull.LENGTH - 1 - z) * 10,
                            ox + ix * 10 + 9, y + 49 + (FirstShipHull.LENGTH - 1 - z) * 10, color);
                }
        }
        g.drawString(mc.font, "Сундуки " + count[0] + "  Бочки " + count[1],
                x + 9, y + 155, 0xffe9d6b8, false);
        g.drawString(mc.font, "Верстаки " + count[2] + "  Печи " + count[3],
                x + 9, y + 167, 0xffe9d6b8, false);
        g.drawString(mc.font, Component.literal("Груз: Shift+ПКМ по судну"),
                x + 9, y + 181, 0xffc1bdab, false);
    }

    private FirstShipInventoryHud() {}
}
