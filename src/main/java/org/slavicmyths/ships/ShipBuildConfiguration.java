package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Versioned shipyard blueprint. Both menu actions and placed vessels use the
 * same immutable validated state. Old V2-V8 ships have no ShipBlueprint tag
 * and must continue through their original fixed-hull geometry path.
 */
public record ShipBuildConfiguration(ShipSectionPlan.ShipClass shipClass,
        int extraLength, int extraWidth, List<Integer> sailTiers,
        int decks, boolean bridge, boolean cabin, int hullRevision) {
    public ShipBuildConfiguration(ShipSectionPlan.ShipClass shipClass, int extraLength,
            int extraWidth, List<Integer> sailTiers, int decks, boolean bridge, boolean cabin) {
        this(shipClass, extraLength, extraWidth, sailTiers, decks, bridge, cabin, 1);
    }

    public static final int VERSION = 1;
    public static final String NBT_KEY = "ShipBlueprint";

    public ShipBuildConfiguration {
        if (hullRevision < 1 || hullRevision > 3
                || (hullRevision == 2 && shipClass != ShipSectionPlan.ShipClass.LARGE
                        && shipClass != ShipSectionPlan.ShipClass.LARGEST)
                || (hullRevision == 3 && shipClass != ShipSectionPlan.ShipClass.LARGE))
            throw new IllegalArgumentException("Unsupported hull revision");
        ShipSectionPlan.validate(shipClass, extraLength, extraWidth);
        if (sailTiers == null) throw new IllegalArgumentException("Missing sails");
        sailTiers = List.copyOf(sailTiers);
        new ShipSuperstructurePlan(shipClass, hullWidth(shipClass, extraWidth),
                hullLength(shipClass, extraLength), sailTiers, decks, bridge, cabin, hullRevision);
    }

    public static int hullWidth(ShipSectionPlan.ShipClass type, int extraWidth) {
        int base = switch (type) {
            case SMALL -> 7;
            case MEDIUM -> 13;
            case LARGE, LARGEST -> 21;
        };
        return Math.addExact(base, Math.multiplyExact(extraWidth, 2));
    }

    public static int hullLength(ShipSectionPlan.ShipClass type, int extraLength) {
        int base = switch (type) {
            case SMALL -> 11;
            case MEDIUM -> 21;
            case LARGE, LARGEST -> 33;
        };
        return Math.addExact(base, Math.multiplyExact(extraLength, 4));
    }

    /** Keep legacy class IDs readable without allowing rejected hulls to be built. */
    public boolean withdrawn() {
        return shipClass == ShipSectionPlan.ShipClass.LARGEST
                || shipClass == ShipSectionPlan.ShipClass.LARGE && hullRevision != 3;
    }

    public int width() { return hullWidth(shipClass, extraWidth); }
    public int length() { return hullLength(shipClass, extraLength); }

    public ShipSuperstructurePlan structure() {
        return new ShipSuperstructurePlan(shipClass, width(), length(),
                sailTiers, decks, bridge, cabin, hullRevision);
    }

    public static ShipBuildConfiguration initial(ShipSectionPlan.ShipClass type) {
        if (type == ShipSectionPlan.ShipClass.LARGEST)
            throw new IllegalArgumentException("Rejected tier IV ship has no starting blueprint");
        return new ShipBuildConfiguration(type, 0, 0,
                type == ShipSectionPlan.ShipClass.LARGE ? List.of(1, 1) : List.of(1),
                1, false, false, type == ShipSectionPlan.ShipClass.LARGE ? 3 : 1);
    }

    public ShipBuildConfiguration resize(int lengthSections, int widthSections) {
        return new ShipBuildConfiguration(shipClass, lengthSections, widthSections,
                sailTiers, decks, bridge, cabin, hullRevision);
    }

    public ShipBuildConfiguration withSail(int mast, int tier) {
        List<Integer> next = new ArrayList<>(sailTiers);
        if (mast < 0 || mast > next.size())
            throw new IllegalArgumentException("Unsupported mast index");
        if (mast == next.size()) next.add(tier);
        else next.set(mast, tier);
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                next, decks, bridge, cabin, hullRevision);
    }

    public ShipBuildConfiguration withDecks(int count) {
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, count, bridge, cabin, hullRevision);
    }

    public ShipBuildConfiguration withBridge(boolean enabled) {
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, decks, enabled, cabin, hullRevision);
    }

    public ShipBuildConfiguration withCabin(boolean enabled) {
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, decks, bridge, enabled, hullRevision);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", VERSION);
        if (hullRevision != 1) tag.putInt("HullRevision", hullRevision);
        tag.putString("Class", shipClass.name());
        tag.putInt("ExtraLength", extraLength);
        tag.putInt("ExtraWidth", extraWidth);
        tag.putIntArray("Sails", sailTiers.stream().mapToInt(Integer::intValue).toArray());
        tag.putInt("Decks", decks);
        tag.putBoolean("Bridge", bridge);
        tag.putBoolean("Cabin", cabin);
        return tag;
    }

    /** Reject malformed or future formats rather than silently reducing size. */
    public static ShipBuildConfiguration load(CompoundTag tag) {
        if (tag == null || tag.getInt("Version") != VERSION
                || !tag.contains("Class", Tag.TAG_STRING)
                || !tag.contains("Sails", Tag.TAG_INT_ARRAY)
                || !tag.contains("Decks", Tag.TAG_INT)
                || !tag.contains("ExtraLength", Tag.TAG_INT)
                || !tag.contains("ExtraWidth", Tag.TAG_INT)
                || !tag.contains("Bridge", Tag.TAG_BYTE)
                || !tag.contains("Cabin", Tag.TAG_BYTE))
            throw new IllegalArgumentException("Invalid ship blueprint format");
        ShipSectionPlan.ShipClass type = ShipSectionPlan.ShipClass.valueOf(tag.getString("Class"));
        List<Integer> sails = new ArrayList<>();
        for (int tier : tag.getIntArray("Sails")) sails.add(tier);
        return new ShipBuildConfiguration(type, tag.getInt("ExtraLength"),
                tag.getInt("ExtraWidth"), sails, tag.getInt("Decks"),
                tag.getBoolean("Bridge"), tag.getBoolean("Cabin"),
                tag.contains("HullRevision", Tag.TAG_INT) ? tag.getInt("HullRevision") : 1);
    }

    /** Compact string for synced entity metadata, validated on every decode. */
    public String wire() {
        StringBuilder out = new StringBuilder();
        out.append(shipClass.name()).append(':').append(extraLength).append(':')
                .append(extraWidth).append(':').append(decks).append(':')
                .append(bridge ? '1' : '0').append(':').append(cabin ? '1' : '0').append(':');
        for (int i = 0; i < sailTiers.size(); i++) {
            if (i != 0) out.append(',');
            out.append(sailTiers.get(i));
        }
        if (hullRevision != 1) out.append(":").append(hullRevision);
        return out.toString();
    }

    public static ShipBuildConfiguration fromWire(String value) {
        if (value == null || value.length() > 90)
            throw new IllegalArgumentException("Invalid blueprint wire value");
        String[] parts = value.split(":", -1);
        if (parts.length != 7 && parts.length != 8)
            throw new IllegalArgumentException("Wrong blueprint fields");
        List<Integer> sails = new ArrayList<>();
        if (!parts[6].isEmpty()) for (String tier : parts[6].split(","))
            sails.add(Integer.parseInt(tier));
        if (!parts[4].equals("0") && !parts[4].equals("1")
                || !parts[5].equals("0") && !parts[5].equals("1"))
            throw new IllegalArgumentException("Invalid blueprint flags");
        return new ShipBuildConfiguration(
                ShipSectionPlan.ShipClass.valueOf(parts[0]),
                Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                sails, Integer.parseInt(parts[3]), parts[4].equals("1"),
                parts[5].equals("1"), parts.length == 8 ? Integer.parseInt(parts[7]) : 1);
    }
}
