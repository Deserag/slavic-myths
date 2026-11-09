package org.slavicmyths.ships.client;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.RaftContent;

/**
 * Docked: vanilla chunk blocks own physics and rendering.
 * Underway: move one local block palette using the ship entity PoseStack,
 * fractional translation and arbitrary yaw, as in a moving contraption.
 * No copies of world blocks are left behind on departure.
 */
public final class FirstShipRenderer extends EntityRenderer<FirstShipEntity> {
    private final ItemRenderer items;
    private final ItemStack helm;
    private final Map<Integer, Map<BlockPos, BlockState>> paletteCache = new HashMap<>();

    public FirstShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        helm = new ItemStack(RaftContent.HELM_MODULE.get());
        shadowRadius = 0F;
    }

    @Override public boolean shouldRender(FirstShipEntity ship, Frustum camera,
            double camX, double camY, double camZ) {
        return (ship.isVoyaging() && ship.distanceToSqr(camX, camY, camZ) < 16384D)
                || super.shouldRender(ship, camera, camX, camY, camZ);
    }

    private Map<BlockPos, BlockState> palette(FirstShipEntity ship) {
        int key = ship.geometryVersion() * 128 + ship.rigLevel() * 8 + ship.fabricQuality();
        return paletteCache.computeIfAbsent(key, ignored ->
                FirstShipBlockHull.movingPalette(ship.rigLevel(),
                        ship.fabricQuality(), ship.geometryVersion()));
    }

    private void drawCargo(FirstShipEntity ship, PoseStack pose,
            MultiBufferSource buffers, int light) {
        String data = ship.voyageCargoVisual();
        if (data.isEmpty() || data.length() > 4096) return;
        String[] cells = data.split(";");
        if (cells.length > FirstShipHull.WIDTH * FirstShipHull.LENGTH * 2) return;
        for (String cell : cells) {
            String[] pieces = cell.split(",");
            if (pieces.length != 4) continue;
            int x, y, z, kind;
            try {
                x = Integer.parseInt(pieces[0]);
                y = Integer.parseInt(pieces[1]);
                z = Integer.parseInt(pieces[2]);
                kind = Integer.parseInt(pieces[3]);
            } catch (NumberFormatException invalid) { continue; }
            if (Math.abs(x) > 3 || Math.abs(z) > 5 || y < 0 || y > 5) continue;
            BlockState state = switch (kind) {
                case 1 -> Blocks.CHEST.defaultBlockState();
                case 2 -> Blocks.BARREL.defaultBlockState();
                case 3 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                case 4 -> Blocks.FURNACE.defaultBlockState();
                default -> Blocks.AIR.defaultBlockState();
            };
            if (state.isAir()) continue;
            pose.pushPose();
            pose.translate(x, y, z);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state,
                    pose, buffers, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }

    @Override public void render(FirstShipEntity ship, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light) {
        if (!ship.isNativeHull()) {
            super.render(ship, yaw, partialTick, pose, buffers, light);
            return;
        }
        if (ship.isVoyaging()) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-ship.shipYaw()));
            pose.translate(-.5D, 0D, -.5D);
            for (Map.Entry<BlockPos, BlockState> part : palette(ship).entrySet()) {
                BlockPos cell = part.getKey();
                pose.pushPose();
                pose.translate(cell.getX(), cell.getY(), cell.getZ());
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        part.getValue(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
            drawCargo(ship, pose, buffers, light);
            pose.popPose();
        }
        if (ship.geometryVersion() >= 5) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-ship.shipYaw()));
            pose.translate(0D, FirstShipHull.MAIN_DECK + .75D, -4D);
            pose.scale(1.9F, 1.9F, 1.9F);
            items.renderStatic(helm, ItemDisplayContext.NONE, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, ship.level(), ship.getId());
            pose.popPose();
        }
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
