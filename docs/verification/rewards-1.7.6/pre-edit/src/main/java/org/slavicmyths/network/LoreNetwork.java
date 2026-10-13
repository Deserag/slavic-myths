package org.slavicmyths.network;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class LoreNetwork {
 public static void register(RegisterPayloadHandlersEvent event) {
  event.registrar("0.9.4").playToClient(MythPayloads.LoreOpen.TYPE, MythPayloads.LoreOpen.CODEC,
   (payload, context) -> NeoForge.EVENT_BUS.post(new ClientPayloadEvents.LoreOpened(payload)));
 }
 public static void open(ServerPlayer player,int mask) { PacketDistributor.sendToPlayer(player,new MythPayloads.LoreOpen(mask)); }
 private LoreNetwork() { }
}
