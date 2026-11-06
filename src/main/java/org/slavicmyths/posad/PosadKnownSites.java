package org.slavicmyths.posad;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Index of verified Nether starts, maintained when their owning chunks load. */
@EventBusSubscriber(modid = "slavicmyths")
public final class PosadKnownSites extends SavedData {
    private static final int LIMIT = 1024;
    private final LinkedHashMap<Long, BlockPos> sites = new LinkedHashMap<>();

    private static PosadKnownSites get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(
            new Factory<>(PosadKnownSites::new, (tag, registries) -> load(tag)),
            "slavicmyths_posad_discovered");
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel world) || !world.dimension().equals(Level.NETHER)
            || !(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk)) return;
        for (var start : event.getChunk().getAllStarts().values()) {
            if (!start.isValid() || !(start.getStructure() instanceof PosadStructure)) continue;
            long owner = start.getChunkPos().toLong();
            BlockPos center = start.getBoundingBox().getCenter();
            world.getServer().execute(() -> get(world).remember(owner, center));
        }
    }

    private void remember(long owner, BlockPos center) {
        if (center.equals(sites.put(owner, center))) return;
        if (sites.size() > LIMIT) sites.remove(sites.keySet().iterator().next());
        setDirty();
    }

    static Optional<BlockPos> nearest(ServerLevel world, BlockPos origin) {
        return get(world).sites.values().stream().filter(world.getWorldBorder()::isWithinBounds)
            .min(Comparator.comparingDouble(pos -> {
                double dx = (double) pos.getX() - origin.getX();
                double dz = (double) pos.getZ() - origin.getZ();
                return dx*dx + dz*dz;
            }));
    }

    private static PosadKnownSites load(CompoundTag tag) {
        var data = new PosadKnownSites();
        long[] entries = tag.getLongArray("Sites");
        for (int i = 0; i + 1 < entries.length && data.sites.size() < LIMIT; i += 2) {
            data.sites.put(entries[i], BlockPos.of(entries[i+1]));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] entries = new long[sites.size()*2];
        int i = 0;
        for (var entry : sites.entrySet()) {
            entries[i++] = entry.getKey();
            entries[i++] = entry.getValue().asLong();
        }
        tag.putLongArray("Sites", entries);
        return tag;
    }
}
