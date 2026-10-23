package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.finist.*;
import org.slavicmyths.item.ItemState;

/** Native client use/release, real integrated-server ticks and actual damage. No input injection. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class Issue223FinistProbe {
 static final int BASE_X=128,BASE_Z=128; // Empty test arena, away from placed trophy collision boxes.
 static boolean opened,requested,ready,done;static int tick,selected=-1;static Zombie target;static final List<Map<String,Object>> results=new ArrayList<>();
 @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
  if(!Boolean.getBoolean("slavicmyths.issue223.finist"))return;var mc=Minecraft.getInstance();
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.2D);mc.createWorldOpenFlows().openWorld("VisualQA",()->{});}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!requested){requested=true;Issue217Probe.server(p->{var w=p.serverLevel();for(var z:w.getEntitiesOfClass(Zombie.class,p.getBoundingBox().inflate(80)))z.discard();w.setDayTime(6000);for(int x=-32;x<=32;x++)for(int z=-32;z<=32;z++)w.setBlock(new net.minecraft.core.BlockPos(BASE_X+x,99,BASE_Z+z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);System.out.println("ISSUE223_EMPTY_ARENA=128,100,128 radius32; no trophy obstacles");p.setGameMode(GameType.SURVIVAL);p.teleportTo(w,BASE_X,100,BASE_Z,180,0);p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,Issue217Probe.item("finist_bow"));top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow().setEquippedCurio("belt",0,Issue217Probe.item("finist_quiver"));p.inventoryMenu.broadcastChanges();target=EntityType.ZOMBIE.create(w);target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);target.setHealth(1000);target.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.NETHERITE_HELMET));target.moveTo(BASE_X,100,BASE_Z-8);w.addFreshEntity(target);mc.execute(()->ready=true);});}
  if(!ready||done)return;int scene=tick/100,phase=tick%100;tick++;
  if(scene>=19&&tick<2100){int extra=tick-1900,stage=extra/40;if(selected!=19+stage){selected=19+stage;Issue217Probe.server(p->{p.teleportTo(p.serverLevel(),BASE_X,100,BASE_Z,180,0);if(stage<2)target.moveTo(BASE_X+(stage==0?-3:3),100,BASE_Z-8);if(stage==2)p.teleportTo(p.serverLevel(),BASE_X,100,BASE_Z,0,0);if(stage==3){for(int x=-6;x<=6;x++)for(int y=100;y<=104;y++)p.serverLevel().setBlock(new net.minecraft.core.BlockPos(BASE_X+x,y,BASE_Z-4),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);}if(stage==4){for(int x=-6;x<=6;x++)for(int y=100;y<=104;y++)p.serverLevel().removeBlock(new net.minecraft.core.BlockPos(BASE_X+x,y,BASE_Z-4),false);target.discard();}});}if(extra%40==25)Issue217Probe.capture(mc,"marker-stage"+stage);return;}
  if(scene>=19){done=true;mc.options.keyUse.setDown(false);Issue217Probe.server(p->{try{Path out=Path.of(System.getProperty("slavicmyths.issue217.output"));Files.createDirectories(out);Files.writeString(out.resolve("actual-world-shots.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(results));boolean bad=results.size()!=(Boolean.getBoolean("slavicmyths.issue224.markerOnly")?1:19)||results.stream().anyMatch(row->!(Boolean)row.get("hit"));System.out.println((bad?"ISSUE223_LIVE_FAIL":"ISSUE223_LIVE_PASS")+" native client releases="+results.size()+", actual world ticks/hits, ammo and durability checked");target.discard();if(bad)throw new AssertionError("Actual world guided shot failed: "+results);}catch(Exception ex){throw new RuntimeException(ex);}finally{mc.execute(mc::stop);}});return;}
  int draw=scene==0?30:new int[]{8,18,30}[(scene-1)/6];int angle=scene==0?0:new int[]{0,10,45,90,135,180}[(scene-1)%6];
  if(selected!=scene){selected=scene;Issue217Probe.server(p->{p.setHealth(20);p.getFoodData().setFoodLevel(20);p.teleportTo(p.serverLevel(),BASE_X,100,BASE_Z,180+angle,0);target.moveTo(BASE_X,100,BASE_Z-8);target.setHealth(1000);target.invulnerableTime=0;var q=FinistQuiver.equipped(p);q.set(ItemState.FINIST_QUIVER.get(),new ItemState.QuiverState(12,GreyPackRing.now(p),"live223"));});}
  if(phase==20){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
  if(phase==20+draw){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}
  if(phase==20||phase==20+draw+3||phase==70)Issue217Probe.capture(mc,"finist-case"+scene+"-tick"+phase);
  if(phase==90)Issue217Probe.server(p->{boolean hit=target.getHealth()<1000;var row=new LinkedHashMap<String,Object>();row.put("case",scene);row.put("drawTicks",draw);row.put("angle",angle);row.put("hit",hit);row.put("health",target.getHealth());row.put("bowWear",p.getMainHandItem().getDamageValue());row.put("charges",FinistQuiver.charges(FinistQuiver.equipped(p)));results.add(row);if(p.getMainHandItem().getDamageValue()!=scene+1||FinistQuiver.charges(FinistQuiver.equipped(p))!=11)throw new AssertionError("Actual draw/ammo/durability mismatch "+row);System.out.println("ISSUE223_LIVE_SHOT "+new com.google.gson.Gson().toJson(row));if(Boolean.getBoolean("slavicmyths.issue224.markerOnly"))mc.execute(()->tick=1900);});
 }
}
