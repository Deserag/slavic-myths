package org.slavicmyths.ships;

/**
 * Weather-aware, deterministic ocean breeze for all raft sizes.
 * One world direction per slowly changing region; no machinery or external mods.
 * Pure Java21 so heading and wind behavior can be verified independently.
 */
public final class RaftWind {
    public record Sample(double east, double south, double strength) {
        public double alignment(double yawDegrees) {
            double yaw = Math.toRadians(yawDegrees);
            return -Math.sin(yaw) * east + Math.cos(yaw) * south;
        }
        public double sailFactor(double yawDegrees) {
            // No progress into a direct headwind; beam wind gives weak propulsion.
            double alignment = alignment(yawDegrees);
            return Math.max(0.0D, Math.min(1.0D, (alignment + 0.20D) / 1.20D));
        }
        public String direction() {
            double degrees = (Math.toDegrees(Math.atan2(east, -south)) + 360.0D) % 360.0D;
            String[] compass = {"С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ"};
            return compass[((int)Math.round(degrees / 45.0D)) % 8];
        }
    }

    /** Coordinates and game time are ordinary world values, never network state. */
    public static Sample at(long tick, double x, double z, boolean raining, boolean thunder) {
        double angle = 1.5D + tick / 24000.0D
                + 0.24D * Math.sin(x / 430.0D) + 0.18D * Math.cos(z / 510.0D);
        double east = Math.sin(angle), south = Math.cos(angle);
        double gust = 0.79D + 0.21D * Math.sin(tick / 210.0D + x / 160.0D + z / 190.0D);
        double weather = thunder ? 1.32D : raining ? 1.15D : 1.0D;
        return new Sample(east, south, Math.max(0.1D, gust * weather));
    }

    /** Acceleration in blocks per tick per tick. Paddling remains an additive system. */
    public static double thrust(Sample sample, double headingDegrees, int sails) {
        if (sails <= 0) return 0D;
        return (0.042D + 0.014D * (sails - 1)) *
                sample.strength() * sample.sailFactor(headingDegrees);
    }

    private RaftWind() {}
}
