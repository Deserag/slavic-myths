package org.slavicmyths.verify.client;
import java.nio.file.*;import java.util.*;import java.util.concurrent.*;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.inventory.InventoryScreen;import net.minecraft.resources.ResourceLocation;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.world.item.ItemStack;import net.minecraft.world.level.GameType;
/** Disposable resource-pack preview, no production assets or mechanics are changed. */
public final class Art298 {
 static final Path OUT=Path.of("../../docs/implementation/chud298-preview-20261027");
 static final String[] IDS={"chud_master_pickaxe","chud_multitool_pickaxe","chud_multitool_axe","chud_multitool_shovel"};
 static int step=-1,shot=-1;static long at,born=System.nanoTime();static boolean started,done;static CompletableFuture<Void> reload,setup;static final List<String> images=new ArrayList<>();
 static void finish(Minecraft mc,Throwable ex){if(done)return;done=true;try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("runtime-receipt.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",ex==null?"PASS_CAPTURE_ONLY":"FAIL","error",ex==null?"":ex.toString(),"images",images,"resource_packs",mc.getResourcePackRepository().getSelectedIds(),"master_volume",mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).get(),"scope","actual dev-client screenshots; preview pack only, owner visual approval pending")));}catch(Exception e){e.printStackTrace();}if(ex!=null)ex.printStackTrace();mc.stop();}
 public static void tick(Minecraft mc){if(done||mc.player==null||mc.getSingleplayerServer()==null)return;try{
  if(System.nanoTime()-born>240_000_000_000L)throw new IllegalStateException("preview timeout");
  if(!started){started=true;mc.getWindow().setWindowed(854,480);mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.setCameraType(CameraType.FIRST_PERSON);mc.getResourcePackRepository().reload();mc.getResourcePackRepository().setSelected(mc.getResourcePackRepository().getSelectedIds().stream().filter(id->!id.equals("file/Chud298-preview")).toList());reload=mc.reloadResourcePacks();setup=CompletableFuture.runAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(true);p.getInventory().clearContent();p.setNoGravity(true);p.teleportTo(1378.5,210,1140.5);p.setYRot(0);p.setXRot(0);for(int i=0;i<4;i++)p.getInventory().setItem(i,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",IDS[i]))));p.inventoryMenu.broadcastChanges();},mc.getSingleplayerServer());return;}
  if(setup!=null){if(!setup.isDone())return;setup.join();setup=null;}
  if(reload!=null){if(!reload.isDone()||mc.getOverlay()!=null)return;reload.join();reload=null;at=System.nanoTime();}
  if(step>=0&&(shot!=step||System.nanoTime()-at<1_000_000_000L))return;
  step++;if(step>=16){finish(mc,null);return;}
  if(step==8){var packs=new ArrayList<>(mc.getResourcePackRepository().getSelectedIds());packs.add("file/Chud298-preview");mc.getResourcePackRepository().setSelected(packs);if(!mc.getResourcePackRepository().getSelectedIds().contains("file/Chud298-preview"))throw new IllegalStateException("preview pack unavailable");reload=mc.reloadResourcePacks();}
  mc.player.getInventory().selected=(step%8)/2;mc.player.setXRot(0);mc.player.setYRot(0);mc.setScreen(step%2==0?null:new InventoryScreen(mc.player));at=System.nanoTime();
 }catch(Throwable ex){finish(mc,ex);}}
 public static void capture(Minecraft mc){if(done||step<0||shot==step||reload!=null||mc.getOverlay()!=null||System.nanoTime()-at<2_500_000_000L)return;try{Files.createDirectories(OUT);String name=(step<8?"before-":"preview-")+IDS[(step%8)/2]+(step%2==0?"-hand":"-gui")+".png";try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(OUT.resolve(name));}images.add(name);shot=step;}catch(Exception ex){finish(mc,ex);}}
}
