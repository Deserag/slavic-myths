package org.slavicmyths.wolf;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
/** Bounded permanent encounter locations; no chunk tickets or player progress duplication. */
public final class WolfRecords extends SavedData {
 public final Map<UUID,BlockPos> homes=new LinkedHashMap<>();public final Set<Long> regions=new HashSet<>();
 public static WolfRecords get(ServerLevel level){return level.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new Factory<>(WolfRecords::new,WolfRecords::load),"slavicmyths_grey_wolf");}
 public static WolfRecords load(CompoundTag tag,HolderLookup.Provider lookup){var d=new WolfRecords();for(var n:tag.getList("Homes",10)){var t=(CompoundTag)n;if(t.hasUUID("Id"))d.homes.put(t.getUUID("Id"),BlockPos.of(t.getLong("Pos")));}for(long k:tag.getLongArray("Regions"))d.regions.add(k);return d;}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){var list=new ListTag();homes.forEach((id,pos)->{var n=new CompoundTag();n.putUUID("Id",id);n.putLong("Pos",pos.asLong());list.add(n);});tag.put("Homes",list);tag.putLongArray("Regions",regions.stream().mapToLong(Long::longValue).toArray());return tag;}
}
