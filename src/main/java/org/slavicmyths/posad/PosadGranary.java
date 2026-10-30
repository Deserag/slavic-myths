package org.slavicmyths.posad;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Strictly decodes the approved 2.2.7 NBT asset, without generating extra loot.
 * Only freshly-created schema-7 cities use this district; older cities are immutable.
 */
public final class PosadGranary {
    public static final int ORIGIN_X = 76, ORIGIN_Z = 12, WIDTH = 25, HEIGHT = 11, DEPTH = 28;
    private static final int EXPECTED_BLOCKS = 1349, EXPECTED_CHESTS = 5;
    private static final String RESOURCE = "/data/slavicmyths/structure/posad/granary_2_2_8.nbt";
    private record Model(BlockState[] states, String[] loot) {}
    private static final Model MODEL = load();

    private static int index(int x, int y, int z) { return (y * DEPTH + z) * WIDTH + x; }

    public static BlockState voxel(int cityX, int y, int cityZ) {
        int x = cityX - ORIGIN_X, z = cityZ - ORIGIN_Z;
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH ? MODEL.states()[index(x, y, z)] : null;
    }

    public static String chestLoot(int cityX, int y, int cityZ) {
        int x = cityX - ORIGIN_X, z = cityZ - ORIGIN_Z;
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH ? MODEL.loot()[index(x, y, z)] : null;
    }

    private static Model load() {
        try (InputStream input = PosadGranary.class.getResourceAsStream(RESOURCE)) {
            if (input == null) throw new IllegalStateException("Missing approved granary NBT " + RESOURCE);
            CompoundTag root = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            ListTag dims = root.getList("size", Tag.TAG_INT);
            if (dims.size() != 3 || dims.getInt(0) != WIDTH || dims.getInt(1) != HEIGHT || dims.getInt(2) != DEPTH)
                throw new IllegalStateException("Unexpected granary dimensions");
            ListTag palette = root.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = root.getList("blocks", Tag.TAG_COMPOUND);
            if (blocks.size() != EXPECTED_BLOCKS || palette.isEmpty()) throw new IllegalStateException("Unexpected granary block count/palette");
            BlockState[] cells = new BlockState[WIDTH * HEIGHT * DEPTH];
            String[] loot = new String[cells.length];
            BlockState[] states = new BlockState[palette.size()];
            for (int i = 0; i < states.length; i++) states[i] = parse(palette.getCompound(i));
            int chestCount = 0;
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag entry = blocks.getCompound(i);
                ListTag pos = entry.getList("pos", Tag.TAG_INT);
                if (pos.size() != 3) throw new IllegalStateException("Invalid granary voxel coordinates");
                int x = pos.getInt(0), y = pos.getInt(1), z = pos.getInt(2);
                int stateIndex = entry.getInt("state");
                if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH || stateIndex < 0 || stateIndex >= states.length)
                    throw new IllegalStateException("Granary voxel out of range");
                int at = index(x, y, z);
                if (cells[at] != null) throw new IllegalStateException("Duplicate granary voxel");
                BlockState state = states[stateIndex];
                if (state.isAir()) throw new IllegalStateException("Unexpected air voxel in authored NBT");
                cells[at] = state;
                if (entry.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag data = entry.getCompound("nbt");
                    String table = data.getString("LootTable");
                    if (!state.is(Blocks.CHEST) || !"minecraft:chest".equals(data.getString("id")) || !table.startsWith("slavicmyths:chests/posad_granary_"))
                        throw new IllegalStateException("Invalid granary chest loot " + x + "," + y + "," + z);
                    loot[at] = table.substring("slavicmyths:".length());
                }
                if (state.is(Blocks.CHEST)) {
                    chestCount++;
                    if (loot[at] == null) throw new IllegalStateException("Chest without loot table");
                }
            }
            if (chestCount != EXPECTED_CHESTS) throw new IllegalStateException("Granary chest count " + chestCount);
            return new Model(cells, loot);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read granary NBT", e);
        }
    }

    private static BlockState parse(CompoundTag tag) {
        ResourceLocation key = ResourceLocation.parse(tag.getString("Name"));
        if (!BuiltInRegistries.BLOCK.containsKey(key)) throw new IllegalStateException("Unknown granary block " + key);
        BlockState state = BuiltInRegistries.BLOCK.get(key).defaultBlockState();
        CompoundTag props = tag.getCompound("Properties");
        for (String name : props.getAllKeys()) {
            Property<?> property = state.getProperties().stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow(() -> new IllegalStateException("Unknown block property " + name));
            state = withProperty(state, property, props.getString(name));
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState withProperty(BlockState state, Property<T> property, String value) {
        return state.setValue(property, property.getValue(value).orElseThrow(() -> new IllegalStateException("Invalid property value " + value)));
    }

    private PosadGranary() {}
}
