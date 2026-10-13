package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.mor.*;
@GameTestHolder("slavicmyths_poloz") @PrefixGameTestTemplate(false)
public final class MorExposureTests {
 static final class Connection extends net.minecraft.network.Connection{private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}}
 static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl{Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}@Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}}
 static ServerPlayer player(GameTestHelper t){var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"MorExposure"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(4.5,2,4.5)));try{var f=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");f.setAccessible(true);f.setInt(p,0);}catch(Exception e){throw new RuntimeException(e);}return p;}
 static void sample(ServerPlayer p,int second){p.tickCount=second*20;p.invulnerableTime=0;MorBlockExposure.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));}
 @GameTest(template="empty",timeoutTicks=30) public static void actualContactThresholds(GameTestHelper t){var p=player(t);t.setBlock(new BlockPos(4,1,4),MorBlocks.BLOCKS.get("dirt").get());float hp=p.getHealth();sample(p,1);sample(p,2);sample(p,3);t.assertTrue(!p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&p.getHealth()==hp,"no early effect");sample(p,4);t.assertTrue(p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&p.getHealth()==hp,"3-second grace");sample(p,5);t.assertTrue(p.getHealth()==hp,"no early DOT");sample(p,6);t.assertTrue(p.getHealth()==hp-1,"5-second DOT");p.moveTo(t.absoluteVec(new Vec3(16,3,16)));sample(p,7);t.assertTrue(!p.getPersistentData().contains("MorBlockSeconds"),"exit reset");p.discard();t.succeed();}
 @GameTest(template="empty",timeoutTicks=30) public static void multipleBlocksAndCreative(GameTestHelper t){var p=player(t);for(int x=3;x<=5;x++)for(int z=3;z<=5;z++)t.setBlock(new BlockPos(x,1,z),MorBlocks.BLOCKS.get("dirt").get());float hp=p.getHealth();for(int i=1;i<=6;i++)sample(p,i);t.assertTrue(p.getHealth()==hp-1,"nine blocks single DOT");p.removeAllEffects();p.setGameMode(GameType.CREATIVE);for(int i=7;i<=13;i++)sample(p,i);t.assertTrue(p.getHealth()==hp-1&&!p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"creative immune");p.discard();t.succeed();}
}
