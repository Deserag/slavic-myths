package org.slavicmyths.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.AgneyaBoss;

/** Testable live renderer for the approved Agneya V6 visual model. */
public final class AgneyaRenderer extends MobRenderer<AgneyaBoss, AgneyaModelV7> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/agneya_v6.png");
    public AgneyaRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer) {
        super(ctx, new AgneyaModelV7(ctx.bakeLayer(layer)), 1.05F);
    }
    @Override public ResourceLocation getTextureLocation(AgneyaBoss entity) { return TEX; }
}