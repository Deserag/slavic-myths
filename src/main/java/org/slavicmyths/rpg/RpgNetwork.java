package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slavicmyths.network.ClientPayloadEvents;
import org.slavicmyths.network.MythPayloads;
public final class RpgNetwork {
 public static void register(RegisterPayloadHandlersEvent event) {
  var registrar=event.registrar("0.9.4");
  registrar.playToClient(MythPayloads.RpgSync.TYPE,MythPayloads.RpgSync.CODEC,
   (payload,context)->NeoForge.EVENT_BUS.post(new ClientPayloadEvents.RpgSynced(payload)));
  registrar.playToServer(MythPayloads.Activate.TYPE,MythPayloads.Activate.CODEC,(payload,context)->{
   if(context.player() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
    && PathData.ready(player,"request",4)) Abilities.activate(player);
  });
 }
 public static void sync(ServerPlayer player) { PacketDistributor.sendToPlayer(player,new MythPayloads.RpgSync(PathData.data(player),player.experienceLevel)); }
 public static void activate() { PacketDistributor.sendToServer(new MythPayloads.Activate()); }
 private RpgNetwork() { }
}
