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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.LanternBlock;
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
 * Forge (15 x 11), armory (17 x 13) and shared smithing yard are built
 * together after the harbor and hill outpost, before the player joins.
 * Every planned block is checked before any world edit.
 *
 * Quarter grid: local X -40..40, Z 0..74, including six future craft lots.
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
    private static final BlockState ARM_ROOF = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState ARM_BEAM = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();

    // The first two plots are occupied. The remaining six stay untouched:
    // carpentry, cooperage, pottery, weaving, tannery and general storage.
    private static final int[][] LOTS = {
        {-36, 2, -22, 12}, {8, 0, 24, 12},
        {27, 2, 39, 12}, {-36, 43, -24, 54},
        {-18, 43, -5, 54}, {8, 43, 24, 54}, {27, 43, 40, 54},
        {-18, 22, -5, 32}
    };

    private BuyanCraftQuarter() {}

    private static final class Record extends SavedData {
        final Map<Long, Long> positions = new HashMap<>();
        final Map<Long, Integer> rotations = new HashMap<>();
        // Missing value means a legacy save containing only the old forge.
        final Map<Long, Integer> armoryFloors = new HashMap<>();

        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), ID);
        }

        static Record load(CompoundTag n, HolderLookup.Provider registries) {
            Record r = new Record();
            long[] keys = n.getLongArray("Keys");
            long[] pos = n.getLongArray("Positions");
            int[] turns = n.getIntArray("Rotations");
            int[] armory = n.getIntArray("ArmoryFloors");
            for (int i = 0; i < Math.min(keys.length, pos.length); i++) {
                r.positions.put(keys[i], pos[i]);
                r.rotations.put(keys[i], i < turns.length ? turns[i] : 0);
                if (i < armory.length && armory[i] > 0)
                    r.armoryFloors.put(keys[i], armory[i]);
            }
            return r;
        }

        @Override public CompoundTag save(CompoundTag n, HolderLookup.Provider registries) {
            long[] keys = new long[positions.size()];
            long[] pos = new long[positions.size()];
            int[] turns = new int[positions.size()];
            int[] armory = new int[positions.size()];
            int i = 0;
            for (var e : positions.entrySet()) {
                keys[i] = e.getKey();
                pos[i] = e.getValue();
                turns[i] = rotations.getOrDefault(e.getKey(), 0);
                armory[i] = armoryFloors.getOrDefault(e.getKey(), 0);
                i++;
            }
            n.putLongArray("Keys", keys);
            n.putLongArray("Positions", pos);
            n.putIntArray("Rotations", turns);
            n.putIntArray("ArmoryFloors", armory);
            return n;
        }

        void mark(long key, BlockPos center, Rotation rotation, int armoryFloor) {
            positions.put(key, center.asLong());
            rotations.put(key, rotation.ordinal());
            armoryFloors.put(key, armoryFloor);
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
        final Map<BlockPos, Integer> displayStands = new LinkedHashMap<>();
        int armoryFloor;
        int firstFloor;
        String collisionReason = "none";

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

        int armoryRoofY(int z) {
            // Broad roof ridge runs EAST-WEST, unlike the forge's NORTH-SOUTH ridge.
            return armoryFloor + 15 + Math.max(0, 6 - Math.abs(z - 6));
        }

        void rack(int x, int y, int z, int equipment) {
            displayStands.put(pos(x, y, z), equipment);
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
            if (hi - lo > 4) return false;
            lo = Integer.MAX_VALUE;
            hi = Integer.MIN_VALUE;
            for (int x = 8; x <= 24; x++)
                for (int z = 0; z <= 12; z++) {
                    int y = top(x, z);
                    if (y == Integer.MIN_VALUE) return false;
                    lo = Math.min(lo, y);
                    hi = Math.max(hi, y);
                }
            return hi - lo <= 7;
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
            if (ground == Integer.MIN_VALUE || level < ground)
                throw new IllegalStateException("Craft road outside island or below terrain surface");
            for (int y = ground + 1; y < level; y++)
                at(x, y, z, y < level - 3 ? COBBLE : ANDESITE);
            at(x, level, z, surface);
            for (int y = level + 1; y <= level + 4; y++) at(x, y, z, AIR);
            // Natural crowns and trunks must not remain suspended over the new street.
            // Only vegetation above normal headroom is additionally cleared.
            for (int y = level + 5; y <= level + 18; y++) {
                BlockState existing = world.getBlockState(pos(x, y, z));
                if (existing.is(BlockTags.LOGS) || existing.is(BlockTags.LEAVES))
                    at(x, y, z, AIR);
            }
        }

        BlockState surface(int x, int z, BlockState slope) {
            if (slope.is(Blocks.STONE_BRICK_STAIRS)) return slope;
            long mix = BuyanTerrain.hash(seed ^ 0xA4E9B223L,
                pos(x, 0, z).getX(), pos(x, 0, z).getZ(), 0);
            int n = (int) Math.floorMod(mix, 20);
            if (n < 9) return ANDESITE;
            if (n < 14) return COBBLE;
            if (n < 17) return STONE;
            if (n < 19) return Blocks.GRAVEL.defaultBlockState();
            return Blocks.COARSE_DIRT.defaultBlockState();
        }

        boolean isPaving(BlockState s) {
            return s != null && (s.is(Blocks.STONE_BRICK_STAIRS)
                || s.is(Blocks.POLISHED_ANDESITE) || s.is(Blocks.COBBLESTONE)
                || s.is(Blocks.STONE_BRICKS) || s.is(Blocks.GRAVEL)
                || s.is(Blocks.COARSE_DIRT));
        }

        int armoryPorchHeight(int x, int z) {
            int street = intersections[0][x + 40];
            return armoryFloor + (int) Math.round((street - armoryFloor) * (z - 12) / 4.0);
        }

        BlockState armoryPorchSurface(int x, int z) {
            int y = armoryPorchHeight(x, z);
            Direction facing = y > (z == 13 ? armoryFloor : armoryPorchHeight(x, z - 1))
                ? Direction.SOUTH
                : y > (z == 15 ? intersections[0][x + 40] : armoryPorchHeight(x, z + 1))
                    ? Direction.NORTH : null;
            return facing == null ? surface(x, z, ANDESITE)
                : Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
        }

        BlockState slope(int[] levels, int index, Direction forward, Direction backward) {
            Direction facing = null;
            if (index > 0 && levels[index] > levels[index - 1]) facing = forward;
            else if (index + 1 < levels.length && levels[index] > levels[index + 1])
                facing = backward;
            return facing == null ? ANDESITE : Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
        }

        int porchHeight(int x, int z) {
            int street = intersections[0][x + 40];
            return firstFloor + (int) Math.round((street - firstFloor) * (z - 12) / 4.0);
        }

        BlockState porchSurface(int x, int z) {
            int y = porchHeight(x, z);
            Direction face = y > (z == 13 ? firstFloor : porchHeight(x, z - 1))
                ? Direction.SOUTH
                : y > (z == 15 ? intersections[0][x + 40] : porchHeight(x, z + 1))
                    ? Direction.NORTH : null;
            return face == null ? ANDESITE : Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, face).setValue(StairBlock.HALF, Half.BOTTOM);
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
                for (int x = 0; x <= 4; x++)
                    roadCell(x, z, floor, surface(x, z, paving));
            }
            for (int lane = 0; lane < crossZ.length; lane++) {
                int[] heights = intersections[lane];
                for (int x = -40; x <= 40; x++) {
                    int level = heights[x + 40];
                    BlockState paving = slope(heights, x + 40, Direction.EAST, Direction.WEST);
                    for (int z = crossZ[lane]; z <= crossZ[lane] + 4; z++)
                        roadCell(x, z, level, surface(x, z, paving));
                }
            }

            // A short wide approach joins the smithy door to the first
            // cross-street, never requiring a jump at the porch.
            for (int x = -30; x <= -28; x++) {
                int street = intersections[0][x + 40];
                if (Math.abs(street - firstFloor) > 4)
                    throw new IllegalStateException("Craft forge porch is disconnected from street");
                for (int z = 13; z <= 15; z++)
                    roadCell(x, z, porchHeight(x, z), porchSurface(x, z));
            }
            // Two separate, genuinely accessible entrances: retail and workshop.
            for (int left : new int[]{12, 19})
                for (int x = left; x <= left + 2; x++) {
                    int street = intersections[0][x + 40];
                    if (Math.abs(street - armoryFloor) > 4)
                        throw new IllegalStateException("Craft armory entrance has no graded road");
                    for (int z = 13; z <= 15; z++)
                        roadCell(x, z, armoryPorchHeight(x, z),
                            armoryPorchSurface(x, z));
                }
        }

        void forge() {
            final int f = firstFloor, x0 = -36, z0 = 2;
            // Clear the complete workshop and upper dwelling volume before placing
            // walls and furnishings, otherwise a tree can survive inside the house.
            for (int x = 0; x < 15; x++)
                for (int z = 0; z < 11; z++)
                    for (int y = f + 1; y <= f + 26; y++)
                        at(x0 + x, y, z0 + z, AIR);
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
            // Partition the raw-material store from the active work bay,
            // leaving a two-block-high doorway at its south-west corner.
            for (int z = 1; z <= 4; z++)
                for (int y = f + 1; y <= f + 5; y++)
                    if (z != 4 || y > f + 2) at(x0 + 9, y, z0 + z, STONE);
            at(x0 + 11, f + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
            at(x0 + 10, f + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
            fill(x0 + 10, f + 4, z0 + 5, x0 + 11, f + 5, z0 + 5, OAK);
            at(x0 + 11, f + 3, z0 + 1, Blocks.OAK_TRAPDOOR.defaultBlockState());
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
            bed(x0 + 12, f + 8, z0 + 2);
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
            // The 3-wide porch/ramp was staged together with the road at
            // the correct stair heights; do not flatten it while furnishing.
            for (int x : new int[] {5, 9}) {
                fill(x0 + x, porchHeight(x0 + x, z0 + 12) + 1, z0 + 12,
                    x0 + x, f + 3, z0 + 12, LOG);
                for (int z = 10; z <= 13; z++)
                    at(x0 + x, f + 4, z0 + z, Blocks.SPRUCE_SLAB.defaultBlockState());
            }
            for (int z : new int[] {3, 8}) {
                int soil = top(x0 - 4, z0 + z);
                for (int y = soil + 1; y <= f + 3; y++)
                    at(x0 - 4, y, z0 + z, LOG);
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
            at(x0 + 5, f + 3, z0 + 11,
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }


        void armory() {
            final int x0 = 8, z0 = 0, f = armoryFloor;
            // Clear the entire two-storey volume so naturally generated
            // trees never remain embedded in timber, furniture or shingles.
            for (int x = 0; x < 17; x++)
                for (int z = 0; z < 13; z++)
                    for (int y = f + 1; y <= f + 26; y++) at(x0 + x, y, z0 + z, AIR);

            // 17x13 masonry plinth on actual terrain, including deep piers.
            for (int x = 0; x < 17; x++)
                for (int z = 0; z < 13; z++) {
                    int ground = top(x0 + x, z0 + z);
                    for (int y = ground + 1; y < f; y++)
                        at(x0 + x, y, z0 + z, y >= f - 3 ? STONE : COBBLE);
                    at(x0 + x, f, z0 + z,
                        x == 0 || x == 16 || z == 0 || z == 12 ? STONE : ANDESITE);
                }
            // Workshop below; residential timber floor and frames above.
            for (int y = f + 1; y <= f + 6; y++) {
                BlockState masonry = y <= f + 3 ? STONE : OAK;
                for (int x = 0; x < 17; x++) {
                    at(x0 + x, y, z0, masonry);
                    at(x0 + x, y, z0 + 12, masonry);
                }
                for (int z = 1; z < 12; z++) {
                    at(x0, y, z0 + z, masonry);
                    at(x0 + 16, y, z0 + z, masonry);
                }
            }
            for (int x : new int[]{0, 4, 8, 12, 16})
                for (int z : new int[]{0, 12})
                    fill(x0 + x, f + 1, z0 + z, x0 + x, f + 13, z0 + z, ARM_BEAM);
            for (int z : new int[]{0, 4, 8, 12})
                for (int x : new int[]{0, 16})
                    fill(x0 + x, f + 1, z0 + z, x0 + x, f + 13, z0 + z, ARM_BEAM);

            fill(x0, f + 7, z0, x0 + 16, f + 7, z0 + 12, SPRUCE);
            for (int x = 13; x <= 15; x++)
                for (int z = 4; z <= 10; z++)
                    at(x0 + x, f + 7, z0 + z, AIR);
            for (int y = f + 8; y <= f + 13; y++) {
                for (int x = 0; x < 17; x++) {
                    at(x0 + x, y, z0, OAK);
                    at(x0 + x, y, z0 + 12, OAK);
                }
                for (int z = 1; z < 12; z++) {
                    at(x0, y, z0 + z, OAK);
                    at(x0 + 16, y, z0 + z, OAK);
                }
            }
            for (int x : new int[]{0, 4, 8, 12, 16})
                for (int z : new int[]{0, 12})
                    fill(x0 + x, f + 8, z0 + z, x0 + x, f + 13, z0 + z, ARM_BEAM);

            // Distinct broad east-west ridge, backed along every tile
            // (and solid gables beneath the roof).
            for (int z = -1; z <= 13; z++) {
                int y = armoryRoofY(z);
                for (int x = -1; x <= 17; x++) {
                    at(x0 + x, y - 1, z0 + z, ARM_ROOF);
                    BlockState tile = z == 6 ? ARM_ROOF
                        : Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, z < 6
                                ? Direction.SOUTH : Direction.NORTH)
                            .setValue(StairBlock.HALF, Half.BOTTOM);
                    at(x0 + x, y, z0 + z, tile);
                }
                for (int x : new int[]{0, 16})
                    for (int yy = f + 14; yy < y - 1; yy++)
                        at(x0 + x, yy, z0 + z, OAK);
            }
            // Protected masonry flue above the armory repair hearth.
            for (int y = f + 1; y <= f + 25; y++)
                for (int x = 1; x <= 3; x++)
                    for (int z = 1; z <= 3; z++)
                        at(x0 + x, y, z0 + z,
                            x == 2 && z == 2
                                ? y == f + 1 ? Blocks.CAMPFIRE.defaultBlockState() : AIR
                                : STONE);
            for (int x = 1; x <= 3; x++)
                for (int z = 1; z <= 3; z++)
                    if (x != 2 || z != 2)
                        at(x0 + x, f + 26, z0 + z,
                            Blocks.STONE_BRICK_WALL.defaultBlockState());

            // Exterior: two different entrances, open windows and shallow
            // cantilevered shop canopy with wooden support posts.
            door(x0 + 5, f + 1, z0 + 12, Direction.SOUTH);
            door(x0 + 12, f + 1, z0 + 12, Direction.SOUTH);
            for (int x : new int[]{2, 9, 15})
                for (int z : new int[]{0, 12}) {
                    at(x0 + x, f + 4, z0 + z, Blocks.GLASS_PANE.defaultBlockState());
                    at(x0 + x, f + 11, z0 + z, Blocks.GLASS_PANE.defaultBlockState());
                }
            for (int z : new int[]{5, 9})
                for (int x : new int[]{0, 16})
                    at(x0 + x, f + 10, z0 + z, Blocks.GLASS_PANE.defaultBlockState());
            for (int x = 2; x <= 14; x++)
                for (int z = 13; z <= 14; z++)
                    at(x0 + x, f + 5, z0 + z,
                        Blocks.DARK_OAK_SLAB.defaultBlockState());
            for (int x : new int[]{2, 14})
                fill(x0 + x, f + 1, z0 + 13,
                    x0 + x, f + 4, z0 + 13, ARM_BEAM);

            // Three clearly separated ground-floor functions: display /
            // sales to the west, repair in the middle, fabrication east.
            for (int z = 1; z <= 11; z++)
                for (int y = f + 1; y <= f + 5; y++)
                    if (z < 6 || z > 8) at(x0 + 8, y, z0 + z, OAK);
            at(x0 + 2, f + 1, z0 + 10, Blocks.BARREL.defaultBlockState());
            at(x0 + 3, f + 1, z0 + 10, Blocks.BARREL.defaultBlockState());
            fill(x0 + 2, f + 1, z0 + 8, x0 + 6, f + 1, z0 + 8, SPRUCE);
            for (int x : new int[]{2, 6})
                at(x0 + x, f + 2, z0 + 8, Blocks.OAK_FENCE.defaultBlockState());
            rack(x0 + 3, f + 1, z0 + 6, 0);
            rack(x0 + 5, f + 1, z0 + 6, 1);
            rack(x0 + 10, f + 1, z0 + 6, 2);
            chest(x0 + 5, f + 1, z0 + 4, "craft_armory_goods");
            chest(x0 + 6, f + 1, z0 + 2, "craft_armory_goods");
            at(x0 + 10, f + 1, z0 + 2, Blocks.SMITHING_TABLE.defaultBlockState());
            at(x0 + 12, f + 1, z0 + 2, Blocks.ANVIL.defaultBlockState());
            at(x0 + 14, f + 1, z0 + 2, Blocks.BLAST_FURNACE.defaultBlockState());
            at(x0 + 10, f + 1, z0 + 4, Blocks.GRINDSTONE.defaultBlockState());
            at(x0 + 12, f + 1, z0 + 5, Blocks.CRAFTING_TABLE.defaultBlockState());
            at(x0 + 10, f + 1, z0 + 10, Blocks.FLETCHING_TABLE.defaultBlockState());
            chest(x0 + 9, f + 1, z0 + 3, "craft_armory_materials");
            chest(x0 + 11, f + 1, z0 + 9, "craft_armory_materials");
            at(x0 + 2, f + 1, z0 + 5, Blocks.OAK_STAIRS.defaultBlockState());
            at(x0 + 6, f + 1, z0 + 5, Blocks.OAK_STAIRS.defaultBlockState());
            for (int x : new int[]{6, 11}) {
                at(x0 + x, f + 6, z0 + 11, SPRUCE);
                at(x0 + x, f + 5, z0 + 11, Blocks.CHAIN.defaultBlockState());
                at(x0 + x, f + 4, z0 + 11,
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }

            // Seven real ascending stairs and a cleared 2-block headspace.
            for (int z = 10; z >= 4; z--) {
                int y = f + 11 - z;
                at(x0 + 14, y, z0 + z, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
                for (int clear = 1; clear <= 3; clear++)
                    at(x0 + 14, y + clear, z0 + z, AIR);
            }
            at(x0 + 14, f + 7, z0 + 3, SPRUCE);
            for (int z = 4; z <= 10; z++)
                at(x0 + 12, f + 8, z0 + z, Blocks.OAK_FENCE.defaultBlockState());

            // Master bedroom + apprentice room; connected by the central
            // upstairs corridor, with genuine beds and small dining kitchen.
            for (int x = 1; x <= 11; x++)
                if (x != 7)
                    fill(x0 + x, f + 8, z0 + 8, x0 + x, f + 10, z0 + 8, OAK);
            for (int z = 1; z <= 7; z++)
                if (z != 5)
                    fill(x0 + 8, f + 8, z0 + z, x0 + 8, f + 10, z0 + z, OAK);
            bed(x0 + 4, f + 8, z0 + 2);
            bed(x0 + 6, f + 8, z0 + 2);
            chest(x0 + 3, f + 8, z0 + 5, "craft_armory_home");
            at(x0 + 10, f + 8, z0 + 2, Blocks.FURNACE.defaultBlockState());
            at(x0 + 11, f + 8, z0 + 4, Blocks.OAK_FENCE.defaultBlockState());
            at(x0 + 11, f + 9, z0 + 4, Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState());
            at(x0 + 6, f + 8, z0 + 10, Blocks.OAK_FENCE.defaultBlockState());
            at(x0 + 6, f + 9, z0 + 10, Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState());
            at(x0 + 5, f + 8, z0 + 10, Blocks.OAK_STAIRS.defaultBlockState());
            at(x0 + 7, f + 8, z0 + 10, Blocks.OAK_STAIRS.defaultBlockState());
            at(x0 + 3, f + 8, z0 + 10, Blocks.BOOKSHELF.defaultBlockState());
            at(x0 + 9, f + 8, z0 + 10, Blocks.BARREL.defaultBlockState());
            at(x0 + 10, f + 8, z0 + 10, Blocks.BARREL.defaultBlockState());
            for (int x : new int[]{2, 11, 15})
                at(x0 + x, f + 8, z0 + 11, Blocks.LANTERN.defaultBlockState());
        }

        void smithingYard() {
            // All work areas remain west of the five-block principal avenue;
            // the forge entrance and both armory ramps stay clear.
            // Natural stone/grass below is preserved, with short stone piers
            // instead of destructive full-volume terrain flattening.
            for (int x = -19; x <= -5; x++)
                for (int z = 1; z <= 12; z++) {
                    int ground = top(x, z);
                    if (ground == Integer.MIN_VALUE)
                        throw new IllegalStateException("Smithing yard outside island");
                    int y = ground + 1;
                    BlockState surface = (x + z) % 4 == 0 ? COBBLE :
                        (x + z) % 4 == 1 ? ANDESITE :
                        (x + z) % 4 == 2 ? Blocks.GRAVEL.defaultBlockState()
                            : Blocks.COARSE_DIRT.defaultBlockState();
                    at(x, y, z, surface);
                    for (int up = 1; up <= 3; up++) at(x, y + up, z, AIR);
                }
            // Loading and raw-metal bays. Their low walls and heavy tarps
            // leave a broad, unblocked aisle at x=-12..-10.
            for (int x = -18; x <= -16; x++)
                for (int z = 3; z <= 8; z++) {
                    int y = top(x, z) + 2;
                    if (z == 3 || z == 8)
                        at(x, y, z, Blocks.IRON_BLOCK.defaultBlockState());
                    else at(x, y, z, Blocks.COAL_BLOCK.defaultBlockState());
                }
            for (int x = -8; x <= -6; x++)
                for (int z = 3; z <= 6; z++) {
                    int y = top(x, z) + 2;
                    at(x, y, z, Blocks.SPRUCE_LOG.defaultBlockState());
                }
            // Two accessible timber awnings: charcoal and dry tool stock.
            for (int left : new int[]{-19, -9}) {
                int right = left + 3;
                int floor = Integer.MIN_VALUE;
                for (int x = left; x <= right; x++)
                    for (int z = 8; z <= 12; z++)
                        floor = Math.max(floor, top(x, z) + 1);
                for (int x : new int[]{left, right})
                    for (int z : new int[]{8, 12}) {
                        int base = top(x, z);
                        for (int y = base + 1; y <= floor + 4; y++)
                            at(x, y, z, y < floor ? COBBLE : LOG);
                    }
                for (int x = left; x <= right; x++)
                    for (int z = 8; z <= 12; z++)
                        at(x, floor + 5, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
                at(left + 1, floor + 1, 10, Blocks.BARREL.defaultBlockState());
            }
            // Finished goods, tool preparation and repair, separate from the
            // central loading aisle and the raised solid-fuel bays.
            at(-14, top(-14, 4) + 2, 4, Blocks.ANVIL.defaultBlockState());
            at(-9, top(-9, 4) + 2, 4, Blocks.SMITHING_TABLE.defaultBlockState());
            at(-15, top(-15, 3) + 2, 3, Blocks.BARREL.defaultBlockState());
            at(-7, top(-7, 2) + 2, 2, Blocks.CRAFTING_TABLE.defaultBlockState());
            // Charcoal and timber are actually sheltered by the two roofs,
            // not merely placed next to the awnings.
            for (int x = -18; x <= -17; x++)
                for (int z : new int[]{9, 11})
                    at(x, top(x, z) + 2, z, Blocks.COAL_BLOCK.defaultBlockState());
            for (int x = -8; x <= -7; x++)
                for (int z : new int[]{9, 11})
                    at(x, top(x, z) + 2, z, Blocks.SPRUCE_LOG.defaultBlockState());
            // Low railings protect loading stacks without closing the open aisle.
            for (int x = -19; x <= -16; x++)
                at(x, top(x, 1) + 2, 1, Blocks.OAK_FENCE.defaultBlockState());
            for (int z = 4; z <= 6; z++)
                at(-5, top(-5, z) + 2, z, Blocks.OAK_FENCE.defaultBlockState());
            // Three lantern posts avoid the two entrance ramps and roads.
            for (int x : new int[]{-19, -13, -5}) {
                int z = x == -19 ? 2 : x == -13 ? 4 : 11;
                int base = top(x, z) + 1;
                for (int h = 1; h <= 4; h++) at(x, base + h, z, Blocks.OAK_FENCE.defaultBlockState());
                at(x, base + 5, z, Blocks.LANTERN.defaultBlockState());
            }
            // Graded three-wide loading lane from the inner yard to the
            // existing cross-street. The final row is fixed at the junction:
            // if terrain cannot be connected without changing that road,
            // reject this site and select a different orientation.
            int[] lane = new int[14]; // local Z=3..16
            for (int z = 3; z <= 15; z++) {
                int high = Integer.MIN_VALUE;
                for (int x = -12; x <= -10; x++)
                    high = Math.max(high, top(x, z) + 1);
                lane[z - 3] = high;
            }
            lane[13] = Integer.MIN_VALUE;
            for (int x = -12; x <= -10; x++)
                lane[13] = Math.max(lane[13], intersections[0][x + 40]);
            for (int attempt = 0; attempt < 32 && grade(lane); attempt++) {
                if (lane[13] != Math.max(intersections[0][28],
                    Math.max(intersections[0][29], intersections[0][30])))
                    throw new IllegalStateException("Smithing yard road cannot reach existing crossing");
            }
            for (int z = 3; z <= 15; z++) {
                int y = lane[z - 3];
                if (Math.abs(y - lane[z - 2]) > 1)
                    throw new IllegalStateException("Smithing yard road has a discontinuity");
                for (int x = -12; x <= -10; x++)
                    roadCell(x, z, y, surface(x, z, ANDESITE));
            }
        }

        void build() {
            int high = Integer.MIN_VALUE;
            for (int x = -36; x <= -22; x++)
                for (int z = 2; z <= 12; z++)
                    high = Math.max(high, top(x, z));
            firstFloor = high + 1;
            high = Integer.MIN_VALUE;
            for (int x = 8; x <= 24; x++)
                for (int z = 0; z <= 12; z++)
                    high = Math.max(high, top(x, z));
            armoryFloor = high + 1;
            roads();
            forge();
            armory();
            smithingYard();
        }

        boolean checkDesign() {
            final int f = firstFloor, x0 = -36, z0 = 2;
            if (roadHeights != null) {
                int[] crossZ = {16, 35, 58};
                for (int z = roadStart; z <= 74; z++) {
                    int i = z - roadStart, y = roadHeights[i];
                    if (i > 0 && Math.abs(y - roadHeights[i - 1]) > 1) return false;
                    BlockState road = blocks.get(pos(2, y, z));
                    if (!isPaving(road)) return false;
                    for (int x = 0; x <= 4; x++) {
                        BlockState surface = blocks.get(pos(x, y, z));
                        if (!isPaving(surface)) return false;
                    }
                }
                for (int lane = 0; lane < intersections.length; lane++) {
                    int[] heights = intersections[lane];
                    for (int x = -40; x <= 40; x++) {
                        int i = x + 40, y = heights[i];
                        if (i > 0 && Math.abs(y - heights[i - 1]) > 1) return false;
                        BlockState paving = blocks.get(pos(x, y, crossZ[lane] + 2));
                        if (!isPaving(paving)) return false;
                    }
                    for (int z = crossZ[lane]; z <= crossZ[lane] + 4; z++)
                        for (int x = 0; x <= 4; x++)
                            if (roadHeights[z - roadStart] != heights[x + 40]) return false;
                }
                for (int x = -30; x <= -28; x++) {
                    int last = firstFloor;
                    for (int z = 13; z <= 16; z++) {
                        int y = z == 16 ? intersections[0][x + 40] : porchHeight(x, z);
                        if (Math.abs(y - last) > 1) return false;
                        last = y;
                        if (z < 16) {
                            BlockState path = blocks.get(pos(x, y, z));
                            if (!isPaving(path)) return false;
                        }
                    }
                }
            }
            for (int left : new int[]{12, 19})
                for (int x = left; x <= left + 2; x++) {
                    int last = armoryFloor;
                    for (int z = 13; z <= 16; z++) {
                        int y = z == 16 ? intersections[0][x + 40]
                            : armoryPorchHeight(x, z);
                        if (Math.abs(y - last) > 1) return false;
                        last = y;
                        if (z < 16 && !isPaving(blocks.get(pos(x, y, z)))) return false;
                    }
                }
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
            // Every sloped armory roof tile has solid backing; only the
            // fully enclosed 3x3 masonry chimney may interrupt it.
            for (int z = -1; z <= 13; z++)
                for (int x = -1; x <= 17; x++) {
                    int y = armoryRoofY(z);
                    BlockState tile = blocks.get(pos(8 + x, y, z));
                    if (x >= 1 && x <= 3 && z >= 1 && z <= 3) {
                        if (tile == null || !(tile.is(STONE.getBlock())
                            || (x == 2 && z == 2 && tile.isAir()))) return false;
                        continue;
                    }
                    BlockState backing = blocks.get(pos(8 + x, y - 1, z));
                    if (tile == null || !(tile.is(Blocks.DEEPSLATE_TILE_STAIRS)
                        || tile.is(ARM_ROOF.getBlock()))
                        || backing == null || !backing.is(ARM_ROOF.getBlock())) return false;
                }
            for (int z = 10; z >= 4; z--) {
                BlockPos step = pos(22, armoryFloor + 11 - z, z);
                BlockState stair = blocks.get(step);
                if (stair == null || !stair.is(Blocks.SPRUCE_STAIRS)
                    || stair.getValue(StairBlock.HALF) != Half.BOTTOM
                    || stair.getValue(StairBlock.FACING) !=
                        Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH)
                            .rotate(rotation).getValue(StairBlock.FACING)) return false;
                for (int y = 1; y <= 2; y++) {
                    BlockState above = blocks.get(step.above(y));
                    if (above != null && !above.isAir()) return false;
                }
            }
            if (displayStands.size() != 3) return false;
            if (!blocks.getOrDefault(pos(x0 + 2, f + 1, z0 + 3), AIR).is(Blocks.CAMPFIRE))
                return false;
            for (int y = f + 2; y <= f + 25; y++)
                if (!blocks.getOrDefault(pos(x0 + 2, y, z0 + 3), STONE).isAir()) return false;

            for (int z = 9; z >= 3; z--) {
                BlockPos step = pos(x0 + 12, f + 10 - z, z0 + z);
                BlockState stair = blocks.get(step);
                if (stair == null || !stair.is(Blocks.SPRUCE_STAIRS)
                    || stair.getValue(StairBlock.HALF) != Half.BOTTOM
                    || stair.getValue(StairBlock.FACING) !=
                        Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH)
                            .rotate(rotation).getValue(StairBlock.FACING)) return false;
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
            return chests.size() == 8;
        }

        boolean worldAvailable() {
            Set<Long> loaded = new HashSet<>();
            for (var entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                if (p.getY() <= world.getMinBuildHeight()
                    || p.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(p)) {
                    collisionReason = "outside build limit at " + p;
                    return false;
                }
                if (loaded.add(ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4)))
                    world.getChunk(p.getX() >> 4, p.getZ() >> 4);
            }
            // Reserved plots cannot swallow existing buildings. Trees and
            // leaves in the actual building/road footprint are also protected.
            for (var entry : blocks.entrySet()) {
                BlockState old = world.getBlockState(entry.getKey());
                // Generated trees may be cleared for construction. Preserve
                // buildings, containers and all other non-replaceable blocks.
                if (!old.equals(entry.getValue()) && !BuyanGenerator.canReplace(old)) {
                    collisionReason = "protected block " + old + " at " + entry.getKey();
                    return false;
                }
            }
            for (int[] lot : LOTS)
                for (int x = lot[0]; x <= lot[2]; x += 3)
                    for (int z = lot[1]; z <= lot[3]; z += 3) {
                        int ground = top(x, z);
                        if (ground == Integer.MIN_VALUE
                            || ground <= site.waterLevel() + 5) {
                            collisionReason = "future lot outside safe land at " + x + "," + z;
                            return false;
                        }
                        BlockPos p = pos(x, ground + 1, z);
                        BlockState old = world.getBlockState(p);
                        if (!BuyanGenerator.canReplace(old) && !old.isAir()) {
                            collisionReason = "future lot occupied by " + old + " at " + p;
                            return false;
                        }
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
            for (var entry : displayStands.entrySet()) {
                ArmorStand stand = EntityType.ARMOR_STAND.create(world);
                if (stand == null) continue;
                BlockPos point = entry.getKey();
                stand.setPos(point.getX() + 0.5, point.getY(), point.getZ() + 0.5);
                switch (entry.getValue()) {
                    case 0 -> {
                        stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                        stand.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                    }
                    case 1 -> {
                        stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                        stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    }
                    default -> {
                        stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    }
                }
                if (!world.addFreshEntity(stand))
                    LogUtils.getLogger().warn("Craft armory display did not spawn at {}", point);
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

        // The full quarter spans 74 blocks beyond its origin. Starting at
        // radius 108 placed the distant plots beyond the narrow coast on
        // valid seeds. Search closer to the central square while keeping the
        // same shoreline, terrain-gradient and collision protection.
        // The connecting road begins at radius 55 on every candidate.
        int candidates = 0, terrainMatches = 0, invalidBlueprints = 0;
        int occupiedSites = 0, invalidGrades = 0;
        String lastRejection = "no terrain matches";
        for (int radius = 60; radius <= 180; radius += 6)
            for (Rotation turn : Rotation.values())
                for (int lateral = 0; lateral <= 60; lateral += 4)
                    for (int sign : lateral == 0 ? new int[]{1} : new int[]{-1, 1}) {
                        candidates++;
                        Plan p = new Plan(world, site, turn, radius, lateral * sign, world.getSeed());
                        if (!p.terrainFit()) continue;
                        terrainMatches++;
                        try {
                            p.build();
                            if (!p.checkDesign()) {
                                invalidBlueprints++;
                                lastRejection = "blueprint " + p.ox + "," + p.oz;
                                continue;
                            }
                            if (!p.worldAvailable()) {
                                occupiedSites++;
                                lastRejection = p.collisionReason;
                                continue;
                            }
                        } catch (IllegalStateException badSite) {
                            // Invalid slope/approach/collision at this hillside:
                            // never apply a partial candidate to the world.
                            invalidGrades++;
                            lastRejection = badSite.getMessage();
                            continue;
                        }
                        p.commit();
                        record.mark(key, new BlockPos(p.ox, p.firstFloor, p.oz),
                            turn, p.armoryFloor);
                        LogUtils.getLogger().info(
                            "Buyan craft complex completed at {},{}: forge Y={}, armory Y={}, orientation {}",
                            p.ox, p.oz, p.firstFloor, p.armoryFloor, turn);
                        return true;
                    }
        LogUtils.getLogger().error(
            "Buyan forge+armory complex: no viable site among {} candidates, {} terrain matches, "
                + "{} invalid blueprints, {} occupied sites, {} grade errors; last rejection: {}",
            candidates, terrainMatches, invalidBlueprints, occupiedSites,
            invalidGrades, lastRejection);
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
                "Кузнечно-оружейный комплекс: безопасный участок не найден. Мир не изменён."));
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
            source.sendSuccess(() -> Component.literal("Кузнечно-оружейный комплекс не построен."), false);
            return 0;
        }
        BlockPos p = BlockPos.of(stored);
        Rotation orientation = Rotation.values()[Math.floorMod(
            record.rotations.getOrDefault(siteKey(site), 0), Rotation.values().length)];
        // Actual entrance (forge local X=-29,Z=12) rather than the district origin.
        BlockPos entrance = switch (orientation) {
            case NONE -> new BlockPos(p.getX() - 29, p.getY() + 1, p.getZ() + 12);
            case CLOCKWISE_90 -> new BlockPos(p.getX() - 12, p.getY() + 1, p.getZ() - 29);
            case CLOCKWISE_180 -> new BlockPos(p.getX() + 29, p.getY() + 1, p.getZ() - 12);
            case COUNTERCLOCKWISE_90 -> new BlockPos(p.getX() + 12, p.getY() + 1, p.getZ() + 29);
        };
        Integer armoryY = record.armoryFloors.get(siteKey(site));
        if (armoryY == null) {
            source.sendSuccess(() -> Component.literal(
                "Старая кузница готова (без оружейной). Вход: "
                    + entrance.getX() + "," + entrance.getY() + "," + entrance.getZ()
                    + ". Сохранение не изменено автоматически."), false);
            return 1;
        }
        BlockPos armoryEntry = switch (orientation) {
            case NONE -> new BlockPos(p.getX() + 13, armoryY + 1, p.getZ() + 12);
            case CLOCKWISE_90 -> new BlockPos(p.getX() - 12, armoryY + 1, p.getZ() + 13);
            case CLOCKWISE_180 -> new BlockPos(p.getX() - 13, armoryY + 1, p.getZ() - 12);
            case COUNTERCLOCKWISE_90 -> new BlockPos(p.getX() + 12, armoryY + 1, p.getZ() - 13);
        };
        source.sendSuccess(() -> Component.literal(
            "Кузница: X=" + entrance.getX() + " Y=" + entrance.getY()
            + " Z=" + entrance.getZ() + "; оружейная: X="
            + armoryEntry.getX() + " Y=" + armoryEntry.getY()
            + " Z=" + armoryEntry.getZ()
            + "; рабочий двор между ними; ещё шесть участков зарезервировано."), false);
        return 1;
    }


    /**
     * Deterministic fixture selection uses the production terrain and road
     * constraints, but never writes to the GameTest world's blocks.
     */
    private static Plan findBlueprintFixture(ServerLevel world, long seed) {
        BuyanState.Site site = new BuyanState.Site(0, 0, 350, 63);
        int eligible = 0, rejected = 0;
        for (int radius = 60; radius <= 180; radius += 6)
            for (Rotation turn : Rotation.values())
                for (int lateral = 0; lateral <= 60; lateral += 4)
                    for (int sign : lateral == 0 ? new int[]{1} : new int[]{-1, 1}) {
                        Plan p = new Plan(world, site, turn, radius,
                            lateral * sign, seed);
                        if (!p.terrainFit()) continue;
                        eligible++;
                        try {
                            p.build();
                            if (!p.checkDesign()) {
                                rejected++;
                                if (rejected <= 8)
                                    LogUtils.getLogger().warn(
                                        "Buyan craft fixture seed {} at {},{} rotation {} failed design validation",
                                        seed, p.ox, p.oz, turn);
                                continue;
                            }
                        } catch (IllegalStateException invalidSite) {
                            rejected++;
                            if (rejected <= 8)
                                LogUtils.getLogger().warn(
                                    "Buyan craft fixture seed {} at {},{} rotation {} rejected during build: {}",
                                    seed, p.ox, p.oz, turn, invalidSite.getMessage());
                            continue;
                        }
                        LogUtils.getLogger().info(
                            "Buyan craft complex blueprint: seed {} origin {},{} rotation {}",
                            seed, p.ox, p.oz, turn);
                        return p;
                    }
        throw new IllegalStateException(
            "Buyan forge+armory fixture has no site for seed " + seed
                + "; terrain candidates " + eligible + "; rejected " + rejected);
    }

    private static void mustCatchBlueprintDamage(Plan p, BlockPos point,
                                                  String description) {
        BlockState old = p.blocks.put(point, AIR);
        if (old == null || old.isAir())
            throw new IllegalStateException(
                "Regression fixture tried to remove nonexistent " + description + " at " + point);
        try {
            if (p.checkDesign())
                throw new IllegalStateException(
                    "Blueprint validator missed damage: " + description + " at " + point);
        } finally {
            p.blocks.put(point, old);
        }
        if (!p.checkDesign())
            throw new IllegalStateException("Blueprint not restored after " + description);
    }

    /** No block edits: the same validations run against a staged full complex. */
    public static void verifyBlueprintFixture(ServerLevel world) {
        Plan p = findBlueprintFixture(world, 9083133736688572422L);
        if (p.chests.size() != 8 || p.displayStands.size() != 3)
            throw new IllegalStateException("Buyan armory loot or displays missing");
        mustCatchBlueprintDamage(p, p.pos(-32, p.roofY(4), 8), "forge roof");
        mustCatchBlueprintDamage(p, p.pos(16, p.armoryRoofY(10), 10), "armory roof");
        mustCatchBlueprintDamage(p, p.pos(-24, p.firstFloor + 1, 11), "forge stair");
        mustCatchBlueprintDamage(p, p.pos(22, p.armoryFloor + 1, 10), "armory stair");
        mustCatchBlueprintDamage(p, p.pos(12, p.armoryFloor + 8, 2), "armory bed");
        mustCatchBlueprintDamage(p, p.pos(2,
            p.roadHeights[16 - p.roadStart], 16), "street junction");
    }

    /** The exact crash seed from 17 November 2026. */
    public static void verifyCrashSeedFixture(ServerLevel world) {
        Plan p = findBlueprintFixture(world, 7670812339892729374L);
        if (!BuyanGenerator.canReplace(Blocks.OAK_LOG.defaultBlockState())
            || !BuyanGenerator.canReplace(Blocks.OAK_LEAVES.defaultBlockState())
            || BuyanGenerator.canReplace(Blocks.OAK_PLANKS.defaultBlockState())
            || BuyanGenerator.canReplace(Blocks.CHEST.defaultBlockState()))
            throw new IllegalStateException(
                "Crash seed: natural trees and protected construction are misclassified");
        if (p.chests.size() != 8 || p.armoryFloor <= 63)
            throw new IllegalStateException("Crash seed: smithing complex incomplete");
        // November 17 integrated-client crash: terrainFit rejected all 868
        // original candidates for this exact seed before world construction.
        Plan clientCrash = findBlueprintFixture(world, -9047918643626153269L);
        if (clientCrash.chests.size() != 8 || !clientCrash.checkDesign())
            throw new IllegalStateException("Integrated-client crash seed blueprint incomplete");
    }

    public static void verifyReportedSeedSite(ServerLevel world) {
        Plan p = findBlueprintFixture(world, 9083133736688572422L);
        if (!p.checkDesign())
            throw new IllegalStateException("Original site blueprint did not validate");
    }

}
