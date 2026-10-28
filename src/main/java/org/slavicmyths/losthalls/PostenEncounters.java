package org.slavicmyths.losthalls;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Server-thread encounter accounting. Entity spawning, attacks and item delivery
 * are separate consumers; this journal never loads a chunk or ticks a world. */
public final class PostenEncounters extends SavedData {
    public static final int VERSION = 1;
    public static final float MAX_HEALTH = 650;
    public static final float SPLIT_HEALTH = 260;
    public static final float COPY_HEALTH = 65;

    public enum Phase { BODY, COPIES, DEFEATED }
    public enum Mask { SORROW, ANGER, DECAY, SILENCE }
    public record Copy(UUID entity, Mask mask, float health) {}
    public record Snapshot(UUID arena, UUID body, Phase phase, float health,
                           List<Copy> copies, boolean rewardsClaimed) {}
    public record Damage(float applied, boolean splitNow, boolean defeatedNow) {
        private static final Damage NONE = new Damage(0, false, false);
    }

    private static final class Encounter {
        final UUID arena;
        final UUID body;
        final UUID[] copies = new UUID[4];
        final float[] copyHealth = {COPY_HEALTH, COPY_HEALTH, COPY_HEALTH, COPY_HEALTH};
        Phase phase = Phase.BODY;
        float bodyHealth = MAX_HEALTH;
        boolean rewardsClaimed;

        Encounter(UUID arena, UUID body) {
            this.arena = Objects.requireNonNull(arena);
            this.body = Objects.requireNonNull(body);
            for (Mask mask : Mask.values()) {
                copies[mask.ordinal()] = UUID.nameUUIDFromBytes(
                        ("slavicmyths:posten:" + arena + ":" + mask.name())
                                .getBytes(StandardCharsets.UTF_8));
            }
        }

        float health() {
            if (phase == Phase.BODY) return bodyHealth;
            float sum = 0;
            for (float hp : copyHealth) sum += hp;
            return sum;
        }

        Snapshot snapshot() {
            List<Copy> states = new ArrayList<>(4);
            for (Mask mask : Mask.values()) {
                states.add(new Copy(copies[mask.ordinal()], mask, copyHealth[mask.ordinal()]));
            }
            return new Snapshot(arena, body, phase, health(), List.copyOf(states), rewardsClaimed);
        }
    }

    private final Map<UUID, Encounter> encounters = new HashMap<>();
    // Reserve body AND deterministic clone identities, including defeated arenas.
    private final HashSet<UUID> entityIds = new HashSet<>();
    // Preserve unknown/corrupt data verbatim and reject mutations, rather than
    // recreating an encounter and paying rewards a second time after reload.
    private CompoundTag readOnlySnapshot;

    public static PostenEncounters get(ServerLevel level) {
        if (!level.dimension().equals(Level.NETHER) || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Posten journal requires the Nether server thread");
        }
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(PostenEncounters::new, PostenEncounters::load),
                "slavicmyths_posten_encounters");
    }

    public boolean isReadOnly() { return readOnlySnapshot != null; }

    /** One immutable arena identity, one body. An unrelated entity cannot take it over. */
    public boolean register(UUID arena, UUID body) {
        Objects.requireNonNull(arena);
        Objects.requireNonNull(body);
        if (isReadOnly()) return false;
        Encounter existing = encounters.get(arena);
        if (existing != null) return existing.body.equals(body) && existing.phase != Phase.DEFEATED;
        Encounter created = new Encounter(arena, body);
        HashSet<UUID> identities = new HashSet<>();
        identities.add(body);
        for (UUID copy : created.copies) if (!identities.add(copy)) return false;
        if (identities.stream().anyMatch(entityIds::contains)) return false;
        entityIds.addAll(identities);
        encounters.put(arena, created);
        setDirty();
        return true;
    }

    public Optional<Snapshot> snapshot(UUID arena) {
        if (isReadOnly()) return Optional.empty();
        Encounter encounter = encounters.get(arena);
        return encounter == null ? Optional.empty() : Optional.of(encounter.snapshot());
    }

    /** Boundary overkill is discarded: body stops at 260; copies start at 4x65. */
    public Damage damageBody(UUID arena, UUID body, float amount) {
        Encounter e = encounters.get(arena);
        if (isReadOnly() || e == null || !e.body.equals(body) || e.phase != Phase.BODY
                || !validDamage(amount)) return Damage.NONE;
        float applied = Math.min(amount, e.bodyHealth - SPLIT_HEALTH);
        e.bodyHealth = Math.max(SPLIT_HEALTH, e.bodyHealth - applied);
        boolean split = e.bodyHealth == SPLIT_HEALTH;
        if (split) e.phase = Phase.COPIES;
        setDirty();
        return new Damage(applied, split, false);
    }

    public Damage damageCopy(UUID arena, UUID entity, float amount) {
        Encounter e = encounters.get(arena);
        if (isReadOnly() || e == null || e.phase != Phase.COPIES || !validDamage(amount)) {
            return Damage.NONE;
        }
        for (int i = 0; i < e.copies.length; i++) {
            if (!e.copies[i].equals(entity) || e.copyHealth[i] == 0) continue;
            float applied = Math.min(amount, e.copyHealth[i]);
            e.copyHealth[i] = Math.max(0, e.copyHealth[i] - applied);
            boolean defeated = e.health() == 0;
            if (defeated) e.phase = Phase.DEFEATED;
            setDirty();
            return new Damage(applied, false, defeated);
        }
        return Damage.NONE;
    }

    /** At-most-once authorization for ONE reward batch, not item delivery itself.
     * The entity integration must own the death/drop pipeline and crash handling. */
    public boolean claimRewards(UUID arena, UUID body) {
        Encounter e = encounters.get(arena);
        if (isReadOnly() || e == null || !e.body.equals(body)
                || e.phase != Phase.DEFEATED || e.rewardsClaimed) return false;
        e.rewardsClaimed = true;
        setDirty();
        return true;
    }

    private static boolean validDamage(float amount) {
        return Float.isFinite(amount) && amount > 0;
    }

    @Override
    public CompoundTag save(CompoundTag root, HolderLookup.Provider lookup) {
        if (isReadOnly()) return readOnlySnapshot.copy();
        root.putInt("Version", VERSION);
        ListTag list = new ListTag();
        encounters.values().stream().sorted((a, b) -> a.arena.compareTo(b.arena)).forEach(e -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Arena", e.arena);
            tag.putUUID("Body", e.body);
            tag.putString("Phase", e.phase.name());
            tag.putFloat("BodyHealth", e.bodyHealth);
            tag.putBoolean("RewardsClaimed", e.rewardsClaimed);
            ListTag copies = new ListTag();
            for (Mask mask : Mask.values()) {
                CompoundTag copy = new CompoundTag();
                int index = mask.ordinal();
                copy.putUUID("Entity", e.copies[index]);
                copy.putString("Mask", mask.name());
                copy.putFloat("Health", e.copyHealth[index]);
                copies.add(copy);
            }
            tag.put("Copies", copies);
            list.add(tag);
        });
        root.put("Encounters", list);
        return root;
    }

    public static PostenEncounters load(CompoundTag root, HolderLookup.Provider lookup) {
        PostenEncounters data = new PostenEncounters();
        try {
            require(root.contains("Version", Tag.TAG_INT) && root.getInt("Version") == VERSION);
            require(root.contains("Encounters", Tag.TAG_LIST));
            ListTag list = root.getList("Encounters", Tag.TAG_COMPOUND);
            require(list.size() == ((ListTag) root.get("Encounters")).size());
            for (Tag raw : list) {
                CompoundTag tag = (CompoundTag) raw;
                require(tag.hasUUID("Arena") && tag.hasUUID("Body"));
                Encounter e = new Encounter(tag.getUUID("Arena"), tag.getUUID("Body"));
                require(!data.encounters.containsKey(e.arena) && data.entityIds.add(e.body));
                for (UUID copyId : e.copies) require(data.entityIds.add(copyId));
                require(tag.contains("Phase", Tag.TAG_STRING) && tag.contains("BodyHealth", Tag.TAG_FLOAT));
                require(tag.contains("RewardsClaimed", Tag.TAG_BYTE) && tag.contains("Copies", Tag.TAG_LIST));
                e.phase = Phase.valueOf(tag.getString("Phase"));
                e.bodyHealth = tag.getFloat("BodyHealth");
                e.rewardsClaimed = tag.getBoolean("RewardsClaimed");
                ListTag copies = tag.getList("Copies", Tag.TAG_COMPOUND);
                require(copies.size() == 4 && ((ListTag) tag.get("Copies")).size() == 4);
                HashSet<Mask> masks = new HashSet<>();
                for (Tag copyRaw : copies) {
                    CompoundTag copy = (CompoundTag) copyRaw;
                    require(copy.hasUUID("Entity") && copy.contains("Mask", Tag.TAG_STRING)
                            && copy.contains("Health", Tag.TAG_FLOAT));
                    Mask mask = Mask.valueOf(copy.getString("Mask"));
                    int index = mask.ordinal();
                    require(masks.add(mask) && e.copies[index].equals(copy.getUUID("Entity")));
                    float hp = copy.getFloat("Health");
                    require(Float.isFinite(hp) && hp >= 0 && hp <= COPY_HEALTH);
                    e.copyHealth[index] = hp;
                }
                require(Float.isFinite(e.bodyHealth) && e.bodyHealth >= SPLIT_HEALTH && e.bodyHealth <= MAX_HEALTH);
                if (e.phase == Phase.BODY) {
                    require(e.bodyHealth > SPLIT_HEALTH && !e.rewardsClaimed);
                    for (float hp : e.copyHealth) require(hp == COPY_HEALTH);
                } else {
                    require(e.bodyHealth == SPLIT_HEALTH);
                    require(e.phase == Phase.DEFEATED ? e.health() == 0 : e.health() > 0 && !e.rewardsClaimed);
                }
                data.encounters.put(e.arena, e);
            }
        } catch (IllegalArgumentException | ClassCastException invalid) {
            data.encounters.clear();
            data.entityIds.clear();
            data.readOnlySnapshot = root.copy();
        }
        return data;
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalArgumentException("Invalid Posten encounter data");
    }
}
