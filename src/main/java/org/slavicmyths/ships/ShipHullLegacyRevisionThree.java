package org.slavicmyths.ships;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.slavicmyths.ships.ShipHullVoxelLayout.brigantineTopDeck;
import static org.slavicmyths.ships.ShipHullVoxelLayout.sailBottom;
import static org.slavicmyths.ships.ShipHullVoxelLayout.sailLowerYard;
import static org.slavicmyths.ships.ShipHullVoxelLayout.sailTop;
import org.slavicmyths.ships.ShipHullVoxelLayout.Cell;
import org.slavicmyths.ships.ShipHullVoxelLayout.Part;

/**
 * Frozen shipped 2.6 tier-III block layouts. Only used to identify and safely
 * rebuild previously constructed ships after later geometry fixes.
 * Never used for new construction. Each candidate must match world blocks
 * exactly before any old hull block may be removed.
 */
final class ShipHullLegacyRevisionThree {
    private ShipHullLegacyRevisionThree() {}

    static Map<Cell, Part> generate(ShipSuperstructurePlan p, int archive) {
        return switch (archive) {
            case 0 -> historicHull0(p);
            case 1 -> historicHull1(p);
            case 2 -> historicHull2(p);
            case 3 -> historicHull3(p);
            case 4 -> historicHull4(p);
            case 5 -> ShipHullPreFixBrigantine.generate(p);
            default -> throw new IllegalArgumentException("Unknown historic hull template");
        };
    }

    static int archives() { return 6; }

    private static void put(Map<Cell, Part> out, int x, int y, int z, Part part) {
        out.put(new Cell(x, y, z), part);
    }

    // Frozen historic layout from commit e9816eff; read-only upgrade source.
    private static int historicBeam0(ShipSuperstructurePlan plan, int z) {
        int end = plan.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = plan.hullWidth() / 2;
        int bow = end - z, aft = end + z;
        int bowRun = Math.min(12, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * Math.sin(t * Math.PI / 2D)));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        int waist = Math.abs(z) <= 3 ? 1 : 0;
        return half - waist;
    }

    private static Map<Cell, Part> historicHull0(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 2;
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam0(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0 && Math.abs(z) < end - 1) put(out, x, 0, z, Part.KEEL);
                if (abs <= Math.max(1, beam - 3)) put(out, x, 1, z, Part.HULL);
                else if (abs <= beam - 1) put(out, x, 2, z, Part.HULL);
                else put(out, x, 3, z, Part.HULL);
                // Separate hold and gun deck with at least three clear blocks.
                if (abs < beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 5, z, Part.DECK);
                if (!(x == -2 && z == 1)) put(out, x, 10, z, Part.DECK);
                boolean edge = abs == beam;
                if (edge) {
                    for (int y = 4; y <= 9; y++) {
                        boolean port = Math.abs(z) <= end - 5
                                && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                                && (y == 7 || y == 8);
                        if (!port) put(out, x, y, z,
                                y == 6 || Math.floorMod(z, 5) == 0
                                        ? Part.FRAME : Part.HULL);
                    }
                    put(out, x, 10, z, Part.HULL);
                    put(out, x, 11, z,
                            Math.floorMod(z, 4) == 0 ? Part.FRAME : Part.HULL);
                }
            }
        }
        // Permanent functional lighting, not glowing cosmetic textures.
        // Posts stand inside the bulwark without blocking the central aisle.
        for (int z : new int[]{-end + 8, end - 8}) {
            int beam = historicBeam0(p, z);
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 11, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Real hatch between hold, gun deck and weather deck.
        for (int y = 2; y <= 10; y++) {
            put(out, -3, y, 1, Part.FRAME);
            put(out, -2, y, 1, Part.LADDER);
        }
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 11; y <= 15; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 16, front));
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 6;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 16; y <= 20; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 21, front));
            put(out, half - 1, 21, rear + 2, Part.LANTERN);
            put(out, -half + 1, 21, rear + 2, Part.LANTERN);
        }
        if (p.cabin()) {
            int half = Math.min(4, p.hullWidth() / 2 - 4);
            // Cabin is on the weather deck, beneath the actual stern deck.
            for (int z = rear + 1; z <= rear + 6; z++)
                for (int x = -half; x <= half; x++) {
                    boolean wall = Math.abs(x) == half || z == rear + 1 || z == rear + 6;
                    if (!wall) continue;
                    for (int y = 11; y <= 14; y++) {
                        if (z == rear + 6 && x == 0 && (y == 11 || y == 12))
                            put(out, x, y, z, Part.DOOR);
                        else put(out, x, y, z,
                                Math.abs(x) == half && y == 12 && z == rear + 3
                                        ? Part.WINDOW : Part.CABIN_WALL);
                    }
                }
        }
        // Every installed mast is rooted in the main deck; the complete
        // visible rig begins above the highest raised floor, never below it.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i), tier = p.sailTiers().get(i);
            int upper = sailTop(p, tier), yard = sailLowerYard(p);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 11; y <= upper + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(out, x, yard, z, Part.YARD);
                put(out, x, upper + 1, z, Part.YARD);
            }
            for (int y = sailBottom(p); y <= upper; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !out.containsKey(new Cell(x, y, z + 1)))
                        put(out, x, y, z + 1, Part.SAIL);
        }
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }


    // Frozen historic layout from commit ac4d1d63; read-only upgrade source.
    private static int historicBeam1(ShipSuperstructurePlan plan, int z) {
        int end = plan.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = plan.hullWidth() / 2;
        int bow = end - z, aft = end + z;
        int bowRun = Math.min(12, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * Math.sin(t * Math.PI / 2D)));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        int waist = Math.abs(z) <= 3 ? 1 : 0;
        return half - waist;
    }

    private static Map<Cell, Part> historicHull1(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 2;
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam1(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0 && Math.abs(z) < end - 1) put(out, x, 0, z, Part.KEEL);
                // The true bilge narrows toward the keel and flares toward
                // the gunports. Only rings, not a solid full-width box, rise
                // around the three-block-high hold interior.
                if (abs <= Math.max(1, beam - 4)) put(out, x, 1, z, Part.HULL);
                else if (abs <= beam - 3) put(out, x, 2, z, Part.HULL);
                else if (abs <= beam - 2) put(out, x, 3, z, Part.HULL);
                else if (abs == beam - 1) put(out, x, 4, z, Part.HULL);
                // Wide gun floor below a narrower weather deck.
                if (abs < beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 5, z, Part.DECK);
                if (abs <= beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 10, z, Part.DECK);
                for (int y = 5; y <= 9; y++) {
                    int skin = y == 7 || y == 8 ? beam : beam - 1;
                    if (abs != skin) continue;
                    boolean port = Math.abs(z) <= end - 5
                            && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                            && (y == 7 || y == 8);
                    if (!port) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM
                                    : Math.floorMod(z, 5) == 0
                                            ? Part.FRAME : Part.HULL);
                }
                // Tumblehome: gunport shoulders are wider than the upper rail.
                if (abs == beam - 1) {
                    put(out, x, 11, z,
                            Math.floorMod(z, 5) == 0 ? Part.FRAME : Part.HULL);
                    int bow = end - z, stern = end + z;
                    int lift = bow <= 2 ? 2 : bow <= 6 ? 1
                            : stern <= 3 ? 2 : stern <= 6 ? 1 : 0;
                    for (int y = 12; y <= 11 + lift; y++)
                        put(out, x, y, z,
                                y == 11 + lift ? Part.TRIM : Part.FRAME);
                }
            }
        }
        // Short true bowsprit: part of the moving block palette, not a fake
        // mast or a detached decorative entity.
        for (int z = end; z <= end + 3; z++)
            put(out, 0, 12, z, Part.BOWSPRIT);
        // Permanent functional lighting, not glowing cosmetic textures.
        // Posts stand inside the bulwark without blocking the central aisle.
        for (int z : new int[]{-end + 8, end - 8}) {
            int beam = historicBeam1(p, z);
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 11, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Real hatch between hold, gun deck and weather deck.
        for (int y = 2; y <= 10; y++) {
            put(out, -3, y, 1, Part.FRAME);
            put(out, -2, y, 1, Part.LADDER);
        }
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 11; y <= 15; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 16, front));
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 6;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 16; y <= 20; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 21, front));
            put(out, half - 1, 21, rear + 2, Part.LANTERN);
            put(out, -half + 1, 21, rear + 2, Part.LANTERN);
        }
        if (p.cabin()) {
            int half = Math.min(4, p.hullWidth() / 2 - 4);
            // Cabin is on the weather deck, beneath the actual stern deck.
            for (int z = rear + 1; z <= rear + 6; z++)
                for (int x = -half; x <= half; x++) {
                    boolean wall = Math.abs(x) == half || z == rear + 1 || z == rear + 6;
                    if (!wall) continue;
                    for (int y = 11; y <= 14; y++) {
                        if (z == rear + 6 && x == 0 && (y == 11 || y == 12))
                            put(out, x, y, z, Part.DOOR);
                        else put(out, x, y, z,
                                Math.abs(x) == half && y == 12 && z == rear + 3
                                        ? Part.WINDOW : Part.CABIN_WALL);
                    }
                }
        }
        // Every installed mast is rooted in the main deck; the complete
        // visible rig begins above the highest raised floor, never below it.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i), tier = p.sailTiers().get(i);
            int upper = sailTop(p, tier), yard = sailLowerYard(p);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 11; y <= upper + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(out, x, yard, z, Part.YARD);
                put(out, x, upper + 1, z, Part.YARD);
            }
            for (int y = sailBottom(p); y <= upper; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !out.containsKey(new Cell(x, y, z + 1)))
                        put(out, x, y, z + 1, Part.SAIL);
        }
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }


    // Frozen historic layout from commit a07f6e1a; read-only upgrade source.
    private static int historicBeam2(ShipSuperstructurePlan plan, int z) {
        int end = plan.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = plan.hullWidth() / 2;
        int bow = end - z, aft = end + z;
        int bowRun = Math.min(12, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * Math.sin(t * Math.PI / 2D)));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        int waist = Math.abs(z) <= 3 ? 1 : 0;
        return half - waist;
    }

    private static Map<Cell, Part> historicHull2(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 2;
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam2(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0 && Math.abs(z) < end - 1) put(out, x, 0, z, Part.KEEL);
                // The true bilge narrows toward the keel and flares toward
                // the gunports. Only rings, not a solid full-width box, rise
                // around the three-block-high hold interior.
                if (abs <= Math.max(1, beam - 4)) put(out, x, 1, z, Part.HULL);
                else if (abs <= beam - 3) put(out, x, 2, z, Part.HULL);
                else if (abs <= beam - 2) put(out, x, 3, z, Part.HULL);
                else if (abs == beam - 1) put(out, x, 4, z, Part.HULL);
                // Wide gun floor below a narrower weather deck.
                if (abs < beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 5, z, Part.DECK);
                if (abs <= beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 10, z, Part.DECK);
                for (int y = 5; y <= 9; y++) {
                    int skin = y == 7 || y == 8 ? beam : beam - 1;
                    if (abs != skin) continue;
                    boolean port = Math.abs(z) <= end - 5
                            && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                            && (y == 7 || y == 8);
                    if (!port) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM
                                    : Math.floorMod(z, 5) == 0
                                            ? Part.FRAME : Part.HULL);
                }
                // Tumblehome: gunport shoulders are wider than the upper rail.
                if (abs == beam - 1) {
                    put(out, x, 11, z,
                            Math.floorMod(z, 5) == 0 ? Part.FRAME : Part.HULL);
                    int bow = end - z, stern = end + z;
                    int lift = bow <= 2 ? 2 : bow <= 6 ? 1
                            : stern <= 3 ? 2 : stern <= 6 ? 1 : 0;
                    for (int y = 12; y <= 11 + lift; y++)
                        put(out, x, y, z,
                                y == 11 + lift ? Part.TRIM : Part.FRAME);
                }
            }
        }
        // Seal the transverse end faces. The original blueprint only
        // generated the left/right skin at each station: looking along Z
        // exposed a full-height open mouth into the gun deck and hold.
        // Fill the end caps and the exposed shoulders at every abrupt
        // change in beam. Keep deliberate side gunports untouched.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam2(p, z);
            int outwardBeam = z >= 0 ? historicBeam2(p, z + 1)
                    : historicBeam2(p, z - 1);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                boolean endFace = Math.abs(z) == end;
                boolean exposedShoulder = outwardBeam >= 0 && abs > outwardBeam;
                if (!endFace && !exposedShoulder) continue;
                for (int y = 5; y <= 9; y++) {
                    // Side ports are exactly two blocks high and only at
                    // stations far from either extremity.
                    boolean gunport = abs == beam && (y == 7 || y == 8)
                            && Math.abs(z) <= end - 5 && Math.abs(z) >= 3
                            && Math.floorMod(z + end, 4) == 0;
                    if (!gunport) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM : Part.HULL);
                }
                // Close the bulwark above the end face as well, without
                // growing a solid rectangular multi-storey stern tower.
                if (endFace) {
                    put(out, x, 11, z, Part.HULL);
                    if (abs <= Math.max(0, beam - 1)) put(out, x, 12, z, Part.TRIM);
                }
            }
        }
        // Short true bowsprit: part of the moving block palette, not a fake
        // mast or a detached decorative entity.
        for (int z = end; z <= end + 3; z++)
            put(out, 0, 12, z, Part.BOWSPRIT);
        // Permanent functional lighting, not glowing cosmetic textures.
        // Posts stand inside the bulwark without blocking the central aisle.
        for (int z : new int[]{-end + 8, end - 8}) {
            int beam = historicBeam2(p, z);
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 11, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Real hatch between hold, gun deck and weather deck.
        for (int y = 2; y <= 10; y++) {
            put(out, -3, y, 1, Part.FRAME);
            put(out, -2, y, 1, Part.LADDER);
        }
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 11; y <= 15; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 16, front));
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 6;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 16; y <= 20; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 21, front));
            put(out, half - 1, 21, rear + 2, Part.LANTERN);
            put(out, -half + 1, 21, rear + 2, Part.LANTERN);
        }
        if (p.cabin()) {
            int half = Math.min(4, p.hullWidth() / 2 - 4);
            // Cabin is on the weather deck, beneath the actual stern deck.
            for (int z = rear + 1; z <= rear + 6; z++)
                for (int x = -half; x <= half; x++) {
                    boolean wall = Math.abs(x) == half || z == rear + 1 || z == rear + 6;
                    if (!wall) continue;
                    for (int y = 11; y <= 14; y++) {
                        if (z == rear + 6 && x == 0 && (y == 11 || y == 12))
                            put(out, x, y, z, Part.DOOR);
                        else put(out, x, y, z,
                                Math.abs(x) == half && y == 12 && z == rear + 3
                                        ? Part.WINDOW : Part.CABIN_WALL);
                    }
                }
        }
        // Every installed mast is rooted in the main deck; the complete
        // visible rig begins above the highest raised floor, never below it.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i), tier = p.sailTiers().get(i);
            int upper = sailTop(p, tier), yard = sailLowerYard(p);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 11; y <= upper + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(out, x, yard, z, Part.YARD);
                put(out, x, upper + 1, z, Part.YARD);
            }
            for (int y = sailBottom(p); y <= upper; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !out.containsKey(new Cell(x, y, z + 1)))
                        put(out, x, y, z + 1, Part.SAIL);
        }
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }


    // Frozen historic layout from commit 21440869; read-only upgrade source.
    private static int historicBeam3(ShipSuperstructurePlan plan, int z) {
        int end = plan.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = plan.hullWidth() / 2;
        int bow = end - z, aft = end + z;
        int bowRun = Math.min(12, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * Math.sin(t * Math.PI / 2D)));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        int waist = Math.abs(z) <= 3 ? 1 : 0;
        return half - waist;
    }

    private static Map<Cell, Part> historicHull3(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 2;
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam3(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0 && Math.abs(z) < end - 1) put(out, x, 0, z, Part.KEEL);
                // The true bilge narrows toward the keel and flares toward
                // the gunports. Only rings, not a solid full-width box, rise
                // around the three-block-high hold interior.
                if (abs <= Math.max(1, beam - 4)) put(out, x, 1, z, Part.HULL);
                else if (abs <= beam - 3) put(out, x, 2, z, Part.HULL);
                else if (abs <= beam - 2) put(out, x, 3, z, Part.HULL);
                else if (abs == beam - 1) put(out, x, 4, z, Part.HULL);
                // Wide gun floor below a narrower weather deck.
                if (abs < beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 5, z, Part.DECK);
                if (abs <= beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 10, z, Part.DECK);
                for (int y = 5; y <= 9; y++) {
                    int skin = y == 7 || y == 8 ? beam : beam - 1;
                    if (abs != skin) continue;
                    boolean port = Math.abs(z) <= end - 5
                            && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                            && (y == 7 || y == 8);
                    if (!port) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM
                                    : Math.floorMod(z, 5) == 0
                                            ? Part.FRAME : Part.HULL);
                }
                // Tumblehome: gunport shoulders are wider than the upper rail.
                if (abs == beam - 1) {
                    put(out, x, 11, z,
                            Math.floorMod(z, 5) == 0 ? Part.FRAME : Part.HULL);
                    int bow = end - z, stern = end + z;
                    int lift = bow <= 2 ? 2 : bow <= 6 ? 1
                            : stern <= 3 ? 2 : stern <= 6 ? 1 : 0;
                    for (int y = 12; y <= 11 + lift; y++)
                        put(out, x, y, z,
                                y == 11 + lift ? Part.TRIM : Part.FRAME);
                }
            }
        }
        // Seal the transverse end faces. The original blueprint only
        // generated the left/right skin at each station: looking along Z
        // exposed a full-height open mouth into the gun deck and hold.
        // Fill the end caps and the exposed shoulders at every abrupt
        // change in beam. Keep deliberate side gunports untouched.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam3(p, z);
            int outwardBeam = z >= 0 ? historicBeam3(p, z + 1)
                    : historicBeam3(p, z - 1);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                boolean endFace = Math.abs(z) == end;
                boolean exposedShoulder = outwardBeam >= 0 && abs > outwardBeam;
                if (!endFace && !exposedShoulder) continue;
                for (int y = 5; y <= 9; y++) {
                    // Side ports are exactly two blocks high and only at
                    // stations far from either extremity.
                    boolean gunport = abs == beam && (y == 7 || y == 8)
                            && Math.abs(z) <= end - 5 && Math.abs(z) >= 3
                            && Math.floorMod(z + end, 4) == 0;
                    if (!gunport) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM : Part.HULL);
                }
                // Close the hold and deck seams at the very tips too:
                // otherwise the end still exposes a two-to-four-block slot
                // when viewed straight down the ship axis.
                if (endFace) {
                    for (int y = 2; y <= 4; y++)
                        put(out, x, y, z, y == 4 ? Part.TRIM : Part.HULL);
                    put(out, x, 10, z, Part.DECK);
                    put(out, x, 11, z, Part.HULL);
                    if (abs <= Math.max(0, beam - 1)) put(out, x, 12, z, Part.TRIM);
                }
            }
        }
        // Short true bowsprit: part of the moving block palette, not a fake
        // mast or a detached decorative entity.
        for (int z = end; z <= end + 3; z++)
            put(out, 0, 12, z, Part.BOWSPRIT);
        // Permanent functional lighting, not glowing cosmetic textures.
        // Posts stand inside the bulwark without blocking the central aisle.
        for (int z : new int[]{-end + 8, end - 8}) {
            int beam = historicBeam3(p, z);
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 11, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Real hatch between hold, gun deck and weather deck.
        for (int y = 2; y <= 10; y++) {
            put(out, -3, y, 1, Part.FRAME);
            put(out, -2, y, 1, Part.LADDER);
        }
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 11; y <= 15; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 16, front));
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 6;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 16; y <= 20; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 21, front));
            put(out, half - 1, 21, rear + 2, Part.LANTERN);
            put(out, -half + 1, 21, rear + 2, Part.LANTERN);
        }
        if (p.cabin()) {
            int half = Math.min(4, p.hullWidth() / 2 - 4);
            // Cabin is on the weather deck, beneath the actual stern deck.
            for (int z = rear + 1; z <= rear + 6; z++)
                for (int x = -half; x <= half; x++) {
                    boolean wall = Math.abs(x) == half || z == rear + 1 || z == rear + 6;
                    if (!wall) continue;
                    for (int y = 11; y <= 14; y++) {
                        if (z == rear + 6 && x == 0 && (y == 11 || y == 12))
                            put(out, x, y, z, Part.DOOR);
                        else put(out, x, y, z,
                                Math.abs(x) == half && y == 12 && z == rear + 3
                                        ? Part.WINDOW : Part.CABIN_WALL);
                    }
                }
        }
        // Every installed mast is rooted in the main deck; the complete
        // visible rig begins above the highest raised floor, never below it.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i), tier = p.sailTiers().get(i);
            int upper = sailTop(p, tier), yard = sailLowerYard(p);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 11; y <= upper + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(out, x, yard, z, Part.YARD);
                put(out, x, upper + 1, z, Part.YARD);
            }
            for (int y = sailBottom(p); y <= upper; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !out.containsKey(new Cell(x, y, z + 1)))
                        put(out, x, y, z + 1, Part.SAIL);
        }
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }


    // Frozen historic layout from commit 55fcf100; read-only upgrade source.
    private static int historicBeam4(ShipSuperstructurePlan plan, int z) {
        int end = plan.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = plan.hullWidth() / 2;
        int bow = end - z, aft = end + z;
        int bowRun = Math.min(12, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * Math.sin(t * Math.PI / 2D)));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        int waist = Math.abs(z) <= 3 ? 1 : 0;
        return half - waist;
    }

    private static Map<Cell, Part> historicHull4(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 2;
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam4(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0 && Math.abs(z) < end - 1) put(out, x, 0, z, Part.KEEL);
                // The true bilge narrows toward the keel and flares toward
                // the gunports. Only rings, not a solid full-width box, rise
                // around the three-block-high hold interior.
                if (abs <= Math.max(1, beam - 4)) put(out, x, 1, z, Part.HULL);
                else if (abs <= beam - 3) put(out, x, 2, z, Part.HULL);
                else if (abs <= beam - 2) put(out, x, 3, z, Part.HULL);
                else if (abs == beam - 1) put(out, x, 4, z, Part.HULL);
                // Wide gun floor below a narrower weather deck.
                if (abs < beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 5, z, Part.DECK);
                if (abs <= beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 10, z, Part.DECK);
                for (int y = 5; y <= 9; y++) {
                    int skin = y == 7 || y == 8 ? beam : beam - 1;
                    if (abs != skin) continue;
                    boolean port = Math.abs(z) <= end - 5
                            && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                            && (y == 7 || y == 8);
                    if (!port) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM
                                    : Math.floorMod(z, 5) == 0
                                            ? Part.FRAME : Part.HULL);
                }
                // Tumblehome: gunport shoulders are wider than the upper rail.
                if (abs == beam - 1) {
                    put(out, x, 11, z,
                            Math.floorMod(z, 5) == 0 ? Part.FRAME : Part.HULL);
                    int bow = end - z, stern = end + z;
                    int lift = bow <= 2 ? 2 : bow <= 6 ? 1
                            : stern <= 3 ? 2 : stern <= 6 ? 1 : 0;
                    for (int y = 12; y <= 11 + lift; y++)
                        put(out, x, y, z,
                                y == 11 + lift ? Part.TRIM : Part.FRAME);
                }
            }
        }
        // Seal the transverse end faces. The original blueprint only
        // generated the left/right skin at each station: looking along Z
        // exposed a full-height open mouth into the gun deck and hold.
        // Fill the end caps and the exposed shoulders at every abrupt
        // change in beam. Keep deliberate side gunports untouched.
        for (int z = -end; z <= end; z++) {
            int beam = historicBeam4(p, z);
            int outwardBeam = z >= 0 ? historicBeam4(p, z + 1)
                    : historicBeam4(p, z - 1);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                boolean endFace = Math.abs(z) == end;
                boolean exposedShoulder = outwardBeam >= 0 && abs > outwardBeam;
                if (!endFace && !exposedShoulder) continue;
                for (int y = 5; y <= 9; y++) {
                    // Side ports are exactly two blocks high and only at
                    // stations far from either extremity.
                    boolean gunport = abs == beam && (y == 7 || y == 8)
                            && Math.abs(z) <= end - 5 && Math.abs(z) >= 3
                            && Math.floorMod(z + end, 4) == 0;
                    if (!gunport) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM : Part.HULL);
                }
                // Close the hold and deck seams at the very tips too:
                // otherwise the end still exposes a two-to-four-block slot
                // when viewed straight down the ship axis.
                if (endFace) {
                    for (int y = 2; y <= 4; y++)
                        put(out, x, y, z, y == 4 ? Part.TRIM : Part.HULL);
                    put(out, x, 10, z, Part.DECK);
                    put(out, x, 11, z, Part.HULL);
                    if (abs <= Math.max(0, beam - 1)) put(out, x, 12, z, Part.TRIM);
                }
            }
        }
        // Short true bowsprit: part of the moving block palette, not a fake
        // mast or a detached decorative entity.
        for (int z = end; z <= end + 3; z++)
            put(out, 0, 12, z, Part.BOWSPRIT);
        // Permanent functional lighting, not glowing cosmetic textures.
        // Posts stand inside the bulwark without blocking the central aisle.
        for (int z : new int[]{-end + 8, end - 8}) {
            int beam = historicBeam4(p, z);
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 11, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Real hatch between hold, gun deck and weather deck.
        for (int y = 2; y <= 10; y++) {
            put(out, -3, y, 1, Part.FRAME);
            put(out, -2, y, 1, Part.LADDER);
        }
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 11; y <= 15; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 16, front));
            // A raised deck needs a real aft bulkhead. Side pillars alone
            // leave a giant through-hole when viewed from the stern.
            for (int x = -half; x <= half; x++)
                for (int y = 11; y <= 14; y++)
                    put(out, x, y, rear,
                            y == 12 && (x == -half + 2 || x == half - 2)
                                    ? Part.WINDOW : Part.CABIN_WALL);
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 6;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 16; y <= 20; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 21, front));
            // Third-level quarterdeck also closes across its entire aft face.
            for (int x = -half; x <= half; x++)
                for (int y = 16; y <= 19; y++)
                    put(out, x, y, rear,
                            y == 17 && Math.abs(x) == Math.max(1, half - 1)
                                    ? Part.WINDOW : Part.CABIN_WALL);
            put(out, half - 1, 21, rear + 2, Part.LANTERN);
            put(out, -half + 1, 21, rear + 2, Part.LANTERN);
        }
        if (p.cabin()) {
            int half = Math.min(4, p.hullWidth() / 2 - 4);
            // Cabin is on the weather deck, beneath the actual stern deck.
            for (int z = rear + 1; z <= rear + 6; z++)
                for (int x = -half; x <= half; x++) {
                    boolean wall = Math.abs(x) == half || z == rear + 1 || z == rear + 6;
                    if (!wall) continue;
                    for (int y = 11; y <= 14; y++) {
                        if (z == rear + 6 && x == 0 && (y == 11 || y == 12))
                            put(out, x, y, z, Part.DOOR);
                        else put(out, x, y, z,
                                Math.abs(x) == half && y == 12 && z == rear + 3
                                        ? Part.WINDOW : Part.CABIN_WALL);
                    }
                }
        }
        // Every installed mast is rooted in the main deck; the complete
        // visible rig begins above the highest raised floor, never below it.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i), tier = p.sailTiers().get(i);
            int upper = sailTop(p, tier), yard = sailLowerYard(p);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 11; y <= upper + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(out, x, yard, z, Part.YARD);
                put(out, x, upper + 1, z, Part.YARD);
            }
            for (int y = sailBottom(p); y <= upper; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !out.containsKey(new Cell(x, y, z + 1)))
                        put(out, x, y, z + 1, Part.SAIL);
        }
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }


}
