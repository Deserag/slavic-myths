package org.slavicmyths.losthalls;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.slavicmyths.registry.ModFeatures;
import org.slavicmyths.losthalls.LostHallsRooms.*;

/** New-chunk, chunk-clipped deterministic geometry. No retro-generation, no AI chunk tickets. */
@EventBusSubscriber(modid="slavicmyths")
public final class HallsWorldgen {
 public static final net.neoforged.neoforge.registries.DeferredHolder<Feature<?>,Terrain> TERRAIN=ModFeatures.FEATURES.register("lost_halls_terrain",Terrain::new);
 public static void init(){}
 @SubscribeEvent public static void presets(net.neoforged.neoforge.event.server.ServerAboutToStartEvent e){var r=e.getServer().registryAccess();var biome=r.registryOrThrow(Registries.BIOME).getHolder(HallsContent.BIOME);var preset=r.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).get(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);if(biome.isPresent()&&preset!=null)((org.slavicmyths.navya.NavyaPresetAccess)(Object)preset).slavicmyths$append(biome.get());}
 public static LostHallsRegionTopology.Plan plan(long seed,int x,int z){return LostHallsRegionTopology.plan(seed,Math.floorDiv(x,192),Math.floorDiv(z,192),76);}
 public static boolean habitat(HallsContent.Kind k,long seed,BlockPos p){var col=HallsGeometry.column(plan(seed,p.getX(),p.getZ()),p.getX(),p.getZ());if(col==null||col.pillar()||p.getY()<=col.floor()||p.getY()>col.ceiling()-2)return false;return switch(k){case SHEPTUN->col.scene()==Scene.CORRIDOR||col.scene()==Scene.JUNCTION;case OSKOLNIK->col.scene()==Scene.LUMINOUS_GROTTO||col.scene()==Scene.LARGE_HALL;case GULOKRYL->col.scene()==Scene.BRIDGE||col.scene()==Scene.LARGE_HALL;case EKHOVIK->col.scene()==Scene.LARGE_HALL;case GRIBOSHMYG->col.scene()==Scene.FUNGUS_HALL;};}
 public static final class Terrain extends Feature<NoneFeatureConfiguration>{public Terrain(){super(NoneFeatureConfiguration.CODEC);}
  @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){var w=c.level();int bx=c.origin().getX()&~15,bz=c.origin().getZ()&~15;var chunk=w.getChunk(new BlockPos(bx,76,bz));
   // Structures own their footprint. Never carve a fortress/bastion/portal reference.
   if(chunk.getAllReferences().values().stream().anyMatch(ref->!ref.isEmpty()))return false;
   var p=plan(w.getSeed(),bx,bz);boolean changed=false;
   for(int x=bx;x<bx+16;x++)for(int z=bz;z<bz+16;z++){HallsGeometry.Column col=HallsGeometry.column(p,x,z);if(col==null||!w.getBiome(new BlockPos(x,76,z)).is(HallsContent.BIOME))continue;
    for(int y=col.floor()-3;y<=col.ceiling()+2;y++){BlockPos at=new BlockPos(x,y,z);if(!w.ensureCanWrite(at)||w.getBlockEntity(at)!=null)continue;var old=w.getBlockState(at);if(!old.isAir()&&!old.is(Blocks.NETHERRACK)&&!old.is(Blocks.BLACKSTONE)&&!old.is(Blocks.BASALT))continue;
     boolean solid=col.pillar()||y<=col.floor()||y>col.ceiling();String material=col.scene()==Scene.LUMINOUS_GROTTO&&Math.floorMod(x*13+y*7+z*19,17)<3?"luminous_vein_stone":Math.floorMod(x*19+y*3+z*7,43)==0?"cracked_lost_hall_stone":"lost_hall_stone";
     var placed=solid?HallsBlocks.get(material).defaultBlockState():y==col.floor()+1&&!col.openBelow()&&Math.floorMod(x*31+z*17,29)==0?HallsBlocks.get("cave_coating").defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN,true):Blocks.CAVE_AIR.defaultBlockState();w.setBlock(at,placed,2);changed=true;
    }
    if(col.scene()==Scene.FUNGUS_HALL&&!col.center())for(Node n:p.rooms())if(n.scene()==Scene.FUNGUS_HALL){int dx=x-n.anchor().getX(),dz=z-n.anchor().getZ();for(int side:new int[]{-1,1}){int fx=side*10,fz=side*7;if(Math.abs(dx-fx)<=2&&Math.abs(dz-fz)<=2){int floor=col.floor();for(int y=floor;y<=floor+8;y++){BlockPos at=new BlockPos(x,y,z);if(!w.ensureCanWrite(at)||w.getBlockEntity(at)!=null)continue;var present=w.getBlockState(at);if(!present.getFluidState().isEmpty()||!present.isAir()&&!present.is(Blocks.NETHERRACK)&&!present.is(Blocks.BLACKSTONE)&&!present.is(Blocks.BASALT)&&!floor(present))continue;if(y==floor)w.setBlock(at,Blocks.WARPED_NYLIUM.defaultBlockState(),2);else if(dx==fx&&dz==fz&&y<floor+7)w.setBlock(at,Blocks.WARPED_STEM.defaultBlockState(),2);else if(y>=floor+7)w.setBlock(at,y==floor+7&&Math.floorMod(dx+dz,3)==0?Blocks.SHROOMLIGHT.defaultBlockState():Blocks.WARPED_WART_BLOCK.defaultBlockState(),2);}}}}
   }return changed;
  }
 }
 // ChunkEvent.Load can run inside FULL conversion. execute() is inline on the server
 // thread, so reading that same chunk here can wait on its own unfinished future.
 // Coalesce only load-event work and publish after the tick, never during conversion.
 private static final java.util.concurrent.ConcurrentMap<ServerLevel,java.util.concurrent.ConcurrentMap<Long,ChunkPos>> PENDING=new java.util.concurrent.ConcurrentHashMap<>();
 @SubscribeEvent public static void loaded(ChunkEvent.Load e){
  if(!(e.getLevel() instanceof ServerLevel s)||!s.dimension().equals(Level.NETHER))return;
  var cp=e.getChunk().getPos();int rx=Math.floorDiv(cp.getMiddleBlockX(),192),rz=Math.floorDiv(cp.getMiddleBlockZ(),192);
  PENDING.computeIfAbsent(s,w->new java.util.concurrent.ConcurrentHashMap<>()).put(ChunkPos.asLong(rx,rz),cp);
 }
 @SubscribeEvent public static void afterLoads(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
  for(var entry:PENDING.entrySet())if(entry.getKey().getServer()==e.getServer()){
   var world=entry.getKey();var queue=entry.getValue();int budget=8;
   for(var pending:queue.entrySet()){
    if(budget--<=0)break;if(!queue.remove(pending.getKey(),pending.getValue()))continue;
    var cp=pending.getValue();publish(world,plan(world.getSeed(),cp.getMiddleBlockX(),cp.getMiddleBlockZ()));
   }
  }
 }
 @SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){PENDING.remove(e.getLevel());}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){PENDING.keySet().removeIf(w->w.getServer()==e.getServer());}
 private static void publish(ServerLevel s,LostHallsRegionTopology.Plan p){if(!s.getServer().isSameThread())return;
  var index=LostHallsRooms.get(s);
  for(Node n:p.rooms()){
   if(index.node(n.id()).filter(n::equals).isPresent())continue;
   if(s.getChunkSource().getChunkNow(n.anchor().getX()>>4,n.anchor().getZ()>>4)==null||!s.getBiome(n.anchor()).is(HallsContent.BIOME)||!floor(s.getBlockState(n.anchor().below())))continue;
   Set<BlockPos> feet=new HashSet<>();Map<BlockPos,BlockPos> projected=new HashMap<>();for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){var q=n.anchor().offset(x,0,z);var col=HallsGeometry.column(p,q.getX(),q.getZ());if(col!=null&&!col.pillar()){var foot=new BlockPos(q.getX(),col.floor()+1,q.getZ());feet.add(foot);projected.put(q,foot);}}List<BlockPos> exits=new ArrayList<>();exits.add(n.anchor());for(BlockPos d:List.of(new BlockPos(2,0,0),new BlockPos(-2,0,0),new BlockPos(0,0,2),new BlockPos(0,0,-2)))if(exits.size()<n.exits()){BlockPos foot=projected.get(n.anchor().offset(d));if(foot!=null)exits.add(foot);}LostHallsRoomPublication.publish(s,n,exits,feet);
  }
  // Complete region proof is conservative: all six published nodes and resident arena.
  for(Node n:p.rooms())if(index.node(n.id()).filter(n::equals).isEmpty())return;
  BlockPos at=p.arena();int cx=at.getX()>>4,cz=at.getZ()>>4;for(int x=cx-2;x<=cx+2;x++)for(int z=cz-2;z<=cz+2;z++)if(!s.hasChunk(x,z)||!s.areEntitiesLoaded(ChunkPos.asLong(x,z)))return;
  if(!s.getBiome(at).is(HallsContent.BIOME)||!s.isEmptyBlock(at)||!s.getBlockState(at.below()).isFaceSturdy(s,at.below(),Direction.UP)||s.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)return;
  if(PostenEncounters.get(s).snapshot(p.arenaId()).isPresent()||!verifyRegion(s,p))return;UUID id=UUID.nameUUIDFromBytes(("posten-body:"+p.arenaId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));if(s.getEntity(id)!=null)return;
  var boss=PostenMob.TYPE.get().create(s);if(boss==null)return;boss.setUUID(id);boss.bind(p.arenaId(),at);boss.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);if(s.noCollision(boss))s.addFreshEntity(boss);
 }
 /** Actual loaded geometry, not logical room receipts, authorizes the first boss. */
 private static boolean verifyRegion(ServerLevel s,LostHallsRegionTopology.Plan p){
  Map<UUID,BlockPos> anchors=new HashMap<>();for(Node n:p.rooms())anchors.put(n.id(),n.anchor());anchors.put(p.arenaId(),p.arena());
  for(var edge:p.links()){BlockPos a=anchors.get(edge.from()),b=anchors.get(edge.to());int steps=(int)Math.ceil(Math.sqrt(a.distSqr(b)));for(int i=0;i<=steps;i++){double t=i/(double)Math.max(1,steps);int x=(int)Math.floor(a.getX()+.5+t*(b.getX()-a.getX())),z=(int)Math.floor(a.getZ()+.5+t*(b.getZ()-a.getZ()));if(!walkable(s,p,x,z,2))return false;}}
  // 32x32 footprint and enough standing room for the four 3.9-block-tall bodies.
  for(int x=-16;x<16;x++)for(int z=-16;z<16;z++)if(!walkable(s,p,p.arena().getX()+x,p.arena().getZ()+z,4))return false;
  return true;
 }
 private static boolean walkable(ServerLevel s,LostHallsRegionTopology.Plan p,int x,int z,int height){var col=HallsGeometry.column(p,x,z);if(col==null||col.pillar())return false;BlockPos at=new BlockPos(x,col.floor()+1,z);if(!s.hasChunkAt(at)||!s.getWorldBorder().isWithinBounds(at)||!floor(s.getBlockState(at.below())))return false;for(int y=0;y<height;y++){BlockPos q=at.above(y);if(!s.hasChunkAt(q)||!s.getFluidState(q).isEmpty()||!s.getBlockState(q).getCollisionShape(s,q).isEmpty())return false;}return true;}
 private static boolean floor(net.minecraft.world.level.block.state.BlockState s){for(String id:List.of("lost_hall_stone","cracked_lost_hall_stone","luminous_vein_stone","polished_lost_hall_stone"))if(s.is(HallsBlocks.get(id)))return true;return s.is(Blocks.WARPED_NYLIUM);}
 private HallsWorldgen(){}
}
