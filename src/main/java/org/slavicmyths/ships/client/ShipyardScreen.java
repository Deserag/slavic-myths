package org.slavicmyths.ships.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.ships.ShipyardMenu;

/** Illustrated dark-oak/sandstone shipyard panel; controls send vanilla menu button packets. */
public final class ShipyardScreen extends AbstractContainerScreen<ShipyardMenu> {
    private static final String[] LABELS = {
            "Создать плот 3×3", "Удлинить до 3×5", "Расширить до 5×5", "Удлинить до 5×7",
            "Ограждения", "Малый парус", "Второй парус", "Рулевой пост",
            "Балласт", "Судовой фонарь", "Усилить корпус", "Доп. гребные места"
    };
    private static final int[] BUTTONS = {0, 1, 2, 3, 10, 11, 12, 13, 14, 15, 16, 17};

    public ShipyardScreen(ShipyardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 260;
        imageHeight = 292;
        inventoryLabelY = 195;
    }

    @Override protected void init() {
        super.init();
        for (int i = 0; i < LABELS.length; i++) {
            final int id = BUTTONS[i];
            int x = leftPos + 13 + (i % 2) * 120;
            int y = topPos + 64 + (i / 2) * 22;
            addRenderableWidget(Button.builder(Component.literal(LABELS[i]),
                    button -> {
                        if (minecraft != null && minecraft.gameMode != null)
                            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                    }).bounds(x, y, 115, 20).build());
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF51351E);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFFC8A87C);
        graphics.fill(leftPos + 8, topPos + 8, leftPos + imageWidth - 8, topPos + 55, 0xFFE3CFAB);
        graphics.fill(leftPos + 9, topPos + 192, leftPos + imageWidth - 9, topPos + imageHeight - 9, 0xFFE3CFAB);
        graphics.fill(leftPos + 118, topPos + 33, leftPos + 137, topPos + 52, 0xFF4E3823);
        graphics.fill(leftPos + 119, topPos + 34, leftPos + 136, topPos + 51, 0xFF8A7251);
        // Slot backgrounds: three inventory rows, and the hotbar.
        for (int y = 0; y < 3; y++) for (int x = 0; x < 9; x++) slotBg(graphics, leftPos + 39 + 18*x, topPos + 204 + y*18);
        for (int x = 0; x < 9; x++) slotBg(graphics, leftPos + 39 + 18*x, topPos + 262);
    }

    private void slotBg(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF6D5132);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFE5D7B7);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "Корабельный верстак", 12, 12, 0x50301A, false);
        graphics.drawString(font, "Судно для улучшения:", 13, 38, 0x50301A, false);
        graphics.drawString(font, "Материалы берутся из инвентаря", 13, 54, 0x50301A, false);
        graphics.drawString(font, "Инвентарь", 40, 194, 0x50301A, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
