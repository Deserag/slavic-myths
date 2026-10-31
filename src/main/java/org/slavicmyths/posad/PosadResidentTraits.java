package org.slavicmyths.posad;

import java.util.UUID;

/** Stable appearance defaults derived from the persisted member identity.
 * Explicit future customization may override these without changing identity.
 */
public record PosadResidentTraits(int horns, int wings, int face, int skin, int clothes) {
    public PosadResidentTraits {
        if (horns < 0 || horns > 3 || wings < 0 || wings > 3 || face < 0 || face > 3 || skin < 0 || skin > 4 || clothes < 0 || clothes > 3) throw new IllegalArgumentException("Invalid resident appearance");
    }

    public static PosadResidentTraits of(PosadPopulationPlan.WorldMember member) {
        UUID id = member.identity();
        long seed = mix(id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 21));
        int horns = Math.floorMod(seed, 4);
        int wings = Math.floorMod(seed >>> 8, 4);
        int face = Math.floorMod(seed >>> 16, 4);
        int skin = Math.floorMod(seed >>> 24, 5);
        int clothes = switch (member.member().role()) {
            case POSADNIK -> 3;
            case GUARD -> 2;
            case CIVILIAN -> Math.floorMod(seed >>> 32, 2);
        };
        return new PosadResidentTraits(horns, wings, face, skin, clothes);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
