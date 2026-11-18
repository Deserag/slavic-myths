package org.slavicmyths.verify;
import java.util.*;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.sanctuary.*;
import org.slavicmyths.guardians.*;

/** Production SavedData/transition/door methods; no client/server/world is constructed. */
public final class Rituals273Offline {
 static int checks;
 static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
 static void sequence(){
  UUID player=UUID.randomUUID();
  for(boolean spring:new boolean[]{false,true})for(var rotation:Rotation.values()){
   var sanctuary=new SanctuaryRecords();var ledger=new GuardianLedger();UUID id=UUID.randomUUID();var origin=new BlockPos(-153,40,273);
   sanctuary.ensure(id,origin,rotation,spring);ledger.ensure(id,sanctuary.row(id));
   check(!ledger.unlock(id,sanctuary)&&ledger.stage(id).equals("SEALED"),"missing prerequisites stay sealed");
   int[] changes={0};var initial=sanctuary;
   if(spring){
    check(!sanctuary.pool(id,0,()->false)&&sanctuary.row(id).getInt("Pools")==0,"refused world update does not commit pool");
    for(int bad:new int[]{-1,2,31,32,Integer.MAX_VALUE})check(!sanctuary.pool(id,bad,()->{throw new AssertionError("invalid pool accesses world");}),"invalid pool rejected before callback");
    check(sanctuary.pool(id,0,()->{changes[0]++;return true;})&&!ledger.unlock(id,sanctuary),"one pool insufficient");
    check(!sanctuary.pool(id,0,()->{throw new AssertionError("duplicate pool mutates world");}),"repeat no mutation");
    check(sanctuary.pool(id,1,()->{changes[0]++;return true;})&&changes[0]==2,"two distinct pool activations");
   }else{
    check(!sanctuary.restore(id,()->false)&&!sanctuary.restored(id),"refused world update does not restore fire");
    check(sanctuary.restore(id,()->{check(!initial.restored(id),"world activated before ledger commit");changes[0]++;return true;}),"first fire");
    check(!sanctuary.restore(id,()->{throw new AssertionError("duplicate fire mutates world");})&&changes[0]==1,"repeat no mutation");
   }
   // Legacy world: the prerequisite flags precede creation of the guardian ledger.
   sanctuary=SanctuaryRecords.load(sanctuary.save(new CompoundTag(),null),null);ledger=new GuardianLedger();ledger.ensure(id,sanctuary.row(id));
   check(ledger.unlock(id,sanctuary)&&ledger.stage(id).equals("READY"),"old prerequisite flags unlock new encounter");
   check(sanctuary.row(id).getString("RelicDoorState").equals("OPEN_194"),"existing door metadata retained");
   check(!ledger.unlock(id,sanctuary),"repeat unlock no stage reset");
   for(String stage:List.of("READY","ACTIVE","PURIFICATION")){ledger.stage(id,stage);check(!ledger.rewardAvailable(id),"no fragment before purification");}
   ledger.participate(id,player);ledger.stage(id,"PURIFICATION");
   ledger=GuardianLedger.load(ledger.save(new CompoundTag(),null),null);check(ledger.stage(id).equals("PURIFICATION"),"purification survives data reload");
   ledger.stage(id,"CLEARED");check(ledger.rewardAvailable(id),"fragment gate opens only after completion");check(!ledger.unlock(id,sanctuary)&&ledger.stage(id).equals("CLEARED"),"cleared never reset by ritual");
   var saved=ledger.save(new CompoundTag(),null);var claims=new CompoundTag();var receipt=new CompoundTag();receipt.putBoolean(spring?"Water":"Fire",true);claims.put(player.toString(),receipt);saved.put("Claims",claims);ledger=GuardianLedger.load(saved,null);
   check(ledger.claimed(player,spring)&&!ledger.claimed(player,!spring),"global per-type receipt survives reload");check(ledger.rewardAvailable(id)&&ledger.row(id).getCompound("Participants").getBoolean(player.toString()),"cleared and participants survive");
   check(ledger.near(BlockPos.of(ledger.row(id).getLong("SpawnAnchor"))).contains(id),"rotated spatial index rebuilt");
   ledger.row(id).putBoolean("Standalone",true);check(!ledger.rewardAvailable(id),"egg encounter never dispenses story fragments");
  }
 }
 static void doors(){
  var blocks=new HashMap<BlockPos,BlockState>();boolean[] loaded={true};int[] queries={0};
  LevelAccessor world=(LevelAccessor)Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},(p,m,a)->switch(m.getName()){
   case "hasChunkAt"->loaded[0];
   case "getBlockState"->{queries[0]++;yield blocks.getOrDefault(a[0],Blocks.AIR.defaultBlockState());}
   case "setBlock"->{blocks.put(((BlockPos)a[0]).immutable(),(BlockState)a[1]);yield true;}
   default->throw new AssertionError("unexpected world/chunk API: "+m.getName());
  });
  for(boolean spring:new boolean[]{false,true})for(var r:Rotation.values()){
   var records=new SanctuaryRecords();var id=UUID.randomUUID();records.ensure(id,new BlockPos(-153,40,273),r,spring);var n=records.row(id);blocks.clear();
   for(int i=-1;i<=1;i++)for(int y=spring?1:2;y<=(spring?5:6);y++){var at=spring?GuardianWorld.at(n,81+i,y,29):GuardianWorld.at(n,61,y,43+i);blocks.put(at,Blocks.BARRIER.defaultBlockState());}
   var protectedPos=BlockPos.of(n.getLong("Door"));blocks.put(protectedPos,Blocks.CHEST.defaultBlockState());loaded[0]=false;queries[0]=0;GuardianWorld.openDoor(world,n,Blocks.BARRIER);check(queries[0]==0,"unloaded cells not read/loaded");
   loaded[0]=true;GuardianWorld.openDoor(world,n,Blocks.BARRIER);check(blocks.get(protectedPos).is(Blocks.CHEST),"player replacement retained");check(blocks.values().stream().noneMatch(s->s.is(Blocks.BARRIER)),"rotated seal fully removed");
   GuardianWorld.openDoor(world,n,Blocks.BARRIER);check(blocks.get(protectedPos).is(Blocks.CHEST),"repeat preserves replacements");
  }
 }
 static void markers()throws Exception{
  var markers=new HashMap<String,Set<BlockPos>>();int tiles=0;
  try(var files=Files.walk(Path.of("src/main/resources/data/slavicmyths/structure/sanctuaries"))){for(var path:files.filter(p->p.toString().endsWith(".nbt")).toList()){
   var stem=path.getFileName().toString().replace(".nbt","").split("_");int ox=Integer.parseInt(stem[0]),oz=Integer.parseInt(stem[1]);var root=NbtIo.readCompressed(path,NbtAccounter.unlimitedHeap());
   for(var raw:root.getList("blocks",10)){var b=(CompoundTag)raw;String role=b.getCompound("nbt").getString("metadata");if(role.isEmpty())continue;var pos=b.getList("pos",3);markers.computeIfAbsent(path.getParent().getFileName()+"/"+role,k->new HashSet<>()).add(new BlockPos(ox+pos.getInt(0),pos.getInt(1),oz+pos.getInt(2)));}tiles++;
  }}
  check(tiles==61,"all actual templates read");
  check(markers.get("fire/loot:guaranteed").equals(Set.of(new BlockPos(11,2,17))),"guaranteed ember source marker in accessible store");
  check(markers.get("fire/heart:fire").equals(Set.of(new BlockPos(40,2,51))),"real brazier interaction block, not campfire");
  check(markers.get("spring/pool:0").equals(Set.of(new BlockPos(23,1,39)))&&markers.get("spring/pool:1").equals(Set.of(new BlockPos(23,1,48))),"both real pool node markers");
  check(markers.get("spring/heart:spring").equals(Set.of(new BlockPos(53,1,55))),"actual spring heart");
 }
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  sequence();doors();markers();System.out.println("RITUALS273_OFFLINE_PASS suites=3 checks="+checks+"; native saved-data/door/template methods, GAMEPLAY_NOT_RUN, no world/client/server");
 }
}
