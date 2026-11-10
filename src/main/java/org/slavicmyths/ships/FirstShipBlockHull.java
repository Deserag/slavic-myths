package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

/**
 * First ship V2: every part of the hull is an ACTUAL vanilla world block.
 * No BlockRenderDispatcher scaling, ghost floors, artificial walk collision,
 * player-tick dragging, custom hull blocks, or water inside the lower hold.
 *
 * Native block movement is DISCRETE on the voxel grid, not interpolated
 * vehicle physics: it keeps ordinary Minecraft collision/ladder behavior.
 * This is an experimental controlled transit mechanism, not smooth sailing.
 */
public final class FirstShipBlockHull {
    public static final int LENGTH = 11, WIDTH = 7;
    private static final int MIN_Z = -5, MAX_Z = 5;
    private static final int MAX_Y = 22;
    private static final int[] WIDTHS = {1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 0};
    private static final int UPDATE = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public record Cargo(int x, int y, int z, int type, BlockState state, CompoundTag data) {}
    private record Snapshot(List<Cargo> cargo, Map<BlockPos, BlockState> blocks) {}

    public static int halfWidth(int z) {
        return z >= MIN_Z && z <= MAX_Z ? WIDTHS[z - MIN_Z] : -1;
    }

    public static boolean inside(int x, int z) {
        return Math.abs(x) <= halfWidth(z);
    }

    private static boolean hatch(int x, int z) {
        return (x == -1 || x == 0) && (z == 1 || z == 2);
    }

    private static boolean exposedEdge(int x, int z) {
        return !inside(x - 1, z) || !inside(x + 1, z)
                || !inside(x, z - 1) || !inside(x, z + 1);
    }

    private static boolean cargoCell(int x, int y, int z) {
        if (!inside(x, z) || exposedEdge(x, z)) return false;
        if (y == 1) return !(x == -1 && z == 2) && !(x == -2 && z == 2);
        if (y == 5) return !(x == 0 && (z == -1 || z == -4 || z == -3))
                && !hatch(x, z);
        return false;
    }

    public static BlockPos world(BlockPos anchor, int turns, int x, int y, int z) {
        int t = Math.floorMod(turns, 4);
        int xx = t == 0 ? x : t == 1 ? -z : t == 2 ? -x : z;
        int zz = t == 0 ? z : t == 1 ? x : t == 2 ? -z : -x;
        return anchor.offset(xx, y, zz);
    }

    public static int rotationFromYaw(float yaw) {
        return Math.floorMod(Math.round(yaw / 90F), 4);
    }

    private static Map<BlockPos, BlockState> layout(BlockPos anchor, int turns, int rig, int fabric) {
        if (rig < 0 || rig > 4 || fabric < 0 || fabric > 3
                || (rig == 0) != (fabric == 0))
            throw new IllegalArgumentException("Invalid sail rig or cloth quality");
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int z = MIN_Z; z <= MAX_Z; z++) {
            int beam = halfWidth(z);
            for (int x = -beam; x <= beam; x++) {
                // Water is replaced by a FULL solid spruce floor at y=0.
                blocks.put(world(anchor, turns, x, 0, z),
                        x == 0 ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS,
                                        turns % 2 == 0 ? Direction.Axis.Z : Direction.Axis.X)
                                : Blocks.SPRUCE_PLANKS.defaultBlockState());
                if (exposedEdge(x, z)) {
                    for (int y = 1; y <= 3; y++) {
                        BlockState side = (x == 0 && z == 5) || (z == -5 && x == 0)
                                ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                                : Blocks.SPRUCE_PLANKS.defaultBlockState();
                        blocks.put(world(anchor, turns, x, y, z), side);
                    }
                }
                // Two-block standing headroom in the dry hold: air at y=1,2,
                // ceiling at y=3. Keep the real ladder/hatch shaft clear.
                if (!hatch(x, z) && !exposedEdge(x, z))
                    blocks.put(world(anchor, turns, x, 3, z),
                            Blocks.SPRUCE_PLANKS.defaultBlockState());
                if (!hatch(x, z)) {
                    blocks.put(world(anchor, turns, x, 4, z),
                            ((z + x) & 3) == 0 ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                                    : Blocks.OAK_PLANKS.defaultBlockState());
                }
                if (exposedEdge(x, z)) {
                    // Fore and aft narrowing requires FULL blocks: fence corners
                    // do not connect diagonally and would leave fall-through gaps.
                    if (z >= 2 || z <= -3) {
                        blocks.put(world(anchor, turns, x, 5, z),
                                Blocks.SPRUCE_PLANKS.defaultBlockState());
                    } else {
                        BlockState rail = Blocks.SPRUCE_FENCE.defaultBlockState();
                        boolean north = exposedEdge(x, z - 1) && inside(x, z - 1);
                        boolean south = exposedEdge(x, z + 1) && inside(x, z + 1);
                        boolean west = exposedEdge(x - 1, z) && inside(x - 1, z);
                        boolean east = exposedEdge(x + 1, z) && inside(x + 1, z);
                        rail = rail.setValue(FenceBlock.NORTH, north)
                                .setValue(FenceBlock.SOUTH, south)
                                .setValue(FenceBlock.WEST, west)
                                .setValue(FenceBlock.EAST, east);
                        for (int rotation = 0; rotation < Math.floorMod(turns, 4); rotation++)
                            rail = rail.rotate(Rotation.CLOCKWISE_90);
                        blocks.put(world(anchor, turns, x, 5, z), rail);
                    }
                }
            }
        }

        // Real 2x2 hatch with ACTUAL vanilla ladder and solid backing pillar.
        for (int y = 1; y <= 3; y++)
            blocks.put(world(anchor, turns, -2, y, 2), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        for (int y = 1; y <= 4; y++)
            blocks.put(world(anchor, turns, -1, y, 2), Blocks.LADDER.defaultBlockState()
                    .setValue(LadderBlock.FACING, switch (Math.floorMod(turns, 4)) { case 0 -> Direction.EAST; case 1 -> Direction.SOUTH; case 2 -> Direction.WEST; default -> Direction.NORTH; }));

        // Solid, connected bow from a single stem block and carved high stempost.
        for (int y = 5; y <= 6; y++)
            blocks.put(world(anchor, turns, 0, y, 5), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());

        // A bare hull has no automatic sail. Mast/yards and full block sails
        // appear only after material-backed rigging installation.
        int mastTop = switch (rig) {
            case 0 -> 6;
            case 1 -> 12;
            case 2 -> 16;
            case 3 -> 17;
            default -> 22;
        };
        for (int y = 5; y <= mastTop; y++)
            blocks.put(world(anchor, turns, 0, y, -1), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        switch (rig) {
            case 1 -> addSail(blocks, anchor, turns, fabric, 8, 10);
            case 2 -> {
                addSail(blocks, anchor, turns, fabric, 8, 10);
                addSail(blocks, anchor, turns, fabric, 13, 15);
            }
            case 3 -> addSail(blocks, anchor, turns, fabric, 8, 15);
            case 4 -> {
                addSail(blocks, anchor, turns, fabric, 8, 15);
                addSail(blocks, anchor, turns, fabric, 18, 20);
            }
            default -> {}
        }

        // Real full-sized 3x3 wooden ship's wheel, perpendicular to the
        // centreline. Cross-spokes are slender fences, not a solid wall.
        // The captain sits at z=-3, in front of this visible wheel at z=-4.
        blocks.put(world(anchor, turns, 0, 5, -4), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        blocks.put(world(anchor, turns, 0, 7, -4), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS,
                        turns % 2 == 0 ? Direction.Axis.Z : Direction.Axis.X));
        for (int x : new int[]{-1, 1}) {
            blocks.put(world(anchor, turns, x, 7, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());
            blocks.put(world(anchor, turns, x, 6, -4), Blocks.SPRUCE_FENCE.defaultBlockState());
            blocks.put(world(anchor, turns, x, 8, -4), Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        blocks.put(world(anchor, turns, 0, 6, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());
        blocks.put(world(anchor, turns, 0, 8, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());

        return blocks;
    }


    /**
     * Geometry V3 is additive in ID, NOT an in-place reinterpretation of V2
     * saves. Existing world blocks are inspected with their saved geometry
     * until an unoccupied, cargo-safe migration can complete.
     */
    private static Map<BlockPos, BlockState> layoutV3(BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layout(anchor, turns, rig, fabric);
        for (int z = MIN_Z; z <= MAX_Z; z++) {
            for (int x = -halfWidth(z); x <= halfWidth(z); x++) {
                if (exposedEdge(x, z)) {
                    // Continuous dark strake with pronounced stem and transom.
                    blocks.put(world(anchor, turns, x, 2, z),
                            (x == 0 && (z == -5 || z == 5))
                                    ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                                    : Blocks.DARK_OAK_PLANKS.defaultBlockState());
                    if (z >= 2 || z <= -3)
                        blocks.put(world(anchor, turns, x, 5, z),
                                Blocks.DARK_OAK_PLANKS.defaultBlockState());
                } else if (!hatch(x, z) && (Math.abs(x) >= 2 || z >= 3 || z <= -4)) {
                    blocks.put(world(anchor, turns, x, 4, z), Blocks.SPRUCE_PLANKS.defaultBlockState());
                }
            }
        }
        // Delete the old squared-off 3x3 wheel before laying out a rim, hub
        // and spokes. All spokes sit aft of the captain, not in the seat cell.
        for (int x = -2; x <= 2; x++)
            for (int y = 6; y <= 9; y++)
                blocks.remove(world(anchor, turns, x, y, -4));
        blocks.put(world(anchor, turns, 0, 5, -4), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        for (int x : new int[]{-2, 2})
            blocks.put(world(anchor, turns, x, 7, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());
        for (int x : new int[]{-1, 1}) {
            blocks.put(world(anchor, turns, x, 6, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());
            blocks.put(world(anchor, turns, x, 7, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());
            blocks.put(world(anchor, turns, x, 8, -4), Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        for (int y = 6; y <= 9; y++)
            blocks.put(world(anchor, turns, 0, y, -4),
                    y == 7 ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS,
                                    turns % 2 == 0 ? Direction.Axis.Z : Direction.Axis.X)
                            : Blocks.DARK_OAK_FENCE.defaultBlockState());

        // The naked hull still has a TALL MAST, gaff/yard and fittings but
        // deliberately receives NO free sailcloth when rig == 0.
        for (int x = -2; x <= 2; x++) {
            for (int y = 7; y <= 22; y++) {
                blocks.remove(world(anchor, turns, x, y, 0));
                if (x != 0) blocks.remove(world(anchor, turns, x, y, -1));
            }
        }
        int top = switch (rig) {
            case 0, 1 -> 12;
            case 2 -> 16;
            case 3 -> 17;
            default -> 22;
        };
        for (int y = 5; y <= top; y++)
            blocks.put(world(anchor, turns, 0, y, -1), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        if (rig == 0) addYardV3(blocks, anchor, turns, 10, 2);
        else if (rig == 1) addSailV3(blocks, anchor, turns, fabric, 8, 10, 2);
        else if (rig == 2) {
            addSailV3(blocks, anchor, turns, fabric, 8, 10, 3);
            addSailV3(blocks, anchor, turns, fabric, 13, 14, 2);
        } else if (rig == 3) addSailV3(blocks, anchor, turns, fabric, 8, 15, 3);
        else {
            addSailV3(blocks, anchor, turns, fabric, 8, 15, 3);
            addSailV3(blocks, anchor, turns, fabric, 18, 20, 2);
        }
        // Dark wooden bow stem: higher than the gunwale but inside 7x11.
        blocks.put(world(anchor, turns, 0, 7, 5), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        return blocks;
    }

    /**
     * V4: a recognizable five-block diameter ship wheel, not the old row of
     * upright fences, plus an independent stern rudder blade below the tiller.
     * The wooden rim is made of short slabs; the center and four spokes are
     * visible through its empty middle when viewed from the helm side.
     */
    private static Map<BlockPos, BlockState> layoutV4(BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layoutV3(anchor, turns, rig, fabric);
        for (int x = -2; x <= 2; x++)
            for (int y = 6; y <= 10; y++)
                blocks.remove(world(anchor, turns, x, y, -4));
        for (int y = 6; y <= 10; y++) {
            for (int x = -2; x <= 2; x++) {
                boolean outerSides = Math.abs(x) == 2 && y >= 7 && y <= 9;
                boolean roundedEnds = Math.abs(x) <= 1 && (y == 6 || y == 10);
                if (outerSides || roundedEnds)
                    blocks.put(world(anchor, turns, x, y, -4),
                            Blocks.DARK_OAK_SLAB.defaultBlockState());
            }
        }
        for (int x : new int[]{-1, 1})
            blocks.put(world(anchor, turns, x, 8, -4), Blocks.SPRUCE_FENCE.defaultBlockState());
        for (int y : new int[]{7, 9})
            blocks.put(world(anchor, turns, 0, y, -4), Blocks.SPRUCE_FENCE.defaultBlockState());
        blocks.put(world(anchor, turns, 0, 8, -4),
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(
                        RotatedPillarBlock.AXIS, turns % 2 == 0
                                ? Direction.Axis.Z : Direction.Axis.X));
        // A genuine stern rudder protrudes aft, rather than pretending that
        // the wheel itself is the hydrodynamic rudder.
        for (int y = 0; y <= 2; y++)
            blocks.put(world(anchor, turns, 0, y, -6), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        blocks.put(world(anchor, turns, 0, 3, -6), Blocks.DARK_OAK_FENCE.defaultBlockState());
        return blocks;
    }

    /**
     * V5 removes the unsuccessful wheel made from vanilla fences/slabs. The
     * registered 3D model of the existing HELM_MODULE item is rendered by
     * FirstShipRenderer at this exact wheel position. The genuine stern rudder
     * and every load-bearing deck block remain physical vanilla blocks.
     */
    private static Map<BlockPos, BlockState> layoutV5(BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layoutV4(anchor, turns, rig, fabric);
        for (int x = -2; x <= 2; x++)
            for (int y = 6; y <= 10; y++)
                blocks.remove(world(anchor, turns, x, y, -4));
        blocks.remove(world(anchor, turns, 0, 5, -4));
        return blocks;
    }

    /** Full-size basic sail on NEW V6 hulls; V5 layouts remain byte-for-byte stable. */
    private static Map<BlockPos, BlockState> layoutV6(BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layoutV5(anchor, turns, rig, fabric);
        if (rig != 1) return blocks;
        for (int y = 8; y <= 10; y++)
            for (int x = -2; x <= 2; x++)
                blocks.remove(world(anchor, turns, x, y, 0));
        for (int y : new int[]{7, 11})
            for (int x = -2; x <= 2; x++)
                if (x != 0) blocks.remove(world(anchor, turns, x, y, -1));
        addSailV3(blocks, anchor, turns, fabric, 7, 12, 3);
        return blocks;
    }

    /** V7 keeps the mast and yards physical; the cloth is a thin rendered mesh. */
    private static Map<BlockPos, BlockState> layoutV7(BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layoutV6(anchor, turns, rig, fabric);
        blocks.entrySet().removeIf(entry -> entry.getValue().is(Blocks.WHITE_WOOL)
                || entry.getValue().is(Blocks.LIGHT_GRAY_WOOL)
                || entry.getValue().is(Blocks.YELLOW_WOOL)
                || entry.getValue().is(Blocks.RED_WOOL)
                || entry.getValue().is(Blocks.BLUE_WOOL)
                || entry.getValue().is(Blocks.BROWN_WOOL));
        return blocks;
    }

    /**
     * V8 only: preserve V7 hull ownership while introducing cloth-tier-specific
     * mastheads and genuine wooden yards. Old saved geometry stays untouched.
     * Tier I keeps the historical rig, II receives a taller head yard,
     * III a wider top yard and longer mast.
     */
    private static Map<BlockPos, BlockState> layoutV8(BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layoutV7(anchor, turns, rig, fabric);
        if (rig == 0 || fabric <= 1) return blocks;
        if (rig == 1) {
            int top = fabric == 2 ? 13 : 14;
            int span = fabric == 2 ? 3 : 4;
            for (int y = 13; y <= top + 1; y++)
                blocks.put(world(anchor, turns, 0, y, -1),
                        Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
            addYardV3(blocks, anchor, turns, top + 1, span);
        } else {
            int top = rig == 2 ? 16 : rig == 3 ? 17 : 22;
            if (fabric == 3) addYardV3(blocks, anchor, turns, Math.min(22, top), 4);
        }
        return blocks;
    }

    private static void addYardV3(Map<BlockPos, BlockState> blocks, BlockPos anchor,
            int turns, int y, int span) {
        BlockState beam = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(
                RotatedPillarBlock.AXIS,
                turns % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z);
        for (int x = -span; x <= span; x++)
            blocks.put(world(anchor, turns, x, y, -1), beam);
    }

    private static void addSailV3(Map<BlockPos, BlockState> blocks, BlockPos anchor,
            int turns, int fabric, int bottom, int top, int span) {
        BlockState cloth = (fabric == 1 ? Blocks.WHITE_WOOL
                : fabric == 2 ? Blocks.LIGHT_GRAY_WOOL : Blocks.YELLOW_WOOL).defaultBlockState();
        BlockState edge = (fabric == 1 ? Blocks.RED_WOOL
                : fabric == 2 ? Blocks.BLUE_WOOL : Blocks.BROWN_WOOL).defaultBlockState();
        addYardV3(blocks, anchor, turns, top + 1, span);
        addYardV3(blocks, anchor, turns, bottom - 1, Math.max(1, span - 1));
        for (int y = bottom; y <= top; y++) {
            int rowSpan = span - (y == bottom || y == top ? 1 : 0);
            for (int x = -rowSpan; x <= rowSpan; x++) {
                // A taper/belly and contrasting boltrope, rather than a box.
                BlockState color = Math.abs(x) == rowSpan ? edge : cloth;
                if (fabric == 3 && x == 0) color = edge;
                blocks.put(world(anchor, turns, x, y, 0), color);
            }
        }
    }

    private static Map<BlockPos, BlockState> layout(BlockPos anchor, int turns,
            int rig, int fabric, int geometry) {
        return geometry >= 8 ? layoutV8(anchor, turns, rig, fabric)
                : geometry == 7 ? layoutV7(anchor, turns, rig, fabric)
                : geometry == 6 ? layoutV6(anchor, turns, rig, fabric)
                : geometry == 5 ? layoutV5(anchor, turns, rig, fabric)
                : geometry == 4 ? layoutV4(anchor, turns, rig, fabric)
                : geometry == 3 ? layoutV3(anchor, turns, rig, fabric)
                : layout(anchor, turns, rig, fabric);
    }

    private static void addSail(Map<BlockPos, BlockState> blocks, BlockPos anchor,
            int turns, int fabric, int bottom, int top) {
        BlockState base = (fabric == 1 ? Blocks.WHITE_WOOL
                : fabric == 2 ? Blocks.LIGHT_GRAY_WOOL : Blocks.YELLOW_WOOL).defaultBlockState();
        BlockState trim = (fabric == 1 ? Blocks.RED_WOOL
                : fabric == 2 ? Blocks.BLUE_WOOL : Blocks.BROWN_WOOL).defaultBlockState();
        for (int x = -2; x <= 2; x++) {
            for (int y : new int[]{bottom - 1, top + 1}) {
                blocks.put(world(anchor, turns, x, y, -1),
                        Blocks.DARK_OAK_LOG.defaultBlockState().setValue(
                                RotatedPillarBlock.AXIS, turns % 2 == 0
                                        ? Direction.Axis.X : Direction.Axis.Z));
            }
            for (int y = bottom; y <= top; y++)
                blocks.put(world(anchor, turns, x, y, 0),
                        (x == 0 || y == (bottom + top) / 2) ? trim : base);
        }
    }

    private static Set<BlockPos> volume(BlockPos anchor, int turns, int rig, int fabric, int geometry) {
        Set<BlockPos> volume = new HashSet<>(layout(anchor, turns, rig, fabric, geometry).keySet());
        for (int z = MIN_Z; z <= MAX_Z; z++)
            for (int x = -halfWidth(z); x <= halfWidth(z); x++) {
                for (int y = 1; y <= 3; y++) volume.add(world(anchor, turns, x, y, z));
                for (int y = 5; y <= 6; y++) volume.add(world(anchor, turns, x, y, z));
            }
        return volume;
    }

    private static int kind(BlockState state) {
        if (state.is(Blocks.CHEST)) return 1;
        if (state.is(Blocks.BARREL)) return 2;
        if (state.is(Blocks.CRAFTING_TABLE)) return 3;
        if (state.is(Blocks.FURNACE)) return 4;
        return 0;
    }

    private static BlockState typeState(int type) {
        return switch (type) {
            case 1 -> Blocks.CHEST.defaultBlockState();
            case 2 -> Blocks.BARREL.defaultBlockState();
            case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
            case 4 -> Blocks.FURNACE.defaultBlockState();
            default -> Blocks.AIR.defaultBlockState();
        };
    }

    /**
     * Exact vanilla water obstacles only. Never classify player blocks, non-vanilla
     * blocks or any BlockEntity as disposable. Coral BLOCKS are intentionally
     * included, but ordinary stone, wood and containers are not.
     */
    private static boolean softWaterObstacle(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) != null) return false;
        Block block = state.getBlock();
        if (block == Blocks.LILY_PAD || block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS
                || block == Blocks.KELP || block == Blocks.KELP_PLANT
                || block == Blocks.SEA_PICKLE || block == Blocks.SMALL_DRIPLEAF) return true;
        var id = BuiltInRegistries.BLOCK.getKey(block);
        if (!"minecraft".equals(id.getNamespace())) return false;
        String name = id.getPath();
        for (String kind : new String[]{"tube", "brain", "bubble", "fire", "horn"})
            if (name.startsWith(kind + "_coral") || name.startsWith("dead_" + kind + "_coral"))
                return true;
        return false;
    }

    /** Collect allowed obstruction positions during preflight, without mutating the world. */
    private static Set<BlockPos> clearableFootprint(ServerLevel level, BlockPos anchor, int turns,
            Set<BlockPos> owned, int rig, int fabric, int geometry) {
        Set<BlockPos> removable = new HashSet<>();
        for (BlockPos pos : volume(anchor, turns, rig, fabric, geometry)) {
            if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.below())) return null;
            if (owned.contains(pos)) continue;
            BlockState state = level.getBlockState(pos);
            boolean removableState = softWaterObstacle(level, pos, state);
            int relativeY = pos.getY() - anchor.getY();
            if (relativeY == 0) {
                if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return null;
                if (!removableState && (!level.getFluidState(pos).is(FluidTags.WATER)
                        || !level.getFluidState(pos).isSource())) return null;
            } else if (!state.isAir() && !removableState) return null;
            if (removableState) removable.add(pos.immutable());
        }
        return removable;
    }

    /**
     * Perform clearance only AFTER validating the full destination, source,
     * and every cargo BlockEntity. setBlock does not spawn item drops.
     * Below the waterline it restores water, above it clears the hold to air.
     */
    private static void clearObstacles(ServerLevel level, BlockPos anchor, Set<BlockPos> removable) {
        for (BlockPos pos : removable) {
            BlockState replacement = pos.getY() == anchor.getY()
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            level.setBlock(pos, replacement, UPDATE);
        }
    }

    /** Exact block positions are checked before anything is overwritten. */
    private static boolean canWrite(ServerLevel level, BlockPos anchor, int turns, Set<BlockPos> owned,
            List<Cargo> cargo, int rig, int fabric, int geometry) {
        Set<BlockPos> target = volume(anchor, turns, rig, fabric, geometry);
        for (Cargo entry : cargo) {
            BlockPos point = world(anchor, turns, entry.x(), entry.y(), entry.z());
            if (!cargoCell(entry.x(), entry.y(), entry.z()) || !target.contains(point)) return false;
        }
        return clearableFootprint(level, anchor, turns, owned, rig, fabric, geometry) != null;
    }

    private static Snapshot inspect(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric, int geometry) {
        return inspect(level, anchor, turns, layout(anchor, turns, rig, fabric, geometry));
    }

    private static Snapshot inspect(ServerLevel level, BlockPos anchor, int turns,
            Map<BlockPos, BlockState> hull) {
        for (var entry : hull.entrySet()) {
            if (!level.hasChunkAt(entry.getKey())
                    || level.getBlockState(entry.getKey()).getBlock() != entry.getValue().getBlock())
                return null;
        }
        List<Cargo> cargo = new ArrayList<>();
        for (int z = MIN_Z; z <= MAX_Z; z++)
            for (int x = -halfWidth(z); x <= halfWidth(z); x++)
                for (int y : new int[]{1, 5}) {
                    BlockPos pos = world(anchor, turns, x, y, z);
                    if (hull.containsKey(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    int type = kind(state);
                    if (type == 0 || !cargoCell(x, y, z)) return null;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (type != 3 && be == null) return null;
                    CompoundTag tag = be == null ? new CompoundTag()
                            : be.saveWithFullMetadata(level.registryAccess());
                    cargo.add(new Cargo(x, y, z, type, state, tag));
                }
        // Do not bulldoze arbitrary additions inside the lower hold.
        // Also examine every interior air cell: a legacy player's structure or
        // unrecognized block is never silently overwritten as part of a step.
        Set<BlockPos> occupied = new HashSet<>(hull.keySet());
        for (int z = MIN_Z; z <= MAX_Z; z++)
            for (int x = -halfWidth(z); x <= halfWidth(z); x++) {
                for (int y = 1; y <= 3; y++) occupied.add(world(anchor, turns, x, y, z));
                for (int y = 5; y <= 6; y++) occupied.add(world(anchor, turns, x, y, z));
            }
        for (BlockPos pos : occupied) {
            if (hull.containsKey(pos) || level.getBlockState(pos).isAir()) continue;
            boolean recognized = false;
            for (Cargo entry : cargo) if (world(anchor, turns, entry.x(), entry.y(), entry.z()).equals(pos)) {
                recognized = true;
                break;
            }
            if (!recognized) return null;
        }
        return new Snapshot(cargo, hull);
    }

    private static boolean restoreCargo(ServerLevel level, BlockPos anchor, int turns, List<Cargo> cargo) {
        for (Cargo entry : cargo) {
            BlockPos pos = world(anchor, turns, entry.x(), entry.y(), entry.z());
            if (!level.setBlock(pos, entry.state(), UPDATE)) return false;
            if (entry.type() != 3) {
                BlockEntity be = BlockEntity.loadStatic(pos, entry.state(), entry.data(), level.registryAccess());
                if (be == null) return false;
                level.setBlockEntity(be);
                be.setChanged();
            }
        }
        return true;
    }

    private static void clear(ServerLevel level, BlockPos anchor, int turns, Snapshot snapshot) {
        List<BlockPos> locations = new ArrayList<>(snapshot.blocks().keySet());
        for (Cargo entry : snapshot.cargo()) {
            BlockPos pos = world(anchor, turns, entry.x(), entry.y(), entry.z());
            // Remove the tile entity BEFORE replacing a container: it prevents
            // vanilla onRemove from dropping a duplicate inventory into the world.
            level.removeBlockEntity(pos);
            locations.add(pos);
        }
        locations.sort(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed());
        for (BlockPos pos : locations) {
            BlockState replacement = pos.getY() == anchor.getY()
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            level.setBlock(pos, replacement, UPDATE);
        }
    }

    private static boolean build(ServerLevel level, BlockPos anchor, int turns,
            List<Cargo> cargo, int rig, int fabric, int geometry) {
        Map<BlockPos, BlockState> blocks = layout(anchor, turns, rig, fabric, geometry);
        List<Map.Entry<BlockPos, BlockState>> ordered = new ArrayList<>(blocks.entrySet());
        ordered.sort(Comparator.comparingInt(e -> e.getKey().getY()));
        for (var entry : ordered) if (!level.setBlock(entry.getKey(), entry.getValue(), UPDATE)) return false;
        return restoreCargo(level, anchor, turns, cargo);
    }

    /**
     * One-time upgrade for worlds saved by the original 52c8d72 native ship:
     * the old wheel was short and the hold had three blocks of free headroom.
     * Its original wool sail stays installed. The source remains unchanged on
     * all validation failures; normal crates/BlockEntities are snapshotted.
     */
    private static Map<BlockPos, BlockState> legacyLayout(BlockPos anchor, int turns) {
        Map<BlockPos, BlockState> legacy = layout(anchor, turns, 1, 1);
        for (int z = MIN_Z; z <= MAX_Z; z++)
            for (int x = -halfWidth(z); x <= halfWidth(z); x++)
                if (!hatch(x, z) && !exposedEdge(x, z))
                    legacy.remove(world(anchor, turns, x, 3, z));
        for (int x = -1; x <= 1; x++) {
            legacy.remove(world(anchor, turns, x, 7, -4));
            legacy.remove(world(anchor, turns, x, 8, -4));
        }
        return legacy;
    }

    public static boolean migrateLegacy(ServerLevel level, BlockPos anchor, int turns) {
        Snapshot original = inspect(level, anchor, turns, legacyLayout(anchor, turns));
        if (original == null) return false;
        Set<BlockPos> owned = new HashSet<>(original.blocks().keySet());
        for (Cargo cargo : original.cargo())
            owned.add(world(anchor, turns, cargo.x(), cargo.y(), cargo.z()));
        if (!canWrite(level, anchor, turns, owned, original.cargo(), 1, 1, 2)) return false;
        for (Cargo cargo : original.cargo()) {
            if (cargo.type() == 3) continue;
            BlockPos target = world(anchor, turns, cargo.x(), cargo.y(), cargo.z());
            if (BlockEntity.loadStatic(target, cargo.state(), cargo.data(), level.registryAccess()) == null)
                return false;
        }
        Set<BlockPos> removable = clearableFootprint(level, anchor, turns, owned, 1, 1, 2);
        if (removable == null) return false;
        clear(level, anchor, turns, original);
        clearObstacles(level, anchor, removable);
        return build(level, anchor, turns, original.cargo(), 1, 1, 2);
    }

    private static boolean migrateGeometry(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric, int from, int to) {
        Snapshot old = inspect(level, anchor, turns, rig, fabric, from);
        if (old == null) return false;
        Set<BlockPos> owned = new HashSet<>(old.blocks().keySet());
        for (Cargo c : old.cargo()) owned.add(world(anchor, turns, c.x(), c.y(), c.z()));
        if (!canWrite(level, anchor, turns, owned, old.cargo(), rig, fabric, to)) return false;
        for (Cargo c : old.cargo()) {
            if (c.type() == 3) continue;
            BlockPos p = world(anchor, turns, c.x(), c.y(), c.z());
            if (BlockEntity.loadStatic(p, c.state(), c.data(), level.registryAccess()) == null)
                return false;
        }
        Set<BlockPos> removable = clearableFootprint(level, anchor, turns, owned, rig, fabric, to);
        if (removable == null) return false;
        clear(level, anchor, turns, old);
        clearObstacles(level, anchor, removable);
        return build(level, anchor, turns, old.cargo(), rig, fabric, to);
    }

    /** Atomic V5 starter-sail upgrade; preserves real inventory BlockEntities. */
    public static boolean upgradeStarterSailV6(ServerLevel level, BlockPos anchor,
            int turns, int oldRig, int oldFabric) {
        if (oldRig < 0 || oldRig > 1) return false;
        Snapshot old = inspect(level, anchor, turns, oldRig, oldFabric, 5);
        if (old == null) return false;
        Set<BlockPos> owned = new HashSet<>(old.blocks().keySet());
        for (Cargo entry : old.cargo())
            owned.add(world(anchor, turns, entry.x(), entry.y(), entry.z()));
        if (!canWrite(level, anchor, turns, owned, old.cargo(), 1, 1, 6)) return false;
        for (Cargo entry : old.cargo()) {
            if (entry.type() == 3) continue;
            BlockPos p = world(anchor, turns, entry.x(), entry.y(), entry.z());
            if (BlockEntity.loadStatic(p, entry.state(), entry.data(), level.registryAccess()) == null)
                return false;
        }
        Set<BlockPos> removable = clearableFootprint(level, anchor, turns, owned, 1, 1, 6);
        if (removable == null) return false;
        clear(level, anchor, turns, old);
        clearObstacles(level, anchor, removable);
        return build(level, anchor, turns, old.cargo(), 1, 1, 6);
    }

    public static boolean migrateVisualSailV7(ServerLevel level, BlockPos anchor,
            int turns, int rig, int fabric) {
        return rig > 0 && migrateGeometry(level, anchor, turns, rig, fabric, 6, 7);
    }

    /** Migrate V2 saves without reinterpreting the existing physical layout. */
    public static boolean migrateGeometryV2(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric) {
        return migrateGeometry(level, anchor, turns, rig, fabric, 2, 3);
    }

    /** Migrate occupied-world V3 ships only after all people disembark safely. */
    public static boolean migrateGeometryV3(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric) {
        return migrateGeometry(level, anchor, turns, rig, fabric, 3, 4);
    }

    /** Replaces the physical V4 wheel with the existing helm item's 3D model. */
    public static boolean migrateGeometryV4(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric) {
        return migrateGeometry(level, anchor, turns, rig, fabric, 4, 5);
    }

    /** Transaction checks source intact, future footprint safe, then moves blocks once. */
    public static boolean move(ServerLevel level, BlockPos previous, int oldTurn,
            BlockPos target, int nextTurn, int oldRig, int oldFabric,
            int nextRig, int nextFabric, int geometry) {
        Snapshot snapshot = inspect(level, previous, oldTurn, oldRig, oldFabric, geometry);
        if (snapshot == null) return false;
        Set<BlockPos> owned = new HashSet<>(snapshot.blocks().keySet());
        for (Cargo entry : snapshot.cargo())
            owned.add(world(previous, oldTurn, entry.x(), entry.y(), entry.z()));
        if (!canWrite(level, target, nextTurn, owned, snapshot.cargo(), nextRig, nextFabric, geometry)) return false;
        List<Cargo> turned = new ArrayList<>();
        int delta = Math.floorMod(nextTurn - oldTurn, 4);
        for (Cargo entry : snapshot.cargo()) {
            BlockState state = entry.state();
            for (int i = 0; i < delta; i++)
                state = state.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
            turned.add(new Cargo(entry.x(), entry.y(), entry.z(), entry.type(), state, entry.data()));
        }
        // Validate restoration of every container BEFORE changing the world.
        for (Cargo entry : turned) {
            if (entry.type() == 3) continue;
            BlockPos targetPos = world(target, nextTurn, entry.x(), entry.y(), entry.z());
            if (BlockEntity.loadStatic(targetPos, entry.state(), entry.data(), level.registryAccess()) == null)
                return false;
        }
        // Recheck the full clearance plan before touching the source or terrain.
        Set<BlockPos> removable = clearableFootprint(level, target, nextTurn, owned, nextRig, nextFabric, geometry);
        if (removable == null) return false;
        // All BlockEntities are serialized before clearing any old block.
        clear(level, previous, oldTurn, snapshot);
        clearObstacles(level, target, removable);
        return build(level, target, nextTurn, turned, nextRig, nextFabric, geometry);
    }

    public static boolean move(ServerLevel level, BlockPos previous, int oldTurn,
            BlockPos target, int nextTurn, int oldRig, int oldFabric, int nextRig, int nextFabric) {
        return move(level, previous, oldTurn, target, nextTurn, oldRig, oldFabric, nextRig, nextFabric, 5);
    }

    public static boolean place(ServerLevel level, BlockPos anchor, int turns,
            ListTag packed, int rig, int fabric, int geometry) {
        List<Cargo> cargo = decode(packed);
        if (cargo == null || !canWrite(level, anchor, turns, Set.of(), cargo, rig, fabric, geometry)) return false;
        for (Cargo entry : cargo) {
            if (entry.type() == 3) continue;
            BlockPos target = world(anchor, turns, entry.x(), entry.y(), entry.z());
            if (BlockEntity.loadStatic(target, entry.state(), entry.data(), level.registryAccess()) == null)
                return false;
        }
        Set<BlockPos> removable = clearableFootprint(level, anchor, turns, Set.of(), rig, fabric, geometry);
        if (removable == null) return false;
        clearObstacles(level, anchor, removable);
        return build(level, anchor, turns, cargo, rig, fabric, geometry);
    }

    public static boolean place(ServerLevel level, BlockPos anchor, int turns,
            ListTag packed, int rig, int fabric) {
        return place(level, anchor, turns, packed, rig, fabric, 5);
    }

    public static ListTag pack(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric, int geometry) {
        Snapshot snapshot = inspect(level, anchor, turns, rig, fabric, geometry);
        if (snapshot == null) return null;
        ListTag list = new ListTag();
        for (Cargo c : snapshot.cargo()) {
            CompoundTag data = new CompoundTag();
            data.putInt("X", c.x()); data.putInt("Y", c.y()); data.putInt("Z", c.z());
            data.putInt("Kind", c.type());
            data.put("BlockEntity", c.data().copy());
            list.add(data);
        }
        return list;
    }

    public static ListTag pack(ServerLevel level, BlockPos anchor, int turns, int rig, int fabric) {
        return pack(level, anchor, turns, rig, fabric, 5);
    }

    /** Only 4 vanilla blocks may be attached; malformed saves are rejected. */
    private static List<Cargo> decode(ListTag packed) {
        if (packed == null || packed.size() > WIDTH * LENGTH * 2) return null;
        List<Cargo> cargo = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int n = 0; n < packed.size(); n++) {
            CompoundTag data = packed.getCompound(n);
            int x = data.getInt("X"), y = data.getInt("Y"), z = data.getInt("Z");
            int kind = data.getInt("Kind");
            if (!cargoCell(x, y, z) || kind < 1 || kind > 4
                    || !seen.add(x + ":" + y + ":" + z)
                    || (kind != 3 && !data.contains("BlockEntity", Tag.TAG_COMPOUND))) return null;
            cargo.add(new Cargo(x, y, z, kind, typeState(kind),
                    data.getCompound("BlockEntity").copy()));
        }
        return cargo;
    }

    /** Packing refuses a changed hull; no silent conversion of damaged structures. */
    public static boolean remove(ServerLevel level, BlockPos anchor, int turns,
            int rig, int fabric, int geometry) {
        Snapshot snapshot = inspect(level, anchor, turns, rig, fabric, geometry);
        if (snapshot == null) return false;
        clear(level, anchor, turns, snapshot);
        return true;
    }

    public static boolean remove(ServerLevel level, BlockPos anchor, int turns, int rig, int fabric) {
        return remove(level, anchor, turns, rig, fabric, 5);
    }

    /**
     * Validated source for the future moving-contraption handoff.
     *
     * The physical world block hull can be detached only after its exact
     * expected hull, all installed containers and their NBT have been checked.
     * A snapshot contains the complete cargo payload, geometry, rigging and
     * original bearing. This is NOT enabled by a helm command yet: the moving
     * entity must first have crash-safe persistence and an interactive floor.
     */
    public record MotionSnapshot(ListTag cargo, int geometry, int rig, int fabric, int turn) {
        public MotionSnapshot {
            if (cargo == null || cargo.size() > WIDTH * LENGTH * 2
                    || geometry < 2 || geometry > 8
                    || turn < 0 || turn > 3
                    || decode(cargo) == null)
                throw new IllegalArgumentException("Unsafe moving ship snapshot");
            new ShipRigging(rig, fabric);
            cargo = cargo.copy();
        }
        @Override public ListTag cargo() { return cargo.copy(); }
    }

    public static MotionSnapshot captureForMotion(ServerLevel level, BlockPos anchor,
            int turns, int rig, int fabric, int geometry) {
        if (geometry < 2 || geometry > 8 || turns < 0 || turns > 3) return null;
        ListTag cargo = pack(level, anchor, turns, rig, fabric, geometry);
        return cargo == null ? null : new MotionSnapshot(cargo, geometry, rig, fabric, turns);
    }

    /**
     * Immovable-to-moving phase: validates the entire source AGAIN just before
     * removing it. The snapshot must be durably attached to the moving ship
     * entity BEFORE calling this method; otherwise a crash can lose cargo.
     */
    public static boolean detachForMotion(ServerLevel level, BlockPos anchor,
            MotionSnapshot snapshot) {
        if (snapshot == null) return false;
        ListTag cargo = pack(level, anchor, snapshot.turn(), snapshot.rig(),
                snapshot.fabric(), snapshot.geometry());
        if (cargo == null || !cargo.equals(snapshot.cargo())) return false;
        return remove(level, anchor, snapshot.turn(), snapshot.rig(),
                snapshot.fabric(), snapshot.geometry());
    }

    /**
     * Moving-to-immovable phase: when the continuous hull has stopped at a
     * safe integer anchor / cardinal parking angle, rebuild ordinary blocks
     * and BlockEntities from the validated cargo payload.
     */
    public static boolean restoreFromMotion(ServerLevel level, BlockPos target,
            int parkingTurn, MotionSnapshot snapshot) {
        if (snapshot == null || parkingTurn < 0 || parkingTurn > 3) return false;
        return place(level, target, parkingTurn, snapshot.cargo(),
                snapshot.rig(), snapshot.fabric(), snapshot.geometry());
    }

    /** Deterministic local block palette. The moving renderer transforms these
     * blocks by continuous pose rather than placing them in chunk sections. */
    public static Map<BlockPos, BlockState> localMotionPalette(MotionSnapshot snapshot) {
        if (snapshot == null) return Map.of();
        Map<BlockPos, BlockState> result = new LinkedHashMap<>(
                layout(BlockPos.ZERO, 0, snapshot.rig(), snapshot.fabric(), snapshot.geometry()));
        List<Cargo> cargo = decode(snapshot.cargo());
        if (cargo == null) return Map.of();
        for (Cargo c : cargo) result.put(new BlockPos(c.x(), c.y(), c.z()), c.state());
        return java.util.Collections.unmodifiableMap(result);
    }

    public static int cargoKind(net.minecraft.world.level.Level level, BlockPos anchor, int turns, int floor, int x, int z) {
        int lx = x - 3, lz = z - 5, y = floor == 0 ? 1 : 5;
        if (!cargoCell(lx, y, lz)) return 0;
        return kind(level.getBlockState(world(anchor, turns, lx, y, lz)));
    }

    private FirstShipBlockHull() {}
}
