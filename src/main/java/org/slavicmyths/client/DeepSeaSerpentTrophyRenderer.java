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
import org.slavicmyths.marine.DeepSeaSerpentContent;
import org.slavicmyths.marine.DeepSeaSerpentTrophyBlock;

/** 2x3 skull and 1x2 crest; both render across their actual multi-cell footprints. */
public final class DeepSeaSerpentTrophyRenderer implements BlockEntityRenderer<DeepSeaSerpentTrophyBlock.Tile> {
    private static final ResourceLocation WHITE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final DeepSeaSerpentModel model=new DeepSeaSerpentModel();
    public DeepSeaSerpentTrophyRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public void render(DeepSeaSerpentTrophyBlock.Tile tile,float partial,PoseStack pose,
        MultiBufferSource out,int light,int overlay){
        if(tile.getLevel()==null)return;
        if(!(tile.getBlockState().getBlock() instanceof DeepSeaSerpentTrophyBlock block))return;
        Direction facing=tile.getBlockState().getValue(DeepSeaSerpentTrophyBlock.FACING);
        float angle=switch(facing){
            case NORTH->0;case EAST->90;case SOUTH->180;case WEST->270;default->0;
        };
        float age=tile.getLevel().getGameTime()+partial;
        pose.pushPose();
        // Owner block is rear-left, rotation around CENTER of the full 2x3 / 1x2 grid.
        pose.translate(.5,.15,.5);
        pose.translate(facing.getClockWise().getStepX()*(block.width()-1)*.5
                +facing.getStepX()*(block.length()-1)*.5,0,
            facing.getClockWise().getStepZ()*(block.width()-1)*.5
                +facing.getStepZ()*(block.length()-1)*.5);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        if(block.isHead()){
            // Head spans roughly 56 pixels in Z, 46 in X: at .73 it fills 2 x 3 blocks.
            pose.translate(0,0,1.72);
            pose.scale(.70F,-.70F,.70F);
            model.renderHead(pose,out.getBuffer(RenderType.entityCutoutNoCull(WHITE)),
                light,overlay,age,tile.active()||tile.reactive());
        } else {
            pose.translate(0,.15,0);
            pose.scale(.73F,-.73F,.73F);
            model.renderCrest(pose,out.getBuffer(RenderType.entityCutoutNoCull(WHITE)),
                light,overlay,age,tile.active()||tile.reactive());
        }
        pose.popPose();
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(DeepSeaSerpentContent.TROPHY_TILE.get(),
            DeepSeaSerpentTrophyRenderer::new);
    }
}
