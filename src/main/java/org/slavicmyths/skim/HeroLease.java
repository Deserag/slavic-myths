package org.slavicmyths.skim;

import net.minecraft.nbt.CompoundTag;

/** Persistent budgets inside HeroCalls, measured only by the overworld server clock. */
public final class HeroLease {
    public static final long HELP_TICKS = 12_000, COOLDOWN_TICKS = 36_000;
    public static void start(CompoundTag n, long now) {
        n.putLong("SummonedAt", now);
        n.putLong("ExpiresAt", now + HELP_TICKS);
        n.putLong("LastTick", now);
        n.putLong("ReadyAt", 0);
        n.putInt("Recoveries", 0);
        n.putBoolean("Active", true);
        n.remove("Exhausted");
    }
    public static void advance(CompoundTag n, long now, boolean mandatory) {
        if (!n.getBoolean("Active")) return;
        long last = n.contains("LastTick") ? n.getLong("LastTick") : now;
        // Pauses are explicit mandatory encounter phases, never mere proximity to an arena.
        if (mandatory) n.putLong("ExpiresAt", n.getLong("ExpiresAt") + Math.max(0, now - last));
        n.putLong("LastTick", now);
    }
    public static boolean expired(CompoundTag n, long now) {
        return n.getBoolean("Active") && (n.getBoolean("Exhausted") || now >= n.getLong("ExpiresAt"));
    }
    public static boolean recovery(CompoundTag n, long now) {
        if (!n.getBoolean("Active") || expired(n, now) || n.getInt("Recoveries") >= 2) return false;
        n.putInt("Recoveries", n.getInt("Recoveries") + 1);
        return true;
    }
    public static void end(CompoundTag n, long now) {
        // Reconciliation, repeated death callbacks and reload must not extend a running cooldown.
        if (n.getBoolean("Active") || n.getLong("ReadyAt") == 0) n.putLong("ReadyAt", now + COOLDOWN_TICKS);
        n.putBoolean("Active", false);
    }
    private HeroLease() { }
}
