package org.slavicmyths.marine;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.registry.ModEffects;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class MarineNewWeaponGameTests {
 static net.minecraft.server.level.ServerPlayer player(GameTestHelper t){var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"MarineSkillQA"));p.moveTo(t.absoluteVec(new Vec3(3,3,3)));return p;}
 static net.minecraft.world.entity.animal.Cow target(GameTestHelper t){var cow=EntityType.COW.create(t.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);cow.moveTo(t.absoluteVec(new Vec3(3,3,8)));t.getLevel().addFreshEntity(cow);return cow;}
 @GameTest(template="empty",timeoutTicks=40) public static void oneHarpoonImpactCountsOnlyOnce(GameTestHelper t){
  var p=player(t);var cow=target(t);var shot=new ThrownTrident(t.getLevel(),p,new ItemStack(MarineWeapons.HARPOON.get()));
  MarineHarpoons.impact(new ProjectileImpactEvent(shot,new EntityHitResult(cow)));cow.invulnerableTime=0;
  MarineHarpoons.impact(new ProjectileImpactEvent(shot,new EntityHitResult(cow)));
  t.assertTrue(p.getPersistentData().getInt(MarineHarpoons.HITS)==1,"Single projectile counted multiple hits");cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void missedTetherPreservesChargeAndConsumesProjectile(GameTestHelper t){
  var p=player(t);var cow=target(t);p.getPersistentData().putInt(MarineHarpoons.HITS,4);
  var shot=new ThrownTrident(t.getLevel(),p,new ItemStack(MarineWeapons.HARPOON.get()));shot.getPersistentData().putUUID(MarineHarpoons.TETHER,cow.getUUID());
  Vec3 landing=cow.position().add(11,0,0);MarineHarpoons.impact(new ProjectileImpactEvent(shot,new BlockHitResult(landing,Direction.UP,BlockPos.containing(landing),false)));
  t.assertTrue(p.getPersistentData().getInt(MarineHarpoons.HITS)==4,"Miss consumed charge");t.assertTrue(!shot.isAlive(),"Missed harpoon survived");t.assertTrue(!cow.getPersistentData().hasUUID(MarineHarpoons.TETHER+"Owner"),"Target beyond ten blocks was tethered");cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void normalHarpoonCannotBeRecoveredAfterBlockHit(GameTestHelper t){
  var p=player(t);var shot=new ThrownTrident(t.getLevel(),p,new ItemStack(MarineWeapons.HARPOON.get()));Vec3 at=p.position().add(0,0,3);
  MarineHarpoons.impact(new ProjectileImpactEvent(shot,new BlockHitResult(at,Direction.UP,BlockPos.containing(at),false)));t.assertTrue(!shot.isAlive(),"Consumed harpoon remained recoverable");p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void staffShotTravelsBeforeHitting(GameTestHelper t){
  var p=player(t);var cow=target(t);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarineWeapons.STAFF.get()));p.setYRot(0);p.setXRot(5);
  for(int x=1;x<=5;x++)for(int y=3;y<=7;y++)for(int z=3;z<=10;z++)t.setBlock(new BlockPos(x,y,z),Blocks.AIR);
  MarineWeaponAbilities.use(new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));t.assertTrue(cow.getHealth()==100,"Staff shot hit instantly");
  for(int i=0;i<10;i++)MarineWeaponAbilities.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(cow.getHealth()<100,"Travelling water shot did not hit");cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void anchorStunPreventsAttacksThenExpires(GameTestHelper t){
  var p=player(t);var cow=target(t);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);cow.setHealth(80);
  p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;
  MarineWeaponAbilities.stun(p,cow);
  t.assertTrue(cow.getPersistentData().getLong(MarineWeaponAbilities.PREFIX+"anchorStunUntil")==t.getLevel().getGameTime()+30,"Stun must last 1.5 seconds");
  var victim=target(t);float health=victim.getHealth();victim.hurt(cow.damageSources().mobAttack(cow),2);
  t.assertTrue(victim.getHealth()==health,"Stunned attacker dealt damage");
  cow.getPersistentData().putLong(MarineWeaponAbilities.PREFIX+"anchorStunUntil",t.getLevel().getGameTime()-1);cow.getPersistentData().putLong("RareStunUntil",org.slavicmyths.combat.RareCombat.now(cow)-1);cow.removeEffect(ModEffects.DISORIENTATION);victim.invulnerableTime=0;victim.hurt(cow.damageSources().mobAttack(cow),2);
  t.assertTrue(victim.getHealth()<health,"Attack lock persisted after stun expiry");victim.discard();
  cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void resistantTargetGetsSlowWithoutAnchorStun(GameTestHelper t){
  var p=player(t);var cow=target(t);MarineWeaponAbilities.stun(p,cow);
  t.assertTrue(cow.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Resistant target lacks slow");
  t.assertTrue(!cow.hasEffect(ModEffects.DISORIENTATION)&&!cow.getPersistentData().contains(MarineWeaponAbilities.PREFIX+"anchorStunUntil"),"Resistant target received full stun");
  cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void shellGuardReadinessTracksPlayerCooldown(GameTestHelper t){
  var p=player(t);var stack=new ItemStack(ZhelvaShield.ITEM.get());long now=t.getLevel().getGameTime();
  t.assertTrue(ZhelvaShield.COOLDOWN==240,"Shell guard cooldown must be twelve seconds");
  t.assertTrue(ZhelvaShield.ready(stack,now),"Fresh shield not ready");
  p.getPersistentData().putLong(MarineWeaponAbilities.PREFIX+"perfectBlock",now+240);
  stack.getItem().inventoryTick(stack,t.getLevel(),p,0,true);
  t.assertTrue(!ZhelvaShield.ready(stack,now),"Shield did not sync cooldown");
  t.assertTrue(ZhelvaShield.ready(stack.copy(),now+240),"Shield did not recover after twelve seconds");
  p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void thrownPharaohTridentRefreshesBleedingTwo(GameTestHelper t){
  var p=player(t);var cow=target(t);var shot=new ThrownTrident(t.getLevel(),p,new ItemStack(PharaohkaTrident.ITEM.get()));
  for(int i=0;i<3;i++){cow.invulnerableTime=0;cow.hurt(p.damageSources().trident(shot,p),4);t.assertTrue(cow.hasEffect(ModEffects.RARE_BLEED)&&cow.getEffect(ModEffects.RARE_BLEED).getAmplifier()==1,"Thrown trident must refresh Bleeding II");}
  shot.discard();cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void tidalSplitHitsForwardAndRespectsCooldown(GameTestHelper t){
  var p=player(t);var cow=target(t);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PharaohkaTrident.ITEM.get()));p.setShiftKeyDown(true);p.setYRot(0);p.setXRot(0);
  for(int x=1;x<=5;x++)for(int y=3;y<=7;y++)for(int z=3;z<=9;z++)t.setBlock(new BlockPos(x,y,z),Blocks.AIR);
  MarineWeaponAbilities.use(new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));
  t.assertTrue(cow.getHealth()<100,"Tidal split did not hit visible forward target");
  float health=cow.getHealth();cow.invulnerableTime=0;MarineWeaponAbilities.use(new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));
  t.assertTrue(cow.getHealth()==health,"Tidal split bypassed cooldown");
  t.assertTrue(p.getPersistentData().getLong(MarineWeaponAbilities.PREFIX+"wave")==t.getLevel().getGameTime()+240,"Wrong split cooldown");
  cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void allMarineWeaponsSupportExistingRuneSockets(GameTestHelper t){
  Item[] items={PharaohkaTrident.ITEM.get(),MarineWeapons.HARPOON.get(),MarineWeapons.ANCHOR.get(),MarineWeapons.STAFF.get(),ZhelvaShield.ITEM.get()};
  String[] categories={"spear","spear","heavy","staff","shield"};
  for(int i=0;i<items.length;i++){
   var stack=new ItemStack(items[i]);
   t.assertTrue(org.slavicmyths.rpg.Runes.capacity(stack)==3,"Marine weapon must support three sockets: "+stack);
   t.assertTrue(org.slavicmyths.rpg.Runes.category(stack).equals(categories[i]),"Wrong rune category: "+stack);
   String rune=java.util.Arrays.stream(org.slavicmyths.rpg.Runes.IDS).filter(id->org.slavicmyths.rpg.Runes.compatible(stack,id)).findFirst().orElseThrow();
   t.assertTrue(org.slavicmyths.rpg.Runes.installationReason(1,3,3,List.of(),rune,true).isEmpty(),"Existing installation rejected rune");
   org.slavicmyths.rpg.Runes.write(stack,3,List.of(rune));
   t.assertTrue(org.slavicmyths.rpg.Runes.has(stack.copy(),rune),"Copied weapon lost rune state");
   t.assertTrue(org.slavicmyths.rpg.Runes.installationReason(1,3,3,List.of(rune,rune,rune),rune,true).length()>0,"Full sockets accepted another rune");
  }
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void masterCraftsEightCopiesAndSurvives(GameTestHelper t){
  var holder=t.getLevel().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","storm_harpoon_ammo")).orElseThrow();var recipe=(ShapedRecipe)holder.value();var values=new ArrayList<ItemStack>();
  for(var ingredient:recipe.getIngredients())values.add(ingredient.isEmpty()?ItemStack.EMPTY:ingredient.getItems()[0].copy());
  var input=CraftingInput.of(recipe.getWidth(),recipe.getHeight(),values);t.assertTrue(recipe.matches(input,t.getLevel()),"Copy recipe does not craft");
  var output=recipe.assemble(input,t.getLevel().registryAccess());t.assertTrue(output.is(MarineHarpoons.AMMO.get())&&output.getCount()==8,"Expected eight expendable copies");
  t.assertTrue(recipe.getRemainingItems(input).stream().anyMatch(s->s.is(MarineWeapons.HARPOON.get())&&s.getCount()==1),"Craft consumed master");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void fixedBleedingAndElementalAdvantage(GameTestHelper t){
  var p=player(t);var cow=target(t);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PharaohkaTrident.ITEM.get()));
  cow.hurt(p.damageSources().playerAttack(p),4);t.assertTrue(cow.getEffect(ModEffects.RARE_BLEED)!=null&&cow.getEffect(ModEffects.RARE_BLEED).getAmplifier()==1,"Trident must start at Bleeding II");
  cow.invulnerableTime=0;cow.hurt(p.damageSources().playerAttack(p),4);t.assertTrue(cow.getEffect(ModEffects.RARE_BLEED).getAmplifier()==1,"Trident bleed escalates without limit");
  var blaze=EntityType.BLAZE.create(t.getLevel());t.assertTrue(Math.abs(MarineWeaponRules.multiplier(blaze)-1.35F)<.001,"Fire advantage must be 35 percent");t.assertTrue(MarineWeaponRules.multiplier(cow)==1,"Neutral creatures gained elemental penalty");
  cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void bubbleProvidesThreeHeartsWithoutStacking(GameTestHelper t){var p=player(t);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarineWeapons.STAFF.get()));float before=p.getAbsorptionAmount();t.assertTrue(MarineWeaponAbilities.bubble(p),"Bubble failed");t.assertTrue(p.getAbsorptionAmount()==before+6,"Bubble did not add three hearts");t.assertTrue(!MarineWeaponAbilities.bubble(p)&&p.getAbsorptionAmount()==before+6,"Bubble stacked through cooldown");p.discard();t.succeed();}
 @GameTest(template="empty",timeoutTicks=40) public static void chargedHarpoonCreatesSevenSecondTether(GameTestHelper t){
  var p=player(t);var cow=target(t);p.getPersistentData().putInt(MarineHarpoons.HITS,4);
  var shot=new ThrownTrident(t.getLevel(),p,new ItemStack(MarineHarpoons.AMMO.get()));shot.getPersistentData().putUUID(MarineHarpoons.TETHER,cow.getUUID());
  Vec3 landing=cow.position().add(2,0,0);MarineHarpoons.impact(new ProjectileImpactEvent(shot,new BlockHitResult(landing,Direction.UP,BlockPos.containing(landing),false)));
  t.assertTrue(cow.getPersistentData().getLong(MarineHarpoons.TETHER+"Until")==t.getLevel().getGameTime()+140,"Tether duration is not seven seconds");t.assertTrue(p.getPersistentData().getInt(MarineHarpoons.HITS)==0,"Tether failed to consume four-hit charge");t.assertTrue(!shot.isAlive(),"Tether harpoon duplicated its ammunition");cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=240) public static void waterVortexDealsDamageThenEnds(GameTestHelper t){
  var p=player(t);var cow=target(t);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarineWeapons.STAFF.get()));p.setShiftKeyDown(true);p.setLastHurtMob(cow);t.setBlock(new BlockPos(4,3,3),Blocks.WATER);
  MarineWeaponAbilities.use(new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));t.assertTrue(p.getPersistentData().hasUUID(MarineWeaponAbilities.PREFIX+"vortexTarget"),"Vortex failed near water");
  for(int tick=1;tick<=200;tick++)t.runAfterDelay(tick,()->MarineWeaponAbilities.tick(new PlayerTickEvent.Post(p)));
  t.runAfterDelay(205,()->{t.assertTrue(cow.getHealth()<100&&cow.getHealth()>50,"Vortex damage missing or unbounded");t.assertTrue(!p.getPersistentData().hasUUID(MarineWeaponAbilities.PREFIX+"vortexTarget"),"Vortex outlived eight-second hit effect");cow.discard();p.discard();t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=40) public static void anchorCannotStrikeThroughNewWall(GameTestHelper t){
  var p=player(t);var cow=target(t);
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarineWeapons.ANCHOR.get()));
  var data=p.getPersistentData();data.putUUID(MarineWeaponAbilities.PREFIX+"anchorTarget",cow.getUUID());
  data.putLong(MarineWeaponAbilities.PREFIX+"anchorImpact",t.getLevel().getGameTime());
  for(int x=1;x<=5;x++)for(int y=2;y<=6;y++)t.setBlock(new BlockPos(x,y,5),Blocks.STONE);
  t.assertTrue(!p.hasLineOfSight(cow),"Test wall did not block sight");
  MarineWeaponAbilities.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(cow.getHealth()==100,"Anchor struck through a wall raised during flight");
  t.assertTrue(!data.hasUUID(MarineWeaponAbilities.PREFIX+"anchorTarget"),"Blocked strike remained pending");
  cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void spentChargeCannotActivateAnotherFlyingHarpoon(GameTestHelper t){
  var p=player(t);var cow=target(t);p.getPersistentData().putInt(MarineHarpoons.HITS,0);
  var shot=new ThrownTrident(t.getLevel(),p,new ItemStack(MarineHarpoons.AMMO.get()));
  shot.getPersistentData().putUUID(MarineHarpoons.TETHER,cow.getUUID());
  Vec3 at=cow.position().add(2,0,0);
  MarineHarpoons.impact(new ProjectileImpactEvent(shot,new BlockHitResult(at,Direction.UP,BlockPos.containing(at),false)));
  t.assertTrue(!cow.getPersistentData().hasUUID(MarineHarpoons.TETHER+"Owner"),"Already-spent charge activated another harpoon");
  t.assertTrue(!shot.isAlive(),"Special harpoon was duplicated");cow.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void anchorPullsSmallTargetButOnlySlowsLargeTarget(GameTestHelper t){
  var p=player(t);var cow=target(t);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);cow.setHealth(80);p.setPos(p.position().add(0,10,0));cow.setPos(cow.position().add(0,10,0));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarineWeapons.ANCHOR.get()));
  for(int x=1;x<=5;x++)for(int y=12;y<=17;y++)for(int z=3;z<=10;z++)t.setBlock(new BlockPos(x,y,z),Blocks.AIR);
  var state=p.getPersistentData();state.putUUID(MarineWeaponAbilities.PREFIX+"anchorTarget",cow.getUUID());state.putLong(MarineWeaponAbilities.PREFIX+"anchorImpact",t.getLevel().getGameTime());
  MarineWeaponAbilities.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(cow.getHealth()<80&&cow.getDeltaMovement().z<0&&cow.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Small target state health="+cow.getHealth()+" velocity="+cow.getDeltaMovement()+" slow="+cow.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)+" sight="+p.hasLineOfSight(cow)+" resistant="+MarineWeaponAbilities.large(cow)+" collision="+t.getLevel().noCollision(cow,cow.getBoundingBox().move(0,0,-.6)));cow.discard();
  var giant=EntityType.IRON_GOLEM.create(t.getLevel());giant.setNoAi(true);giant.moveTo(t.absoluteVec(new Vec3(3,13,8)));t.getLevel().addFreshEntity(giant);
  t.assertTrue(MarineWeaponAbilities.anchorPullable(giant),"Normal golem below four blocks must be pullable");
  giant.getAttribute(Attributes.SCALE).setBaseValue(2);giant.refreshDimensions();
  t.assertTrue(!MarineWeaponAbilities.anchorPullable(giant),"Target above four blocks must resist anchor pull");
  state.putUUID(MarineWeaponAbilities.PREFIX+"anchorTarget",giant.getUUID());state.putLong(MarineWeaponAbilities.PREFIX+"anchorImpact",t.getLevel().getGameTime());MarineWeaponAbilities.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(giant.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Large target did not receive slow");
  t.assertTrue(giant.getDeltaMovement().z>=0,"Large target was pulled");giant.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void rangeEnchantmentAndMytarWeaponsAreUsable(GameTestHelper t){
  var world=t.getLevel();
  var enchant=world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(org.slavicmyths.combat.RangeEnchantment.KEY);
  for(var item:java.util.List.of(MarineWeapons.ANCHOR.get(),MarineWeapons.HARPOON.get(),PharaohkaTrident.ITEM.get(),org.slavicmyths.registry.ModItems.MYTAR_SHACKLED_SICKLE.get())){
   var stack=new ItemStack(item);
   t.assertTrue(enchant.value().isSupportedItem(stack),"Range tag does not support "+item);
   for(int rank=1;rank<=3;rank++){
    stack.enchant(enchant,rank);
    t.assertTrue(Math.abs(org.slavicmyths.combat.RangeEnchantment.multiplier(world,stack)-(1+.25*rank))<.001,"Incorrect range multiplier");
   }
  }
  t.assertTrue(org.slavicmyths.registry.ModItems.MYTAR_SICKLE.get() instanceof net.minecraft.world.item.SwordItem,"Mytar sickle remains a plain item");
  t.assertTrue(org.slavicmyths.registry.ModItems.MYTAR_SHACKLED_SICKLE.get() instanceof org.slavicmyths.posad.boss.ShackledSickleItem,"Shackled sickle throw implementation missing");
  int weapons=org.slavicmyths.registry.CreativeCatalog.TABS.indexOf("weapons");
  var stacks=org.slavicmyths.registry.ModItemGroup.stacks(weapons);
  t.assertTrue(stacks.stream().anyMatch(x->x.is(org.slavicmyths.registry.ModItems.MYTAR_SICKLE.get())),"Mytar sickle absent from creative weapons");
  t.assertTrue(stacks.stream().anyMatch(x->x.is(org.slavicmyths.registry.ModItems.MYTAR_SHACKLED_SICKLE.get())),"Shackled sickle absent from creative weapons");
  t.succeed();
 } @GameTest(template="empty",timeoutTicks=40) public static void higherMorRetaliatesInsteadOfSwitchingToNearbyPlayer(GameTestHelper t){
  var p=player(t);var cow=target(t);
  var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","ovinnik"));
  var boss=(org.slavicmyths.entity.LandSpiritEntity)type.create(t.getLevel());
  boss.moveTo(cow.getX()+2,cow.getY(),cow.getZ());boss.setHealth(boss.getMaxHealth());boss.morSync(2,0,0);boss.tickCount=10;t.getLevel().addFreshEntity(boss);
  t.assertTrue(!org.slavicmyths.mor.MorRetaliation.valid(boss,cow),"Passive bystander became retaliation target");
  boss.setLastHurtByMob(cow);boss.setTarget(p);
  t.assertTrue(org.slavicmyths.mor.MorRetaliation.valid(boss,cow),"Real non-player aggressor rejected");
  new org.slavicmyths.mor.HigherMor(boss).tick();
  t.assertTrue(boss.getTarget()==cow,"Nearby player overrode the attacking mob");
  boss.tickCount+=201;
  t.assertTrue(!org.slavicmyths.mor.MorRetaliation.valid(boss,cow),"Expired provocation retained forever");
  boss.discard();cow.discard();p.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=180) public static void bossThreatSwitchesToActiveSecondPlayer(GameTestHelper t){
  for(int x=1;x<=5;x++)for(int y=7;y<=20;y++)for(int z=1;z<=12;z++)t.setBlock(new BlockPos(x,y,z),Blocks.AIR);
  var tank=player(t);var striker=player(t);tank.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);striker.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  tank.setNoGravity(true);striker.setNoGravity(true);tank.moveTo(t.absoluteVec(new Vec3(3,8,3)));striker.moveTo(t.absoluteVec(new Vec3(3,8,10)));
  t.getLevel().addNewPlayer(tank);t.getLevel().addNewPlayer(striker);
  var boss=org.slavicmyths.sisters.SistersContent.LOMEYA.get().create(t.getLevel());boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(t.absoluteVec(new Vec3(3,8,6)));t.getLevel().addFreshEntity(boss);boss.setTarget(tank);
  t.runAfterDelay(15,()->{
   boss.invulnerableTime=0;t.assertTrue(boss.hurt(striker.damageSources().playerAttack(striker),200),"Second player's actual damage rejected");
   t.assertTrue(boss.getTarget()==tank,"Boss switched immediately inside minimum focus time");
  });
  t.runAfterDelay(85,()->{
   t.assertTrue(boss.getTarget()==striker,"Boss ignored high damage: sight="+boss.hasLineOfSight(striker)+" found="+(t.getLevel().getPlayerByUUID(striker.getUUID())==striker)+" age="+boss.tickCount+" target="+boss.getTarget()+" busy="+org.slavicmyths.combat.BossThreat.attacking(boss)+" health="+boss.getHealth());
   boss.invulnerableTime=0;boss.hurt(tank.damageSources().playerAttack(tank),1);
   t.assertTrue(boss.getTarget()==striker,"Tiny hit caused target oscillation");
   boss.discard();tank.discard();striker.discard();t.succeed();
  });
 } @GameTest(template="empty",timeoutTicks=40) public static void netherAuraSurvivalAndCreativeDoNotCrash(GameTestHelper t){
  var world=t.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
  var p=FakePlayerFactory.get(world,new GameProfile(UUID.randomUUID(),"AuraRegressionQA"));p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  p.setHealth(p.getMaxHealth());
  var state=org.slavicmyths.winters.WintersEvents.data(p);
  long now=org.slavicmyths.winters.WintersEvents.now(p);
  state.putLong("AuraStart",now-1300);state.putInt("AuraStage",-1);
  org.slavicmyths.winters.WintersEvents.aura(p);
  t.assertTrue(p.hasEffect(org.slavicmyths.winters.WintersContent.AURA),"Survival Nether aura is missing");
  p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);org.slavicmyths.winters.WintersEvents.aura(p);
  t.assertTrue(!p.hasEffect(org.slavicmyths.winters.WintersContent.AURA)&&!state.contains("AuraStart"),"Creative retains Nether punishment");
  p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=140) public static void morovoyFireZmeyFinishesDeathAndDisappears(GameTestHelper t){
  var boss=org.slavicmyths.morovoy.MorovoyFireZmeyContent.BOSS.get().create(t.getLevel());boss.moveTo(t.absoluteVec(new Vec3(3,10,3)));boss.setNoAi(true);t.getLevel().addFreshEntity(boss);
  boss.die(boss.damageSources().genericKill());
  t.assertTrue(boss.state()==org.slavicmyths.morovoy.MorovoyFireZmey.DYING,"Death animation did not begin");
  t.runAfterDelay(110,()->{t.assertTrue(boss.isRemoved(),"Fire serpent corpse remained after death animation");t.succeed();});
 } @GameTest(template="empty",timeoutTicks=40) public static void aquaticEggCannotLeaveBossOnDryLand(GameTestHelper t){
  var boss=ZhelvaContent.BOSS.get().create(t.getLevel());boss.moveTo(t.absoluteVec(new Vec3(3,10,3)));
  var event=new net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent(boss,t.getLevel(),boss.getX(),boss.getY(),boss.getZ(),t.getLevel().getCurrentDifficultyAt(boss.blockPosition()),MobSpawnType.SPAWN_EGG,null,null);
  MarineEncounterSpawns.naturalSpawn(event);
  t.assertTrue(event.isSpawnCancelled()||t.getLevel().getFluidState(boss.blockPosition()).is(net.minecraft.tags.FluidTags.WATER),"Aquatic egg left a turtle on land");boss.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void ownerlessKalinSoldiersKeepCommandersTarget(GameTestHelper t){
  var p=player(t);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var boss=org.slavicmyths.kalin.KalinContent.KALIN.get().create(t.getLevel());boss.setNoAi(true);boss.moveTo(p.position().add(3,0,0));t.getLevel().addFreshEntity(boss);boss.setTarget(p);t.assertTrue(boss.getTarget()==p,"Commander rejected the survival target");
  var soldier=org.slavicmyths.kalin.KalinContent.COMMON.get().create(t.getLevel());soldier.commander=boss.getUUID();soldier.moveTo(p.position().add(6,0,0));boss.soldiers.add(soldier.getUUID());t.getLevel().addFreshEntity(soldier);soldier.tickCount=10;soldier.tick();
  t.assertTrue(soldier.getTarget()==p,"Ownerless soldier lost the commander's legitimate target");soldier.discard();boss.discard();p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40) public static void bossBarsShowLogicalHealthAndKeepTierColor(GameTestHelper t){
  var boss=org.slavicmyths.sisters.SistersContent.LOMEYA.get().create(t.getLevel());
  var bar=new org.slavicmyths.combat.CreatureBossBar(boss,net.minecraft.network.chat.Component.literal("Ломея"),net.minecraft.world.BossEvent.BossBarColor.RED,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
  bar.setProgress(.5F);bar.setColor(net.minecraft.world.BossEvent.BossBarColor.BLUE);
  t.assertTrue(bar.getName().getString().contains("1000 / 2000 HP"),"Numeric boss health is wrong");
  t.assertTrue(bar.getColor()==net.minecraft.world.BossEvent.BossBarColor.PURPLE,"Phase change overrode tier color");
  var zmey=org.slavicmyths.morovoy.MorovoyFireZmeyContent.BOSS.get().create(t.getLevel());
  var scaled=new org.slavicmyths.combat.CreatureBossBar(zmey,net.minecraft.network.chat.Component.literal("Змей"),net.minecraft.world.BossEvent.BossBarColor.RED,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);scaled.setProgress(.5F);
  t.assertTrue(scaled.getName().getString().contains("1100 / 2200 HP"),"Internal capped health leaked into the boss label");boss.discard();zmey.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=40) public static void pairedSistersUseRoleHealthWithoutChangingSoloSisters(GameTestHelper t){
  var world=t.getLevel();
  var melee=org.slavicmyths.sisters.SistersContent.AGNEYA.get().create(world);var ranged=org.slavicmyths.sisters.SistersContent.LEDEYA.get().create(world);
  var tank=org.slavicmyths.sisters.SistersContent.GNETEYA.get().create(world);var caster=org.slavicmyths.sisters.SistersContent.TRYASEYA.get().create(world);
  t.assertTrue(melee.getMaxHealth()==1200&&ranged.getMaxHealth()==800&&tank.getMaxHealth()==1200&&caster.getMaxHealth()==800,"Paired sister health is outside the approved role values");
  var lomeya=org.slavicmyths.sisters.SistersContent.LOMEYA.get().create(world);var neveya=org.slavicmyths.sisters.SistersContent.NEVEYA.get().create(world);
  t.assertTrue(lomeya.getMaxHealth()==2000&&neveya.getMaxHealth()==2400,"Solo sister health changed");
  melee.discard();ranged.discard();tank.discard();caster.discard();lomeya.discard();neveya.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=40) public static void turtleChecksItsWholeShellAgainstTreeBranches(GameTestHelper t){
  for(int x=-2;x<=18;x++)for(int y=5;y<=13;y++)for(int z=-2;z<=20;z++)t.setBlock(new BlockPos(x,y,z),Blocks.WATER);
  var turtle=ZhelvaContent.BOSS.get().create(t.getLevel());turtle.moveTo(t.absoluteVec(new Vec3(8,7,8)));t.getLevel().addFreshEntity(turtle);turtle.baseTick();
  var box=turtle.getBoundingBox();var branch=new BlockPos((int)Math.floor(box.maxX)-1,(int)Math.floor(turtle.getY()+1),(int)Math.ceil(box.maxZ));
  t.getLevel().setBlock(branch,Blocks.OAK_LOG.defaultBlockState(),3);
  t.assertTrue(t.getLevel().noCollision(turtle,box),"Fixture begins inside the tree");
  turtle.setDeltaMovement(0,0,.8);turtle.swimToward(turtle.position().add(0,0,20),.31);
  var movement=turtle.getDeltaMovement();
  t.assertTrue(t.getLevel().noCollision(turtle,box.expandTowards(movement.scale(4))),"Turtle steered its shell into a branch while its center was clear");
  t.assertTrue(t.getLevel().getBlockState(branch).is(Blocks.OAK_LOG),"Obstacle was destroyed");turtle.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=80) public static void largeBossesRejectTreeBranchesAlongTheirWholeMovement(GameTestHelper t){
  String[] ids={"zmey_gorynych","ocean_zmey","deep_sea_serpent","morovoy_fire_zmey","morovoy_raven","stratim_bird","zhelva","tsar_pike","pharaohka"};
  var world=t.getLevel();
  for(String id:ids){
   var entity=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(world);
   entity.moveTo(t.absoluteVec(new Vec3(8,20,8)));
   var box=entity.getBoundingBox();
   var branch=BlockPos.containing(box.maxX-.5,box.minY+.5,Math.ceil(box.maxZ));
   var old=world.getBlockState(branch);world.setBlock(branch,Blocks.OAK_LOG.defaultBlockState(),3);
   t.assertTrue(!org.slavicmyths.combat.LargeMobClearance.clear(entity,new Vec3(0,0,4)),id+" accepted a branch across its body route");
   var motion=org.slavicmyths.combat.LargeMobClearance.steer(entity,new Vec3(0,0,1.5));
   t.assertTrue(org.slavicmyths.combat.LargeMobClearance.clear(entity,motion.scale(3)),id+" chose an obstructed detour");
   t.assertTrue(world.getBlockState(branch).is(Blocks.OAK_LOG),id+" destroyed the tree");
   world.setBlock(branch,old,3);entity.discard();
  }
  t.succeed();
 } @GameTest(template="empty",timeoutTicks=40) public static void pairedHammerSkillsRespectNewDamageAndCooldown(GameTestHelper t){
  var p=player(t);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var cow=target(t);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);cow.setHealth(1000);
  var main=new ItemStack(org.slavicmyths.sisters.SistersContent.LOMEYA_HAMMER.get());p.setItemInHand(InteractionHand.MAIN_HAND,main);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(org.slavicmyths.sisters.SistersContent.LOMEYA_HAMMER_OFFHAND.get()));
  // Lomeya routes RMB actions through WeaponInputHandler, not Item.use.
  var handler=(org.slavicmyths.combat.WeaponInputHandler)main.getItem();
  handler.onWeaponAction(p,main,org.slavicmyths.combat.WeaponInputHandler.Action.RIGHT_CLICK);
  t.assertTrue(Math.abs(cow.getHealth()-972)<.1,"Dual hammer wave is not 28 damage: "+cow.getHealth());
  t.assertTrue(p.getCooldowns().isOnCooldown(main.getItem()),"Hammer skill has no cooldown");
  float after=cow.getHealth();
  handler.onWeaponAction(p,main,org.slavicmyths.combat.WeaponInputHandler.Action.RIGHT_CLICK);
  t.assertTrue(cow.getHealth()==after,"Skill bypassed its cooldown");
  p.getCooldowns().removeCooldown(main.getItem());
  cow.invulnerableTime=0;
  p.setShiftKeyDown(true);
  handler.onWeaponAction(p,main,org.slavicmyths.combat.WeaponInputHandler.Action.SHIFT_RIGHT_CLICK);
  t.assertTrue(Math.abs(cow.getHealth()-(after-36))<.1,"Dual hammer fracture is not 36 damage: "+cow.getHealth());
  t.assertTrue(org.slavicmyths.item.StormStaffItem.pulseDamage(false,false)==8&&org.slavicmyths.item.StormStaffItem.cooldownTicks(false)==32,"Storm staff balance mismatch");
  cow.discard();p.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=40)
 public static void marineBossesPreferRecentMobAttacker(GameTestHelper t){
  for(String id:new String[]{"stratim_bird","ocean_zmey","zhelva","tsar_pike","deep_sea_serpent"}){
   var boss=(net.minecraft.world.entity.Mob)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(t.getLevel());
   var attacker=EntityType.COW.create(t.getLevel());
   boss.moveTo(t.absoluteVec(new Vec3(3,5,3)));attacker.moveTo(boss.position().add(4,0,0));
   boss.setLastHurtByMob(attacker);
   String method=id.equals("tsar_pike")?"searchPrey":id.equals("deep_sea_serpent")?"findPrey":"findTarget";
   try{var search=boss.getClass().getDeclaredMethod(method);search.setAccessible(true);search.invoke(boss);}catch(ReflectiveOperationException e){throw new RuntimeException(e);}
   t.assertTrue(boss.getTarget()==attacker,id+" ignored the attacking mob");
   attacker.setHealth(0);
   t.assertTrue(!org.slavicmyths.combat.BossRetaliation.select(boss,80),id+" retained dead retaliation");
   attacker.discard();boss.discard();
  }
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void lomeyaDamagesMobRetaliationTarget(GameTestHelper t){
  var boss=(org.slavicmyths.sisters.LomeyaBoss)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","lomeya")).create(t.getLevel());
  var cow=target(t);boss.moveTo(cow.position().add(0,0,-2));boss.setYRot(0);boss.setLastHurtByMob(cow);boss.setTarget(cow);
  try{var damage=boss.getClass().getDeclaredMethod("damage",net.minecraft.world.entity.LivingEntity.class,float.class,boolean.class);damage.setAccessible(true);damage.invoke(boss,cow,10F,false);}catch(ReflectiveOperationException e){throw new RuntimeException(e);}
  t.assertTrue(cow.getHealth()<100,"Lomeya selected a mob but could not damage it");cow.discard();boss.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=40)
 public static void neveyaAndViyCanDamageMobAttackers(GameTestHelper t){
  for(String id:new String[]{"neveya","viy"}){
   var boss=(net.minecraft.world.entity.Mob)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(t.getLevel());
   var cow=target(t);boss.moveTo(cow.position().add(0,0,-2));boss.setLastHurtByMob(cow);boss.setTarget(cow);
   try{
    if(id.equals("neveya")){var hit=boss.getClass().getDeclaredMethod("hit",net.minecraft.world.entity.LivingEntity.class,float.class,int.class);hit.setAccessible(true);hit.invoke(boss,cow,10F,1);}
    else{var area=boss.getClass().getDeclaredMethod("area",net.minecraft.server.level.ServerLevel.class,Vec3.class,float.class,float.class);area.setAccessible(true);area.invoke(boss,t.getLevel(),cow.position(),4F,10F);}
   }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
   t.assertTrue(cow.getHealth()<100,id+" cannot damage mob target");cow.discard();boss.discard();
  }
  t.succeed();
 } @GameTest(template="empty",timeoutTicks=40)
 public static void posadBossesDamageMobTargets(GameTestHelper t){
  for(String id:new String[]{"cursed_mytar","bauk"}){
   var boss=(org.slavicmyths.posad.boss.PosadBoss)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(t.getLevel());
   var cow=target(t);boss.moveTo(cow.position().add(0,0,-2));boss.setYRot(0);boss.setLastHurtByMob(cow);boss.setTarget(cow);
   try{
    var begin=boss.getClass().getDeclaredMethod("begin",org.slavicmyths.posad.boss.PosadBoss.Move.class,int.class,int.class);begin.setAccessible(true);
    var hit=boss.getClass().getDeclaredMethod("applyHit");hit.setAccessible(true);
    for(var move:id.equals("cursed_mytar")?new org.slavicmyths.posad.boss.PosadBoss.Move[]{org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_SICKLE,org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_COINS,org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_CHAIN}:new org.slavicmyths.posad.boss.PosadBoss.Move[]{org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_BITE,org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_PAW,org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_SLAM,org.slavicmyths.posad.boss.PosadBoss.Move.WINDUP_GRAB}){
     cow.invulnerableTime=0;cow.setHealth(100);begin.invoke(boss,move,40,20);hit.invoke(boss);
     t.assertTrue(cow.getHealth()<100,id+" cannot hit mob with "+move);
    }
   }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
   cow.discard();boss.discard();
  }
  t.succeed();
 }}