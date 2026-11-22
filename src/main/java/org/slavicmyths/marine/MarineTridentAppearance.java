package org.slavicmyths.marine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Vanilla trident pickup stacks are not synced to clients; transmit only their visual variant. */
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class MarineTridentAppearance {
 public static final String KEY="SlavicMarineTridentAppearance";
 public record Appearance(int entityId,int variant) implements CustomPacketPayload {
  public static final Type<Appearance> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","marine_trident_appearance"));
  public static final StreamCodec<FriendlyByteBuf,Appearance> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entityId());b.writeByte(p.variant());},b->new Appearance(b.readVarInt(),b.readUnsignedByte()));
  @Override public Type<Appearance> type(){return TYPE;}
 }
 public static final class Received extends Event {
  public final Appearance value;
  public Received(Appearance value){this.value=value;}
 }
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){
  event.registrar("1").playToClient(Appearance.TYPE,Appearance.CODEC,(payload,context)->
    context.enqueueWork(()->NeoForge.EVENT_BUS.post(new Received(payload))));
 }
 @EventBusSubscriber(modid="slavicmyths")
 public static final class Tracking {
  @SubscribeEvent public static void start(PlayerEvent.StartTracking event){
   if(!(event.getEntity() instanceof ServerPlayer player)||!(event.getTarget() instanceof ThrownTrident trident))return;
   var stack=trident.getWeaponItem();
   int variant=stack!=null&&stack.is(PharaohkaTrident.ITEM.get())?1:stack!=null&&(stack.is(MarineWeapons.HARPOON.get())||stack.is(MarineHarpoons.AMMO.get()))?2:0;
   if(variant!=0)PacketDistributor.sendToPlayer(player,new Appearance(trident.getId(),variant));
  }
 }
}