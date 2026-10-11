package org.slavicmyths.hunt;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.volkolak.CursedVolkolak;
import static org.slavicmyths.hunt.BossRules.Move;

@GameTestHolder("slavicmyths_boss_rework") @PrefixGameTestTemplate(false)
public final class BossReworkGameTests {
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
 private static ServerPlayer actor(GameTestHelper t){
  var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BossProbe"),ClientInformation.createDefault());
  p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,20,3)));p.setNoGravity(true);
  for(int i=0;i<61;i++)p.tick();t.getLevel().addNewPlayer(p);return p;
 }
 private static WorldBoss boss(GameTestHelper t){var b=ModEntities.TUGARIN_ZMEY.get().create(t.getLevel());b.moveTo(t.absoluteVec(new Vec3(3,20,3)));b.setNoAi(true);b.setNoGravity(true);t.getLevel().addFreshEntity(b);return b;}
 private static TugarinCombat combat(WorldBoss b)throws Exception{var m=WorldBoss.class.getDeclaredMethod("tugarin");m.setAccessible(true);return (TugarinCombat)m.invoke(b);}
 private static void begin(WorldBoss b,Move move,LivingEntity e)throws Exception{var m=TugarinCombat.class.getDeclaredMethod("begin",Move.class,LivingEntity.class);m.setAccessible(true);m.invoke(combat(b),move,e);}
 private static void step(WorldBoss b,LivingEntity e)throws Exception{if(e.invulnerableTime>0)e.invulnerableTime--;b.tickCount++;combat(b).tick();}
 @GameTest(template="empty") public static void threeSpinHitsAndRecovery(GameTestHelper t)throws Exception{
  var b=boss(t);var e=EntityType.COW.create(t.getLevel());e.moveTo(b.position().add(4.5,0,0));e.setNoAi(true);e.setNoGravity(true);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);e.setHealth(200);t.getLevel().addFreshEntity(e);b.setTarget(e);combat(b).threat(e,1);begin(b,Move.SPIN,e);
  for(int i=0;i<15;i++)step(b,e);t.assertTrue(e.getHealth()==200,"Spin hit before windup");
  step(b,e);t.assertTrue(e.getHealth()==184,"First circular hit");
  for(int i=0;i<11;i++)step(b,e);t.assertTrue(e.getHealth()==184,"Unbounded repeat damage");
  step(b,e);t.assertTrue(e.getHealth()==168,"Second revolution hit");
  for(int i=0;i<12;i++)step(b,e);t.assertTrue(e.getHealth()==152,"Third revolution hit");
  for(int i=0;i<12;i++)step(b,e);t.assertTrue(b.move()==Move.RECOVERY,"Missing braking window");
  for(int i=0;i<23;i++)step(b,e);t.assertTrue(b.move()==Move.RECOVERY&&e.getHealth()==152,"Recovery interrupted");t.succeed();
 }
 @GameTest(template="empty") public static void spinRange(GameTestHelper t)throws Exception{
  var b=boss(t);var e=EntityType.COW.create(t.getLevel());e.moveTo(b.position().add(0,0,5.5));e.setNoAi(true);t.getLevel().addFreshEntity(e);b.setTarget(e);combat(b).threat(e,1);begin(b,Move.SPIN,e);for(int i=0;i<16;i++)step(b,e);t.assertTrue(e.getHealth()==e.getMaxHealth(),"Spin outside arm reach");t.succeed();
 }
 @GameTest(template="empty") public static void spinCannotHitThroughWall(GameTestHelper t)throws Exception{
  var b=boss(t);var e=EntityType.COW.create(t.getLevel());e.moveTo(b.position().add(4,0,0));e.setNoAi(true);e.setNoGravity(true);t.getLevel().addFreshEntity(e);
  var base=b.blockPosition();for(int y=-2;y<=7;y++)for(int z=-3;z<=3;z++)t.getLevel().setBlock(base.offset(2,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
  b.setTarget(e);combat(b).threat(e,1);begin(b,Move.SPIN,e);for(int i=0;i<16;i++)step(b,e);
  t.assertTrue(e.getHealth()==e.getMaxHealth(),"Spin damaged target through a solid wall");t.succeed();
 }
 @GameTest(template="empty") public static void phaseTwoHalfDamageAndReload(GameTestHelper t){
  var a=boss(t);var b=boss(t);b.moveTo(b.position().add(12,0,0));a.tugarinState(WorldBoss.TugarinState.PHASE_ONE);b.tugarinState(WorldBoss.TugarinState.PHASE_TWO);
  float ah=a.getHealth(),bh=b.getHealth();a.hurt(a.damageSources().genericKill(),20);b.hurt(b.damageSources().genericKill(),20);
  t.assertTrue(Math.abs((ah-a.getHealth())/2-(bh-b.getHealth()))<.01,"Phase two reduction must be exactly 50% before armor");
  var n=new net.minecraft.nbt.CompoundTag();b.addAdditionalSaveData(n);var loaded=ModEntities.TUGARIN_ZMEY.get().create(t.getLevel());loaded.readAdditionalSaveData(n);
  float before=loaded.getHealth();loaded.hurt(loaded.damageSources().genericKill(),20);t.assertTrue(before-loaded.getHealth()==10,"Reduction lost after NBT load");t.succeed();
 }
 @GameTest(template="empty") public static void physicalDimensionsAndSeparateLegacyMob(GameTestHelper t){
  var b=boss(t);var n=ModEntities.NIGHTINGALE.get().create(t.getLevel());var c=ModEntities.CURSED_VOLKOLAK.get().create(t.getLevel());var old=ModEntities.VOLKOLAK.get().create(t.getLevel());
  t.assertTrue(b.getBbWidth()==3.5F&&b.getBbHeight()==4.85F,"Giant collision");
  t.assertTrue(n.getBbHeight()==1.85F&&n.getEyeHeight()<1.5,"Compact body eye height");
  t.assertTrue(c.getMaxHealth()==400&&c.getBbWidth()==1.65F&&old.getMaxHealth()==165,"Legacy mob or cursed anatomy replaced");t.succeed();
 }
 private static void armor(ServerPlayer p,boolean diamond){p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(diamond?Items.DIAMOND_HELMET:Items.IRON_HELMET));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(diamond?Items.DIAMOND_CHESTPLATE:Items.IRON_CHESTPLATE));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(diamond?Items.DIAMOND_LEGGINGS:Items.IRON_LEGGINGS));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(diamond?Items.DIAMOND_BOOTS:Items.IRON_BOOTS));p.doTick();}
 private static void cursedArmor(GameTestHelper t,boolean diamond)throws Exception{
  var p=actor(t);armor(p,diamond);var b=ModEntities.CURSED_VOLKOLAK.get().create(t.getLevel());b.moveTo(p.position().add(0,0,-2));b.setNoAi(true);b.setNoGravity(true);t.getLevel().addFreshEntity(b);
  var begin=CursedVolkolak.class.getDeclaredMethod("begin",CursedVolkolak.Move.class,LivingEntity.class);begin.setAccessible(true);begin.invoke(b,CursedVolkolak.Move.CLAW_L,p);
  var hit=CursedVolkolak.class.getDeclaredMethod("strike",LivingEntity.class,float.class,double.class,double.class,boolean.class);hit.setAccessible(true);float before=p.getHealth();
  t.assertTrue((boolean)hit.invoke(b,p,15F,2.8,75.0,true),"Native armored hit rejected");float damage=before-p.getHealth();
  System.out.println("CURSED_WEREWOLF_"+(diamond?"DIAMOND":"IRON")+"_CLAW_DAMAGE="+damage);t.assertTrue(damage>3&&damage<15,"Armor pipeline or threat lost: "+damage);t.succeed();
 }
 @GameTest(template="empty") public static void werewolfIronDamage(GameTestHelper t)throws Exception{cursedArmor(t,false);}
 @GameTest(template="empty") public static void werewolfDiamondDamage(GameTestHelper t)throws Exception{cursedArmor(t,true);}
 @GameTest(template="empty") public static void nightingaleCompactMeleeReach(GameTestHelper t)throws Exception{
  var b=ModEntities.NIGHTINGALE.get().create(t.getLevel());b.moveTo(t.absoluteVec(new Vec3(3,20,3)));b.setNoAi(true);b.setNoGravity(true);t.getLevel().addFreshEntity(b);
  var p=actor(t);p.moveTo(b.position().add(0,0,2));b.setTarget(p);
  var start=b.getClass().getDeclaredMethod("startAttack",int.class,int.class,int.class);start.setAccessible(true);start.invoke(b,b.QUICK,20,200);
  for(int i=0;i<10;i++){b.tickCount++;b.aiStep();}t.assertTrue(p.getHealth()==10,"Compact blade cannot reach its visible target");
  p.setHealth(20);p.invulnerableTime=0;p.moveTo(b.position().add(0,0,2.7));start.invoke(b,b.QUICK,20,200);
  for(int i=0;i<10;i++){b.tickCount++;b.aiStep();}t.assertTrue(p.getHealth()==20,"Compact knife still hits beyond 2.2 blocks");t.succeed();
 }
 @GameTest(template="empty") public static void nightingaleTransformationAndPersistence(GameTestHelper t){
  var b=ModEntities.NIGHTINGALE.get().create(t.getLevel());b.setHealth(125);b.aiStep();
  t.assertTrue(b.phase()==org.slavicmyths.bandit.NightingaleEntity.Phase.TRANSFORMING,"Missing phase transition");
  var n=new net.minecraft.nbt.CompoundTag();b.addAdditionalSaveData(n);var loaded=ModEntities.NIGHTINGALE.get().create(t.getLevel());loaded.readAdditionalSaveData(n);
  t.assertTrue(loaded.phase()==org.slavicmyths.bandit.NightingaleEntity.Phase.PHASE_TWO,"Saved phase replayed transformation");t.succeed();
 }
}
