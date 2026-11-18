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

    /**
     * Simulate a save written before beds and supported lanterns existed.
     * The live retrofit must restore these without rebuilding any houses.
     */
    private static void checkExistingHouseUpgrade(GameTestHelper helper,
                                                   BuyanState.Site island,
                                                   BuyanCityPlan.Layout city) {
        int cx = city.centerX(), cz = city.centerZ(), y = city.centerY();
        BlockPos lamp = new BlockPos(cx - 99, y + 3, cz + 29);
        BlockPos bed = new BlockPos(cx - 93, y + 1, cz + 32);
        helper.getLevel().setBlock(lamp, Blocks.LANTERN.defaultBlockState(), 2);
        for (int h = 1; h <= 4; h++)
            helper.getLevel().setBlock(lamp.above(h), Blocks.AIR.defaultBlockState(), 2);
        helper.getLevel().setBlock(bed, Blocks.AIR.defaultBlockState(), 2);
        helper.getLevel().setBlock(bed.south(), Blocks.AIR.defaultBlockState(), 2);
        require(BuyanDevelopment.verifyLegacyFurnishingUpgrade(helper.getLevel(), island),
            "legacy furnished house migration failed");
        require(helper.getLevel().getBlockState(lamp.above()).is(Blocks.CHAIN)
            && helper.getLevel().getBlockState(lamp).is(Blocks.LANTERN),
            "legacy lantern was not supported after migration");
        require(helper.getLevel().getBlockState(bed).getBlock()
            instanceof net.minecraft.world.level.block.BedBlock,
            "legacy house bed was not restored");
    }

    private static void checkTownBuildings(GameTestHelper helper, BuyanCityPlan.Layout city) {
        int cx = city.centerX(), cz = city.centerZ(), y = city.centerY();
        int homes = 0, markets = 0;
        for (int row = 0; row < 3; row++) {
            int z = new int[]{27, 50, 79}[row];
            int depth = new int[]{13, 19, 14}[row];
            for (int col = 0; col < 8; col++) {
                int x = col < 4
                    ? new int[]{-101, -82, -58, -39}[col]
                    : new int[]{28, 47, 71, 90}[col - 4];
                boolean residential = col < 4;
                String kind = residential ? "house" : row == 0 ? "shop"
                    : row == 1 ? (col == 5 ? "inn" : "shop")
                        : (col - 4) % 2 == 0 ? "workshop" : "house";
                int frontZ = row == 0 ? z + depth - 1 : z;
                BlockPos door = new BlockPos(cx + x + 6, y + 1, cz + frontZ);
                BlockState lower = helper.getLevel().getBlockState(door);
                BlockState upper = helper.getLevel().getBlockState(door.above());
                require(lower.is(Blocks.SPRUCE_DOOR)
                    && lower.getValue(net.minecraft.world.level.block.DoorBlock.HALF)
                        == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER
                    && upper.is(Blocks.SPRUCE_DOOR)
                    && upper.getValue(net.minecraft.world.level.block.DoorBlock.HALF)
                        == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER,
                    "town door missing or wrong half at " + door);
                BlockPos lantern = new BlockPos(cx + x + 2, y + 3, cz + z + 2);
                require(helper.getLevel().getBlockState(lantern).is(Blocks.LANTERN)
                    && helper.getLevel().getBlockState(lantern.above()).is(Blocks.CHAIN)
                    && helper.getLevel().getBlockState(lantern.above(2))
                        .is(Blocks.STRIPPED_SPRUCE_LOG),
                    "hanging lantern has no real roof support at " + lantern);
                int roofX = x + 6;
                int roofY = y + 5 + Math.min(roofX - (x - 1), x + 12 - roofX);
                BlockPos roof = new BlockPos(cx + roofX, roofY, cz + z);
                require(!helper.getLevel().getBlockState(roof).isAir(),
                    "town roof missing at " + roof);
                if (kind.equals("house") || kind.equals("inn")) {
                    int bedX = kind.equals("inn") ? x + 3 : x + 8;
                    BlockPos foot = new BlockPos(cx + bedX, y + 1, cz + z + 5);
                    BlockPos head = foot.south();
                    BlockState first = helper.getLevel().getBlockState(foot);
                    BlockState second = helper.getLevel().getBlockState(head);
                    require(first.getBlock() instanceof net.minecraft.world.level.block.BedBlock
                        && second.getBlock() == first.getBlock()
                        && first.getValue(net.minecraft.world.level.block.BedBlock.PART)
                            == net.minecraft.world.level.block.state.properties.BedPart.FOOT
                        && second.getValue(net.minecraft.world.level.block.BedBlock.PART)
                            == net.minecraft.world.level.block.state.properties.BedPart.HEAD,
                        "residential bed missing or invalid at " + foot);
                    homes++;
                } else markets++;
            }
        }
        require(homes == 15 && markets == 9,
            "expected 15 furnished homes/inns and 9 shops/workshops");
    }

    private static void checkAllCityRoadSurfaces(GameTestHelper helper,
                                                BuyanCityPlan.Layout city) {
        // After all structures: verify every part of all four main axes and
        // eight narrow streets, not only the center lines.
        int level = city.centerY();
        for (BuyanCityPlan.Street street : city.streets()) {
            for (int x = street.minX(); x <= street.maxX(); x++)
                for (int z = street.minZ(); z <= street.maxZ(); z++) {
                    // Monument approaches may add paving beside a road, but
                    // cannot destroy the continuous original city surface.
                    BlockPos point = new BlockPos(x, level, z);
                    require(road(helper.getLevel().getBlockState(point)),
                        "city road overwritten at " + point + " (" + street.name() + ")");
                    for (int h = 1; h <= 3; h++)
                        require(helper.getLevel().getBlockState(point.above(h)).isAir(),
                            "road headroom blocked at " + point.above(h)
                                + " (" + street.name() + ")");
                }
        }
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

    private static int pavedHeight(GameTestHelper helper, int x, int z, int referenceY) {
        for (int y = referenceY + 62; y >= referenceY - 62; y--) {
            BlockPos at = new BlockPos(x, y, z);
            if (road(helper.getLevel().getBlockState(at))
                && helper.getLevel().getBlockState(at.above()).isAir()
                && !helper.getLevel().getBlockState(at.below()).isAir())
                return y;
        }
        throw new IllegalStateException("Missing backed road surface at " + x + "," + z);
    }

    private static void checkStreetGrades(GameTestHelper helper, BuyanCityPlan.Layout city) {
        for (BuyanCityPlan.Street street : city.streets()) {
            boolean alongX = street.maxX() - street.minX()
                >= street.maxZ() - street.minZ();
            int last = Integer.MIN_VALUE;
            int length = alongX ? street.maxX() - street.minX()
                : street.maxZ() - street.minZ();
            for (int n = 0; n <= length; n++) {
                int x = alongX ? street.minX() + n
                    : (street.minX() + street.maxX()) / 2;
                int z = alongX ? (street.minZ() + street.maxZ()) / 2
                    : street.minZ() + n;
                int actual = pavedHeight(helper, x, z, city.centerY());
                if (last != Integer.MIN_VALUE)
                    require(Math.abs(actual - last) <= 1,
                        "steep street " + street.name() + " at " + x + "," + z
                            + " jumps from Y=" + last + " to Y=" + actual);
                last = actual;
            }
        }
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
        require(BuyanShrine.hasBuilt(helper.getLevel(), island),
            "new north-west sanctuary was not generated");
        require(BuyanDevelopment.placeOak(helper.getLevel(), island),
            "shared-precinct sacred oak was not generated");
        require(BuyanDevelopment.placeSacredPrecinct(helper.getLevel(), island),
            "ritual court and walkways were not generated");
        require(BuyanDevelopment.placeHouses(helper.getLevel(), island),
            "future residential reserves were not prepared");
        require(BuyanHarbor.placeOnce(helper.getLevel(), island),
            "harbor was not generated");
        require(BuyanCraftQuarter.placeOnce(helper.getLevel(), island),
            "forge and armory were not generated");
        require(BuyanOutpost.placeOnce(helper.getLevel(), island),
            "eleven-building military outpost was not generated");
        BuyanCityPlan.Layout layout = BuyanCityPlan.savedLayout(helper.getLevel(), island);
        require(layout != null, "persisted layout missing");
        require(layout.plots().size() == 5, "expected five reserved districts");
        require(layout.centerX() == 0 && layout.centerZ() == 0,
            "city center must match the island center");
        require(layout.streets().size() == 12, "four main and eight secondary roads expected");
        for (BuyanCityPlan.Plot lot : layout.plots()) {
            if ("TEREM".equals(lot.name())) {
                require(lot.facade() == net.minecraft.core.Direction.NORTH,
                    "terem facade does not face the plaza");
                checkEntrance(helper, lot);
            }
        }
        for (String district : new String[]{"SANCTUARY_AND_OAK", "CIVIC_RESERVE",
            "RESIDENTIAL", "RESIDENTIAL_MARKET", "TEREM"}) {
            require(layout.plots().stream().anyMatch(p -> district.equals(p.name())),
                "missing reserve " + district);
        }
        for (int i = 0; i < layout.plots().size(); i++)
            for (int j = i + 1; j < layout.plots().size(); j++)
                require(!layout.plots().get(i).overlaps(layout.plots().get(j)),
                    "building plots overlap");

        checkStreetGrades(helper, layout);
        checkAllCityRoadSurfaces(helper, layout);
        BuyanCityPlan.Street southern = layout.streets().stream()
            .filter(s -> s.name().equals("MAIN_SOUTH")).findFirst().orElseThrow();
        require(southern.minZ() == layout.centerZ() + 23
            && southern.maxZ() == layout.centerZ() + 50,
            "main road to terem was not shortened to 28 blocks");
        int cityY = layout.centerY();
        require(helper.getLevel().getBlockState(new BlockPos(
            layout.centerX() - 43, cityY + 12, layout.centerZ() - 78))
                .is(Blocks.OAK_WOOD),
            "sacred oak is not next to the sanctuary");
        require(helper.getLevel().getBlockState(new BlockPos(
            layout.centerX() - 62, cityY + 2, layout.centerZ() - 65))
                .is(Blocks.SOUL_CAMPFIRE),
            "ritual court centerpiece missing");
        require(helper.getLevel().getBlockState(new BlockPos(
            layout.centerX() - 10, cityY, layout.centerZ() - 53))
                .is(Blocks.POLISHED_ANDESITE),
            "sanctuary processional walkway missing");
        require(!helper.getLevel().getBlockState(new BlockPos(
            layout.centerX() - 95, cityY + 2, layout.centerZ() + 32))
                .is(Blocks.SPRUCE_PLANKS),
            "old compact houses were generated despite deferred architecture");
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
