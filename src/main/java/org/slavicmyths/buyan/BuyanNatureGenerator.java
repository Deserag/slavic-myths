package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.worldgen.SiteChunks;

/**
 * Manual nature overlay on an already completed Buyan foundation.
 *
 * Pass 0 sculpts the rock and topsoil; pass 1 adds vegetation. Each step
 * touches only the currently ticketed chunk and checks protected blocks.
 * There are no permanent chunk tickets, cross-chunk tree writes or
 * dependencies on the traversal order.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class BuyanNatureGenerator {
    private static final Map<ServerLevel, Work> ACTIVE = new HashMap<>();

    private BuyanNatureGenerator() {}

    private static BuyanState.Site selectedFoundation(ServerLevel world, int x, int z) {
        BuyanState.Site nearest = null;
        long distance = Long.MAX_VALUE;
        for (BuyanState.Site site : BuyanState.get(world).completedSites()) {
            if (site.radius() != BuyanTerrain.MAX_RADIUS) continue;
            long dx = (long) x - site.x();
            long dz = (long) z - site.z();
            long sq = dx * dx + dz * dz;
            if (sq <= (long) site.radius() * site.radius() && sq < distance
                && BuyanNature.shoreDistance(world.getSeed(), site.x(), site.z(), x, z) >= 0) {
                nearest = site;
                distance = sq;
            }
        }
        return nearest;
    }

    public static int start(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        if (world.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Природный этап Буяна доступен только в Overworld."));
            return 0;
        }
        if (BuyanState.get(world).active) {
            source.sendFailure(Component.literal("Сначала завершите генерацию основания Буяна."));
            return 0;
        }
        BuyanNatureState state = BuyanNatureState.get(world);
        if (state.active) {
            source.sendFailure(Component.literal("Природный этап уже запущен; смотрите nature status."));
            return 0;
        }
        int x = (int) Math.floor(source.getPosition().x);
        int z = (int) Math.floor(source.getPosition().z);
        BuyanState.Site site = selectedFoundation(world, x, z);
        if (site == null) {
            source.sendFailure(Component.literal(
                "Здесь нет подтверждённого завершённого основания Буяна. "
                + "Встаньте на готовый остров и проверьте /slavicmyths dev buyan status."));
            return 0;
        }
        if (state.finishedAt(site)) {
            source.sendFailure(Component.literal("Природный этап этого Буяна уже завершён."));
            return 0;
        }
        double cutoff = BuyanNature.forestCutoff(world.getSeed(),
            site.x(), site.z(), site.radius(), world.getSeaLevel());
        if (!Double.isFinite(cutoff)) {
            source.sendFailure(Component.literal("Недостаточно подходящей суши для смешанных лесов."));
            return 0;
        }
        state.begin(site, cutoff);
        source.sendSuccess(() -> Component.literal(
            "Буян: природный этап запущен для острова " + site.x() + "," + site.z()
            + ". Сначала рельеф и грунты, затем леса и подлесок. "
            + "Команды: /slavicmyths dev buyan nature status|pause|resume."), true);
        return 1;
    }

    public static int status(CommandSourceStack source) {
        BuyanNatureState state = BuyanNatureState.get(source.getLevel());
        if (!state.active) {
            source.sendSuccess(() -> Component.literal(
                "Буян: активного природного этапа нет (или он завершён)."), false);
            return 1;
        }
        int count = BuyanGenerator.totalChunks(state.centerX, state.centerZ, state.radius);
        int percent = (int) Math.min(100L,
            (100L * (state.phase * (long) count + state.chunkIndex)) / (2L * count));
        source.sendSuccess(() -> Component.literal(
            "Буян природа: " + (state.phase == 0 ? "рельеф/грунты" : "леса/подлесок")
            + "; " + percent + "%; чанк " + state.chunkIndex + "/" + count
            + "; пропуски: чанки " + state.skippedChunks
            + ", колонки " + state.skippedColumns
            + "; посажено деревьев " + state.plantedTrees
            + "; " + (state.paused ? "ПАУЗА" : "ВЫПОЛНЯЕТСЯ")
            + (state.lastError.isEmpty() ? "" : "; " + state.lastError)), false);
        return 1;
    }

    public static int pause(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanNatureState state = BuyanNatureState.get(world);
        if (!state.active) return status(source);
        state.pause("Остановлено оператором");
        Work work = ACTIVE.remove(world);
        if (work != null) work.close();
        source.sendSuccess(() -> Component.literal(
            "Природный этап поставлен на паузу; изменения сохранены."), false);
        return 1;
    }

    public static int resume(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanNatureState state = BuyanNatureState.get(world);
        if (!state.active) return status(source);
        if (!state.compatible || !Double.isFinite(state.forestCutoff)
            || state.phase < 0 || state.phase > 1 || BuyanState.get(world).active) {
            source.sendFailure(Component.literal("Несовместимое состояние Буяна; возобновление запрещено."));
            return 0;
        }
        state.resume();
        source.sendSuccess(() -> Component.literal("Природный этап Буяна возобновлён."), false);
        return 1;
    }

    private static boolean outside(BuyanNatureState state, ChunkPos chunk) {
        int x = Math.clamp(state.centerX, chunk.getMinBlockX(), chunk.getMaxBlockX());
        int z = Math.clamp(state.centerZ, chunk.getMinBlockZ(), chunk.getMaxBlockZ());
        long dx = (long) x - state.centerX;
        long dz = (long) z - state.centerZ;
        return dx * dx + dz * dz > (long) state.radius * state.radius;
    }

    /**
     * Preflight all positions before the first write. Re-sculpt the top of the
     * accepted foundation, never excavating its supporting stone down to sea.
     */
    private static boolean terrainColumn(ServerLevel world, BuyanNatureState state,
                                         int x, int z, BlockState[] original) {
        long seed = world.getSeed();
        int sea = world.getSeaLevel();
        int foundation = BuyanTerrain.top(seed, state.centerX, state.centerZ,
            state.radius, x, z, sea);
        if (foundation == Integer.MIN_VALUE) return true;
        int top = BuyanNature.top(seed, state.centerX, state.centerZ,
            state.radius, x, z, sea);
        if (top == Integer.MIN_VALUE) return true;
        top = Math.min(world.getMaxBuildHeight() - 15, top);
        BlockState support = world.getBlockState(new BlockPos(x, foundation, z));
        if (support.isAir() || !support.getFluidState().isEmpty()
            || !BuyanGenerator.canReplace(support)) return false;

        int minY = world.getMinBuildHeight();
        int start = Math.max(minY + 1, Math.min(top - 5, foundation - 5));
        int upper = Math.min(world.getMaxBuildHeight() - 1,
            Math.max(Math.max(top, foundation),
                world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = start; y <= upper; ++y) {
            pos.set(x, y, z);
            BlockState old = world.getBlockState(pos);
            if (!BuyanGenerator.canReplace(old)) return false;
            original[y - minY] = old;
        }
        BuyanTerrain.Surface kind = BuyanNature.surface(seed, state.centerX, state.centerZ,
            x, z, sea, top);
        double cover = BuyanTerrain.surfaceCover(seed, x, z);
        int rockRoll = BuyanTerrain.surfaceRockRoll(seed, x, z);
        for (int y = start; y <= upper; ++y) {
            pos.set(x, y, z);
            BlockState target = y <= top
                ? BuyanNature.block(seed, state.centerX, state.centerZ, state.radius,
                    x, y, z, top, sea, state.forestCutoff, kind, cover, rockRoll)
                : Blocks.AIR.defaultBlockState();
            if (!original[y - minY].equals(target)) world.setBlock(pos, target, 2);
        }
        return true;
    }

    private static boolean ground(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)
            || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.COARSE_DIRT);
    }

    private static BlockState leaves(BuyanNature.TreeKind kind) {
        return switch (kind) {
            case OAK -> Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            case BIRCH -> Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            case SPRUCE -> Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        };
    }

    private static BlockState log(BuyanNature.TreeKind kind) {
        return switch (kind) {
            case OAK -> Blocks.OAK_LOG.defaultBlockState();
            case BIRCH -> Blocks.BIRCH_LOG.defaultBlockState();
            case SPRUCE -> Blocks.SPRUCE_LOG.defaultBlockState();
        };
    }

    private static void crown(Map<BlockPos, BlockState> shape, long seed, int x, int z,
                              int y, int radius, BlockState leaf) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + (radius == 3 ? 1 : 0)) continue;
                if (radius > 1 && Math.abs(dx) == radius && Math.abs(dz) == radius
                    && BuyanTerrain.value(seed ^ 0xB22A0801L, x + dx, z + dz, y) < 0.60) continue;
                shape.put(new BlockPos(x + dx, y, z + dz), leaf);
            }
        }
    }

    private static Map<BlockPos, BlockState> treeShape(long seed, int x, int y, int z,
                                                       int height, BuyanNature.TreeKind kind) {
        Map<BlockPos, BlockState> shape = new LinkedHashMap<>();
        BlockState leaf = leaves(kind);
        int top = y + height;
        if (kind == BuyanNature.TreeKind.SPRUCE) {
            for (int layer = 1; layer <= height - 1; layer++) {
                int crownY = top - layer;
                int radius = layer < 3 ? 1 : layer < 6 ? 2 : 3;
                if (layer % 3 == 0) radius = Math.max(1, radius - 1);
                crown(shape, seed, x, z, crownY, radius, leaf);
            }
            crown(shape, seed, x, z, top, 1, leaf);
            shape.put(new BlockPos(x, top + 1, z), leaf);
        } else {
            crown(shape, seed, x, z, top - 2, kind == BuyanNature.TreeKind.OAK ? 2 : 1, leaf);
            crown(shape, seed, x, z, top - 1, kind == BuyanNature.TreeKind.OAK ? 3 : 2, leaf);
            crown(shape, seed, x, z, top, kind == BuyanNature.TreeKind.OAK ? 3 : 2, leaf);
            crown(shape, seed, x, z, top + 1, 2, leaf);
            crown(shape, seed, x, z, top + 2, 1, leaf);
        }
        BlockState trunk = log(kind);
        for (int level = 1; level <= height; level++) {
            shape.put(new BlockPos(x, y + level, z), trunk);
        }
        return shape;
    }

    /** Tree coordinates keep their entire crown inside one chunk. */
    private static boolean treeCandidate(ServerLevel world, BuyanNatureState state,
                                         ChunkPos chunk, int x, int z,
                                         BuyanGenerator.Protection protection) {
        int lx = x - chunk.getMinBlockX();
        int lz = z - chunk.getMinBlockZ();
        int cornerX = lx >= 8 ? 11 : 3;
        int cornerZ = lz >= 8 ? 11 : 3;
        long seed = world.getSeed();
        int anchorX = cornerX + (int) (2.0 * BuyanTerrain.value(
            seed ^ 0xB22A0601L, chunk.x * 2 + lx / 8, chunk.z * 2 + lz / 8, 0));
        int anchorZ = cornerZ + (int) (2.0 * BuyanTerrain.value(
            seed ^ 0xB22A0602L, chunk.x * 2 + lx / 8, chunk.z * 2 + lz / 8, 0));
        if (lx != anchorX || lz != anchorZ) return false;
        if (BuyanTerrain.value(seed ^ 0xB22A0603L, x, z, 0) > 0.55) return false;

        int sea = world.getSeaLevel();
        if (!BuyanNature.forest(seed, state.centerX, state.centerZ, state.radius,
            x, z, sea, state.forestCutoff)
            || BuyanNature.shoreDistance(seed, state.centerX, state.centerZ, x, z) < 6.0) {
            return false;
        }
        int y = BuyanNature.top(seed, state.centerX, state.centerZ, state.radius,
            x, z, sea);
        if (y == Integer.MIN_VALUE || y + 16 >= world.getMaxBuildHeight()
            || !ground(world.getBlockState(new BlockPos(x, y, z)))) return false;
        BuyanNature.TreeKind kind = BuyanNature.treeKind(seed, x, z, y, sea);
        int slopeLimit = kind == BuyanNature.TreeKind.SPRUCE ? 3 : 2;
        for (int dx = -2; dx <= 2; dx += 2) {
            for (int dz = -2; dz <= 2; dz += 2) {
                int neighbor = BuyanNature.top(seed, state.centerX, state.centerZ,
                    state.radius, x + dx, z + dz, sea);
                if (neighbor == Integer.MIN_VALUE || Math.abs(neighbor - y) > slopeLimit) return false;
            }
        }

        int height = (kind == BuyanNature.TreeKind.SPRUCE ? 9 : 6)
            + (int) (4.0 * BuyanTerrain.value(seed ^ 0xB22A0604L, x, z, 0));
        Map<BlockPos, BlockState> shape = treeShape(seed, x, y, z, height, kind);
        for (BlockPos pos : shape.keySet()) {
            if (pos.getX() < chunk.getMinBlockX() || pos.getX() > chunk.getMaxBlockX()
                || pos.getZ() < chunk.getMinBlockZ() || pos.getZ() > chunk.getMaxBlockZ()
                || protection.occupies(pos.getX(), pos.getZ())) return false;
            BlockState previous = world.getBlockState(pos);
            if (!previous.getFluidState().isEmpty()
                || !(previous.isAir() || previous.is(BlockTags.REPLACEABLE)
                    || previous.is(BlockTags.FLOWERS))) return false;
        }
        for (var entry : shape.entrySet()) {
            world.setBlock(entry.getKey(), entry.getValue(), 2);
        }
        state.tree();
        return true;
    }

    private static void undergrowth(ServerLevel world, BuyanNatureState state, int x, int z) {
        long seed = world.getSeed();
        int sea = world.getSeaLevel();
        int y = BuyanNature.top(seed, state.centerX, state.centerZ,
            state.radius, x, z, sea);
        if (y == Integer.MIN_VALUE) return;
        BlockPos groundPos = new BlockPos(x, y, z);
        BlockState soil = world.getBlockState(groundPos);
        BlockPos pos = groundPos.above();
        if (!world.getBlockState(pos).isAir()) return;
        double passage = BuyanNature.passage(seed, state.centerX, state.centerZ, x, z);
        double clearing = BuyanNature.clearingMetric(seed, state.centerX, state.centerZ, x, z);
        double roll = BuyanTerrain.value(seed ^ 0xB22A0701L, x, z, 0);
        if (!ground(soil)) {
            // Sparse natural stone outcrops on existing rocky ground, not roads.
            if (y >= sea + 18 && passage < 0.10 && clearing > 1.25
                && (soil.is(Blocks.STONE) || soil.is(Blocks.TUFF) || soil.is(Blocks.ANDESITE))
                && roll < 0.012) {
                world.setBlock(pos, (roll < 0.006 ? Blocks.ANDESITE
                    : Blocks.DIORITE).defaultBlockState(), 2);
            }
            return;
        }
        boolean wooded = BuyanNature.forest(seed, state.centerX, state.centerZ,
            state.radius, x, z, sea, state.forestCutoff);
        double humidity = BuyanTerrain.noise(seed ^ 0xB22A0702L, x, z, 33.0);
        if (!wooded && clearing > 0.92 && clearing < 1.38
            && passage < 0.10 && roll < 0.003) {
            world.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            return;
        }

        if (passage > 0.30 && roll < 0.94) return;
        if (clearing < 0.65 && roll < 0.965) return;
        if (wooded && roll < 0.008 && humidity > 0.20) {
            world.setBlock(pos, (roll < 0.004 ? Blocks.BROWN_MUSHROOM
                : Blocks.RED_MUSHROOM).defaultBlockState(), 2);
        } else if (wooded && roll < 0.11) {
            world.setBlock(pos, Blocks.FERN.defaultBlockState(), 2);
        } else if (wooded && roll < 0.15 && world.getBlockState(pos.above()).isAir()) {
            world.setBlock(pos, Blocks.LARGE_FERN.defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 2);
            world.setBlock(pos.above(), Blocks.LARGE_FERN.defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 2);
        } else if (roll >= (wooded ? 0.84 : 0.79) && roll < 0.96) {
            world.setBlock(pos, Blocks.SHORT_GRASS.defaultBlockState(), 2);
        } else if (roll >= 0.96 && roll < 0.98 && world.getBlockState(pos.above()).isAir()
                   && passage < 0.30 && clearing > 0.75) {
            world.setBlock(pos, Blocks.TALL_GRASS.defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 2);
            world.setBlock(pos.above(), Blocks.TALL_GRASS.defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 2);
        } else if (roll >= 0.98) {
            BlockState flower = humidity > 0.30 ? Blocks.BLUE_ORCHID.defaultBlockState()
                : humidity > 0.0 ? Blocks.CORNFLOWER.defaultBlockState()
                : humidity < -0.38 ? Blocks.POPPY.defaultBlockState()
                : Blocks.DANDELION.defaultBlockState();
            world.setBlock(pos, flower, 2);
        }
    }

    private static final class Work implements AutoCloseable {
        private final ServerLevel world;
        private final BlockState[] original;
        private SiteChunks ticket;
        private ChunkPos inspectedChunk;
        private BuyanGenerator.Protection protection;

        private Work(ServerLevel world) {
            this.world = world;
            this.original = new BlockState[world.getMaxBuildHeight() - world.getMinBuildHeight()];
            this.ticket = new SiteChunks(world);
        }

        private void releaseChunk() {
            ticket.close();
            ticket = new SiteChunks(world);
            inspectedChunk = null;
        }

        private void step() {
            BuyanNatureState state = BuyanNatureState.get(world);
            long tickNanos = world.getServer().getAverageTickTimeNanos();
            boolean strained = tickNanos >= 35_000_000L;
            boolean healthy = tickNanos < 20_000_000L;
            if (strained && (world.getGameTime() & 3L) != 0L) return;
            long end = System.nanoTime() + (strained ? 750_000L
                : healthy ? 3_000_000L : 1_500_000L);
            int limit = strained ? 2 : healthy ? 24 : 10;
            int processed = 0;
            while (processed < limit && System.nanoTime() < end) {
                int count = BuyanGenerator.totalChunks(
                    state.centerX, state.centerZ, state.radius);
                if (state.chunkIndex >= count) {
                    if (state.phase == 0) {
                        state.nextPhase();
                        releaseChunk();
                        return;
                    }
                    state.finish();
                    LogUtils.getLogger().info(
                        "Buyan nature finished at {},{} with {} trees, {} skipped columns",
                        state.centerX, state.centerZ, state.plantedTrees, state.skippedColumns);
                    close();
                    ACTIVE.remove(world);
                    return;
                }
                ChunkPos chunk = BuyanGenerator.chunk(
                    state.centerX, state.centerZ, state.chunkIndex);
                if (outside(state, chunk)) {
                    state.progress(state.chunkIndex + 1, 0);
                    continue;
                }
                if (!ticket.ready(chunk)) return;
                if (!chunk.equals(inspectedChunk)) {
                    inspectedChunk = chunk;
                    protection = BuyanGenerator.protectedStructure(world, chunk);
                }
                if (protection.unknown()) {
                    state.skippedChunk();
                    state.progress(state.chunkIndex + 1, 0);
                    releaseChunk();
                    continue;
                }
                int column = state.columnIndex;
                int x = chunk.getMinBlockX() + (column & 15);
                int z = chunk.getMinBlockZ() + (column >>> 4);
                if (protection.occupies(x, z)) {
                    state.skippedColumn();
                } else if (state.phase == 0) {
                    if (!terrainColumn(world, state, x, z, original)) state.skippedColumn();
                } else if (BuyanNature.shoreDistance(world.getSeed(),
                        state.centerX, state.centerZ, x, z) >= 0.0) {
                    treeCandidate(world, state, chunk, x, z, protection);
                    undergrowth(world, state, x, z);
                }
                processed++;
                if (column == 255) {
                    state.progress(state.chunkIndex + 1, 0);
                    releaseChunk();
                } else {
                    state.progress(state.chunkIndex, column + 1);
                }
            }
        }

        @Override public void close() {
            ticket.close();
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        ServerLevel world = event.getServer().overworld();
        BuyanNatureState state = BuyanNatureState.get(world);
        if (!state.active || state.paused) return;
        Work work = ACTIVE.computeIfAbsent(world, Work::new);
        try {
            work.step();
        } catch (RuntimeException ex) {
            state.pause(ex.getMessage() == null ? ex.getClass().getSimpleName()
                : ex.getMessage());
            work.close();
            ACTIVE.remove(world);
            LogUtils.getLogger().error(
                "Buyan nature paused in pass {} chunk {} column {}",
                state.phase, state.chunkIndex, state.columnIndex, ex);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel world) {
            Work work = ACTIVE.remove(world);
            if (work != null) work.close();
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent event) {
        for (Work work : ACTIVE.values()) work.close();
        ACTIVE.clear();
    }
}
