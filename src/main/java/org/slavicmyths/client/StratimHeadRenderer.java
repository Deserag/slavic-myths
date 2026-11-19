package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.*;
public final class StratimHeadRenderer implements BlockEntityRenderer<StratimHeadBlock.Tile>{
    private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final StratimModel model=new StratimModel();
    public StratimHeadRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public void render(StratimHeadBlock.Tile tile,float partial,PoseStack pose,
       MultiBufferSource output,int light,int overlay){
        if(tile.getLevel()==null)return;
        Direction f=tile.getBlockState().getValue(StratimHeadBlock.FACING);
        float yaw=switch(f){case NORTH->0;case EAST->90;case SOUTH->180;case WEST->270;default->0;};
        pose.pushPose();
        pose.translate(.5,.25,.5);
        pose.translate(f.getClockWise().getStepX()*.5+f.getStepX(),0,
            f.getClockWise().getStepZ()*.5+f.getStepZ());
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.translate(0,.75,1.05);
        pose.scale(.73F,-.73F,.73F);
        model.renderTrophy(pose,output.getBuffer(RenderType.entityCutoutNoCull(WHITE)),light,overlay,
            tile.getLevel().getGameTime()+partial);
        pose.popPose();
    }
    @Override public AABB getRenderBoundingBox(StratimHeadBlock.Tile tile){return tile.getRenderBoundingBox();}
    public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerBlockEntityRenderer(StratimContent.HEAD_TILE.get(),StratimHeadRenderer::new);
    }
}
