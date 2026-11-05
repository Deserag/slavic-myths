package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.LomeyaBoss;

/** Localized articulated renderer; no temporary Agneya or Lideya model substitutions. */
public final class LomeyaRenderer extends MobRenderer<LomeyaBoss,LomeyaModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/lomeya.png");
    public LomeyaRenderer(EntityRendererProvider.Context ctx) {super(ctx,new LomeyaModel(),1.1F);}
    @Override public ResourceLocation getTextureLocation(LomeyaBoss boss) {return TEXTURE;}
}
