package org.slavicmyths.blackcinder;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** Addressed encounter journal. Accessed by ticking entities on the server thread only. */
public final class CinderEncounters extends SavedData {
 public record Entry(UUID boss,boolean defeated){}
 private final Map<Long,Entry> entries=new HashMap<>();
 public static CinderEncounters get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(CinderEncounters::new,CinderEncounters::load),"slavicmyths_cinder_encounters");}
 public boolean register(long anchor,UUID boss){var old=entries.get(anchor);if(old!=null)return old.boss.equals(boss)&&!old.defeated;entries.put(anchor,new Entry(boss,false));setDirty();return true;}
 public boolean claim(long anchor,UUID boss){var old=entries.get(anchor);if(old!=null&&(!old.boss.equals(boss)||old.defeated))return false;entries.put(anchor,new Entry(boss,true));setDirty();return true;}
 public static CinderEncounters load(CompoundTag tag,HolderLookup.Provider lookup){var data=new CinderEncounters();for(var element:tag.getList("Encounters",10)){var e=(CompoundTag)element;if(e.hasUUID("Boss"))data.entries.put(e.getLong("Anchor"),new Entry(e.getUUID("Boss"),e.getBoolean("Defeated")));}return data;}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){var list=new ListTag();entries.forEach((anchor,entry)->{var e=new CompoundTag();e.putLong("Anchor",anchor);e.putUUID("Boss",entry.boss);e.putBoolean("Defeated",entry.defeated);list.add(e);});tag.put("Encounters",list);return tag;}
}
