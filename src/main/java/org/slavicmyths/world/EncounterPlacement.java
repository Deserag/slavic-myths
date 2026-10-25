package org.slavicmyths.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

/** Loaded terrain only. Callers retain their biome, time, floor and population rules. */
public final class EncounterPlacement {
    public static AABB body(BlockPos feet, float width, float height) {
        return new AABB(feet.getX()+.5-width/2, feet.getY(), feet.getZ()+.5-width/2,
                feet.getX()+.5+width/2, feet.getY()+height, feet.getZ()+.5+width/2);
    }
    public static boolean loaded(LevelAccessor world, AABB box) {
        for (int x=((int)Math.floor(box.minX))>>4; x<=((int)Math.floor(Math.nextDown(box.maxX)))>>4; x++)
            for (int z=((int)Math.floor(box.minZ))>>4; z<=((int)Math.floor(Math.nextDown(box.maxZ)))>>4; z++)
                if (!world.hasChunk(x,z)) return false;
        return true;
    }
    public static boolean clear(LevelAccessor world, AABB box) {
        return loaded(world,box) && world.getWorldBorder().isWithinBounds(box)
                && world.noCollision(box) && !world.containsAnyLiquid(box);
    }
    private EncounterPlacement() {}
}
