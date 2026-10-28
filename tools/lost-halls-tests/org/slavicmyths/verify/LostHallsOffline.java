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

    public static void main(String[] args) throws Exception {
        encounterBoundaries();
        encounterTrials();
        corruptEncounters();
        rooms();
        routes();
        Path out = Path.of("docs/implementation/lost-halls-2.0.4/offline-logic.json");
        Files.createDirectories(out.getParent());
        String report = "{\n  \"scope\": \"Production accounting/index, finite voxel routes and binary NBT only\",\n"
                + "  \"status\": \"PASS\",\n  \"assertions\": " + assertions
                + ",\n  \"randomized_encounters\": 200,\n  \"distant_room_records\": 5000,\n"
                + "  \"runtime_launches\": 0,\n  \"gameplay\": \"NOT_RUN_ENV_BLOCK\"\n}\n";
        Files.writeString(out, report);
        System.out.println(report);
    }
}
