package org.slavicmyths.serpenthill;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;
public final class HillWorld extends SavedData {
 private CompoundTag root=new CompoundTag();private final Map<Long,Set<UUID>> index=new HashMap<>();private final Set<UUID> jobs=new LinkedHashSet<>();
 public static HillWorld get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(HillWorld::new,HillWorld::load),"slavicmyths_serpent_hill195");}
 public CompoundTag all(){if(!root.contains("Hills",10))root.put("Hills",new CompoundTag());return root.getCompound("Hills");}
 public CompoundTag row(UUID id){var all=all();if(!all.contains(id.toString(),10))all.put(id.toString(),new CompoundTag());return all.getCompound(id.toString());}
 public void ensure(UUID id,BlockPos origin,Rotation rotation,long seed){var n=row(id);if(n.contains("Origin"))return;n.putLong("Origin",origin.asLong());n.putString("Rotation",rotation.name());n.putLong("Seed",seed);index(id,n);setDirty();}
 private void index(UUID id,CompoundTag n){var b=HillGeometry.bounds(BlockPos.of(n.getLong("Origin")),Rotation.valueOf(n.getString("Rotation")));for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)index.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),key->new HashSet<>()).add(id);}
 public Set<UUID> near(BlockPos at){return index.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4),Set.of());}
 public Set<UUID> jobs(){return jobs;}
 public void enqueue(UUID id){row(id).putBoolean("Building",true);row(id).putInt("Cursor",0);jobs.add(id);setDirty();}
 public void finish(UUID id){row(id).putBoolean("Building",false);jobs.remove(id);setDirty();}
 public static HillWorld load(CompoundTag n,HolderLookup.Provider registry){var d=new HillWorld();d.root=n.copy();for(String key:d.all().getAllKeys()){UUID id=UUID.fromString(key);var row=d.all().getCompound(key);d.index(id,row);if(row.getBoolean("Building"))d.jobs.add(id);}return d;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registry){return root.copy();}
}
