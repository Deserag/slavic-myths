package org.slavicmyths.navigation;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** SERVER ONLY: never serialize this SavedData to a packet. Only player state is public. */
public final class NavigationRecords extends SavedData {
    public record Encounter(UUID entity,ResourceLocation dimension,BlockPos anchor,String type,UUID owner){}
    public record Assignment(UUID player,UUID marker,UUID target,BlockPos anchor,ResourceLocation dimension,
                             String type,String quest,int revision){}
    public record PendingQuest(String quest,String type,UUID id,int revision,int trackingChoiceRevision,boolean preserveTracked){
        public PendingQuest(String quest,String type,UUID id,int revision,int trackingChoiceRevision){this(quest,type,id,revision,trackingChoiceRevision,false);}
    }
    public final Map<UUID,NavigationState> players=new HashMap<>();
    public final Map<UUID,Encounter> encounters=new HashMap<>();
    public final Map<UUID,Assignment> assignments=new HashMap<>();
    public final Map<UUID,PendingQuest> pendingQuests=new HashMap<>();
    private CompoundTag futureSnapshot;
    public NavigationState player(UUID id){return players.computeIfAbsent(id,k->new NavigationState());}
    /** Bounded by this player's marker limit; polling never scans other players' quests. */
    public List<Assignment> assignmentsFor(UUID id){
        NavigationState state=players.get(id);if(state==null)return List.of();
        List<Assignment> own=new ArrayList<>();for(UUID marker:state.markers.keySet()){
            Assignment a=assignments.get(marker);if(a!=null&&a.player().equals(id))own.add(a);
        }return own;
    }
    public static NavigationRecords get(ServerLevel level){ServerLevel world=level.getServer().getLevel(Level.OVERWORLD);
        return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(NavigationRecords::new,NavigationRecords::load),"slavicmyths_navigation");}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        if(futureSnapshot!=null)return futureSnapshot.copy();
        tag.putInt("NavigationDataVersion",NavigationState.VERSION);ListTag ps=new ListTag();
        players.forEach((id,state)->{CompoundTag n=state.save();n.putUUID("Player",id);ps.add(n);});tag.put("Players",ps);
        ListTag es=new ListTag();for(Encounter e:encounters.values()){CompoundTag n=new CompoundTag();n.putUUID("Entity",e.entity());
            n.putString("Dimension",e.dimension().toString());n.putLong("Anchor",e.anchor().asLong());n.putString("Type",e.type());if(e.owner()!=null)n.putUUID("Owner",e.owner());es.add(n);}tag.put("Encounters",es);
        ListTag as=new ListTag();for(Assignment a:assignments.values()){CompoundTag n=new CompoundTag();n.putUUID("Player",a.player());n.putUUID("Marker",a.marker());n.putUUID("Target",a.target());
            n.putLong("Anchor",a.anchor().asLong());n.putString("Dimension",a.dimension().toString());n.putString("Type",a.type());n.putString("Quest",a.quest());n.putInt("Revision",a.revision());as.add(n);}tag.put("Assignments",as);
        ListTag pending=new ListTag();pendingQuests.forEach((player,p)->{CompoundTag t=new CompoundTag();t.putUUID("Player",player);t.putString("Quest",p.quest());t.putString("Type",p.type());t.putUUID("Id",p.id());t.putInt("Revision",p.revision());t.putInt("TrackingChoiceRevision",p.trackingChoiceRevision());t.putBoolean("PreserveTracked",p.preserveTracked());pending.add(t);});tag.put("PendingQuests",pending);return tag;
    }
    public static NavigationRecords load(CompoundTag n,HolderLookup.Provider registries){
        NavigationRecords data=new NavigationRecords();if(n.getInt("NavigationDataVersion")>NavigationState.VERSION){data.futureSnapshot=n.copy();return data;}
        for(Tag raw:n.getList("Players",10))try{CompoundTag t=(CompoundTag)raw;data.players.put(t.getUUID("Player"),NavigationState.read(t));}catch(RuntimeException ignored){}
        for(Tag raw:n.getList("Encounters",10))try{CompoundTag t=(CompoundTag)raw;UUID id=t.getUUID("Entity");data.encounters.put(id,new Encounter(id,ResourceLocation.parse(t.getString("Dimension")),BlockPos.of(t.getLong("Anchor")),t.getString("Type"),t.hasUUID("Owner")?t.getUUID("Owner"):null));}catch(RuntimeException ignored){}
        for(Tag raw:n.getList("Assignments",10))try{CompoundTag t=(CompoundTag)raw;UUID marker=t.getUUID("Marker"),player=t.getUUID("Player");
            if(data.player(player).markers.containsKey(marker))data.assignments.put(marker,new Assignment(player,marker,t.getUUID("Target"),BlockPos.of(t.getLong("Anchor")),ResourceLocation.parse(t.getString("Dimension")),t.getString("Type"),t.getString("Quest"),Math.max(0,t.getInt("Revision"))));}catch(RuntimeException ignored){}
        for(Tag raw:n.getList("PendingQuests",10))try{CompoundTag t=(CompoundTag)raw;data.pendingQuests.put(t.getUUID("Player"),new PendingQuest(t.getString("Quest"),t.getString("Type"),t.getUUID("Id"),Math.max(0,t.getInt("Revision")),Math.max(0,t.getInt("TrackingChoiceRevision")),t.getBoolean("PreserveTracked")));}catch(RuntimeException ignored){}
        return data;
    }
}
