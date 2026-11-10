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
    record Layout(BlockPos anchor,int turns){}
    private final LinkedHashMap<Long,Layout> layouts=new LinkedHashMap<>();
    java.util.List<Layout> nearby(BlockPos origin){return layouts.values().stream().filter(l->l.anchor().offset(56,0,56).distSqr(origin)<192D*192D).limit(4).toList();}

    static PosadKnownSites get(ServerLevel world) {
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
            for(var raw:start.getPieces())if(raw instanceof PosadPiece piece){
                var data=get(world);data.remember(owner,center);
                var value=new Layout(piece.anchor(),piece.turns());
                if(!value.equals(data.layouts.put(owner,value)))data.setDirty();
                if(data.layouts.size()>LIMIT)data.layouts.remove(data.layouts.keySet().iterator().next());
                // A naturally generated city must be visible to the same persisted boss
                // encounter index as an explicitly built settlement, including after relog.
                // The deterministic UUID makes repeated chunk loads idempotent.
                var records=org.slavicmyths.worldgen.ManualStructureRecords.get(world);
                var nativeId=java.util.UUID.nameUUIDFromBytes(("native-nether-posad:"+owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if(records.entries().stream().noneMatch(entry->entry.id().equals(nativeId)))
                    records.add(nativeId,"nether_posad",piece.turns(),piece.anchor(),piece.getBoundingBox());
            }
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
        for(var raw:tag.getList("Layouts",10)){
            var row=(CompoundTag)raw;if(data.layouts.size()>=LIMIT)break;
            data.layouts.put(row.getLong("Owner"),new Layout(BlockPos.of(row.getLong("Anchor")),Math.floorMod(row.getInt("Turns"),4)));
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
        var list=new net.minecraft.nbt.ListTag();
        for(var entry:layouts.entrySet()){var row=new CompoundTag();row.putLong("Owner",entry.getKey());row.putLong("Anchor",entry.getValue().anchor().asLong());row.putInt("Turns",entry.getValue().turns());list.add(row);}
        tag.put("Layouts",list);
        return tag;
    }
}
