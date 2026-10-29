package org.slavicmyths.client;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;import net.minecraft.world.entity.player.Inventory;import net.minecraft.network.chat.Component;import net.minecraft.resources.ResourceLocation;import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;import org.slavicmyths.infernalfurnace.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class InfernalFurnaceClient {
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(InfernalFurnace.MENU.get(),Screen::new);}
 public static final class Screen extends AbstractFurnaceScreen<InfernalFurnaceMenu>{
  public Screen(InfernalFurnaceMenu m,Inventory inv,Component title){super(m,new SmeltingRecipeBookComponent(){@Override protected java.util.Set<net.minecraft.world.item.Item> getFuelItems(){return java.util.Set.of(InfernalFurnace.COAL.get());}},inv,title,ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png"),ResourceLocation.withDefaultNamespace("container/furnace/lit_progress"),ResourceLocation.withDefaultNamespace("container/furnace/burn_progress"));}
  @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float t){super.render(g,x,y,t);if(menu.slots.get(1).getItem().isEmpty()&&isHovering(56,53,16,16,x,y))g.renderTooltip(font,Component.translatable("infernal_furnace.fuel"),x,y);}
 }
 private InfernalFurnaceClient(){}
}

