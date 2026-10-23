package org.slavicmyths.client.runeglow;

/** Finite, observed equipment transition. No game clock, entity or render globals. */
public final class RuneWaveState {
    public static final double DURATION = 20.0;
    private boolean initialized, complete;
    private double started = -1, lastSeen = -1;

    public float observe(boolean nowComplete, double tick, boolean alive) {
        if (!Double.isFinite(tick) || !alive) {
            initialized = false; started = -1; lastSeen = -1;
            return -1;
        }
        // Loading a full set, returning after a long invisible interval or a
        // clock reset is a baseline, not evidence of a new equip transition.
        // Inventory rendering uses partial=1 while a world pass can use <1.
        // Those sub-tick differences are not a relog/clock rollback.
        if (!initialized || tick + 1 < lastSeen || tick - lastSeen > 40) {
            initialized = true; complete = nowComplete; started = -1;
        } else {
            tick = Math.max(tick, lastSeen);
            if (!nowComplete) started = -1;
            else if (!complete) started = tick;
            complete = nowComplete;
        }
        lastSeen = tick;
        if (started < 0) return -1;
        double phase = (tick - started) / DURATION;
        if (phase >= 1) { started = -1; return -1; }
        return (float)Math.max(0, phase);
    }
}
