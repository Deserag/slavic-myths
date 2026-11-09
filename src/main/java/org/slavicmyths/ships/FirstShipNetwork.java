package org.slavicmyths.ships;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * All helm actions are server-authoritative. A malicious client cannot steer
 * an unmounted ship, relocate an unrelated ship, or pack without a real key.
 */
public final class FirstShipNetwork {
    public static final int BOARD = 1, HELM = 2, MODULE_MAIN = 3, MODULE_OFF = 4;
    public static final int FORWARD = 5, BACKWARD = 6, LEFT = 7, RIGHT = 8, PACK = 9;
    public static final int RIG_CONFIGURE = 10, RIG_REMOVE = 11;
    public static final int CONTINUOUS_INPUT = 128; // high bit + four key bits

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
        event.registrar("2.6.2").playToServer(Command.TYPE, Command.CODEC, (packet, context) -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !player.isAlive() || player.isSpectator()) return;
            if (!(player.level().getEntity(packet.shipId()) instanceof FirstShipEntity ship)
                    || ship.isRemoved() || ship.level() != player.level()) return;
            // Native hull center is 5.5 blocks away from its sharpest extremity.
            if (player.distanceToSqr(ship) > 196D) return;
            int action = packet.action();
            if ((action & ~15) == CONTINUOUS_INPUT) {
                ship.setVoyageControl(player, action & 15);
                return;
            }
            if (action >= FORWARD && action <= RIGHT) {
                // Deprecated discrete actions are only used by old fixtures
                // and intentionally no longer sent by the current client.
                ship.helmStep(player, action);
                return;
            }
            if (player.isPassenger()) return;
            if (action == RIG_CONFIGURE || action == RIG_REMOVE) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "Оснащение меняется в корабельном верстаке: вкладка «Оснащение»"), true);
                return;
            }
            if (action == HELM && !player.isShiftKeyDown()) ship.tryHelm(player);
            else if (action == BOARD && !player.isShiftKeyDown()) ship.tryBoard(player);
            else if (action == PACK && player.isShiftKeyDown()
                    && (player.getItemInHand(InteractionHand.MAIN_HAND).is(RaftContent.SHIPYARD_KEY.get())
                        || player.getItemInHand(InteractionHand.OFF_HAND).is(RaftContent.SHIPYARD_KEY.get())))
                ship.packWithKey(player);
            // MODULE_* retained as historical protocol ids, but real chests
            // now use normal vanilla right-click and physical BlockEntities.
        });
    }

    private FirstShipNetwork() {}
}
