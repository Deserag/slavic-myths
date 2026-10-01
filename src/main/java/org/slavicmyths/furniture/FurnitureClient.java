package org.slavicmyths.furniture;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.*;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class FurnitureClient {
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{
  ClientRegistry.bindTileEntityRenderer(Furniture.RACK.get(),RackRenderer::new);
  RenderingRegistry.registerEntityRenderingHandler(Furniture.SEAT.get(),m->new EntityRenderer<SeatEntity>(m){public ResourceLocation getTextureLocation(SeatEntity s){return new ResourceLocation("minecraft","textures/misc/white.png");}public void render(SeatEntity s,float y,float t,MatrixStack p,IRenderTypeBuffer b,int light){}});
 });}
 public static final class RackRenderer extends TileEntityRenderer<RackTile>{
  public RackRenderer(TileEntityRendererDispatcher d){super(d);}
  @Override public void render(RackTile tile,float t,MatrixStack p,IRenderTypeBuffer b,int light,int overlay){if(tile.weapon.isEmpty())return;
   p.pushPose();p.translate(.5,.72,.5);p.mulPose(Vector3f.YP.rotationDegrees(-tile.getBlockState().getValue(HorizontalBlock.FACING).toYRot()));p.mulPose(Vector3f.ZP.rotationDegrees(18));p.scale(.8F,.8F,.8F);
   Minecraft.getInstance().getItemRenderer().renderStatic(tile.weapon,ItemCameraTransforms.TransformType.FIXED,light,overlay,p,b);p.popPose();
  }
 }
}
