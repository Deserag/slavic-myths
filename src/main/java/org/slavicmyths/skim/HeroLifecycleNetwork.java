package org.slavicmyths.skim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
/** Owner-only view. SavedData and all authoritative decisions remain on the server. */
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class HeroLifecycleNetwork {
 public record View(CompoundTag tag) implements CustomPacketPayload {
  public View {tag=tag.copy();}
  public static final Type<View> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","hero_lifecycle"));
  public static final StreamCodec<RegistryFriendlyByteBuf,View> CODEC=StreamCodec.of((b,p)->b.writeNbt(p.tag),b->new View(java.util.Objects.requireNonNull(b.readNbt())));
  public Type<View> type(){return TYPE;}
 }
 public static final class Received extends net.neoforged.bus.api.Event {public final CompoundTag tag;public Received(CompoundTag tag){this.tag=tag.copy();}}
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){e.registrar("1.9.7.31").playToClient(View.TYPE,View.CODEC,(p,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new Received(p.tag)));}
 public static void send(ServerPlayer p,CompoundTag n){PacketDistributor.sendToPlayer(p,new View(n));}
 private HeroLifecycleNetwork(){}
}
