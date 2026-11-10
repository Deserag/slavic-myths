package org.slavicmyths.viy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ViyRewardNetwork {
    public record Summon() implements CustomPacketPayload {
        public static final Type<Summon> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","viy_summon"));
        public static final StreamCodec<FriendlyByteBuf,Summon> CODEC=StreamCodec.of((b,p)->{},b->new Summon());
        @Override public Type<Summon> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){
        e.registrar("1").playToServer(Summon.TYPE,Summon.CODEC,(request,context)->{
            if(context.player() instanceof ServerPlayer p)ViyRewardAbilities.summon(p);
        });
    }
    private ViyRewardNetwork(){}
}
