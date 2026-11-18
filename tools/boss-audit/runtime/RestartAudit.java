package org.slavicmyths.bossaudit;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
/** Two independent dedicated server processes, same disposable world. No NBT-only relog claim. */
@EventBusSubscriber(modid="slavicmyths_bossaudit")
public final class RestartAudit {
 static final String[] IDS={"gneteya","tryaseya","agneya","ledeya","lomeya","neveya","viy"};
 static final List<Mob> actors=new ArrayList<>();static JsonArray before;static boolean read,ready,done;static int ticks;
 static Path out(){return Path.of(System.getProperty("bossaudit.output"));}
 static String phase(Mob m){for(String key:new String[]{"phaseTwo","enraged"})try{return key+":"+m.getClass().getMethod(key).invoke(m);}catch(Exception ignored){}return "NO_STANDARD_PHASE_ACCESSOR";}
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!Boolean.getBoolean("bossaudit.restart"))return;try{
  var w=e.getServer().overworld();Files.createDirectories(out());var file=out().resolve("restart-before.json");read=Files.exists(file);if(read)before=JsonParser.parseString(Files.readString(file)).getAsJsonArray();
  for(int i=0;i<IDS.length;i++){var pos=new BlockPos(2000+i*32,140,2000);var cp=new ChunkPos(pos);w.setChunkForced(cp.x,cp.z,true);w.getChunk(cp.x,cp.z);if(!read){var mob=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",IDS[i])).create(w);mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);mob.setNoAi(false);mob.setNoGravity(true);mob.setPersistenceRequired();mob.setHealth(mob.getMaxHealth()*.45F);w.addFreshEntity(mob);actors.add(mob);}}
  ready=true;
 }catch(Throwable ex){ex.printStackTrace();e.getServer().halt(false);}}
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(!Boolean.getBoolean("bossaudit.restart")||!ready||done||++ticks<(read?20:80))return;done=true;try{
  if(!read){var rows=new JsonArray();for(int i=0;i<actors.size();i++){var m=actors.get(i);var row=new JsonObject();row.addProperty("id",IDS[i]);row.addProperty("uuid",m.getUUID().toString());row.addProperty("health",m.getHealth());row.addProperty("phase",phase(m));row.addProperty("action",AiStressAudit.state(m));rows.add(row);}e.getServer().saveEverything(false,true,true);Files.writeString(out().resolve("restart-before.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));Files.writeString(out().resolve("restart-write.result"),"WORLD_SAVED_BEFORE_PROCESS_EXIT");}
  else{var rows=new JsonArray();boolean pass=true;for(var item:before){var old=item.getAsJsonObject();var found=e.getServer().overworld().getEntity(UUID.fromString(old.get("uuid").getAsString()));var row=new JsonObject();row.addProperty("id",old.get("id").getAsString());row.addProperty("uuid",old.get("uuid").getAsString());boolean present=found instanceof Mob m&&m.isAlive();row.addProperty("present_alive_after_real_restart",present);boolean hp=false,ph=false;if(present){var m=(Mob)found;row.addProperty("health_after",m.getHealth());row.addProperty("phase_after",phase(m));row.addProperty("action_after",AiStressAudit.state(m));hp=Math.abs(m.getHealth()-old.get("health").getAsFloat())<.01;ph=phase(m).equals(old.get("phase").getAsString());}row.addProperty("health_preserved",hp);row.addProperty("phase_preserved",ph);row.addProperty("scope","Two dedicated JVMs and saved disposable world; NoGravity fixture with native AI, no player target; not real client reconnect, active encounter sessions, attack resume or network multiplayer");pass&=present&&hp&&ph;rows.add(row);}Files.writeString(out().resolve("restart-after.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));Files.writeString(out().resolve("restart-read.result"),pass?"PASS_PERSISTENCE_SCOPE_ONLY":"FAIL_PERSISTENCE_SCOPE");}
  e.getServer().halt(false);
 }catch(Throwable ex){ex.printStackTrace();e.getServer().halt(false);}}
}
