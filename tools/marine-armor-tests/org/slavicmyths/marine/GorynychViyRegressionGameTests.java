package org.slavicmyths.marine;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.gorynych.*;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class GorynychViyRegressionGameTests {
 static void load(net.minecraft.server.level.ServerLevel w,net.minecraft.world.phys.AABB b){for(int x=((int)Math.floor(b.minX))>>4;x<=((int)Math.floor(b.maxX))>>4;x++)for(int z=((int)Math.floor(b.minZ))>>4;z<=((int)Math.floor(b.maxZ))>>4;z++)w.getChunk(x,z);}
 @GameTest(template="empty",timeoutTicks=80)
 public static void gorynychSurfacesThroughCaveAndCompletesSecondPhase(GameTestHelper t){
  var b=(Gorynych)BossRetaliationGameTests.boss(t,"zmey_gorynych");var record=new CompoundTag();record.putLong("Origin",t.absolutePos(new BlockPos(0,30,0)).asLong());record.putInt("Node",5);record.putInt("State",GorynychRules.GROUND);record.putFloat("HP",1501);b.configure(java.util.UUID.randomUUID(),record);
  b.moveTo(GorynychPlaces.node(record,5));var destination=GorynychPlaces.node(record,0);var targetBox=b.getBoundingBox().move(destination.subtract(b.position()));load(t.getLevel(),b.getBoundingBox().minmax(targetBox).inflate(1));
  var ground=BlockPos.containing(destination).below();t.getLevel().setBlock(ground,Blocks.STONE.defaultBlockState(),3);
  var ceiling=BlockPos.containing(b.position().add(0,30,0));t.getLevel().setBlock(ceiling,Blocks.STONE.defaultBlockState(),3);
  t.assertTrue(!GorynychPlaces.safe(t.getLevel(),b,destination),"Fixture does not reproduce blocked swept route");
  t.assertTrue(GorynychPlaces.safeDestination(t.getLevel(),b,destination),"Clear surface node incorrectly rejected");
  t.assertTrue(b.hurt(t.getLevel().damageSources().generic(),10),"Threshold hit was rejected");
  t.assertTrue(b.state()==GorynychRules.TRANSFORM,"Threshold did not start transformation");
  for(int n=0;n<=GorynychRules.TRANSFORM_TICKS&&b.state()==GorynychRules.TRANSFORM;n++)b.tick();
  t.assertTrue(b.state()==GorynychRules.AIR&&b.wings(),"Gorynych never entered second phase");
  t.assertTrue(b.position().distanceToSqr(destination)<1,"Gorynych remained underground");
  t.assertTrue(b.hurt(t.getLevel().damageSources().generic(),10),"Gorynych remains invulnerable after transition");
  t.assertTrue(t.getLevel().getBlockState(ceiling).is(Blocks.STONE),"Surfacing destroyed cave ceiling");b.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=80)
 public static void blockedSurfaceCannotCreatePermanentInvulnerability(GameTestHelper t){
  var b=(Gorynych)BossRetaliationGameTests.boss(t,"zmey_gorynych");var record=new CompoundTag();record.putBoolean("Standalone",true);record.putLong("Origin",t.absolutePos(new BlockPos(8,30,8)).asLong());record.putInt("Node",4);record.putInt("State",GorynychRules.TRANSFORM);record.putInt("Age",119);record.putFloat("HP",1500);b.configure(java.util.UUID.randomUUID(),record);b.moveTo(GorynychPlaces.node(record,4));
  for(int i=0;i<3;i++){var point=GorynychPlaces.node(record,i);load(t.getLevel(),b.getBoundingBox().move(point.subtract(b.position())).inflate(1));t.getLevel().setBlock(BlockPos.containing(point),Blocks.STONE.defaultBlockState(),3);}
  var initial=b.position();b.tick();t.assertTrue(b.state()==GorynychRules.AIR&&b.wings(),"Obstructed nodes trapped transformation");
  t.assertTrue(b.position().distanceToSqr(initial)<1,"Boss relocated into blocked surface");t.assertTrue(b.hurt(t.getLevel().damageSources().generic(),10),"Fallback phase is invulnerable");b.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void viyWitherNeverExceedsLevelTwo(GameTestHelper t){
  var b=BossRetaliationGameTests.boss(t,"viy");var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(t.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ViyCurseTest"));p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);
  try{var curse=BossRetaliationGameTests.method(b.getClass(),"curse",net.minecraft.server.level.ServerPlayer.class);
   for(int stage=1;stage<=6;stage++){curse.invoke(b,p);var effect=p.getEffect(net.minecraft.world.effect.MobEffects.WITHER);if(stage<3)t.assertTrue(effect==null,"Early curse unexpectedly inflicts Wither");else t.assertTrue(effect!=null&&effect.getAmplifier()==(stage==3?0:1),"Wither exceeds allowed stage: "+stage);}
  }catch(Exception e){throw new RuntimeException(e);}
  try(var icon=GorynychViyRegressionGameTests.class.getClassLoader().getResourceAsStream("assets/slavicmyths/textures/mob_effect/viy_gaze.png")){t.assertTrue(icon!=null,"Gaze effect icon is absent from resources");var bitmap=javax.imageio.ImageIO.read(icon);t.assertTrue(bitmap!=null&&bitmap.getWidth()==18&&bitmap.getHeight()==18,"Gaze effect icon is invalid");}catch(java.io.IOException e){throw new RuntimeException(e);}
  p.discard();b.discard();t.succeed();
 }
}