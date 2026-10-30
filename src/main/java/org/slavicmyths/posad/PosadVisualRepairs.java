package org.slavicmyths.posad;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Patch 2.3, schema 4 only: seal the underlap below each upward roof step without
 * altering the owner-approved R1 voxels or their original chest markers.
 * A solid strip below adjacent stair courses closes the visible diagonal sky gaps.
 * This overlay is applied only to NEW cities; old saved pieces keep their schema.
 */
public final class PosadVisualRepairs {
    public static BlockState voxel(int cityX, int y, int cityZ) {
        if (cityX >= 42 && cityX <= 70 && cityZ >= 12 && cityZ <= 34) {
            int localZ = cityZ - 12;
            int roofY = 12 + Math.min(localZ / 2, (22 - localZ) / 2);
            if (y == roofY - 1 && roofY > 12 && PosadBuildings.voxel(cityX, y, cityZ) == null)
                return Blocks.NETHER_BRICKS.defaultBlockState();
            if (y == 1 && (cityX == 53 || cityX == 60) && (cityZ == 17 || cityZ == 29)
                && PosadBuildings.voxel(cityX, y, cityZ) == null)
                return Blocks.SOUL_LANTERN.defaultBlockState();
        }
        if (cityX >= 13 && cityX <= 35 && cityZ >= 15 && cityZ <= 29) {
            int localZ = cityZ - 15;
            int roofY = 13 + Math.min(localZ / 2, (14 - localZ) / 2);
            if (y == roofY - 1 && roofY > 13 && PosadBuildings.voxel(cityX, y, cityZ) == null)
                return Blocks.NETHER_BRICKS.defaultBlockState();
            if ((y == 1 || y == 7) && (cityX == 17 || cityX == 31) && (cityZ == 18 || cityZ == 23)
                && PosadBuildings.voxel(cityX, y, cityZ) == null)
                return Blocks.SOUL_LANTERN.defaultBlockState();
        }
        return null;
    }

    private PosadVisualRepairs() {}
}
