package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.mor.MorBolt;

public final class MorBoltRenderer extends EntityRenderer<MorBolt> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/mor/bolt.png");
    private static final ResourceLocation ROOT_TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/mor/root.png");
    private final net.minecraft.client.model.geom.ModelPart root;
    public MorBoltRenderer(EntityRendererProvider.Context context){super(context);root=createRoot();}
    public static net.minecraft.client.model.geom.ModelPart createRoot(){FolkModelGeometry g=new FolkModelGeometry();var root=g.part(32,32);g.box(root,0,0,-2.4F,-17.5F,-2.4F,4.8F,17.5F,4.8F);g.box(root,16,0,-1.6F,-20.8F,-1.6F,3.2F,6,3.2F);g.box(root,16,16,2,-14,-1,4,2,2);return root;}
    @Override public ResourceLocation getTextureLocation(MorBolt e){return e.root()?ROOT_TEXTURE:TEXTURE;}
    @Override public void render(MorBolt e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){p.pushPose();if(e.root()){p.scale(1,-1,1);root.render(p,out.getBuffer(RenderType.entityCutoutNoCull(ROOT_TEXTURE)),light,OverlayTexture.NO_OVERLAY);}else{p.mulPose(entityRenderDispatcher.cameraOrientation());float size=e.thread()?.08F:.175F;p.scale(size,size,size);var b=out.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));var pose=p.last();vertex(b,pose,-1,-1,0,1,light);vertex(b,pose,1,-1,1,1,light);vertex(b,pose,1,1,1,0,light);vertex(b,pose,-1,1,0,0,light);}p.popPose();super.render(e,yaw,partial,p,out,light);}
    private static void vertex(VertexConsumer b,PoseStack.Pose p,float x,float y,float u,float v,int light){b.addVertex(p,x,y,0).setColor(255,255,255,255).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,1,0);}
}
