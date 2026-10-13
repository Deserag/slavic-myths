package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.slavicmyths.karachun.*;
public final class NightBladeRenderer extends EntityRenderer<NightBlade> {
 private final net.minecraft.client.model.geom.ModelPart scythe=KarachunMesh.create().getChild("pelvis").getChild("torso").getChild("arm_right").getChild("forearm_right").getChild("hand_right").getChild("scythe");
 public NightBladeRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public net.minecraft.resources.ResourceLocation getTextureLocation(NightBlade e){return NightContent.id("textures/entity/karachun.png");}
 @Override public void render(NightBlade e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-e.getYRot()+e.tickCount*15));p.mulPose(Axis.ZP.rotationDegrees(90));p.scale(.4f,.4f,.4f);p.translate(.5,1,0);scythe.render(p,out.getBuffer(RenderType.entityTranslucentEmissive(getTextureLocation(e))),15728880,OverlayTexture.NO_OVERLAY,e.hunger()?0x807F6A99:0x998F67DE);p.popPose();}
}
