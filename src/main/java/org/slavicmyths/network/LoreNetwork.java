package org.slavicmyths.network;

import java.util.Optional;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/** One S2C integer only when the book is used. No client authority over discoveries. */
public final class LoreNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("slavicmyths", "lore"), () -> "1", "1"::equals, "1"::equals);
    public static void register() {
        CHANNEL.registerMessage(0, Open.class, (m, b) -> b.writeVarInt(m.mask), b -> new Open(b.readVarInt()),
                (m, ctx) -> {
                    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> org.slavicmyths.client.LoreScreen.open(m.mask)));
                    ctx.get().setPacketHandled(true);
                }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void open(ServerPlayerEntity player, int mask) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Open(mask));
    }
    private static final class Open {
        private final int mask;
        private Open(int mask) { this.mask = mask; }
    }
    private LoreNetwork() { }
}
