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
        int width = raft.raftSize().width, length = raft.raftSize().length;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        // Real visible thickness: log underside is near the waterline, top remains above it.
        // Do not derive the deck height from the floor of the fluid block.
        pose.translate(-width / 2.0D, -0.08D, -length / 2.0D);
        BlockState log = Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        for (int z = 0; z < length; z++) for (int x = 0; x < width; x++) {
            piece(log, pose, buffers, light, x + .03D, 0, z, .94F, .58F, 1F);
            int kind = raft.moduleKind(x, z);
            if (kind == 1) {
                // Lightweight chest stand-in: visually opaque, not a world BlockEntity.
                piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                        x + .14D, .58D, z + .14D, .72F, .66F, .72F);
                piece(Blocks.DARK_OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        x + .11D, 1.23D, z + .11D, .78F, .10F, .78F);
            } else if (kind == 3) {
                piece(Blocks.CRAFTING_TABLE.defaultBlockState(), pose, buffers, light,
                        x + .12D, .58D, z + .12D, .76F, .78F, .76F);
            } else if (kind == 2) {
                piece(Blocks.BARREL.defaultBlockState(), pose, buffers, light,
                        x + .15D, .58D, z + .15D, .7F, .68F, .7F);
            }
        }
        // Cross lashings make separate log sections read as a single raft.
        for (double z : new double[]{.3D, length - .48D}) {
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    .06D, .58D, z, width - .12F, .10F, .16F);
        }
        // Oars pivot at each side and stroke only when the vanilla paddles are active.
        for (int paddle = 0; paddle < 2; paddle++) {
            int side = paddle == 0 ? -1 : 1;
            float rowing = raft.getPaddleState(paddle) ? raft.getRowingTime(paddle, tick) : 0F;
            float sweep = (float)Math.sin(rowing) * (raft.getPaddleState(paddle) ? 17F : 0F);
            pose.pushPose();
            pose.translate(side < 0 ? -.05D : width + .05D, .37D, length / 2.0D);
            pose.mulPose(Axis.YP.rotationDegrees(side * sweep));
            pose.mulPose(Axis.ZP.rotationDegrees(side * 5F));
            piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                    side < 0 ? -1.16D : 0D, -.06D, -.075D, 1.16F, .10F, .15F);
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    side < 0 ? -1.7D : 1.08D, -.13D, -.24D, .62F, .08F, .48F);
            pose.popPose();
        }
        pose.popPose();
        super.render(raft, yaw, tick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(RaftBoatEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
