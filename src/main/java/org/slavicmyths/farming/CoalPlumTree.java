package org.slavicmyths.farming;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.registry.ModBlocks;

/** Bounded seven-block Nether fruit tree. The occupied volume is validated before any block is changed. */
public final class CoalPlumTree {
    private static final Map<BlockPos, Boolean> CROWN = crown();

    private static Map<BlockPos, Boolean> crown() {
        Map<BlockPos, Boolean> result = new LinkedHashMap<>();
        for (int y = 2; y <= 6; y++) {
            int radius = y == 6 ? 0 : (y == 3 || y == 4 ? 2 : 1);
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                if (radius == 2 && Math.abs(x) + Math.abs(z) > 3) continue;
                if (x == 0 && z == 0 && y <= 4) continue;
                boolean fruiting = (y == 3 || y == 4) && Math.abs(x) + Math.abs(z) == 2;
                result.put(new BlockPos(x, y, z), fruiting);
            }
        }
        return Map.copyOf(result);
    }

    private static boolean free(LevelReader level, BlockPos base, BlockPos offset) {
        BlockPos pos = base.offset(offset);
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) return false;
        if (level instanceof LevelAccessor accessor && !accessor.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.isAir() || (offset.equals(BlockPos.ZERO) && state.is(ModBlocks.COAL_PLUM_SAPLING.get()));
    }

    public static boolean canGrow(LevelReader level, BlockPos base) {
        if (!level.getBlockState(base.below()).is(Blocks.CRIMSON_NYLIUM)) return false;
        for (int y = 0; y <= 4; y++) if (!free(level, base, new BlockPos(0, y, 0))) return false;
        for (BlockPos offset : CROWN.keySet()) if (!free(level, base, offset)) return false;
        return true;
    }

    public static boolean grow(LevelAccessor level, BlockPos base, RandomSource random, boolean natural) {
        if (!canGrow(level, base)) return false;
        for (int y = 0; y <= 4; y++) level.setBlock(base.above(y), Blocks.CRIMSON_STEM.defaultBlockState(), 3);
        for (Map.Entry<BlockPos, Boolean> entry : CROWN.entrySet()) {
            boolean fruit = entry.getValue();
            int stage = natural && fruit && random.nextInt(5) == 0 ? 3 : 0;
            level.setBlock(base.offset(entry.getKey()), ModBlocks.COAL_PLUM_LEAVES.get().defaultBlockState()
                    .setValue(CoalPlumLeaves.FRUITING, fruit).setValue(CoalPlumLeaves.AGE, stage), 3);
        }
        return true;
    }

    private CoalPlumTree() {}
}
