package org.slavicmyths.flight;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slavicmyths.network.MythPayloads;
public final class FlightNetwork {
 public static void register(RegisterPayloadHandlersEvent event) {
  event.registrar("0.9.4").playToServer(MythPayloads.FlightInput.TYPE,MythPayloads.FlightInput.CODEC,(payload,context)->{
   if(!payload.finite() || !(context.player() instanceof ServerPlayer player)
    || !(player.getVehicle() instanceof FlyingVessel vessel) || vessel.pilot()!=player
    || !vessel.owns(player) || !player.isAlive()) return;
   long now=player.level().getGameTime();
   if(player.getPersistentData().getLong("SlavicFlightPacket")>now)return;
   player.getPersistentData().putLong("SlavicFlightPacket",now+2);
   vessel.input(player,payload.forward(),payload.strafe(),payload.lift(),payload.yaw(),payload.brake());
   if(payload.cargo())vessel.openCargo(player);
  });
 }
 public static void send(float f,float s,float up,float yaw,boolean brake,boolean cargo) {
  PacketDistributor.sendToServer(new MythPayloads.FlightInput(f,s,up,yaw,brake,cargo));
 }
 private FlightNetwork() { }
}
