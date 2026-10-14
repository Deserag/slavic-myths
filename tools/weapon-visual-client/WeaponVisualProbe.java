package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.karachun.*;
import org.slavicmyths.gorynych.*;

/** Real dev client and integrated server, copied disposable QA world. Not production. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WeaponVisualProbe {
 static boolean opened,requested,ready;static int tick,scene=-1;static long poseAt=-1;static int poseCombo;
 static final Set<String> frames=new HashSet<>();
 static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> fn){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->fn.accept(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst()));}
 static void select(int slot){server(p->{p.getInventory().selected=slot;p.connection.send(new ClientboundSetCarriedItemPacket(slot));});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.options.pauseOnLostFocus=false;mc.createWorldOpenFlows().openWorld("VisualQA",()->{});}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;server(p->{
   var w=p.serverLevel();w.setDayTime(6000);w.setWeatherParameters(0,12000,false,false);w.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
   for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){w.setBlock(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState(),3);for(int y=100;y<106;y++)w.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
   p.setGameMode(GameType.CREATIVE);p.teleportTo(w,0,100,0,180,8);p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(NightContent.SCYTHE.get()));p.getInventory().setItem(1,new ItemStack(GorynychContent.item("serpent_crossbow")));p.getInventory().setItem(2,new ItemStack(GorynychContent.item("three_breath_crossbow")));p.getInventory().setItem(9,new ItemStack(Items.ARROW,64));p.getInventory().selected=0;p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();p.connection.send(new ClientboundSetCarriedItemPacket(0));
   for(double x:new double[]{-2,0,2}){Zombie z=EntityType.ZOMBIE.create(w);z.setNoAi(true);z.setPersistenceRequired();z.setPos(x,100,-2);z.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);z.setHealth(200);z.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));w.addFreshEntity(z);}
   mc.execute(()->{ready=true;if(Boolean.getBoolean("slavicmyths.morTargeted"))tick=720;mc.setScreen(null);});
  });}
  if(!ready)return;tick++;
  int next=tick<720?tick/80:9+(tick-720)/140;if(next!=scene){scene=next;poseAt=-1;mc.options.setCameraType(CameraType.FIRST_PERSON);mc.setScreen(null);
   if(scene==0)select(0);if(scene==1)select(1);if(scene==2)select(2);if(scene==3){select(0);mc.setScreen(new InventoryScreen(mc.player));}
   if(scene==4||scene==5||scene==6){select(0);if(scene==5){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);server(p->{for(var z:p.serverLevel().getEntitiesOfClass(Zombie.class,p.getBoundingBox().inflate(12)))z.setPos(8,100,8);});}}
   if(scene==7){select(1);mc.player.setMainArm(HumanoidArm.LEFT);}
   if(scene==8){select(0);mc.player.setMainArm(HumanoidArm.LEFT);}
   if(scene>=9&&scene<15){mc.player.setMainArm(HumanoidArm.RIGHT);select(2);server(p->setupCase(p,scene-9));}
   if(scene==15||Boolean.getBoolean("slavicmyths.morTargeted")&&scene==11){System.out.println("WEAPON_VISUAL_CLIENT_CAPTURE_DONE frames="+frames.size()+" actual client/integrated server; not manual free play");mc.stop();return;}

   System.out.println("WEAPON_VISUAL_SCENE="+scene);
  }
  if(scene>=9&&scene<15){int local=(tick-720)%140;
   if(local==10){shift(mc,true);if(scene==11||scene==13)mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);if(scene>9){var event=new InputEvent.InteractionKeyMappingTriggered(0,mc.options.keyAttack,net.minecraft.world.InteractionHand.MAIN_HAND);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);if(!event.isCanceled()||event.shouldSwingHand())throw new AssertionError("Shift attack did not select volley");}shift(mc,false);}
   if(local==20){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);}
   if(local==60){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}
   if(local==65)mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);
   if(local==100)server(p->verifyCase(p));
  }
  if((scene==4||scene==5||scene==6)&&tick%80==25){PacketDistributor.sendToServer(new NightNetwork.Action(0));System.out.println("SCYTHE_REAL_ACTION_SENT scene="+scene);}
 }
 @SubscribeEvent public static void pose(NightNetwork.PoseReceived e){var mc=Minecraft.getInstance();if(mc.player!=null&&e.pose.entity()==mc.player.getId()&&e.pose.duration()>0){poseAt=mc.level.getGameTime();poseCombo=e.pose.combo();System.out.println("SCYTHE_REAL_POSE_RECEIVED combo="+poseCombo+" duration="+e.pose.duration());}}
 @SubscribeEvent public static void render(RenderFrameEvent.Post e){var mc=Minecraft.getInstance();if(!ready||mc.level==null||scene<0||scene>=15)return;
  if(scene>=4&&scene<=6&&poseAt>=0){long age=mc.level.getGameTime()-poseAt;if(age<=22)capture(mc,"scene"+scene+"-swing-age"+age);}
  else if(scene<9&&tick%80>=35&&tick%80<45)capture(mc,"scene"+scene+"-idle");
  else if(scene>=9){int local=(tick-720)%140;for(int sample:new int[]{15,45,64,70,78,90,102})if(local==sample)capture(mc,"scene"+scene+"-volley-tick"+sample);}

 }
 static void shift(Minecraft mc,boolean value){mc.player.setShiftKeyDown(value);mc.player.input.shiftKeyDown=value;mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket(mc.player,value?net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY:net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));}
 static int caseId=-1,ammoBefore,wearBefore;static final List<Long> launched=new ArrayList<>();static final Set<Integer> muzzleLanes=new HashSet<>();static int impacts,clouds;static boolean directEffect;static final List<Zombie> targets=new ArrayList<>();
 static void setupCase(net.minecraft.server.level.ServerPlayer p,int id){
  caseId=id;launched.clear();muzzleLanes.clear();impacts=clouds=0;directEffect=false;targets.clear();var w=p.serverLevel();
  for(var entity:w.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,p.getBoundingBox().inflate(40),x->x instanceof Zombie||x instanceof net.minecraft.world.entity.projectile.AbstractArrow||x instanceof BreathCloud))entity.discard();
  if(id==0){p.getInventory().setItem(2,new ItemStack(GorynychContent.item("three_breath_crossbow")));p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();}
  p.setGameMode(GameType.SURVIVAL);ammoBefore=p.getInventory().countItem(Items.ARROW);wearBefore=p.getMainHandItem().getDamageValue();p.teleportTo(w,0,100,0,180,0);
  for(double x:new double[]{-1.4,0,1.4}){Zombie z=EntityType.ZOMBIE.create(w);z.setNoAi(true);z.setPersistenceRequired();z.setPos(x,100,-8);z.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);z.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);z.setHealth(200);z.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));w.addFreshEntity(z);targets.add(z);}
  System.out.println("VOLLEY_CASE_STARTED id="+id);
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){
  if(e.getLevel().isClientSide||caseId<0)return;
  if(e.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow a&&a.getOwner() instanceof net.minecraft.server.level.ServerPlayer){launched.add(e.getLevel().getGameTime());muzzleLanes.add(a.getPersistentData().getInt("GorynychMuzzleLane"));int expected=caseId/2+1;if(a.getPersistentData().getInt("GorynychSpecial")!=expected)throw new AssertionError("wrong arrow effect");System.out.println("VOLLEY_ARROW case="+caseId+" tick="+e.getLevel().getGameTime()+" mode="+expected+" direction="+a.getDeltaMovement()+" muzzle="+a.position()+" lane="+a.getPersistentData().getInt("GorynychMuzzleLane")+" pickup="+a.pickup);}
  if(e.getEntity() instanceof BreathCloud)clouds++;
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void impact(net.neoforged.neoforge.event.entity.ProjectileImpactEvent e){if(e.getProjectile().level().isClientSide||caseId<0||!(e.getProjectile() instanceof net.minecraft.world.entity.projectile.AbstractArrow)||!(e.getProjectile().getOwner() instanceof net.minecraft.server.level.ServerPlayer))return;if(e.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit&&hit.getEntity() instanceof Zombie){impacts++;System.out.println("VOLLEY_REAL_ENTITY_HIT case="+caseId+" at="+hit.getLocation());}}
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void effect(net.neoforged.neoforge.event.entity.ProjectileImpactEvent e){if(e.getProjectile().level().isClientSide||caseId<2)return;if(e.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit&&hit.getEntity() instanceof Zombie z){if(caseId/2==1&&z.getRemainingFireTicks()>0)directEffect=true;if(caseId/2==2&&z.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN))directEffect=true;}}
 static void verifyCase(net.minecraft.server.level.ServerPlayer p){
  if(p.getInventory().countItem(Items.ARROW)!=ammoBefore-1)throw new AssertionError("survival ammo: must spend one arrow per triple charge");
  if(p.getMainHandItem().getDamageValue()!=wearBefore+3)throw new AssertionError("three arrows must consume three durability");
  var state=SerpentCrossbow.state(p.getMainHandItem());if(state.getInt("Mode")!=caseId/2||state.getBoolean("Burst")!=(caseId%2==1))throw new AssertionError("actual control state mismatch");
  if(Boolean.getBoolean("slavicmyths.morTargeted")&&impacts!=3)throw new AssertionError("Mor must deliver all three arrows to enemies, not its own clouds");
  if(!muzzleLanes.equals(Set.of(0,1,2)))throw new AssertionError("three distinct dragon mouths must fire");
  if(launched.size()!=3)throw new AssertionError("expected exactly three arrows: "+launched);
  if(caseId%2==1){if(launched.get(1)-launched.get(0)!=4||launched.get(2)-launched.get(1)!=4)throw new AssertionError("burst spacing");}else if(!launched.get(0).equals(launched.get(1))||!launched.get(1).equals(launched.get(2)))throw new AssertionError("fan not simultaneous");
  boolean damage=targets.stream().anyMatch(z->z.getHealth()<200);boolean weak=targets.stream().anyMatch(z->z.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS));
  if(impacts<1||!damage||caseId<2&&!weak||caseId>=2&&!directEffect)throw new AssertionError("missing real hit/damage/effect case="+caseId+" hits="+impacts+" weak="+weak+" effect="+directEffect);
  System.out.println("VOLLEY_RUNTIME_PASS case="+caseId+" arrows="+launched.size()+" hits="+impacts+" damage="+damage+" weakness="+weak+" flameOrStorm="+directEffect+" clouds="+clouds+" ammoSpent="+(ammoBefore-p.getInventory().countItem(Items.ARROW))+" durabilitySpent="+(p.getMainHandItem().getDamageValue()-wearBefore)+" remainingHP="+targets.stream().map(Zombie::getHealth).toList());
 }
 static void capture(Minecraft mc,String name){if(!frames.add(name))return;try{Path out=Path.of(System.getProperty("slavicmyths.visualOutput"));Files.createDirectories(out);try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(out.resolve(name+".png"));}System.out.println("WEAPON_VISUAL_FRAME="+name);}catch(Exception ex){throw new RuntimeException(ex);}}
}
