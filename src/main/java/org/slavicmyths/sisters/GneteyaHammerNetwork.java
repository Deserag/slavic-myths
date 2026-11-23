package org.slavicmyths.sisters;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client sends only activation intent; targets and damage are resolved on the server. */
public final class GneteyaHammerNetwork {
    public record BoneFracture() implements CustomPacketPayload {
        public static final Type<BoneFracture> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "gneteya_bone_fracture"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoneFracture> CODEC =
            StreamCodec.unit(new BoneFracture());

        @Override
        public Type<BoneFracture> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(BoneFracture.TYPE, BoneFracture.CODEC,
            (intent, context) -> {
                if (context.player() instanceof ServerPlayer player) {
                    GneteyaGreatHammer.boneFracture(player);
                }
            });
    }

    private GneteyaHammerNetwork() {}
}
