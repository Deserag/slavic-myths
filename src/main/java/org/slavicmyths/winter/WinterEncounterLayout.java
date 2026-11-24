package org.slavicmyths.winter;

/** Pure deterministic encounter grid. One site per 512-block cell, with a 384-block minimum axis separation. */
public final class WinterEncounterLayout {
    public static final int CELL = 512, SEARCH = 64;
    public enum Habitat { FOREST, LAND, OCEAN, OTHER }
    public enum Boss { CAPTAIN, QUEEN, AUKA, OWL, WOLVERINE, GLUHEYA }
    public static int cell(int coordinate) { return Math.floorDiv(coordinate, CELL); }
    public static int center(int cell) { return cell * CELL + CELL / 2; }
    public static long key(int x, int z) { return ((long)x << 32) | (z & 0xffffffffL); }
    public static Boss select(Habitat habitat, int x, int z, boolean test, boolean gluheya, long seed) {
        if (habitat == Habitat.OTHER) return null;
        if (habitat == Habitat.OCEAN && test) return Boss.CAPTAIN;
        if (test) {
            int zone = Math.floorMod(x + z, 4);
            if (habitat == Habitat.FOREST) {
                if (gluheya && Math.floorMod(x, 3) == 1) return Boss.GLUHEYA;
                return zone == 0 ? Boss.QUEEN : Boss.AUKA;
            }
            return Math.floorMod(x, 2) == 0 ? Boss.OWL : Boss.WOLVERINE;
        }
        long mixed = seed ^ (key(x,z) * 0x9E3779B97F4A7C15L);
        mixed ^= mixed >>> 33; mixed *= 0xff51afd7ed558ccdL; mixed ^= mixed >>> 33;
        if (habitat == Habitat.OCEAN) return Math.floorMod(mixed, 5) == 0 ? Boss.OWL : Boss.CAPTAIN;
        if (habitat == Habitat.FOREST) {
            return switch (Math.floorMod(mixed, gluheya ? 5 : 4)) {
                case 0 -> Boss.QUEEN;
                case 1 -> Boss.AUKA;
                case 2 -> Boss.OWL;
                case 3 -> Boss.WOLVERINE;
                default -> Boss.GLUHEYA;
            };
        }
        return Math.floorMod(mixed, 2) == 0 ? Boss.OWL : Boss.WOLVERINE;
    }
    private WinterEncounterLayout() {}
}
