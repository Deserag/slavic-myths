package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Actual framebuffer/owner-bound item rendering in disposable integrated-server world. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class Issue217Probe {
 static boolean opened,requested,ready;static int tick,selected=-1;static List<String> ids;
 static List<String> held; static boolean headsPlaced; static final boolean FOCUSED=Boolean.getBoolean("slavicmyths.issue217.focused"); static final int HOLD=FOCUSED?60:30;
 static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
 static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> fn){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->fn.accept(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst()));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
  if(Boolean.getBoolean("slavicmyths.issue105.wave"))return;
  if(Boolean.getBoolean("slavicmyths.issue225.world"))return;
  if(Boolean.getBoolean("slavicmyths.issue217.crossbows")||Boolean.getBoolean("slavicmyths.issue223.finist"))return;
  var mc=Minecraft.getInstance();
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.2D);mc.createWorldOpenFlows().openWorld("VisualQA",()->{});}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;ids=new ArrayList<>();for(String line:Files.readAllLines(Path.of(System.getProperty("slavicmyths.issue217.catalog")))){if(line.contains("\tweapons\t"))ids.add(line.split("\t")[0]);}held=new ArrayList<>(ids);held.addAll(List.of("retainer_shield","beast_challenge_horn"));if(FOCUSED)held=new ArrayList<>(List.of("ohotnichiy_rog"));ids.addAll(List.of("retainer_shield","beast_challenge_horn","birch_bark","animal_fat","goat_hide","large_animal_bone","stone_spearhead","stone_light_spearhead","gold_spearhead","gold_light_spearhead","iron_spearhead","iron_light_spearhead","silver_spearhead","silver_light_spearhead","tugarin_head","skim_head","cursed_volkolak_head","likho_head","mor_likho_head","large_podzemnik_head"));
   server(p->{for(int oldX=-10;oldX<=10;oldX+=4){p.serverLevel().removeBlock(new net.minecraft.core.BlockPos(oldX,100,-4),false);p.serverLevel().removeBlock(new net.minecraft.core.BlockPos(oldX,101,-4),false);}int offset=0;for(String fig:List.of("fire_idol_tree","fire_idol_sun","fire_idol_circle")){var bp=new net.minecraft.core.BlockPos(offset*3-3,99,-6);p.serverLevel().setBlock(bp,net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState(),3);p.serverLevel().setBlock(bp.above(),net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",fig)).defaultBlockState(),3);offset++;}p.serverLevel().setDayTime(6000);p.serverLevel().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,p.getServer());p.setGameMode(GameType.CREATIVE);p.teleportTo(p.serverLevel(),0,100,0,180,8);p.getInventory().clearContent();mc.execute(()->{ready=true;mc.setScreen(new Grid());});});
  }
  if(!ready)return;tick++;
  int pages=(ids.size()+47)/48;
  if(tick<=pages*30){if(tick%30==20)capture(mc,"inventory-page"+(tick/30));return;}
  int age=tick-pages*30,index=age/HOLD;
  if(index>=held.size()*4){
   int headAge=age-held.size()*4*HOLD;
   if(!headsPlaced){headsPlaced=true;mc.options.setCameraType(CameraType.FIRST_PERSON);server(p->{p.serverLevel().setBlock(new net.minecraft.core.BlockPos(0,99,14),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);p.teleportTo(p.serverLevel(),0,100,14,180,0);int pos=-10;for(String id:List.of("tugarin_head","skim_head","cursed_volkolak_head","likho_head","mor_likho_head","large_podzemnik_head")){var at=new net.minecraft.core.BlockPos(pos,99,-4);p.serverLevel().removeBlock(at.above(2),false);p.serverLevel().removeBlock(at.above(),false);var stack=item(id);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);var use=new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at).add(0,.5,0),net.minecraft.core.Direction.UP,at,false));var result=stack.useOn(use);if(p.serverLevel().getBlockEntity(at.above())==null)throw new AssertionError("Trophy placement failed "+id+result);System.out.println("ISSUE217_HEAD_PLACED="+id);pos+=4;}});}
   if(headAge==25||headAge==45||headAge==85||headAge==105)capture(mc,"heads-native-tick"+headAge);
   if(headAge==125)mc.setScreen(new Skins());
   if(headAge>=130&&headAge<=179&&headAge%10==7)capture(mc,"both-skins-phase"+Skins.phase(headAge));
   if(headAge==180)mc.setScreen(new Sides());
   if(headAge>=180&&headAge<=229&&headAge%10==7)capture(mc,"whip-side-phase"+Math.clamp((headAge-180)/10,0,4));
   if(headAge>235){System.out.println("ISSUE217_CAPTURE_DONE actual client frames, master volume20%; no external input");mc.stop();}return;
  }
  if(index!=selected){selected=index;mc.setScreen(null);mc.options.setCameraType(index%4<2?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);boolean off=index%2==1;String id=held.get(index/4);server(p->{p.getInventory().clearContent();p.getInventory().selected=0;p.setItemInHand(off?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND,item(id));p.inventoryMenu.broadcastChanges();p.connection.send(new ClientboundSetCarriedItemPacket(0));});}
  int phase=age%HOLD;
  if(FOCUSED&&phase==6&&List.of("retainer_shield","beast_challenge_horn").contains(held.get(index/4))){var hand=index%2==1?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND;mc.player.startUsingItem(hand);server(p->p.getItemInHand(hand).getItem().use(p.level(),p,hand));}
  if(FOCUSED&&phase==20&&List.of("retainer_shield","beast_challenge_horn").contains(held.get(index/4)))capture(mc,held.get(index/4)+"-view"+(index%4)+"-using");
  if(FOCUSED&&phase==22){mc.player.stopUsingItem();server(p->p.stopUsingItem());}
  if(phase==(FOCUSED?35:15)){var hand=index%2==1?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND;mc.player.swing(hand);server(p->p.swing(hand,true));}
  if(FOCUSED&&phase==30){var h=index%2==1?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND;var m=mc.getItemRenderer().getModel(mc.player.getItemInHand(h),mc.level,mc.player,0);var ps=new com.mojang.blaze3d.vertex.PoseStack();var result=m.applyTransform(index%4<2?(index%2==1?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND):(index%2==1?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),ps,index%2==1);System.out.println("ISSUE217_BAKED_MODEL="+held.get(index/4)+" view="+(index%4)+" resolved="+m.getClass().getName()+" transformed="+result.getClass().getName()+" quads="+result.getQuads(null,null,net.minecraft.util.RandomSource.create(0)).size());}
  if(FOCUSED?(phase==30||phase==36||phase==38||phase==40||phase==44||phase==48):(phase==10||phase==18||phase==24))capture(mc,held.get(index/4)+"-view"+(index%4)+"-phase"+phase);
 }
 static void capture(Minecraft mc,String name)throws Exception{System.out.println("ISSUE225_NATIVE_SWING="+name+" swinging="+mc.player.swinging+" progress="+mc.player.getAttackAnim(mc.getTimer().getGameTimeDeltaPartialTick(false))+" arm="+mc.player.swingingArm);Path out=Path.of(System.getProperty("slavicmyths.issue217.output"));Files.createDirectories(out);try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(out.resolve(name+".png"));}System.out.println("ISSUE217_FRAME="+name);}
 static final class Skins extends Screen {
  final net.minecraft.client.player.RemotePlayer[] actors=new net.minecraft.client.player.RemotePlayer[4];
  static int phase(int headAge){return Math.clamp((headAge-130)/10,0,4);}
  Skins(){super(Component.literal("Actual Steve/Alex renderer"));for(int i=0;i<4;i++){final boolean slim=i>=2;actors[i]=new net.minecraft.client.player.RemotePlayer(Minecraft.getInstance().level,new com.mojang.authlib.GameProfile(new UUID(217,i),"QA217"+i)){
   @Override public net.minecraft.client.resources.PlayerSkin getSkin(){return new net.minecraft.client.resources.PlayerSkin(ResourceLocation.withDefaultNamespace("textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png"),null,null,null,slim?net.minecraft.client.resources.PlayerSkin.Model.SLIM:net.minecraft.client.resources.PlayerSkin.Model.WIDE,false);}
  };}}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xff202830);int headAge=tick-((ids.size()+47)/48)*30-held.size()*4*HOLD;float amount=new float[]{0,.2F,.45F,.72F,1}[phase(headAge)];g.drawString(font,"ACTUAL CLIENT PlayerRenderer / Steve & Alex / R & L / attack="+amount,6,5,0xffffff);
   var names=List.of("ohotnichiy_rog");
   for(int row=0;row<4;row++)for(int col=0;col<names.size();col++){var p=actors[row];boolean left=row%2==1;p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,left?ItemStack.EMPTY:item(names.get(col)));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,left?item(names.get(col)):ItemStack.EMPTY);p.swinging=amount>0&&amount<1;p.swingingArm=left?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND;p.attackAnim=p.oAttackAnim=amount;p.tickCount=1000;int xx=col*125+5,yy=row*78+24;g.drawString(font,(row>=2?"Alex":"Steve")+(left?" L ":" R ")+names.get(col).substring(0,Math.min(12,names.get(col).length())),xx,yy,0xffffff);
    net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsAngle(g,xx,yy+10,xx+120,yy+76,28,.0625F,0,0,p);
   }
  }
 }
 static final class Grid extends Screen{
  Grid(){super(Component.literal("Issue217 actual inventory renderer"));}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xff202830);g.drawString(font,"#217 ACTUAL GUI | one outlined slot per item | volume20%",8,8,0xffffff);int page=Math.min(tick/30,(ids.size()-1)/48);for(int n=0;n<48&&page*48+n<ids.size();n++){String id=ids.get(page*48+n);int xx=12+(n%8)*76,yy=28+(n/8)*49;g.fill(xx-1,yy-1,xx+17,yy+17,0xffcccccc);g.fill(xx,yy,xx+16,yy+16,0xff606060);g.renderItem(item(id),xx,yy);g.drawString(font,id.substring(0,Math.min(12,id.length())),xx,yy+21,0xffffff);}}
 }
 static final class Sides extends Screen{
  final net.minecraft.client.player.RemotePlayer[] actors=new Skins().actors;
  Sides(){super(Component.literal("Native side render, actual whip contact"));}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xff202830);int headAge=tick-((ids.size()+47)/48)*30-held.size()*4*HOLD;float phase=new float[]{0,.2F,.5F,.72F,1}[Math.clamp((headAge-180)/10,0,4)];g.drawString(font,"ACTUAL PlayerRenderer / side view / grip unchanged / attack="+phase,6,5,0xffffff);
   for(int row=0;row<4;row++){var p=actors[row];boolean off=row%2==1;p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,off?ItemStack.EMPTY:item("poloz_whip"));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,off?item("poloz_whip"):ItemStack.EMPTY);p.swinging=phase>0&&phase<1;p.swingingArm=off?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND;p.attackAnim=p.oAttackAnim=phase;p.tickCount=1000;p.yBodyRot=180;p.setYRot(180);p.yHeadRot=p.yHeadRotO=180;p.setXRot(0);int yy=40+row*78;g.drawString(font,(row<2?"Steve":"Alex")+(off?" OFF_HAND":" MAIN_HAND"),10,yy,0xffffff);
    net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory(g,220,yy+48,26,new org.joml.Vector3f(0,p.getBbHeight()/2+.0625F,0),new org.joml.Quaternionf().rotateZ((float)Math.PI).rotateY((float)Math.PI/2),new org.joml.Quaternionf(),p);
   }
  }
 }
}
