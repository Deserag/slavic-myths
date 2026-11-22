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

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** One persistent encounter per native shipwreck; never repeats after defeat or unloading. */
@EventBusSubscriber(modid="slavicmyths")
public final class PharaohkaShipwreckSpawns extends SavedData {
    private final Set<Long> visited=new HashSet<>();
    private final Map<Long, BoundingBox> pending=new LinkedHashMap<>();
    private int pendingCursor;
    public static PharaohkaShipwreckSpawns get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PharaohkaShipwreckSpawns::new, PharaohkaShipwreckSpawns::load, null),
            "slavicmyths_pharaohka_shipwrecks");
    }
    private static PharaohkaShipwreckSpawns load(CompoundTag tag, HolderLookup.Provider provider) {
        PharaohkaShipwreckSpawns data=new PharaohkaShipwreckSpawns();
        for (long key:tag.getLongArray("Visited")) data.visited.add(key);
        data.pendingCursor=Math.max(0,tag.getInt("PendingCursor"));
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
        tag.putInt("PendingCursor",pendingCursor);
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

        level.getServer().execute(()->{
            var structures=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            PharaohkaShipwreckSpawns data=get(level);
            for (var start:List.copyOf(event.getChunk().getAllStarts().values())) {
                if (!start.isValid()) continue;
                var id=structures.getKey(start.getStructure());
                if (id==null || !id.getPath().startsWith("shipwreck") || !id.getNamespace().equals("minecraft")) continue;
                long key=start.getChunkPos().toLong();
                if (!data.visited.add(key)) continue;
                if (shouldSpawn(level.getSeed(), key))
                    data.pending.put(key,start.getBoundingBox());
                data.setDirty();
            }
            // Placement runs only on a later level tick, after chunk promotion to FULL.
        });
    }
    /** Every shipwreck receives its resident encounter, independently of nearby players. */
    public static boolean shouldSpawn(long worldSeed,long shipwreckKey) {
        return true;
    }
    @SubscribeEvent public static void levelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime()%40!=0
            || level.getDifficulty()==Difficulty.PEACEFUL) return;
        if (level.getGameTime()%200==0) {
            var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            var data=get(level);
            for(var player:level.players()) {
                int cx=player.getBlockX()>>4,cz=player.getBlockZ()>>4;
                for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=8;dz++) {
                    var chunk=level.getChunkSource().getChunkNow(cx+dx,cz+dz);
                    if(chunk==null)continue;
                    for(var start:chunk.getAllStarts().values()) {
                        var id=registry.getKey(start.getStructure());
                        if(!start.isValid()||id==null||!id.getNamespace().equals("minecraft")||!id.getPath().startsWith("shipwreck"))continue;
                        long key=start.getChunkPos().toLong();
                        if(data.visited.add(key)) {
                            data.pending.put(key,start.getBoundingBox());
                            data.setDirty();
                        }
                    }
                }
            }
        }
        get(level).process(level);
    }
    private void process(ServerLevel level) {
        if (pending.isEmpty()) return;
        // Rotate over all entries so unloaded wrecks cannot block newer encounters.
        var entries=List.copyOf(pending.entrySet());
        int scans=Math.min(entries.size(),64);
        int attempts=0;
        for (int i=0;i<scans && attempts<4;i++) {
            var entry=entries.get((pendingCursor+i)%entries.size());
            BoundingBox box=entry.getValue();
            BlockPos middle=box.getCenter();
            if (level.getChunkSource().getChunkNow(middle.getX()>>4,middle.getZ()>>4)==null) continue;
            if(!level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(middle).inflate(512),MarineEncounterSpawns::isMarineBoss).isEmpty())continue;
            attempts++;
            PharaohkaEntity boss=PharaohkaContent.LEADER.get().create(level);
            if (boss==null) continue;
            boolean placed=false;
            // Loaded water only; never force chunks or overwrite shipwreck blocks.
            for (int radius=2;radius<=10 && !placed;radius+=2) {
                for (int attempt=0;attempt<16 && !placed;attempt++) {
                    int x=middle.getX()+level.random.nextInt(radius*2+1)-radius;
                    int z=middle.getZ()+level.random.nextInt(radius*2+1)-radius;
                    for (int y=Math.min(level.getSeaLevel(),box.maxY()+5);y>=Math.max(level.getMinBuildHeight(),Math.min(box.minY()-1,level.getSeaLevel()-16));y--) {
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
        // Advance one slot at a time: scanning in multiples of 64 must not starve every other batch.
        pendingCursor=pending.isEmpty()?0:(pendingCursor+1)%pending.size();
    }
}