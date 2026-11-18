package org.slavicmyths.losthalls;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Conservative walking-route check over a FINAL finite voxel snapshot, not a
 * room generator or a world scan. The producer must classify actual collision,
 * support and hazards before calling it; unknown/unprovided cells are impassable.
 * Does not certify artwork, stairs/slab geometry, biome coverage or runtime AI. */
public final class LostHallsRouteCheck {
    public enum Cell { CLEAR, SUPPORT, HAZARD }
    public enum Status { CONNECTED, INVALID_ENTRY, NO_ROUTE, BUDGET_EXCEEDED }
    public record Result(Status status, int visited, Set<BlockPos> reachedEntries) {
        public Result { reachedEntries = Set.copyOf(reachedEntries); }
    }

    public static final class Snapshot {
        private final Map<BlockPos, Cell> cells;

        public Snapshot(Map<BlockPos, Cell> cells) {
            Map<BlockPos, Cell> stable = new HashMap<>();
            cells.forEach((pos, cell) -> stable.put(pos.immutable(), Objects.requireNonNull(cell)));
            this.cells = Map.copyOf(stable);
        }

        private boolean clear(BlockPos pos) { return cells.get(pos) == Cell.CLEAR; }
        private boolean standable(BlockPos pos) {
            return clear(pos) && clear(pos.above()) && cells.get(pos.below()) == Cell.SUPPORT;
        }
    }

    /** Cardinal walking + at most one-block rise/drop. Upward moves require
     * headroom above the source, downward moves headroom above the destination.
     * No diagonal corner clipping, forced crawl or unsupported parkour is accepted. */
    public static Result verify(Snapshot snapshot, BlockPos start,
                                Collection<BlockPos> requiredEntries, int visitLimit) {
        Objects.requireNonNull(snapshot);
        Objects.requireNonNull(start);
        if (visitLimit < 1 || visitLimit > 65_536 || requiredEntries.isEmpty()) {
            throw new IllegalArgumentException("Finite route budget and nonempty room entries required");
        }
        Set<BlockPos> required = new HashSet<>();
        for (BlockPos pos : requiredEntries) required.add(pos.immutable());
        if (!snapshot.standable(start) || required.stream().anyMatch(pos -> !snapshot.standable(pos))) {
            return new Result(Status.INVALID_ENTRY, 0, Set.of());
        }
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos initial = start.immutable();
        queue.add(initial);
        visited.add(initial);
        if (required.contains(initial)) reached.add(initial);
        while (!queue.isEmpty()) {
            BlockPos from = queue.remove();
            if (reached.containsAll(required)) return new Result(Status.CONNECTED, visited.size(), reached);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = from.relative(direction);
                for (int dy : new int[]{0, 1, -1}) {
                    BlockPos to = adjacent.offset(0, dy, 0);
                    if (visited.contains(to) || !snapshot.standable(to)) continue;
                    if ((dy > 0 && !snapshot.clear(from.above(2)))
                            || (dy < 0 && !snapshot.clear(to.above(2)))) continue;
                    if (visited.size() >= visitLimit) {
                        return new Result(Status.BUDGET_EXCEEDED, visited.size(), reached);
                    }
                    visited.add(to);
                    queue.add(to);
                    if (required.contains(to)) {
                        reached.add(to);
                        if (reached.containsAll(required)) {
                            return new Result(Status.CONNECTED, visited.size(), reached);
                        }
                    }
                }
            }
        }
        return new Result(Status.NO_ROUTE, visited.size(), reached);
    }

    private LostHallsRouteCheck() {}
}
