package org.slavicmyths.ships.prototype;

import java.io.*;
import java.util.*;

/** Pure Java, offline-only SPIKE; not a Minecraft Entity or a production renderer. */
public final class Raft26 {
    public static final int FORMAT = 1;
    public static final int WIDTH = 3;
    public static final int CAPACITY = 4;
    private static final int MAX_MODULES = WIDTH * WIDTH;

    public record Cell(int x, int z) {
        public Cell { if (x < 0 || x >= WIDTH || z < 0 || z >= WIDTH) throw new IllegalArgumentException("outside deck"); }
    }
    public record Stack(String id, int count, String components) {
        public Stack {
            Objects.requireNonNull(id);
            Objects.requireNonNull(components);
            if (id.isBlank() || id.length() > 256 || count < 1 || count > 64 || components.length() > 4096) throw new IllegalArgumentException("invalid stack");
        }
    }
    public enum Equipment { CHEST, BARREL, CRAFTING_TABLE }
    public static final class Module {
        public final Equipment kind;
        private final TreeMap<Integer, Stack> slots = new TreeMap<>();
        private Module(Equipment kind) { this.kind = kind; }
        public Map<Integer, Stack> slots() { return Collections.unmodifiableMap(slots); }
        private int slotCount() { return kind == Equipment.CRAFTING_TABLE ? 0 : 27; }
        private void put(int slot, Stack stack) {
            if (slot < 0 || slot >= slotCount()) throw new IllegalArgumentException("invalid inventory slot");
            if (stack == null) slots.remove(slot); else slots.put(slot, stack);
        }
        private double weight() { return 4.0 + slots.values().stream().mapToDouble(s -> s.count() / 8.0).sum(); }
    }

    private final UUID id;
    private final Map<Cell, Module> modules = new HashMap<>();
    private final Map<UUID, Cell> players = new HashMap<>();
    private UUID rower;
    private double x, z, heading, speed, heel;
    private int dangerousTicks, recoveryProgress, recoveryGrace;
    private boolean capsized, packed;

    public Raft26(UUID id) { this.id = Objects.requireNonNull(id); }
    public UUID id() { return id; }
    public double x() { return x; }
    public double z() { return z; }
    public double speed() { return speed; }
    public double heel() { return heel; }
    public boolean capsized() { return capsized; }
    public boolean packed() { return packed; }
    public Map<Cell, Equipment> equipment() {
        Map<Cell, Equipment> copy = new HashMap<>();
        modules.forEach((key, value) -> copy.put(key, value.kind));
        return Map.copyOf(copy);
    }
    public Map<Integer, Stack> cargo(Cell cell) {
        Module module = modules.get(cell);
        if (module == null) throw new IllegalArgumentException("no equipment there");
        return Map.copyOf(module.slots());
    }
    private void active() { if (packed) throw new IllegalStateException("raft already packed"); }
    public void board(UUID player, Cell localCell) {
        active();
        if (!players.containsKey(player) && players.size() >= CAPACITY) throw new IllegalStateException("raft is full");
        players.put(Objects.requireNonNull(player), Objects.requireNonNull(localCell));
    }
    public void disembark(UUID player) {
        active(); players.remove(player);
        if (player.equals(rower)) rower = null;
    }
    public void rower(UUID player) {
        active();
        if (!players.containsKey(player)) throw new IllegalArgumentException("rower must be aboard");
        rower = player;
    }
    public void place(String equipmentId, Cell position) {
        active();
        Equipment equipment = switch (equipmentId) {
            case "minecraft:chest" -> Equipment.CHEST;
            case "minecraft:barrel" -> Equipment.BARREL;
            case "minecraft:crafting_table" -> Equipment.CRAFTING_TABLE;
            default -> throw new IllegalArgumentException("equipment is not verified: " + equipmentId);
        };
        if (modules.containsKey(position) || modules.size() >= MAX_MODULES) throw new IllegalStateException("occupied deck cell");
        modules.put(position, new Module(equipment));
    }
    public void store(Cell position, int slot, Stack stack) {
        active();
        Module module = modules.get(position);
        if (module == null) throw new IllegalArgumentException("no equipment");
        module.put(slot, stack);
    }
    public void tick(boolean onWater, double forward, double steer) {
        active();
        if (!onWater || capsized) { speed = 0; return; }
        forward = Math.clamp(forward, -1.0, 1.0);
        steer = Math.clamp(steer, -1.0, 1.0);
        speed = Math.clamp(speed * 0.93 + (rower == null ? 0 : forward * 0.013), -0.07, 0.18);
        heading += (rower == null ? 0 : steer) * 0.025 * Math.min(1.0, Math.abs(speed) * 9.0);
        x += Math.cos(heading) * speed;
        z += Math.sin(heading) * speed;
        double moment = 0, total = 16; // raft mass, then lateral mass moments about deck centre x=1
        for (Cell cell : players.values()) { moment += 8 * (cell.x() - 1); total += 8; }
        for (Map.Entry<Cell, Module> entry : modules.entrySet()) {
            double weight = entry.getValue().weight();
            moment += weight * (entry.getKey().x() - 1);
            total += weight;
        }
        heel = Math.clamp(40.0 * moment / total, -65.0, 65.0);
        if (recoveryGrace > 0) { recoveryGrace--; dangerousTicks = 0; }
        else if (Math.abs(heel) > 19) dangerousTicks++;
        else dangerousTicks = 0;
        if (dangerousTicks >= 30) { capsized = true; speed = 0; recoveryProgress = 0; }
    }
    /** Push distance/proximity must be server-validated by the later Minecraft adapter. */
    public void pushFromWater(int playersPushing) {
        active();
        if (!capsized || playersPushing < 1 || playersPushing > 4) throw new IllegalArgumentException("invalid rescue");
        recoveryProgress += playersPushing * playersPushing;
        if (recoveryProgress >= 80) {
            capsized = false; heel = 0; dangerousTicks = 0;
            recoveryProgress = 0; recoveryGrace = 120;
        }
    }
    /** Write a complete bounded, versioned snapshot before marking the live raft as packed. */
    public byte[] pack() throws IOException {
        active();
        if (!players.isEmpty()) throw new IllegalStateException("players still aboard");
        byte[] snapshot = save(true);
        packed = true;
        return snapshot;
    }
    public byte[] save() throws IOException { return save(packed); }
    private byte[] save(boolean packedFlag) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(FORMAT);
            out.writeLong(id.getMostSignificantBits()); out.writeLong(id.getLeastSignificantBits());
            out.writeBoolean(packedFlag); out.writeDouble(x); out.writeDouble(z);
            out.writeDouble(heading); out.writeDouble(speed); out.writeDouble(heel);
            out.writeBoolean(capsized); out.writeInt(dangerousTicks); out.writeInt(recoveryGrace);
            out.writeBoolean(rower != null);
            if (rower != null) { out.writeLong(rower.getMostSignificantBits()); out.writeLong(rower.getLeastSignificantBits()); }
            out.writeInt(players.size());
            for (var entry : players.entrySet()) {
                out.writeLong(entry.getKey().getMostSignificantBits()); out.writeLong(entry.getKey().getLeastSignificantBits());
                out.writeByte(entry.getValue().x()); out.writeByte(entry.getValue().z());
            }
            out.writeInt(modules.size());
            for (var entry : modules.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Cell::x).thenComparingInt(Cell::z))).toList()) {
                out.writeByte(entry.getKey().x()); out.writeByte(entry.getKey().z());
                out.writeByte(entry.getValue().kind.ordinal());
                out.writeInt(entry.getValue().slots.size());
                for (var slot : entry.getValue().slots.entrySet()) {
                    out.writeByte(slot.getKey()); out.writeUTF(slot.getValue().id());
                    out.writeByte(slot.getValue().count()); out.writeUTF(slot.getValue().components());
                }
            }
        }
        return bytes.toByteArray();
    }
    public static Raft26 load(byte[] bytes) throws IOException {
        Objects.requireNonNull(bytes);
        if (bytes.length > 131072) throw new IOException("snapshot too large");
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (in.readInt() != FORMAT) throw new IOException("unsupported snapshot format");
            Raft26 raft = new Raft26(new UUID(in.readLong(), in.readLong()));
            raft.packed = in.readBoolean(); raft.x = in.readDouble(); raft.z = in.readDouble();
            raft.heading = in.readDouble(); raft.speed = in.readDouble(); raft.heel = in.readDouble();
            raft.capsized = in.readBoolean(); raft.dangerousTicks = in.readInt(); raft.recoveryGrace = in.readInt();
            if (in.readBoolean()) raft.rower = new UUID(in.readLong(), in.readLong());
            int participants = in.readInt();
            if (participants < 0 || participants > CAPACITY) throw new IOException("bad occupant count");
            for (int i = 0; i < participants; i++) {
                UUID player = new UUID(in.readLong(), in.readLong());
                if (raft.players.put(player, new Cell(in.readUnsignedByte(), in.readUnsignedByte())) != null) throw new IOException("duplicate player");
            }
            if (raft.rower != null && !raft.players.containsKey(raft.rower)) throw new IOException("missing rower");
            int count = in.readInt();
            if (count < 0 || count > MAX_MODULES) throw new IOException("bad module count");
            for (int i = 0; i < count; i++) {
                Cell cell = new Cell(in.readUnsignedByte(), in.readUnsignedByte());
                int kind = in.readUnsignedByte();
                if (kind >= Equipment.values().length) throw new IOException("unknown equipment");
                Module module = new Module(Equipment.values()[kind]);
                int slots = in.readInt();
                if (slots < 0 || slots > module.slotCount()) throw new IOException("bad slot count");
                for (int s = 0; s < slots; s++) {
                    int index = in.readUnsignedByte();
                    if (module.slots.putIfAbsent(index, new Stack(in.readUTF(), in.readUnsignedByte(), in.readUTF())) != null || index >= module.slotCount()) throw new IOException("duplicate or invalid slot");
                }
                if (raft.modules.putIfAbsent(cell, module) != null) throw new IOException("duplicate module");
            }
            if (in.available() != 0) throw new IOException("unexpected trailing bytes");
            return raft;
        } catch (IllegalArgumentException ex) { throw new IOException("invalid snapshot", ex); }
    }
    public static Raft26 deployPacked(byte[] snapshot) throws IOException {
        Raft26 raft = load(snapshot);
        if (!raft.packed || !raft.players.isEmpty()) throw new IOException("not a packed empty raft");
        raft.packed = false;
        raft.rower = null;
        return raft;
    }
}
