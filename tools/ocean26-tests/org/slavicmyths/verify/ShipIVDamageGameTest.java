package org.slavicmyths.verify;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipModularBlockHull;
import org.slavicmyths.ships.ShipMovingContraption;
import org.slavicmyths.ships.ShipRigidMotion;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import java.util.Map;

/** Isolated large fixture; never shares world space with the other ocean tests. */
@GameTestHolder("slavicmyths_ship_iv_damage")
@PrefixGameTestTemplate(false)
public final class ShipIVDamageGameTest {
    @GameTest(template = "empty", timeoutTicks = 150, batch = "iv_damaged_voyage")
    public static void frigateDamagedVoyage(GameTestHelper test) {
        Ocean26GameTests.modularClassVoyage(test,
                ShipSectionPlan.ShipClass.LARGEST, false, true);
    }

    @GameTest(template = "empty", timeoutTicks = 250, batch = "iii_full_hull_scan")
    public static void brigantineFullHullScan(GameTestHelper test) {
        scanHull(test, ShipSectionPlan.ShipClass.LARGE);
    }

    @GameTest(template = "empty", timeoutTicks = 250, batch = "iv_full_hull_scan")
    public static void frigateFullHullScan(GameTestHelper test) {
        scanHull(test, ShipSectionPlan.ShipClass.LARGEST);
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "iv_deck_decor")
    public static void frigateDeckAllowsSafeDecoration(GameTestHelper test) {
        var config = ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.LARGEST)
                .withCabin(true).withBridge(true).withDecks(3);
        int rear = -config.length() / 2 + 4;
        // Keep test decorations off the relocated x=6/7 exterior stair
        // while verifying ordinary player building remains allowed.
        BlockPos first = new BlockPos(4, 12, rear + 20);
        BlockPos second = first.east();
        test.assertTrue(ShipModularBlockHull.movingCargoCell(config, first)
                        && ShipModularBlockHull.movingCargoCell(config, second),
                "IV open weather deck wrongly rejects ordinary supported placements");
        var cargo = ShipModularBlockHull.withAddedMovingCargo(test.getLevel(),
                config, new ListTag(), first, Blocks.OAK_TRAPDOOR.defaultBlockState());
        test.assertTrue(cargo != null && cargo.size() == 1,
                "IV decorative trapdoor cannot be persisted as moving cargo");
        cargo = ShipModularBlockHull.withAddedMovingCargo(test.getLevel(),
                config, cargo, second, Blocks.LANTERN.defaultBlockState());
        test.assertTrue(cargo != null && cargo.size() == 2
                        && ShipModularBlockHull.rotateCargo(config, cargo, 0, 1) != null,
                "IV lantern decoration cannot survive pack/rotation");
        BlockPos stacked = first.above();
        test.assertTrue(ShipModularBlockHull.movingCargoCell(config, stacked),
                "Second level of IV player-built deck decoration remains blocked");
        cargo = ShipModularBlockHull.withAddedMovingCargo(test.getLevel(),
                config, cargo, stacked, Blocks.SPRUCE_PLANKS.defaultBlockState());
        test.assertTrue(cargo != null && cargo.size() == 3,
                "Stacked IV deck decoration cannot survive save/load");
        test.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 300, batch = "iv_upgraded_doors")
    public static void frigateUpgradedDoorAfterSailing(GameTestHelper test) {
        ShipBuildConfiguration config = ShipBuildConfiguration.initial(
                ShipSectionPlan.ShipClass.LARGEST)
                .withCabin(true).withBridge(true).withDecks(3);
        ServerLevel level = test.getLevel();
        BlockPos anchor = test.absolutePos(new BlockPos(70, 4, 70));
        int edge = config.length() / 2 + 22;
        level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED,
                new ChunkPos(anchor), 5, new ChunkPos(anchor));
        for (int x = -config.width() / 2 - 9; x <= config.width() / 2 + 9; x++)
            for (int z = -edge; z <= edge; z++) {
                BlockPos column = anchor.offset(x, 0, z);
                level.setBlock(column.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(column, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 48; y++)
                    level.setBlock(column.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        test.assertTrue(ShipModularBlockHull.place(level, anchor, 0, config, new ListTag()),
                "IV upgraded ship failed initial construction");
        ShipPersistenceGameTest.verifyIVDoor(
                test, level, anchor, 0, config, "upgraded before sailing");
        var initial = ShipModularBlockHull.pack(level, anchor, 0, config);
        test.assertTrue(initial != null, "IV upgraded ship cannot pack");
        test.assertTrue(ShipModularBlockHull.remove(level, anchor, 0, config)
                        && ShipModularBlockHull.place(level, anchor, 0, config, initial),
                "IV upgraded ship cannot unpack");
        ShipPersistenceGameTest.verifyIVDoor(
                test, level, anchor, 0, config, "upgraded after pack/unpack");
        ShipMovingContraption moving = ShipMovingContraption.prepare(level, anchor, 0, config);
        test.assertTrue(moving != null && moving.detach(level, anchor),
                "IV upgraded ship cannot begin sailing");
        double departureZ = moving.pose().z();
        for (int tick = 0; tick < 18; tick++)
            moving.tick(level, new ShipRigidMotion.Control(1D, 0D));
        test.assertTrue(moving.pose().z() > departureZ + .01D,
                "IV upgraded ship did not travel before saving");
        var saved = moving.save();
        var loaded = ShipMovingContraption.load(saved);
        test.assertTrue(loaded != null && loaded.detached()
                        && loaded.modularConfiguration() != null,
                "IV upgraded sailing payload failed save/load");
        loaded.motion().restore(new ShipRigidMotion.Pose(
                anchor.getX() + .5D, anchor.getZ() + .5D, 0D), 0D, 0D);
        test.assertTrue(loaded.park(level),
                "IV upgraded ship could not park after a moving save/load");
        ShipPersistenceGameTest.verifyIVDoor(
                test, level, anchor, 0, config, "upgraded after moving save/load/park");
        test.succeed();
    }

    private static void scanHull(GameTestHelper test, ShipSectionPlan.ShipClass type) {
        ShipBuildConfiguration config = ShipBuildConfiguration.initial(type);
        ServerLevel level = test.getLevel();
        BlockPos anchor = test.absolutePos(new BlockPos(45, 4, 60));
        int end = config.length() / 2, half = config.width() / 2;
        test.assertTrue(config.length() / (double)config.width() >= 3.5D
                && config.length() / (double)config.width() <= 4.5D,
                "Incorrect length-to-beam ratio");
        level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED,
                new ChunkPos(anchor), 4, new ChunkPos(anchor));
        for (int x = -half - 10; x <= half + 10; x++)
            for (int z = -end - 10; z <= end + 10; z++) {
                BlockPos pos = anchor.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 48; y++)
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        test.assertTrue(ShipModularBlockHull.place(level, anchor, 0, config, new ListTag()),
                "Ship placement failed");
        test.runAfterDelay(20, () -> {
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> plan =
                    ShipHullVoxelLayout.generate(config.structure());
            int portCount = 0, stairCount = 0, lightChecks = 0;
            int hangingLights = 0;
            int gunFloor = type == ShipSectionPlan.ShipClass.LARGE ? 5 : 6;
            for (int z = -end; z <= end; z++) {
                int beam = ShipHullVoxelLayout.halfBeam(config.structure(), z);
                for (int side : new int[]{-1, 1}) {
                    int sideX = side * (type == ShipSectionPlan.ShipClass.LARGE
                            ? Math.max(0, beam - 1) : beam);
                    for (int y = 2; y <= (type == ShipSectionPlan.ShipClass.LARGE ? 9 : 11); y++) {
                        var expected = plan.get(new ShipHullVoxelLayout.Cell(sideX, y, z));
                        BlockPos shell = FirstShipBlockHull.world(anchor, 0, sideX, y, z);
                        test.assertTrue(expected != null,
                                "Unplanned broadside void at " + sideX + "," + y + "," + z);
                        test.assertTrue(expected == ShipHullVoxelLayout.Part.GUNPORT_SHUTTER
                                ? level.getBlockState(shell).isAir()
                                : !level.getBlockState(shell).isAir(),
                                "Lower hull shell hole at " + sideX + "," + y + "," + z);
                    }
                }
                for (int x = -beam; x <= beam; x++) {
                    if (plan.get(new ShipHullVoxelLayout.Cell(x, 1, z))
                            == ShipHullVoxelLayout.Part.HULL)
                        test.assertTrue(!level.getBlockState(FirstShipBlockHull.world(
                                anchor, 0, x, 1, z)).isAir(), "Open lower hull floor");
                    for (int y = 2; y <= gunFloor + 4; y++) {
                        var part = plan.get(new ShipHullVoxelLayout.Cell(x, y, z));
                        BlockPos pos = FirstShipBlockHull.world(anchor, 0, x, y, z);
                        if (part == ShipHullVoxelLayout.Part.GUNPORT_SHUTTER) {
                            portCount++;
                            test.assertTrue(level.getBlockState(pos).isAir()
                                    && Math.abs(x) >= beam - 1 && y > gunFloor,
                                    "Gun port is obstructed or misplaced");
                        }
                        if (part == ShipHullVoxelLayout.Part.STAIR) {
                            stairCount++;
                            test.assertTrue(level.getBlockState(pos).is(Blocks.SPRUCE_STAIRS)
                                    && level.getBlockState(pos).getValue(StairBlock.FACING)
                                            == Direction.NORTH,
                                    "Stair faces away from the climb");
                            var left = plan.get(new ShipHullVoxelLayout.Cell(x - 1, y, z));
                            var right = plan.get(new ShipHullVoxelLayout.Cell(x + 1, y, z));
                            test.assertTrue(left == ShipHullVoxelLayout.Part.STAIR
                                    || right == ShipHullVoxelLayout.Part.STAIR,
                                    "Single scattered step");
                        }
                    }
                }
            }
            for (var entry : plan.entrySet()) {
                var c = entry.getKey();
                if (entry.getValue() == ShipHullVoxelLayout.Part.LANTERN
                        || entry.getValue() == ShipHullVoxelLayout.Part.RAIL
                        || entry.getValue() == ShipHullVoxelLayout.Part.HELM
                        || entry.getValue() == ShipHullVoxelLayout.Part.STAIR)
                    test.assertTrue(!level.getBlockState(FirstShipBlockHull.world(
                            anchor, 0, c.x(), c.y() - 1, c.z())).isAir(),
                            "Floating fixture at " + c);
                if (entry.getValue() != ShipHullVoxelLayout.Part.STAIR) continue;
                stairCount++;
                test.assertTrue(plan.get(new ShipHullVoxelLayout.Cell(c.x() - 1, c.y(), c.z()))
                        == ShipHullVoxelLayout.Part.STAIR
                        || plan.get(new ShipHullVoxelLayout.Cell(c.x() + 1, c.y(), c.z()))
                        == ShipHullVoxelLayout.Part.STAIR,
                        "Upper staircase has an unpaired step at " + c);
                for (int head = 1; head <= 3; head++)
                    test.assertTrue(level.getBlockState(FirstShipBlockHull.world(
                            anchor, 0, c.x(), c.y() + head, c.z())).isAir(),
                            "Stair headroom blocked at " + c);
            }
            // Verify real hanging interior lighting and raised standing
            // lanterns on IV helm/roof posts. III keeps its original minimum.
            for (var light : plan.entrySet()) {
                if (light.getValue() != ShipHullVoxelLayout.Part.HANGING_LANTERN
                        && light.getValue() != ShipHullVoxelLayout.Part.LANTERN) continue;
                var cell = light.getKey();
                boolean standing = light.getValue() == ShipHullVoxelLayout.Part.LANTERN;
                test.assertTrue(!standing || type == ShipSectionPlan.ShipClass.LARGEST,
                        "Unexpected standing lantern on III " + cell);
                BlockPos position = FirstShipBlockHull.world(anchor, 0,
                        cell.x(), cell.y(), cell.z());
                test.assertTrue(level.getBlockState(position).is(Blocks.LANTERN)
                                && level.getBlockState(position).getValue(
                                        net.minecraft.world.level.block.LanternBlock.HANGING)
                                        != standing,
                        "Lantern state does not match support: " + cell);
                BlockPos support = standing ? position.below() : position.above();
                test.assertTrue(!level.getBlockState(support).isAir(),
                        "Lantern has no real support: " + cell);
                for (var other : plan.entrySet()) {
                    if (other.getKey().equals(cell)
                            || other.getValue() != ShipHullVoxelLayout.Part.HANGING_LANTERN
                            && other.getValue() != ShipHullVoxelLayout.Part.LANTERN) continue;
                    var second = other.getKey();
                    int dx = cell.x() - second.x();
                    int dy = cell.y() - second.y();
                    int dz = cell.z() - second.z();
                    test.assertTrue(dx * dx + dy * dy + dz * dz >=
                                    (type == ShipSectionPlan.ShipClass.LARGEST ? 9 : 25),
                            "Lanterns too close: " + cell + " / " + second);
                }
                if (!standing) hangingLights++;
            }
            lightChecks = hangingLights;
            test.assertTrue(hangingLights >= 6,
                    "Ship has insufficient spaced ceiling lighting");
            test.assertTrue(portCount > 8 && stairCount > 16 && lightChecks >= 6,
                    "Incomplete bounding-box scan");
            ShipMovingContraption moving = ShipMovingContraption.prepare(level, anchor, 0, config);
            test.assertTrue(moving != null && moving.detach(level, anchor),
                    "Intact hull did not assemble");
            double start = moving.pose().z();
            for (int step = 0; step < 18; step++)
                moving.tick(level, new ShipRigidMotion.Control(1D, 0D));
            test.assertTrue(moving.pose().z() > start + .01D,
                    "Intact moving hull did not travel");
            moving.motion().restore(new ShipRigidMotion.Pose(
                    anchor.getX() + .5D, anchor.getZ() + .5D, 0D), 0D, 0D);
            test.assertTrue(moving.park(level), "Intact hull did not park");
            if (type == ShipSectionPlan.ShipClass.LARGEST)
                ShipPersistenceGameTest.verifyIVDoor(
                        test, level, anchor, 0, config, "IV intact voyage/park");
            test.succeed();
        });
    }

}
