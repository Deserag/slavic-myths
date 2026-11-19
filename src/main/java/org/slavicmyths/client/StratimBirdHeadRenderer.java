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
import org.slavicmyths.marine.StratimBirdContent;
import org.slavicmyths.marine.StratimBirdHeadBlock;

public final class StratimBirdHeadRenderer implements BlockEntityRenderer<StratimBirdHeadBlock.Tile>{
    private static final ResourceLocation TEXTURE=
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final StratimBirdModel model=new StratimBirdModel();
    public StratimBirdHeadRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(StratimBirdHeadBlock.Tile tile,float partial,PoseStack pose,
        MultiBufferSource out,int light,int overlay){
        if(tile.getLevel()==null)return;
        Direction face=tile.getBlockState().getValue(StratimBirdHeadBlock.FACING);
        float angle=switch(face){
            case NORTH->0;case EAST->90;case SOUTH->180;case WEST->270;default->0;
        };
        pose.pushPose();
        // Origin at back-left bottom of the 2x2x3 occupied volume.
        pose.translate(.5+face.getClockWise().getStepX()*.5+face.getStepX(),
            .15,.5+face.getClockWise().getStepZ()*.5+face.getStepZ());
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.scale(.68F,-.68F,.68F);
        model.renderTrophy(pose,out.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
            light,overlay,tile.getLevel().getGameTime()+partial);
        pose.popPose();
    }
    @Override public AABB getRenderBoundingBox(StratimBirdHeadBlock.Tile tile){
        var p=tile.getBlockPos();
        return new AABB(p).inflate(4);
    }
    public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerBlockEntityRenderer(StratimBirdContent.HEAD_TILE.get(),StratimBirdHeadRenderer::new);
    }
}
