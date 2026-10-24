package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
@GameTestHolder("slavicmyths_staff_audit") @PrefixGameTestTemplate(false)
public final class StaffAuditTests {
 @GameTest(template="empty") public static void sabreConeRegression(GameTestHelper t){Issue225GameTests.sabreHitsSlimeAndCone(t);}
 @GameTest(template="empty") public static void sabreWallRegression(GameTestHelper t){Issue225GameTests.sabreWallAndSixTargetLimit(t);}
 @GameTest(template="empty") public static void weaponReachRegression(GameTestHelper t){Issue217GameTests.actualReachAndWhipHit(t);}
 @GameTest(template="empty") public static void throwingRunesRegression(GameTestHelper t){WeaponGameTests.nativeThrowsAndComponents(t);}
 @GameTest(template="empty") public static void primaryWeaponRegression(GameTestHelper t){Issue225GameTests.actualPrimaryAttacksSabreFlailsAndWhip(t);}
 @GameTest(template="empty") public static void upgradesKeepComponents(GameTestHelper t){
  var p=Issue217GameTests.player(t,"UpgradeAudit");var pos=t.absolutePos(new net.minecraft.core.BlockPos(3,2,3));t.getLevel().setBlockAndUpdate(pos,org.slavicmyths.registry.ModBlocks.ARMORER_TABLE.get().defaultBlockState());int checked=0;
  for(var h:t.getLevel().getRecipeManager().getAllRecipesFor(org.slavicmyths.armorer.ArmorerRecipe.TYPE.get())){
   var r=h.value();if(!r.copyComponents)continue;var menu=new org.slavicmyths.armorer.ArmorerMenu(100+checked,p.getInventory(),pos);
   for(int i=0;i<r.getIngredients().size();i++)if(!r.getIngredients().get(i).isEmpty()){
    var s=r.getIngredients().get(i).getItems()[0].copy();if(i==0){s.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("UpgradeAudit"));org.slavicmyths.item.ItemState.runes(s,1,java.util.List.of("heat"));s.enchant(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS),2);}
    menu.input.setItem(r.shapeless?i:(i/r.width)*4+i%r.width,s);
   }
   menu.slotsChanged(menu.input);var output=menu.quickMoveStack(p,0);t.assertTrue(!output.isEmpty(),"upgrade actual menu failed "+h.id());
   t.assertTrue(output.getHoverName().getString().equals("UpgradeAudit")&&org.slavicmyths.item.ItemState.runes(output).runes().equals(java.util.List.of("heat"))&&output.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS).getLevel(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS))==2,"upgrade lost name/rune/enchantment "+h.id());p.getInventory().clearContent();checked++;
  }t.assertTrue(checked>0,"no upgrade recipes checked");System.out.println("STAFF_UPGRADES_MENU_COMPONENTS_PASS count="+checked);t.succeed();
 }
 @GameTest(template="empty") public static void berryBoneMealAndRegrowth(GameTestHelper t){
  var p=Issue217GameTests.player(t,"BerryAudit");int checked=0;
  for(String name:org.slavicmyths.garden.Gardens.BERRIES){
   var pos=t.absolutePos(new net.minecraft.core.BlockPos(3+checked*3,2,3));t.getLevel().setBlockAndUpdate(pos.below(),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());t.getLevel().setBlockAndUpdate(pos.above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
   var bush=(org.slavicmyths.garden.PerennialBush)org.slavicmyths.garden.Gardens.block(name+"_bush");t.getLevel().setBlockAndUpdate(pos,bush.defaultBlockState());var meal=new ItemStack(Items.BONE_MEAL,4);
   for(int i=0;i<4;i++)t.assertTrue(net.minecraft.world.item.BoneMealItem.growCrop(meal,t.getLevel(),pos),"actual bone meal refused "+name);
   t.assertTrue(meal.isEmpty()&&t.getLevel().getBlockState(pos).getValue(org.slavicmyths.garden.PerennialBush.PHASE)==4,"bone meal cost/stage "+name);
   bush.useWithoutItem(t.getLevel().getBlockState(pos),t.getLevel(),pos,p,new net.minecraft.world.phys.BlockHitResult(pos.getCenter(),net.minecraft.core.Direction.UP,pos,false));
   t.assertTrue(t.getLevel().getBlockState(pos).getValue(org.slavicmyths.garden.PerennialBush.PHASE)==1,"actual harvest did not reset "+name);checked++;
  }System.out.println("STAFF_BERRY_ACTUAL_BONEMEAL_HARVEST_PASS kinds="+checked);t.succeed();
 }
 static float hit(GameTestHelper t,String id,boolean sharp,boolean strength){
  var p=Issue217GameTests.player(t,"StaffAudit");var stack=Issue225GameTests.item(id);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
  if(sharp)stack.enchant(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS),5);
  if(strength)p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,0));
  for(int i=0;i<60;i++)p.doTick();p.setOnGround(true);p.setSprinting(false);p.fallDistance=0;
  var target=Issue225GameTests.slime(t,p,0,2);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);
  p.attack(target);float damage=100-target.getHealth();target.discard();return damage;
 }
 @GameTest(template="empty") public static void meleeAndStrength(GameTestHelper t){
  String[] ids={"storm_staff","veles_staff","morozko_staff"};float[] expected={6,4,8};
  for(int i=0;i<ids.length;i++){float base=hit(t,ids[i],false,false),sharp=hit(t,ids[i],true,false),strength=hit(t,ids[i],false,true);
   t.assertTrue(Math.abs(base-expected[i])<.05,"melee "+ids[i]+" actual="+base);
   t.assertTrue(sharp>base&&strength>base,"damage enchant/Strength not effective: "+ids[i]);
   System.out.println("STAFF_ACTUAL_DAMAGE "+ids[i]+" base="+base+" sharpnessV="+sharp+" strengthI="+strength);
  }t.succeed();
 }
 @GameTest(template="empty") public static void enchantingAdmission(GameTestHelper t){
  var lookup=t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
  for(String id:new String[]{"storm_staff","veles_staff","morozko_staff","likho_staff"}){
   var stack=Issue225GameTests.item(id);var sharp=lookup.getOrThrow(Enchantments.SHARPNESS);
   t.assertTrue(stack.supportsEnchantment(sharp)&&stack.isPrimaryItemFor(sharp),"Sharpness table/book admission "+id);
   t.assertTrue(!stack.supportsEnchantment(lookup.getOrThrow(Enchantments.POWER)),"Bow Power wrongly admitted "+id);
   t.assertTrue(stack.supportsEnchantment(lookup.getOrThrow(Enchantments.MENDING))!=id.equals("morozko_staff"),"Mending admission "+id);
   System.out.println("STAFF_AVAILABLE_ENCHANTMENTS "+id+" "+lookup.listElements().filter(h->stack.supportsEnchantment(h)).map(h->h.unwrapKey().orElseThrow().location().toString()).toList());
  }System.out.println("STAFF_ENCHANT_ADMISSION_PASS 4 staffs; actual loaded enchantment definitions/tags");t.succeed();
 }
 @GameTest(template="empty") public static void stormPulse(GameTestHelper t){
  var p=Issue217GameTests.player(t,"StormAudit");var stack=Issue225GameTests.item("storm_staff");p.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var target=Issue225GameTests.slime(t,p,0,3);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);
  stack.getItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);float damage=100-target.getHealth();
  t.assertTrue(damage>0,"storm pulse missed actual hostile target");t.assertTrue(p.getCooldowns().isOnCooldown(stack.getItem()),"storm cooldown absent");
  System.out.println("STAFF_STORM_PULSE actual="+damage+" durability="+stack.getDamageValue());target.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=80) public static void frostStreamAndRelease(GameTestHelper t){
  var p=Issue217GameTests.player(t,"FrostAudit");var stack=Issue225GameTests.item("morozko_staff");p.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var target=Issue225GameTests.slime(t,p,0,3);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);
  stack.getItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);
  t.runAfterDelay(4,()->{
   stack.getItem().onUseTick(t.getLevel(),p,stack,56);float damage=100-target.getHealth();t.assertTrue(Math.abs(damage-3)<.05,"actual frost stream pulse="+damage);
   stack.getItem().releaseUsing(stack,t.getLevel(),p,56);
   t.assertTrue(!p.getPersistentData().getBoolean("FrostStreaming"),"stream remains after release");
   t.assertTrue(!org.slavicmyths.morozko.FrostSkills.startStream(p),"stream cooldown bypassed");
   System.out.println("STAFF_FROST_STREAM_PASS actual="+damage+" release/cooldown PASS");target.discard();t.succeed();
  });
 }
}
