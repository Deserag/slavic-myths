package org.slavicmyths.gorynych;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** At most two pending rounds per player; no world scans or persistent delayed shots. */
@EventBusSubscriber(modid="slavicmyths")
public final class CrossbowVolley {
 private record Round(ItemStack weapon,InteractionHand hand,ItemStack ammo,int mode,int lane,float speed,long due,net.minecraft.server.level.ServerLevel level,UUID volley){}
 private static final Map<ServerPlayer,List<Round>> PENDING=new WeakHashMap<>();
 public static boolean active(ServerPlayer p){return PENDING.containsKey(p);}
 static void enqueue(ServerPlayer p,ItemStack weapon,InteractionHand hand,ItemStack ammo,int mode,float speed,int delay,int lane,UUID volley){PENDING.computeIfAbsent(p,k->new ArrayList<>(2)).add(new Round(weapon,hand,ammo,mode,lane,speed,p.level().getGameTime()+delay,p.serverLevel(),volley));}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;var rounds=PENDING.get(p);if(rounds==null)return;
  var it=rounds.iterator();while(it.hasNext()){
   var r=it.next();if(!p.isAlive()||p.isSpectator()||p.serverLevel()!=r.level||p.getItemInHand(r.hand)!=r.weapon||r.weapon.isEmpty()){it.remove();continue;}
   if(p.level().getGameTime()<r.due)continue;it.remove();
   if(r.weapon.getItem() instanceof SerpentCrossbow bow)bow.fireRound(p.serverLevel(),p,r.hand,r.weapon,r.ammo,r.mode,r.speed,0,null,r.lane,r.volley);
  }if(rounds.isEmpty())PENDING.remove(p);
 }
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){PENDING.remove(e.getEntity());}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){PENDING.clear();}
 public record Toggle() implements CustomPacketPayload {
  public static final Type<Toggle> TYPE=new Type<>(GorynychContent.id("crossbow_volley"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Toggle> CODEC=StreamCodec.unit(new Toggle());
  public Type<Toggle> type(){return TYPE;}
 }
 @EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(Toggle.TYPE,Toggle.CODEC,(v,c)->{
   if(!(c.player() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||!p.isShiftKeyDown()||p.containerMenu!=p.inventoryMenu)return;
   var s=p.getMainHandItem();if(!(s.getItem() instanceof SerpentCrossbow bow)||!bow.special||active(p))return;
   long now=p.level().getGameTime();if(now<p.getPersistentData().getLong("CrossbowToggleReady"))return;p.getPersistentData().putLong("CrossbowToggleReady",now+5);
   var n=SerpentCrossbow.state(s);n.putBoolean("Burst",!n.getBoolean("Burst"));SerpentCrossbow.write(s,n);
   p.displayClientMessage(GorynychContent.text(n.getBoolean("Burst")?"volley_burst":"volley_fan"),true);
  });}
 }
 private CrossbowVolley(){}
}
