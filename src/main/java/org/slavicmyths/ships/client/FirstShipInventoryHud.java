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
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.FirstShipHull;

/** Separate inventory and ship views keep recipe controls and JEI outside the ship overview. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipInventoryHud {
    private static final java.util.Map<InventoryScreen, net.minecraft.client.gui.components.Button[]> TABS = new java.util.WeakHashMap<>();
    public static FirstShipEntity currentShip() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
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

        return ship;
    }
    @SubscribeEvent public static void init(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        var inventory = net.minecraft.client.gui.components.Button.builder(Component.literal("Инвентарь"), b -> {}).bounds(0,0,86,20).build();
        inventory.active = false;
        var ship = net.minecraft.client.gui.components.Button.builder(Component.literal("Корабль"), b -> {
            if (currentShip() != null) Minecraft.getInstance().setScreen(new ShipScreen());
        }).bounds(0,0,86,20).build();
        event.addListener(inventory); event.addListener(ship);
        TABS.put(screen, new net.minecraft.client.gui.components.Button[]{inventory,ship});
    }
    @SubscribeEvent public static void inventory(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        var tabs = TABS.get(screen); if (tabs == null) return;
        boolean onShip = currentShip() != null;
        for (int i=0;i<tabs.length;i++) {
            tabs[i].visible = onShip;
            tabs[i].setPosition(screen.getGuiLeft()+i*90, Math.max(0,screen.getGuiTop()-23));
        }
    }
    private static void drawShip(GuiGraphics g, FirstShipEntity ship, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        g.fill(x, y, x + 190, y + 198, 0xef2c2016);
        g.fill(x + 2, y + 2, x + 188, y + 4, 0xffbb9656);
        g.drawString(mc.font, "КОРАБЛЬ 7×11", x + 10, y + 10, 0xffffd99d, false);
        g.drawString(mc.font, "ПАЛУБА", x + 15, y + 25, 0xffe2c6a2, false);
        g.drawString(mc.font, "ТРЮМ", x + 111, y + 25, 0xffe2c6a2, false);
        int[] count = new int[4];
        for (int floor = 0; floor < 2; floor++) {
            int ox = x + (floor == 0 ? 9 : 103);
            for (int z = 0; z < FirstShipHull.LENGTH; z++)
                for (int ix = 0; ix < FirstShipHull.WIDTH; ix++) {
                    double cx = ix + .5D - FirstShipHull.WIDTH / 2D;
                    double cz = z + .5D - FirstShipHull.LENGTH / 2D;
                    boolean within = FirstShipHull.withinHull(cx, cz, .31D);
                    boolean deck = floor == 0
                            ? FirstShipHull.mainDeckSupports(cx, cz)
                            : FirstShipHull.holdSupports(cx, cz);
                    int type = ship.isNativeHull()
                            ? FirstShipBlockHull.cargoKind(mc.level, ship.blockAnchor(),
                                    ship.quarterTurns(), floor == 0 ? 1 : 0, ix, z)
                            : ship.moduleKind(floor == 0 ? 1 : 0, ix, z);
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
        g.drawString(mc.font, Component.literal("Груз: обычные сундуки/бочки"),
                x + 9, y + 181, 0xffc1bdab, false);

    }
    public static final class ShipScreen extends net.minecraft.client.gui.screens.Screen {
        public ShipScreen() { super(Component.literal("Корабль")); }
        @Override protected void init() {
            int x=(width-190)/2, y=Math.max(24,(height-198)/2);
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Инвентарь"),b->minecraft.setScreen(new InventoryScreen(minecraft.player))).bounds(x,y-23,93,20).build());
            var tab=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Корабль"),b->{}).bounds(x+97,y-23,93,20).build());
            tab.active=false;
        }
        @Override public void tick() { if(currentShip()==null&&minecraft.player!=null)minecraft.setScreen(new InventoryScreen(minecraft.player)); }
        @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float partialTick) {}
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick) {

            var ship=currentShip();
            if(ship!=null)drawShip(g,ship,(width-190)/2,Math.max(24,(height-198)/2));
            super.render(g,mouseX,mouseY,partialTick);
        }
    }
    private FirstShipInventoryHud() {}
}