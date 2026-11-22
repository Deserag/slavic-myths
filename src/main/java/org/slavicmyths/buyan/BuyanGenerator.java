package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static int generate(CommandSourceStack source) {
        // This is the maximum outline bound, not a fixed circular coastline.
        final int radius = BuyanTerrain.MAX_RADIUS;
        ServerLevel world = source.getLevel();
        if (world.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Буян можно создавать только в обычном мире."));
            return 0;
        }
        int sea = world.getSeaLevel();
        int cx = (int) Math.floor(source.getPosition().x);
        int cz = (int) Math.floor(source.getPosition().z);
        BuyanState state = BuyanState.get(world);
        if (state.active) {
            source.sendFailure(Component.literal(
                "Буян уже строится. Прогресс: /slavicmyths dev buyan status."));
            return 0;
        }

        // The same command also resumes the workflow when stage one was
        // completed in an earlier session/version. No duplicate foundation.
        BuyanState.Site completed = BuyanNatureGenerator.selectedFoundation(world, cx, cz);
        if (completed != null) {
            BuyanNatureState nature = BuyanNatureState.get(world);
            if (nature.active) {
                source.sendFailure(Component.literal(
                    "Природный этап уже идёт. Прогресс: /slavicmyths dev buyan status."));
                return 0;
            }
            if (nature.finishedAt(completed)) {
                if (!BuyanCityPlan.placeOnce(world, completed)) {
                    source.sendFailure(Component.literal("Город Буяна не достроен: защищённые блоки или неподходящий участок. Рельеф сохранён."));
                    return 0;
                }
                String result = BuyanDevelopment.develop(world, completed);
                source.sendSuccess(() -> Component.literal("Буян: проверка и достройка города — " + result), false);
                return result.contains("false") || result.contains("не готовы") ? 0 : 1;
            }
            try {
                if (!BuyanNatureGenerator.startCompleted(world, completed)) {
                    source.sendFailure(Component.literal("Невозможно начать природный этап Буяна."));
                    return 0;
                }
            } catch (IllegalStateException ex) {
                source.sendFailure(Component.literal(
                    "Не удалось начать природный этап: " + ex.getMessage()));
                return 0;
            }
            source.sendSuccess(() -> Component.literal(
                "Основание Буяна готово. Природный этап запущен автоматически."
                + " Прогресс: /slavicmyths dev buyan status."), true);
            return 1;
        }

        if (BuyanNatureState.get(world).active) {
            source.sendFailure(Component.literal(
                "Природный этап другого Буяна ещё активен. Проверьте статус."));
            return 0;
        }
        if (!world.getBiome(new BlockPos(cx, sea, cz)).is(BiomeTags.IS_OCEAN)) {
            source.sendFailure(Component.literal("Центр Буяна должен находиться в ванильном океанском биоме."));
            return 0;
        }
        if (!BuyanOceanSite.suitable(world,cx,cz)) {
            source.sendFailure(Component.literal("Для Буяна нужен открытый океан по всей площади острова, вдали от суши и мелководья."));
            return 0;
        }
        // Read the actual water surface from the live ocean column.
        // Flat custom worlds can have a different sea level from their generator.
        int waterSurface = world.getHeight(Heightmap.Types.WORLD_SURFACE, cx, cz) - 1;
        if (waterSurface < world.getMinBuildHeight()
            || !world.getFluidState(new BlockPos(cx, waterSurface, cz)).is(FluidTags.WATER)) {
            source.sendFailure(Component.literal(
                "Центр Буяна должен быть на поверхности воды, а не на суше."));
            return 0;
        }
        int waterLevel = Math.max(sea, waterSurface);
        if (waterLevel + 2 >= world.getMaxBuildHeight() - 15) {
            source.sendFailure(Component.literal(
                "Поверхность воды слишком высока для безопасного создания острова."));
            return 0;
        }
        int extent = radius + BuyanTerrain.margin(radius);
        if (!world.getWorldBorder().isWithinBounds(new BlockPos(cx - extent, sea, cz - extent))
            || !world.getWorldBorder().isWithinBounds(new BlockPos(cx + extent, sea, cz + extent))) {
            source.sendFailure(Component.literal("Остров выходит за границу мира."));
            return 0;
        }
        if (state.overlaps(cx, cz, radius)) {
            source.sendFailure(Component.literal("В этой акватории уже есть завершённое основание Буяна."));
            return 0;
        }
        state.begin(cx, cz, radius, waterLevel);
        source.sendSuccess(() -> Component.literal(
            "Буян: запущена ПОЛНАЯ генерация. Центр X=" + cx + " Z=" + cz
            + ", берег около 260–350 блоков, вода Y=" + waterLevel
            + ", самая низкая суша Y=" + (waterLevel + 2)
            + ". Очередь: 1/3 основание → 2/3 рельеф и грунты"
            + " → 3/3 леса и подлесок. Все этапы запускаются автоматически."
            + " Прогресс: /slavicmyths dev buyan status."), true);
        return 1;
    }

    public static int status(CommandSourceStack source) {
        BuyanState state = BuyanState.get(source.getLevel());
        if (!state.active) return BuyanNatureGenerator.status(source);
        int count = totalChunks(state);
        int progress = (int) Math.min(50L,
            50L * (state.chunkIndex * 256L + state.columnIndex)
            / Math.max(1L, count * 256L));
        source.sendSuccess(() -> Component.literal(
            "Буян: центр " + state.centerX + "," + state.centerZ + "; радиус "
            + " (берег 100–350), уровень воды Y=" + state.waterLevel
            + ", минимальная суша Y=" + (state.waterLevel + 2)
            + "; этап 1/3: ОСНОВАНИЕ; чанки " + state.chunkIndex + "/" + count
            + "; общий прогресс по чанкам ~" + progress + "%; защищённые чанки "
            + state.skippedChunks + ", пропущенные колонки " + state.skippedColumns
            + "; " + (state.paused ? "ПАУЗА" : "СТРОИТСЯ")
            + (state.lastError.isEmpty() ? "" : "; причина: " + state.lastError)), false);
        return 1;
    }

    public static int pause(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState state = BuyanState.get(world);
        if (!state.active) return BuyanNatureGenerator.pause(source);
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
        if (!state.active) return BuyanNatureGenerator.resume(source);
        if (state.traversalVersion != 4) {
            source.sendFailure(Component.literal(
                "Старое задание несовместимо с новым основанием Буяна. "
                + "Нужен новый тестовый мир; старая территория не должна перезаписываться."));
            return 0;
        }
        state.resume();
        source.sendSuccess(() -> Component.literal("Буян: генерация основания продолжена с сохранённого чанка."), false);
        return 1;
    }

    /** Square spiral; chunk zero contains the player's requested island center. */
    private static int rings(int centerX, int centerZ, int radius) {
        int extent = radius + BuyanTerrain.margin(radius);
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        return Math.max(
            Math.max(centerChunkX - ((centerX - extent) >> 4),
                     ((centerX + extent) >> 4) - centerChunkX),
            Math.max(centerChunkZ - ((centerZ - extent) >> 4),
                     ((centerZ + extent) >> 4) - centerChunkZ));
    }

    static int totalChunks(int centerX, int centerZ, int radius) {
        int side = rings(centerX, centerZ, radius) * 2 + 1;
        return side * side;
    }

    private static int totalChunks(BuyanState state) {
        return totalChunks(state.centerX, state.centerZ, state.radius);
    }

    static ChunkPos chunk(int centerX, int centerZ, int index) {
        int cx = centerX >> 4;
        int cz = centerZ >> 4;
        if (index == 0) return new ChunkPos(cx, cz);
        int ring = (int) Math.ceil((Math.sqrt(index + 1.0) - 1.0) / 2.0);
        int side = 2 * ring;
        int first = (2 * ring - 1) * (2 * ring - 1);
        int offset = index - first;
        if (offset < side) {
            return new ChunkPos(cx + ring, cz - ring + 1 + offset);
        }
        if (offset < 2 * side) {
            return new ChunkPos(cx + ring - 1 - (offset - side), cz + ring);
        }
        if (offset < 3 * side) {
            return new ChunkPos(cx - ring, cz + ring - 1 - (offset - 2 * side));
        }
        return new ChunkPos(cx - ring + 1 + (offset - 3 * side), cz - ring);
    }

    private static ChunkPos chunk(BuyanState state) {
        return chunk(state.centerX, state.centerZ, state.chunkIndex);
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
     * Protect only a structure's actual X/Z footprint where its bounding box is
     * known. A reference to an unloaded structure start stays conservatively
     * protected at chunk granularity, without forcing new chunks to load.
     */
    record Protection(boolean unknown, List<net.minecraft.world.level.levelgen.structure.BoundingBox> boxes) {
        boolean occupies(int x, int z) {
            for (var box : boxes) {
                if (x >= box.minX() - 2 && x <= box.maxX() + 2
                    && z >= box.minZ() - 2 && z <= box.maxZ() + 2) return true;
            }
            return false;
        }
    }

    static Protection protectedStructure(ServerLevel world, ChunkPos pos) {
        var loaded = world.getChunkSource().getChunkNow(pos.x, pos.z);
        if (loaded == null) return new Protection(true, List.of());
        List<net.minecraft.world.level.levelgen.structure.BoundingBox> boxes = new ArrayList<>();
        int x0 = pos.getMinBlockX();
        int z0 = pos.getMinBlockZ();
        int x1 = pos.getMaxBlockX();
        int z1 = pos.getMaxBlockZ();
        for (var start : loaded.getAllStarts().values()) {
            if (start.isValid() && intersects(start.getBoundingBox(), x0, z0, x1, z1)) {
                boxes.add(start.getBoundingBox());
            }
        }
        for (var entry : loaded.getAllReferences().entrySet()) {
            for (long ref : entry.getValue()) {
                ChunkPos origin = new ChunkPos(ref);
                var source = world.getChunkSource().getChunkNow(origin.x, origin.z);
                if (source == null) return new Protection(true, List.of());
                var structure = source.getStartForStructure(entry.getKey());
                if (structure != null && structure.isValid()
                    && intersects(structure.getBoundingBox(), x0, z0, x1, z1)) {
                    boxes.add(structure.getBoundingBox());
                }
            }
        }
        return new Protection(false, boxes);
    }

    private static boolean intersects(net.minecraft.world.level.levelgen.structure.BoundingBox box,
                                      int minX, int minZ, int maxX, int maxZ) {
        return box.maxX() >= minX && box.minX() <= maxX
            && box.maxZ() >= minZ && box.minZ() <= maxZ;
    }

    /**
     * Preserve block entities, portals, bedrock and non-natural construction.
     * Naturally generated stone, ores, sand, logs, vegetation and water may be
     * replaced deliberately. Player-placed natural blocks cannot be
     * distinguished from naturally generated blocks in existing saves.
     */
    static boolean canReplace(BlockState old) {
        if (old.hasBlockEntity()
            || old.is(Blocks.BEDROCK) || old.is(Blocks.NETHER_PORTAL)
            || old.is(Blocks.END_PORTAL) || old.is(Blocks.END_PORTAL_FRAME)) return false;
        return old.isAir() || old.getFluidState().is(FluidTags.WATER)
            || old.getFluidState().is(FluidTags.LAVA)
            || old.is(BlockTags.BASE_STONE_OVERWORLD)
            || old.is(BlockTags.DIRT) || old.is(BlockTags.SAND)
            || old.is(BlockTags.LOGS) || old.is(BlockTags.LEAVES)
            || old.is(BlockTags.REPLACEABLE) || old.is(BlockTags.FLOWERS)
            || old.is(BlockTags.CORALS) || old.is(BlockTags.CORAL_BLOCKS)
            || old.is(BlockTags.WALL_CORALS) || old.is(BlockTags.CORAL_PLANTS)
            || old.is(Blocks.DEAD_BRAIN_CORAL_BLOCK)
            || old.is(Blocks.DEAD_BUBBLE_CORAL_BLOCK)
            || old.is(Blocks.DEAD_FIRE_CORAL_BLOCK)
            || old.is(Blocks.DEAD_HORN_CORAL_BLOCK)
            || old.is(Blocks.DEAD_TUBE_CORAL_BLOCK)
            || old.is(Blocks.SEA_PICKLE) || old.is(Blocks.MAGMA_BLOCK)
            || old.is(Blocks.SANDSTONE) || old.is(Blocks.RED_SANDSTONE)
            || old.is(Blocks.CALCITE) || old.is(Blocks.MOSS_BLOCK)
            || old.is(Blocks.MOSS_CARPET) || old.is(Blocks.ROOTED_DIRT)
            || old.is(Blocks.MUD) || old.is(Blocks.MUDDY_MANGROVE_ROOTS)
            || old.is(Blocks.MANGROVE_ROOTS) || old.is(Blocks.DRIPSTONE_BLOCK)
            || old.is(Blocks.POINTED_DRIPSTONE) || old.is(Blocks.TERRACOTTA)
            || old.is(Blocks.OBSIDIAN) || old.is(Blocks.BASALT) 
            || old.is(Blocks.SMOOTH_BASALT)
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

    /**
     * Every original state was already validated in the column preflight.
     * Reuse that snapshot instead of reading/checking each position twice.
     * A Work owns the scratch array and runs exclusively on the server thread.
     */
    private static void writeChecked(ServerLevel world, BlockPos pos, BlockState expected,
                                     BlockState target) {
        if (!expected.equals(target)) world.setBlock(pos, target, 2);
    }

    private static boolean column(ServerLevel world, BuyanState state, int x, int z,
                                  BlockState[] original) {
        if (!withinFootprint(state, x, z)) return true;
        // Always use the fixed observed waterline from the start of this job.
        // It must not change when the island modifies world heightmaps.
        int sea = state.waterLevel;
        // Check the shoreline BEFORE querying the expensive ocean-floor heightmap.
        int top = BuyanTerrain.top(world.getSeed(), state.centerX, state.centerZ,
            state.radius, x, z, sea);
        if (top == Integer.MIN_VALUE) return true;
        int oceanFloor = world.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1;

        int surface = Math.min(world.getMaxBuildHeight() - 8, top);
        BuyanTerrain.Surface type = BuyanTerrain.surface(world.getSeed(),
            state.centerX, state.centerZ, x, z, sea, surface);
        double cover = BuyanTerrain.surfaceCover(world.getSeed(), x, z);
        int rock = BuyanTerrain.surfaceRockRoll(world.getSeed(), x, z);
        // Natural pre-existing ground is adapted as well as empty ocean water.
        int first = Math.max(world.getMinBuildHeight() + 1,
            Math.min(oceanFloor + 1, surface - 3));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        // Preflight the entire column before writing any blocks to this column.
        int minY = world.getMinBuildHeight();
        for (int y = first; y <= surface; y++) {
            cursor.set(x, y, z);
            BlockState old = world.getBlockState(cursor);
            if (!canReplace(old)) return false;
            original[y - minY] = old;
        }
        // A natural island may absorb grass, coastal trees and dirt overhangs.
        // It must not erase player structures or existing block entities.
        int above = Math.min(world.getMaxBuildHeight() - 1,
            world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1);
        if (above > surface) {
            for (int y = surface + 1; y <= above; y++) {
                cursor.set(x, y, z);
                BlockState old = world.getBlockState(cursor);
                if (!canReplace(old)) return false;
                original[y - minY] = old;
            }
        }

        for (int y = first; y <= surface; y++) {
            cursor.set(x, y, z);
            writeChecked(world, cursor, original[y - minY],
                BuyanTerrain.block(world.getSeed(), x, y, z,
                    surface, type, cover, rock));
        }
        if (above > surface) {
            for (int y = surface + 1; y <= above; y++) {
                cursor.set(x, y, z);
                writeChecked(world, cursor, original[y - minY], y <= sea
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
        }
        return true;
    }

    private static final class Work implements AutoCloseable {
        private int announcedDecile = -1;
        private final ServerLevel level;
        private SiteChunks ticket;
        private ChunkPos inspectedChunk;
        private Protection protection;
        private final BlockState[] original;

        private Work(ServerLevel level) {
            this.level = level;
            this.ticket = new SiteChunks(level);
            this.original = new BlockState[level.getMaxBuildHeight() - level.getMinBuildHeight()];
        }

        private void announceProgress(BuyanState state) {
            int count = totalChunks(state);
            long completedColumns = state.chunkIndex * 256L + state.columnIndex;
            int percent = (int) Math.min(50L,
                completedColumns * 50L / Math.max(1L, count * 256L));
            int decile = percent / 10;
            if (decile > announcedDecile) {
                announcedDecile = decile;
                if (decile > 0) {
                    level.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("[Буян] Основание: общий прогресс ~"
                            + percent + "% (обработанные чанки/колонки)."), false);
                }
            }
        }

        private void step() {
            BuyanState state = BuyanState.get(level);
            if (!state.active || state.paused) return;
            // Large island sculpting is expensive even when no single block is
            // expensive. When the server is overloaded, yield most ticks rather
            // than competing with player movement/rendering for resources.
            long tickNanos = level.getServer().getAverageTickTimeNanos();
            // Use spare server tick headroom but throttle immediately when TPS
            // deteriorates. Never attempt hundreds of columns in one game tick.
            boolean strained = tickNanos >= 35_000_000L;
            boolean healthy = tickNanos < 20_000_000L;
            if (strained && (level.getGameTime() & 3L) != 0L) return;
            long budgetNanos = strained ? 750_000L
                : healthy ? 3_000_000L : 1_500_000L;
            long end = System.nanoTime() + budgetNanos;
            int columns = 0;
            int limit = strained ? 2 : healthy ? 24 : 10;
            while (System.nanoTime() < end && columns < limit) {
                if (state.chunkIndex >= totalChunks(state)) {
                    BuyanState.Site completed = new BuyanState.Site(
                        state.centerX, state.centerZ, state.radius, state.waterLevel);
                    state.finish();
                    LogUtils.getLogger().info("Buyan island foundation generated at {},{} radius {}",
                        completed.x(), completed.z(), completed.radius());
                    close();
                    ACTIVE.remove(level);
                    try {
                        if (BuyanNatureGenerator.startCompleted(level, completed)) {
                            level.getServer().getPlayerList().broadcastSystemMessage(
                                Component.literal("[Буян] Основание готово! Автоматически начат природный рельеф (2/3)."),
                                false);
                        } else {
                            LogUtils.getLogger().warn(
                                "Buyan nature handoff not started at {},{}; retry with generate buyan",
                                completed.x(), completed.z());
                        }
                    } catch (RuntimeException ex) {
                        // Foundation is persisted as complete; an operator can retry
                        // the nature phase without regenerating or deleting it.
                        LogUtils.getLogger().error(
                            "Buyan foundation finished, but nature handoff failed at {},{}",
                            completed.x(), completed.z(), ex);
                    }
                    return;
                }
                ChunkPos pos = chunk(state);
                if (outsideFootprint(state, pos)) {
                    state.progress(state.chunkIndex + 1, 0);
                    announceProgress(state);
                    continue;
                }
                if (!ticket.ready(pos)) return;
                // A resumed job can start midway through a protected chunk, so
                // inspect each chunk once per Work, not only at local column zero.
                if (!pos.equals(inspectedChunk)) {
                    inspectedChunk = pos;
                    protection = protectedStructure(level, pos);
                }
                if (protection.unknown()) {
                    state.skipChunk();
                    state.progress(state.chunkIndex + 1, 0);
                    ticket.close();
                    ticket = new SiteChunks(level);
                    inspectedChunk = null;
                    continue;
                }
                int local = state.columnIndex;
                int x = pos.getMinBlockX() + (local & 15);
                int z = pos.getMinBlockZ() + (local >>> 4);
                if (protection.occupies(x, z) || !column(level, state, x, z, original)) {
                    state.skipColumn();
                }
                ++columns;
                if (local == 255) {
                    state.progress(state.chunkIndex + 1, 0);
                    ticket.close();
                    ticket = new SiteChunks(level);
                } else {
                    state.progress(state.chunkIndex, local + 1);
                }
                announceProgress(state);
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
