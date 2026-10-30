package org.slavicmyths.posad;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared deterministic R1 shell. Coordinates are city-local; null means no world write.
 * Only the main south gate opens through the outer defensive perimeter.
 */
public final class PosadShell {
    public static final int HEIGHT = 18;
    private static final int WEST = 1, EAST = 2, NORTH = 4, SOUTH = 8;

    private static BlockState masonry(int x, int y, int z) {
        // Stable, fireproof masonry accents. The same coordinates produce the same blocks on every pass.
        if (y >= 2 && y <= 8 && Math.floorMod(x + z, 11) == 0) return Blocks.POLISHED_BASALT.defaultBlockState();
        int accent = Math.floorMod(x * 17 + z * 31 + y * 11 + Math.floorMod(x * z, 7), 37);
        if (accent <= 1) return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (accent <= 3) return Blocks.NETHER_BRICKS.defaultBlockState();
        if (accent == 4) return Blocks.BLACKSTONE.defaultBlockState();
        return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    }

    private static BlockState road(int x, int z) {
        int accent = Math.floorMod(x * 31 + z * 17 + x * z * 3, 29);
        if (accent <= 1) return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (accent <= 3) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (accent == 4) return Blocks.POLISHED_BASALT.defaultBlockState();
        return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    }

    public static BlockState voxel(int x, int y, int z) {
        if (!PosadGeometry.CITY.contains(x, z) || y < -2 || y >= HEIGHT) return null;
        if (y < 0) return Blocks.BLACKSTONE.defaultBlockState();
        if (y == 0) {
            boolean street = (x >= 53 && x <= 59) || (z >= 40 && z <= 43)
                || (z >= 70 && z <= 74) || PosadGeometry.Slot.VECHE_SQUARE.local().contains(x, z);
            return street ? road(x, z) : Blocks.BLACKSTONE.defaultBlockState();
        }
        // Tower ownership precedes gate and wall ownership, including the corner joints.
        for (int tx : new int[]{0, 102}) for (int tz : new int[]{0, 102}) {
            if (x >= tx && x <= tx + 10 && z >= tz && z <= tz + 10) {
                int indoor = (tx == 0 ? EAST : WEST) | (tz == 0 ? SOUTH : NORTH);
                return tower(x - tx, y, z - tz, 11, 11, tx, tz, indoor, indoor);
            }
        }
        for (int tx : new int[]{42, 62}) {
            if (x >= tx && x <= tx + 8 && z >= 102) {
                int patrol = tx == 42 ? WEST : EAST;
                return tower(x - tx, y, z - 102, 9, 11, tx, 102, NORTH, patrol);
            }
        }
        if (x >= 51 && x <= 61 && z >= 108) {
            if (x >= 53 && x <= 59 && y <= 6) return Blocks.AIR.defaultBlockState();
            if (y <= 8) return masonry(x, y, z);
            return parapet(x, y, z);
        }
        if (x < 5 || x > 107 || z < 5 || z > 107) {
            if (y <= 8) return masonry(x, y, z);
            return parapet(x, y, z);
        }
        return null;
    }

    private static BlockState parapet(int x, int y, int z) {
        if (y > 10) return null;
        boolean outer = x == 0 || x == 112 || z == 0 || z == 112;
        boolean inner = x == 4 || x == 108 || z == 4 || z == 108;
        if (inner) return masonry(x, y, z);
        if (outer) {
            int along = (x == 0 || x == 112) ? z : x;
            if (y == 9 || Math.floorMod(along, 5) < 3) return masonry(x, y, z);
        }
        return Blocks.AIR.defaultBlockState();
    }

    private static BlockState tower(int x, int y, int z, int width, int depth,
                                    int originX, int originZ, int indoorFaces, int patrolFaces) {
        boolean edge = x == 0 || x == width - 1 || z == 0 || z == depth - 1;
        // A full-height internal masonry pier supports EVERY ladder block, even at patrol level.
        // The ladder is never placed against a wall face that is cut away for a doorway.
        if (x == 2 && z == 1 && y <= 14) return masonry(originX + x, y, originZ + z);
        if (x == 2 && z == 2 && y <= 13)
            return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        if (y == 8 || y == 14) return masonry(originX + x, y, originZ + z);
        if (y <= 14) {
            int centerX = (width - 1) / 2, centerZ = (depth - 1) / 2;
            boolean groundDoor =
                ((indoorFaces & WEST) != 0 && x == 0 && Math.abs(z - centerZ) <= 1)
                || ((indoorFaces & EAST) != 0 && x == width - 1 && Math.abs(z - centerZ) <= 1)
                || ((indoorFaces & NORTH) != 0 && z == 0 && Math.abs(x - centerX) <= 1)
                || ((indoorFaces & SOUTH) != 0 && z == depth - 1 && Math.abs(x - centerX) <= 1);
            int patrolZ = originZ == 0 ? 2 : depth - 3;
            int patrolX = originX == 0 ? 2 : width - 3;
            boolean patrolDoor =
                ((patrolFaces & WEST) != 0 && x == 0 && Math.abs(z - patrolZ) <= 1)
                || ((patrolFaces & EAST) != 0 && x == width - 1 && Math.abs(z - patrolZ) <= 1)
                || ((patrolFaces & NORTH) != 0 && z == 0 && Math.abs(x - patrolX) <= 1)
                || ((patrolFaces & SOUTH) != 0 && z == depth - 1 && Math.abs(x - patrolX) <= 1);
            if ((groundDoor && y <= 3) || (patrolDoor && (y == 9 || y == 10)))
                return Blocks.AIR.defaultBlockState();
            return edge ? masonry(originX + x, y, originZ + z) : Blocks.AIR.defaultBlockState();
        }
        int inset = y - 15;
        if (x < inset || x >= width - inset || z < inset || z >= depth - inset) return null;
        return Blocks.NETHER_BRICKS.defaultBlockState();
    }

    private PosadShell() {}
}
