package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.ViyBoss;

public final class ViyRenderer extends MobRenderer<ViyBoss,ViyModel>{
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/viy.png");
    public ViyRenderer(EntityRendererProvider.Context ctx){super(ctx,new ViyModel(),1.45F);}
    @Override public ResourceLocation getTextureLocation(ViyBoss entity){return TEXTURE;}
}
