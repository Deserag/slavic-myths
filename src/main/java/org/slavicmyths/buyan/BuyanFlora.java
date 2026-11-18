package org.slavicmyths.buyan;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModFeatures;
import org.slavicmyths.registry.ModItems;

/** Buyan-only flora, without replacing vanilla birches or other wood families. */
public final class BuyanFlora {
    private static final ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> SAPLING_FEATURE =
        ResourceKey.create(Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "gold_birch_sapling"));

    public static final DeferredHolder<Block, Block> GOLD_BIRCH_LOG = ModBlocks.BLOCKS.register(
        "gold_birch_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LOG)));
    public static final DeferredHolder<Block, Block> GOLD_BIRCH_LEAVES = ModBlocks.BLOCKS.register(
        "gold_birch_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LEAVES)));
    public static final DeferredHolder<Block, Block> GOLD_BIRCH_SAPLING = ModBlocks.BLOCKS.register(
        "gold_birch_sapling", () -> new SaplingBlock(
            new TreeGrower("slavicmyths:gold_birch", Optional.empty(), Optional.of(SAPLING_FEATURE), Optional.empty()),
            BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING)));

    public static final DeferredHolder<Block, Block> CHAMOMILE = registerFlower("chamomile");
    public static final DeferredHolder<Block, Block> YARROW = registerFlower("yarrow");
    public static final DeferredHolder<Block, Block> PASQUE_FLOWER = registerFlower("pasque_flower");
    public static final DeferredHolder<Block, Block> HEATHER = registerFlower("heather");
    public static final DeferredHolder<Block, Block> GLOBEFLOWER = registerFlower("globeflower");
    public static final DeferredHolder<Block, Block> TANSY = registerFlower("tansy");
    public static final DeferredHolder<Feature<?>, GoldBirchFeature> GOLD_BIRCH_FEATURE =
        ModFeatures.FEATURES.register("gold_birch_tree", GoldBirchFeature::new);

    static {
        registerItem("gold_birch_log", GOLD_BIRCH_LOG);
        registerItem("gold_birch_leaves", GOLD_BIRCH_LEAVES);
        registerItem("gold_birch_sapling", GOLD_BIRCH_SAPLING);
        registerItem("chamomile", CHAMOMILE);
        registerItem("yarrow", YARROW);
        registerItem("pasque_flower", PASQUE_FLOWER);
        registerItem("heather", HEATHER);
        registerItem("globeflower", GLOBEFLOWER);
        registerItem("tansy", TANSY);
    }
    private static DeferredHolder<Block, Block> registerFlower(String id) {
        return ModBlocks.BLOCKS.register(id, () -> new MatureFlower(
            BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)));
    }
    private static void registerItem(String id, DeferredHolder<Block, Block> block) {
        ModItems.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    /** Adult-only BushBlock: no AGE state, no crop randomTick. */
    private static final class MatureFlower extends BushBlock {
        private static final com.mojang.serialization.MapCodec<MatureFlower> CODEC = simpleCodec(MatureFlower::new);
        private MatureFlower(BlockBehaviour.Properties properties) { super(properties); }
        @Override protected com.mojang.serialization.MapCodec<MatureFlower> codec() { return CODEC; }
    }

    private static boolean soil(BlockState s) {
        return s.is(BlockTags.DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.MOSS_BLOCK);
    }
    private static boolean open(BlockState s) {
        return s.isAir() || s.is(GOLD_BIRCH_SAPLING.get())
            || s.is(BlockTags.REPLACEABLE) || s.is(BlockTags.FLOWERS);
    }

    public static boolean suitable(long seed, int cx, int cz, int x, int y, int z, int sea) {
        return y >= sea + 6 && y <= sea + 30
            && BuyanNature.shoreDistance(seed, cx, cz, x, z) >= 26.0
            && BuyanNature.clearingMetric(seed, cx, cz, x, z) >= 1.62
            && BuyanNature.passage(seed, cx, cz, x, z) <= 0.075
            && BuyanNature.surface(seed, cx, cz, x, z, sea, y) == BuyanTerrain.Surface.MEADOW;
    }

    /** Three rounded, layered crowns with deterministic shape variations. */
    public static Map<BlockPos, BlockState> goldenTree(long seed, int x, int ground, int z, int height) {
        Map<BlockPos, BlockState> shape = new LinkedHashMap<>();
        int variant = Math.min(2, (int) (3.0 * BuyanTerrain.value(seed ^ 0xB22A0921L, x, z, 0)));
        int[][] radii = {{1,2,3,3,2,1}, {2,3,3,2,2,1}, {1,2,3,3,3,1}};
        int top = ground + height;
        for (int dy = -3; dy <= 2; dy++) {
            int r = radii[variant][dy + 3];
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r + (r == 3 ? 1 : 0)) continue;
                if (r > 1 && (Math.abs(dx) == r || Math.abs(dz) == r)
                    && BuyanTerrain.value(seed ^ 0xB22A0922L, x + dx, z + dz, top + dy) < 0.10) continue;
                int distance = Math.min(6, 1 + Math.max(Math.abs(dx), Math.abs(dz)) + (dy == 2 ? 1 : 0));
                BlockState leaf = GOLD_BIRCH_LEAVES.get().defaultBlockState()
                    .setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.DISTANCE, distance);
                shape.put(new BlockPos(x + dx, top + dy, z + dz), leaf);
            }
        }
        for (int dy = 1; dy <= height; dy++) {
            shape.put(new BlockPos(x, ground + dy, z), GOLD_BIRCH_LOG.get().defaultBlockState());
        }
        return shape;
    }

    /** Used only during first-time prespawn construction; never replaces constructed blocks. */
    public static boolean placeNaturalTree(ServerLevel world, long seed, int x, int ground, int z, int height) {
        if (!suitable(seed, 0, 0, x, ground, z, 63)
            || !soil(world.getBlockState(new BlockPos(x, ground, z)))) return false;
        Map<BlockPos, BlockState> shape = goldenTree(seed, x, ground, z, height);
        for (BlockPos pos : shape.keySet()) {
            if (pos.getY() >= world.getMaxBuildHeight() || !world.hasChunkAt(pos)
                || !open(world.getBlockState(pos))) return false;
        }
        shape.forEach((pos, state) -> world.setBlock(pos, state, 2));
        return true;
    }

    /** About 1.8% of eligible columns, all six final-form flowers. */
    public static BlockState flowerAt(long seed, int cx, int cz, int radius,
                                      int x, int ground, int z, int sea) {
        if (!suitable(seed, cx, cz, x, ground, z, sea)
            || BuyanTerrain.value(seed ^ 0xB22A0701L, x, z, 0) < 0.982) return null;
        int species = Math.min(5, (int) (6.0 * BuyanTerrain.value(seed ^ 0xB22A0923L, x, z, 0)));
        return switch (species) {
            case 0 -> CHAMOMILE.get().defaultBlockState();
            case 1 -> YARROW.get().defaultBlockState();
            case 2 -> PASQUE_FLOWER.get().defaultBlockState();
            case 3 -> HEATHER.get().defaultBlockState();
            case 4 -> GLOBEFLOWER.get().defaultBlockState();
            default -> TANSY.get().defaultBlockState();
        };
    }

    /** The vanilla TreeGrower uses this configured feature for bonemeal and sapling maturation. */
    public static final class GoldBirchFeature extends Feature<NoneFeatureConfiguration> {
        public GoldBirchFeature() { super(NoneFeatureConfiguration.CODEC); }
        @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel world = context.level();
            BlockPos pos = context.origin();
            RandomSource random = context.random();
            if (!soil(world.getBlockState(pos.below()))) return false;
            Map<BlockPos, BlockState> shape = goldenTree(random.nextLong(),
                pos.getX(), pos.getY() - 1, pos.getZ(), 6 + random.nextInt(4));
            for (BlockPos cell : shape.keySet()) {
                if (cell.getY() >= world.getMaxBuildHeight() || !world.hasChunkAt(cell)
                    || !open(world.getBlockState(cell))) return false;
            }
            for (var entry : shape.entrySet())
                if (entry.getValue().is(GOLD_BIRCH_LOG.get())) world.setBlock(entry.getKey(), entry.getValue(), 3);
            for (var entry : shape.entrySet())
                if (entry.getValue().is(GOLD_BIRCH_LEAVES.get())) world.setBlock(
                    entry.getKey(), entry.getValue().setValue(LeavesBlock.PERSISTENT, false), 3);
            return true;
        }
    }
    public static void init() {}
    private BuyanFlora() {}
}
