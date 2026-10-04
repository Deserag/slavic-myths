package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

/** Owner-indexed finite hunts; an unloaded UUID remains ACTIVE, never a respawn trigger. */
public final class HuntRecords extends WorldSavedData {
 public enum Status{ACTIVE,CLEARED,FAILED}
 public static final class Hunt{
  public UUID target;public BlockPos anchor;public boolean oven;public HuntTarget kind;public Status status=Status.ACTIVE;public long ready;
  public Hunt(UUID target,BlockPos anchor,boolean oven,long ready){this.target=target;this.anchor=anchor.immutable();this.oven=oven;this.kind=oven?HuntTarget.OVINNIK:HuntTarget.VOLKOLAK;this.ready=ready;}
  public Hunt(UUID target,BlockPos anchor,HuntTarget kind,long ready){this(target,anchor,kind==HuntTarget.OVINNIK,ready);this.kind=kind;}
 }
 public final Map<UUID,Hunt> hunts=new HashMap<>();
 public HuntRecords(){super("slavicmyths_hunts");}
 public static HuntRecords get(ServerWorld world){return world.getDataStorage().computeIfAbsent(HuntRecords::new,"slavicmyths_hunts");}
 public boolean active(UUID player){Hunt h=hunts.get(player);return h!=null&&h.status==Status.ACTIVE;}
 public boolean finish(UUID player,UUID mob,Status status){Hunt h=hunts.get(player);if(h==null||!h.target.equals(mob)||h.status!=Status.ACTIVE)return false;h.status=status;setDirty();return true;}
 @Override public CompoundNBT save(CompoundNBT n){ListNBT list=new ListNBT();for(Map.Entry<UUID,Hunt> e:hunts.entrySet()){Hunt h=e.getValue();CompoundNBT t=new CompoundNBT();t.putUUID("Player",e.getKey());t.putUUID("Target",h.target);t.putLong("Anchor",h.anchor.asLong());t.putBoolean("Ovinnik",h.oven);t.putString("Kind",h.kind.id);t.putInt("Status",h.status.ordinal());t.putLong("HornReady",h.ready);list.add(t);}n.put("Hunts",list);return n;}
 @Override public void load(CompoundNBT n){hunts.clear();for(INBT raw:n.getList("Hunts",10)){CompoundNBT t=(CompoundNBT)raw;hunts.put(t.getUUID("Player"),new Hunt(t.getUUID("Target"),BlockPos.of(t.getLong("Anchor")),HuntTarget.read(t,"Kind"),t.getLong("HornReady")));hunts.get(t.getUUID("Player")).status=Status.values()[Math.max(0,Math.min(2,t.getInt("Status")))];}}
}
