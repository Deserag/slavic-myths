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
public final class MarineWeaponControls {
 public record Secondary() implements CustomPacketPayload {
  public static final Type<Secondary> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","marine_secondary"));
  public static final StreamCodec<FriendlyByteBuf,Secondary> CODEC=StreamCodec.unit(new Secondary());
  @Override public Type<Secondary> type(){return TYPE;}
 }
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(Secondary.TYPE,Secondary.CODEC,(payload,context)->context.enqueueWork(()->{
  if(context.player() instanceof ServerPlayer player&&player.isAlive()&&!player.isSpectator())MarineWeaponAbilities.bubble(player);
 }));}
}