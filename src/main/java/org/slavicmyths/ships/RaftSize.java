package org.slavicmyths.ships;

/** Four owner-approved raft hull footprints. Width is across the oars, length is fore/aft. */
public enum RaftSize {
    SMALL(3,3,4,"Плот"),
    LONG(3,5,6,"Удлинённый плот"),
    WIDE(5,5,8,"Широкий плот"),
    LARGE(5,7,10,"Большой плот");

    public final int width;
    public final int length;
    public final int capacity;
    public final String displayName;

    RaftSize(int width, int length, int capacity, String displayName) {
        this.width = width;
        this.length = length;
        this.capacity = capacity;
        this.displayName = displayName;
    }

    public static RaftSize byIndex(int index) {
        RaftSize[] sizes = values();
        return index >= 0 && index < sizes.length ? sizes[index] : SMALL;
    }
}
