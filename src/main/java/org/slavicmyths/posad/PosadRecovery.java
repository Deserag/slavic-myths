package org.slavicmyths.posad;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import org.slavicmyths.worldgen.ManualStructureRecords;

/** Conservative OP recovery: never edits blocks, loads chunks, or re-arms possible loot. */
public final class PosadRecovery {
    static final String EMPTY_SAFE = "posad_building/incomplete_city_safe";
    static final String REWARD_LOCKED = "posad_building/incomplete_city_reward_locked";

    public static int releaseEmpty(CommandSourceStack source, BlockPos anchor) {
        if (!source.hasPermission(2)) return 0;
        var world = source.getLevel();
        if (PosadManualJobs.active(world)) return reject(source, "Сначала остановите активное строительство.");
        var records = ManualStructureRecords.get(world);
        var candidates = records.entries().stream().filter(e -> e.arrival().equals(anchor)
                && e.family().equals(EMPTY_SAFE)).toList();
        if (candidates.size() != 1) return reject(source,
                "Нет безопасного незавершённого резерва в этой точке. Завершённые города, старые резервы без журнала и области возможных наград не освобождаются.");
        var entry = candidates.getFirst();
        var box = entry.bounds();
        // Fixed city bounds plus a bounded bridge/foundation; reject corrupt oversized records.
        if ((long)box.getXSpan()*box.getZSpan() > 30000 || box.getYSpan() > 128)
            return reject(source, "Некорректный размер резерва.");
        for (var other : records.overlapping(box)) if (!other.id().equals(entry.id()))
            return reject(source, "Область пересекается с другой записью.");
        if (!world.getEntities((net.minecraft.world.entity.Entity)null, AABB.of(box), e -> true).isEmpty())
            return reject(source, "В области находятся сущности; резерв сохранён.");
        // Inspect section palettes on this server tick. Requiring whole boundary sections
        // to be empty is deliberately stricter than checking only the reservation box.
        // There is no multi-tick window for a player/mod to change an already checked block.
        for (int cx=box.minX()>>4; cx<=box.maxX()>>4; cx++)
            for (int cz=box.minZ()>>4; cz<=box.maxZ()>>4; cz++) {
                var chunk=world.getChunkSource().getChunkNow(cx,cz);
                if (chunk==null) return reject(source, "Все чанки области должны быть уже загружены.");
                if (!chunk.getBlockEntities().isEmpty()) return reject(source, "В чанке есть контейнер или другой block entity.");
                for (int sy=world.getSectionIndex(box.minY()); sy<=world.getSectionIndex(box.maxY()); sy++)
                    if (!chunk.getSection(sy).hasOnlyAir()) return reject(source,
                            "Область не пуста. Уберите блоки вручную; также проверяются граничные секции чанков.");
            }
        records.remove(entry.id());
        world.getDataStorage().save();
        source.sendSuccess(() -> Component.literal("Освобождён пустой резерв без возможных наград: "+anchor.toShortString()+". Блоки и сущности не изменялись."), false);
        return 1;
    }
    private static int reject(CommandSourceStack source,String reason) {
        source.sendFailure(Component.literal("Пекельный посад: "+reason));return 0;
    }
    private PosadRecovery() {}
}
