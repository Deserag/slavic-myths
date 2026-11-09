package org.slavicmyths.verifyclient;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.posad.*;

/** Automated real-client render checks; hidden GLFW context and disposable world only. */
@Mod(value="slavicmyths_posadnative",dist=Dist.CLIENT)
@EventBusSubscriber(modid="slavicmyths_posadnative",value=Dist.CLIENT)
public final class PosadClientQa {
 private record View(String name,boolean nether,double x,double y,double z,double tx,double ty,double tz){}
 private static final List<View> views=new ArrayList<>();
 private static boolean opened,started,finished;private static volatile boolean ready;private static volatile String failure;
 private static int clientTicks,stage=-1,captureAt,shot=-1;private static int build=-1,buildTicks;private static boolean building;
 private static Path output(){return Minecraft.getInstance().gameDirectory.toPath().resolve("posad-client-evidence");}
 private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
 private static void stop(Throwable error){
  if(finished)return;finished=true;
  try{Files.createDirectories(output());Files.writeString(output().resolve("result.txt"),error==null?"PASS: real client render captures; owner visual acceptance remains pending":"FAIL: "+error);}catch(Exception write){write.printStackTrace();}
  if(error!=null)error.printStackTrace();System.out.println(error==null?"POSAD_REAL_CLIENT_CAPTURE_PASS":"POSAD_REAL_CLIENT_CAPTURE_FAIL "+error);Minecraft.getInstance().stop();
 }
 @SubscribeEvent public static void server(ServerTickEvent.Post event){
  if(build<0||build>=(Boolean.getBoolean("posadclient.single")?1:4)||failure!=null)return;var server=event.getServer();var world=server.overworld();int ax=22500+build*300,deck=-61;
  try{
   if(!building){
    if(org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().stream().anyMatch(e->e.family().equals("nether_posad")&&e.arrival().getX()==ax&&e.arrival().getZ()==0)){build++;if(build==(Boolean.getBoolean("posadclient.single")?1:4))ready=true;return;}

    building=true;buildTicks=0;var source=server.createCommandSourceStack().withLevel(world).withPermission(2);
    String command="slavicmyths dev posad create "+build+" "+ax+" "+deck+" 0";
    System.out.println("POSAD_CLIENT_ACTUAL_COMMAND /"+command);server.getCommands().performPrefixedCommand(source,command);
   }
   check(++buildTicks<2400,"Timed out waiting for manual city "+build);
   if(org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().stream().anyMatch(e->e.family().equals("nether_posad")&&e.arrival().getX()==ax&&e.arrival().getZ()==0)){
    System.out.println("POSAD_CLIENT_CITY_READY rotation="+build+" deck="+deck);build++;building=false;if(build==(Boolean.getBoolean("posadclient.single")?1:4))ready=true;
   }
  }catch(Throwable error){failure=error.toString();error.printStackTrace();}
 }
 private static double[] rotated(double x,double z,int turns){return switch(turns){case 0->new double[]{x,z};case 1->new double[]{112-z,x};case 2->new double[]{112-x,112-z};default->new double[]{z,112-x};};}
 private static void addNative(String name,double x,double y,double z,double tx,double ty,double tz){
  var p=rotated(x,z,1);var target=rotated(tx,tz,1);views.add(new View(name,true,3344+p[0],55+y,-48+p[1],3344+target[0],55+ty,-48+target[1]));
 }
 private static void prepareViews(){
  for(int turns=0;turns<4;turns++)views.add(new View("city-rotation-"+turns,false,turns*300+56,4,128,turns*300+56,-57,56));
  views.add(new View("veche-front",false,56,-38,59,56,-53,24));
  views.add(new View("veche-back",false,56,-38,-8,56,-53,24));
  views.add(new View("veche-west-roof",false,20,-38,24,56,-49,24));
  views.add(new View("veche-east-roof",false,92,-38,24,56,-49,24));
  views.add(new View("veche-interior",false,56,-60,30,56,-57,18));
  views.add(new View("communal-upper-bedroom",false,20,-54,20,16,-53.5,18));
  views.add(new View("posadnik-throne",false,67,-60,84,70,-59.5,82));
  views.add(new View("council-throne-new",false,56,-58,20,56,-57.5,14));
  views.add(new View("market-closed-shops",false,88,-59,80,85,-58,80));
  views.add(new View("house-roof-west",false,9,-45,22,24,-48,22));
  views.add(new View("military-roof-west",false,8,-45,81,26,-47,81));
  views.add(new View("herbalist-roof-front",false,88,-48,40,88,-51,53));
  views.add(new View("granary-roof-front",false,70,-48,19,84,-51,19));
  views.add(new View("house-lower-room",false,20,-59,21,17,-59,18));
  views.add(new View("herbalist-bed",false,83,-59,50,79,-59.8,50));
  views.add(new View("terem-bed",false,65,-52,92,64.5,-53.5,91));
  views.add(new View("forge-roofs",false,9,-43,56,24,-48,56));
  views.add(new View("market-roofs",false,107,-44,87,88,-51,87));
  views.add(new View("terem-roof",false,56,-42,87,66,-48,87));
  views.add(new View("hall-west-room",false,48,-59,21,45,-60,19));
  views.add(new View("hall-east-room",false,64,-59,24,68,-60,26));
  views.add(new View("military-corner",false,31,-59,82,33,-59,85));
  views.add(new View("prince-close",false,56,-59,20,56,-59,16));
  views.add(new View("common-back",false,20,-58,56,20,-59,52));
  views.add(new View("common-hair",false,20,-58,56,20,-59,52));
  views.add(new View("terem-chair",false,68,-59,85,70,-59,82));
  views.add(new View("gate-guards",false,56,-59,107,56,-59,102));
  if(Boolean.getBoolean("posadclient.focusOnly"))views.removeIf(v->!java.util.Set.of("council-throne-new","house-lower-room","terem-bed","herbalist-bed","house-roof-west","military-roof-west","herbalist-roof-front","granary-roof-front").contains(v.name()));
  views.replaceAll(v->v.nether()?v:new View(v.name(),false,v.x()+22500,v.y(),v.z(),v.tx()+22500,v.ty(),v.tz()));
  if(Boolean.getBoolean("posadclient.single"))views.removeIf(v->!java.util.Set.of("prince-close","common-hair","common-back").contains(v.name()));
  if(!Boolean.getBoolean("posadclient.single"))addNative("native-veche-front",56,4,48,56,9,23);
  if(!Boolean.getBoolean("posadclient.single"))addNative("native-house-window",9,3,22,15,4,22);
 }
 private static void move(Minecraft mc,View view){
  mc.setScreen(null);mc.options.hideGui=true;
  mc.getSingleplayerServer().execute(()->{
   try{var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();var world=view.nether()?server.getLevel(Level.NETHER):server.overworld();
    if(view.name().equals("prince-close")||view.name().equals("common-hair")||view.name().equals("common-back")){
     for(var e:world.getAllEntities())if(e instanceof PosadResidentEntity r&&r.getX()>22500&&r.getX()<22613&&r.role()==(view.name().equals("prince-close")?PosadPopulationPlan.Role.POSADNIK:PosadPopulationPlan.Role.BLACKSMITH)){
      if(view.name().equals("common-hair")||view.name().equals("common-back"))r.moveTo(22556.5,-60,56.5,0,0);
      r.setNoAi(true);float bodyYaw=view.name().equals("common-back")?180F:0F;r.setYRot(bodyYaw);r.setYHeadRot(bodyYaw);r.yBodyRot=bodyYaw;
      p.setGameMode(GameType.SPECTATOR);p.setNoGravity(true);p.teleportTo(world,r.getX()+(view.name().equals("prince-close")?2:0),r.getY()+.9,r.getZ()+(view.name().equals("prince-close")?3:4),view.name().equals("prince-close")?146.31F:180,22);return;
     }
     throw new IllegalStateException("Appearance subject not loaded: "+view.name());
    }
    double dx=view.tx()-view.x(),dz=view.tz()-view.z(),dy=view.ty()-(view.y()+p.getEyeHeight());float yaw=(float)(Math.toDegrees(Math.atan2(dz,dx))-90),pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.sqrt(dx*dx+dz*dz)));
    p.setGameMode(GameType.SPECTATOR);p.setNoGravity(true);p.teleportTo(world,view.x(),view.y(),view.z(),yaw,pitch);
   }catch(Throwable error){failure=error.toString();error.printStackTrace();}
  });
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(finished)return;
  try{
   mc.setWindowActive(false);mc.mouseHandler.releaseMouse();
   check(org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUSED)==0,"QA window unexpectedly focused");
   check(org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_VISIBLE)==0,"QA window became visible on the owner's desktop");
   if(failure!=null)throw new IllegalStateException(failure);
   if(!opened&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){
    opened=true;mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0D);mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(8);mc.options.simulationDistance().set(4);mc.options.framerateLimit().set(30);mc.options.guiScale().set(2);mc.options.fov().set(80);mc.options.gamma().set(1D);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.options.save();
    System.out.println("POSAD_CLIENT_ISOLATION_PASS visible=0 focused="+org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUSED)+" renderer="+org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
    var worldFile=mc.gameDirectory.toPath().resolve("saves/Posad416/level.dat");
    var data=net.minecraft.nbt.NbtIo.readCompressed(worldFile,net.minecraft.nbt.NbtAccounter.unlimitedHeap());
    data.getCompound("Data").getCompound("WorldGenSettings").putBoolean("generate_features",false);
    net.minecraft.nbt.NbtIo.writeCompressed(data,worldFile);
    System.out.println("POSAD_CLIENT_DISPOSABLE_FIXTURE new vanilla structures disabled; saved native Nether city retained");
    mc.createWorldOpenFlows().openWorld("Posad416",()->{if(!finished)failure="World open was cancelled";});return;
   }
   if(mc.player==null||mc.getSingleplayerServer()==null)return;
   if(!started){started=true;prepareViews();mc.getSingleplayerServer().execute(()->{var server=mc.getSingleplayerServer();var w=server.overworld();w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);w.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false,server);w.setDayTime(6000);var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(GameType.SPECTATOR);p.setNoGravity(true);p.teleportTo(w,56,-50,120,180,20);build=0;});}
   if(!ready)return;clientTicks++;
   if(stage<0){stage=0;move(mc,views.get(stage));captureAt=clientTicks+180;}
   else if(shot==stage){stage++;if(stage>=views.size()){stop(null);return;}move(mc,views.get(stage));captureAt=clientTicks+(stage<4?180:100);}
  }catch(Throwable error){stop(error);}
 }
 @SubscribeEvent public static void render(RenderFrameEvent.Post event){
  if(finished||stage<0||shot==stage||clientTicks<captureAt)return;
  try{var mc=Minecraft.getInstance();Files.createDirectories(output());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output().resolve(views.get(stage).name()+".png"));}
   System.out.println("POSAD_REAL_CLIENT_SCREENSHOT "+views.get(stage).name());
   mc.getSingleplayerServer().execute(()->{var w=mc.getSingleplayerServer().overworld();for(var q:new int[][]{{20,2,20},{20,2,80},{66,8,85},{82,2,50},{56,2,20},{88,2,80},{24,2,56},{84,2,19}})System.out.println("POSAD_ROOM_BLOCK_LIGHT "+java.util.Arrays.toString(q)+" "+w.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,new BlockPos(22500+q[0],-61+q[1],q[2])));for(var e:mc.getSingleplayerServer().overworld().getAllEntities())if(e instanceof PosadResidentEntity r)System.out.println("POSAD_RESIDENT_REOPEN "+r.role()+" "+r.position());});shot=stage;
  }catch(Throwable error){stop(error);}
 }
}
