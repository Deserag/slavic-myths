package org.slavicmyths.posad;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;
import org.slavicmyths.rpg.tools.ToolLogOrigins;
import org.slavicmyths.worldgen.ManualStructureRecords;

/**
 * Explicit construction for disposable QA worlds only. A fail-closed, server-thread
 * job: all affected chunks must already be loaded; no tickets or chunk generation.
 * Scans before writing, permanently reserves before the first block, and never
 * retries a partially written city. Does NOT retrofit or upgrade existing cities.
 */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadManualJobs {
    private static final Map<ServerLevel, Job> JOBS = new HashMap<>();
    private static final String QA_WORLD_PREFIX = "posad-qa-";

    public static int create(CommandSourceStack source, int turns, int ax, int deckY, int az) {
        ServerLevel world = source.getLevel();
        if (!source.hasPermission(4) || !world.dimension().equals(Level.NETHER)) {
            source.sendFailure(Component.literal("Пекельный посад: требуется OP 4 и Незер. Блоки не изменены."));
            return 0;
        }
        String worldName = world.getServer().getWorldData().getLevelName();
        if (!worldName.startsWith(QA_WORLD_PREFIX)) {
            source.sendFailure(Component.literal("ОТКАЗ: только отдельный тестовый мир с именем posad-qa-*. Обычные миры не изменены."));
            return 0;
        }
        if (JOBS.containsKey(world)) {
            source.sendFailure(Component.literal("В этом измерении уже выполняется создание Пекельного посада."));
            return 0;
        }
        try {
            Math.addExact(ax, 112);
            Math.addExact(az, 112);
            if (Math.abs((long)ax) > 29_998_000L || Math.abs((long)az) > 29_998_000L) throw new IllegalArgumentException("Координаты слишком близко к пределам мира.");
            if (deckY - 48 < world.getMinBuildHeight() || deckY + 17 >= world.getMaxBuildHeight()) throw new IllegalArgumentException("Высота вне допустимых границ.");
            Job job = new Job(source, turns, ax, deckY, az);
            JOBS.put(world, job);
            source.sendSuccess(() -> Component.literal("Запущена проверка места. Постройка начнётся ТОЛЬКО после полного preflight. Нужные чанки должны быть уже загружены; статус: /slavicmyths dev posad status."), false);
            return 1;
        } catch (RuntimeException ex) {
            source.sendFailure(Component.literal("Пекельный посад: проверка аргументов отклонена: " + ex.getMessage()));
            return 0;
        }
    }

    public static int status(CommandSourceStack source, boolean cancel) {
        Job job = JOBS.get(source.getLevel());
        if (job == null) {
            source.sendSuccess(() -> Component.literal("Нет активного задания Пекельного посада."), false);
            return 0;
        }
        if (cancel) {
            boolean partial = job.reserved;
            JOBS.remove(job.world);
            source.sendSuccess(() -> Component.literal(partial ? "ОСТАНОВЛЕНО: область навсегда зарезервирована; возможна незавершённая постройка, автоматического повтора и выдачи наград нет." : "Отменено до записи блоков. Область не зарезервирована."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Пекельный посад: " + job.phase + ", чанки " + job.chunk + "/" + job.chunks.size() + ", резерв " + job.reserved), false);
        return 1;
    }

    private static final class Job {
        final CommandSourceStack source;
        final ServerLevel world;
        final int turns, ax, deckY, az;
        final UUID token = UUID.randomUUID();
        final ToolLogOrigins protectedOrigins;
        final List<ChunkPos> chunks = new ArrayList<>();
        PosadPiece piece;
        BoundingBox bounds;
        int phase, chunk, layer, cell;
        boolean reserved;

        Job(CommandSourceStack source, int turns, int ax, int deckY, int az) {
            this.source = source;
            this.world = source.getLevel();
            this.turns = turns;
            this.ax = ax;
            this.deckY = deckY;
            this.az = az;
            this.protectedOrigins = ToolLogOrigins.get(world);
        }

        private PosadTerrain.Material material(int x, int y, int z) {
            if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) return PosadTerrain.Material.PROTECTED;
            if (world.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) return PosadTerrain.Material.PROTECTED;
            var state = world.getBlockState(new BlockPos(x, y, z));
            if (state.isAir()) return PosadTerrain.Material.AIR;
            if (state.is(Blocks.LAVA)) return PosadTerrain.Material.LAVA;
            return NetherBridgeTerrain.natural(state) ? PosadTerrain.Material.NATURAL : PosadTerrain.Material.PROTECTED;
        }

        private void plan() {
            // The native terrain planner is reused verbatim; a different shore
            // orientation is NOT silently substituted for the owner's test request.
            PosadTerrain.Decision check = PosadTerrain.inspect(this::material, ax, deckY, az, turns);
            if (!check.accepted()) throw new IllegalStateException("terrain: " + check.reason() + "; заранее загрузите чанки города и берега");
            if (check.plan().turns() != turns) throw new IllegalStateException("Подход к берегу недоступен для запрошенного поворота " + turns);
            piece = new PosadPiece(check.plan());
            bounds = piece.getBoundingBox();
            if (!world.getWorldBorder().isWithinBounds(new BlockPos(bounds.minX(), deckY, bounds.minZ())) || !world.getWorldBorder().isWithinBounds(new BlockPos(bounds.maxX(), deckY, bounds.maxZ()))) throw new IllegalStateException("Город или мост выходит за границу мира");
            if (!ManualStructureRecords.get(world).overlapping(bounds).isEmpty()) throw new IllegalStateException("Есть ранее созданная или зарезервированная структура");
            for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
                for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++) {
                    if (world.getChunkSource().getChunkNow(cx, cz) == null) throw new IllegalStateException("Не загружен чанк " + cx + "," + cz + " (загрузка автоматически запрещена)");
                    chunks.add(new ChunkPos(cx, cz));
                }
            }
            layer = bounds.minY();
            phase = 1;
        }

        private void preflight() {
            int examined = 0;
            while (chunk < chunks.size() && examined++ < 8192) {
                ChunkPos cp = chunks.get(chunk);
                if (world.getChunkSource().getChunkNow(cp.x, cp.z) == null) throw new IllegalStateException("Чанк выгружен во время проверки: " + cp);
                int x = cp.getMinBlockX() + (cell & 15), z = cp.getMinBlockZ() + (cell >> 4);
                BlockPos at = new BlockPos(x, layer, z);
                if (bounds.isInside(at)) {
                    int dx = x - ax, dz = z - az;
                    int lx = switch (turns) { case 0 -> dx; case 1 -> dz; case 2 -> 112 - dx; default -> 112 - dz; };
                    int lz = switch (turns) { case 0 -> dz; case 1 -> 112 - dx; case 2 -> 112 - dz; default -> dx; };
                    var intended = piece.voxel(lx, layer - deckY, lz);
                    if (intended != null) {
                        var old = world.getBlockState(at);
                        if (old.hasBlockEntity() || world.getBlockEntity(at) != null || old.is(Blocks.BEDROCK) || protectedOrigins.blocked(at) || !(old.isAir() || old.is(Blocks.LAVA) || NetherBridgeTerrain.natural(old))) throw new IllegalStateException("Защищённый/чужой блок: " + at.toShortString() + " " + old);
                    }
                }
                if (++cell == 256) {
                    cell = 0;
                    if (++layer > bounds.maxY()) {
                        layer = bounds.minY();
                        chunk++;
                    }
                }
            }
            if (chunk != chunks.size()) return;
            if (!ManualStructureRecords.get(world).overlapping(bounds).isEmpty()) throw new IllegalStateException("Место занято другим заданием");
            // A durable reservation is written before touching any block. Never
            // remove it on cancellation/error; a partial job must not be replayed.
            ManualStructureRecords.get(world).add(token, "construction", 0, new BlockPos(ax, deckY, az), bounds);
            world.getDataStorage().save();
            reserved = true;
            phase = 2;
            chunk = 0;
            source.sendSuccess(() -> Component.literal("Проверка всей территории завершена. Область зарезервирована; начинается поэтапное строительство."), false);
        }

        private void place() {
            if (chunk >= chunks.size()) {
                // Persist modified chunks while the durable reservation still says construction.
                // If a save fails, NEVER publish COMPLETE or permit a retry.
                if (!world.getServer().saveEverything(false, true, true)) throw new IllegalStateException("Не удалось надёжно сохранить построенные чанки");
                ManualStructureRecords.get(world).replace(token, "nether_posad", 0, new BlockPos(ax, deckY, az), bounds);
                world.getDataStorage().save();
                source.sendSuccess(() -> Component.literal("Пекельный посад построен в QA-мире: X=" + ax + " Y=" + deckY + " Z=" + az + ", поворот " + turns * 90 + "°. Награды — только одноразовые сундуки."), false);
                JOBS.remove(world);
                return;
            }
            ChunkPos cp = chunks.get(chunk);
            if (world.getChunkSource().getChunkNow(cp.x, cp.z) == null) throw new IllegalStateException("Чанк выгружен во время строительства: " + cp);
            BoundingBox clip = new BoundingBox(cp.getMinBlockX(), bounds.minY(), cp.getMinBlockZ(), cp.getMaxBlockX(), bounds.maxY(), cp.getMaxBlockZ());
            piece.postProcess(world, world.structureManager(), world.getChunkSource().getGenerator(), world.random, clip, cp, new BlockPos(ax, deckY, az));
            world.getChunkSource().getChunkNow(cp.x, cp.z).setUnsaved(true);
            chunk++;
        }

        void step() {
            try {
                if (phase == 0) plan();
                else if (phase == 1) preflight();
                else place();
            } catch (RuntimeException ex) {
                LogUtils.getLogger().error("Posad manual QA job failed (reserved={}, phase={}, chunk={})", reserved, phase, chunk, ex);
                source.sendFailure(Component.literal("Создание Пекельного посада остановлено: " + ex.getMessage() + (reserved ? "; резерв сохранён, повтор запрещён" : "; блоки не записывались")));
                JOBS.remove(world);
            }
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (Job job : List.copyOf(JOBS.values())) if (job.world.getServer() == event.getServer()) job.step();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel world) JOBS.remove(world);
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent event) {
        JOBS.keySet().removeIf(world -> world.getServer() == event.getServer());
    }

    private PosadManualJobs() {}
}
