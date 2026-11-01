package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.SisterBoss;

/** Separate original albedo atlases and alpha-masked emissive eyes/lantern. */
public final class SistersRenderer extends MobRenderer<SisterBoss,SistersModel> {
    private static final ResourceLocation G_ALBEDO=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/gneteya_albedo.png");
    private static final ResourceLocation T_ALBEDO=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/tryaseya_albedo.png");
    private static final ResourceLocation G_EMISSIVE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/gneteya_emissive.png");
    private static final ResourceLocation T_EMISSIVE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/tryaseya_emissive.png");
    private final boolean tryaseya;

    public SistersRenderer(EntityRendererProvider.Context ctx,boolean tryaseya) {
        super(ctx,new SistersModel(tryaseya),tryaseya?.55F:1.05F);
        this.tryaseya=tryaseya;
        ResourceLocation emissive=tryaseya?T_EMISSIVE:G_EMISSIVE;
        addLayer(new RenderLayer<SisterBoss,SistersModel>(this) {
            @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,SisterBoss boss,float walk,float amount,float partial,float age,float yaw,float pitch) {
                getParentModel().renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(emissive)),15728880,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
            }
        });
    }

    @Override public ResourceLocation getTextureLocation(SisterBoss boss) {return tryaseya?T_ALBEDO:G_ALBEDO;}
    @Override protected RenderType getRenderType(SisterBoss boss,boolean visible,boolean translucent,boolean glowing) {
        return tryaseya?RenderType.entityTranslucent(T_ALBEDO):super.getRenderType(boss,visible,translucent,glowing);
    }
}
