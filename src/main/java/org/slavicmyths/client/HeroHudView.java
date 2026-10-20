package org.slavicmyths.client;

import java.util.*;

/** Loaded-client snapshots and drawing shared with the offline checks. No world/network queries. */
public final class HeroHudView {
    public record Entry(UUID id, UUID owner, String type, String name, float health,
                        float maxHealth, String status, boolean resting, double distanceSquared, boolean alive) { }
    public interface Canvas {
        int width(String text);
        String trim(String text, int width);
        void fill(int left, int top, int right, int bottom, int color);
        void text(String text, int x, int y, int color);
    }
    public record Slot(int x, int y, int width, int height) { }
    public static final float SCALE = .75F;
    public static final int MAX_VISIBLE = 3;
    private static final int ROW = 24;

    public static List<Entry> select(UUID player, List<Entry> loaded) {
        if (player == null) return List.of();
        var unique = new LinkedHashMap<UUID, Entry>();
        for (var hero : loaded) {
            if (player.equals(hero.owner()) && hero.alive() && hero.distanceSquared() <= 64 * 64)
                unique.putIfAbsent(hero.id(), hero);
        }
        return unique.values().stream().sorted(Comparator.comparing(Entry::type)
                .thenComparing(h -> h.id().toString())).limit(MAX_VISIBLE).toList();
    }

    /** Original bottom-left anchor for 1/2, extended by one real row for the lawful third. */
    public static List<Slot> layout(int width, int height, int count, int offsetX, int offsetY) {
        count = Math.clamp(count, 0, MAX_VISIBLE);
        if (count == 0) return List.of();
        int w = (int)(width / SCALE), h = (int)(height / SCALE);
        int panel = Math.min(160, Math.max(1, w - 16));
        int bottom = (int)(Math.min(65, height / 2) / SCALE);
        int blockHeight = count * ROW;
        int x = Math.clamp(8 + (int)(offsetX / SCALE), 4, Math.max(4, w - panel - 4));
        int maxY = Math.max(8, h - bottom - blockHeight);
        int y = Math.clamp(maxY + (int)(offsetY / SCALE), 8, maxY);
        var slots = new ArrayList<Slot>();
        for (int i = 0; i < count; i++) slots.add(new Slot(x, y + i * ROW, panel, ROW));
        return List.copyOf(slots);
    }

    public static void draw(Canvas canvas, List<Entry> heroes, List<Slot> slots) {
        for (int i = 0; i < Math.min(MAX_VISIBLE, Math.min(heroes.size(), slots.size())); i++) {
            var hero = heroes.get(i); var slot = slots.get(i);
            int x = slot.x(), y = slot.y(), width = slot.width();
            String hp = Math.round(hero.health()) + "/" + Math.round(hero.maxHealth());
            int hpWidth = canvas.width(hp);
            canvas.fill(x, y, x + width, y + 23, 0xB0211D19);
            String name = fit(canvas, hero.name(), Math.max(0, width - hpWidth - 9));
            canvas.text(name, x + 3, y + 2, 0xEEE1C9);
            canvas.text(hp, x + width - hpWidth - 3, y + 2, 0xDDD4C4);
            canvas.text(fit(canvas, hero.status(), width - 6), x + 3, y + 11,
                    hero.resting() ? 0xC1B497 : 0xAFCCAA);
            int barWidth = Math.max(0, width - 6);
            float fraction = hero.maxHealth() > 0 && Float.isFinite(hero.health())
                    ? Math.clamp(hero.health() / hero.maxHealth(), 0, 1) : 0;
            canvas.fill(x + 3, y + 20, x + 3 + barWidth, y + 22, 0xFF493B35);
            canvas.fill(x + 3, y + 20, x + 3 + Math.round(barWidth * fraction), y + 22,
                    hero.resting() ? 0xFF897951 : 0xFFB99B61);
        }
    }
    private static String fit(Canvas canvas, String text, int width) {
        if (canvas.width(text) <= width) return text;
        String ellipsis = "…";
        return width < canvas.width(ellipsis) ? "" : canvas.trim(text, width - canvas.width(ellipsis)) + ellipsis;
    }
    private HeroHudView() { }
}
