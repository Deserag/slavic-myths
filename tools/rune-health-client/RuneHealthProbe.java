package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.rpg.Runes;
import org.slavicmyths.item.ArmorSetHealth;
/** Opt-in real client, disposable world; never in production JAR. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RuneHealthProbe {
 static boolean opened,started,ready,failed; static int ticks,stage=-1,shot=-1; static double base; static float before;
 static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
 static final String[] PART={"helmet","chestplate","leggings","boots"};
 static final String[] SCENES={"iron-day","poloz-day","perunite-day","dragon-day","linen-day","iron-night","poloz-night","vanilla-enchantment-reference","poloz-enchant-and-runes","poloz-inventory","poloz-back","poloz-side"};
 static final List<Entity> ACTORS=new ArrayList<>();
 static Path out(){return Path.of("../../docs/implementation/armor-health-48/game-frames");}
 static ItemStack piece(String prefix,int i,int count,boolean enchanted,ServerPlayer p){
  var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(prefix+PART[i]));var stack=new ItemStack(item);
  if(count>0)Runes.write(stack,3,List.of("protection","resilience","fortitude").subList(0,count));
  if(enchanted)stack.enchant(p.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION),1);
  return stack;
 }
 static void equip(ServerPlayer p,String prefix){for(int i=0;i<4;i++)p.setItemSlot(SLOTS[i],prefix==null?ItemStack.EMPTY:piece(prefix,i,0,false,p));}
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);System.out.println("HEALTH48_PASS "+text);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
  try {
   if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.createWorldOpenFlows().openWorld("RuneHealthQA",()->{});}
   if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||failed)return;
   if(!started){started=true;mc.getSingleplayerServer().execute(()->{try{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var w=p.serverLevel();for(var old:w.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(-64,60,-64,64,110,64),a->!(a instanceof net.minecraft.world.entity.player.Player)))old.discard();equip(p,null);p.removeAllEffects();ArmorSetHealth.refresh(p);base=p.getMaxHealth();if(Boolean.getBoolean("runeHealthInventoryOnly"))ticks=1139;p.setHealth(8);p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();w.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false,mc.getSingleplayerServer());w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,mc.getSingleplayerServer());w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,mc.getSingleplayerServer());w.setWeatherParameters(60000,0,false,false);w.setDayTime(6000);for(int x=-8;x<=8;x++)for(int z=-10;z<=5;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);p.teleportTo(w,0,82,-7,0,4);ready=true;}catch(Throwable ex){fail(ex);}});}
   if(!ready)return;ticks++;mc.options.hideGui=true;mc.player.setYRot(0);mc.player.setXRot(4);
   if(ticks%40==1&&ticks<400){int test=ticks/40;mc.getSingleplayerServer().execute(()->{try{health(mc,test);}catch(Throwable ex){fail(ex);}});}
   int next=(ticks-420)/80;
   if(ticks>=420&&next<SCENES.length&&stage!=next){stage=next;mc.getSingleplayerServer().execute(()->{try{scene(mc,next);}catch(Throwable ex){fail(ex);}});}
   if(ticks>=420&&stage==9&&mc.screen==null&&ticks>420+9*80+15){mc.options.hideGui=false;mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));}
   if(ticks>420+SCENES.length*80){System.out.println("RUNE_HEALTH48_RUNTIME_COMPLETE real_client_launch=1 scripted_fixture_not_owner_acceptance");mc.stop();}
  }catch(Throwable ex){fail(ex);}
 }
 static void fail(Throwable ex){failed=true;ex.printStackTrace();System.out.println("RUNE_HEALTH48_RUNTIME_FAIL "+ex);Minecraft.getInstance().execute(()->Minecraft.getInstance().stop());}
 static void health(Minecraft mc,int n){var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
  switch(n){
   case 0 -> {check(Math.abs(p.getMaxHealth()-base)<.001,"no-set baseline="+base);equip(p,"slavicmyths:perunite_");p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);}
   case 1 -> {check(Math.abs(p.getMaxHealth()-base)<.001,"partial no bonus");equip(p,"slavicmyths:perunite_");}
   case 2 -> {check(Math.abs(p.getMaxHealth()-base-5)<.001,"perunite +5");before=p.getHealth();equip(p,"slavicmyths:poloz_");}
   case 3 -> {check(Math.abs(p.getMaxHealth()-base-14)<.001,"poloz +14 replaces old2");check(p.getHealth()<=before,"equip does not heal");equip(p,"slavicmyths:dragon_");}
   case 4 -> {check(Math.abs(p.getMaxHealth()-base-20)<.001,"dragon +20");for(int i=0;i<20;i++)ArmorSetHealth.refresh(p);check(Math.abs(p.getMaxHealth()-base-20)<.001,"repeated refresh no stacking");p.setHealth(p.getMaxHealth());p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);}
   case 5 -> {check(Math.abs(p.getMaxHealth()-base)<.001&&p.getHealth()<=base,"remove piece removes bonus and clamps health");equip(p,"slavicmyths:poloz_");p.setItemSlot(EquipmentSlot.FEET,piece("slavicmyths:perunite_",3,0,false,p));}
   case 6 -> {check(Math.abs(p.getMaxHealth()-base)<.001,"mixed set no bonus");p.setHealth(8);equip(p,"slavicmyths:dragon_");}
   case 7 -> {check(Math.abs(p.getMaxHealth()-base-20)<.001&&p.getHealth()<=8,"re-equip no free healing");equip(p,null);}
   case 8 -> check(Math.abs(p.getMaxHealth()-base)<.001,"all removed baseline restored");
  }
 }
 static void scene(Minecraft mc,int n){var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var w=p.serverLevel();for(var a:ACTORS)a.discard();ACTORS.clear();
  mc.execute(()->mc.setScreen(null));w.setDayTime(n==5||n==6?18000:6000);String prefix=switch(n){case 1,6,8,9,10,11->"slavicmyths:poloz_";case 2->"slavicmyths:perunite_";case 3->"slavicmyths:dragon_";default->"minecraft:iron_";};
  for(int count=0;count<4;count++){var a=EntityType.ARMOR_STAND.create(w);a.setNoGravity(true);float yaw=n==10?0:n==11?90:180;a.moveTo(3-count*2,81,0,yaw,0);a.yBodyRot=yaw;a.yHeadRot=yaw;
   if(n==4){var s=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("slavicmyths:linen_shirt")));if(count>0)Runes.write(s,3,List.of("protection","resilience","fortitude").subList(0,count));a.setItemSlot(EquipmentSlot.CHEST,s);}
   else for(int i=0;i<4;i++)a.setItemSlot(SLOTS[i],piece(prefix,i,n==7?0:count,n==7||n==8,p));
   w.addFreshEntity(a);ACTORS.add(a);
  }
  if(n==9){p.setGameMode(GameType.SURVIVAL);equip(p,"slavicmyths:poloz_");for(int i=0;i<4;i++)p.setItemSlot(SLOTS[i],piece(prefix,i,3,false,p));}
  p.teleportTo(w,0,82,-7,0,4);System.out.println("RUNE48_SCENE "+SCENES[n]+" left_to_right_runes=0,1,2,3");
 }
 @SubscribeEvent public static void render(RenderFrameEvent.Post e){if(stage<0||ticks<420+stage*80+40||failed)return;int phase=ticks>=420+stage*80+60?1:0;if(shot==stage*2+phase)return;var mc=Minecraft.getInstance();try{Files.createDirectories(out());try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(out().resolve(SCENES[stage]+(phase==1?"-late":"")+".png"));}shot=stage*2+phase;System.out.println("RUNE48_FRAME "+SCENES[stage]);}catch(Exception ex){fail(ex);}}
}
