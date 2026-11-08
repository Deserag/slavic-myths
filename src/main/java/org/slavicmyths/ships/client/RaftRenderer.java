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

/** Original voxel shipyard style, all four raft sizes and functional upgrades. */
public final class RaftRenderer extends EntityRenderer<RaftBoatEntity> {
    private final BlockRenderDispatcher blocks;
    private final BlockState log = Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
    public RaftRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 1.7F;
    }

    private void piece(BlockState state, PoseStack pose, MultiBufferSource buffers, int light,
                       double x, double y, double z, float sx, float sy, float sz) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale(sx, sy, sz);
        blocks.renderSingleBlock(state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private void mast(PoseStack pose, MultiBufferSource buffers, int light, double cx, double cz,
                      double sway) {
        pose.pushPose();
        pose.translate(cx, .58D, cz);
        pose.mulPose(Axis.ZP.rotationDegrees((float)sway));
        piece(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), pose, buffers, light, -.16D, 0, -.16D, .32F, 4.45F, .32F);
        piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light, -1.18D, 3.95D, -.13D, 2.36F, .16F, .26F);
        piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light, -1.08D, 1.0D, -.13D, 2.16F, .14F, .26F);
        // Cream-coloured cloth with a broad red woven cross and visible borders.
        piece(Blocks.WHITE_WOOL.defaultBlockState(), pose, buffers, light, -1.04D, 1.15D, -.08D, 2.08F, 2.72F, .12F);
        piece(Blocks.RED_WOOL.defaultBlockState(), pose, buffers, light, -.09D, 1.36D, -.12D, .18F, 2.32F, .035F);
        piece(Blocks.RED_WOOL.defaultBlockState(), pose, buffers, light, -.76D, 2.56D, -.12D, 1.52F, .17F, .035F);
        // Rope stays from mast to hull corners.
        piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light, -.12D, .32D, -.13D, .24F, .3F, .26F);
        pose.popPose();
    }

    @Override
    public void render(RaftBoatEntity raft, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        // Light must be sampled ABOVE the waterline: vanilla Boat feet are partially
        // submerged, otherwise its entity light can flash as it moves through water.
        net.minecraft.core.BlockPos daylight = net.minecraft.core.BlockPos.containing(
                raft.getX(), raft.deckTop() + .8D, raft.getZ());
        light = net.minecraft.client.renderer.LightTexture.pack(
                raft.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, daylight),
                raft.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, daylight));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        int width = raft.deckWidth(), length = raft.deckLength();
        pose.translate(-width / 2D, -.08D, -length / 2D);
        for (int z = 0; z < length; z++) for (int x = 0; x < width; x++) {
            piece(log, pose, buffers, light, x + .03D, 0, z, .94F, .58F, 1F);
            int kind = raft.moduleKind(x, z);
            if (kind == 1) {
                // A hand-crafted oak ship chest: actual inventory state lives on the raft.
                piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        x + .12D, .58D, z + .12D, .76F, .68F, .76F);
                piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                        x + .09D, 1.28D, z + .09D, .82F, .12F, .82F);
                piece(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffers, light,
                        x + .46D, 1.00D, z + .045D, .09F, .21F, .09F);
            } else if (kind == 2) {
                piece(Blocks.BARREL.defaultBlockState(), pose, buffers, light,
                        x + .15D, .58D, z + .15D, .7F, .72F, .7F);
            } else if (kind == 3) {
                piece(Blocks.CRAFTING_TABLE.defaultBlockState(), pose, buffers, light,
                        x + .12D, .58D, z + .12D, .76F, .78F, .76F);
            } else if (kind == 4) {
                piece(Blocks.FURNACE.defaultBlockState(), pose, buffers, light,
                        x + .12D, .58D, z + .12D, .76F, .78F, .76F);
            }
        }
        // Visible bindings around every seam: spruce reinforcement looks handcrafted.
        for (int z = 0; z < length; z += 2) {
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light, .07D, .58D,
                    z + .25D, width - .14F, .10F, .18F);
        }
        for (int x = 0; x < width; x += 2) {
            piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                    x + .2D, -.02D, 0D, .16F, .15F, length);
        }
        if (raft.hasRails()) {
            // Strong log uprights and rails, rather than a flat decorative texture.
            for (int z = 0; z <= length; z += 2) for (int edge : new int[]{0, width}) {
                piece(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), pose, buffers, light,
                        edge == 0 ? 0D : width - .22D, .53D, Math.min(z, length - .22D), .22F, .92F, .22F);
            }
            for (int edge : new int[]{0, width}) {
                piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                        edge == 0 ? .035D : width - .14D, 1.20D, .04D, .11F, .16F, length - .08F);
            }
            for (int x = 0; x <= width; x += 2) {
                piece(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), pose, buffers, light,
                        Math.min(x, width - .22D), .53D, .04D, .22F, .92F, .22F);
            }
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    .04D, 1.20D, .04D, width - .08F, .16F, .11F);
        }
        int sails = raft.sailCount();
        for (int i = 0; i < sails; i++) {
            double z = length * (i + 1.0D) / (sails + 1.0D);
            mast(pose, buffers, light, width / 2D, z, Math.sin((raft.tickCount + tick) * .045D + i) * 3.5D);
        }
        if (raft.hasHelm()) {
            double z = length - 1.16D;
            piece(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), pose, buffers, light,
                    width / 2D - .18D, .58D, z, .36F, 1.2F, .36F);
            // An upright eight-spoke carved steering wheel, not an industrial mechanism.
            pose.pushPose();
            pose.translate(width / 2D, 1.78D, z - .08D);
            for (int spoke = 0; spoke < 8; spoke++) {
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(spoke * 45F));
                piece(Blocks.DARK_OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        -.07D, .10D, -.08D, .14F, .54F, .16F);
                piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        -.14D, .52D, -.11D, .28F, .15F, .22F);
                pose.popPose();
            }
            piece(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffers, light,
                    -.15D, -.15D, -.12D, .30F, .30F, .24F);
            pose.popPose();
        }
        // Native paddle state drives the shaft and broad wooden blades.
        for (int paddle = 0; paddle < 2; paddle++) {
            int side = paddle == 0 ? -1 : 1;
            float rowing = raft.getPaddleState(paddle) ? raft.getRowingTime(paddle, tick) : 0F;
            float sweep = (float)Math.sin(rowing) * (raft.getPaddleState(paddle) ? 20F : 0F);
            pose.pushPose();
            pose.translate(side < 0 ? -.05D : width + .05D, .41D, length / 2.0D);
            pose.mulPose(Axis.YP.rotationDegrees(side * sweep));
            pose.mulPose(Axis.ZP.rotationDegrees(side * 7F));
            piece(Blocks.STRIPPED_OAK_LOG.defaultBlockState(), pose, buffers, light,
                    side < 0 ? -1.30D : 0D, -.04D, -.11D, 1.30F, .12F, .22F);
            piece(Blocks.SPRUCE_PLANKS.defaultBlockState(), pose, buffers, light,
                    side < 0 ? -1.95D : 1.24D, -.12D, -.28D, .7F, .10F, .54F);
            pose.popPose();
        }
        if (raft.hasExtraOars()) {
            for (int side : new int[]{-1, 1})
                piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        side < 0 ? -1.6D : width, .35D, length / 2D + 1.1D, 1.6F, .11F, .24F);
        }
        pose.popPose();
        super.render(raft, yaw, tick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(RaftBoatEntity raft) { return TextureAtlas.LOCATION_BLOCKS; }
}
