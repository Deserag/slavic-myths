package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.posad.PosadResidentEntity;

/** Own atlas and geometry; not a recolored vanilla villager or piglin. */
public final class PosadResidentRenderer extends MobRenderer<PosadResidentEntity,PosadResidentModel> {
    public PosadResidentRenderer(EntityRendererProvider.Context context) {
        super(context, new PosadResidentModel(), .45F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new RenderLayer<PosadResidentEntity,PosadResidentModel>(this) {
            @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, PosadResidentEntity entity, float walk, float amount, float partial, float age, float yaw, float pitch) {
                ResourceLocation glow = ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/posad_resident_" + entity.traits().textureVariant() + "_glow.png");
                getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(glow)), 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        });
    }
    @Override public ResourceLocation getTextureLocation(PosadResidentEntity entity) {
        int skin = entity.traits().textureVariant();
        return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/posad_resident_" + skin + ".png");
    }
}
