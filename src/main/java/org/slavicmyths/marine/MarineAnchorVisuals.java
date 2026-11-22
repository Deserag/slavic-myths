package org.slavicmyths.marine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class MarineAnchorVisuals {
 public record Flight(int owner,ResourceLocation dimension,Vec3 end) implements CustomPacketPayload {
  public static final Type<Flight> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","marine_anchor_flight"));
  public static final StreamCodec<FriendlyByteBuf,Flight> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.owner);b.writeResourceLocation(p.dimension);b.writeDouble(p.end.x);b.writeDouble(p.end.y);b.writeDouble(p.end.z);},b->new Flight(b.readVarInt(),b.readResourceLocation(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble())));
  @Override public Type<Flight> type(){return TYPE;}
 }
 public static final class Received extends Event {public final Flight value;public Received(Flight value){this.value=value;}}
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToClient(Flight.TYPE,Flight.CODEC,(payload,context)->context.enqueueWork(()->NeoForge.EVENT_BUS.post(new Received(payload))));}
 public static void show(ServerPlayer player,Vec3 end){PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,new Flight(player.getId(),player.level().dimension().location(),end));}
}