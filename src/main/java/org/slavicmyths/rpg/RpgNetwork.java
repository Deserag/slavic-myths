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
 public static void sync(ServerPlayer player) { PacketDistributor.sendToPlayer(player,new MythPayloads.RpgSync(presentation(player),player.experienceLevel)); }
 private static net.minecraft.nbt.CompoundTag presentation(ServerPlayer player){var d=PathData.data(player).copy();var gates=new net.minecraft.nbt.CompoundTag();for(String id:new String[]{"first_ritual","friend_domovoy","meet_leshy","thunder_stone"})gates.putBoolean(id,org.slavicmyths.progression.Knowledge.knows(player,id));d.put("KnownGates",gates);if(player.containerMenu instanceof RpgMenu menu&&menu.anvil)d.putInt("AnvilMode",menu.mode);return d;}
 public static void activate() { PacketDistributor.sendToServer(new MythPayloads.Activate()); }
 private RpgNetwork() { }
}
