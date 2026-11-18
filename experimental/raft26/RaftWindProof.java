package org.slavicmyths.ships;

/** JDK21-only acceptance checks for the directional sea-breeze algorithm. */
public final class RaftWindProof {
    private static int assertions;

    private static void check(boolean ok, String reason) {
        assertions++;
        if (!ok) throw new AssertionError(reason);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 240; i++) {
            long tick = i * 401L;
            double x = 31.5D * i, z = -23.7D * i;
            RaftWind.Sample a = RaftWind.at(tick, x, z, false, false);
            RaftWind.Sample b = RaftWind.at(tick, x, z, false, false);
            check(a.equals(b), "Wind is not deterministic");
            check(Math.abs(Math.hypot(a.east(), a.south()) - 1.0D) < 1e-10, "Wind vector not normalized");
            check(a.strength() > 0D && a.strength() < 2D, "Invalid wind strength");
            double heading = Math.toDegrees(Math.atan2(-a.east(), a.south()));
            check(a.alignment(heading) > .999999D, "Tailwind should align");
            check(a.sailFactor(heading) > .99999D, "Tailwind should fill sail");
            check(a.sailFactor(heading + 180D) == 0D, "Headwind must block propulsion");
            check(RaftWind.thrust(a, heading, 0) == 0D, "No sail means no wind thrust");
            check(RaftWind.thrust(a, heading, 2) > RaftWind.thrust(a, heading, 1),
                    "Two sails must be stronger than one");
            check(RaftWind.thrust(a, heading + 180D, 1) == 0D, "Headwind thrust nonzero");
            RaftWind.Sample storm = RaftWind.at(tick, x, z, true, true);
            check(storm.strength() > a.strength(), "Thunderstorm should increase wind strength");
        }
        check(RaftSize.SMALL.maxSails() == 1, "Small raft requires one sail maximum");
        check(RaftSize.CARGO.maxSails() == 2, "Large raft should allow twin sails");
        System.out.println("RAFT WIND: PASS " + assertions + " JDK21 assertions; Minecraft runtime NOT_RUN");
    }

    private RaftWindProof() {}
}
