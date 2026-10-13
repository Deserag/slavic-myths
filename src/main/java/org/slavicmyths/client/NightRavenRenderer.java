package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import org.slavicmyths.karachun.*;
public final class NightRavenRenderer extends MobRenderer<NightRaven,NightRavenRenderer.Model> {
 public NightRavenRenderer(EntityRendererProvider.Context c){super(c,new Model(),.2f);addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<NightRaven,Model>(this){@Override public void render(PoseStack p,MultiBufferSource out,int light,NightRaven e,float a,float b,float partial,float time,float y,float x){getParentModel().renderToBuffer(p,out.getBuffer(RenderType.eyes(NightContent.id("textures/entity/karachun_raven_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,e.allied()?0xFFA9C6EE:0xFFFFFFFF);}});}
 @Override public net.minecraft.resources.ResourceLocation getTextureLocation(NightRaven e){return NightContent.id("textures/entity/karachun_raven.png");}
 public static final class Model extends EntityModel<NightRaven>{final ModelPart root=NightRavenMesh.create(),bird=root.getChild("bird");@Override public void setupAnim(NightRaven e,float a,float b,float time,float y,float x){root.getAllParts().forEach(ModelPart::resetPose);bird.getChild("head").yRot=y*.01745f;boolean dive=e.state()==2;float flap=dive?1.1f:e.state()==0&&time%160>128?.12f:(float)Math.sin(time*.55)*.55f;bird.getChild("wing_left").zRot=flap;bird.getChild("wing_right").zRot=-flap;bird.getChild("tail").xRot=dive?-.4f:.15f;bird.xRot=dive?.65f:e.state()==3?-.3f:.12f;for(String s:new String[]{"left","right"})bird.getChild("claw_"+s).xRot=dive?-.8f:.6f;}@Override public void renderToBuffer(PoseStack p,VertexConsumer b,int light,int overlay,int color){root.render(p,b,light,overlay,color);}}
}
