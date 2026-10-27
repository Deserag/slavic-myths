package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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

    @SubscribeEvent public static void loaded(ChunkEvent.Load event){
        if(!(event.getLevel() instanceof ServerLevel world)||world.dimension()!=Level.OVERWORLD||
                !(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk))return;
        var starts=List.copyOf(event.getChunk().getAllStarts().values());
        world.getServer().execute(()->{
            if(world.getServer().isStopped())return;
            for(var start:starts)if(start.isValid())for(var piece:start.getPieces())if(piece instanceof ChudPiece city){
                var entrance=city.plan.world(city.plan.points.get(ChudPlan.Role.ENTRANCE));
                var b=start.getBoundingBox();
                if(!b.isInside(entrance))continue;
                var bounds=new BoundingBox(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());
                synchronized(KNOWN){
                    var sites=KNOWN.computeIfAbsent(world,key->new LinkedHashMap<>());
                    sites.put(start.getChunkPos().toLong(),new ChudCreationJobs.Result(entrance,start.getChunkPos(),bounds,start.getPieces().size()));
                    while(sites.size()>LIMIT)sites.remove(sites.keySet().iterator().next());
                }
            }
        });
    }

    /** Unloaded starts remain known until world unload; durable selected quests remain in TaleData. */
    public static Optional<ChudCreationJobs.Result> nearest(ServerLevel world,BlockPos origin,Set<Long> targets,Set<Long> visitedOwners){
        synchronized(KNOWN){
            var sites=KNOWN.get(world);
            if(sites==null)return Optional.empty();
            return sites.values().stream().filter(site->!targets.contains(site.entrance().asLong())&&
                    !visitedOwners.contains(site.owner().toLong())&&world.getWorldBorder().isWithinBounds(site.entrance()))
                .min(Comparator.comparingDouble(site->site.entrance().distSqr(origin)));
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
