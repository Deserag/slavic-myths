package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.posad.PosadResidentEntity;

/** Own atlas and geometry; not a recolored vanilla villager or piglin. */
public final class PosadResidentRenderer extends MobRenderer<PosadResidentEntity,PosadResidentModel> {
    public PosadResidentRenderer(EntityRendererProvider.Context context) {
        super(context, new PosadResidentModel(), .45F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    @Override public ResourceLocation getTextureLocation(PosadResidentEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/posad_resident_" + entity.traits().skin() + ".png");
    }
}
