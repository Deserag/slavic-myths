package org.slavicmyths.posad;

import java.util.ArrayList;
import java.util.List;

/** Approved 113x113 Nether settlement geometry; pure math, never writes world blocks. */
public final class PosadGeometry {
    public static final int SIZE = 113;
    public static final int MAX = SIZE - 1;
    public static final int SCHEMA_VERSION = 5;
    public record Cell(int x, int z) {}
    /** Inclusive coordinates. */
    public record Rect(int minX, int maxX, int minZ, int maxZ) {
        public Rect {
            if (minX > maxX || minZ > maxZ) throw new IllegalArgumentException("Inverted rectangle");
        }
        public int width() { return maxX - minX + 1; }
        public int depth() { return maxZ - minZ + 1; }
        public boolean contains(int x, int z) { return x >= minX && x <= maxX && z >= minZ && z <= maxZ; }
        public boolean intersects(Rect r) { return minX <= r.maxX && maxX >= r.minX && minZ <= r.maxZ && maxZ >= r.minZ; }
        public Rect intersection(Rect r) {
            int x0 = Math.max(minX, r.minX), x1 = Math.min(maxX, r.maxX);
            int z0 = Math.max(minZ, r.minZ), z1 = Math.min(maxZ, r.maxZ);
            return x0 <= x1 && z0 <= z1 ? new Rect(x0, x1, z0, z1) : null;
        }
        public Rect translated(int x, int z) {
            return new Rect(Math.addExact(minX, x), Math.addExact(maxX, x), Math.addExact(minZ, z), Math.addExact(maxZ, z));
        }
        public Rect rotated(int turns) {
            Cell a = rotate(new Cell(minX, minZ), turns);
            Cell b = rotate(new Cell(maxX, maxZ), turns);
            return new Rect(Math.min(a.x(), b.x()), Math.max(a.x(), b.x()), Math.min(a.z(), b.z()), Math.max(a.z(), b.z()));
        }
    }

    /** The eight independently planned districts have immutable world positions. */
    public enum Slot {
        VECHE_HALL(new Rect(42, 70, 12, 34)),
        COMMUNAL_HOUSE(new Rect(12, 37, 12, 37)),
        GRANARY(new Rect(76, 100, 12, 39)),
        KUZNECHNY_DVOR(new Rect(12, 37, 44, 69)),
        ZELEYNAYA_IZBA(new Rect(76, 100, 44, 69)),
        DRUZHINNY_DVOR(new Rect(12, 39, 75, 99)),
        MENOVYE_RYADY(new Rect(75, 101, 75, 99)),
        VECHE_SQUARE(new Rect(40, 72, 40, 72));
        private final Rect area;
        Slot(Rect area) { this.area = area; }
        public Rect local() { return area; }
        public Rect world(int anchorX, int anchorZ, int turns) { return area.rotated(turns).translated(anchorX, anchorZ); }
    }

    public static final Rect GATE_PASSAGE = new Rect(53, 59, 108, 112);
    public static final Rect CITY = new Rect(0, MAX, 0, MAX);

    /** Clockwise: south face -> west face; origin is NW corner of rotated square. */
    public static Cell rotate(Cell pos, int turns) {
        if (!CITY.contains(pos.x(), pos.z())) throw new IllegalArgumentException("Outside city: " + pos);
        return switch (Math.floorMod(turns, 4)) {
            case 0 -> pos;
            case 1 -> new Cell(MAX - pos.z(), pos.x());
            case 2 -> new Cell(MAX - pos.x(), MAX - pos.z());
            default -> new Cell(pos.z(), MAX - pos.x());
        };
    }

    public static Cell world(Cell pos, int anchorX, int anchorZ, int turns) {
        Cell p = rotate(pos, turns);
        return new Cell(Math.addExact(anchorX, p.x()), Math.addExact(anchorZ, p.z()));
    }

    public static Rect footprint(int anchorX, int anchorZ) { return CITY.translated(anchorX, anchorZ); }
    public record ChunkPatch(int chunkX, int chunkZ, Rect worldClip) {}

    /**
     * Exact nonoverlapping chunk clips. No chunk load or world mutation occurs.
     * Minecraft native pieces must still enforce their current chunk clip.
     */
    public static List<ChunkPatch> chunkPatches(int anchorX, int anchorZ) {
        Rect city = footprint(anchorX, anchorZ);
        List<ChunkPatch> result = new ArrayList<>();
        for (int cz = Math.floorDiv(city.minZ(), 16); cz <= Math.floorDiv(city.maxZ(), 16); cz++) {
            for (int cx = Math.floorDiv(city.minX(), 16); cx <= Math.floorDiv(city.maxX(), 16); cx++) {
                Rect chunk = new Rect(cx * 16, cx * 16 + 15, cz * 16, cz * 16 + 15);
                Rect clip = city.intersection(chunk);
                if (clip != null) result.add(new ChunkPatch(cx, cz, clip));
            }
        }
        return List.copyOf(result);
    }

    private PosadGeometry() {}
}
