package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.poloz.*;
public final class PolozSpikeRenderer extends EntityRenderer<PolozStoneSpike> {
 private final ModelPart model;
 public PolozSpikeRenderer(EntityRendererProvider.Context c){super(c);var mesh=new MeshDefinition();var boxes=CubeListBuilder.create().texOffs(0,0);for(int i=0;i<4;i++){float width=8-i*2;boxes.addBox(-width/2,i*5,-width/2,width,5,width);}mesh.getRoot().addOrReplaceChild("spike",boxes,PartPose.ZERO);model=LayerDefinition.create(mesh,64,64).bakeRoot();}
 @Override public ResourceLocation getTextureLocation(PolozStoneSpike s){return PolozContent.id("textures/entity/poloz_stone_spike.png");}
 @Override public void render(PolozStoneSpike s,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){float age=s.tickCount+partial;float rise=Math.min(1,age/4)*Math.min(1,(24-age)/6);p.pushPose();p.scale(1.5f,Math.max(.01f,rise)*1.5f,1.5f);model.render(p,b.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(s))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();}
}
