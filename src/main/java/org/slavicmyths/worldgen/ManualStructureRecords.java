package org.slavicmyths.worldgen;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** Completed manual structures are locatable after saving and reloading, without native structure sets. */
public final class ManualStructureRecords extends SavedData {
 public record Entry(UUID id,String family,int tier,BlockPos arrival,net.minecraft.world.level.levelgen.structure.BoundingBox bounds){}
 private final Map<UUID,Entry> entries=new LinkedHashMap<>();private final Map<Long,List<UUID>> byChunk=new HashMap<>();
 public static ManualStructureRecords get(ServerLevel world){return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(ManualStructureRecords::new,(tag,registries)->load(tag)),"slavicmyths_manual_structures");}
 public void add(UUID id,String family,int tier,BlockPos arrival,net.minecraft.world.level.levelgen.structure.BoundingBox bounds){entries.put(id,new Entry(id,family,tier,arrival,bounds));index(entries.get(id));setDirty();}
 private void index(Entry entry){var box=entry.bounds();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)byChunk.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),k->new ArrayList<>()).add(entry.id());}
 public List<Entry> overlapping(net.minecraft.world.level.levelgen.structure.BoundingBox box){var ids=new LinkedHashSet<UUID>();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)ids.addAll(byChunk.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(x,z),List.of()));var result=new ArrayList<Entry>();for(var id:ids){var entry=entries.get(id);if(entry!=null&&entry.bounds().intersects(box))result.add(entry);}return result;}
 public List<Entry> entries(){return List.copyOf(entries.values());}
 public static ManualStructureRecords load(CompoundTag tag){var data=new ManualStructureRecords();for(Tag raw:tag.getList("Structures",Tag.TAG_COMPOUND)){var n=(CompoundTag)raw;int tier=n.getInt("Tier");String family=n.getString("Family");if(n.hasUUID("Id")&&tier>=0&&tier<=2&&(family.equals("kurgan")||family.equals("bandit_camp"))){var arrival=BlockPos.of(n.getLong("Arrival"));int[] box=n.getIntArray("Bounds");var bounds=box.length==6?new net.minecraft.world.level.levelgen.structure.BoundingBox(box[0],box[1],box[2],box[3],box[4],box[5]):family.equals("kurgan")?new net.minecraft.world.level.levelgen.structure.BoundingBox(arrival.getX()-128,arrival.getY()-128,arrival.getZ()-256,arrival.getX()+128,arrival.getY()+32,arrival.getZ()+64):new net.minecraft.world.level.levelgen.structure.BoundingBox(arrival.getX()-(tier==2?39:10),arrival.getY()-6,arrival.getZ()-2,arrival.getX()-(tier==2?39:10)+(tier==0?28:tier==1?46:84),arrival.getY()+32,arrival.getZ()-2+(tier==0?28:tier==1?46:84));var entry=new Entry(n.getUUID("Id"),family,tier,arrival,bounds);data.entries.put(entry.id(),entry);}}for(var entry:data.entries.values())data.index(entry);return data;}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){var list=new ListTag();for(var entry:entries.values()){var n=new CompoundTag();n.putUUID("Id",entry.id());n.putString("Family",entry.family());n.putInt("Tier",entry.tier());n.putLong("Arrival",entry.arrival().asLong());var b=entry.bounds();n.putIntArray("Bounds",new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()});list.add(n);}tag.put("Structures",list);return tag;}
}
