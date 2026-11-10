package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.ShipSuperstructurePlan;
import org.slavicmyths.ships.ShipSuperstructureData;
import org.slavicmyths.ships.RaftContent;

/**
 * Stationary hull: actual vanilla chunk blocks. Moving hull: local vanilla
 * block PALETTE rendered with the vessel's continuous interpolated pose.
 * This mirrors the essential Create contraption transform without requiring
 * the Create mod, and never renders scaled "fake" floors while stationary.
 *
 * Container inventory NBT is server-only; clients see just type/position.
 * The model is a working correctness prototype, NOT a final GPU-batched
 * renderer: per-block calls can be expensive on larger ships.
 */
public final class FirstShipRenderer extends EntityRenderer<FirstShipEntity> {
    private static final ResourceLocation SAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "slavicmyths", "textures/block/linen_cover.png");
    private final ItemRenderer items;
    private final ItemStack helm;
    private String paletteKey = "";
    private Map<BlockPos, BlockState> cachedPalette = Map.of();

    public FirstShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        helm = new ItemStack(RaftContent.HELM_MODULE.get());
        shadowRadius = 0F;
    }


    private Map<BlockPos, BlockState> movingPalette(FirstShipEntity ship) {
        String key = ship.geometryVersion() + ":" + ship.rigLevel() + ":"
                + ship.fabricQuality() + ":" + ship.movingCargoView()
                + ":" + (ship.superstructure() == null ? "" : ShipSuperstructureData.sync(ship.superstructure()));
        if (key.equals(paletteKey)) return cachedPalette;
        ListTag cargo = new ListTag();
        String types = ship.movingCargoView();
        if (!types.isBlank()) {
            for (String item : types.split(";")) {
                try {
                    String[] parts = item.split(",");
                    if (parts.length != 4) continue;
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("X", Integer.parseInt(parts[0]));
                    entry.putInt("Y", Integer.parseInt(parts[1]));
                    entry.putInt("Z", Integer.parseInt(parts[2]));
                    entry.putInt("Kind", Integer.parseInt(parts[3]));
                    entry.put("BlockEntity", new CompoundTag());
                    cargo.add(entry);
                } catch (NumberFormatException invalid) {
                    // A bad cosmetic token must never crash world rendering.
                }
            }
        }
        try {
            var snapshot = new FirstShipBlockHull.MotionSnapshot(cargo,
                    ship.geometryVersion(), ship.rigLevel(), ship.fabricQuality(), 0,
                    ship.superstructure() == null ? new CompoundTag()
                            : ShipSuperstructureData.save(ship.superstructure()));
            cachedPalette = FirstShipBlockHull.localMotionPalette(snapshot);
        } catch (RuntimeException invalid) {
            cachedPalette = Map.of();
        }
        paletteKey = key;
        return cachedPalette;
    }

    @Override public void render(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        if (!ship.isNativeHull()) {
            super.render(ship, yaw, partialTick, pose, buffers, light);
            return;
        }
        if (ship.isMoving()) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-yaw));
            Map<BlockPos, BlockState> palette = movingPalette(ship);
            for (var entry : palette.entrySet()) {
                BlockState state = entry.getValue();
                if (state.isAir()) continue;
                BlockPos p = entry.getKey();
                pose.pushPose();
                pose.translate(p.getX() - .5D, p.getY(), p.getZ() - .5D);
                if (state.is(Blocks.CHEST)) {
                    items.renderStatic(new ItemStack(Items.CHEST),
                            ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                            pose, buffers, ship.level(), ship.getId());
                } else Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
            pose.popPose();
        }
        if (ship.geometryVersion() < 5) return;
        BlockPos daylight = BlockPos.containing(ship.getX(), ship.deckY() + 2D, ship.getZ());
        int deckLight = net.minecraft.client.renderer.LightTexture.pack(
                ship.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, daylight),
                ship.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, daylight));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        ShipSuperstructurePlan structure = ship.superstructure();
        int helmY = ship.isVoxelShip() && structure.bridge() ? 10 : 5;
        int helmZ = ship.isVoxelShip() ? -structure.hullLength() / 2
                + (structure.bridge() ? 4 : structure.cabin() ? 9 : 3) : -4;
        pose.translate(0D, helmY, helmZ);
        // The same eight-spoke wooden steering wheel already used by rafts.
        // Render as real block-textured geometry, not the dark item inventory mesh.
        piece(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), pose, buffers, deckLight,
                -.18D, .12D, .08D, .36F, 1.35F, .36F);
        pose.pushPose();
        pose.translate(0D, 1.78D, 0D);
        for (int spoke = 0; spoke < 8; spoke++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(spoke * 45F));
            piece(Blocks.DARK_OAK_PLANKS.defaultBlockState(), pose, buffers, deckLight,
                    -.07D, .10D, -.08D, .14F, .54F, .16F);
            piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, deckLight,
                    -.14D, .52D, -.11D, .28F, .15F, .22F);
            pose.popPose();
        }
        piece(Blocks.IRON_BLOCK.defaultBlockState(), pose, buffers, deckLight,
                -.15D, -.15D, -.12D, .30F, .30F, .24F);
        pose.popPose();
        pose.popPose();
        if (ship.isVoxelShip())
            renderVoxelFabricSails(ship, yaw, partialTick, pose, buffers, deckLight);
        else if (ship.geometryVersion() >= 7 && ship.rigLevel() > 0)
            renderFabricSail(ship, yaw, partialTick, pose, buffers, deckLight);
    }

    private void renderVoxelFabricSails(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        ShipSuperstructurePlan design = ship.superstructure();
        if (design == null) return;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        for (int i = 0; i < design.sailTiers().size(); i++) {
            int tier = design.sailTiers().get(i);
            int top = tier == 1 ? 12 : tier == 2 ? 15 : 19;
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            BlockState seam = tier == 1 ? Blocks.RED_WOOL.defaultBlockState()
                    : tier == 2 ? Blocks.BLUE_WOOL.defaultBlockState()
                    : Blocks.BROWN_WOOL.defaultBlockState();
            pose.pushPose();
            pose.translate(0D, 0D, design.mastPositions().get(i) + 1D);
            sailPanel(ship, partialTick, pose, buffers, light, seam, 8D, top, span, tier);
            pose.popPose();
        }
        pose.popPose();
    }

    /** Thin, bowed cloth panels. The V7 layout retains physical mast and yards. */
    private void renderFabricSail(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        BlockState seam = (ship.fabricQuality() == 2 ? Blocks.BLUE_WOOL
                : ship.fabricQuality() == 3 ? Blocks.BROWN_WOOL
                : Blocks.RED_WOOL).defaultBlockState();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        int rig = ship.rigLevel();
        if (rig == 1) {
            int top = ship.geometryVersion() >= 8 && ship.fabricQuality() > 1
                    ? (ship.fabricQuality() == 2 ? 13 : 14) : 12;
            int span = ship.geometryVersion() >= 8 && ship.fabricQuality() == 3 ? 4 : 3;
            sailPanel(ship, partialTick, pose, buffers, light, seam, 7D, top, span);
        }
        else if (rig == 2) {
            sailPanel(ship, partialTick, pose, buffers, light, seam, 8D, 10D, 3);
            int top = ship.geometryVersion() >= 8
                    ? ship.fabricQuality() == 3 ? 19 : ship.fabricQuality() == 2 ? 16 : 15
                    : 15;
            int span = ship.geometryVersion() >= 8 && ship.fabricQuality() == 3 ? 4
                    : ship.geometryVersion() >= 8 && ship.fabricQuality() == 2 ? 3 : 2;
            sailPanel(ship, partialTick, pose, buffers, light, seam, 13D, top, span);
        } else {
            int top = ship.geometryVersion() >= 8 && rig == 3
                    ? ship.fabricQuality() == 3 ? 19 : ship.fabricQuality() == 2 ? 17 : 15
                    : 15;
            int span = ship.geometryVersion() >= 8 && rig == 3 && ship.fabricQuality() == 3 ? 4 : 3;
            sailPanel(ship, partialTick, pose, buffers, light, seam, 8D, top, span);
            if (rig >= 4) {
                int upperSpan = ship.geometryVersion() >= 8 ? ship.fabricQuality() == 3 ? 4
                        : ship.fabricQuality() == 2 ? 3 : 2 : 2;
                int upperTop = ship.geometryVersion() >= 8 && ship.fabricQuality() >= 2 ? 21 : 20;
                sailPanel(ship, partialTick, pose, buffers, light, seam, 18D, upperTop, upperSpan);
            }
        }
        pose.popPose();
    }

    private void sailPanel(FirstShipEntity ship, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, BlockState seam,
            double bottom, double top, int span) {
        sailPanel(ship, partialTick, pose, buffers, light, seam,
                bottom, top, span, ship.fabricQuality());
    }

    private void sailPanel(FirstShipEntity ship, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, BlockState seam,
            double bottom, double top, int span, int quality) {
        double height = top - bottom + 1D;
        VertexConsumer fabric = buffers.getBuffer(RenderType.entityCutoutNoCull(SAIL_TEXTURE));
        float tintR = quality == 2 ? .82F : 1F;
        float tintG = quality == 3 ? .86F : 1F;
        float tintB = quality == 3 ? .58F : 1F;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 8; column++) {
                sailVertex(fabric, pose, ship, partialTick, light, tintR, tintG, tintB,
                        column, row, bottom, height, span);
                sailVertex(fabric, pose, ship, partialTick, light, tintR, tintG, tintB,
                        column + 1, row, bottom, height, span);
                sailVertex(fabric, pose, ship, partialTick, light, tintR, tintG, tintB,
                        column + 1, row + 1, bottom, height, span);
                sailVertex(fabric, pose, ship, partialTick, light, tintR, tintG, tintB,
                        column, row + 1, bottom, height, span);
            }
        }
        // Boltrope and lashing points remain visible from either side.
        for (double x : new double[]{-span, span})
            piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                    x, bottom, .16D, .055F, (float)height, .055F);
        for (double y : new double[]{bottom, bottom + height - .08D})
            piece(seam, pose, buffers, light, -span, y, .17D,
                    (float)(span * 2D), .08F, .025F);
    }

    private void sailVertex(VertexConsumer out, PoseStack pose, FirstShipEntity ship,
            float partialTick, int light, float red, float green, float blue,
            int column, int row, double bottom, double height, int span) {
        float u = column / 8F, v = row / 5F;
        double x = span * (2D * u - 1D);
        double y = bottom + height * v;
        double widthShape = 1D - (x * x) / (span * span);
        double heightShape = Math.sin(Math.PI * v);
        double belly = .12D + .95D * widthShape * heightShape;
        double wind = Math.sin((ship.tickCount + partialTick) * .075D + x * .6D) * .045D
                * heightShape;
        float slopeX = (float)(-.95D * 2D * x / (span * span) * heightShape);
        float slopeY = (float)(.95D * widthShape * Math.PI * Math.cos(Math.PI * v)
                / height);
        out.addVertex(pose.last().pose(), (float)x, (float)y, (float)(belly + wind))
                .setColor(red, green, blue, 1F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), -slopeX, -slopeY, 1F);
    }

    private void piece(BlockState state, PoseStack pose, MultiBufferSource buffers, int light,
                       double x, double y, double z, float sx, float sy, float sz) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale(sx, sy, sz);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Override public boolean shouldRender(FirstShipEntity ship,
            net.minecraft.client.renderer.culling.Frustum frustum,
            double cameraX, double cameraY, double cameraZ) {
        // Entity's vanilla boat AABB is much smaller than the 7x11 ship and
        // can be outside the camera while its stern wheel remains visible.
        return ship.isNativeHull()
                ? frustum.isVisible(ship.getBoundingBox().inflate(9D, 16D, 9D))
                : super.shouldRender(ship, frustum, cameraX, cameraY, cameraZ);
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
