package org.slavicmyths.navigation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.navigation.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class NavigationClient {
    private static final KeyMapping KEY=new KeyMapping("key.slavicmyths.navigation",-1,"key.categories.slavicmyths");
    public static NavigationState state=new NavigationState();
    private static MapIntegrationBridge bridge=MapIntegrationBridge.NONE;
    private static boolean initialized,mapGuidance;
    private static int retry;
    public static void keys(RegisterKeyMappingsEvent e){e.register(KEY);}
    private static void initialize(){
        if(initialized)return;initialized=true;
        var xaero=ModList.get().getModContainerById("xaerominimap");
        if(xaero.isPresent()&&xaero.get().getModInfo().getVersion().toString().equals("26.5.0")){
            try{bridge=new org.slavicmyths.compat.xaero.XaeroNavigationBridge();}
            catch(LinkageError mismatch){disableBridge();}
        }
    }
    private static void disableBridge(){bridge=MapIntegrationBridge.NONE;mapGuidance=false;
        com.mojang.logging.LogUtils.getLogger().warn("Slavic navigation map adapter unavailable; using built-in guidance");}
    @SubscribeEvent public static void synced(NavigationNetwork.Synced e){
        state=NavigationState.read(e.payload.state());initialize();publish();
        if(e.payload.open())Minecraft.getInstance().setScreen(new NavigationScreen());
        else if(Minecraft.getInstance().screen instanceof NavigationScreen screen)screen.refresh();
    }
    private static void publish(){var player=Minecraft.getInstance().player;if(player==null)return;
        try{mapGuidance=bridge.publish(state,player.level().dimension().location());}
        catch(LinkageError|RuntimeException mismatch){try{bridge.clear();}catch(LinkageError|RuntimeException ignored){}disableBridge();}
    }
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        if(mc.screen==null&&KEY.consumeClick())NavigationNetwork.request(NavigationNetwork.Action.OPEN,null,0,true);
        // Only retry display/session binding; never regenerate areas or send packets here.
        if(++retry%20==0){initialize();publish();}
    }
    @SubscribeEvent public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut e){var previous=bridge;bridge=MapIntegrationBridge.NONE;state=new NavigationState();initialized=false;mapGuidance=false;retry=0;try{previous.clear();}catch(RuntimeException|LinkageError unavailable){com.mojang.logging.LogUtils.getLogger().debug("Optional map cleanup unavailable",unavailable);}}
    @SubscribeEvent public static void hud(RenderGuiEvent.Post event){
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.options.hideGui||mc.screen!=null||!state.preferences.contains(NavigationState.Preference.FALLBACK_HUD))return;
        var marker=state.markers.get(state.tracked);if(marker==null||!state.visible(marker))return;
        boolean same=NavigationMath.sameDimension(marker,p.level().dimension().location().toString());
        boolean inside=same&&marker.area()!=null&&marker.area().contains(p.getX(),p.getZ());
        if(mapGuidance&&same&&!inside)return;
        Component line;
        if(!same)line=Component.translatable("navigation.slavicmyths.other_dimension",marker.dimension().toString());
        else if(inside)line=Component.translatable("navigation.slavicmyths.inside");
        else {String arrow=new String[]{"↑","↗","→","↘","↓","↙","←","↖"}[NavigationMath.arrow(p.getX(),p.getZ(),p.getYRot(),marker.x()+.5,marker.z()+.5)];
            line=Component.translatable(marker.area()==null?"navigation.slavicmyths.distance":"navigation.slavicmyths.area_distance",arrow,(int)Math.ceil(NavigationMath.distance(marker,p.getX(),p.getZ())));}
        Component title=Component.translatable(marker.area()==null?"navigation.slavicmyths.target_name":"navigation.slavicmyths.search_name",Component.translatable(marker.name()));
        int center=mc.getWindow().getGuiScaledWidth()/2,w=Math.min(300,Math.max(mc.font.width(title),mc.font.width(line))+16);
        var g=event.getGuiGraphics();g.fill(center-w/2,5,center+w/2,33,0xa0000000);
        g.drawCenteredString(mc.font,title,center,8,marker.category().color);g.drawCenteredString(mc.font,line,center,21,0xffffff);
    }
    private NavigationClient(){}
}
