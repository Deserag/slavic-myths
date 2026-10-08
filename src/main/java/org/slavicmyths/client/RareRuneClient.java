package org.slavicmyths.client;

import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.rpg.classes.ClassHudConfig;

/** One shared rune input: press R to use; Shift+R to cycle. Existing class slots remain intact. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RareRuneClient {
    public static final KeyMapping USE=new KeyMapping("key.slavicmyths.rare_rune",82,"key.categories.slavicmyths");
    private static long receivedTick,serverTick;
    public static void keys(RegisterKeyMappingsEvent e){e.register(USE);}
    public static void synced(){var mc=Minecraft.getInstance();receivedTick=mc.level==null?0:mc.level.getGameTime();serverTick=RpgClient.data.getCompound(RareRuneRuntime.KEY).getLong("ServerTick");}
    private static long now(){var mc=Minecraft.getInstance();return serverTick+(mc.level==null?0:Math.max(0,mc.level.getGameTime()-receivedTick));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();while(USE.consumeClick())if(mc.player!=null&&mc.screen==null)RareRuneNetwork.send(mc.player.isShiftKeyDown());}
    @SubscribeEvent public static void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||mc.options.hideGui)return;var g=e.getGuiGraphics();int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();var d=RpgClient.data.getCompound(RareRuneRuntime.KEY);long now=now();
        int tier=RareRuneRuntime.tier(mc.player);double amount=ClassHudConfig.BERSERKER_SCREEN_EFFECT.get()?tier*.10*ClassHudConfig.BERSERKER_INTENSITY.get():0;
        if(tier>=2)amount*=1+.08*Math.sin((mc.level.getGameTime())*.07);
        if(tier==3&&d.getLong("HitUntil")>now)amount+=.025*ClassHudConfig.BERSERKER_INTENSITY.get();
        if(d.getLong("PulseUntil")>now&&ClassHudConfig.BERSERKER_SCREEN_EFFECT.get())amount=Math.max(amount,.13*ClassHudConfig.BERSERKER_INTENSITY.get());
        // Narrow soft bands leave the crosshair, hotbar and central 70% clear. Never draw over an open screen.
        for(int i=0;i<12&&amount>0;i++){int alpha=(int)(255*amount*(12-i)/12);int color=(alpha<<24)|0x680e26;int dx=Math.max(1,w/100),dy=Math.max(1,h/100);g.fill(i*dx,0,(i+1)*dx,h-60,color);g.fill(w-(i+1)*dx,0,w-i*dx,h-60,color);g.fill((i+1)*dx,i*dy,w-(i+1)*dx,(i+1)*dy,color);}
        var active=RareRunes.active(mc.player);if(active.isEmpty())return;String id=active.get(RareRuneRules.selected(d.getInt("Selected"),active.size()));int x=w-128,y=Math.max(6,h-174);g.fill(x,y,x+120,y+40,0xb0201927);
        var item=RareRunes.ITEMS.get("rune_"+id);g.renderItem(new net.minecraft.world.item.ItemStack(item.get()),x+3,y+3);String name=Component.translatable("item.slavicmyths.rune_"+id).getString();g.drawString(mc.font,mc.font.plainSubstrByWidth(name,93),x+23,y+5,0xf2ddd0,false);
        long left=Math.max(0,d.getLong("CD_"+id)-now);g.drawString(mc.font,left>0?RareRunes.text("cooldown",(left+19)/20):RareRunes.text("ready"),x+23,y+16,0xdbb7d6,false);g.drawString(mc.font,RareRunes.text("key",USE.getTranslatedKeyMessage()),x+4,y+28,0xc4b7cf,false);
    }
    private RareRuneClient(){}
}
