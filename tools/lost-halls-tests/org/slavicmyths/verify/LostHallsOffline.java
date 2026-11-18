package org.slavicmyths.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.slavicmyths.losthalls.LostHallsRooms;
import org.slavicmyths.losthalls.LostHallsRooms.Node;
import org.slavicmyths.losthalls.LostHallsRooms.Scene;
import org.slavicmyths.losthalls.LostHallsRouteCheck;
import org.slavicmyths.losthalls.LostHallsRouteCheck.Cell;
import org.slavicmyths.losthalls.LostHallsRouteCheck.Status;
import org.slavicmyths.losthalls.PostenEncounters;
import org.slavicmyths.losthalls.PostenEncounters.Phase;

/** Production journals and native binary NBT only. No Minecraft main, Bootstrap,
 * server, client, world, registry mutation, entity or GameTest is invoked. */
public final class LostHallsOffline {
    private static int assertions;

    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    private static UUID id(long number) { return new UUID(0, number); }

    private static byte[] encode(CompoundTag tag) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) { NbtIo.write(tag, output); }
        return bytes.toByteArray();
    }

    private static CompoundTag binary(CompoundTag tag) throws Exception {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encode(tag)))) {
            return NbtIo.read(input, NbtAccounter.unlimitedHeap());
        }
    }

    private static PostenEncounters reload(PostenEncounters data) throws Exception {
        PostenEncounters result = PostenEncounters.load(binary(data.save(new CompoundTag(), null)), null);
        check(!result.isReadOnly(), "valid encounter binary NBT accepted");
        return result;
    }

    private static LostHallsRooms reload(LostHallsRooms data) throws Exception {
        LostHallsRooms result = LostHallsRooms.load(binary(data.save(new CompoundTag(), null)), null);
        check(!result.isReadOnly() && !result.isDirty(), "valid room binary NBT accepted without dirty writes");
        return result;
    }

    private static void encounterBoundaries() throws Exception {
        UUID arena = id(1), boss = id(2);
        PostenEncounters data = new PostenEncounters();
        check(!data.claimRewards(arena, boss), "unknown encounter cannot pay rewards");
        check(data.register(arena, boss), "first registration");
        check(data.register(arena, boss), "registration idempotent");
        check(!data.register(arena, id(3)), "arena cannot be replaced");
        check(!data.register(id(4), boss), "same body cannot own two arenas");
        check(data.snapshot(arena).orElseThrow().health() == 650, "exact max HP");
        for (float bad : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0, -1}) {
            check(data.damageBody(arena, boss, bad).applied() == 0, "reject invalid body damage");
        }
        check(data.damageBody(arena, id(3), 100).applied() == 0, "wrong body UUID rejected");
        check(data.damageBody(arena, boss, 1).applied() == 1, "650 to 649");
        check(data.snapshot(arena).orElseThrow().health() == 649, "649 boundary");
        check(!data.claimRewards(arena, boss), "body cannot pay rewards");
        check(data.damageBody(arena, boss, 388).applied() == 388, "649 to 261");
        var split = data.damageBody(arena, boss, 2);
        check(split.splitNow() && split.applied() == 1, "261 overkill clamps to260, excess discarded");
        var snapshot = data.snapshot(arena).orElseThrow();
        check(snapshot.phase() == Phase.COPIES && snapshot.health() == 260, "four-copy phase");
        check(snapshot.copies().size() == 4 && new HashSet<>(snapshot.copies().stream().map(PostenEncounters.Copy::entity).toList()).size() == 4,
                "four distinct stable UUIDs");
        check(snapshot.copies().stream().allMatch(c -> c.health() == 65), "four exact65 HP pools");
        check(new HashSet<>(snapshot.copies().stream().map(PostenEncounters.Copy::mask).toList()).size() == 4, "one of each mask");
        check(!data.damageBody(arena, boss, 1).splitNow(), "no recursive split");
        data = reload(data);
        check(data.snapshot(arena).orElseThrow().equals(snapshot), "complete phase2 snapshot survives NBT");
        UUID copy = snapshot.copies().getFirst().entity();
        check(data.damageCopy(arena, id(12345), 1).applied() == 0, "foreign copy UUID rejected");
        check(data.damageCopy(arena, copy, 1).applied() == 1, "one copy hit counted once");
        check(data.snapshot(arena).orElseThrow().health() == 259, "260 to259 shared pool");
        check(data.damageCopy(arena, copy, Float.NaN).applied() == 0, "invalid copy damage");
        check(data.damageCopy(arena, copy, Float.MAX_VALUE).applied() == 64, "copy overkill clamps");
        check(data.damageCopy(arena, copy, 1).applied() == 0, "dead copy stays dead");
        check(!data.claimRewards(arena, boss), "partial victory cannot pay rewards");
        for (int i = 1; i < 4; i++) {
            var death = data.damageCopy(arena, snapshot.copies().get(i).entity(), 65);
            check(death.defeatedNow() == (i == 3), "only fourth copy completes encounter");
        }
        data = reload(data);
        check(data.snapshot(arena).orElseThrow().phase() == Phase.DEFEATED, "defeated state persisted");
        check(!data.claimRewards(arena, copy), "copy cannot claim reward batch");
        check(data.claimRewards(arena, boss), "one reward authorization");
        check(!data.claimRewards(arena, boss), "repeat reward authorization denied");
        data = reload(data);
        check(!data.claimRewards(arena, boss) && !data.register(arena, boss), "reload does not rearm rewards or boss");
    }

    private static void encounterTrials() throws Exception {
        Random random = new Random(204650);
        for (int trial = 0; trial < 200; trial++) {
            PostenEncounters data = new PostenEncounters();
            UUID arena = id(trial + 1000), boss = id(trial + 2000);
            data.register(arena, boss);
            float oracle = 650;
            while (oracle > 260) {
                float damage = 1 + random.nextInt(120);
                oracle = Math.max(260, oracle - damage);
                data.damageBody(arena, boss, damage);
                data = reload(data);
                check(data.snapshot(arena).orElseThrow().health() == oracle, "body independent arithmetic oracle");
            }
            var order = new ArrayList<>(data.snapshot(arena).orElseThrow().copies());
            Collections.shuffle(order, random);
            for (var copy : order) {
                float hp = 65;
                while (hp > 0) {
                    float damage = 1 + random.nextInt(45);
                    hp = Math.max(0, hp - damage);
                    data.damageCopy(arena, copy.entity(), damage);
                    data = reload(data);
                    var snapshot = data.snapshot(arena).orElseThrow();
                    check(snapshot.copies().stream().filter(c -> c.entity().equals(copy.entity())).findFirst().orElseThrow().health() == hp,
                            "independent per-copy arithmetic oracle");
                    float sum = 0;
                    for (var c : snapshot.copies()) sum += c.health();
                    check(snapshot.health() == sum, "bossbar pool is sum, never double-subtracted");
                }
            }
            check(data.claimRewards(arena, boss) && !reload(data).claimRewards(arena, boss), "trial at-most-once reward claim");
        }
    }

    private static void corruptEncounters() throws Exception {
        PostenEncounters original = new PostenEncounters();
        original.register(id(1), id(2));
        List<CompoundTag> invalids = new ArrayList<>();
        var future = original.save(new CompoundTag(), null); future.putInt("Version", 99); invalids.add(future);
        var noVersion = original.save(new CompoundTag(), null); noVersion.remove("Version"); invalids.add(noVersion);
        var nan = original.save(new CompoundTag(), null); nan.getList("Encounters", 10).getCompound(0).putFloat("BodyHealth", Float.NaN); invalids.add(nan);
        var copies = original.save(new CompoundTag(), null); copies.getList("Encounters", 10).getCompound(0).getList("Copies", 10).remove(0); invalids.add(copies);
        var duplicate = original.save(new CompoundTag(), null); duplicate.getList("Encounters", 10).add(duplicate.getList("Encounters", 10).get(0).copy()); invalids.add(duplicate);
        var paidEarly = original.save(new CompoundTag(), null); paidEarly.getList("Encounters", 10).getCompound(0).putBoolean("RewardsClaimed", true); invalids.add(paidEarly);
        for (CompoundTag bad : invalids) {
            PostenEncounters restored = PostenEncounters.load(binary(bad), null);
            check(restored.isReadOnly(), "invalid/future encounter data fails closed");
            check(!restored.register(id(1), id(2)) && !restored.claimRewards(id(1), id(2)), "no recreation or reward on corrupt save");
            // NaN != NaN in FloatTag.equals; compound serialization order may
            // change on decode. Sorted SNBT retains every key, type and value.
            check(restored.save(new CompoundTag(), null).toString().equals(bad.toString()),
                    "preserve opaque corrupt/future NBT: " + bad + " -> " + restored.save(new CompoundTag(), null));
        }
    }

    private static Node node(long id, Scene scene, int x, int y, int z, int exits) {
        return new Node(id(id), scene, new BlockPos(x, y, z), exits);
    }

    private static void rooms() throws Exception {
        LostHallsRooms data = new LostHallsRooms();
        BlockPos origin = new BlockPos(0, 64, 0);
        AtomicInteger checks = new AtomicInteger();
        check(data.nearest(origin, pos -> { checks.incrementAndGet(); return true; }).isEmpty() && checks.get() == 0,
                "no metadata returns no target without world access");
        Node boundary = node(1, Scene.LARGE_HALL, 48, 64, 0, 1);
        check(data.registerCompleted(boundary) && data.registerCompleted(boundary), "completion idempotent");
        check(!data.registerCompleted(node(1, Scene.LARGE_HALL, 1, 64, 0, 1)), "conflicting UUID cannot move metadata");
        check(data.nearest(origin, pos -> true).orElseThrow().equals(boundary), "48 inclusive3D radius");
        check(data.nearest(origin, pos -> false).isEmpty(), "unloaded/blocked entry predicate respected");
        data.registerCompleted(node(2, Scene.LUMINOUS_GROTTO, 49, 64, 0, 1));
        data.registerCompleted(node(3, Scene.CORRIDOR, 1, 64, 0, 1));
        check(data.nearest(origin, pos -> true).orElseThrow().equals(boundary), "outside radius and dead-end corridor excluded");
        Node negative = node(4, Scene.BRIDGE, -17, 64, -16, 2);
        data.registerCompleted(negative);
        check(data.nearest(origin, pos -> true).orElseThrow().equals(negative), "negative chunk boundaries");
        Node vertical = node(5, Scene.FUNGUS_HALL, 0, 113, 0, 1);
        data.registerCompleted(vertical);
        check(data.nearest(origin, pos -> pos.equals(vertical.anchor())).isEmpty(), "not a horizontal cylinder");
        data = reload(data);
        check(data.nearest(origin, pos -> true).orElseThrow().equals(negative), "nearest target survives binary NBT");
        check(data.remove(negative.id()) && !data.remove(negative.id()), "addressed removal idempotent");
        check(data.nearest(origin, pos -> true).orElseThrow().equals(boundary), "removal updates buckets");
        var tieA = node(10, Scene.JUNCTION, 16, 64, 0, 3);
        var tieB = node(11, Scene.LARGE_HALL, -16, 64, 0, 1);
        for (List<Node> order : List.of(List.of(tieA, tieB), List.of(tieB, tieA))) {
            LostHallsRooms ties = new LostHallsRooms();
            order.forEach(ties::registerCompleted);
            check(ties.nearest(origin, pos -> true).orElseThrow().equals(tieA), "stable tie independent of chunk completion order");
        }
        LostHallsRooms density = new LostHallsRooms();
        for (int i = 0; i < 64; i++) check(density.registerCompleted(node(i + 100, Scene.LARGE_HALL, 0, i + 1, 0, 1)), "bounded column entry");
        check(!density.registerCompleted(node(999, Scene.LARGE_HALL, 0, 100, 0, 1)), "overflow explicitly rejected");
        check(reload(density).size() == 64, "density cap NBT");
        check(density.remove(id(100)) && density.registerCompleted(node(999, Scene.LARGE_HALL, 0, 100, 0, 1)), "removal frees bucket capacity");
        LostHallsRooms far = new LostHallsRooms();
        for (int i = 0; i < 5000; i++) far.registerCompleted(node(i + 10000, Scene.LARGE_HALL, 10000 + 16 * i, 64, 10000, 1));
        checks.set(0);
        check(far.nearest(origin, pos -> { checks.incrementAndGet(); return true; }).isEmpty()
                && checks.get() == 0, "5000 distant records cause no block/chunk predicates");
        CompoundTag unknown = data.save(new CompoundTag(), null); unknown.putInt("Version", 99);
        LostHallsRooms unknownData = LostHallsRooms.load(binary(unknown), null);
        check(unknownData.isReadOnly() && unknownData.nearest(origin, pos -> true).isEmpty(), "unknown room schema not misinterpreted");
        check(!unknownData.registerCompleted(boundary) && unknownData.save(new CompoundTag(), null).equals(unknown), "unknown metadata preserved");
        CompoundTag bad = data.save(new CompoundTag(), null); bad.getList("Nodes", 10).getCompound(0).putString("Scene", "FUTURE_ROOM");
        LostHallsRooms corrupt = LostHallsRooms.load(binary(bad), null);
        check(corrupt.isReadOnly() && corrupt.save(new CompoundTag(), null).equals(bad), "corrupt scene not silently dropped");
        for (int exits : new int[]{0, 9}) {
            boolean rejected = false;
            try { node(9999, Scene.CORRIDOR, 0, 64, 0, exits); } catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "invalid exit count rejected");
        }
    }

    private static void standable(Map<BlockPos, Cell> cells, BlockPos feet) {
        cells.put(feet.below(), Cell.SUPPORT);
        cells.put(feet, Cell.CLEAR);
        cells.put(feet.above(), Cell.CLEAR);
    }

    private static void routes() {
        Map<BlockPos, Cell> cells = new HashMap<>();
        BlockPos start = new BlockPos(-17, 64, -17), finish = new BlockPos(17, 64, 17);
        for (int x = -17; x <= 17; x++) for (int z = -17; z <= 17; z++) {
            standable(cells, new BlockPos(x, 64, z));
        }
        var forward = LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(cells), start, List.of(finish), 4096);
        check(forward.status() == Status.CONNECTED, "finite voxel route crosses negative/positive2and4chunk boundaries");
        List<BlockPos> reversed = new ArrayList<>(cells.keySet());
        Collections.reverse(reversed);
        Map<BlockPos, Cell> reverseCells = new LinkedHashMap<>();
        reversed.forEach(pos -> reverseCells.put(pos, cells.get(pos)));
        var backward = LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(reverseCells), start, List.of(finish), 4096);
        check(forward.equals(backward), "route decision independent of snapshot publication order");
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(cells), start, List.of(finish), 5).status()
                == Status.BUDGET_EXCEEDED, "bounded route traversal stops honestly");
        BlockPos center = new BlockPos(0, 64, 0);
        BlockPos firstNeighbor = center.relative(net.minecraft.core.Direction.Plane.HORIZONTAL.iterator().next());
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(cells), center, List.of(firstNeighbor), 2).status()
                == Status.CONNECTED, "target reached at budget boundary returns before exploring unrelated branches");
        Map<BlockPos, Cell> corridor = new HashMap<>();
        for (int x = 0; x <= 4; x++) standable(corridor, new BlockPos(x, 64, 0));
        BlockPos from = new BlockPos(0, 64, 0), to = new BlockPos(4, 64, 0);
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(corridor), from, List.of(to), 20).status()
                == Status.CONNECTED, "two-high physically supported path");
        corridor.put(new BlockPos(2, 63, 0), Cell.HAZARD);
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(corridor), from, List.of(to), 20).status()
                == Status.NO_ROUTE, "lava/hazard on compulsory path rejected");
        corridor.remove(new BlockPos(2, 63, 0));
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(corridor), from, List.of(to), 20).status()
                == Status.NO_ROUTE, "missing support is not a bridge");
        corridor.put(new BlockPos(2, 63, 0), Cell.SUPPORT);
        corridor.put(new BlockPos(2, 65, 0), Cell.SUPPORT);
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(corridor), from, List.of(to), 20).status()
                == Status.NO_ROUTE, "one-high crawl not accepted");
        Map<BlockPos, Cell> steps = new HashMap<>();
        standable(steps, from);
        BlockPos upper = new BlockPos(1, 65, 0);
        standable(steps, upper);
        steps.put(from.above(2), Cell.CLEAR);
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(steps), from, List.of(upper), 20).status()
                == Status.CONNECTED, "one-block step up with jumping headroom");
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(steps), upper, List.of(from), 20).status()
                == Status.CONNECTED, "return route over one-block step down");
        steps.put(from.above(2), Cell.SUPPORT);
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(steps), from, List.of(upper), 20).status()
                == Status.NO_ROUTE, "low ceiling blocks required jump");
        check(LostHallsRouteCheck.verify(new LostHallsRouteCheck.Snapshot(Map.of()), from, List.of(upper), 20).status()
                == Status.INVALID_ENTRY, "unknown geometry cannot pass");
    }


    private static void publicationPlans() throws Exception {
        BlockPos entry = new BlockPos(15, 64, 0), exit = new BlockPos(17, 64, 0);
        Node room = new Node(id(7501), Scene.BRIDGE, entry, 2);
        List<BlockPos> feet = List.of(entry, entry.east(), exit);
        Map<BlockPos, Cell> cells = new HashMap<>();
        feet.forEach(pos -> standable(cells, pos));
        var snapshot = new LostHallsRouteCheck.Snapshot(cells);
        check(org.slavicmyths.losthalls.LostHallsRoomPublication.verifyTopology(room,
                List.of(entry, exit), feet, snapshot), "completed two-chunk passage accepted");
        check(!org.slavicmyths.losthalls.LostHallsRoomPublication.verifyTopology(room,
                List.of(entry, entry), feet, snapshot), "duplicate exit cannot fake completion");
        check(!org.slavicmyths.losthalls.LostHallsRoomPublication.verifyTopology(room,
                List.of(entry), feet, snapshot), "missing exit cannot fake completion");
        check(!org.slavicmyths.losthalls.LostHallsRoomPublication.verifyTopology(room,
                List.of(entry, exit), List.of(entry, entry.east()), snapshot),
                "absent exit cannot fake completion");
        Map<BlockPos, Cell> collapsed = new HashMap<>(cells);
        collapsed.put(entry.east().below(), Cell.HAZARD);
        check(!org.slavicmyths.losthalls.LostHallsRoomPublication.verifyTopology(room,
                List.of(entry, exit), feet, new LostHallsRouteCheck.Snapshot(collapsed)),
                "collapsed crossing rejected before metadata publication");
        check(!org.slavicmyths.losthalls.LostHallsRoomPublication.verifyTopology(room,
                List.of(entry, exit), List.of(entry, entry.east(), exit.offset(97, 0, 0)), snapshot),
                "out-of-bounds geometry rejected");
        LostHallsRooms stored = new LostHallsRooms();
        check(stored.size() == 0, "unpublished room is not navigable");
        check(stored.registerCompleted(room) && reload(stored).size() == 1,
                "metadata survives binary NBT roundtrip (offline, not actual world save)");
    }


    private static org.slavicmyths.losthalls.LostHallsRoomPieces reload(
            org.slavicmyths.losthalls.LostHallsRoomPieces data) throws Exception {
        var restored = org.slavicmyths.losthalls.LostHallsRoomPieces.load(
                binary(data.save(new CompoundTag(), null)), null);
        check(!restored.isReadOnly() && !restored.isDirty(), "piece receipts survived native binary NBT");
        return restored;
    }

    private static void roomPieceReceipts() throws Exception {
        var data = new org.slavicmyths.losthalls.LostHallsRoomPieces();
        Node room = node(8201, Scene.LARGE_HALL, -16, 70, 0, 2);
        var west = new net.minecraft.world.level.ChunkPos(-1, 0);
        var east = new net.minecraft.world.level.ChunkPos(0, 0);
        check(data.status(room.id()).isEmpty(), "unknown room not ready");
        check(data.begin(room, List.of(west, east)), "two-chunk room plan recorded");
        check(data.begin(room, List.of(east, west)), "plan idempotent across chunk ordering");
        check(!data.begin(new Node(room.id(), Scene.LUMINOUS_GROTTO, room.anchor(), 2),
                List.of(west, east)), "same UUID cannot change a reserved scene");
        check(!data.begin(room, List.of(west)), "same UUID cannot shrink planned footprint");
        check(!data.begin(node(8202, Scene.BRIDGE, -16, 70, 0, 2),
                List.of(west, west)), "duplicate chunk cannot fake a distinct part");
        check(!data.begin(node(8203, Scene.BRIDGE, -16, 70, 0, 2),
                List.of(east)), "footprint must include anchor chunk");
        check(!data.begin(node(8204, Scene.BRIDGE, -16, 70, 0, 2),
                List.of(west, new net.minecraft.world.level.ChunkPos(10, 0))),
                "far-away chunk receipt rejected");

        check(!data.begin(node(8205, Scene.BRIDGE, -16, 70, 0, 2),
                List.of(west, new net.minecraft.world.level.ChunkPos(1, 1))),
                "disconnected room footprint rejected even inside bounded radius");
        check(!data.begin(node(8206, Scene.BRIDGE, -16, 70, 0, 2),
                List.of(west, new net.minecraft.world.level.ChunkPos(0, 1))),
                "diagonal corner contact is not a walkable room connection");
        check(data.begin(node(8207, Scene.BRIDGE, -16, 70, 0, 2),
                List.of(west, east, new net.minecraft.world.level.ChunkPos(0, 1))),
                "connected three-chunk room accepted regardless of input order");
        check(!data.acknowledge(room.id(), new net.minecraft.world.level.ChunkPos(1, 0)),
                "unplanned chunk receipt rejected");
        check(!data.acknowledge(id(999), west), "unregistered room cannot receive a chunk");
        check(data.acknowledge(room.id(), east) && data.acknowledge(room.id(), east),
                "chunk receipt replay is idempotent");
        check(!data.status(room.id()).orElseThrow().ready(), "one of two chunks cannot publish");
        check(data.status(room.id()).orElseThrow().completed().size() == 1,
                "repeated receipt did not count twice");
        data = reload(data);
        check(!data.status(room.id()).orElseThrow().ready(), "partial completion survives reload");
        check(data.acknowledge(room.id(), west), "final actual chunk receipt accepted");
        check(data.status(room.id()).orElseThrow().ready(), "only fully acknowledged plan becomes ready");
        data = reload(data);
        check(data.status(room.id()).orElseThrow().ready(), "completed set survives reload");
        CompoundTag valid = data.save(new CompoundTag(), null);
        CompoundTag future = valid.copy();
        future.putInt("Version", 999);
        var readOnly = org.slavicmyths.losthalls.LostHallsRoomPieces.load(binary(future), null);
        check(readOnly.isReadOnly() && !readOnly.begin(room, List.of(west, east))
                && !readOnly.acknowledge(room.id(), west) && readOnly.status(room.id()).isEmpty(),
                "unknown schema cannot rearm a room plan");
        check(readOnly.save(new CompoundTag(), null).equals(future),
                "unknown completion schema preserved byte-for-byte in NBT values");
        CompoundTag repeated = valid.copy();
        var receipt = repeated.getList("Plans", 10).getCompound(0);
        receipt.putLongArray("Completed", new long[]{west.toLong(), west.toLong()});
        var invalid = org.slavicmyths.losthalls.LostHallsRoomPieces.load(binary(repeated), null);
        check(invalid.isReadOnly() && invalid.size() == 0,
                "duplicate completed receipt fails closed after reload");
        CompoundTag missing = valid.copy();
        missing.getList("Plans", 10).getCompound(0).putLongArray("Expected", new long[]{east.toLong()});
        check(org.slavicmyths.losthalls.LostHallsRoomPieces.load(binary(missing), null).isReadOnly(),
                "corrupt plan excluding anchor chunk fails closed");
    }


    private static void postenReloadPlan() throws Exception {
        UUID arena = id(9201), body = id(9202);
        PostenEncounters journal = new PostenEncounters();
        check(journal.register(arena, body), "recovery fixture registered");
        var first = journal.snapshot(arena).orElseThrow();
        var pending = org.slavicmyths.losthalls.PostenReconciliation.inspect(first, List.of(), false);
        check(pending.deferred() && !pending.hasChanges(), "no boss spawning before all arena chunks loaded");
        var bodyPlan = org.slavicmyths.losthalls.PostenReconciliation.inspect(first, List.of(), true);
        check(bodyPlan.missing().size() == 1 && bodyPlan.missing().getFirst().isBody()
                && bodyPlan.missing().getFirst().health() == 650, "missing body restored at journal HP");
        check(!org.slavicmyths.losthalls.PostenReconciliation.inspect(first,
                List.of(body), true).hasChanges(), "loaded body kept without second spawn");
        check(journal.damageBody(arena, body, 390).splitNow(), "phase boundary accepted");
        journal = reload(journal);
        var split = journal.snapshot(arena).orElseThrow();
        var firstSplit = org.slavicmyths.losthalls.PostenReconciliation.inspect(split,
                List.of(body), true);
        check(firstSplit.missing().size() == 4 && firstSplit.stale().equals(java.util.Set.of(body)),
                "body removed and exactly four correct clones requested after restart");
        UUID dead = split.copies().getFirst().entity();
        check(journal.damageCopy(arena, dead, 65).applied() == 65, "first clone died");
        journal = reload(journal);
        var afterDeath = org.slavicmyths.losthalls.PostenReconciliation.inspect(
                journal.snapshot(arena).orElseThrow(), List.of(dead), true);
        check(afterDeath.missing().size() == 3 && afterDeath.stale().contains(dead),
                "dead clone never resurrected, stale physical clone removed");
        for (int i = 1; i < 4; i++) {
            check(journal.damageCopy(arena, split.copies().get(i).entity(), 65).applied() == 65,
                    "remaining clone killed once");
        }
        journal = reload(journal);
        var finished = journal.snapshot(arena).orElseThrow();
        check(finished.phase() == Phase.DEFEATED, "terminal status survives binary reload");
        var noBoss = org.slavicmyths.losthalls.PostenReconciliation.inspect(
                finished, List.of(body, dead), true);
        check(noBoss.missing().isEmpty() && noBoss.stale().containsAll(List.of(body, dead)),
                "defeated arena never respawns a body or copy");
        boolean rejected = false;
        try {
            org.slavicmyths.losthalls.PostenReconciliation.inspect(split, List.of(body, body), true);
        } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "duplicate observed entity UUID rejected");
        var badCopies = new ArrayList<>(split.copies());
        badCopies.set(1, badCopies.getFirst());
        var invalid = new PostenEncounters.Snapshot(arena, body, Phase.COPIES, 260,
                badCopies, false);
        rejected = false;
        try {
            org.slavicmyths.losthalls.PostenReconciliation.inspect(invalid, List.of(), true);
        } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "malformed saved mask identities fail closed");
        var foreignClone = new ArrayList<>(split.copies());
        var oldClone = foreignClone.getFirst();
        foreignClone.set(0, new PostenEncounters.Copy(id(9998), oldClone.mask(), oldClone.health()));
        rejected = false;
        try {
            org.slavicmyths.losthalls.PostenReconciliation.inspect(
                    new PostenEncounters.Snapshot(arena, body, Phase.COPIES, 260,
                            foreignClone, false), List.of(), true);
        } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "forged copy UUID cannot request resurrection");
        var deadBeforeSplit = new ArrayList<>(first.copies());
        var before = deadBeforeSplit.getFirst();
        deadBeforeSplit.set(0, new PostenEncounters.Copy(before.entity(), before.mask(), 0));
        rejected = false;
        try {
            org.slavicmyths.losthalls.PostenReconciliation.inspect(
                    new PostenEncounters.Snapshot(arena, body, Phase.BODY, 650,
                            deadBeforeSplit, false), List.of(), true);
        } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "body phase with prematurely dead clone fails closed");
    }


    private static void regionTopology() {
        java.util.Set<UUID> all = new java.util.HashSet<>();
        for (long seed : new long[]{42041, 42042, 42043, 42044, 42045}) {
            for (int rx : new int[]{-2, 0, 2}) {
                var plan = org.slavicmyths.losthalls.LostHallsRegionTopology.plan(seed, rx, -1, 76);
                check(plan.equals(org.slavicmyths.losthalls.LostHallsRegionTopology.plan(seed, rx, -1, 76)),
                        "same seed and region produce the same topology");
                check(plan.rooms().size() == 6 && plan.isConnected(), "six scenes and arena connected");
                check(plan.rooms().stream().map(Node::scene).collect(java.util.stream.Collectors.toSet())
                        .equals(java.util.EnumSet.allOf(Scene.class)), "six distinct scene types");
                check(plan.links().size() == 9, "ring, alternative edge and two independent arena approaches");
                java.util.Map<UUID, Integer> degrees = new java.util.HashMap<>();
                for (var link : plan.links()) {
                    degrees.merge(link.from(), 1, Integer::sum);
                    degrees.merge(link.to(), 1, Integer::sum);
                }
                for (Node node : plan.rooms()) {
                    check(node.exits() == degrees.get(node.id()), "node exits agree with graph degree");
                    check(all.add(node.id()), "room ID is seed-and-region unique");
                }
                check(degrees.get(plan.arenaId()) == 2 && all.add(plan.arenaId()),
                        "one unique planned Posten arena");
            }
        }
        boolean invalid = false;
        try { org.slavicmyths.losthalls.LostHallsRegionTopology.plan(42041, 200_000, 0, 76); }
        catch (IllegalArgumentException expected) { invalid = true; }
        check(invalid, "reject out-of-bounds region before coordinate conversion");
        invalid = false;
        try { org.slavicmyths.losthalls.LostHallsRegionTopology.plan(42041, 0, 0, 255); }
        catch (IllegalArgumentException expected) { invalid = true; }
        check(invalid, "reject unsafe vertical planning anchor");
    }

    public static void main(String[] args) throws Exception {
        rewardCrashCuts();
        rewardAuthority();
        physicalRegionRoutes();
        encounterIdentityIsolation();
        encounterBoundaries();
        encounterTrials();
        corruptEncounters();
        rooms();
        routes();
        publicationPlans();
        roomPieceReceipts();
        postenReloadPlan();
        regionTopology();
        Path out = Path.of(System.getProperty("halls.logic.report","docs/implementation/lost-halls-2.0.4/offline-logic.json"));
        Files.createDirectories(out.getParent());
        String report = "{\n  \"scope\": \"Production journals, binary NBT, finite voxel routes and 20 pure heightfield regions; no generated worlds\",\n"
                + "  \"status\": \"PASS\",\n  \"assertions\": " + assertions
                + ",\n  \"randomized_encounters\": 200,\n  \"distant_room_records\": 5000,\n"
                + "  \"runtime_launches\": 0,\n  \"gameplay\": \"NOT_RUN_ENV_BLOCK\"\n}\n";
        Files.writeString(out, report);
        System.out.println(report);
    }

    private static void rewardCrashCuts() throws Exception {
        for(int cut=1;cut<=4;cut++){
            var folder=Files.createTempDirectory("slavic-reward-cut-");String java=Path.of(System.getProperty("java.home"),"bin","java.exe").toString();
            var process=new ProcessBuilder(java,"-cp",System.getProperty("java.class.path"),"org.slavicmyths.verify.RewardCrashCut",folder.toString(),Integer.toString(cut)).redirectErrorStream(true).start();
            check(process.waitFor()==31,"deliberate process cut "+cut+": "+new String(process.getInputStream().readAllBytes()));
            var authority=new org.slavicmyths.losthalls.BossRewardAuthority(folder.resolve("authority.json"));
            var pin=authority.pin(new org.slavicmyths.losthalls.BossRewardAuthority.Entry("arena","magma",new UUID(0,2),6,0));check(pin.recipient().equals(new UUID(0,1))&&pin.cores()==4,"world rollback never transfers reward to B at cut "+cut);
            var player=folder.resolve("playerA.json");boolean delivered=Files.exists(player);if(!delivered)RewardCrashCut.save(player,"inventory=one_batch;receipt=arena");
            check(Files.readString(player).equals("inventory=one_batch;receipt=arena")&&!Files.exists(folder.resolve("playerB.json")),"recipient A recovers exactly one coupled batch/receipt; B none");
            for(var file:Files.list(folder).toList())Files.delete(file);Files.delete(folder);
        }
    }

    private static void rewardAuthority() throws Exception {
        var folder=Files.createTempDirectory("slavicmyths-reward-test-");var file=folder.resolve("authority.json");
        var a=new org.slavicmyths.losthalls.BossRewardAuthority(file);
        var first=new org.slavicmyths.losthalls.BossRewardAuthority.Entry("magma:1","magma",id(1),4,80);
        check(a.pin(first).equals(first),"first pin durable");byte[] bytes=Files.readAllBytes(file);
        a=new org.slavicmyths.losthalls.BossRewardAuthority(file);
        check(a.pin(new org.slavicmyths.losthalls.BossRewardAuthority.Entry("magma:1","magma",id(2),6,0)).equals(first),"rollback cannot reassign recipient or reroll amount");
        check(java.util.Arrays.equals(bytes,Files.readAllBytes(file)),"duplicate pin never rewrites authority");
        check(a.forRecipient(id(1)).size()==1&&a.forRecipient(id(2)).isEmpty(),"recipient index isolated");
        boolean conflict=false;try{a.pin(new org.slavicmyths.losthalls.BossRewardAuthority.Entry("magma:1","posten",id(2),0,0));}catch(java.io.IOException expected){conflict=true;}check(conflict,"cross-kind collision rejected");
        for(String invalid:List.of("{broken","{\"version\":2,\"entries\":[]}")){
            Files.writeString(file,invalid);a=new org.slavicmyths.losthalls.BossRewardAuthority(file);check(a.isReadOnly(),"invalid authority fail closed");
            boolean denied=false;try{a.pin(first);}catch(java.io.IOException expected){denied=true;}check(denied&&Files.readString(file).equals(invalid),"invalid authority bytes preserved");
        }
        Files.delete(file);Files.delete(folder);
    }

    private static void physicalRegionRoutes() {
        for(long seed:new long[]{42041,42042,42043,42044,42045})for(int rx:new int[]{-2,-1,0,1}){
            var plan=org.slavicmyths.losthalls.LostHallsRegionTopology.plan(seed,rx,-1,76);
            int width=192,ox=rx*width,oz=-width;int[] floor=new int[width*width];java.util.Arrays.fill(floor,Integer.MIN_VALUE);
            for(int x=0;x<width;x++)for(int z=0;z<width;z++){
                var column=org.slavicmyths.losthalls.HallsGeometry.column(plan,ox+x,oz+z);
                if(column!=null&&!column.pillar()&&column.ceiling()-column.floor()>=2)floor[x*width+z]=column.floor();
            }
            var start=plan.rooms().getFirst().anchor();int first=(start.getX()-ox)*width+start.getZ()-oz;
            java.util.Set<Integer> reached=new java.util.HashSet<>();java.util.ArrayDeque<Integer> pending=new java.util.ArrayDeque<>();reached.add(first);pending.add(first);
            while(!pending.isEmpty()){
                int cell=pending.remove(),x=cell/width,z=cell%width;
                for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=x+d[0],nz=z+d[1];if(nx<0||nz<0||nx>=width||nz>=width)continue;int next=nx*width+nz;if(floor[next]!=Integer.MIN_VALUE&&Math.abs((long)floor[cell]-floor[next])<=1&&reached.add(next))pending.add(next);}
            }
            for(var n:plan.rooms()){check(floor[(n.anchor().getX()-ox)*width+n.anchor().getZ()-oz]==n.anchor().getY()-1,"published anchor has its actual floor");check(reached.contains((n.anchor().getX()-ox)*width+n.anchor().getZ()-oz),"physical heightfield route to "+n.scene()+" seed="+seed+" region="+rx);}
            check(reached.contains((plan.arena().getX()-ox)*width+plan.arena().getZ()-oz),"physical heightfield route to arena seed="+seed+" region="+rx);
            for(int dx=-16;dx<16;dx++)for(int dz=-16;dz<16;dz++){
                var c=org.slavicmyths.losthalls.HallsGeometry.column(plan,plan.arena().getX()+dx,plan.arena().getZ()+dz);
                check(c!=null&&!c.pillar()&&!c.openBelow()&&c.ceiling()-c.floor()>=5,"32x32 arena footprint clear");
            }
        }
    }

    private static void encounterIdentityIsolation() throws Exception {
        UUID arena = id(70001), body = id(70002), secondArena = id(70003);
        PostenEncounters data = new PostenEncounters();
        check(data.register(arena, body), "identity fixture registration");
        UUID copy = data.snapshot(arena).orElseThrow().copies().getFirst().entity();
        check(!data.register(secondArena, copy), "existing clone cannot become another arena body");
        PostenEncounters reverse = new PostenEncounters();
        check(reverse.register(secondArena, copy), "reverse identity fixture registration");
        check(!reverse.register(arena, body), "new clone cannot reuse another arena body");
        PostenEncounters self = new PostenEncounters();
        check(!self.register(arena, copy), "body cannot reuse its own deterministic clone UUID");
        data = reload(data);
        check(!data.register(secondArena, copy), "identity reservation survives binary NBT");

        // Both records are individually valid under V1, but share an entity UUID.
        var firstTag = data.save(new CompoundTag(), null);
        var secondTag = reverse.save(new CompoundTag(), null);
        firstTag.getList("Encounters", 10).add(secondTag.getList("Encounters", 10).getCompound(0).copy());
        var corrupt = PostenEncounters.load(binary(firstTag), null);
        check(corrupt.isReadOnly(), "cross-arena UUID alias fails closed");
        check(corrupt.save(new CompoundTag(), null).equals(firstTag), "aliased V1 preserved verbatim");
        check(!corrupt.register(id(70004), id(70005)), "aliased journal cannot authorize a new encounter");
    }
}
