package org.slavicmyths.pesigolovtsy;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.pesigolovtsy.DogheadRules.*;
/** Disposable native render fixture. Never included in the production jar. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class DogheadSceneProbe {
 static boolean opened,started,finished;static int time,stage=-1,shot=-1;static DogheadEntity actor;static net.minecraft.world.entity.animal.Cow prey;
 static final Path OUT=Path.of("D:/slavic-myths/docs/verification/pesigolovtsy-1.6.5/client-frames");
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
  if(!opened&&mc.screen instanceof TitleScreen&&System.getProperty("doghead.relog")!=null){opened=true;mc.createWorldOpenFlows().openWorld(System.getProperty("doghead.relog"),()->{});return;}
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.createWorldOpenFlows().createFreshLevel("DogheadQA-"+System.currentTimeMillis(),new LevelSettings("Doghead QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(165,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null||finished)return;
  if(System.getProperty("doghead.relog")!=null){if(++time==60){mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var loaded=p.serverLevel().getEntitiesOfClass(DogheadEntity.class,new net.minecraft.world.phys.AABB(-8,79,-8,8,86,8));if(loaded.size()!=1||loaded.getFirst().role()!=Role.LEADER||loaded.getFirst().getHealth()!=50||DogheadPacks.members(loaded.getFirst()).size()!=1)throw new IllegalStateException("Real saved-world reload failed: "+loaded);System.out.println("DOGHEAD_REAL_WORLD_RELOG_PASS role="+loaded.getFirst().role()+" hp="+loaded.getFirst().getHealth()+" pack="+loaded.getFirst().pack);});}if(time>100){finished=true;mc.stop();}return;}
  if(!started){started=true;mc.getSingleplayerServer().execute(()->{var s=mc.getSingleplayerServer();var p=s.getPlayerList().getPlayers().getFirst();var w=p.serverLevel();w.setDayTime(6000);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,s);w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,s);for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(w,0,82,-5,0,6);});}
  time++;int next=(time-100)/160;
  if(time>=100&&next<6&&next!=stage){stage=next;int n=next;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();if(actor!=null){System.out.println("DOGHEAD_SCENE_DAMAGE="+actor.role()+":"+(prey==null?"idle":400-prey.getHealth()));actor.discard();}if(prey!=null){prey.discard();prey=null;}actor=DogheadContent.ENTITY.get().create(p.serverLevel());actor.role(Role.values()[n%3],true);actor.setNoAi(true);actor.moveTo(0,81,0,210,0);actor.yBodyRot=210;actor.yHeadRot=210;p.serverLevel().addFreshEntity(actor);if(n>=3){actor.setNoAi(false);actor.setYRot(180);actor.yBodyRot=180;actor.yHeadRot=180;prey=EntityType.COW.create(p.serverLevel());prey.setNoAi(true);prey.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(400);prey.setHealth(400);prey.moveTo(0,81,n==4?-9:-2);p.serverLevel().addFreshEntity(prey);actor.setTarget(prey);}p.teleportTo(p.serverLevel(),2.2,82,-3.5,32,6);System.out.println("DOGHEAD_NATIVE_SCENE="+actor.role());});}
  mc.options.hideGui=true;mc.player.setYRot(32);mc.player.setXRot(6);
  if(time>1100){finished=true;System.out.println("DOGHEAD_NATIVE_CAPTURE_COMPLETE=6");mc.stop();}
 }
 @SubscribeEvent public static void render(RenderFrameEvent.Post e){if(stage<0||stage>5||shot==stage||time<100+stage*160+20)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;if(stage<3&&time<100+stage*160+55)return;if(stage>=3){var rendered=mc.level.getEntitiesOfClass(DogheadEntity.class,new net.minecraft.world.phys.AABB(-4,80,-4,4,85,4)).stream().findFirst().orElse(null);Clip wanted=stage==3?Clip.THRUST:stage==4?Clip.BOW:Clip.CHOP;if(rendered==null||rendered.clip()!=wanted||Math.abs(rendered.clipAge(0)-wanted.marker)>1)return;}try{Files.createDirectories(OUT);try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(OUT.resolve(Role.values()[stage%3].id()+(stage>=3?"-attack":"")+".png"));}shot=stage;System.out.println("DOGHEAD_NATIVE_FRAME="+Role.values()[stage%3]);}catch(Exception x){throw new RuntimeException(x);}}
}
