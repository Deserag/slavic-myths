package org.slavicmyths.hunt;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.bandit.NightingaleEntity;
import org.slavicmyths.volkolak.CursedVolkolak;

/** Real ticking AI tests: never force begin/strike or hit the predator to wake it. */
@GameTestHolder("slavicmyths_boss_rework") @PrefixGameTestTemplate(false)
public final class CombatFeedbackGameTests {
 private static final class Connection extends net.minecraft.network.Connection {
  private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
  Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
  @Override public io.netty.channel.Channel channel(){return channel;}
 }
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
  Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
 }
 private static ServerPlayer actor(GameTestHelper t,int height){
  var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"PassiveProbe"),ClientInformation.createDefault());
  p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,height,3)));p.setNoGravity(true);
  p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400);p.setHealth(400);for(int i=0;i<61;i++)p.tick();t.getLevel().addNewPlayer(p);
  var at=p.blockPosition();for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)t.getLevel().setBlock(at.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
  return p;
 }
 private static void predator(GameTestHelper t,double range,int height,boolean roof){
  var p=actor(t,height);var anchor=p.position();var b=ModEntities.CURSED_VOLKOLAK.get().create(t.getLevel());b.moveTo(anchor.add(0,0,-range));t.getLevel().addFreshEntity(b);
  if(roof){var at=b.blockPosition();for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)t.getLevel().setBlock(at.offset(x,2,z),Blocks.STONE.defaultBlockState(),3);}
  Set<CursedVolkolak.Move> moves=new HashSet<>();int[] firstHit={-1};
  t.onEachTick(()->{moves.add(b.move());if(p.getHealth()<400&&firstHit[0]<0)firstHit[0]=(int)t.getTick();if(!p.isPassenger()){p.moveTo(anchor);p.setDeltaMovement(Vec3.ZERO);}});
  t.runAfterDelay(120,()->{System.out.println("PREDATOR range="+range+" roof="+roof+" firstHit="+firstHit[0]+" HP="+p.getHealth()+" moves="+moves+" finalRange="+b.distanceTo(p));
   t.assertTrue(firstHit[0]>=0&&firstHit[0]<80,"AI never attacked an unprovoking survival player: "+moves);
   t.assertTrue(moves.stream().anyMatch(m->m==CursedVolkolak.Move.BITE||m==CursedVolkolak.Move.CLAW_L||m==CursedVolkolak.Move.CLAW_R),"No ordinary pressure attack");
   if(roof)t.assertTrue(!b.upright(),"Stood through the roof");b.discard();p.discard();t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=150) public static void predatorAttacksPassivePlayerAtDeadZone(GameTestHelper t){predator(t,2.7,60,false);}
 @GameTest(template="empty",timeoutTicks=150) public static void predatorClosesSixBlockGap(GameTestHelper t){predator(t,6,90,false);}
 @GameTest(template="empty",timeoutTicks=150) public static void predatorKeepsAttackingUnderRoof(GameTestHelper t){predator(t,2.7,120,true);}
 @GameTest(template="empty",timeoutTicks=180) public static void nightingaleBreaksFiveSecondPressure(GameTestHelper t){
  var p=actor(t,160);var b=ModEntities.NIGHTINGALE.get().create(t.getLevel());b.moveTo(p.position().add(0,0,-2.7));t.getLevel().addFreshEntity(b);boolean[] broke={false};double[] separation={0};
  t.onEachTick(()->{if(t.getTick()<105){p.moveTo(b.position().add(0,0,2.7));p.setDeltaMovement(Vec3.ZERO);}if(b.action()==NightingaleEntity.BREAK_AWAY)broke[0]=true;if(t.getTick()>=108)separation[0]=Math.max(separation[0],b.distanceTo(p));});
  t.runAfterDelay(150,()->{System.out.println("NIGHT_PRESSURE broke="+broke[0]+" separation="+separation[0]);t.assertTrue(broke[0]&&separation[0]>4,"Five-second pressure failed to create distance");b.discard();p.discard();t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=230) public static void nightingaleHoversDivesAndRises(GameTestHelper t){
  var p=actor(t,200);var anchor=p.position();var b=ModEntities.NIGHTINGALE.get().create(t.getLevel());b.moveTo(anchor.add(0,0,-4));b.setHealth(120);t.getLevel().addFreshEntity(b);
  double[] highest={0};boolean[] dive={false},landed={false},rose={false};float[] hp={p.getHealth()};
  t.onEachTick(()->{p.moveTo(anchor);p.setDeltaMovement(Vec3.ZERO);double h=b.getY()-p.getY();highest[0]=Math.max(highest[0],h);if(b.action()==NightingaleEntity.AIR_DIVE)dive[0]=true;if(dive[0]&&hp[0]-p.getHealth()>=15.9F)landed[0]=true;if(landed[0]&&h>=4)rose[0]=true;hp[0]=p.getHealth();});
  t.runAfterDelay(210,()->{System.out.println("NIGHT_AIR height="+highest[0]+" dive="+dive[0]+" landed="+landed[0]+" rose="+rose[0]+" HP="+p.getHealth());t.assertTrue(highest[0]>=4&&highest[0]<=5.1&&dive[0]&&landed[0]&&rose[0],"Hover/dive/native damage/re-ascent cycle failed");b.discard();p.discard();t.succeed();});
 }
 @GameTest(template="empty") public static void serpentGuardIsVisibleAndHalvesNativeAttack(GameTestHelper t){
  var a=ModEntities.TUGARIN_ZMEY.get().create(t.getLevel());var b=ModEntities.TUGARIN_ZMEY.get().create(t.getLevel());
  a.getAttribute(Attributes.ARMOR).setBaseValue(0);b.getAttribute(Attributes.ARMOR).setBaseValue(0);b.tugarinState(WorldBoss.TugarinState.PHASE_TWO);
  var attacker=EntityType.COW.create(t.getLevel());float ah=a.getHealth(),bh=b.getHealth();a.hurt(a.damageSources().mobAttack(attacker),20);b.hurt(b.damageSources().mobAttack(attacker),20);
  t.assertTrue(ah-a.getHealth()==20&&bh-b.getHealth()==10&&b.hasEffect(ModEffects.SERPENT_GUARD),"Visible phase guard or actual damage reduction missing");
  var effect=b.getEffect(ModEffects.SERPENT_GUARD);t.assertTrue(effect.isVisible()&&effect.showIcon(),"Hidden guard effect");
  var n=new net.minecraft.nbt.CompoundTag();b.addAdditionalSaveData(n);var loaded=ModEntities.TUGARIN_ZMEY.get().create(t.getLevel());loaded.readAdditionalSaveData(n);t.assertTrue(loaded.hasEffect(ModEffects.SERPENT_GUARD),"Reload lost visible armor");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=180) public static void airborneNightingaleStillAttacksUnderCeiling(GameTestHelper t){
  var p=actor(t,240);var anchor=p.position();var at=p.blockPosition();for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)t.getLevel().setBlock(at.offset(x,2,z),Blocks.STONE.defaultBlockState(),3);
  var b=ModEntities.NIGHTINGALE.get().create(t.getLevel());b.moveTo(anchor.add(0,0,-4));b.setHealth(120);t.getLevel().addFreshEntity(b);
  t.onEachTick(()->{p.moveTo(anchor);p.setDeltaMovement(Vec3.ZERO);});
  t.runAfterDelay(160,()->{t.assertTrue(p.getHealth()<400&&b.getY()-p.getY()<1.2,"Flight stalled beneath a ceiling or passed through it");b.discard();p.discard();t.succeed();});
 }
}
