package org.slavicmyths.burningislands;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Addressed encounter journal. Corrupt/future NBT is kept verbatim and never
 * permits a fresh encounter or second boss reward. Server-thread callers only. */
public final class MagmaEncounters extends SavedData {
 public static final int VERSION=1;
 public record Entry(UUID boss,boolean defeated){}
 private final Map<Long,Entry> entries=new HashMap<>();
 private CompoundTag readOnlySnapshot;
 public static MagmaEncounters get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(MagmaEncounters::new,MagmaEncounters::load),"slavicmyths_magma_encounters");}
 public boolean isReadOnly(){return readOnlySnapshot!=null;}
 public boolean register(long anchor,UUID boss){
  if(isReadOnly()||boss==null)return false;
  var old=entries.get(anchor);
  if(old!=null)return old.boss.equals(boss)&&!old.defeated;
  if(entries.values().stream().anyMatch(e->e.boss.equals(boss)))return false;
  entries.put(anchor,new Entry(boss,false));setDirty();return true;
 }
 public boolean claim(long anchor,UUID boss){
  if(isReadOnly()||boss==null)return false;
  var old=entries.get(anchor);
  if(old==null||!old.boss.equals(boss)||old.defeated)return false;
  entries.put(anchor,new Entry(boss,true));setDirty();return true;
 }
 public static MagmaEncounters load(CompoundTag tag,HolderLookup.Provider lookup){
  var data=new MagmaEncounters();
  try {
   // Existing worlds have no Version tag; accept that legacy shape.
   if(tag.contains("Version")&&(!tag.contains("Version",Tag.TAG_INT)||tag.getInt("Version")!=VERSION))
    throw new IllegalArgumentException("Unknown magma journal version");
   if(!tag.contains("Encounters",Tag.TAG_LIST))throw new IllegalArgumentException("Missing encounters");
   var raw=(ListTag)tag.get("Encounters");
   var list=tag.getList("Encounters",Tag.TAG_COMPOUND);
   if(raw.size()!=list.size())throw new IllegalArgumentException("Mixed encounter tags");
   var bosses=new HashSet<UUID>();
   for(Tag element:list){
    var e=(CompoundTag)element;
    if(!e.contains("Anchor",Tag.TAG_LONG)||!e.hasUUID("Boss")||!e.contains("Defeated",Tag.TAG_BYTE))
     throw new IllegalArgumentException("Invalid magma encounter");
    long anchor=e.getLong("Anchor");
    UUID boss=e.getUUID("Boss");
    if(!bosses.add(boss)||data.entries.putIfAbsent(anchor,new Entry(boss,e.getBoolean("Defeated")))!=null)
     throw new IllegalArgumentException("Duplicate magma encounter");
   }
  } catch(IllegalArgumentException|ClassCastException ex){
   data.entries.clear();data.readOnlySnapshot=tag.copy();
  }
  data.setDirty(false);
  return data;
 }
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){
  if(isReadOnly())return readOnlySnapshot.copy();
  tag.putInt("Version",VERSION);
  var list=new ListTag();
  entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->{
   var e=new CompoundTag();e.putLong("Anchor",entry.getKey());
   e.putUUID("Boss",entry.getValue().boss);e.putBoolean("Defeated",entry.getValue().defeated);list.add(e);
  });
  tag.put("Encounters",list);return tag;
 }
}
