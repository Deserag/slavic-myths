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
 * Voxel ship, not a flat raft mesh: keel, curved wooden sides, hold,
 * raised weather deck with real hatch, ladder, beams, mast, sail and helm.
 * Collision math is shared with FirstShipHull rather than invented here.
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

    private void piece(BlockState state, PoseStack pose, MultiBufferSource buffers,
            int light, double x, double y, double z, double sx, double sy, double sz) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale((float)sx, (float)sy, (float)sz);
        blocks.renderSingleBlock(state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Override public void render(FirstShipEntity ship, float yaw, float partial,
            PoseStack pose, MultiBufferSource buffers, int light) {
        BlockPos daylight = BlockPos.containing(ship.getX(), ship.deckY() + .85D, ship.getZ());
        light = LightTexture.pack(
                ship.level().getBrightness(LightLayer.BLOCK, daylight),
                ship.level().getBrightness(LightLayer.SKY, daylight));
        pose.pushPose();
        // Matches FirstShipHull.toWorld() for X/Z and the stern/helm offset.
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        double hold = FirstShipHull.HOLD_FLOOR, deck = FirstShipHull.MAIN_DECK;

        // Curved clinker hull: four overlapping plank strakes, taller toward beam.
        for (int zi = 0; zi < FirstShipHull.LENGTH; zi++) {
            double z = zi - 5D;
            double beamWidth = FirstShipHull.halfBeam(z);
            double nextWidth = FirstShipHull.halfBeam(z + .46D);
            double half = Math.min(beamWidth, nextWidth) - .09D;
            if (half <= 0D) continue;
            piece(dark, pose, buffers, light, -half + .32D, FirstShipHull.KEEL,
                    z, 2D * (half - .32D), .32D, .94D);
            for (int layer = 0; layer < 4; layer++) {
                double y = FirstShipHull.KEEL + .26D + layer * .48D;
                double shape = Math.max(.25D, half - (3 - layer) * .22D);
                for (int side : new int[]{-1, 1}) {
                    double xx = side < 0 ? -shape : shape - .27D;
                    piece(layer % 2 == 0 ? oak : spruce, pose, buffers, light,
                            xx, y, z - .04D, .27D, .49D, 1.05D);
                }
            }
            // High upper sides form a REAL visually enclosed lower cabin.
            for (int side : new int[]{-1, 1}) {
                double xx = side < 0 ? -half : half - .24D;
                piece(oak, pose, buffers, light,
                        xx, hold + .12D, z, .24D, deck - hold, .96D);
                piece(dark, pose, buffers, light,
                        xx - .045D, deck - .13D, z, .33D, .20D, 1.03D);
                if (zi % 2 == 0) {
                    piece(beam, pose, buffers, light,
                            side < 0 ? xx + .05D : xx, hold + .10D, z + .10D,
                            .23D, deck - hold + .48D, .23D);
                }
            }
            // Bow and stern join both gunwales; no open rectangular raft ends.
            if (zi == 0 || zi == FirstShipHull.LENGTH - 1) {
                piece(oak, pose, buffers, light, -half, hold + .3D, z,
                        half * 2D, deck - hold + .05D, .23D);
            }
        }

        // Internal cargo floor and real weather deck, separated by 2.8 blocks.
        for (int zi = 0; zi < FirstShipHull.LENGTH; zi++)
            for (int xi = 0; xi < FirstShipHull.WIDTH; xi++) {
                double x = xi - 3D, z = zi - 5D;
                if (!FirstShipHull.withinHull(x, z, .32D)) continue;
                piece(spruce, pose, buffers, light,
                        x - .46D, hold - .17D, z - .46D, .92D, .17D, .92D);
                if (FirstShipHull.mainDeckSupports(x, z)) {
                    piece(oak, pose, buffers, light,
                            x - .46D, deck - .17D, z - .46D, .92D, .17D, .92D);
                }
                if (xi % 2 == 0) {
                    piece(dark, pose, buffers, light,
                            x - .45D, hold - .23D, z - .45D, .12D, .08D, .90D);
                }
                for (int floor = 0; floor <= 1; floor++) {
                    int module = ship.moduleKind(floor, xi, zi);
                    double fy = floor == 0 ? hold : deck;
                    BlockState moduleState = switch (module) {
                        case 1 -> Blocks.CHEST.defaultBlockState();
                        case 2 -> Blocks.BARREL.defaultBlockState();
                        case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                        case 4 -> Blocks.FURNACE.defaultBlockState();
                        default -> null;
                    };
                    if (moduleState != null) {
                        piece(moduleState, pose, buffers, light,
                                x - .34D, fy, z - .34D, .68D, .72D, .68D);
                    }
                }
            }

        // Large transverse rib beams below the weather deck.
        for (int zi = -3; zi <= 3; zi += 3) {
            double half = FirstShipHull.halfBeam(zi) - .32D;
            piece(beam, pose, buffers, light, -half, deck - .41D, zi - .11D,
                    half * 2D, .24D, .22D);
        }

        // Hatch frame (2x2) and wooden ladder descending to the hold.
        for (double xx : new double[]{-.99D, 1.07D}) {
            piece(dark, pose, buffers, light, xx, deck - .18D, 1.0D, .16D, .28D, 2.1D);
        }
        for (double zz : new double[]{1.0D, 2.97D}) {
            piece(dark, pose, buffers, light, -1D, deck - .18D, zz, 2.22D, .28D, .16D);
        }
        for (double xx : new double[]{FirstShipHull.LADDER_X - .34D, FirstShipHull.LADDER_X + .34D}) {
            piece(beam, pose, buffers, light, xx, hold, FirstShipHull.LADDER_Z,
                    .12D, deck - hold, .13D);
        }
        for (double yy = hold + .28D; yy < deck; yy += .33D) {
            piece(oak, pose, buffers, light,
                    FirstShipHull.LADDER_X - .34D, yy, FirstShipHull.LADDER_Z,
                    .80D, .10D, .14D);
        }

        // Carved mast, yardarms, embroidered linen sail and rope-like braces.
        double mast = FirstShipHull.MAST_Z;
        piece(beam, pose, buffers, light, -.19D, deck, mast - .19D,
                .38D, 6.35D, .38D);
        piece(dark, pose, buffers, light, -2.34D, deck + 5.60D, mast - .11D,
                4.68D, .22D, .22D);
        piece(dark, pose, buffers, light, -2.20D, deck + 1.27D, mast - .11D,
                4.40D, .19D, .22D);
        piece(cloth, pose, buffers, light, -2.1D, deck + 1.51D, mast -.035D,
                4.20D, 3.99D, .11D);
        piece(motif, pose, buffers, light, -.13D, deck + 1.72D, mast - .06D,
                .26D, 3.57D, .03D);
        piece(motif, pose, buffers, light, -1.65D, deck + 3.55D, mast - .06D,
                3.30D, .26D, .03D);

        // Stern helmsman's console: clearly separated from the mast and hatch.
        double helmZ = FirstShipHull.HELM_Z;
        piece(beam, pose, buffers, light, -.20D, deck, helmZ - .14D,
                .4D, 1.30D, .36D);
        pose.pushPose();
        pose.translate(0D, deck + 1.48D, helmZ - .06D);
        for (int n = 0; n < 8; n++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(n * 45F));
            piece(dark, pose, buffers, light, -.065D, .08D, -.06D,
                    .13D, .58D, .12D);
            piece(oak, pose, buffers, light, -.14D, .57D, -.09D,
                    .28D, .16D, .19D);
            pose.popPose();
        }
        piece(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffers, light,
                -.14D, -.14D, -.12D, .28D, .28D, .24D);
        pose.popPose();

        // Elevated bulwark rails; not vanilla fences on a raft.
        for (int zi = -4; zi <= 4; zi += 2) {
            double hw = FirstShipHull.halfBeam(zi) - .12D;
            for (int s : new int[]{-1, 1}) {
                piece(beam, pose, buffers, light, s < 0 ? -hw : hw - .20D,
                        deck, zi, .20D, FirstShipHull.RAIL_HEIGHT, .20D);
            }
        }
        for (int zi = -5; zi <= 4; zi++) {
            double hw = FirstShipHull.halfBeam(zi + .4D) - .15D;
            for (int s : new int[]{-1, 1}) {
                piece(spruce, pose, buffers, light, s < 0 ? -hw : hw - .16D,
                        deck + .76D, zi, .16D, .17D, 1.0D);
            }
        }

        pose.popPose();
        super.render(ship, yaw, partial, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
