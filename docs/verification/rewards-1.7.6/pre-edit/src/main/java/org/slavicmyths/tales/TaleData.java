package org.slavicmyths.tales;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** World-owned UUID records survive loss of the book, death and dimension travel. */
public final class TaleData extends SavedData {
    public static final int VERSION=1;
    private final Map<UUID,CompoundTag> players=new HashMap<>();
    private CompoundTag future;
    public boolean compatible(){return future==null;}
    public static TaleData get(ServerLevel level){var world=level.getServer().getLevel(Level.OVERWORLD);return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(TaleData::new,TaleData::load),"slavicmyths_tales");}
    public CompoundTag player(UUID id){return players.computeIfAbsent(id,k->{setDirty();CompoundTag n=new CompoundTag();n.putInt("Version",VERSION);n.put("Entries",new CompoundTag());n.put("Bonds",new CompoundTag());n.put("Hints",new CompoundTag());return n;});}
    public boolean contains(UUID id){return players.containsKey(id);}
    public static CompoundTag compound(CompoundTag parent,String key){if(!parent.contains(key,10))parent.put(key,new CompoundTag());return parent.getCompound(key);}
    public int stage(UUID player,String id){CompoundTag e=player(player).getCompound("Entries").getCompound(id);return Math.clamp(e.getInt("Stage"),0,4);}
    public boolean promote(UUID player,String id,int stage,long now,boolean defeated){
        if(!compatible()||TaleCatalog.entry(id)==null)return false;CompoundTag entries=compound(player(player),"Entries");CompoundTag e=compound(entries,id);int old=Math.clamp(e.getInt("Stage"),0,4);boolean changed=stage>old||defeated&&!e.getBoolean("Defeated");if(!changed)return false;
        e.putInt("Stage",Math.max(old,Math.clamp(stage,0,4)));e.putBoolean("Unread",true);if(!e.contains("SeenAt"))e.putLong("SeenAt",now);if(stage>=4&&!e.contains("VerifiedAt"))e.putLong("VerifiedAt",now);if(defeated)e.putBoolean("Defeated",true);setDirty();return true;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registries){if(future!=null)return future.copy();n.putInt("Version",VERSION);ListTag list=new ListTag();players.forEach((id,value)->{CompoundTag p=value.copy();p.putUUID("Player",id);list.add(p);});n.put("Players",list);return n;}
    public static TaleData load(CompoundTag n,HolderLookup.Provider registries){TaleData data=new TaleData();if(n.getInt("Version")>VERSION){data.future=n.copy();return data;}for(Tag raw:n.getList("Players",10)){CompoundTag p=(CompoundTag)raw;if(p.hasUUID("Player"))data.players.put(p.getUUID("Player"),p.copy());}return data;}
}
