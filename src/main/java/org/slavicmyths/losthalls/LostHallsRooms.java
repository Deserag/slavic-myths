package org.slavicmyths.losthalls;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

/** Compact metadata for completed rooms. No generated room = no navigation target.
 * Publishing belongs to the server-thread completion hook of #291/#294, never to
 * an asynchronous worldgen worker. This index does not generate or repair rooms. */
public final class LostHallsRooms extends SavedData {
    public static final int VERSION = 1;
    public static final int SEARCH_RADIUS = 48;
    // A defensive metadata density bound, not room/spawn frequency or biome size.
    public static final int MAX_NODES_PER_COLUMN = 64;

    public enum Scene {
        CORRIDOR("S1"), BRIDGE("S2"), LARGE_HALL("S3"),
        FUNGUS_HALL("S4"), LUMINOUS_GROTTO("S5"), JUNCTION("S6");
        private final String id;
        Scene(String id) { this.id = id; }
        public String id() { return id; }
        public boolean isHall() {
            return this == LARGE_HALL || this == FUNGUS_HALL || this == LUMINOUS_GROTTO;
        }
        private static Scene read(String id) {
            for (Scene scene : values()) if (scene.id.equals(id)) return scene;
            throw new IllegalArgumentException("Unknown room scene: " + id);
        }
    }

    /** Anchor is a previously validated walkable room entry, not its solid center.
     * Through-passages require >=2 exits; a large hall can have one valid entrance. */
    public record Node(UUID id, Scene scene, BlockPos anchor, int exits) {
        public Node {
            Objects.requireNonNull(id);
            Objects.requireNonNull(scene);
            anchor = Objects.requireNonNull(anchor).immutable();
            if (Math.abs((long) anchor.getX()) > 30_000_000
                    || Math.abs((long) anchor.getZ()) > 30_000_000
                    || anchor.getY() < 1 || anchor.getY() > 254 || exits < 1 || exits > 8) {
                throw new IllegalArgumentException("Invalid completed room entry");
            }
        }
        public boolean isNavigationTarget() { return scene.isHall() || exits >= 2; }
    }

    private final Map<UUID, Node> nodes = new HashMap<>();
    private final Map<Long, Map<UUID, Node>> columns = new HashMap<>();
    private CompoundTag readOnlySnapshot;

    public static LostHallsRooms get(ServerLevel level) {
        if (!level.dimension().equals(Level.NETHER) || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Room metadata requires the Nether server thread");
        }
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(LostHallsRooms::new, LostHallsRooms::load),
                "slavicmyths_lost_halls_rooms");
    }

    public boolean isReadOnly() { return readOnlySnapshot != null; }
    public int size() { return nodes.size(); }
    public Optional<Node> node(UUID id) { return isReadOnly() ? Optional.empty() : Optional.ofNullable(nodes.get(id)); }

    /** Idempotent completion publication; a conflicting UUID cannot move a room. */
    public boolean registerCompleted(Node node) {
        Objects.requireNonNull(node);
        if (isReadOnly()) return false;
        Node existing = nodes.get(node.id());
        if (existing != null) return existing.equals(node);
        long key = column(node.anchor());
        Map<UUID, Node> bucket = columns.get(key);
        if (bucket != null && bucket.size() >= MAX_NODES_PER_COLUMN) return false;
        nodes.put(node.id(), node);
        columns.computeIfAbsent(key, ignored -> new HashMap<>()).put(node.id(), node);
        setDirty();
        return true;
    }

    public boolean remove(UUID id) {
        if (isReadOnly()) return false;
        Node node = nodes.remove(id);
        if (node == null) return false;
        Map<UUID, Node> bucket = columns.get(column(node.anchor()));
        bucket.remove(id);
        if (bucket.isEmpty()) columns.remove(column(node.anchor()));
        setDirty();
        return true;
    }

    /** At most 7x7 columns x64 compact nodes, on tool use, never global tick polling.
     * The predicate must inspect only already loaded data; it is not a locate call. */
    public Optional<Node> nearest(BlockPos from, Predicate<BlockPos> usableEntry) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(usableEntry);
        if (isReadOnly() || Math.abs((long) from.getX()) > 30_000_000
                || Math.abs((long) from.getZ()) > 30_000_000) return Optional.empty();
        Node best = null;
        double bestDistance = SEARCH_RADIUS * SEARCH_RADIUS;
        int minX = Math.floorDiv(from.getX() - SEARCH_RADIUS, 16);
        int maxX = Math.floorDiv(from.getX() + SEARCH_RADIUS, 16);
        int minZ = Math.floorDiv(from.getZ() - SEARCH_RADIUS, 16);
        int maxZ = Math.floorDiv(from.getZ() + SEARCH_RADIUS, 16);
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            Map<UUID, Node> bucket = columns.get(ChunkPos.asLong(x, z));
            if (bucket == null) continue;
            for (Node node : bucket.values()) {
                if (!node.isNavigationTarget()) continue;
                double distance = node.anchor().distSqr(from);
                if (distance > bestDistance || (distance == bestDistance && best != null
                        && node.id().compareTo(best.id()) >= 0)) continue;
                if (!usableEntry.test(node.anchor())) continue;
                best = node;
                bestDistance = distance;
            }
        }
        return Optional.ofNullable(best);
    }

    /** Actual server lookup: check chunk residency BEFORE any block-state reads.
     * Entry clearance is a current local check; graph reachability is the producer's
     * responsibility and still needs physical survival-route QA. */
    public static Optional<Node> findNearest(ServerLevel level, BlockPos from) {
        if (!level.dimension().equals(Level.NETHER)) return Optional.empty();
        return get(level).nearest(from, pos -> {
            if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)
                    || level.isOutsideBuildHeight(pos.above())
                    || !level.getWorldBorder().isWithinBounds(pos)) return false;
            var feet = level.getBlockState(pos);
            var head = level.getBlockState(pos.above());
            var floor = level.getBlockState(pos.below());
            return feet.getCollisionShape(level, pos).isEmpty()
                    && head.getCollisionShape(level, pos.above()).isEmpty()
                    && feet.getFluidState().isEmpty() && head.getFluidState().isEmpty()
                    && !feet.is(Blocks.FIRE) && !feet.is(Blocks.SOUL_FIRE)
                    && !floor.is(Blocks.MAGMA_BLOCK) && !floor.is(Blocks.CAMPFIRE)
                    && !floor.is(Blocks.SOUL_CAMPFIRE)
                    && floor.isFaceSturdy(level, pos.below(), Direction.UP);
        });
    }

    private static long column(BlockPos pos) {
        return ChunkPos.asLong(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16));
    }

    @Override
    public CompoundTag save(CompoundTag root, HolderLookup.Provider lookup) {
        if (isReadOnly()) return readOnlySnapshot.copy();
        root.putInt("Version", VERSION);
        ListTag list = new ListTag();
        nodes.values().stream().sorted((a, b) -> a.id().compareTo(b.id())).forEach(node -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", node.id());
            tag.putString("Scene", node.scene().id());
            tag.putLong("Entry", node.anchor().asLong());
            tag.putInt("Exits", node.exits());
            list.add(tag);
        });
        root.put("Nodes", list);
        return root;
    }

    public static LostHallsRooms load(CompoundTag root, HolderLookup.Provider lookup) {
        LostHallsRooms data = new LostHallsRooms();
        try {
            require(root.contains("Version", Tag.TAG_INT) && root.getInt("Version") == VERSION);
            require(root.contains("Nodes", Tag.TAG_LIST));
            ListTag list = root.getList("Nodes", Tag.TAG_COMPOUND);
            require(list.size() == ((ListTag) root.get("Nodes")).size());
            for (Tag raw : list) {
                CompoundTag tag = (CompoundTag) raw;
                require(tag.hasUUID("Id") && tag.contains("Scene", Tag.TAG_STRING)
                        && tag.contains("Entry", Tag.TAG_LONG) && tag.contains("Exits", Tag.TAG_INT));
                Node node = new Node(tag.getUUID("Id"), Scene.read(tag.getString("Scene")),
                        BlockPos.of(tag.getLong("Entry")), tag.getInt("Exits"));
                require(!data.nodes.containsKey(node.id()) && data.registerCompleted(node));
            }
            data.setDirty(false);
        } catch (IllegalArgumentException | ClassCastException invalid) {
            data.nodes.clear();
            data.columns.clear();
            data.readOnlySnapshot = root.copy();
            data.setDirty(false);
        }
        return data;
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalArgumentException("Invalid Lost Halls room metadata");
    }
}
