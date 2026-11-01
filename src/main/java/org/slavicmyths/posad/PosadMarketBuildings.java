package org.slavicmyths.posad;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Exact market model #352 (2203 blocks, eight fixed one-time loot chests). */
public final class PosadMarketBuildings {
    private static final int X = 75, Z = 75, W = 27, H = 12, D = 25;
    private static final BlockState[] BLOCKS = new BlockState[W * H * D];
    private static final String[] LOOT = new String[BLOCKS.length];

    static {
        String path = "/data/slavicmyths/posad/menovye_ryady_2_2_6.json";
        try (InputStream stream = PosadMarketBuildings.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing market: " + path);
            JsonObject source = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray origin = source.getAsJsonArray("origin"), dimensions = source.getAsJsonArray("size");
            if (origin.get(0).getAsInt() != X || origin.get(1).getAsInt() != Z || dimensions.get(0).getAsInt() != W || dimensions.get(1).getAsInt() != H || dimensions.get(2).getAsInt() != D) throw new IllegalStateException("Market footprint mismatch");
            JsonArray palette = source.getAsJsonArray("palette");
            BlockState[] parsed = new BlockState[palette.size()];
            for (int i = 0; i < parsed.length; i++) parsed[i] = PosadNextBuildings.parseState(palette.get(i).getAsString());
            JsonArray rows = source.getAsJsonArray("blocks");
            if (rows.size() != 2203) throw new IllegalStateException("Market block count mismatch");
            int floor = 0, physicalChests = 0;
            for (var value : rows) {
                JsonArray b = value.getAsJsonArray();
                int x = b.get(0).getAsInt(), y = b.get(1).getAsInt(), z = b.get(2).getAsInt(), paletteId = b.get(3).getAsInt();
                if (!inside(x, y, z) || paletteId < 0 || paletteId >= parsed.length) throw new IllegalStateException("Out-of-bounds market block");
                int idx = index(x, y, z);
                if (BLOCKS[idx] != null) throw new IllegalStateException("Duplicate market block");
                BLOCKS[idx] = parsed[paletteId];
                if (y == 0) floor++;
                if (BLOCKS[idx].is(Blocks.CHEST)) physicalChests++;
            }
            if (floor != 675 || physicalChests != 8) throw new IllegalStateException("Market floor/chest count mismatch");
            JsonArray chests = source.getAsJsonArray("chests");
            if (chests.size() != 8) throw new IllegalStateException("Market must contain eight chests");
            for (var value : chests) {
                JsonArray c = value.getAsJsonArray();
                int x = c.get(0).getAsInt(), y = c.get(1).getAsInt(), z = c.get(2).getAsInt();
                String id = c.get(3).getAsString();
                if (!inside(x, y, z) || !id.startsWith("chests/nether_market_")) throw new IllegalStateException("Bad market chest marker");
                int idx = index(x, y, z);
                if (BLOCKS[idx] == null || !BLOCKS[idx].is(Blocks.CHEST) || LOOT[idx] != null) throw new IllegalStateException("Duplicate/nonchest marker");
                LOOT[idx] = id;
            }
            for (int z = 0; z < D; z++) for (int x = 11; x <= 15; x++) for (int y = 1; y <= 2; y++) if (BLOCKS[index(x,y,z)] != null) throw new IllegalStateException("Market aisle blocked");
        } catch (IOException e) {
            throw new IllegalStateException("Invalid market resource", e);
        }
    }
    private static boolean inside(int x, int y, int z) { return x >= 0 && x < W && y >= 0 && y < H && z >= 0 && z < D; }
    private static int index(int x, int y, int z) { return (y * D + z) * W + x; }
    public static BlockState voxel(int x, int y, int z) { int u = x - X, v = z - Z; return inside(u,y,v) ? BLOCKS[index(u,y,v)] : null; }
    public static String chestLoot(int x, int y, int z) { int u = x - X, v = z - Z; return inside(u,y,v) ? LOOT[index(u,y,v)] : null; }
    private PosadMarketBuildings() {}
}
