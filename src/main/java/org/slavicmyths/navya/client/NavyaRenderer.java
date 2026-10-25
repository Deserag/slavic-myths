package org.slavicmyths.navya.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.navya.*;
public final class NavyaRenderer extends MobRenderer<NavyaMob,NavyaModel> {
    public NavyaRenderer(EntityRendererProvider.Context context,NavyaContent.Kind kind){
        super(context,new NavyaModel(kind),kind==NavyaContent.Kind.GUARDIAN?1.35F:kind==NavyaContent.Kind.WISP?.12F:.55F);
        addLayer(new RenderLayer<NavyaMob,NavyaModel>(this){
            @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out,int light,NavyaMob entity,float a,float b,float partial,float age,float yaw,float pitch){
                var texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+NavyaMesh.name(entity.kind)+"_glow.png");
                getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.eyes(texture)),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,entity.secondPhase()?0xFFFFFFFF:0xBFFFFFFF);
            }
        });
    }
    @Override protected float getFlipDegrees(NavyaMob entity){return 0;}
    @Override public ResourceLocation getTextureLocation(NavyaMob mob){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+NavyaMesh.name(mob.kind)+".png");}
}
