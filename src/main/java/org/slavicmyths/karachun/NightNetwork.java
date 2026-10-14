package org.slavicmyths.karachun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
public final class NightNetwork {
 public record Action(int action) implements CustomPacketPayload{public static final Type<Action> TYPE=new Type<>(NightContent.id("night192_action"));public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.action),b->new Action(b.readVarInt()));public Type<Action> type(){return TYPE;}}
 public record View(CompoundTag tag) implements CustomPacketPayload{public View{tag=tag.copy();}public static final Type<View> TYPE=new Type<>(NightContent.id("night192_view"));public static final StreamCodec<RegistryFriendlyByteBuf,View> CODEC=StreamCodec.of((b,p)->b.writeNbt(p.tag),b->new View(java.util.Objects.requireNonNull(b.readNbt())));public Type<View> type(){return TYPE;}}
 public static final class Received extends net.neoforged.bus.api.Event{public final CompoundTag tag;public Received(CompoundTag n){tag=n;}}
 public record Pose(int entity,int action,int combo,int duration) implements CustomPacketPayload{public static final Type<Pose> TYPE=new Type<>(NightContent.id("night192_pose"));public static final StreamCodec<RegistryFriendlyByteBuf,Pose> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entity);b.writeVarInt(p.action);b.writeVarInt(p.combo);b.writeVarInt(p.duration);},b->new Pose(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));public Type<Pose> type(){return TYPE;}}
 public static final class PoseReceived extends net.neoforged.bus.api.Event{public final Pose pose;public PoseReceived(Pose p){pose=p;}}
 public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){var r=e.registrar("1.9.2");r.playToServer(Action.TYPE,Action.CODEC,(a,c)->{if(c.player() instanceof ServerPlayer p)NightCombat.action(p,a.action);});r.playToClient(View.TYPE,View.CODEC,(v,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new Received(v.tag)));r.playToClient(Pose.TYPE,Pose.CODEC,(v,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new PoseReceived(v)));}
 public static void pose(ServerPlayer p,int action,int combo,int duration){PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Pose(p.getId(),action,combo,duration));}
 public static void send(ServerPlayer p){var n=NightLedger.get(p.serverLevel()).player(p.getUUID()).copy();n.remove("Pending");n.remove("Souls");n.putLong("Now",NightCombat.now(p));n.putDouble("Waning",NightStatus.value(p,"Waning"));n.putDouble("Fear",NightStatus.visualFear(p));n.putBoolean("Night",NightStatus.night(p));n.putBoolean("WingEquipped",!org.slavicmyths.item.FolkAccessoryItem.equipped(p,NightContent.WING.get()).isEmpty());PacketDistributor.sendToPlayer(p,new View(n));}
 private NightNetwork(){}
}
