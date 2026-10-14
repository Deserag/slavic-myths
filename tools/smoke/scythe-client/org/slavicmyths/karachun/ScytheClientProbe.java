package org.slavicmyths.karachun;
import java.nio.file.*;
import java.util.UUID;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
/** Actual framebuffer capture in a disposable copied world. Not survival acceptance. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ScytheClientProbe {
 private static boolean opened,started,done;private static int time,stage=-1,shot=-1;private static Karachun boss;
 private static final String[] NAMES={"boss-approved-scythe","first-person-idle","first-person-attack","first-person-after","third-person-idle","third-person-attack","third-person-after","first-person-left","first-person-fov90","first-person-fov110","gui-icon","steve-wide","alex-slim","breath-attack","harvest-attack","third-person-walk","third-person-run","third-person-jump","third-person-crouch","third-person-left","armor-unique-attack","armor-perunite","victor-spear","likho-staff","ataman-flail","updated-items"};
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.createWorldOpenFlows().openWorld("BossReworkQA",()->{});}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null||done)return;
  if(!started){started=true;mc.getSingleplayerServer().execute(()->{
   var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();var w=p.serverLevel();
   w.setDayTime(6000);w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);w.setWeatherParameters(60000,0,false,false);
   for(var old:w.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(-20,75,-20,20,100,20),e->!(e instanceof net.minecraft.world.entity.player.Player)))old.discard();
   for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
   p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(w,0,81,-7,0,0);
   p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(NightContent.SCYTHE.get()));p.getInventory().selected=0;p.inventoryMenu.broadcastChanges();
   boss=NightContent.BOSS.get().create(w);boss.session=UUID.randomUUID();boss.anchor=new net.minecraft.world.phys.Vec3(0,81,0);boss.setNoAi(true);boss.moveTo(0,81,0,180,0);boss.yBodyRot=boss.yHeadRot=180;w.addFreshEntity(boss);
  });}
  if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
  time++;if(time<100)return;int selected=(time-100)/60;
  if(selected>=NAMES.length){done=true;System.out.println("SCYTHE_NATIVE_CLIENT_COMPLETE="+NAMES.length+"; skin="+mc.player.getSkin().model());mc.stop();return;}
  if(selected!=stage){if(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)mc.setScreen(null);stage=selected;mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyShift.setDown(false);
   mc.options.hideGui=stage==0;mc.options.fov().set(stage==8?90:stage==9?110:70);mc.options.mainHand().set(stage==7||stage==19?HumanoidArm.LEFT:HumanoidArm.RIGHT);
   mc.options.setCameraType((stage>=4&&stage<=6)||(stage>=15&&stage<=24)?CameraType.THIRD_PERSON_FRONT:CameraType.FIRST_PERSON);
   mc.player.setYRot(0);mc.player.setXRot(stage==0?-4:0);
   mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.teleportTo(p.serverLevel(),0,stage==0?82:81,stage==0?-7:-5,0,stage==0?-4:0);});
   if(stage==1)mc.getSingleplayerServer().execute(()->{if(boss!=null)boss.discard();var w=mc.getSingleplayerServer().overworld();for(var bird:w.getEntitiesOfClass(NightRaven.class,new net.minecraft.world.phys.AABB(-30,70,-30,30,120,30)))bird.discard();});
   if(stage==11||stage==12){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);mc.player.setXRot(0);
    var actor=new net.minecraft.client.player.RemotePlayer(mc.level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),stage==11?"SteveProbe":"AlexProbe")){
     private final boolean slim=stage==12;
     @Override public net.minecraft.client.resources.PlayerSkin getSkin(){return new net.minecraft.client.resources.PlayerSkin(net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png"),null,null,null,slim?net.minecraft.client.resources.PlayerSkin.Model.SLIM:net.minecraft.client.resources.PlayerSkin.Model.WIDE,true);}
    };
    actor.setId(98700+stage);actor.moveTo(0,81,0,180,0);actor.yBodyRot=actor.yHeadRot=180;actor.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(NightContent.SCYTHE.get()));
    mc.level.addEntity(actor);org.slavicmyths.client.NightClient.pose(new NightNetwork.PoseReceived(new NightNetwork.Pose(actor.getId(),0,stage==11?1:2,60)));
    if(stage==12){var old=mc.level.getEntity(98711);if(old!=null)old.discard();}
   }
   if(stage>=13){var old=mc.level.getEntity(98712);if(old!=null)old.discard();}
   if(stage>=15)mc.getSingleplayerServer().execute(()->{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.getAbilities().flying=false;p.onUpdateAbilities();
    for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})p.setItemSlot(slot,net.minecraft.world.item.ItemStack.EMPTY);
    if(stage==20||stage==21){String[] armor=stage==20?new String[]{"clear_sight_helmet","bogatyr_chestplate","indomitability_leggings","wind_boots"}:new String[]{"perunite_helmet","perunite_chestplate","perunite_leggings","perunite_boots"};var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};for(int i=0;i<4;i++)p.setItemSlot(slots[i],new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(NightContent.id(armor[i]))));}
    String name=stage==22?"victor_spear":stage==23?"likho_staff":stage==24?"ataman_flail":"karachun_scythe";
    p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(NightContent.id(name))));
    if(stage==25){String[] items={"victor_spear","likho_staff","ataman_flail","trophy_kalin_sabre","serpent_crossbow","three_breath_crossbow","rune_counterstrike_iii","rune_veles_oath_iv"};for(int i=0;i<items.length;i++)p.getInventory().setItem(i+1,new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(NightContent.id(items[i]))));}
    p.inventoryMenu.broadcastChanges();
   });
   if(stage==15||stage==16)mc.options.keyUp.setDown(true);if(stage==16){mc.options.keySprint.setDown(true);mc.player.setSprinting(true);}if(stage==18)mc.options.keyShift.setDown(true);
   System.out.println("SCYTHE_NATIVE_SCENE="+NAMES[stage]);
  }
  if((stage==2||stage==5||stage==20)&&time==100+stage*60+22)PacketDistributor.sendToServer(new NightNetwork.Action(0));
  if((stage==13||stage==14)&&time==100+stage*60+22)PacketDistributor.sendToServer(new NightNetwork.Action(stage==13?1:2));
  if(stage==17&&time==100+stage*60+22)mc.options.keyJump.setDown(true);
  if(stage==17&&time==100+stage*60+32)mc.options.keyJump.setDown(false);
  if((stage==10||stage==25)&&mc.screen==null)mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
 }
 @SubscribeEvent public static void render(RenderFrameEvent.Post event){
  if(stage<0||stage>=NAMES.length||time<100+stage*60+27||shot==stage)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  try{var out=Path.of("../../docs/weapon-stage-a/native-client");Files.createDirectories(out);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(NAMES[stage]+".png"));}System.out.println("SCYTHE_FRAME_SAVED="+NAMES[stage]);shot=stage;}catch(Exception ex){throw new RuntimeException(ex);}
 }
}
