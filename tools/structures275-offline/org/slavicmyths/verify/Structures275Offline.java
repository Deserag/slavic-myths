package org.slavicmyths.verify;

import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.placement.*;
import org.slavicmyths.bayun.*;
import org.slavicmyths.yaga.YagaPlacement;
import org.slavicmyths.worldgen.*;

/** Offline native block shapes/transactions/NBT and candidate arithmetic, not real terrain generation. */
public final class Structures275Offline {
 static int checks;
 static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
 static final class Terrain implements InvocationHandler {
  final Map<BlockPos,BlockState> blocks=new HashMap<>();final LevelAccessor world;
  boolean loaded=true,slope;int reads,writes,throwAfterWrite=-1;
  Terrain(){world=(LevelAccessor)Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},this);}
  int height(int x){return 63+(slope&&x>0?4:0);}
  BlockState get(BlockPos p){return blocks.getOrDefault(p,p.getY()>height(p.getX())?Blocks.AIR.defaultBlockState():p.getY()==height(p.getX())?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.DIRT.defaultBlockState());}
  public Object invoke(Object proxy,Method m,Object[] a){return switch(m.getName()){
   case "hasChunksAt","hasChunkAt","hasChunk"->loaded;
   case "getBlockState"->{reads++;yield get((BlockPos)a[0]);}
   case "getFluidState"->get((BlockPos)a[0]).getFluidState();
   case "getBlockEntity"->get((BlockPos)a[0]).is(Blocks.CHEST)?new net.minecraft.world.level.block.entity.ChestBlockEntity((BlockPos)a[0],get((BlockPos)a[0])):null;
   case "getHeight"->{if(a==null||a.length==0)yield 384;int x=(int)a[1],z=(int)a[2],h=height(x);for(var e:blocks.entrySet())if(e.getKey().getX()==x&&e.getKey().getZ()==z&&!e.getValue().isAir()&&!(e.getValue().getBlock() instanceof LeavesBlock))h=Math.max(h,e.getKey().getY());yield h+1;}
   case "getMinBuildHeight"->-64;
   case "getMaxBuildHeight"->320;
   case "setBlock"->{writes++;var p=((BlockPos)a[0]).immutable();blocks.put(p,(BlockState)a[1]);if(writes==throwAfterWrite)throw new IllegalStateException("write mutated then failed");yield true;}
   case "getRandom"->net.minecraft.util.RandomSource.create(71);
   case "getBlockTicks","getFluidTicks"->net.minecraft.world.ticks.BlackholeTickAccess.emptyLevelList();
   case "isEmptyBlock"->get((BlockPos)a[0]).isAir();
   case "isClientSide"->false;
   case "getShade"->1F;
   case "toString"->"OFFLINE block fixture";
   default->{if(m.getReturnType()==boolean.class)yield false;if(m.getReturnType()==int.class)yield 0;if(m.getReturnType()==long.class)yield 0L;throw new UnsupportedOperationException(m.toString());}
  };}
 }
 static void restored(Terrain world,YagaPlacement.Site site){for(var e:site.old.entrySet())check(world.get(e.getKey()).equals(e.getValue()),"rollback at "+e.getKey());}
 static void placement(){
  for(boolean npcAccepted:new boolean[]{true,false}){
   var natural=new Terrain();natural.slope=true;var site=BayunPlacement.prepare(natural.world,BlockPos.ZERO);int[] added={0},undone={0};
   var result=YagaPlacement.commit(natural.world,site,()->{added[0]++;return npcAccepted;},()->undone[0]++);
   check(result.ok==npcAccepted&&added[0]==1,"natural feature commit/NPC outcome");
   if(!npcAccepted){restored(natural,site);check(undone[0]==1,"natural feature NPC rollback");}
  }
  for(boolean slope:new boolean[]{false,true})for(int mode=0;mode<4;mode++){
   var t=new Terrain();t.slope=slope;var site=BayunPlacement.prepare(t.world,new BlockPos(0,64,0));check(site.failure==null,"natural site rejected");check(t.writes==0,"preflight wrote world");
   if(mode==0)System.out.println("Bayun fixture slope="+slope+", desired cells="+site.desired.size());
   check(site.desired.size()<300,"nook expanded into old 13x13x28 clearing");int[] spawns={0},undo={0};boolean success=mode==0;
   var cursor=new YagaPlacement.Cursor(t.world,site,()->{spawns[0]++;return success;},()->undo[0]++,r->{});
   if(mode==2)t.throwAfterWrite=2;
   int steps=0;while(!cursor.done()){int before=t.writes;cursor.step(8,1_000_000_000);check(t.writes-before<=8,"write budget exceeded");if(mode==3&&steps==2)cursor.abort();check(++steps<1000,"non-terminating cursor");}
   check(cursor.result.ok==success,"wrong completion status");
   if(!success){restored(t,site);check(undo[0]==1,"NPC undo not once");}else{check(spawns[0]==1,"duplicate NPC callback");check(t.get(site.anchor.below()).is(Blocks.MOSSY_COBBLESTONE),"actual platform missing");}
  }
  var unloaded=new Terrain();unloaded.loaded=false;check(BayunPlacement.prepare(unloaded.world,BlockPos.ZERO).failure.reason.equals("chunks")&&unloaded.reads==0&&unloaded.writes==0,"unloaded reads/writes");
  for(var block:new Block[]{Blocks.CHEST,Blocks.STONE_BRICKS,Blocks.GLASS,Blocks.BEDROCK,Blocks.NETHER_PORTAL,Blocks.END_PORTAL_FRAME}){
   var t=new Terrain();t.blocks.put(new BlockPos(0,64,0),block.defaultBlockState());var site=BayunPlacement.prepare(t.world,BlockPos.ZERO);check(site.failure!=null&&t.writes==0,"protected block accepted: "+block);
  }
  {var t=new Terrain();var site=BayunPlacement.prepare(t.world,BlockPos.ZERO);var c=new YagaPlacement.Cursor(t.world,site,()->false,()->{},r->{});c.step(8,1_000_000_000);var pos=site.desired.keySet().iterator().next();t.blocks.put(pos,Blocks.GOLD_BLOCK.defaultBlockState());c.abort();while(!c.done())c.step(8,1_000_000_000);check(t.get(pos).is(Blocks.GOLD_BLOCK),"rollback destroyed later player edit");}
 }
 static void records(){
  check(org.slavicmyths.navya.NavyaHeartStructure.checksBasalFluid(39),"basal fluid inside footprint still checked");
  check(!org.slavicmyths.navya.NavyaHeartStructure.checksBasalFluid(40),"lava below footprint must not veto elevated heart");
  var data=new ManualStructureRecords();var box=new BoundingBox(-32,-20,-32,32,100,32);var ids=new ArrayList<UUID>();
  for(var family:List.of("gorodishche","serpent_hill","sanctuary_unfading_fire","abode_pure_spring","site/bayun","site/yaga","construction","chud_city","chud_abandoned","kurgan","bandit_camp")){
   var id=UUID.randomUUID();ids.add(id);data.add(id,family,0,BlockPos.ZERO,box);
  }
  var copy=ManualStructureRecords.load(data.save(new CompoundTag(),null));check(copy.entries().size()==ids.size(),"completed sites lost after NBT reload");
  check(copy.overlapping(box).size()==ids.size(),"reloaded exclusion index incomplete");copy.remove(ids.get(0));check(copy.entries().size()==ids.size()-1&&copy.overlapping(box).size()==ids.size()-1,"reservation cleanup/index");
  var homes=new BayunHomes();var at=new BlockPos(1,64,2);var a=UUID.randomUUID();var b=UUID.randomUUID();check(homes.reconcile(at,a)&&!homes.reconcile(at,b),"home ownership");homes.forget(at,b);check(homes.nearest(at).entity().equals(a),"undo removed another cat");homes.forget(at,a);check(homes.nearest(at)==null,"failed NPC reservation retained");
 }
 static void unusedPreparation()throws Exception{
  var field=SiteChunks.class.getDeclaredField("ACTIVE");field.setAccessible(true);var active=(Set<?>)field.get(null);int before=active.size();
  // No ServerLevel is constructed or touched: an unused preparation owns no tickets.
  var unused=new SiteChunks(null);check(active.size()==before,"unused constructor retained in ticket-owner set");unused.close();unused.close();
  try{unused.ready(new net.minecraft.world.level.ChunkPos(0,0));throw new AssertionError("closed preparation reopened");}catch(IllegalStateException expected){check(true,"closed guard before world access");}
 }
 static final class Placement extends RandomSpreadStructurePlacement {
  Placement(int spacing,int separation,int salt,RandomSpreadType spread,float frequency){super(Vec3i.ZERO,FrequencyReductionMethod.DEFAULT,frequency,salt,Optional.empty(),spacing,separation,spread);}
 }
 static void distribution()throws Exception{
  var output=new StringBuilder("SET\tSEED\tCANDIDATES_AFTER_FREQUENCY\tAREA_BLOCKS2\tUNTESTED_FILTERS\n");
  try(var files=Files.list(Path.of("src/main/resources/data/slavicmyths/worldgen/structure_set"))){
   for(var file:files.filter(p->p.toString().endsWith(".json")).sorted().toList()){
    var data=JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonObject("placement");int spacing=data.get("spacing").getAsInt(),separation=data.get("separation").getAsInt(),salt=data.get("salt").getAsInt();float frequency=data.has("frequency")?data.get("frequency").getAsFloat():1;
    var type=data.has("spread_type")&&data.get("spread_type").getAsString().equals("triangular")?RandomSpreadType.TRIANGULAR:RandomSpreadType.LINEAR;
    var p=new Placement(spacing,separation,salt,type,frequency);
    for(long seed:new long[]{1,71,2432026,987654321,-271275}){
     var positions=new HashSet<Long>();
     for(int x=Math.floorDiv(-512,spacing);x<=Math.floorDiv(511,spacing);x++)for(int z=Math.floorDiv(-512,spacing);z<=Math.floorDiv(511,spacing);z++){
      var at=p.getPotentialStructureChunk(seed,x*spacing,z*spacing);check(Math.floorDiv(at.x,spacing)==x&&Math.floorDiv(at.z,spacing)==z,"negative candidate cell");
      if(at.x< -512||at.x>511||at.z< -512||at.z>511||!p.applyAdditionalChunkRestrictions(at.x,at.z,seed))continue;
      if(data.get("type").getAsString().equals("slavicmyths:chud_region")){
       for(var q:org.slavicmyths.chud.ChudSitePolicy.candidates(at.x,at.z,at.x,at.z))if(q.x()>=-512&&q.x()<=511&&q.z()>=-512&&q.z()<=511)positions.add(net.minecraft.world.level.ChunkPos.asLong(q.x(),q.z()));
      }else positions.add(at.toLong());
     }
     if(frequency==0)check(positions.isEmpty(),"disabled standalone placement became active");
     output.append(file.getFileName()).append('\t').append(seed).append('\t').append(positions.size()).append("\t268435456\tbiome/terrain/exclusion/weighted choice/real generation NOT_RUN\n");
    }
   }
  }
  Files.writeString(Path.of("docs/implementation/structures275-20261025/candidates-5seeds.tsv"),output);
 }
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  placement();records();unusedPreparation();distribution();System.out.println("PASS "+checks+" native offline checks; vanilla registry bootstrap only, no world/client/server. GAMEPLAY_NOT_RUN.");
 }
}
