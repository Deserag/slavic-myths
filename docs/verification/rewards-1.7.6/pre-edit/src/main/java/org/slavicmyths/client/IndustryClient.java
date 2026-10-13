package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slavicmyths.industry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class IndustryClient {
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(MachineMenu.BLOOMERY.get(),MachineScreen::new);e.register(MachineMenu.HEARTH.get(),MachineScreen::new);}
    public static final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
        public MachineScreen(MachineMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=166;}
        protected void renderBg(GuiGraphics g,float partial,int x,int y){
            g.blit(Industry.id("textures/gui/"+(menu.refining?"refining_hearth":"chud_bloomery")+".png"),leftPos,topPos,0,0,176,166,256,256);
            int width=Math.min(24,menu.data.get(2)*24/Math.max(1,menu.data.get(3)));if(width>0)g.fill(leftPos+79,topPos+37,leftPos+79+width,topPos+43,0xfffaf1cb);
            int fire=Math.min(13,menu.data.get(0)*13/Math.max(1,menu.data.get(1)));if(fire>0){int fx=menu.refining?46:56;g.fill(leftPos+fx+3,topPos+49-fire,leftPos+fx+12,topPos+49,0xffdc902e);}
        }
        public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);
            if(menu.refining&&menu.slots.get(1).getItem().isEmpty()&&isHovering(58,17,16,16,x,y))g.renderTooltip(font,Component.translatable("item.slavicmyths.purification_flux"),x,y);
            int out=menu.refining?3:2;if(menu.slots.get(out).getItem().isEmpty()&&isHovering(116,35,16,16,x,y)&&!menu.slots.get(0).getItem().isEmpty()){
                ItemStack result=menu.refining?new ItemStack(menu.slots.get(0).getItem().is(net.minecraft.world.item.Items.COPPER_INGOT)?Industry.COPPER.get():Industry.IRON.get()):new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
                g.renderTooltip(font,result.getHoverName(),x,y);
            }
        }
    }
}
