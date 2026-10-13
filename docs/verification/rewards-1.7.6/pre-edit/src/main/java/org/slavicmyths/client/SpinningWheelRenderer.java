package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import org.slavicmyths.textile.*;

public final class SpinningWheelRenderer implements BlockEntityRenderer<TextileStation> {
    private final ModelPart wheel;
    public SpinningWheelRenderer(BlockEntityRendererProvider.Context c){var geometry=new FolkModelGeometry();wheel=geometry.part(32,32);
        for(int i=0;i<8;i++){var rim=geometry.part(32,32);float angle=i*(float)Math.PI/4;rim.setPos((float)Math.cos(angle)*6,(float)Math.sin(angle)*6,0);rim.zRot=angle+(float)Math.PI/2;geometry.box(rim,0,0,-2.5F,-.7F,-.8F,5,1.4F,1.6F);geometry.attach(wheel,rim);}
        for(int i=0;i<4;i++){var spoke=geometry.part(32,32);spoke.zRot=i*(float)Math.PI/4;geometry.box(spoke,0,0,-5.7F,-.35F,-.5F,11.4F,.7F,1);geometry.attach(wheel,spoke);}
        geometry.box(wheel,0,0,-1,-1,-1,2,2,2);
    }
    @Override public void render(TextileStation tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=tile.getBlockState();if(!(state.getBlock() instanceof StationBlock station)||station.kind!=1)return;
        pose.pushPose();pose.translate(.5,.875,.5);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-state.getValue(StationBlock.FACING).toYRot()));
        if(state.getValue(StationBlock.ACTIVE)&&tile.getLevel()!=null)pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(((tile.getLevel().getGameTime()+partial)%360)*4));
        pose.scale(1F/16F,1F/16F,1F/16F);wheel.render(pose,buffers.getBuffer(RenderType.entityCutout(Textiles.id("textures/block/textile_wood.png"))),light,overlay,-1);pose.popPose();
    }
}
