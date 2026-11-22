package org.slavicmyths.sisters;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class SistersArmorNetwork {
    public record Aura() implements CustomPacketPayload {
        public static final Type<Aura> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_armor_aura"));
        public static final StreamCodec<FriendlyByteBuf,Aura> CODEC=StreamCodec.unit(new Aura());
        @Override public Type<Aura> type(){return TYPE;}
    }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToServer(Aura.TYPE,Aura.CODEC,(payload,context)->context.enqueueWork(()->{
            if(context.player() instanceof ServerPlayer player)SistersArmorEvents.activateAura(player);
        }));
    }
    private SistersArmorNetwork(){}
}
