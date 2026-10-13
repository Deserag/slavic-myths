package org.slavicmyths.verify;
import java.util.*;import net.minecraft.core.BlockPos;import net.minecraft.gametest.framework.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.item.*;import net.minecraft.world.level.GameType;import net.minecraft.world.level.block.Blocks;import net.minecraft.world.phys.Vec3;import net.neoforged.neoforge.gametest.*;import org.slavicmyths.finist.*;import org.slavicmyths.skim.*;
@GameTestHolder("slavicmyths_feedback") @PrefixGameTestTemplate(false)
public final class FeedbackGameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
    }
    @SuppressWarnings("unchecked") private static Map<UUID,ServerPlayer> online(ServerPlayer p){try{var f=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");f.setAccessible(true);return (Map<UUID,ServerPlayer>)f.get(p.server.getPlayerList());}catch(Exception e){throw new RuntimeException(e);}}
    private static ServerPlayer player(GameTestHelper t){var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BogatyrTest"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.CREATIVE);try{var f=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");f.setAccessible(true);f.setInt(p,0);}catch(Exception e){throw new RuntimeException(e);}p.setNoGravity(true);p.moveTo(t.absoluteVec(new Vec3(16,2,18)));p.setOnGround(true);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(24);p.setHealth(24);w.addNewPlayer(p);online(p).put(p.getUUID(),p);return p;}

 static void check(GameTestHelper t,boolean ok,String message){t.assertTrue(ok,message);System.out.println("FEEDBACK_PASS "+message);}
 @GameTest(template="empty",timeoutTicks=2100) public static void ilya(GameTestHelper t){matrix(t,"ilya_muromets",0);}
 @GameTest(template="empty",timeoutTicks=2100) public static void dobrynya(GameTestHelper t){matrix(t,"dobrynya_nikitich",0);}
 @GameTest(template="empty",timeoutTicks=2100) public static void alyosha(GameTestHelper t){matrix(t,"alyosha_popovich",0);}
 @GameTest(template="empty",timeoutTicks=2100) public static void finist(GameTestHelper t){matrix(t,"finist",0);}
 static final String[] ENEMIES={"minecraft:zombie","minecraft:skeleton","minecraft:slime","slavicmyths:idolishche_poganoe","slavicmyths:marinka","slavicmyths:skim_beast","slavicmyths:kartaus","slavicmyths:kalin_tsar"};
 static void matrix(GameTestHelper t,String id,int index){
  if(index==ENEMIES.length){System.out.println("FEEDBACK_MATRIX_DONE "+id);t.succeed();return;}
  for(int x=0;x<48;x++)for(int z=0;z<48;z++)t.setBlock(new BlockPos(x,1,z),Blocks.STONE);
  var p=player(t);p.getInventory().add(new ItemStack(org.slavicmyths.registry.ModItems.LORE_BOOK.get()));
  check(t,HeroCalls.toggle(p,id),id+" real summon "+index);
  var hero=(Mob)HeroCalls.loaded(p,id);hero.moveTo(t.absoluteVec(new Vec3(17,2,18)));hero.setYRot(-90);hero.yBodyRot=-90;hero.setOnGround(true);
  var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse(ENEMIES[index]));
  var enemy=(Mob)type.create(t.getLevel());enemy.moveTo(t.absoluteVec(new Vec3(25,2,18)));enemy.setNoGravity(true);enemy.setNoAi(true);t.getLevel().addFreshEntity(enemy);
  if(enemy instanceof net.minecraft.world.entity.monster.Slime s)s.setSize(3,true);
  if(index<3){enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);enemy.setHealth(200);}
  if(enemy instanceof org.slavicmyths.bogatyr.IdolishcheEntity b){b.start(p,true);b.begin(org.slavicmyths.bogatyr.BogatyrRules.State.GUARD,1000);}
  if(enemy instanceof org.slavicmyths.marinka.MarinkaEntity b){b.owner=p.getUUID();b.active=true;b.begin(org.slavicmyths.marinka.WitchAction.IDLE,1000);}
  if(enemy instanceof SkimBeastEntity b)b.begin(SkimAction.IDLE,1000);
  if(enemy instanceof KartausEntity b)b.begin(FalconAction.IDLE,1000);
  if(enemy instanceof org.slavicmyths.kalin.KalinEntity b)b.begin(FalconAction.IDLE,1000);
  float hp=enemy.getHealth();var start=hero.position();
  t.runAfterDelay(70,()->{
   check(t,hero.getTarget()==enemy,id+" autonomous target "+ENEMIES[index]+" with creative owner");
   if(index<3&&!id.equals("alyosha_popovich"))check(t,hero.position().distanceToSqr(start)>.01,id+" navigation "+ENEMIES[index]);
   if(org.slavicmyths.bogatyr.HeroThreats.debugEnemy(enemy))check(t,org.slavicmyths.bogatyr.HeroThreats.debugOpponent(enemy,24)==hero,"debug opponent recognizes "+id+" against "+ENEMIES[index]);
  });
  t.runAfterDelay(210,()->{
   check(t,enemy.getHealth()<hp,id+" actual attack damages "+ENEMIES[index]);
   if(index<3&&id.equals("alyosha_popovich"))check(t,hero.position().distanceToSqr(start)>.01,id+" navigation after bow "+ENEMIES[index]);
   check(t,p.isCreative()&&hero.isAlive(),id+" survives fixture "+ENEMIES[index]);
   if(enemy instanceof KartausEntity b)b.cleanup();if(enemy instanceof org.slavicmyths.kalin.KalinEntity b)b.cleanup();
   hero.discard();enemy.discard();HeroCalls.map(p.serverLevel(),id).remove(p.getUUID());online(p).remove(p.getUUID());p.discard();
   matrix(t,id,index+1);
  });
 }
 @GameTest(template="empty",timeoutTicks=900) public static void debug_reciprocal(GameTestHelper t){reciprocal(t,3);}
 static void reciprocal(GameTestHelper t,int index){
  if(index==ENEMIES.length){t.succeed();return;}
  for(int x=0;x<48;x++)for(int z=0;z<48;z++)t.setBlock(new BlockPos(x,1,z),Blocks.STONE);
  var p=player(t);p.getInventory().add(new ItemStack(org.slavicmyths.registry.ModItems.LORE_BOOK.get()));
  check(t,HeroCalls.toggle(p,"dobrynya_nikitich"),"reciprocal summon "+index);
  var hero=(Mob)HeroCalls.loaded(p,"dobrynya_nikitich");hero.moveTo(t.absoluteVec(new Vec3(18,2,18)));hero.setOnGround(true);
  var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse(ENEMIES[index]));
  var enemy=(Mob)type.create(t.getLevel());enemy.moveTo(t.absoluteVec(new Vec3(26,2,18)));enemy.setOnGround(true);t.getLevel().addFreshEntity(enemy);
  t.runAfterDelay(100,()->{
   check(t,enemy.getTarget()==hero,"real debug controller targets creative-owned hero "+ENEMIES[index]);
   if(enemy instanceof KartausEntity b)b.cleanup();if(enemy instanceof org.slavicmyths.kalin.KalinEntity b)b.cleanup();
   hero.discard();enemy.discard();HeroCalls.map(p.serverLevel(),"dobrynya_nikitich").remove(p.getUUID());online(p).remove(p.getUUID());p.discard();reciprocal(t,index+1);
  });
 }
 static final String[] HEROES={"ilya_muromets","dobrynya_nikitich","alyosha_popovich","finist"};
 @GameTest(template="empty",timeoutTicks=1500) public static void pairs(GameTestHelper t){pair(t,0,1);}
 static void pair(GameTestHelper t,int a,int b){
  if(a==3){t.succeed();return;}
  for(int x=0;x<48;x++)for(int z=0;z<48;z++)t.setBlock(new BlockPos(x,1,z),Blocks.STONE);
  var p=player(t);p.getInventory().add(new ItemStack(org.slavicmyths.registry.ModItems.LORE_BOOK.get()));
  var heroes=new ArrayList<Mob>();var enemies=new ArrayList<Mob>();
  for(int n=0;n<2;n++){
   String id=HEROES[n==0?a:b];check(t,HeroCalls.toggle(p,id),"pair summon "+id);
   var hero=(Mob)HeroCalls.loaded(p,id);hero.moveTo(t.absoluteVec(new Vec3(12+n*14,2,18)));hero.setOnGround(true);heroes.add(hero);
   var enemy=EntityType.ZOMBIE.create(t.getLevel());enemy.moveTo(t.absoluteVec(new Vec3(12+n*14,2,24)));enemy.setNoAi(true);enemy.setNoGravity(true);enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);enemy.setHealth(200);t.getLevel().addFreshEntity(enemy);enemies.add(enemy);
  }
  check(t,HeroCalls.count(p)==2,"pair cap2 "+a+"/"+b);
  t.runAfterDelay(210,()->{
   for(int n=0;n<2;n++){var h=heroes.get(n);check(t,enemies.get(n).getHealth()<200,"pair both attack "+HEROES[n==0?a:b]);check(t,h.getHealth()==h.getMaxHealth(),"pair no friendly damage "+HEROES[n==0?a:b]);h.discard();enemies.get(n).discard();HeroCalls.map(p.serverLevel(),HEROES[n==0?a:b]).remove(p.getUUID());}
   online(p).remove(p.getUUID());p.discard();if(b==3)pair(t,a+1,a+2);else pair(t,a,b+1);
  });
 }
}
