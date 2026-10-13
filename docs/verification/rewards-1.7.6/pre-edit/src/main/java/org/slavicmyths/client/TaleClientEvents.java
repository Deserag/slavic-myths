package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.tales.TaleNetwork;
import org.slavicmyths.bayun.BayunNetwork;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class TaleClientEvents {
    private static int charmer=-1;private static long charmExpires;
    private static final java.util.Map<java.util.UUID,BayunSong> songs=new java.util.HashMap<>();
    @SubscribeEvent public static void book(TaleNetwork.Received e){TaleScreen.receive(e.payload);}
    @SubscribeEvent public static void dialogue(BayunNetwork.DialogReceived e){var mc=Minecraft.getInstance();if(e.value.open())mc.setScreen(new BayunDialogueScreen(e.value));else if(mc.screen instanceof BayunDialogueScreen screen)screen.answer(e.value);}
    @SubscribeEvent public static void charm(BayunNetwork.CharmReceived e){var mc=Minecraft.getInstance();if(e.value.active()){charmer=e.value.entity();charmExpires=mc.level==null?0:mc.level.getGameTime()+240;if(mc.screen instanceof BayunDialogueScreen)mc.setScreen(null);}else if(e.value.entity()==charmer)charmer=-1;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){charmer=-1;songs.clear();TaleScreen.clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();songs.values().removeIf(BayunSong::isStopped);if(mc.level!=null&&mc.player!=null&&mc.player.tickCount%10==0){for(var cat:mc.level.getEntitiesOfClass(org.slavicmyths.bayun.BayunEntity.class,mc.player.getBoundingBox().inflate(15),c->c.clip().equals("hypnosis_song")&&mc.player.distanceToSqr(c)<=225&&mc.player.hasLineOfSight(c))){if(songs.size()>=32)break;if(!songs.containsKey(cat.getUUID())){var sound=new BayunSong(cat);songs.put(cat.getUUID(),sound);mc.getSoundManager().play(sound);}}}if(mc.level==null||mc.player==null||!mc.player.isAlive()||mc.player.isCreative()||mc.player.isSpectator()||mc.level.getGameTime()>=charmExpires||mc.level.getEntity(charmer)==null){charmer=-1;return;}if(charmer>=0){mc.player.input.forwardImpulse=0;mc.player.input.leftImpulse=0;mc.player.input.jumping=false;}}
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post e){if(charmer<0)return;var mc=Minecraft.getInstance();int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();var g=e.getGuiGraphics();for(int i=0;i<8;i++){int color=(16-i)*0x01000000|0x552244;g.fill(i*2,0,i*2+2,h,color);g.fill(w-i*2-2,0,w-i*2,h,color);g.fill(0,i*2,w,i*2+2,color);g.fill(0,h-i*2-2,w,h-i*2,color);}}
    private TaleClientEvents(){}
}
