package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morovoy.MorovoyRavenContent;
import org.slavicmyths.morovoy.RavenMiasma;

/** No opaque proxy block: the client draws bounded border particles in RavenMiasma.tick. */
public final class RavenMiasmaRenderer extends EntityRenderer<RavenMiasma> {
    private static final ResourceLocation TEXTURE = MorovoyRavenContent.id("textures/entity/morovoy_blight.png");
    public RavenMiasmaRenderer(EntityRendererProvider.Context ctx) { super(ctx); }
    @Override public ResourceLocation getTextureLocation(RavenMiasma entity) { return TEXTURE; }
    @Override public void render(RavenMiasma entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light) {}
}
