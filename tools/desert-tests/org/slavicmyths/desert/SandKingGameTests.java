package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.*;
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
 private SandKingGameTests(){}
}
