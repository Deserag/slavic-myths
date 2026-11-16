package org.slavicmyths.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipModularBlockHull;
import org.slavicmyths.ships.ShipMovingContraption;
import org.slavicmyths.ships.ShipSectionPlan;

@GameTestHolder("slavicmyths_ship_persist")
@PrefixGameTestTemplate(false)
public final class ShipPersistenceGameTest {
    @GameTest(template = "empty", timeoutTicks = 300, batch = "persist_iii")
    public static void brigantine(GameTestHelper test) {
        verify(test, ShipSectionPlan.ShipClass.LARGE);
    }

    @GameTest(template = "empty", timeoutTicks = 300, batch = "persist_iv")
    public static void frigate(GameTestHelper test) {
        verify(test, ShipSectionPlan.ShipClass.LARGEST);
    }

    private static void verify(GameTestHelper test, ShipSectionPlan.ShipClass type) {
        String stage = System.getProperty("shipPersistenceStage", "off");
        Path marker = Path.of("ship-persistence-" + type + ".txt");
        ShipBuildConfiguration config = ShipBuildConfiguration.initial(type);
        ServerLevel level = test.getLevel();
        try {
            if ("write".equals(stage)) {
                BlockPos anchor = test.absolutePos(new BlockPos(
                        type == ShipSectionPlan.ShipClass.LARGE ? 45 : 245, 4, 60));
                // The IV knyavdiged projects eighteen blocks beyond the
                // configured hull length, so its whole swept tip must be
                // unloaded of terrain before the persistence fixture builds.
                int extent = config.length() / 2
                        + (type == ShipSectionPlan.ShipClass.LARGEST ? 22 : 8);
                for (int x = -config.width() / 2 - 8; x <= config.width() / 2 + 8; x++)
                    for (int z = -extent; z <= extent; z++) {
                        BlockPos pos = anchor.offset(x, 0, z);
                        level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), 2);
                        level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                        for (int y = 1; y <= 48; y++)
                            level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 2);
                    }
                test.assertTrue(ShipModularBlockHull.place(level, anchor, 0,
                        config, new ListTag()), "Could not place persistent hull");
                BlockPos hole = FirstShipBlockHull.world(anchor, 0, 0, 1, 0);
                level.setBlock(hole, Blocks.AIR.defaultBlockState(), 2);
                test.assertTrue(ShipModularBlockHull.pack(level, anchor, 0, config) != null,
                        "One-block damage failed before save");
                Files.writeString(marker,
                        anchor.getX() + " " + anchor.getY() + " " + anchor.getZ());
                test.succeed();
            } else if ("read".equals(stage)) {
                String[] xyz = Files.readString(marker).trim().split(" ");
                BlockPos anchor = new BlockPos(Integer.parseInt(xyz[0]),
                        Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                BlockPos hole = FirstShipBlockHull.world(anchor, 0, 0, 1, 0);
                for (int x = -config.width() / 2 - 8; x <= config.width() / 2 + 15; x += 8)
                    for (int z = -config.length() / 2 - 8;
                            z <= config.length() / 2
                                    + (type == ShipSectionPlan.ShipClass.LARGEST ? 24 : 15);
                            z += 8)
                        level.getChunkAt(anchor.offset(x, 0, z));
                test.assertTrue(level.getBlockState(hole).isAir()
                        && !level.getBlockState(hole.north()).isAir(),
                        "Saved world did not retain exact hole and adjacent planking");
                ShipMovingContraption moving = ShipMovingContraption.prepare(
                        level, anchor, 0, config);
                String firstMismatch = "none";
                int mismatches = 0;
                for (var expected : ShipModularBlockHull.localPalette(config).entrySet()) {
                    BlockPos local = expected.getKey();
                    BlockPos pos = FirstShipBlockHull.world(anchor, 0,
                            local.getX(), local.getY(), local.getZ());
                    if (level.getBlockState(pos).getBlock() == expected.getValue().getBlock()
                            || pos.equals(hole)) continue;
                    mismatches++;
                    if (firstMismatch.equals("none")) firstMismatch = local + " expected="
                            + expected.getValue().getBlock() + " actual="
                            + level.getBlockState(pos).getBlock();
                }
                test.assertTrue(moving != null && moving.detach(level, anchor),
                        "Saved damaged hull cannot assemble mismatches=" + mismatches
                                + " first=" + firstMismatch);
                test.assertTrue(moving.park(level) && level.getBlockState(hole).isAir(),
                        "Saved damaged hull did not retain hole after park");
                test.succeed();
            } else test.fail("Specify shipPersistenceStage=write or read");
        } catch (Exception failure) {
            test.fail("Persistence stage " + stage + " failed: " + failure);
        }
    }
}
