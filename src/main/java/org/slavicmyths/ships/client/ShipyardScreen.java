package org.slavicmyths.ships.client;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slavicmyths.ships.RaftCharacteristics;
import org.slavicmyths.ships.RaftItem;
import org.slavicmyths.ships.ShipyardMenu;
import org.slavicmyths.ships.ShipyardUpgrades;

/** Shipwright-style inspection panel. One real inventory slot; no fake visual item. */
public final class ShipyardScreen extends AbstractContainerScreen<ShipyardMenu> {
    private static final int WOOD = 0xff38291d;
    private static final int RIM = 0xffbd985c;
    private static final int PAPER = 0xffe8d5a8;

    public ShipyardScreen(ShipyardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 372;
        imageHeight = 249;
        inventoryLabelY = 143;
        inventoryLabelX = 12;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Создать 3×3"), button -> action(2))
                .bounds(leftPos + 12, topPos + 78, 91, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Улучшить"), button -> action(1))
                .bounds(leftPos + 108, topPos + 78, 69, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Борта"), button -> action(3))
                .bounds(leftPos + 10, topPos + 106, 41, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Парус"), button -> action(4))
                .bounds(leftPos + 54, topPos + 106, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Руль"), button -> action(5))
                .bounds(leftPos + 99, topPos + 106, 37, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Вёсла"), button -> action(6))
                .bounds(leftPos + 139, topPos + 106, 40, 20).build());
        // Reconfiguration is distinct from installation; every removal refunds
        // one real part on the server and fails safely if inventory is full.
        addRenderableWidget(Button.builder(Component.literal("−Борт"), button -> action(7))
                .bounds(leftPos + 10, topPos + 127, 41, 15).build());
        addRenderableWidget(Button.builder(Component.literal("−Парус"), button -> action(8))
                .bounds(leftPos + 54, topPos + 127, 42, 15).build());
        addRenderableWidget(Button.builder(Component.literal("−Руль"), button -> action(9))
                .bounds(leftPos + 99, topPos + 127, 37, 15).build());
        addRenderableWidget(Button.builder(Component.literal("−Вёсла"), button -> action(10))
                .bounds(leftPos + 139, topPos + 127, 40, 15).build());
    }

    private void action(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, RIM);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3,
                topPos + imageHeight - 3, WOOD);
        graphics.fill(leftPos + 7, topPos + 19, leftPos + 179, topPos + 139, 0xff5b4028);
        graphics.fill(leftPos + 18, topPos + 35, leftPos + 63, topPos + 74, 0xff967447);
        graphics.fill(leftPos + 29, topPos + 43, leftPos + 49, topPos + 64, 0xff171c20);
        graphics.fill(leftPos + 183, topPos + 20, leftPos + 364, topPos + 235, PAPER);
        graphics.fill(leftPos + 185, topPos + 22, leftPos + 362, topPos + 233, 0xffd0b987);
        graphics.fill(leftPos + 187, topPos + 24, leftPos + 360, topPos + 231, PAPER);
        graphics.fill(leftPos + 7, topPos + 140, leftPos + 179, topPos + 242, 0xff74543a);
        graphics.fill(leftPos + 9, topPos + 146, leftPos + 176, topPos + 237, 0xff4a3022);
        // Recessed inventory slots.
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                graphics.fill(leftPos + 11 + col * 18, topPos + 154 + row * 18,
                        leftPos + 29 + col * 18, topPos + 172 + row * 18, 0xff2a2420);
        for (int col = 0; col < 9; col++)
            graphics.fill(leftPos + 11 + col * 18, topPos + 212,
                    leftPos + 29 + col * 18, topPos + 230, 0xff2a2420);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "КОРАБЕЛЬНАЯ ВЕРФЬ", 13, 8, 0xfff5d49d, false);
        graphics.drawString(font, "Судно", 23, 26, 0xfff4e2be, false);
        graphics.drawString(font, "Осмотр и улучшения", 192, 29, 0xff493322, false);
        if (!(menu.inspected().getItem() instanceof RaftItem)) {
            graphics.drawString(font, "Поместите плот", 192, 48, 0xff5c4836, false);
            graphics.drawString(font, "в судовую ячейку", 192, 59, 0xff5c4836, false);
            graphics.drawString(font, "слева.", 192, 70, 0xff5c4836, false);
            graphics.drawString(font, "Ячейка возвращает", 192, 98, 0xff5c4836, false);
            graphics.drawString(font, "судно при закрытии.", 192, 109, 0xff5c4836, false);
            graphics.drawString(font, "Кнопки слева:", 192, 140, 0xff5c4836, false);
            graphics.drawString(font, "сборка, расширение", 192, 151, 0xff5c4836, false);
            graphics.drawString(font, "и установка деталей.", 192, 162, 0xff5c4836, false);
            graphics.drawString(font, "Shift + ПКМ:", 192, 188, 0xff5c4836, false);
            graphics.drawString(font, "сетка крафта.", 192, 199, 0xff5c4836, false);
            return;
        }
        RaftCharacteristics.Info info = RaftCharacteristics.fromItem(menu.inspected());
        if (info == null) return;
        List<Component> lines = RaftCharacteristics.tooltip(info);
        int y = 44;
        for (Component line : lines) {
            for (var wrapped : font.split(line, 158)) {
                if (y > 207) return;
                graphics.drawString(font, wrapped, 191, y, 0xff493322, false);
                y += 10;
            }
            y += 2;
        }
        if (info.size().next() != null && y < 214) {
            graphics.drawString(font, "Секций для роста: " + ShipyardUpgrades.sectionCost(info.size()),
                    191, y + 3, 0xff60401e, false);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
