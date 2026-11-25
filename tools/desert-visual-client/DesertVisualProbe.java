package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Isolated, fresh, disposable real-client acceptance captures. Never opens an existing world. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class DesertVisualProbe {
 static final boolean DESC=Boolean.getBoolean("slavicmyths.desertDescriptionOnly");
 static final boolean GRIP=Boolean.getBoolean("slavicmyths.desertGripOnly");
 static final boolean MOTION=Boolean.getBoolean("slavicmyths.desertMotionOnly");static final int STRIDE=MOTION?2:4;
 static int warmup=80;static boolean opened,ready,requested;static volatile String failure;static int ticks,scene=-1;static final Set<String> frames=new HashSet<>();
 static final String[] IDS=DESC?new String[]{"colony_scepter"}:GRIP?new String[]{"desert_halberd","desert_scythe","buried_hammer","lamya_weapon"}:new String[]{"desert_sword","desert_axe","desert_mace","desert_halberd","desert_scythe","scorpion_double_sting","stinger_dagger","sand_sling","desert_glass_blade","dune_mace","buried_hammer","hala_solar_staff","colony_scepter","four_winds_fists","lamya_weapon","quicksand_whip","mirage_bow","ice_sword"};

 static final Path OUT=Path.of("../../art/desert-weapons/revision-2/game");
 static void runtimeChecks(net.minecraft.server.level.ServerPlayer p) {
  var shield=new ItemStack(Items.SHIELD);p.setItemInHand(InteractionHand.OFF_HAND,shield);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item("four_winds_fists")));
  org.slavicmyths.combat.TugarinPair.update(p);if(!p.getOffhandItem().isEmpty())throw new AssertionError("Pair did not reserve second hand");
  p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);org.slavicmyths.combat.TugarinPair.update(p);if(!ItemStack.isSameItemSameComponents(shield,p.getOffhandItem()))throw new AssertionError("Pair lost offhand shield");
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item("buried_hammer")));org.slavicmyths.combat.TugarinPair.update(p);if(!p.getOffhandItem().isEmpty())throw new AssertionError("Two handed weapon did not reserve hand");
  p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);org.slavicmyths.combat.TugarinPair.update(p);if(!ItemStack.isSameItemSameComponents(shield,p.getOffhandItem()))throw new AssertionError("Two handed weapon lost shield");
  p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);var bow=(org.slavicmyths.desert.MirageBowItem)item("mirage_bow");var held=new ItemStack(bow);p.setItemInHand(InteractionHand.MAIN_HAND,held);p.getInventory().setItem(8,new ItemStack(Items.ARROW,64));p.setGameMode(GameType.SURVIVAL);
  if(bow.intercepts(org.slavicmyths.combat.WeaponInputHandler.Action.RIGHT_CLICK))throw new AssertionError("Bow intercepted vanilla draw");
  bow.use(p.serverLevel(),p,InteractionHand.MAIN_HAND);bow.releaseUsing(held,p.serverLevel(),p,bow.getUseDuration(held,p)-20);p.stopUsingItem();
  var arrows=p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.AbstractArrow.class,p.getBoundingBox().inflate(5),a->a.getOwner()==p);
  if(arrows.size()!=1||p.getInventory().getItem(8).getCount()!=63||held.getDamageValue()!=1)throw new AssertionError("Bow draw release/ammo/durability failed");
  if(Math.abs(arrows.getFirst().getBaseDamage()-26D/3D)>.001)throw new AssertionError("Bow damage changed");
  arrows.forEach(net.minecraft.world.entity.Entity::discard);p.setShiftKeyDown(true);
  var left=org.slavicmyths.combat.WeaponInputHandler.Action.SHIFT_LEFT_CLICK;var right=org.slavicmyths.combat.WeaponInputHandler.Action.SHIFT_RIGHT_CLICK;
  p.startUsingItem(InteractionHand.MAIN_HAND);bow.onWeaponAction(p,held,left);if(p.isUsingItem())throw new AssertionError("Special skill did not cancel native draw");int count=p.serverLevel().getEntitiesOfClass(org.slavicmyths.combat.WeaponProjectile.class,p.getBoundingBox().inflate(5)).size();if(count!=3)throw new AssertionError("Mirage triple shot count "+count);
  bow.onWeaponAction(p,held,left);if(p.serverLevel().getEntitiesOfClass(org.slavicmyths.combat.WeaponProjectile.class,p.getBoundingBox().inflate(5)).size()!=3)throw new AssertionError("Mirage cooldown failed");
  bow.onWeaponAction(p,held,right);if(p.serverLevel().getEntitiesOfClass(org.slavicmyths.combat.WeaponProjectile.class,p.getBoundingBox().inflate(5)).size()!=4)throw new AssertionError("Mirage piercing shot failed");
  p.setShiftKeyDown(false);p.setGameMode(GameType.CREATIVE);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
  try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("runtime-checks.txt"),"PASS: paired and two-handed offhand reservation/restoration; native bow draw/release; survival ammo and durability; retained full-draw 26 base damage; 3-shot skill; duplicate rejection; piercing skill.\n");}catch(Exception e){throw new RuntimeException(e);}
  var cow=net.minecraft.world.entity.EntityType.COW.create(p.serverLevel());cow.setNoAi(true);cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(5000);cow.setHealth(5000);cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);cow.setPos(0,-60,-3);p.serverLevel().addFreshEntity(cow);
  int checked=0;
  for(var spec:org.slavicmyths.desert.DesertWeaponSpec.values()){
   if(spec==org.slavicmyths.desert.DesertWeaponSpec.MIRAGE_BOW||spec==org.slavicmyths.desert.DesertWeaponSpec.HALA_STAFF)continue;
   var weapon=item(spec.id);var stack=new ItemStack(weapon);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
   int slots=spec.rank==org.slavicmyths.desert.DesertWeaponSpec.Rank.LEGENDARY?3:spec.rank==org.slavicmyths.desert.DesertWeaponSpec.Rank.RARE?2:1;
   for(int slot=1;slot<=slots;slot++){
    p.teleportTo(p.serverLevel(),0,-60,0,180,10);p.setOnGround(true);cow.setPos(0,-60,-3);cow.invulnerableTime=0;p.setShiftKeyDown(slot>1);
    var action=slot==1?org.slavicmyths.combat.WeaponInputHandler.Action.RIGHT_CLICK:slot==2?right:left;
    ((org.slavicmyths.combat.WeaponInputHandler)weapon).onWeaponAction(p,stack,action);
    long cd=org.slavicmyths.rpg.PathData.data(p).getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey(spec.id,slot));
    if(cd<=p.server.overworld().getGameTime()){
     var details=new StringBuilder("Skill not activated: "+spec.id+" slot "+slot+" player="+p.position()+" difficulty="+p.serverLevel().getDifficulty());
     for(int n=0;n<4;n++){double angle=n*Math.PI/2;var pos=p.position().add(Math.cos(angle)*1.6,0,Math.sin(angle)*1.6);var ant=org.slavicmyths.desert.AntContent.WORKER.get().create(p.serverLevel());ant.setPos(pos.x,pos.y,pos.z);details.append(" pos="+pos+" collision="+p.serverLevel().noCollision(ant)+" block="+p.serverLevel().getBlockState(ant.blockPosition()));}
     try{Files.writeString(OUT.resolve("failure.txt"),details);}catch(Exception ex){}throw new AssertionError(details.toString());
    }checked++;
   }
  }
  var solar=item("hala_solar_staff");var solarStack=new ItemStack(solar);p.setItemInHand(InteractionHand.MAIN_HAND,solarStack);
  for(boolean burst:new boolean[]{false,true}){p.setShiftKeyDown(burst);((org.slavicmyths.combat.WeaponInputHandler)solar).onWeaponAction(p,solarStack,burst?right:org.slavicmyths.combat.WeaponInputHandler.Action.RIGHT_CLICK);if(org.slavicmyths.combat.IndependentWeaponCooldowns.ready(p,"hala_solar_staff",burst))throw new AssertionError("Solar staff skill failed");checked++;}
  cow.discard();p.setShiftKeyDown(false);p.teleportTo(p.serverLevel(),0,-60,0,180,0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);org.slavicmyths.combat.TugarinPair.update(p);
  try{Files.writeString(OUT.resolve("skill-checks.txt"),"PASS: "+checked+" existing desert skill slots accepted by real server handlers against a live target; bow native draw, volley and pierce checked separately.\n");}catch(Exception e){throw new RuntimeException(e);}
  for(var entity:p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Entity.class,p.getBoundingBox().inflate(64),entity->!(entity instanceof net.minecraft.world.entity.player.Player)))entity.discard();
  System.out.println("DESERT_RUNTIME_PASS skills="+checked);
 }
 static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> fn){Minecraft.getInstance().getSingleplayerServer().execute(()->{try{fn.accept(Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().getFirst());}catch(Throwable t){failure=t.toString();t.printStackTrace();}});}
 static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();try{
  if(failure!=null)throw new IllegalStateException(failure);
  mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.1D);
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!opened&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){opened=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.guiScale().set(MOTION?3:2);mc.options.framerateLimit().set(60);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
   mc.createWorldOpenFlows().createFreshLevel("DesertWeapons-Isolated-"+System.currentTimeMillis(),new LevelSettings("Desert weapons visual QA",GameType.CREATIVE,false,net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(25112026L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
  if(mc.player==null||mc.getSingleplayerServer()==null||mc.level==null)return;
  if(!requested){requested=true;server(p->{p.serverLevel().setDayTime(6000);p.serverLevel().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,p.server);p.teleportTo(p.serverLevel(),0, -60,0,180,0);runtimeChecks(p);p.getInventory().clearContent();for(int i=0;i<IDS.length;i++)p.getInventory().setItem(i+9,new ItemStack(item(IDS[i])));p.getInventory().setItem(8,new ItemStack(Items.ARROW,64));p.inventoryMenu.broadcastChanges();mc.execute(()->ready=true);});}
  if(!ready||mc.getOverlay()!=null)return;if(warmup-->0)return;ticks++;
  int next=ticks/70;if(next!=scene){scene=next;mc.setScreen(null);mc.options.keyUse.setDown(false);if(mc.player.isUsingItem())mc.gameMode.releaseUsingItem(mc.player);
   if(scene>=IDS.length*STRIDE+(DESC?0:4)){Files.createDirectories(OUT);Files.writeString(OUT.resolve(DESC?"description-complete.txt":GRIP?"grip-complete.txt":MOTION?"motion-complete.txt":"complete.txt"),"Captured "+frames.size()+" real-client frames. Fresh test world. MASTER=10%. See the accompanying verification README for the exact pass coverage.\n");System.out.println("DESERT_VISUAL_COMPLETE "+frames.size());mc.stop();return;}
   int idx=scene/STRIDE;int mode=MOTION?(scene%2==0?0:2):scene%4;
   if(idx<IDS.length){String id=IDS[idx];server(p->{p.setGameMode(mode==1?GameType.SURVIVAL:GameType.CREATIVE);p.getInventory().setItem(0,new ItemStack(item(id)));p.getInventory().selected=0;p.setYRot(180);p.setXRot(0);p.inventoryMenu.broadcastChanges();p.connection.send(new ClientboundSetCarriedItemPacket(0));});var tested=new ItemStack(item(id));try{Files.createDirectories(OUT);Files.writeString(OUT.resolve(id+"-tooltip.txt"),String.join("\n",tested.getTooltipLines(Item.TooltipContext.of(mc.level),mc.player,TooltipFlag.NORMAL).stream().map(net.minecraft.network.chat.Component::getString).toList()));}catch(Exception ex){throw new RuntimeException(ex);}
   mc.options.setCameraType(mode==2?CameraType.THIRD_PERSON_FRONT:mode==3?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);}
   else{server(p->{p.getInventory().setItem(0,new ItemStack(item("mirage_bow")));p.getInventory().selected=0;p.inventoryMenu.broadcastChanges();});mc.options.setCameraType(scene%2==0?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);}
   System.out.println("DESERT_SCENE "+scene);
  }
  int local=ticks%70,idx=scene/STRIDE,mode=MOTION?(scene%2==0?0:2):scene%4;
  if(idx<IDS.length){if(mode==1&&local==20)mc.setScreen(new InventoryScreen(mc.player));if((mode==0||mode==2)&&local==35&&mc.screen==null)mc.player.swing(InteractionHand.MAIN_HAND);}
  else{if(local==10){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}if(local==40){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}}
 }catch(Throwable t){t.printStackTrace();System.out.println("DESERT_VISUAL_FAILURE "+t);mc.stop();}}
 @SubscribeEvent public static void tooltip(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(ready&&scene>=0&&!MOTION&&scene<IDS.length*4&&scene%4==1&&mc.screen instanceof InventoryScreen){e.getGuiGraphics().renderTooltip(mc.font,mc.player.getMainHandItem(),12,32);}}
 @SubscribeEvent public static void render(RenderFrameEvent.Post event){var mc=Minecraft.getInstance();if(!ready||mc.level==null||scene<0)return;int local=ticks%70;if(local==28||local==38||local==45){String id=scene/STRIDE<IDS.length?IDS[scene/STRIDE]:"bow_draw";String name=(DESC?"description-":GRIP?"grip-":MOTION?"motion-":"")+String.format("%02d-%s-%d",scene,id,local);if(frames.add(name)){try{Files.createDirectories(OUT);Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),c->{});Path src=mc.gameDirectory.toPath().resolve("screenshots").resolve(name+".png"); // Screenshot saving is asynchronous; copied after the run.
 }catch(Exception e){throw new RuntimeException(e);}}}}
}
