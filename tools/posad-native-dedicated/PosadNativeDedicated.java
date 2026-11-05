package org.slavicmyths.verify_native;
import org.slavicmyths.posad.*;

import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.common.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Uses actual dedicated Nether chunks and registry StructureStarts, never manual placement. */
@Mod("slavicmyths_posadnative") @EventBusSubscriber(modid="slavicmyths_posadnative")
public final class PosadNativeDedicated {
 private static int candidate,cursor,ticks;private static PosadPiece piece;private static Structure structure;
 private static final List<ChunkPos> chunks=new ArrayList<>();private static boolean finished;
 private static long worstNanos,startedNanos;
 private static int populationPhase=-1,untilTick,popCursor,verifiedBlocks;
 private static ServerPlayer first,second;
 private static void move(ServerLevel world,ServerPlayer player,int x,int z){
  var cell=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),piece.turns());var at=piece.anchor().offset(cell.x(),1,cell.z());
  world.getChunk(at);player.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);if(world.players().contains(player))world.getChunkSource().move(player);
 }
 private static boolean population(ServerLevel world){
  if(populationPhase==0){
   if(ticks<untilTick)return false;
   long loaded=chunks.stream().filter(cp->world.getChunkSource().getChunkNow(cp.x,cp.z)!=null).count();
   check(loaded<chunks.size(),"Fixture did not unload the native city before gate approach: "+loaded);
   first=NativePlayers.create(world,"GatePopulationA");second=NativePlayers.create(world,"GatePopulationB");
   move(world,first,56,105);move(world,second,58,105);world.addNewPlayer(first);world.addNewPlayer(second);
   populationPhase=1;untilTick=ticks+120;
   System.out.println("POSAD_REAL_POPULATION_GATE fixtureServerPlayers=2 (not network multiplayer clients) previouslyLoaded="+loaded+"/"+chunks.size());return false;
  }
  if(ticks<untilTick)return false;
  if(populationPhase<=5){
   int[][] stops={{20,20},{88,20},{20,55},{88,55},{66,85}};
   var stop=stops[populationPhase-1];move(world,first,stop[0],stop[1]);move(world,second,stop[0]+1,stop[1]);
   populationPhase++;untilTick=ticks+120;return false;
  }
  if(populationPhase==6){
   check(PosadResidentSpawner.populated(world,piece.anchor()),"Automatic population did not account for all seventeen slots");
   populationPhase=7;popCursor=0;System.out.println("POSAD_POPULATION_VERIFY_LOADING test_only_forced_chunks=true; automatic spawning already complete");
  }
  if(popCursor<chunks.size()){var cp=chunks.get(popCursor++);world.setChunkForced(cp.x,cp.z,true);world.getChunk(cp.x,cp.z);if(popCursor==chunks.size())untilTick=ticks+60;return false;}
  var city=PosadPopulationPlan.settlementId(world.dimension().location().toString(),piece.anchor().getX(),piece.anchor().getY(),piece.anchor().getZ());
  var residents=world.getEntitiesOfClass(PosadResidentEntity.class,net.minecraft.world.phys.AABB.of(piece.getBoundingBox()).inflate(32),npc->city.equals(npc.city()));
  check(residents.size()==17,"Automatic population count "+residents.size()+" instead of 17");
  check(residents.stream().filter(npc->npc.role()==PosadPopulationPlan.Role.POSADNIK).count()==1,"Posadnik missing or duplicated");
  check(residents.stream().map(npc->npc.getUUID()).distinct().count()==17,"Repeated resident UUID");
  System.out.println("POSAD_REAL_POPULATION_PASS stage="+System.getProperty("posadnative.stage")+" count=17 leaders=1 viewDistance=2 no_direct_spawner_call=true");
  first.discard();second.discard();for(var cp:chunks)world.setChunkForced(cp.x,cp.z,false);return true;
 }
 private static Path manifest(MinecraftServer server){return server.getWorldPath(LevelResource.ROOT).resolve("posad-native.properties");}
 private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
 @SubscribeEvent public static void started(ServerStartedEvent event)throws Exception{
  var server=event.getServer();check(server.isDedicatedServer(),"Not a dedicated server");startedNanos=System.nanoTime();
  structure=server.registryAccess().registryOrThrow(Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.parse("slavicmyths:nether_posad"));
  check(structure!=null,"Missing native registry ID");
  if(System.getProperty("posadnative.stage").equals("reload")){
   var p=new Properties();try(var input=Files.newInputStream(manifest(server))){p.load(input);}
   var w=server.getLevel(Level.NETHER);int cx=Integer.parseInt(p.getProperty("startX")),cz=Integer.parseInt(p.getProperty("startZ"));
   var start=w.getChunkSource().getChunk(cx,cz,ChunkStatus.STRUCTURE_STARTS,true).getStartForStructure(structure);
   check(start!=null&&start.isValid(),"Saved native start lost");select(start);
  }
 }
 private static void select(StructureStart start){
  piece=(PosadPiece)start.getPieces().stream().filter(p->p instanceof PosadPiece).findFirst().orElseThrow();
  var box=piece.getBoundingBox();
  for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)chunks.add(new ChunkPos(x,z));
 }
 @SubscribeEvent public static void tick(ServerTickEvent.Post event){
  if(finished||++ticks<5)return;var server=event.getServer();var world=server.getLevel(Level.NETHER);long begin=System.nanoTime();
  try{
   if(piece==null){
    check(candidate<64,"No accepted native start in 64 placement cells");
    var placement=(RandomSpreadStructurePlacement)server.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)
     .get(net.minecraft.resources.ResourceLocation.parse("slavicmyths:nether_posad")).placement();
    var cp=placement.getPotentialStructureChunk(world.getSeed(),candidate++*placement.spacing(),0);
    var chunk=world.getChunkSource().getChunk(cp.x,cp.z,ChunkStatus.STRUCTURE_STARTS,true);
    var start=chunk.getStartForStructure(structure);
    System.out.println("POSAD_REAL_NATIVE_CANDIDATE seed="+world.getSeed()+" index="+(candidate-1)+" chunk="+cp+" accepted="+(start!=null&&start.isValid())+" nanos="+(System.nanoTime()-begin));
    if(start==null||!start.isValid())return;
    select(start);
    boolean wasFull=world.getChunkSource().getChunkNow(cp.x,cp.z)!=null;
    int[] result={-1};var source=server.createCommandSourceStack().withLevel(world).withPosition(net.minecraft.world.phys.Vec3.atCenterOf(piece.anchor().offset(56,0,56))).withPermission(4).withCallback((ok,n)->result[0]=ok?n:0);
    server.getCommands().performPrefixedCommand(source,"locate structure slavicmyths:nether_posad");
    check(result[0]>=0,"Native locate failed");
    check(wasFull||world.getChunkSource().getChunkNow(cp.x,cp.z)==null,"Locate upgraded start to FULL and built the city");
    var props=new Properties();props.setProperty("startX",Integer.toString(cp.x));props.setProperty("startZ",Integer.toString(cp.z));props.setProperty("seed",Long.toString(world.getSeed()));
    try(var out=Files.newOutputStream(manifest(server))){props.store(out,"Actual native start; disposable QA world");}
    System.out.println("POSAD_REAL_LOCATE_PASS seed="+world.getSeed()+" no_full_upgrade=true");return;
   }
   if(cursor<chunks.size()){
    var cp=chunks.get(cursor++);world.getChunk(cp.x,cp.z);
    System.out.println("POSAD_REAL_NATIVE_CHUNK seed="+world.getSeed()+" built="+cursor+"/"+chunks.size()+" nanos="+(System.nanoTime()-begin));return;
   }
   if(populationPhase>=0){
    if(!population(world))return;
   }else{
   int tested=0,wrong=0;
   for(int x=0;x<113;x++)for(int z=0;z<113;z++)for(int y=0;y<18;y++){
    var local=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),piece.turns());var at=piece.anchor().offset(local.x(),y,local.z());
    var expected=piece.placedState(x,y,z,world,at);if(expected==null||expected.isAir())continue;
    tested++;var actual=world.getBlockState(at);
    boolean same=actual.is(expected.getBlock());
    if(same&&System.getProperty("posadnative.stage").equals("reload")&&expected.getBlock() instanceof net.minecraft.world.level.block.DoorBlock){
     // Citizens/players legitimately leave doors open. Reload must preserve the
     // saved interactive state, while facing/hinge/half still match the geometry.
     var door=net.minecraft.world.level.block.DoorBlock.OPEN;var powered=net.minecraft.world.level.block.DoorBlock.POWERED;
     expected=expected.setValue(door,actual.getValue(door)).setValue(powered,actual.getValue(powered));
     var other=world.getBlockState(actual.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER?at.above():at.below());
     check(other.is(actual.getBlock())&&other.getValue(door)==actual.getValue(door),"Saved door halves disagree at "+at);
    }
    if(same&&(expected.getBlock() instanceof net.minecraft.world.level.block.StairBlock||expected.getBlock() instanceof net.minecraft.world.level.block.DoorBlock||expected.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock||expected.is(PosadBeds.COT.get())))same=actual.equals(expected);
    if(!same){if(wrong++<12)System.out.println("POSAD_REAL_NATIVE_MISMATCH "+at+" expected="+expected+" actual="+actual);}
   }
   check(wrong==0,"Actual native block mismatches="+wrong+" tested="+tested);
   verifiedBlocks=tested;
   if(Boolean.getBoolean("posadnative.population")){populationPhase=0;untilTick=ticks+400;System.out.println("POSAD_REAL_POPULATION_WAIT_UNLOAD ticks=400");return;}
   }
   String biome=world.getBiome(piece.anchor().offset(56,8,56)).unwrapKey().orElseThrow().location().toString();
   String requiredBiome=System.getProperty("posadnative.biome","");check(requiredBiome.isEmpty()||requiredBiome.equals(biome),"Wrong actual fixture biome: "+biome);
   System.out.println("POSAD_REAL_NATIVE_BIOME seed="+world.getSeed()+" biome="+biome);
   check(server.saveEverything(false,true,true),"Native save failed");
   System.out.println("POSAD_REAL_NATIVE_PASS seed="+world.getSeed()+" stage="+System.getProperty("posadnative.stage")+" candidates="+candidate+" chunks="+chunks.size()+" verifiedBlocks="+verifiedBlocks+" orientation="+piece.turns()+" anchor="+piece.anchor()+" worstStepNanos="+worstNanos+" elapsedNanos="+(System.nanoTime()-startedNanos));
   Files.writeString(server.getWorldPath(LevelResource.ROOT).resolve("posad-native.result"),"PASS");
   finished=true;server.halt(false);
  }catch(Throwable failure){finished=true;System.out.println("POSAD_REAL_NATIVE_FAIL "+failure);failure.printStackTrace();try{Files.writeString(server.getWorldPath(LevelResource.ROOT).resolve("posad-native.result"),"FAIL: "+failure);}catch(Exception writeFailure){writeFailure.printStackTrace();}server.halt(false);}
  finally{worstNanos=Math.max(worstNanos,System.nanoTime()-begin);}
 }
}
