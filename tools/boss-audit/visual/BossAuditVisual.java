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
 static final List<Mob> sceneActors=new ArrayList<>();static final List<Long> frameIntervals=new ArrayList<>();static long lastFrame;
 static final List<Shot> SHOTS=new ArrayList<>();static final JsonArray RESULTS=new JsonArray();static boolean opened,requested,prepared,finished;static volatile boolean ready;static volatile String error;static int index=-1,age;static Mob actor;static ServerPlayer target;static double hpBefore;
 static Path out(){return Path.of(System.getProperty("bossaudit.visualOutput"));}
 static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
 static void finish(String failure){if(finished)return;finished=true;try{Files.createDirectories(out());var d=new JsonObject();d.addProperty("result",failure==null?"CAPTURE_COMPLETE_NOT_VISUAL_ACCEPTANCE":"FAIL");d.addProperty("failure",failure);d.addProperty("renderer",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));d.add("frames",RESULTS);Files.writeString(out().resolve("finished.json"),new GsonBuilder().setPrettyPrinting().create().toJson(d));}catch(Exception ex){ex.printStackTrace();}Minecraft.getInstance().stop();}
 static void setup(ServerPlayer camera)throws Exception{
  var w=camera.serverLevel();w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,w.getServer());w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,w.getServer());w.setDayTime(6000);camera.setGameMode(GameType.SPECTATOR);camera.setNoGravity(true);camera.teleportTo(w,100,80,120,180,0);
  for(int x=36;x<=164;x++)for(int z=36;z<=164;z++)w.setBlock(new BlockPos(x,69,z),Blocks.STONE.defaultBlockState(),2);
  target=AuditPlayers.create(w,"VisualPoseTarget");target.setGameMode(GameType.SURVIVAL);target.getAbilities().invulnerable=true;target.setNoGravity(true);target.setPos(100,72,106);w.addNewPlayer(target);
  for(String id:System.getProperty("bossaudit.ids").split(",")){
   if(Boolean.getBoolean("bossaudit.posesOnly")){int[] actions=switch(id){case "gneteya"->new int[]{1,2,3,4,5};case "tryaseya"->new int[]{6,7,8,9,10};case "agneya"->new int[]{1};case "ledeya"->new int[]{0,1,2};case "lomeya"->new int[]{1,2,3,4,5};default->new int[]{1,2,3,4,5,6,7,8,9};};for(int a:actions)for(int f:new int[]{1,8,14})for(String view:new String[]{"front","right"})SHOTS.add(new Shot(id,view,8,"pose_"+a+"_"+f));continue;}
   if(Boolean.getBoolean("bossaudit.performance")){for(int n:new int[]{0,1,5,10})SHOTS.add(new Shot(id,"front",16,"performance_"+n));continue;}
   if(Boolean.getBoolean("bossaudit.metadataOnly")){SHOTS.add(new Shot(id,"front",8,"model_metadata"));continue;}
   for(String view:new String[]{"front","back","left","right","top","head","body"})SHOTS.add(new Shot(id,view,8,"idle"));
   for(int distance:new int[]{1,3,8,16})SHOTS.add(new Shot(id,"front",distance,"idle"));
   SHOTS.add(new Shot(id,"front",8,"hitboxes"));SHOTS.add(new Shot(id,"front",8,"walk"));SHOTS.add(new Shot(id,"front",8,"hurt"));SHOTS.add(new Shot(id,"front",8,"death"));
   if(Set.of("gneteya","tryaseya","agneya","ledeya","lomeya","neveya","viy").contains(id))SHOTS.add(new Shot(id,"front",8,"controlled_attack_pose"));
  }ready=true;
 }
 static JsonObject modelInfo(Mob mob)throws Exception{
  var result=new JsonObject();var renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(mob);Object model=null;
  if(renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?,?> living)model=living.getModel();
  if(model==null){result.addProperty("status","NO_STANDARD_ENTITY_MODEL");return result;}
  result.addProperty("model_class",model.getClass().getName());var roots=Collections.newSetFromMap(new IdentityHashMap<net.minecraft.client.model.geom.ModelPart,Boolean>());var parts=Collections.newSetFromMap(new IdentityHashMap<net.minecraft.client.model.geom.ModelPart,Boolean>());
  var nodes=new JsonArray();
  for(Class<?> c=model.getClass();c!=null;c=c.getSuperclass())for(var field:c.getDeclaredFields())if(net.minecraft.client.model.geom.ModelPart.class.isAssignableFrom(field.getType())){field.setAccessible(true);var part=(net.minecraft.client.model.geom.ModelPart)field.get(model);if(part!=null){roots.add(part);var node=new JsonObject();node.addProperty("field",c.getSimpleName()+"."+field.getName());node.addProperty("xScale",part.xScale);node.addProperty("yScale",part.yScale);node.addProperty("zScale",part.zScale);node.addProperty("x",part.x);node.addProperty("y",part.y);node.addProperty("z",part.z);nodes.add(node);}}
  for(var part:roots)part.getAllParts().forEach(parts::add);var cubesField=net.minecraft.client.model.geom.ModelPart.class.getDeclaredField("cubes");cubesField.setAccessible(true);int cubes=0,flat=0;
  for(var part:parts)for(Object o:(List<?>)cubesField.get(part)){var cube=(net.minecraft.client.model.geom.ModelPart.Cube)o;cubes++;if(cube.maxX-cube.minX<.01||cube.maxY-cube.minY<.01||cube.maxZ-cube.minZ<.01)flat++;}
  result.addProperty("unique_model_parts",parts.size());result.addProperty("cuboids",cubes);result.addProperty("near_zero_dimension_cuboids",flat);result.add("named_part_transforms",nodes);result.addProperty("note","Actual renderer model after captured frame; not exact world-space mesh bounds or visual acceptance");return result;
 }
 static void configureVisualFixture(Mob mob,ServerPlayer camera){
  // Isolated pose fixtures only: these are not successful encounter-entry tests.
  if(mob instanceof org.slavicmyths.karachun.Karachun k){k.session=UUID.randomUUID();k.owner=camera.getUUID();k.anchor=mob.position();}
  if(mob instanceof org.slavicmyths.morozko.Morozko m){m.session=UUID.randomUUID();m.owner=camera.getUUID();m.anchor=mob.position();}
  if(mob instanceof org.slavicmyths.marinka.MarinkaEntity m){m.owner=camera.getUUID();m.active=true;}
  if(mob instanceof org.slavicmyths.burningislands.IslandsMob m)m.bindArena(mob.blockPosition().offset(index+1,0,0));
 }
 static double clientFrame(Mob mob){try{for(String method:new String[]{"actionFrame","attackPoseTicks","attackTicks"})try{return ((Number)(method.equals("actionFrame")?mob.getClass().getMethod(method).invoke(mob):mob.getClass().getMethod(method,float.class).invoke(mob,0F))).doubleValue();}catch(NoSuchMethodException ignored){}}catch(Exception ignored){}return -1;}
 static boolean clientPoseMatches(Shot shot,Mob client)throws Exception{if(client==null)return false;int expected=Integer.parseInt(shot.mode.split("_")[1]);int action=shot.id.equals("agneya")?(clientFrame(client)>=0?1:0):InterruptionAudit.state(client);return action==expected&&clientFrame(client)>=Integer.parseInt(shot.mode.split("_")[2]);}
 static void prepare(Shot shot)throws Exception{
  var mc=Minecraft.getInstance();var s=mc.getSingleplayerServer();var w=s.overworld();if(actor!=null)actor.discard();actor=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",shot.id)).create(w);check(actor!=null,"Unregistered mob "+shot.id);actor.setNoAi(true);actor.moveTo(100,70,100,0,0);w.addFreshEntity(actor);hpBefore=actor.getHealth();
  configureVisualFixture(actor,s.getPlayerList().getPlayers().getFirst());
  sceneActors.clear();frameIntervals.clear();lastFrame=0;
  if(shot.mode.startsWith("performance_")){
   int count=Integer.parseInt(shot.mode.substring(12));
   for(var old:w.getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(36,60,36,164,160,164),m->m!=actor))old.discard();
   if(count==0)actor.discard();else sceneActors.add(actor);
   for(int n=1;n<count;n++){var other=(Mob)actor.getType().create(w);other.setNoAi(true);double spacing=Math.max(4,actor.getBbWidth()*1.3);other.moveTo(100+(n%3-1)*spacing,70,100-(1+n/3)*spacing,0,0);configureVisualFixture(other,s.getPlayerList().getPlayers().getFirst());if(other instanceof org.slavicmyths.burningislands.IslandsMob m)m.bindArena(other.blockPosition().offset(index*16+n,0,0));w.addFreshEntity(other);sceneActors.add(other);}
  }
  if(shot.mode.equals("hurt"))actor.hurt(actor.damageSources().magic(),5);
  if(shot.mode.equals("death"))actor.setHealth(0);
  if(shot.mode.startsWith("pose_")){var bits=shot.mode.split("_");InterruptionAudit.enter(actor,target,Integer.parseInt(bits[1]),Integer.parseInt(bits[2]));}
  if(shot.mode.equals("controlled_attack_pose"))InterruptionAudit.enter(actor,target,shot.id.equals("tryaseya")?6:shot.id.equals("ledeya")?0:1,14);
  if(shot.mode.equals("walk"))actor.setDeltaMovement(.12,0,0);
  double radius=actor.getBbWidth()/2.,h=actor.getBbHeight(),x=100,z=100,y=70+h*.5,d=radius+shot.distance,tx=x,ty=y,tz=z;
  switch(shot.view){case "back"->z-=d;case "left"->x-=d;case "right"->x+=d;case "top"->y=70+h+shot.distance;case "head"->{z+=radius+3;y=70+h*.88;ty=y;}case "body"->{z+=radius+3;y=70+h*.45;ty=y;}default->z+=d;}
  var p=s.getPlayerList().getPlayers().getFirst();double dx=tx-x,dz=tz-z,dy=ty-y;p.teleportTo(w,x,y-p.getEyeHeight(),z,(float)Math.toDegrees(Math.atan2(-dx,dz)),(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz))));prepared=true;
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(finished)return;try{
  check("true".equals(System.getenv("GITHUB_ACTIONS"))&&"github-hosted".equals(System.getenv("RUNNER_ENVIRONMENT")),"Hosted runner only; no desktop bypass");var mc=Minecraft.getInstance();if(error!=null)throw new IllegalStateException(error);
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;Files.createDirectories(out());var jar=net.neoforged.fml.ModList.get().getModFileById("slavicmyths").getFile().getFilePath();check(jar.toString().endsWith(".jar"),"Production mod must load from JAR");mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(Boolean.getBoolean("bossaudit.performance")?260:60);mc.options.enableVsync().set(false);mc.options.hideGui=true;mc.options.fov().set(70);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.createWorldOpenFlows().createFreshLevel("BossAudit-"+System.currentTimeMillis(),new LevelSettings("Disposable hosted audit",GameType.SPECTATOR,false,net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(5738403L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;mc.getSingleplayerServer().execute(()->{try{setup(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst());}catch(Throwable ex){error=ex.toString();ex.printStackTrace();}});return;}
  if(!ready)return;
  if(index<0){index=0;next();}if(prepared)age++;
 }catch(Throwable ex){ex.printStackTrace();finish(ex.toString());}}
 static void next(){prepared=false;age=0;if(index==SHOTS.size()){finish(null);return;}var mc=Minecraft.getInstance();mc.getEntityRenderDispatcher().setRenderHitBoxes(SHOTS.get(index).mode.equals("hitboxes"));mc.getSingleplayerServer().execute(()->{try{prepare(SHOTS.get(index));}catch(Throwable ex){error=ex.toString();ex.printStackTrace();}});}
 @SubscribeEvent public static void render(RenderFrameEvent.Post e){if(finished||!ready||!prepared||index<0)return;try{var shot=SHOTS.get(index);int wait=(shot.mode.equals("controlled_attack_pose")||shot.mode.startsWith("pose_"))?2:shot.mode.equals("death")||shot.mode.equals("hurt")?5:25;if(shot.mode.startsWith("performance_"))wait=60;if(age<wait)return;if(shot.mode.startsWith("performance_")){long now=System.nanoTime();if(lastFrame!=0)frameIntervals.add(now-lastFrame);lastFrame=now;if(frameIntervals.size()<120)return;}var mc=Minecraft.getInstance();var rendered=mc.level.getEntity(actor.getId()) instanceof Mob mob?mob:null;if(shot.mode.startsWith("pose_")&&!clientPoseMatches(shot,rendered)&&age<40)return;String file=String.format("%04d-%s-%s-d%.0f-%s.png",index,shot.id,shot.view,shot.distance,shot.mode);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out().resolve(file));}
  var row=new JsonObject();row.addProperty("file",file);row.addProperty("id",shot.id);row.addProperty("view",shot.view);row.addProperty("camera_distance_beyond_aabb_radius",shot.distance);row.addProperty("mode",shot.mode);row.addProperty("renderer",mc.getEntityRenderDispatcher().getRenderer(actor).getClass().getName());row.addProperty("hp_before",hpBefore);row.addProperty("hp_at_capture",actor.getHealth());row.addProperty("removed_at_capture",actor.isRemoved());row.addProperty("fps_sample_not_performance_acceptance",mc.getFps());row.addProperty("note","Controlled geometry/pose capture, not live combat or owner art acceptance; death pose is forced health0; frames need human review");if(shot.mode.equals("controlled_attack_pose")||shot.mode.startsWith("pose_"))row.addProperty("server_action_at_capture",InterruptionAudit.state(actor));if(shot.mode.startsWith("pose_")){row.addProperty("client_actor_present",rendered!=null);row.addProperty("client_frame_at_capture",rendered==null?-1:clientFrame(rendered));row.addProperty("client_pose_matches_requested",clientPoseMatches(shot,rendered));if(rendered!=null&&!shot.id.equals("agneya"))row.addProperty("client_action_at_capture",InterruptionAudit.state(rendered));}row.add("model_metadata",modelInfo(rendered==null?actor:rendered));if(shot.mode.startsWith("performance_")){var intervals=new JsonArray();frameIntervals.forEach(intervals::add);row.add("render_frame_intervals_ns",intervals);row.addProperty("requested_count",Integer.parseInt(shot.mode.substring(12)));row.addProperty("alive_count",sceneActors.stream().filter(Mob::isAlive).count());row.addProperty("visible_scene_note","60 client tick warmup;120 frame intervals; llvmpipe software rendering; NoAI controlled scene; not hardware FPS or full combat load; camera may cull large actors");}RESULTS.add(row);System.out.println("BOSS_AUDIT_FRAME "+file);index++;next();
 }catch(Throwable ex){ex.printStackTrace();finish(ex.toString());}}
}
