package org.slavicmyths.netherboss;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Durable encounter/reward reservations. Unknown interrupted records fail closed. */
public final class NetherBossLedger extends SavedData {
 public final CompoundTag records;
 public NetherBossLedger(){records=new CompoundTag();}
 private NetherBossLedger(CompoundTag root){records=root.getCompound("Records");for(String key:records.getAllKeys()){var row=records.getCompound(key);if(row.getString("State").equals("RESERVED")){row.putString("State","ENDED");setDirty();}}}
 public static NetherBossLedger get(ServerLevel world){return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(NetherBossLedger::new,(n,r)->new NetherBossLedger(n),null),"slavicmyths_nether_bosses_21x");}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){n.put("Records",records);return n;}
 public void flush(ServerLevel world){setDirty();world.getDataStorage().save();}
}
