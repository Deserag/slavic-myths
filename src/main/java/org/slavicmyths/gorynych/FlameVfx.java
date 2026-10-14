package org.slavicmyths.gorynych;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** One impact message, no scheduled server damage or recurring particle packets. */
public final class FlameVfx {
    public static final int DURATION=40,MAX_EMITTERS=32;
    public record Impact(Vec3 at,float radius) implements CustomPacketPayload {
        public static final Type<Impact> TYPE=new Type<>(GorynychContent.id("flame_impact"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Impact> CODEC=StreamCodec.of(
            (b,p)->{b.writeDouble(p.at.x);b.writeDouble(p.at.y);b.writeDouble(p.at.z);b.writeFloat(p.radius);},
            b->new Impact(new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat()));
        @Override public Type<Impact> type(){return TYPE;}
    }
    public static final class Received extends net.neoforged.bus.api.Event {
        public final Impact impact;
        public Received(Impact impact){this.impact=impact;}
    }
    public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1.9.7").playToClient(Impact.TYPE,Impact.CODEC,(p,c)->
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new Received(p)));
    }
    public static void play(ServerLevel level,Vec3 at,double radius){
        PacketDistributor.sendToPlayersNear(level,null,at.x,at.y,at.z,48,new Impact(at,(float)Math.min(3,Math.max(.5,radius))));
    }
    private FlameVfx(){}
}
