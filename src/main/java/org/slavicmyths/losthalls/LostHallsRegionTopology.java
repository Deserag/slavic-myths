package org.slavicmyths.losthalls;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.slavicmyths.losthalls.LostHallsRooms.Node;
import org.slavicmyths.losthalls.LostHallsRooms.Scene;

/** Deterministic logical room graph, NOT finished Nether geometry or a chunk
 * generator. Requires terrain preflight and real, connected voxel geometry. */
public final class LostHallsRegionTopology {
    public static final int REGION_CHUNKS = 12;
    private static final int REGION_BLOCKS = REGION_CHUNKS * 16;

    public record Link(UUID from, UUID to) {
        public Link {
            if (from == null || to == null || from.equals(to)) {
                throw new IllegalArgumentException("Invalid room graph edge");
            }
        }
    }

    public record Plan(List<Node> rooms, UUID arenaId, BlockPos arena, List<Link> links) {
        public Plan {
            rooms = List.copyOf(rooms);
            links = List.copyOf(links);
            arena = arena.immutable();
        }

        /** Connectivity of the planned seven-vertex graph ONLY. */
        public boolean isConnected() {
            Map<UUID, Set<UUID>> neighbors = new HashMap<>();
            for (Node node : rooms) if (neighbors.putIfAbsent(node.id(), new HashSet<>()) != null) return false;
            if (neighbors.putIfAbsent(arenaId, new HashSet<>()) != null || rooms.isEmpty()) return false;
            for (Link link : links) {
                if (!neighbors.containsKey(link.from()) || !neighbors.containsKey(link.to())) return false;
                neighbors.get(link.from()).add(link.to());
                neighbors.get(link.to()).add(link.from());
            }
            Set<UUID> reached = new HashSet<>();
            ArrayDeque<UUID> pending = new ArrayDeque<>();
            pending.add(rooms.getFirst().id());
            reached.add(rooms.getFirst().id());
            while (!pending.isEmpty()) for (UUID other : neighbors.get(pending.remove())) {
                if (reached.add(other)) pending.add(other);
            }
            return reached.size() == neighbors.size();
        }
    }

    /** Region indices enumerate 12x12 chunk cells. Y is a terrain-preflight
     * suggestion, not a forced spawn height. This never writes world blocks. */
    public static Plan plan(long seed, int regionX, int regionZ, int baseY) {
        if (baseY < 54 || baseY > 120) throw new IllegalArgumentException("Unsafe base Y");
        long x0 = (long) regionX * REGION_BLOCKS + REGION_BLOCKS / 2;
        long z0 = (long) regionZ * REGION_BLOCKS + REGION_BLOCKS / 2;
        if (Math.abs(x0) > 29_999_900 || Math.abs(z0) > 29_999_900) {
            throw new IllegalArgumentException("Region outside supported world bounds");
        }
        SplittableRandom random = new SplittableRandom(seed
                ^ (0x9e3779b97f4a7c15L * regionX) ^ (0xbf58476d1ce4e5b9L * regionZ));
        int turns = random.nextInt(4);
        boolean mirror = random.nextBoolean();
        int[] xs = {-58, -20, 28, 50, 14, -35, 60};
        int[] zs = {-31, -44, -25, 14, 43, 31, 54};
        int[] heights = {0, 0, 8, 8, 0, -8, 8};
        Scene[] scenes = {Scene.CORRIDOR, Scene.JUNCTION, Scene.BRIDGE,
                Scene.LARGE_HALL, Scene.LUMINOUS_GROTTO, Scene.FUNGUS_HALL};
        UUID[] ids = new UUID[7];
        BlockPos[] positions = new BlockPos[7];
        for (int i = 0; i < ids.length; i++) {
            long x = xs[i] + random.nextInt(-4, 5), z = zs[i] + random.nextInt(-4, 5);
            if (mirror) x = -x;
            for (int j = 0; j < turns; j++) {
                long old = x;
                x = -z;
                z = old;
            }
            positions[i] = new BlockPos((int) (x0 + x), baseY + heights[i], (int) (z0 + z));
            ids[i] = UUID.nameUUIDFromBytes(("slavicmyths:lost-halls:" + seed + ":"
                    + regionX + ":" + regionZ + ":" + i).getBytes(StandardCharsets.UTF_8));
        }
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5},
                {5, 0}, {1, 3}, {3, 6}, {4, 6}};
        int[] exits = new int[6];
        List<Link> links = new ArrayList<>();
        for (int[] pair : edges) {
            if (pair[0] < exits.length) exits[pair[0]]++;
            if (pair[1] < exits.length) exits[pair[1]]++;
            links.add(new Link(ids[pair[0]], ids[pair[1]]));
        }
        List<Node> rooms = new ArrayList<>();
        for (int i = 0; i < scenes.length; i++) rooms.add(new Node(ids[i], scenes[i], positions[i], exits[i]));
        return new Plan(rooms, ids[6], positions[6], links);
    }

    private LostHallsRegionTopology() {}
}
