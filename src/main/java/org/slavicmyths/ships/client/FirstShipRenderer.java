package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.ships.FirstShipEntity;

/**
 * The marker is intentionally invisible. Minecraft renders the ACTUAL spruce
 * planks, oak deck, vanilla ladder and fences placed in world chunks. This
 * fixes camera-frustum disappearance, flickering textures and fake air gaps.
 */
public final class FirstShipRenderer extends EntityRenderer<FirstShipEntity> {
    public FirstShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0F;
    }

    @Override public void render(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        // Native world blocks are drawn by Minecraft's normal chunk renderer.
        // Legacy experimental entities have no world blocks. Only render their
        // warning nametag so old saves can still be located and repacked.
        if (!ship.isNativeHull())
            super.render(ship, yaw, partialTick, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
