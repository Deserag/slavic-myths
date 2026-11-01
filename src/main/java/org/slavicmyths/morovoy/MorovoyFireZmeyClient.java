package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.math.Axis;
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
  private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
  private static final ResourceLocation ALBEDO=MorovoyFireZmeyContent.id("textures/entity/morovoy_fire_zmey_albedo.png");
  public BossRenderer(EntityRendererProvider.Context context){
   super(context,new MorovoyFireZmeyModel(),1.2f);
   // Right-side bones use an exclusively white sampling surface, recolored by scoped passes.
   // No original green UV data can enter the fire head, neck, or tail passes.
   addLayer(new RenderLayer<MorovoyFireZmey,MorovoyFireZmeyModel>(this){
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,MorovoyFireZmey boss,float limb,float amount,float partial,float age,float yaw,float pitch){
     MorovoyFireZmeyModel rig=getParentModel();
     rig.renderFireSide(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE)),light,OverlayTexture.NO_OVERLAY,0xFF682333);
     rig.renderMorAccents(pose,buffers.getBuffer(RenderType.eyes(WHITE)),15728880,OverlayTexture.NO_OVERLAY,0xFF33F43D);
     rig.renderFireAccents(pose,buffers.getBuffer(RenderType.eyes(WHITE)),15728880,OverlayTexture.NO_OVERLAY,0xFF963CFA,false);
     rig.renderFireAccents(pose,buffers.getBuffer(RenderType.eyes(WHITE)),15728880,OverlayTexture.NO_OVERLAY,0xFFFFFFFF,true);
    }
   });
  }
  @Override public ResourceLocation getTextureLocation(MorovoyFireZmey boss){return ALBEDO;}
  @Override protected void setupRotations(MorovoyFireZmey boss,PoseStack pose,float bob,float bodyYaw,float partial,float scale){
   super.setupRotations(boss,pose,bob,bodyYaw,partial,scale);
   if(boss.flying())pose.mulPose(Axis.ZP.rotationDegrees(boss.bank()));
  }
  @Override public boolean shouldRender(MorovoyFireZmey boss,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){
   return frustum.isVisible(boss.getBoundingBox().inflate(12,10,12));
  }
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
