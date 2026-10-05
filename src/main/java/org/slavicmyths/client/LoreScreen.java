package org.slavicmyths.client;
import net.minecraft.network.chat.Component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;

public final class LoreScreen extends Screen {
    private final int mask;
    private int article, page;
    private static final String[] IDS = {"intro", "domovoy", "leshy", "shrine", "altar", "ritual", "paths",
            "silver", "amber", "thunder_axe", "storm_staff", "charms", "herbs", "ritual_tools", "runes", "reforging", "water_fish", "fishing_net", "vodyanoy", "rusalka", "elder_vodyanoy", "deep_pool", "pool_pearl", "pool_spear", "depth_amulet", "swamp_hut", "abandoned_settlement", "bog_causeway", "flooded_shrine", "fishing_camp", "underwater_ruins", "bandit_gangs", "bandit_small", "bandit_medium", "ataman", "arms_craft", "woodlands", "forest_whistle", "nightingale"};
    public LoreScreen(int mask) { super(Component.translatable("item.slavicmyths.lore_book")); this.mask = mask; }
    public static void open(int mask) { Minecraft.getInstance().setScreen(new LoreScreen(mask)); }
    @Override protected void init() {
        int left = width / 2 - 150;
        for (int i = 0; i < 7; i++) {
            final int selected = i;
            boolean unlocked = i == 0 || i == 6 || (mask & (1 << (i - 1))) != 0;
            Button button = Button.builder(
                    Component.translatable(unlocked ? "book.slavicmyths." + IDS[i] + ".title" : "book.slavicmyths.locked"),
                    b -> { article = selected; page = 0; }).bounds(left, 35 + i * 19, 112, 18).build();
            button.active = unlocked;
            addRenderableWidget(button);
        }
        addRenderableWidget(Button.builder( Component.translatable("book.slavicmyths.craft"),
                b -> { article = 7; page = 0; }).bounds(left, 170, 112, 18).build());
        addRenderableWidget(Button.builder( Component.translatable("book.slavicmyths.next_topic"),
                b -> { do { article = article < 7 || article >= IDS.length - 1 ? 7 : article + 1; } while(article>=16&&(mask&(1<<(article-11)))==0); page = 0; }).bounds(left, 190, 112, 18).build());
        addRenderableWidget(Button.builder( Component.translatable("book.slavicmyths.previous"), b -> page = Math.max(0, page - 1)).bounds(left + 124, height - 30, 75, 20).build());
        addRenderableWidget(Button.builder( Component.translatable("book.slavicmyths.next"), b -> page++).bounds(left + 205, height - 30, 75, 20).build());
    }
    @Override public void render(GuiGraphics pose, int mouseX, int mouseY, float partial) {
        renderBackground(pose,mouseX,mouseY,partial);
        int left = width / 2 - 150;
        pose.fill( left - 8, 26, left + 306, height - 34, 0xEE332B21);
        pose.drawCenteredString( font, title, width / 2, 12, 0xF3DFC0);
        pose.drawString( font, Component.translatable("book.slavicmyths." + IDS[article] + ".title"), left + 120, 27, 0xE9D295);
        List<FormattedCharSequence> lines = font.split(Component.translatable("book.slavicmyths." + IDS[article] + ".text"), 178);
        int perPage = Math.max(1, (height - 83) / 11);
        page = Math.min(page, Math.max(0, (lines.size() - 1) / perPage));
        for (int n = page * perPage; n < Math.min(lines.size(), (page + 1) * perPage); n++)
            pose.drawString(font, lines.get(n), left + 120, 38 + (n % perPage) * 11, 0xF3DFC0,false);
        super.render(pose, mouseX, mouseY, partial);
    }
    @Override public boolean isPauseScreen() { return false; }
}
