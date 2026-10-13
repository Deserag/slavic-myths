package org.slavicmyths.kurgan;
import net.minecraft.world.level.ChunkPos;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** Existing world data extended with versioned instances; legacy opened bits are preserved. */
public final class BurialRecords extends SavedData {
 private final Set<UUID> opened=new HashSet<>();
 public final Map<UUID,KurganInstance> instances=new LinkedHashMap<>();
 public final Map<UUID,Set<UUID>> persistentCurses=new HashMap<>();
 private final Map<Long,List<UUID>> byChunk=new HashMap<>();
 public BurialRecords(){super();}
 public static BurialRecords get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(BurialRecords::new,(tag,registries)->{BurialRecords data=new BurialRecords();data.load(tag);return data;}),"slavicmyths_burials");}
 public void opened(UUID id){if(opened.add(id))setDirty();}
 public boolean burialOpened(UUID id){return opened.contains(id);}
 /** Return players whose last curse source was removed; offline data is handled too. */
 public Set<UUID> clearCurseSource(UUID source){Set<UUID> freed=new HashSet<>();for(UUID player:new HashSet<>(persistentCurses.keySet())){Set<UUID> ids=persistentCurses.get(player);if(ids.remove(source)){if(ids.isEmpty()){persistentCurses.remove(player);freed.add(player);}setDirty();}}return freed;}
 public void register(KurganInstance i){if(instances.containsKey(i.id))return;for(var r:i.plan.rooms)i.encounters.computeIfAbsent(r.id,key->new KurganEncounterState()).prepare(KurganRoster.roster(i,r));instances.put(i.id,i);index(i);setDirty();}
 private void index(KurganInstance i){KurganPlan.Box b=i.plan.bounds();for(int x=(b.x0+i.origin.getX())>>4;x<=(b.x1+i.origin.getX())>>4;x++)for(int z=(b.z0+i.origin.getZ())>>4;z<=(b.z1+i.origin.getZ())>>4;z++)byChunk.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),k->new ArrayList<>()).add(i.id);}
 /** Spatial metadata lookup; no chunk requests and no scan of every recorded dungeon. */
 public List<KurganInstance> overlapping(net.minecraft.world.level.levelgen.structure.BoundingBox box){Set<UUID> ids=new LinkedHashSet<>();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)ids.addAll(byChunk.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(x,z),Collections.emptyList()));var result=new ArrayList<KurganInstance>();for(UUID id:ids){var instance=instances.get(id);if(instance!=null)result.add(instance);}return result;}
 public KurganInstance at(net.minecraft.core.BlockPos p){for(UUID id:byChunk.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(p.getX()>>4,p.getZ()>>4),Collections.emptyList())){KurganInstance i=instances.get(id);if(i.contains(p))return i;}return null;}
 public void load(CompoundTag n){opened.clear();instances.clear();byChunk.clear();persistentCurses.clear();for(Tag v:n.getList("Opened",8))try{opened.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}
  for(Tag v:n.getList("Kurgans",10)){KurganInstance i=KurganInstance.load((CompoundTag)v);instances.put(i.id,i);index(i);}for(Tag v:n.getList("Curses",10)){CompoundTag t=(CompoundTag)v;Set<UUID> ids=new HashSet<>();for(Tag s:t.getList("Sources",8))ids.add(UUID.fromString(s.getAsString()));persistentCurses.put(t.getUUID("Player"),ids);}}
 public CompoundTag save(CompoundTag n){ListTag list=new ListTag();for(UUID id:opened)list.add(StringTag.valueOf(id.toString()));n.put("Opened",list);ListTag all=new ListTag();for(KurganInstance i:instances.values())all.add(i.save());n.put("Kurgans",all);ListTag curses=new ListTag();for(Map.Entry<UUID,Set<UUID>> e:persistentCurses.entrySet()){CompoundTag t=new CompoundTag();t.putUUID("Player",e.getKey());ListTag ids=new ListTag();for(UUID id:e.getValue())ids.add(StringTag.valueOf(id.toString()));t.put("Sources",ids);curses.add(t);}n.put("Curses",curses);return n;}
 @Override public CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){return save(tag);}
}
