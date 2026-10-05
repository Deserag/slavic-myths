package org.slavicmyths.navigation;

import java.util.*;
import net.minecraft.nbt.*;

/** Player-owned state, independently serialized for each player; no target secrets. */
public final class NavigationState {
    public static final int VERSION=1,MAX_MARKERS=2048;
    public enum Preference { ALWAYS_TRACKED,AUTO_QUEST,AUTO_THREAD,FALLBACK_HUD,XAERO,HIDE_COMPLETED,AUTO_DISCOVERY }
    public final Map<UUID,SlavicMarker> markers=new LinkedHashMap<>();
    public final EnumSet<MarkerCategory> categories=EnumSet.allOf(MarkerCategory.class);
    public final EnumSet<Preference> preferences=EnumSet.allOf(Preference.class);
    public final Set<UUID> hidden=new HashSet<>();
    public final List<SlavicMarker> history=new ArrayList<>();
    public UUID tracked;
    public int trackingChoiceRevision;
    public boolean trackingEnded;
    public transient long requestWindow=-1,enterMessageAfter;
    public transient int requests;
    public boolean upsert(SlavicMarker marker){
        SlavicMarker previous=markers.get(marker.id());
        if(previous!=null&&previous.revision()>marker.revision())return false;
        if(previous==null&&markers.size()>=MAX_MARKERS)return false;
        if(marker.equals(previous))return false;markers.put(marker.id(),marker);return true;
    }
    public boolean visible(SlavicMarker marker){
        if(preferences.contains(Preference.ALWAYS_TRACKED)&&marker.id().equals(tracked))return true;
        return !hidden.contains(marker.id())&&categories.contains(marker.category())&&marker.kind()!=SlavicMarker.Kind.TRACKED_ONLY;
    }
    public boolean track(UUID id){if(id!=null&&!markers.containsKey(id))return false;if(Objects.equals(tracked,id))return false;tracked=id;return true;}
    public boolean remove(UUID id){SlavicMarker old=markers.remove(id);if(old==null)return false;hidden.remove(id);if(id.equals(tracked))tracked=null;return true;}
    public boolean end(UUID id,boolean completed){
        SlavicMarker old=markers.get(id);if(old==null)return false;
        if(id.equals(tracked))trackingEnded=true;
        if(old.area()!=null)old=old.withArea(old.area().withState(completed?SearchArea.State.COMPLETED:SearchArea.State.EXPIRED));
        history.add(old);while(history.size()>32)history.removeFirst();return remove(id);
    }
    public CompoundTag save(){
        CompoundTag n=new CompoundTag();n.putInt("NavigationDataVersion",VERSION);
        ListTag all=new ListTag();markers.values().forEach(m->all.add(m.save()));n.put("Markers",all);
        ListTag archive=new ListTag();history.forEach(m->archive.add(m.save()));n.put("History",archive);
        n.putInt("Categories",mask(categories));n.putInt("Preferences",mask(preferences));
        n.putInt("TrackingChoiceRevision",trackingChoiceRevision);n.putBoolean("TrackingEnded",trackingEnded);
        ListTag h=new ListTag();hidden.forEach(id->h.add(StringTag.valueOf(id.toString())));n.put("Hidden",h);
        if(tracked!=null)n.putUUID("Tracked",tracked);return n;
    }
    private static int mask(Collection<? extends Enum<?>> values){int n=0;for(var e:values)n|=1<<e.ordinal();return n;}
    public static NavigationState read(CompoundTag n){
        NavigationState state=new NavigationState();
        state.trackingChoiceRevision=Math.max(0,n.getInt("TrackingChoiceRevision"));state.trackingEnded=n.getBoolean("TrackingEnded");
        if(n.getInt("NavigationDataVersion")>VERSION)return state;
        for(Tag raw:n.getList("Markers",10))try{if(state.markers.size()<MAX_MARKERS)state.upsert(SlavicMarker.read((CompoundTag)raw));}catch(RuntimeException ignored){}
        for(Tag raw:n.getList("History",10))try{if(state.history.size()<32)state.history.add(SlavicMarker.read((CompoundTag)raw));}catch(RuntimeException ignored){}
        if(n.contains("Categories")){state.categories.clear();for(var c:MarkerCategory.values())if((n.getInt("Categories")&(1<<c.ordinal()))!=0)state.categories.add(c);}
        if(n.contains("Preferences")){state.preferences.clear();for(var p:Preference.values())if((n.getInt("Preferences")&(1<<p.ordinal()))!=0)state.preferences.add(p);}
        for(Tag raw:n.getList("Hidden",8))try{UUID id=UUID.fromString(raw.getAsString());if(state.markers.containsKey(id))state.hidden.add(id);}catch(IllegalArgumentException ignored){}
        if(n.hasUUID("Tracked")&&state.markers.containsKey(n.getUUID("Tracked")))state.tracked=n.getUUID("Tracked");
        return state;
    }
}
