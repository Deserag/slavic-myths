package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.slavicmyths.morozko.*;
/** Crystalline geometry, not a snowball billboard. */
public final class FrostGeometryRenderer<T extends Entity> extends EntityRenderer<T> {
 private final ModelPart crystal;
 public FrostGeometryRenderer(EntityRendererProvider.Context c){super(c);var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("crystal",CubeListBuilder.create().texOffs(0,0).addBox(-3,0,-3,6,14,6).texOffs(0,0).addBox(-2,14,-2,4,10,4).texOffs(0,0).addBox(-1,24,-1,2,10,2),PartPose.ZERO);crystal=LayerDefinition.create(mesh,64,64).bakeRoot();}
 @Override public ResourceLocation getTextureLocation(T e){return FrostContent.id("textures/entity/morozko_crystal.png");}
 @Override public void render(T e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){if(e instanceof FrostHazard h&&(h.kind()==2||h.age()<h.delay()))return;p.pushPose();if(e instanceof FrostHazard h){p.scale(1,1,1);if(h.kind()==1){p.translate(0,2.125,0);p.mulPose(Axis.ZP.rotationDegrees(180));}else p.scale(1,Math.min(1,(h.age()-h.delay()+partial)/5f),1);}else{p.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));p.mulPose(Axis.XP.rotationDegrees(90+e.getXRot()));p.scale(.35f,.2f,.35f);}crystal.render(p,out.getBuffer(RenderType.entityTranslucent(getTextureLocation(e))),light,OverlayTexture.NO_OVERLAY,0xD9B9EDFF);p.popPose();super.render(e,yaw,partial,p,out,light);}
}
