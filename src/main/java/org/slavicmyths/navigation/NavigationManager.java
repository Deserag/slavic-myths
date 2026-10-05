package org.slavicmyths.navigation;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.slavicmyths.hunt.*;
import org.slavicmyths.yaga.*;

public final class NavigationManager {
    public static void changed(ServerPlayer p){NavigationRecords.get(p.serverLevel()).setDirty();NavigationNetwork.sync(p,false);}
    public static boolean reveal(ServerPlayer p,String source,MarkerCategory category,SlavicMarker.Kind kind,
                                 String name,ResourceLocation dimension,BlockPos pos,boolean automatic,boolean track){
        NavigationRecords data=NavigationRecords.get(p.serverLevel());NavigationState state=data.player(p.getUUID());
        if(automatic&&!state.preferences.contains(NavigationState.Preference.AUTO_DISCOVERY))return false;
        UUID id=SlavicMarker.identity(source);SlavicMarker previous=state.markers.get(id);
        var marker=new SlavicMarker(id,source,category,kind,SlavicMarker.Lifetime.PERMANENT,name,dimension,
            pos.getX(),pos.getY(),pos.getZ(),previous==null?p.serverLevel().getGameTime():previous.createdAt(),previous==null?0:previous.revision(),null);
        boolean update=state.upsert(marker);if(track)update|=state.track(id);if(update)changed(p);return update;
    }
    public static boolean thread(ServerPlayer p){
        ServerLevel world=p.server.getLevel(Level.OVERWORLD);YagaData home=YagaData.get(world);
        if(!home.placed||home.anchor==null){p.displayClientMessage(Component.translatable("navigation.slavicmyths.thread_unresolved"),false);return false;}
        var state=NavigationRecords.get(world).player(p.getUUID());
        reveal(p,"yaga_hut:world",MarkerCategory.YAGA,SlavicMarker.Kind.PERMANENT_DISCOVERY,"navigation.slavicmyths.yaga_hut",
            Level.OVERWORLD.location(),home.anchor,false,state.preferences.contains(NavigationState.Preference.AUTO_THREAD));return true;
    }
    public static void waystone(ServerPlayer p,BlockPos pos){
        reveal(p,"waystone:"+p.level().dimension().location()+":"+pos.asLong(),MarkerCategory.WAYSTONE,
            SlavicMarker.Kind.TRAVEL_POINT,"block.slavicmyths.path_stone",p.level().dimension().location(),pos,false,false);
    }
    public static String type(Entity entity){
        var key=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if(!key.getNamespace().equals("slavicmyths")||!Set.of("ovinnik","volkolak","fire_serpent","podvey","likho_one_eyed","tugarin_zmey").contains(key.getPath()))return null;
        if(entity instanceof WorldBoss boss)return boss.kind().id;
        if(entity instanceof HuntMob mob&&mob.huntTarget()!=null)return mob.huntTarget().id;
        return null;
    }
    public static void remember(Entity entity){
        if(!(entity.level() instanceof ServerLevel level)||!entity.isAlive())return;
        String type=type(entity);if(type==null)return;
        HuntMob mob=(HuntMob)entity;
        var data=NavigationRecords.get(level);UUID owner=mob instanceof WorldBoss boss?boss.bossOwner:mob.owner;
        var encounter=new NavigationRecords.Encounter(entity.getUUID(),level.dimension().location(),
            (mob.home==null?entity.blockPosition():mob.home).immutable(),type,owner);
        if(!encounter.equals(data.encounters.put(entity.getUUID(),encounter)))data.setDirty();
    }
    private static String contract(ServerPlayer p){
        YagaData.Progress progress=YagaData.get(p.server.getLevel(Level.OVERWORLD)).progress(p.getUUID());
        return progress.active.startsWith("contract:")?progress.active:"";
    }
    private static String contractType(ServerPlayer p){
        var progress=YagaData.get(p.server.getLevel(Level.OVERWORLD)).progress(p.getUUID());
        int kind=YagaServices.POOL[progress.contract];return new String[]{"ovinnik","volkolak","fire_serpent","podvey","likho_one_eyed","tugarin_zmey"}[kind];
    }
    /** Existing contracts remain trophy deliveries. Attach guidance to an actual known encounter, never invent a lair. */
    public static void contractAccepted(ServerPlayer p){
        String quest=contract(p);if(quest.isEmpty())return;var data=NavigationRecords.get(p.serverLevel());
        if(data.assignmentsFor(p.getUUID()).stream().anyMatch(a->a.quest().equals(quest)))return;
        var pending=data.pendingQuests.get(p.getUUID());
        if(pending==null||!pending.quest().equals(quest)){
            pending=new NavigationRecords.PendingQuest(quest,contractType(p),UUID.randomUUID(),0,data.player(p.getUUID()).trackingChoiceRevision);
            data.pendingQuests.put(p.getUUID(),pending);data.setDirty();
        }
        if(!bindPending(p))p.displayClientMessage(Component.translatable("navigation.slavicmyths.no_known_encounter"),false);
    }
    public static boolean bindPending(ServerPlayer p){
        var data=NavigationRecords.get(p.serverLevel());var pending=data.pendingQuests.get(p.getUUID());
        if(pending==null||!pending.quest().equals(contract(p))||data.assignments.containsKey(pending.id()))return false;
        String type=pending.type();NavigationRecords.Encounter nearest=null;double distance=4096d*4096d;
        for(var encounter:data.encounters.values()){
            if(!encounter.type().equals(type)||!encounter.dimension().equals(p.level().dimension().location())||
               encounter.owner()!=null&&!encounter.owner().equals(p.getUUID()))continue;
            double d=encounter.anchor().distSqr(p.blockPosition());if(d<distance){distance=d;nearest=encounter;}
        }
        if(nearest==null)return false;var state=data.player(p.getUUID());
        boolean track=(pending.preserveTracked()||state.preferences.contains(NavigationState.Preference.AUTO_QUEST))&&pending.trackingChoiceRevision()==state.trackingChoiceRevision;
        return assign(p,nearest,pending.quest(),pending.id(),pending.revision(),track);
    }
    public static boolean assign(ServerPlayer p,NavigationRecords.Encounter encounter,String quest,UUID reuse,int revision,boolean preserveTracking){
        var data=NavigationRecords.get(p.serverLevel());var state=data.player(p.getUUID());
        UUID id=reuse==null?UUID.randomUUID():reuse;
        SearchArea.Scale scale=switch(encounter.type()){
            case "likho_one_eyed","tugarin_zmey"->SearchArea.Scale.WORLD;
            case "fire_serpent","podvey"->SearchArea.Scale.RARE;
            default->SearchArea.Scale.MINI;
        };
        ServerLevel dimension=p.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,encounter.dimension()));
        if(dimension==null)return false;
        SearchArea area=SearchArea.generate(encounter.anchor().getX()+.5,encounter.anchor().getZ()+.5,
            scale,id,dimension.getSeed()^0x451abc7528129d37L,revision,
            (x,z,r)->plausible(dimension,encounter,x,z,r));
        if(area==null){p.displayClientMessage(Component.translatable("navigation.slavicmyths.no_plausible_area"),false);return false;}
        String source="quest:"+id+":search";var marker=new SlavicMarker(id,source,MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,
            SlavicMarker.Lifetime.UNTIL_QUEST_END,"entity.slavicmyths."+encounter.type(),encounter.dimension(),
            (int)Math.floor(area.centerX()),0,(int)Math.floor(area.centerZ()),p.serverLevel().getGameTime(),revision,area);
        if(!state.upsert(marker))return false;
        data.assignments.put(id,new NavigationRecords.Assignment(p.getUUID(),id,encounter.entity(),encounter.anchor(),encounter.dimension(),encounter.type(),quest,revision));
        if(preserveTracking||reuse==null&&state.preferences.contains(NavigationState.Preference.AUTO_QUEST))state.track(id);
        changed(p);return true;
    }
    private static boolean plausible(ServerLevel level,NavigationRecords.Encounter target,double x,double z,int radius){
        var generator=level.getChunkSource().getGenerator();var noise=generator.getBiomeSource();var sampler=level.getChunkSource().randomState().sampler();
        var home=noise.getNoiseBiome(target.anchor().getX()>>2,target.anchor().getY()>>2,target.anchor().getZ()>>2,sampler);
        // Noise height queries do not request/generate chunks. Reject submerged centers and cliffs.
        int cx=(int)Math.floor(x),cz=(int)Math.floor(z);
        var randomState=level.getChunkSource().randomState();
        int centerHeight=generator.getBaseHeight(cx,cz,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,level,randomState);
        int floorHeight=generator.getBaseHeight(cx,cz,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,level,randomState);
        if(centerHeight-floorHeight>2||centerHeight<level.getMinBuildHeight()+2||centerHeight>level.getMaxBuildHeight()-4)return false;
        for(int[] offset:new int[][]{{24,0},{-24,0},{0,24},{0,-24}}){
            int height=generator.getBaseHeight(cx+offset[0],cz+offset[1],net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,level,randomState);
            if(Math.abs(height-floorHeight)>32)return false;
        }
        int compatible=0;
        for(int i=0;i<13;i++){
            double angle=(i-1)*Math.PI/6,distance=i==0?0:radius*(i<=6?.45:.85);
            int sx=(int)Math.floor(x+Math.cos(angle)*distance),sz=(int)Math.floor(z+Math.sin(angle)*distance);
            var biome=noise.getNoiseBiome(sx>>2,target.anchor().getY()>>2,sz>>2,sampler);
            boolean dry=!biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN)&&!biome.is(net.minecraft.tags.BiomeTags.IS_DEEP_OCEAN);
            boolean same=biome.equals(home);
            for(String tag:List.of("core_forest","core_plains","core_taiga","core_savanna","core_swamp")){
                var key=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/"+tag));
                same|=home.is(key)&&biome.is(key);
            }
            if(i==0&&(!dry||!level.getWorldBorder().isWithinBounds(new BlockPos(sx,target.anchor().getY(),sz))))return false;
            if(dry&&same)compatible++;
        }
        return compatible>=8;
    }
    public static void discovered(ServerPlayer p,NavigationRecords.Assignment assignment,Entity entity){
        var state=NavigationRecords.get(p.serverLevel()).player(p.getUUID());var old=state.markers.get(assignment.marker());
        if(old==null||old.area()==null)return;
        BlockPos pos=entity.blockPosition();state.upsert(new SlavicMarker(old.id(),old.source(),MarkerCategory.BOSS,
            SlavicMarker.Kind.TEMPORARY_QUEST,old.lifetime(),old.name(),old.dimension(),pos.getX(),pos.getY(),pos.getZ(),old.createdAt(),old.revision()+1,null));
        p.displayClientMessage(Component.translatable("navigation.slavicmyths.target_discovered"),false);changed(p);
    }
    public static void endQuest(ServerPlayer p,String quest,boolean completed){
        var data=NavigationRecords.get(p.serverLevel());var state=data.player(p.getUUID());boolean dirty=false,wasTracked=false;
        for(var a:data.assignmentsFor(p.getUUID()))if(a.quest().equals(quest)){
            wasTracked|=a.marker().equals(state.tracked);dirty|=state.end(a.marker(),completed);data.assignments.remove(a.marker());
        }
        if(wasTracked)p.displayClientMessage(Component.translatable("navigation.slavicmyths.tracking_ended"),false);
        if(wasTracked)state.trackingEnded=false;
        var pending=data.pendingQuests.get(p.getUUID());if(pending!=null&&pending.quest().equals(quest)){data.pendingQuests.remove(p.getUUID());dirty=true;}
        if(dirty)changed(p);
    }
    public static void request(ServerPlayer p,NavigationNetwork.Request request){
        if(!p.isAlive()||p.isSpectator())return;var data=NavigationRecords.get(p.serverLevel());var state=data.player(p.getUUID());
        long now=p.serverLevel().getGameTime();if(state.requestWindow<0||now-state.requestWindow>=20){state.requestWindow=now;state.requests=0;}
        if(++state.requests>12)return;boolean update=false;UUID id=request.marker();
        switch(request.action()){
            case OPEN->{NavigationNetwork.sync(p,true);return;}
            case TRACK->{if(state.markers.containsKey(id)){state.trackingChoiceRevision++;update=state.track(id);data.setDirty();}}
            case STOP->{state.trackingChoiceRevision++;update=state.track(null);data.setDirty();}
            case HIDE->{if(state.markers.containsKey(id))update=request.enabled()?state.hidden.add(id):state.hidden.remove(id);}
            case FORGET->{var marker=state.markers.get(id);if(marker!=null&&marker.permanent())update=state.remove(id);}
            case CATEGORY->{if(request.option()>=0&&request.option()<MarkerCategory.values().length){var category=MarkerCategory.values()[request.option()];update=request.enabled()?state.categories.add(category):state.categories.remove(category);}}
            case PREFERENCE->{if(request.option()>=0&&request.option()<NavigationState.Preference.values().length){var pref=NavigationState.Preference.values()[request.option()];update=request.enabled()?state.preferences.add(pref):state.preferences.remove(pref);}}
            case SAVE_NEARBY->{NavigationEvents.discoverStructures(p,true);return;}
            case CLEAR_OWNED->{if(p.hasPermissions(2)){for(UUID marker:List.copyOf(state.markers.keySet()))state.remove(marker);state.history.clear();data.assignments.values().removeIf(a->a.player().equals(p.getUUID()));data.pendingQuests.remove(p.getUUID());update=true;}}
        }
        if(update)changed(p);
    }
    private NavigationManager(){}
}
