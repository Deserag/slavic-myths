package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.registry.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.kitchen.*;
import org.slavicmyths.navigation.*;

/** Real server interactions and inventories, isolated fixtures; no client/window bootstrap. */
@GameTestHolder("slavicmyths_hotfix") @PrefixGameTestTemplate(false)
public final class PlaytestWorkstationGameTests {
 private static ServerPlayer player(GameTestHelper t,String name){
  var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name),net.minecraft.server.level.ClientInformation.createDefault());
  p.connection=FakePlayerFactory.get(w,p.getGameProfile()).connection;p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.moveTo(Vec3.atBottomCenterOf(t.absolutePos(new BlockPos(2,2,2))));return p;
 }
 private static void open(GameTestHelper t,ServerPlayer p,net.minecraft.world.level.block.Block block){
  var pos=t.absolutePos(new BlockPos(2,1,2));t.getLevel().setBlock(pos,block.defaultBlockState(),3);
  var state=t.getLevel().getBlockState(pos);var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
  state.useWithoutItem(t.getLevel(),p,hit);
 }
 private static int count(ServerPlayer p,Item item){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void pathStoneServer(GameTestHelper t){
  var p=player(t,"RCPath");open(t,p,ModBlocks.PATH_STONE.get());t.assertTrue(p.containerMenu instanceof RpgMenu&&!((RpgMenu)p.containerMenu).anvil,"Path stone failed to open server menu");
  var menu=(RpgMenu)p.containerMenu;p.giveExperienceLevels(20);p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));p.getInventory().setItem(1,new ItemStack(Items.SHIELD));
  t.assertTrue(menu.clickMenuButton(p,0),"Path selection packet rejected");t.assertTrue(PathData.data(p).getString("Main").equals("druzhinnik")&&p.experienceLevel==10,"Path or XP wrong");t.assertTrue(count(p,Items.IRON_SWORD)==0&&count(p,Items.SHIELD)==0,"Offering not consumed");
  menu.clickMenuButton(p,10);t.assertTrue(PathData.has(p,"drill")&&p.experienceLevel==7,"Skill purchase failed");
  var records=NavigationRecords.get(t.getLevel());t.assertTrue(records.player(p.getUUID()).markers.size()==1,"Waystone marker missing");
  var restored=NavigationRecords.load(records.save(new CompoundTag(),t.getLevel().registryAccess()),t.getLevel().registryAccess());t.assertTrue(restored.player(p.getUUID()).markers.size()==1,"Marker NBT lost");
  var saved=new CompoundTag();p.saveWithoutId(saved);var copy=player(t,"RCPathCopy");copy.load(saved);t.assertTrue(PathData.has(copy,"drill")&&PathData.data(copy).getString("Main").equals("druzhinnik"),"Path player NBT lost");
  int xp=p.experienceLevel;p.moveTo(p.getX()+30,p.getY(),p.getZ());t.assertTrue(!menu.clickMenuButton(p,11)&&p.experienceLevel==xp,"Remote menu mutation");p.closeContainer();
  System.out.println("RC2_PATH_SERVER_PASS RMB=true menu=true offerings=true XP=true skill=true navigation=true NBT=true rangeGuard=true realReconnectNotTested=true");t.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void runicAnvilServer(GameTestHelper t){
  var p=player(t,"RCAnvil");open(t,p,ModBlocks.RUNIC_ANVIL.get());t.assertTrue(p.containerMenu instanceof RpgMenu&&((RpgMenu)p.containerMenu).anvil,"Anvil failed to open server menu");var m=(RpgMenu)p.containerMenu;
  p.giveExperienceLevels(100);p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));t.assertTrue(!m.quickMoveStack(p,29).isEmpty()&&m.input.getItem(0).is(Items.IRON_SWORD),"Weapon shift-click failed");
  m.input.setItem(1,new ItemStack(Items.IRON_INGOT,3));m.clickMenuButton(p,0);t.assertTrue(Runes.slots(m.input.getItem(0))==1&&m.input.getItem(1).isEmpty()&&p.experienceLevel==94,"Forge consumption failed");
  m.input.setItem(1,new ItemStack(ModItems.RUNE_THUNDER.get()));m.clickMenuButton(p,1);t.assertTrue(Runes.has(m.input.getItem(0),"thunder")&&p.experienceLevel==87&&m.input.getItem(1).isEmpty(),"Install failed");
  var restored=ItemStack.parseOptional(t.getLevel().registryAccess(),(CompoundTag)m.input.getItem(0).saveOptional(t.getLevel().registryAccess()));t.assertTrue(Runes.has(restored,"thunder"),"Rune component save failed");
  m.clickMenuButton(p,2);t.assertTrue(!Runes.has(m.input.getItem(0),"thunder")&&p.experienceLevel==85,"Remove failed");
  m.input.setItem(1,new ItemStack(Items.DIRT,3));m.clickMenuButton(p,0);t.assertTrue(p.experienceLevel==85&&m.input.getItem(1).getCount()==3&&Runes.slots(m.input.getItem(0))==1,"Invalid materials consumed");
  t.assertTrue(!m.slots.get(0).mayPlace(new ItemStack(Items.DIRT)),"Unsupported first-slot item accepted");t.assertTrue(!m.quickMoveStack(p,0).isEmpty()&&count(p,Items.IRON_SWORD)==1,"Output shift-click failed");
  for(int i=2;i<m.slots.size();i++)if(m.slots.get(i).getItem().is(Items.IRON_SWORD)){m.quickMoveStack(p,i);break;}
  t.assertTrue(count(p,Items.IRON_SWORD)==0&&!m.input.getItem(0).isEmpty(),"Weapon return shift-click failed");m.input.setItem(0,restored);p.closeContainer();t.assertTrue(count(p,Items.IRON_SWORD)==1&&count(p,Items.DIRT)==3,"Close lost/duplicated input sword="+count(p,Items.IRON_SWORD)+" dirt="+count(p,Items.DIRT));
  System.out.println("RC2_ANVIL_SERVER_PASS RMB=true forgeInstallRemove=true XP=true materials=true validation=true outputShiftClick=true closeReturn=true componentNBT=true");t.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void creativeAndInventoryCosts(GameTestHelper t){
  var p=player(t,"VisualCreative");p.setGameMode(GameType.CREATIVE);open(t,p,ModBlocks.PATH_STONE.get());var path=(RpgMenu)p.containerMenu;
  p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));p.getInventory().setItem(1,new ItemStack(Items.SHIELD));path.clickMenuButton(p,0);path.clickMenuButton(p,10);
  t.assertTrue(p.experienceLevel==0&&PathData.has(p,"drill"),"Creative path/skill requires XP");p.closeContainer();open(t,p,ModBlocks.RUNIC_ANVIL.get());var m=(RpgMenu)p.containerMenu;
  t.assertTrue(m.input.getContainerSize()==2&&m.slots.size()==38,"Catalyst primary slot remains");m.input.setItem(0,new ItemStack(Items.IRON_SWORD));m.input.setItem(1,new ItemStack(Items.IRON_INGOT,3));
  var preview=Runes.preview(m.input,0);t.assertTrue(Runes.slots(preview)==1&&Runes.slots(m.input.getItem(0))==0,"Preview mutated original");m.clickMenuButton(p,0);
  m.input.setItem(1,new ItemStack(ModItems.SILVER_INGOT.get(),2));t.assertTrue(Runes.blocker(p,m.input,0)!=null,"Missing inventory extra accepted");m.clickMenuButton(p,0);t.assertTrue(Runes.slots(m.input.getItem(0))==1&&m.input.getItem(1).getCount()==2,"Missing extra consumed inputs");
  p.getInventory().setItem(5,new ItemStack(ModItems.ANCIENT_SIGN.get()));m.clickMenuButton(p,0);t.assertTrue(Runes.slots(m.input.getItem(0))==2&&count(p,ModItems.ANCIENT_SIGN.get())==0,"Extra not consumed from inventory");
  m.clickMenuButton(p,101);t.assertTrue(m.slots.get(1).mayPlace(new ItemStack(ModItems.RUNE_THUNDER.get()))&&!m.slots.get(1).mayPlace(new ItemStack(Items.DIRT)),"Rune mode validation failed");
  m.input.setItem(1,new ItemStack(ModItems.RUNE_THUNDER.get(),2));preview=Runes.preview(m.input,1);t.assertTrue(!Runes.has(m.input.getItem(0),"thunder")&&Runes.has(preview,"thunder"),"Installation preview mutated input");m.clickMenuButton(p,1);
  t.assertTrue(p.experienceLevel==0&&Runes.has(m.input.getItem(0),"thunder")&&m.input.getItem(1).getCount()==1,"Creative install XP or consumption wrong");m.clickMenuButton(p,1);t.assertTrue(m.input.getItem(1).getCount()==1,"Duplicate consumed rune");
  m.clickMenuButton(p,102);t.assertTrue(!m.slots.get(1).isActive(),"Remove mode still exposes second input");m.clickMenuButton(p,2);t.assertTrue(p.experienceLevel==0&&!Runes.has(m.input.getItem(0),"thunder"),"Creative removal failed");
  m.input.setItem(1,new ItemStack(Items.IRON_INGOT,2));p.closeContainer();t.assertTrue(count(p,Items.IRON_SWORD)==1&&count(p,Items.IRON_INGOT)==2,"Close lost or duplicated items");
  System.out.println("RC2_VISUAL_CREATIVE_PASS XP0=true extraInventory=true inputs2=true previewCopy=true duplicateGuard=true modes=true close=true");t.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void kitchenServer(GameTestHelper t){
  var level=t.getLevel();var p=player(t,"RCKitchen");
  open(t,p,ModBlocks.KITCHEN_TABLE.get());
  t.assertTrue(p.containerMenu instanceof KitchenMenu,"Kitchen failed to open server menu");
  var menu=(KitchenMenu)p.containerMenu;BlockPos pos=t.absolutePos(new BlockPos(2,1,2));
  t.assertTrue(level.getBlockEntity(pos) instanceof KitchenTile,"Real kitchen BlockEntity missing");
  var tile=(KitchenTile)level.getBlockEntity(pos);Container input=menu.inventory;
  t.assertTrue(input==tile.inventory&&input.getContainerSize()==9
    &&menu.slots.size()==45,"Kitchen inventory/menu is not the current nine-slot implementation");
  var doughHolder=level.getRecipeManager().byKey(
    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","kitchen/dough"));
  t.assertTrue(doughHolder.isPresent()&&doughHolder.get().value() instanceof KitchenRecipe,
    "Actual dough recipe failed to load");
  KitchenRecipe dough=(KitchenRecipe)doughHolder.get().value();
  t.assertTrue(dough.mode().equals("ROLLING")&&dough.time()>0
    &&dough.result().is(KitchenII.item("dough")),"Loaded rolling recipe differs from fixture");

  // Rolling slot 0, cooking pot slot 1, ingredients slots 2..5,
  // finished dishes slot 6, by-products slots 7..8.
  input.setItem(2,new ItemStack(KitchenII.item("wheat_flour"),2));
  input.setItem(3,new ItemStack(Items.WATER_BUCKET));
  KitchenTile.tick(level,pos,level.getBlockState(pos),tile);
  t.assertTrue(!tile.busy()&&input.getItem(2).getCount()==2
    &&input.getItem(3).is(Items.WATER_BUCKET),"Missing rolling pin consumed ingredients");
  input.setItem(0,new ItemStack(KitchenII.item("rolling_pin")));
  input.setItem(6,new ItemStack(Items.DIRT));
  KitchenTile.tick(level,pos,level.getBlockState(pos),tile);
  t.assertTrue(!tile.busy()&&input.getItem(2).getCount()==2
    &&input.getItem(6).is(Items.DIRT),"Blocked output consumed ingredients");
  input.setItem(6,ItemStack.EMPTY);
  KitchenTile.tick(level,pos,level.getBlockState(pos),tile);
  t.assertTrue(tile.busy()&&tile.total==dough.time()
    &&input.getItem(2).isEmpty()&&input.getItem(3).isEmpty()
    &&input.getItem(7).is(Items.BUCKET)&&tile.toolLocked(0),
    "Rolling recipe did not consume exact ingredients, preserve remainder or lock tool");
  for(int tick=0;tick<dough.time();tick++)KitchenTile.tick(level,pos,level.getBlockState(pos),tile);
  ItemStack result=input.getItem(6);
  t.assertTrue(!tile.busy()&&result.is(KitchenII.item("dough"))
    &&result.getCount()==dough.result().getCount()
    &&input.getItem(0).is(KitchenII.item("rolling_pin")),
    "Completed dough result or persistent rolling pin wrong");
  var saved=ItemStack.parseOptional(level.registryAccess(),
    (CompoundTag)result.saveOptional(level.registryAccess()));
  t.assertTrue(ItemStack.isSameItemSameComponents(result,saved),"Food item components lost in NBT");
  t.assertTrue(!menu.quickMoveStack(p,6).isEmpty()&&input.getItem(6).isEmpty()
    &&count(p,KitchenII.item("dough"))==2,"Dough output shift-click failed");

  // Current POT recipe makes three servings, not an instant clickable craft.
  var porridgeHolder=level.getRecipeManager().byKey(
    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","kitchen/barley_porridge"));
  t.assertTrue(porridgeHolder.isPresent()&&porridgeHolder.get().value() instanceof KitchenRecipe,
    "Actual barley-porridge recipe failed to load");
  KitchenRecipe porridge=(KitchenRecipe)porridgeHolder.get().value();
  t.assertTrue(porridge.pot()&&porridge.servings()==3,"Pot recipe fixture is inconsistent");
  input.setItem(0,ItemStack.EMPTY);
  input.setItem(7,ItemStack.EMPTY);
  input.setItem(1,new ItemStack(KitchenII.item("metal_pot")));
  input.setItem(2,new ItemStack(KitchenII.item("barley_groats"),2));
  input.setItem(3,new ItemStack(Items.WATER_BUCKET));
  KitchenTile.tick(level,pos,level.getBlockState(pos),tile);
  t.assertTrue(tile.busy()&&tile.activePot&&tile.total==porridge.time()
    &&input.getItem(2).isEmpty()&&input.getItem(3).isEmpty()
    &&input.getItem(7).is(Items.BUCKET),"Pot cooking or bucket remainder failed");
  for(int tick=0;tick<porridge.time();tick++)KitchenTile.tick(level,pos,level.getBlockState(pos),tile);
  t.assertTrue(!tile.busy()&&tile.servings==3&&tile.maxServings==3
    &&tile.dish.is(KitchenII.item("barley_porridge"))&&tile.toolLocked(1),
    "Pot did not finish exactly three stored servings");
  t.assertTrue(!menu.clickMenuButton(p,-1)&&!menu.clickMenuButton(p,1)
    &&!menu.clickMenuButton(p,0),"Invalid serving request or serving without bowl was accepted");
  p.getInventory().setItem(5,new ItemStack(Items.BOWL,3));
  for(int n=1;n<=3;n++){
   t.assertTrue(menu.clickMenuButton(p,0)&&tile.servings==3-n
      &&count(p,KitchenII.item("barley_porridge"))==n,
      "Serving transaction failed at portion "+n);
  }
  t.assertTrue(input.getItem(1).is(KitchenII.item("metal_pot"))
    &&tile.dish.isEmpty()&&!tile.toolLocked(1)
    &&count(p,Items.BOWL)==0,"Cookware/portion conservation failed");
  p.closeContainer();
  System.out.println("RC2_KITCHEN_SERVER_PASS recipeRegistry=true slots9=true rolling=true outputGuard=true toolGuard=true remainders=true tickCook=true itemNBT=true shiftClick=true potServings3=true");t.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void shieldServer(GameTestHelper t){
  var p=player(t,"RCShield");for(int i=0;i<61;i++)p.tick();var shield=new ItemStack(ModItems.RETAINER_SHIELD.get());p.setItemSlot(EquipmentSlot.OFFHAND,shield);
  t.assertTrue(shield.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK)&&shield.getItem().isValidRepairItem(shield,new ItemStack(Items.OAK_PLANKS)),"Shield ability/repair missing");
  shield.getItem().use(t.getLevel(),p,InteractionHand.OFF_HAND);for(int i=0;i<6;i++)p.doTick();t.assertTrue(p.isBlocking(),"RMB failed to reach blocking state");
  var attacker=EntityType.ZOMBIE.create(t.getLevel());attacker.moveTo(p.getX(),p.getY(),p.getZ()+3);float health=p.getHealth();p.invulnerableTime=0;p.hurt(t.getLevel().damageSources().mobAttack(attacker),6);
  t.assertTrue(p.getHealth()==health&&shield.getDamageValue()==7,"Front block or durability failed");
  attacker.moveTo(p.getX(),p.getY(),p.getZ()-3);p.invulnerableTime=0;p.hurt(t.getLevel().damageSources().mobAttack(attacker),4);t.assertTrue(p.getHealth()<health,"Rear damage was blocked / fixture invulnerable");
  attacker.moveTo(p.getX(),p.getY(),p.getZ()+3);attacker.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_AXE));p.invulnerableTime=0;p.hurt(t.getLevel().damageSources().mobAttack(attacker),6);
  t.assertTrue(p.getCooldowns().isOnCooldown(ModItems.RETAINER_SHIELD.get())&&!p.isUsingItem(),"Axe disable cooldown failed");
  var registry=t.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);for(var key:List.of(Enchantments.UNBREAKING,Enchantments.MENDING))t.assertTrue(registry.getHolderOrThrow(key).value().isSupportedItem(shield),"Durability enchant unsupported "+key);
  shield.enchant(registry.getHolderOrThrow(Enchantments.UNBREAKING),1);var saved=ItemStack.parseOptional(t.getLevel().registryAccess(),(CompoundTag)shield.saveOptional(t.getLevel().registryAccess()));t.assertTrue(saved.getDamageValue()==shield.getDamageValue()&&EnchantmentHelper.getItemEnchantmentLevel(registry.getHolderOrThrow(Enchantments.UNBREAKING),saved)==1,"Shield durability/enchant NBT failed");
  System.out.println("RC2_SHIELD_SERVER_PASS RMB=true frontBlock=true rearDamage=true durability=true axeCooldown=true repair=true unbreakingMending=true itemNBT=true visualsManual=true");t.succeed();
 }
}
