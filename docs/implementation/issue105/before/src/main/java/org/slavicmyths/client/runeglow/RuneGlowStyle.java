package org.slavicmyths.client.runeglow;

/** Per-stack visual strength, never socket capacity or rune tier. No GL state. */
public record RuneGlowStyle(int count, int alpha) {
    public static RuneGlowStyle of(int installed) {
        int n = Math.clamp(installed, 0, 3);
        return new RuneGlowStyle(n, switch (n) { case 1 -> 64; case 2 -> 100; case 3 -> 140; default -> 0; });
    }
}
