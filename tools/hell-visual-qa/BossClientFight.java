package org.slavicmyths.hellvisualqa;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.slavicmyths.netherboss.NetherSummonBoss;
import org.slavicmyths.registry.ModEntities;

/** Two actual networked Minecraft clients, hosted VM only. Explicitly modified diagnostic arenas. */
public final class BossClientFight {
 private static final JsonArray RECEIPTS=new JsonArray();
 private static final Set<UUID> PEER_BOSSES=new HashSet<>();
 private static final Set<Integer> PHASES=new HashSet<>();
 private static volatile NetherSummonBoss active;
 private static volatile boolean preparing;
 private static int fight,age,waiting,finishAge;private static long phaseAt;private static int previousPhase;
 private static boolean peerOpened,peerJoined,peerSawTwo,playerHurt;
 private static float lastHealth=200;
 private static final java.util.Map<String,Integer> DAMAGE=new java.util.concurrent.ConcurrentHashMap<>();
 public static void recordDamage(org.slavicmyths.netherboss.NetherSummonBoss boss,float amount){if(amount>0)DAMAGE.merge((boss.isMorova()?"morova":"trizna")+"/phase"+boss.phase()+"/action"+boss.action(),1,Integer::sum);}
 public static void openLoopback(net.minecraft.server.MinecraftServer server){
  if(!Boolean.getBoolean("hellvisual.mp"))return;
  try{server.setUsesAuthentication(false);server.getConnection().startTcpServerListener(java.net.InetAddress.getLoopbackAddress(),25591);System.out.println("BOSS_MP_LOOPBACK_READY 127.0.0.1:25591");}
  catch(java.io.IOException ex){throw new RuntimeException(ex);}
 }
 private static void arena(ServerPlayer host){
  var w=host.serverLevel();for(int x=-16;x<=16;x++)for(int z=144;z<=176;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
  for(var p:w.getServer().getPlayerList().getPlayers()){
   p.setGameMode(GameType.SURVIVAL);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);p.setHealth(200);
   p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(35);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.NETHERITE_SWORD));
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.NETHERITE_HELMET));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.NETHERITE_LEGGINGS));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.NETHERITE_BOOTS));
   p.teleportTo(w,p==host?-2.4:2.4,81,160,p==host?-90:90,0);p.getFoodData().setFoodLevel(20);
  }
  var boss=(fight%2==0?ModEntities.MOROVA_DEVA:ModEntities.TRIZNA_PRINCE).get().create(w);boss.moveTo(0,81,160,0,0);boss.setTarget(host);w.addFreshEntity(boss);active=boss;preparing=false;
 }
 public static boolean host(Minecraft mc)throws Exception{
  var server=mc.getSingleplayerServer();if(server==null)return false;
  if(server.getPlayerList().getPlayerCount()<2){if(++waiting>2400)throw new AssertionError("Second actual Minecraft client never joined");return false;}
  if(fight>=30){if(++finishAge<120)return false;
   var result=new JsonObject();result.addProperty("fights",fight);result.addProperty("two_actual_clients",true);result.addProperty("fixture","Survival-mode players with diagnostic 200HP/35 base attack, full netherite and periodic healing; NOT normal balance or owner acceptance");result.add("encounters",RECEIPTS);result.add("boss_damage_contacts",new Gson().toJsonTree(DAMAGE));if(DAMAGE.isEmpty())throw new AssertionError("No actual boss-to-player damage observed");
   Files.writeString(HellVisualCapture.OUT.resolve("multiplayer-fights.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));return true;
  }
  if(active==null){if(!preparing){preparing=true;age=0;previousPhase=0;PHASES.clear();playerHurt=false;server.execute(()->arena(server.getPlayerList().getPlayers().getFirst()));}return false;}
  age++;var visible=mc.level.getEntity(active.getId());
  if(visible instanceof NetherSummonBoss boss){
   PHASES.add(boss.phase());if(boss.phase()!=previousPhase){previousPhase=boss.phase();phaseAt=mc.level.getGameTime();}
   attack(mc,boss,mc.level.getGameTime()-phaseAt>=120);
   if(mc.player.getHealth()<lastHealth)playerHurt=true;lastHealth=mc.player.getHealth();
   if(age%100==0||boss.deathTime>0){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(HellVisualCapture.OUT.resolve(String.format("fight-%02d-phase-%d-t%05d.png",fight,boss.phase(),age)));}}
  }
  if(age%20==0)server.execute(()->{for(var p:server.getPlayerList().getPlayers()){p.heal(80);p.getFoodData().setFoodLevel(20);}});
  if(!active.isAlive()){
   if(PHASES.size()!=3)throw new AssertionError("Missing client-tracked phases in actual MP fight "+fight+" "+PHASES);
   var row=new JsonObject();row.addProperty("fight",fight);row.addProperty("boss",active.isMorova()?"morova_deva":"trizna_prince");row.addProperty("client_ticks",age);row.addProperty("phases",PHASES.toString());row.addProperty("host_observed_health_loss",playerHurt);row.addProperty("boss_uuid",active.getUUID().toString());RECEIPTS.add(row);
   System.out.println("BOSS_MP_FIGHT_PASS "+row);active=null;fight++;
  }
  if(age>4800)throw new AssertionError("Actual MP fight timed out "+fight);return false;
 }
 private static void attack(Minecraft mc,NetherSummonBoss target,boolean enabled){
  mc.setScreen(null);mc.options.hideGui=false;mc.options.setCameraType(CameraType.FIRST_PERSON);
  mc.player.lookAt(EntityAnchorArgument.Anchor.EYES,target.getEyePosition());
  if(enabled&&mc.player.tickCount%20==0&&target.isAlive()&&mc.player.distanceToSqr(target)<20.25){mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);}
 }
 public static void peer(Minecraft mc)throws Exception{
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!peerOpened&&mc.screen instanceof TitleScreen){
   peerOpened=true;Files.createDirectories(HellVisualCapture.OUT);mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(4);mc.options.enableVsync().set(false);mc.options.framerateLimit().set(40);
   var data=new ServerData("Isolated boss QA peer","127.0.0.1:25591",ServerData.Type.OTHER);
   ConnectScreen.startConnecting(mc.screen,mc,ServerAddress.parseString("127.0.0.1:25591"),data,false,null);return;
  }
  if(mc.player!=null&&mc.level!=null){
   peerJoined=true;peerSawTwo|=mc.level.players().size()>=2;
   NetherSummonBoss nearest=null;for(var entity:mc.level.entitiesForRendering())if(entity instanceof NetherSummonBoss boss&&boss.isAlive()&&mc.player.distanceToSqr(boss)<256){PEER_BOSSES.add(boss.getUUID());if(nearest==null||mc.player.distanceToSqr(boss)<mc.player.distanceToSqr(nearest))nearest=boss;}
   if(nearest!=null){if(nearest.phase()!=previousPhase){previousPhase=nearest.phase();phaseAt=mc.level.getGameTime();}attack(mc,nearest,mc.level.getGameTime()-phaseAt>=120);}
  }else if(peerJoined&&mc.screen instanceof DisconnectedScreen){
   var result=new JsonObject();result.addProperty("actual_network_join",true);result.addProperty("two_players_seen",peerSawTwo);result.addProperty("distinct_bosses_tracked",PEER_BOSSES.size());
   Files.writeString(HellVisualCapture.OUT.resolve("peer-finished.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
   if(!peerSawTwo||PEER_BOSSES.size()<30)throw new AssertionError("Peer did not track all 30 fights: "+result);mc.stop();
  }
 }
 private BossClientFight(){}
}
