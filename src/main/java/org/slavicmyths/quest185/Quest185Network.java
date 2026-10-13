package org.slavicmyths.quest185;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class Quest185Network {
 public record View(CompoundTag data) implements CustomPacketPayload{
  public View{data=data.copy();}@Override public CompoundTag data(){return data.copy();}
  public static final Type<View> TYPE=new Type<>(Quest185Content.id("quest185_view"));
  public static final StreamCodec<RegistryFriendlyByteBuf,View> CODEC=StreamCodec.of((b,p)->b.writeNbt(p.data),b->new View(java.util.Objects.requireNonNull(b.readNbt())));
  public Type<View> type(){return TYPE;}
 }
 public record Action(UUID token,int sequence,int action,int stage,int entity) implements CustomPacketPayload{
  public static final Type<Action> TYPE=new Type<>(Quest185Content.id("quest185_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((b,p)->{b.writeUUID(p.token);b.writeVarInt(p.sequence);b.writeVarInt(p.action);b.writeVarInt(p.stage);b.writeVarInt(p.entity);},b->new Action(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
  public Type<Action> type(){return TYPE;}
 }
 public static final class Received extends Event{public final View payload;public Received(View v){payload=v;}}
 public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1.8.5");r.playToClient(View.TYPE,View.CODEC,(v,c)->NeoForge.EVENT_BUS.post(new Received(v)));r.playToServer(Action.TYPE,Action.CODEC,(a,c)->{if(c.player() instanceof ServerPlayer p)Quest185Sessions.action(p,a);});}
 public static void send(ServerPlayer p,CompoundTag n){PacketDistributor.sendToPlayer(p,new View(n));}
 private Quest185Network(){}
}
