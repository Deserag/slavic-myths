package org.slavicmyths.rpg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Only selection/activation intent crosses C2S. No client damage, inventory or cooldown values. */
public final class RareRuneNetwork {
    public record Intent(boolean cycle) implements CustomPacketPayload {
        public static final Type<Intent> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","rare_rune_intent"));
        public static final StreamCodec<FriendlyByteBuf,Intent> CODEC=StreamCodec.of((b,i)->b.writeBoolean(i.cycle),b->new Intent(b.readBoolean()));
        public Type<Intent> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("1.3.4.3").playToServer(Intent.TYPE,Intent.CODEC,(i,c)->{if(c.player() instanceof ServerPlayer p)RareRuneRuntime.intent(p,i.cycle());});}
    public static void send(boolean cycle){net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Intent(cycle));}
    private RareRuneNetwork(){}
}
