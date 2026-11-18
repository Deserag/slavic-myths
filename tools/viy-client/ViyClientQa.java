package org.slavicmyths.verifyclient;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.*;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.common.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.viy.ViyContent;
import org.slavicmyths.sisters.ViyBoss;
import java.nio.file.*;
@Mod(value="slavicmyths_posadnative",dist=Dist.CLIENT)
@EventBusSubscriber(modid="slavicmyths_posadnative",value=Dist.CLIENT)
public final class ViyClientQa {
 static boolean opened,started,ready,done;static volatile String error;static int ticks,stage;static volatile ViyBoss boss;
 static Path out(){return Minecraft.getInstance().gameDirectory.toPath().resolve("posad-client-evidence");}
 static void end(String failure){if(done)return;done=true;try{Files.createDirectories(out());Files.writeString(out().resolve("result.txt"),failure==null?"PASS: actual Viy client spawn, closed/open eyelids, phase transition and save; full combat/MP NOT_TESTED":"FAIL: "+failure);}catch(Exception e){e.printStackTrace();}Minecraft.getInstance().stop();}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(done)return;
  try{
   mc.setWindowActive(false);mc.mouseHandler.releaseMouse();
   if(org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_VISIBLE)!=0)throw new IllegalStateException("QA window visible");
   if(error!=null)throw new IllegalStateException(error);
   if(!opened&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){opened=true;mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.createWorldOpenFlows().openWorld("Posad416",()->error="World open cancelled");return;}
   if(mc.player==null||mc.getSingleplayerServer()==null)return;
   if(!started){started=true;mc.getSingleplayerServer().execute(()->{try{
    var server=mc.getSingleplayerServer();var w=server.overworld();server.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);w.setDayTime(6000);
    for(int x=6000;x<=6020;x++)for(int z=0;z<=20;z++)w.setBlockAndUpdate(new BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
    boss=ViyContent.VIY.get().create(w);if(boss==null)throw new IllegalStateException("No boss");boss.moveTo(6010,100,10,0,0);boss.setNoGravity(true);if(!w.addFreshEntity(boss)||boss.getMaxHealth()!=2800)throw new IllegalStateException("Invalid Viy attributes/spawn");
    var player=server.getPlayerList().getPlayers().getFirst();player.setGameMode(GameType.SPECTATOR);player.setNoGravity(true);player.teleportTo(w,6010,102,22,180,0);ready=true;System.out.println("VIY_CLIENT_SPAWN_PASS HP="+boss.getHealth());
   }catch(Throwable t){error=t.toString();t.printStackTrace();}});}
   if(!ready)return;ticks++;
   if(stage==0&&ticks>=100){capture("viy-closed");stage=1;mc.getSingleplayerServer().execute(()->boss.setHealth(boss.getMaxHealth()*.45F));}
   if(stage==1&&ticks>=180){if(!boss.phaseTwo()||!boss.eyesOpen())throw new IllegalStateException("Phase2/eyelid transition missing");capture("viy-open-phase2");stage=2;System.out.println("VIY_CLIENT_PHASE2_EYES_PASS progress="+boss.eyelidProgress());}
   if(stage==2&&ticks>=200){mc.getSingleplayerServer().execute(()->{if(!mc.getSingleplayerServer().saveEverything(false,true,true))error="Save failed";});stage=3;}
   if(stage==3&&ticks>=240)end(null);
  }catch(Throwable t){t.printStackTrace();end(t.toString());}
 }
 static void capture(String name)throws Exception{var mc=Minecraft.getInstance();Files.createDirectories(out());try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(out().resolve(name+".png"));}System.out.println("VIY_CLIENT_SCREENSHOT "+name);}
}
