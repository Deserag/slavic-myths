package org.slavicmyths.depth;
import java.util.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
public final class PoolIndex extends SavedData {
 private final Set<Long> pools=new HashSet<>(),defeated=new HashSet<>();
 public PoolIndex(){super();}
 public static PoolIndex get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PoolIndex::new,(tag,registries)->{PoolIndex data=new PoolIndex();data.load(tag);return data;}),"slavic_depth_pools");}
 public void record(BlockPos p){if(pools.size()<4096&&pools.add(p.asLong()))setDirty();}
 public void defeat(BlockPos p){defeated.add(p.asLong());setDirty();}
 public boolean defeated(BlockPos p){return defeated.contains(p.asLong());}
 public BlockPos nearest(BlockPos from){return pools.stream().filter(p->!defeated.contains(p)).map(BlockPos::of).min(Comparator.comparingDouble(from::distSqr)).orElse(null);}
 public void load(CompoundTag n){pools.clear();defeated.clear();for(long p:n.getLongArray("Pools"))pools.add(p);for(long p:n.getLongArray("Defeated"))defeated.add(p);}
 public CompoundTag save(CompoundTag n){n.putLongArray("Pools",pools.stream().mapToLong(Long::longValue).toArray());n.putLongArray("Defeated",defeated.stream().mapToLong(Long::longValue).toArray());return n;}
 @Override public CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){return save(tag);}
}
