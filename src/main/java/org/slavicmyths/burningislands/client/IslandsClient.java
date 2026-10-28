package org.slavicmyths.burningislands.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.burningislands.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class IslandsClient {
 public static final class Renderer extends MobRenderer<IslandsMob,IslandsModel>{
  public Renderer(EntityRendererProvider.Context c,IslandsContent.Kind k){super(c,new IslandsModel(k),k==IslandsContent.Kind.MAGMA_LORD?1:.6F);
   addLayer(new RenderLayer<IslandsMob,IslandsModel>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack p,MultiBufferSource out,int light,IslandsMob e,float a,float b,float partial,float age,float yaw,float pitch){getParentModel().renderToBuffer(p,out.getBuffer(RenderType.eyes(texture(e.kind,true))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xffffffff);}});
  }
  @Override public ResourceLocation getTextureLocation(IslandsMob e){return texture(e.kind,false);}
  @Override protected float getFlipDegrees(IslandsMob e){return 0;}
 }
 public static ResourceLocation texture(IslandsContent.Kind k,boolean glow){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/burning_islands/"+IslandsContent.id(k)+(glow?"_glow":"")+".png");}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){IslandsContent.TYPES.forEach((k,t)->e.registerEntityRenderer(t.get(),c->new Renderer(c,k)));}
 private IslandsClient(){}
}
