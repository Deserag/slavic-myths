package org.slavicmyths.rpg.classes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public final class ClassNetwork {
    public record Intent(int action,String id,int slot) implements CustomPacketPayload {
        public static final Type<Intent> TYPE=new Type<>(ClassState.id("class_intent"));
        public static final StreamCodec<FriendlyByteBuf,Intent> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.action);b.writeUtf(p.id,64);b.writeVarInt(p.slot);},b->new Intent(b.readVarInt(),b.readUtf(64),b.readVarInt()));
        public Type<Intent> type(){return TYPE;}
    }
    public record Open() implements CustomPacketPayload {
        public static final Type<Open> TYPE=new Type<>(ClassState.id("class_open"));
        public static final StreamCodec<FriendlyByteBuf,Open> CODEC=StreamCodec.of((b,p)->{},b->new Open());
        public Type<Open> type(){return TYPE;}
    }
    public static final class OpenEvent extends Event {}
    public static void register(RegisterPayloadHandlersEvent event){var r=event.registrar("1.3.2.1");
        r.playToClient(Open.TYPE,Open.CODEC,(p,c)->NeoForge.EVENT_BUS.post(new OpenEvent()));
        r.playToServer(Intent.TYPE,Intent.CODEC,(i,c)->{if(c.player() instanceof ServerPlayer p)handle(p,i);});
    }
    public static boolean handle(ServerPlayer p,Intent i){
        if(!p.isAlive()||p.isSpectator()||i.id().length()>64||i.action()<0||i.action()>6||i.action()==4&&(i.slot()<0||i.slot()>2))return false;
        long now=ClassState.now(p);var temp=p.getPersistentData();String gate=i.action()==4?"ClassSkillRequest"+i.slot()+"Until":"ClassRequestUntil";if(temp.getLong(gate)>now)return false;temp.putLong(gate,now+2);
        return switch(i.action()){
            case 0 -> ClassState.choose(p,i.id());
            case 1 -> ClassState.learn(p,i.id());
            case 2 -> ClassState.equip(p,i.id(),i.slot());
            case 3 -> ClassState.advance(p,i.id());
            case 4 -> ClassRuntime.activate(p,i.slot());
            case 5 -> {ClassState.data(p).putBoolean("Hud",i.slot()!=0);ClassState.sync(p);yield true;}
            case 6 -> {open(p);yield true;}
            default -> false;
        };
    }
    public static void open(ServerPlayer p){ClassState.sync(p);PacketDistributor.sendToPlayer(p,new Open());}
    public static void send(int action,String id,int slot){PacketDistributor.sendToServer(new Intent(action,id,slot));}
    private ClassNetwork(){}
}
