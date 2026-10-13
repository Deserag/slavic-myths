package org.slavicmyths.bogatyr;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class BogatyrNetwork {
    public record Call(int action) implements CustomPacketPayload {
        public static final Type<Call> TYPE=new Type<>(BogatyrContent.id("bogatyr_call"));
        public static final StreamCodec<FriendlyByteBuf,Call> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.action),b->new Call(b.readVarInt()));
        public Type<Call> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("1.7.1").playToServer(Call.TYPE,Call.CODEC,(payload,c)->{if(!(c.player() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;if(payload.action==0){long now=p.level().getGameTime();var n=p.getPersistentData();if(n.getLong("BogatyrCallLast")+20>now)return;n.putLong("BogatyrCallLast",now);BogatyrLedger.call(p);}else if(payload.action==2){long now=p.level().getGameTime();var n=p.getPersistentData();if(n.getLong("BogatyrCallLast")+20>now)return;n.putLong("BogatyrCallLast",now);org.slavicmyths.marinka.WitchYard.call(p);}else if(payload.action==1){for(var b:p.level().getEntitiesOfClass(IdolishcheEntity.class,p.getBoundingBox().inflate(12),b->b.grabbed(p)))b.struggle(p);}});}
    private BogatyrNetwork(){}
}
