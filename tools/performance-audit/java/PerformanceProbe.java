package org.slavicmyths.audit;
import java.nio.file.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import jdk.jfr.Recording;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** Explicitly opt-in disposable benchmark; no production instrumentation. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class PerformanceProbe {
 static final boolean EXT=Boolean.getBoolean("audit.extended");
 static final String[] SCENES=EXT?new String[]{"idle","poloz_combat","gorynych_combat","worldgen_new","worldgen_old","classes_iv","runes","crossbow","post_cleanup"}:new String[]{"idle","vanilla_32","mod_32","gorynych","great_poloz","karachun","morozko","post_cleanup"};
 static final Path OUT=Path.of(System.getProperty("audit.output"));
 static int baseY=-60; static volatile long chunkLoads,newChunkLoads; static volatile int scene=-1,age,repeat; static volatile boolean ready,done;
 static boolean opened; static String lastShot=""; static long pre,lastFrame; static PrintWriter ticks,frames,events; static Recording recording;
 static void init() throws Exception {Files.createDirectories(OUT);ticks=new PrintWriter(Files.newBufferedWriter(OUT.resolve("ticks.csv")));frames=new PrintWriter(Files.newBufferedWriter(OUT.resolve("frames.csv")));events=new PrintWriter(Files.newBufferedWriter(OUT.resolve("events.txt")));ticks.println("scene,repeat,age,ms,interval_ns,heap_bytes,entities,loaded_chunks,gc_count,gc_ms,chunk_load_events,new_chunk_load_events");frames.println("scene,repeat,age,frame_ms");recording=new Recording(jdk.jfr.Configuration.getConfiguration("profile"));recording.setMaxSize(128L*1024*1024);recording.start();}
 @SubscribeEvent public static void client(ClientTickEvent.Post e) {
  var mc=Minecraft.getInstance();
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;try{init();}catch(Exception ex){throw new RuntimeException(ex);}mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(8);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(260);mc.options.enableVsync().set(false);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.options.save();events.println("ACTIVE_OPTIONS rd="+mc.options.renderDistance().get()+" sim="+mc.options.simulationDistance().get()+" vsync="+mc.options.enableVsync().get()+" fpsLimit="+mc.options.framerateLimit().get());events.println("WINDOW="+mc.getWindow().getWidth()+"x"+mc.getWindow().getHeight()+" GPU="+com.mojang.blaze3d.platform.GlUtil.getRenderer());events.flush();
   mc.createWorldOpenFlows().createFreshLevel("PerfAudit-"+System.currentTimeMillis(),new LevelSettings("Perf audit",GameType.SURVIVAL,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(280015,EXT,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(EXT?WorldPresets.NORMAL:WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
  }
  if(ready&&EXT&&scene>=0&&scene<SCENES.length&&age%20==0&&mc.level!=null){events.println("CLIENT_FPS "+SCENES[scene]+" repeat="+repeat+" age="+age+" fps="+mc.getFps()+" particles="+mc.particleEngine.countParticles());events.flush();}
  if(done){done=false;mc.stop();}
 }
 static long previousTick;
 @SubscribeEvent public static void before(ServerTickEvent.Pre e){pre=System.nanoTime();if(EXT&&ready&&scene>=0&&scene<SCENES.length)stimulus(e.getServer());}
 @SubscribeEvent public static void after(ServerTickEvent.Post e){
  if(ticks==null||scene>=SCENES.length)return;
  long now=System.nanoTime(),elapsed=now-pre,interval=previousTick==0?0:now-previousTick;previousTick=now;
  var s=e.getServer();var w=s.overworld();if(s.getPlayerList().getPlayers().isEmpty())return;
  var p=s.getPlayerList().getPlayers().getFirst();
  if(!ready){ready=true;w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,s);w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,s);w.setDayTime(18000);baseY=EXT?Math.min(w.getMaxBuildHeight()-16,Math.max(120,w.getSharedSpawnPos().getY()+8)):-60;
   if(EXT){for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++)w.getChunk(cx,cz);for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++){w.setBlock(new net.minecraft.core.BlockPos(x,baseY-1,z),net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(),2);for(int dy=0;dy<8;dy++)w.setBlock(new net.minecraft.core.BlockPos(x,baseY+dy,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);}if(!w.getBlockState(new net.minecraft.core.BlockPos(0,baseY-1,-12)).isSolid())throw new IllegalStateException("Fixture platform not ready");events.println("PLATFORM y="+baseY+" seed=280015");}
   p.teleportTo(w,0,baseY,-12,0,0);p.setInvulnerable(true);p.setNoGravity(true);scene=0;setup(w,p);return;}
  if(scene>=SCENES.length)return;
  if(++age>100){long gc=0,gcms=0;for(var b:ManagementFactory.getGarbageCollectorMXBeans()){gc+=b.getCollectionCount();gcms+=b.getCollectionTime();}var rt=Runtime.getRuntime();ticks.println(SCENES[scene]+","+repeat+","+age+","+elapsed/1e6+","+interval+","+(rt.totalMemory()-rt.freeMemory())+","+entityCount(w)+","+w.getChunkSource().getLoadedChunksCount()+","+gc+","+gcms+","+chunkLoads+","+newChunkLoads);}
  if(age>=300){for(var a:w.getAllEntities())if(a instanceof Mob)events.println("END_ACTOR "+SCENES[scene]+" repeat="+repeat+" type="+BuiltInRegistries.ENTITY_TYPE.getKey(a.getType())+" pos="+a.position()+" hp="+((Mob)a).getHealth());ticks.flush();frames.flush();age=0;if(++repeat>=3){repeat=0;scene++;}if(scene>=SCENES.length){try{recording.stop();recording.dump(OUT.resolve("profile.jfr"));recording.close();}catch(Exception ex){throw new RuntimeException(ex);}events.println("COMPLETE");events.close();ticks.close();frames.close();done=true;return;}setup(w,p);}
 }
 static int entityCount(net.minecraft.server.level.ServerLevel w){int n=0;for(var e:w.getAllEntities())n++;return n;}
 @SubscribeEvent public static void chunk(net.neoforged.neoforge.event.level.ChunkEvent.Load e){if(e.getLevel() instanceof net.minecraft.server.level.ServerLevel){chunkLoads++;if(e.isNewChunk())newChunkLoads++;}}
 static void stimulus(net.minecraft.server.MinecraftServer server){
  if(server.getPlayerList().getPlayers().isEmpty())return;var p=server.getPlayerList().getPlayers().getFirst();var w=p.serverLevel();String name=SCENES[scene];
  if(!p.isAlive()||p.getY()<w.getMinBuildHeight()){events.println("INVALID_FIXTURE player_dead_or_void "+name+" age="+age);events.flush();try{recording.stop();recording.dump(OUT.resolve("profile.jfr"));}catch(Exception ex){throw new RuntimeException(ex);}scene=SCENES.length;done=true;return;}
  if((name.equals("poloz_combat")||name.equals("gorynych_combat"))&&age%20==0){p.setHealth(p.getMaxHealth());for(var a:w.getAllEntities())if(a instanceof Mob m)events.println("BOSS_STATE "+name+" age="+age+" target="+(m.getTarget()==p)+" phase="+(m instanceof org.slavicmyths.poloz.GreatPoloz b?b.phase():m instanceof org.slavicmyths.gorynych.Gorynych b?b.state():-1)+" action="+(m instanceof org.slavicmyths.poloz.GreatPoloz b?b.action():m instanceof org.slavicmyths.gorynych.Gorynych b?b.bodyAction():-1));events.flush();}
  if(name.startsWith("worldgen_")&&age%40==0){int x=512+repeat*1024+(age/40)*32;w.getChunk(x>>4,0);p.setNoGravity(true);p.teleportTo(w,x,200,0,90,25);events.println("ROUTE "+name+" repeat="+repeat+" x="+x+" age="+age+" new_loads="+newChunkLoads);events.flush();}
  if(name.equals("classes_iv")&&age<280&&age%70==5){int index=repeat*4+age/70;if(index<12&&new java.util.ArrayList<>(org.slavicmyths.rpg.classes.ClassUltimates.ALL.values()).get(index).skill().equals("living_wall")){p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));p.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);}}
  if(name.equals("classes_iv")&&age<280&&age%70==10){int index=repeat*4+age/70;if(index>=12)return;var u=new java.util.ArrayList<>(org.slavicmyths.rpg.classes.ClassUltimates.ALL.values()).get(index);org.slavicmyths.rpg.classes.ClassUltimateRuntime.clear(p);var d=org.slavicmyths.rpg.classes.ClassState.data(p);d.putString("Base",u.base());d.putInt("Level",40);d.putString("Second",u.parent());d.putString("First",org.slavicmyths.rpg.classes.ClassDefinitions.BRANCHES.get(u.parent()).parent());d.putString("Third",u.id());var t=new net.minecraft.nbt.CompoundTag();t.putBoolean(u.parent()+"Done",true);d.put("Trials",t);org.slavicmyths.rpg.classes.ClassState.cooldown(p,u.skill(),0);p.setHealth(u.skill().equals("living_water")?10:20);if(!u.skill().equals("living_wall"))p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD));p.setYRot(0);p.setXRot(0);boolean ok=org.slavicmyths.rpg.classes.ClassUltimateRuntime.activate(p);events.println("CAST "+u.skill()+" success="+ok+" age="+age);events.flush();}
  if(name.equals("runes")&&age%20==10){var enemy=w.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(6),m->m.isAlive()).stream().findFirst();enemy.ifPresent(p::attack);events.println("RUNE_ATTACK repeat="+repeat+" age="+age+" target="+enemy.isPresent());events.flush();}

  if(name.equals("crossbow")&&age%30==10){var bow=new net.minecraft.world.item.ItemStack(org.slavicmyths.gorynych.GorynychContent.item("three_breath_crossbow"));var tag=new net.minecraft.nbt.CompoundTag();var state=new net.minecraft.nbt.CompoundTag();state.putInt("Mode",repeat);tag.put("Gorynych196",state);bow.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));bow.set(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW)));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,bow);((org.slavicmyths.gorynych.SerpentCrossbow)bow.getItem()).performShooting(w,p,net.minecraft.world.InteractionHand.MAIN_HAND,bow,3.15f,0,null);}
 }
 static void setup(net.minecraft.server.level.ServerLevel w,net.minecraft.server.level.ServerPlayer p){
  if(org.slavicmyths.poloz.PolozEvent.get(w).running())org.slavicmyths.poloz.PolozEvent.get(w).finish(w,false);
  var list=new java.util.ArrayList<Entity>();w.getAllEntities().forEach(x->{if(!(x instanceof net.minecraft.world.entity.player.Player))list.add(x);});list.forEach(a->{if(a instanceof org.slavicmyths.morozko.Morozko b)b.cancelPeacefully();else if(a instanceof org.slavicmyths.karachun.Karachun b)b.cancelPeacefully();else if(a instanceof org.slavicmyths.gorynych.Gorynych b)b.cancelPeacefully();else a.discard();});
  if(EXT){for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++)w.setBlock(new net.minecraft.core.BlockPos(x,baseY-1,z),net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(),2);}
  p.teleportTo(w,0,baseY,-12,0,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;p.setOnGround(true);p.setHealth(p.getMaxHealth());p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,net.minecraft.world.item.ItemStack.EMPTY);
  String name=SCENES[scene];org.slavicmyths.rpg.classes.ClassUltimateRuntime.clear(p);if(EXT){p.setInvulnerable(false);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20);p.setHealth(20);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,100000,4));p.setNoGravity(false);}
  if(EXT&&name.equals("runes")){var sword=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);var runes=repeat==0?java.util.List.of("thunder","heat"):repeat==1?java.util.List.of("blood","strength"):java.util.List.of("dragon_flame","mor_breath");for(var rune:runes)if(!org.slavicmyths.rpg.Runes.compatible(sword,rune))throw new IllegalStateException("Invalid rune loadout "+rune);org.slavicmyths.item.ItemState.runes(sword,2,runes);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);events.println("LOADOUT "+runes);}
  int n=EXT?(java.util.Set.of("classes_iv","runes","crossbow").contains(name)?128:name.endsWith("combat")?1:0):name.endsWith("32")?32:name.equals("idle")||name.equals("post_cleanup")?0:1;
  for(int i=0;i<n;i++){String id=EXT?(name.equals("poloz_combat")?"slavicmyths:great_poloz":name.equals("gorynych_combat")?"slavicmyths:zmey_gorynych":"minecraft:pillager"):name.equals("vanilla_32")?"minecraft:zombie":name.equals("mod_32")?"slavicmyths:volkolak":"slavicmyths:"+(name.equals("gorynych")?"zmey_gorynych":name);
   var type=BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(id)).orElseThrow(()->new IllegalStateException("missing "+id));var a=type.create(w);a.moveTo(EXT?(n==1?0:(i%16-8)*1.5):(i%8-4)*2,baseY,EXT?(n==1?-4:-8+(i/16)*2):4+(i/8)*2,180,0);if(EXT&&a instanceof net.minecraft.world.entity.monster.Pillager pillager)pillager.finalizeSpawn(w,w.getCurrentDifficultyAt(a.blockPosition()),MobSpawnType.COMMAND,null);if(EXT&&!name.endsWith("combat")&&a instanceof LivingEntity l){l.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);l.setHealth(1000);}if(a instanceof org.slavicmyths.morozko.Morozko b){b.session=java.util.UUID.randomUUID();b.owner=p.getUUID();b.anchor=p.position();b.setHealth(b.getMaxHealth()*(repeat==0?.9f:repeat==1?.5f:.2f));}
   if(a instanceof org.slavicmyths.karachun.Karachun b){b.session=java.util.UUID.randomUUID();b.owner=p.getUUID();b.anchor=p.position();b.configureHealth(1600);b.setHealth(1600*(repeat==0?.9f:repeat==1?.5f:.2f));}
   if(EXT&&a instanceof org.slavicmyths.poloz.GreatPoloz b){b.setHealth(repeat==0?3000:repeat==1?1800:600);b.initializeBody();}
   if(EXT&&a instanceof org.slavicmyths.gorynych.Gorynych b){var row=new net.minecraft.nbt.CompoundTag();row.putLong("Origin",a.blockPosition().asLong());row.putString("Rotation","NONE");row.putBoolean("Standalone",true);row.putFloat("HP",repeat==0?2800:repeat==1?1500:400);row.putInt("State",repeat==0?org.slavicmyths.gorynych.GorynychRules.GROUND:org.slavicmyths.gorynych.GorynychRules.AIR);row.putInt(org.slavicmyths.gorynych.GorynychRules.BALANCE,org.slavicmyths.gorynych.GorynychRules.BALANCE_VERSION);b.configure(a.getUUID(),row);}
   boolean added=w.addFreshEntity(a);if(a instanceof Mob m)m.setTarget(p);if(EXT&&a instanceof org.slavicmyths.poloz.GreatPoloz b&&repeat>0)b.hurt(p.damageSources().playerAttack(p),1);events.println("SPAWN "+name+" repeat="+repeat+" id="+id+" added="+added);}
  events.println("SCENE "+name+" repeat="+repeat+" time="+System.currentTimeMillis()+" entities="+entityCount(w));events.flush();
 }
 @SubscribeEvent public static void frame(RenderFrameEvent.Post e){long n=System.nanoTime();if(EXT&&ready&&scene>=0&&scene<SCENES.length&&age==70&&!lastShot.equals(SCENES[scene]+repeat)){lastShot=SCENES[scene]+repeat;var mc=Minecraft.getInstance();try(var im=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(OUT.resolve(lastShot+".png"));}catch(Exception ex){throw new RuntimeException(ex);}}if(frames!=null&&ready&&scene>=0&&scene<SCENES.length&&age>100&&lastFrame!=0&&Minecraft.getInstance().level!=null)frames.println(SCENES[scene]+","+repeat+","+age+","+(n-lastFrame)/1e6);lastFrame=n;}
}
