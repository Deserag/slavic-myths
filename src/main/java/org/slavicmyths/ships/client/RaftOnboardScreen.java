package org.slavicmyths.ships.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.ships.RaftCharacteristics;
import org.slavicmyths.ships.RaftOnboardMenu;

/**
 * E on the captain's seat: compact ship overview with real paged storage.
 * Slots map directly to live server containers, never NBT client copies.
 */
public final class RaftOnboardScreen extends AbstractContainerScreen<RaftOnboardMenu> {
    public RaftOnboardScreen(RaftOnboardMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 345;
        imageHeight = 237;
    }

    private void page(int action) {
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), b -> page(1))
                .bounds(leftPos + 10, topPos + 20, 23, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> page(2))
                .bounds(leftPos + 150, topPos + 20, 23, 18).build());
    }

    @Override protected void renderBg(GuiGraphics gg, float t, int mx, int my) {
        gg.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffa17b4d);
        gg.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xff362a21);
        gg.fill(leftPos + 7, topPos + 15, leftPos + 179, topPos + 228, 0xff715037);
        gg.fill(leftPos + 184, topPos + 15, leftPos + 337, topPos + 228, 0xffe2cda5);
        gg.fill(leftPos + 186, topPos + 17, leftPos + 335, topPos + 226, 0xfff0e2c6);
        for (int i = 0; i < 27; i++) {
            int x = 12 + i % 9 * 18, y = 46 + i / 9 * 18;
            gg.fill(leftPos + x - 1, topPos + y - 1,
                    leftPos + x + 17, topPos + y + 17, 0xff2e241a);
        }
        for (int i = 0; i < 36; i++) {
            int x = 12 + i % 9 * 18;
            int y = i < 27 ? 137 + i / 9 * 18 : 195;
            gg.fill(leftPos + x - 1, topPos + y - 1,
                    leftPos + x + 17, topPos + y + 17, 0xff2e241a);
        }
    }

    @Override protected void renderLabels(GuiGraphics gg, int mx, int my) {
        gg.drawString(font, "СУДОВОЙ ЖУРНАЛ", 12, 6, 0xfff5d8a4, false);
        gg.drawString(font, "Груз " + (menu.moduleCount() == 0 ? "0/0" :
                (menu.currentPage() + 1) + "/" + menu.moduleCount()), 57, 25, 0xfff7dcaf, false);
        String kind = switch (menu.currentKind()) {
            case 1 -> "Сундук";
            case 2 -> "Бочка";
            case 3 -> "Верстак";
            case 4 -> "Печь";
            default -> "Нет оборудования";
        };
        gg.drawString(font, kind, 13, 108, 0xffeed4a6, false);
        gg.drawString(font, "ИНВЕНТАРЬ ИГРОКА", 13, 125, 0xffeed4a6, false);
        gg.drawString(font, "ОБЗОР СУДНА", 192, 25, 0xff563d25, false);
        if (menu.raft() != null) {
            RaftCharacteristics.Info info = RaftCharacteristics.fromEntity(menu.raft());
            List<Component> properties = RaftCharacteristics.tooltip(info);
            int y = 43;
            for (Component line : properties) {
                for (var wrapped : font.split(line, 137)) {
                    if (y > 211) break;
                    gg.drawString(font, wrapped, 192, y, 0xff483b2d, false);
                    y += 11;
                }
                if (y > 211) break;
            }
        }
        if (menu.currentKind() == 4 || menu.currentKind() == 3)
            gg.drawString(font, "Осмотр без переноса", 192, 212, 0xff725a40, false);
    }

    @Override public void render(GuiGraphics gg, int mx, int my, float t) {
        super.render(gg, mx, my, t);
        renderTooltip(gg, mx, my);
    }
}
