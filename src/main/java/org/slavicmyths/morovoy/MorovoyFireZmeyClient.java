package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Dedicated model and separate emissive texture; vanilla fire textures are untouched. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MorovoyFireZmeyClient {
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event){
  event.registerEntityRenderer(MorovoyFireZmeyContent.BOSS.get(),BossRenderer::new);
  event.registerEntityRenderer(MorovoyFireZmeyContent.BOLT.get(),EffectsRenderer::new);
  event.registerEntityRenderer(MorovoyFireZmeyContent.FIELD.get(),EffectsRenderer::new);
 }
 public static final class BossRenderer extends MobRenderer<MorovoyFireZmey,MorovoyFireZmeyModel>{
  public BossRenderer(EntityRendererProvider.Context context){super(context,new MorovoyFireZmeyModel(),1.6f);addLayer(new RenderLayer<MorovoyFireZmey,MorovoyFireZmeyModel>(this){
   @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,MorovoyFireZmey boss,float limb,float amount,float partial,float age,float yaw,float pitch){getParentModel().renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_emissive.png"))),15728880,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);}
  });}
  @Override public ResourceLocation getTextureLocation(MorovoyFireZmey boss){return MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_albedo.png");}
  @Override public boolean shouldRender(MorovoyFireZmey boss,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){return frustum.isVisible(boss.getBoundingBox().inflate(10,8,10));}
 }
 /** Fields are rendered through bounded client particles; projectiles receive a visible cube core. */
 public static final class EffectsRenderer<T extends Entity> extends EntityRenderer<T>{
  public EffectsRenderer(EntityRendererProvider.Context context){super(context);}
  @Override public ResourceLocation getTextureLocation(T entity){return MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_albedo.png");}
  @Override public void render(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
   if(entity instanceof MorovoyZmeyBolt bolt){var consumer=buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));int tint=bolt.element()==0?0xFF32EA47:0xFFB43BFA;org.slavicmyths.client.GuardianClient.cube(pose,consumer,-.24,0,-.24,.48,.48,.48,tint);}
   super.render(entity,yaw,partial,pose,buffers,light);
  }
 }
 private MorovoyFireZmeyClient(){}
}
