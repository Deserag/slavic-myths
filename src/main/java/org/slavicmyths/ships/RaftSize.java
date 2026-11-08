package org.slavicmyths.ships;

/** Stable progression: the initial 3x3 registry item remains compatible with older saves. */
public enum RaftSize {
    SMALL(3, 3, "raft_2511_test", "Плот 3×3"),
    LONG(3, 5, "raft_3x5", "Удлинённый плот 3×5"),
    WIDE(5, 5, "raft_5x5", "Широкий плот 5×5"),
    CARGO(5, 7, "raft_5x7", "Большой плот 5×7");

    public final int width;
    public final int length;
    public final String id;
    public final String title;

    RaftSize(int width, int length, String id, String title) {
        this.width = width;
        this.length = length;
        this.id = id;
        this.title = title;
    }

    public static RaftSize of(int width, int length) {
        for (RaftSize size : values()) if (size.width == width && size.length == length) return size;
        throw new IllegalArgumentException("Unsupported raft size " + width + "×" + length);
    }

    public RaftSize next() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }

    public int maxSails() { return this == CARGO ? 2 : (ordinal() >= 1 ? 1 : 0); }
}
