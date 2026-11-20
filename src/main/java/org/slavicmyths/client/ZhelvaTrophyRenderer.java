package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.ZhelvaContent;
import org.slavicmyths.marine.ZhelvaTrophyBlock;

/** Block entity renders the creature's real full-depth beak, skull, eyes and hinged jaw. */
public final class ZhelvaTrophyRenderer implements BlockEntityRenderer<ZhelvaTrophyBlock.Tile> {
    private static final ResourceLocation WHITE =
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final ZhelvaModel model = new ZhelvaModel();
    public ZhelvaTrophyRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(ZhelvaTrophyBlock.Tile tile, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        if (tile.getLevel() == null) return;
        Direction facing = tile.getBlockState().getValue(ZhelvaTrophyBlock.FACING);
        float rotation = switch (facing) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        pose.pushPose();
        pose.translate(.5, .32, .5);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.scale(.37F, -.37F, .37F);
        pose.translate(0, 28, 72);
        model.renderTrophyHead(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE)),
            light, overlay, tile.getLevel().getGameTime() + partialTick);
        pose.popPose();
    }
    @Override public AABB getRenderBoundingBox(ZhelvaTrophyBlock.Tile tile) {
        return new AABB(tile.getBlockPos()).inflate(1.2, .8, 1.2);
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ZhelvaContent.HEAD_TILE.get(), ZhelvaTrophyRenderer::new);
    }
}
