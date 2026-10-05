package org.slavicmyths.verify;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.slavicmyths.network.MythPayloads;

/** Real wire codecs: byte ownership, truncation, empty payload and non-finite controls. */
public final class PortPayloadsHeadless {
    private static <T> T roundtrip(StreamCodec<FriendlyByteBuf, T> codec, T value) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            codec.encode(buffer, value);
            T decoded = codec.decode(buffer);
            if (buffer.isReadable()) throw new AssertionError("Unconsumed payload bytes");
            return decoded;
        } finally { buffer.release(); }
    }
    private static void require(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        require(roundtrip(MythPayloads.LoreOpen.CODEC, new MythPayloads.LoreOpen(0x7654321)).mask() == 0x7654321);
        require(roundtrip(MythPayloads.Activate.CODEC, new MythPayloads.Activate()).equals(new MythPayloads.Activate()));
        var tag = new CompoundTag(); tag.putString("Path", "hunter");
        var sync = new MythPayloads.RpgSync(tag, 42);
        tag.putString("Path", "mutated");
        var copy = sync.data(); copy.putString("Path", "also mutated");
        var decoded = roundtrip(MythPayloads.RpgSync.CODEC, sync);
        require(decoded.xp() == 42 && decoded.data().getString("Path").equals("hunter"));
        require(new MythPayloads.RpgSync(null, 0).data().isEmpty());
        var input = new MythPayloads.FlightInput(-1, .5F, 1, -179.5F, true, false);
        require(roundtrip(MythPayloads.FlightInput.CODEC, input).equals(input) && input.finite());
        for (float value : new float[] { Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY }) {
            require(!new MythPayloads.FlightInput(value, 0, 0, 0, false, false).finite());
            require(!new MythPayloads.FlightInput(0, value, 0, 0, false, false).finite());
            require(!new MythPayloads.FlightInput(0, 0, value, 0, false, false).finite());
            require(!new MythPayloads.FlightInput(0, 0, 0, value, false, false).finite());
        }
        var truncated = new FriendlyByteBuf(Unpooled.buffer());
        try {
            truncated.writeFloat(1);
            try { MythPayloads.FlightInput.CODEC.decode(truncated); throw new AssertionError("Accepted truncated input"); }
            catch (IndexOutOfBoundsException expected) { }
        } finally { truncated.release(); }
        System.out.println("PASS: payload codecs, copied RPG NBT, finite controls and truncation. No mod/network/game bootstrap.");
    }
}
