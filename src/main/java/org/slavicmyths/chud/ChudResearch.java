package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.slavicmyths.tales.*;

/** NPC research in the existing tale quest journal. Seed planning has a finite worker budget;
 * assignment occurs only after a real generated StructureStart has been verified on the server. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ChudResearch {
    private static final Set<UUID> PENDING=ConcurrentHashMap.newKeySet();
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"slavicmyths-research");t.setDaemon(true);return t;});
    @net.neoforged.bus.api.SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent e){PENDING.clear();}
    private static CompoundTag state(ServerPlayer p){return TaleData.compound(TaleData.get(p.serverLevel()).player(p.getUUID()),"ChudResearch");}
    public static boolean offer(ServerPlayer p,String giver){
        if(p.level().dimension()!=Level.OVERWORLD||!TaleData.get(p.serverLevel()).compatible())return false;
        var root=state(p);var q=root.getCompound(giver);
        if(q.contains("Target")){
            if(!q.getBoolean("Visited")){guide(p,giver,q);return true;}
            if(ChudResearchProgress.claim(q)){TaleData.get(p.serverLevel()).setDirty();org.slavicmyths.yaga.YagaServices.give(p,new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get()));p.displayClientMessage(Component.translatable("research.chud.reward"),false);TaleNetwork.send(p,TaleService.snapshot(p),false,true);return true;}
        }
        if(!PENDING.add(p.getUUID()))return false;
        var world=p.serverLevel();var registry=p.registryAccess();var structures=registry.registryOrThrow(Registries.STRUCTURE);
        var id=ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city");var structure=structures.get(id);
        var set=registry.registryOrThrow(Registries.STRUCTURE_SET).get(id);
        if(structure==null||set==null||!(set.placement() instanceof RandomSpreadStructurePlacement placement)){PENDING.remove(p.getUUID());return false;}
        var generator=world.getChunkSource().getGenerator();var random=world.getChunkSource().randomState();var templates=world.getStructureManager();
        var height=LevelHeightAccessor.create(world.getMinBuildHeight(),world.getHeight());long seed=world.getSeed();var origin=p.blockPosition();var server=p.server;UUID player=p.getUUID();
        var excluded=new HashSet<Long>();for(String key:root.getAllKeys()){var old=root.getCompound(key);if(old.contains("Target"))excluded.add(old.getLong("Target"));}
        for(long visited:root.getLongArray("Visited"))excluded.add(visited);
        var visitedStarts=new HashSet<Long>();for(long visited:root.getLongArray("VisitedStarts"))visitedStarts.add(visited);
        String legacyPrefix="EXPLORATION:structure/slavicmyths:chud_city/"+world.dimension().location()+"/";
        for(String key:org.slavicmyths.rpg.classes.ClassState.data(p).getCompound("XpLedger").getAllKeys())if(key.startsWith(legacyPrefix))try{visitedStarts.add(Long.parseLong(key.substring(legacyPrefix.length())));}catch(NumberFormatException ignored){}
        int page=Math.floorMod(root.getInt("SearchPage"),16);root.putInt("SearchPage",page+1);TaleData.get(world).setDirty();
        p.displayClientMessage(Component.translatable("research.chud.searching"),false);
        WORKER.execute(()->{
            BlockPos found=null;ChunkPos owner=null;
            ChudSearchBudget.begin(System.nanoTime()+TimeUnit.SECONDS.toNanos(30));
            try{
                int gx=Math.floorDiv(origin.getX()>>4,placement.spacing()),gz=Math.floorDiv(origin.getZ()>>4,placement.spacing());
                var candidates=new ArrayList<ChunkPos>();for(int x=gx-32;x<=gx+32;x++)for(int z=gz-32;z<=gz+32;z++)candidates.add(placement.getPotentialStructureChunk(seed,x*placement.spacing(),z*placement.spacing()));
                candidates.sort(Comparator.comparingDouble(c->Math.hypot(c.getMiddleBlockX()-origin.getX(),c.getMiddleBlockZ()-origin.getZ())));
                int checked=0;for(var chunk:candidates.subList(page*96,Math.min(candidates.size(),(page+1)*96))){if(++checked>96)break;ChudSearchBudget.check();
                    var context=new Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,templates,seed,chunk,height,structure.biomes()::contains);
                    var selected=ChudRegionPlanner.select(context);if(selected.isEmpty()||selected.get().city()==null)continue;
                    var plan=selected.get();var entrance=plan.city().world(plan.city().points.get(ChudPlan.Role.ENTRANCE));if(excluded.contains(entrance.asLong())||visitedStarts.contains(plan.owner().toLong()))continue;
                    found=entrance;owner=plan.owner();break;
                }
            }catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().debug("Bounded Chud research search ended",failure);}
            finally{ChudSearchBudget.end();}
            final BlockPos target=found;final ChunkPos startChunk=owner;
            server.execute(()->{try{
                var online=server.getPlayerList().getPlayer(player);if(online==null||online.serverLevel()!=world)return;
                if(target==null||!world.getWorldBorder().isWithinBounds(target)){online.displayClientMessage(Component.translatable("research.chud.unavailable"),false);return;}
                var chunk=world.getChunk(startChunk.x,startChunk.z);var start=chunk.getStartForStructure(structure);
                if(start==null||!start.isValid()||!start.getBoundingBox().isInside(target)){online.displayClientMessage(Component.translatable("research.chud.unavailable"),false);return;}
                CompoundTag assigned=new CompoundTag();assigned.putLong("Target",target.asLong());assigned.putLong("Owner",startChunk.toLong());assigned.putString("Dimension",world.dimension().location().toString());
                state(online).put(giver,assigned);TaleData.get(world).setDirty();TaleService.promote(online,"slavicmyths:chud_research_"+giver,1,false);guide(online,giver,assigned);
            }finally{PENDING.remove(player);}});
        });return true;
    }
    private static void guide(ServerPlayer p,String giver,CompoundTag q){
        var target=BlockPos.of(q.getLong("Target"));TaleService.hint(p,"slavicmyths:chud_research_"+giver,q.getString("Dimension"),UUID.nameUUIDFromBytes(("chud:"+q.getLong("Owner")).getBytes(java.nio.charset.StandardCharsets.UTF_8)),target);
        var hint=TaleData.get(p.serverLevel()).player(p.getUUID()).getCompound("Hints").getCompound("slavicmyths:chud_research_"+giver);hint.remove("Target");hint.putString("Source","entity.slavicmyths."+(giver.equals("yaga")?"baba_yaga":"kot_bayun"));hint.putLong("Expires",Long.MAX_VALUE);TaleData.get(p.serverLevel()).setDirty();
        p.displayClientMessage(Component.translatable("research.chud.assigned"),false);TaleNetwork.send(p,TaleService.snapshot(p),false,true);
    }
    /** Called by the existing loaded-structure visit observer, never a second polling loop. */
    public static void visited(ServerPlayer p,StructureStart start){
        var id=p.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure());if(id==null||!id.getPath().equals("chud_city"))return;
        var root=state(p);var visited=new HashSet<Long>();for(long value:root.getLongArray("Visited"))visited.add(value);
        boolean changed=false;
        for(String giver:List.of("yaga","bayun")){var q=root.getCompound(giver);if(ChudResearchProgress.visit(q,p.level().dimension().location().toString(),start.getChunkPos().toLong())){changed=true;visited.add(q.getLong("Target"));TaleService.promote(p,"slavicmyths:chud_research_"+giver,3,false);p.displayClientMessage(Component.translatable("research.chud.visited"),false);}}
        var starts=new HashSet<Long>();for(long value:root.getLongArray("VisitedStarts"))starts.add(value);starts.add(start.getChunkPos().toLong());root.putLongArray("VisitedStarts",starts.stream().mapToLong(Long::longValue).toArray());
        root.putLongArray("Visited",visited.stream().mapToLong(Long::longValue).toArray());TaleData.get(p.serverLevel()).setDirty();
        if(changed)TaleNetwork.send(p,TaleService.snapshot(p),false,true);
    }
    private ChudResearch(){}
}
