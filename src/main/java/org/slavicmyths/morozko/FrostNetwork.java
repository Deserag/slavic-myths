package org.slavicmyths.morozko;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class FrostNetwork {
 public record Action(int action) implements CustomPacketPayload{public static final Type<Action> TYPE=new Type<>(FrostContent.id("frost191_action"));public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.action),b->new Action(b.readVarInt()));public Type<Action> type(){return TYPE;}}
 public record View(CompoundTag tag) implements CustomPacketPayload{public View{tag=tag.copy();}public static final Type<View> TYPE=new Type<>(FrostContent.id("frost191_view"));public static final StreamCodec<RegistryFriendlyByteBuf,View> CODEC=StreamCodec.of((b,p)->b.writeNbt(p.tag),b->new View(java.util.Objects.requireNonNull(b.readNbt())));public Type<View> type(){return TYPE;}}
 public static final class Received extends net.neoforged.bus.api.Event{public final View view;public Received(View v){view=v;}}
 public record Stream(int entity) implements CustomPacketPayload{public static final Type<Stream> TYPE=new Type<>(FrostContent.id("frost191_stream"));public static final StreamCodec<RegistryFriendlyByteBuf,Stream> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.entity),b->new Stream(b.readVarInt()));public Type<Stream> type(){return TYPE;}}
 public static final class StreamReceived extends net.neoforged.bus.api.Event{public final int entity;public StreamReceived(int e){entity=e;}}
 public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1.9.1");r.playToServer(Action.TYPE,Action.CODEC,(a,c)->{if(c.player() instanceof ServerPlayer p)FrostSkills.action(p,a.action);});r.playToClient(View.TYPE,View.CODEC,(v,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new Received(v)));r.playToClient(Stream.TYPE,Stream.CODEC,(s,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new StreamReceived(s.entity)));}
 public static void send(ServerPlayer p){var n=FrostLedger.get(p.serverLevel()).player(p.getUUID()).copy();n.remove("Pending");n.putLong("Now",FrostSkills.now(p));n.putDouble("Cold",p.getPersistentData().getCompound(FrostCold.KEY).getDouble("Value"));n.putBoolean("MirrorEquipped",!org.slavicmyths.item.FolkAccessoryItem.equipped(p,FrostContent.MIRROR.get()).isEmpty());PacketDistributor.sendToPlayer(p,new View(n));}
 private FrostNetwork(){}
}
