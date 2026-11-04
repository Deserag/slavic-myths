package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.SisterBoss;

/**
 * Исправленный производственный рендерер для боссов Сёстер.
 * Корректно передает запеченный ModelPart слоя в обновленный конструктор SistersModel.
 */
public class SistersRenderer extends MobRenderer<SisterBoss, SistersModel> {
    
    // Пути к текстурам для обеих сестер
    private static final ResourceLocation TRYASEYA_TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/tryaseya_v14.png");
    private static final ResourceLocation GNETEYA_TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/gneteya_v5.png");
    private final boolean isTryaseya;

    public SistersRenderer(EntityRendererProvider.Context ctx, boolean tryaseya, ModelLayerLocation layerLocation) {
        // Вызываем конструктор суперкласса, запекая ModelPart из контекста и передавая его вместе с флагом tryaseya
        super(ctx, new SistersModel(ctx.bakeLayer(layerLocation), tryaseya), tryaseya ? 0.55F : 1.05F);
        this.isTryaseya = tryaseya;
    }

    /**
     * Возвращает правильную текстуру в зависимости от того, какая из сестер рендерится.
     */
    @Override
    public ResourceLocation getTextureLocation(SisterBoss entity) {
        return this.isTryaseya ? TRYASEYA_TEXTURE : GNETEYA_TEXTURE;
    }
}
