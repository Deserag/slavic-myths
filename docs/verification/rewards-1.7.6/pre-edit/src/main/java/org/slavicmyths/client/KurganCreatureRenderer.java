package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.slavicmyths.kurgan.*;

public final class KurganCreatureRenderer extends MobRenderer<KurganCreature,KurganCreatureModel> {
    private final ResourceLocation texture;private final KurganFighter.Kind kind;
    public KurganCreatureRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,KurganFighter.Kind kind){super(manager,new KurganCreatureModel(kind),kind.width*.55F);this.kind=kind;texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+kind.id+".png");addLayer(new Glow(this));}
    @Override public ResourceLocation getTextureLocation(KurganCreature e){return texture;}
    @Override protected RenderType getRenderType(KurganCreature e,boolean visible,boolean translucent,boolean glowing){return kind==KurganFighter.Kind.NAV?RenderType.entityTranslucent(texture):super.getRenderType(e,visible,translucent,glowing);}
    @Override public void render(KurganCreature e,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){super.render(e,yaw,partial,pose,out,light);if(kind==KurganFighter.Kind.VOLKHV&&e.clones())for(int n=0;n<2;n++){java.util.Optional<BlockPos> pos=e.decoy(n);if(!pos.isPresent())continue;BlockPos p=pos.get();pose.pushPose();pose.translate(p.getX()+.5-e.getX(),p.getY()-e.getY(),p.getZ()+.5-e.getZ());pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180-yaw));pose.scale(-1,-1,1);pose.translate(0,-1.501,0);model.renderToBuffer(pose,out.getBuffer(RenderType.entityTranslucent(texture)),light,OverlayTexture.NO_OVERLAY,0xBFFFFFFF);pose.popPose();}}
    private final class Glow extends RenderLayer<KurganCreature,KurganCreatureModel>{Glow(net.minecraft.client.renderer.entity.RenderLayerParent<KurganCreature,KurganCreatureModel> parent){super(parent);}@Override public void render(PoseStack pose,MultiBufferSource out,int light,KurganCreature e,float stride,float amount,float partial,float age,float yaw,float pitch){model.renderEyes(pose,out.getBuffer(RenderType.eyes(texture)));}}
}
