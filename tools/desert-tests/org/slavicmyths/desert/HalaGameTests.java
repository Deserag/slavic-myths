package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class HalaGameTests {
 private static <T extends Mob> T mob(GameTestHelper h,EntityType<T> type,Vec3 p){T m=type.create(h.getLevel());if(m==null)throw new IllegalStateException("spawn failed");m.moveTo(p);m.setNoAi(true);m.setNoGravity(true);m.setPersistenceRequired();h.assertTrue(h.getLevel().addFreshEntity(m),"spawn rejected");return m;}
 private static Vec3 origin(GameTestHelper h){return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));}
 @GameTest(template="empty") public static void halaV2Registration(GameTestHelper h){
  var q=HalaContent.BOSS.get().create(h.getLevel());h.assertTrue(q!=null,"Hala not registered");h.assertTrue(q.getMaxHealth()==800,"HP");h.assertTrue(q.getParts().length>=12,"multipart missing");h.assertTrue(q.pose(0).length==2781*24,"V2 geometry lost");q.discard();h.succeed();
 }
 @GameTest(template="empty") public static void halaProjectileRetaliationAndLock(GameTestHelper h){
  var pos=origin(h);var q=mob(h,HalaContent.BOSS.get(),pos);var a=mob(h,EntityType.SKELETON,pos.add(0,0,6));var b=mob(h,EntityType.ZOMBIE,pos.add(6,0,0));var arrow=EntityType.ARROW.create(h.getLevel());
  h.assertTrue(arrow!=null,"arrow");h.assertTrue(q.getParts()[0].hurt(h.getLevel().damageSources().arrow(arrow,a),20),"part damage");h.assertTrue(q.getTarget()==a,"retaliation before combat");
  q.action(HalaRules.BEAM);q.invulnerableTime=0;q.hurt(h.getLevel().damageSources().mobAttack(b),50);q.setTarget(b);h.assertTrue(q.getTarget()==a,"target lock failed");arrow.discard();q.discard();a.discard();b.discard();h.succeed();
 }
 @GameTest(template="empty") public static void halaBurnStages(GameTestHelper h){
  var e=mob(h,EntityType.ZOMBIE,origin(h));SolarBurnEffect.apply(e,20);h.assertTrue(SolarBurnEffect.stage(e)==1,"stage I");SolarBurnEffect.apply(e,30);h.assertTrue(SolarBurnEffect.stage(e)==2,"stage II");SolarBurnEffect.apply(e,40);h.assertTrue(SolarBurnEffect.stage(e)==3,"stage III");h.assertTrue(HalaRules.outgoing(3,.1F)==0&&HalaRules.outgoing(2,.1F)<1,"damage rule");h.assertTrue(HalaRules.cool(90,true)<HalaRules.cool(90,false),"water cooling");e.discard();h.succeed();
 }

 @GameTest(template="empty") public static void halaWaterLowersBurn(GameTestHelper h){
  var pos=origin(h);var e=mob(h,EntityType.ZOMBIE,pos);SolarBurnEffect.apply(e,90);
  h.getLevel().setBlockAndUpdate(BlockPos.containing(pos),net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
  h.runAfterDelay(45,()->{h.assertTrue(SolarBurnEffect.stage(e)<3,"water failed to lower burn stage");e.discard();h.succeed();});
 }

 private static final class ProbeConnection extends net.minecraft.network.Connection {
  private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
  ProbeConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
  @Override public io.netty.channel.Channel channel(){return channel;}
 }
 private static final class ProbeListener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
  ProbeListener(net.minecraft.server.level.ServerPlayer p){super(p.getServer(),new ProbeConnection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
 }
 @GameTest(template="empty") public static void halaPlayerRetaliationAndBurnDamage(GameTestHelper h){
  var w=h.getLevel();var p=new net.minecraft.server.level.ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"HalaProbe"),net.minecraft.server.level.ClientInformation.createDefault());
  p.connection=new ProbeListener(p);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.moveTo(origin(h));p.setNoGravity(true);
  var q=mob(h,HalaContent.BOSS.get(),p.position().add(0,0,5));
  h.assertTrue(q.getParts()[0].hurt(w.damageSources().playerAttack(p),20),"player attack not accepted");
  h.assertTrue(q.getTarget()==p,"player retaliation failed");q.discard();
  var cow=mob(h,EntityType.COW,p.position().add(0,0,3));cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);
  SolarBurnEffect.apply(p,50);h.assertTrue(SolarBurnEffect.stage(p)==2,"player stage II");
  cow.invulnerableTime=0;cow.hurt(w.damageSources().playerAttack(p),12);h.assertTrue(Math.abs(cow.getHealth()-91)<.1,"stage II did not reduce native damage");
  SolarBurnEffect.apply(p,40);h.assertTrue(SolarBurnEffect.stage(p)==3,"player stage III");
  int misses=0;p.getRandom().setSeed(6123);
  for(int i=0;i<24;i++){cow.invulnerableTime=0;float before=cow.getHealth();cow.hurt(w.damageSources().playerAttack(p),1);if(cow.getHealth()==before)misses++;}
  h.assertTrue(misses>0&&misses<24,"stage III must sometimes miss, not always");cow.discard();h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=110) public static void halaBeamDamagesMob(GameTestHelper h){
  var pos=origin(h).add(0,24,0);var q=mob(h,HalaContent.BOSS.get(),pos);var cow=mob(h,EntityType.COW,pos.add(0,0,11));
  cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);
  q.setTarget(cow);q.begin(HalaRules.BEAM,cow);
  h.runAfterDelay(72,()->{h.assertTrue(cow.getHealth()<100&&SolarBurnEffect.stage(cow)>0,"native solar beam missed stationary hostile mob");q.discard();cow.discard();h.succeed();});
 }
}
