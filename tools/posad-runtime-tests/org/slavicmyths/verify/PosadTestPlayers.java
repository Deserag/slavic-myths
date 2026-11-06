package org.slavicmyths.verify;
import net.minecraft.server.level.*;
/** Native server players with a local discard-only transport. FakePlayer disables riding/menu APIs. */
final class PosadTestPlayers {
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
 static ServerPlayer create(ServerLevel world,String name){
  var player=new ServerPlayer(world.getServer(),world,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),name),ClientInformation.createDefault());
  player.connection=new Listener(player);return player;
 }
 private PosadTestPlayers(){}
}
