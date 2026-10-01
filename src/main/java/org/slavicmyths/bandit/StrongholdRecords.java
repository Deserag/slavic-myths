package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import org.slavicmyths.furniture.Furniture;
/** Loaded NPC goals request bounded zone transitions. No global tick manager or forced chunks. */
public final class StrongholdRecords extends WorldSavedData {
 public static final int TOTAL=26;
 public static final class Record {
  public long spawned,dead,spreadAt,calmAt;public boolean cleared,nightingaleSpawned,nightingaleDead;public final BlockPos[]bells=new BlockPos[5];public final int[]states=new int[5];
 }
 private final Map<UUID,Record> camps=new HashMap<>();
 public StrongholdRecords(){super("slavicmyths_strongholds");}
 public static synchronized StrongholdRecords get(ServerWorld w){return w.getDataStorage().computeIfAbsent(StrongholdRecords::new,"slavicmyths_strongholds");}
 public synchronized Record record(UUID id){return camps.computeIfAbsent(id,k->new Record());}
 public synchronized boolean nightingaleProcessed(UUID id){Record r=record(id);return r.nightingaleSpawned||r.nightingaleDead;}
 public synchronized void nightingaleSpawned(UUID id){record(id).nightingaleSpawned=true;setDirty();}
 public synchronized void nightingaleDied(UUID id){record(id).nightingaleDead=true;setDirty();}
 public synchronized void bell(UUID id,int zone,BlockPos p){record(id).bells[zone]=p;setDirty();}
 public synchronized boolean processed(UUID id,int slot){Record r=record(id);return r.cleared||((r.spawned|r.dead)&(1L<<slot))!=0;}
 public synchronized void spawned(UUID id,int slot){record(id).spawned|=1L<<slot;setDirty();}
 public synchronized void killed(BanditEntity b,Entity killer){Record r=record(b.camp);r.dead|=1L<<b.campMember;
  if(!r.cleared&&(r.dead&3)==3&&Long.bitCount(r.dead&((1L<<TOTAL)-1))>=18){r.cleared=true;Arrays.fill(r.states,0);if(killer instanceof PlayerEntity)org.slavicmyths.progression.Knowledge.award((PlayerEntity)killer,"clear_large_camp");}setDirty();
 }
 public boolean intact(ServerWorld w,BlockPos p){return p!=null&&w.hasChunkAt(p)&&w.getBlockState(p).is(Furniture.get("signal_bell"));}
 public synchronized void suspect(UUID id,int zone){Record r=record(id);if(!r.cleared&&r.states[zone]==0){r.states[zone]=1;setDirty();}}
 public synchronized void calm(UUID id,int zone){Record r=record(id);if(r.states[zone]==1){r.states[zone]=0;setDirty();}}
 public synchronized void ring(ServerWorld w,UUID id,int zone){Record r=record(id);if(r.cleared||!intact(w,r.bells[zone]))return;r.states[zone]=2;r.calmAt=w.getGameTime()+1200;r.spreadAt=w.getGameTime()+100;setDirty();w.playSound(null,r.bells[zone],net.minecraft.util.SoundEvents.ANVIL_LAND,net.minecraft.util.SoundCategory.HOSTILE,1.5F,.7F);}
 public synchronized int state(ServerWorld w,UUID id,int zone){Record r=record(id);long now=w.getGameTime();if(r.cleared)return 0;
  if(r.calmAt>0&&now>r.calmAt){Arrays.fill(r.states,0);r.calmAt=0;setDirty();}
  if(r.spreadAt>0&&now>=r.spreadAt){int[]before=r.states.clone();r.spreadAt=now+100;boolean active=false;
   for(int i=0;i<5;i++)if(before[i]==2&&intact(w,r.bells[i])){active=true;for(int j:new int[]{i-1,i+1})if(j>=0&&j<5&&before[j]!=2&&intact(w,r.bells[j])){r.states[j]=2;w.playSound(null,r.bells[j],net.minecraft.util.SoundEvents.ANVIL_LAND,net.minecraft.util.SoundCategory.HOSTILE,1,.8F);}}
   if(!active)r.spreadAt=0;setDirty();
  }return r.states[zone];
 }
 @Override public synchronized void load(CompoundNBT n){camps.clear();for(INBT raw:n.getList("Camps",10)){CompoundNBT t=(CompoundNBT)raw;if(!t.hasUUID("Id"))continue;Record r=record(t.getUUID("Id"));r.spawned=t.getLong("Spawned");r.dead=t.getLong("Dead");r.cleared=t.getBoolean("Cleared");r.nightingaleSpawned=t.getBoolean("NightingaleSpawned");r.nightingaleDead=t.getBoolean("NightingaleDead");r.spreadAt=t.getLong("Spread");r.calmAt=t.getLong("Calm");for(int i=0;i<5;i++){if(t.contains("Bell"+i))r.bells[i]=BlockPos.of(t.getLong("Bell"+i));r.states[i]=Math.max(0,Math.min(2,t.getInt("Zone"+i)));}}}
 @Override public synchronized CompoundNBT save(CompoundNBT n){ListNBT list=new ListNBT();camps.forEach((id,r)->{CompoundNBT t=new CompoundNBT();t.putUUID("Id",id);t.putLong("Spawned",r.spawned);t.putLong("Dead",r.dead);t.putBoolean("Cleared",r.cleared);t.putBoolean("NightingaleSpawned",r.nightingaleSpawned);t.putBoolean("NightingaleDead",r.nightingaleDead);t.putLong("Spread",r.spreadAt);t.putLong("Calm",r.calmAt);for(int i=0;i<5;i++){if(r.bells[i]!=null)t.putLong("Bell"+i,r.bells[i].asLong());t.putInt("Zone"+i,r.states[i]);}list.add(t);});n.put("Camps",list);return n;}
}
