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
    private int detailsScroll;

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
        java.util.List<net.minecraft.util.FormattedCharSequence> wrapped = new java.util.ArrayList<>();
        if (!(menu.inspected().getItem() instanceof RaftItem)) {
            for (String line : new String[]{"Поместите плот в", "судовую ячейку слева.",
                    "Сборка: 9 секций + вёсла.", "Улучшение: палубные секции.",
                    "Модификации: детали", "из личного инвентаря.",
                    "Кнопки − снимают части", "и возвращают их игроку.",
                    "Shift + ПКМ: крафт."})
                wrapped.addAll(font.split(Component.literal(line), 158));
        } else {
            RaftCharacteristics.Info info = RaftCharacteristics.fromItem(menu.inspected());
            if (info == null) return;
            for (Component line : RaftCharacteristics.tooltip(info))
                wrapped.addAll(font.split(line, 158));
            if (info.size().next() != null)
                wrapped.addAll(font.split(Component.literal(
                        "Для расширения: " + ShipyardUpgrades.sectionCost(info.size()) + " палубных секций"), 158));
            wrapped.addAll(font.split(Component.literal(
                    "Парус: по ветру. Встречный ветер не ускоряет."), 158));
            wrapped.addAll(font.split(Component.literal(
                    "Кнопки со знаком − снимают оснащение и возвращают детали."), 158));
        }
        final int shown = 17;
        detailsScroll = Math.max(0, Math.min(detailsScroll, Math.max(0, wrapped.size() - shown)));
        graphics.enableScissor(leftPos + 189, topPos + 43, leftPos + 358, topPos + 214);
        for (int i = detailsScroll; i < Math.min(wrapped.size(), detailsScroll + shown); i++) {
            graphics.drawString(font, wrapped.get(i), 191, 44 + (i - detailsScroll) * 10,
                    0xff493322, false);
        }
        graphics.disableScissor();
        if (wrapped.size() > shown) graphics.drawString(font, "Колёсико: ещё сведения",
                191, 216, 0xff614526, false);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX >= leftPos + 188 && mouseX < leftPos + 363 &&
                mouseY >= topPos + 40 && mouseY < topPos + 234) {
            detailsScroll = Math.max(0, detailsScroll - (int)Math.signum(vertical) * 2);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
