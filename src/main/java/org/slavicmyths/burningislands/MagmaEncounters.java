package org.slavicmyths.burningislands;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Encounter authority plus recoverable recipient/amount. Never trust a second death hook. */
public final class MagmaEncounters extends SavedData {
 public static final int VERSION=2;
 public record Entry(UUID boss,boolean defeated,UUID recipient,int cores,long readyAt){}
 public record Delivery(long anchor,int cores,long readyAt){}
 private final Map<Long,Entry> entries=new HashMap<>();
 private final Set<UUID> bossIds=new HashSet<>();
 private final Map<UUID,Set<Long>> recipients=new HashMap<>();
 private CompoundTag readOnlySnapshot;
 public static MagmaEncounters get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(MagmaEncounters::new,MagmaEncounters::load),"slavicmyths_magma_encounters");}
 public boolean isReadOnly(){return readOnlySnapshot!=null;}
 public boolean register(long anchor,UUID boss){
  if(isReadOnly()||boss==null)return false;
  var old=entries.get(anchor);if(old!=null)return old.boss.equals(boss)&&!old.defeated;
  if(!bossIds.add(boss))return false;
  entries.put(anchor,new Entry(boss,false,null,0,0));setDirty();return true;
 }
 public boolean canClaim(long anchor,UUID boss){var old=entries.get(anchor);return !isReadOnly()&&old!=null&&old.boss.equals(boss)&&!old.defeated;}
 public boolean claim(long anchor,UUID boss,UUID recipient,int cores,long readyAt){
  if(isReadOnly()||boss==null||recipient==null||cores<4||cores>6||readyAt<0)return false;
  var old=entries.get(anchor);if(old==null||!old.boss.equals(boss)||old.defeated)return false;
  entries.put(anchor,new Entry(boss,true,recipient,cores,readyAt));recipients.computeIfAbsent(recipient,k->new HashSet<>()).add(anchor);setDirty();return true;
 }
 /** Indexed per recipient, not a world/entity scan. Delivered records stay for crash reconciliation. */
 public List<Delivery> deliveries(UUID recipient,long now){
  var result=new ArrayList<Delivery>();for(long anchor:recipients.getOrDefault(recipient,Set.of())){var e=entries.get(anchor);if(now>=e.readyAt)result.add(new Delivery(anchor,e.cores,e.readyAt));}
  result.sort(Comparator.comparingLong(Delivery::anchor));return result;
 }
 public static MagmaEncounters load(CompoundTag tag,HolderLookup.Provider lookup){
  var data=new MagmaEncounters();
  try {
   if(!tag.contains("Version",Tag.TAG_INT)||tag.getInt("Version")!=VERSION||!tag.contains("Encounters",Tag.TAG_LIST))throw new IllegalArgumentException("Unknown magma journal");
   var raw=(ListTag)tag.get("Encounters");var list=tag.getList("Encounters",Tag.TAG_COMPOUND);if(raw.size()!=list.size())throw new IllegalArgumentException("Mixed encounter tags");
   for(Tag value:list){var e=(CompoundTag)value;
    if(!e.contains("Anchor",Tag.TAG_LONG)||!e.hasUUID("Boss")||!e.contains("Defeated",Tag.TAG_BYTE))throw new IllegalArgumentException("Malformed encounter");
    long anchor=e.getLong("Anchor");UUID boss=e.getUUID("Boss");boolean dead=e.getBoolean("Defeated");
    UUID recipient=dead&&e.hasUUID("Recipient")?e.getUUID("Recipient"):null;int cores=dead?e.getInt("Cores"):0;long ready=dead?e.getLong("ReadyAt"):0;
    if(dead&&(recipient==null||!e.contains("Cores",Tag.TAG_INT)||cores<4||cores>6||!e.contains("ReadyAt",Tag.TAG_LONG)||ready<0))throw new IllegalArgumentException("Missing reward recovery data");
    if(!data.bossIds.add(boss)||data.entries.putIfAbsent(anchor,new Entry(boss,dead,recipient,cores,ready))!=null)throw new IllegalArgumentException("Duplicate encounter");
    if(dead)data.recipients.computeIfAbsent(recipient,k->new HashSet<>()).add(anchor);
   }
  }catch(IllegalArgumentException|ClassCastException ex){data.entries.clear();data.bossIds.clear();data.recipients.clear();data.readOnlySnapshot=tag.copy();}
  data.setDirty(false);return data;
 }
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){
  if(isReadOnly())return readOnlySnapshot.copy();tag.putInt("Version",VERSION);var list=new ListTag();
  entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(row->{var e=new CompoundTag();var v=row.getValue();e.putLong("Anchor",row.getKey());e.putUUID("Boss",v.boss);e.putBoolean("Defeated",v.defeated);if(v.defeated){e.putUUID("Recipient",v.recipient);e.putInt("Cores",v.cores);e.putLong("ReadyAt",v.readyAt);}list.add(e);});tag.put("Encounters",list);return tag;
 }
}
