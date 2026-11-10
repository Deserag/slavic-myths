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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Server-side V9 native-block operations. Old V2-V8 vessels never enter this
 * code. Every edit validates its entire source, target and attached cargo.
 * The V9 palette is also the ONLY source for moving renderer/clearance.
 */
public final class ShipHullV9 {
    private static final int UPDATE = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final int MAX_CARGO = 128;
    private record Snapshot(Map<BlockPos, BlockState> hull, ListTag cargo) {}

    public static BlockPos world(BlockPos anchor, int turns, int x, int y, int z) {
        return FirstShipBlockHull.world(anchor, turns, x, y, z);
    }

    public static Map<BlockPos, BlockState> layout(BlockPos anchor, int turns,
            ShipSuperstructurePlan design) {
        ShipSuperstructureData.validateBounds(design);
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (var entry : ShipHullVoxelLayout.generate(design).entrySet()) {
            ShipHullVoxelLayout.Cell cell = entry.getKey();
            ShipHullVoxelLayout.Part part = entry.getValue();
            if (part == ShipHullVoxelLayout.Part.SAIL) continue;
            BlockState state = state(part, turns, cell.y());
            result.put(world(anchor, turns, cell.x(), cell.y(), cell.z()), state);
        }
        return result;
    }

    private static BlockState state(ShipHullVoxelLayout.Part part, int turns, int y) {
        return switch (part) {
            case KEEL -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, turns % 2 == 0
                            ? Direction.Axis.Z : Direction.Axis.X);
            case HULL, DECK, BRIDGE -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case FRAME, MAST -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            case RAIL -> Blocks.SPRUCE_FENCE.defaultBlockState();
            case LADDER -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,
                    switch (Math.floorMod(turns, 4)) {
                        case 0 -> Direction.EAST;
                        case 1 -> Direction.SOUTH;
                        case 2 -> Direction.WEST;
                        default -> Direction.NORTH;
                    });
            case YARD -> Blocks.DARK_OAK_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS,
                            turns % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z);
            case CABIN_WALL -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case WINDOW -> Blocks.GLASS.defaultBlockState();
            case DOOR -> Blocks.SPRUCE_DOOR.defaultBlockState()
                    .setValue(DoorBlock.HALF, y % 2 == 0
                            ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                    .setValue(DoorBlock.FACING, switch (Math.floorMod(turns, 4)) {
                        case 0 -> Direction.SOUTH;
                        case 1 -> Direction.WEST;
                        case 2 -> Direction.NORTH;
                        default -> Direction.EAST;
                    });
            case HELM -> Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
            case SAIL -> throw new IllegalArgumentException("Visual cloth is not a solid block");
        };
    }

    private static int kind(BlockState state) {
        if (state.is(Blocks.CHEST)) return 1;
        if (state.is(Blocks.BARREL)) return 2;
        if (state.is(Blocks.CRAFTING_TABLE)) return 3;
        if (state.is(Blocks.FURNACE)) return 4;
        return 0;
    }

    private static BlockState kindState(int kind) {
        return switch (kind) {
            case 1 -> Blocks.CHEST.defaultBlockState();
            case 2 -> Blocks.BARREL.defaultBlockState();
            case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
            case 4 -> Blocks.FURNACE.defaultBlockState();
            default -> throw new IllegalArgumentException("Unsupported cargo");
        };
    }

    /** Any floor tile that is not reserved for structural support is valid. */
    private static boolean cargoCell(ShipSuperstructurePlan design, int x, int y, int z) {
        if (y != 1 && y != 5 && y != 9 && y != 13) return false;
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> layout =
                ShipHullVoxelLayout.generate(design);
        ShipHullVoxelLayout.Part below = layout.get(new ShipHullVoxelLayout.Cell(x, y - 1, z));
        return below == ShipHullVoxelLayout.Part.HULL
                || below == ShipHullVoxelLayout.Part.KEEL
                || below == ShipHullVoxelLayout.Part.DECK
                || below == ShipHullVoxelLayout.Part.BRIDGE;
    }

    private static ListTag inspectCargo(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design, Map<BlockPos, BlockState> hull) {
        ListTag list = new ListTag();
        int hw = design.hullWidth() / 2, hl = design.hullLength() / 2;
        for (int z = -hl; z <= hl; z++) for (int x = -hw; x <= hw; x++) {
            if (Math.abs(x) > ShipHullVoxelLayout.halfBeam(design, z)) continue;
            for (int y : new int[]{1, 5, 9, 13}) {
                BlockPos pos = world(anchor, turns, x, y, z);
                if (hull.containsKey(pos)) continue;
                if (!level.hasChunkAt(pos)) return null;
                BlockState observed = level.getBlockState(pos);
                if (observed.isAir()) continue;
                if (!cargoCell(design, x, y, z) || kind(observed) == 0
                        || list.size() >= MAX_CARGO) return null;
                BlockEntity be = level.getBlockEntity(pos);
                if (kind(observed) != 3 && be == null) return null;
                CompoundTag item = new CompoundTag();
                item.putInt("X", x); item.putInt("Y", y); item.putInt("Z", z);
                item.putInt("Kind", kind(observed));
                item.put("BlockEntity", be == null ? new CompoundTag()
                        : be.saveWithFullMetadata(level.registryAccess()));
                list.add(item);
            }
        }
        return list;
    }

    public static boolean validCargo(ShipSuperstructurePlan design, ListTag cargo) {
        if (design == null || cargo == null || cargo.size() > MAX_CARGO) return false;
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> layout =
                ShipHullVoxelLayout.generate(design);
        Set<ShipHullVoxelLayout.Cell> occupied = new HashSet<>();
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag entry = cargo.getCompound(i);
            int x = entry.getInt("X"), y = entry.getInt("Y"), z = entry.getInt("Z");
            int type = entry.getInt("Kind");
            ShipHullVoxelLayout.Cell cell = new ShipHullVoxelLayout.Cell(x, y, z);
            if (type < 1 || type > 4 || !occupied.add(cell)
                    || layout.containsKey(cell) || !cargoCell(design, x, y, z)
                    || (type != 3 && !entry.contains("BlockEntity", Tag.TAG_COMPOUND)))
                return false;
        }
        return true;
    }

    private static Snapshot inspect(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design) {
        Map<BlockPos, BlockState> expected = layout(anchor, turns, design);
        for (var entry : expected.entrySet()) {
            if (!level.hasChunkAt(entry.getKey())
                    || level.getBlockState(entry.getKey()).getBlock()
                    != entry.getValue().getBlock()) return null;
        }
        ListTag cargo = inspectCargo(level, anchor, turns, design, expected);
        if (cargo == null || !validCargo(design, cargo)) return null;
        Set<BlockPos> owned = new HashSet<>(expected.keySet());
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag item = cargo.getCompound(i);
            owned.add(world(anchor, turns, item.getInt("X"), item.getInt("Y"), item.getInt("Z")));
        }
        // Refuse to detach any untracked player blocks within enclosed hull air.
        // This is intentionally stricter than dropping an unknown block on the floor.
        int hw = design.hullWidth() / 2, hl = design.hullLength() / 2;
        int maxY = 5 + design.decks() * 4;
        for (int z = -hl; z <= hl; z++) for (int x = -hw; x <= hw; x++) {
            if (Math.abs(x) > ShipHullVoxelLayout.halfBeam(design, z)) continue;
            for (int y = 1; y <= maxY; y++) {
                BlockPos pos = world(anchor, turns, x, y, z);
                if (!level.hasChunkAt(pos)) return null;
                if (!owned.contains(pos) && !level.getBlockState(pos).isAir()) return null;
            }
        }
        return new Snapshot(expected, cargo);
    }

    public static ListTag pack(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design) {
        Snapshot snapshot = inspect(level, anchor, turns, design);
        return snapshot == null ? null : snapshot.cargo().copy();
    }

    /** The hull cannot be placed on terrain or on an existing block entity. */
    private static boolean clearToBuild(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design, ListTag cargo) {
        if (!validCargo(design, cargo)) return false;
        Map<BlockPos, BlockState> proposed = layout(anchor, turns, design);
        Set<BlockPos> allowed = new HashSet<>(proposed.keySet());
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag c = cargo.getCompound(i);
            allowed.add(world(anchor, turns, c.getInt("X"), c.getInt("Y"), c.getInt("Z")));
        }
        int hw = design.hullWidth() / 2, hl = design.hullLength() / 2;
        // Include air of every interior compartment, not only solid members.
        int maxY = 5 + design.decks() * 4;
        for (int z = -hl; z <= hl; z++) for (int x = -hw; x <= hw; x++)
            for (int y = 0; y <= maxY; y++) {
                if (ShipHullVoxelLayout.halfBeam(design, z) < Math.abs(x)) continue;
                BlockPos pos = world(anchor, turns, x, y, z);
                if (!level.hasChunkAt(pos)) return false;
                if (level.getBlockEntity(pos) != null) return false;
                if (y == 0 && (!level.getFluidState(pos).is(FluidTags.WATER)
                        || !level.getFluidState(pos).isSource()
                        || !level.getFluidState(pos.below()).is(FluidTags.WATER))) return false;
                if (y > 0 && !level.getBlockState(pos).isAir()) return false;
            }
        // Masts and yards outside the deck's footprint must also be free.
        for (BlockPos pos : allowed) {
            if (!level.hasChunkAt(pos) || level.getBlockEntity(pos) != null) return false;
            if (pos.getY() > anchor.getY() && !level.getBlockState(pos).isAir()) return false;
        }
        return true;
    }

    private static boolean build(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design, ListTag cargo) {
        List<Map.Entry<BlockPos, BlockState>> entries = new ArrayList<>(
                layout(anchor, turns, design).entrySet());
        entries.sort(Comparator.comparingInt(a -> a.getKey().getY()));
        for (var entry : entries)
            if (!level.setBlock(entry.getKey(), entry.getValue(), UPDATE)) return false;
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag c = cargo.getCompound(i);
            int x = c.getInt("X"), y = c.getInt("Y"), z = c.getInt("Z");
            BlockPos pos = world(anchor, turns, x, y, z);
            BlockState state = kindState(c.getInt("Kind"));
            if (!level.setBlock(pos, state, UPDATE)) return false;
            if (c.getInt("Kind") != 3) {
                BlockEntity be = BlockEntity.loadStatic(pos, state,
                        c.getCompound("BlockEntity"), level.registryAccess());
                if (be == null) return false;
                level.setBlockEntity(be);
                be.setChanged();
            }
        }
        return true;
    }

    private static void clear(ServerLevel level, BlockPos anchor,
            Map<BlockPos, BlockState> hull, ListTag cargo, int turns) {
        List<BlockPos> positions = new ArrayList<>(hull.keySet());
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag c = cargo.getCompound(i);
            BlockPos pos = world(anchor, turns, c.getInt("X"), c.getInt("Y"), c.getInt("Z"));
            level.removeBlockEntity(pos);
            positions.add(pos);
        }
        positions.sort(Comparator.comparingInt(BlockPos::getY).reversed());
        for (BlockPos pos : positions)
            level.setBlock(pos, pos.getY() == anchor.getY()
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), UPDATE);
    }

    public static boolean place(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design, ListTag cargo) {
        if (!clearToBuild(level, anchor, turns, design, cargo)) return false;
        // Validate all cargo NBT before world mutation.
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag c = cargo.getCompound(i);
            if (c.getInt("Kind") == 3) continue;
            BlockPos p = world(anchor, turns, c.getInt("X"), c.getInt("Y"), c.getInt("Z"));
            if (BlockEntity.loadStatic(p, kindState(c.getInt("Kind")),
                    c.getCompound("BlockEntity"), level.registryAccess()) == null) return false;
        }
        if (build(level, anchor, turns, design, cargo)) return true;
        // Building can fail even after a successful preflight (e.g. a protection mod).
        // Roll back only the cells this transaction attempted to create.
        clear(level, anchor, layout(anchor, turns, design), cargo, turns);
        return false;
    }

    public static boolean remove(ServerLevel level, BlockPos anchor, int turns,
            ShipSuperstructurePlan design) {
        Snapshot original = inspect(level, anchor, turns, design);
        if (original == null) return false;
        clear(level, anchor, original.hull(), original.cargo(), turns);
        return true;
    }

    public static Map<BlockPos, BlockState> movingPalette(ShipSuperstructurePlan design,
            ListTag cargo) {
        if (!validCargo(design, cargo)) return Map.of();
        Map<BlockPos, BlockState> palette = new LinkedHashMap<>(
                layout(BlockPos.ZERO, 0, design));
        for (int i = 0; i < cargo.size(); i++) {
            CompoundTag c = cargo.getCompound(i);
            palette.put(new BlockPos(c.getInt("X"), c.getInt("Y"), c.getInt("Z")),
                    kindState(c.getInt("Kind")));
        }
        return java.util.Collections.unmodifiableMap(palette);
    }

    private ShipHullV9() {}
}
