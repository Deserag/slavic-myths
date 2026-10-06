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
  p.giveExperienceLevels(100);p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));t.assertTrue(!m.quickMoveStack(p,30).isEmpty()&&m.input.getItem(0).is(Items.IRON_SWORD),"Weapon shift-click failed");
  m.input.setItem(1,new ItemStack(Items.IRON_INGOT,3));m.clickMenuButton(p,0);t.assertTrue(Runes.slots(m.input.getItem(0))==1&&m.input.getItem(1).isEmpty()&&p.experienceLevel==94,"Forge consumption failed");
  m.input.setItem(1,new ItemStack(ModItems.RUNE_THUNDER.get()));m.clickMenuButton(p,1);t.assertTrue(Runes.has(m.input.getItem(0),"thunder")&&p.experienceLevel==87&&m.input.getItem(1).isEmpty(),"Install failed");
  var restored=ItemStack.parseOptional(t.getLevel().registryAccess(),(CompoundTag)m.input.getItem(0).saveOptional(t.getLevel().registryAccess()));t.assertTrue(Runes.has(restored,"thunder"),"Rune component save failed");
  m.clickMenuButton(p,2);t.assertTrue(!Runes.has(m.input.getItem(0),"thunder")&&p.experienceLevel==85,"Remove failed");
  m.input.setItem(1,new ItemStack(Items.DIRT,3));m.clickMenuButton(p,0);t.assertTrue(p.experienceLevel==85&&m.input.getItem(1).getCount()==3&&Runes.slots(m.input.getItem(0))==1,"Invalid materials consumed");
  t.assertTrue(!m.slots.get(0).mayPlace(new ItemStack(Items.DIRT)),"Unsupported first-slot item accepted");t.assertTrue(!m.quickMoveStack(p,0).isEmpty()&&count(p,Items.IRON_SWORD)==1,"Output shift-click failed");
  for(int i=3;i<m.slots.size();i++)if(m.slots.get(i).getItem().is(Items.IRON_SWORD)){m.quickMoveStack(p,i);break;}
  t.assertTrue(count(p,Items.IRON_SWORD)==0&&!m.input.getItem(0).isEmpty(),"Weapon return shift-click failed");m.input.setItem(0,restored);p.closeContainer();t.assertTrue(count(p,Items.IRON_SWORD)==1&&count(p,Items.DIRT)==3,"Close lost/duplicated input sword="+count(p,Items.IRON_SWORD)+" dirt="+count(p,Items.DIRT));
  System.out.println("RC2_ANVIL_SERVER_PASS RMB=true forgeInstallRemove=true XP=true materials=true validation=true outputShiftClick=true closeReturn=true componentNBT=true");t.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void kitchenServer(GameTestHelper t){
  var p=player(t,"RCKitchen");open(t,p,ModBlocks.KITCHEN_TABLE.get());t.assertTrue(p.containerMenu instanceof KitchenMenu,"Kitchen failed to open");var m=(KitchenMenu)p.containerMenu;
  for(int id=0;id<KitchenRecipes.COUNT;id++){
   for(int n=0;n<5;n++)m.input.setItem(n,ItemStack.EMPTY);
   var ingredients=KitchenRecipes.ingredients(id);for(int n=0;n<3;n++)m.input.setItem(n,new ItemStack(ingredients[n],KitchenRecipes.count(id,n)));
   t.assertTrue(m.unavailable(id)!=null&&!m.clickMenuButton(p,id),"Missing tool craft succeeded");
   var tool=new ItemStack(KitchenRecipes.tool(id));m.input.setItem(3,tool);m.input.setItem(4,new ItemStack(Items.DIRT));
   t.assertTrue(!m.clickMenuButton(p,id)&&m.input.getItem(0).getCount()==KitchenRecipes.count(id,0)&&tool.getDamageValue()==0,"Blocked output consumed ingredients");m.input.setItem(4,ItemStack.EMPTY);
   t.assertTrue(m.unavailable(id)==null&&m.clickMenuButton(p,id),"Valid recipe failed "+id);var result=m.input.getItem(4);
   t.assertTrue(result.is(KitchenRecipes.output(id))&&result.getCount()==KitchenRecipes.amount(id)&&tool.getDamageValue()==1,"Recipe result/tool wear wrong "+id);
   if(id==4)t.assertTrue(m.input.getItem(1).is(Items.BUCKET),"Water bucket remainder lost");
   var saved=ItemStack.parseOptional(t.getLevel().registryAccess(),(CompoundTag)result.saveOptional(t.getLevel().registryAccess()));t.assertTrue(ItemStack.isSameItemSameComponents(result,saved),"Food component persistence failed");
   t.assertTrue(!m.quickMoveStack(p,4).isEmpty()&&m.input.getItem(4).isEmpty(),"Result shift-click failed");
  }
  t.assertTrue(!m.clickMenuButton(p,-1)&&!m.clickMenuButton(p,KitchenRecipes.COUNT),"Invalid packet accepted");p.closeContainer();
  System.out.println("RC2_KITCHEN_SERVER_PASS recipes=12 RMB=true toolGuard=true outputGuard=true consumption=true wear=true remainder=true shiftClick=true itemNBT=true");t.succeed();
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
