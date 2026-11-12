package org.slavicmyths.verify;

import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipMediumSailPlan;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipClearanceSailPlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipModularBlockHull;
import org.slavicmyths.ships.ShipSectionPlan;

/** Regression coverage for player carry and canonical sail/deck planning. */
@GameTestHolder("slavicmyths_ocean26")
@PrefixGameTestTemplate(false)
public final class Ocean26ShipP0GameTests {
    @GameTest(template = "empty", timeoutTicks = 100, batch = "ocean_p0_crew")
    public static void deckCarryAvoidsDuplicateClientTransform(GameTestHelper t) {
        FirstShipHull.Point oldFeet = new FirstShipHull.Point(1D, 2D);
        FirstShipHull.Point carried = FirstShipHull.transfer(
                oldFeet.x(), oldFeet.z(), 0D, 0D, 0D, .2D, .15D, 8D);
        t.assertTrue(!FirstShipHull.likelyAlreadyCarried(oldFeet, oldFeet, carried),
                "A standing walker without a carry must receive the ship delta");
        t.assertTrue(FirstShipHull.likelyAlreadyCarried(oldFeet, carried, carried),
                "An already carried walker must not receive the ship delta twice");
        FirstShipHull.Point local = FirstShipHull.toLocal(
                carried.x(), carried.z(), .2D, .15D, 8D);
        t.assertTrue(Math.abs(local.x() - 1D) < 1E-9D
                && Math.abs(local.z() - 2D) < 1E-9D,
                "Rigid pose changed the walker's local coordinates");
        FirstShipHull.Point tiny = new FirstShipHull.Point(1.005D, 2D);
        t.assertTrue(!FirstShipHull.likelyAlreadyCarried(oldFeet, tiny, tiny),
                "Sub-noise movement should not trigger the correction guard");
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 150, batch = "ocean_p0_upgrades")
    public static void rigAndDeckUpgradeOrderProducesIdenticalPalettes(GameTestHelper t) {
        int comparisons = 0;
        for (ShipSectionPlan.ShipClass type : new ShipSectionPlan.ShipClass[]{
                ShipSectionPlan.ShipClass.MEDIUM, ShipSectionPlan.ShipClass.LARGE,
                ShipSectionPlan.ShipClass.LARGEST}) {
            ShipBuildConfiguration initial = ShipBuildConfiguration.initial(type);
            for (int decks = 2; decks <= type.maxDecks; decks++) {
                ShipBuildConfiguration sailsFirst = initial.withSail(1, 2).withDecks(decks);
                ShipBuildConfiguration decksFirst = initial.withDecks(decks).withSail(1, 2);
                compare(t, sailsFirst, decksFirst, type + " deck " + decks);
                comparisons++;
                if (type == ShipSectionPlan.ShipClass.LARGE
                        || type == ShipSectionPlan.ShipClass.LARGEST) {
                    ShipBuildConfiguration roomsLast = sailsFirst.withCabin(true).withBridge(true);
                    ShipBuildConfiguration roomsFirst = initial.withDecks(decks)
                            .withCabin(true).withBridge(true).withSail(1, 2);
                    compare(t, roomsLast, roomsFirst, type + " rooms " + decks);
                    comparisons++;
                    ShipBuildConfiguration lengthLast = initial.withSail(1, 2)
                            .resize(type.maxLength, type.maxWidth).withDecks(decks);
                    ShipBuildConfiguration lengthFirst = initial
                            .resize(type.maxLength, type.maxWidth)
                            .withDecks(decks).withSail(1, 2);
                    compare(t, lengthLast, lengthFirst, type + " dimensions " + decks);
                    comparisons++;
                }
            }
        }
        t.assertTrue(comparisons >= 10, "Insufficient order comparisons");
        System.out.println("OCEAN26_SHIP_P0_ORDER_PASS comparisons=" + comparisons);
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "ocean_p0_versions")
    public static void largeV6AndLegacyV5V4MaintainClearanceAndSaves(GameTestHelper t) {
        for (ShipSectionPlan.ShipClass type : new ShipSectionPlan.ShipClass[]{
                ShipSectionPlan.ShipClass.LARGE, ShipSectionPlan.ShipClass.LARGEST}) {
            ShipBuildConfiguration latest = ShipBuildConfiguration.initial(type)
                    .withDecks(3).withSail(1, 2);
            t.assertTrue(latest.hullGeometry() == 6, type + " new ships must use geometry 6");
            t.assertTrue(ShipBuildConfiguration.load(latest.save()).equals(latest),
                    "New geometry lost its NBT version");
            var layout = ShipHullVoxelLayout.generate(latest.structure(), 6);
            ShipBuildConfiguration savedV5 = new ShipBuildConfiguration(type, 0, 0,
                    List.of(1, 2), 3, false, false, 5);
            t.assertTrue(ShipBuildConfiguration.load(savedV5.save()).equals(savedV5),
                    "Existing V5 packed ship no longer loads");
            t.assertTrue(ShipHullVoxelLayout.generate(savedV5.structure(), 5)
                    .values().stream().noneMatch(p -> p == ShipHullVoxelLayout.Part.STAIR),
                    "Existing V5 geometry must not silently gain V6 stair treads");
            ShipBuildConfiguration previousRig = new ShipBuildConfiguration(type, 0, 0,
                    List.of(1, 2), 3, false, false, 4);
            t.assertTrue(ShipBuildConfiguration.load(previousRig.save()).equals(previousRig),
                    "Existing V4 packed ship no longer loads");
            t.assertTrue(ShipHullVoxelLayout.generate(previousRig.structure(), 4).size() > 0,
                    "Existing V4 physical ship geometry is missing");
            var actual = org.slavicmyths.ships.ShipRiggingPlan.forPlan(
                    latest.structure(), latest.hullGeometry());
            for (var mast : actual.masts()) {
                t.assertTrue(mast.yards().size() == 1,
                        type + " new mast must start with a single physical yard");
                int highestDeck = mast.panels().get(0).bottom() - 5;
                int lowestYard = mast.yards().stream().mapToInt(yard -> yard.y())
                        .min().orElseThrow();
                t.assertTrue(lowestYard - highestDeck - 1 >= 3,
                        type + " real yard blocks upper-deck headroom");
                for (var yard : mast.yards()) {
                    t.assertTrue(layout.get(new ShipHullVoxelLayout.Cell(
                            yard.span(), yard.y(), mast.z()))
                            == ShipHullVoxelLayout.Part.YARD,
                            type + " physical independent yard missing at y=" + yard.y());
                }
            }
            ShipBuildConfiguration legacy = new ShipBuildConfiguration(
                    type, 0, 0, List.of(1), 1, false, false, 2);
            t.assertTrue(ShipBuildConfiguration.load(legacy.save()).equals(legacy),
                    "Existing V2 ship NBT no longer loads");
            t.assertTrue(ShipHullVoxelLayout.generate(legacy.structure(), 2).get(
                    new ShipHullVoxelLayout.Cell(2, 7, 0))
                    == ShipHullVoxelLayout.Part.YARD,
                    "Legacy V2 lower yard was silently rebuilt");
        }
        t.succeed();
    }

    private static void compare(GameTestHelper t, ShipBuildConfiguration a,
            ShipBuildConfiguration b, String label) {
        t.assertTrue(a.equals(b), label + " canonical blueprints differ");
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> voxelsA =
                ShipHullVoxelLayout.generate(a.structure(), a.hullGeometry());
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> voxelsB =
                ShipHullVoxelLayout.generate(b.structure(), b.hullGeometry());
        t.assertTrue(voxelsA.equals(voxelsB), label + " actual voxels differ");
        t.assertTrue(ShipModularBlockHull.localPalette(a).equals(
                ShipModularBlockHull.localPalette(b)), label + " real blocks differ");
        t.assertTrue(ShipModularBlockHull.localMotionPalette(a, new ListTag()).equals(
                ShipModularBlockHull.localMotionPalette(b, new ListTag())),
                label + " moving palettes differ");
        t.assertTrue(ShipBuildConfiguration.load(a.save()).equals(a),
                label + " blueprint NBT changed");
        if (a.hullGeometry() == 3) {
            for (var sail : FirstShipMediumSailPlan.forPlan(a.structure()).sails())
                t.assertTrue(sail.lowerYardY() - sail.highestDeckY() - 1 >= 3,
                        label + " MEDIUM upper-deck collision");
        } else if (a.hullGeometry() >= 4) {
            for (var sail : ShipClearanceSailPlan.forPlan(a.structure()).sails())
                t.assertTrue(sail.lowerYardY() - sail.highestDeckY() - 1 >= 3,
                        label + " LARGE upper-deck collision");
        }
    }

    private Ocean26ShipP0GameTests() {}
}
