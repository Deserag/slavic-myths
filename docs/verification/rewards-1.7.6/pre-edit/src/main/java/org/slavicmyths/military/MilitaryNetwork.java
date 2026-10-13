package org.slavicmyths.military;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slavicmyths.village.VillageRoles;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class MilitaryNetwork {
 public record Snapshot(CompoundTag data) implements CustomPacketPayload {public static final Type<Snapshot> TYPE=new Type<>(VillageRoles.id("military_snapshot"));public static final StreamCodec<FriendlyByteBuf,Snapshot> CODEC=StreamCodec.of((b,v)->b.writeNbt(v.data),b->new Snapshot(b.readNbt()));public Type<Snapshot> type(){return TYPE;}}
 public record Request(int menu,int action,String key) implements CustomPacketPayload {public static final Type<Request> TYPE=new Type<>(VillageRoles.id("military_action"));public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.menu);b.writeVarInt(v.action);b.writeUtf(v.key,192);},b->new Request(b.readVarInt(),b.readVarInt(),b.readUtf(192)));public Type<Request> type(){return TYPE;}}
 public static final class Synced extends Event {public final CompoundTag data;public Synced(CompoundTag data){this.data=data;}}
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1.2.3");r.playToClient(Snapshot.TYPE,Snapshot.CODEC,(p,c)->NeoForge.EVENT_BUS.post(new Synced(p.data)));r.playToServer(Request.TYPE,Request.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer player&&player.containerMenu instanceof MilitaryMenu menu&&menu.containerId==p.menu&&menu.stillValid(player)&&player.serverLevel().hasChunkAt(menu.pos)){MilitaryQuests.action(player,menu.pos,p.key,p.action);sync(player,menu.pos);}});}
 public static void sync(ServerPlayer p,BlockPos table){PacketDistributor.sendToPlayer(p,new Snapshot(MilitaryQuests.snapshot(p,table)));}
}
