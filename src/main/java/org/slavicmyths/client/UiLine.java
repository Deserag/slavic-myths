package org.slavicmyths.client;

/** Original integer DDA pixels, clipped before submission and joined into axis-aligned runs. */
public final class UiLine {
    @FunctionalInterface public interface Rectangles {
        void fill(int x1, int y1, int x2, int y2);
    }

    public static boolean intersects(int x, int y, int width, int height, int left, int top, int right, int bottom) {
        return x < right && y < bottom && (long) x + width > left && (long) y + height > top;
    }

    public static void draw(int x1, int y1, int x2, int y2, int left, int top, int right, int bottom, Rectangles out) {
        if (left >= right || top >= bottom) return;
        long dx = (long) x2 - x1, dy = (long) y2 - y1;
        long steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        boolean horizontal = Math.abs(dx) >= Math.abs(dy);
        long start = horizontal ? x1 : y1, direction = (horizontal ? dx : dy) > 0 ? 1 : -1;
        long low = horizontal ? left : top, high = (horizontal ? right : bottom) - 1L;
        long first = Math.max(0, direction > 0 ? low - start : start - high);
        long last = Math.min(steps, direction > 0 ? high - start : start - low);
        boolean run = false;
        int sx = 0, sy = 0, px = 0, py = 0;
        for (long i = first; i <= last; i++) {
            int x = (int) (x1 + dx * i / steps), y = (int) (y1 + dy * i / steps);
            if (x < left || x >= right || y < top || y >= bottom) continue;
            if (run && (horizontal ? y != py : x != px)) {
                out.fill(Math.min(sx, px), Math.min(sy, py), Math.max(sx, px) + 1, Math.max(sy, py) + 1);
                run = false;
            }
            if (!run) { sx = x; sy = y; run = true; }
            px = x;
            py = y;
        }
        if (run) out.fill(Math.min(sx, px), Math.min(sy, py), Math.max(sx, px) + 1, Math.max(sy, py) + 1);
    }

    private UiLine() {}
}
