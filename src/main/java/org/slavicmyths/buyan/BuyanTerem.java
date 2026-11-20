package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Central white-stone palace of Buyan, copied from the approved make_terem.py
 * block model. In the prototype (x, y, z), z is vertical. Here (x, v, h)
 * means horizontal x, horizontal prototype y (Minecraft Z), and vertical h
 * (Minecraft Y). All cuboids use exclusive upper bounds, as in Python.
 *
 * A fresh Slavic Oceans world is the only automatic construction path.
 * Ordinary existing saves are never retrofitted.
 */
public final class BuyanTerem {
    private static final String SAVE_ID = "slavicmyths_buyan_terem_2622";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WHITE = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState LIGHT = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState SLATE = Blocks.POLISHED_ANDESITE.defaultBlockState();
    private static final BlockState BLUE = Blocks.BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState BLUE2 = Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState RED = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState RED2 = Blocks.RED_GLAZED_TERRACOTTA.defaultBlockState();
    private static final BlockState GOLD = Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState();
    private static final BlockState WOOD = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState BEAM = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
    private static final BlockState GLASS = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
    private static final BlockState FLOOR = Blocks.SPRUCE_PLANKS.defaultBlockState();

    private BuyanTerem() {}

    private static final class Record extends SavedData {
        final Map<Long, Long> built = new LinkedHashMap<>();

        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), SAVE_ID);
        }

        static Record load(CompoundTag tag, HolderLookup.Provider registries) {
            Record result = new Record();
            long[] keys = tag.getLongArray("Keys");
            long[] values = tag.getLongArray("Centers");
            for (int i = 0; i < Math.min(keys.length, values.length); i++) {
                result.built.put(keys[i], values[i]);
            }
            return result;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            long[] keys = new long[built.size()];
            long[] values = new long[built.size()];
            int i = 0;
            for (var entry : built.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                i++;
            }
            tag.putLongArray("Keys", keys);
            tag.putLongArray("Centers", values);
            return tag;
        }

        void finish(long key, BlockPos anchor) {
            built.put(key, anchor.asLong());
            setDirty();
        }
    }

    private static long key(BuyanState.Site site) {
        return new BlockPos(site.x(), 0, site.z()).asLong();
    }

    /**
     * The model is a complete 67x41x52 volume (x -33..33, v -15..25, h 0..51).
     * Its occupied core is on the existing center clearing, slightly north of
     * the reserved central square. The original terrain function determines
     * each base height; only shallow support under the actual structure is made.
     */
    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final int ox, oz, base;
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        final Map<BlockPos, String> loot = new LinkedHashMap<>();

        Plan(ServerLevel world, BuyanState.Site site, int ox, int oz, int base) {
            this.world = world;
            this.site = site;
            this.ox = ox;
            this.oz = oz;
            this.base = base;
        }

        BlockPos pos(int x, int v, int h) {
            return new BlockPos(ox + x, base + h, oz + v);
        }

        int ground(int x, int v) {
            return BuyanNature.top(world.getSeed(), site.x(), site.z(),
                site.radius(), ox + x, oz + v, site.waterLevel());
        }

        void at(int x, int v, int h, BlockState state) {
            blocks.put(pos(x, v, h), state);
        }

        void box(int x0, int x1, int v0, int v1, int h0, int h1, BlockState state) {
            for (int x = x0; x < x1; x++)
                for (int v = v0; v < v1; v++)
                    for (int h = h0; h < h1; h++)
                        at(x, v, h, state);
        }

        void border(int x0, int x1, int v0, int v1, int h0, int h1, BlockState state) {
            for (int x = x0; x < x1; x++)
                for (int v = v0; v < v1; v++)
                    for (int h = h0; h < h1; h++)
                        if (x == x0 || x == x1 - 1 || v == v0 || v == v1 - 1 || h == h0)
                            at(x, v, h, state);
        }

        void window(int x, int h, int width) {
            for (int dx = 0; dx < width; dx++)
                for (int dy = 0; dy < 4; dy++)
                    at(x + dx, 0, h + dy,
                        (dy == 1 || dy == 2) && dx > 0 && dx < width - 1 ? GLASS : BEAM);
            for (int dx = -1; dx <= width; dx++) {
                at(x + dx, -1, h - 1, STONE);
                at(x + dx, -1, h + 4, GOLD);
            }
            for (int dy = 0; dy < 4; dy++) {
                at(x - 1, -1, h + dy, LIGHT);
                at(x + width, -1, h + dy, LIGHT);
            }
            at(x + width / 2, -1, h + 2, Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
        }

        void roof(int x0, int x1, int v0, int v1, int h0, int rise,
                  BlockState field, BlockState trim) {
            for (int x = x0; x < x1; x++) {
                int edge = Math.min(x - x0, x1 - 1 - x);
                int h = h0 + Math.min(rise, Math.max(0, edge));
                for (int v = v0; v < v1; v++) {
                    at(x, v, h, v == v0 || v == v1 - 1 || x == x0 || x == x1 - 1
                        ? trim : field);
                    if (v == v0 || v == v1 - 1)
                        for (int yy = h0; yy < h; yy++)
                            at(x, v, yy, WHITE);
                }
                if (Math.floorMod(x, 3) == 0) at(x, v0, h + 1, GOLD);
            }
            // The source has full block surfaces. Add correctly oriented
            // stepped eaves OUTSIDE these surfaces, rather than replacing
            // their sealing blocks with half-height stairs.
            for (int x = x0 + 1; x < x1 - 1; x++) {
                BlockState eave = Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, x < (x0 + x1) / 2
                        ? Direction.EAST : Direction.WEST);
                int h = h0 + Math.min(rise, Math.min(x - x0, x1 - 1 - x));
                at(x, v0 - 1, h, eave);
                at(x, v1, h, eave);
            }
        }

        void door(int x, int v, int h, DoorHingeSide hinge) {
            BlockState lower = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HINGE, hinge)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            at(x, v, h, lower);
            at(x, v, h + 1, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }

        void internalDoor(int x, int v, int h) {
            at(x, v, h, AIR);
            at(x, v, h + 1, AIR);
            at(x, v, h + 2, AIR);
            // Interior openings are intentionally unobstructed three-high arches.
        }

        void bed(int x, int v, int h, boolean red) {
            BlockState foot = (red ? Blocks.RED_BED : Blocks.BLUE_BED).defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
            at(x, v, h, foot);
            at(x, v + 1, h, foot.setValue(BedBlock.PART, BedPart.HEAD));
        }

        void chest(int x, int v, int h, String table) {
            at(x, v, h, Blocks.CHEST.defaultBlockState());
            loot.put(pos(x, v, h), table);
        }

        void fixture(int x, int v, int h) {
            at(x, v, h, Blocks.LANTERN.defaultBlockState());
        }

        void ceilingLight(int x, int v, int ceiling) {
            // Attach to an existing full timber deck. All blocks are inside
            // the room and the lantern is suspended above walkable headroom.
            if (!blocks.getOrDefault(pos(x,v,ceiling),AIR).is(Blocks.SPRUCE_PLANKS))
                throw new IllegalStateException("No ceiling for hall lighting");
            at(x,v,ceiling-1,Blocks.CHAIN.defaultBlockState());
            at(x,v,ceiling-2,Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING,true));
        }

        void decoratedColumn(int x,int v) {
            at(x,v,3,Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            for(int h=4;h<=7;h++)
                at(x,v,h,Blocks.QUARTZ_PILLAR.defaultBlockState());
            at(x,v,8,Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        }

        void chandelier(int x, int v, int ceiling) {
            // Secured to the actual upper-floor deck rather than floating in air.
            if (!blocks.getOrDefault(pos(x, v, ceiling), AIR).is(Blocks.SPRUCE_PLANKS))
                throw new IllegalStateException("Chandelier ceiling missing");
            for (int h = ceiling - 2; h < ceiling; h++)
                at(x, v, h, Blocks.CHAIN.defaultBlockState());
            BlockState hanging = Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true);
            at(x, v, ceiling - 3, hanging);
            for (Direction axis : new Direction[]{Direction.EAST,Direction.WEST,
                Direction.NORTH,Direction.SOUTH}) {
                int dx=axis.getStepX(), dv=axis.getStepZ();
                BlockState link=Blocks.CHAIN.defaultBlockState().setValue(
                    ChainBlock.AXIS,dx!=0?Direction.Axis.X:Direction.Axis.Z);
                at(x+dx,v+dv,ceiling-2,link);
                at(x+2*dx,v+2*dv,ceiling-2,link);
                at(x+2*dx,v+2*dv,ceiling-3,hanging);
            }
        }

        void foundation() {
            // The 45x29 plinth is precisely the Python 0..2 stone base.
            // Support only actual occupied building columns; no island-wide pad.
            for (int x = -33; x <= 33; x++)
                for (int v = -3; v <= 25; v++) {
                    boolean shell = x >= -23 && x <= 23
                        || x >= -32 && x <= -23 && v <= 21 && v >= -1
                        || x >= 24 && x <= 32 && v <= 21 && v >= -1;
                    if (!shell) continue;
                    int g = ground(x, v);
                    for (int h = g + 1 - base; h < 2; h++)
                        at(x, v, h, h < 0 ? STONE : SLATE);
                }
            box(-23, 24, -3, 26, 0, 2, STONE);
        }

        void constructShell() {
            // Remove only vegetation in the palace footprint, from its own
            // foundations upward. All original world blocks are preflighted.
            box(-34, 35, -8, 27, 2, 53, AIR);
            foundation();

            int[][] bodies = {{-22, 23, 0, 23}, {-30, -18, 3, 22}, {19, 31, 3, 22}};
            for (int[] body : bodies) {
                int x0 = body[0], x1 = body[1], v0 = body[2], v1 = body[3];
                border(x0, x1, v0, v1, 2, 26, WHITE);
                box(x0, x1, v0, v1, 9, 10, FLOOR);
                box(x0, x1, v0, v1, 17, 18, FLOOR);
                for (int h : new int[]{9, 17, 25}) {
                    box(x0 - 1, x1 + 1, v0 - 1, v0, h, h + 1, RED);
                    box(x0 - 1, x1 + 1, v1, v1 + 1, h, h + 1, RED);
                }
            }
            border(-9, 10, -4, 11, 18, 30, WHITE);
            border(-6, 7, 4, 19, 27, 38, WHITE);

            for (int[] tower : new int[][]{{-32, -23}, {24, 33}}) {
                int x0 = tower[0], x1 = tower[1];
                border(x0, x1, -1, 9, 3, 29, WHITE);
                for (int h : new int[]{9, 17, 25})
                    box(x0 - 1, x1 + 1, -2, 10, h, h + 1, RED);
                for (int h : new int[]{6, 13, 21}) {
                    for (int x = x0 + 3; x <= x0 + 4; x++)
                        for (int yy = h; yy < h + 3; yy++)
                            at(x, -2, yy, GLASS);
                    box(x0 + 2, x0 + 6, -3, -2, h - 1, h, GOLD);
                }
            }
            for (int x = -21; x < 23; x += 7) {
                for (int h = 2; h < 25; h++)
                    if (h != 9 && h != 17)
                        at(x, -1, h, Blocks.QUARTZ_PILLAR.defaultBlockState());
                for (int h : new int[]{8, 16, 24}) at(x, -2, h, GOLD);
            }
            for (int x : new int[]{-29, -24, 25, 30})
                for (int h = 3; h < 27; h++)
                    if (h != 9 && h != 17 && h != 25)
                        at(x, -2, h, Blocks.POLISHED_DIORITE.defaultBlockState());
            for (int h : new int[]{4, 12, 20}) {
                for (int x : new int[]{-19, -12, 11, 18}) window(x, h, 3);
            }
            for (int x : new int[]{-7, 5}) window(x, 20, 3);
            for (int x : new int[]{-22, 22, -31, 31})
                for (int v : new int[]{8, 16})
                    for (int h : new int[]{5, 13, 21})
                        for (int vv = v; vv < v + 3; vv++)
                            for (int yy = h; yy < h + 3; yy++)
                                at(x, vv, yy, vv == v + 1 ? GLASS : BEAM);

            // Source portal shape, but the solid "door" panel is replaced
            // later with real double doors and a clear entry.
            for (int x = -3; x <= 3; x++)
                for (int h = 2; h <= 8; h++)
                    at(x, -1, h, LIGHT);
            for (int x = -5; x <= 5; x++) at(x, -2, 10, GOLD);
            for (int h = 3; h < 10; h++) {
                at(-5, -2, h, LIGHT);
                at(5, -2, h, LIGHT);
            }
            for (int x : new int[]{-2, 2})
                for (int h = 3; h < 8; h++) at(x, -2, h, BEAM);
            for (int x : new int[]{-8, 8})
                for (int h = 9; h < 18; h++) at(x, -6, h, LIGHT);
            box(-9, 10, -7, -1, 17, 18, SLATE);
            for (int x = -9; x < 10; x++) {
                at(x, -8, 18, BEAM);
                if ((x & 1) == 0) at(x, -8, 19, GOLD);
                at(x, -7, 25, RED);
            }
            for (int[] wing : new int[][]{{-23, -10}, {11, 24}}) {
                box(wing[0], wing[1], -4, -1, 17, 18, FLOOR);
                for (int x = wing[0]; x < wing[1]; x++) {
                    at(x, -5, 18, BEAM);
                    at(x, -5, 19, Math.floorMod(x, 3) == 0 ? GOLD : BEAM);
                }
            }
            for (int h : new int[]{10, 18, 26})
                for (int x = -22; x <= 22; x++) {
                    int r = Math.floorMod(x, 5);
                    if (r == 0 || r == 2) at(x, -2, h, RED);
                    else if (r == 3) at(x, -2, h, GOLD);
                }
            for (int x = -20; x < 22; x += 8)
                for (int h : new int[]{7, 15, 23}) {
                    at(x, -2, h, RED2);
                    at(x + 1, -2, h + 1, RED2);
                    at(x + 2, -2, h, RED2);
                }

            roof(-23, 24, -1, 24, 26, 10, BLUE, RED);
            roof(-30, -18, 2, 23, 26, 5, BLUE2, RED);
            roof(19, 31, 2, 23, 26, 5, BLUE2, RED);
            roof(-7, 8, 3, 20, 38, 8, RED, BLUE);
            for (int[] tower : new int[][]{{-33, -22}, {23, 34}}) {
                // Seal the one-block gap left between the reference tower
                // walls (up to h=28) and its first pyramid row (h=30).
                box(tower[0] + 1, tower[1] - 1, -1, 9, 29, 30, WHITE);
                for (int h = 0; h < 6; h++)
                    box(tower[0] + h, tower[1] - h,
                        -2 + h, 10 - h, 30 + h, 31 + h,
                        h % 2 == 0 ? BLUE : RED);
                for (int h = 36; h <= 40; h++)
                    at((tower[0] + tower[1]) / 2, 4, h, GOLD);
            }
            for (int h = 46; h < 52; h++) at(0, 11, h, GOLD);
            for (int x = 1; x < 7; x++) at(x, 11, 50, RED);
            for (int x = 1; x < 5; x++) at(x, 11, 49, RED);
            // Model flowerboxes and painted front lamps.
            for (int x : new int[]{-19, -12, 11, 18})
                for (int h : new int[]{4, 12}) {
                    box(x, x + 3, -3, -2, h - 1, h, WOOD);
                    for (int xx = x; xx < x + 3; xx++)
                        at(xx, -3, h, (xx & 1) == 0
                            ? Blocks.MOSS_BLOCK.defaultBlockState()
                            : Blocks.PINK_CONCRETE.defaultBlockState());
                }
            for (int x : new int[]{-10, 10})
                for (int h : new int[]{6, 14}) {
                    at(x, -3, h - 1, Blocks.STONE_BRICK_WALL.defaultBlockState());
                    fixture(x, -3, h);
                }
        }

        void towerInteriors() {
            // The prototype's full red floor bands are continuous. Retain
            // their outer trim but make walkable timber decks inside both
            // towers, with aligned ladder openings and access from the wings.
            for (int[] tower : new int[][]{{-32, -23}, {24, 33}}) {
                int x0 = tower[0], x1 = tower[1];
                for (int h : new int[]{9, 17, 25})
                    box(x0 + 1, x1 - 1, 0, 8, h, h + 1, FLOOR);
                for (int h = 4; h <= 26; h++) {
                    at(x0 + 1, 0, h, Blocks.LADDER.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LadderBlock.FACING,
                            Direction.SOUTH));
                    at(x0 + 1, -1, h, WHITE);
                }
                for (int h : new int[]{4, 10, 18}) {
                    int passageX = x0 < 0 ? -30 : 30;
                    for (int yy = h; yy < h + 3; yy++)
                        at(passageX, 4, yy, AIR);
                }
                fixture(x0 + 4, 5, 10);
                fixture(x0 + 4, 5, 18);
                fixture(x0 + 4, 5, 26);
            }
            // Join the overlapping wings to the main body's corridors at
            // the correct floor heights, instead of enclosing dead rooms.
            for (int x : new int[]{-22, 22})
                for (int h : new int[]{3, 10, 18}) {
                    internalDoor(x, 12, h);
                    internalDoor(x, 13, h);
                }
        }

        void stairsAndEntrance() {
            // Height-adjusted functional entrance: the prototype's decorative
            // six-layer steps rose ABOVE its first floor. These real stairs
            // rise just two blocks to meet the h=2 main floor.
            for (int v = -15; v <= -4; v++) {
                int step = v <= -12 ? 0 : v <= -9 ? 1 : 2;
                for (int x = -7; x <= 7; x++) {
                    int g = ground(x, v);
                    for (int h = g + 1 - base; h < step; h++)
                        at(x, v, h, STONE);
                    if (v == -12 || v == -9)
                        at(x, v, step, Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
                    else at(x, v, step, SLATE);
                }
                for (int x : new int[]{-7, 7})
                    at(x, v, step + 1, LIGHT);
            }
            box(-3, 4, -3, 1, 2, 3, SLATE);
            // Accessible 2-wide double entrance with opposing hinges.
            for (int v = -2; v <= 0; v++)
                for (int x = -1; x <= 0; x++)
                    for (int h = 3; h <= 5; h++) at(x, v, h, AIR);
            door(-1, 0, 3, DoorHingeSide.LEFT);
            door(0, 0, 3, DoorHingeSide.RIGHT);
            for (int x = -1; x <= 0; x++) at(x, 0, 5, LIGHT);

            // Real stairwell A, floor 1 (h=2) to floor 2 (h=9).
            for (int i = 0; i < 7; i++) {
                int v = 3 + i, h = 3 + i;
                for (int x = 12; x <= 14; x++) {
                    for (int clearance = h + 1; clearance <= h + 3; clearance++)
                        at(x, v, clearance, AIR);
                    at(x, v, h, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.SOUTH));
                    if (h > 3) at(x, v, h - 1, BEAM);
                }
            }
            // Real stairwell B, floor 2 (h=9) to floor 3 (h=17).
            for (int i = 0; i < 8; i++) {
                int v = 3 + i, h = 10 + i;
                for (int x = -14; x <= -12; x++) {
                    for (int clearance = h + 1; clearance <= h + 3; clearance++)
                        at(x, v, clearance, AIR);
                    at(x, v, h, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.SOUTH));
                    if (h > 10) at(x, v, h - 1, BEAM);
                }
            }
            // Dedicated second-floor balcony and third-floor terraces. Every
            // threshold shares the adjacent interior floor elevation.
            box(-8, 9, -5, 0, 9, 10, WOOD);
            for (int x = -8; x <= 8; x++) {
                at(x, -6, 10, BEAM);
                at(x, -6, 11, Blocks.SPRUCE_FENCE.defaultBlockState());
                if (Math.floorMod(x,4) == 0) at(x, -6, 12, GOLD);
            }
            for (int v = -5; v < 0; v++) {
                at(-8, v, 11, Blocks.SPRUCE_FENCE.defaultBlockState());
                at(8, v, 11, Blocks.SPRUCE_FENCE.defaultBlockState());
            }
            for (int h : new int[]{10, 11}) {
                for (int x : new int[]{-1, 0}) at(x, 0, h, AIR);
            }
            door(-1, 0, 10, DoorHingeSide.LEFT);
            door(0, 0, 10, DoorHingeSide.RIGHT);
            for (int x = -1; x <= 0; x++) at(x, 0, 12, LIGHT);
            // Third floor center and wing balconies use the prototype h=17 deck.
            for (int[] range : new int[][]{{-18, -17}, {16, 17}})
                for (int x = range[0]; x <= range[1]; x++)
                    for (int v = -1; v <= 0; v++)
                        for (int h = 18; h <= 19; h++) at(x, v, h, AIR);
            door(-18, 0, 18, DoorHingeSide.LEFT);
            door(-17, 0, 18, DoorHingeSide.RIGHT);
            door(16, 0, 18, DoorHingeSide.LEFT);
            door(17, 0, 18, DoorHingeSide.RIGHT);
            for (int x : new int[]{-18,-17,16,17}) at(x, 0, 20, LIGHT);
            for (int x = -1; x <= 0; x++) {
                for (int v = -5; v <= -3; v++)
                    for (int h = 19; h <= 21; h++) at(x, v, h, AIR);
                at(x, -5, 18, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH));
            }
            door(-1, -4, 19, DoorHingeSide.LEFT);
            door(0, -4, 19, DoorHingeSide.RIGHT);
            for (int x = -1; x <= 0; x++) at(x, -4, 21, LIGHT);
        }

        void roomWall(int x0, int x1, int v0, int v1, int h0, int h1) {
            box(x0, x1, v0, v1, h0, h1, WHITE);
        }

        void furnishings() {
            // Ground floor: central entrance/reception, guards and service rooms.
            roomWall(-10, -9, 8, 22, 3, 9);
            roomWall(9, 10, 11, 22, 3, 9);
            internalDoor(-10, 11, 3);
            internalDoor(9, 14, 3);
            roomWall(-21, -10, 16, 17, 3, 9);
            internalDoor(-15, 16, 3);
            box(-4, 5, 14, 17, 3, 4, WOOD);
            for (int x = -5; x <= 5; x += 2) {
                at(x, 13, 3, Blocks.SPRUCE_STAIRS.defaultBlockState());
                at(x, 17, 3, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH));
            }
            at(-19, 18, 3, Blocks.BARREL.defaultBlockState());
            at(-18, 18, 3, Blocks.FURNACE.defaultBlockState());
            at(-17, 18, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
            chest(-16, 18, 3, "buyan_terem_household");
            chandelier(0, 7, 9);
            chandelier(0, 20, 9);
            // Clear reception floor: hang lanterns from the deck above
            // rather than scattering them at ankle height.
            for (int v : new int[]{5,12,19})
                for (int x : new int[]{-17,17})
                    ceilingLight(x,v,9);
            for(int x:new int[]{-7,7})
                for(int v:new int[]{5,19})
                    decoratedColumn(x,v);
            for(int x:new int[]{-5,-4,4,5})
                at(x,6,3,Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING,x<0?Direction.EAST:Direction.WEST));
            // Second floor: prince's council, two libraries and the accessible loggia.
            roomWall(-10, -9, 12, 22, 10, 17);
            roomWall(9, 10, 12, 22, 10, 17);
            internalDoor(-10, 14, 10);
            internalDoor(9, 14, 10);
            box(-4, 5, 13, 16, 10, 11, WOOD);
            for (int x = -5; x <= 5; x += 2) {
                at(x, 12, 10, Blocks.SPRUCE_STAIRS.defaultBlockState());
                at(x, 16, 10, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH));
            }
            for (int v = 13; v <= 20; v += 2) {
                at(-19, v, 10, Blocks.BOOKSHELF.defaultBlockState());
                at(19, v, 10, Blocks.BOOKSHELF.defaultBlockState());
            }
            chest(17, 19, 10, "buyan_terem_household");
            chandelier(0, 7, 17);
            chandelier(0, 20, 17);
            for (int v : new int[]{5,12,19})
                for (int x : new int[]{-17,17})
                    ceilingLight(x,v,17);
            // Third floor: royal bedchamber, guest room, strongroom and upper hall.
            roomWall(-9, -8, 4, 22, 18, 25);
            roomWall(8, 9, 4, 22, 18, 25);
            internalDoor(-9, 13, 18);
            internalDoor(8, 13, 18);
            roomWall(-21, -9, 17, 18, 18, 25);
            internalDoor(-15, 17, 18);
            bed(-16, 10, 18, true);
            bed(15, 10, 18, false);
            chest(-17, 19, 18, "buyan_terem_treasury");
            chest(-15, 19, 18, "buyan_terem_treasury");
            at(-18, 15, 18, Blocks.BOOKSHELF.defaultBlockState());
            at(18, 15, 18, Blocks.BOOKSHELF.defaultBlockState());
            box(-3, 4, 14, 16, 18, 19, WOOD);
            // Third floor retains free passages, with warm lights safely
            // above rather than on the walkable floor.
            for(int x:new int[]{-13,0,13}) {
                at(x,20,24,BEAM);
                at(x,20,23,Blocks.CHAIN.defaultBlockState());
                at(x,20,22,Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING,true));
            }
            // Fully accessible central keep via a backed ladder and loft floor.
            box(-5, 6, 5, 18, 27, 28, FLOOR);
            for (int h = 18; h <= 29; h++) {
                at(0, 4, h, WHITE);
                at(0, 5, h, Blocks.LADDER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LadderBlock.FACING,
                        Direction.SOUTH));
                at(0, 5, h + 1, AIR);
            }
            at(0, 5, 27, Blocks.LADDER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
            fixture(-3, 9, 28);
            fixture(3, 15, 28);
        }

        void approach() {
            // Short link to a future Alatyr square, NOT the square itself.
            // The natural surface remains otherwise untouched.
            for (int v = -24; v <= -16; v++)
                for (int x = -2; x <= 2; x++) {
                    int g = ground(x, v);
                    if (g == Integer.MIN_VALUE) continue;
                    at(x, v, g - base, x == 0 ? SLATE : STONE);
                    at(x, v, g + 1 - base, AIR);
                    at(x, v, g + 2 - base, AIR);
                }
        }

        void build() {
            constructShell();
            towerInteriors();
            stairsAndEntrance();
            furnishings();
            // The south avenue already provides the approach for the relocated
            // palace. Never overwrite its nine-wide quartz pavement.
            if (oz - site.z() < 60) approach();
        }

        boolean preflight() {
            for (var entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                if (p.getY() <= world.getMinBuildHeight()
                    || p.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(p))
                    return false;
            }
            // Explicitly load all intersecting chunks before the first write.
            java.util.Set<Long> chunks = new java.util.HashSet<>();
            for (BlockPos p : blocks.keySet())
                if (chunks.add(ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4)))
                    world.getChunk(p.getX() >> 4, p.getZ() >> 4);
            for (var entry : blocks.entrySet()) {
                BlockState old = world.getBlockState(entry.getKey());
                if (!old.equals(entry.getValue()) && !BuyanGenerator.canReplace(old)
                    && !(oz - site.z() > 60 && old.is(Blocks.BLUE_TERRACOTTA))) {
                    LogUtils.getLogger().warn("Buyan terem protected block at {}: {}",
                        entry.getKey(), old);
                    return false;
                }
            }
            return true;
        }

        void commit() {
            List<Map.Entry<BlockPos, BlockState>> changes = new ArrayList<>();
            Map<BlockPos, BlockState> original = new LinkedHashMap<>();
            for (var entry : blocks.entrySet()) {
                BlockState old = world.getBlockState(entry.getKey());
                if (!old.equals(entry.getValue())) {
                    original.put(entry.getKey(), old);
                    changes.add(entry);
                }
            }
            try {
                for (var entry : changes)
                    if (!world.setBlock(entry.getKey(), entry.getValue(), 2))
                        throw new IllegalStateException("Unable to set " + entry.getKey());
                for (var entry : loot.entrySet()) {
                    if (!(world.getBlockEntity(entry.getKey())
                        instanceof RandomizableContainerBlockEntity chest))
                        throw new IllegalStateException("Missing loot chest at " + entry.getKey());
                    ResourceKey<LootTable> table = ResourceKey.create(
                        Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(
                            "slavicmyths", "chests/" + entry.getValue()));
                    chest.setLootTable(table, world.getSeed() ^ entry.getKey().asLong());
                    chest.setChanged();
                }
            } catch (RuntimeException error) {
                // The only replaced original states were validated natural
                // blocks with no block entities. Undo only this plan's writes.
                for (int i = changes.size() - 1; i >= 0; i--) {
                    BlockPos p = changes.get(i).getKey();
                    world.setBlock(p, original.get(p), 2);
                }
                throw error;
            }
        }

        boolean geometryCheck() {
            for (int h : new int[]{2, 9, 17}) {
                if (!blocks.getOrDefault(pos(0, 12, h), AIR).is(Blocks.SPRUCE_PLANKS)
                    && h != 2) return false;
            }
            for (int i = 0; i < 7; i++)
                for (int x = 12; x <= 14; x++)
                    if (!blocks.getOrDefault(pos(x, 3 + i, 3 + i), AIR)
                        .is(Blocks.SPRUCE_STAIRS)) return false;
            for (int i = 0; i < 8; i++)
                for (int x = -14; x <= -12; x++)
                    if (!blocks.getOrDefault(pos(x, 3 + i, 10 + i), AIR)
                        .is(Blocks.SPRUCE_STAIRS)) return false;
            for (int x = -22; x <= 22; x += 4)
                for (int v = 1; v <= 22; v += 4) {
                    int rise = Math.min(10, Math.min(x + 23, 23 - x));
                    int h = 26 + rise;
                    if (!blocks.containsKey(pos(x, v, h))) return false;
                }
            for (int[] pair : new int[][]{{-1, 0, 3}, {-1, 0, 10},
                {-18, 0, 18}, {16, 0, 18}, {-1, -4, 19}}) {
                if (!blocks.getOrDefault(pos(pair[0], pair[1], pair[2]), AIR)
                    .is(Blocks.DARK_OAK_DOOR)) return false;
            }
            for (int x=-8;x<=8;x++)
                if (!blocks.getOrDefault(pos(x,-6,11),AIR).is(Blocks.SPRUCE_FENCE))
                    return false;
            for (int v=-5;v<0;v++)
                for (int x : new int[]{-8,8})
                    if (!blocks.getOrDefault(pos(x,v,11),AIR).is(Blocks.SPRUCE_FENCE))
                        return false;
            for (int[] lintel : new int[][]{{-1,0,5},{0,0,5},
                {-1,0,12},{0,0,12},{-18,0,20},{-17,0,20},
                {16,0,20},{17,0,20},{-1,-4,21},{0,-4,21}})
                if (blocks.getOrDefault(pos(lintel[0],lintel[1],lintel[2]),AIR).isAir())
                    return false;
            for (int[] light : new int[][]{{0,7,6},{0,20,6},{0,7,14},{0,20,14}})
                if (!blocks.getOrDefault(pos(light[0],light[1],light[2]),AIR)
                    .is(Blocks.LANTERN))return false;
            for(int x:new int[]{-17,17})
                for(int v:new int[]{5,12,19})
                    for(int floor:new int[]{9,17})
                        if(!blocks.getOrDefault(pos(x,v,floor-2),AIR)
                            .is(Blocks.LANTERN))return false;
            if (loot.size() != 4) return false;
            for (BlockPos chest : loot.keySet())
                if (!blocks.getOrDefault(chest, AIR).is(Blocks.CHEST))
                    return false;
            return true;
        }
    }

    private static int[] fit(ServerLevel world, BuyanState.Site site, int ox, int oz) {
        long seed = world.getSeed();
        int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
        for (int x = -33; x <= 33; x += 3)
            for (int v = -15; v <= 25; v += 3) {
                int h = BuyanNature.top(seed, site.x(), site.z(),
                    site.radius(), ox + x, oz + v, site.waterLevel());
                if (h == Integer.MIN_VALUE || h < site.waterLevel() + 7)
                    return null;
                low = Math.min(low, h);
                high = Math.max(high, h);
            }
        if (high - low > 5 || high + 55 >= world.getMaxBuildHeight()) return null;
        return new int[]{low, high};
    }

    static boolean placeOnce(ServerLevel world, BuyanState.Site site) {
        if (world.dimension() != Level.OVERWORLD
            || BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site)) return false;
        Record record = Record.get(world);
        long key = key(site);
        if (record.built.containsKey(key)) return true;
        // Derive the anchor from the saved plot so the compact 32-block
        // approach and older worlds can use their respective coordinates.
        BuyanCityPlan.Layout layout = BuyanCityPlan.savedLayout(world, site);
        if (layout == null) return false;
        BuyanCityPlan.Plot lot = layout.plots().stream()
            .filter(p -> p.name().equals("TEREM")).findFirst().orElse(null);
        if (lot == null) return false;
        int ox = site.x(), oz = lot.minZ() + 15;
        int[] heights = fit(world, site, ox, oz);
        if (heights != null) {
            Plan p = new Plan(world, site, ox, oz, heights[1] + 1);
            p.build();
            if (p.geometryCheck() && p.preflight()) {
                try {
                    p.commit();
                } catch (RuntimeException failure) {
                    LogUtils.getLogger().error("Buyan southern terem placement failed", failure);
                    return false;
                }
                record.finish(key, new BlockPos(ox, p.base, oz));
                LogUtils.getLogger().info("Buyan compact southern terem placed at {},{},{}",
                    ox, p.base, oz);
                return true;
            }
        }
        LogUtils.getLogger().error("Buyan terem: southern reserve not available");
        return false;
    }

    static boolean hasBuilt(ServerLevel world, BuyanState.Site site) {
        return Record.get(world).built.containsKey(key(site));
    }

    /** Interior approach of the saved terem, before the native villager AI claims a bed. */
    static BlockPos princeHome(ServerLevel world, BuyanState.Site site) {
        Long stored = Record.get(world).built.get(key(site));
        if (stored == null) throw new IllegalStateException("Buyan terem is not built");
        return BlockPos.of(stored).offset(0, 3, 4);
    }

    /** Safe surface spawn outside the palace and its front steps. */
    static BlockPos spawn(ServerLevel world, BuyanState.Site site) {
        Long stored = Record.get(world).built.get(key(site));
        if (stored == null) return new BlockPos(site.x(), site.waterLevel() + 14, site.z());
        BlockPos palace = BlockPos.of(stored);
        int z = palace.getZ() - 30;
        int y = BuyanNature.top(world.getSeed(), site.x(), site.z(),
            site.radius(), palace.getX(), z, site.waterLevel());
        if (y == Integer.MIN_VALUE) y = site.waterLevel() + 13;
        return new BlockPos(palace.getX(), y + 1, z);
    }

    /** Deterministic block-by-block geometry gate; no saved world writes. */
    public static void verifyBlueprintFixture(ServerLevel world) {
        BuyanState.Site site = new BuyanState.Site(0, 0, BuyanTerrain.MAX_RADIUS, 63);
        Plan plan = new Plan(world, site, 0, 10, 80);
        plan.build();
        if (!plan.geometryCheck())
            throw new IllegalStateException("Terem silhouette/doors/floors/roof/loot fixture failed");
        for (BlockPos chest : plan.loot.keySet())
            if (!plan.blocks.getOrDefault(chest, AIR).is(Blocks.CHEST))
                throw new IllegalStateException("Loot chest overwritten in finished plan: " + chest);
        BlockPos deliberatelyBrokenChest = plan.loot.keySet().iterator().next();
        BlockState originalChest = plan.blocks.put(deliberatelyBrokenChest, AIR);
        try {
            if (plan.geometryCheck())
                throw new IllegalStateException("Terem validator did not detect missing chest");
        } finally {
            plan.blocks.put(deliberatelyBrokenChest, originalChest);
        }
        if (!plan.geometryCheck())
            throw new IllegalStateException("Terem chest validator cannot restore blueprint");
        for (int h : new int[]{3, 10, 18})
            if (!plan.blocks.getOrDefault(plan.pos(0, 12, h), AIR).isAir())
                throw new IllegalStateException("Central hallway blocked at floor " + h);
        for (int i = 0; i < 7; i++)
            for (int x = 12; x <= 14; x++)
                if (!plan.blocks.getOrDefault(plan.pos(x, 3 + i, 4 + i), AIR).isAir())
                    throw new IllegalStateException("First stair headroom blocked at step " + i);
        for (int i = 0; i < 8; i++)
            for (int x = -14; x <= -12; x++)
                if (!plan.blocks.getOrDefault(plan.pos(x, 3 + i, 11 + i), AIR).isAir())
                    throw new IllegalStateException("Second stair headroom blocked at step " + i);
        for (int x : new int[]{-31, 25})
            for (int h : new int[]{4, 9, 17, 25, 26})
                if (!plan.blocks.getOrDefault(plan.pos(x, 0, h), AIR).is(Blocks.LADDER))
                    throw new IllegalStateException("Inaccessible side tower ladder " + x + ":" + h);
        for (int[] d : new int[][]{{-1, 0, 3}, {0, 0, 3},
                {-1, 0, 10}, {0, 0, 10}, {-18, 0, 18}, {-17, 0, 18},
                {16, 0, 18}, {17, 0, 18}, {-1, -4, 19}, {0, -4, 19}})
            for (int h = d[2]; h <= d[2] + 1; h++)
                if (!plan.blocks.getOrDefault(plan.pos(d[0], d[1], h), AIR)
                    .is(Blocks.DARK_OAK_DOOR))
                    throw new IllegalStateException("Missing double-height door at " + d[0] + "/" + d[1] + "/" + h);
        if (!plan.blocks.getOrDefault(plan.pos(-16, 10, 18), AIR).is(Blocks.RED_BED)
            || !plan.blocks.getOrDefault(plan.pos(15, 10, 18), AIR).is(Blocks.BLUE_BED)
            || !plan.blocks.getOrDefault(plan.pos(0, 11, 51), AIR).is(Blocks.YELLOW_GLAZED_TERRACOTTA))
            throw new IllegalStateException("Terem beds or upper finial missing");
        LogUtils.getLogger().info("Buyan central terem blueprint fixture: {} positions, {} loot chests",
            plan.blocks.size(), plan.loot.size());
    }

    public static int status(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState.Site site = BuyanNatureGenerator.selectedFoundation(world,
            (int) Math.floor(source.getPosition().x),
            (int) Math.floor(source.getPosition().z));
        if (site == null) {
            source.sendFailure(Component.literal("Завершённый Буян поблизости не найден."));
            return 0;
        }
        Long built = Record.get(world).built.get(key(site));
        if (built == null) {
            source.sendSuccess(() -> Component.literal("Главный терем ещё не построен."), false);
            return 0;
        }
        BlockPos p = BlockPos.of(built);
        source.sendSuccess(() -> Component.literal(
            "Главный белокаменный терем: X=" + p.getX() + ", основание Y="
                + p.getY() + ", Z=" + p.getZ()
                + ". Парадный вход Z=" + (p.getZ() - 1)
                + ", лестница Z=" + (p.getZ() - 15) + ".." + (p.getZ() - 4)),
            false);
        return 1;
    }
}
