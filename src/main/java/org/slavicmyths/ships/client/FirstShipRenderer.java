package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
                + ship.fabricQuality() + ":" + ship.movingCargoView();
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
                    ship.geometryVersion(), ship.rigLevel(), ship.fabricQuality(), 0);
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
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(ship.isMoving() ? yaw : ship.shipYaw())));
        pose.translate(0D, FirstShipHull.MAIN_DECK + 0.75D, -4D);
        pose.scale(1.9F, 1.9F, 1.9F);
        items.renderStatic(helm, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, ship.level(), ship.getId());
        pose.popPose();
    }

    @Override public ResourceLocation getTextureLocation(FirstShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
