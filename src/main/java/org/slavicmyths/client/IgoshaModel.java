package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.entity.IgoshaEntity;
public final class IgoshaModel extends EntityModel<IgoshaEntity> {
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;
    public IgoshaModel() {texWidth=256;texHeight=256;
        head=geometry.part(texWidth,texHeight);head.setPos(0.0F,19.0F,-1.0F);
        geometry.box(head,0,0,-4.0F,-7.0F,-3.5F,8.0F,7.0F,7.0F);
        body=geometry.part(texWidth,texHeight);body.setPos(0.0F,19.0F,0.0F);
        geometry.box(body,31,0,-2.5F,0.0F,-2.0F,5.0F,4.0F,4.0F);
        rightArm=geometry.part(texWidth,texHeight);rightArm.setPos(-3.0F,21.0F,0.0F);
        geometry.box(rightArm,50,0,-1.0F,0.0F,-1.0F,2.0F,2.0F,2.0F);
        leftArm=geometry.part(texWidth,texHeight);leftArm.setPos(3.0F,21.0F,0.0F);
        geometry.box(leftArm,59,0,-1.0F,0.0F,-1.0F,2.0F,1.0F,2.0F);
        rightLeg=geometry.part(texWidth,texHeight);rightLeg.setPos(-1.5F,23.0F,0.0F);
        geometry.box(rightLeg,68,0,-1.0F,0.0F,-1.0F,2.0F,1.0F,3.0F);
        leftLeg=geometry.part(texWidth,texHeight);leftLeg.setPos(1.5F,23.0F,0.0F);
        geometry.box(leftLeg,79,0,-1.0F,0.0F,-1.0F,2.0F,1.0F,2.0F);
    }
    @Override public void setupAnim(IgoshaEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        head.y=19.0F;head.xRot=head.yRot=head.zRot=0;
        body.y=19.0F;body.xRot=body.yRot=body.zRot=0;
        rightArm.y=21.0F;rightArm.xRot=rightArm.yRot=rightArm.zRot=0;
        leftArm.y=21.0F;leftArm.xRot=leftArm.yRot=leftArm.zRot=0;
        rightLeg.y=23.0F;rightLeg.xRot=rightLeg.yRot=rightLeg.zRot=0;
        leftLeg.y=23.0F;leftLeg.xRot=leftLeg.yRot=leftLeg.zRot=0;
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        float hop=entity.onGround()?0:Mth.sin(age*.65F)*.08F;body.zRot=hop;head.zRot=-hop*.4F;rightArm.zRot=-.3F;leftArm.zRot=.4F;rightLeg.xRot=leftLeg.xRot=0;
    }
    @Override public void renderToBuffer(PoseStack p,VertexConsumer b,int l,int o,int color){
        head.render(p,b,l,o,color);
        body.render(p,b,l,o,color);
        rightArm.render(p,b,l,o,color);
        leftArm.render(p,b,l,o,color);
        rightLeg.render(p,b,l,o,color);
        leftLeg.render(p,b,l,o,color);
    }
}
