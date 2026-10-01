package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
/** One bit per natural barrow, written only on an actual opening. No tick handler. */
public final class BurialRecords extends WorldSavedData {
 private final Set<UUID> opened=new HashSet<>();
 public BurialRecords(){super("slavicmyths_burials");}
 public static BurialRecords get(ServerWorld w){return w.getDataStorage().computeIfAbsent(BurialRecords::new,"slavicmyths_burials");}
 public void opened(UUID id){if(opened.add(id))setDirty();}
 public boolean burialOpened(UUID id){return opened.contains(id);}
 @Override public void load(CompoundNBT n){opened.clear();for(INBT v:n.getList("Opened",8))try{opened.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}}
 @Override public CompoundNBT save(CompoundNBT n){ListNBT list=new ListNBT();for(UUID id:opened)list.add(StringNBT.valueOf(id.toString()));n.put("Opened",list);return n;}
}
