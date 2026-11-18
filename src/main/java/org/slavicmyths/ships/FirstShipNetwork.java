package org.slavicmyths.ships;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * All helm actions are server-authoritative. A malicious client cannot steer
 * an unmounted ship, relocate an unrelated ship, or pack without a real key.
 */
public final class FirstShipNetwork {
    public static final int BOARD = 1, HELM = 2, MODULE_MAIN = 3, MODULE_OFF = 4;
    public static final int FORWARD = 5, BACKWARD = 6, LEFT = 7, RIGHT = 8, PACK = 9;
    public static final int RIG_CONFIGURE = 10, RIG_REMOVE = 11;
    public static final int FORWARD_LEFT = 12, FORWARD_RIGHT = 13;
    public static final int BACKWARD_LEFT = 14, BACKWARD_RIGHT = 15, HELM_IDLE = 16;
    public static final int DISMOUNT_ACK = 17;
    public static final int CARGO_BASE = 1000;

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

    /**
     * Client-selected local block placement on an already detached ship.
     * The server always determines the actual item and rejects invalid
     * cells, unsupported blocks, protected stairs and excessive reach.
     */
    public record PlaceCargo(int shipId, int x, int y, int z, int hand)
            implements CustomPacketPayload {
        public static final Type<PlaceCargo> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths",
                        "first_ship_place_cargo"));
        public static final StreamCodec<FriendlyByteBuf, PlaceCargo> CODEC =
                StreamCodec.of((buffer, packet) -> {
                    buffer.writeVarInt(packet.shipId);
                    buffer.writeVarInt(packet.x);
                    buffer.writeVarInt(packet.y);
                    buffer.writeVarInt(packet.z);
                    buffer.writeVarInt(packet.hand);
                }, buffer -> new PlaceCargo(buffer.readVarInt(),
                        buffer.readVarInt(), buffer.readVarInt(),
                        buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<PlaceCargo> type() { return TYPE; }
    }

    public record UseMovingDoor(int shipId, int x, int y, int z)
            implements CustomPacketPayload {
        public static final Type<UseMovingDoor> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "first_ship_use_door"));
        public static final StreamCodec<FriendlyByteBuf, UseMovingDoor> CODEC =
                StreamCodec.of((buffer, packet) -> {
                    buffer.writeVarInt(packet.shipId);
                    buffer.writeVarInt(packet.x);
                    buffer.writeVarInt(packet.y);
                    buffer.writeVarInt(packet.z);
                }, buffer -> new UseMovingDoor(buffer.readVarInt(), buffer.readVarInt(),
                        buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<UseMovingDoor> type() { return TYPE; }
    }

    public record BreakMovingBlock(int shipId, int x, int y, int z)
            implements CustomPacketPayload {
        public static final Type<BreakMovingBlock> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "first_ship_break_block"));
        public static final StreamCodec<FriendlyByteBuf, BreakMovingBlock> CODEC =
                StreamCodec.of((buffer, packet) -> {
                    buffer.writeVarInt(packet.shipId);
                    buffer.writeVarInt(packet.x);
                    buffer.writeVarInt(packet.y);
                    buffer.writeVarInt(packet.z);
                }, buffer -> new BreakMovingBlock(buffer.readVarInt(), buffer.readVarInt(),
                        buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<BreakMovingBlock> type() { return TYPE; }
    }

    /** Boat tracking does not reliably stream continuous ship pose around dismount. */
    public record MotionPose(int shipId, double x, double y, double z, float yaw,
                             double speed, double turn)
            implements CustomPacketPayload {
        public static final Type<MotionPose> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "first_ship_motion_pose"));
        public static final StreamCodec<FriendlyByteBuf, MotionPose> CODEC = StreamCodec.of(
                (buffer, pose) -> {
                    buffer.writeVarInt(pose.shipId);
                    buffer.writeDouble(pose.x);
                    buffer.writeDouble(pose.y);
                    buffer.writeDouble(pose.z);
                    buffer.writeFloat(pose.yaw);
                    buffer.writeDouble(pose.speed);
                    buffer.writeDouble(pose.turn);
                }, buffer -> new MotionPose(buffer.readVarInt(), buffer.readDouble(),
                        buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
                        buffer.readDouble(), buffer.readDouble()));
        @Override public Type<MotionPose> type() { return TYPE; }
    }


    public static void sendMotionPose(FirstShipEntity ship) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(ship, new MotionPose(ship.getId(),
                ship.getX(), ship.getY(), ship.getZ(), ship.getYRot(),
                ship.serverMovingSpeed(), ship.serverMovingTurn()));
    }


    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2.6.3");
        registrar.playToClient(MotionPose.TYPE, MotionPose.CODEC, (pose, context) -> {
            context.enqueueWork(() -> {
                if (context.player().level().getEntity(pose.shipId()) instanceof FirstShipEntity ship)
                    ship.acceptCaptainMotionPose(pose.x(), pose.y(), pose.z(), pose.yaw(),
                            pose.speed(), pose.turn());
            });
        });
        registrar.playToServer(PlaceCargo.TYPE, PlaceCargo.CODEC, (packet, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)
                            || !player.isAlive() || player.isSpectator()
                            || packet.hand() < 0 || packet.hand() > 1) return;
                    if (!(player.level().getEntity(packet.shipId())
                            instanceof FirstShipEntity ship)
                            || ship.isRemoved() || !ship.isMoving()
                            || ship.level() != player.level()) return;
                    ship.placeMovingCargo(player,
                            new BlockPos(packet.x(), packet.y(), packet.z()),
                            packet.hand() == 0 ? InteractionHand.MAIN_HAND
                                    : InteractionHand.OFF_HAND);
                }));
        registrar.playToServer(UseMovingDoor.TYPE, UseMovingDoor.CODEC, (packet, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)
                            || !player.isAlive() || player.isSpectator()) return;
                    if (player.level().getEntity(packet.shipId()) instanceof FirstShipEntity ship
                            && ship.level() == player.level())
                        ship.useMovingDoor(player,
                                new BlockPos(packet.x(), packet.y(), packet.z()));
                }));
        registrar.playToServer(BreakMovingBlock.TYPE, BreakMovingBlock.CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)
                            || !player.isAlive()) return;
                    if (player.level().getEntity(packet.shipId()) instanceof FirstShipEntity ship
                            && ship.level() == player.level())
                        ship.breakMovingBlock(player,
                                new BlockPos(packet.x(), packet.y(), packet.z()));
                }));
        registrar.playToServer(Command.TYPE, Command.CODEC, (packet, context) ->
                context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !player.isAlive() || player.isSpectator()) return;
            if (!(player.level().getEntity(packet.shipId()) instanceof FirstShipEntity ship)
                    || ship.isRemoved() || ship.level() != player.level()) return;
            // Native hull center is 5.5 blocks away from its sharpest extremity.
            ShipBuildConfiguration configuration = ship.modularConfiguration();
            double reach = configuration == null ? 14D : configuration.length() / 2D + 7D;
            if (player.distanceToSqr(ship) > reach * reach) return;
            int action = packet.action();
            if ("true".equals(System.getenv("GITHUB_ACTIONS"))
                    && "github-hosted".equals(System.getenv("RUNNER_ENVIRONMENT"))
                    && action >= CARGO_BASE)
                System.out.println("SHIP_COMMAND_SERVER action=" + action
                        + " ship=" + ship.getId() + " player=" + player.position());
            if (action == DISMOUNT_ACK) {
                ship.confirmDismount(player);
                return;
            }
            if ((action >= FORWARD && action <= RIGHT)
                    || (action >= FORWARD_LEFT && action <= HELM_IDLE)) {
                ship.helmStep(player, action);
                return;
            }
            if (action >= CARGO_BASE && action < CARGO_BASE + 8192) {
                ship.openMovingCargo(player, action - CARGO_BASE);
                return;
            }
            if (player.isPassenger()) return;
            if (action == RIG_CONFIGURE || action == RIG_REMOVE) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "Оснащение меняется в корабельном верстаке: вкладка «Модификации»"), true);
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
        }));
    }

    private FirstShipNetwork() {}
}
