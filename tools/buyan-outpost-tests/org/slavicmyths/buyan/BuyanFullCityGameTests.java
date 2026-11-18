package org.slavicmyths.buyan;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Execute the real masterplan on disposable new terrain; buildings are phase two. */
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class BuyanFullCityGameTests {
    private BuyanFullCityGameTests() {}

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("White City stage one: " + message);
    }

    private static boolean road(BlockState state) {
        return state.is(Blocks.SMOOTH_QUARTZ)
            || state.is(Blocks.POLISHED_DIORITE)
            || state.is(Blocks.BLUE_TERRACOTTA);
    }

    private static void checkEntrance(GameTestHelper helper, BuyanCityPlan.Plot plot) {
        int centerX = (plot.minX() + plot.maxX()) / 2;
        int centerZ = (plot.minZ() + plot.maxZ()) / 2;
        int success = 0;
        for (int offset = -2; offset <= 2; offset++) {
            int x = switch (plot.facade()) {
                case EAST -> plot.maxX() + 1;
                case WEST -> plot.minX() - 1;
                default -> centerX + offset;
            };
            int z = switch (plot.facade()) {
                case SOUTH -> plot.maxZ() + 1;
                case NORTH -> plot.minZ() - 1;
                default -> centerZ + offset;
            };
            int bestDelta = Integer.MAX_VALUE;
            for (int y = plot.level() - 25; y <= plot.level() + 25; y++) {
                BlockPos at = new BlockPos(x, y, z);
                if (road(helper.getLevel().getBlockState(at))
                    && helper.getLevel().getBlockState(at.above()).isAir()) {
                    bestDelta = Math.min(bestDelta, Math.abs(y - plot.level()));
                }
            }
            if (bestDelta <= 1) success++;
        }
        require(success >= 3, "plot " + plot.name()
            + " has no 3-block traversable entry onto its reserved road");
    }

    @GameTest(template = "empty", timeoutTicks = 3000)
    public static void fullBuyanCityBuildsInSingleWorld(GameTestHelper helper) {
        BuyanPreSpawn.constructOrResume(helper.getLevel());
        BuyanPreSpawn.constructOrResume(helper.getLevel());
        BuyanState.Site island = new BuyanState.Site(0, 0, BuyanTerrain.MAX_RADIUS, 63);
        require(BuyanNatureState.get(helper.getLevel()).finishedAt(island),
            "nature was not finished");
        require(BuyanCityPlan.hasBuilt(helper.getLevel(), island),
            "masterplan missing");
        require(!BuyanShrine.hasBuilt(helper.getLevel(), island),
            "sanctuary was built before owner approval");
        BuyanCityPlan.Layout layout = BuyanCityPlan.savedLayout(helper.getLevel(), island);
        require(layout != null, "persisted layout missing");
        require(layout.plots().size() == 6, "expected six reserved plots");
        for (BuyanCityPlan.Plot lot : layout.plots()) {
            if ("TEREM".equals(lot.name()))
                require(lot.facade() == net.minecraft.core.Direction.NORTH,
                    "terem facade does not face the plaza");
            checkEntrance(helper, lot);
        }
        long small = layout.plots().stream()
            .filter(p -> p.width() == 25 && p.depth() == 25).count();
        long large = layout.plots().stream()
            .filter(p -> p.width() == 35 && p.depth() == 35).count();
        require(small == 2 && large == 2, "25x25 and 35x35 cells missing");
        for (int i = 0; i < layout.plots().size(); i++)
            for (int j = i + 1; j < layout.plots().size(); j++)
                require(!layout.plots().get(i).overlaps(layout.plots().get(j)),
                    "building plots overlap");

        int cx = layout.centerX(), cy = layout.centerY(), cz = layout.centerZ();
        for (int dx = -22; dx <= 22; dx++)
            for (int dz = -22; dz <= 22; dz++) {
                BlockPos floor = new BlockPos(cx + dx, cy, cz + dz);
                BlockState tile = helper.getLevel().getBlockState(floor);
                require(tile.is(Blocks.SMOOTH_QUARTZ)
                    || tile.is(Blocks.POLISHED_DIORITE)
                    || tile.is(Blocks.RED_TERRACOTTA)
                    || tile.is(Blocks.BLUE_TERRACOTTA)
                    || tile.is(Blocks.YELLOW_TERRACOTTA),
                    "unprepared square floor at " + floor);
                for (int down = 1; down <= 3; down++)
                    require(!helper.getLevel().getBlockState(floor.below(down)).isAir(),
                        "hollow plaza support at " + floor.below(down));
            }
        require(helper.getLevel().getBlockState(new BlockPos(cx, cy, cz))
            .is(Blocks.YELLOW_TERRACOTTA), "Alatyr reserved marker missing");
        require(helper.getLevel().getBlockState(new BlockPos(cx, cy + 1, cz)).isAir(),
            "Alatyr already placed");
        for (int z : new int[]{cz - 35, cz + 30}) {
            for (int x = cx - 4; x <= cx + 4; x++) {
                boolean found = false;
                for (int y = cy - 35; y <= cy + 35; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState tile = helper.getLevel().getBlockState(p);
                    if (tile.is(Blocks.SMOOTH_QUARTZ)
                        || tile.is(Blocks.POLISHED_DIORITE)
                        || tile.is(Blocks.BLUE_TERRACOTTA)) {
                        require(!helper.getLevel().getBlockState(p.below()).isAir(),
                            "unsupported nine-block avenue");
                        for (int h = 1; h <= 3; h++)
                            require(helper.getLevel().getBlockState(p.above(h)).isAir(),
                                "street headroom blocked at " + p.above(h));
                        found = true;
                        break;
                    }
                }
                require(found, "nine-block avenue missing at x=" + x + " z=" + z);
            }
        }
        helper.succeed();
    }
}
