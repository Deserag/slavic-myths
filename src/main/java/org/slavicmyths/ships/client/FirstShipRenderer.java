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
import org.slavicmyths.ships.ShipModularBlockHull;
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
        var config = ship.modularConfiguration();
        String key = ship.geometryVersion() + ":" + ship.rigLevel() + ":"
                + ship.fabricQuality() + ":" + ship.movingCargoView()
                + ":" + (config == null ? "" : config.wire());
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
            if (config != null) {
                Map<BlockPos, BlockState> visual = new java.util.LinkedHashMap<>(
                        ShipModularBlockHull.localPalette(config));
                for (int i = 0; i < cargo.size(); i++) {
                    CompoundTag entry = cargo.getCompound(i);
                    int x = entry.getInt("X"), y = entry.getInt("Y"), z = entry.getInt("Z");
                    if (Math.abs(x) > config.width() / 2 || y < 1 || y > 24
                            || Math.abs(z) > config.length() / 2) continue;
                    BlockState state = switch (entry.getInt("Kind")) {
                        case 1 -> Blocks.CHEST.defaultBlockState();
                        case 2 -> Blocks.BARREL.defaultBlockState();
                        case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                        case 4 -> Blocks.FURNACE.defaultBlockState();
                        default -> Blocks.AIR.defaultBlockState();
                    };
                    if (!state.isAir()) visual.putIfAbsent(new BlockPos(x, y, z), state);
                }
                cachedPalette = java.util.Collections.unmodifiableMap(visual);
            }
            else {
                var snapshot = new FirstShipBlockHull.MotionSnapshot(cargo,
                        ship.geometryVersion(), ship.rigLevel(), ship.fabricQuality(), 0);
                cachedPalette = FirstShipBlockHull.localMotionPalette(snapshot);
            }
        } catch (RuntimeException invalid) {
            cachedPalette = Map.of();
        }
        // Calculate hidden cells only when the ship palette changes. Doing
        // six neighbour lookups for every cube on every rendered frame would
        // replace GPU pressure with avoidable render-thread CPU work.
        if (!cachedPalette.isEmpty()) {
            Map<BlockPos, BlockState> source = cachedPalette;
            Map<BlockPos, BlockState> visible = new LinkedHashMap<>();
            for (var entry : source.entrySet())
                if (!hiddenOpaque(entry.getKey(), entry.getValue(), source))
                    visible.put(entry.getKey(), entry.getValue());
            cachedPalette = java.util.Collections.unmodifiableMap(visible);
        }
        paletteKey = key;
        return cachedPalette;
    }

    /**
     * An enclosed opaque cube contributes no visible faces. Unlike disabling
     * collision cells, renderer-only culling leaves cargo, physics and world
     * restoration untouched. Exclude stairs, rails, windows, chests and other
     * partial/interactive blocks from both the candidate and its neighbours.
     */
    private static boolean fullOpaque(BlockState state) {
        return state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.DARK_OAK_PLANKS)
                || state.is(Blocks.STRIPPED_SPRUCE_LOG)
                || state.is(Blocks.STRIPPED_DARK_OAK_LOG)
                || state.is(Blocks.DARK_OAK_LOG)
                || state.is(Blocks.WHITE_WOOL);
    }

    private static boolean hiddenOpaque(BlockPos pos, BlockState state,
            Map<BlockPos, BlockState> palette) {
        return fullOpaque(state)
                && fullOpaque(palette.getOrDefault(pos.above(), Blocks.AIR.defaultBlockState()))
                && fullOpaque(palette.getOrDefault(pos.below(), Blocks.AIR.defaultBlockState()))
                && fullOpaque(palette.getOrDefault(pos.north(), Blocks.AIR.defaultBlockState()))
                && fullOpaque(palette.getOrDefault(pos.south(), Blocks.AIR.defaultBlockState()))
                && fullOpaque(palette.getOrDefault(pos.east(), Blocks.AIR.defaultBlockState()))
                && fullOpaque(palette.getOrDefault(pos.west(), Blocks.AIR.defaultBlockState()));
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
        FirstShipHull.Point wheelWorld = FirstShipHull.toWorld(0D, ship.helmWheelZ(),
                ship.getX(), ship.getZ(), ship.isMoving() ? yaw : ship.shipYaw());
        BlockPos daylight = BlockPos.containing(wheelWorld.x(),
                ship.getY() + ship.helmSeatHeight() + 2D, wheelWorld.z());
        int deckLight = net.minecraft.client.renderer.LightTexture.pack(
                ship.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, daylight),
                ship.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, daylight));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        var config = ship.modularConfiguration();
        double helmZ = ship.helmWheelZ();
        double helmHeight = config == null ? FirstShipHull.MAIN_DECK : ship.helmSeatHeight();
        pose.translate(0D, helmHeight, helmZ);
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
        if (ship.modularConfiguration() != null)
            renderModularSails(ship, yaw, partialTick, pose, buffers, deckLight);
        else if (ship.geometryVersion() >= 7 && ship.rigLevel() > 0)
            renderFabricSail(ship, yaw, partialTick, pose, buffers, deckLight);
    }

    /** Separate thin wind-shaped sailcloth per mast; tier controls real size. */
    private void renderModularSails(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        var plan = ship.modularConfiguration().structure();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        for (int i = 0; i < plan.sailTiers().size(); i++) {
            int tier = plan.sailTiers().get(i);
            int top = tier == 1 ? 12 : tier == 2 ? 15 : 19;
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            BlockState edging = (tier == 1 ? Blocks.RED_WOOL
                    : tier == 2 ? Blocks.BLUE_WOOL : Blocks.BROWN_WOOL).defaultBlockState();
            pose.pushPose();
            pose.translate(0D, 0D, plan.mastPositions().get(i));
            double bottom = plan.shipClass() == org.slavicmyths.ships.ShipSectionPlan.ShipClass.MEDIUM
                    && plan.decks() >= 2 ? 10D : 8D;
            sailPanel(ship, partialTick, pose, buffers, light, edging, bottom, top, span);
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
            sailPanel(ship, partialTick, pose, buffers, light, seam, 13D, 15D, 2);
        } else {
            sailPanel(ship, partialTick, pose, buffers, light, seam, 8D, 15D, 3);
            if (rig >= 4) sailPanel(ship, partialTick, pose, buffers, light,
                    seam, 18D, 20D, 2);
        }
        pose.popPose();
    }

    private void sailPanel(FirstShipEntity ship, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, BlockState seam,
            double bottom, double top, int span) {
        double height = top - bottom + 1D;
        VertexConsumer fabric = buffers.getBuffer(RenderType.entityCutoutNoCull(SAIL_TEXTURE));
        float tintR = ship.fabricQuality() == 2 ? .82F : 1F;
        float tintG = ship.fabricQuality() == 3 ? .86F : 1F;
        float tintB = ship.fabricQuality() == 3 ? .58F : 1F;
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
        for (int row = 0; row < 5; row++) {
            double v = (row + .5D) / 5D;
            double edge = span * (.78D + .22D * Math.sin(Math.PI * v));
            for (double x : new double[]{-edge, edge})
                piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        x, bottom + row * height / 5D, .16D,
                        .055F, (float)(height / 5D), .055F);
        }
        for (double y : new double[]{bottom, bottom + height - .08D})
            piece(seam, pose, buffers, light, -span * .78D, y, .17D,
                    (float)(span * 1.56D), .08F, .025F);
    }

    private void sailVertex(VertexConsumer out, PoseStack pose, FirstShipEntity ship,
            float partialTick, int light, float red, float green, float blue,
            int column, int row, double bottom, double height, int span) {
        float u = column / 8F, v = row / 5F;
        double shoulder = .78D + .22D * Math.sin(Math.PI * v);
        double x = span * shoulder * (2D * u - 1D);
        double y = bottom + height * v;
        double widthShape = 1D - (x * x) / (span * span);
        double heightShape = Math.sin(Math.PI * v);
        double belly = .10D + .70D * widthShape * heightShape;
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
        if (!ship.isNativeHull())
            return super.shouldRender(ship, frustum, cameraX, cameraY, cameraZ);
        var config = ship.modularConfiguration();
        double radius = config == null ? 9D : Math.hypot(
                config.width() / 2D + 1D, config.length() / 2D + 1D) + 2D;
        double height = config == null ? 22D : 24D;
        return frustum.isVisible(ship.getBoundingBox().inflate(radius, height, radius));
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
