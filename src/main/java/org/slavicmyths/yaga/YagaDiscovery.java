package org.slavicmyths.yaga;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import org.slavicmyths.tales.*;
import org.slavicmyths.worldgen.SiteChunks;

/** Book and command requests share one actual build, not a remote chunk search. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class YagaDiscovery {
 private static final Map<MinecraftServer,Build> BUILDS=new HashMap<>();
 public static boolean active(MinecraftServer server){return BUILDS.containsKey(server);}
 private static final class Build {
  final ServerLevel world;final Set<UUID> players=new LinkedHashSet<>();final List<BlockPos> candidates=new ArrayList<>();final SiteChunks chunks;
  final long deadline=System.nanoTime()+180_000_000_000L;int candidate,chunk;List<ChunkPos> footprint=List.of();YagaPlacement.Cursor placement;String reason="surface";
  Build(ServerPlayer p,BlockPos preferred){world=p.server.overworld();chunks=new SiteChunks(world);players.add(p.getUUID());
   for(int r:new int[]{192,320,448})for(int[] d:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}){var at=p.blockPosition().offset(d[0]*r,0,d[1]*r);if(!loaded(world,at)&&candidates.size()<3){var biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(at.getX()>>2,16,at.getZ()>>2,world.getChunkSource().randomState().sampler());if(biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST))candidates.add(at);}}
   for(int r:new int[]{40,72,104})for(int[] d:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}){var at=p.blockPosition().offset(d[0]*r,0,d[1]*r);if(loaded(world,at))candidates.add(at);}
   // The saved untouched reservation precedes the bounded local candidates.
   if(preferred!=null&&!loaded(world,preferred)&&!candidates.contains(preferred))candidates.add(0,preferred);
  }
 }
 private static boolean loaded(ServerLevel w,BlockPos p){for(int x=(p.getX()-10)>>4;x<=(p.getX()+10)>>4;x++)for(int z=(p.getZ()-17)>>4;z<=(p.getZ()+10)>>4;z++)if(w.getChunkSource().getChunkNow(x,z)==null)return false;return true;}
 public static boolean create(ServerPlayer p,BlockPos preferred){
  var w=p.server.overworld();var data=YagaData.get(w);if(data.placed){notify(p);return true;}
  var old=BUILDS.get(p.server);if(old!=null){old.players.add(p.getUUID());return true;}
  var build=new Build(p,preferred);if(build.candidates.isEmpty()){p.displayClientMessage(Component.literal("Поблизости нет подходящего лесного места. Дальние чанки не перебираются."),false);return false;}
  BUILDS.put(p.server,build);p.displayClientMessage(Component.literal("Создаётся избушка: сначала новая площадка, затем загруженная местность. Мир не сканируется."),false);return true;
 }
 @net.neoforged.bus.api.SubscribeEvent public static void step(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
  var b=BUILDS.get(e.getServer());if(b==null)return;
  try{
   if(b.placement!=null){b.placement.step(256,2_000_000);if(!b.placement.done())return;if(b.placement.result.ok){finish(e.getServer(),b,true);return;}b.reason=b.placement.result.reason;if(b.reason.equals("rollback")){finish(e.getServer(),b,false);return;}var data=YagaData.get(b.world);var failed=b.candidates.get(b.candidate);if(data.anchor!=null&&data.anchor.getX()==failed.getX()&&data.anchor.getZ()==failed.getZ()){data.examined.add(data.anchor.asLong());data.anchor=null;data.setDirty();}b.placement=null;b.candidate++;b.footprint=List.of();}
   if(System.nanoTime()>b.deadline||b.candidate>=b.candidates.size()){finish(e.getServer(),b,false);return;}
   var at=b.candidates.get(b.candidate);
   if(!b.world.getWorldBorder().isWithinBounds(at.offset(-12,0,-20))||!b.world.getWorldBorder().isWithinBounds(at.offset(12,0,12))){b.candidate++;return;}
   if(b.footprint.isEmpty()){var list=new ArrayList<ChunkPos>();for(int x=(at.getX()-10)>>4;x<=(at.getX()+10)>>4;x++)for(int z=(at.getZ()-17)>>4;z<=(at.getZ()+10)>>4;z++)list.add(new ChunkPos(x,z));b.footprint=list;b.chunk=0;}
   if(b.chunk<b.footprint.size()){if(b.chunks.ready(b.footprint.get(b.chunk)))b.chunk++;return;}
   b.placement=YagaHut.beginPlace(b.world,at);
  }catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().warn("Yaga creation stopped safely",failure);b.reason="internal";finish(e.getServer(),b,false);}
 }
 private static void notify(ServerPlayer p){reconcile(p,false);var delta=new net.minecraft.nbt.CompoundTag();delta.put("Hints",TaleService.hints(p));TaleNetwork.send(p,delta,false,false);}
 private static void finish(MinecraftServer server,Build b,boolean success){BUILDS.remove(server);b.chunks.close();for(var id:b.players){var p=server.getPlayerList().getPlayer(id);if(p!=null){p.displayClientMessage(success?Component.translatable("research.yaga.found"):Component.literal("Избушка не создана: "+b.reason+". Дальнейшей загрузки чанков нет."),false);notify(p);}}}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){var b=BUILDS.remove(e.getServer());if(b!=null){if(b.placement!=null){b.placement.abort();while(!b.placement.done())b.placement.step(65536,1_000_000_000L);}b.chunks.close();}}
 public static boolean eligible(ServerPlayer p){return YagaServices.killed(p,"name_your_misfortune")||YagaServices.killed(p,"steppe_champion")||YagaServices.killed(p,"defeat_nightingale");}
 public static void reconcile(ServerPlayer p,boolean create){
  if(!eligible(p))return;TaleService.promote(p,"slavicmyths:yaga_search",1,false);var world=p.server.overworld();var data=YagaData.get(world);
  if(!data.placed&&create&&p.level().dimension()==Level.OVERWORLD){long now=world.getGameTime();var memory=p.getPersistentData();if(now<memory.getLong("YagaSearchReady"))return;memory.putLong("YagaSearchReady",now+40);YagaHut.prepare(world);create(p,data.anchor);}
  if(data.placed&&data.anchor!=null&&data.npc!=null){TaleService.hint(p,"slavicmyths:yaga_search",Level.OVERWORLD.location().toString(),data.npc,data.anchor);TaleService.promote(p,"slavicmyths:yaga_search",2,false);}
 }
 private YagaDiscovery(){}
}
