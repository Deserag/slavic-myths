package org.slavicmyths.verify;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WeaponHudVisual {
 static boolean opened,done,audited;static volatile String failure;static int ticks,index;
 static final String[] ITEMS={"abyss_staff","morozko_staff","karachun_scythe","morovoy_staff","storm_anchor","pharaohka_trident","storm_harpoon","neveya_scythe","lomeya_hammer","lomeya_hammer_offhand","nine_mor_spear","nine_mor_axe","three_breath_crossbow","serpent_crossbow"};
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(done)return;var mc=Minecraft.getInstance();try{
 if(failure!=null)throw new IllegalStateException(failure);
 mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0D);
 if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
 if(!opened&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){opened=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(3);mc.options.simulationDistance().set(3);mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;mc.options.framerateLimit().set(60);
 mc.createWorldOpenFlows().createFreshLevel("WeaponHud-Isolated-"+System.currentTimeMillis(),new LevelSettings("Weapon HUD test",GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(22112026L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;}
 if(mc.player==null||mc.getSingleplayerServer()==null||mc.screen!=null)return;
 if(!audited){audited=true;mc.getSingleplayerServer().execute(()->{try{verifySisters(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst());}catch(Throwable t){failure=t.toString();t.printStackTrace();}});var rows=new java.util.ArrayList<java.util.Map<String,Object>>();
 for(String id:new String[]{"abyss_staff","ancient_carolingian_sword","ancient_chekan","ancient_spear","ashen_greatsword","ataman_flail","battle_axe","berdysh","bone_arrow","bone_knife","butchering_knife","carved_staff","chekan","chud_multitool_axe","club","diamond_flail","diamond_spear","diamond_sulitsa","diamond_throwing_knife","empowered_infernal_bow","finist_bow","flail","gift_ilya_shield","gold_flail","gold_spear","gold_sulitsa","gold_throwing_knife","heavy_bow","infernal_bow","iron_spear","iron_sulitsa","iron_throwing_knife","karachun_scythe","likho_staff","lomeya_hammer","lomeya_hammer_offhand","mace","morovoy_staff","morozko_staff","mytar_shackled_sickle","mytar_sickle","navya_ostroga","netherite_flail","netherite_spear","netherite_sulitsa","netherite_throwing_knife","neveya_scythe","nightingale_dagger","nine_mor_axe","nine_mor_shield","nine_mor_spear","noon_sickle","perunite_axe","perunite_flail","perunite_mace","perunite_shield","perunite_spear","perunite_sulitsa","perunite_sword","perunite_throwing_knife","pharaohka_trident","poloz_whip","pool_spear","restored_carolingian_sword","restored_spear","retainer_shield","ritual_knife","serpent_crossbow","shadow_throwing_knife","short_bow","silver_dagger","silver_flail","silver_mace","silver_spear","silver_sulitsa","silver_sword","silver_throwing_knife","spear","stone_flail","stone_mace","stone_sulitsa","stone_throwing_knife","storm_anchor","storm_harpoon","storm_staff","three_breath_crossbow","thunder_axe","thunder_spear","trophy_kalin_sabre","tugarin_sword","veles_staff","victor_spear","volcanic_heart_flail","ward_shield","wooden_mace","zhara_axe","zhara_flail","zhara_spear","zhara_sulitsa","zhara_sword","zhara_throwing_knife","zhelva_shield"}){
 var key=ResourceLocation.fromNamespaceAndPath("slavicmyths",id);
 if(!BuiltInRegistries.ITEM.containsKey(key))throw new IllegalStateException("Missing weapon "+id);
 var item=BuiltInRegistries.ITEM.get(key);var stack=new ItemStack(item);var row=new java.util.LinkedHashMap<String,Object>();
 row.put("id",id);row.put("class",item.getClass().getName());row.put("name",stack.getHoverName().getString());
 row.put("tooltip",stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level),mc.player,net.minecraft.world.item.TooltipFlag.NORMAL).stream().map(net.minecraft.network.chat.Component::getString).toList());
 row.put("useOwner",item.getClass().getMethod("use",net.minecraft.world.level.Level.class,net.minecraft.world.entity.player.Player.class,net.minecraft.world.InteractionHand.class).getDeclaringClass().getName());rows.add(row);
 }Path out=mc.gameDirectory.toPath().resolve("evidence");Files.createDirectories(out);Files.writeString(out.resolve("all-weapons-runtime-audit.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));System.out.println("WEAPON_AUDIT_102_REGISTERED_TOOLTIPS_PASS");}
 ticks++;
 if(ticks%40==1){int step=index;mc.options.guiScale().set(1+step/ITEMS.length);mc.resizeDisplay();String id=ITEMS[step%ITEMS.length];mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id))));});}
 if(ticks%40==20&&ITEMS[index%ITEMS.length].equals("abyss_staff"))mc.getSingleplayerServer().execute(()->org.slavicmyths.marine.MarineWeaponAbilities.bubble(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst()));
 if(ticks%40==35){Path out=mc.gameDirectory.toPath().resolve("evidence");Files.createDirectories(out);String name="scale-"+(1+index/ITEMS.length)+"-"+ITEMS[index%ITEMS.length];try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name+".png"));}System.out.println("WEAPON_HUD_FRAME "+name);if(++index==ITEMS.length*3){Files.writeString(out.resolve("result.txt"),"Captured fourteen weapons at GUI scales 1, 2, 3; visual inspection required.");done=true;mc.stop();}}
 }catch(Throwable failure){failure.printStackTrace();done=true;mc.stop();}}

 static void verifySisters(net.minecraft.server.level.ServerPlayer p)throws Exception{
 var world=p.serverLevel();var cow=net.minecraft.world.entity.EntityType.COW.create(world);
 cow.setNoAi(true);cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500);cow.setHealth(500);cow.moveTo(p.getX(),p.getY(),p.getZ()+2);world.addFreshEntity(cow);p.setYRot(0);p.setXRot(0);
 try{
 var scythe=org.slavicmyths.sisters.SistersContent.NEVEYA_SCYTHE.get();
 for(boolean alternate:new boolean[]{false,true}){
  p.getCooldowns().removeCooldown(scythe);cow.setHealth(500);cow.invulnerableTime=0;
  var stack=new ItemStack(scythe);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);p.setShiftKeyDown(alternate);
  scythe.use(world,p,net.minecraft.world.InteractionHand.MAIN_HAND);p.setShiftKeyDown(!alternate);
  scythe.releaseUsing(stack,world,p,scythe.getUseDuration(stack,p)-10);
  float dealt=500-cow.getHealth();if(Math.abs(dealt-(alternate?36:26))>.01)throw new IllegalStateException("Scythe variant changed after Shift release: "+alternate+" damage="+dealt);
  scythe.releaseUsing(stack,world,p,scythe.getUseDuration(stack,p)-10);if(cow.getHealth()!=500-dealt)throw new IllegalStateException("Scythe duplicate cast during cooldown");p.stopUsingItem();
 }
 var hammer=org.slavicmyths.sisters.SistersContent.LOMEYA_HAMMER.get();p.getCooldowns().removeCooldown(hammer);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(hammer));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);cow.setHealth(500);cow.invulnerableTime=0;
 hammer.use(world,p,net.minecraft.world.InteractionHand.MAIN_HAND);if(cow.getHealth()!=500||p.getCooldowns().isOnCooldown(hammer))throw new IllegalStateException("Single hammer fired dual skill");
 p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(org.slavicmyths.sisters.SistersContent.LOMEYA_HAMMER_OFFHAND.get()));hammer.use(world,p,net.minecraft.world.InteractionHand.MAIN_HAND);if(Math.abs(cow.getHealth()-464)>.01)throw new IllegalStateException("Dual alternate damage: "+cow.getHealth());
 float health=cow.getHealth();hammer.use(world,p,net.minecraft.world.InteractionHand.MAIN_HAND);if(cow.getHealth()!=health)throw new IllegalStateException("Hammer duplicate cast");
 java.nio.file.Path out=Minecraft.getInstance().gameDirectory.toPath().resolve("evidence");Files.createDirectories(out);Files.writeString(out.resolve("sisters-behavior.txt"),"PASS: scythe captures Shift at charge start; primary26/alternate36; no second cast during cooldown; single hammer rejects alternate; paired alternate36; no duplicate hammer cast.");System.out.println("SISTERS_BEHAVIOR_PASS");
 }finally{cow.discard();p.setShiftKeyDown(false);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY);}
 }
}
