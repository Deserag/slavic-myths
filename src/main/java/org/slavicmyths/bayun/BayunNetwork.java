package org.slavicmyths.bayun;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slavicmyths.tales.*;

public final class BayunNetwork {
    public record Dialogue(int entity,UUID token,int trust,String answer,boolean open) implements CustomPacketPayload {
        public static final Type<Dialogue> TYPE=new Type<>(BayunContent.id("bayun_dialogue"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Dialogue> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entity);b.writeUUID(p.token);b.writeVarInt(p.trust);b.writeUtf(p.answer,128);b.writeBoolean(p.open);},b->new Dialogue(b.readVarInt(),b.readUUID(),b.readVarInt(),b.readUtf(128),b.readBoolean()));
        @Override public Type<Dialogue> type(){return TYPE;}
    }
    public record Choice(int entity,UUID token,int sequence,int option) implements CustomPacketPayload {
        public static final Type<Choice> TYPE=new Type<>(BayunContent.id("bayun_choice"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Choice> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entity);b.writeUUID(p.token);b.writeVarInt(p.sequence);b.writeVarInt(p.option);},b->new Choice(b.readVarInt(),b.readUUID(),b.readVarInt(),b.readVarInt()));
        @Override public Type<Choice> type(){return TYPE;}
    }
    public record Charm(int entity,boolean active) implements CustomPacketPayload {
        public static final Type<Charm> TYPE=new Type<>(BayunContent.id("bayun_charm"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Charm> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entity);b.writeBoolean(p.active);},b->new Charm(b.readVarInt(),b.readBoolean()));
        @Override public Type<Charm> type(){return TYPE;}
    }
    public static final class DialogReceived extends Event {public final Dialogue value;public DialogReceived(Dialogue p){value=p;}}
    public static final class CharmReceived extends Event {public final Charm value;public CharmReceived(Charm p){value=p;}}
    public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1.6.9");r.playToClient(Dialogue.TYPE,Dialogue.CODEC,(p,c)->NeoForge.EVENT_BUS.post(new DialogReceived(p)));r.playToClient(Charm.TYPE,Charm.CODEC,(p,c)->NeoForge.EVENT_BUS.post(new CharmReceived(p)));r.playToServer(Choice.TYPE,Choice.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer player)choose(player,p);});}
    public static void charm(ServerPlayer p,int entity,boolean active){PacketDistributor.sendToPlayer(p,new Charm(entity,active));}
    public static void open(ServerPlayer p,BayunEntity cat){if(!cat.canTalk(p))return;var n=p.getPersistentData();UUID token=UUID.randomUUID();n.putUUID("BayunMenu",token);n.putInt("BayunMenuEntity",cat.getId());n.putInt("BayunMenuSequence",0);n.putLong("BayunMenuExpires",p.level().getGameTime()+1200);PacketDistributor.sendToPlayer(p,new Dialogue(cat.getId(),token,cat.trust(p),"bayun.dialogue.greeting",true));}
    public static void choose(ServerPlayer p,Choice request){var n=p.getPersistentData();long now=p.level().getGameTime();if(!n.hasUUID("BayunMenu")||!n.getUUID("BayunMenu").equals(request.token)||n.getInt("BayunMenuEntity")!=request.entity||request.sequence<=n.getInt("BayunMenuSequence")||request.sequence>n.getInt("BayunMenuSequence")+32||now>n.getLong("BayunMenuExpires")||now<n.getLong("BayunChoiceReady")||request.option<0||request.option>5)return;
        if(!(p.serverLevel().getEntity(request.entity) instanceof BayunEntity cat)||!cat.canTalk(p))return;org.slavicmyths.quest185.Quest185Story.talk(p,cat);n.putInt("BayunMenuSequence",request.sequence);n.putLong("BayunChoiceReady",now+10);String answer="bayun.dialogue.identity";
        if(request.option==0){var data=TaleData.get(p.serverLevel());var entry=TaleCatalog.entries().stream().filter(e->e.initial()==1&&e.chapter().equals("legends")&&data.stage(p.getUUID(),e.id())==0).findFirst();if(entry.isPresent()){TaleService.promote(p,entry.get().id(),1,false);answer="tales.entry."+entry.get().id().split(":",2)[1]+".rumor";}else answer="bayun.dialogue.no_lore";}
        else if(request.option==2){TaleService.hint(p,"slavicmyths:kot_bayun",p.level().dimension().location().toString(),cat.getUUID(),cat.home);TaleNetwork.send(p,TaleService.snapshot(p),false,true);answer="bayun.dialogue.places";}
        else if(request.option==5){org.slavicmyths.karachun.NightCombat.unlock(p,cat);answer="night192.npc_offer";}
        else if(request.option==4){org.slavicmyths.morozko.FrostSkills.unlock(p,cat);answer="frost191.npc_offer";}
        else if(request.option==3)n.remove("BayunMenu");
        PacketDistributor.sendToPlayer(p,new Dialogue(cat.getId(),request.token,cat.trust(p),answer,false));if(request.option!=3)p.displayClientMessage(Component.translatable(answer,p.getName()),false);
    }
    private BayunNetwork(){}
}
