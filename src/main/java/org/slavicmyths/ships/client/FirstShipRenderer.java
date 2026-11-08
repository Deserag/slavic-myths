package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;

/**
 * Hand-built clinker-planked first ship, not the previous square storage box.
 * Geometry matches FirstShipHull for bow/stern shape, two floors, hatch and helm.
 * All details are live block-textured voxel cuboids, not generated imagery.
 */
public final class FirstShipRenderer extends EntityRenderer<FirstShipEntity> {
    private final BlockRenderDispatcher blocks;
    private final BlockState oak = Blocks.OAK_PLANKS.defaultBlockState();
    private final BlockState spruce = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private final BlockState dark = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private final BlockState beam = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private final BlockState cloth = Blocks.WHITE_WOOL.defaultBlockState();
    private final BlockState motif = Blocks.RED_WOOL.defaultBlockState();

    public FirstShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 3.1F;
    }

    private void piece(BlockState state, PoseStack pose, MultiBufferSource buffers, int light,
            double x, double y, double z, double sx, double sy, double sz) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale((float)sx, (float)sy, (float)sz);
        blocks.renderSingleBlock(state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Override
    public void render(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        BlockPos lightPos = BlockPos.containing(ship.getX(), ship.deckY() + .85D, ship.getZ());
        light = LightTexture.pack(
                ship.level().getBrightness(LightLayer.BLOCK, lightPos),
                ship.level().getBrightness(LightLayer.SKY, lightPos));
        pose.pushPose();
        // +Z is the bow; this quaternion corresponds to FirstShipHull.toWorld().
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        double deck = FirstShipHull.MAIN_DECK, hold = FirstShipHull.HOLD_FLOOR;

        // Nine overlapping courses with increasing beam toward the upper deck:
        // narrow underwater keel, full beam above water, fine bow and broad stern.
        for (int zi = 0; zi < FirstShipHull.LENGTH; zi++) {
            double z = zi - 5.0D;
            double half = Math.max(.33D, Math.min(
                    FirstShipHull.halfBeam(z), FirstShipHull.halfBeam(z + .4D)) - .05D);
            piece(dark, pose, buffers, light, -half * .36D, FirstShipHull.KEEL - .03D,
                    z - .03D, half * .72D, .32D, 1.06D);
            for (int layer = 0; layer < 9; layer++) {
                double y = FirstShipHull.KEEL + .16D + layer * .46D;
                double spread = .60D + .40D * Math.pow(layer / 8D, .86D);
                double width = Math.max(.36D, half * spread);
                for (int side : new int[]{-1, 1}) {
                    double x = side < 0 ? -width : width - .24D;
                    piece(layer % 3 == 1 ? spruce : oak, pose, buffers, light,
                            x, y, z - .055D, .25D, .53D, 1.05D);
                    if (layer == 2 || layer == 5 || layer == 8) {
                        piece(dark, pose, buffers, light,
                                side < 0 ? x - .01D : x - .015D,
                                y + .49D, z - .05D, .285D, .055D, 1.04D);
                    }
                }
            }
            // Visible internal frames every two longitudinal blocks.
            if (zi % 2 == 0) {
                for (int side : new int[]{-1, 1}) {
                    double x = side < 0 ? -half * .83D : half * .83D - .15D;
                    piece(beam, pose, buffers, light, x, hold - .15D, z + .08D,
                            .15D, deck - hold + .30D, .21D);
                }
            }
            // Planked and closed end bulkheads, not an untextured dark wall.
            if (zi == 0 || zi == FirstShipHull.LENGTH - 1) {
                double tip = half * .70D;
                for (int n = 0; n < 6; n++) {
                    piece(n % 2 == 0 ? spruce : oak, pose, buffers, light,
                            -tip, hold - .06D + n * .45D, z - .07D,
                            tip * 2D, .46D, .18D);
                }
            }
        }

        // Raised prow and carved sternpost clearly distinguish ship from raft.
        piece(beam, pose, buffers, light, -.23D, FirstShipHull.KEEL - .12D,
                5.02D, .46D, deck - FirstShipHull.KEEL + 1.05D, .46D);
        piece(dark, pose, buffers, light, -.29D, deck + .55D,
                5.16D, .58D, .28D, .75D);
        piece(beam, pose, buffers, light, -.29D, FirstShipHull.KEEL - .25D,
                -5.45D, .58D, deck - FirstShipHull.KEEL + .92D, .46D);

        // REAL two-level visual construction: floor below at 0.42, deck at 3.22.
        // Plank seams are 0.02 block wide instead of 0.08 holes across both floors.
        for (int zi = 0; zi < FirstShipHull.LENGTH; zi++) {
            for (int xi = 0; xi < FirstShipHull.WIDTH; xi++) {
                double x = xi - 3D, z = zi - 5D;
                if (!FirstShipHull.withinHull(x, z, .32D)) continue;
                if (FirstShipHull.holdSupports(x, z)) {
                    piece(spruce, pose, buffers, light,
                            x - .49D, hold - .17D, z - .49D, .98D, .17D, .98D);
                    if (xi % 2 == 0) {
                        piece(dark, pose, buffers, light,
                                x - .46D, hold - .22D, z - .49D, .10D, .06D, .98D);
                    }
                }
                if (FirstShipHull.mainDeckSupports(x, z)) {
                    piece(zi % 2 == 0 ? oak : spruce, pose, buffers, light,
                            x - .49D, deck - .18D, z - .49D, .98D, .18D, .98D);
                    if (zi % 2 == 0) {
                        piece(dark, pose, buffers, light,
                                x - .49D, deck - .185D, z - .49D, .98D, .02D, .035D);
                    }
                }
                for (int floor = 0; floor <= 1; floor++) {
                    if (floor == 0 ? !FirstShipHull.holdSupports(x, z)
                            : !FirstShipHull.mainDeckSupports(x, z)) continue;
                    int module = ship.moduleKind(floor, xi, zi);
                    double foot = floor == 0 ? hold : deck;
                    BlockState model = switch (module) {
                        case 1 -> Blocks.CHEST.defaultBlockState();
                        case 2 -> Blocks.BARREL.defaultBlockState();
                        case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                        case 4 -> Blocks.FURNACE.defaultBlockState();
                        default -> null;
                    };
                    if (model != null) {
                        piece(model, pose, buffers, light,
                                x - .34D, foot, z - .34D, .68D, .72D, .68D);
                    }
                }
            }
        }

        // Supporting transverse ribs below the upper deck, kept within the hold.
        for (int z : new int[]{-3, 0, 3}) {
            double half = FirstShipHull.holdBeam(z) - .18D;
            piece(beam, pose, buffers, light,
                    -half, deck - .41D, z - .10D, half * 2D, .24D, .19D);
        }

        // 2x2 hatch and attached rung ladder down to lower hold.
        for (double x : new double[]{-.99D, 1.07D}) {
            piece(dark, pose, buffers, light, x, deck - .18D,
                    1D, .16D, .28D, 2.10D);
        }
        for (double z : new double[]{1D, 2.97D}) {
            piece(dark, pose, buffers, light, -1D, deck - .18D,
                    z, 2.22D, .28D, .16D);
        }
        for (double x : new double[]{FirstShipHull.LADDER_X - .34D, FirstShipHull.LADDER_X + .34D}) {
            piece(beam, pose, buffers, light, x, hold, FirstShipHull.LADDER_Z,
                    .12D, deck - hold, .13D);
        }
        for (double y = hold + .28D; y < deck; y += .33D) {
            piece(oak, pose, buffers, light,
                    FirstShipHull.LADDER_X - .34D, y, FirstShipHull.LADDER_Z,
                    .80D, .10D, .14D);
        }

        // A tall but not oversized mast with a slightly billowing cloth sail.
        double mast = FirstShipHull.MAST_Z;
        piece(beam, pose, buffers, light, -.18D, deck, mast - .18D,
                .36D, 6.45D, .36D);
        piece(dark, pose, buffers, light, -2.28D, deck + 5.55D,
                mast - .10D, 4.56D, .19D, .24D);
        piece(dark, pose, buffers, light, -2.18D, deck + 1.18D,
                mast - .10D, 4.36D, .18D, .23D);
        for (int strip = 0; strip < 10; strip++) {
            double t = strip / 9.0D;
            double width = 3.45D + .65D * Math.sin(Math.PI * t);
            double bow = -.06D - .37D * Math.sin(Math.PI * t);
            piece(cloth, pose, buffers, light,
                    -width / 2D, deck + 1.42D + strip * .40D,
                    mast + bow, width, .40D, .12D);
        }
        piece(motif, pose, buffers, light, -.11D, deck + 1.55D,
                mast - .52D, .22D, 3.5D, .06D);
        piece(motif, pose, buffers, light, -1.50D, deck + 3.35D,
                mast - .55D, 3D, .24D, .06D);

        // Carved helmsman's wheel at the stern, visible from the weather deck.
        double helm = FirstShipHull.HELM_Z;
        piece(beam, pose, buffers, light, -.17D, deck,
                helm - .10D, .34D, 1.20D, .34D);
        pose.pushPose();
        pose.translate(0D, deck + 1.35D, helm - .07D);
        for (int n = 0; n < 12; n++) {
            double angle = n * Math.PI / 6D;
            double xx = Math.sin(angle) * .61D, yy = Math.cos(angle) * .61D;
            piece(dark, pose, buffers, light,
                    xx - .09D, yy - .09D, 0D, .18D, .18D, .16D);
            if (n % 2 == 0) {
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(n * 30F));
                piece(oak, pose, buffers, light,
                        -.065D, .07D, -.05D, .13D, .60D, .13D);
                pose.popPose();
            }
        }
        piece(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffers, light,
                -.14D, -.14D, -.06D, .28D, .28D, .24D);
        pose.popPose();

        // Continuous arced gunwales and uprights instead of fence blocks.
        for (int z = -5; z <= 5; z++) {
            double half = FirstShipHull.halfBeam(z) - .10D;
            for (int side : new int[]{-1, 1}) {
                double x = side < 0 ? -half : half - .20D;
                if (z % 2 == 0) {
                    piece(beam, pose, buffers, light,
                            x, deck - .01D, z, .20D, FirstShipHull.RAIL_HEIGHT + .05D, .20D);
                }
                piece(spruce, pose, buffers, light,
                        x, deck + .78D, z - .05D, .20D, .19D, 1.02D);
            }
        }
        for (double end : new double[]{-5.25D, 5.17D}) {
            double half = FirstShipHull.halfBeam(end);
            piece(dark, pose, buffers, light,
                    -half, deck + .70D, end, half * 2D, .21D, .13D);
        }
        pose.popPose();
        super.render(ship, yaw, partialTick, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
