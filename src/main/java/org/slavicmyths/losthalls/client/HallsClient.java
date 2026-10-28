package org.slavicmyths.losthalls.client;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.losthalls.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class HallsClient {
 public static ResourceLocation texture(String id){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/lost_halls/"+id+".png");}
 public static final class Model extends HierarchicalModel<HallsMob>{public final HallsMesh mesh;public Model(HallsContent.Kind k){mesh=new HallsMesh(HallsContent.id(k));}@Override public ModelPart root(){return mesh.root;}@Override public void setupAnim(HallsMob e,float walk,float amount,float age,float yaw,float pitch){mesh.poseMob(HallsContent.id(e.kind),walk,amount,age,e.feedPulse()>0?-e.feedPulse():e.cast()==0?0:e.castTime(age-e.tickCount),e.hurtTime,e.deathTime,e.isOrderedToSit(),!e.onGround()&&e.getDeltaMovement().horizontalDistanceSqr()>.025,e.variant());var head=mesh.parts.get("head");if(head!=null){head.yRot+=yaw*.0174533F;head.xRot+=pitch*.0174533F;}}}
 private static String name(HallsMob e){return HallsContent.id(e.kind)+(e.kind==HallsContent.Kind.SHEPTUN?"_"+e.variant():"");}
 public static final class Renderer extends MobRenderer<HallsMob,Model>{public Renderer(EntityRendererProvider.Context c,HallsContent.Kind k){super(c,new Model(k),k==HallsContent.Kind.EKHOVIK?.9F:.5F);addLayer(new RenderLayer<HallsMob,Model>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack p,MultiBufferSource out,int light,HallsMob e,float a,float b,float partial,float age,float yaw,float pitch){getParentModel().renderToBuffer(p,out.getBuffer(RenderType.eyes(texture(name(e)+"_glow"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xffffffff);}});}@Override public ResourceLocation getTextureLocation(HallsMob e){return texture(name(e));}@Override protected float getFlipDegrees(HallsMob e){return 0;}}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){HallsContent.TYPES.forEach((k,t)->e.registerEntityRenderer(t.get(),c->new Renderer(c,k)));e.registerEntityRenderer(PostenMob.TYPE.get(),PostenRenderer::new);e.registerEntityRenderer(ShadowDecoy.TYPE.get(),ShadowDecoyRenderer::new);e.registerEntityRenderer(HallsBolt.TYPE.get(),c->new ThrownItemRenderer<>(c,.65F,true));}
 private HallsClient(){}
}
