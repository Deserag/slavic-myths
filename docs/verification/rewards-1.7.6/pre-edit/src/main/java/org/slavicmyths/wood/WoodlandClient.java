package org.slavicmyths.wood;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class WoodlandClient {
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{
  for(Woodlands.Set s:Woodlands.SETS.values()){
   Sheets.addWoodType(s.type);
   for(String k:new String[]{"leaves","sapling","door","trapdoor"})ItemBlockRenderTypes.setRenderLayer(s.get(k),RenderType.cutoutMipped());
  }
  ItemBlockRenderTypes.setRenderLayer(Woodlands.HANGING.get(),RenderType.cutoutMipped());
 });}
 @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(Woodlands.SIGN_TILE.get(),SignRenderer::new);}
}
