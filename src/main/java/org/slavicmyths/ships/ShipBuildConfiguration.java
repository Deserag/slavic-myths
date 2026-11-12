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
        int decks, boolean bridge, boolean cabin, int hullGeometry,
        List<ShipRiggingState> rigging) {
    public static final int VERSION = 2;
    public static final String NBT_KEY = "ShipBlueprint";


    public static List<ShipRiggingState> defaultRigging(List<Integer> tiers) {
        return ShipRiggingState.defaults(tiers);
    }

    /** Old eight-field construction remains binary-source compatible. */
    public ShipBuildConfiguration(ShipSectionPlan.ShipClass shipClass,
            int extraLength, int extraWidth, List<Integer> sailTiers,
            int decks, boolean bridge, boolean cabin, int hullGeometry) {
        this(shipClass, extraLength, extraWidth, sailTiers, decks, bridge, cabin,
                hullGeometry, defaultRigging(sailTiers));
    }

    /** Seven-field construction remains legacy to avoid changing saved ships. */
    public ShipBuildConfiguration(ShipSectionPlan.ShipClass shipClass,
            int extraLength, int extraWidth, List<Integer> sailTiers,
            int decks, boolean bridge, boolean cabin) {
        this(shipClass, extraLength, extraWidth, sailTiers, decks, bridge, cabin, 1);
    }

    public ShipBuildConfiguration {
        ShipSectionPlan.validate(shipClass, extraLength, extraWidth);
        if (hullGeometry != 1 && hullGeometry != 2
                && !(hullGeometry == 3 && shipClass == ShipSectionPlan.ShipClass.MEDIUM)
                && !((hullGeometry >= 4 && hullGeometry <= 9)
                        && (shipClass == ShipSectionPlan.ShipClass.LARGE
                        || shipClass == ShipSectionPlan.ShipClass.LARGEST)))
            throw new IllegalArgumentException("Unsupported modular hull geometry");
        if (sailTiers == null) throw new IllegalArgumentException("Missing sails");
        sailTiers = List.copyOf(sailTiers);
        if (rigging == null || rigging.size() != sailTiers.size())
            throw new IllegalArgumentException("Each mast needs its own rigging");
        rigging = List.copyOf(rigging);
        int maxYards = shipClass == ShipSectionPlan.ShipClass.SMALL ? 2
                : shipClass == ShipSectionPlan.ShipClass.LARGEST ? 4 : 3;
        for (ShipRiggingState r : rigging)
            if (r == null || r.yards() > maxYards
                    || shipClass == ShipSectionPlan.ShipClass.SMALL && r.height() > 1)
                throw new IllegalArgumentException("Rigging exceeds ship class limits");
        new ShipSuperstructurePlan(shipClass, hullWidth(shipClass, extraWidth),
                hullLength(shipClass, extraLength), sailTiers, decks, bridge, cabin, rigging);
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

    public int width() { return hullWidth(shipClass, extraWidth); }
    public int length() { return hullLength(shipClass, extraLength); }

    public ShipSuperstructurePlan structure() {
        return new ShipSuperstructurePlan(shipClass, width(), length(),
                sailTiers, decks, bridge, cabin, rigging);
    }

    public static ShipBuildConfiguration initial(ShipSectionPlan.ShipClass type) {
        return new ShipBuildConfiguration(type, 0, 0, List.of(1), 1, false, false,
                type == ShipSectionPlan.ShipClass.SMALL ? 1
                        : type == ShipSectionPlan.ShipClass.MEDIUM ? 3 : 6);
    }

    public ShipBuildConfiguration resize(int lengthSections, int widthSections) {
        return new ShipBuildConfiguration(shipClass, lengthSections, widthSections,
                sailTiers, decks, bridge, cabin, hullGeometry, rigging);
    }

    public ShipBuildConfiguration withSail(int mast, int tier) {
        List<Integer> next = new ArrayList<>(sailTiers);
        if (mast < 0 || mast > next.size())
            throw new IllegalArgumentException("Unsupported mast index");
        List<ShipRiggingState> upgraded = new ArrayList<>(rigging);
        if (mast == next.size()) {
            next.add(tier);
            upgraded.add(defaultRigging(List.of(tier)).get(0));
        } else {
            int previous = next.set(mast, tier);
            if (upgraded.get(mast).equals(defaultRigging(List.of(previous)).get(0)))
                upgraded.set(mast, defaultRigging(List.of(tier)).get(0));
        }
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                next, decks, bridge, cabin, hullGeometry, upgraded);
    }

    public ShipBuildConfiguration upgradeRigging(int mast, int aspect) {
        if (mast < 0 || mast >= rigging.size())
            throw new IllegalArgumentException("The selected mast does not exist");
        List<ShipRiggingState> upgraded = new ArrayList<>(rigging);
        upgraded.set(mast, upgraded.get(mast).increase(aspect));
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, decks, bridge, cabin, hullGeometry, upgraded);
    }

    public ShipBuildConfiguration withLighting() {
        if (hullGeometry < 6 || hullGeometry >= 9)
            throw new IllegalArgumentException("Lighting requires LARGE/LARGEST V6");
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, decks, bridge, cabin, hullGeometry + 1, rigging);
    }

    public ShipBuildConfiguration withDecks(int count) {
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, count, bridge, cabin, hullGeometry, rigging);
    }

    public ShipBuildConfiguration withBridge(boolean enabled) {
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, decks, enabled, cabin, hullGeometry, rigging);
    }

    public ShipBuildConfiguration withCabin(boolean enabled) {
        return new ShipBuildConfiguration(shipClass, extraLength, extraWidth,
                sailTiers, decks, bridge, enabled, hullGeometry, rigging);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", VERSION);
        tag.putInt("HullGeometry", hullGeometry);
        tag.putString("Class", shipClass.name());
        tag.putInt("ExtraLength", extraLength);
        tag.putInt("ExtraWidth", extraWidth);
        tag.putIntArray("Sails", sailTiers.stream().mapToInt(Integer::intValue).toArray());
        tag.putInt("Decks", decks);
        tag.putBoolean("Bridge", bridge);
        tag.putBoolean("Cabin", cabin);
        int[] serialized = new int[rigging.size() * 7];
        for (int i = 0; i < rigging.size(); i++) {
            ShipRiggingState r = rigging.get(i);
            int j = i * 7;
            serialized[j] = r.height(); serialized[j + 1] = r.strength();
            serialized[j + 2] = r.width(); serialized[j + 3] = r.yards();
            serialized[j + 4] = r.area(); serialized[j + 5] = r.layers();
            serialized[j + 6] = r.fabric();
        }
        tag.putIntArray("Rigging", serialized);
        return tag;
    }

    /** Reject malformed or future formats rather than silently reducing size. */
    public static ShipBuildConfiguration load(CompoundTag tag) {
        if (tag == null || tag.getInt("Version") != 1 && tag.getInt("Version") != VERSION
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
        List<ShipRiggingState> setup = defaultRigging(sails);
        if (tag.getInt("Version") == VERSION) {
            if (!tag.contains("Rigging", Tag.TAG_INT_ARRAY))
                throw new IllegalArgumentException("Missing mast rigging in version 2");
            setup = decodeRigging(tag.getIntArray("Rigging"), sails.size());
        }
        return new ShipBuildConfiguration(type, tag.getInt("ExtraLength"),
                tag.getInt("ExtraWidth"), sails, tag.getInt("Decks"),
                tag.getBoolean("Bridge"), tag.getBoolean("Cabin"),
                tag.contains("HullGeometry", Tag.TAG_INT) ? tag.getInt("HullGeometry") : 1, setup);
    }

    private static List<ShipRiggingState> decodeRigging(int[] values, int count) {
        if (values.length != count * 7) throw new IllegalArgumentException("Invalid rigging array");
        List<ShipRiggingState> setup = new ArrayList<>();
        for (int j = 0; j < values.length; j += 7)
            setup.add(new ShipRiggingState(values[j], values[j + 1], values[j + 2],
                    values[j + 3], values[j + 4], values[j + 5], values[j + 6]));
        return List.copyOf(setup);
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
        out.append(':').append(hullGeometry);
        if (!rigging.equals(defaultRigging(sailTiers))) {
            out.append(':');
            for (int i = 0; i < rigging.size(); i++) {
                if (i > 0) out.append(';');
                ShipRiggingState r = rigging.get(i);
                out.append(r.height()).append(',').append(r.strength()).append(',')
                        .append(r.width()).append(',').append(r.yards()).append(',')
                        .append(r.area()).append(',').append(r.layers()).append(',')
                        .append(r.fabric());
            }
        }
        return out.toString();
    }

    public static ShipBuildConfiguration fromWire(String value) {
        if (value == null || value.length() > 512)
            throw new IllegalArgumentException("Invalid blueprint wire value");
        String[] parts = value.split(":", -1);
        if (parts.length != 7 && parts.length != 8 && parts.length != 9)
            throw new IllegalArgumentException("Wrong blueprint fields");
        List<Integer> sails = new ArrayList<>();
        if (!parts[6].isEmpty()) for (String tier : parts[6].split(","))
            sails.add(Integer.parseInt(tier));
        if (!parts[4].equals("0") && !parts[4].equals("1")
                || !parts[5].equals("0") && !parts[5].equals("1"))
            throw new IllegalArgumentException("Invalid blueprint flags");
        List<ShipRiggingState> setup = defaultRigging(sails);
        if (parts.length == 9) {
            String[] masts = parts[8].split(";", -1);
            int[] values = new int[masts.length * 7];
            for (int i = 0; i < masts.length; i++) {
                String[] fields = masts[i].split(",", -1);
                if (fields.length != 7) throw new IllegalArgumentException("Invalid mast fields");
                for (int j = 0; j < 7; j++) values[i * 7 + j] = Integer.parseInt(fields[j]);
            }
            setup = decodeRigging(values, sails.size());
        }
        return new ShipBuildConfiguration(
                ShipSectionPlan.ShipClass.valueOf(parts[0]),
                Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                sails, Integer.parseInt(parts[3]), parts[4].equals("1"),
                parts[5].equals("1"), parts.length >= 8 ? Integer.parseInt(parts[7]) : 1, setup);
    }
}
