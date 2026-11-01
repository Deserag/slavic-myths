package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persisted per-city guilt and one-time citizen slots; independent of chunks and player sessions. */
public final class PosadGrievances extends SavedData {
    public static final long FORGIVENESS_MILLIS = 900_000L;
    private record City(Map<UUID,Long> guilty, Set<String> spawned) {}
    private final Map<UUID,City> cities = new HashMap<>();
    public PosadGrievances() { super(); }
    public static PosadGrievances get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PosadGrievances::new, (tag, registry) -> {
            PosadGrievances result = new PosadGrievances();
            result.load(tag);
            return result;
        }), "slavicmyths_posad_residents");
    }
    private City city(UUID id) { return cities.computeIfAbsent(id, ignored -> new City(new HashMap<>(), new HashSet<>())); }
    public synchronized void attacked(UUID city, UUID player) {
        if (city == null || player == null) return;
        long deadline = Math.addExact(System.currentTimeMillis(), FORGIVENESS_MILLIS);
        city(city).guilty.put(player, deadline);
        setDirty();
    }
    public synchronized boolean hostile(UUID city, UUID player) {
        if (city == null || player == null) return false;
        City record = cities.get(city);
        if (record == null) return false;
        Long until = record.guilty.get(player);
        if (until == null) return false;
        if (System.currentTimeMillis() < until) return true;
        record.guilty.remove(player);
        setDirty();
        return false;
    }
    public synchronized boolean spawned(UUID city, String key) {
        City record = cities.get(city);
        return record != null && record.spawned.contains(key);
    }
    /** Call only after server successfully inserted a real NPC. Deceased residents are not free loot farms. */
    public synchronized void markSpawned(UUID city, String key) {
        if (city(city).spawned.add(key)) setDirty();
    }
    private synchronized void load(CompoundTag root) {
        cities.clear();
        for (Tag raw : root.getList("Cities", 10)) {
            CompoundTag tag = (CompoundTag) raw;
            if (!tag.hasUUID("Id")) continue;
            City record = city(tag.getUUID("Id"));
            for (Tag item : tag.getList("Aggressors", 10)) {
                CompoundTag p = (CompoundTag) item;
                if (p.hasUUID("Player")) record.guilty.put(p.getUUID("Player"), p.getLong("Until"));
            }
            for (Tag key : tag.getList("Spawned", 8)) record.spawned.add(key.getAsString());
        }
    }
    @Override public synchronized CompoundTag save(CompoundTag root, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (var entry : cities.entrySet()) {
            CompoundTag city = new CompoundTag();
            city.putUUID("Id", entry.getKey());
            ListTag players = new ListTag();
            for (var p : entry.getValue().guilty.entrySet()) {
                CompoundTag aggressor = new CompoundTag();
                aggressor.putUUID("Player", p.getKey());
                aggressor.putLong("Until", p.getValue());
                players.add(aggressor);
            }
            city.put("Aggressors", players);
            ListTag spawned = new ListTag();
            for (String key : entry.getValue().spawned) spawned.add(StringTag.valueOf(key));
            city.put("Spawned", spawned);
            list.add(city);
        }
        root.put("Cities", list);
        return root;
    }
}
