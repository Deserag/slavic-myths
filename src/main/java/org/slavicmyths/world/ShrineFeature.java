package org.slavicmyths.world;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slavicmyths.registry.ModBlocks;

/** A small physical ruin placed during chunk generation, entirely inside its source chunk. */
public final class ShrineFeature extends Feature<NoneFeatureConfiguration> {
    public ShrineFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){WorldGenLevel world=context.level();ChunkGenerator generator=context.chunkGenerator();RandomSource random=context.random();BlockPos origin=context.origin();NoneFeatureConfiguration config=context.config();
        if (!world.getLevel().dimension().equals(Level.OVERWORLD)) return false;
        int x = (origin.getX() & ~15) + 8, z = (origin.getZ() & ~15) + 8;
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos center = new BlockPos(x, y, z);
        // Verify the whole footprint before writing: no water, steep slopes, trees or existing buildings.
        for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
            BlockPos ground = center.offset(dx, -1, dz);
            Block block = world.getBlockState(ground).getBlock();
            if (block != Blocks.GRASS_BLOCK && block != Blocks.DIRT && block != Blocks.PODZOL && block != Blocks.COARSE_DIRT) return false;
            for (int dy = 0; dy < 4; dy++) {
                BlockPos p = center.offset(dx, dy, dz);
                if (!world.getFluidState(p).isEmpty() || (!world.isEmptyBlock(p) && !org.slavicmyths.worldgen.Vegetation.plant(world.getBlockState(p)))) return false;
            }
        }
        for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
            int radius = dx * dx + dz * dz;
            if (radius > 25) continue;
            set(world, center.offset(dx, -1, dz), random.nextInt(3) == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE);
            set(world, center.offset(dx, 0, dz), Blocks.AIR);
            if (radius >= 18 && random.nextInt(3) != 0) {
                set(world, center.offset(dx, 0, dz), Blocks.MOSSY_COBBLESTONE);
                if (random.nextBoolean()) set(world, center.offset(dx, 1, dz), Blocks.COBBLESTONE_WALL);
            }
        }
        set(world, center, ModBlocks.ANCIENT_IDOL.get());
        set(world, center.above(), ModBlocks.ANCIENT_IDOL.get());
        set(world, center.offset(0, 0, 2), ModBlocks.ALTAR.get());
        set(world, center.offset(-2, 0, 0), Blocks.CAMPFIRE);
        set(world, center.offset(2, 0, -2), Blocks.CHEST);
        BlockEntity chest = world.getBlockEntity(center.offset(2, 0, -2));
        if (chest instanceof ChestBlockEntity) ((ChestBlockEntity) chest).setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths", "chests/ancient_shrine")), random.nextLong());
        for (int dy = 0; dy < 3; dy++) set(world, center.offset(-3, dy, -2), Blocks.OAK_LOG);
        for (BlockPos leaf : new BlockPos[]{center.offset(-3, 3, -2), center.offset(-2, 2, -2), center.offset(-4, 2, -2)})
            world.setBlock(leaf, Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), 2);
        set(world, center.offset(2, -1, 3), Blocks.DIRT);
        set(world, center.offset(-2, -1, 3), Blocks.DIRT);
        set(world, center.offset(2, 0, 3), ModBlocks.WORMWOOD.get());
        set(world, center.offset(-2, 0, 3), Blocks.FERN);
        return true;
    }
    private static void set(WorldGenLevel world, BlockPos pos, Block block) { world.setBlock(pos, block.defaultBlockState(), 2); }
}
