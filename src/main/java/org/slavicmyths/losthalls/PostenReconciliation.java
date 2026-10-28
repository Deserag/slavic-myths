package org.slavicmyths.losthalls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.slavicmyths.losthalls.PostenEncounters.Copy;
import org.slavicmyths.losthalls.PostenEncounters.Mask;
import org.slavicmyths.losthalls.PostenEncounters.Phase;
import org.slavicmyths.losthalls.PostenEncounters.Snapshot;

/** Safe, read-only reconciliation plan for actual Posten entities after chunk
 * loading / restart. This does not spawn entities, acquire chunk tickets,
 * change encounter HP or authorize loot. The future arena controller must
 * prove EVERY relevant arena chunk and its entities are resident before
 * executing an action, then recheck the journal immediately before spawning. */
public final class PostenReconciliation {
    public record Instance(UUID id, Mask mask, float health) {
        /** A null mask denotes the unique original body, never a fifth copy. */
        public boolean isBody() { return mask == null; }
    }

    public record Plan(boolean deferred, List<Instance> missing,
                       List<Instance> present, Set<UUID> stale) {
        public Plan {
            missing = List.copyOf(missing);
            present = List.copyOf(present);
            stale = Set.copyOf(stale);
        }
        public boolean hasChanges() { return !missing.isEmpty() || !stale.isEmpty(); }
    }

    /** observedIds MUST include all loaded Posten entities belonging to this
     * arena, not arbitrary neighboring mobs. Unknown IDs are left untouched.
     * Duplicated UUID observations or malformed snapshots fail closed. */
    public static Plan inspect(Snapshot state, Collection<UUID> observedIds,
                               boolean allArenaChunksAndEntitiesLoaded) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(observedIds);
        if (!allArenaChunksAndEntitiesLoaded) return new Plan(true, List.of(), List.of(), Set.of());
        if (state.body() == null || state.arena() == null || state.phase() == null
                || state.copies() == null || state.copies().size() != 4) {
            throw new IllegalArgumentException("Invalid Posten snapshot");
        }

        Map<UUID, Instance> expected = new HashMap<>();
        Set<UUID> owned = new HashSet<>();
        owned.add(state.body());
        Set<Mask> masks = new HashSet<>();
        for (Copy copy : state.copies()) {
            if (copy == null || copy.entity() == null || copy.mask() == null
                    || !masks.add(copy.mask()) || !owned.add(copy.entity())
                    || !Float.isFinite(copy.health()) || copy.health() < 0
                    || copy.health() > PostenEncounters.COPY_HEALTH) {
                throw new IllegalArgumentException("Conflicting Posten clone identity");
            }
            if (state.phase() == Phase.COPIES && copy.health() > 0) {
                expected.put(copy.entity(), new Instance(copy.entity(), copy.mask(), copy.health()));
            }
        }
        if (!masks.equals(Set.of(Mask.values()))
                || !Float.isFinite(state.health()) || state.health() < 0
                || state.health() > PostenEncounters.MAX_HEALTH) {
            throw new IllegalArgumentException("Invalid Posten phase health");
        }
        float cloneSum = 0;
        for (Copy copy : state.copies()) cloneSum += copy.health();
        if (state.phase() == Phase.BODY) {
            if (state.health() <= PostenEncounters.SPLIT_HEALTH || state.rewardsClaimed()) {
                throw new IllegalArgumentException("Body phase cannot be complete");
            }
            expected.put(state.body(), new Instance(state.body(), null, state.health()));
        } else if (state.phase() == Phase.COPIES) {
            if (state.health() <= 0 || state.health() != cloneSum || state.rewardsClaimed()) {
                throw new IllegalArgumentException("Clone phase cannot be complete");
            }
        } else if (state.phase() == Phase.DEFEATED) {
            if (state.health() != 0 || cloneSum != 0) {
                throw new IllegalArgumentException("Defeated arena cannot have live clones");
            }
        }

        Set<UUID> observed = new HashSet<>();
        for (UUID id : observedIds) {
            if (id == null || !observed.add(id)) {
                throw new IllegalArgumentException("Duplicate/unknown Posten UUID observation");
            }
        }
        List<Instance> missing = new ArrayList<>();
        List<Instance> present = new ArrayList<>();
        expected.values().stream().sorted((a, b) -> a.id().compareTo(b.id())).forEach(instance -> {
            if (observed.contains(instance.id())) present.add(instance);
            else missing.add(instance);
        });
        Set<UUID> stale = new HashSet<>(observed);
        stale.retainAll(owned);
        stale.removeAll(expected.keySet());
        return new Plan(false, missing, present, stale);
    }

    private PostenReconciliation() {}
}
