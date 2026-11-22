package org.slavicmyths.client;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ShipInventoryVisualCheck {
 static int ticks; static volatile boolean ready;
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  if(!Boolean.getBoolean("slavicmyths.shipInventoryVisual"))return;
  var mc=Minecraft.getInstance();mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.1D);
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(ticks>=21&&!ready)return; ticks++;
  if(ticks==20){var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
   var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var w=p.serverLevel();w.setDayTime(6000);
   var ship=org.slavicmyths.ships.RaftContent.FIRST_SHIP.get().create(w);ship.moveTo(.5,80,.5);ship.setNoGravity(true);w.addFreshEntity(ship);p.teleportTo(.5,84,.5);p.startRiding(ship,true);ready=true;System.out.println("SHIP_FIXTURE mounted="+p.isPassenger());
  });}
  if(ticks==90||ticks==150){mc.setScreen(null);mc.player.getRecipeBook().setOpen(RecipeBookType.CRAFTING,ticks==150);mc.setScreen(new InventoryScreen(mc.player));}
  if(ticks==120||ticks==180){String name=ticks==120?"ship-inventory-book-closed.png":"ship-inventory-book-open.png";Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),x->System.out.println("SHIP_SCREENSHOT "+x.getString()));}
    if(ticks==210)mc.setScreen(new org.slavicmyths.ships.client.FirstShipInventoryHud.ShipScreen());
  if(ticks==240)Screenshot.grab(mc.gameDirectory,"ship-tab-clear.png",mc.getMainRenderTarget(),x->System.out.println("SHIP_SCREENSHOT "+x.getString()));
  if(ticks==260)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.stopRiding();p.teleportTo(40,85,40);});
  if(ticks==300)Screenshot.grab(mc.gameDirectory,"inventory-after-leaving-ship.png",mc.getMainRenderTarget(),x->System.out.println("SHIP_SCREENSHOT "+x.getString()));
  if(ticks==320)mc.setScreen(new CreaturePreview());
  if(ticks==350)Screenshot.grab(mc.gameDirectory,"creature-faces-front.png",mc.getMainRenderTarget(),x->System.out.println("FACE_SCREENSHOT "+x.getString()));
    if(ticks==380)mc.setScreen(new BossPreview(false));
  if(ticks==410)Screenshot.grab(mc.gameDirectory,"sisters-front.png",mc.getMainRenderTarget(),x->System.out.println("FACE_SCREENSHOT "+x.getString()));
  if(ticks==430)mc.setScreen(new BossPreview(true));
  if(ticks==460)Screenshot.grab(mc.gameDirectory,"sisters-back.png",mc.getMainRenderTarget(),x->System.out.println("FACE_SCREENSHOT "+x.getString()));
  if(ticks==480)mc.stop();
 }
 static final class CreaturePreview extends net.minecraft.client.gui.screens.Screen {
  final net.minecraft.world.entity.LivingEntity[] models=new net.minecraft.world.entity.LivingEntity[3];
  CreaturePreview(){super(net.minecraft.network.chat.Component.literal("Проверка лиц"));var mc=Minecraft.getInstance();String[] ids={"kikimora","bolotnik","rusalka"};for(int i=0;i<3;i++)models[i]=(net.minecraft.world.entity.LivingEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",ids[i])).create(mc.level);}
  @Override public void renderBackground(net.minecraft.client.gui.GuiGraphics g,int x,int y,float p){}
  @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float p){
   g.fill(0,0,width,height,0xff26333b);String[] names={"Кикимора","Болотник","Русалка"};
   for(int i=0;i<3;i++){int left=i*width/3;g.drawCenteredString(font,names[i],left+width/6,15,0xffffff);
    InventoryScreen.renderEntityInInventoryFollowsAngle(g,left+8,35,left+width/3-8,height-15,Math.min(65,height/4),.55F,0F,0F,models[i]);
   }
  }
 } static final class BossPreview extends net.minecraft.client.gui.screens.Screen {
  final net.minecraft.world.entity.LivingEntity[] models=new net.minecraft.world.entity.LivingEntity[3]; final boolean back;
  BossPreview(boolean back){super(net.minecraft.network.chat.Component.literal("Проверка сестёр"));this.back=back;var mc=Minecraft.getInstance();String[] ids={"neveya","lomeya","gneteya"};for(int i=0;i<3;i++)models[i]=(net.minecraft.world.entity.LivingEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",ids[i])).create(mc.level);}
  @Override public void renderBackground(net.minecraft.client.gui.GuiGraphics g,int x,int y,float p){}
  @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float p){
   g.fill(0,0,width,height,0xff26333b);String[] names={"Невея","Ломея","Гнетея"};
   for(int i=0;i<3;i++){int left=i*width/3;g.drawCenteredString(font,names[i],left+width/6,15,0xffffff);
    int scale=Math.min(50,(int)((height-75)/(models[i].getBbHeight()*2)));
    InventoryScreen.renderEntityInInventoryFollowsAngle(g,left+8,35,left+width/3-8,height-15,scale,.55F,back?3.14159F:0F,0F,models[i]);
   }
  }
 }}