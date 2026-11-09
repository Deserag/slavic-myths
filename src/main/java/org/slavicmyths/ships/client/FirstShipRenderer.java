package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.RaftContent;

/**
 * The load-bearing hull stays vanilla chunk geometry. The actual REGISTERED
 * 3D ship_helm_module model is rendered at the stern from geometry V5 onwards.
 * It is attached to the synced ship heading, not hand-built from slab blocks.
 */
public final class FirstShipRenderer extends EntityRenderer<FirstShipEntity> {
    private final ItemRenderer items;
    private final ItemStack helm;

    public FirstShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        helm = new ItemStack(RaftContent.HELM_MODULE.get());
        shadowRadius = 0F;
    }

    @Override public void render(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        if (!ship.isNativeHull()) {
            super.render(ship, yaw, partialTick, pose, buffers, light);
            return;
        }
        if (ship.geometryVersion() < 5) return;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-ship.shipYaw()));
        pose.translate(0D, FirstShipHull.MAIN_DECK + 0.75D, -4D);
        pose.scale(1.9F, 1.9F, 1.9F);
        items.renderStatic(helm, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, ship.level(), ship.getId());
        pose.popPose();
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
