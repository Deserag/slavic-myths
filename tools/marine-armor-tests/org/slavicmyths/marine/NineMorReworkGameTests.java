package org.slavicmyths.marine;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.ninemor.*;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class NineMorReworkGameTests {
 static net.minecraft.server.level.ServerPlayer player(GameTestHelper t,String name,Vec3 pos){var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(t.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),name));p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.moveTo(pos);p.setNoGravity(true);t.getLevel().addNewPlayer(p);return p;}
 @GameTest(template="empty",timeoutTicks=60)
 public static void headsLockDifferentPlayersAndCancelLostVictims(GameTestHelper t){
  var b=NineMorContent.BOSS.get().create(t.getLevel());b.moveTo(t.absoluteVec(new Vec3(8,30,8)));b.setNoAi(true);b.setNoGravity(true);t.getLevel().addFreshEntity(b);
  var a=player(t,"NineMorLeft",b.position().add(-16,0,24));var c=player(t,"NineMorRight",b.position().add(16,0,24));
  try{var tick=BossRetaliationGameTests.method(b.getClass(),"tickHeads",LivingEntity.class);for(int n=0;n<21;n++)tick.invoke(b,a);
   java.util.Set<Integer> targets=new java.util.HashSet<>();int active=0;int[] locked=new int[9];
   for(int h=0;h<9;h++){locked[h]=b.headTargetId(h);if(b.headMode(h)!=0){targets.add(locked[h]);active++;t.assertTrue(b.headTarget(h)!=null,"Head target does not resolve");}}
   t.assertTrue(active<=3&&targets.contains(a.getId())&&targets.contains(c.getId()),"Heads did not split between two players: "+targets);
   for(int n=0;n<8;n++)tick.invoke(b,c);
   for(int h=0;h<9;h++)if(locked[h]>=0&&b.headMode(h)!=0)t.assertTrue(b.headTargetId(h)==locked[h],"Head changed victim during windup");
   a.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);tick.invoke(b,c);
   for(int h=0;h<9;h++)t.assertTrue(b.headTargetId(h)!=a.getId(),"Head retained spectator victim");
  }catch(Exception e){throw new RuntimeException(e);}a.discard();c.discard();b.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void flightHasShortAirWindowAndRequiresActualLanding(GameTestHelper t){
  var b=NineMorContent.BOSS.get().create(t.getLevel());b.moveTo(t.absoluteVec(new Vec3(8,30,8)));var p=player(t,"NineMorFlight",b.position().add(0,0,28));
  try{var flight=BossRetaliationGameTests.method(b.getClass(),"flight",int.class);var tick=BossRetaliationGameTests.method(b.getClass(),"tickFlight",LivingEntity.class);
   var base=b.getClass().getDeclaredField("takeoffY");base.setAccessible(true);base.setDouble(b,b.getY());flight.invoke(b,1);
   for(int i=0;i<18;i++)tick.invoke(b,p);t.assertTrue(b.flightPhase()==2,"Takeoff did not finish");
   for(int i=0;i<40;i++)tick.invoke(b,p);t.assertTrue(b.flightPhase()==3&&!b.isNoGravity(),"Short hover did not begin descent");
   for(int i=0;i<100;i++)tick.invoke(b,p);t.assertTrue(b.flightPhase()==3,"Boss declared itself landed while still airborne");
   b.setOnGround(true);tick.invoke(b,p);t.assertTrue(b.flightPhase()==0&&!b.isNoGravity(),"Touchdown did not restore grounded phase");
   for(int i=0;i<240;i++)tick.invoke(b,p);t.assertTrue(b.flightPhase()==0,"Melee window was too short");
  }catch(Exception e){throw new RuntimeException(e);}p.discard();b.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40)
 public static void rigOriginsStayFiniteAtLargeWorldCoordinates(GameTestHelper t){
  var b=NineMorContent.BOSS.get().create(t.getLevel());b.moveTo(20000000,80,-20000000);b.setYRot(137);
  for(int h=0;h<9;h++){var mouth=b.headMouth(h);t.assertTrue(Double.isFinite(mouth.x)&&mouth.distanceToSqr(b.position())<400,"Invalid head origin");t.assertTrue(mouth.y>80&&mouth.y<95,"Head origin does not match grounded model height");}
  b.discard();t.succeed();
 }
}