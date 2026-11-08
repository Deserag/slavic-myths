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
        // Modular guards and raised wooden rails. Rails surround each permitted hull size.
        if (raft.hasUpgrade(1)) {
            for (int z = 0; z < length; z += 2) for (int x : new int[]{0, width - 1}) {
                piece(Blocks.OAK_FENCE.defaultBlockState(), pose, buffers, light,
                        x + .31D, .58D, z + .31D, .38F, .75F, .38F);
            }
            for (int x = 0; x < width; x += 2) for (int z : new int[]{0, length - 1}) {
                piece(Blocks.OAK_FENCE.defaultBlockState(), pose, buffers, light,
                        x + .31D, .58D, z + .31D, .38F, .75F, .38F);
            }
            for (int x : new int[]{0, width - 1}) piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    x + .39D, 1.27D, .31D, .15F, .13F, length - .62F);
            for (int z : new int[]{0, length - 1}) piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    .35D, 1.27D, z + .39D, width - .7F, .13F, .15F);
        }
        if (raft.hasUpgrade(64)) piece(Blocks.DARK_OAK_LOG.defaultBlockState(), pose, buffers, light,
                .25D, -.13D, .25D, width - .5F, .18F, length - .5F);
        if (raft.hasUpgrade(16)) piece(Blocks.STONE_BRICKS.defaultBlockState(), pose, buffers, light,
                width / 2.0D - .5D, .58D, length / 2.0D - .5D, 1F, .25F, 1F);
        if (raft.hasUpgrade(32)) for (int x : new int[]{0, width - 1})
            piece(Blocks.LANTERN.defaultBlockState(), pose, buffers, light, x + .2D, 1.22D, .2D, .5F, .6F, .5F);
        // Rudder post: ordinary wooden controls instead of modern machinery.
        if (raft.hasUpgrade(8)) {
            double cx = width / 2.0D, cz = length - .75D;
            piece(Blocks.DARK_OAK_FENCE.defaultBlockState(), pose, buffers, light,
                    cx - .16D, .58D, cz - .15D, .32F, 1.12F, .32F);
            piece(Blocks.STRIPPED_OAK_LOG.defaultBlockState(), pose, buffers, light,
                    cx - .39D, 1.44D, cz - .34D, .78F, .15F, .17F);
            piece(Blocks.STRIPPED_OAK_LOG.defaultBlockState(), pose, buffers, light,
                    cx - .07D, 1.18D, cz - .34D, .14F, .76F, .17F);
        }
        // One or two sturdy masts, with white wool and red Slavic cloth ornaments.
        for (int i = 0; i < raft.sailCount(); i++) {
            double cz = raft.sailCount() == 1 ? length * .48D : length * (i == 0 ? .32D : .68D);
            double cx = width / 2.0D;
            piece(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), pose, buffers, light,
                    cx - .12D, .58D, cz - .12D, .24F, 3.9F, .24F);
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    cx - 1.2D, 3.99D, cz - .13D, 2.4F, .16F, .26F);
            piece(Blocks.WHITE_WOOL.defaultBlockState(), pose, buffers, light,
                    cx - 1.08D, 1.87D, cz - .07D, 2.16F, 2.08F, .13F);
            piece(Blocks.RED_WOOL.defaultBlockState(), pose, buffers, light,
                    cx - .09D, 2.40D, cz - .10D, .18F, 1.04F, .18F);
            piece(Blocks.RED_WOOL.defaultBlockState(), pose, buffers, light,
                    cx - .57D, 2.90D, cz - .10D, 1.14F, .16F, .18F);
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
        if (raft.hasUpgrade(128)) for (int side : new int[]{-1, 1}) {
            pose.pushPose();
            pose.translate(side < 0 ? -.05D : width + .05D, .37D, length * .26D);
            piece(Blocks.STRIPPED_OAK_LOG.defaultBlockState(), pose, buffers, light,
                    side < 0 ? -1.12D : 0D, -.06D, -.08D, 1.12F, .11F, .16F);
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    side < 0 ? -1.70D : 1.05D, -.13D, -.25D, .65F, .08F, .50F);
            pose.popPose();
        }
        pose.popPose();
        super.render(raft, yaw, tick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(RaftBoatEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
