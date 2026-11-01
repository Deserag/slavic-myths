package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morovoy.MorovoyRaven;
import org.slavicmyths.morovoy.MorovoyRavenContent;

/** Unique four-wing boss rig; the smaller plague ravens use the identical rig at .28. */
public final class MorovoyRavenRenderer extends MobRenderer<MorovoyRaven,MorovoyRavenModel> {
    private static final ResourceLocation TEXTURE = MorovoyRavenContent.id("textures/entity/morovoy_raven.png");
    public MorovoyRavenRenderer(EntityRendererProvider.Context ctx) { super(ctx,new MorovoyRavenModel(),1.6F); }
    @Override public ResourceLocation getTextureLocation(MorovoyRaven raven) { return TEXTURE; }
    @Override protected void scale(MorovoyRaven raven,PoseStack matrices,float tick) {
        if (raven.minion()) matrices.scale(.28F,.28F,.28F);
    }
}
