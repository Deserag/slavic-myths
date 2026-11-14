package org.slavicmyths.ships;

/**
 * Pure, server-safe section limits and topology for future native-block hull rebuilding.
 * These are logical section coordinates, not physical block dimensions or material costs.
 * The same final section set results regardless of the order of length/beam upgrades.
 */
public final class ShipSectionPlan {
    public enum ShipClass {
        SMALL(1, 0, 1, 1, 1, 1),
        MEDIUM(2, 1, 2, 2, 2, 2),
        LARGE(3, 2, 3, 3, 3, 3),
        LARGEST(3, 2, 3, 4, 4, 3);

        public final int maxLength, maxWidth, maxDecks, maxMasts, maxSails, maxSailTier;

        ShipClass(int maxLength, int maxWidth, int maxDecks,
                  int maxMasts, int maxSails, int maxSailTier) {
            this.maxLength = maxLength;
            this.maxWidth = maxWidth;
            this.maxDecks = maxDecks;
            this.maxMasts = maxMasts;
            this.maxSails = maxSails;
            this.maxSailTier = maxSailTier;
        }
    }

    /** A width step adds matching sections on BOTH sides of the centerline. */
    public record Section(int lateral, int longitudinal) {}

    public record Configuration(ShipClass shipClass, int length, int width) {
        public Configuration {
            if (shipClass == null || length < 0 || length > shipClass.maxLength
                    || width < 0 || width > shipClass.maxWidth)
                throw new IllegalArgumentException("Invalid ship section configuration");
        }

        /** Canonical logical cells, suitable for deterministic geometry generation. */
        public java.util.Set<Section> sections() {
            java.util.Set<Section> result = new java.util.LinkedHashSet<>();
            for (int z = 0; z <= length; z++)
                for (int x = -width; x <= width; x++)
                    result.add(new Section(x, z));
            return java.util.Collections.unmodifiableSet(result);
        }

        public int sectionCount() { return (length + 1) * (width * 2 + 1); }

        public Configuration extend(int steps) {
            return new Configuration(shipClass, Math.addExact(length, steps), width);
        }

        public Configuration widen(int steps) {
            return new Configuration(shipClass, length, Math.addExact(width, steps));
        }

        public java.util.Set<Section> additionsTo(Configuration next) {
            if (next.shipClass != shipClass || next.length < length || next.width < width)
                throw new IllegalArgumentException("Only increasing the same hull is supported");
            java.util.Set<Section> added = new java.util.LinkedHashSet<>(next.sections());
            added.removeAll(sections());
            return java.util.Collections.unmodifiableSet(added);
        }
    }

    /** Validate loaded values on the server, never trust a client's proposed dimensions. */
    public static Configuration validate(ShipClass shipClass, int length, int width) {
        return new Configuration(shipClass, length, width);
    }

    private ShipSectionPlan() {}
}
