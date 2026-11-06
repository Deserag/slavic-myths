package org.slavicmyths.bossaudit;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
@EventBusSubscriber(modid="slavicmyths_bossaudit")
public final class DedicatedPerformanceAudit {
 static final BlockPos HOME=new BlockPos(1000,80,1000);static final int[] COUNTS={0,1,5,10};static List<String> ids;static int group,step,ticks;static ServerPlayer player;static final List<Mob> actors=new ArrayList<>();static final List<Long> samples=new ArrayList<>();static JsonArray rows=new JsonArray();static boolean ready,done;static long start;
 static Path out(){return Path.of(System.getProperty("bossaudit.output"));}
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!Boolean.getBoolean("bossaudit.performance"))return;try{
  ids=java.util.Arrays.stream(new String(DedicatedPerformanceAudit.class.getResourceAsStream("/boss-audit-candidates.txt").readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).split("\n")).map(String::strip).filter(x->!x.isBlank()).toList();var s=e.getServer();var w=s.overworld();s.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,s);w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,s);w.setDayTime(6000);
  for(int cx=-4;cx<=4;cx++)for(int cz=-4;cz<=4;cz++){var cp=new ChunkPos(HOME.offset(cx*16,0,cz*16));w.setChunkForced(cp.x,cp.z,true);w.getChunk(cp.x,cp.z);}
  for(int x=-64;x<=64;x++)for(int z=-64;z<=64;z++)w.setBlock(HOME.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
  player=AuditPlayers.create(w,"BossPerfProxy");player.setGameMode(GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.setNoGravity(true);player.setPos(Vec3.atCenterOf(HOME.above()));w.addNewPlayer(player);ready=true;startCase(w);
 }catch(Throwable ex){ex.printStackTrace();e.getServer().halt(false);}}
 static void startCase(ServerLevel w)throws Exception{
  for(var old:w.getEntitiesOfClass(Entity.class,new AABB(HOME).inflate(160),x->!(x instanceof net.minecraft.world.entity.player.Player)))old.discard();actors.clear();player.removeAllEffects();player.setDeltaMovement(Vec3.ZERO);player.setPos(Vec3.atCenterOf(HOME.above()));ticks=0;samples.clear();
  var type=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",ids.get(group)));
  for(int i=0;i<COUNTS[step];i++){var m=(Mob)type.create(w);double radius=Math.max(10,Math.min(38,m.getBbWidth()*1.5+8));double a=2*Math.PI*i/Math.max(1,COUNTS[step]);m.moveTo(HOME.getX()+Math.sin(a)*radius,HOME.getY(),HOME.getZ()+Math.cos(a)*radius,180,0);w.addFreshEntity(m);m.setTarget(player);try{m.getClass().getMethod("setArenaAnchor",BlockPos.class).invoke(m,HOME);}catch(NoSuchMethodException ignored){}actors.add(m);}start=System.nanoTime();System.out.println("BOSS_AUDIT_PERF_START id="+ids.get(group)+" count="+COUNTS[step]);
 }
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(!Boolean.getBoolean("bossaudit.performance")||!ready||done)return;var s=e.getServer();try{
  ticks++;if(ticks>40&&ticks<=140)samples.add(s.getTickTimesNanos()[s.getTickCount()%100]);if(ticks<140)return;
  var sorted=new ArrayList<>(samples);Collections.sort(sorted);var r=new JsonObject();r.addProperty("id",ids.get(group));r.addProperty("count_requested",COUNTS[step]);r.addProperty("alive_end",actors.stream().filter(Entity::isAlive).count());r.addProperty("ticking_entities_end",actors.stream().filter(m->m.tickCount>=120).count());r.addProperty("samples",samples.size());r.addProperty("mean_mspt",samples.stream().mapToLong(Long::longValue).average().orElse(0)/1e6);r.addProperty("p50_mspt",sorted.get(sorted.size()/2)/1e6);r.addProperty("p95_mspt",sorted.get((int)(sorted.size()*.95))/1e6);r.addProperty("max_mspt",sorted.get(sorted.size()-1)/1e6);r.addProperty("wall_seconds",(System.nanoTime()-start)/1e9);var raw=new JsonArray();samples.forEach(raw::add);r.add("tick_times_nanos",raw);r.addProperty("method","Vanilla MinecraftServer tickTimesNanos at ServerTickEvent.Post;40warmup+100samples;single run;counts0/1/5/10; synthetic unlinked encounters and invulnerable Survival proxy; NOT client FPS/network MP or statistically repeated benchmark");rows.add(r);Files.createDirectories(out());Files.writeString(out().resolve("server-performance.partial.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));System.out.println("BOSS_AUDIT_PERF_RESULT id="+ids.get(group)+" count="+COUNTS[step]+" mean_mspt="+r.get("mean_mspt"));
  if(++step==COUNTS.length){step=0;group++;}if(group==ids.size()){done=true;Files.writeString(out().resolve("server-performance.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));Files.writeString(out().resolve("server-performance.result"),"COLLECTION_COMPLETED");s.halt(false);}else startCase(s.overworld());
 }catch(Throwable ex){done=true;ex.printStackTrace();try{Files.createDirectories(out());Files.writeString(out().resolve("server-performance.result"),"HARNESS_FAIL "+ex);}catch(Exception ignored){}s.halt(false);}}
}
