package org.slavicmyths.navigation;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NavigationNetwork {
    public record Snapshot(CompoundTag state,boolean open) implements CustomPacketPayload {
        public Snapshot { state=state.copy(); }
        @Override public CompoundTag state(){return state.copy();}
        public static final Type<Snapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","navigation_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Snapshot> CODEC=StreamCodec.of(
            (b,p)->{b.writeNbt(p.state);b.writeBoolean(p.open);},b->{CompoundTag n=b.readNbt();if(n==null)throw new IllegalArgumentException("Missing navigation state");return new Snapshot(n,b.readBoolean());});
        @Override public Type<Snapshot> type(){return TYPE;}
    }
    public enum Action { OPEN,TRACK,STOP,HIDE,FORGET,CATEGORY,PREFERENCE,SAVE_NEARBY,CLEAR_OWNED }
    public record Request(Action action,UUID marker,int option,boolean enabled) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","navigation_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of(
            (b,p)->{b.writeEnum(p.action);b.writeUUID(p.marker);b.writeVarInt(p.option);b.writeBoolean(p.enabled);},
            b->new Request(b.readEnum(Action.class),b.readUUID(),b.readVarInt(),b.readBoolean()));
        @Override public Type<Request> type(){return TYPE;}
    }
    public static final class Synced extends Event {public final Snapshot payload;public Synced(Snapshot payload){this.payload=payload;}}
    public static void register(RegisterPayloadHandlersEvent e){
        var registrar=e.registrar("0.9.5");
        registrar.playToClient(Snapshot.TYPE,Snapshot.CODEC,(p,c)->NeoForge.EVENT_BUS.post(new Synced(p)));
        registrar.playToServer(Request.TYPE,Request.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer player)NavigationManager.request(player,p);});
    }
    public static void sync(ServerPlayer player,boolean open){
        // Deliberately serialize only this player's public state, never NavigationRecords/Assignment/Encounter.
        var state=NavigationRecords.get(player.serverLevel()).player(player.getUUID());
        PacketDistributor.sendToPlayer(player,new Snapshot(state.save(),open));
    }
    public static void request(Action action,UUID marker,int option,boolean enabled){
        PacketDistributor.sendToServer(new Request(action,marker==null?new UUID(0,0):marker,option,enabled));
    }
    private NavigationNetwork(){}
}
