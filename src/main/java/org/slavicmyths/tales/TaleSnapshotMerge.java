package org.slavicmyths.tales;
import java.util.*;
import net.minecraft.nbt.*;
/** Shared wire-cache merge, independent of client classes and render state. */
public final class TaleSnapshotMerge {
 public static CompoundTag merge(CompoundTag snapshot,CompoundTag delta,boolean full){
  if(full)return delta.copy();
  if(delta.getBoolean("EntriesReset"))snapshot.put("Entries",new CompoundTag());
  if(delta.getBoolean("AdvancementReset"))snapshot.put("Advancements",new ListTag());
  for(String key:delta.getAllKeys()){
   if(key.equals("Entries")){var target=snapshot.getCompound(key);for(String id:delta.getCompound(key).getAllKeys())target.put(id,delta.getCompound(key).get(id).copy());snapshot.put(key,target);}
   else if(key.equals("Advancements")){Map<String,CompoundTag> all=new TreeMap<>();for(Tag raw:snapshot.getList(key,10)){var n=(CompoundTag)raw;all.put(n.getString("Id"),n.copy());}for(Tag raw:delta.getList(key,10)){var n=(CompoundTag)raw;all.put(n.getString("Id"),n.copy());}var list=new ListTag();all.values().forEach(list::add);snapshot.put(key,list);}
   else snapshot.put(key,delta.get(key).copy());
  }
  return snapshot;
 }
 private TaleSnapshotMerge(){}
}
