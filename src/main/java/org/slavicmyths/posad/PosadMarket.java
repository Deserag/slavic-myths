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
 * Fully authored 2.2.6 Menovye Ryady: four shops, two warehouses, eight lazy
 * loot chests. The approved 2203-voxel R1 NBT is not generated procedurally.
 * It is integrated only for newly created city schema 6 (no retrofits).
 */
public final class PosadMarket {
    public static final int ORIGIN_X = 75, ORIGIN_Z = 75, WIDTH = 27, DEPTH = 25, HEIGHT = 12;
    private static final String RESOURCE = "/data/slavicmyths/structure/posad/menovye_ryady_2_2_6.nbt";
    private record Market(BlockState[] blocks, String[] loot) {}
    private static final Market SOURCE = read();

    private static int index(int x, int y, int z) { return (y * DEPTH + z) * WIDTH + x; }

    public static BlockState voxel(int cityX, int y, int cityZ) {
        int x = cityX - ORIGIN_X, z = cityZ - ORIGIN_Z;
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH ? SOURCE.blocks()[index(x,y,z)] : null;
    }

    public static String chestLoot(int cityX, int y, int cityZ) {
        int x = cityX - ORIGIN_X, z = cityZ - ORIGIN_Z;
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH ? SOURCE.loot()[index(x,y,z)] : null;
    }

    private static Market read() {
        try (InputStream input = PosadMarket.class.getResourceAsStream(RESOURCE)) {
            if (input == null) throw new IllegalStateException("Missing authored market NBT " + RESOURCE);
            CompoundTag root = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            ListTag dims = root.getList("size", Tag.TAG_INT);
            if (dims.size() != 3 || dims.getInt(0) != WIDTH || dims.getInt(1) != HEIGHT || dims.getInt(2) != DEPTH) throw new IllegalStateException("Wrong market size");
            ListTag palette = root.getList("palette", Tag.TAG_COMPOUND);
            ListTag voxels = root.getList("blocks", Tag.TAG_COMPOUND);
            if (voxels.size() != 2203 || palette.isEmpty()) throw new IllegalStateException("Unexpected market voxel count");
            BlockState[] types = new BlockState[palette.size()];
            for (int i = 0; i < palette.size(); i++) types[i] = parse(palette.getCompound(i));
            BlockState[] blocks = new BlockState[WIDTH * HEIGHT * DEPTH];
            String[] loot = new String[blocks.length];
            int chests = 0;
            for (int i = 0; i < voxels.size(); i++) {
                CompoundTag voxel = voxels.getCompound(i);
                ListTag pos = voxel.getList("pos", Tag.TAG_INT);
                if (pos.size() != 3) throw new IllegalStateException("Bad market coordinate");
                int x = pos.getInt(0), y = pos.getInt(1), z = pos.getInt(2), stateIndex = voxel.getInt("state");
                if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH || stateIndex < 0 || stateIndex >= types.length)
                    throw new IllegalStateException("Out of range market voxel");
                int at = index(x,y,z);
                if (blocks[at] != null) throw new IllegalStateException("Duplicate market voxel");
                BlockState state = types[stateIndex];
                if (state.isAir()) throw new IllegalStateException("Explicit air in market NBT");
                blocks[at] = state;
                if (voxel.contains("nbt", Tag.TAG_COMPOUND)) {
                    CompoundTag data = voxel.getCompound("nbt");
                    String table = data.getString("LootTable");
                    if (!state.is(Blocks.CHEST) || !"minecraft:chest".equals(data.getString("id")) || !table.startsWith("slavicmyths:chests/nether_market_"))
                        throw new IllegalStateException("Invalid market chest " + x + "," + y + "," + z);
                    loot[at] = table.substring("slavicmyths:".length());
                }
                if (state.is(Blocks.CHEST)) {
                    chests++;
                    if (loot[at] == null) throw new IllegalStateException("Unbound market chest");
                }
            }
            if (chests != 8) throw new IllegalStateException("Market must contain exactly eight loot chests");
            return new Market(blocks,loot);
        } catch (IOException e) {
            throw new IllegalStateException("Unreadable market NBT",e);
        }
    }

    private static BlockState parse(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.parse(tag.getString("Name"));
        if (!BuiltInRegistries.BLOCK.containsKey(id)) throw new IllegalStateException("Unknown market block " + id);
        BlockState state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
        CompoundTag props = tag.getCompound("Properties");
        for (String name : props.getAllKeys()) {
            Property<?> property = state.getProperties().stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow(() -> new IllegalStateException("Unknown market blockstate property "+name));
            state = withProperty(state,property,props.getString(name));
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState withProperty(BlockState state, Property<T> property, String value) {
        return state.setValue(property,property.getValue(value).orElseThrow(() -> new IllegalStateException("Invalid market blockstate value " + value)));
    }

    private PosadMarket() {}
}
