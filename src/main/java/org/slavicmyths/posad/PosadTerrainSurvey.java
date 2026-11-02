package org.slavicmyths.posad;

import java.util.ArrayList;
import java.util.List;

/**
 * Tick-budgeted equivalent of PosadTerrain.inspect for the OP-only QA builder.
 * One call to advance(n) executes at most n terrain reads; no background threads,
 * chunk tickets, sleeps or writes. Once finished it never inspects again.
 *
 * Native worldgen deliberately keeps its original pure/noise planner until
 * production-performance tests separately authorize a worldgen rollout.
 */
public final class PosadTerrainSurvey {
    private enum Phase { CITY_AIR, CITY_BASE, FOUNDATION, BANK_BASE, BANK_AIR, BRIDGE_AIR, BRIDGE_BASE, BRIDGE_SUPPORT }
    private final PosadTerrain.Columns columns;
    private final int anchorX, deckY, anchorZ, firstTurns;
    private final List<PosadTerrain.Support> foundation = new ArrayList<>();
    private final List<PosadTerrain.Support> bridge = new ArrayList<>();
    private Phase phase = Phase.CITY_AIR;
    private int x, z, delta = 1, depth = 3;
    private int bridgeLength = 1, option, lane = 53, bridgeStep = 1, reads;
    private PosadTerrain.Decision decision;

    public PosadTerrainSurvey(PosadTerrain.Columns columns, int anchorX, int deckY, int anchorZ, int firstTurns) {
        this.columns = java.util.Objects.requireNonNull(columns, "columns");
        this.anchorX = anchorX;
        this.deckY = deckY;
        this.anchorZ = anchorZ;
        this.firstTurns = Math.floorMod(firstTurns, 4);
    }

    public boolean advance(int maxReads) {
        if (maxReads < 1) throw new IllegalArgumentException("maxReads must be positive");
        for (int i = 0; i < maxReads && decision == null; i++) inspectOne();
        return decision != null;
    }

    public boolean completed() { return decision != null; }
    public PosadTerrain.Decision result() {
        if (decision == null) throw new IllegalStateException("Survey has not completed");
        return decision;
    }
    public int inspected() { return reads; }
    public String stage() { return phase.name(); }
    private PosadTerrain.Material get(int wx, int wy, int wz) {
        reads++;
        return columns.at(wx, wy, wz);
    }
    private int rotation() { return Math.floorMod(firstTurns + option, 4); }
    private PosadGeometry.Cell shoreCell(int localX, int localZ) {
        return switch (rotation()) {
            case 0 -> new PosadGeometry.Cell(anchorX + localX, anchorZ + localZ);
            case 1 -> new PosadGeometry.Cell(anchorX + 112 - localZ, anchorZ + localX);
            case 2 -> new PosadGeometry.Cell(anchorX + 112 - localX, anchorZ + 112 - localZ);
            default -> new PosadGeometry.Cell(anchorX + localZ, anchorZ + 112 - localX);
        };
    }

    private void fail(String reason) { decision = new PosadTerrain.Decision(null, reason, reads); }

    private void nextCityCell() {
        delta = 1;
        x++;
        if (x == 113) { x = 0; z++; }
        if (z == 113) { x = 0; z = 0; depth = 3; phase = Phase.FOUNDATION; }
        else phase = Phase.CITY_AIR;
    }

    private void nextFoundation() {
        depth = 3;
        x += PosadTerrain.SUPPORT_STEP;
        if (x >= 113) { x = 0; z += PosadTerrain.SUPPORT_STEP; }
        if (z >= 113) {
            option = 0;
            bridgeLength = 1;
            lane = 53;
            phase = Phase.BANK_BASE;
        }
    }

    private void rejectBankOrBridge() {
        option++;
        if (option == 4) { option = 0; bridgeLength++; }
        if (bridgeLength > PosadTerrain.MAX_BRIDGE) {
            fail("NO_REACHABLE_SHORE");
            return;
        }
        bridge.clear();
        lane = 53;
        bridgeStep = 1;
        delta = 1;
        depth = 3;
        phase = Phase.BANK_BASE;
    }

    private void nextBridgeCell() {
        lane++;
        if (lane == 61) { lane = 52; bridgeStep++; }
        if (bridgeStep > bridgeLength) {
            List<PosadTerrain.Support> rotated = new ArrayList<>(foundation.size() + bridge.size());
            for (var support : foundation) {
                var cell = PosadGeometry.rotate(new PosadGeometry.Cell(support.x(), support.z()), -rotation());
                rotated.add(new PosadTerrain.Support(cell.x(), cell.z(), support.depth()));
            }
            rotated.addAll(bridge);
            decision = new PosadTerrain.Decision(new PosadTerrain.Plan(anchorX, deckY, anchorZ, rotation(), bridgeLength, rotated), "ACCEPTED", reads);
        } else {
            delta = 1;
            phase = Phase.BRIDGE_AIR;
        }
    }

    private boolean pier() {
        return (bridgeStep == bridgeLength || bridgeStep % PosadTerrain.SUPPORT_STEP == 0) && (lane == 52 || lane == 60);
    }

    private void inspectOne() {
        switch (phase) {
            case CITY_AIR -> {
                if (get(anchorX + x, deckY + delta, anchorZ + z) != PosadTerrain.Material.AIR) {
                    fail("HEADROOM");
                    return;
                }
                if (++delta > 17) { delta = 0; phase = Phase.CITY_BASE; }
            }
            case CITY_BASE -> {
                if (get(anchorX + x, deckY + delta, anchorZ + z) == PosadTerrain.Material.PROTECTED) {
                    fail("PROTECTED_PLATFORM");
                    return;
                }
                if (--delta < -2) nextCityCell();
            }
            case FOUNDATION -> {
                PosadTerrain.Material material = get(anchorX + x, deckY - depth, anchorZ + z);
                if (material == PosadTerrain.Material.NATURAL) {
                    foundation.add(new PosadTerrain.Support(x, z, depth));
                    nextFoundation();
                } else if (material == PosadTerrain.Material.PROTECTED || depth == PosadTerrain.MAX_SUPPORT_DEPTH) {
                    fail("NO_FOOTING");
                } else depth++;
            }
            case BANK_BASE -> {
                PosadGeometry.Cell location = shoreCell(lane, 112 + bridgeLength);
                if (get(location.x(), deckY - 1, location.z()) != PosadTerrain.Material.NATURAL) rejectBankOrBridge();
                else { delta = 1; phase = Phase.BANK_AIR; }
            }
            case BANK_AIR -> {
                PosadGeometry.Cell location = shoreCell(lane, 112 + bridgeLength);
                if (get(location.x(), deckY + delta, location.z()) != PosadTerrain.Material.AIR) {
                    rejectBankOrBridge();
                    return;
                }
                if (++delta > 3) {
                    lane++;
                    if (lane > 59) { lane = 52; bridgeStep = 1; delta = 1; bridge.clear(); phase = Phase.BRIDGE_AIR; }
                    else phase = Phase.BANK_BASE;
                }
            }
            case BRIDGE_AIR -> {
                PosadGeometry.Cell location = shoreCell(lane, 112 + bridgeStep);
                if (get(location.x(), deckY + delta, location.z()) != PosadTerrain.Material.AIR) {
                    rejectBankOrBridge();
                    return;
                }
                if (++delta > 3) { delta = 0; phase = Phase.BRIDGE_BASE; }
            }
            case BRIDGE_BASE -> {
                PosadGeometry.Cell location = shoreCell(lane, 112 + bridgeStep);
                if (get(location.x(), deckY + delta, location.z()) == PosadTerrain.Material.PROTECTED) {
                    rejectBankOrBridge();
                    return;
                }
                if (--delta < -2) {
                    if (pier()) { depth = 3; phase = Phase.BRIDGE_SUPPORT; }
                    else nextBridgeCell();
                }
            }
            case BRIDGE_SUPPORT -> {
                PosadGeometry.Cell location = shoreCell(lane, 112 + bridgeStep);
                PosadTerrain.Material material = get(location.x(), deckY - depth, location.z());
                if (material == PosadTerrain.Material.NATURAL) {
                    bridge.add(new PosadTerrain.Support(lane, 112 + bridgeStep, depth));
                    nextBridgeCell();
                } else if (material == PosadTerrain.Material.PROTECTED || depth == PosadTerrain.MAX_SUPPORT_DEPTH) {
                    rejectBankOrBridge();
                } else depth++;
            }
        }
    }
}
