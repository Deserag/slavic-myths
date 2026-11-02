package org.slavicmyths.posad;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Immutable exact approved R1 city-local voxel sources for #350 and #351.
 * These are DISTRICTS in the same single native 113x113 PosadPiece, not
 * independently located structures. Classpath assets are generated from
 * versioned source JSON without reinterpretation of drawing/loot weights.
 */
public final class PosadNextBuildings {
    private static final District DRUZHINA = load("druzhinny_dvor_2_2_4", 12, 75, 28, 18, 25, 2440, true);
    private static final District FORGE = load("kuznechny_dvor_2_2_5", 12, 44, 26, 18, 26, 1901, false);

    private record District(int originX, int originZ, int width, int height, int depth, BlockState[] states, String[] loot) {
        int index(int localX, int y, int localZ) { return (y * depth + localZ) * width + localX; }
        boolean contains(int x, int y, int z) { return x >= originX && x < originX + width && z >= originZ && z < originZ + depth && y >= 0 && y < height; }
        BlockState voxel(int x, int y, int z) { return states[index(x - originX, y, z - originZ)]; }
        String loot(int x, int y, int z) { return loot[index(x - originX, y, z - originZ)]; }
    }

    private static JsonObject read(String name) {
        String path = "/data/slavicmyths/posad/" + name + ".gz.base64";
        try (InputStream input = PosadNextBuildings.class.getResourceAsStream(path)) {
            if (input == null) throw new IllegalStateException("Missing immutable posad district: " + path);
            String text = new String(input.readAllBytes(), StandardCharsets.US_ASCII).trim();
            try (GZIPInputStream decoded = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(text)))) {
                return JsonParser.parseString(new String(decoded.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid posad district resource " + path, e);
        }
    }

    private static District load(String name, int ox, int oz, int width, int height, int depth, int expected, boolean rowModel) {
        JsonObject model = read(name);
        BlockState[] cells = new BlockState[width * height * depth];
        String[] loot = new String[cells.length];
        Map<String, BlockState> parsed = new HashMap<>();
        int count = 0;
        if (rowModel) {
            JsonArray layers = model.getAsJsonArray("rows_y_z_x");
            JsonObject palette = model.getAsJsonObject("palette");
            if (layers.size() != height) throw new IllegalStateException(name + ": wrong number of layers");
            for (int y = 0; y < height; y++) {
                JsonArray rows = layers.get(y).getAsJsonArray();
                if (rows.size() != depth) throw new IllegalStateException(name + ": wrong depth");
                for (int z = 0; z < depth; z++) {
                    String row = rows.get(z).getAsString();
                    if (row.length() != width) throw new IllegalStateException(name + ": wrong width");
                    for (int x = 0; x < width; x++) {
                        String symbol = String.valueOf(row.charAt(x));
                        if (".".equals(symbol)) continue;
                        if (!palette.has(symbol)) throw new IllegalStateException(name + ": invalid symbol " + symbol);
                        int pos = (y * depth + z) * width + x;
                        cells[pos] = parsed.computeIfAbsent(palette.get(symbol).getAsString(), PosadNextBuildings::parseState);
                        count++;
                    }
                }
            }
        } else {
            for (var entry : model.getAsJsonArray("voxels")) {
                JsonObject v = entry.getAsJsonObject();
                int x = v.get("u").getAsInt(), y = v.get("y").getAsInt(), z = v.get("v").getAsInt();
                if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) throw new IllegalStateException(name + ": invalid voxel");
                int pos = (y * depth + z) * width + x;
                if (cells[pos] != null) throw new IllegalStateException(name + ": duplicate voxel");
                cells[pos] = parsed.computeIfAbsent(v.get("block").getAsString(), PosadNextBuildings::parseState);
                count++;
            }
        }
        if (count != expected) throw new IllegalStateException(name + ": expected " + expected + " blocks, got " + count);
        JsonArray markers = rowModel ? model.getAsJsonArray("loot_markers") : model.getAsJsonArray("chests");
        if (markers.size() != 5) throw new IllegalStateException(name + ": expected exactly five chests");
        int marked = 0, physical = 0;
        for (BlockState state : cells) if (state != null && state.is(Blocks.CHEST)) physical++;
        for (var entry : markers) {
            JsonObject v = entry.getAsJsonObject();
            int x, y, z;
            String table;
            if (rowModel) {
                JsonArray p = v.getAsJsonArray("local");
                x = p.get(0).getAsInt(); y = p.get(1).getAsInt(); z = p.get(2).getAsInt();
                table = v.get("table").getAsString();
            } else {
                x = v.get("u").getAsInt(); y = v.get("y").getAsInt(); z = v.get("v").getAsInt();
                table = v.get("loot_table").getAsString();
            }
            if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth || !table.startsWith("slavicmyths:chests/")) throw new IllegalStateException(name + ": invalid chest marker");
            int pos = (y * depth + z) * width + x;
            if (cells[pos] == null || !cells[pos].is(Blocks.CHEST) || loot[pos] != null) throw new IllegalStateException(name + ": duplicate/nonchest marker");
            loot[pos] = table.substring("slavicmyths:".length());
            marked++;
        }
        if (marked != 5 || physical != 5) throw new IllegalStateException(name + ": chest count mismatch");
        return new District(ox, oz, width, height, depth, cells, loot);
    }

    static BlockState parseState(String input) {
        int begin = input.indexOf('[');
        String id = begin < 0 ? input : input.substring(0, begin);
        ResourceLocation key = ResourceLocation.parse(id);
        if (!BuiltInRegistries.BLOCK.containsKey(key)) throw new IllegalStateException("Unknown Minecraft block in approved R1: " + id);
        BlockState state = BuiltInRegistries.BLOCK.get(key).defaultBlockState();
        if (begin < 0) return state;
        if (!input.endsWith("]")) throw new IllegalStateException("Invalid R1 block state " + input);
        String props = input.substring(begin + 1, input.length() - 1);
        for (String pair : props.split(",")) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) throw new IllegalStateException("Malformed R1 property " + pair);
            Property<?> property = null;
            for (Property<?> candidate : state.getProperties()) if (candidate.getName().equals(parts[0])) { property = candidate; break; }
            if (property == null) throw new IllegalStateException("Unsupported R1 property " + pair + " of " + id);
            state = withProperty(state, property, parts[1]);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState withProperty(BlockState state, Property<T> property, String value) {
        return state.setValue(property, property.getValue(value).orElseThrow(() -> new IllegalStateException("Invalid approved R1 property " + property.getName() + "=" + value)));
    }

    public static BlockState voxel(int cityX, int y, int cityZ) {
        if (DRUZHINA.contains(cityX, y, cityZ)) return DRUZHINA.voxel(cityX, y, cityZ);
        if (FORGE.contains(cityX, y, cityZ)) return FORGE.voxel(cityX, y, cityZ);
        return null;
    }

    public static String chestLoot(int cityX, int y, int cityZ) {
        if (DRUZHINA.contains(cityX, y, cityZ)) return DRUZHINA.loot(cityX, y, cityZ);
        if (FORGE.contains(cityX, y, cityZ)) return FORGE.loot(cityX, y, cityZ);
        return null;
    }

    private PosadNextBuildings() {}
}
