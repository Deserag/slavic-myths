package org.slavicmyths.volkolak;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** A world encounter belongs to the wolf's home, while skull issues belong to players. */
public final class VolkolakRecords extends SavedData {
 public static final class Encounter {
  public UUID id,boss;public BlockPos wolf,anchor,grave;public String stage="PENDING";public boolean placed,cinematicDone;public long escapedAt;
  public final Map<UUID,Long> participants=new HashMap<>();public final Map<UUID,UUID> issues=new HashMap<>();public final Set<UUID> returned=new HashSet<>();
  public CompoundTag terrain=new CompoundTag();
  public CompoundTag save(){var t=new CompoundTag();t.putUUID("Id",id);if(boss!=null)t.putUUID("Boss",boss);t.putLong("Wolf",wolf.asLong());t.putLong("Anchor",anchor.asLong());if(grave!=null)t.putLong("Grave",grave.asLong());t.putString("Stage",stage);t.putBoolean("Placed",placed);t.putBoolean("CinematicDone",cinematicDone);t.putLong("EscapedAt",escapedAt);t.put("Terrain",terrain.copy());var list=new ListTag();participants.forEach((p,time)->{var v=new CompoundTag();v.putUUID("Player",p);v.putLong("At",time);list.add(v);});t.put("Participants",list);var issued=new ListTag();issues.forEach((p,nonce)->{var v=new CompoundTag();v.putUUID("Player",p);v.putUUID("Nonce",nonce);issued.add(v);});t.put("Issues",issued);var handed=new ListTag();returned.forEach(p->handed.add(StringTag.valueOf(p.toString())));t.put("Returned",handed);return t;}
  public static Encounter load(CompoundTag t){var e=new Encounter();e.id=t.getUUID("Id");e.boss=t.hasUUID("Boss")?t.getUUID("Boss"):null;e.wolf=BlockPos.of(t.getLong("Wolf"));e.anchor=BlockPos.of(t.getLong("Anchor"));e.grave=t.contains("Grave")?BlockPos.of(t.getLong("Grave")):null;e.stage=t.getString("Stage");e.placed=t.getBoolean("Placed");e.cinematicDone=t.getBoolean("CinematicDone");e.escapedAt=t.getLong("EscapedAt");e.terrain=t.getCompound("Terrain");for(var v:t.getList("Participants",10)){var n=(CompoundTag)v;e.participants.put(n.getUUID("Player"),n.getLong("At"));}for(var v:t.getList("Issues",10)){var n=(CompoundTag)v;e.issues.put(n.getUUID("Player"),n.getUUID("Nonce"));}for(var v:t.getList("Returned",8))try{e.returned.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}return e;}
 }
 public final Map<UUID,Encounter> encounters=new LinkedHashMap<>();
 public final Set<Long> editedChunks=new HashSet<>();
 public static VolkolakRecords get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(VolkolakRecords::new,(t,r)->load(t)),"slavicmyths_cursed_volkolak");}
 public static VolkolakRecords load(CompoundTag tag){var d=new VolkolakRecords();for(long chunk:tag.getLongArray("EditedChunks"))d.editedChunks.add(chunk);for(var n:tag.getList("Encounters",10)){var e=Encounter.load((CompoundTag)n);d.encounters.put(e.id,e);}return d;}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){var list=new ListTag();encounters.values().forEach(e->list.add(e.save()));tag.putInt("Version",1);tag.putLongArray("EditedChunks",editedChunks.stream().mapToLong(Long::longValue).toArray());tag.put("Encounters",list);return tag;}
}
