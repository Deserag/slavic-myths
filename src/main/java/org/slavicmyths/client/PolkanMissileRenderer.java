package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slavicmyths.savanna.PolkanMissile;
public final class PolkanMissileRenderer extends EntityRenderer<PolkanMissile>{
    private final ModelPart spear,arrow;
    public PolkanMissileRenderer(EntityRendererProvider.Context c){super(c);FolkModelGeometry g=new FolkModelGeometry();spear=PolkanWeapons.spear(g,null,0,0,0);arrow=PolkanWeapons.arrow(g,null,0,0,0);}
    @Override public void render(PolkanMissile e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partial,e.yRotO,e.getYRot())));pose.mulPose(Axis.XP.rotationDegrees(-90-Mth.lerp(partial,e.xRotO,e.getXRot())));
        float scale=e.spear()?1.08F:.8F;pose.scale(scale,scale,scale);
        (e.spear()?spear:arrow).render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(PolkanRenderer.TEXTURE)),light,OverlayTexture.NO_OVERLAY,0xffffffff);
        pose.popPose();super.render(e,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(PolkanMissile e){return PolkanRenderer.TEXTURE;}
}