package org.slavicmyths.posad;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.zip.GZIPInputStream;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Exact owner-reviewed R1 voxels, never approximated from an illustration.
 * City-local coordinate lookup only; the enclosing native PosadPiece performs
 * chunk clipping, rotation, terrain protection, and one-time chest binding.
 * No automatic upgrade of previously saved schema-1 structures.
 */
public final class PosadBuildings {
    private static final int VECHE_WIDTH = 29, VECHE_DEPTH = 23, VECHE_HEIGHT = 18;
    private static final int HOUSE_WIDTH = 23, HOUSE_DEPTH = 15, HOUSE_HEIGHT = 17;
    private static final String VECHE_SYMBOLS = "BCFGHKNPQRSTUVX";
    private static final String VECHE_SHA256 = "8813d5c44e144ba5e6f087584f7acbf8dfa4cf25c0e31f6f9193f0b933035cda";
    private static final byte[] VECHE = readVeche();
    private static final char[] HOUSE = readHouse();

    private static byte[] readResource(String path) {
        try (InputStream in = PosadBuildings.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing posad model: " + path);
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Unreadable posad model: " + path, e);
        }
    }

    private static byte[] readVeche() {
        String encoded = new String(readResource("/data/slavicmyths/posad/veche_hall_r1.gz.base64"), StandardCharsets.US_ASCII).trim();
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
            byte[] cells = in.readAllBytes();
            if (cells.length != (VECHE_WIDTH * VECHE_DEPTH * VECHE_HEIGHT + 1) / 2) throw new IllegalStateException("Veche R1 dimensions");
            String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(cells));
            if (!VECHE_SHA256.equals(sha)) throw new IllegalStateException("Veche R1 checksum");
            int placed = 0;
            for (int i = 0; i < VECHE_WIDTH * VECHE_DEPTH * VECHE_HEIGHT; i++) {
                int packed = Byte.toUnsignedInt(cells[i / 2]);
                int value = (i & 1) == 0 ? (packed >>> 4) & 15 : packed & 15;
                if (value > VECHE_SYMBOLS.length()) throw new IllegalStateException("Invalid veche voxel");
                if (value != 0) placed++;
            }
            if (placed != 3598) throw new IllegalStateException("Veche R1 block count " + placed);
            return cells;
        } catch (Exception e) {
            throw new IllegalStateException("Invalid veche hall R1", e);
        }
    }

    private static char[] readHouse() {
        JsonObject source = JsonParser.parseString(new String(readResource("/data/slavicmyths/posad/communal_house_r1.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray layers = source.getAsJsonArray("rows_y_z_x");
        if (layers.size() != HOUSE_HEIGHT) throw new IllegalStateException("House R1 layer count");
        char[] cells = new char[HOUSE_WIDTH * HOUSE_DEPTH * HOUSE_HEIGHT];
        int filled = 0, chests = 0;
        for (int y = 0; y < HOUSE_HEIGHT; y++) {
            JsonArray rows = layers.get(y).getAsJsonArray();
            if (rows.size() != HOUSE_DEPTH) throw new IllegalStateException("House R1 depth");
            for (int z = 0; z < HOUSE_DEPTH; z++) {
                String line = rows.get(z).getAsString();
                if (line.length() != HOUSE_WIDTH) throw new IllegalStateException("House R1 width");
                for (int x = 0; x < HOUSE_WIDTH; x++) {
                    char symbol = line.charAt(x);
                    if (symbol != '.' && !source.getAsJsonObject("palette").has(String.valueOf(symbol))) throw new IllegalStateException("Unknown house block");
                    cells[(y * HOUSE_DEPTH + z) * HOUSE_WIDTH + x] = symbol;
                    if (symbol != '.') filled++;
                    if (symbol == 'H') chests++;
                }
            }
        }
        if (filled != 1992 || chests != 4) throw new IllegalStateException("House R1 block/chest count");
        for (int[] p : new int[][]{{2,1,2},{18,1,2},{7,1,10},{18,7,9}}) {
            if (cells[(p[1] * HOUSE_DEPTH + p[2] + 1) * HOUSE_WIDTH + p[0] + 1] != 'H') throw new IllegalStateException("House R1 chest marker mismatch");
        }
        return cells;
    }

    public static BlockState voxel(int cityX, int y, int cityZ) {
        if (y < 0) return null;
        if (cityX >= 42 && cityX <= 70 && cityZ >= 12 && cityZ <= 34 && y < VECHE_HEIGHT) {
            int index = (y * VECHE_DEPTH + cityZ - 12) * VECHE_WIDTH + cityX - 42;
            int packed = Byte.toUnsignedInt(VECHE[index / 2]);
            int value = (index & 1) == 0 ? (packed >>> 4) & 15 : packed & 15;
            return value == 0 ? null : vecheState(VECHE_SYMBOLS.charAt(value - 1));
        }
        if (cityX >= 13 && cityX <= 35 && cityZ >= 15 && cityZ <= 29 && y < HOUSE_HEIGHT) {
            int index = (y * HOUSE_DEPTH + cityZ - 15) * HOUSE_WIDTH + cityX - 13;
            char symbol = HOUSE[index];
            return symbol == '.' ? null : houseState(symbol);
        }
        return null;
    }

    /** Four approved positions only. Never invent bonus chests or new loot. */
    public static String chestLoot(int cityX, int y, int cityZ) {
        if (y == 1 && cityX == 16 && cityZ == 18) return "chests/nether_communal_pantry";
        if (y == 1 && cityX == 32 && cityZ == 18) return "chests/nether_communal_utility";
        if (y == 1 && cityX == 21 && cityZ == 26) return "chests/nether_communal_personal";
        if (y == 7 && cityX == 32 && cityZ == 25) return "chests/nether_communal_elder";
        return null;
    }

    private static BlockState door(DoorHingeSide hinge, DoubleBlockHalf half) {
        return Blocks.CRIMSON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HINGE, hinge).setValue(DoorBlock.HALF, half);
    }

    private static BlockState vecheState(char symbol) {
        return switch (symbol) {
            case 'B' -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 'C' -> Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
            case 'F' -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case 'G' -> Blocks.IRON_BARS.defaultBlockState();
            case 'H' -> Blocks.CRIMSON_SLAB.defaultBlockState();
            case 'K' -> Blocks.CRIMSON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH).setValue(TrapDoorBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP);
            case 'N' -> Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            case 'P' -> Blocks.POLISHED_BASALT.defaultBlockState();
            case 'Q' -> Blocks.BOOKSHELF.defaultBlockState();
            case 'R' -> Blocks.NETHER_BRICKS.defaultBlockState();
            case 'S' -> Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            case 'T' -> Blocks.NETHER_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            case 'U' -> Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            case 'V' -> Blocks.CRIMSON_PLANKS.defaultBlockState();
            case 'X' -> Blocks.CRIMSON_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            default -> throw new IllegalStateException("Unknown veche symbol " + symbol);
        };
    }

    private static BlockState houseState(char symbol) {
        return switch (symbol) {
            case 'P' -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 'B' -> Blocks.POLISHED_BASALT.defaultBlockState();
            case 'F' -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case 'W' -> Blocks.NETHER_BRICKS.defaultBlockState();
            case 'C' -> Blocks.CRIMSON_PLANKS.defaultBlockState();
            case 'N' -> Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            case 'S' -> Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            case 'L' -> Blocks.CRIMSON_SLAB.defaultBlockState();
            case 'T' -> Blocks.CRIMSON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH).setValue(TrapDoorBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP);
            case 'G' -> Blocks.IRON_BARS.defaultBlockState();
            case 'H' -> Blocks.CHEST.defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING, Direction.SOUTH);
            case 'Q' -> door(DoorHingeSide.LEFT, DoubleBlockHalf.LOWER);
            case 'q' -> door(DoorHingeSide.LEFT, DoubleBlockHalf.UPPER);
            case 'J' -> door(DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER);
            case 'j' -> door(DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER);
            case 'U' -> Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            case 'K' -> Blocks.CRIMSON_FENCE.defaultBlockState();
            case 'V' -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
            default -> throw new IllegalStateException("Unknown communal house symbol " + symbol);
        };
    }

    private PosadBuildings() {}
}
