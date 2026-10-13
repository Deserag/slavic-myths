package org.slavicmyths.wolf;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slavicmyths.military.GreyWolfStory;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class WolfStoryNetwork {
 private record Session(UUID wolf,UUID nonce,long expires){}
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 public record View(CompoundTag data,boolean open) implements CustomPacketPayload {public static final Type<View> TYPE=new Type<>(id("wolf_story_view"));public static final StreamCodec<FriendlyByteBuf,View> CODEC=StreamCodec.of((b,v)->{b.writeNbt(v.data);b.writeBoolean(v.open);},b->new View(b.readNbt(),b.readBoolean()));public View{data=data==null?new CompoundTag():data.copy();}public Type<View> type(){return TYPE;}}
 public record Action(UUID wolf,UUID nonce,int quest,int action) implements CustomPacketPayload {public static final Type<Action> TYPE=new Type<>(id("wolf_story_action"));public static final StreamCodec<FriendlyByteBuf,Action> CODEC=StreamCodec.of((b,v)->{b.writeUUID(v.wolf);b.writeUUID(v.nonce);b.writeVarInt(v.quest);b.writeVarInt(v.action);},b->new Action(b.readUUID(),b.readUUID(),b.readVarInt(),b.readVarInt()));public Type<Action> type(){return TYPE;}}
 public record Ring() implements CustomPacketPayload {public static final Type<Ring> TYPE=new Type<>(id("grey_pack_activate"));public static final StreamCodec<FriendlyByteBuf,Ring> CODEC=StreamCodec.unit(new Ring());public Type<Ring> type(){return TYPE;}}
 public static final class Viewed extends Event {public final View view;public Viewed(View v){view=v;}}
 private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1.4.2");r.playToClient(View.TYPE,View.CODEC,(p,c)->net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new Viewed(p)));r.playToServer(Action.TYPE,Action.CODEC,(a,c)->{if(c.player() instanceof ServerPlayer p){var session=SESSIONS.get(p.getUUID());if(session==null||!session.wolf.equals(a.wolf)||!session.nonce.equals(a.nonce))return;if(session.expires<GreyWolfStory.now(p)){SESSIONS.remove(p.getUUID());sendClosed(p);return;}var actor=p.serverLevel().getEntity(a.wolf);if(!(actor instanceof GreyWolf wolf)||!valid(p,wolf)){SESSIONS.remove(p.getUUID());sendClosed(p);return;}boolean ok=GreyWolfStory.action(p,wolf,a.quest,a.action);if(!ok)GreyWolfStory.cue(p,"reject");sync(p,wolf,false,ok?a.action:-1);}});r.playToServer(Ring.TYPE,Ring.CODEC,(a,c)->{if(c.player() instanceof ServerPlayer p)org.slavicmyths.finist.GreyPackRing.activate(p);});}
 public static boolean valid(ServerPlayer p,GreyWolf wolf){return p.isAlive()&&!p.isSpectator()&&p.level()==wolf.level()&&p.distanceToSqr(wolf)<=64&&p.hasLineOfSight(wolf)&&WolfProgress.passed(p)&&wolf.state()==WolfTimeline.State.POST_TRIAL_NPC;}
 public static void open(ServerPlayer p,GreyWolf wolf){org.slavicmyths.quest185.Quest185Story.talk(p,wolf);if(!valid(p,wolf))return;GreyWolfStory.refreshTarget(p);GreyWolfStory.cue(p,"dialog_open");SESSIONS.put(p.getUUID(),new Session(wolf.getUUID(),UUID.randomUUID(),GreyWolfStory.now(p)+12000));sync(p,wolf,true,-2);}
 private static void sync(ServerPlayer p,GreyWolf wolf,boolean open,int action){var session=SESSIONS.get(p.getUUID());if(session==null)return;var data=GreyWolfStory.snapshot(p,wolf);data.putUUID("Nonce",session.nonce);data.putInt("Action",action);PacketDistributor.sendToPlayer(p,new View(data,open));}
 private static void sendClosed(ServerPlayer p){PacketDistributor.sendToPlayer(p,new View(new CompoundTag(),false));}
 public static void changed(ServerPlayer p){var session=SESSIONS.get(p.getUUID());if(session!=null&&p.serverLevel().getEntity(session.wolf) instanceof GreyWolf wolf&&valid(p,wolf))sync(p,wolf,false,-2);}
 public static void clear(ServerPlayer p){SESSIONS.remove(p.getUUID());}
 private WolfStoryNetwork(){}
}
