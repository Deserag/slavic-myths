package org.slavicmyths.bossaudit;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
@Mod(value="slavicmyths_bossaudit",dist=Dist.CLIENT) @EventBusSubscriber(modid="slavicmyths_bossaudit",value=Dist.CLIENT)
public final class BossAuditVisual {
 record Shot(String id,String view,double distance,String mode){}
 static final List<Shot> SHOTS=new ArrayList<>();static final JsonArray RESULTS=new JsonArray();static boolean opened,requested,prepared,finished;static volatile boolean ready;static volatile String error;static int index=-1,age;static Mob actor;static ServerPlayer target;static double hpBefore;
 static Path out(){return Path.of(System.getProperty("bossaudit.visualOutput"));}
 static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
 static void finish(String failure){if(finished)return;finished=true;try{Files.createDirectories(out());var d=new JsonObject();d.addProperty("result",failure==null?"CAPTURE_COMPLETE_NOT_VISUAL_ACCEPTANCE":"FAIL");d.addProperty("failure",failure);d.addProperty("renderer",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));d.add("frames",RESULTS);Files.writeString(out().resolve("finished.json"),new GsonBuilder().setPrettyPrinting().create().toJson(d));}catch(Exception ex){ex.printStackTrace();}Minecraft.getInstance().stop();}
 static void setup(ServerPlayer camera)throws Exception{
  var w=camera.serverLevel();w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,w.getServer());w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,w.getServer());w.setDayTime(6000);camera.setGameMode(GameType.SPECTATOR);camera.setNoGravity(true);camera.teleportTo(w,100,80,120,180,0);
  for(int x=36;x<=164;x++)for(int z=36;z<=164;z++)w.setBlock(new BlockPos(x,69,z),Blocks.STONE.defaultBlockState(),2);
  target=AuditPlayers.create(w,"VisualPoseTarget");target.setGameMode(GameType.SURVIVAL);target.getAbilities().invulnerable=true;target.setNoGravity(true);target.setPos(100,72,106);w.addNewPlayer(target);
  for(String id:System.getProperty("bossaudit.ids").split(",")){
   for(String view:new String[]{"front","back","left","right","top","head","body"})SHOTS.add(new Shot(id,view,8,"idle"));
   for(int distance:new int[]{1,3,8,16})SHOTS.add(new Shot(id,"front",distance,"idle"));
   SHOTS.add(new Shot(id,"front",8,"hitboxes"));SHOTS.add(new Shot(id,"front",8,"walk"));SHOTS.add(new Shot(id,"front",8,"hurt"));SHOTS.add(new Shot(id,"front",8,"death"));
   if(Set.of("gneteya","tryaseya","agneya","ledeya","lomeya","neveya","viy").contains(id))SHOTS.add(new Shot(id,"front",8,"controlled_attack_pose"));
  }ready=true;
 }
 static void prepare(Shot shot)throws Exception{
  var mc=Minecraft.getInstance();var s=mc.getSingleplayerServer();var w=s.overworld();if(actor!=null)actor.discard();actor=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",shot.id)).create(w);check(actor!=null,"Unregistered mob "+shot.id);actor.setNoAi(true);actor.moveTo(100,70,100,0,0);w.addFreshEntity(actor);hpBefore=actor.getHealth();
  if(shot.mode.equals("hurt"))actor.hurt(actor.damageSources().magic(),5);
  if(shot.mode.equals("death"))actor.setHealth(0);
  if(shot.mode.equals("controlled_attack_pose"))InterruptionAudit.enter(actor,target,shot.id.equals("tryaseya")?6:shot.id.equals("ledeya")?0:1,14);
  if(shot.mode.equals("walk"))actor.setDeltaMovement(.12,0,0);
  double radius=actor.getBbWidth()/2.,h=actor.getBbHeight(),x=100,z=100,y=70+h*.5,d=radius+shot.distance,tx=x,ty=y,tz=z;
  switch(shot.view){case "back"->z-=d;case "left"->x-=d;case "right"->x+=d;case "top"->y=70+h+shot.distance;case "head"->{z+=radius+3;y=70+h*.88;ty=y;}case "body"->{z+=radius+3;y=70+h*.45;ty=y;}default->z+=d;}
  var p=s.getPlayerList().getPlayers().getFirst();double dx=tx-x,dz=tz-z,dy=ty-y;p.teleportTo(w,x,y-p.getEyeHeight(),z,(float)Math.toDegrees(Math.atan2(-dx,dz)),(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz))));prepared=true;
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(finished)return;try{
  check("true".equals(System.getenv("GITHUB_ACTIONS"))&&"github-hosted".equals(System.getenv("RUNNER_ENVIRONMENT")),"Hosted runner only; no desktop bypass");var mc=Minecraft.getInstance();if(error!=null)throw new IllegalStateException(error);
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;Files.createDirectories(out());var jar=net.neoforged.fml.ModList.get().getModFileById("slavicmyths").getFile().getFilePath();check(jar.toString().endsWith(".jar"),"Production mod must load from JAR");mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(60);mc.options.enableVsync().set(false);mc.options.hideGui=true;mc.options.fov().set(70);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.createWorldOpenFlows().createFreshLevel("BossAudit-"+System.currentTimeMillis(),new LevelSettings("Disposable hosted audit",GameType.SPECTATOR,false,net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(5738403L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{try{setup(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst());}catch(Throwable ex){error=ex.toString();ex.printStackTrace();}});return;}
  if(!ready)return;
  if(index<0){index=0;next();}if(prepared)age++;
 }catch(Throwable ex){ex.printStackTrace();finish(ex.toString());}}
 static void next(){prepared=false;age=0;if(index==SHOTS.size()){finish(null);return;}var mc=Minecraft.getInstance();mc.getEntityRenderDispatcher().setRenderHitBoxes(SHOTS.get(index).mode.equals("hitboxes"));mc.getSingleplayerServer().execute(()->{try{prepare(SHOTS.get(index));}catch(Throwable ex){error=ex.toString();ex.printStackTrace();}});}
 @SubscribeEvent public static void render(RenderFrameEvent.Post e){if(finished||!ready||!prepared||index<0)return;try{var shot=SHOTS.get(index);int wait=shot.mode.equals("death")||shot.mode.equals("hurt")?5:25;if(age<wait)return;var mc=Minecraft.getInstance();String file=String.format("%04d-%s-%s-d%.0f-%s.png",index,shot.id,shot.view,shot.distance,shot.mode);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out().resolve(file));}
  var row=new JsonObject();row.addProperty("file",file);row.addProperty("id",shot.id);row.addProperty("view",shot.view);row.addProperty("camera_distance_beyond_aabb_radius",shot.distance);row.addProperty("mode",shot.mode);row.addProperty("renderer",mc.getEntityRenderDispatcher().getRenderer(actor).getClass().getName());row.addProperty("hp_before",hpBefore);row.addProperty("hp_at_capture",actor.getHealth());row.addProperty("removed_at_capture",actor.isRemoved());row.addProperty("fps_sample_not_performance_acceptance",mc.getFps());row.addProperty("note","Controlled geometry/pose capture, not live combat or owner art acceptance; death pose is forced health0; frames need human review");RESULTS.add(row);System.out.println("BOSS_AUDIT_FRAME "+file);index++;next();
 }catch(Throwable ex){ex.printStackTrace();finish(ex.toString());}}
}
