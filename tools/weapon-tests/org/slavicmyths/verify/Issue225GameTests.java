package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
@GameTestHolder("slavicmyths_weapons") @PrefixGameTestTemplate(false)
public final class Issue225GameTests {
 static ItemStack item(String n){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",n)));}
 static Slime slime(GameTestHelper t,net.minecraft.server.level.ServerPlayer p,double x,double z){var s=EntityType.SLIME.create(t.getLevel());s.setSize(4,true);s.setNoAi(true);s.setNoGravity(true);s.moveTo(p.getX()+x,p.getY(),p.getZ()+z);t.getLevel().addFreshEntity(s);return s;}
 @GameTest(template="empty") public static void sabreHitsSlimeAndCone(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Sabre225");p.setYRot(0);p.setXRot(0);
  var front=slime(t,p,0,3);var behind=slime(t,p,0,-3);var side=slime(t,p,3,0);var outside=slime(t,p,0,6);
  float health=front.getHealth();org.slavicmyths.rewards.RewardEvents.sabreWave(p);
  t.assertTrue(front.getHealth()<health,"Sabre missed actual Slime (Enemy, not Monster)");
  t.assertTrue(behind.getHealth()==health&&side.getHealth()==health&&outside.getHealth()==health,"Sabre hit outside advertised cone/range");
  System.out.println("ISSUE225_SABRE_SLIME_CONE_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void sabreFriendlyAndCooldown(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Reuse225");p.setYRot(0);p.setXRot(0);var target=slime(t,p,0,3);var cow=EntityType.COW.create(t.getLevel());cow.moveTo(p.getX()+1,p.getY(),p.getZ()+2);cow.setNoAi(true);t.getLevel().addFreshEntity(cow);float friendly=cow.getHealth();
  var stack=item("trophy_kalin_sabre");p.setItemInHand(InteractionHand.MAIN_HAND,stack);stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);float after=target.getHealth();target.invulnerableTime=0;stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);
  t.assertTrue(after<target.getMaxHealth()&&target.getHealth()==after,"Actual use/reuse cooldown failed");t.assertTrue(cow.getHealth()==friendly,"Sabre injured friendly cow");t.assertTrue(p.getCooldowns().isOnCooldown(stack.getItem()),"Missing cooldown");
  System.out.println("ISSUE225_SABRE_USE_REUSE_FRIENDLY_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void partialFiguresDoNotOccludeGround(GameTestHelper t){
  for(String n:List.of("fire_idol_tree","fire_idol_sun","fire_idol_circle")){
   var block=BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",n));
   for(Direction facing:Direction.Plane.HORIZONTAL){var state=block.defaultBlockState().setValue(org.slavicmyths.sanctuary.SanctuaryBlock.FACING,facing);t.assertTrue(state.getOcclusionShape(t.getLevel(),t.absolutePos(BlockPos.ZERO)).isEmpty(),"Figure hides full ground face "+n+facing);}
  }
  System.out.println("ISSUE225_FIGURES_12_OCCLUSION_STATES_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void actualHuntingHornSelectionAndDayGuard(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Horn225");var stack=item("ohotnichiy_rog");p.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var old=org.slavicmyths.item.ItemState.target(stack);p.setShiftKeyDown(true);stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);
  t.assertTrue(org.slavicmyths.item.ItemState.target(stack)!=old,"Actual hunting horn did not cycle target");p.setShiftKeyDown(false);
  org.slavicmyths.item.ItemState.target(stack,org.slavicmyths.hunt.HuntTarget.FIRE_SERPENT);
  long time=t.getLevel().getDayTime();try{t.getLevel().setDayTime(6000);stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);t.assertTrue(!org.slavicmyths.hunt.HuntRecords.get(t.getLevel()).active(p.getUUID()),"Horn ignored daytime restriction");t.assertTrue(stack.getCount()==1,"Horn consumed on failed call");}finally{t.getLevel().setDayTime(time);}
  System.out.println("ISSUE225_ACTUAL_HUNT_HORN_SELECTION_DAY_GUARD_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void sabreWallAndSixTargetLimit(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Limit225");p.setYRot(0);p.setXRot(0);var victims=new ArrayList<Slime>();
  for(int i=0;i<8;i++)victims.add(slime(t,p,(i%3-1)*.4,2+i*.15));
  org.slavicmyths.rewards.RewardEvents.sabreWave(p);t.assertTrue(victims.stream().filter(s->s.getHealth()<s.getMaxHealth()).count()==6,"Sabre exceeded/missed six-target cap");
  for(var v:victims)v.discard();var target=slime(t,p,0,3);var at=p.blockPosition().offset(0,0,1);
  for(int x=-2;x<=2;x++)for(int y=0;y<5;y++)t.getLevel().setBlockAndUpdate(at.offset(x,y,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
  org.slavicmyths.rewards.RewardEvents.sabreWave(p);t.assertTrue(target.getHealth()==target.getMaxHealth(),"Sabre wave penetrated wall");
  System.out.println("ISSUE225_SABRE_SIX_CAP_WALL_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void huntingHornActualNightEncounter(GameTestHelper t){
  var p=Issue217GameTests.player(t,"NightHorn225");var stack=item("ohotnichiy_rog");p.setItemInHand(InteractionHand.MAIN_HAND,stack);org.slavicmyths.item.ItemState.target(stack,org.slavicmyths.hunt.HuntTarget.FIRE_SERPENT);
  long time=t.getLevel().getDayTime();try{
   t.getLevel().setDayTime(18000);t.getLevel().updateSkyBrightness();System.out.println("ISSUE225_HORN_ENV day="+t.getLevel().isDay()+" difficulty="+t.getLevel().getDifficulty()+" sky="+t.getLevel().canSeeSky(p.blockPosition()));stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);
   var records=org.slavicmyths.hunt.HuntRecords.get(t.getLevel());t.assertTrue(records.active(p.getUUID()),"Native hunting horn failed actual night encounter");
   var first=records.hunts.get(p.getUUID());stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);t.assertTrue(records.hunts.get(p.getUUID()).equals(first),"Horn duplicated active hunt");
   t.assertTrue(stack.getCount()==1,"Horn consumed on encounter");
  }finally{t.getLevel().setDayTime(time);}
  System.out.println("ISSUE225_HUNT_HORN_NATIVE_NIGHT_ENCOUNTER_UNIQUE_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void ironFlailCanonicalRecipeOnly(GameTestHelper t){
  var recipes=t.getLevel().getRecipeManager();
  t.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath("slavicmyths","flail")).isEmpty(),"Obsolete generic flail recipe is still offered");
  t.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath("slavicmyths","weapon_flail_assembly")).isPresent(),"Iron material flail assembly missing");
  for(String n:List.of("stone","gold","silver","diamond","perunite","netherite"))t.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath("slavicmyths","weapon_"+n+"_flail_assembly")).isPresent(),"Material flail recipe missing "+n);
  t.assertTrue(!item("flail").isEmpty(),"Existing worlds lost canonical iron flail registry ID");
  System.out.println("ISSUE225_FLAIL_SEVEN_MATERIAL_RECIPES_NO_GENERIC_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void figureNativePlaceBreakDrop(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Figure225");int x=1;
  for(String n:List.of("fire_idol_tree","fire_idol_sun","fire_idol_circle")){
   var base=t.absolutePos(new BlockPos(x,1,1));t.getLevel().setBlockAndUpdate(base,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());var stack=item(n);stack.setCount(2);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var context=new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(base).add(0,.5,0),Direction.UP,base,false));stack.useOn(context);
   t.assertTrue(t.getLevel().getBlockState(base.above()).is(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",n))),"Actual figure placement failed "+n);
   t.assertTrue(stack.getCount()==1,"Figure placement did not consume one item");t.getLevel().destroyBlock(base.above(),true,p);
   t.assertTrue(t.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(base.above()).inflate(1),e->e.getItem().is(Items.STONE_BRICKS)).stream().mapToInt(e->e.getItem().getCount()).sum()==1,"Defined stone-bricks loot missing/duplicated "+n);x+=3;
  }
  System.out.println("ISSUE225_FIGURE_NATIVE_PLACE_BREAK_DROP_PASS");t.succeed();
 }
 @GameTest(template="empty") public static void furnitureOnlyRealContainersHaveBlockEntities(GameTestHelper t){
  int count=0;
  for(var entry:org.slavicmyths.furniture.Furniture.BLOCKS.entrySet()){
   var state=entry.getValue().get().defaultBlockState();String id=entry.getKey();boolean expected=id.endsWith("_table")||id.endsWith("_weapon_rack")||id.equals("wooden_crate");
   t.assertTrue(state.hasBlockEntity()==expected,"Wrong block entity declaration "+id);
   var at=t.absolutePos(new BlockPos(1+(count%4)*3,3,1+(count/4)*3));t.getLevel().setBlockAndUpdate(at,state);
   var entity=t.getLevel().getBlockEntity(at);t.assertTrue((entity!=null)==expected,"Missing/phantom furniture block entity "+id);
   if(expected)t.assertTrue(entity.getType().isValid(state),"Container block entity has invalid registry type "+id);count++;
  }
  System.out.println("ISSUE225_FURNITURE_ENTITY_CONTRACT_PASS count="+count);t.succeed();
 }
 @GameTest(template="empty") public static void multipartEntityContractAllStates(GameTestHelper t){
  int count=0;
  for(var block:BuiltInRegistries.BLOCK){
   if(!(block instanceof org.slavicmyths.storage.StorageBlock||block instanceof org.slavicmyths.kurgan.BurialCoffinBlock||block instanceof org.slavicmyths.block.KitchenTableBlock||block instanceof org.slavicmyths.trophy.TrophyHeadBlock))continue;
   var factory=(net.minecraft.world.level.block.EntityBlock)block;
   for(var state:block.getStateDefinition().getPossibleStates()){
    var entity=factory.newBlockEntity(t.absolutePos(BlockPos.ZERO),state);t.assertTrue(state.hasBlockEntity()==(entity!=null),"Multipart declares missing entity: "+state);
    if(entity!=null)t.assertTrue(entity.getType().isValid(state),"Master entity registry type invalid: "+state);count++;
   }
  }
  System.out.println("ISSUE225_MULTIPART_ENTITY_ALL_STATES_PASS count="+count);t.succeed();
 }
 @GameTest(template="empty") public static void actualArmorerMenuConsumesAndRejectsDuplication(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Craft225");var pos=t.absolutePos(new BlockPos(3,2,3));t.getLevel().setBlockAndUpdate(pos,org.slavicmyths.registry.ModBlocks.ARMORER_TABLE.get().defaultBlockState());int count=0;
  for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(org.slavicmyths.armorer.ArmorerRecipe.TYPE.get())){
   if(!holder.id().getPath().startsWith("weapon_"))continue;
   p.getInventory().clearContent();var menu=new org.slavicmyths.armorer.ArmorerMenu(10+count,p.getInventory(),pos);var recipe=holder.value();var used=new ArrayList<Integer>();
   for(int i=0;i<recipe.getIngredients().size();i++){
    var ingredient=recipe.getIngredients().get(i);if(ingredient.isEmpty())continue;var input=ingredient.getItems()[0].copy();input.setCount(2);menu.input.setItem(i,input);used.add(i);
   }
   menu.slotsChanged(menu.input);var expected=menu.getSlot(0).getItem().copy();t.assertTrue(!expected.isEmpty(),"Native result slot empty "+holder.id());
   t.assertTrue(!menu.quickMoveStack(p,0).isEmpty(),"Native menu craft failed "+holder.id());
   for(int slot:used)t.assertTrue(menu.input.getItem(slot).getCount()==1,"Craft consumed wrong ingredient count "+holder.id()+" slot="+slot);
   t.assertTrue(!menu.quickMoveStack(p,0).isEmpty(),"Second native craft failed "+holder.id());
   for(int slot:used)t.assertTrue(menu.input.getItem(slot).isEmpty(),"Second craft left ingredients "+holder.id());
   int actual=p.getInventory().items.stream().filter(s->s.is(expected.getItem())).mapToInt(ItemStack::getCount).sum();t.assertTrue(actual==expected.getCount()*2,"Craft output duplicated/lost "+holder.id());
   t.assertTrue(menu.quickMoveStack(p,0).isEmpty(),"Empty inputs produced extra weapon "+holder.id());count++;
  }
  t.assertTrue(count==37,"Unexpected weapon menu recipe coverage "+count);System.out.println("ISSUE225_NATIVE_MENU_CRAFT_COST_ANTIDUPE_PASS recipes="+count+" crafts="+(count*2));t.succeed();
 }
 @GameTest(template="empty") public static void actualPrimaryAttacksSabreFlailsAndWhip(GameTestHelper t){
  var p=Issue217GameTests.player(t,"Primary225");int hits=0;
  for(String name:List.of("trophy_kalin_sabre","flail","stone_flail","gold_flail","silver_flail","diamond_flail","perunite_flail","netherite_flail","ataman_flail","poloz_whip")){
   var stack=item(name);p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.doTick();var target=slime(t,p,0,2);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);target.invulnerableTime=0;
   p.attack(target);t.assertTrue(target.getHealth()<100,"Actual primary attack missed Slime: "+name);target.discard();hits++;
  }
  System.out.println("ISSUE225_PRIMARY_NATIVE_DAMAGE_PASS weapons="+hits);t.succeed();
 }
}
