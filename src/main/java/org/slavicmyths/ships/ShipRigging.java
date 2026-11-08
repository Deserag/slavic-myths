package org.slavicmyths.ships;

/**
 * Versioned, strictly validated equipment state. The level describes yard/mast
 * geometry; fabric describes cloth quality. They are independent axes.
 *
 * Kept free of Minecraft classes so its balance and save invariants can be
 * tested with javac without a running client.
 */
public record ShipRigging(int level, int fabric) {
    public ShipRigging {
        if (level < 0 || level > 4 || fabric < 0 || fabric > 3
                || ((level == 0) != (fabric == 0)))
            throw new IllegalArgumentException("Invalid ship rigging: " + level + "/" + fabric);
    }

    public double clothMultiplier() {
        return switch (fabric) {
            case 1 -> 1.00D;
            case 2 -> 1.15D;
            case 3 -> 1.30D;
            default -> 1.00D;
        };
    }

    public double rigMultiplier() {
        return switch (level) {
            case 1 -> 1.00D;
            case 2 -> 1.25D;
            case 3 -> 1.55D;
            case 4 -> 1.80D;
            default -> 0.60D; // Unrigged hull: slow manual rowing.
        };
    }

    /** Fractional server-tick schedule makes every fabric grade measurable. */
    public double exactCooldownTicks() {
        return Math.max(3D, 10D / (clothMultiplier() * rigMultiplier()));
    }
    /** Integer display ceiling; actual server travel uses exactCooldownTicks. */
    public int cooldownTicks() {
        return (int)Math.ceil(exactCooldownTicks());
    }
}
