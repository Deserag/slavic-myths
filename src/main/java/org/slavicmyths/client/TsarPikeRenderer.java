package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.TsarPikeContent;
import org.slavicmyths.marine.TsarPikeEntity;

/** Procedural V1 ModelPart fish; colors are baked per real cuboid material. */
public final class TsarPikeRenderer extends MobRenderer<TsarPikeEntity,TsarPikeModel> {
    private static final ResourceLocation WHITE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public TsarPikeRenderer(EntityRendererProvider.Context context){
        super(context,new TsarPikeModel(),1.25F);
    }
    @Override public ResourceLocation getTextureLocation(TsarPikeEntity entity){return WHITE;}
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(TsarPikeContent.BOSS.get(),TsarPikeRenderer::new);
    }
}
