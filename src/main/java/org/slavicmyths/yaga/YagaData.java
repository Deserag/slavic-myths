package org.slavicmyths.yaga;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** One world home and per-player durable progression. Death/logout cannot reset it. */
public final class YagaData extends SavedData {
 public enum Favor{STRANGER,GUEST,TRUSTED}
 public static final class Progress{
  public int intro,stage;public String active="";public long blockedUntil,contractReady,warningUntil;public int contract;
  public Favor favor(){return stage>=3?Favor.TRUSTED:stage>=1?Favor.GUEST:Favor.STRANGER;}
  public boolean rewardsVisible(){return favor()!=Favor.STRANGER;}
  public boolean blocked(long now){return now<blockedUntil;}
  public boolean accept(String quest,long now){if(blocked(now)||!active.isEmpty())return false;if(quest.equals("main:"+stage)&&stage<3||quest.equals("contract:"+contract)&&stage>=1&&now>=contractReady){active=quest;return true;}return false;}
  public boolean completeMain(int expected){if(expected<0||expected>=3||stage!=expected||!active.equals("main:"+expected))return false;stage++;active="";return true;}
  public boolean completeContract(int expected,long now){if(stage<1||expected!=contract||!active.equals("contract:"+expected)||now<contractReady||blocked(now))return false;active="";contractReady=now+24000;contract=YagaServices.nextContract(stage,contract);return true;}
  public boolean attack(long now,boolean serious){if(serious||now<warningUntil){blockedUntil=Math.max(blockedUntil,now+24000);warningUntil=0;return true;}warningUntil=now+1200;return false;}
  private CompoundTag save(){CompoundTag n=new CompoundTag();n.putInt("Intro",intro);n.putInt("Stage",stage);n.putString("Active",active);n.putLong("BlockedUntil",blockedUntil);n.putLong("ContractReady",contractReady);n.putLong("WarningUntil",warningUntil);n.putInt("Contract",contract);return n;}
  private static Progress read(CompoundTag n){Progress p=new Progress();p.intro=Math.max(0,Math.min(2,n.getInt("Intro")));p.stage=Math.max(0,Math.min(3,n.getInt("Stage")));p.contract=Math.floorMod(n.getInt("Contract"),YagaServices.POOL.length);if(p.stage<3&&YagaServices.POOL[p.contract]>=4)p.contract=YagaServices.nextContract(p.stage,p.contract);p.blockedUntil=Math.max(0,n.getLong("BlockedUntil"));p.contractReady=Math.max(0,n.getLong("ContractReady"));p.warningUntil=Math.max(0,n.getLong("WarningUntil"));String active=n.getString("Active");if(active.equals("main:"+p.stage)&&p.stage<3||active.equals("contract:"+p.contract)&&p.stage>=1)p.active=active;return p;}
 }
 public BlockPos anchor;public UUID npc;public boolean placed;public final Set<Long> examined=new HashSet<>();public final Map<UUID,Progress> players=new HashMap<>();
 public YagaData(){super();}
 public static YagaData get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(YagaData::new,(tag,registries)->{YagaData data=new YagaData();data.load(tag);return data;}),"slavicmyths_yaga");}
 public Progress progress(UUID id){Progress p=players.get(id);if(p==null){p=new Progress();players.put(id,p);setDirty();}return p;}
 @Override public CompoundTag save(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){if(anchor!=null)n.putLong("Anchor",anchor.asLong());if(npc!=null)n.putUUID("Npc",npc);n.putBoolean("Placed",placed);long[] tried=new long[examined.size()];int i=0;for(long c:examined)tried[i++]=c;n.putLongArray("Examined",tried);ListTag list=new ListTag();for(Map.Entry<UUID,Progress> e:players.entrySet()){CompoundTag p=e.getValue().save();p.putUUID("Player",e.getKey());list.add(p);}n.put("Players",list);return n;}
 public void load(CompoundTag n){anchor=n.contains("Anchor")?BlockPos.of(n.getLong("Anchor")):null;npc=n.hasUUID("Npc")?n.getUUID("Npc"):null;placed=n.getBoolean("Placed")&&anchor!=null&&npc!=null;examined.clear();for(long c:n.getLongArray("Examined"))examined.add(c);players.clear();for(Tag raw:n.getList("Players",10)){CompoundTag p=(CompoundTag)raw;if(p.hasUUID("Player"))players.put(p.getUUID("Player"),Progress.read(p));}}
}
