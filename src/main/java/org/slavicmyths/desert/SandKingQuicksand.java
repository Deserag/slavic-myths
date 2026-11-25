package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
public final class SandKingQuicksand extends SavedData {
 private record Entry(boolean red,long expires){}
 private final Map<BlockPos,Entry> entries=new HashMap<>();
 private static final int MAX=8192,LIFE=600;
 public static SandKingQuicksand get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(SandKingQuicksand::new,SandKingQuicksand::load),"slavicmyths_sand_king_quicksand");}
 public static void create(ServerLevel w,Vec3 landing){
  if(!w.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return;
  var data=get(w);var base=BlockPos.containing(landing);Block quick=DesertBlocks.ALL.get("quicksand").get();long expiry=w.getGameTime()+LIFE;
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){
   if(x*x+z*z>16)continue;
   for(int dy=1;dy>=-3;dy--){BlockPos p=base.offset(x,dy,z);if(!w.hasChunkAt(p)||!w.isInWorldBounds(p))break;var state=w.getBlockState(p);
    if(state.is(quick)){Entry old=data.entries.get(p);if(old!=null){data.entries.put(p,new Entry(old.red,expiry));data.setDirty();}break;}
    if(state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)){if(data.entries.size()<MAX&&w.getBlockState(p.above()).isAir()){data.entries.put(p,new Entry(state.is(Blocks.RED_SAND),expiry));data.setDirty();if(!w.setBlock(p,quick.defaultBlockState(),Block.UPDATE_ALL))data.entries.remove(p);}break;}
    if(!state.getCollisionShape(w,p).isEmpty())break;
   }
  }
 }
 public static void tick(LevelTickEvent.Post event){if(event.getLevel() instanceof ServerLevel w&&w.getGameTime()%20==0)get(w).restore(w);}
 private void restore(ServerLevel w){Block quick=DesertBlocks.ALL.get("quicksand").get();long now=w.getGameTime();var it=entries.entrySet().iterator();while(it.hasNext()){var row=it.next();BlockPos p=row.getKey();Entry e=row.getValue();if(now<e.expires||!w.hasChunkAt(p))continue;if(w.getBlockState(p).is(quick))w.setBlock(p,(e.red?Blocks.RED_SAND:Blocks.SAND).defaultBlockState(),Block.UPDATE_ALL);it.remove();setDirty();}}
 private static SandKingQuicksand load(CompoundTag tag,HolderLookup.Provider registries){var result=new SandKingQuicksand();for(var raw:tag.getList("Sand",Tag.TAG_COMPOUND)){var n=(CompoundTag)raw;if(result.entries.size()>=MAX)break;result.entries.put(BlockPos.of(n.getLong("Pos")),new Entry(n.getBoolean("Red"),n.getLong("Expires")));}return result;}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){var list=new ListTag();entries.forEach((p,e)->{var n=new CompoundTag();n.putLong("Pos",p.asLong());n.putBoolean("Red",e.red);n.putLong("Expires",e.expires);list.add(n);});tag.put("Sand",list);return tag;}
}
