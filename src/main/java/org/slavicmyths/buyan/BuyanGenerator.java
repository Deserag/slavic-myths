package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.worldgen.SiteChunks;

/**
 * Manual operator-only terrain construction. Large footprints are generated
 * incrementally, one loaded chunk at a time, without a permanent chunk ticket.
 *
 * This is not natural worldgen: the separate ocean structure-set is future work.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class BuyanGenerator {
    private static final Map<ServerLevel, Work> ACTIVE = new HashMap<>();

    private BuyanGenerator() {}

    public static int generate(CommandSourceStack source, int radius) {
        ServerLevel world = source.getLevel();
        if (world.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Буян можно создавать только в обычном мире."));
            return 0;
        }
        int sea = world.getSeaLevel();
        int cx = (int) Math.floor(source.getPosition().x);
        int cz = (int) Math.floor(source.getPosition().z);
        if (!world.getBiome(new BlockPos(cx, sea, cz)).is(BiomeTags.IS_OCEAN)) {
            source.sendFailure(Component.literal("Центр Буяна должен находиться в ванильном океанском биоме."));
            return 0;
        }
        int extent = radius + BuyanTerrain.margin(radius);
        if (!world.getWorldBorder().isWithinBounds(new BlockPos(cx - extent, sea, cz - extent))
            || !world.getWorldBorder().isWithinBounds(new BlockPos(cx + extent, sea, cz + extent))) {
            source.sendFailure(Component.literal("Остров выходит за границу мира."));
            return 0;
        }
        BuyanState state = BuyanState.get(world);
        if (state.active) {
            source.sendFailure(Component.literal("Буян уже зарезервирован: /slavicmyths dev buyan status."));
            return 0;
        }
        if (state.overlaps(cx, cz, radius)) {
            source.sendFailure(Component.literal("В этой акватории уже есть завершённое основание Буяна."));
            return 0;
        }
        state.begin(cx, cz, radius);
        source.sendSuccess(() -> Component.literal(
            "Буян: начата поэтапная генерация центра X=" + cx + " Z=" + cz
            + ", радиус=" + radius + ". Естественные блоки подстраиваются под остров."
            + " Прогресс: /slavicmyths dev buyan status."), true);
        return 1;
    }

    public static int status(CommandSourceStack source) {
        BuyanState state = BuyanState.get(source.getLevel());
        if (!state.active) {
            source.sendSuccess(() -> Component.literal("Буян: активной генерации нет."), false);
            return 1;
        }
        int count = totalChunks(state);
        int progress = (int) Math.min(100L, 100L * state.chunkIndex / Math.max(1, count));
        source.sendSuccess(() -> Component.literal(
            "Буян: центр " + state.centerX + "," + state.centerZ + "; радиус "
            + state.radius + "; чанки " + state.chunkIndex + "/" + count
            + "; " + progress + "%; " + (state.paused ? "ПАУЗА" : "СТРОИТСЯ")
            + (state.lastError.isEmpty() ? "" : "; причина: " + state.lastError)), false);
        return 1;
    }

    public static int pause(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState state = BuyanState.get(world);
        if (!state.active) return status(source);
        state.pause("Остановлено оператором; уже построенные блоки сохраняются.");
        Work work = ACTIVE.remove(world);
        if (work != null) work.close();
        source.sendSuccess(() -> Component.literal(
            "Буян поставлен на паузу. Построенная часть сохранена; /slavicmyths dev buyan resume продолжит."), false);
        return 1;
    }

    public static int resume(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState state = BuyanState.get(world);
        if (!state.active) return status(source);
        state.resume();
        source.sendSuccess(() -> Component.literal("Буян: генерация продолжена с сохранённого чанка."), false);
        return 1;
    }

    private static int minChunk(BuyanState state) {
        return (state.centerX - state.radius - BuyanTerrain.margin(state.radius)) >> 4;
    }

    private static int minChunkZ(BuyanState state) {
        return (state.centerZ - state.radius - BuyanTerrain.margin(state.radius)) >> 4;
    }

    private static int width(BuyanState state) {
        int extent = state.radius + BuyanTerrain.margin(state.radius);
        return ((state.centerX + extent) >> 4) - minChunk(state) + 1;
    }

    private static int height(BuyanState state) {
        int extent = state.radius + BuyanTerrain.margin(state.radius);
        return ((state.centerZ + extent) >> 4) - minChunkZ(state) + 1;
    }

    private static int totalChunks(BuyanState state) {
        return width(state) * height(state);
    }

    private static ChunkPos chunk(BuyanState state) {
        int span = width(state);
        return new ChunkPos(minChunk(state) + state.chunkIndex % span,
            minChunkZ(state) + state.chunkIndex / span);
    }

    private static boolean withinFootprint(BuyanState state, int x, int z) {
        long dx = (long) x - state.centerX;
        long dz = (long) z - state.centerZ;
        long radius = (long) state.radius + BuyanTerrain.margin(state.radius);
        return dx * dx + dz * dz <= radius * radius;
    }

    private static boolean outsideFootprint(BuyanState state, ChunkPos chunk) {
        int x = Math.clamp(state.centerX, chunk.getMinBlockX(), chunk.getMaxBlockX());
        int z = Math.clamp(state.centerZ, chunk.getMinBlockZ(), chunk.getMaxBlockZ());
        return !withinFootprint(state, x, z);
    }

    /**
     * Preserve block entities, portals, bedrock and non-natural construction.
     * Naturally generated stone, ores, sand, logs, vegetation and water may be
     * replaced deliberately; we cannot reliably detect a player-placed log.
     */
    private static boolean canReplace(BlockState old) {
        if (old.hasBlockEntity()
            || old.is(Blocks.BEDROCK) || old.is(Blocks.NETHER_PORTAL)
            || old.is(Blocks.END_PORTAL) || old.is(Blocks.END_PORTAL_FRAME)) return false;
        return old.isAir() || old.getFluidState().is(FluidTags.WATER)
            || old.is(BlockTags.BASE_STONE_OVERWORLD)
            || old.is(BlockTags.DIRT) || old.is(BlockTags.SAND)
            || old.is(BlockTags.LOGS) || old.is(BlockTags.LEAVES)
            || old.is(BlockTags.REPLACEABLE) || old.is(BlockTags.FLOWERS)
            || old.is(BlockTags.CORALS) || old.is(BlockTags.CORAL_BLOCKS)
            || old.is(Blocks.SEA_PICKLE) || old.is(Blocks.MAGMA_BLOCK)
            || old.is(Blocks.SANDSTONE) || old.is(Blocks.RED_SANDSTONE)
            || old.is(Blocks.CALCITE) || old.is(Blocks.MOSS_BLOCK)
            || old.is(Blocks.GRAVEL) || old.is(Blocks.CLAY)
            || old.is(Blocks.TUFF) || old.is(Blocks.DEEPSLATE)
            || old.is(Blocks.ICE) || old.is(Blocks.PACKED_ICE)
            || old.is(Blocks.BLUE_ICE) || old.is(Blocks.SNOW_BLOCK)
            || old.is(Blocks.KELP) || old.is(Blocks.KELP_PLANT)
            || old.is(Blocks.SEAGRASS) || old.is(Blocks.TALL_SEAGRASS)
            || old.is(Blocks.ANDESITE) || old.is(Blocks.DIORITE)
            || old.is(Blocks.GRANITE) || old.is(Blocks.COAL_ORE)
            || old.is(Blocks.IRON_ORE) || old.is(Blocks.COPPER_ORE)
            || old.is(Blocks.GOLD_ORE) || old.is(Blocks.LAPIS_ORE)
            || old.is(Blocks.REDSTONE_ORE) || old.is(Blocks.DIAMOND_ORE);
    }

    private static void setNatural(ServerLevel world, BlockPos pos, BlockState target) {
        BlockState old = world.getBlockState(pos);
        if (old.equals(target)) return;
        if (!canReplace(old)) {
            throw new IllegalStateException("Защищённый блок " + pos.toShortString() + " / " + old.getBlock());
        }
        world.setBlock(pos, target, 2);
    }

    private static void column(ServerLevel world, BuyanState state, int x, int z) {
        if (!withinFootprint(state, x, z)) return;
        int sea = world.getSeaLevel();
        int top = BuyanTerrain.top(world.getSeed(), state.centerX, state.centerZ,
            state.radius, x, z, sea);
        if (top == Integer.MIN_VALUE) return;

        // Existing ocean floor remains the base of the newly raised geological mass.
        int oceanFloor = world.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1;
        int surface = Math.min(world.getMaxBuildHeight() - 8, top);
        // Natural pre-existing ground is adapted as well as empty ocean water.
        int first = Math.max(world.getMinBuildHeight() + 1,
            Math.min(oceanFloor + 1, surface - 3));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        // Preflight the entire column before writing any blocks to this column.
        for (int y = first; y <= surface; y++) {
            cursor.set(x, y, z);
            if (!canReplace(world.getBlockState(cursor))) {
                throw new IllegalStateException("Нельзя заменить защищённый блок "
                    + cursor.toShortString());
            }
        }
        // A natural island may absorb grass, coastal trees and dirt overhangs.
        // It must not erase player structures or existing block entities.
        int above = Math.min(world.getMaxBuildHeight() - 1,
            world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1);
        if (above > surface) {
            for (int y = surface + 1; y <= above; y++) {
                cursor.set(x, y, z);
                if (!canReplace(world.getBlockState(cursor))) {
                    throw new IllegalStateException("Препятствие над островом: "
                        + cursor.toShortString());
                }
            }
        }

        for (int y = first; y <= surface; y++) {
            cursor.set(x, y, z);
            setNatural(world, cursor, BuyanTerrain.block(world.getSeed(), x, y, z, surface, sea));
        }
        if (above > surface) {
            for (int y = surface + 1; y <= above; y++) {
                cursor.set(x, y, z);
                setNatural(world, cursor, y <= sea
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static final class Work implements AutoCloseable {
        private final ServerLevel level;
        private SiteChunks ticket;

        private Work(ServerLevel level) {
            this.level = level;
            this.ticket = new SiteChunks(level);
        }

        private void step() {
            BuyanState state = BuyanState.get(level);
            if (!state.active || state.paused) return;
            long end = System.nanoTime() + 2_000_000L;
            int columns = 0;
            while (System.nanoTime() < end && columns < 20) {
                if (state.chunkIndex >= totalChunks(state)) {
                    state.finish();
                    LogUtils.getLogger().info("Buyan island foundation generated at {},{} radius {}",
                        state.centerX, state.centerZ, state.radius);
                    close();
                    ACTIVE.remove(level);
                    return;
                }
                ChunkPos pos = chunk(state);
                if (outsideFootprint(state, pos)) {
                    state.progress(state.chunkIndex + 1, 0);
                    continue;
                }
                if (!ticket.ready(pos)) return;
                int local = state.columnIndex;
                int x = pos.getMinBlockX() + (local & 15);
                int z = pos.getMinBlockZ() + (local >>> 4);
                column(level, state, x, z);
                ++columns;
                if (local == 255) {
                    state.progress(state.chunkIndex + 1, 0);
                    ticket.close();
                    ticket = new SiteChunks(level);
                } else {
                    state.progress(state.chunkIndex, local + 1);
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
        BuyanState state = BuyanState.get(world);
        if (!state.active || state.paused) return;
        Work work = ACTIVE.computeIfAbsent(world, Work::new);
        try {
            work.step();
        } catch (RuntimeException ex) {
            state.pause(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            work.close();
            ACTIVE.remove(world);
            LogUtils.getLogger().error("Buyan island generation paused at chunk {} column {}",
                state.chunkIndex, state.columnIndex, ex);
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
