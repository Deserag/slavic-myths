package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

/** Extra protection rows share the vanilla bottom-up HUD layout, including absorption hearts. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class SistersArmorHud {
    private static final ResourceLocation ARMOR_FULL=ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation ARMOR_HALF=ResourceLocation.withDefaultNamespace("hud/armor_half");

    @SubscribeEvent public static void draw(RenderGuiLayerEvent.Post event) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui||mc.screen!=null)return;
        if (!event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.ARMOR_LEVEL)
                || mc.gameMode == null || !mc.gameMode.canHurtPlayer()
                || mc.getCameraEntity() != mc.player) return;
        int armor=Math.max(0,(int)Math.round(mc.player.getAttributeValue(Attributes.ARMOR)));
        double toughness=Math.max(0D,mc.player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        int extraArmor=Math.max(0,armor-20);
        if(extraArmor==0&&toughness<=0D)return;

        GuiGraphics graphics=event.getGuiGraphics();
        int y=graphics.guiHeight()-mc.gui.leftHeight;
        int left=graphics.guiWidth()/2-91;
        int extraIcons=(extraArmor+1)/2;
        int extraRows=(extraIcons+9)/10;

        for(int i=0;i<extraIcons;i++){
            int x=left+(i%10)*8;
            int iconY=y-(i/10)*10;
            graphics.blitSprite(extraArmor>=2*(i+1)?ARMOR_FULL:ARMOR_HALF,x,iconY,9,9);
        }

        int toughnessIcons=(int)Math.ceil(toughness/2D);
        mc.gui.leftHeight += (extraRows+(toughnessIcons+9)/10)*10;
        for(int i=0;i<toughnessIcons;i++){
            int x=left+(i%10)*8;
            int iconY=y-extraRows*10-(i/10)*10;
            drawToughnessShield(graphics,x,iconY,toughness>=2D*(i+1));
        }
    }

    private static void drawToughnessShield(GuiGraphics graphics,int x,int y,boolean full){
        int outline=0xff1a2440,base=0xff58728e,shine=0xff88d2f0;
        graphics.fill(x+1,y+1,x+8,y+5,outline);
        graphics.fill(x+2,y+5,x+7,y+7,outline);
        graphics.fill(x+3,y+7,x+6,y+8,outline);
        graphics.fill(x+4,y+8,x+5,y+9,outline);
        graphics.fill(x+2,y+2,x+7,y+5,base);
        graphics.fill(x+3,y+5,x+6,y+6,base);
        graphics.fill(x+2,y+2,x+(full?7:4),y+5,shine);
        graphics.fill(x+3,y+5,x+(full?6:4),y+6,shine);
    }

    private SistersArmorHud() {}
}
