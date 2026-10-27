package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.nbt.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/** Bounded memory of actual loaded starts. No generation, remote reads, search radius or tick scan. */
@EventBusSubscriber(modid="slavicmyths")
public final class ChudKnownSites {
    private static final int LIMIT=1024;
    private static final Map<ServerLevel,LinkedHashMap<Long,ChudCreationJobs.Result>> KNOWN=new WeakHashMap<>();
    /** Persist the coordinates of physically loaded native starts. Never treat a seed forecast as a discovery. */
    private static final class Discoveries extends SavedData {
        final LinkedHashMap<Long,ChudCreationJobs.Result> sites=new LinkedHashMap<>();
        static Discoveries get(ServerLevel world){return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Discoveries::new,(tag,registries)->load(tag)),"slavicmyths_chud_discovered");}
        static Discoveries load(CompoundTag tag){var d=new Discoveries();for(Tag value:tag.getList("Cities",Tag.TAG_COMPOUND)){
            var n=(CompoundTag)value;int[] b=n.getIntArray("Bounds");if(b.length!=6)continue;
            long owner=n.getLong("Owner");var box=new BoundingBox(b[0],b[1],b[2],b[3],b[4],b[5]);
            d.sites.put(owner,new ChudCreationJobs.Result(BlockPos.of(n.getLong("Entrance")),new net.minecraft.world.level.ChunkPos(owner),box,n.getInt("Pieces")));
        }return d;}
        void remember(ChudCreationJobs.Result site){long key=site.owner().toLong();var old=sites.put(key,site);if(old==null||!old.equals(site))setDirty();while(sites.size()>1024){sites.remove(sites.keySet().iterator().next());setDirty();}}
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){var cities=new ListTag();for(var site:sites.values()){
            var n=new CompoundTag();n.putLong("Owner",site.owner().toLong());n.putLong("Entrance",site.entrance().asLong());n.putInt("Pieces",site.pieces());var b=site.bounds();n.putIntArray("Bounds",new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()});cities.add(n);
        }tag.put("Cities",cities);return tag;}
    }

    @SubscribeEvent public static void loaded(ChunkEvent.Load event){
        if(!(event.getLevel() instanceof ServerLevel world)||world.dimension()!=Level.OVERWORLD||
                !(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk))return;
        var starts=List.copyOf(event.getChunk().getAllStarts().values());
        world.getServer().execute(()->{
            if(world.getServer().isStopped())return;
            for(var start:starts)if(start.isValid())for(var piece:start.getPieces())if(piece instanceof ChudPiece city){
                var entrance=city.access==null?city.plan.world(city.plan.points.get(ChudPlan.Role.ENTRANCE)):city.access.mouth;
                // An underground-only native start is legitimate: the book gives its X/Z
                // exploration area and the player may excavate to the existing entrance.
                // Lack of a staircase must not hide a real city from Yaga/Bayun quests.
                var b=start.getBoundingBox();
                if(!b.isInside(entrance))continue;
                var bounds=new BoundingBox(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());
                synchronized(KNOWN){
                    var sites=KNOWN.computeIfAbsent(world,key->new LinkedHashMap<>());
                    var site=new ChudCreationJobs.Result(entrance,start.getChunkPos(),bounds,start.getPieces().size());
                    sites.put(start.getChunkPos().toLong(),site);Discoveries.get(world).remember(site);
                    while(sites.size()>LIMIT)sites.remove(sites.keySet().iterator().next());
                }
            }
        });
    }

    /** Unloaded starts remain known until world unload; durable selected quests remain in TaleData. */
    public static Optional<ChudCreationJobs.Result> nearest(ServerLevel world,BlockPos origin,Set<Long> targets,Set<Long> visitedOwners){
        synchronized(KNOWN){
            var sites=new LinkedHashMap<Long,ChudCreationJobs.Result>(Discoveries.get(world).sites);
            var loaded=KNOWN.get(world);if(loaded!=null)sites.putAll(loaded);
            return sites.values().stream().filter(site->!targets.contains(site.entrance().asLong())&&
                    !visitedOwners.contains(site.owner().toLong())&&world.getWorldBorder().isWithinBounds(site.entrance())&&ChudResearch.xzDistanceSquared(site.entrance(),origin)<=3000.*3000)
                .min(Comparator.comparingDouble(site->ChudResearch.xzDistanceSquared(site.entrance(),origin)));
        }
    }

    @SubscribeEvent public static void unloaded(LevelEvent.Unload event){
        if(event.getLevel() instanceof ServerLevel world)synchronized(KNOWN){KNOWN.remove(world);}
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event){
        synchronized(KNOWN){KNOWN.keySet().removeIf(world->world.getServer()==event.getServer());}
    }
    private ChudKnownSites(){}
}
