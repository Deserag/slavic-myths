package org.slavicmyths.ships;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Intent-only packets. Never trust client-supplied cargo, coordinates or permissions. */
public final class RaftNetwork {
    public record OpenShip() implements CustomPacketPayload {
        public static final Type<OpenShip> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "raft_open_ship"));
        public static final StreamCodec<FriendlyByteBuf, OpenShip> CODEC =
                StreamCodec.of((buf, packet) -> {}, buf -> new OpenShip());
        @Override public Type<OpenShip> type() { return TYPE; }
    }

    public record PlaceEquipment(int raftId, int tile) implements CustomPacketPayload {
        public static final Type<PlaceEquipment> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "raft_place_equipment"));
        public static final StreamCodec<FriendlyByteBuf, PlaceEquipment> CODEC =
                StreamCodec.of((buf, packet) -> {
                    buf.writeVarInt(packet.raftId);
                    buf.writeVarInt(packet.tile);
                }, buf -> new PlaceEquipment(buf.readVarInt(), buf.readVarInt()));
        @Override public Type<PlaceEquipment> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2.6.1");
        registrar.playToServer(OpenShip.TYPE, OpenShip.CODEC, (packet, context) -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.isAlive()) return;
            if (!(player.getVehicle() instanceof RaftBoatEntity raft)) return;
            // Real server-side menu, not an inventory screenshot or a client-only overlay.
            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, p) -> new RaftOnboardMenu(windowId, playerInventory, raft),
                    Component.literal("Судовой журнал")), buf -> buf.writeVarInt(raft.getId()));
        });
        registrar.playToServer(PlaceEquipment.TYPE, PlaceEquipment.CODEC, (packet, context) -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() ||
                    !player.isShiftKeyDown()) return;
            if (!(player.level().getEntity(packet.raftId) instanceof RaftBoatEntity raft)) return;
            if (!raft.placeEquipment(player, InteractionHand.MAIN_HAND, packet.tile))
                player.displayClientMessage(Component.literal(
                        "Не удалось установить оборудование: прицельтесь в свободную клетку палубы"), true);
        });
    }

    private RaftNetwork() {}
}
