package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.GluheyaBoss;

/** Phase-dependent geometry comes from GluheyaModel; one consistent texture atlas. */
public final class GluheyaRenderer extends MobRenderer<GluheyaBoss,GluheyaModel> {
    private static final ResourceLocation TEXTURE=
        ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/gluheya.png");
    public GluheyaRenderer(EntityRendererProvider.Context context){
        super(context,new GluheyaModel(),.85F);
    }
    @Override public ResourceLocation getTextureLocation(GluheyaBoss boss){return TEXTURE;}
    @Override protected boolean shouldShowName(GluheyaBoss boss){return false;}
}
