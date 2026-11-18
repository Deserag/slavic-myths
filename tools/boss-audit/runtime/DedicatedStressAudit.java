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
public final class DedicatedStressAudit {
 static final String[] IDS=System.getProperty("bossaudit.ids","gneteya,tryaseya,agneya,ledeya,lomeya,neveya,viy,morovoy_raven,morovoy_fire_zmey,nine_mor_serpent,cursed_mytar,bauk").split(",");
 static final boolean LEGAL=Boolean.getBoolean("bossaudit.legalPressure");static String name(){return System.getProperty("bossaudit.stressName",LEGAL?"dedicated-legal-pressure":"dedicated-stress");}
 static final BlockPos HOME=new BlockPos(1000,80,1000);static int index,ticks,hits,changes;static float minStrength=1,maxStrength;static int routeChunks;static Mob boss;static ServerPlayer player;static long began;static String last="";static Set<String> states=new TreeSet<>();static JsonArray rows=new JsonArray();static boolean ready,done;
 static Path out(){return Path.of(System.getProperty("bossaudit.output"));}
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!Boolean.getBoolean("bossaudit.dedicated"))return;try{
  var s=e.getServer();var w=s.overworld();s.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,s);w.setDayTime(6000);
  for(int cx=LEGAL?-8:-4;cx<=(LEGAL?8:4);cx++)for(int cz=LEGAL?-8:-4;cz<=(LEGAL?8:4);cz++){var cp=new ChunkPos(HOME.offset(cx*16,0,cz*16));w.setChunkForced(cp.x,cp.z,true);w.getChunk(cp.x,cp.z);}
  for(int x=-64;x<=64;x++)for(int z=-64;z<=64;z++)w.setBlock(HOME.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
  player=AuditPlayers.create(w,"DedicatedBossAudit");player.setGameMode(GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.setNoGravity(true);player.setPos(Vec3.atCenterOf(HOME.offset(0,1,7)));w.addNewPlayer(player);if(LEGAL)player.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WOODEN_SWORD));ready=true;
 }catch(Throwable ex){ex.printStackTrace();e.getServer().halt(false);}}
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(!Boolean.getBoolean("bossaudit.dedicated")||!ready||done)return;var s=e.getServer();var w=s.overworld();try{
  if(boss==null){
   if(index==IDS.length){done=true;Files.createDirectories(out());Files.writeString(out().resolve(name()+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));Files.writeString(out().resolve(name()+".result"),"COLLECTION_COMPLETED");s.saveEverything(false,true,true);s.halt(false);return;}
   for(var old:w.getEntitiesOfClass(Entity.class,new AABB(HOME).inflate(160),x->!(x instanceof net.minecraft.world.entity.player.Player)))old.discard();
   player.setPos(Vec3.atCenterOf(HOME.offset(0,1,7)));player.removeAllEffects();if(LEGAL)player.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WOODEN_SWORD));boss=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",IDS[index])).create(w);boss.moveTo(HOME.getX()+.5,HOME.getY(),HOME.getZ()+.5,0,0);w.addFreshEntity(boss);boss.setTarget(player);try{boss.getClass().getMethod("setArenaAnchor",BlockPos.class).invoke(boss,HOME);}catch(NoSuchMethodException ignored){}ticks=hits=changes=0;minStrength=1;maxStrength=0;routeChunks=0;last="";states.clear();began=System.nanoTime();System.out.println("BOSS_AUDIT_DEDICATED_START "+IDS[index]);return;
  }
  ticks++;if(LEGAL){var cp=boss.chunkPosition();for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){var next=new ChunkPos(cp.x+dx,cp.z+dz);if(!w.getForcedChunks().contains(next.toLong())){if(++routeChunks>128)throw new IllegalStateException("QA route chunk budget exceeded");w.setChunkForced(next.x,next.z,true);w.getChunk(next.x,next.z);}}player.doTick();}if(LEGAL&&boss.isAlive()){player.setDeltaMovement(Vec3.ZERO);player.setPos(boss.position().add(0,0,boss.getBbWidth()*.5+1.2));player.setOnGround(true);}String state=AiStressAudit.state(boss);states.add(state);if(!state.equals(last))changes++;last=state;
  if(ticks%10==0&&ticks<=600&&boss.isAlive()&&(!LEGAL||boss.getBoundingBox().inflate(3).contains(player.position()))){if(LEGAL&&player.getMainHandItem().isEmpty())player.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WOODEN_SWORD));float strength=player.getAttackStrengthScale(.5F);minStrength=Math.min(minStrength,strength);maxStrength=Math.max(maxStrength,strength);float hp=boss.getHealth();player.attack(boss);if(boss.getHealth()<hp)hits++;}
  if(ticks>=610&&(System.nanoTime()-began)>=30_000_000_000L){var r=new JsonObject();r.addProperty("id",IDS[index]);r.addProperty("simulation_ticks",ticks);r.addProperty("entity_ticks",boss.tickCount);r.addProperty("wall_seconds",(System.nanoTime()-began)/1e9);r.addProperty("accepted_native_player_hits",hits);r.addProperty("state_changes",changes);r.addProperty("states",String.join(";",states));r.addProperty("alive",boss.isAlive());r.addProperty("removed",boss.isRemoved());r.addProperty("health_end",boss.getHealth());r.addProperty("no_gravity",boss.isNoGravity());r.addProperty("y_delta",boss.getY()-HOME.getY());r.addProperty("horizontal_distance",boss.position().subtract(Vec3.atCenterOf(HOME)).multiply(1,0,1).length());r.addProperty("legal_pressure_mode",LEGAL);r.addProperty("extra_route_chunks",routeChunks);r.addProperty("min_attack_strength",minStrength);r.addProperty("max_attack_strength",maxStrength);r.addProperty("attack_damage_attribute",player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));r.addProperty("held_item",player.getMainHandItem().toString());r.addProperty("fixture",LEGAL?"129x129 platform;289 forced chunks; invulnerable Survival proxy follows boss at1.2 beyond AABB; wooden sword every10ticks;NoGravity player; explicitly advanced native ServerPlayer.doTick (no network transport); native boss AI;20TPS; not network MP/damage-output acceptance":"129x129 platform;81 forced test chunks; invulnerable Survival ServerPlayer proxy;20TPS dedicated; NOT network MP or damage-output acceptance");rows.add(r);Files.createDirectories(out());Files.writeString(out().resolve(name()+".partial.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));System.out.println("BOSS_AUDIT_DEDICATED_RESULT "+r);boss.discard();boss=null;index++;}
 }catch(Throwable ex){done=true;ex.printStackTrace();try{Files.createDirectories(out());Files.writeString(out().resolve(name()+".result"),"HARNESS_FAIL "+ex);}catch(Exception ignored){}s.halt(false);}}
}
