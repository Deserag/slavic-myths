package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.slavicmyths.rpg.RuneVisual;

public final class RuneVisualRenderer extends EntityRenderer<RuneVisual> {
    private static final ResourceLocation SHIELD=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/rune_shield.png");
    private static final ResourceLocation CRACKED=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/rune_shield_cracked.png");
    private final ItemRenderer items;
    public RuneVisualRenderer(EntityRendererProvider.Context c){super(c);items=c.getItemRenderer();}
    public void render(RuneVisual e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        if(e.mode()==0){pose.mulPose(Axis.YP.rotationDegrees(e.getYRot()));pose.mulPose(Axis.ZP.rotationDegrees(e.phase()==2?-90:-35));float size=e.phase()==0?Math.min(1,(e.tickCount+partial)/12):e.phase()==20?Math.max(0,1-(e.animationAge+partial)/8):1;pose.scale(size,size,size);items.renderStatic(e.item(),ItemDisplayContext.FIXED,15728880,OverlayTexture.NO_OVERLAY,pose,buffers,e.level(),e.getId());}
        else{int n=e.count();for(int i=0;i<3;i++){if(i>=n&&e.phase()!=10&&e.phase()!=20)continue;pose.pushPose();float angle=e.phase()==10?e.getYRot()+i*120:(e.tickCount+partial)*3+i*120;pose.mulPose(Axis.YP.rotationDegrees(angle));pose.translate(0,0,1);float alpha=i<n?.65F:.22F;if(e.phase()==20)alpha=.65F*Math.max(0,1-(e.animationAge+partial)/8);var v=buffers.getBuffer(RenderType.entityTranslucent(i>=n||e.phase()==20?CRACKED:SHIELD));var m=pose.last();quad(v,m,-.35F,-.45F,0,0,1,alpha);quad(v,m,.35F,-.45F,0,1,1,alpha);quad(v,m,.35F,.45F,0,1,0,alpha);quad(v,m,-.35F,.45F,0,0,0,alpha);quad(v,m,-.35F,.45F,0,0,0,alpha);quad(v,m,.35F,.45F,0,1,0,alpha);quad(v,m,.35F,-.45F,0,1,1,alpha);quad(v,m,-.35F,-.45F,0,0,1,alpha);pose.popPose();}}
        pose.popPose();super.render(e,yaw,partial,pose,buffers,light);
    }
    private static void quad(com.mojang.blaze3d.vertex.VertexConsumer v,PoseStack.Pose m,float x,float y,float z,float u,float w,float a){v.addVertex(m.pose(),x,y,z).setColor(170,190,255,(int)(a*255)).setUv(u,w).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(m,0,0,1);}
    public ResourceLocation getTextureLocation(RuneVisual e){return e.mode()==1?SHIELD:net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;}
}
