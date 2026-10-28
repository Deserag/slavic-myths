package org.slavicmyths.losthalls;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import org.slavicmyths.losthalls.LostHallsRouteCheck.Cell;
import org.slavicmyths.losthalls.LostHallsRouteCheck.Snapshot;
import org.slavicmyths.losthalls.LostHallsRouteCheck.Status;

/** Server-thread completion gate for room producers. No world generation, chunk
 * tickets or tick scans. Call only AFTER all pieces of a room are placed and
 * its final entrances are known; a partial room must never be published. */
public final class LostHallsRoomPublication {
    public static final int MAX_WALKABLE_CANDIDATES = 4096;
    private static final int MAX_HORIZONTAL_SPAN = 96;
    private static final int MAX_VERTICAL_SPAN = 48;

    /** Pure finite route proof for offline fixtures. Only publish() validates
     * actual Minecraft blocks before writing authoritative room metadata. */
    public static boolean verifyTopology(LostHallsRooms.Node node, Collection<BlockPos> exits,
                                         Collection<BlockPos> candidateFeet, Snapshot snapshot) {
        Objects.requireNonNull(snapshot);
        if (!validPlan(node, exits, candidateFeet)) return false;
        return LostHallsRouteCheck.verify(snapshot, node.anchor(), exits,
                MAX_WALKABLE_CANDIDATES).status() == Status.CONNECTED;
    }

    /** Publish only if every sampled voxel is already loaded and all declared
     * exits are mutually reachable. Reject without changing SavedData otherwise.
     * Generator retries after all pieces finish; no chunks are loaded on demand. */
    public static boolean publish(ServerLevel level, LostHallsRooms.Node node,
                                  Collection<BlockPos> exits, Collection<BlockPos> candidateFeet) {
        LostHallsRooms rooms = LostHallsRooms.get(level); // Nether + server-thread guard
        if (rooms.isReadOnly() || !validPlan(node, exits, candidateFeet)) return false;
        Set<BlockPos> sampled = new HashSet<>();
        for (BlockPos feet : candidateFeet) for (int dy = -1; dy <= 2; dy++) {
            BlockPos pos = feet.offset(0, dy, 0);
            if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)
                    || !level.getWorldBorder().isWithinBounds(pos)) return false;
            sampled.add(pos);
        }
        Map<BlockPos, Cell> cells = new HashMap<>();
        for (BlockPos pos : sampled) {
            var state = level.getBlockState(pos);
            Cell cell;
            if (!state.getFluidState().isEmpty() || state.is(Blocks.FIRE)
                    || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.MAGMA_BLOCK)
                    || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                cell = Cell.HAZARD;
            } else if (state.getCollisionShape(level, pos).isEmpty()) {
                cell = Cell.CLEAR;
            } else {
                cell = state.isFaceSturdy(level, pos, Direction.UP) ? Cell.SUPPORT : Cell.HAZARD;
            }
            cells.put(pos, cell);
        }
        return verifyTopology(node, exits, candidateFeet, new Snapshot(cells))
                && rooms.registerCompleted(node);
    }

    private static boolean validPlan(LostHallsRooms.Node node, Collection<BlockPos> exits,
                                     Collection<BlockPos> candidateFeet) {
        if (node == null || exits == null || candidateFeet == null
                || exits.size() != node.exits() || candidateFeet.isEmpty()
                || candidateFeet.size() > MAX_WALKABLE_CANDIDATES) return false;
        Set<BlockPos> feet = new HashSet<>();
        for (BlockPos pos : candidateFeet) {
            if (pos == null || !inside(node.anchor(), pos)) return false;
            feet.add(pos);
        }
        Set<BlockPos> distinctExits = new HashSet<>();
        for (BlockPos exit : exits) {
            if (exit == null || !feet.contains(exit) || !distinctExits.add(exit)) return false;
        }
        return distinctExits.contains(node.anchor());
    }

    private static boolean inside(BlockPos anchor, BlockPos pos) {
        return Math.abs((long) pos.getX() - anchor.getX()) <= MAX_HORIZONTAL_SPAN
                && Math.abs((long) pos.getZ() - anchor.getZ()) <= MAX_HORIZONTAL_SPAN
                && Math.abs((long) pos.getY() - anchor.getY()) <= MAX_VERTICAL_SPAN;
    }

    private LostHallsRoomPublication() {}
}
