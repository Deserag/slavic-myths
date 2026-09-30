package org.slavicmyths.depth;
import java.util.*;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
public final class PoolIndex extends WorldSavedData {
 private final Set<Long> pools=new HashSet<>(),defeated=new HashSet<>();
 public PoolIndex(){super("slavic_depth_pools");}
 public static PoolIndex get(ServerWorld w){return w.getDataStorage().computeIfAbsent(PoolIndex::new,"slavic_depth_pools");}
 public void record(BlockPos p){if(pools.size()<4096&&pools.add(p.asLong()))setDirty();}
 public void defeat(BlockPos p){defeated.add(p.asLong());setDirty();}
 public boolean defeated(BlockPos p){return defeated.contains(p.asLong());}
 public BlockPos nearest(BlockPos from){return pools.stream().filter(p->!defeated.contains(p)).map(BlockPos::of).min(Comparator.comparingDouble(from::distSqr)).orElse(null);}
 public void load(CompoundNBT n){pools.clear();defeated.clear();for(long p:n.getLongArray("Pools"))pools.add(p);for(long p:n.getLongArray("Defeated"))defeated.add(p);}
 public CompoundNBT save(CompoundNBT n){n.putLongArray("Pools",pools.stream().mapToLong(Long::longValue).toArray());n.putLongArray("Defeated",defeated.stream().mapToLong(Long::longValue).toArray());return n;}
}
