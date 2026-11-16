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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;

/**
 * Server-only, versioned full-block construction of modular wooden vessels.
 * No existing V2-V8 ship is touched. Every mutation is preflighted and uses
 * a snapshot; malformed source or obstructed destination is left unchanged.
 */
public final class ShipModularBlockHull {
    private static final int UPDATE = 2 | 16;
    private static final int MAX_BLOCKS = 50000;
    private static final int MISSING_HULL = 5;
    private static final int GENERAL_CARGO = 6;
    private record Cargo(int x, int y, int z, int kind, BlockState state, CompoundTag data) {}
    private record Snapshot(Map<BlockPos, BlockState> hull, List<Cargo> cargo) {}

    private static BlockState state(ShipHullVoxelLayout.Part part,
            int turns, int y, int x, ShipBuildConfiguration config) {
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
            case STAIR -> Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(
                    // The upper half points toward the stern (-Z). All
                    // modular flights now climb the same local direction.
                    StairBlock.FACING, switch (Math.floorMod(turns, 4)) {
                        case 0 -> Direction.NORTH;
                        case 1 -> Direction.EAST;
                        case 2 -> Direction.SOUTH;
                        default -> Direction.WEST;
                    });
            case YARD -> Blocks.DARK_OAK_LOG.defaultBlockState().setValue(
                    RotatedPillarBlock.AXIS,
                    turns % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z);
            case SAIL -> Blocks.WHITE_WOOL.defaultBlockState();
            case CABIN_WALL -> Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
            // Full glass blocks render consistently regardless of whether surrounding
            // walls were placed before or after the windows.
            case WINDOW -> Blocks.GLASS.defaultBlockState();
            case DOOR -> Blocks.OAK_DOOR.defaultBlockState()
                    .setValue(DoorBlock.HALF,
                            y == 6 || y == 12 && config.hullRevision() != 4
                                    || y == 13 && config.hullRevision() == 4
                                    || y == 17 && config.shipClass() == ShipSectionPlan.ShipClass.LARGE
                                            && config.hullRevision() == 3
                                    ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                    .setValue(DoorBlock.FACING, switch (Math.floorMod(turns, 4)) {
                        case 0 -> Direction.SOUTH;
                        case 1 -> Direction.WEST;
                        case 2 -> Direction.NORTH;
                        default -> Direction.EAST;
                    });
            case BRIDGE -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            case HELM -> Blocks.DARK_OAK_FENCE.defaultBlockState();
            case LANTERN -> Blocks.LANTERN.defaultBlockState();
            case HANGING_LANTERN -> Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true);
            case TRIM -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            case GUNPORT_SHUTTER -> Blocks.AIR.defaultBlockState();
            case BOWSPRIT -> Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(
                    RotatedPillarBlock.AXIS, turns % 2 == 0 ? Direction.Axis.Z : Direction.Axis.X);
        };
    }

    public static Map<BlockPos, BlockState> localPalette(ShipBuildConfiguration config) {
        if (config == null || config.withdrawn()) return Map.of();
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (var entry : ShipHullVoxelLayout.generate(config.structure()).entrySet()) {
            if (entry.getValue() == ShipHullVoxelLayout.Part.SAIL
                    || entry.getValue() == ShipHullVoxelLayout.Part.GUNPORT_SHUTTER) continue;
            var c = entry.getKey();
            blocks.put(new BlockPos(c.x(), c.y(), c.z()),
                    state(entry.getValue(), 0, c.y(), c.x(), config));
        }
        connectFenceRails(blocks);
        return java.util.Collections.unmodifiableMap(blocks);
    }

    /** Complete local collision/renderer palette, including attached containers. */
    public static Map<BlockPos, BlockState> localMotionPalette(
            ShipBuildConfiguration config, ListTag cargo) {
        if (config == null || config.withdrawn()) return Map.of();
        List<Cargo> unpacked = decode(config, cargo);
        if (unpacked == null) return Map.of();
        Map<BlockPos, BlockState> result = new LinkedHashMap<>(localPalette(config));
        for (Cargo entry : unpacked) {
            BlockPos local = new BlockPos(entry.x(), entry.y(), entry.z());
            if (entry.kind() == MISSING_HULL) result.remove(local);
            else result.put(local, entry.state());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    /**
     * Public cosmetic moving-cargo data only. Compatibility: older saves and
     * tracked ships may still send four- or five-field rows without properties.
     * The sixth field carries public state properties, never inventory NBT.
     */
    public static ListTag decodeVisibleCargo(String view) {
        ListTag cargo = new ListTag();
        if (view == null || view.isBlank()) return cargo;
        for (String token : view.split(";")) {
            String[] fields = token.split(",", -1);
            if (fields.length < 4 || fields.length > 6) continue;
            try {
                CompoundTag row = new CompoundTag();
                row.putInt("X", Integer.parseInt(fields[0]));
                row.putInt("Y", Integer.parseInt(fields[1]));
                row.putInt("Z", Integer.parseInt(fields[2]));
                row.putInt("Kind", Integer.parseInt(fields[3]));
                if (fields.length >= 5 && !fields[4].isEmpty())
                    row.putString("BlockId", fields[4]);
                if (fields.length == 6) {
                    CompoundTag props = new CompoundTag();
                    for (String pair : fields[5].split("\\|")) {
                        int equals = pair.indexOf('=');
                        if (equals <= 0 || equals == pair.length() - 1) continue;
                        props.putString(pair.substring(0, equals), pair.substring(equals + 1));
                    }
                    row.put("State", props);
                }
                cargo.add(row);
            } catch (NumberFormatException invalid) {
                // Ignore client metadata tokens with invalid coordinates.
            }
        }
        return cargo;
    }

    /** Client collision uses public block types, never private inventory NBT. */
    public static Map<BlockPos, BlockState> localVisualPalette(
            ShipBuildConfiguration config, ListTag visibleCargo) {
        if (config == null || config.withdrawn()) return Map.of();
        Map<BlockPos, BlockState> result = new LinkedHashMap<>(localPalette(config));
        for (int i = 0; i < visibleCargo.size(); i++) {
            CompoundTag row = visibleCargo.getCompound(i);
            int x = row.getInt("X"), y = row.getInt("Y"), z = row.getInt("Z");
            if (Math.abs(x) > config.width() / 2 || Math.abs(z) > config.length() / 2
                    || y < 1 || y > 48) continue;
            BlockPos local = new BlockPos(x, y, z);
            int type = row.getInt("Kind");
            if (type == MISSING_HULL) {
                result.remove(local);
                continue;
            }
            BlockState state;
            if (type >= 1 && type <= 4) state = kindState(type);
            else if (type == GENERAL_CARGO) {
                ResourceLocation id = ResourceLocation.tryParse(row.getString("BlockId"));
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) continue;
                state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
            } else continue;
            if (row.contains("State", Tag.TAG_COMPOUND)) {
                CompoundTag props = row.getCompound("State");
                for (Property<?> property : state.getProperties())
                    if (props.contains(property.getName(), Tag.TAG_STRING)) {
                        try {
                            state = withProperty(state, property,
                                    props.getString(property.getName()));
                        } catch (RuntimeException invalid) {
                            // Leave the unaffected vanilla state intact.
                        }
                    }
            }
            result.putIfAbsent(local, state);
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private static Map<BlockPos, BlockState> worldPalette(ShipBuildConfiguration config,
            BlockPos anchor, int turns) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (var entry : ShipHullVoxelLayout.generate(config.structure()).entrySet()) {
            if (entry.getValue() == ShipHullVoxelLayout.Part.SAIL
                    || entry.getValue() == ShipHullVoxelLayout.Part.GUNPORT_SHUTTER) continue;
            var c = entry.getKey();
            blocks.put(FirstShipBlockHull.world(anchor, turns, c.x(), c.y(), c.z()),
                    state(entry.getValue(), turns, c.y(), c.x(), config));
        }
        if (blocks.size() > MAX_BLOCKS) throw new IllegalArgumentException("Oversized ship hull");
        connectFenceRails(blocks);
        return blocks;
    }

    /**
     * Ships placed before full-glass windows used GLASS_PANE. Accept those
     * legacy panes only where the current blueprint expects a glass window;
     * the next pack/restore or rebuild installs full glass blocks instead.
     */
    private static boolean matchesHullBlock(BlockState existing, BlockState expected) {
        return existing.getBlock() == expected.getBlock()
                || expected.is(Blocks.GLASS) && existing.is(Blocks.GLASS_PANE);
    }

    private static boolean matchesPhysicalHull(ServerLevel level,
            Map<BlockPos, BlockState> expected) {
        for (var entry : expected.entrySet())
            if (!level.hasChunkAt(entry.getKey())
                    || !matchesHullBlock(level.getBlockState(entry.getKey()),
                            entry.getValue())) return false;
        return true;
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

    private static boolean allowedSimple(BlockState state) {
        Block block = state.getBlock();
        if (ShipCargoPlacementGuard.forbidden(state)) return false;
        return block instanceof BedBlock || block instanceof CraftingTableBlock
                || block instanceof SlabBlock || block instanceof StairBlock
                || block instanceof FenceBlock || block.getClass() == Block.class;
    }

    private static int kind(BlockState state, BlockEntity blockEntity) {
        if (ShipCargoPlacementGuard.forbidden(state)) return 0;
        if (state.is(Blocks.CHEST)) return 1;
        if (state.is(Blocks.BARREL)) return 2;
        if (state.is(Blocks.CRAFTING_TABLE)) return 3;
        if (state.is(Blocks.FURNACE)) return 4;
        if (blockEntity instanceof Container) return GENERAL_CARGO;
        if (allowedSimple(state)) return GENERAL_CARGO;
        return 0;
    }

    private static BlockState kindState(int type) {
        return switch (type) {
            case 1 -> Blocks.CHEST.defaultBlockState();
            case 2 -> Blocks.BARREL.defaultBlockState();
            case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
            case 4 -> Blocks.FURNACE.defaultBlockState();
            case MISSING_HULL -> Blocks.AIR.defaultBlockState();
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
        boolean supported = below == ShipHullVoxelLayout.Part.DECK
                || below == ShipHullVoxelLayout.Part.BRIDGE
                || below == ShipHullVoxelLayout.Part.HULL;
        if (!supported) return false;
        // Keep the whole approach, stair, and one-cell landing available,
        // including the player's headroom. No cargo may seal the new path.
        for (int dz = -1; dz <= 1; dz++)
            for (int dy = y - 3; dy <= y + 2; dy++)
                if (plan.get(new ShipHullVoxelLayout.Cell(x, dy, z + dz))
                        == ShipHullVoxelLayout.Part.STAIR) return false;
        // A cargo block must not obstruct an active door, ladder or helm
        // on its own floor. Fixtures BELOW the supporting deck cannot be
        // blocked by a crate ABOVE that deck; rejecting them also broke
        // previously valid medium-ship cargo during rebuild.
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = y; dy <= y + 2; dy++) {
                    var fixture = plan.get(new ShipHullVoxelLayout.Cell(
                            x + dx, dy, z + dz));
                    if (fixture == ShipHullVoxelLayout.Part.DOOR
                            || fixture == ShipHullVoxelLayout.Part.HELM
                            || fixture == ShipHullVoxelLayout.Part.LADDER)
                        return false;
                }
        return true;
    }

    /** Preview of the same supported, clear stair-safe cargo cell rule. */
    public static boolean movingCargoCell(ShipBuildConfiguration config, BlockPos local) {
        return config != null && !config.withdrawn() && local != null
                && cargoCell(ShipHullVoxelLayout.generate(config.structure()),
                        local.getX(), local.getY(), local.getZ());
    }

    private static Snapshot inspect(ServerLevel level, BlockPos anchor, int turns,
            ShipBuildConfiguration config) {
        if (config == null || config.withdrawn()) return null;
        Map<BlockPos, BlockState> source = worldPalette(config, anchor, turns);
        ShipHullVoxelLayout.Cell missingCell = null;
        if (!matchesPhysicalHull(level, source)) {
            boolean invalidDamage = false;
            // A single mined block is recorded as damage, never recreated as
            // free timber when the vessel moves or is parked again.
            if ((config.shipClass() == ShipSectionPlan.ShipClass.LARGE
                    || config.shipClass() == ShipSectionPlan.ShipClass.LARGEST)
                    && (config.hullRevision() == 3 || config.hullRevision() == 4)) {
                for (var part : ShipHullVoxelLayout.generate(config.structure()).entrySet()) {
                    if (part.getValue() == ShipHullVoxelLayout.Part.SAIL
                            || part.getValue() == ShipHullVoxelLayout.Part.GUNPORT_SHUTTER) continue;
                    var c = part.getKey();
                    BlockPos pos = FirstShipBlockHull.world(anchor, turns, c.x(), c.y(), c.z());
                    if (!level.hasChunkAt(pos)) return null;
                    if (matchesHullBlock(level.getBlockState(pos), source.get(pos))) continue;
                    boolean empty = level.getBlockState(pos).isAir()
                            || level.getFluidState(pos).is(FluidTags.WATER);
                    if (missingCell != null || !empty) {
                        invalidDamage = true;
                        break;
                    }
                    missingCell = c;
                }
            }
            if (missingCell != null && !invalidDamage) {
                BlockPos missingWorld = FirstShipBlockHull.world(anchor, turns,
                        missingCell.x(), missingCell.y(), missingCell.z());
                source = new LinkedHashMap<>(source);
                source.remove(missingWorld);
            } else return null;
        }
        List<Cargo> cargo = new ArrayList<>();
        if (missingCell != null)
            cargo.add(new Cargo(missingCell.x(), missingCell.y(), missingCell.z(),
                    MISSING_HULL, Blocks.AIR.defaultBlockState(), new CompoundTag()));
        ShipSuperstructurePlan structure = config.structure();
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cargoPlan =
                ShipHullVoxelLayout.generate(structure);
        int halfWidth = config.width() / 2, halfLength = config.length() / 2;
        int maxY = config.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && config.hullRevision() == 4 ? 48
                : Math.max(24, 4 + config.decks() * 4);
        for (int z = -halfLength; z <= halfLength; z++)
            for (int x = -halfWidth; x <= halfWidth; x++)
                for (int y = 1; y <= maxY; y++) {
                    if (Math.abs(x) > ShipHullVoxelLayout.halfBeam(structure, z))
                        continue;
                    BlockPos world = FirstShipBlockHull.world(anchor, turns, x, y, z);
                    if (!level.hasChunkAt(world)) return null;
                    if (missingCell != null && missingCell.x() == x
                            && missingCell.y() == y && missingCell.z() == z) continue;
                    if (source.containsKey(world)) continue;
                    BlockState state = level.getBlockState(world);
                    if (state.isAir()) continue;
                    BlockEntity blockEntity = level.getBlockEntity(world);
                    int type = kind(state, blockEntity);
                    if (type == 0 || !cargoCell(cargoPlan, x, y, z)) return null;
                    if (state.hasBlockEntity() && blockEntity == null) return null;
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
                ShipHullVoxelLayout.generate(config.structure());
        Set<String> occupied = new HashSet<>();
        int missing = 0;
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            int x = entry.getInt("X"), y = entry.getInt("Y"), z = entry.getInt("Z");
            int kind = entry.getInt("Kind");
            boolean damage = kind == MISSING_HULL;
            if (kind < 1 || kind > GENERAL_CARGO
                    || (damage && config.shipClass() != ShipSectionPlan.ShipClass.LARGE
                            && config.shipClass() != ShipSectionPlan.ShipClass.LARGEST)
                    || (damage && (++missing > 1 || !cargoPlan.containsKey(
                            new ShipHullVoxelLayout.Cell(x, y, z))))
                    || (!damage && !cargoCell(cargoPlan, x, y, z))
                    || !occupied.add(x + ":" + y + ":" + z)
                    || !entry.contains("State", Tag.TAG_COMPOUND)
                    || (kind != 3 && !damage && !(entry.contains("BlockEntity", Tag.TAG_COMPOUND)
                            || entry.contains("Data", Tag.TAG_COMPOUND))))
                return null;
            BlockState state;
            if (kind == GENERAL_CARGO) {
                if (!entry.contains("BlockId", Tag.TAG_STRING)) return null;
                ResourceLocation id = ResourceLocation.tryParse(entry.getString("BlockId"));
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) return null;
                state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
            } else state = kindState(kind);
            if (entry.contains("State", Tag.TAG_COMPOUND)) {
                CompoundTag properties = entry.getCompound("State");
                for (Property<?> property : state.getProperties()) {
                    if (!properties.contains(property.getName(), Tag.TAG_STRING)) return null;
                    try {
                        state = withProperty(state, property, properties.getString(property.getName()));
                    } catch (RuntimeException invalid) { return null; }
                }
            }
            // Saved NBT may be edited independently of live placement events.
            // Reject farm blocks and crops even when they carry a BlockEntity.
            if (!damage && ShipCargoPlacementGuard.forbidden(state)) return null;
            if (kind == GENERAL_CARGO && !state.hasBlockEntity()
                    && !allowedSimple(state)) return null;
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
            if (entry.kind() == GENERAL_CARGO)
                row.putString("BlockId", BuiltInRegistries.BLOCK.getKey(
                        entry.state().getBlock()).toString());
            CompoundTag properties = new CompoundTag();
            for (Property<?> property : entry.state().getProperties())
                properties.putString(property.getName(), stateProperty(entry.state(), property));
            row.put("State", properties);
            row.put("BlockEntity", entry.data().copy());
            result.add(row);
        }
        return result;
    }

    /**
     * Append real block cargo to a detached modular hull without touching
     * world chunks. This is the same validated NBT format as ordinary
     * on-deck cargo, so rotation, parking, saves and chest inventory all use
     * the existing restore path. A bed is placed atomically as two blocks.
     */
    public static ListTag withAddedMovingCargo(ServerLevel level,
            ShipBuildConfiguration config, ListTag entries,
            BlockPos local, BlockState requested) {
        if (level == null || config == null || config.withdrawn()
                || entries == null || local == null || requested == null
                || requested.isAir() || ShipCargoPlacementGuard.forbidden(requested))
            return null;
        List<Cargo> old = decode(config, entries);
        if (old == null) return null;
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> plan =
                ShipHullVoxelLayout.generate(config.structure());
        List<Map.Entry<BlockPos, BlockState>> pieces = new ArrayList<>();
        if (requested.getBlock() instanceof BedBlock) {
            BlockState foot = requested.setValue(BedBlock.PART, BedPart.FOOT);
            Direction facing = foot.getValue(BedBlock.FACING);
            pieces.add(Map.entry(local, foot));
            pieces.add(Map.entry(local.relative(facing),
                    foot.setValue(BedBlock.PART, BedPart.HEAD)));
        } else pieces.add(Map.entry(local, requested));
        if (old.size() + pieces.size()
                > config.width() * config.length() * config.decks()) return null;
        List<Cargo> next = new ArrayList<>(old);
        for (var piece : pieces) {
            BlockPos p = piece.getKey();
            BlockState state = piece.getValue();
            if (p.getY() < 1 || p.getY() > 48
                    || !cargoCell(plan, p.getX(), p.getY(), p.getZ())
                    || next.stream().anyMatch(c -> c.x() == p.getX()
                            && c.y() == p.getY() && c.z() == p.getZ()))
                return null;
            BlockEntity blockEntity = state.getBlock() instanceof EntityBlock entity
                    ? entity.newBlockEntity(p, state) : null;
            if (state.hasBlockEntity() && blockEntity == null) return null;
            int type = kind(state, blockEntity);
            if (type == 0) return null;
            CompoundTag data = blockEntity == null ? new CompoundTag()
                    : blockEntity.saveWithFullMetadata(level.registryAccess());
            next.add(new Cargo(p.getX(), p.getY(), p.getZ(), type, state, data));
        }
        ListTag encoded = encode(next);
        return decode(config, encoded) == null ? null : encoded;
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
            if (entry.kind() == MISSING_HULL) continue;
            if (target.containsKey(FirstShipBlockHull.world(
                    anchor, turns, entry.x(), entry.y(), entry.z()))) return false;
            if (!entry.state().hasBlockEntity()) continue;
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            BlockEntity loaded = BlockEntity.loadStatic(pos, entry.state(),
                    entry.data(), level.registryAccess());
            if (loaded == null || entry.kind() == GENERAL_CARGO
                    && !(loaded instanceof Container)
                    && !allowedSimple(entry.state())) return false;
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
        int maxY = config.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && config.hullRevision() == 4 ? 48
                : Math.max(24, 4 + config.decks() * 4);
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
            if (entry.kind() == MISSING_HULL) continue;
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            if (!level.setBlock(pos, entry.state(), UPDATE)) return false;
            if (entry.state().hasBlockEntity()) {
                BlockEntity be = BlockEntity.loadStatic(
                        pos, entry.state(), entry.data(), level.registryAccess());
                if (be == null) return false;
                level.setBlockEntity(be);
                be.setChanged();
            }
        }
        return true;
    }

    private static void removeDamage(Map<BlockPos, BlockState> blocks,
            List<Cargo> cargo, BlockPos anchor, int turns) {
        for (Cargo entry : cargo)
            if (entry.kind() == MISSING_HULL)
                blocks.remove(FirstShipBlockHull.world(anchor, turns,
                        entry.x(), entry.y(), entry.z()));
    }

    private static boolean clearBlocks(ServerLevel level, BlockPos anchor,
            Map<BlockPos, BlockState> owned, List<Cargo> cargo, int turns) {
        Set<BlockPos> positions = new HashSet<>(owned.keySet());
        for (Cargo entry : cargo) {
            if (entry.kind() == MISSING_HULL) continue;
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
        if (level == null || anchor == null || config == null || config.withdrawn())
            return false;
        List<Cargo> cargo = decode(config, packedCargo);
        if (cargo == null) return false;
        Map<BlockPos, BlockState> blocks = worldPalette(config, anchor, turns);
        if (!validateRestore(level, anchor, turns, cargo, blocks)
                || !clearSpace(level, anchor, blocks, Set.of(), config, turns)) return false;
        for (Cargo entry : cargo) {
            if (entry.kind() == MISSING_HULL) continue;
            BlockPos pos = FirstShipBlockHull.world(anchor, turns,
                    entry.x(), entry.y(), entry.z());
            if (!level.getBlockState(pos).isAir() || level.getBlockEntity(pos) != null)
                return false;
        }
        removeDamage(blocks, cargo, anchor, turns);
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
        if (next == null || next.withdrawn()) return false;
        Snapshot source = inspect(level, oldAnchor, oldTurn, previous);
        if (source == null) return false;
        Map<BlockPos, BlockState> targetHull = worldPalette(next, target, nextTurn);
        Set<BlockPos> owned = new HashSet<>(source.hull().keySet());
        for (Cargo entry : source.cargo()) owned.add(FirstShipBlockHull.world(
                oldAnchor, oldTurn, entry.x(), entry.y(), entry.z()));
        int rotation = Math.floorMod(nextTurn - oldTurn, 4);
        List<Cargo> transported = new ArrayList<>();
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cargoPlan =
                ShipHullVoxelLayout.generate(next.structure());
        for (Cargo entry : source.cargo()) {
            if (entry.kind() == MISSING_HULL && !cargoPlan.containsKey(
                    new ShipHullVoxelLayout.Cell(entry.x(), entry.y(), entry.z()))) return false;
            if (entry.kind() != MISSING_HULL
                    && !cargoCell(cargoPlan, entry.x(), entry.y(), entry.z())) return false;
            BlockState state = entry.state();
            for (int n = 0; n < rotation; n++) state = state.rotate(Rotation.CLOCKWISE_90);
            transported.add(new Cargo(entry.x(), entry.y(), entry.z(),
                    entry.kind(), state, entry.data()));
        }
        if (!validateRestore(level, target, nextTurn, transported, targetHull)
                || !clearSpace(level, target, targetHull, owned, next, nextTurn)) return false;
        for (Cargo entry : transported) {
            if (entry.kind() == MISSING_HULL) continue;
            BlockPos p = FirstShipBlockHull.world(target, nextTurn,
                    entry.x(), entry.y(), entry.z());
            if (!owned.contains(p) && !level.getBlockState(p).isAir()) return false;
        }
        if (!clearBlocks(level, oldAnchor, source.hull(), source.cargo(), oldTurn)) {
            if (!placeBlocks(level, source.hull(), source.cargo(), oldAnchor, oldTurn))
                throw new IllegalStateException("Failed to restore modular hull after failed clear");
            return false;
        }
        removeDamage(targetHull, transported, target, nextTurn);
        if (placeBlocks(level, targetHull, transported, target, nextTurn)) return true;
        clearBlocks(level, target, targetHull, transported, nextTurn);
        if (!placeBlocks(level, source.hull(), source.cargo(), oldAnchor, oldTurn))
            throw new IllegalStateException("Failed to restore original modular hull after rebuild failure");
        return false;
    }

    private ShipModularBlockHull() {}
}
