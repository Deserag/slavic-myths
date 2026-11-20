package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.saveddata.SavedData;

/** Variant 4: large white three-storey L-shaped communal houses, not village huts. */
final class BuyanCityHousing {
    private static final String SAVE = "slavicmyths_buyan_housing_variant4_2622";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WALL = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState TRIM = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState BEAM = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
    private static final BlockState RED = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState GOLD = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState ROOF = Blocks.RED_NETHER_BRICKS.defaultBlockState();
    private static final BlockState DARK = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState FLOOR = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState GLASS = Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState();

    private BuyanCityHousing() {}

    private static final class Record extends SavedData {
        final Set<Long> finished = new HashSet<>();
        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), SAVE);
        }
        static Record load(CompoundTag tag, HolderLookup.Provider registries) {
            Record r = new Record();
            for (long id : tag.getLongArray("Sites")) r.finished.add(id);
            return r;
        }
        @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putLongArray("Sites", finished.stream().mapToLong(Long::longValue).toArray());
            return tag;
        }
    }

    record Building(int x, int z, int width, int depth, boolean facesSouth,
                    boolean mirror, boolean commerce) {
        int minX() { return x - 1; }
        int maxX() { return x + width; }
        int minZ() { return z - 1; }
        int maxZ() { return z + depth; }
        int frontZ() { return facesSouth ? z + depth - 1 : z; }
    }

    /** Eight full-height townhouses; landscaped shallow southern strip retained. */
    static List<Building> buildings() {
        List<Building> result = new ArrayList<>();
        for (int row = 0; row < 2; row++) {
            int z = row == 0 ? 27 : 53;
            int depth = row == 0 ? 18 : 23;
            result.add(new Building(-101, z, 22, depth, row == 0, row == 1, false));
            result.add(new Building(-69, z, 27, depth, row == 0, row == 0, false));
            result.add(new Building(43, z, 27, depth, row == 0, row == 0, row == 0));
            result.add(new Building(80, z, 22, depth, row == 0, row == 1, row == 1));
        }
        return List.copyOf(result);
    }

    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final int level;
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        Plan(ServerLevel world, BuyanState.Site site, int level) {
            this.world = world;
            this.site = site;
            this.level = level;
        }
        private int wx(Building b, int x) { return b.x + (b.mirror ? b.width - 1 - x : x); }
        private int wz(Building b, int z) { return b.z + (b.facesSouth ? b.depth - 1 - z : z); }
        private Direction north(Building b) { return b.facesSouth ? Direction.SOUTH : Direction.NORTH; }
        private Direction south(Building b) { return north(b).getOpposite(); }
        private Direction east(Building b) { return b.mirror ? Direction.WEST : Direction.EAST; }
        void at(Building b, int x, int z, int h, BlockState block) {
            blocks.put(new BlockPos(site.x() + wx(b, x), level + h, site.z() + wz(b, z)), block);
        }
        void fill(Building b, int x0, int x1, int z0, int z1,
                  int h0, int h1, BlockState block) {
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++)
                    for (int h = h0; h <= h1; h++) at(b, x, z, h, block);
        }
        boolean inside(Building b, int x, int z) {
            return x >= 0 && x < b.width && z >= 0 && z < b.depth
                && (z <= 13 || x <= 11);
        }
        boolean edge(Building b, int x, int z) {
            return inside(b, x, z) && (!inside(b, x - 1, z) || !inside(b, x + 1, z)
                || !inside(b, x, z - 1) || !inside(b, x, z + 1));
        }
        void door(Building b, int x, int z, int floor, Direction facing,
                  DoorHingeSide hinge) {
            BlockState bottom = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, hinge)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            at(b, x, z, floor + 1, bottom);
            at(b, x, z, floor + 2, bottom.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
        void bed(Building b, int x, int z, int floor, boolean blue) {
            BlockState state = (blue ? Blocks.BLUE_BED : Blocks.RED_BED).defaultBlockState()
                .setValue(BedBlock.FACING, south(b)).setValue(BedBlock.PART, BedPart.FOOT);
            at(b, x, z, floor + 1, state);
            at(b, x, z + 1, floor + 1, state.setValue(BedBlock.PART, BedPart.HEAD));
        }
        void light(Building b, int x, int z, int floor) {
            at(b, x, z, floor + 6, Blocks.CHAIN.defaultBlockState());
            at(b, x, z, floor + 5, Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true));
        }

        void build(Building b) {
            // Actual L footprint: 14-deep frontage and 12-wide rear side-wing.
            for (int x = 0; x < b.width; x++)
                for (int z = 0; z < b.depth; z++) if (inside(b, x, z)) {
                    for (int h = 1; h <= 34; h++) at(b, x, z, h, AIR);
                    for (int floor : new int[]{0, 7, 14}) {
                        at(b, x, z, floor, floor == 0 ? STONE : FLOOR);
                        if (edge(b, x, z))
                            for (int h = floor + 1; h <= floor + 6; h++)
                                at(b, x, z, h, h == floor + 6 ? RED
                                    : ((x % 6 == 0 || z % 6 == 0) ? TRIM : WALL));
                    }
                }
            // Three liveable levels, separated by solid continuous floor decks.
            for (int floor : new int[]{0, 7, 14}) {
                for (int x = 1; x < b.width - 1; x++)
                    for (int z : new int[]{5, 9})
                        fill(b, x, x, z, z, floor + 1, floor + 5,
                            Blocks.BIRCH_PLANKS.defaultBlockState());
                for (int x : new int[]{8, b.width - 11})
                    for (int z : new int[]{1, 10}) {
                        // No partition immediately behind either leaf of the
                        // paired entrance in the narrower mirrored houses.
                        if (z == 1 && (x == b.width / 2 - 1 || x == b.width / 2))
                            continue;
                        fill(b, x, x, z, z + 3, floor + 1, floor + 5,
                            Blocks.BIRCH_PLANKS.defaultBlockState());
                    }
                for (int x : new int[]{4, 11}) {
                    door(b, x, 5, floor, north(b), DoorHingeSide.LEFT);
                    door(b, x, 9, floor, south(b), DoorHingeSide.RIGHT);
                }
                // Corridor access to the L-wing and the stair-service bay.
                door(b, 6, 9, floor, south(b), DoorHingeSide.LEFT);
                door(b, b.width - 2, 5, floor, north(b), DoorHingeSide.RIGHT);
                door(b, b.width - 2, 9, floor, south(b), DoorHingeSide.LEFT);
                for (int z = 14; z < b.depth - 1; z++)
                    for (int x : new int[]{4, 8})
                        fill(b, x, x, z, z, floor + 1, floor + 5,
                            Blocks.BIRCH_PLANKS.defaultBlockState());
                for (int z = 16; z < b.depth - 1; z += 7) {
                    door(b, 4, z, floor, east(b), DoorHingeSide.LEFT);
                    door(b, 8, z, floor, east(b).getOpposite(), DoorHingeSide.RIGHT);
                }
                for (int x : new int[]{3, 10}) {
                    bed(b, x, 2, floor, ((x + floor) & 1) == 0);
                    if (!b.commerce || floor > 0)
                        bed(b, x, 11, floor, (x & 1) == 0);
                    at(b, x + 2, 3, floor + 1, Blocks.BARREL.defaultBlockState());
                }
                if (b.commerce && floor == 0) {
                    // Shopfront and workshop downstairs; upper two levels residential.
                    for (int x : new int[]{2, 10}) {
                        at(b, x, 11, 1, Blocks.LECTERN.defaultBlockState());
                        at(b, x + 1, 11, 1, Blocks.BARREL.defaultBlockState());
                        at(b, x + 2, 11, 1, Blocks.CRAFTING_TABLE.defaultBlockState());
                    }
                    at(b, 6, 12, 1, Blocks.SMOKER.defaultBlockState());
                } else {
                    at(b, 3, 12, floor + 1, Blocks.BOOKSHELF.defaultBlockState());
                    at(b, 10, 12, floor + 1, Blocks.CRAFTING_TABLE.defaultBlockState());
                }
                for (int z = 16; z < b.depth - 2; z += 6) {
                    if (z + 1 < b.depth - 1) bed(b, 2, z, floor, (z & 2) == 0);
                    at(b, 9, z, floor + 1, Blocks.BOOKSHELF.defaultBlockState());
                }
                for (int x : new int[]{4, 11, b.width - 4}) light(b, x, 7, floor);
                for (int x : new int[]{4, 11})
                    for (int z : new int[]{3, 11}) light(b, x, z, floor);
                if (b.depth > 19) light(b, 6, b.depth - 4, floor);
                if (floor > 0) {
                    // A real balcony door from the rear room opens onto the
                    // courtyard gallery; two full-height landings are reachable.
                    door(b, 14, 13, floor, south(b), DoorHingeSide.LEFT);
                    fill(b, 12, 17, 14, 16, floor, floor,
                        Blocks.SPRUCE_SLAB.defaultBlockState());
                    for (int x = 12; x <= 17; x++)
                        at(b, x, 16, floor + 1, Blocks.SPRUCE_FENCE.defaultBlockState());
                    for (int z = 14; z <= 16; z++)
                        at(b, 17, z, floor + 1, Blocks.SPRUCE_FENCE.defaultBlockState());
                }
            }
            // Clear first-floor landing headroom before fitting the two
            // flights. Otherwise the old z=9 room partition seals the stairs.
            for (int x = b.width - 9; x <= b.width - 3; x++)
                for (int h = 8; h <= 10; h++) at(b, x, 9, h, AIR);
            // Paired 7-step switchback staircase; floor-to-floor travel is physical.
            for (int flight = 0; flight < 2; flight++)
                for (int step = 0; step < 7; step++) {
                    int x0 = flight == 0 ? b.width - 9 : b.width - 5;
                    int z = flight == 0 ? 2 + step : 9 - step;
                    int h = flight * 7 + 1 + step;
                    for (int x = x0; x <= x0 + 2; x++) {
                        for (int dy = 0; dy <= 3; dy++) at(b, x, z, h + dy, AIR);
                        at(b, x, z, h, Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING,
                                flight == 0 ? south(b) : north(b)));
                    }
                }
            for (int x = b.width - 9; x <= b.width - 3; x++)
                at(b, x, 9, 7, FLOOR);

            int mid = b.width / 2;
            door(b, mid - 1, 0, 0, north(b), DoorHingeSide.LEFT);
            door(b, mid, 0, 0, north(b), DoorHingeSide.RIGHT);
            for (int x : new int[]{mid - 3, mid + 2}) {
                fill(b, x, x, 0, 0, 1, 6, BEAM);
                at(b, x, 0, 7, GOLD);
            }
            for (int x = mid - 4; x <= mid + 3; x++)
                at(b, x, -1, 7, ROOF);
            for (int x : new int[]{mid - 3, mid + 2}) {
                at(b, x, -1, 6, Blocks.CHAIN.defaultBlockState());
                at(b, x, -1, 5, Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true));
            }
            door(b, 0, b.depth - 5, 0, east(b).getOpposite(), DoorHingeSide.LEFT);
            // Lit glazing every floor, retaining doorways and stair structural bays.
            for (int floor : new int[]{0, 7, 14})
                for (int x = 0; x < b.width; x++)
                    for (int z = 0; z < b.depth; z++) {
                        if (!edge(b, x, z)) continue;
                        if ((x + z) % 6 != 2 && (x + z) % 6 != 3) continue;
                        if (floor == 0 && z == 0 && Math.abs(x - mid) < 5) continue;
                        if (floor == 0 && x == 0 && z >= b.depth - 6) continue;
                        for (int h = floor + 3; h <= floor + 4; h++)
                            at(b, x, z, h, GLASS);
                    }
            // Burgundy intersecting pitched roofs and sealed gables.
            for (int x = -1; x <= b.width; x++)
                for (int z = -1; z <= 14; z++) {
                    int peak = 21 + Math.max(0, 7 - Math.abs(z - 6));
                    for (int h = 21; h < peak; h++)
                        at(b, x, z, h, x == 0 || x == b.width - 1 ? WALL : BEAM);
                    at(b, x, z, peak, ((x + z) & 3) == 0 ? DARK : ROOF);
                }
            for (int z = 14; z <= b.depth; z++)
                for (int x = -1; x <= 12; x++) {
                    int peak = 21 + Math.max(0, 6 - Math.abs(x - 5));
                    for (int h = 21; h < peak; h++)
                        at(b, x, z, h, z == b.depth - 1 ? WALL : BEAM);
                    at(b, x, z, peak, ((x + z) & 3) == 0 ? DARK : ROOF);
                }
            for (int x : new int[]{4, b.width - 5}) {
                fill(b, x, x + 1, 7, 8, 26, 33, TRIM);
                fill(b, x, x + 1, 7, 8, 34, 34, DARK);
            }
            // Decorative red/gold bands visible on all exterior sides.
            for (int x = 0; x < b.width; x++)
                for (int z = 0; z < b.depth; z++)
                    if (edge(b, x, z))
                        for (int h : new int[]{7, 14, 20})
                            at(b, x, z, h, h == 20 ? GOLD : RED);
        }

        boolean validate(BuyanCityPlan.Layout city) {
            if (blocks.isEmpty()) return false;
            int stairs = 0, lanterns = 0, doors = 0, beds = 0;
            for (var entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                BlockState state = entry.getValue();
                if (p.getY() <= world.getMinBuildHeight()
                    || p.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(p)) return false;
                for (BuyanCityPlan.Street road : city.streets())
                    if (p.getX() >= road.minX() && p.getX() <= road.maxX()
                        && p.getZ() >= road.minZ() && p.getZ() <= road.maxZ())
                        return false;
                if (state.is(Blocks.SPRUCE_STAIRS)) stairs++;
                if (state.is(Blocks.LANTERN)) lanterns++;
                if (state.is(Blocks.DARK_OAK_DOOR)) doors++;
                if (state.getBlock() instanceof BedBlock) beds++;
            }
            return stairs >= 8 * 42 && lanterns >= 8 * 12
                && doors >= 8 * 6 && beds >= 8 * 6;
        }

        boolean safe() {
            Set<Long> chunks = new HashSet<>();
            for (var entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                long chunk = ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4);
                if (chunks.add(chunk)) world.getChunk(p.getX() >> 4, p.getZ() >> 4);
                BlockState old = world.getBlockState(p);
                if (!old.equals(entry.getValue()) && !BuyanGenerator.canReplace(old)) {
                    LogUtils.getLogger().warn("Buyan variant 4 housing protected block {}: {}", p, old);
                    return false;
                }
            }
            return true;
        }

        void commit() {
            Map<BlockPos, BlockState> original = new LinkedHashMap<>();
            try {
                for (var entry : blocks.entrySet()) {
                    BlockPos p = entry.getKey();
                    BlockState old = world.getBlockState(p);
                    if (old.equals(entry.getValue())) continue;
                    original.put(p, old);
                    if (!world.setBlock(p, entry.getValue(), 2)
                        && !world.getBlockState(p).equals(entry.getValue()))
                        throw new IllegalStateException("Failed to place Buyan house block " + p);
                }
            } catch (RuntimeException error) {
                List<Map.Entry<BlockPos, BlockState>> undo = new ArrayList<>(original.entrySet());
                for (int i = undo.size() - 1; i >= 0; i--)
                    world.setBlock(undo.get(i).getKey(), undo.get(i).getValue(), 2);
                throw error;
            }
        }
    }

    static boolean hasBuilt(ServerLevel world, BuyanState.Site site) {
        return Record.get(world).finished.contains(new BlockPos(site.x(), 0, site.z()).asLong());
    }

    static boolean placeOnce(ServerLevel world, BuyanState.Site site, BuyanCityPlan.Layout city) {
        if (city == null || city.plots().stream().noneMatch(
                p -> p.name().equals("SANCTUARY_AND_OAK"))) return false;
        Record record = Record.get(world);
        long key = new BlockPos(site.x(), 0, site.z()).asLong();
        if (record.finished.contains(key)) return true;
        Plan plan = new Plan(world, site, city.centerY());
        for (Building b : buildings()) plan.build(b);
        if (!plan.validate(city) || !plan.safe()) return false;
        plan.commit();
        record.finished.add(key);
        record.setDirty();
        LogUtils.getLogger().info("Buyan variant four: eight 3-floor L-house bodies, two shopfronts");
        return true;
    }

    static void verifyBlueprintFixture(ServerLevel world) {
        BuyanState.Site site = new BuyanState.Site(0, 0, BuyanTerrain.MAX_RADIUS, 63);
        BuyanCityPlan.Layout city = BuyanCityPlan.calculateForSeed(site,
            -9047918643626153269L);
        Plan plan = new Plan(world, site, city.centerY());
        for (Building b : buildings()) plan.build(b);
        if (!plan.validate(city))
            throw new IllegalStateException("Variant-four housing blocks a road or lacks interiors");
        List<Building> all = buildings();
        for (int i = 0; i < all.size(); i++)
            for (int j = i + 1; j < all.size(); j++) {
                Building a = all.get(i), b = all.get(j);
                if (a.minX() <= b.maxX() && a.maxX() >= b.minX()
                    && a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ())
                    throw new IllegalStateException("Buyan townhouses intersect");
            }
    }
}
