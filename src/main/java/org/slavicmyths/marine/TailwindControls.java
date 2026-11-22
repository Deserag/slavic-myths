package org.slavicmyths.marine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class TailwindControls {
 public record Dash() implements CustomPacketPayload {
  public static final Type<Dash> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","tailwind_dash"));
  public static final StreamCodec<FriendlyByteBuf,Dash> CODEC=StreamCodec.unit(new Dash());
  public Type<Dash> type(){return TYPE;}
 }
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(Dash.TYPE,Dash.CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer player)MarineAccessories.dash(player);}));}
}
