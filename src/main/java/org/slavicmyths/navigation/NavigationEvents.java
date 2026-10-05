package org.slavicmyths.navigation;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slavicmyths.hunt.*;
import org.slavicmyths.yaga.*;

@EventBusSubscriber(modid="slavicmyths")
public final class NavigationEvents {
    // Per-player transient discovery probe, cleared on logout. Not a world scan/index of all structures.
    private record Probe(ResourceLocation dimension,BlockPos position){}
    private static final Map<UUID,Probe> probes=new HashMap<>();
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){reconcile(p);var state=NavigationRecords.get(p.serverLevel()).player(p.getUUID());if(state.trackingEnded){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("navigation.slavicmyths.tracking_ended"),false);state.trackingEnded=false;NavigationRecords.get(p.serverLevel()).setDirty();}NavigationNetwork.sync(p,false);}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){probes.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){probes.remove(p.getUUID());NavigationNetwork.sync(p,false);}}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)NavigationNetwork.sync(p,false);}
    @SubscribeEvent public static void joined(EntityJoinLevelEvent e){
        if(!(e.getLevel() instanceof ServerLevel level)||NavigationManager.type(e.getEntity())==null)return;
        NavigationManager.remember(e.getEntity());
        var data=NavigationRecords.get(level);
        for(ServerPlayer p:level.getServer().getPlayerList().getPlayers()){
            var pending=data.pendingQuests.get(p.getUUID());
            if(pending!=null&&pending.type().equals(NavigationManager.type(e.getEntity())))NavigationManager.bindPending(p);
        }
    }
    @SubscribeEvent public static void left(EntityLeaveLevelEvent e){
        if(!(e.getLevel() instanceof ServerLevel w))return;
        var reason=e.getEntity().getRemovalReason();
        if(reason!=null&&reason.shouldDestroy())ended(w,e.getEntity().getUUID(),null);
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void died(LivingDeathEvent e){
        if(e.getEntity().level() instanceof ServerLevel w&&NavigationManager.type(e.getEntity())!=null)
            ended(w,e.getEntity().getUUID(),e.getSource().getEntity() instanceof ServerPlayer p?p.getUUID():null);
    }
    @SubscribeEvent public static void attacked(LivingIncomingDamageEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||NavigationManager.type(e.getEntity())==null)return;
        var data=NavigationRecords.get(p.serverLevel());
        for(var a:data.assignmentsFor(p.getUUID()))if(a.target().equals(e.getEntity().getUUID()))
            NavigationManager.discovered(p,a,e.getEntity());
    }
    private static void ended(ServerLevel level,UUID target,UUID killer){
        var data=NavigationRecords.get(level);if(data.encounters.remove(target)!=null)data.setDirty();
        for(var a:List.copyOf(data.assignments.values()))if(a.target().equals(target)){
            boolean completed=a.player().equals(killer);
            var state=data.player(a.player());boolean tracked=a.marker().equals(state.tracked);state.end(a.marker(),completed);data.assignments.remove(a.marker());data.setDirty();
            ServerPlayer p=level.getServer().getPlayerList().getPlayer(a.player());
            if(a.quest().startsWith("contract:")){
                var pending=data.pendingQuests.get(a.player());
                if(pending!=null&&!completed)data.pendingQuests.put(a.player(),new NavigationRecords.PendingQuest(pending.quest(),pending.type(),pending.id(),pending.revision()+1,
                    tracked?state.trackingChoiceRevision:pending.trackingChoiceRevision(),tracked));
                else if(pending!=null)data.pendingQuests.remove(a.player());
            }
            if(p!=null){if(tracked){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("navigation.slavicmyths.tracking_ended"),false);state.trackingEnded=false;}
                if(!completed&&a.quest().startsWith("contract:"))NavigationManager.bindPending(p);
                NavigationNetwork.sync(p,false);}
        }
    }
    public static void reconcile(ServerPlayer p){
        var data=NavigationRecords.get(p.serverLevel());var records=HuntRecords.get(p.server.getLevel(Level.OVERWORLD));
        var progress=YagaData.get(p.server.getLevel(Level.OVERWORLD)).progress(p.getUUID());
        var pending=data.pendingQuests.get(p.getUUID());if(pending!=null&&!pending.quest().equals(progress.active)){data.pendingQuests.remove(p.getUUID());data.setDirty();}
        for(var assignment:data.assignmentsFor(p.getUUID())){
            boolean valid=true;
            if(assignment.quest().startsWith("contract:"))valid=progress.active.equals(assignment.quest());
            if(assignment.quest().startsWith("hunt:")){var h=records.hunts.get(p.getUUID());valid=h!=null&&h.status==HuntRecords.Status.ACTIVE&&h.target.equals(assignment.target());}
            if(assignment.quest().startsWith("ritual:")){var b=records.bosses.get(assignment.quest());valid=b!=null&&b.active&&assignment.target().equals(b.target);}
            if(!valid){data.player(p.getUUID()).end(assignment.marker(),false);data.assignments.remove(assignment.marker());data.setDirty();}
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.tickCount%20!=0)return;
        // Bounded reconciliation of this player's existing assignments, including unloaded dev-cleared targets.
        if(p.tickCount%100==0){var own=NavigationRecords.get(p.serverLevel()).player(p.getUUID());int before=own.markers.size();reconcile(p);
            if(own.markers.size()!=before){if(own.trackingEnded){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("navigation.slavicmyths.tracking_ended"),false);own.trackingEnded=false;}NavigationManager.changed(p);}}
        var data=NavigationRecords.get(p.serverLevel());var state=data.player(p.getUUID());boolean changed=false;
        for(var assignment:data.assignmentsFor(p.getUUID()))if(assignment.dimension().equals(p.level().dimension().location())){
            ServerLevel level=p.serverLevel();var entity=level.getEntity(assignment.target());var marker=state.markers.get(assignment.marker());if(marker==null)continue;
            if(entity!=null&&entity.isAlive()&&(p.distanceToSqr(entity)<=48*48||entity instanceof Mob mob&&mob.getTarget()==p)){
                NavigationManager.discovered(p,assignment,entity);continue;
            }
            if(marker.area()==null){
                if(entity!=null&&entity.isAlive()&&entity.blockPosition().distSqr(new BlockPos(marker.x(),marker.y(),marker.z()))>=64){
                    var pos=entity.blockPosition();changed|=state.upsert(new SlavicMarker(marker.id(),marker.source(),marker.category(),marker.kind(),marker.lifetime(),marker.name(),marker.dimension(),
                        pos.getX(),pos.getY(),pos.getZ(),marker.createdAt(),marker.revision()+1,null));
                }
                continue;
            }
            SearchArea area=marker.area();boolean wasInside=area.state()==SearchArea.State.INSIDE_SEARCH_AREA;
            boolean inside=area.distance(p.getX(),p.getZ())<=(wasInside?area.radius()+12:area.radius()-4);
            var next=inside?SearchArea.State.INSIDE_SEARCH_AREA:marker.id().equals(state.tracked)?SearchArea.State.APPROACHING:SearchArea.State.ASSIGNED;
            if(next!=area.state()){
                changed|=state.upsert(marker.withArea(area.withState(next)));
                long now=level.getGameTime();if(inside&&!wasInside&&now>=state.enterMessageAfter){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("navigation.slavicmyths.entered"),false);state.enterMessageAfter=now+200;}
            }
        }
        if(changed)NavigationManager.changed(p);
        if(p.tickCount%40!=0)return;
        Probe probe=probes.get(p.getUUID());
        if(probe==null||!probe.dimension().equals(p.level().dimension().location())||probe.position().distSqr(p.blockPosition())>=64){
            probes.put(p.getUUID(),new Probe(p.level().dimension().location(),p.blockPosition()));discoverStructures(p,false);
            discoverYaga(p);
        }
        // Validate only this player's activated stones when their chunks are already loaded.
        for(var marker:List.copyOf(state.markers.values()))if(marker.category()==MarkerCategory.WAYSTONE&&marker.dimension().equals(p.level().dimension().location())){
            BlockPos pos=new BlockPos(marker.x(),marker.y(),marker.z());
            if(p.serverLevel().hasChunkAt(pos)&&!p.serverLevel().getBlockState(pos).is(org.slavicmyths.registry.ModBlocks.PATH_STONE.get())){
                state.remove(marker.id());NavigationManager.changed(p);
            }
        }
    }
    private static void discoverYaga(ServerPlayer p){
        if(p.level().dimension()!=Level.OVERWORLD)return;var home=YagaData.get(p.serverLevel());
        if(home.placed&&home.anchor!=null&&home.anchor.distSqr(p.blockPosition())<=64*64)
            NavigationManager.reveal(p,"yaga_hut:world",MarkerCategory.YAGA,SlavicMarker.Kind.PERMANENT_DISCOVERY,
                "navigation.slavicmyths.yaga_hut",Level.OVERWORLD.location(),home.anchor,true,false);
    }
    /** Fixed 13x13 loaded-chunk window at most once / 40 ticks AND after 8-block movement.
     * Reads starts already present in chunks, never locate(), generation or SavedData world inventories. */
    public static void discoverStructures(ServerPlayer p,boolean explicit){
        ServerLevel w=p.serverLevel();var registry=w.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ChunkPos center=p.chunkPosition();Set<net.minecraft.world.level.levelgen.structure.StructureStart> seen=new HashSet<>();
        boolean saved=false;
        Map<net.minecraft.world.level.levelgen.structure.Structure,Map<ChunkPos,net.minecraft.world.level.levelgen.structure.StructureStart>> candidates=new HashMap<>();
        Set<Long> visitedReferences=new HashSet<>();
        for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++){
            var chunk=w.getChunkSource().getChunk(center.x+dx,center.z+dz,ChunkStatus.FULL,false);if(chunk==null)continue;
            chunk.getAllStarts().forEach((structure,start)->candidates.computeIfAbsent(structure,k->new HashMap<>()).put(start.getChunkPos(),start));
            // Near the far edge of a large dungeon its start chunk may be outside this window.
            // Follow only references already in loaded chunks; cap extra lookups and never force load.
            for(var refs:chunk.getAllReferences().entrySet()){
                var id=registry.getKey(refs.getKey());if(id==null||!id.getNamespace().equals("slavicmyths"))continue;
                for(long ref:refs.getValue()){
                    if(visitedReferences.size()>=256&&!visitedReferences.contains(ref))continue;
                    if(!visitedReferences.add(ref))continue;var origin=new ChunkPos(ref);
                    var source=w.getChunkSource().getChunk(origin.x,origin.z,ChunkStatus.FULL,false);if(source==null)continue;
                    source.getAllStarts().forEach((structure,start)->candidates.computeIfAbsent(structure,k->new HashMap<>()).put(start.getChunkPos(),start));
                }
            }
        }
        for(var entry:candidates.entrySet())for(var start:entry.getValue().values()){
                if(!start.isValid()||!seen.add(start))continue;
                ResourceLocation id=registry.getKey(entry.getKey());if(id==null||!id.getNamespace().equals("slavicmyths"))continue;
                String path=id.getPath();boolean major=path.equals("kurgan_great")||path.equals("bandit_camp_large")||path.equals("flooded_shrine")||path.equals("abandoned_settlement");
                if(!major&&!explicit)continue;var box=start.getBoundingBox();double x=Math.max(box.minX(),Math.min(p.getX(),box.maxX())),z=Math.max(box.minZ(),Math.min(p.getZ(),box.maxZ()));
                double y=Math.max(box.minY(),Math.min(p.getY(),box.maxY()));int radius=major?96:path.contains("medium")||path.contains("warrior")?64:48;
                if(Math.pow(p.getX()-x,2)+Math.pow(p.getY()-y,2)+Math.pow(p.getZ()-z,2)>radius*radius)continue;
                MarkerCategory category=path.startsWith("kurgan_")?MarkerCategory.KURGAN:path.startsWith("bandit_")?MarkerCategory.BANDIT:MarkerCategory.SPECIAL_LOCATION;
                BlockPos pos=new BlockPos((box.minX()+box.maxX())/2,box.maxY()+1,(box.minZ()+box.maxZ())/2);
                saved|=NavigationManager.reveal(p,"structure:"+w.dimension().location()+":"+id+":"+start.getChunkPos().toLong(),category,
                    SlavicMarker.Kind.PERMANENT_DISCOVERY,"navigation.slavicmyths.structure."+path,w.dimension().location(),pos,!explicit,false);
        }
        if(explicit&&!saved)p.displayClientMessage(net.minecraft.network.chat.Component.translatable("navigation.slavicmyths.no_new_location"),false);
    }
    @SubscribeEvent public static void blockRemoved(BlockEvent.BreakEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||!e.getState().is(org.slavicmyths.registry.ModBlocks.PATH_STONE.get()))return;
        // Delayed loaded-block validation above handles actual removal, cancellation, explosions and pistons.
        probes.remove(p.getUUID());
    }
    private NavigationEvents(){}
}
