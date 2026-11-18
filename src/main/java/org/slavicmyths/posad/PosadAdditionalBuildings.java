package org.slavicmyths.posad;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Approved #355/#356 city-local districts. Compact tables retain the complete
 * original block states and the ten fixed source loot markers. No random slots,
 * duplicate settlement start or automatic retrofit of schema 1-3 cities.
 */
public final class PosadAdditionalBuildings {
    private static final District ZELEYNAYA = load("zeleynaya_2_2_7", 76, 44, 25, 13, 26, 1599);
    private static final District GRANARY = load("granary_2_2_8", 76, 12, 25, 11, 28, 1349);

    private record District(int x, int z, int width, int height, int depth, BlockState[] states, String[] loot) {
        boolean contains(int cx, int cy, int cz) { return cx >= x && cx < x + width && cz >= z && cz < z + depth && cy >= 0 && cy < height; }
        int index(int cx, int cy, int cz) { return (cy * depth + cz - z) * width + cx - x; }
        BlockState voxel(int cx, int cy, int cz) { return states[index(cx, cy, cz)]; }
        String loot(int cx, int cy, int cz) { return loot[index(cx, cy, cz)]; }
    }

    private static JsonObject read(String id) {
        String path = "/data/slavicmyths/posad/" + id + ".json";
        try (InputStream stream = PosadAdditionalBuildings.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing Posad district resource " + path);
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid Posad district resource " + path, e);
        }
    }

    private static District load(String id, int ox, int oz, int width, int height, int depth, int expectedBlocks) {
        JsonObject model = read(id);
        JsonArray origin = model.getAsJsonArray("origin");
        JsonArray dimensions = model.getAsJsonArray("size");
        if (origin.get(0).getAsInt() != ox || origin.get(1).getAsInt() != oz || dimensions.get(0).getAsInt() != width || dimensions.get(1).getAsInt() != height || dimensions.get(2).getAsInt() != depth) throw new IllegalStateException(id + ": wrong city footprint");
        BlockState[] states = new BlockState[width * height * depth];
        String[] loot = new String[states.length];
        JsonArray palette = model.getAsJsonArray("palette");
        BlockState[] parsed = new BlockState[palette.size()];
        for (int i = 0; i < parsed.length; i++) parsed[i] = PosadNextBuildings.parseState(palette.get(i).getAsString());
        JsonArray blocks = model.getAsJsonArray("blocks");
        if (blocks.size() != expectedBlocks) throw new IllegalStateException(id + ": wrong voxel count");
        int physicalChests = 0;
        for (var value : blocks) {
            JsonArray b = value.getAsJsonArray();
            int x = b.get(0).getAsInt(), y = b.get(1).getAsInt(), z = b.get(2).getAsInt(), index = b.get(3).getAsInt();
            if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth || index < 0 || index >= parsed.length) throw new IllegalStateException(id + ": voxel outside approved model");
            int position = (y * depth + z) * width + x;
            if (states[position] != null) throw new IllegalStateException(id + ": duplicate voxel");
            states[position] = parsed[index];
            if (parsed[index].is(Blocks.CHEST)) physicalChests++;
        }
        JsonArray markers = model.getAsJsonArray("chests");
        if (markers.size() != 5 || physicalChests != 5) throw new IllegalStateException(id + ": chest count mismatch");
        for (var value : markers) {
            JsonArray c = value.getAsJsonArray();
            int x = c.get(0).getAsInt(), y = c.get(1).getAsInt(), z = c.get(2).getAsInt();
            String table = c.get(3).getAsString();
            if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth || !table.startsWith("chests/posad_")) throw new IllegalStateException(id + ": invalid loot marker");
            int position = (y * depth + z) * width + x;
            if (states[position] == null || !states[position].is(Blocks.CHEST) || loot[position] != null) throw new IllegalStateException(id + ": unknown/duplicate chest");
            loot[position] = table;
        }
        return new District(ox, oz, width, height, depth, states, loot);
    }

    public static BlockState voxel(int x, int y, int z) {
        if (ZELEYNAYA.contains(x, y, z)) return ZELEYNAYA.voxel(x, y, z);
        if (GRANARY.contains(x, y, z)) return GRANARY.voxel(x, y, z);
        return null;
    }

    public static String chestLoot(int x, int y, int z) {
        if (ZELEYNAYA.contains(x, y, z)) return ZELEYNAYA.loot(x, y, z);
        if (GRANARY.contains(x, y, z)) return GRANARY.loot(x, y, z);
        return null;
    }

    private PosadAdditionalBuildings() {}
}
