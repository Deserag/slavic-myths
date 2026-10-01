package org.slavicmyths.wood;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.SignTileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class WoodlandClient {
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{
  ClientRegistry.bindTileEntityRenderer(Woodlands.SIGN_TILE.get(),SignTileEntityRenderer::new);
  for(Woodlands.Set s:Woodlands.SETS.values()){
   Atlases.addWoodType(s.type);
   for(String k:new String[]{"leaves","sapling","door","trapdoor"})RenderTypeLookup.setRenderLayer(s.get(k),RenderType.cutoutMipped());
  }
  RenderTypeLookup.setRenderLayer(Woodlands.HANGING.get(),RenderType.cutoutMipped());
 });}
}
