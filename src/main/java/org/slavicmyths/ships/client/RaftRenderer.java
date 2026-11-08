package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.ships.RaftBoatEntity;

/** Clear 3x3 wooden log raft at the WATER SURFACE. Provisional art for gameplay QA. */
public final class RaftRenderer extends EntityRenderer<RaftBoatEntity> {
    private final BlockRenderDispatcher blocks;
    public RaftRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 1.5F;
    }
    private void piece(BlockState state, PoseStack pose, MultiBufferSource buffers, int light,
                       double x, double y, double z, float sx, float sy, float sz) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale(sx, sy, sz);
        blocks.renderSingleBlock(state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
    @Override
    public void render(RaftBoatEntity raft, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        // Coordinates below are local to the BACK-LEFT corner; entity pivot is deck centre.
        // Deck top is about 0.19 above the boat pivot; it must be ABOVE the water surface.
        pose.translate(-1.5D, -0.15D, -1.5D);
        BlockState log = Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        for (int z = 0; z < 3; z++) for (int x = 0; x < 3; x++) {
            piece(log, pose, buffers, light, x + .03D, 0, z, .94F, .34F, 1F);
            int kind = raft.moduleKind(x, z);
            if (kind == 1) {
                // Lightweight chest stand-in: visually opaque, not a world BlockEntity.
                piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                        x + .14D, .34D, z + .14D, .72F, .66F, .72F);
                piece(Blocks.DARK_OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        x + .11D, .99D, z + .11D, .78F, .10F, .78F);
            } else if (kind == 2) {
                piece(Blocks.BARREL.defaultBlockState(), pose, buffers, light,
                        x + .15D, .34D, z + .15D, .7F, .68F, .7F);
            }
        }
        // Cross lashings make separate log sections read as a single raft.
        for (double z : new double[]{.3D, 2.52D}) {
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    .06D, .34D, z, 2.88F, .10F, .16F);
        }
        // Two paired oars, one at each side; shaft and wide blade have distinct proportions.
        for (int side : new int[]{-1, 1}) {
            double shaftX = side < 0 ? -1.12D : 3.0D;
            double bladeX = side < 0 ? -1.55D : 4.0D;
            piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                    shaftX, .13D, 1.43D, 1.16F, .09F, .13F);
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    bladeX, .08D, 1.28D, .55F, .08F, .44F);
        }
        pose.popPose();
        super.render(raft, yaw, tick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(RaftBoatEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
