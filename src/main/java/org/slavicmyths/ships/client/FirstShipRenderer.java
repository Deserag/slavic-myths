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
import org.slavicmyths.ships.FirstShipSmallSailPlan;
import org.slavicmyths.ships.FirstShipMediumSailPlan;
import org.slavicmyths.ships.ShipClearanceSailPlan;
import org.slavicmyths.ships.ShipSectionPlan;
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
    private static final boolean PROFILE = Boolean.getBoolean("ocean26.longForward")
            || Boolean.getBoolean("ocean26.renderProfile");
    private static final int PROFILE_WARMUP = 20, PROFILE_SAMPLES = 100;
    private final long[] profileTimes = new long[PROFILE_SAMPLES];
    private long profileTotalNs, profilePaletteNs, profileBlocksNs, profileBuildNs, profileLightNs;
    private int profileFrames, profileCalls, profileHullCalls, profilePieceCalls;
    private int profileBlocks, profileLanterns, profileMisses;
    private java.util.UUID profileShipId;

    private void resetProfile(java.util.UUID shipId) {
        profileShipId = shipId;
        profileFrames = profileCalls = profileHullCalls = profilePieceCalls = 0;
        profileBlocks = profileLanterns = profileMisses = 0;
        profileTotalNs = profilePaletteNs = profileBlocksNs = profileBuildNs = profileLightNs = 0L;
    }

    public boolean profileComplete(java.util.UUID shipId) {
        return PROFILE && shipId.equals(profileShipId)
                && profileFrames >= PROFILE_WARMUP + PROFILE_SAMPLES;
    }
    private final ItemRenderer items;
    private final ItemStack helm;
    private record PaletteKey(java.util.UUID shipId, org.slavicmyths.ships.ShipBuildConfiguration config,
            int geometry, int rig, int fabric, String cargo) {}
    private record MovingPalette(Map<BlockPos, BlockState> blocks, Map<BlockPos, Integer> blockLight) {}
    private record SailPanel(int z, int bottom, int top, int span, BlockState cloth) {}

    private final Map<PaletteKey, MovingPalette> paletteCache = new LinkedHashMap<>(16, .75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<PaletteKey, MovingPalette> eldest) {
            return size() > 12;
        }
    };
    private final Map<org.slavicmyths.ships.ShipBuildConfiguration, java.util.List<SailPanel>> sailCache =
            new LinkedHashMap<>(32, .75F, true) {
                @Override protected boolean removeEldestEntry(
                        Map.Entry<org.slavicmyths.ships.ShipBuildConfiguration, java.util.List<SailPanel>> eldest) {
                    return size() > 32;
                }
            };

    /**
     * Moving blocks are outside the world's light engine. Compute the fixed
     * local lantern contribution once per palette revision, not per frame.
     */
    private static Map<BlockPos, Integer> localBlockLight(
            Map<BlockPos, BlockState> blocks, org.slavicmyths.ships.ShipBuildConfiguration config) {
        if (config == null || config.hullGeometry() < 7) return Map.of();
        java.util.List<BlockPos> lanterns = new java.util.ArrayList<>();
        for (var entry : blocks.entrySet())
            if (entry.getValue().is(Blocks.LANTERN)) lanterns.add(entry.getKey());
        if (lanterns.isEmpty()) return Map.of();
        Map<BlockPos, Integer> light = new java.util.HashMap<>();
        for (BlockPos block : blocks.keySet()) {
            int brightest = 0;
            for (BlockPos lantern : lanterns) {
                int distance = Math.abs(block.getX() - lantern.getX())
                        + Math.abs(block.getY() - lantern.getY())
                        + Math.abs(block.getZ() - lantern.getZ());
                if (distance <= 13) brightest = Math.max(brightest, 14 - distance);
                if (brightest == 14) break;
            }
            if (brightest > 0) light.put(block, brightest << 4);
        }
        return Map.copyOf(light);
    }

    public FirstShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        helm = new ItemStack(RaftContent.HELM_MODULE.get());
        shadowRadius = 0F;
    }

    private MovingPalette movingPalette(FirstShipEntity ship,
            org.slavicmyths.ships.ShipBuildConfiguration config) {
        String types = ship.movingCargoView();
        PaletteKey key = new PaletteKey(ship.getUUID(), config, ship.geometryVersion(),
                ship.rigLevel(), ship.fabricQuality(), types);
        MovingPalette cached = paletteCache.get(key);
        if (cached != null) return cached;
        long buildStart = PROFILE ? System.nanoTime() : 0L;
        ListTag cargo = new ListTag();
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
        Map<BlockPos, BlockState> blocks;
        try {
            if (config != null) {
                Map<BlockPos, BlockState> visual = new LinkedHashMap<>(
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
                blocks = java.util.Collections.unmodifiableMap(visual);
            } else {
                var snapshot = new FirstShipBlockHull.MotionSnapshot(cargo,
                        ship.geometryVersion(), ship.rigLevel(), ship.fabricQuality(), 0);
                blocks = FirstShipBlockHull.localMotionPalette(snapshot);
            }
        } catch (RuntimeException invalid) {
            blocks = Map.of();
        }
        long lightStart = PROFILE ? System.nanoTime() : 0L;
        Map<BlockPos, Integer> blockLight = localBlockLight(blocks, config);
        if (PROFILE) profileLightNs += System.nanoTime() - lightStart;
        MovingPalette built = new MovingPalette(blocks, blockLight);
        paletteCache.put(key, built);
        if (PROFILE) {
            profileMisses++;
            profileBuildNs += System.nanoTime() - buildStart;
        }
        return built;
    }

    @Override public void render(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        if (!ship.isNativeHull()) {
            super.render(ship, yaw, partialTick, pose, buffers, light);
            return;
        }
        var config = ship.modularConfiguration();
        if (PROFILE && ship.isMoving() && config != null && !ship.getUUID().equals(profileShipId))
            resetProfile(ship.getUUID());
        boolean measuring = PROFILE && ship.isMoving() && config != null
                && profileFrames < PROFILE_WARMUP + PROFILE_SAMPLES;
        long renderStart = measuring ? System.nanoTime() : 0L;
        long paletteElapsed = 0L, blocksElapsed = 0L;
        if (measuring) profileCalls = profileHullCalls = profilePieceCalls = 0;
        if (ship.isMoving()) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-yaw));
            long paletteStart = measuring ? System.nanoTime() : 0L;
            MovingPalette palette = movingPalette(ship, config);
            if (measuring) {
                paletteElapsed = System.nanoTime() - paletteStart;
                if (profileFrames == 0) {
                    profileBlocks = (int)palette.blocks().values().stream().filter(state -> !state.isAir()).count();
                    profileLanterns = (int)palette.blocks().values().stream().filter(state -> state.is(Blocks.LANTERN)).count();
                }
            }
            Map<BlockPos, Integer> localLight = palette.blockLight();
            long blocksStart = measuring ? System.nanoTime() : 0L;
            for (var entry : palette.blocks().entrySet()) {
                BlockState state = entry.getValue();
                if (state.isAir()) continue;
                BlockPos p = entry.getKey();
                int visualLight = localLight.isEmpty() ? light
                        : (light & 0xFFFF0000) | Math.max(light & 0xFFFF, localLight.getOrDefault(p, 0));
                pose.pushPose();
                pose.translate(p.getX() - .5D, p.getY(), p.getZ() - .5D);
                if (state.is(Blocks.CHEST)) {
                    items.renderStatic(new ItemStack(Items.CHEST),
                            ItemDisplayContext.FIXED, visualLight, OverlayTexture.NO_OVERLAY,
                            pose, buffers, ship.level(), ship.getId());
                } else {
                    if (measuring) { profileCalls++; profileHullCalls++; }
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                            state, pose, buffers, state.is(Blocks.LANTERN)
                                    ? (visualLight & 0xFFFF0000) | 0x00F0 : visualLight,
                            OverlayTexture.NO_OVERLAY);
                }
                pose.popPose();
            }
            pose.popPose();
            if (measuring) blocksElapsed = System.nanoTime() - blocksStart;
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
            renderModularSails(ship, config, yaw, partialTick, pose, buffers, deckLight);
        else if (ship.geometryVersion() >= 7 && ship.rigLevel() > 0)
            renderFabricSail(ship, yaw, partialTick, pose, buffers, deckLight);
        if (measuring) recordProfile(ship, renderStart, paletteElapsed, blocksElapsed);
    }

    private void recordProfile(FirstShipEntity ship, long start, long paletteNs, long blocksNs) {
        long total = System.nanoTime() - start;
        int frame = profileFrames++;
        if (frame < PROFILE_WARMUP) return;
        int sample = frame - PROFILE_WARMUP;
        profileTimes[sample] = total;
        profileTotalNs += total;
        profilePaletteNs += paletteNs;
        profileBlocksNs += blocksNs;
        if (sample != PROFILE_SAMPLES - 1) return;
        long[] ordered = profileTimes.clone();
        java.util.Arrays.sort(ordered);
        double unit = PROFILE_SAMPLES * 1_000_000D;
        System.out.println(String.format(java.util.Locale.ROOT,
                "OCEAN26_RENDER_PROFILE class=%s geometry=%d size=%dx%d warmup=%d frames=%d blocks=%d hullCalls=%d extraCalls=%d singleBlockCalls=%d lanterns=%d avgCpuMs=%.5f p95CpuMs=%.5f avgPaletteMs=%.5f avgBlockLoopMs=%.5f cacheMisses=%d cacheBuildTotalMs=%.5f lightBuildTotalMs=%.5f",
                ship.modularConfiguration().shipClass(), ship.modularConfiguration().hullGeometry(),
                ship.modularConfiguration().width(), ship.modularConfiguration().length(),
                PROFILE_WARMUP, PROFILE_SAMPLES,
                profileBlocks, profileHullCalls, profilePieceCalls, profileCalls, profileLanterns,
                profileTotalNs / unit, ordered[(int)Math.ceil(.95D * PROFILE_SAMPLES) - 1] / 1_000_000D,
                profilePaletteNs / unit, profileBlocksNs / unit,
                profileMisses, profileBuildNs / 1_000_000D, profileLightNs / 1_000_000D));
    }

    private static BlockState sailCloth(int fabric) {
        return (fabric == 1 ? Blocks.RED_WOOL
                : fabric == 2 ? Blocks.BLUE_WOOL : Blocks.BROWN_WOOL).defaultBlockState();
    }

    /** Immutable sail dimensions keyed by the full, validated blueprint. */
    private java.util.List<SailPanel> modularSailPanels(
            org.slavicmyths.ships.ShipBuildConfiguration config) {
        java.util.List<SailPanel> cached = sailCache.get(config);
        if (cached != null) return cached;
        var plan = config.structure();
        java.util.List<SailPanel> panels = new java.util.ArrayList<>();
        if (plan.customPhysicalRigging()) {
            var layout = org.slavicmyths.ships.ShipRiggingPlan.forPlan(plan, config.hullGeometry());
            for (int i = 0; i < layout.masts().size(); i++) {
                var mast = layout.masts().get(i);
                BlockState cloth = sailCloth(plan.rigging().get(i).fabric());
                for (var panel : mast.panels())
                    panels.add(new SailPanel(mast.z(), panel.bottom(), panel.top(), panel.span(), cloth));
            }
        } else {
            boolean mediumV3 = config.hullGeometry() == 3
                    && plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM;
            boolean largeV4 = config.hullGeometry() >= 4;
            var mediumSails = mediumV3 ? FirstShipMediumSailPlan.forPlan(plan).sails() : null;
            var largeSails = largeV4 ? ShipClearanceSailPlan.forPlan(plan).sails() : null;
            java.util.List<Integer> masts = plan.mastPositions();
            for (int i = 0; i < plan.sailTiers().size(); i++) {
                int tier = plan.sailTiers().get(i);
                int top = mediumV3 ? mediumSails.get(i).top()
                        : largeV4 ? largeSails.get(i).top()
                        : tier == 1 ? 12 : tier == 2 ? 15 : 19;
                int span = mediumV3 ? mediumSails.get(i).halfSpan()
                        : largeV4 ? largeSails.get(i).halfSpan()
                        : tier == 1 ? 2 : tier == 2 ? 3 : 4;
                int bottom = mediumV3 ? mediumSails.get(i).bottom()
                        : largeV4 ? largeSails.get(i).bottom() : 8;
                panels.add(new SailPanel(masts.get(i), bottom, top, span,
                        sailCloth(plan.rigging().get(i).fabric())));
            }
        }
        java.util.List<SailPanel> immutable = java.util.List.copyOf(panels);
        sailCache.put(config, immutable);
        return immutable;
    }

    /** Preserve exactly the existing mesh positions and sizes. */
    private void renderModularSails(FirstShipEntity ship,
            org.slavicmyths.ships.ShipBuildConfiguration config, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        for (SailPanel panel : modularSailPanels(config)) {
            pose.pushPose();
            pose.translate(0D, 0D, panel.z());
            sailPanel(ship, partialTick, pose, buffers, light, panel.cloth(),
                    panel.bottom(), panel.top(), panel.span());
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
        if (ship.geometryVersion() >= 12) {
            // This mesh is the ONLY cloth for V12; the world palette contains
            // masts/yards but no wool, and both use the same tiered plan.
            for (FirstShipSmallSailPlan.Sail sail :
                    FirstShipSmallSailPlan.forRig(rig, ship.fabricQuality()).sails())
                smallSailPanel(ship, partialTick, pose, buffers, light, seam, sail);
        }
        else if (rig == 1) {
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

    /**
     * V12 SMALL only: individual one-block-high fabric strips form a stepped
     * Minecraft silhouette rather than an uninterrupted rounded rectangle.
     * Cloth starts ABOVE the lowest solid yard; the V11 renderer is untouched.
     */
    private void smallSailPanel(FirstShipEntity ship, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, BlockState seam,
            FirstShipSmallSailPlan.Sail sail) {
        VertexConsumer fabric = buffers.getBuffer(RenderType.entityCutoutNoCull(SAIL_TEXTURE));
        float tintR = ship.fabricQuality() == 2 ? .82F : 1F;
        float tintG = ship.fabricQuality() == 3 ? .86F : 1F;
        float tintB = ship.fabricQuality() == 3 ? .58F : 1F;
        for (int y = sail.bottom(); y <= sail.top(); y++) {
            int half = sail.rowHalfSpan(y);
            for (int col = 0; col < 8; col++) {
                smallSailVertex(fabric, pose, ship, partialTick, light, tintR, tintG,
                        tintB, col, y, half, sail);
                smallSailVertex(fabric, pose, ship, partialTick, light, tintR, tintG,
                        tintB, col + 1, y, half, sail);
                smallSailVertex(fabric, pose, ship, partialTick, light, tintR, tintG,
                        tintB, col + 1, y + 1, half, sail);
                smallSailVertex(fabric, pose, ship, partialTick, light, tintR, tintG,
                        tintB, col, y + 1, half, sail);
            }
            for (double side : new double[]{-1D, 1D})
                piece(Blocks.OAK_PLANKS.defaultBlockState(), pose, buffers, light,
                        side * (half + .25D) - .03D, y, .13D, .06F, 1F, .06F);
        }
        for (int y : new int[]{sail.bottom(), sail.top() + 1}) {
            int span = sail.rowHalfSpan(y == sail.bottom() ? y : sail.top());
            piece(seam, pose, buffers, light, -span - .25D, y, .15D,
                    (float)(span * 2D + .5D), .08F, .025F);
        }
    }

    private void smallSailVertex(VertexConsumer out, PoseStack pose, FirstShipEntity ship,
            float partialTick, int light, float red, float green, float blue,
            int column, int y, int half, FirstShipSmallSailPlan.Sail sail) {
        float u = column / 8F;
        float v = (y - sail.bottom()) / (float)(sail.top() - sail.bottom() + 1);
        double x = (half + .25D) * (2D * u - 1D);
        double fullness = 1D - Math.pow(2D * u - 1D, 2D);
        double wave = Math.sin(Math.PI * v);
        double belly = .10D + .55D * fullness * wave;
        double wind = Math.sin((ship.tickCount + partialTick) * .075D + x * .6D)
                * .045D * wave;
        out.addVertex(pose.last().pose(), (float)x, y, (float)(belly + wind))
                .setColor(red, green, blue, 1F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), (float)(-.5D * (2D * u - 1D) * wave), 0F, 1F);
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
        if (PROFILE && profileFrames < PROFILE_WARMUP + PROFILE_SAMPLES) {
            profileCalls++;
            profilePieceCalls++;
        }
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
