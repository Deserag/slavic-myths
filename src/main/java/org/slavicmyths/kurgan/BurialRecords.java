package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
/** Existing world data extended with versioned instances; legacy opened bits are preserved. */
public final class BurialRecords extends WorldSavedData {
 private final Set<UUID> opened=new HashSet<>();
 public final Map<UUID,KurganInstance> instances=new LinkedHashMap<>();
 public final Map<UUID,Set<UUID>> persistentCurses=new HashMap<>();
 private final Map<Long,List<UUID>> byChunk=new HashMap<>();
 public BurialRecords(){super("slavicmyths_burials");}
 public static BurialRecords get(ServerWorld w){return w.getDataStorage().computeIfAbsent(BurialRecords::new,"slavicmyths_burials");}
 public void opened(UUID id){if(opened.add(id))setDirty();}
 public boolean burialOpened(UUID id){return opened.contains(id);}
 /** Return players whose last curse source was removed; offline data is handled too. */
 public Set<UUID> clearCurseSource(UUID source){Set<UUID> freed=new HashSet<>();for(UUID player:new HashSet<>(persistentCurses.keySet())){Set<UUID> ids=persistentCurses.get(player);if(ids.remove(source)){if(ids.isEmpty()){persistentCurses.remove(player);freed.add(player);}setDirty();}}return freed;}
 public void register(KurganInstance i){if(instances.containsKey(i.id))return;instances.put(i.id,i);index(i);setDirty();}
 private void index(KurganInstance i){KurganPlan.Box b=i.plan.bounds();for(int x=(b.x0+i.origin.getX())>>4;x<=(b.x1+i.origin.getX())>>4;x++)for(int z=(b.z0+i.origin.getZ())>>4;z<=(b.z1+i.origin.getZ())>>4;z++)byChunk.computeIfAbsent(net.minecraft.util.math.ChunkPos.asLong(x,z),k->new ArrayList<>()).add(i.id);}
 public KurganInstance at(net.minecraft.util.math.BlockPos p){for(UUID id:byChunk.getOrDefault(net.minecraft.util.math.ChunkPos.asLong(p.getX()>>4,p.getZ()>>4),Collections.emptyList())){KurganInstance i=instances.get(id);if(i.contains(p))return i;}return null;}
 @Override public void load(CompoundNBT n){opened.clear();instances.clear();byChunk.clear();persistentCurses.clear();for(INBT v:n.getList("Opened",8))try{opened.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}
  for(INBT v:n.getList("Kurgans",10)){KurganInstance i=KurganInstance.load((CompoundNBT)v);instances.put(i.id,i);index(i);}for(INBT v:n.getList("Curses",10)){CompoundNBT t=(CompoundNBT)v;Set<UUID> ids=new HashSet<>();for(INBT s:t.getList("Sources",8))ids.add(UUID.fromString(s.getAsString()));persistentCurses.put(t.getUUID("Player"),ids);}}
 @Override public CompoundNBT save(CompoundNBT n){ListNBT list=new ListNBT();for(UUID id:opened)list.add(StringNBT.valueOf(id.toString()));n.put("Opened",list);ListNBT all=new ListNBT();for(KurganInstance i:instances.values())all.add(i.save());n.put("Kurgans",all);ListNBT curses=new ListNBT();for(Map.Entry<UUID,Set<UUID>> e:persistentCurses.entrySet()){CompoundNBT t=new CompoundNBT();t.putUUID("Player",e.getKey());ListNBT ids=new ListNBT();for(UUID id:e.getValue())ids.add(StringNBT.valueOf(id.toString()));t.put("Sources",ids);curses.add(t);}n.put("Curses",curses);return n;}
}
