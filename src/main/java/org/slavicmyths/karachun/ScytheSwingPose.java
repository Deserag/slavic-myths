package org.slavicmyths.karachun;

/** Continuous finite swing envelope; no entity state, client classes or ticking. */
public final class ScytheSwingPose {
    private static float smooth(float t) {
        t = Math.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public static float motion(float age, int windup, int duration) {
        if (!Float.isFinite(age) || age <= 0 || age >= duration || windup < 3 || duration <= windup + 3) return 0;
        if (age < windup - 2) return -.45F * smooth(age / (windup - 2));
        if (age < windup + 3) return -.45F + 1.3F * smooth((age - windup + 2) / 5);
        return .85F * (1 - smooth((age - windup - 3) / (duration - windup - 3)));
    }

    public static float influence(float age, int duration) {
        if (!Float.isFinite(age) || age <= 0 || age >= duration) return 0;
        return smooth(age / 2) * smooth((duration - age) / 4);
    }

    private ScytheSwingPose() { }
}
