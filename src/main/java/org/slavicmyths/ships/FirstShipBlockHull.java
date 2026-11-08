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
    private static final int MAX_Y = 21;
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
            default -> 21;
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
                addSail(blocks, anchor, turns, fabric, 7, 14);
                addSail(blocks, anchor, turns, fabric, 17, 19);
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

    private static Set<BlockPos> volume(BlockPos anchor, int turns, int rig, int fabric) {
        Set<BlockPos> volume = new HashSet<>(layout(anchor, turns, rig, fabric).keySet());
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

    /** Exact block positions are checked before anything is overwritten. */
    private static boolean canWrite(ServerLevel level, BlockPos anchor, int turns, Set<BlockPos> owned,
            List<Cargo> cargo, int rig, int fabric) {
        Set<BlockPos> target = volume(anchor, turns, rig, fabric);
        for (Cargo entry : cargo) {
            BlockPos point = world(anchor, turns, entry.x(), entry.y(), entry.z());
            if (!cargoCell(entry.x(), entry.y(), entry.z()) || !target.contains(point)) return false;
        }
        for (BlockPos pos : target) {
            if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.below())) return false;
            if (owned.contains(pos)) continue;
            int relativeY = pos.getY() - anchor.getY();
            if (relativeY == 0) {
                if (!level.getFluidState(pos).is(FluidTags.WATER)
                        || !level.getFluidState(pos).isSource()
                        || !level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
            } else if (!level.getBlockState(pos).isAir()) return false;
        }
        return true;
    }

    private static Snapshot inspect(ServerLevel level, BlockPos anchor, int turns, int rig, int fabric) {
        Map<BlockPos, BlockState> hull = layout(anchor, turns, rig, fabric);
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
        for (BlockPos pos : volume(anchor, turns)) {
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

    private static boolean build(ServerLevel level, BlockPos anchor, int turns, List<Cargo> cargo, int rig, int fabric) {
        Map<BlockPos, BlockState> blocks = layout(anchor, turns, rig, fabric);
        List<Map.Entry<BlockPos, BlockState>> ordered = new ArrayList<>(blocks.entrySet());
        ordered.sort(Comparator.comparingInt(e -> e.getKey().getY()));
        for (var entry : ordered) if (!level.setBlock(entry.getKey(), entry.getValue(), UPDATE)) return false;
        return restoreCargo(level, anchor, turns, cargo);
    }

    /** Transaction checks source intact, future footprint safe, then moves blocks once. */
    public static boolean move(ServerLevel level, BlockPos previous, int oldTurn,
            BlockPos target, int nextTurn, int oldRig, int oldFabric, int nextRig, int nextFabric) {
        Snapshot snapshot = inspect(level, previous, oldTurn, oldRig, oldFabric);
        if (snapshot == null) return false;
        Set<BlockPos> owned = new HashSet<>(snapshot.blocks().keySet());
        for (Cargo entry : snapshot.cargo())
            owned.add(world(previous, oldTurn, entry.x(), entry.y(), entry.z()));
        if (!canWrite(level, target, nextTurn, owned, snapshot.cargo(), nextRig, nextFabric)) return false;
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
        // All BlockEntities are serialized before clearing any old block.
        clear(level, previous, oldTurn, snapshot);
        return build(level, target, nextTurn, turned, nextRig, nextFabric);
    }

    public static boolean place(ServerLevel level, BlockPos anchor, int turns, ListTag packed, int rig, int fabric) {
        List<Cargo> cargo = decode(packed);
        if (cargo == null || !canWrite(level, anchor, turns, Set.of(), cargo, rig, fabric)) return false;
        for (Cargo entry : cargo) {
            if (entry.type() == 3) continue;
            BlockPos target = world(anchor, turns, entry.x(), entry.y(), entry.z());
            if (BlockEntity.loadStatic(target, entry.state(), entry.data(), level.registryAccess()) == null)
                return false;
        }
        return build(level, anchor, turns, cargo, rig, fabric);
    }

    public static ListTag pack(ServerLevel level, BlockPos anchor, int turns, int rig, int fabric) {
        Snapshot snapshot = inspect(level, anchor, turns, rig, fabric);
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
    public static boolean remove(ServerLevel level, BlockPos anchor, int turns, int rig, int fabric) {
        Snapshot snapshot = inspect(level, anchor, turns, rig, fabric);
        if (snapshot == null) return false;
        clear(level, anchor, turns, snapshot);
        return true;
    }

    public static int cargoKind(net.minecraft.world.level.Level level, BlockPos anchor, int turns, int floor, int x, int z) {
        int lx = x - 3, lz = z - 5, y = floor == 0 ? 1 : 5;
        if (!cargoCell(lx, y, lz)) return 0;
        return kind(level.getBlockState(world(anchor, turns, lx, y, lz)));
    }

    private FirstShipBlockHull() {}
}
