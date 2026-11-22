package org.slavicmyths.client;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.marine.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MarineWeaponVisualCheck {
 static int ticks;static volatile int cameraId,targetId;
 static final String[] NAMES={"trident","shield","anchor","staff","harpoon"};
 static Item item(int i){return switch(i){case 0->PharaohkaTrident.ITEM.get();case 1->ZhelvaShield.ITEM.get();case 2->MarineWeapons.ANCHOR.get();case 3->MarineWeapons.STAFF.get();default->MarineHarpoons.AMMO.get();};}
 static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,"weapon-"+name+".png",mc.getMainRenderTarget(),message->System.out.println("WEAPON_VISUAL "+name+" "+message.getString()));}
 static void camera(Minecraft mc,double x,double z,float yaw){var entity=mc.level.getEntity(cameraId);if(entity==null)return;entity.setPos(x,79.8,z);entity.xo=x;entity.yo=79.8;entity.zo=z;entity.setYRot(yaw);entity.yRotO=yaw;if(entity instanceof LivingEntity living){living.setYHeadRot(yaw);living.yHeadRotO=yaw;living.yBodyRot=yaw;living.yBodyRotO=yaw;}entity.setXRot(10);entity.xRotO=10;mc.options.hideGui=true;mc.setCameraEntity(entity);mc.options.setCameraType(CameraType.FIRST_PERSON);}
 static void equip(Minecraft mc,int index){var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);int slot=index==1?6:index;p.getInventory().selected=slot;p.connection.send(new ClientboundSetCarriedItemPacket(slot));p.setItemSlot(EquipmentSlot.OFFHAND,index==1?new ItemStack(ZhelvaShield.ITEM.get()):ItemStack.EMPTY);p.setYRot(0);p.setXRot(0);p.containerMenu.broadcastChanges();});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){if(Boolean.getBoolean("slavicmyths.shipInventoryVisual"))return;
  if(!Boolean.getBoolean("slavicmyths.marineWeaponVisual"))return;var mc=Minecraft.getInstance();mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.1D);if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;mc.options.pauseOnLostFocus=false;ticks++;
  if(ticks==40){var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
   var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayer(uuid);var world=p.serverLevel();server.setDifficulty(net.minecraft.world.Difficulty.PEACEFUL,true);world.setDayTime(6000);world.setWeatherParameters(6000,0,false,false);
   for(int x=-10;x<=10;x++)for(int z=-8;z<=20;z++){world.setBlock(new BlockPos(x,79,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);for(int y=80;y<=87;y++)world.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
   for(var entity:world.getEntitiesOfClass(net.minecraft.world.entity.projectile.ThrownTrident.class,p.getBoundingBox().inflate(160)))entity.discard();
   p.teleportTo(.5,80,.5);p.setYRot(0);p.setXRot(0);p.getInventory().clearContent();for(int i=0;i<5;i++)p.getInventory().setItem(i,new ItemStack(item(i),i==4?16:1));p.getInventory().setItem(5,new ItemStack(MarineWeapons.HARPOON.get()));
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(MarineArmor.HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(MarineArmor.CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(MarineArmor.LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(MarineArmor.BOOTS.get()));
   var stand=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(world);stand.setInvisible(true);stand.setNoGravity(true);stand.moveTo(5,79.8,.5);world.addFreshEntity(stand);cameraId=stand.getId();p.containerMenu.broadcastChanges();
   var cow=EntityType.COW.create(world);cow.setNoAi(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);cow.moveTo(.5,80,10);world.addFreshEntity(cow);targetId=cow.getId();
  });}
  if(Boolean.getBoolean("slavicmyths.bubbleProbe")&&ticks>=60){
   if(ticks==60){mc.setScreen(null);mc.setCameraEntity(mc.player);equip(mc,3);}
   if(ticks==80){System.out.println("BUBBLE_PROBE send main="+mc.player.getMainHandItem()+" paused="+mc.isPaused());net.neoforged.neoforge.network.PacketDistributor.sendToServer(new MarineWeaponControls.Secondary());}
   if(ticks==100){System.out.println("BUBBLE_PROBE client absorption="+mc.player.getAbsorptionAmount()+" paused="+mc.isPaused());mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());System.out.println("BUBBLE_PROBE server main="+p.getMainHandItem()+" absorption="+p.getAbsorptionAmount()+" data="+p.getPersistentData().getLong("MarineWeapon_bubbleUntil")+" now="+p.serverLevel().getGameTime()+" ready="+p.getPersistentData().getLong("MarineWeapon_bubble")+" alive="+p.isAlive()+" spectator="+p.isSpectator()+" direct="+MarineWeaponAbilities.bubble(p)+" after="+p.getAbsorptionAmount());});shot(mc,"bubble-probe");}
   if(ticks==140)mc.stop();return;
  }
  if(ticks>=120&&ticks<720){int index=(ticks-120)/120,phase=(ticks-120)%120;
   if(phase==0)equip(mc,index);
   mc.player.setYRot(0);mc.player.yRotO=0;mc.player.setYHeadRot(0);mc.player.yHeadRotO=0;mc.player.yBodyRot=0;mc.player.yBodyRotO=0;
   if(phase>=15&&phase<=35)camera(mc,3,.5,90);
   if(phase==35)shot(mc,NAMES[index]+"-side");
   if(phase>=45&&phase<=55)camera(mc,.5,3,180);
   if(phase==55)shot(mc,NAMES[index]+"-front");
   if(phase==65){mc.options.hideGui=false;mc.setCameraEntity(mc.player);mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);mc.player.setXRot(0);}
   if(phase==80)shot(mc,NAMES[index]+"-hand");
   if(phase==90)mc.setScreen(new InventoryScreen(mc.player));
   if(phase==100)shot(mc,NAMES[index]+"-inventory");
   if(phase==110)mc.setScreen(null);
  }
  if(ticks==740){mc.setCameraEntity(mc.player);equip(mc,2);}
  if(ticks==760)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setShiftKeyDown(false);MarineWeaponAbilities.use(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));});
  if(ticks==765)shot(mc,"anchor-chain");
  if(ticks==800)equip(mc,3);
  if(ticks==820)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.serverLevel().setBlock(new BlockPos(2,80,0),Blocks.WATER.defaultBlockState(),3);var target=(LivingEntity)p.serverLevel().getEntity(targetId);p.setLastHurtMob(target);p.setShiftKeyDown(true);MarineWeaponAbilities.use(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));p.setShiftKeyDown(false);});
  if(ticks==850)shot(mc,"water-vortex");
  if(ticks==900)net.neoforged.neoforge.network.PacketDistributor.sendToServer(new MarineWeaponControls.Secondary());
  if(ticks==920){System.out.println("WEAPON_BUBBLE absorption="+mc.player.getAbsorptionAmount());shot(mc,"protective-bubble");}
  if(ticks==1060)mc.stop();
 }
}