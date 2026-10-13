package org.slavicmyths.chud;

import java.util.concurrent.CancellationException;

/** Cooperative cancellation belongs only to an explicitly requested search worker. */
public final class ChudSearchBudget {
    private static final ThreadLocal<Long> DEADLINE = new ThreadLocal<>();
    public static void begin(long deadline) { DEADLINE.set(deadline); }
    public static void end() { DEADLINE.remove(); }
    public static void check() {
        Long deadline = DEADLINE.get();
        if (deadline != null && (Thread.currentThread().isInterrupted() || System.nanoTime() >= deadline))
            throw new CancellationException("Search cancelled or time limit reached");
    }
    private ChudSearchBudget() {}
}
