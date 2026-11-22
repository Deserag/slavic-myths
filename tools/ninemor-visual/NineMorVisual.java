package org.slavicmyths.verify;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.ninemor.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class NineMorVisual {
 static boolean opened,requested,ready,done;static volatile String error;static int frames,serverAge;static NineMorSerpent boss;static net.minecraft.server.level.ServerPlayer left,right;static boolean airborne,landed;static int landingAge;static double maxRise;
 static Path out(){return Minecraft.getInstance().gameDirectory.toPath().resolve("evidence");}
 static Object field(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void set(Object o,String n,Object value)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,value);}
 @SuppressWarnings("unchecked") static void data(String field,int value)throws Exception{var f=NineMorSerpent.class.getDeclaredField(field);f.setAccessible(true);boss.getEntityData().set((net.minecraft.network.syncher.EntityDataAccessor<Integer>)f.get(null),value);}
 static void camera(double x,double y,double z,float yaw,float pitch){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.teleportTo(p.serverLevel(),x,y,z,yaw,pitch);});}
 static void capture(String name)throws Exception{var mc=Minecraft.getInstance();Files.createDirectories(out());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out().resolve(name+".png"));}System.out.println("NINEMOR_FRAME "+name);}
 static net.minecraft.server.level.ServerPlayer player(net.minecraft.server.level.ServerLevel w,String name,double x,double z){var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),name));p.setGameMode(GameType.SURVIVAL);p.getAbilities().invulnerable=true;p.setNoGravity(true);p.moveTo(x,80,z);return p;}
 static void finish(String failure){if(done)return;done=true;try{Files.createDirectories(out());Files.writeString(out().resolve("result.txt"),failure==null?"PASS: native client model, two targets, real takeoff and landing; maximum rise="+maxRise:"FAIL: "+failure);}catch(Exception e){e.printStackTrace();}Minecraft.getInstance().stop();}
 @SubscribeEvent public static void client(ClientTickEvent.Post event){if(done)return;var mc=Minecraft.getInstance();try{
  mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.1D);
  if(error!=null)throw new IllegalStateException(error);
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){opened=true;mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.options.renderDistance().set(6);mc.options.simulationDistance().set(5);mc.options.fov().set(55);mc.options.enableVsync().set(false);mc.options.framerateLimit().set(60);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.createWorldOpenFlows().createFreshLevel("NineMor-Isolated-"+System.currentTimeMillis(),new LevelSettings("Isolated NineMor visual test",GameType.SPECTATOR,false,net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(22112026L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{try{
   var server=mc.getSingleplayerServer();var w=server.overworld();w.setDayTime(6000);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
   for(int x=-45;x<=45;x++)for(int z=-35;z<=55;z++)w.setBlock(new BlockPos(x,79,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
   var observer=server.getPlayerList().getPlayers().getFirst();observer.setGameMode(GameType.SPECTATOR);observer.teleportTo(w,0,89,49,180,6);
   left=player(w,"TargetLeft",-18,25);right=player(w,"TargetRight",18,25);
   observer.connection.send(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(java.util.List.of(left,right)));
   w.addNewPlayer(left);w.addNewPlayer(right);
   boss=NineMorContent.BOSS.get().create(w);boss.moveTo(0,80,0,0,0);var tag=new net.minecraft.nbt.CompoundTag();boss.saveWithoutId(tag);tag.putBoolean("Ritual",false);tag.putInt("RitualAge",60);boss.readAdditionalSaveData(tag);boss.setTarget(left);w.addFreshEntity(boss);ready=true;
  }catch(Throwable e){error=e.toString();e.printStackTrace();}});}
  if(!ready)return;frames++;
  if(frames==95)verifyRig(mc);
  if(frames==100)capture("01-ground-front");
  if(frames==110)camera(44,88,8,90,5);
  if(frames==170)capture("02-ground-side");
  if(frames==180)camera(0,91,44,180,5);
  if(frames==240)capture("03-independent-heads");
  if(frames==250)camera(30,92,45,146,8);
  if(frames==310)capture("04-ground-three-quarter");
  if(frames==320){camera(boss.getX()+30,boss.getY()+12,boss.getZ()+35,139.4F,10);mc.getSingleplayerServer().execute(()->{try{set(boss,"nextFlight",0);}catch(Exception e){error=e.toString();}});}
  if(airborne&&boss.flightPhase()==2&&boss.flightAge()>=7&&boss.flightAge()%8<=1)captureOnce("07-wing-spread-"+(boss.flightAge()/8));
  if(landed&&frames>330){captureOnce("06-returned-to-ground");if(frames>landingAge+45)finish(null);}
  if(frames>750)throw new IllegalStateException("Flight did not land in bounded time");
 }catch(Throwable e){e.printStackTrace();finish(e.toString());}}
 static void verifyRig(Minecraft mc)throws Exception{
  var clientBoss=(NineMorSerpent)mc.level.getEntity(boss.getId());if(clientBoss==null)throw new IllegalStateException("Client boss missing");
  var model=new NineMorModel();var joints=new java.util.ArrayList<net.minecraft.client.model.geom.ModelPart>();
  for(String name:new String[]{"necks","neckMids","neckTips","heads","legs","tails","wings","wingElbows","wingWrists"})for(var p:(net.minecraft.client.model.geom.ModelPart[])field(model,name))joints.add(p);
  model.setupAnim(clientBoss,0,0,clientBoss.tickCount,0,0);
  for(var p:joints){var initial=p.getInitialPose();if(Math.abs(p.x-initial.x)+Math.abs(p.y-initial.y)+Math.abs(p.z-initial.z)>.00001)throw new IllegalStateException("Animated joint detached from baked pivot");}
  var body=(net.minecraft.client.model.geom.ModelPart)field(model,"body");
  var neck=(net.minecraft.client.model.geom.ModelPart[])field(model,"necks");var mid=(net.minecraft.client.model.geom.ModelPart[])field(model,"neckMids");var tip=(net.minecraft.client.model.geom.ModelPart[])field(model,"neckTips");var head=(net.minecraft.client.model.geom.ModelPart[])field(model,"heads");
  for(int i=0;i<9;i++){
   var pose=new com.mojang.blaze3d.vertex.PoseStack();pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180-clientBoss.getYRot()));pose.scale(-4,-4,4);pose.translate(0,-1.501,0);body.translateAndRotate(pose);neck[i].translateAndRotate(pose);mid[i].translateAndRotate(pose);tip[i].translateAndRotate(pose);head[i].translateAndRotate(pose);
   var point=pose.last().pose().transformPosition(new org.joml.Vector3f(0,0,NineMorRig.MOUTH_DEPTH/16F));var actual=clientBoss.position().add(point.x,point.y,point.z);
   if(actual.distanceToSqr(clientBoss.headMouth(i))>.00001)throw new IllegalStateException("Head mouth origin mismatch: "+i);
  }
  Files.createDirectories(out());Files.writeString(out().resolve("native-rig-check.txt"),"PASS: fixed joint origins and 9 production model mouth transforms match shared server rig");
 } static java.util.Set<String> captured=new java.util.HashSet<>();static void captureOnce(String name)throws Exception{if(captured.add(name))capture(name);}
 @SubscribeEvent public static void server(ServerTickEvent.Post event){if(!ready||done)return;try{
  serverAge++;if(boss==null||!boss.isAlive())throw new IllegalStateException("Boss missing");
  if(frames<320)set(boss,"nextFlight",500);
  if(boss.flightPhase()!=0)airborne=true;
  if(airborne){maxRise=Math.max(maxRise,boss.getY()-80);if(maxRise>3.0)throw new IllegalStateException("Excessive flight height: "+maxRise);if(boss.flightPhase()==0&&!landed){landed=true;landingAge=frames;System.out.println("NINEMOR_REAL_LANDING height="+boss.getY()+" maxRise="+maxRise);}}
 }catch(Throwable e){error=e.toString();e.printStackTrace();}}
}