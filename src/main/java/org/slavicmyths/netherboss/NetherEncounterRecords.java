package org.slavicmyths.netherboss;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent one-time encounter reservations. A reservation is not consumed until all entities spawn. */
public final class NetherEncounterRecords extends SavedData {
    public static final class Encounter {
        public final String key;
        public final String type;
        public BlockPos anchor;
        public boolean spawned;
        public Encounter(String key,String type,BlockPos anchor,boolean spawned){this.key=key;this.type=type;this.anchor=anchor;this.spawned=spawned;}
    }
    public final Map<String,Encounter> encounters=new LinkedHashMap<>();
    public static NetherEncounterRecords get(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(NetherEncounterRecords::new,(tag,registries)->load(tag)),"slavicmyths_nether_encounters");
    }
    private static NetherEncounterRecords load(CompoundTag tag){
        var records=new NetherEncounterRecords();
        for(Tag value:tag.getList("Encounters",Tag.TAG_COMPOUND)){
            var n=(CompoundTag)value;
            String key=n.getString("Key"),type=n.getString("Type");
            if(!key.isEmpty()&&!type.isEmpty())records.encounters.put(key,new Encounter(key,type,BlockPos.of(n.getLong("Anchor")),n.getBoolean("Spawned")));
        }
        return records;
    }
    public Encounter reserve(String key,String type,BlockPos anchor){
        Encounter old=encounters.get(key);
        if(old!=null)return old;
        Encounter next=new Encounter(key,type,anchor,false);
        encounters.put(key,next);setDirty();return next;
    }
    public void relocate(Encounter encounter,BlockPos anchor){encounter.anchor=anchor;setDirty();}
    public void markSpawned(Encounter encounter){encounter.spawned=true;setDirty();}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        ListTag list=new ListTag();
        for(Encounter e:encounters.values()){
            CompoundTag n=new CompoundTag();n.putString("Key",e.key);n.putString("Type",e.type);n.putLong("Anchor",e.anchor.asLong());n.putBoolean("Spawned",e.spawned);list.add(n);
        }
        tag.put("Encounters",list);return tag;
    }
}
