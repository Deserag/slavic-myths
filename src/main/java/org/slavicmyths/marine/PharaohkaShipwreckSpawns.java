package org.slavicmyths.marine;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Once per native shipwreck: deterministic 50% encounter; never repeats after defeat or unloading. */
@EventBusSubscriber(modid="slavicmyths")
public final class PharaohkaShipwreckSpawns extends SavedData {
    private final Set<Long> visited=new HashSet<>();
    private final Map<Long, BoundingBox> pending=new LinkedHashMap<>();
    public static PharaohkaShipwreckSpawns get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PharaohkaShipwreckSpawns::new, PharaohkaShipwreckSpawns::load, null),
            "slavicmyths_pharaohka_shipwrecks");
    }
    private static PharaohkaShipwreckSpawns load(CompoundTag tag, HolderLookup.Provider provider) {
        PharaohkaShipwreckSpawns data=new PharaohkaShipwreckSpawns();
        for (long key:tag.getLongArray("Visited")) data.visited.add(key);
        for (Tag entry:tag.getList("Pending",Tag.TAG_COMPOUND)) {
            CompoundTag p=(CompoundTag)entry;
            int[] a=p.getIntArray("Box");
            if (a.length==6) data.pending.put(p.getLong("Key"),
                new BoundingBox(a[0],a[1],a[2],a[3],a[4],a[5]));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLongArray("Visited",visited.stream().mapToLong(Long::longValue).toArray());
        ListTag list=new ListTag();
        pending.forEach((key,box)->{
            CompoundTag entry=new CompoundTag();
            entry.putLong("Key",key);
            entry.putIntArray("Box",new int[]{box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()});
            list.add(entry);
        });
        tag.put("Pending",list);
        return tag;
    }
    @SubscribeEvent public static void chunkLoaded(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var starts=List.copyOf(event.getChunk().getAllStarts().values());
        level.getServer().execute(()->{
            var structures=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            PharaohkaShipwreckSpawns data=get(level);
            for (var start:starts) {
                if (!start.isValid()) continue;
                var id=structures.getKey(start.getStructure());
                if (id==null || !id.getPath().startsWith("shipwreck") || !id.getNamespace().equals("minecraft")) continue;
                long key=start.getChunkPos().toLong();
                if (!data.visited.add(key)) continue;
                if (RandomSource.create(level.getSeed() ^ (key*0x9E3779B97F4A7C15L)).nextBoolean())
                    data.pending.put(key,start.getBoundingBox());
                data.setDirty();
            }
        });
    }
    @SubscribeEvent public static void levelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime()%40!=0
            || level.getDifficulty()==Difficulty.PEACEFUL) return;
        get(level).process(level);
    }
    private void process(ServerLevel level) {
        int budget=0;
        for (var entry:List.copyOf(pending.entrySet())) {
            if (budget++>=4) break;
            BoundingBox box=entry.getValue();
            BlockPos middle=box.getCenter();
            if (level.getChunkSource().getChunkNow(middle.getX()>>4,middle.getZ()>>4)==null) continue;
            PharaohkaEntity boss=PharaohkaContent.LEADER.get().create(level);
            if (boss==null) continue;
            boolean placed=false;
            // Loaded water only; never force chunks or overwrite shipwreck blocks.
            for (int radius=2;radius<=10 && !placed;radius+=2) {
                for (int attempt=0;attempt<16 && !placed;attempt++) {
                    int x=middle.getX()+level.random.nextInt(radius*2+1)-radius;
                    int z=middle.getZ()+level.random.nextInt(radius*2+1)-radius;
                    for (int y=Math.min(level.getSeaLevel(),box.maxY()+5);y>=box.minY()-1;y--) {
                        BlockPos pos=new BlockPos(x,y,z);
                        if (!level.hasChunkAt(pos) || !level.getFluidState(pos).is(FluidTags.WATER)) continue;
                        boss.setPos(x+.5,y+.2,z+.5);
                        if (!level.noCollision(boss)) continue;
                        placed=level.addFreshEntity(boss);
                        if (placed) boss.setPersistenceRequired();
                        break;
                    }
                }
            }
            if (placed) {
                pending.remove(entry.getKey());
                setDirty();
            }
        }
    }
}