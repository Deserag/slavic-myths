package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.ViyServant;

public final class ViyServantRenderer extends MobRenderer<ViyServant,ViyServantModel>{
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/viy.png");
    public ViyServantRenderer(EntityRendererProvider.Context ctx,ViyServant.Kind kind){
        super(ctx,new ViyServantModel(kind),kind==ViyServant.Kind.KORNELAP?.5F:.65F);
    }
    @Override public ResourceLocation getTextureLocation(ViyServant mob){return TEXTURE;}
}
