package org.slavicmyths.ships.prototype;

import java.util.*;
import java.io.IOException;

/** No NeoForge required. Run with java -ea; proves state rules, NOT in-game walking or block ticks. */
public final class Raft26Proof {
    private static int checks;
    private static void yes(boolean actual, String label) { checks++; if (!actual) throw new AssertionError(label); }
    private static void mustFail(Runnable action, String label) {
        checks++;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError(label);
    }
    public static void main(String[] args) throws IOException {
        Raft26 raft = new Raft26(UUID.randomUUID());
        var chest = new Raft26.Cell(2,1);
        var table = new Raft26.Cell(0,0);
        raft.place("minecraft:chest", chest);
        raft.place("minecraft:crafting_table", table);
        raft.store(chest, 3, new Raft26.Stack("minecraft:diamond", 17, "{custom_data:{owner:'preserved'}}"));
        mustFail(() -> raft.place("minecraft:piston", new Raft26.Cell(1,1)), "block whitelist");
        mustFail(() -> raft.place("minecraft:barrel", chest), "occupancy collision");
        mustFail(() -> new Raft26.Cell(3, 0), "deck boundary");
        mustFail(() -> raft.store(table, 0, new Raft26.Stack("minecraft:diamond",1,"")), "no table inventory");
        UUID[] crew = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
        for (UUID player : crew) raft.board(player, new Raft26.Cell(1,1));
        mustFail(() -> raft.board(UUID.randomUUID(), new Raft26.Cell(1,1)), "4 players only");
        raft.rower(crew[0]);
        double originalX = raft.x();
        raft.tick(false, 1, 0);
        yes(raft.x() == originalX, "water-only motion");
        for (int i = 0; i < 80; i++) raft.tick(true, 1, .1);
        yes(raft.x() > originalX + 1, "rower moves raft");
        yes(raft.cargo(chest).get(3).count() == 17, "cargo remains after motion");
        byte[] snapshot = raft.save();
        Raft26 loaded = Raft26.load(snapshot);
        yes(loaded.id().equals(raft.id()), "stable raft id");
        yes(loaded.x() == raft.x() && loaded.z() == raft.z(), "motion persisted");
        yes(loaded.cargo(chest).equals(raft.cargo(chest)), "equipment and exact cargo persisted");
        yes(loaded.equipment().equals(raft.equipment()), "local equipment position persisted");
        for (UUID player : crew) loaded.board(player, new Raft26.Cell(2,1));
        for (int i = 0; i < 35; i++) loaded.tick(true, 0, 0);
        yes(loaded.heel() > 19 && loaded.capsized(), "critical lateral load capsizes");
        mustFail(() -> loaded.pushFromWater(0), "no rescue without pushers");
        for (int i = 0; i < 79; i++) loaded.pushFromWater(1);
        yes(loaded.capsized(), "solo push not instant");
        loaded.pushFromWater(1);
        yes(!loaded.capsized(), "solo rescue after 80 pushes");
        for (UUID player : crew) loaded.board(player, new Raft26.Cell(0,1));
        for (int i = 0; i < 160; i++) loaded.tick(true, 0, 0);
        yes(loaded.capsized(), "repeat overload after grace period");
        for (int i = 0; i < 19; i++) loaded.pushFromWater(2);
        yes(loaded.capsized(), "pair still needs time");
        loaded.pushFromWater(2);
        yes(!loaded.capsized(), "two players restore in 20 pushes");
        for (UUID player : crew) loaded.disembark(player);
        byte[] packed = loaded.pack();
        mustFail(() -> loaded.place("minecraft:barrel", new Raft26.Cell(1,2)), "packed raft frozen");
        mustFail(() -> { try { loaded.pack(); } catch (IOException e) { throw new RuntimeException(e); } }, "no double package");
        Raft26 unpacked = Raft26.deployPacked(packed);
        yes(unpacked.id().equals(loaded.id()), "packed identity remains");
        yes(unpacked.cargo(chest).get(3).equals(new Raft26.Stack("minecraft:diamond",17,"{custom_data:{owner:'preserved'}}")), "packed item data preserved");
        yes(!unpacked.packed(), "deployed raft is active");
        System.out.println("RAFT26 OFFLINE PROOF PASS: " + checks + " assertions. GAMEPLAY/NEOFORGE/MP NOT_RUN.");
    }
}
