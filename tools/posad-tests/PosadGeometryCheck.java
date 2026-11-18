package org.slavicmyths.posad;

import java.util.HashSet;
import java.util.Set;

/** Offline Java 21 checks only; not Minecraft GameTest or safe retrofit verification. */
public final class PosadGeometryCheck {
    private static int count;
    private static void check(boolean ok, String reason) {
        count++;
        if (!ok) throw new AssertionError(reason);
    }
    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

    private static void verifyRotations() {
        for (int rot = 0; rot < 4; rot++) {
            for (int x = 0; x < 113; x++) for (int z = 0; z < 113; z++) {
                PosadGeometry.Cell p = new PosadGeometry.Cell(x, z);
                check(PosadGeometry.rotate(PosadGeometry.rotate(p, rot), 4 - rot).equals(p), "Rotation roundtrip");
            }
            for (PosadGeometry.Slot s : PosadGeometry.Slot.values()) {
                PosadGeometry.Rect p = s.local().rotated(rot);
                check(p.width() * p.depth() == s.local().width() * s.local().depth(), "Slot area");
                check(PosadGeometry.CITY.contains(p.minX(), p.minZ()), "Slot NW bound");
                check(PosadGeometry.CITY.contains(p.maxX(), p.maxZ()), "Slot SE bound");
                check(!p.intersects(PosadGeometry.GATE_PASSAGE.rotated(rot)), "Blocked gateway");
                for (PosadGeometry.Slot other : PosadGeometry.Slot.values()) {
                    if (other != s) check(!p.intersects(other.local().rotated(rot)), "Overlapping slots: " + s + ", " + other);
                }
            }
        }
    }

    private static void verifyChunks(int anchorX, int anchorZ) {
        PosadGeometry.Rect city = PosadGeometry.footprint(anchorX, anchorZ);
        Set<Long> covered = new HashSet<>();
        for (PosadGeometry.ChunkPatch patch : PosadGeometry.chunkPatches(anchorX, anchorZ)) {
            PosadGeometry.Rect p = patch.worldClip();
            check(p.minX() >= patch.chunkX() * 16 && p.maxX() <= patch.chunkX() * 16 + 15, "Chunk X clip");
            check(p.minZ() >= patch.chunkZ() * 16 && p.maxZ() <= patch.chunkZ() * 16 + 15, "Chunk Z clip");
            for (int x = p.minX(); x <= p.maxX(); x++) for (int z = p.minZ(); z <= p.maxZ(); z++) {
                check(city.contains(x, z), "Outside footprint");
                check(covered.add(key(x, z)), "Duplicate chunk ownership");
            }
        }
        check(covered.size() == 113 * 113, "Missing chunk coverage");
    }

    private static void verifyRetrofit() {
        using(true, PosadRetrofitPolicy.Stage.ABSENT, true, false, false, PosadRetrofitPolicy.Decision.ALLOW_ONLY_AFTER_PERSISTING_APPLYING);
        using(false, PosadRetrofitPolicy.Stage.ABSENT, true, false, false, PosadRetrofitPolicy.Decision.DENY_UNTRUSTED_ANCHOR);
        using(true, PosadRetrofitPolicy.Stage.COMPLETE, true, false, false, PosadRetrofitPolicy.Decision.SKIP_COMPLETED);
        using(true, PosadRetrofitPolicy.Stage.APPLYING, true, false, false, PosadRetrofitPolicy.Decision.DENY_RETRY_WITHOUT_RECOVERY);
        using(true, PosadRetrofitPolicy.Stage.BLOCKED, true, false, false, PosadRetrofitPolicy.Decision.DENY_RETRY_WITHOUT_RECOVERY);
        using(true, PosadRetrofitPolicy.Stage.ABSENT, false, false, false, PosadRetrofitPolicy.Decision.DENY_UNLOADED_CHUNKS);
        using(true, PosadRetrofitPolicy.Stage.ABSENT, true, true, false, PosadRetrofitPolicy.Decision.DENY_FOREIGN_BLOCKS);
        using(true, PosadRetrofitPolicy.Stage.ABSENT, true, false, true, PosadRetrofitPolicy.Decision.DENY_EXISTING_CONTAINER);
    }
    private static void using(boolean trusted, PosadRetrofitPolicy.Stage stage, boolean loaded,
                              boolean foreign, boolean chest, PosadRetrofitPolicy.Decision expected) {
        check(PosadRetrofitPolicy.decide(new PosadRetrofitPolicy.Snapshot(trusted, stage, loaded, foreign, chest)) == expected, "Retrofit guard " + expected);
    }
    public static void main(String[] args) {
        check(PosadGeometry.SIZE == 113, "Approved city");
        check(PosadGeometry.GATE_PASSAGE.width() == 7, "Approved gate");
        check(PosadGeometry.Slot.GRANARY.local().width() == 25 && PosadGeometry.Slot.GRANARY.local().depth() == 28, "NBT granary footprint");
        verifyRotations();
        verifyChunks(0, 0);
        verifyChunks(7, 13);
        verifyChunks(-113, -287);
        verifyChunks(31, -1);
        verifyRetrofit();
        System.out.println("PASS: " + count + " Java 21 offline geometry/preflight assertions (NOT Minecraft QA)");
    }
    private PosadGeometryCheck() {}
}
