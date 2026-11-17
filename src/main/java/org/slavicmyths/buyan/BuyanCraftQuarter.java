package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The first of eight reserved Buyan craft-quarter buildings. 15 x 11 forge,
 * built only after the island's existing harbor and outpost. The complete
 * staging map is checked before any world edits.
 *
 * Quarter grid: local X -40..40, Z 0..74; forge X -36..-22, Z 2..12.
 * The other seven plots are reserved metadata, never pre-cleared or populated.
 */
public final class BuyanCraftQuarter {
    private static final String ID = "slavicmyths_buyan_craft_2622";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState COBBLE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState ANDESITE = Blocks.POLISHED_ANDESITE.defaultBlockState();
    private static final BlockState OAK = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState LOG = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState BEAM = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
    private static final BlockState DARK = Blocks.DARK_OAK_PLANKS.defaultBlockState();

    // Reserved future lots: armory, carpentry, cooper, pottery, weaving,
    // tannery and warehouse. They are not touched by this issue.
    private static final int[][] LOTS = {
        {-36, 2, -22, 12}, {-18, 2, -5, 12}, {8, 2, 24, 12},
        {27, 2, 39, 12}, {-36, 43, -24, 54}, {-18, 43, -5, 54},
        {8, 43, 24, 54}, {27, 43, 40, 54}
    };

    private BuyanCraftQuarter() {}

    private static final class Record extends SavedData {
        final Map<Long, Long> positions = new HashMap<>();
        final Map<Long, Integer> rotations = new HashMap<>();

        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), ID);
        }

        static Record load(CompoundTag n, HolderLookup.Provider registries) {
            Record r = new Record();
            long[] keys = n.getLongArray("Keys");
            long[] pos = n.getLongArray("Positions");
            int[] turns = n.getIntArray("Rotations");
            for (int i = 0; i < Math.min(keys.length, pos.length); i++) {
                r.positions.put(keys[i], pos[i]);
                r.rotations.put(keys[i], i < turns.length ? turns[i] : 0);
            }
            return r;
        }

        @Override public CompoundTag save(CompoundTag n, HolderLookup.Provider registries) {
            long[] keys = new long[positions.size()];
            long[] pos = new long[positions.size()];
            int[] turns = new int[positions.size()];
            int i = 0;
            for (var e : positions.entrySet()) {
                keys[i] = e.getKey();
                pos[i] = e.getValue();
                turns[i] = rotations.getOrDefault(e.getKey(), 0);
                i++;
            }
            n.putLongArray("Keys", keys);
            n.putLongArray("Positions", pos);
            n.putIntArray("Rotations", turns);
            return n;
        }

        void mark(long key, BlockPos center, Rotation rotation) {
            positions.put(key, center.asLong());
            rotations.put(key, rotation.ordinal());
            setDirty();
        }
    }

    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final Rotation rotation;
        final int ox, oz, outward;
        final long seed;
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        final Map<BlockPos, String> chests = new LinkedHashMap<>();
        int firstFloor;

        Plan(ServerLevel world, BuyanState.Site site, Rotation rotation,
             int outward, int lateral, long seed) {
            this.world = world;
            this.site = site;
            this.rotation = rotation;
            this.outward = outward;
            this.seed = seed;
            BlockPos origin = switch (rotation) {
                case NONE -> new BlockPos(site.x() + lateral, 0, site.z() + outward);
                case CLOCKWISE_90 -> new BlockPos(site.x() - outward, 0, site.z() + lateral);
                case CLOCKWISE_180 -> new BlockPos(site.x() - lateral, 0, site.z() - outward);
                case COUNTERCLOCKWISE_90 -> new BlockPos(site.x() + outward, 0, site.z() - lateral);
            };
            ox = origin.getX();
            oz = origin.getZ();
        }

        BlockPos pos(int x, int y, int z) {
            return switch (rotation) {
                case NONE -> new BlockPos(ox + x, y, oz + z);
                case CLOCKWISE_90 -> new BlockPos(ox - z, y, oz + x);
                case CLOCKWISE_180 -> new BlockPos(ox - x, y, oz - z);
                case COUNTERCLOCKWISE_90 -> new BlockPos(ox + z, y, oz - x);
            };
        }

        int top(int x, int z) {
            BlockPos p = pos(x, 0, z);
            return BuyanNature.top(seed, site.x(), site.z(), site.radius(),
                p.getX(), p.getZ(), site.waterLevel());
        }

        void at(int x, int y, int z, BlockState state) {
            blocks.put(pos(x, y, z), state.rotate(rotation));
        }

        void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            for (int x = x1; x <= x2; x++)
                for (int z = z1; z <= z2; z++)
                    for (int y = y1; y <= y2; y++) at(x, y, z, state);
        }

        int roofY(int x) {
            return firstFloor + 14 + Math.max(0, 7 - Math.abs(x - 7));
        }

        void chest(int x, int y, int z, String table) {
            at(x, y, z, Blocks.CHEST.defaultBlockState());
            chests.put(pos(x, y, z), table);
        }

        void lamp(int x, int y, int z) {
            at(x, y, z, Blocks.LANTERN.defaultBlockState());
        }

        void door(int x, int y, int z, Direction facing) {
            BlockState lower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            at(x, y, z, lower);
            at(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }

        void bed(int x, int y, int z) {
            BlockState foot = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
            at(x, y, z, foot);
            at(x, y, z + 1, foot.setValue(BedBlock.PART, BedPart.HEAD));
        }

        boolean terrainFit() {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (int x = -40; x <= 40; x += 8)
                for (int z = 0; z <= 74; z += 8) {
                    int h = top(x, z);
                    if (h == Integer.MIN_VALUE || h < site.waterLevel() + 7
                        || BuyanNature.shoreDistance(seed, site.x(), site.z(),
                            pos(x, 0, z).getX(), pos(x, 0, z).getZ()) < 10)
                        return false;
                    min = Math.min(min, h);
                    max = Math.max(max, h);
                }
            if (max - min > 48) return false;
            // The access avenue runs into the existing open central clearing;
            // it may not end at a random stand of trees or in the ocean.
            for (int z = -(outward - 55); z < 0; z += 8) {
                int h = top(2, z);
                if (h == Integer.MIN_VALUE || h < site.waterLevel() + 5) return false;
            }
            int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
            for (int x = -37; x <= -21; x++)
                for (int z = 1; z <= 13; z++) {
                    int y = top(x, z);
                    if (y == Integer.MIN_VALUE) return false;
                    lo = Math.min(lo, y);
                    hi = Math.max(hi, y);
                }
            return hi - lo <= 4;
        }

        int roadStart;
        int[] roadHeights;
        int[][] intersections;

        private static boolean grade(int[] levels) {
            boolean changed = false;
            for (int i = levels.length - 2; i >= 0; i--) {
                int required = levels[i + 1] - 1;
                if (levels[i] < required) { levels[i] = required; changed = true; }
            }
            for (int i = 1; i < levels.length; i++) {
                int required = levels[i - 1] - 1;
                if (levels[i] < required) { levels[i] = required; changed = true; }
            }
            return changed;
        }

        void roadCell(int x, int z, int level, BlockState surface) {
            int ground = top(x, z);
            if (ground == Integer.MIN_VALUE || level < ground + 1)
                throw new IllegalStateException("Craft road outside island or under terrain");
            for (int y = ground + 1; y < level; y++)
                at(x, y, z, y < level - 3 ? COBBLE : ANDESITE);
            at(x, level, z, surface);
            for (int y = level + 1; y <= level + 4; y++) at(x, y, z, AIR);
        }

        BlockState slope(int[] levels, int index, Direction forward, Direction backward) {
            Direction facing = null;
            if (index > 0 && levels[index] > levels[index - 1]) facing = forward;
            else if (index + 1 < levels.length && levels[index] > levels[index + 1])
                facing = backward;
            return facing == null ? ANDESITE : Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
        }

        void roads() {
            // Model the full five-wide avenue and three cross streets as
            // interconnected height profiles. Shared 5x5 junctions are flat;
            // raising a road is safe, lowering it through terrain is forbidden.
            roadStart = -(outward - 55);
            roadHeights = new int[75 - roadStart];
            for (int z = roadStart; z <= 74; z++) {
                int highest = Integer.MIN_VALUE;
                for (int x = 0; x <= 4; x++)
                    highest = Math.max(highest, top(x, z) + 1);
                roadHeights[z - roadStart] = highest;
            }
            int[] crossZ = {16, 35, 58};
            intersections = new int[crossZ.length][81];
            for (int lane = 0; lane < crossZ.length; lane++)
                for (int x = -40; x <= 40; x++) {
                    int high = Integer.MIN_VALUE;
                    for (int z = crossZ[lane]; z <= crossZ[lane] + 4; z++)
                        high = Math.max(high, top(x, z) + 1);
                    intersections[lane][x + 40] = high;
                }
            // Repeated monotone relaxation terminates: 1-Lipschitz grades
            // and exactly level crossing squares are one connected network.
            boolean changed;
            int iterations = 0;
            do {
                changed = grade(roadHeights);
                for (int lane = 0; lane < crossZ.length; lane++) {
                    changed |= grade(intersections[lane]);
                    int level = Integer.MIN_VALUE;
                    for (int z = crossZ[lane]; z <= crossZ[lane] + 4; z++)
                        level = Math.max(level, roadHeights[z - roadStart]);
                    for (int x = 40; x <= 44; x++)
                        level = Math.max(level, intersections[lane][x]);
                    for (int z = crossZ[lane]; z <= crossZ[lane] + 4; z++) {
                        int index = z - roadStart;
                        if (roadHeights[index] < level) {
                            roadHeights[index] = level;
                            changed = true;
                        }
                    }
                    for (int x = 40; x <= 44; x++)
                        if (intersections[lane][x] < level) {
                            intersections[lane][x] = level;
                            changed = true;
                        }
                }
                if (++iterations > 128)
                    throw new IllegalStateException("Craft road grade did not converge");
            } while (changed);

            for (int z = roadStart; z <= 74; z++) {
                int index = z - roadStart, floor = roadHeights[index];
                BlockState paving = slope(roadHeights, index, Direction.SOUTH, Direction.NORTH);
                for (int x = 0; x <= 4; x++) roadCell(x, z, floor, paving);
            }
            for (int lane = 0; lane < crossZ.length; lane++) {
                int[] heights = intersections[lane];
                for (int x = -40; x <= 40; x++) {
                    int level = heights[x + 40];
                    BlockState paving = slope(heights, x + 40, Direction.EAST, Direction.WEST);
                    for (int z = crossZ[lane]; z <= crossZ[lane] + 4; z++)
                        roadCell(x, z, level, paving);
                }
            }

            // A short wide approach joins the smithy door to the first
            // cross-street, never requiring a jump at the porch.
            for (int x = -31; x <= -27; x++) {
                int street = intersections[0][x + 40];
                if (Math.abs(street - firstFloor) > 1)
                    throw new IllegalStateException("Craft forge porch is disconnected from street");
                for (int z = 13; z <= 15; z++)
                    roadCell(x, z, firstFloor, ANDESITE);
            }
        }

        void forge() {
            final int f = firstFloor, x0 = -36, z0 = 2;
            // Stone platform and continuous piers; no floating foundations.
            for (int x = 0; x < 15; x++)
                for (int z = 0; z < 11; z++) {
                    int ground = top(x0 + x, z0 + z);
                    for (int y = ground + 1; y < f; y++)
                        at(x0 + x, y, z0 + z, y < f - 3 ? COBBLE : STONE);
                    at(x0 + x, f, z0 + z, (x == 0 || x == 14 || z == 0 || z == 10) ? STONE : ANDESITE);
                }
            // A six-block-high fire-resistant workshop.
            for (int y = f + 1; y <= f + 6; y++) {
                for (int x = 0; x < 15; x++) {
                    at(x0 + x, y, z0, STONE);
                    at(x0 + x, y, z0 + 10, STONE);
                }
                for (int z = 1; z < 10; z++) {
                    at(x0, y, z0 + z, STONE);
                    at(x0 + 14, y, z0 + z, STONE);
                }
            }
            // Interior framing, shuttered window bays, and material contrast.
            for (int x : new int[] {0, 7, 14})
                for (int z : new int[] {0, 10})
                    fill(x0 + x, f + 1, z0 + z, x0 + x, f + 13, z0 + z, BEAM);
            for (int x : new int[] {3, 11}) {
                at(x0 + x, f + 3, z0, Blocks.GLASS_PANE.defaultBlockState());
                at(x0 + x, f + 3, z0 + 10, Blocks.GLASS_PANE.defaultBlockState());
                at(x0 + x, f + 10, z0, Blocks.GLASS_PANE.defaultBlockState());
                at(x0 + x, f + 10, z0 + 10, Blocks.GLASS_PANE.defaultBlockState());
                at(x0 + x - 1, f + 4, z0 + 10, Blocks.SPRUCE_TRAPDOOR.defaultBlockState());
            }
            for (int z : new int[] {3, 7}) {
                at(x0, f + 3, z0 + z, Blocks.IRON_BARS.defaultBlockState());
                at(x0 + 14, f + 3, z0 + z, Blocks.GLASS_PANE.defaultBlockState());
            }
            // Real second-floor joists and flooring with a stairwell opening.
            fill(x0, f + 7, z0, x0 + 14, f + 7, z0 + 10, SPRUCE);
            for (int z = 3; z <= 9; z++)
                for (int x = 11; x <= 13; x++)
                    at(x0 + x, f + 7, z0 + z, AIR);
            // A timber residence with six-block-high walls.
            for (int y = f + 8; y <= f + 13; y++) {
                for (int x = 0; x < 15; x++) {
                    at(x0 + x, y, z0, OAK);
                    at(x0 + x, y, z0 + 10, OAK);
                }
                for (int z = 1; z < 10; z++) {
                    at(x0, y, z0 + z, OAK);
                    at(x0 + 14, y, z0 + z, OAK);
                }
            }
            for (int x : new int[] {0, 7, 14})
                for (int z : new int[] {0, 10})
                    fill(x0 + x, f + 8, z0 + z, x0 + x, f + 13, z0 + z, LOG);
            fill(x0 + 1, f + 13, z0 + 1, x0 + 13, f + 13, z0 + 1, LOG);
            fill(x0 + 1, f + 13, z0 + 9, x0 + 13, f + 13, z0 + 9, LOG);

            // Gables are backed by full oak planks. Stair roof has no holes:
            // every roof stair also rests on an explicit full backing block.
            for (int x = -1; x <= 15; x++) {
                int y = roofY(x);
                for (int z = -1; z <= 11; z++) {
                    at(x0 + x, y - 1, z0 + z, DARK);
                    BlockState tile = x == 7 ? DARK
                        : Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, x < 7 ? Direction.EAST : Direction.WEST)
                            .setValue(StairBlock.HALF, Half.BOTTOM);
                    at(x0 + x, y, z0 + z, tile);
                }
                if (x >= 0 && x <= 14)
                    for (int y0 = f + 14; y0 < y - 1; y0++) {
                        at(x0 + x, y0, z0, OAK);
                        at(x0 + x, y0, z0 + 10, OAK);
                    }
            }

            // Fire pit in a 3x3 stone chimney, vertically isolated from all
            // wooden floors and rafters. Open flue vents safely above the ridge.
            for (int y = f + 1; y <= f + 25; y++)
                for (int x = 1; x <= 3; x++)
                    for (int z = 2; z <= 4; z++)
                        at(x0 + x, y, z0 + z, x == 2 && z == 3
                            ? (y == f + 1 ? Blocks.CAMPFIRE.defaultBlockState() : AIR)
                            : STONE);
            for (int x = 1; x <= 3; x++)
                for (int z = 2; z <= 4; z++)
                    at(x0 + x, f + 7, z0 + z, STONE);
            at(x0 + 2, f + 7, z0 + 3, AIR);
            for (int x = 1; x <= 3; x++)
                for (int z = 2; z <= 4; z++)
                    if (x != 2 || z != 3)
                        at(x0 + x, f + 26, z0 + z, Blocks.STONE_BRICK_WALL.defaultBlockState());

            // Ground-floor workshop; clear walking aisle from doorway to anvil.
            door(x0 + 7, f + 1, z0 + 10, Direction.SOUTH);
            at(x0 + 6, f + 1, z0 + 5, Blocks.ANVIL.defaultBlockState());
            at(x0 + 8, f + 1, z0 + 2, Blocks.SMITHING_TABLE.defaultBlockState());
            at(x0 + 8, f + 1, z0 + 4, Blocks.BLAST_FURNACE.defaultBlockState());
            at(x0 + 5, f + 1, z0 + 2, Blocks.CRAFTING_TABLE.defaultBlockState());
            at(x0 + 5, f + 1, z0 + 8, Blocks.GRINDSTONE.defaultBlockState());
            at(x0 + 9, f + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
            at(x0 + 10, f + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
            fill(x0 + 10, f + 1, z0 + 4, x0 + 10, f + 4, z0 + 4, OAK);
            at(x0 + 10, f + 2, z0 + 5, Blocks.OAK_TRAPDOOR.defaultBlockState());
            at(x0 + 8, f + 2, z0 + 6, Blocks.IRON_BARS.defaultBlockState());
            at(x0 + 7, f + 2, z0 + 1, Blocks.CHAIN.defaultBlockState());
            chest(x0 + 4, f + 1, z0 + 7, "craft_forge_tools");
            chest(x0 + 10, f + 1, z0 + 1, "craft_forge_materials");
            lamp(x0 + 5, f + 1, z0 + 1);
            lamp(x0 + 9, f + 1, z0 + 8);

            // Seven usable ascending stairs (no jump), the loft floor is
            // absent above the shaft and a landing is left at the upper end.
            for (int z = 9; z >= 3; z--) {
                int y = f + (10 - z);
                at(x0 + 12, y, z0 + z, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
                for (int air = 1; air <= 3; air++)
                    at(x0 + 12, y + air, z0 + z, AIR);
            }
            // At the final step the original stair, not an air block,
            // defines the upper landing elevation.
            at(x0 + 12, f + 7, z0 + 3, Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH));
            at(x0 + 12, f + 7, z0 + 2, SPRUCE);
            for (int z = 4; z <= 9; z++)
                for (int x : new int[] {11, 13})
                    at(x0 + x, f + 8, z0 + z, Blocks.OAK_FENCE.defaultBlockState());

            // Upper rooms: bedroom west, kitchen/dining east, storage south.
            for (int z = 1; z <= 6; z++)
                if (z != 5) fill(x0 + 9, f + 8, z0 + z, x0 + 9, f + 10, z0 + z, OAK);
            for (int x = 1; x <= 10; x++)
                if (x != 6) fill(x0 + x, f + 8, z0 + 7, x0 + x, f + 10, z0 + 7, OAK);
            bed(x0 + 6, f + 8, z0 + 2);
            chest(x0 + 7, f + 8, z0 + 5, "craft_forge_home");
            at(x0 + 4, f + 8, z0 + 5, Blocks.OAK_FENCE.defaultBlockState());
            at(x0 + 4, f + 9, z0 + 5, Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState());
            at(x0 + 5, f + 8, z0 + 5, Blocks.OAK_STAIRS.defaultBlockState());
            at(x0 + 10, f + 8, z0 + 2, Blocks.FURNACE.defaultBlockState());
            at(x0 + 10, f + 8, z0 + 4, Blocks.OAK_FENCE.defaultBlockState());
            at(x0 + 10, f + 9, z0 + 4, Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState());
            at(x0 + 11, f + 8, z0 + 4, Blocks.OAK_STAIRS.defaultBlockState());
            at(x0 + 10, f + 8, z0 + 5, Blocks.BARREL.defaultBlockState());
            at(x0 + 3, f + 8, z0 + 8, Blocks.BARREL.defaultBlockState());
            at(x0 + 5, f + 8, z0 + 8, Blocks.BOOKSHELF.defaultBlockState());
            at(x0 + 7, f + 8, z0 + 8, Blocks.BARREL.defaultBlockState());
            lamp(x0 + 8, f + 8, z0 + 8);
            lamp(x0 + 10, f + 8, z0 + 1);
            lamp(x0 + 5, f + 8, z0 + 1);

            // Entry porch, working awning, outdoor fuel and tool benches.
            for (int x = 5; x <= 9; x++)
                for (int z = 11; z <= 13; z++) {
                    int y = top(x0 + x, z0 + z);
                    for (int h = y + 1; h < f; h++) at(x0 + x, h, z0 + z, COBBLE);
                    at(x0 + x, f, z0 + z, ANDESITE);
                }
            for (int x : new int[] {5, 9}) {
                fill(x0 + x, f + 1, z0 + 12, x0 + x, f + 3, z0 + 12, LOG);
                for (int z = 10; z <= 13; z++)
                    at(x0 + x, f + 4, z0 + z, Blocks.SPRUCE_SLAB.defaultBlockState());
            }
            for (int x = -4; x <= -1; x++) {
                for (int z = 3; z <= 8; z++) {
                    at(x0 + x, f + 4, z0 + z, Blocks.DARK_OAK_SLAB.defaultBlockState());
                    if (z == 3 || z == 8) at(x0 + x, f + 3, z0 + z, LOG);
                }
            }
            at(x0 - 3, f + 1, z0 + 5, Blocks.BARREL.defaultBlockState());
            at(x0 - 2, f + 1, z0 + 6, Blocks.COAL_BLOCK.defaultBlockState());
            at(x0 - 3, f + 1, z0 + 7, Blocks.IRON_BLOCK.defaultBlockState());
            at(x0 + 10, f + 1, z0 + 12, Blocks.BARREL.defaultBlockState());
            lamp(x0 + 4, f + 1, z0 + 12);
        }

        void build() {
            int high = Integer.MIN_VALUE;
            for (int x = -36; x <= -22; x++)
                for (int z = 2; z <= 12; z++)
                    high = Math.max(high, top(x, z));
            firstFloor = high + 1;
            roads();
            forge();
        }

        boolean checkDesign() {
            final int f = firstFloor, x0 = -36, z0 = 2;
            // Assertions validate actual FINAL staged blocks. Check every
            // roof tile and backing (the hollow stone flue is the only opening).
            for (int x = -1; x <= 15; x++)
                for (int z = -1; z <= 11; z++) {
                    int y = roofY(x);
                    BlockState tile = blocks.get(pos(x0 + x, y, z0 + z));
                    if (x >= 1 && x <= 3 && z >= 2 && z <= 4) {
                        if (tile == null || !(tile.is(STONE.getBlock())
                            || (x == 2 && z == 3 && tile.isAir()))) return false;
                        continue;
                    }
                    BlockState backing = blocks.get(pos(x0 + x, y - 1, z0 + z));
                    if (tile == null || !(tile.is(Blocks.DARK_OAK_STAIRS) || tile.is(DARK.getBlock()))
                        || backing == null || !backing.is(DARK.getBlock())) return false;
                }
            if (!blocks.getOrDefault(pos(x0 + 2, f + 1, z0 + 3), AIR).is(Blocks.CAMPFIRE))
                return false;
            for (int y = f + 2; y <= f + 25; y++)
                if (!blocks.getOrDefault(pos(x0 + 2, y, z0 + 3), STONE).isAir()) return false;

            for (int z = 9; z >= 3; z--) {
                BlockPos step = pos(x0 + 12, f + 10 - z, z0 + z);
                BlockState stair = blocks.get(step);
                if (stair == null || !stair.is(Blocks.SPRUCE_STAIRS)
                    || stair.getValue(StairBlock.HALF) != Half.BOTTOM
                    || stair.getValue(StairBlock.FACING) != Direction.NORTH) return false;
                for (int h = 1; h <= 2; h++) {
                    BlockState above = blocks.get(step.above(h));
                    if (above != null && !above.isAir()) return false;
                }
            }
            if (!blocks.getOrDefault(pos(x0 + 12, f + 7, z0 + 2), AIR).is(SPRUCE.getBlock()))
                return false;
            for (var entry : blocks.entrySet()) {
                BlockState state = entry.getValue();
                BlockPos p = entry.getKey();
                if (state.getBlock() instanceof DoorBlock) {
                    boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                    BlockState partner = blocks.get(lower ? p.above() : p.below());
                    if (partner == null || partner.getBlock() != state.getBlock()
                        || partner.getValue(DoorBlock.HALF) ==
                            state.getValue(DoorBlock.HALF)) return false;
                }
                if (state.getBlock() instanceof BedBlock) {
                    boolean foot = state.getValue(BedBlock.PART) == BedPart.FOOT;
                    Direction face = state.getValue(BedBlock.FACING);
                    BlockState partner = blocks.get(p.relative(foot ? face : face.getOpposite()));
                    if (partner == null || !partner.is(Blocks.RED_BED)
                        || partner.getValue(BedBlock.PART) ==
                            state.getValue(BedBlock.PART)) return false;
                }
            }
            for (BlockPos p : chests.keySet())
                if (!blocks.getOrDefault(p, AIR).is(Blocks.CHEST)) return false;
            return chests.size() == 3;
        }

        boolean worldAvailable() {
            Set<Long> loaded = new HashSet<>();
            for (var entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                if (p.getY() <= world.getMinBuildHeight()
                    || p.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(p)) return false;
                if (loaded.add(ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4)))
                    world.getChunk(p.getX() >> 4, p.getZ() >> 4);
            }
            // Reserved plots cannot swallow existing buildings. Trees and
            // leaves in the actual building/road footprint are also protected.
            for (var entry : blocks.entrySet()) {
                BlockState old = world.getBlockState(entry.getKey());
                if (old.is(BlockTags.LOGS) || old.is(BlockTags.LEAVES)
                    || (!old.equals(entry.getValue()) && !BuyanGenerator.canReplace(old)))
                    return false;
            }
            for (int[] lot : LOTS)
                for (int x = lot[0]; x <= lot[2]; x += 3)
                    for (int z = lot[1]; z <= lot[3]; z += 3) {
                        BlockPos p = pos(x, top(x, z) + 1, z);
                        BlockState old = world.getBlockState(p);
                        if (!BuyanGenerator.canReplace(old) && !old.isAir())
                            return false;
                    }
            return true;
        }

        void commit() {
            for (var entry : blocks.entrySet())
                world.setBlock(entry.getKey(), entry.getValue(), 2);
            for (var entry : chests.entrySet()) {
                if (!(world.getBlockEntity(entry.getKey())
                    instanceof RandomizableContainerBlockEntity chest))
                    throw new IllegalStateException("Craft quarter loot chest entity missing");
                ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath("slavicmyths", "chests/" + entry.getValue()));
                chest.setLootTable(table, seed ^ entry.getKey().asLong());
                chest.setChanged();
            }
        }
    }

    private static long siteKey(BuyanState.Site site) {
        return new BlockPos(site.x(), 0, site.z()).asLong();
    }

    static boolean placeOnce(ServerLevel world, BuyanState.Site site) {
        Record record = Record.get(world);
        long key = siteKey(site);
        if (record.positions.containsKey(key)) return true;
        if (BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site)) return false;

        // Near-center sites take precedence over a remote forest clearing;
        // all rotations/offsets are deterministic for every world seed.
        for (int radius = 108; radius <= 180; radius += 12)
            for (Rotation turn : Rotation.values())
                for (int lateral = 0; lateral <= 60; lateral += 12)
                    for (int sign : lateral == 0 ? new int[]{1} : new int[]{-1, 1}) {
                        Plan p = new Plan(world, site, turn, radius, lateral * sign, world.getSeed());
                        if (!p.terrainFit()) continue;
                        try {
                            p.build();
                            if (!p.checkDesign() || !p.worldAvailable()) continue;
                        } catch (IllegalStateException badSite) {
                            // Invalid slope/approach/collision at this hillside:
                            // never apply a partial candidate to the world.
                            continue;
                        }
                        p.commit();
                        record.mark(key, new BlockPos(p.ox, p.firstFloor, p.oz), turn);
                        LogUtils.getLogger().info(
                            "Buyan craft forge completed at {},{} (floor {}) in {} orientation",
                            p.ox, p.oz, p.firstFloor, turn);
                        return true;
                    }
        return false;
    }

    public static int generate(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState.Site site = BuyanNatureGenerator.selectedFoundation(world,
            (int) Math.floor(source.getPosition().x), (int) Math.floor(source.getPosition().z));
        if (site == null || !BuyanNatureState.get(world).finishedAt(site)) {
            source.sendFailure(Component.literal("Буян: требуется завершённый остров и природный этап."));
            return 0;
        }
        if (!placeOnce(world, site)) {
            source.sendFailure(Component.literal(
                "Кузница: свободный безопасный участок не найден. Мир не изменён."));
            return 0;
        }
        return status(source);
    }

    public static int status(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState.Site site = BuyanNatureGenerator.selectedFoundation(world,
            (int) Math.floor(source.getPosition().x), (int) Math.floor(source.getPosition().z));
        if (site == null) {
            source.sendFailure(Component.literal("Буян: в этом месте нет завершённого острова."));
            return 0;
        }
        Record record = Record.get(world);
        Long stored = record.positions.get(siteKey(site));
        if (stored == null) {
            source.sendSuccess(() -> Component.literal("Ремесленная кузница ещё не построена."), false);
            return 0;
        }
        BlockPos p = BlockPos.of(stored);
        source.sendSuccess(() -> Component.literal(
            "Ремесленный квартал: кузница готова, ориентир X=" + p.getX()
            + " Y=" + p.getY() + " Z=" + p.getZ()
            + "; участки под 7 следующих мастерских зарезервированы."), false);
        return 1;
    }

    /** Isolated non-mutating NeoForge regression fixture. */
    public static void verifyBlueprintFixture(ServerLevel world) {
        BuyanState.Site site = new BuyanState.Site(0, 0, 350, 63);
        Plan p = new Plan(world, site, Rotation.NONE, 70, 0, 9083133736688572422L);
        p.firstFloor = 90;
        p.forge();
        if (!p.checkDesign())
            throw new IllegalStateException("Craft forge blueprint failed final roof/stair/door/bed/chest assertions");
        BlockPos roof = p.pos(-32, p.roofY(4), 8);
        BlockState original = p.blocks.put(roof, AIR);
        if (p.checkDesign())
            throw new IllegalStateException("Craft roof hole was not detected");
        p.blocks.put(roof, original);
        BlockPos stair = p.pos(-24, 91, 11);
        BlockState originalStair = p.blocks.put(stair, AIR);
        if (p.checkDesign())
            throw new IllegalStateException("Craft inaccessible stair was not detected");
        p.blocks.put(stair, originalStair);
        if (!p.checkDesign())
            throw new IllegalStateException("Craft blueprint restoration failed");
        verifyReportedSeedSite(world);
    }

    public static void verifyReportedSeedSite(ServerLevel world) {
        BuyanState.Site site = new BuyanState.Site(0, 0, 350, 63);
        long seed = 9083133736688572422L;
        int tested = 0;
        for (int distance = 108; distance <= 180; distance += 12)
            for (Rotation direction : Rotation.values())
                for (int lateral = 0; lateral <= 60; lateral += 12)
                    for (int sign : lateral == 0 ? new int[]{1} : new int[]{-1, 1}) {
                        Plan candidate = new Plan(world, site, direction,
                            distance, lateral * sign, seed);
                        if (!candidate.terrainFit()) continue;
                        tested++;
                        try {
                            candidate.build();
                            if (!candidate.checkDesign()) continue;
                            LogUtils.getLogger().info(
                                "Craft quarter real-seed terrain fixture passed at {},{} floor {} rotation {} after {} sites",
                                candidate.ox, candidate.oz, candidate.firstFloor, direction, tested);
                            return;
                        } catch (IllegalStateException badSite) {
                            // Continue checking the same candidate set as production.
                        }
                    }
        throw new IllegalStateException("Craft quarter: reported seed has no viable blueprint site after "
            + tested + " terrain candidates");
    }
}
