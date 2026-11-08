package org.slavicmyths.ships;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Client clicks the DRAWN helm or ship side; the server never trusts the
 * requested attachment point or ship transform provided by the client.
 */
public final class FirstShipNetwork {
    public static final int BOARD = 1, HELM = 2;

    public record Command(int shipId, int action) implements CustomPacketPayload {
        public static final Type<Command> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "first_ship_action"));
        public static final StreamCodec<FriendlyByteBuf, Command> CODEC = StreamCodec.of(
                (buffer, packet) -> {
                    buffer.writeVarInt(packet.shipId);
                    buffer.writeVarInt(packet.action);
                },
                buffer -> new Command(buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<Command> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2.6.1").playToServer(Command.TYPE, Command.CODEC, (packet, context) -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !player.isAlive() || player.isSpectator()
                    || player.isPassenger() || player.isShiftKeyDown()) return;
            if (!(player.level().getEntity(packet.shipId()) instanceof FirstShipEntity ship)
                    || ship.isRemoved() || ship.level() != player.level()) return;
            if (player.distanceToSqr(ship) > 130D) return;
            if (packet.action() == HELM) ship.tryHelm(player);
            else if (packet.action() == BOARD) ship.tryBoard(player);
        });
    }
    private FirstShipNetwork() {}
}
