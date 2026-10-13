package org.slavicmyths.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Common wire values; no client classes or gameplay authority. */
public final class MythPayloads {
    public record LoreOpen(int mask) implements CustomPacketPayload {
        public static final Type<LoreOpen> TYPE = MythPayloads.type("lore_open");
        public static final StreamCodec<FriendlyByteBuf, LoreOpen> CODEC = StreamCodec.of(
                (buffer, value) -> buffer.writeVarInt(value.mask), buffer -> new LoreOpen(buffer.readVarInt()));
        @Override public Type<LoreOpen> type() { return TYPE; }
    }
    public record RpgSync(CompoundTag data, int xp) implements CustomPacketPayload {
        public static final Type<RpgSync> TYPE = MythPayloads.type("rpg_sync");
        public static final StreamCodec<FriendlyByteBuf, RpgSync> CODEC = StreamCodec.of(
                (buffer, value) -> { buffer.writeNbt(value.data); buffer.writeVarInt(value.xp); },
                buffer -> new RpgSync(buffer.readNbt(), buffer.readVarInt()));
        public RpgSync { data = data == null ? new CompoundTag() : data.copy(); }
        @Override public CompoundTag data() { return data.copy(); }
        @Override public Type<RpgSync> type() { return TYPE; }
    }
    public record Activate() implements CustomPacketPayload {
        public static final Type<Activate> TYPE = MythPayloads.type("rpg_activate");
        public static final StreamCodec<FriendlyByteBuf, Activate> CODEC = StreamCodec.unit(new Activate());
        @Override public Type<Activate> type() { return TYPE; }
    }
    public record FlightInput(float forward, float strafe, float lift, float yaw,
                              boolean brake, boolean cargo) implements CustomPacketPayload {
        public static final Type<FlightInput> TYPE = MythPayloads.type("flight_input");
        public static final StreamCodec<FriendlyByteBuf, FlightInput> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeFloat(value.forward); buffer.writeFloat(value.strafe);
                    buffer.writeFloat(value.lift); buffer.writeFloat(value.yaw);
                    buffer.writeBoolean(value.brake); buffer.writeBoolean(value.cargo);
                }, buffer -> new FlightInput(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                        buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean()));
        public boolean finite() {
            return Float.isFinite(forward) && Float.isFinite(strafe) && Float.isFinite(lift) && Float.isFinite(yaw);
        }
        @Override public Type<FlightInput> type() { return TYPE; }
    }
    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths", path));
    }
    private MythPayloads() { }
}
