package org.slavicmyths.losthalls;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent completion receipts for multi-chunk rooms, not a generator.
 * The producer reports a chunk ONLY after its own room pieces are committed,
 * on the Nether server thread. All receipts are required before the final
 * loaded-block route check; neither stage loads chunks or places blocks. */
public final class LostHallsRoomPieces extends SavedData {
    public static final int VERSION = 1;
    public static final int MAX_ACTIVE_ROOMS = 4096;
    public static final int MAX_CHUNKS_PER_ROOM = 64;
    private static final int MAX_CHUNK_DISTANCE = 7;

    public record Status(LostHallsRooms.Node node, Set<Long> expected, Set<Long> completed) {
        public Status {
            expected = Set.copyOf(expected);
            completed = Set.copyOf(completed);
        }
        public boolean ready() { return completed.size() == expected.size(); }
    }

    private static final class Plan {
        final LostHallsRooms.Node node;
        final Set<Long> expected;
        final Set<Long> completed = new HashSet<>();
        Plan(LostHallsRooms.Node node, Set<Long> expected) {
            this.node = node;
            this.expected = Set.copyOf(expected);
        }
        Status status() { return new Status(node, expected, completed); }
    }

    private final Map<UUID, Plan> plans = new HashMap<>();
    private CompoundTag readOnlySnapshot;

    public static LostHallsRoomPieces get(ServerLevel level) {
        if (!level.dimension().equals(Level.NETHER) || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Room completion receipts require the Nether server thread");
        }
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(LostHallsRoomPieces::new, LostHallsRoomPieces::load),
                "slavicmyths_lost_halls_room_pieces");
    }

    public boolean isReadOnly() { return readOnlySnapshot != null; }
    public int size() { return plans.size(); }
    public Optional<Status> status(UUID id) {
        Plan plan = isReadOnly() ? null : plans.get(id);
        return plan == null ? Optional.empty() : Optional.of(plan.status());
    }

    /** Idempotent reservation. A repeated room ID cannot change its footprint,
     * even if receipts have already arrived in an arbitrary chunk order. */
    public boolean begin(LostHallsRooms.Node node, Collection<ChunkPos> chunks) {
        if (isReadOnly() || node == null || chunks == null || chunks.isEmpty()
                || chunks.size() > MAX_CHUNKS_PER_ROOM) return false;
        Set<Long> expected = new HashSet<>();
        int x = Math.floorDiv(node.anchor().getX(), 16);
        int z = Math.floorDiv(node.anchor().getZ(), 16);
        for (ChunkPos chunk : chunks) {
            if (chunk == null || Math.abs((long) chunk.x - x) > MAX_CHUNK_DISTANCE
                    || Math.abs((long) chunk.z - z) > MAX_CHUNK_DISTANCE
                    || !expected.add(chunk.toLong())) return false;
        }
        long anchorChunk = ChunkPos.asLong(x, z);
        if (!expected.contains(anchorChunk)) return false;
        // Corner-only contact or disconnected chunk islands cannot be a single
        // walkable room. Validate bounded 4-neighbor connectivity before saving.
        Set<Long> reachable = new HashSet<>();
        java.util.ArrayDeque<Long> pending = new java.util.ArrayDeque<>();
        reachable.add(anchorChunk);
        pending.add(anchorChunk);
        while (!pending.isEmpty()) {
            ChunkPos at = new ChunkPos(pending.remove());
            for (long next : new long[]{ChunkPos.asLong(at.x + 1, at.z),
                    ChunkPos.asLong(at.x - 1, at.z), ChunkPos.asLong(at.x, at.z + 1),
                    ChunkPos.asLong(at.x, at.z - 1)}) {
                if (expected.contains(next) && reachable.add(next)) pending.add(next);
            }
        }
        if (reachable.size() != expected.size()) return false;
        Plan current = plans.get(node.id());
        if (current != null) return current.node.equals(node) && current.expected.equals(expected);
        if (plans.size() >= MAX_ACTIVE_ROOMS) return false;
        plans.put(node.id(), new Plan(node, expected));
        setDirty();
        return true;
    }

    /** Accept only acknowledged chunks of this exact planned room. Replaying a
     * receipt after restart does not increase the completed count. */
    public boolean acknowledge(UUID roomId, ChunkPos completedChunk) {
        if (isReadOnly() || roomId == null || completedChunk == null) return false;
        Plan plan = plans.get(roomId);
        if (plan == null || !plan.expected.contains(completedChunk.toLong())) return false;
        if (plan.completed.add(completedChunk.toLong())) setDirty();
        return true;
    }

    /** Readiness alone NEVER publishes metadata. Final geometry and all exit
     * routes are still checked against currently loaded blocks. If any chunk is
     * unloaded, keep receipts and retry later without acquiring chunk tickets. */
    public static boolean publish(ServerLevel level, UUID roomId, Collection<BlockPos> exits,
                                  Collection<BlockPos> candidateFeet) {
        LostHallsRoomPieces data = get(level);
        Plan plan = data.plans.get(roomId);
        if (data.isReadOnly() || plan == null || !plan.status().ready()
                || exits == null || candidateFeet == null || candidateFeet.isEmpty()) return false;
        for (BlockPos pos : candidateFeet) {
            if (pos == null || !plan.expected.contains(column(pos))) return false;
        }
        for (BlockPos pos : exits) {
            if (pos == null || !plan.expected.contains(column(pos))) return false;
        }
        if (!LostHallsRoomPublication.publish(level, plan.node, exits, candidateFeet)) return false;
        data.plans.remove(roomId);
        data.setDirty();
        return true;
    }

    private static long column(BlockPos pos) {
        return ChunkPos.asLong(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16));
    }

    @Override
    public CompoundTag save(CompoundTag root, HolderLookup.Provider lookup) {
        if (isReadOnly()) return readOnlySnapshot.copy();
        root.putInt("Version", VERSION);
        ListTag list = new ListTag();
        plans.values().stream().sorted((a, b) -> a.node.id().compareTo(b.node.id())).forEach(plan -> {
            var node = plan.node;
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", node.id());
            tag.putString("Scene", node.scene().id());
            tag.putLong("Entry", node.anchor().asLong());
            tag.putInt("Exits", node.exits());
            tag.putLongArray("Expected", plan.expected.stream().sorted().mapToLong(Long::longValue).toArray());
            tag.putLongArray("Completed", plan.completed.stream().sorted().mapToLong(Long::longValue).toArray());
            list.add(tag);
        });
        root.put("Plans", list);
        return root;
    }

    public static LostHallsRoomPieces load(CompoundTag root, HolderLookup.Provider lookup) {
        LostHallsRoomPieces data = new LostHallsRoomPieces();
        try {
            require(root.contains("Version", Tag.TAG_INT) && root.getInt("Version") == VERSION);
            require(root.contains("Plans", Tag.TAG_LIST));
            ListTag list = root.getList("Plans", Tag.TAG_COMPOUND);
            require(list.size() == ((ListTag) root.get("Plans")).size()
                    && list.size() <= MAX_ACTIVE_ROOMS);
            for (Tag raw : list) {
                CompoundTag tag = (CompoundTag) raw;
                require(tag.hasUUID("Id") && tag.contains("Scene", Tag.TAG_STRING)
                        && tag.contains("Entry", Tag.TAG_LONG) && tag.contains("Exits", Tag.TAG_INT)
                        && tag.contains("Expected", Tag.TAG_LONG_ARRAY)
                        && tag.contains("Completed", Tag.TAG_LONG_ARRAY));
                String sceneId = tag.getString("Scene");
                var scene = Arrays.stream(LostHallsRooms.Scene.values())
                        .filter(value -> value.id().equals(sceneId)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown scene"));
                var node = new LostHallsRooms.Node(tag.getUUID("Id"), scene,
                        BlockPos.of(tag.getLong("Entry")), tag.getInt("Exits"));
                long[] expected = tag.getLongArray("Expected");
                long[] completed = tag.getLongArray("Completed");
                require(expected.length > 0 && expected.length <= MAX_CHUNKS_PER_ROOM
                        && completed.length <= expected.length && !data.plans.containsKey(node.id()));
                var chunks = Arrays.stream(expected).mapToObj(ChunkPos::new).toList();
                require(data.begin(node, chunks));
                for (long key : completed) {
                    Plan plan = data.plans.get(node.id());
                    require(!plan.completed.contains(key) && data.acknowledge(node.id(), new ChunkPos(key)));
                }
            }
            data.setDirty(false);
        } catch (IllegalArgumentException | ClassCastException invalid) {
            data.plans.clear();
            data.readOnlySnapshot = root.copy();
            data.setDirty(false);
        }
        return data;
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalArgumentException("Invalid room piece receipts");
    }
}
