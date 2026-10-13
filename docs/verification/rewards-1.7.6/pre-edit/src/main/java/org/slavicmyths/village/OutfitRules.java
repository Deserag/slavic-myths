package org.slavicmyths.village;

import java.util.UUID;

/** Pure visual rules: no gender, age system, world access or render-time RNG. */
public final class OutfitRules {
    public enum Climate { COLD, TEMPERATE, WARM }
    public static Climate climate(String type) {
        return switch (type) {
            case "snow", "snowy", "taiga" -> Climate.COLD;
            case "desert", "savanna", "jungle" -> Climate.WARM;
            default -> Climate.TEMPERATE;
        };
    }
    public static int variant(UUID uuid) {
        long bits = uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 23);
        bits ^= bits >>> 33;
        bits *= 0xff51afd7ed558ccdL;
        bits ^= bits >>> 33;
        return (int) bits & 1;
    }
    private OutfitRules() {}
}
