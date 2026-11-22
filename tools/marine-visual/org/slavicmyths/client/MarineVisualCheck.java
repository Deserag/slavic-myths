package org.slavicmyths.client;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MarineVisualCheck {
 static int ticks,waiting;
 static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
 static void screenshot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),message->System.out.println("MARINE_VISUAL "+name+" "+message.getString()));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){if(Boolean.getBoolean("slavicmyths.shipInventoryVisual"))return;
  if(Boolean.getBoolean("slavicmyths.marineWeaponVisual"))return;
  var mc=Minecraft.getInstance();mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.1D);if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null){
   if(++waiting==100 && mc.screen!=null){screenshot(mc,"marine-bootstrap");System.out.println("MARINE_SCREEN "+mc.screen.getClass()+" "+mc.screen.getTitle().getString());for(var child:mc.screen.children())if(child instanceof net.minecraft.client.gui.components.AbstractWidget widget)System.out.println("MARINE_BUTTON "+widget.getMessage().getString());}
   return;
  }
  ticks++;
  if(ticks==40){var server=mc.getSingleplayerServer();var uuid=mc.player.getUUID();server.execute(()->{
   var player=server.getPlayerList().getPlayer(uuid);var world=player.serverLevel();
   world.setDayTime(6000);world.setWeatherParameters(6000,0,false,false);server.setDifficulty(net.minecraft.world.Difficulty.PEACEFUL,true);
   for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)world.setBlock(new BlockPos(x,79,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
   player.teleportTo(.5,80,.5);player.setYRot(0);player.setXRot(0);
   player.setItemSlot(EquipmentSlot.HEAD,item("tide_helmet"));player.setItemSlot(EquipmentSlot.CHEST,item("abyss_chestplate"));
   player.setItemSlot(EquipmentSlot.LEGS,item("storm_leggings"));player.setItemSlot(EquipmentSlot.FEET,item("waterwalk_boots"));
   String[] ids={"pharaohka_trident","zhelva_shield","storm_harpoon","abyss_staff","storm_anchor","tide_ring","abyss_necklace","storm_bracelet","tailwind_feather","tsar_pike_belt","zhelva_amulet","marine_compass","depths_lantern","breathing_bowl","rune_tide","rune_abyss","rune_storm","abyss_elixir","storm_tincture","tsar_pike_meat","ship_helm_module","ship_oar_module","ship_sail_module"};
   for(int i=0;i<ids.length;i++)player.getInventory().setItem(i,item(ids[i]));player.getInventory().selected=35;
   player.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
   for(var projectile:world.getEntitiesOfClass(net.minecraft.world.entity.projectile.ThrownTrident.class,player.getBoundingBox().inflate(160)))projectile.discard();player.containerMenu.broadcastChanges();
  });}
  if(ticks==120){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(0);mc.player.setXRot(0);}
  if(ticks==180)screenshot(mc,"marine-front");
  if(ticks==200)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(ticks==240)screenshot(mc,"marine-back");
  if(ticks==260)mc.setScreen(new InventoryScreen(mc.player));
  if(ticks==300)screenshot(mc,"marine-inventory");
  if(ticks==310)mc.stop();
  if(ticks==320){mc.setScreen(null);mc.options.setCameraType(CameraType.FIRST_PERSON);}
  if(ticks==360)screenshot(mc,"marine-in-hand");
  if(ticks==380)mc.options.keyUse.setDown(true);
  if(ticks==420)screenshot(mc,"marine-throwing");
  if(ticks==470){for(var entity:mc.level.entitiesForRendering())if(entity instanceof net.minecraft.world.entity.projectile.ThrownTrident)System.out.println("MARINE_PROJECTILE variant="+entity.getPersistentData().getInt(org.slavicmyths.marine.MarineTridentAppearance.KEY));}
  if(ticks==440){mc.options.keyUse.setDown(false);mc.getSingleplayerServer().execute(()->{
    var world=mc.getSingleplayerServer().overworld();
    for(int x=-2;x<=3;x++)for(int z=4;z<=9;z++)world.setBlock(new BlockPos(x,79,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
    var block=org.slavicmyths.marine.StratimBirdContent.HEAD.get();
    var pos=new BlockPos(0,80,7);var state=block.defaultBlockState();world.setBlock(pos,state,3);
    block.setPlacedBy(world,pos,state,null,item("stratim_bird_head"));
   });}
  if(ticks==520)screenshot(mc,"marine-stratim-trophy");
    if(ticks==550){mc.getSingleplayerServer().execute(()->{
    var world=mc.getSingleplayerServer().overworld();
    var blocks=java.util.List.of(org.slavicmyths.marine.DeepSeaSerpentContent.HEAD.get(),org.slavicmyths.marine.OceanZmeyContent.HEAD.get(),org.slavicmyths.marine.TsarPikeContent.HEAD.get(),org.slavicmyths.marine.ZhelvaContent.HEAD.get());
    for(int i=0;i<blocks.size();i++) {
        var block=blocks.get(i);var pos=new BlockPos(16+i*24,80,7);var state=block.defaultBlockState();
        world.setBlock(pos,state,3);block.setPlacedBy(world,pos,state,null,ItemStack.EMPTY);
        for(int x=-4;x<=5;x++)for(int z=-4;z<=10;z++)world.setBlock(new BlockPos(16+i*24+x,79,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
    }
   });}
  if(ticks>=600&&ticks<=900&&ticks%100==0){int i=(ticks-600)/100;mc.getSingleplayerServer().execute(()->{
    var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());player.teleportTo(16+i*24+.5,80,.5);player.setYRot(0);player.setXRot(0);
   });}
  if(ticks>=650&&ticks<=950&&ticks%100==50)screenshot(mc,"marine-trophy-"+((ticks-650)/100));
  if(ticks==1020)mc.stop();
 }
}