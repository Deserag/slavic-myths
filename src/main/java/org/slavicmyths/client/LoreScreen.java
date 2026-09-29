package org.slavicmyths.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.IReorderingProcessor;
import java.util.List;

public final class LoreScreen extends Screen {
    private final int mask;
    private int article, page;
    private static final String[] IDS = {"intro", "domovoy", "leshy", "shrine", "altar", "ritual", "paths"};
    public LoreScreen(int mask) { super(new TranslationTextComponent("item.slavicmyths.lore_book")); this.mask = mask; }
    public static void open(int mask) { Minecraft.getInstance().setScreen(new LoreScreen(mask)); }
    @Override protected void init() {
        int left = width / 2 - 150;
        for (int i = 0; i < IDS.length; i++) {
            final int selected = i;
            boolean unlocked = i == 0 || i == 6 || (mask & (1 << (i - 1))) != 0;
            Button button = new Button(left, 35 + i * 22, 112, 20,
                    new TranslationTextComponent(unlocked ? "book.slavicmyths." + IDS[i] + ".title" : "book.slavicmyths.locked"),
                    b -> { article = selected; page = 0; });
            button.active = unlocked;
            addButton(button);
        }
        addButton(new Button(left + 124, height - 30, 75, 20, new TranslationTextComponent("book.slavicmyths.previous"), b -> page = Math.max(0, page - 1)));
        addButton(new Button(left + 205, height - 30, 75, 20, new TranslationTextComponent("book.slavicmyths.next"), b -> page++));
    }
    @Override public void render(MatrixStack pose, int mouseX, int mouseY, float partial) {
        renderBackground(pose);
        int left = width / 2 - 150;
        fill(pose, left - 8, 26, left + 306, height - 34, 0xEE332B21);
        drawCenteredString(pose, font, title, width / 2, 12, 0xF3DFC0);
        List<IReorderingProcessor> lines = font.split(new TranslationTextComponent("book.slavicmyths." + IDS[article] + ".text"), 178);
        int perPage = Math.max(1, (height - 83) / 11);
        page = Math.min(page, Math.max(0, (lines.size() - 1) / perPage));
        for (int n = page * perPage; n < Math.min(lines.size(), (page + 1) * perPage); n++)
            font.draw(pose, lines.get(n), left + 120, 38 + (n % perPage) * 11, 0xF3DFC0);
        super.render(pose, mouseX, mouseY, partial);
    }
    @Override public boolean isPauseScreen() { return false; }
}
