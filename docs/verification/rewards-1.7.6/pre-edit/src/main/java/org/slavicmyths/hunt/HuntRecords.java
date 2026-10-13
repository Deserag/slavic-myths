package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Owner-indexed finite hunts; an unloaded UUID remains ACTIVE, never a respawn trigger. */
public final class HuntRecords extends SavedData {
 public enum Status{ACTIVE,CLEARED,FAILED}
 public static final class Hunt{
  public UUID target;public BlockPos anchor;public boolean oven;public HuntTarget kind;public Status status=Status.ACTIVE;public long ready;
  public Hunt(UUID target,BlockPos anchor,boolean oven,long ready){this.target=target;this.anchor=anchor.immutable();this.oven=oven;this.kind=oven?HuntTarget.OVINNIK:HuntTarget.VOLKOLAK;this.ready=ready;}
  public Hunt(UUID target,BlockPos anchor,HuntTarget kind,long ready){this(target,anchor,kind==HuntTarget.OVINNIK,ready);this.kind=kind;}
 }
 public static final class Boss {
  public String key;public BossKind kind;public BlockPos anchor;public UUID target,owner;public boolean natural,active,safe;public long ready;
  public Boss(String key,BossKind kind,BlockPos p,boolean natural){this.key=key;this.kind=kind;anchor=p.immutable();this.natural=natural;}
 }
 public final Map<String,Boss> bosses=new HashMap<>();public final Set<String> bossRegions=new HashSet<>();public final Map<UUID,String> bossOwners=new HashMap<>();
 public boolean bossActive(UUID owner){Boss b=bosses.get(bossOwners.get(owner));return b!=null&&b.active;}
 public boolean finishBoss(String key,UUID target,long now){Boss b=bosses.get(key);if(b==null||!b.active||!target.equals(b.target))return false;b.active=false;b.ready=b.natural?now+BossRules.RESPAWN:Long.MAX_VALUE;setDirty();return true;}
 private void saveBossData(CompoundTag n){ListTag all=new ListTag();for(Boss b:bosses.values()){CompoundTag t=new CompoundTag();t.putString("Key",b.key);t.putString("Kind",b.kind.id);t.putLong("Anchor",b.anchor.asLong());if(b.target!=null)t.putUUID("Target",b.target);if(b.owner!=null)t.putUUID("Owner",b.owner);t.putBoolean("Natural",b.natural);t.putBoolean("Active",b.active);t.putBoolean("Safe",b.safe);t.putLong("Ready",b.ready);all.add(t);}n.put("BossAnchors",all);ListTag regions=new ListTag();for(String key:bossRegions)regions.add(StringTag.valueOf(key));n.put("BossRegions",regions);}
 private void loadBossData(CompoundTag n){bosses.clear();bossRegions.clear();bossOwners.clear();for(Tag raw:n.getList("BossAnchors",10)){CompoundTag t=(CompoundTag)raw;Boss b=new Boss(t.getString("Key"),BossKind.read(t.getString("Kind")),BlockPos.of(t.getLong("Anchor")),t.getBoolean("Natural"));b.target=t.hasUUID("Target")?t.getUUID("Target"):null;b.owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;b.active=t.getBoolean("Active")&&b.target!=null;b.safe=t.getBoolean("Safe");b.ready=t.getLong("Ready");bosses.put(b.key,b);if(b.owner!=null)bossOwners.put(b.owner,b.key);}for(Tag raw:n.getList("BossRegions",8))bossRegions.add(raw.getAsString());}
 public final Map<UUID,Hunt> hunts=new HashMap<>();
 public HuntRecords(){super();}
 public static HuntRecords get(ServerLevel world){return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(HuntRecords::new,(tag,registries)->{HuntRecords data=new HuntRecords();data.load(tag);return data;}),"slavicmyths_hunts");}
 public boolean active(UUID player){Hunt h=hunts.get(player);return h!=null&&h.status==Status.ACTIVE;}
 public boolean finish(UUID player,UUID mob,Status status){Hunt h=hunts.get(player);if(h==null||!h.target.equals(mob)||h.status!=Status.ACTIVE)return false;h.status=status;setDirty();return true;}
 public CompoundTag save(CompoundTag n){ListTag list=new ListTag();for(Map.Entry<UUID,Hunt> e:hunts.entrySet()){Hunt h=e.getValue();CompoundTag t=new CompoundTag();t.putUUID("Player",e.getKey());t.putUUID("Target",h.target);t.putLong("Anchor",h.anchor.asLong());t.putBoolean("Ovinnik",h.oven);t.putString("Kind",h.kind.id);t.putInt("Status",h.status.ordinal());t.putLong("HornReady",h.ready);list.add(t);}n.put("Hunts",list);saveBossData(n);return n;}
 public void load(CompoundTag n){hunts.clear();for(Tag raw:n.getList("Hunts",10)){CompoundTag t=(CompoundTag)raw;hunts.put(t.getUUID("Player"),new Hunt(t.getUUID("Target"),BlockPos.of(t.getLong("Anchor")),HuntTarget.read(t,"Kind"),t.getLong("HornReady")));hunts.get(t.getUUID("Player")).status=Status.values()[Math.max(0,Math.min(2,t.getInt("Status")))];}loadBossData(n);}
 @Override public CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){return save(tag);}
}
