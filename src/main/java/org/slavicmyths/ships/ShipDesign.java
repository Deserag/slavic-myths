package org.slavicmyths.ships;

import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Extensible, versioned selection data for the native-block ship.
 * The editor, alternate woods, dyes and heraldry are NOT enabled in 2.6.
 * Saving their stable logical IDs now avoids repacking cargo to upgrade later.
 */
public record ShipDesign(String hullSize, String hullWood, String mastWood,
        int sailColor, String flagPattern, String flagSymbol, String cabinStyle) {
    public static final int VERSION = 1;
    private static final Set<String> SIZES = Set.of("small", "medium", "large", "largest");
    public ShipDesign {
        if (!SIZES.contains(hullSize) || !validResource(hullWood)
                || !validResource(mastWood) || sailColor < 0 || sailColor > 16
                || !validSymbol(flagPattern, 64) || !validSymbol(flagSymbol, 64)
                || !validSymbol(cabinStyle, 48))
            throw new IllegalArgumentException("Invalid ship design");
    }

    private static boolean validResource(String id) {
        return id != null && id.length() <= 128
                && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }
    private static boolean validSymbol(String id, int max) {
        return id != null && !id.isBlank() && id.length() <= max
                && id.matches("[a-z0-9_./:-]+");
    }
    public static ShipDesign defaults() {
        return new ShipDesign("small", "minecraft:spruce_planks",
                "minecraft:stripped_spruce_log", 0, "none", "none", "none");
    }
    public CompoundTag save() {
        CompoundTag data = new CompoundTag();
        data.putInt("Version", VERSION);
        data.putString("HullSize", hullSize);
        data.putString("HullWood", hullWood);
        data.putString("MastWood", mastWood);
        data.putInt("SailColor", sailColor);
        data.putString("FlagPattern", flagPattern);
        data.putString("FlagSymbol", flagSymbol);
        data.putString("CabinStyle", cabinStyle);
        return data;
    }
    public static ShipDesign load(CompoundTag data) {
        if (data.isEmpty()) return defaults();
        if (!data.contains("Version", Tag.TAG_INT) || data.getInt("Version") != VERSION)
            throw new IllegalArgumentException("Unsupported ship design format");
        return new ShipDesign(
                data.getString("HullSize"), data.getString("HullWood"),
                data.getString("MastWood"), data.getInt("SailColor"),
                data.getString("FlagPattern"), data.getString("FlagSymbol"),
                data.getString("CabinStyle"));
    }

    /** Size definitions are proposals only; oversized hulls cannot yet sail. */
    public int proposedWidth() {
        return switch (hullSize) { case "small" -> 7; case "medium" -> 13; case "large" -> 21; default -> 27; };
    }
    public int proposedLength() {
        return switch (hullSize) { case "small" -> 11; case "medium" -> 21; case "large" -> 33; default -> 41; };
    }
}
