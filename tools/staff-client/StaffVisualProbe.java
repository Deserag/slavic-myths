package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class StaffVisualProbe {
 static boolean opened,requested,ready;static int tick,scene=-1,waiting;static String[] names=new String[9];static final Set<Integer> frames=new HashSet<>();
 static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst()));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
  if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
  if(!ready&&++waiting%100==0)System.out.println("STAFF_WAIT_SCREEN "+(mc.screen==null?"none":mc.screen.getClass().getName()+" "+mc.screen.getTitle().getString()));
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.options.pauseOnLostFocus=false;mc.createWorldOpenFlows().openWorld("StaffVisualQA",()->{});}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;server(p->{var w=p.serverLevel();w.setDayTime(6000);w.setWeatherParameters(0,12000,false,false);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.teleportTo(w,0,100,0,180,8);p.getInventory().clearContent();
   for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){w.setBlock(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState(),3);for(int y=100;y<106;y++)w.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
   String[] ids={"carved_staff","storm_staff","veles_staff","likho_staff","morozko_staff"};int slot=0;for(String id:ids){names[slot]=id;p.getInventory().setItem(slot++,IssueItem(id));}
   for(var holder:w.getRecipeManager().getAllRecipesFor(org.slavicmyths.armorer.ArmorerRecipe.TYPE.get())){var r=holder.value();if(!r.copyComponents||slot>=9)continue;
    var items=new ArrayList<ItemStack>(Collections.nCopies(16,ItemStack.EMPTY));for(int i=0;i<r.getIngredients().size();i++)if(!r.getIngredients().get(i).isEmpty())items.set(r.shapeless?i:(i/r.width)*4+i%r.width,r.getIngredients().get(i).getItems()[0].copy());
    var output=r.assemble(new org.slavicmyths.armorer.ArmorerInput(4,4,items),w.registryAccess());names[slot]=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(output.getItem()).getPath();p.getInventory().setItem(slot++,output);
   }p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();mc.execute(()->{ready=true;mc.setScreen(null);});});}
  if(!ready)return;tick++;int next=tick/30;if(next>=(Boolean.getBoolean("slavicmyths.staffGuiOnly")?9:36)){System.out.println("STAFF_VISUAL_CAPTURE_DONE frames="+frames.size()+" real client; scripted views, not manual acceptance");mc.stop();return;}
  int wanted=Boolean.getBoolean("slavicmyths.staffGuiOnly")?next*4+3:next;if(wanted!=scene){scene=wanted;int slot=scene/4,view=scene%4;mc.setScreen(null);mc.options.setCameraType(view==2?CameraType.THIRD_PERSON_FRONT:CameraType.FIRST_PERSON);mc.player.setMainArm(view==1?HumanoidArm.LEFT:HumanoidArm.RIGHT);server(p->{p.getInventory().selected=slot;p.connection.send(new ClientboundSetCarriedItemPacket(slot));});if(view==3)mc.setScreen(new InventoryScreen(mc.player));}
 }
 static ItemStack IssueItem(String id){return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
 @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!ready||scene<0||scene>=36||tick%30<20||!frames.add(scene))return;
  Path out=Path.of(System.getProperty("slavicmyths.staffVisualOutput"));Files.createDirectories(out);String name=String.format("%02d-%s-view%d.png",scene,names[scene/4],scene%4);try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(out.resolve(name));}System.out.println("STAFF_VISUAL_FRAME "+name);
 }
}
