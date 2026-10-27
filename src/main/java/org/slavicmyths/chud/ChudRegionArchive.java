package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.ManualStructureRecords;

/** Bounded per-region saved-start reads, never Minecraft chunk requests or retrogen.
 * Existing native roots win over a later generator policy. Live manual reservations are protected too. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ChudRegionArchive {
    private record WorldKey(Object generator,Object random,long seed){}
    private record Region(int x,int z){}
    public record Existing(boolean blocked,ChudRegionPlanner.Selected selected){}
    private static final Map<WorldKey,Binding> WORLDS=new ConcurrentHashMap<>();
    private static final class Binding {
        final ServerLevel world;final ManualStructureRecords records;volatile boolean active=true;
        final Map<Region,CompletableFuture<Existing>> regions=new LinkedHashMap<>(128,.75f,true);
        Binding(ServerLevel w){world=w;records=ManualStructureRecords.get(w);}
    }
    private static WorldKey key(ServerLevel w){return new WorldKey(w.getChunkSource().getGenerator(),w.getChunkSource().randomState(),w.getSeed());}
    private static Binding binding(Structure.GenerationContext c){return WORLDS.get(new WorldKey(c.chunkGenerator(),c.randomState(),c.seed()));}
    @net.neoforged.bus.api.SubscribeEvent public static void load(net.neoforged.neoforge.event.level.LevelEvent.Load e){if(e.getLevel() instanceof ServerLevel w&&w.dimension()==Level.OVERWORLD)WORLDS.put(key(w),new Binding(w));}
    @net.neoforged.bus.api.SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel w)WORLDS.entrySet().removeIf(v->{if(v.getValue().world!=w)return false;v.getValue().active=false;return true;});}
    @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){WORLDS.entrySet().removeIf(v->{if(v.getValue().world.getServer()!=e.getServer())return false;v.getValue().active=false;return true;});}
    public static Existing existing(Structure.GenerationContext c,org.slavicmyths.worldgen.ChudRegionPlacement placement){
        var binding=binding(c);if(binding==null)return new Existing(false,null);if(!binding.active)return new Existing(true,null);
        var region=new Region(Math.floorDiv(c.chunkPos().x,placement.spacing()),Math.floorDiv(c.chunkPos().z,placement.spacing()));
        CompletableFuture<Existing> future;
        synchronized(binding.regions){future=binding.regions.get(region);if(future==null){
            var candidates=placement.candidates(c.seed(),c.chunkPos().x,c.chunkPos().z);
            var reads=candidates.stream().map(at->binding.world.getChunkSource().chunkMap.read(at)).toList();
            future=CompletableFuture.allOf(reads.toArray(CompletableFuture[]::new)).thenApply(ignored->{
                if(!binding.active)return new Existing(true,null);ChudRegionPlanner.Selected found=null;
                for(int i=0;i<reads.size();i++){var saved=reads.get(i).join();if(saved.isEmpty())continue;var starts=saved.get().getCompound("structures").getCompound("starts");
                    for(String type:List.of("chud_city","chud_abandoned")){
                        var n=starts.getCompound("slavicmyths:"+type);if(n.isEmpty()||n.getString("id").equals("INVALID"))continue;
                        var root=decode(candidates.get(i),n);if(root==null)return new Existing(true,null);
                        if(found!=null&&!found.owner().equals(root.owner()))return new Existing(true,null);found=root;
                    }
                }return new Existing(false,found);
            });
            binding.regions.put(region,future);while(binding.regions.size()>512)binding.regions.remove(binding.regions.keySet().iterator().next());
        }}
        try{while(true){ChudSearchBudget.check();try{return future.get(50,TimeUnit.MILLISECONDS);}catch(TimeoutException waiting){if(!binding.active)return new Existing(true,null);}}}
        catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new CancellationException("Saved-start inspection interrupted");}
        catch(ExecutionException failed){com.mojang.logging.LogUtils.getLogger().warn("Chud saved-start read failed safely for {}",region,failed.getCause());return new Existing(true,null);}
    }
    private static ChudRegionPlanner.Selected decode(ChunkPos owner,CompoundTag start){
        try{for(var tag:start.getList("Children",10)){var n=(CompoundTag)tag;
            if(n.contains("ChudPlan")){var p=ChudPlan.load(n.getCompound("ChudPlan"));var abandoned=n.contains("AbandonedPlan")?AbandonedPlan.load(n.getCompound("AbandonedPlan")):null;var access=n.contains("Access")?ChudSurfaceAccess.load(n.getCompound("Access")):null;return new ChudRegionPlanner.Selected(owner,access==null?p.world(p.points.get(ChudPlan.Role.ENTRANCE)):access.mouth,p,abandoned,access,n.contains("RoofShell")?n.getInt("RoofShell"):14,true);}
            if(n.contains("AbandonedPlan")){var p=AbandonedPlan.load(n.getCompound("AbandonedPlan"));var access=n.contains("Access")?ChudSurfaceAccess.load(n.getCompound("Access")):null;return new ChudRegionPlanner.Selected(owner,access==null?p.world(p.targets.get("entrance")):access.mouth,null,p,access,14,true);}
        }}catch(RuntimeException malformed){com.mojang.logging.LogUtils.getLogger().warn("Chud saved root cannot be decoded safely at {}",owner,malformed);}return null;
    }
    @net.neoforged.bus.api.SubscribeEvent public static void chunk(net.neoforged.neoforge.event.level.ChunkEvent.Load e){
        if(!(e.getLevel() instanceof ServerLevel world)||world.dimension()!=Level.OVERWORLD)return;var binding=WORLDS.get(key(world));if(binding==null)return;
        for(var start:e.getChunk().getAllStarts().values())if(start.isValid()){
            ChudRegionPlanner.Selected selected=null;
            for(var piece:start.getPieces())if(piece instanceof ChudPiece p)selected=new ChudRegionPlanner.Selected(start.getChunkPos(),p.access==null?p.plan.world(p.plan.points.get(ChudPlan.Role.ENTRANCE)):p.access.mouth,p.plan,p.abandoned,p.access,p.roofShell,true);else if(piece instanceof AbandonedPiece p)selected=new ChudRegionPlanner.Selected(start.getChunkPos(),p.access==null?p.plan.world(p.plan.targets.get("entrance")):p.access.mouth,null,p.plan,p.access,14,true);
            if(selected==null)continue;
            var placement=world.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));if(placement==null||!(placement.placement() instanceof org.slavicmyths.worldgen.ChudRegionPlacement p)||!p.candidates(world.getSeed(),start.getChunkPos().x,start.getChunkPos().z).contains(start.getChunkPos()))continue;
            var region=new Region(Math.floorDiv(start.getChunkPos().x,p.spacing()),Math.floorDiv(start.getChunkPos().z,p.spacing()));
            synchronized(binding.regions){var existing=binding.regions.get(region);var old=existing==null||!existing.isDone()?null:existing.getNow(null);if(old!=null&&old.selected()!=null&&!old.selected().owner().equals(selected.owner()))binding.regions.put(region,CompletableFuture.completedFuture(new Existing(true,null)));else binding.regions.put(region,CompletableFuture.completedFuture(new Existing(false,selected)));while(binding.regions.size()>512)binding.regions.remove(binding.regions.keySet().iterator().next());}
        }
    }
    public static boolean protectedRecords(Structure.GenerationContext c,Collection<BoundingBox> excavation){
        var binding=binding(c);if(binding==null)return false;
        for(var record:binding.records.entries())for(var box:excavation)if(record.bounds().intersects(box)){noteProtectedRecord(c,record.family());return true;}return false;
    }
    private static void noteProtectedRecord(Structure.GenerationContext c,String family){org.slavicmyths.worldgen.StructureCoverageService.note(org.slavicmyths.worldgen.StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"savedRecord:"+family,1);}
    private ChudRegionArchive(){}
}
