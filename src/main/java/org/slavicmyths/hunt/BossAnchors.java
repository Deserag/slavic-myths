package org.slavicmyths.hunt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import java.util.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
/** Controlled regional anchors on chunk lifecycle; only local player-proximity activation every 40 ticks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BossAnchors {
 public static boolean night(ServerLevel w){long t=Math.floorMod(w.getDayTime(),24000);return t>=13000&&t<=23000;}
 public static String key(BossKind k,int rx,int rz){return k.id+":"+rx+":"+rz;}
 public static boolean biome(ServerLevel w,net.minecraft.core.Holder<Biome> b,BossKind kind){ResourceLocation id=b.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);if(kind==BossKind.LIKHO)return id!=null&&(id.equals(ResourceLocation.parse("minecraft:dark_forest"))||id.equals(ResourceLocation.parse("minecraft:dark_forest_hills")));return b.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/core_plains")))||b.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/core_savanna")));}
 public static void prepare(ServerLevel w,int rx,int rz,BossKind kind){HuntRecords data=HuntRecords.get(w);String key=key(kind,rx,rz);if(!data.bossRegions.add(key))return;data.setDirty();if(!BossRules.eligibleRegion(w.getSeed(),rx,rz,kind))return;long seed=BossRules.seed(w.getSeed(),rx,rz,kind);for(int i=0;i<64;i++){int[] xz=BossRules.candidate(seed,rx,rz,i);net.minecraft.core.Holder<Biome> b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(xz[0]>>2,16,xz[1]>>2,w.getChunkSource().randomState().sampler());if(biome(w,b,kind)){data.bosses.put(key,new HuntRecords.Boss(key,kind,new BlockPos(xz[0],0,xz[1]),true));break;}}}
 @SubscribeEvent public static void chunk(ChunkEvent.Load e){if(!(e.getLevel() instanceof ServerLevel))return;ServerLevel w=(ServerLevel)e.getLevel();if(w.dimension()!=Level.OVERWORLD)return;ChunkPos chunk=e.getChunk().getPos();w.getServer().execute(()->{for(BossKind kind:BossKind.values()){int rx=Math.floorDiv(chunk.x,32),rz=Math.floorDiv(chunk.z,32);prepare(w,rx,rz,kind);HuntRecords d=HuntRecords.get(w);String key=key(kind,rx,rz);if(d.bosses.containsKey(key)||!BossRules.eligibleRegion(w.getSeed(),rx,rz,kind))continue;int x=chunk.getMinBlockX()+8,z=chunk.getMinBlockZ()+8;net.minecraft.core.Holder<Biome> b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2,w.getChunkSource().randomState().sampler());if(biome(w,b,kind)){d.bosses.put(key,new HuntRecords.Boss(key,kind,new BlockPos(x,0,z),true));d.setDirty();}}
  // No terrain read here: Load may fire before the chunk's main-thread future completes.
 });}
 public static boolean clear(ServerLevel w,BlockPos p,BossKind kind){float width=kind==BossKind.LIKHO?.95F:1.5F,height=kind==BossKind.LIKHO?3.25F:2.9F;if(kind==BossKind.TUGARIN&&!w.canSeeSky(p)||p.getY()<1||p.getY()+5>=w.getMaxBuildHeight())return false;for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){BlockPos q=p.offset(x,0,z);if(!w.hasChunkAt(q)||!w.getWorldBorder().isWithinBounds(q))return false;for(int y=0;y<5;y++){net.minecraft.world.level.block.state.BlockState s=w.getBlockState(q.above(y));if(!w.getFluidState(q.above(y)).isEmpty()||!s.getCollisionShape(w,q.above(y)).isEmpty()&&!(y==0&&s.is(net.minecraft.world.level.block.Blocks.SNOW)&&s.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS)==1))return false;}}
  if(!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP)||!w.getFluidState(p.below()).isEmpty())return false;return w.noCollision(new AABB(p.getX()+.5-width/2,p.getY()+snow(w,p),p.getZ()+.5-width/2,p.getX()+.5+width/2,p.getY()+height,p.getZ()+.5+width/2))&&org.slavicmyths.kurgan.BurialRecords.get(w).at(p)==null;
 }
 private static double snow(ServerLevel w,BlockPos p){return w.getBlockState(p).is(net.minecraft.world.level.block.Blocks.SNOW)?.125:0;}
 private static BlockPos ground(ServerLevel w,BlockPos xz){BlockPos top=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,xz);for(int y=0;y<48&&top.getY()>1;y++){net.minecraft.world.level.block.state.BlockState state=w.getBlockState(top.below());if(state.is(net.minecraft.world.level.block.Blocks.SNOW)||state.is(net.minecraft.tags.BlockTags.LOGS)||state.is(net.minecraft.tags.BlockTags.LEAVES)||state.getCollisionShape(w,top.below()).isEmpty()){top=top.below();continue;}break;}return top;}
 public static BlockPos find(ServerLevel w,BlockPos center,BossKind kind,int min,int max,int attempts){return find(w,center,kind,min,max,attempts,false);}
 public static BlockPos find(ServerLevel w,BlockPos center,BossKind kind,int min,int max,int attempts,boolean debug){Random rng=new Random(w.getSeed()^center.asLong()^kind.ordinal());for(int i=0;i<attempts;i++){double angle=rng.nextDouble()*Math.PI*2,radius=min+rng.nextDouble()*(max-min);BlockPos xz=center.offset((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius));if(!w.hasChunkAt(xz))continue;BlockPos q=ground(w,xz);if(min==0&&(Math.floorDiv(q.getX(),512)!=Math.floorDiv(center.getX(),512)||Math.floorDiv(q.getZ(),512)!=Math.floorDiv(center.getZ(),512)))continue;double dx=q.getX()-center.getX(),dz=q.getZ()-center.getZ();if(dx*dx+dz*dz<min*min||dx*dx+dz*dz>max*max||!debug&&!biome(w,w.getBiome(q),kind)||!clear(w,q,kind))continue;return q;}return null;}
 public static boolean nearby(ServerLevel w,BlockPos p,BossKind k,int radius){return !w.getEntitiesOfClass(WorldBoss.class,new AABB(p).inflate(radius),b->b.kind()==k&&b.isAlive()).isEmpty();}
 public static boolean spawn(ServerLevel w,HuntRecords.Boss b,ServerPlayer player){if(b.active||!clear(w,b.anchor,b.kind)||nearby(w,b.anchor,b.kind,b.kind==BossKind.LIKHO?96:128))return false;WorldBoss boss=(b.kind==BossKind.LIKHO?ModEntities.LIKHO_ONE_EYED.get():ModEntities.TUGARIN_ZMEY.get()).create(w);if(boss==null)return false;boss.encounter=b.key;boss.home=b.anchor;boss.bossOwner=b.owner;boss.moveTo(b.anchor.getX()+.5,b.anchor.getY()+snow(w,b.anchor),b.anchor.getZ()+.5,0,0);boss.setPersistenceRequired();if(player!=null&&!player.isCreative()&&!player.isSpectator())boss.setTarget(player);if(!w.addFreshEntity(boss))return false;b.target=boss.getUUID();b.active=true;b.safe=true;HuntRecords.get(w).setDirty();return true;}
 @SubscribeEvent public static void player(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer)||e.getEntity().tickCount%40!=0)return;ServerPlayer p=(ServerPlayer)e.getEntity();ServerLevel w=p.serverLevel();if(w.dimension()!=Level.OVERWORLD||p.isSpectator()||w.getDifficulty()==Difficulty.PEACEFUL)return;HuntRecords d=HuntRecords.get(w);int rx=Math.floorDiv(p.blockPosition().getX(),512),rz=Math.floorDiv(p.blockPosition().getZ(),512);
  for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)for(BossKind kind:BossKind.values()){HuntRecords.Boss b=d.bosses.get(key(kind,rx+x,rz+z));if(b==null||!b.natural||b.active||b.ready>w.getGameTime()||kind==BossKind.LIKHO&&!night(w))continue;if((b.safe?b.anchor.distSqr(p.blockPosition()):Math.pow(b.anchor.getX()-p.getX(),2)+Math.pow(b.anchor.getZ()-p.getZ(),2))>(kind==BossKind.LIKHO?64*64:80*80))continue;if(!w.hasChunkAt(b.anchor))continue;if(!b.safe||!clear(w,b.anchor,kind)){BlockPos spot=find(w,b.anchor,kind,0,24,64);if(spot==null)continue;b.anchor=spot;b.safe=true;d.setDirty();}spawn(w,b,p);}
 }
 private BossAnchors(){}
}
