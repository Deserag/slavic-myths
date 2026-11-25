package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class SandKingGameTests {
 @GameTest(template="empty")
 public static void sandKingRegisteredAndPoisonImmune(GameTestHelper h){
  var boss=SandKingContent.BOSS.get().create(h.getLevel());h.assertTrue(boss!=null,"Sand King registry failed");if(boss==null)return;
  h.assertTrue(boss.getMaxHealth()==800,"wrong boss health");h.assertTrue(boss.getParts().length==26,"wrong multipart count");
  h.assertTrue(!boss.canBeAffected(new MobEffectInstance(MobEffects.POISON,100,1)),"poison immunity missing");h.assertTrue(SandKingRules.LEVEL==7,"wrong level");boss.discard();h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=650)
 public static void sandKingQuicksandRestoresBothSandTypes(GameTestHelper h){
  var w=h.getLevel();BlockPos a=h.absolutePos(new BlockPos(2,2,2)),b=a.east(),protectedPos=a.south();
  for(var p:new BlockPos[]{a,b,protectedPos}){w.setBlock(p.below(),Blocks.STONE.defaultBlockState(),Block.UPDATE_ALL);w.setBlock(p.above(),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
  w.setBlock(a,Blocks.SAND.defaultBlockState(),Block.UPDATE_ALL);w.setBlock(b,Blocks.RED_SAND.defaultBlockState(),Block.UPDATE_ALL);w.setBlock(protectedPos,Blocks.SANDSTONE.defaultBlockState(),Block.UPDATE_ALL);
  SandKingQuicksand.create(w,Vec3.atBottomCenterOf(a.above()));Block quick=DesertBlocks.ALL.get("quicksand").get();
  h.assertTrue(w.getBlockState(a).is(quick)&&w.getBlockState(b).is(quick),"landing did not convert sand");h.assertTrue(w.getBlockState(protectedPos).is(Blocks.SANDSTONE),"building damaged");
  h.runAfterDelay(625,()->{h.assertTrue(w.getBlockState(a).is(Blocks.SAND)&&w.getBlockState(b).is(Blocks.RED_SAND),"original sand not restored");h.succeed();});
 }
 @GameTest(template="empty")
 public static void sandKingDamageTypeRegistered(GameTestHelper h){
  var registry=h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
  h.assertTrue(registry.getHolderOrThrow(SandKingDamage.RAPID).is(net.minecraft.tags.DamageTypeTags.BYPASSES_COOLDOWN),"rapid claws still lose hits to vanilla cooldown");h.succeed();
 }
 private static <T extends Mob> T mob(GameTestHelper h,EntityType<T> type,Vec3 p){
  T mob=type.create(h.getLevel());if(mob==null)throw new IllegalStateException("Entity creation failed");
  mob.moveTo(p);mob.setNoAi(true);mob.setNoGravity(true);mob.setPersistenceRequired();
  h.assertTrue(h.getLevel().addFreshEntity(mob),"Entity spawn failed");return mob;
 }
 private static Vec3 origin(GameTestHelper h){return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));}
 @GameTest(template="empty")
 public static void sandKingRetaliatesAgainstMeleeMob(GameTestHelper h){
  Vec3 pos=origin(h);var boss=mob(h,SandKingContent.BOSS.get(),pos);
  var zombie=mob(h,EntityType.ZOMBIE,pos.add(0,0,3.5));
  h.assertTrue(boss.getTarget()==null,"Unexpected initial target");
  h.assertTrue(boss.hurt(h.getLevel().damageSources().mobAttack(zombie),20),"Mob strike rejected");
  h.assertTrue(boss.getTarget()==zombie,"First melee attacker did not wake Sand King");
  h.runAfterDelay(5,()->{
   h.assertTrue(boss.getTarget()==zombie,"Melee attacker lost");
   h.assertTrue(boss.action()!=SandKingRules.IDLE,"No attack selected against melee mob");
   boss.discard();zombie.discard();h.succeed();
  });
 }
 @GameTest(template="empty")
 public static void sandKingRetaliatesAgainstProjectileOwner(GameTestHelper h){
  Vec3 pos=origin(h);var boss=mob(h,SandKingContent.BOSS.get(),pos);
  var skeleton=mob(h,EntityType.SKELETON,pos.add(0,0,5));
  var arrow=EntityType.ARROW.create(h.getLevel());if(arrow==null)throw new IllegalStateException("Arrow creation failed");
  h.assertTrue(boss.getParts()[0].hurt(h.getLevel().damageSources().arrow(arrow,skeleton),20),"Projectile hit on part rejected");
  h.assertTrue(boss.getTarget()==skeleton,"Did not retaliate against actual arrow owner");
  arrow.discard();boss.discard();skeleton.discard();h.succeed();
 }
 @GameTest(template="empty")
 public static void sandKingTracksSeveralMobAttackers(GameTestHelper h){
  Vec3 pos=origin(h);var boss=mob(h,SandKingContent.BOSS.get(),pos);
  var first=mob(h,EntityType.ZOMBIE,pos.add(0,0,3));
  var second=mob(h,EntityType.SKELETON,pos.add(3,0,0));
  h.assertTrue(boss.hurt(h.getLevel().damageSources().mobAttack(first),20),"First mob hit rejected");
  boss.invulnerableTime=0;
  h.assertTrue(boss.hurt(h.getLevel().damageSources().mobAttack(second),30),"Second mob hit rejected");
  h.assertTrue(boss.getTarget()==first,"Target changed immediately after a second attack");
  first.discard();
  h.runAfterDelay(5,()->{
   h.assertTrue(boss.getTarget()==second,"No failover to another known attacker");
   boss.discard();second.discard();h.succeed();
  });
 }
 private SandKingGameTests(){}
}
