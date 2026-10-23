package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.slavicmyths.trophy.TrophyHeads;

/** Real loader/world/placement/use regressions for the changes made in this chat. */
@GameTestHolder("slavicmyths_weapons") @PrefixGameTestTemplate(false)
public final class Issue217GameTests {
 static ItemStack item(String name){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",name)));}
 static ServerPlayer player(GameTestHelper t,String name){try{var method=WeaponGameTests.class.getDeclaredMethod("player",GameTestHelper.class,String.class);method.setAccessible(true);return (ServerPlayer)method.invoke(null,t,name);}catch(Exception ex){throw new RuntimeException(ex);}}
 @GameTest(template="empty") public static void actualReachAndWhipHit(GameTestHelper t){
  var p=player(t,"Reach217");var w=t.getLevel();var target=EntityType.COW.create(w);target.setNoAi(true);target.setNoGravity(true);w.addFreshEntity(target);
  for(String id:List.of("ancient_spear","restored_spear","thunder_spear","poloz_whip")){
   var stack=item(id);p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.doTick();double reach=id.equals("poloz_whip")?6:4.25;
   t.assertTrue(Math.abs(p.entityInteractionRange()-reach)<.0001,"Actual equipped reach changed "+id+"="+p.entityInteractionRange());
   double radius=target.getBbWidth()*.5;
   target.moveTo(p.getX(),p.getEyeY()-.5,p.getZ()+reach+radius+.05);t.assertTrue(!p.canInteractWithEntity(target,0),"Server reach accepted beyond boundary "+id);
   target.moveTo(p.getX(),p.getEyeY()-.5,p.getZ()+reach+radius-.05);t.assertTrue(p.canInteractWithEntity(target,0),"Server reach rejected inner boundary "+id);
  }
  target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);p.resetAttackStrengthTicker();p.attack(target);t.assertTrue(target.getHealth()<100,"Actual whip attack failed to damage in-range target");target.discard();System.out.println("ISSUE217_REACH_RUNTIME_PASS equipped spear4.25/whip6, inside/outside AABB and actual whip hit");t.succeed();
 }
 @GameTest(template="empty") public static void hornThresholdOutsideArena(GameTestHelper t){
  var p=player(t,"Horn217");var w=t.getLevel();var horn=item("beast_challenge_horn");p.setItemInHand(InteractionHand.MAIN_HAND,horn);horn.getItem().use(w,p,InteractionHand.MAIN_HAND);
  horn.getItem().onUseTick(w,p,horn,19);t.assertTrue(!p.getCooldowns().isOnCooldown(horn.getItem()),"Horn triggered before20 ticks");
  horn.getItem().onUseTick(w,p,horn,18);t.assertTrue(p.getCooldowns().isOnCooldown(horn.getItem())&&p.getPersistentData().getLong("CallCooldown_beast_challenge_horn")==w.getGameTime()+300,"Horn threshold/CD failed");
  t.assertTrue(org.slavicmyths.skim.SkimValley.get(w).lock(p.getUUID())==null&&horn.getCount()==1,"Out-of-arena horn created encounter or consumed itself");p.stopUsingItem();System.out.println("ISSUE217_HORN_RUNTIME_PASS threshold20, cooldown300, wrong-arena rejection without item loss");t.succeed();
 }
 @GameTest(template="empty") public static void retainerActualFrontBlock(GameTestHelper t)throws Exception{
  var attacker=player(t,"Shield217A");var p=player(t,"Shield217B");p.moveTo(attacker.position().add(0,0,-2));p.setYRot(0);var shield=item("retainer_shield");p.setItemInHand(InteractionHand.OFF_HAND,shield);shield.getItem().use(t.getLevel(),p,InteractionHand.OFF_HAND);
  var spawn=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");spawn.setAccessible(true);spawn.setInt(p,0);
  t.runAfterDelay(6,()->{for(int i=0;i<6;i++)p.doTick();t.assertTrue(p.isBlocking(),"Native retainer shield not raised after delay");float health=p.getHealth();var zombie=EntityType.ZOMBIE.create(t.getLevel());zombie.moveTo(attacker.position());p.hurt(p.damageSources().mobAttack(zombie),5);t.assertTrue(p.getHealth()==health&&shield.getDamageValue()>0,"Frontal attack bypassed retainer shield or failed durability cost");p.stopUsingItem();System.out.println("ISSUE217_SHIELD_USE_RUNTIME_PASS native frontal block prevents damage and spends shield durability");t.succeed();});
 }
 @GameTest(template="empty") public static void finistActualAntiparallelTick(GameTestHelper t){
  var p=player(t,"Guide217");p.moveTo(p.getX(),205,p.getZ());var w=t.getLevel();var target=EntityType.ZOMBIE.create(w);target.setNoAi(true);target.setNoGravity(true);target.moveTo(p.getX(),p.getY(),p.getZ()-10);w.addFreshEntity(target);
  var arrow=new org.slavicmyths.finist.FinistArrow(p,w,item("finist_bow"));arrow.setNoGravity(true);arrow.setPos(p.getX(),target.getBoundingBox().getCenter().y,p.getZ());arrow.setDeltaMovement(0,0,1);arrow.guide(target.getUUID());w.addFreshEntity(arrow);arrow.tick();
  double turn=Math.toDegrees(Math.acos(Math.clamp(arrow.getDeltaMovement().normalize().dot(new Vec3(0,0,1)),-1,1)));
  t.assertTrue(turn>6&&turn<=45.01,"Actual guided arrow turn outside adaptive bound: "+turn);t.assertTrue(target.getUUID().equals(arrow.guide()),"Actual guiding state lost");arrow.discard();target.discard();System.out.println("ISSUE217_FINIST_RUNTIME_PASS actual projectile tick, bounded adaptive antiparallel turn="+turn);t.succeed();
 }
 @GameTest(template="empty") public static void modernThrownHeatDoesNotGainLegacyProc(GameTestHelper t){
  var p=player(t,"Modern217");var w=t.getLevel();var target=EntityType.COW.create(w);target.setNoAi(true);target.setNoGravity(true);target.moveTo(p.getX(),p.getY(),p.getZ()+2);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);w.addFreshEntity(target);
  var stack=item("iron_throwing_knife");org.slavicmyths.item.ItemState.runes(stack,1,List.of("heat"));t.assertTrue(org.slavicmyths.item.ItemState.legacyRuneSpecials(stack).isEmpty(),"Fixture unexpectedly legacy");
  var shot=new org.slavicmyths.combat.WeaponProjectile(w,p,stack,true,false);shot.setPos(p.getX(),p.getY()+.7,p.getZ());shot.setDeltaMovement(0,0,3);w.addFreshEntity(shot);shot.tick();
  t.assertTrue(target.getHealth()<100&&!target.isOnFire(),"Modern thrown rune acquired legacy-only fire proc");shot.discard();target.discard();System.out.println("ISSUE217_MODERN_HEAT_RUNTIME_PASS hit without unintended legacy fire");t.succeed();
 }
 @GameTest(template="empty") public static void trophyNativePlacementAndUpperRemoval(GameTestHelper t){
  var p=player(t,"Heads217");var w=t.getLevel();var floor=t.absolutePos(new BlockPos(3,1,3));w.setBlock(floor,Blocks.STONE.defaultBlockState(),3);
  for(var kind:TrophyHeads.Kind.values()){
   var stack=new ItemStack(TrophyHeads.ITEMS.get(kind).get());p.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var result=stack.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false)));
   t.assertTrue(result.consumesAction(),"Head placement rejected "+kind);
   t.assertTrue(w.getBlockEntity(floor.above())!=null,"Native head tile missing "+kind);
   t.assertTrue(w.getBlockState(floor.above(kind.height)).is(TrophyHeads.BLOCKS.get(kind).get()),"Native upper/lower placement "+kind);
   w.destroyBlock(floor.above(kind.height),true);
   t.assertTrue(w.getBlockState(floor.above()).isAir()&&w.getBlockState(floor.above(2)).isAir(),"Orphan trophy half "+kind);
   var drops=w.getEntitiesOfClass(ItemEntity.class,new AABB(floor).inflate(3),e->e.getItem().is(TrophyHeads.ITEMS.get(kind).get()));
   t.assertTrue(drops.stream().mapToInt(e->e.getItem().getCount()).sum()==1,"Trophy duplicate/missing drop "+kind);drops.forEach(Entity::discard);
  }
  System.out.println("ISSUE217_HEADS_RUNTIME_PASS six native placements/tiles, upper removal and exactly one recovery each");t.succeed();
 }
 @GameTest(template="empty") public static void stormAndLikhoActualUse(GameTestHelper t){
  var p=player(t,"Staves217");var w=t.getLevel();var target=EntityType.ZOMBIE.create(w);target.setNoAi(true);target.setNoGravity(true);target.moveTo(p.getX(),p.getY(),p.getZ()+3);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);w.addFreshEntity(target);
  var storm=item("storm_staff");p.setItemInHand(InteractionHand.MAIN_HAND,storm);p.setXRot(27);float before=target.getHealth();storm.getItem().use(w,p,InteractionHand.MAIN_HAND);
  t.assertTrue(target.getHealth()<before,"Actual storm ray pulse did not damage valid target");t.assertTrue(storm.getDamageValue()==1&&p.getCooldowns().isOnCooldown(storm.getItem()),"Storm cost/CD not applied");
  float after=target.getHealth();storm.getItem().use(w,p,InteractionHand.MAIN_HAND);t.assertTrue(target.getHealth()==after&&storm.getDamageValue()==1,"Storm cooldown allowed repeat damage/cost");
  var likho=item("likho_staff");p.setItemInHand(InteractionHand.OFF_HAND,likho);p.setXRot(0);likho.getItem().use(w,p,InteractionHand.OFF_HAND);
  t.assertTrue(likho.getDamageValue()==1&&p.getCooldowns().isOnCooldown(likho.getItem()),"Likho actual offhand use did not apply cost/CD");
  t.assertTrue(target.getPersistentData().getUUID("RareMorokOwner").equals(p.getUUID()),"Likho did not affect selected actual target");
  target.discard();System.out.println("ISSUE217_STAVES_RUNTIME_PASS storm hit/cost/CD rejection and Likho offhand target/owner");t.succeed();
 }
 @GameTest(template="empty") public static void velesActualGuardianAndReuse(GameTestHelper t){
  var p=player(t,"Veles217");var w=t.getLevel();var at=p.blockPosition();for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){w.setBlock(at.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);w.setBlock(at.offset(x,0,z),Blocks.AIR.defaultBlockState(),3);w.setBlock(at.offset(x,1,z),Blocks.AIR.defaultBlockState(),3);}
  var staff=item("veles_staff");p.setItemInHand(InteractionHand.MAIN_HAND,staff);var result=staff.getItem().use(w,p,InteractionHand.MAIN_HAND);
  t.assertTrue(result.getResult().consumesAction()&&staff.getDamageValue()==1,"Veles actual summon rejected/cost incorrect");
  var owned=w.getEntitiesOfClass(org.slavicmyths.entity.WildlifeEntity.class,p.getBoundingBox().inflate(5),e->e.getPersistentData().hasUUID("SlavicGuardianOwner")&&e.getPersistentData().getUUID("SlavicGuardianOwner").equals(p.getUUID()));
  t.assertTrue(owned.size()==1,"Veles did not create exactly one guardian");staff.getItem().use(w,p,InteractionHand.MAIN_HAND);t.assertTrue(staff.getDamageValue()==1,"Veles repeat spent durability despite server cooldown");owned.forEach(Entity::discard);System.out.println("ISSUE217_VELES_RUNTIME_PASS actual guardian/owner/durability/server reuse rejection");t.succeed();
 }
 @GameTest(template="empty") public static void shieldAndHornActualUse(GameTestHelper t){
  var p=player(t,"Use217");var w=t.getLevel();var shield=item("retainer_shield");p.setItemInHand(InteractionHand.OFF_HAND,shield);var blocked=shield.getItem().use(w,p,InteractionHand.OFF_HAND);
  t.assertTrue(blocked.getResult().consumesAction()&&p.isUsingItem()&&p.getUsedItemHand()==InteractionHand.OFF_HAND,"Shield offhand use path failed");t.assertTrue(p.getUseItem().getUseAnimation()==UseAnim.BLOCK,"Shield lost BLOCK animation");p.stopUsingItem();
  var horn=item("beast_challenge_horn");p.setItemInHand(InteractionHand.MAIN_HAND,horn);horn.getItem().use(w,p,InteractionHand.MAIN_HAND);
  t.assertTrue(p.isUsingItem()&&horn.getUseAnimation()==UseAnim.TOOT_HORN&&horn.getUseDuration(p)==38,"Horn native hold/duration/animation failed");p.stopUsingItem();
  t.assertTrue(!p.getCooldowns().isOnCooldown(horn.getItem()),"Cancelled horn spent cooldown before threshold");System.out.println("ISSUE217_USE_RUNTIME_PASS offhand shield BLOCK and horn hold/cancel before threshold");t.succeed();
 }
}
