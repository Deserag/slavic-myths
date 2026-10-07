package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.textile.*;

public final class CarcassRenderer extends EntityRenderer<Carcass> {
    private final ModelPart bundle;
    public CarcassRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.35F;var g=new FolkModelGeometry();bundle=g.part(64,64);g.box(bundle,0,0,-10,0,-4,20,6,8);g.box(bundle,0,24,-7,0,-4.2F,1,6.2F,8.4F);g.box(bundle,0,24,5,0,-4.2F,1,6.2F,8.4F);g.box(bundle,24,24,-1,6,-1.2F,2,1,2.4F);}
    @Override public ResourceLocation getTextureLocation(Carcass e){return Textiles.id("textures/entity/carcass/"+e.kind().name().toLowerCase(java.util.Locale.ROOT)+".png");}
    @Override public void render(Carcass e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yaw));float s=e.visualScale()/16F;pose.scale(s,s,s);
        bundle.render(pose,buffers.getBuffer(RenderType.entityCutout(getTextureLocation(e))),light,OverlayTexture.NO_OVERLAY,-1);pose.popPose();super.render(e,yaw,partial,pose,buffers,light);
    }
}
