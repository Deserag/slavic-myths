package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slavicmyths.bogatyr.IlyaEntity;
import org.slavicmyths.marinka.DobrynyaEntity;
import org.slavicmyths.skim.AlyoshaEntity;
import org.slavicmyths.finist.FinistEntity;

/** One compact stack for the two summoned companions, independent of hero type. */
@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT)
public final class HeroHud {
    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui
                ||!org.slavicmyths.bogatyr.BogatyrConfig.HUD.get())return;
        var heroes=new ArrayList<LivingEntity>();
        for(var entity:mc.level.entitiesForRendering()) {
            UUID owner=entity instanceof IlyaEntity h?h.syncedOwner():entity instanceof DobrynyaEntity h?h.syncedOwner()
                    :entity instanceof AlyoshaEntity h?h.syncedOwner():entity instanceof FinistEntity h?h.syncedOwner():null;
            if(mc.player.getUUID().equals(owner)&&entity instanceof LivingEntity h&&h.isAlive()
                    &&mc.player.distanceToSqr(h)<=64*64)heroes.add(h);
        }
        heroes.sort(Comparator.comparing(h->h.getType().toString()));
        var g=event.getGuiGraphics();
        float scale=.75F;
        int count=Math.min(2,heroes.size());
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        int x=8,y=Math.max(8,(int)((g.guiHeight()-65)/scale)-count*23);
        for(int i=0;i<count;i++) {
            var h=heroes.get(i);int row=y+i*23;
            boolean resting=org.slavicmyths.kalin.KalinParty.recovering(h);
            String hp=Math.round(h.getHealth())+"/"+Math.round(h.getMaxHealth());
            int hpWidth=mc.font.width(hp),panel=160;
            g.fill(x-3,row-2,x+panel+3,row+19,0x98211D19);
            String label=resting?Component.translatable("gui.slavicmyths.174.recovery").getString():h.getName().getString();
            if(h instanceof FinistEntity f&&f.bird()==4)label=Component.translatable("message.slavicmyths.falcon_cursed").getString();
            String name=mc.font.plainSubstrByWidth(label,panel-hpWidth-8);
            g.drawString(mc.font,name,x,row,resting?0xC1B497:0xEEE1C9);
            g.drawString(mc.font,hp,x+panel-hpWidth,row,0xDDD4C4);
            g.fill(x,row+12,x+panel,row+15,0xFF493B35);
            g.fill(x,row+12,x+Math.round(panel*Math.clamp(h.getHealth()/h.getMaxHealth(),0,1)),row+15,
                    resting?0xFF897951:0xFFB99B61);
        }
        g.pose().popPose();
    }
    private HeroHud() {}
}
