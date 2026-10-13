package org.slavicmyths.furniture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class FurnitureClient {
 @SubscribeEvent public static void setup(EntityRenderersEvent.RegisterRenderers e){
  e.registerBlockEntityRenderer(Furniture.RACK.get(),RackRenderer::new);
  e.registerEntityRenderer(Furniture.SEAT.get(),m->new EntityRenderer<SeatEntity>(m){public ResourceLocation getTextureLocation(SeatEntity s){return ResourceLocation.fromNamespaceAndPath("minecraft","textures/misc/white.png");}public void render(SeatEntity s,float y,float t,PoseStack p,MultiBufferSource b,int light){}});
 }
 public static final class RackRenderer implements BlockEntityRenderer<RackTile>{
  public RackRenderer(BlockEntityRendererProvider.Context d){}
  @Override public void render(RackTile tile,float t,PoseStack p,MultiBufferSource b,int light,int overlay){if(tile.weapon.isEmpty())return;
   p.pushPose();p.translate(.5,.72,.5);p.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-tile.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));p.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(18));p.scale(.8F,.8F,.8F);
   Minecraft.getInstance().getItemRenderer().renderStatic(tile.weapon,ItemDisplayContext.FIXED,light,overlay,p,b,tile.getLevel(),0);p.popPose();
  }
 }
}
