package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Stable, strict on-disk payload for hulls using physical voxel layout V9. */
public final class ShipSuperstructureData {
    public static final int VERSION = 1;
    public static final int GEOMETRY = 9;

    public static CompoundTag save(ShipSuperstructurePlan plan) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", VERSION);
        tag.putString("Class", plan.shipClass().name());
        tag.putInt("Width", plan.hullWidth());
        tag.putInt("Length", plan.hullLength());
        tag.putInt("Decks", plan.decks());
        tag.putBoolean("Bridge", plan.bridge());
        tag.putBoolean("Cabin", plan.cabin());
        ListTag sails = new ListTag();
        for (int tier : plan.sailTiers()) {
            CompoundTag sail = new CompoundTag();
            sail.putInt("Tier", tier);
            sails.add(sail);
        }
        tag.put("Masts", sails);
        return tag;
    }

    public static ShipSuperstructurePlan load(CompoundTag tag) {
        if (tag == null || tag.getInt("Version") != VERSION
                || !tag.contains("Class", Tag.TAG_STRING)
                || !tag.contains("Width", Tag.TAG_INT)
                || !tag.contains("Length", Tag.TAG_INT)
                || !tag.contains("Decks", Tag.TAG_INT)
                || !tag.contains("Masts", Tag.TAG_LIST))
            throw new IllegalArgumentException("Incomplete ship structure data");
        ShipSectionPlan.ShipClass kind;
        try { kind = ShipSectionPlan.ShipClass.valueOf(tag.getString("Class")); }
        catch (IllegalArgumentException invalid) { throw new IllegalArgumentException("Unknown ship class", invalid); }
        ListTag sails = tag.getList("Masts", Tag.TAG_COMPOUND);
        if (sails.size() > 4) throw new IllegalArgumentException("Too many masts");
        List<Integer> tiers = new ArrayList<>();
        for (int index = 0; index < sails.size(); index++) {
            CompoundTag entry = sails.getCompound(index);
            if (!entry.contains("Tier", Tag.TAG_INT))
                throw new IllegalArgumentException("Missing sail tier");
            tiers.add(entry.getInt("Tier"));
        }
        return new ShipSuperstructurePlan(kind, tag.getInt("Width"), tag.getInt("Length"),
                tiers, tag.getInt("Decks"), tag.getBoolean("Bridge"), tag.getBoolean("Cabin"));
    }

    public static ShipSuperstructurePlan defaults(ShipSectionPlan.ShipClass type) {
        return switch (type) {
            case SMALL -> new ShipSuperstructurePlan(type, 7, 11, List.of(1), 1, false, false);
            case MEDIUM -> new ShipSuperstructurePlan(type, 13, 21, List.of(1), 1, false, false);
            case LARGE -> new ShipSuperstructurePlan(type, 21, 33, List.of(1), 1, false, false);
            case LARGEST -> new ShipSuperstructurePlan(type, 27, 41, List.of(1), 1, false, false);
        };
    }

    /** Exhaustive class-size caps used at the server trust boundary. */
    public static ShipSuperstructurePlan validateBounds(ShipSuperstructurePlan plan) {
        int maxWidth = switch (plan.shipClass()) {
            case SMALL -> 7;
            case MEDIUM -> 15;
            case LARGE -> 23;
            case LARGEST -> 29;
        };
        int maxLength = switch (plan.shipClass()) {
            case SMALL -> 11;
            case MEDIUM -> 25;
            case LARGE -> 37;
            case LARGEST -> 45;
        };
        if (plan.hullWidth() > maxWidth || plan.hullLength() > maxLength)
            throw new IllegalArgumentException("Ship exceeds class maximum hull dimensions");
        return plan;
    }

    private ShipSuperstructureData() {}
}
