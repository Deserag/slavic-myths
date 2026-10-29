package org.slavicmyths.posad;

import java.util.Objects;

/**
 * Fail-closed PRECHECK for future server-owned retrofit.
 * No actual scanning, journal persistence, or placement has been implemented here.
 */
public final class PosadRetrofitPolicy {
    public enum Stage { ABSENT, APPLYING, COMPLETE, BLOCKED }
    public enum Decision {
        ALLOW_ONLY_AFTER_PERSISTING_APPLYING, SKIP_COMPLETED,
        DENY_UNTRUSTED_ANCHOR, DENY_RETRY_WITHOUT_RECOVERY,
        DENY_UNLOADED_CHUNKS, DENY_FOREIGN_BLOCKS, DENY_EXISTING_CONTAINER
    }

    /** All flags must be obtained via real full-area, server-side preflight. */
    public record Snapshot(boolean trustedPersistedAnchor, Stage persistedStage, boolean everyChunkReady,
                           boolean foreignBlocks, boolean existingContainer) {
        public Snapshot { Objects.requireNonNull(persistedStage, "persistedStage"); }
    }

    public static Decision decide(Snapshot s) {
        Objects.requireNonNull(s, "snapshot");
        if (!s.trustedPersistedAnchor()) return Decision.DENY_UNTRUSTED_ANCHOR;
        if (s.persistedStage() == Stage.COMPLETE) return Decision.SKIP_COMPLETED;
        if (s.persistedStage() != Stage.ABSENT) return Decision.DENY_RETRY_WITHOUT_RECOVERY;
        if (!s.everyChunkReady()) return Decision.DENY_UNLOADED_CHUNKS;
        if (s.existingContainer()) return Decision.DENY_EXISTING_CONTAINER;
        if (s.foreignBlocks()) return Decision.DENY_FOREIGN_BLOCKS;
        return Decision.ALLOW_ONLY_AFTER_PERSISTING_APPLYING;
    }

    private PosadRetrofitPolicy() {}
}
