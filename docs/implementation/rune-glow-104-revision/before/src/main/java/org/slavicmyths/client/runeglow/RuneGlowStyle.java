package org.slavicmyths.client.runeglow;

/** Per-stack visual strength, never socket capacity or rune tier. No GL state. */
public record RuneGlowStyle(int count, int alpha) {
    public static RuneGlowStyle of(int installed) {
        int n = Math.clamp(installed, 0, 3);
        return new RuneGlowStyle(n, switch (n) { case 1 -> 112; case 2 -> 156; case 3 -> 200; default -> 0; });
    }
}
