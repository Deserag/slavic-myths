package org.slavicmyths.gorynych;

import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class StormVfx {
    public record Impact(Vec3 at,List<Vec3> ends) implements CustomPacketPayload {
        public Impact{ends=List.copyOf(ends);if(ends.size()>StormCrossbow.MAX_TARGETS)throw new IllegalArgumentException("Too many storm arcs");}
        public static final Type<Impact> TYPE=new Type<>(GorynychContent.id("storm_impact"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Impact> CODEC=StreamCodec.of((b,p)->{write(b,p.at);b.writeVarInt(p.ends.size());p.ends.forEach(v->write(b,v));},b->{var at=read(b);int count=b.readVarInt();if(count<0||count>StormCrossbow.MAX_TARGETS)throw new IllegalArgumentException("Invalid storm arcs");var ends=new ArrayList<Vec3>();for(int i=0;i<count;i++)ends.add(read(b));return new Impact(at,ends);});
        @Override public Type<Impact> type(){return TYPE;}
    }
    private static void write(RegistryFriendlyByteBuf b,Vec3 v){b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);}
    private static Vec3 read(RegistryFriendlyByteBuf b){return new Vec3(b.readDouble(),b.readDouble(),b.readDouble());}
    public static final class Received extends net.neoforged.bus.api.Event{public final Impact impact;public Received(Impact impact){this.impact=impact;}}
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1.9.7").playToClient(Impact.TYPE,Impact.CODEC,(p,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new Received(p)));}
    public static void play(ServerLevel level,Vec3 at,List<Vec3> ends){PacketDistributor.sendToPlayersNear(level,null,at.x,at.y,at.z,48,new Impact(at,ends));}
    private StormVfx(){}
}
