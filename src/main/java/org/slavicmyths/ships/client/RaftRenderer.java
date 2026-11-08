package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import org.slavicmyths.ships.RaftBoatEntity;

/** Temporary source-backed 3x3 oak deck; deliberately not final owner-approved art. */
public final class RaftRenderer extends EntityRenderer<RaftBoatEntity> {
    private final BlockRenderDispatcher blocks;
    public RaftRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 1.4F;
    }
    @Override
    public void render(RaftBoatEntity raft, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        pose.translate(-1.5D, 0.08D, -1.5D);
        for (int z = 0; z < 3; z++) for (int x = 0; x < 3; x++) {
            pose.pushPose();
            pose.translate(x, 0, z);
            pose.scale(1F, 0.24F, 1F);
            blocks.renderSingleBlock(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
            if (raft.moduleKind(x, z) != 0) {
                pose.pushPose();
                pose.translate(x + 0.15D, 0.24D, z + 0.15D);
                pose.scale(.7F, .72F, .7F);
                blocks.renderSingleBlock(Blocks.BARREL.defaultBlockState(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
        }
        // Temporary broad oars attached to the port and starboard sides.
        for (int side : new int[]{-1, 1}) {
            pose.pushPose();
            pose.translate(side < 0 ? -1.18D : 3.02D, 0.06D, 1.43D);
            pose.scale(1.18F, .12F, .18F);
            blocks.renderSingleBlock(Blocks.STRIPPED_OAK_LOG.defaultBlockState(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        pose.popPose();
        super.render(raft, yaw, tick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(RaftBoatEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
