package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
final class PolkanHerd extends SavedData {
    private final Map<UUID,CompoundTag> records=new HashMap<>();
    static PolkanHerd get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PolkanHerd::new,PolkanHerd::load),"slavicmyths_polkan_herds");}
    static PolkanHerd load(CompoundTag n,HolderLookup.Provider lookup){PolkanHerd d=new PolkanHerd();CompoundTag r=n.getCompound("Herds");for(String key:r.getAllKeys())try{d.records.put(UUID.fromString(key),r.getCompound(key));}catch(IllegalArgumentException ignored){}return d;}
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){CompoundTag r=new CompoundTag();records.forEach((id,row)->r.put(id.toString(),row.copy()));n.put("Herds",r);return n;}
    void register(UUID leader,List<SavannaAnimal> guards){CompoundTag row=new CompoundTag();for(int i=0;i<guards.size();i++)row.putUUID("Guard"+i,guards.get(i).getUUID());records.put(leader,row);setDirty();}
    boolean exists(UUID leader){return records.containsKey(leader);}
    boolean ended(UUID leader){return records.containsKey(leader)&&records.get(leader).getBoolean("Ended");}
    boolean member(UUID leader,UUID guard,int slot){CompoundTag row=records.get(leader);return row!=null&&slot>=0&&slot<Polkan.HERD_SIZE&&row.hasUUID("Guard"+slot)&&row.getUUID("Guard"+slot).equals(guard);}
    void finish(UUID leader){CompoundTag row=records.get(leader);if(row!=null){row.putBoolean("Ended",true);setDirty();}}
    void dead(UUID leader,int slot){CompoundTag row=records.get(leader);if(row!=null&&slot>=0&&slot<Polkan.HERD_SIZE){row.putInt("Dead",row.getInt("Dead")|(1<<slot));setDirty();}}
    int losses(UUID leader){CompoundTag row=records.get(leader);return row==null?0:Integer.bitCount(row.getInt("Dead"));}
}