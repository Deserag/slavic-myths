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
 * Explicit OP construction in any world. A fail-closed server-thread job;
 * affected chunks are prepared with bounded native tickets, released on every exit.
 * Scans before writing, permanently reserves before the first block, and never
 * retries a partially written city. Does NOT retrofit or upgrade existing cities.
 */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadManualJobs {
    private static final Map<ServerLevel, Job> JOBS = new HashMap<>();

    public static int create(CommandSourceStack source, int turns, int ax, int deckY, int az) {
        ServerLevel world = source.getLevel();
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.literal("Пекельный посад: требуется право команд OP 2."));
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
            if (deckY - 2 < world.getMinBuildHeight() || deckY + 17 >= world.getMaxBuildHeight()) throw new IllegalArgumentException("Высота вне допустимых границ.");
            Job job = new Job(source, turns, ax, deckY, az);
            JOBS.put(world, job);
            source.sendSuccess(() -> Component.literal("Запущена проверка места. Постройка начнётся ТОЛЬКО после полного preflight. Чанки площадки подготавливаются постепенно; статус: /slavicmyths dev posad status."), false);
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
            job.preparation.close();
            JOBS.remove(job.world);
            source.sendSuccess(() -> Component.literal(partial ? "ОСТАНОВЛЕНО: область навсегда зарезервирована; возможна незавершённая постройка, автоматического повтора и выдачи наград нет." : "Отменено до записи блоков. Область не зарезервирована."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Пекельный посад: " + (job.phase == 0 ? "ПОДГОТОВКА ОСНОВАНИЯ z=" + job.supportZ : job.phase == 1 ? "PREFLIGHT" : "BUILD") + ", чанки " + job.chunk + "/" + job.chunks.size() + ", резерв " + job.reserved), false);
        return 1;
    }

    private static final class WaitingChunk extends RuntimeException {}
    private static final class Job {
        final CommandSourceStack source;
        final ServerLevel world;
        final org.slavicmyths.worldgen.SiteChunks preparation;
        final int turns, ax, deckY, az;
        final UUID token = UUID.randomUUID();
        final ToolLogOrigins protectedOrigins;
        final List<ChunkPos> chunks = new ArrayList<>();
        PosadPiece piece;
        BoundingBox bounds;
        int phase, chunk, layer, cell, supportX, supportZ, supportDepth=3;
        final List<PosadTerrain.Support> manualSupports=new ArrayList<>();
        boolean reserved;

        Job(CommandSourceStack source, int turns, int ax, int deckY, int az) {
            this.source = source;
            this.world = source.getLevel();
            this.preparation = new org.slavicmyths.worldgen.SiteChunks(world);
            this.turns = turns;
            this.ax = ax;
            this.deckY = deckY;
            this.az = az;
            this.protectedOrigins = ToolLogOrigins.get(world);
        }

        private void plan() {
            // Explicit construction adapts natural terrain, like bandit camps.
            // Native worldgen retains its independent strict terrain survey.
            int reads=0;
            while(supportZ<=112 && reads++<4096){
                int wx=ax+supportX,wz=az+supportZ;
                if(!preparation.ready(new ChunkPos(wx>>4,wz>>4)))throw new WaitingChunk();
                var at=new BlockPos(wx,deckY-supportDepth,wz);var old=world.getBlockState(at);
                if(old.hasBlockEntity()||protectedOrigins.blocked(at)||!natural(old)&&old.getFluidState().isEmpty())throw new IllegalStateException("Защищённое основание: "+at.toShortString());
                if(!old.isAir()&&old.getFluidState().isEmpty()&&!org.slavicmyths.worldgen.LandTerrain.vegetation(old)){
                    var local=PosadGeometry.rotate(new PosadGeometry.Cell(supportX,supportZ),-turns);
                    manualSupports.add(new PosadTerrain.Support(local.x(),local.z(),supportDepth));
                    supportDepth=3;supportX+=8;if(supportX>112){supportX=0;supportZ+=8;}
                }else if(++supportDepth>PosadTerrain.MAX_SUPPORT_DEPTH)throw new IllegalStateException("Не найдено основание в пределах "+PosadTerrain.MAX_SUPPORT_DEPTH+" блоков");
            }
            if(supportZ<=112)return;
            var check=new PosadTerrain.Decision(new PosadTerrain.Plan(ax,deckY,az,turns,0,manualSupports),"MANUAL_ADAPTIVE",reads);
            piece = new PosadPiece(check.plan());
            bounds = piece.getBoundingBox();
            if (!world.getWorldBorder().isWithinBounds(new BlockPos(bounds.minX(), deckY, bounds.minZ())) || !world.getWorldBorder().isWithinBounds(new BlockPos(bounds.maxX(), deckY, bounds.maxZ()))) throw new IllegalStateException("Город или мост выходит за границу мира");
            if (!ManualStructureRecords.get(world).overlapping(bounds).isEmpty()) throw new IllegalStateException("Есть ранее созданная или зарезервированная структура");
            chunks.clear();
            for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
                for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++) {
                    if (!preparation.ready(new ChunkPos(cx,cz))) throw new WaitingChunk();
                    chunks.add(new ChunkPos(cx, cz));
                }
            }
            layer = bounds.minY();
            phase = 1;
        }

        private net.minecraft.world.level.block.state.BlockState intended(int x,int y,int z){
            var state=piece.voxel(x,y,z);
            return state==null&&x>=0&&x<=112&&z>=0&&z<=112&&y>=1&&y<=17?Blocks.AIR.defaultBlockState():state;
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
                    var intended = intended(lx, layer - deckY, lz);
                    if (intended != null) {
                        var old = world.getBlockState(at);
                        if (old.hasBlockEntity() || world.getBlockEntity(at) != null || old.is(Blocks.BEDROCK) || protectedOrigins.blocked(at) || !(old.isAir() || old.is(Blocks.LAVA) || natural(old))) throw new IllegalStateException("Защищённый/чужой блок: " + at.toShortString() + " " + old);
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

        /**
         * Recheck a whole target chunk immediately before changing it. Full-area
         * preflight can take several ticks; another player or mod might have
         * altered blocks in that interval. Both check and placement are on the
         * same server tick, with no opportunity for interleaved server actions.
         */
        private void verifyChunkImmediately(ChunkPos cp) {
            for (var entry : ManualStructureRecords.get(world).overlapping(bounds)) {
                if (!entry.id().equals(token)) throw new IllegalStateException("Другой объект занял территорию во время строительства");
            }
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
                    int dx = x - ax, dz = z - az;
                    int lx = switch (turns) { case 0 -> dx; case 1 -> dz; case 2 -> 112 - dx; default -> 112 - dz; };
                    int lz = switch (turns) { case 0 -> dz; case 1 -> 112 - dx; case 2 -> 112 - dz; default -> dx; };
                    for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                        BlockPos at = new BlockPos(x, y, z);
                        if (!bounds.isInside(at) || intended(lx, y - deckY, lz) == null) continue;
                        var old = world.getBlockState(at);
                        if (old.hasBlockEntity() || world.getBlockEntity(at) != null || old.is(Blocks.BEDROCK) || protectedOrigins.blocked(at) || !(old.isAir() || old.is(Blocks.LAVA) || natural(old))) throw new IllegalStateException("Участок изменён после preflight: " + at.toShortString() + " " + old);
                    }
                }
            }
        }

        private void place() {
            if (chunk >= chunks.size()) {
                // Persist modified chunks while the durable reservation still says construction.
                // If a save fails, NEVER publish COMPLETE or permit a retry.
                if (!world.getServer().saveEverything(false, true, true)) throw new IllegalStateException("Не удалось надёжно сохранить построенные чанки");
                ManualStructureRecords.get(world).replace(token, "nether_posad", 0, new BlockPos(ax, deckY, az), bounds);
                world.getDataStorage().save();
                source.sendSuccess(() -> Component.literal("Пекельный посад построен: X=" + ax + " Y=" + deckY + " Z=" + az + ", поворот " + turns * 90 + "°. Награды — только одноразовые сундуки."), false);
                preparation.close();
                JOBS.remove(world);
                return;
            }
            ChunkPos cp = chunks.get(chunk);
            if (world.getChunkSource().getChunkNow(cp.x, cp.z) == null) throw new IllegalStateException("Чанк выгружен во время строительства: " + cp);
            verifyChunkImmediately(cp);
            BoundingBox clip = new BoundingBox(cp.getMinBlockX(), bounds.minY(), cp.getMinBlockZ(), cp.getMaxBlockX(), bounds.maxY(), cp.getMaxBlockZ());
            for(int x=Math.max(cp.getMinBlockX(),bounds.minX());x<=Math.min(cp.getMaxBlockX(),bounds.maxX());x++)for(int z=Math.max(cp.getMinBlockZ(),bounds.minZ());z<=Math.min(cp.getMaxBlockZ(),bounds.maxZ());z++){
                int dx=x-ax,dz=z-az;
                int lx=switch(turns){case 0->dx;case 1->dz;case 2->112-dx;default->112-dz;};
                int lz=switch(turns){case 0->dz;case 1->112-dx;case 2->112-dz;default->dx;};
                for(int y=bounds.minY();y<=bounds.maxY();y++)if(intended(lx,y-deckY,lz)!=null&&!world.getBlockState(new BlockPos(x,y,z)).isAir())world.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
            }
            piece.postProcess(world, world.structureManager(), world.getChunkSource().getGenerator(), world.random, clip, cp, new BlockPos(ax, deckY, az));
            world.getChunkSource().getChunkNow(cp.x, cp.z).setUnsaved(true);
            chunk++;
        }

        void step() {
            try {
                if (phase == 0) plan();
                else if (phase == 1) preflight();
                else place();
            } catch (WaitingChunk wait) {
                // Retry this survey cell next tick; SiteChunks owns bounded native tickets.
            } catch (RuntimeException ex) {
                LogUtils.getLogger().error("Posad manual QA job failed (reserved={}, phase={}, chunk={})", reserved, phase, chunk, ex);
                source.sendFailure(Component.literal("Создание Пекельного посада остановлено: " + ex.getMessage() + (reserved ? "; резерв сохранён, повтор запрещён" : "; блоки не записывались")));
                preparation.close();
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
        if (event.getLevel() instanceof ServerLevel world) {var job=JOBS.remove(world);if(job!=null)job.preparation.close();}
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent event) {
        for(var job:List.copyOf(JOBS.values()))if(job.world.getServer()==event.getServer()){job.preparation.close();JOBS.remove(job.world);}
    }

    private static boolean natural(net.minecraft.world.level.block.state.BlockState state) {
        return org.slavicmyths.worldgen.ManualStructureJobs.natural(state);
    }
    private PosadManualJobs() {}
}
