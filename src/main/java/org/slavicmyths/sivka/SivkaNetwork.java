package org.slavicmyths.sivka;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SivkaNetwork {
    public record Input(int entity,int sequence,float forward,float strafe,float yaw,boolean sprint,boolean jump,boolean down) implements CustomPacketPayload {
        public static final Type<Input> TYPE=new Type<>(SivkaContent.id("sivka_input"));
        public static final StreamCodec<FriendlyByteBuf,Input> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entity);b.writeVarInt(p.sequence);b.writeFloat(p.forward);b.writeFloat(p.strafe);b.writeFloat(p.yaw);b.writeBoolean(p.sprint);b.writeBoolean(p.jump);b.writeBoolean(p.down);},b->new Input(b.readVarInt(),b.readVarInt(),b.readFloat(),b.readFloat(),b.readFloat(),b.readBoolean(),b.readBoolean(),b.readBoolean()));
        public Type<Input> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("1.6").playToServer(Input.TYPE,Input.CODEC,(i,c)->{
        if(!(c.player() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||!(p.getVehicle() instanceof SivkaEntity h)||h.getId()!=i.entity||!h.owns(p)||!Float.isFinite(i.forward)||!Float.isFinite(i.strafe)||!Float.isFinite(i.yaw)||Math.abs(i.forward)>1||Math.abs(i.strafe)>1)return;
        h.input(i);
    });}
    private SivkaNetwork(){}
}
