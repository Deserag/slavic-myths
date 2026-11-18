package org.slavicmyths.verify_native;
import net.minecraft.server.level.*;
final class NativePlayers {
 private static final class Connection extends net.minecraft.network.Connection {
  private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
  Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
  @Override public io.netty.channel.Channel channel(){return channel;}
 }
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
  Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener listener){}
 }
 static ServerPlayer create(ServerLevel world,String name){
  var p=new ServerPlayer(world.getServer(),world,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),name),ClientInformation.createDefault());
  p.connection=new Listener(p);p.setNoGravity(true);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);return p;
 }
 private NativePlayers(){}
}
