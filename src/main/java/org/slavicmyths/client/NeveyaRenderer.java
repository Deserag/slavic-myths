package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.NeveyaBoss;

public final class NeveyaRenderer extends MobRenderer<NeveyaBoss,NeveyaModel>{
    private static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/neveya.png");
    public NeveyaRenderer(EntityRendererProvider.Context ctx){super(ctx,new NeveyaModel(),1.15F);}
    @Override public ResourceLocation getTextureLocation(NeveyaBoss boss){return TEX;}
    @Override protected boolean shouldShowName(NeveyaBoss boss) { return false; }

}
