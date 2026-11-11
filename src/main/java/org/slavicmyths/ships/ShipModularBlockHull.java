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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Server-only, versioned full-block construction of modular wooden vessels.
 * No existing V2-V8 ship is touched. Every mutation is preflighted and uses
 * a snapshot; malformed source or obstructed destination is left unchanged.
 */
public final class ShipModularBlockHull {
    private static final int UPDATE = 2 | 16;
    private static final int MAX_BLOCKS = 50000;
    private record Cargo(int x, int y, int z, int kind, BlockState state, CompoundTag data) {}
    private record Snapshot(Map<BlockPos, BlockState> hull, List<Cargo> cargo) {}

    private static BlockState state(ShipHullVoxelLayout.Part part, int turns, int y) {
        return switch (part) {
            case KEEL -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(
                    RotatedPillarBlock.AXIS, turns % 2 == 0 ? Direction.Axis.Z : Direction.Axis.X);
            case HULL, DECK -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case FRAME, MAST -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            case RAIL -> Blocks.SPRUCE_FENCE.defaultBlockState();
            case LADDER -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,
                    switch (Math.floorMod(turns, 4)) {
                        case 0 -> Direction.EAST;
                        case 1 -> Direction.SOUTH;
                        case 2 -> Direction.WEST;
                        default -> Direction.NORTH;
                    });
            case YARD -> Blocks.DARK_OAK_LOG.defaultBlockState().setValue(
                    RotatedPillarBlock.AXIS,
                    turns % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z);
            case SAIL -> Blocks.WHITE_WOOL.defaultBlockState();
            case CABIN_WALL -> Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
            case WINDOW -> Blocks.GLASS_PANE.defaultBlockState();
            case DOOR -> Blocks.OAK_DOOR.defaultBlockState()
                    .setValue(DoorBlock.HALF,
                            y == 6 ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                    .setValue(DoorBlock.FACING, switch (Math.floorMod(turns, 4)) {
                        case 0 -> Direction.SOUTH;
                        case 1 -> Direction.WEST;
                        case 2 -> Direction.NORTH;
                        default -> Direction.EAST;
                    });
            case BRIDGE -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            case HELM -> Blocks.DARK_OAK_FENCE.defaultBlockState();
            case LIGHT -> Blocks.LANTERN.defaultBlockState();
        };
    }

    public static Map<BlockPos, BlockState> localPalette(ShipBuildConfiguration config) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (var entry : config.layout().entrySet()) {
            if (entry.getValue() == ShipHullVoxelLayout.Part.SAIL) continue;
            var c = entry.getKey();
            blocks.put(new BlockPos(c.x(), c.y(), c.z()), state(entry.getValue(), 0, c.y()));
        }
        connectFenceRails(blocks);
        return java.util.Collections.unmodifiableMap(blocks);
    }

    /** Complete local collision/renderer palette, including attached containers. */
    public static Map<BlockPos, BlockState> localMotionPalette(
            ShipBuildConfiguration config, ListTag cargo) {
        if (config == null) return Map.of();
        List<Cargo> unpacked = decode(config, cargo);
        if (unpacked == null) return Map.of();
        Map<BlockPos, BlockState> result = new LinkedHashMap<>(localPalette(config));
        for (Cargo entry : unpacked)
            result.put(new BlockPos(entry.x(), entry.y(), entry.z()), entry.state());
        return java.util.Collections.unmodifiableMap(result);
    }

    private static Map<BlockPos, BlockState> worldPalette(ShipBuildConfiguration config,
            BlockPos anchor, int turns) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (var entry : config.layout().entrySet()) {
            if (entry.getValue() == ShipHullVoxelLayout.Part.SAIL) continue;
            var c = entry.getKey();
            blocks.put(FirstShipBlockHull.world(anchor, turns, c.x(), c.y(), c.z()),
                    state(entry.getValue(), turns, c.y()));
        }
        if (blocks.size() > MAX_BLOCKS) throw new IllegalArgumentException("Oversized ship hull");
        connectFenceRails(blocks);
        return blocks;
    }

    private static void connectFenceRails(Map<BlockPos, BlockState> blocks) {
        for (var entry : blocks.entrySet()) {
            if (!entry.getValue().is(Blocks.SPRUCE_FENCE)) continue;
            BlockPos p = entry.getKey();
            BlockState state = entry.getValue()
                    .setValue(net.minecraft.world.level.block.FenceBlock.NORTH,
                            blocks.containsKey(p.north()) && blocks.get(p.north()).is(Blocks.SPRUCE_FENCE))
                    .setValue(net.minecraft.world.level.block.FenceBlock.SOUTH,
                            blocks.containsKey(p.south()) && blocks.get(p.south()).is(Blocks.SPRUCE_FENCE))
                    .setValue(net.minecraft.world.level.block.FenceBlock.EAST,
                            blocks.containsKey(p.east()) && blocks.get(p.east()).is(Blocks.SPRUCE_FENCE))
                    .setValue(net.minecraft.world.level.block.FenceBlock.WEST,
                            blocks.containsKey(p.west()) && blocks.get(p.west()).is(Blocks.SPRUCE_FENCE));
            entry.setValue(state);
        }
    }

    private static int kind(BlockState state) {
        if (state.is(Blocks.CHEST)) return 1;
        if (state.is(Blocks.BARREL)) return 2;
        if (state.is(Blocks.CRAFTING_TABLE)) return 3;
        if (state.is(Blocks.FURNACE)) return 4;
        return 0;
    }

    private static BlockState kindState(int type) {
        return switch (type) {
            case 1 -> Blocks.CHEST.defaultBlockState();
            case 2 -> Blocks.BARREL.defaultBlockState();
            case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
            case 4 -> Blocks.FURNACE.defaultBlockState();
            default -> throw new IllegalArgumentException("Invalid attached block kind");
        };
    }

    private static boolean cargoCell(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> plan,
            int x, int y, int z) {
        // A freestanding container may occupy any clear part of a deck, not a
        // mast, rail, hatch or cabin wall.
        var cell = new ShipHullVoxelLayout.Cell(x, y, z);
        if (plan.containsKey(cell)) return false;
        var below = plan.get(new ShipHullVoxelLayout.Cell(x, y - 1, z));
        return below == ShipHullVoxelLayout.Part.DECK
                || below == ShipHullVoxelLayout.Part.BRIDGE;
    }

    private static Snapshot inspect(ServerLevel level, BlockPos anchor, int turns,
            ShipBuildConfiguration config) {
        Map<BlockPos, BlockState> source = worldPalette(config, anchor, turns);
        for (var entry : source.entrySet()) {
            if (!level.hasChunkAt(entry.getKey())
                    || level.getBlockState(entry.getKey()).getBlock() != entry.getValue().getBlock())
                return null;
        }
        List<Cargo> cargo = new ArrayList<>();
        ShipSuperstructurePlan structure = config.structure();
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cargoPlan =
                config.layout();
        int halfWidth = config.width() / 2, halfLength = config.length() / 2;
        int maxY = Math.max(24, 4 + config.decks() * 4);
        for (int z = -halfLength; z <= halfLength; z++)
            for (int x = -halfWidth; x <= halfWidth; x++)
                for (int y = 1; y <= maxY; y++) {
                    if (Math.abs(x) > ShipHullVoxelLayout.halfBeam(structure, z))
                        continue;
                    BlockPos world = FirstShipBlockHull.world(anchor, turns, x, y, z);
                    if (!level.hasChunkAt(world)) return null;
                    if (source.containsKey(world)) continue;
                    BlockState state = level.getBlockState(world);
                    if (state.isAir()) continue;
                    int type = kind(state);
                    if (type == 0 || !cargoCell(cargoPlan, x, y, z)) return null;
                    BlockEntity blockEntity = level.getBlockEntity(world);
                    if (type != 3 && blockEntity == null) return null;
                    CompoundTag data = blockEntity == null ? new CompoundTag()
                            : blockEntity.saveWithFullMetadata(level.registryAccess());
                    cargo.add(new Cargo(x, y, z, type, state, data));
                }
        return new Snapshot(source, cargo);
    }

    private static List<Cargo> decode(ShipBuildConfiguration config, ListTag entries) {
        if (entries == null || entries.size() > config.width() * config.length() * config.decks())
            return null;
        List<Cargo> result = new ArrayList<>();
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cargoPlan =
                config.layout();
        Set<String> occupied = new HashSet<>();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            int x = entry.getInt("X"), y = entry.getInt("Y"), z = entry.getInt("Z");
            int kind = entry.getInt("Kind");
            if (kind < 1 || kind > 4 || !cargoCell(cargoPlan, x, y, z)
                    || !occupied.add(x + ":" + y + ":" + z)
                    || !entry.contains("State", Tag.TAG_COMPOUND)
                    || (kind != 3 && !(entry.contains("BlockEntity", Tag.TAG_COMPOUND) || entry.contains("Data", Tag.TAG_COMPOUND))))
                return null;
            BlockState state = kindState(kind);
            if (entry.contains("State", Tag.TAG_COMPOUND)) {
                CompoundTag properties = entry.getCompound("State");
                for (Property<?> property : state.getProperties()) {
                    if (!properties.contains(property.getName(), Tag.TAG_STRING)) return null;
                    try {
                        state = withProperty(state, property, properties.getString(property.getName()));
                    } catch (RuntimeException invalid) { return null; }
                }
            }
            result.add(new Cargo(x, y, z, kind, state,
                    entry.getCompound(entry.contains("BlockEntity", Tag.TAG_COMPOUND) ? "BlockEntity" : "Data").copy()));
        }
        return result;
    }

    private static <T extends Comparable<T>> String stateProperty(
            BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static <T extends Comparable<T>> BlockState withProperty(
            BlockState state, Property<T> property, String encoded) {
        T value = property.getValue(encoded).orElseThrow(
                () -> new IllegalArgumentException("Invalid cargo block state"));
        return state.setValue(property, value);
    }

    private static ListTag encode(List<Cargo> cargo) {
        ListTag result = new ListTag();
        for (Cargo entry : cargo) {
            CompoundTag row = new CompoundTag();
            row.putInt("X", entry.x());
            row.putInt("Y", entry.y());
            row.putInt("Z", entry.z());
            row.putInt("Kind", entry.kind());
            CompoundTag properties = new CompoundTag();
            for (Property<?> property : entry.state().getProperties())
                properties.putString(property.getName(), stateProperty(entry.state(), property));
            row.put("State", properties);
            row.put("BlockEntity", entry.data().copy());
            result.add(row);
        }
        return result;
    }

    /** Rotate saved cargo states when a moving hull parks on a new heading. */
    public static ListTag rotateCargo(ShipBuildConfiguration config, ListTag entries,
            int oldTurn, int newTurn) {
        List<Cargo> source = decode(config, entries);
        if (source == null) return null;
        int steps = Math.floorMod(newTurn - oldTurn, 4);
        List<Cargo> rotated = new ArrayList<>(source.size());
        for (Cargo entry : source) {
            BlockState state = entry.state();
            for (int n = 0; n < steps; n++) state = state.rotate(Rotation.CLOCKWISE_90);
            rotated.add(new Cargo(entry.x(), entry.y(), entry.z(),
                    entry.kind(), state, entry.data()));
        }
        return encode(rotated);
    }

    private static boolean validateRestore(ServerLevel level, BlockPos anchor, int turns,
            List<Cargo> cargo, Map<BlockPos, BlockState> target) {
        for (Cargo entry : cargo) {
            if (target.containsKey(FirstShipBlockHull.world(
                    anchor, turns, entry.x(), entry.y(), entry.z()))) return false;
            if (entry.kind() == 3) continue;
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            if (BlockEntity.loadStatic(pos, entry.state(),
                    entry.data(), level.registryAccess()) == null) return false;
        }
        return true;
    }

    private static boolean clearSpace(ServerLevel level, BlockPos anchor,
            Map<BlockPos, BlockState> planned, Set<BlockPos> owned,
            ShipBuildConfiguration config, int turns) {
        for (BlockPos pos : planned.keySet()) {
            if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.below())) return false;
            if (owned.contains(pos)) continue;
            if (level.getBlockEntity(pos) != null) return false;
            if (pos.getY() == anchor.getY()) {
                if (!level.getFluidState(pos).is(FluidTags.WATER)
                        || !level.getFluidState(pos).isSource()
                        || !level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
            } else if (!level.getBlockState(pos).isAir()) return false;
        }
        // Check interior AIR too. A hollow ship must never engulf terrain,
        // existing buildings or unloaded chunks between its timber supports.
        int half = config.length() / 2;
        int maxY = Math.max(24, 4 + config.decks() * 4);
        for (int z = -half; z <= half; z++)
            for (int x = -ShipHullVoxelLayout.halfBeam(config.structure(), z);
                 x <= ShipHullVoxelLayout.halfBeam(config.structure(), z); x++)
                for (int y = 1; y <= maxY; y++) {
                    BlockPos p = FirstShipBlockHull.world(anchor, turns, x, y, z);
                    if (!level.hasChunkAt(p)) return false;
                    if (!planned.containsKey(p) && !owned.contains(p)
                            && !level.getBlockState(p).isAir()) return false;
                }
        return true;
    }

    private static boolean placeBlocks(ServerLevel level,
            Map<BlockPos, BlockState> blocks, List<Cargo> cargo,
            BlockPos anchor, int turns) {
        List<Map.Entry<BlockPos, BlockState>> ordered = new ArrayList<>(blocks.entrySet());
        ordered.sort(Comparator.comparingInt(x -> x.getKey().getY()));
        for (var entry : ordered)
            if (!level.setBlock(entry.getKey(), entry.getValue(), UPDATE)) return false;
        for (Cargo entry : cargo) {
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            if (!level.setBlock(pos, entry.state(), UPDATE)) return false;
            if (entry.kind() != 3) {
                BlockEntity be = BlockEntity.loadStatic(
                        pos, entry.state(), entry.data(), level.registryAccess());
                if (be == null) return false;
                level.setBlockEntity(be);
                be.setChanged();
            }
        }
        return true;
    }

    private static boolean clearBlocks(ServerLevel level, BlockPos anchor,
            Map<BlockPos, BlockState> owned, List<Cargo> cargo, int turns) {
        Set<BlockPos> positions = new HashSet<>(owned.keySet());
        for (Cargo entry : cargo) {
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            level.removeBlockEntity(pos);
            positions.add(pos);
        }
        List<BlockPos> ordered = new ArrayList<>(positions);
        ordered.sort(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed());
        boolean success = true;
        for (BlockPos pos : ordered) {
            if (!level.setBlock(pos, pos.getY() == anchor.getY()
                    ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), UPDATE)) success = false;
        }
        return success;
    }

    public static boolean place(ServerLevel level, BlockPos anchor,
            int turns, ShipBuildConfiguration config, ListTag packedCargo) {
        if (level == null || anchor == null || config == null) return false;
        List<Cargo> cargo = decode(config, packedCargo);
        if (cargo == null) return false;
        Map<BlockPos, BlockState> blocks = worldPalette(config, anchor, turns);
        if (!validateRestore(level, anchor, turns, cargo, blocks)
                || !clearSpace(level, anchor, blocks, Set.of(), config, turns)) return false;
        for (Cargo entry : cargo) {
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            if (!level.getBlockState(pos).isAir() || level.getBlockEntity(pos) != null)
                return false;
        }
        if (placeBlocks(level, blocks, cargo, anchor, turns)) return true;
        // All target positions were water/air before this operation.
        clearBlocks(level, anchor, blocks, cargo, turns);
        return false;
    }

    /** Capture the entire validated cargo payload, returning null on tampering. */
    public static ListTag pack(ServerLevel level, BlockPos anchor,
            int turns, ShipBuildConfiguration config) {
        Snapshot source = inspect(level, anchor, turns, config);
        return source == null ? null : encode(source.cargo());
    }

    public static boolean remove(ServerLevel level, BlockPos anchor,
            int turns, ShipBuildConfiguration config) {
        Snapshot source = inspect(level, anchor, turns, config);
        if (source == null) return false;
        if (clearBlocks(level, anchor, source.hull(), source.cargo(), turns))
            return true;
        if (!placeBlocks(level, source.hull(), source.cargo(), anchor, turns))
            throw new IllegalStateException("Failed to restore modular hull after incomplete removal");
        return false;
    }

    /**
     * Single server-thread transaction, including arbitrary legal cargo.
     * No world changes occur before all source/destination checks succeed.
     */
    public static boolean rebuild(ServerLevel level, BlockPos oldAnchor, int oldTurn,
            ShipBuildConfiguration previous, BlockPos target, int nextTurn,
            ShipBuildConfiguration next) {
        if (previous == null || next == null
                || previous.shipClass() != next.shipClass()) return false;
        Snapshot source = inspect(level, oldAnchor, oldTurn, previous);
        if (source == null) return false;
        Map<BlockPos, BlockState> targetHull = worldPalette(next, target, nextTurn);
        Set<BlockPos> owned = new HashSet<>(source.hull().keySet());
        for (Cargo entry : source.cargo()) owned.add(FirstShipBlockHull.world(
                oldAnchor, oldTurn, entry.x(), entry.y(), entry.z()));
        int rotation = Math.floorMod(nextTurn - oldTurn, 4);
        List<Cargo> transported = new ArrayList<>();
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cargoPlan =
                next.layout();
        for (Cargo entry : source.cargo()) {
            if (!cargoCell(cargoPlan, entry.x(), entry.y(), entry.z())) return false;
            BlockState state = entry.state();
            for (int n = 0; n < rotation; n++) state = state.rotate(Rotation.CLOCKWISE_90);
            transported.add(new Cargo(entry.x(), entry.y(), entry.z(),
                    entry.kind(), state, entry.data()));
        }
        if (!validateRestore(level, target, nextTurn, transported, targetHull)
                || !clearSpace(level, target, targetHull, owned, next, nextTurn)) return false;
        for (Cargo entry : transported) {
            BlockPos p = FirstShipBlockHull.world(target, nextTurn,
                    entry.x(), entry.y(), entry.z());
            if (!owned.contains(p) && !level.getBlockState(p).isAir()) return false;
        }
        if (!clearBlocks(level, oldAnchor, source.hull(), source.cargo(), oldTurn)) {
            if (!placeBlocks(level, source.hull(), source.cargo(), oldAnchor, oldTurn))
                throw new IllegalStateException("Failed to restore modular hull after failed clear");
            return false;
        }
        if (placeBlocks(level, targetHull, transported, target, nextTurn)) return true;
        clearBlocks(level, target, targetHull, transported, nextTurn);
        if (!placeBlocks(level, source.hull(), source.cargo(), oldAnchor, oldTurn))
            throw new IllegalStateException("Failed to restore original modular hull after rebuild failure");
        return false;
    }

    private ShipModularBlockHull() {}
}
