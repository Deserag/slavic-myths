package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
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
import org.slavicmyths.ships.ShipSailRigGeometry;

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
            "slavicmyths", "textures/block/textile_cloth.png");
    private final ItemRenderer items;
    private final ItemStack helm;
    private final Map<String, Map<BlockPos, BlockState>> paletteCache =
            new LinkedHashMap<>(8, .75F, true) {
                @Override protected boolean removeEldestEntry(
                        Map.Entry<String, Map<BlockPos, BlockState>> eldest) {
                    return size() > 8;
                }
            };
    private final Map<FirstShipEntity, Double> sailPressure = new WeakHashMap<>();

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
        Map<BlockPos, BlockState> cached = paletteCache.get(key);
        if (cached != null) return cached;
        ListTag cargo = ShipModularBlockHull.decodeVisibleCargo(
                ship.movingCargoView());
        Map<BlockPos, BlockState> palette;
        try {
            if (config != null)
                palette = ShipModularBlockHull.localVisualPalette(config, cargo);
            else {
                var snapshot = new FirstShipBlockHull.MotionSnapshot(cargo,
                        ship.geometryVersion(), ship.rigLevel(), ship.fabricQuality(), 0);
                palette = FirstShipBlockHull.localMotionPalette(snapshot);
            }
        } catch (RuntimeException invalid) {
            palette = Map.of();
        }
        // Calculate hidden cells only when the ship palette changes. Doing
        // six neighbour lookups for every cube on every rendered frame would
        // replace GPU pressure with avoidable render-thread CPU work.
        if (!palette.isEmpty()) {
            Map<BlockPos, BlockState> source = palette;
            Map<BlockPos, BlockState> visible = new LinkedHashMap<>();
            for (var entry : source.entrySet())
                if (!hiddenOpaque(entry.getKey(), entry.getValue(), source))
                    visible.put(entry.getKey(), entry.getValue());
            palette = java.util.Collections.unmodifiableMap(visible);
        }
        paletteCache.put(key, palette);
        return palette;
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


    /**
     * Each installed mast gets its own sail tiers, anchored standing rigging
     * and non-colliding running rigging. The server block palette remains the
     * authoritative physical hull; this layer draws only decorative fabric
     * and fine ropes in the same ship-local pose as the moving hull.
     */
    private void renderModularSails(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        var plan = ship.modularConfiguration().structure();
        var geometry = ShipSailRigGeometry.forPlan(plan);
        if (geometry.panels().isEmpty()) return;
        double cap = ShipSailRigGeometry.isFrigate(plan)
                ? org.slavicmyths.ships.ShipRigidMotion.MAX_IV_FORWARD
                : org.slavicmyths.ships.ShipRigidMotion.MAX_FORWARD;
        double target = ship.isMoving()
                ? Math.min(1D, Math.abs(ship.visualMotionSpeed()) / cap) : 0D;
        double previous = sailPressure.getOrDefault(ship, target);
        // Damp network jitter without touching the server-authoritative physics.
        double pressure = previous + .18D * (target - previous);
        sailPressure.put(ship, pressure);

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        VertexConsumer cloth = buffers.getBuffer(RenderType.entityCutoutNoCull(SAIL_TEXTURE));
        for (var panel : geometry.panels()) {
            pose.pushPose();
            pose.translate(0D, 0D, panel.mastZ());
            renderRigPanel(ship, partialTick, cloth, pose, light, panel, pressure);
            pose.popPose();
        }
        for (var triangle : geometry.staysails())
            renderStaysail(cloth, pose, light, triangle, pressure);
        for (var rope : geometry.ropes())
            renderRope(cloth, pose, light, rope.start(), rope.end(), rope.kind());
        // Intermediate tiers require extra light yards between the two
        // physical wooden yards on III. They are purely cosmetic.
        if (ShipSailRigGeometry.isBrigantine(plan)) {
            double base = org.slavicmyths.ships.ShipHullVoxelLayout.sailLowerYard(plan);
            for (int i = 0; i < plan.sailTiers().size(); i++) {
                int tier = plan.sailTiers().get(i);
                double z = plan.mastPositions().get(i);
                if (tier >= 2)
                    renderLightYard(cloth, pose, light, base + 5.5D,
                            z, tier == 2 ? 2.3D : 3D);
                if (tier >= 3)
                    renderLightYard(cloth, pose, light, base + 9.5D, z, 2.2D);
            }
        } else if (ShipSailRigGeometry.isFrigate(plan)) {
            for (int i = 0; i < plan.sailTiers().size(); i++) {
                // Tier-II central masts carry an extra upper sail without a
                // collision-bearing top yard; draw a lightweight support.
                if (plan.sailTiers().get(i) == 2 && (i == 1 || i == 2))
                    renderLightYard(cloth, pose, light, 43D,
                            plan.mastPositions().get(i), 4.5D);
            }
        }
        pose.popPose();
    }

    private void renderLightYard(VertexConsumer out, PoseStack pose, int light,
            double y, double z, double halfWidth) {
        renderRibbon(out, pose, light,
                new ShipSailRigGeometry.Point(-halfWidth, y, z),
                new ShipSailRigGeometry.Point(halfWidth, y, z),
                .14D, .44F, .27F, .12F);
    }

    private ShipSailRigGeometry.Point clothPoint(FirstShipEntity ship,
            float partialTick, ShipSailRigGeometry.Panel panel,
            double pressure, double u, double v) {
        return ShipSailRigGeometry.clothSurface(panel, pressure, u, v,
                ship.tickCount + partialTick);
    }

    private void renderRigPanel(FirstShipEntity ship, float partialTick,
            VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Panel panel, double pressure) {
        final int columns = 10, rows = 8;
        float red = panel.tier() == 2 ? .91F : 1F;
        float green = panel.tier() == 3 ? .91F : 1F;
        float blue = panel.tier() == 3 ? .82F : 1F;
        for (int row = 0; row < rows; row++) {
            double v0 = row / (double) rows, v1 = (row + 1D) / rows;
            for (int column = 0; column < columns; column++) {
                double u0 = column / (double) columns, u1 = (column + 1D) / columns;
                var a = clothPoint(ship, partialTick, panel, pressure, u0, v0);
                var b = clothPoint(ship, partialTick, panel, pressure, u1, v0);
                var c = clothPoint(ship, partialTick, panel, pressure, u1, v1);
                var d = clothPoint(ship, partialTick, panel, pressure, u0, v1);
                renderQuad(out, pose, light, a, b, c, d,
                        (float) u0, (float) u1, (float) v0, (float) v1,
                        red, green, blue);
            }
        }
        // Boltrope follows the animated cloth boundary exactly. Subtle seam
        // lines run between independent panels instead of being solid blocks.
        for (int edge = 0; edge < 2; edge++) {
            double u = edge;
            for (int row = 0; row < rows; row++)
                renderRope(out, pose, light,
                        clothPoint(ship, partialTick, panel, pressure, u, row / (double) rows),
                        clothPoint(ship, partialTick, panel, pressure, u, (row + 1D) / rows),
                        ShipSailRigGeometry.RopeKind.LIFT);
        }
        for (int edge = 0; edge < 2; edge++) {
            double v = edge;
            for (int column = 0; column < columns; column++)
                renderRope(out, pose, light,
                        clothPoint(ship, partialTick, panel, pressure,
                                column / (double) columns, v),
                        clothPoint(ship, partialTick, panel, pressure,
                                (column + 1D) / columns, v),
                        ShipSailRigGeometry.RopeKind.LIFT);
        }
        for (double seamV : new double[]{.34D, .67D})
            for (int column = 0; column < columns; column++)
                renderRibbon(out, pose, light,
                        clothPoint(ship, partialTick, panel, pressure,
                                column / (double) columns, seamV),
                        clothPoint(ship, partialTick, panel, pressure,
                                (column + 1D) / columns, seamV),
                        .018D, .72F, .65F, .51F);
    }

    private void renderStaysail(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Staysail sail, double pressure) {
        var a = sail.a();
        var b = sail.b();
        var c = sail.c();
        var middle = new ShipSailRigGeometry.Point(
                (a.x() + b.x() + c.x()) / 3D + .22D + .52D * pressure,
                (a.y() + b.y() + c.y()) / 3D,
                (a.z() + b.z() + c.z()) / 3D);
        renderTriangle(out, pose, light, a, b, middle, .96F, .94F, .87F);
        renderTriangle(out, pose, light, b, c, middle, .96F, .94F, .87F);
        renderTriangle(out, pose, light, c, a, middle, .96F, .94F, .87F);
        renderRope(out, pose, light, a, b, ShipSailRigGeometry.RopeKind.LIFT);
        renderRope(out, pose, light, b, c, ShipSailRigGeometry.RopeKind.LIFT);
        renderRope(out, pose, light, c, a, ShipSailRigGeometry.RopeKind.LIFT);
    }

    private void renderTriangle(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Point a, ShipSailRigGeometry.Point b,
            ShipSailRigGeometry.Point c, float red, float green, float blue) {
        float[] normal = surfaceNormal(a, b, c);
        fabricVertex(out, pose, light, a, red, green, blue, 0F, 0F, normal, 1F);
        fabricVertex(out, pose, light, b, red, green, blue, 0F, 1F, normal, 1F);
        fabricVertex(out, pose, light, c, red, green, blue, 1F, 1F, normal, 1F);
        fabricVertex(out, pose, light, c, red, green, blue, 1F, 1F, normal, 1F);
    }

    private void renderQuad(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Point a, ShipSailRigGeometry.Point b,
            ShipSailRigGeometry.Point c, ShipSailRigGeometry.Point d,
            float u0, float u1, float v0, float v1,
            float red, float green, float blue) {
        float[] normal = surfaceNormal(a, b, d);
        fabricVertex(out, pose, light, a, red, green, blue, u0, v0, normal, 1F);
        fabricVertex(out, pose, light, b, red, green, blue, u1, v0, normal, 1F);
        fabricVertex(out, pose, light, c, red, green, blue, u1, v1, normal, 1F);
        fabricVertex(out, pose, light, d, red, green, blue, u0, v1, normal, 1F);
    }

    private static float[] surfaceNormal(ShipSailRigGeometry.Point a,
            ShipSailRigGeometry.Point b, ShipSailRigGeometry.Point c) {
        double abx = b.x() - a.x(), aby = b.y() - a.y(), abz = b.z() - a.z();
        double acx = c.x() - a.x(), acy = c.y() - a.y(), acz = c.z() - a.z();
        double x = aby * acz - abz * acy;
        double y = abz * acx - abx * acz;
        double z = abx * acy - aby * acx;
        double length = Math.max(1E-7D, Math.sqrt(x * x + y * y + z * z));
        return new float[]{(float) (x / length), (float) (y / length),
                (float) (z / length)};
    }

    private void fabricVertex(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Point p, float red, float green, float blue,
            float u, float v, float[] normal, float facing) {
        out.addVertex(pose.last().pose(), (float) p.x(), (float) p.y(), (float) p.z())
                .setColor(red, green, blue, 1F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), normal[0] * facing, normal[1] * facing,
                        normal[2] * facing);
    }

    private void renderRope(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Point a, ShipSailRigGeometry.Point b,
            ShipSailRigGeometry.RopeKind kind) {
        double width = kind == ShipSailRigGeometry.RopeKind.SHROUD
                || kind == ShipSailRigGeometry.RopeKind.STAY
                || kind == ShipSailRigGeometry.RopeKind.BACKSTAY ? .055D
                : kind == ShipSailRigGeometry.RopeKind.RATLINE ? .025D : .035D;
        float r = kind == ShipSailRigGeometry.RopeKind.RATLINE ? .51F : .43F;
        float g = kind == ShipSailRigGeometry.RopeKind.RATLINE ? .38F : .30F;
        float bl = kind == ShipSailRigGeometry.RopeKind.RATLINE ? .23F : .18F;
        renderRibbon(out, pose, light, a, b, width, r, g, bl);
    }

    private void renderRibbon(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Point a, ShipSailRigGeometry.Point b,
            double width, float r, float g, float blue) {
        double dx = b.x() - a.x(), dy = b.y() - a.y(), dz = b.z() - a.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1E-5D) return;
        dx /= length;
        dy /= length;
        dz /= length;
        double sx = -dz, sy = 0D, sz = dx;
        double horizontal = Math.hypot(sx, sz);
        if (horizontal < 1E-5D) {
            sx = 1D;
            sz = 0D;
        } else {
            sx /= horizontal;
            sz /= horizontal;
        }
        renderRibbonStrip(out, pose, light, a, b, sx, sy, sz, width, r, g, blue);
        renderRibbonStrip(out, pose, light, a, b,
                dy * sz - dz * sy, dz * sx - dx * sz,
                dx * sy - dy * sx, width, r, g, blue);
    }

    private void renderRibbonStrip(VertexConsumer out, PoseStack pose, int light,
            ShipSailRigGeometry.Point a, ShipSailRigGeometry.Point b,
            double sx, double sy, double sz, double width,
            float r, float g, float blue) {
        double half = width / 2D;
        var a0 = new ShipSailRigGeometry.Point(
                a.x() - sx * half, a.y() - sy * half, a.z() - sz * half);
        var a1 = new ShipSailRigGeometry.Point(
                a.x() + sx * half, a.y() + sy * half, a.z() + sz * half);
        var b0 = new ShipSailRigGeometry.Point(
                b.x() - sx * half, b.y() - sy * half, b.z() - sz * half);
        var b1 = new ShipSailRigGeometry.Point(
                b.x() + sx * half, b.y() + sy * half, b.z() + sz * half);
        // A rope must sample one cloth texel. Spreading the entire patterned
        // sail atlas over a hair-thin strip made its apparent colour flicker
        // as the vessel moved and the strip rasterized to different pixels.
        renderQuad(out, pose, net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,
                a0, b0, b1, a1, .5F, .5F, .5F, .5F, r, g, blue);
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
        double motion = ship.isMoving()
                ? Math.min(1D, Math.abs(ship.visualMotionSpeed())
                        / org.slavicmyths.ships.ShipRigidMotion.MAX_IV_FORWARD) : 0D;
        double belly = .10D + (.22D + 1.55D * motion) * widthShape * heightShape;
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
        boolean iv = config != null
                && config.shipClass() == org.slavicmyths.ships.ShipSectionPlan.ShipClass.LARGEST
                && config.hullRevision() == 4;
        if (iv) radius += 8D; // Long diagonal bowsprit and raised stern.
        double height = iv ? 52D : config == null ? 22D : 24D;
        if (config != null && config.shipClass() == org.slavicmyths.ships.ShipSectionPlan.ShipClass.LARGE
                && config.hullRevision() == 3)
            for (int tier : config.sailTiers())
                height = Math.max(height,
                        org.slavicmyths.ships.ShipHullVoxelLayout.sailTop(
                                config.structure(), tier) + 4D);
        return frustum.isVisible(ship.getBoundingBox().inflate(radius, height, radius));
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
