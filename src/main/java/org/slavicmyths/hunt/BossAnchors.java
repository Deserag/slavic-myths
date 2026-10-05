package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
/** Controlled regional anchors on chunk lifecycle; only local player-proximity activation every 40 ticks. */
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class BossAnchors {
 public static boolean night(ServerWorld w){long t=Math.floorMod(w.getDayTime(),24000);return t>=13000&&t<=23000;}
 public static String key(BossKind k,int rx,int rz){return k.id+":"+rx+":"+rz;}
 public static boolean biome(ServerWorld w,Biome b,BossKind kind){ResourceLocation id=w.registryAccess().registryOrThrow(net.minecraft.util.registry.Registry.BIOME_REGISTRY).getKey(b);if(kind==BossKind.LIKHO)return id!=null&&(id.equals(new ResourceLocation("minecraft:dark_forest"))||id.equals(new ResourceLocation("minecraft:dark_forest_hills")));return b.getBiomeCategory()==Biome.Category.PLAINS||b.getBiomeCategory()==Biome.Category.SAVANNA;}
 public static void prepare(ServerWorld w,int rx,int rz,BossKind kind){HuntRecords data=HuntRecords.get(w);String key=key(kind,rx,rz);if(!data.bossRegions.add(key))return;data.setDirty();if(!BossRules.eligibleRegion(w.getSeed(),rx,rz,kind))return;long seed=BossRules.seed(w.getSeed(),rx,rz,kind);for(int i=0;i<64;i++){int[] xz=BossRules.candidate(seed,rx,rz,i);Biome b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(xz[0]>>2,16,xz[1]>>2);if(biome(w,b,kind)){data.bosses.put(key,new HuntRecords.Boss(key,kind,new BlockPos(xz[0],0,xz[1]),true));break;}}}
 @SubscribeEvent public static void chunk(ChunkEvent.Load e){if(!(e.getWorld() instanceof ServerWorld))return;ServerWorld w=(ServerWorld)e.getWorld();if(w.dimension()!=World.OVERWORLD)return;ChunkPos chunk=e.getChunk().getPos();w.getServer().execute(()->{for(BossKind kind:BossKind.values()){int rx=Math.floorDiv(chunk.x,32),rz=Math.floorDiv(chunk.z,32);prepare(w,rx,rz,kind);HuntRecords d=HuntRecords.get(w);String key=key(kind,rx,rz);if(d.bosses.containsKey(key)||!BossRules.eligibleRegion(w.getSeed(),rx,rz,kind))continue;int x=chunk.getMinBlockX()+8,z=chunk.getMinBlockZ()+8;Biome b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2);if(biome(w,b,kind)){d.bosses.put(key,new HuntRecords.Boss(key,kind,new BlockPos(x,0,z),true));d.setDirty();}}
  // No terrain read here: Load may fire before the chunk's main-thread future completes.
 });}
 public static boolean clear(ServerWorld w,BlockPos p,BossKind kind){float width=kind==BossKind.LIKHO?.95F:1.5F,height=kind==BossKind.LIKHO?3.25F:2.9F;if(kind==BossKind.TUGARIN&&!w.canSeeSky(p)||p.getY()<1||p.getY()+5>=w.getMaxBuildHeight())return false;for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){BlockPos q=p.offset(x,0,z);if(!w.hasChunkAt(q)||!w.getWorldBorder().isWithinBounds(q))return false;for(int y=0;y<5;y++){net.minecraft.block.BlockState s=w.getBlockState(q.above(y));if(!w.getFluidState(q.above(y)).isEmpty()||!s.getCollisionShape(w,q.above(y)).isEmpty()&&!(y==0&&s.is(net.minecraft.block.Blocks.SNOW)&&s.getValue(net.minecraft.block.SnowBlock.LAYERS)==1))return false;}}
  if(!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP)||!w.getFluidState(p.below()).isEmpty())return false;return w.noCollision(new AxisAlignedBB(p.getX()+.5-width/2,p.getY()+snow(w,p),p.getZ()+.5-width/2,p.getX()+.5+width/2,p.getY()+height,p.getZ()+.5+width/2))&&org.slavicmyths.kurgan.BurialRecords.get(w).at(p)==null;
 }
 private static double snow(ServerWorld w,BlockPos p){return w.getBlockState(p).is(net.minecraft.block.Blocks.SNOW)?.125:0;}
 private static BlockPos ground(ServerWorld w,BlockPos xz){BlockPos top=w.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,xz);for(int y=0;y<48&&top.getY()>1;y++){net.minecraft.block.BlockState state=w.getBlockState(top.below());if(state.is(net.minecraft.block.Blocks.SNOW)||state.is(net.minecraft.tags.BlockTags.LOGS)||state.getMaterial()==net.minecraft.block.material.Material.LEAVES||state.getCollisionShape(w,top.below()).isEmpty()){top=top.below();continue;}break;}return top;}
 public static BlockPos find(ServerWorld w,BlockPos center,BossKind kind,int min,int max,int attempts){return find(w,center,kind,min,max,attempts,false);}
 public static BlockPos find(ServerWorld w,BlockPos center,BossKind kind,int min,int max,int attempts,boolean debug){Random rng=new Random(w.getSeed()^center.asLong()^kind.ordinal());for(int i=0;i<attempts;i++){double angle=rng.nextDouble()*Math.PI*2,radius=min+rng.nextDouble()*(max-min);BlockPos xz=center.offset((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius));if(!w.hasChunkAt(xz))continue;BlockPos q=ground(w,xz);if(min==0&&(Math.floorDiv(q.getX(),512)!=Math.floorDiv(center.getX(),512)||Math.floorDiv(q.getZ(),512)!=Math.floorDiv(center.getZ(),512)))continue;double dx=q.getX()-center.getX(),dz=q.getZ()-center.getZ();if(dx*dx+dz*dz<min*min||dx*dx+dz*dz>max*max||!debug&&!biome(w,w.getBiome(q),kind)||!clear(w,q,kind))continue;return q;}return null;}
 public static boolean nearby(ServerWorld w,BlockPos p,BossKind k,int radius){return !w.getEntitiesOfClass(WorldBoss.class,new AxisAlignedBB(p).inflate(radius),b->b.kind()==k&&b.isAlive()).isEmpty();}
 public static boolean spawn(ServerWorld w,HuntRecords.Boss b,ServerPlayerEntity player){if(b.active||!clear(w,b.anchor,b.kind)||nearby(w,b.anchor,b.kind,b.kind==BossKind.LIKHO?96:128))return false;WorldBoss boss=(b.kind==BossKind.LIKHO?ModEntities.LIKHO_ONE_EYED.get():ModEntities.TUGARIN_ZMEY.get()).create(w);if(boss==null)return false;boss.encounter=b.key;boss.home=b.anchor;boss.bossOwner=b.owner;boss.moveTo(b.anchor.getX()+.5,b.anchor.getY()+snow(w,b.anchor),b.anchor.getZ()+.5,0,0);boss.setPersistenceRequired();if(player!=null&&!player.isCreative()&&!player.isSpectator())boss.setTarget(player);if(!w.addFreshEntity(boss))return false;b.target=boss.getUUID();b.active=true;b.safe=true;HuntRecords.get(w).setDirty();return true;}
 @SubscribeEvent public static void player(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayerEntity)||e.player.tickCount%40!=0)return;ServerPlayerEntity p=(ServerPlayerEntity)e.player;ServerWorld w=p.getLevel();if(w.dimension()!=World.OVERWORLD||p.isSpectator()||w.getDifficulty()==Difficulty.PEACEFUL)return;HuntRecords d=HuntRecords.get(w);int rx=Math.floorDiv(p.blockPosition().getX(),512),rz=Math.floorDiv(p.blockPosition().getZ(),512);
  for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)for(BossKind kind:BossKind.values()){HuntRecords.Boss b=d.bosses.get(key(kind,rx+x,rz+z));if(b==null||!b.natural||b.active||b.ready>w.getGameTime()||kind==BossKind.LIKHO&&!night(w))continue;if((b.safe?b.anchor.distSqr(p.blockPosition()):Math.pow(b.anchor.getX()-p.getX(),2)+Math.pow(b.anchor.getZ()-p.getZ(),2))>(kind==BossKind.LIKHO?64*64:80*80))continue;if(!w.hasChunkAt(b.anchor))continue;if(!b.safe||!clear(w,b.anchor,kind)){BlockPos spot=find(w,b.anchor,kind,0,24,64);if(spot==null)continue;b.anchor=spot;b.safe=true;d.setDirty();}spawn(w,b,p);}
 }
 private BossAnchors(){}
}
