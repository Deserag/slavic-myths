package org.slavicmyths.tales;

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

public final class TaleNetwork {
    public record Snapshot(CompoundTag data,boolean open,boolean full) implements CustomPacketPayload {
        public Snapshot {data=data.copy();} @Override public CompoundTag data(){return data.copy();}
        public static final Type<Snapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","tales_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Snapshot> CODEC=StreamCodec.of((b,p)->{b.writeNbt(p.data);b.writeBoolean(p.open);b.writeBoolean(p.full);},b->{var n=b.readNbt();if(n==null)throw new IllegalArgumentException("Missing tale snapshot");return new Snapshot(n,b.readBoolean(),b.readBoolean());});
        @Override public Type<Snapshot> type(){return TYPE;}
    }
    public record Read(String entry) implements CustomPacketPayload {
        public static final Type<Read> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","tales_read"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Read> CODEC=StreamCodec.of((b,p)->b.writeUtf(p.entry,128),b->new Read(b.readUtf(128)));
        @Override public Type<Read> type(){return TYPE;}
    }
    public static final class Received extends Event {public final Snapshot payload;public Received(Snapshot p){payload=p;}}
    public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1.6.9");r.playToClient(Snapshot.TYPE,Snapshot.CODEC,(p,c)->NeoForge.EVENT_BUS.post(new Received(p)));r.playToServer(Read.TYPE,Read.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer player)TaleService.read(player,p.entry);});}
    public static void send(ServerPlayer p,CompoundTag n,boolean open,boolean full){PacketDistributor.sendToPlayer(p,new Snapshot(n,open,full));}
    private TaleNetwork(){}
}
