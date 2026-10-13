package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.skim.*;
public final class SkimClient {
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {@SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(SkimContent.ALYOSHA.get(),HeroRenderer::new);e.registerEntityRenderer(SkimContent.SKIM.get(),BeastRenderer::new);e.registerEntityRenderer(SkimContent.ARROW.get(),BowArrowRenderer::new);}
}
 public static final class BowArrowRenderer extends ArrowRenderer<AlyoshaArrow>{public BowArrowRenderer(EntityRendererProvider.Context c){super(c);}@Override public ResourceLocation getTextureLocation(AlyoshaArrow e){return ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");}}
 public static final class HeroRenderer extends MobRenderer<AlyoshaEntity,SkimModel<AlyoshaEntity>> {public HeroRenderer(EntityRendererProvider.Context c){super(c,new SkimModel<>(AlyoshaMesh.create(),false),.4F);}@Override public ResourceLocation getTextureLocation(AlyoshaEntity e){return SkimContent.id("textures/entity/alyosha_popovich.png");}@Override protected float getFlipDegrees(AlyoshaEntity e){return 0;}@Override public void render(AlyoshaEntity e,float yaw,float partial,PoseStack p,MultiBufferSource b,int l){super.render(e,yaw,partial,p,b,l);if(e.action()==SkimAction.TAUNT&&e.age(partial)>=8&&e.age(partial)<=20){p.pushPose();p.translate(0,2.65,0);p.mulPose(entityRenderDispatcher.cameraOrientation());p.scale(.025F,-.025F,.025F);getFont().drawInBatch("!",-2,0,0xE8C66A,false,p.last().pose(),b,net.minecraft.client.gui.Font.DisplayMode.NORMAL,0,0xF000F0);p.popPose();}}}
 public static final class BeastRenderer extends MobRenderer<SkimBeastEntity,SkimModel<SkimBeastEntity>> {public BeastRenderer(EntityRendererProvider.Context c){super(c,new SkimModel<>(SkimMesh.create(),true),1.25F);}@Override public ResourceLocation getTextureLocation(SkimBeastEntity e){return SkimContent.id("textures/entity/skim_beast.png");}@Override protected float getFlipDegrees(SkimBeastEntity e){return 0;}
  @Override public void render(SkimBeastEntity e,float yaw,float partial,PoseStack p,MultiBufferSource b,int l){super.render(e,yaw,partial,p,b,l);if(e.action()==SkimAction.LEAP&&e.age(partial)>=12&&e.age(partial)<=29){var q=e.landing();ring(p,b,q.getX()+.5-e.getX(),q.getY()+.025-e.getY(),q.getZ()+.5-e.getZ(),2.2,0xBA623D,1);}if(e.action()==SkimAction.WHISTLE&&e.age(partial)>=9&&e.age(partial)<=31){for(int i=0;i<3;i++)ring(p,b,0,1.5+i*.3,0,1.5+i*.7,0xA54747,.65F);}if(e.action()==SkimAction.CLAWS&&e.age(partial)>=24&&e.age(partial)<=30)ring(p,b,0,.05,0,3.3,0xE0DBC0,.8F);}
 }
 
 private static void ring(PoseStack p,MultiBufferSource b,double x,double y,double z,double r,int c,float alpha){var v=b.getBuffer(RenderType.entityTranslucent(SkimContent.id("textures/entity/marinka_magic.png")));for(int i=0;i<48;i++){if(i%4==3)continue;double a=i*Math.PI/24,xx=x+Math.cos(a)*r,zz=z+Math.sin(a)*r;double[][] q={{xx-.045,zz-.045},{xx+.045,zz-.045},{xx+.045,zz+.045},{xx-.045,zz+.045}};for(int j=0;j<4;j++)v.addVertex(p.last().pose(),(float)q[j][0],(float)y,(float)q[j][1]).setColor((c>>16&255)/255F,(c>>8&255)/255F,(c&255)/255F,alpha).setUv(j==0||j==3?0:1,j<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p.last(),0,1,0);}}
}
