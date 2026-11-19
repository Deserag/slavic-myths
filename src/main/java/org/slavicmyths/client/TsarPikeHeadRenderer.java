package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.TsarPikeContent;
import org.slavicmyths.marine.TsarPikeHeadBlock;

/** Uses the identical real skull/jaw/teeth cuboids as the mob, scaled to 1x2 blocks. */
public final class TsarPikeHeadRenderer implements BlockEntityRenderer<TsarPikeHeadBlock.Tile> {
    private static final ResourceLocation WHITE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final TsarPikeModel model=new TsarPikeModel();

    public TsarPikeHeadRenderer(BlockEntityRendererProvider.Context context){}

    @Override public void render(TsarPikeHeadBlock.Tile tile,float partial,PoseStack pose,
                                MultiBufferSource buffer,int light,int overlay){
        if(tile.getLevel()==null)return;
        Direction facing=tile.getBlockState().getValue(TsarPikeHeadBlock.FACING);
        float rotation=switch(facing){
            case NORTH->0F; case EAST->90F; case SOUTH->180F; case WEST->270F;
            default->0F;
        };
        float time=tile.getLevel().getGameTime()+partial;
        pose.pushPose();
        pose.translate(.5,.68,.94);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.scale(.51F,-.51F,.51F);
        model.renderHead(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE)),
            light,overlay,time,tile.active());
        pose.popPose();
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(TsarPikeContent.HEAD_TILE.get(),TsarPikeHeadRenderer::new);
    }
}
