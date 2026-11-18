package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Stage-one White City masterplan. All city coordinates derive from one saved
 * plaza origin. No palace, sanctuary, oak, Alatyr or house is constructed here.
 */
public final class BuyanCityPlan {
    private static final String SAVE = "slavicmyths_buyan_masterplan_2622_v2";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WHITE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState EDGE = Blocks.POLISHED_DIORITE.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState RED = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState BLUE = Blocks.BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState GOLD = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final int HALF = 22;
    private static final int SQUARE_SIZE = 45;
    private static final int MAIN_WIDTH = 9;

    private BuyanCityPlan() {}

    /** Inclusive world-space footprint with its intended facade direction. */
    public record Plot(String name, int minX, int maxX, int minZ, int maxZ,
                       int level, Direction facade) {
        public int width() { return maxX - minX + 1; }
        public int depth() { return maxZ - minZ + 1; }
        public BlockPos center() {
            return new BlockPos((minX + maxX) / 2, level, (minZ + maxZ) / 2);
        }
        public boolean overlaps(Plot other) {
            return minX <= other.maxX && maxX >= other.minX
                && minZ <= other.maxZ && maxZ >= other.minZ;
        }
    }

    /** Adjacent equal-height 35x35 cells may form one continuous 70x35 lot. */
    public static Plot merge(Plot first, Plot second, String name) {
        if (first.level != second.level || first.facade != second.facade)
            throw new IllegalArgumentException("Plots must share an elevation and facade");
        boolean alongX = first.minZ == second.minZ && first.maxZ == second.maxZ
            && (first.maxX + 1 == second.minX || second.maxX + 1 == first.minX);
        boolean alongZ = first.minX == second.minX && first.maxX == second.maxX
            && (first.maxZ + 1 == second.minZ || second.maxZ + 1 == first.minZ);
        if (!alongX && !alongZ) throw new IllegalArgumentException("Plots are not adjacent");
        return new Plot(name, Math.min(first.minX, second.minX),
            Math.max(first.maxX, second.maxX), Math.min(first.minZ, second.minZ),
            Math.max(first.maxZ, second.maxZ), first.level, first.facade);
    }

    private record Street(String name, int minX, int maxX, int minZ, int maxZ,
                          int width) {}

    public record Layout(int centerX, int centerY, int centerZ, int side,
                         List<Plot> plots, List<Street> streets) {}

    private static long key(BuyanState.Site site) {
        return new BlockPos(site.x(), 0, site.z()).asLong();
    }

    private static final class Record extends SavedData {
        final Map<Long, Layout> built = new LinkedHashMap<>();

        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), SAVE);
        }

        static Record load(CompoundTag tag, HolderLookup.Provider registries) {
            Record record = new Record();
            ListTag entries = tag.getList("Cities", Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                List<Plot> plots = new ArrayList<>();
                for (Tag raw : entry.getList("Plots", Tag.TAG_COMPOUND)) {
                    CompoundTag p = (CompoundTag) raw;
                    Direction facing = Direction.byName(p.getString("Facade"));
                    if (facing == null) facing = Direction.NORTH;
                    plots.add(new Plot(p.getString("Name"), p.getInt("MinX"),
                        p.getInt("MaxX"), p.getInt("MinZ"), p.getInt("MaxZ"),
                        p.getInt("Y"), facing));
                }
                List<Street> streets = new ArrayList<>();
                for (Tag raw : entry.getList("Streets", Tag.TAG_COMPOUND)) {
                    CompoundTag p = (CompoundTag) raw;
                    streets.add(new Street(p.getString("Name"), p.getInt("MinX"),
                        p.getInt("MaxX"), p.getInt("MinZ"), p.getInt("MaxZ"),
                        p.getInt("Width")));
                }
                record.built.put(entry.getLong("Site"), new Layout(
                    entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"),
                    entry.getInt("Side"), List.copyOf(plots), List.copyOf(streets)));
            }
            return record;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag cities = new ListTag();
            for (var e : built.entrySet()) {
                Layout layout = e.getValue();
                CompoundTag city = new CompoundTag();
                city.putLong("Site", e.getKey());
                city.putInt("X", layout.centerX);
                city.putInt("Y", layout.centerY);
                city.putInt("Z", layout.centerZ);
                city.putInt("Side", layout.side);
                ListTag plots = new ListTag();
                for (Plot plot : layout.plots) {
                    CompoundTag p = new CompoundTag();
                    p.putString("Name", plot.name);
                    p.putInt("MinX", plot.minX);
                    p.putInt("MaxX", plot.maxX);
                    p.putInt("MinZ", plot.minZ);
                    p.putInt("MaxZ", plot.maxZ);
                    p.putInt("Y", plot.level);
                    p.putString("Facade", plot.facade.getName());
                    plots.add(p);
                }
                city.put("Plots", plots);
                ListTag streets = new ListTag();
                for (Street street : layout.streets) {
                    CompoundTag p = new CompoundTag();
                    p.putString("Name", street.name);
                    p.putInt("MinX", street.minX);
                    p.putInt("MaxX", street.maxX);
                    p.putInt("MinZ", street.minZ);
                    p.putInt("MaxZ", street.maxZ);
                    p.putInt("Width", street.width);
                    streets.add(p);
                }
                city.put("Streets", streets);
                cities.add(city);
            }
            tag.put("Cities", cities);
            return tag;
        }

        void mark(BuyanState.Site site, Layout layout) {
            built.put(key(site), layout);
            setDirty();
        }
    }

    static boolean hasBuilt(ServerLevel world, BuyanState.Site site) {
        return Record.get(world).built.containsKey(key(site));
    }

    public static Layout savedLayout(ServerLevel world, BuyanState.Site site) {
        return Record.get(world).built.get(key(site));
    }

    static BlockPos spawn(ServerLevel world, BuyanState.Site site) {
        Layout layout = savedLayout(world, site);
        if (layout == null) throw new IllegalStateException("City masterplan not constructed");
        return new BlockPos(layout.centerX, layout.centerY + 2, layout.centerZ - 15);
    }

    private static int natural(ServerLevel world, BuyanState.Site site, int x, int z) {
        return BuyanNature.top(world.getSeed(), site.x(), site.z(), site.radius(),
            x, z, site.waterLevel());
    }

    private static int elevation(ServerLevel world, BuyanState.Site site,
                                 int x, int z) {
        int h = natural(world, site, x, z);
        if (h == Integer.MIN_VALUE) throw new IllegalStateException(
            "City masterplan intersects sea at " + x + "," + z);
        return h + 1;
    }

    private static Plot plot(ServerLevel world, BuyanState.Site site, String name,
                             int x0, int x1, int z0, int z1, Direction facade) {
        int left = Math.min(x0, x1), right = Math.max(x0, x1);
        int near = Math.min(z0, z1), far = Math.max(z0, z1);
        int[] samples = {
            elevation(world, site, left, near),
            elevation(world, site, left, far),
            elevation(world, site, right, near),
            elevation(world, site, right, far),
            elevation(world, site, (left + right) / 2, (near + far) / 2)
        };
        java.util.Arrays.sort(samples);
        return new Plot(name, left, right, near, far, samples[2], facade);
    }

    /** Layout selection uses both coasts. Do not take a dry square for granted. */
    private static double sideScore(ServerLevel world, BuyanState.Site site,
                                    int cx, int cz, int side) {
        double score = Double.POSITIVE_INFINITY;
        for (int x : new int[]{35, 100, 171}) {
            for (int z : new int[]{-45, 0, 41}) {
                int wx = cx + side * x, wz = cz + z;
                double margin = BuyanNature.shoreDistance(
                    world.getSeed(), site.x(), site.z(), wx, wz);
                if (margin < 5.0) return Double.NEGATIVE_INFINITY;
                score = Math.min(score, margin);
            }
        }
        for (int x : new int[]{-96, -62, -32}) {
            for (int z : new int[]{-41, 0, 41}) {
                int wx = cx + side * x, wz = cz + z;
                double margin = BuyanNature.shoreDistance(
                    world.getSeed(), site.x(), site.z(), wx, wz);
                if (margin < 5.0) return Double.NEGATIVE_INFINITY;
                score = Math.min(score, margin);
            }
        }
        return score;
    }

    static Layout calculate(ServerLevel world, BuyanState.Site site) {
        int cx = site.x(), cz = site.z() - 54;
        int cy = elevation(world, site, cx, cz);
        int side = sideScore(world, site, cx, cz, 1) >=
            sideScore(world, site, cx, cz, -1) ? 1 : -1;
        if (!Double.isFinite(sideScore(world, site, cx, cz, side)))
            throw new IllegalStateException("No safe side for the sanctuary reserve");
        List<Plot> lots = new ArrayList<>();
        // The approved palace's real volume is 67x41; the lot reserves
        // its towers, front stairs, approach and surrounding clearance.
        lots.add(plot(world, site, "TEREM", cx - 40, cx + 40,
            cz + 43, cz + 103, Direction.NORTH));
        // 129x81 is the sanctuary's enclosed complex. Reserve exterior
        // clearance for the crown, roots, chains and ritual court as well.
        lots.add(plot(world, site, "SANCTUARY_AND_OAK", cx + side * 35,
            cx + side * 171, cz - 45, cz + 41,
            side == 1 ? Direction.WEST : Direction.EAST));
        for (int[] z : new int[][]{{-41, -7}, {7, 41}}) {
            lots.add(plot(world, site, "B_35_" + z[0],
                cx - side * 96, cx - side * 62, cz + z[0], cz + z[1],
                Direction.valueOf(side == 1 ? "EAST" : "WEST")));
        }
        for (int[] z : new int[][]{{-41, -17}, {17, 41}}) {
            lots.add(plot(world, site, "A_25_" + z[0],
                cx - side * 56, cx - side * 32, cz + z[0], cz + z[1],
                Direction.valueOf(side == 1 ? "EAST" : "WEST")));
        }
        List<Street> streets = new ArrayList<>();
        streets.add(new Street("MAIN_SOUTH", cx - 4, cx + 4,
            cz - 90, cz - 23, MAIN_WIDTH));
        streets.add(new Street("MAIN_NORTH", cx - 4, cx + 4,
            cz + 23, cz + 42, MAIN_WIDTH));
        streets.add(new Street("SANCTUARY_APPROACH",
            Math.min(cx + side * 23, cx + side * 34),
            Math.max(cx + side * 23, cx + side * 34), cz - 2, cz + 2, 5));
        streets.add(new Street("WEST_CROSS_STREET",
            Math.min(cx - side * 97, cx - side * 23),
            Math.max(cx - side * 97, cx - side * 23), cz - 2, cz + 2, 5));
        for (int[] span : new int[][]{{-61, -59}, {-29, -27}}) {
            streets.add(new Street("RESIDENTIAL_ACCESS_" + span[0],
                Math.min(cx + side * span[0], cx + side * span[1]),
                Math.max(cx + side * span[0], cx + side * span[1]),
                cz - 42, cz + 42, 3));
        }
        return new Layout(cx, cy, cz, side, List.copyOf(lots), List.copyOf(streets));
    }

    private record Tile(BlockState block, int y, int clearance) {}

    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final Layout layout;
        final Map<Long, Tile> columns = new LinkedHashMap<>();
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        Plan(ServerLevel world, BuyanState.Site site, Layout layout) {
            this.world = world;
            this.site = site;
            this.layout = layout;
        }

        void put(int x, int z, int y, BlockState block, int clear) {
            columns.put(new BlockPos(x, 0, z).asLong(), new Tile(block, y, clear));
        }

        void plots() {
            for (Plot plot : layout.plots) {
                for (int x = plot.minX; x <= plot.maxX; x++)
                    for (int z = plot.minZ; z <= plot.maxZ; z++) {
                        boolean edge = x == plot.minX || x == plot.maxX
                            || z == plot.minZ || z == plot.maxZ;
                        boolean corner = (x == plot.minX || x == plot.maxX)
                            && (z == plot.minZ || z == plot.maxZ);
                        put(x, z, plot.level,
                            corner ? GOLD : edge ? STONE : Blocks.GRASS_BLOCK.defaultBlockState(),
                            16);
                    }
            }
        }

        void roads() {
            for (Street street : layout.streets) {
                for (int x = street.minX; x <= street.maxX; x++)
                    for (int z = street.minZ; z <= street.maxZ; z++) {
                        boolean main = street.width == MAIN_WIDTH;
                        boolean edge = main
                            ? (x == street.minX || x == street.maxX)
                            : (street.maxX - street.minX > street.maxZ - street.minZ
                                ? z == street.minZ || z == street.maxZ
                                : x == street.minX || x == street.maxX);
                        int nat = elevation(world, site, x, z);
                        int distance = Math.max(Math.abs(x - layout.centerX),
                            Math.abs(z - layout.centerZ));
                        int allowed = Math.max(0, distance - HALF);
                        int y = Math.clamp(nat, layout.centerY - allowed, layout.centerY + allowed);
                        put(x, z, y, edge ? EDGE : main
                            && x == layout.centerX ? BLUE : WHITE, 9);
                    }
            }
        }

        void plaza() {
            int cx = layout.centerX, cz = layout.centerZ;
            for (int dx = -HALF; dx <= HALF; dx++)
                for (int dz = -HALF; dz <= HALF; dz++) {
                    int ax = Math.abs(dx), az = Math.abs(dz);
                    int ring = Math.max(ax, az);
                    BlockState tile = WHITE;
                    if (ring == HALF || ring == HALF - 1) tile = EDGE;
                    else if (ring == 17 || (ax == az && ring >= 11 && ring <= 19))
                        tile = RED;
                    else if (ring == 12 || ring == 8) tile = BLUE;
                    else if ((ax == 15 && dz == 0) || (az == 15 && dx == 0))
                        tile = GOLD;
                    // The center remains only a marked and supported dais;
                    // no Alatyr geometry is built in this stage.
                    if (ring <= 6) tile = EDGE;
                    if (dx == 0 && dz == 0) tile = GOLD;
                    put(cx + dx, cz + dz, layout.centerY, tile, 14);
                }
        }

        void foundation() {
            for (var entry : columns.entrySet()) {
                BlockPos horizontal = BlockPos.of(entry.getKey());
                int x = horizontal.getX(), z = horizontal.getZ();
                Tile tile = entry.getValue();
                int ground = natural(world, site, x, z);
                if (ground == Integer.MIN_VALUE)
                    throw new IllegalStateException("City plan touches sea " + x + "," + z);
                if (tile.y < site.waterLevel() + 4)
                    throw new IllegalStateException("City grade too low " + x + "," + z);
                if (Math.abs(tile.y - ground) > 52)
                    throw new IllegalStateException("Excessive earthwork " + x + "," + z);
                int bottom = Math.min(ground - 2, tile.y - 3);
                for (int y = bottom; y < tile.y; y++)
                    blocks.put(new BlockPos(x, y, z), STONE);
                blocks.put(new BlockPos(x, tile.y, z), tile.block);
                for (int y = tile.y + 1; y <= Math.max(tile.y + tile.clearance,
                    ground + tile.clearance); y++)
                    blocks.put(new BlockPos(x, y, z), AIR);
            }
        }

        void lamps() {
            for (int dz : new int[]{-84, -27, 31}) {
                for (int dx : new int[]{-6, 6}) {
                    int x = layout.centerX + dx, z = layout.centerZ + dz;
                    int y = natural(world, site, x, z) + 1;
                    if (y <= site.waterLevel() + 3) continue;
                    blocks.put(new BlockPos(x, y, z), STONE);
                    for (int h = 1; h <= 3; h++)
                        blocks.put(new BlockPos(x, y + h, z),
                            Blocks.QUARTZ_PILLAR.defaultBlockState());
                    blocks.put(new BlockPos(x, y + 4, z),
                        Blocks.LANTERN.defaultBlockState());
                }
            }
        }

        void build() {
            plots();
            roads();
            plaza();
            foundation();
            lamps();
        }

        boolean geometryCheck() {
            if (layout.plots.size() != 6 || layout.streets.size() != 6
                || columns.size() < SQUARE_SIZE * SQUARE_SIZE)
                return false;
            for (int dx = -HALF; dx <= HALF; dx++)
                for (int dz = -HALF; dz <= HALF; dz++)
                    if (!columns.containsKey(new BlockPos(layout.centerX + dx,
                        0, layout.centerZ + dz).asLong())) return false;
            if (!blocks.getOrDefault(
                new BlockPos(layout.centerX, layout.centerY, layout.centerZ), AIR).is(
                    Blocks.YELLOW_TERRACOTTA)) return false;
            for (int i = 0; i < layout.plots.size(); i++) {
                Plot p = layout.plots.get(i);
                if (p.overlaps(new Plot("square", layout.centerX - HALF,
                    layout.centerX + HALF, layout.centerZ - HALF,
                    layout.centerZ + HALF, layout.centerY, Direction.NORTH)))
                    return false;
                for (int j = i + 1; j < layout.plots.size(); j++)
                    if (p.overlaps(layout.plots.get(j))) return false;
            }
            if (layout.plots.stream().filter(p -> p.width() == 25 && p.depth() == 25)
                    .count() != 2
                || layout.plots.stream().filter(p -> p.width() == 35 && p.depth() == 35)
                    .count() != 2) return false;
            for (int z = layout.centerZ - 90; z <= layout.centerZ + 42; z++)
                if (z >= layout.centerZ - HALF && z <= layout.centerZ + HALF
                    || columns.containsKey(new BlockPos(layout.centerX, 0, z).asLong()))
                    continue;
                else return false;
            return true;
        }

        boolean preflight() {
            Set<Long> loaded = new HashSet<>();
            for (var entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                if (p.getY() <= world.getMinBuildHeight()
                    || p.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(p)) return false;
                long chunk = ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4);
                if (loaded.add(chunk)) world.getChunk(p.getX() >> 4, p.getZ() >> 4);
                BlockState original = world.getBlockState(p);
                if (!original.equals(entry.getValue())
                    && !BuyanGenerator.canReplace(original)) {
                    LogUtils.getLogger().warn("City masterplan protected block {}: {}", p, original);
                    return false;
                }
            }
            return true;
        }

        void commit() {
            Map<BlockPos, BlockState> originals = new LinkedHashMap<>();
            try {
                for (var entry : blocks.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockState previous = world.getBlockState(pos);
                    if (previous.equals(entry.getValue())) continue;
                    originals.put(pos, previous);
                    if (!world.setBlock(pos, entry.getValue(), 2)
                        && !world.getBlockState(pos).equals(entry.getValue()))
                        throw new IllegalStateException("City block cannot be placed at " + pos);
                }
                for (var entry : columns.entrySet()) {
                    BlockPos at = BlockPos.of(entry.getKey());
                    Tile tile = entry.getValue();
                    if (!world.getBlockState(new BlockPos(at.getX(), tile.y, at.getZ()))
                        .equals(tile.block))
                        throw new IllegalStateException("City floor changed after placement " + at);
                }
            } catch (RuntimeException error) {
                List<Map.Entry<BlockPos, BlockState>> undo =
                    new ArrayList<>(originals.entrySet());
                for (int i = undo.size() - 1; i >= 0; i--)
                    world.setBlock(undo.get(i).getKey(), undo.get(i).getValue(), 2);
                throw error;
            }
        }
    }

    static boolean placeOnce(ServerLevel world, BuyanState.Site site) {
        if (hasBuilt(world, site)) return true;
        if (BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site)) return false;
        Layout layout = calculate(world, site);
        Plan plan = new Plan(world, site, layout);
        plan.build();
        if (!plan.geometryCheck() || !plan.preflight()) return false;
        plan.commit();
        Record.get(world).mark(site, layout);
        LogUtils.getLogger().info(
            "Buyan White City masterplan: plaza {},{},{} (45x45), side {}, {} plots",
            layout.centerX, layout.centerY, layout.centerZ, layout.side,
            layout.plots.size());
        return true;
    }

    /** Non-mutating checks used from GameTests. */
    public static void verifyBlueprintFixture(ServerLevel world) {
        BuyanState.Site site = new BuyanState.Site(
            0, 0, BuyanTerrain.MAX_RADIUS, 63);
        Layout layout = calculate(world, site);
        Plan plan = new Plan(world, site, layout);
        plan.build();
        if (!plan.geometryCheck())
            throw new IllegalStateException("White City masterplan fixture invalid");
        Plot left = new Plot("A", 0, 34, 0, 34, 74, Direction.NORTH);
        Plot right = new Plot("B", 35, 69, 0, 34, 74, Direction.NORTH);
        Plot combined = merge(left, right, "70x35");
        if (combined.width() != 70 || combined.depth() != 35)
            throw new IllegalStateException("Merged plot geometry invalid");
    }
}
