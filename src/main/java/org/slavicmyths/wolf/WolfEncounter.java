package org.slavicmyths.wolf;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.hunt.*;
/** One bounded loaded terrain attempt per eligible local chunk. Never generates/loads chunks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WolfEncounter {
 public static final TagKey<Biome> FORESTS=TagKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","grey_wolf_forests"));
 private static final Map<ServerLevel,Set<Long>> CHECKED=new WeakHashMap<>();
 public static boolean eligibleRegion(long seed,int rx,int rz){return new Random(BossRules.seed(seed,rx,rz,BossKind.TUGARIN)^0x1471a19L).nextDouble()<.35;}
 public static int quota(ServerLevel w){long valid=HuntRecords.get(w).bosses.values().stream().filter(b->b.kind==BossKind.TUGARIN&&b.natural&&b.safe).count();return Math.min(12,Math.max(3,(int)(valid*2/5)));}
 public static boolean safeBody(ServerLevel w,AABB box,BlockPos feet){for(int x=(int)Math.floor(box.minX);x<Math.ceil(box.maxX);x++)for(int z=(int)Math.floor(box.minZ);z<Math.ceil(box.maxZ);z++)if(!w.hasChunkAt(new BlockPos(x,feet.getY(),z)))return false;return w.noCollision(box)&&w.getFluidState(feet).isEmpty()&&w.getBlockState(feet.below()).isFaceSturdy(w,feet.below(),Direction.UP);}
 public static boolean safeWolf(ServerLevel w,BlockPos p){return !w.getBlockState(p.below()).is(BlockTags.LEAVES)&&!w.getBlockState(p.below()).is(BlockTags.LOGS)&&!hazard(w,p)&&safeBody(w,new AABB(p.getX()+.1,p.getY(),p.getZ()+.1,p.getX()+.9,p.getY()+1.1,p.getZ()+.9),p);}
 private static BlockPos forestGround(ServerLevel w,BlockPos xz){var p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,xz);for(int i=0;i<24&&p.getY()>w.getMinBuildHeight()+1;i++){var below=w.getBlockState(p.below());if(!below.isAir()&&!below.is(BlockTags.LOGS)&&!below.is(BlockTags.LEAVES)&&below.isSolid())break;p=p.below();}return p;}
 private static boolean hazard(ServerLevel w,BlockPos p){for(var q:List.of(p,p.below())){var b=w.getBlockState(q);if(b.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||b.is(net.minecraft.world.level.block.Blocks.CACTUS)||b.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH)||b.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW)||b.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)||b.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)||b.is(BlockTags.FIRE)||!w.getFluidState(q).isEmpty())return true;}return false;}
 public static boolean safeArenaColumn(ServerLevel w,BlockPos p){return safeWolf(w,p)&&w.noCollision(new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+3.5,p.getZ()+1));}
 public static List<BlockPos> packSpots(ServerLevel w,BlockPos origin,int attempts){var spots=new ArrayList<BlockPos>();var rng=new Random(w.getSeed()^origin.asLong()^w.getGameTime());for(int i=0;i<Math.min(32,attempts);i++){
  double a=rng.nextDouble()*Math.PI*2,r=7+rng.nextDouble()*6;var xz=origin.offset((int)Math.round(Math.cos(a)*r),0,(int)Math.round(Math.sin(a)*r));if(!w.hasChunkAt(xz))continue;var p=forestGround(w,xz);if(Math.abs(p.getY()-origin.getY())>2||!safeWolf(w,p)||spots.stream().anyMatch(q->q.distSqr(p)<9))continue;spots.add(p);if(spots.size()==3)break;
 }return List.copyOf(spots);}
 public static boolean arena(ServerLevel w,BlockPos center){
  if(w.dimension()!=Level.OVERWORLD||!w.hasChunkAt(center)||!w.getBiome(center).is(FORESTS))return false;
  if(!safeBody(w,new AABB(center.getX()-.6,center.getY(),center.getZ()-.6,center.getX()+1.6,center.getY()+3.5,center.getZ()+1.6),center))return false;
  int total=0,safe=0;for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++){if(x*x+z*z>196)continue;total++;var xz=center.offset(x,0,z);if(!w.hasChunkAt(xz))return false;var p=forestGround(w,xz);if(Math.abs(p.getY()-center.getY())<=4&&safeArenaColumn(w,p))safe++;}
  return safe>=Math.ceil(total*.35)&&!excluded(w,center);
 }
 private static boolean excluded(ServerLevel w,BlockPos center){
  if(!org.slavicmyths.worldgen.ManualStructureRecords.get(w).overlapping(new BoundingBox(center.getX()-96,w.getMinBuildHeight(),center.getZ()-96,center.getX()+96,w.getMaxBuildHeight(),center.getZ()+96)).isEmpty())return true;
  if(HuntRecords.get(w).bosses.values().stream().anyMatch(b->b.anchor.distSqr(center)<128*128))return true;
  if(w.getPoiManager().getInRange(h->h.is(net.minecraft.world.entity.ai.village.poi.PoiTypes.HOME),center,96,net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy.ANY).findAny().isPresent())return true;
  // Only loaded structure starts: no locateStructure or chunk loading.
  var exclusion=new BoundingBox(center.getX()-96,w.getMinBuildHeight(),center.getZ()-96,center.getX()+96,w.getMaxBuildHeight(),center.getZ()+96);
  for(int cx=(center.getX()-96)>>4;cx<=(center.getX()+96)>>4;cx++)for(int cz=(center.getZ()-96)>>4;cz<=(center.getZ()+96)>>4;cz++){var chunk=w.getChunkSource().getChunkNow(cx,cz);if(chunk==null)continue;for(var start:chunk.getAllStarts().values())if(start.isValid()&&start.getBoundingBox().intersects(exclusion))return true;}
  return false;
 }
 @SubscribeEvent public static void player(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%40!=0||p.isSpectator())return;var w=p.serverLevel();if(w.dimension()!=Level.OVERWORLD)return;
  int rx=Math.floorDiv(p.blockPosition().getX(),512),rz=Math.floorDiv(p.blockPosition().getZ(),512);if(!eligibleRegion(w.getSeed(),rx,rz))return;var data=WolfRecords.get(w);long region=ChunkPos.asLong(rx,rz);if(data.regions.contains(region)||data.homes.size()>=quota(w))return;
  var checked=CHECKED.computeIfAbsent(w,k->new HashSet<>());long chunkKey=p.chunkPosition().toLong();if(checked.contains(chunkKey)||checked.size()>=8192)return;checked.add(chunkKey);
  var xz=new BlockPos(p.chunkPosition().getMinBlockX()+8,0,p.chunkPosition().getMinBlockZ()+8);if(!w.hasChunkAt(xz))return;var center=forestGround(w,xz);
  if(data.homes.values().stream().anyMatch(q->q.distSqr(center)<768*768)||!arena(w,center))return;
  var wolf=org.slavicmyths.registry.ModEntities.GREY_WOLF.get().create(w);if(wolf==null)return;wolf.moveTo(center.getX()+.5,center.getY(),center.getZ()+.5,0,0);if(!w.addFreshEntity(wolf))return;data.homes.put(wolf.encounterId(),center);data.regions.add(region);data.setDirty();
 }
 private WolfEncounter(){}
}
