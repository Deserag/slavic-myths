package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.IgoshaEntity;
public final class IgoshaModel extends EntityModel<IgoshaEntity> {
    private final ModelRenderer head, body, rightArm, leftArm, rightLeg, leftLeg;
    public IgoshaModel() {texWidth=256;texHeight=256;
        head=new ModelRenderer(this);head.setPos(0.0F,19.0F,-1.0F);
        head.texOffs(0,0).addBox(-4.0F,-7.0F,-3.5F,8.0F,7.0F,7.0F);
        body=new ModelRenderer(this);body.setPos(0.0F,19.0F,0.0F);
        body.texOffs(31,0).addBox(-2.5F,0.0F,-2.0F,5.0F,4.0F,4.0F);
        rightArm=new ModelRenderer(this);rightArm.setPos(-3.0F,21.0F,0.0F);
        rightArm.texOffs(50,0).addBox(-1.0F,0.0F,-1.0F,2.0F,2.0F,2.0F);
        leftArm=new ModelRenderer(this);leftArm.setPos(3.0F,21.0F,0.0F);
        leftArm.texOffs(59,0).addBox(-1.0F,0.0F,-1.0F,2.0F,1.0F,2.0F);
        rightLeg=new ModelRenderer(this);rightLeg.setPos(-1.5F,23.0F,0.0F);
        rightLeg.texOffs(68,0).addBox(-1.0F,0.0F,-1.0F,2.0F,1.0F,3.0F);
        leftLeg=new ModelRenderer(this);leftLeg.setPos(1.5F,23.0F,0.0F);
        leftLeg.texOffs(79,0).addBox(-1.0F,0.0F,-1.0F,2.0F,1.0F,2.0F);
    }
    @Override public void setupAnim(IgoshaEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        head.y=19.0F;head.xRot=head.yRot=head.zRot=0;
        body.y=19.0F;body.xRot=body.yRot=body.zRot=0;
        rightArm.y=21.0F;rightArm.xRot=rightArm.yRot=rightArm.zRot=0;
        leftArm.y=21.0F;leftArm.xRot=leftArm.yRot=leftArm.zRot=0;
        rightLeg.y=23.0F;rightLeg.xRot=rightLeg.yRot=rightLeg.zRot=0;
        leftLeg.y=23.0F;leftLeg.xRot=leftLeg.yRot=leftLeg.zRot=0;
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        float hop=entity.isOnGround()?0:MathHelper.sin(age*.65F)*.08F;body.zRot=hop;head.zRot=-hop*.4F;rightArm.zRot=-.3F;leftArm.zRot=.4F;rightLeg.xRot=leftLeg.xRot=0;
    }
    @Override public void renderToBuffer(MatrixStack p,IVertexBuilder b,int l,int o,float r,float g,float blue,float a){
        head.render(p,b,l,o,r,g,blue,a);
        body.render(p,b,l,o,r,g,blue,a);
        rightArm.render(p,b,l,o,r,g,blue,a);
        leftArm.render(p,b,l,o,r,g,blue,a);
        rightLeg.render(p,b,l,o,r,g,blue,a);
        leftLeg.render(p,b,l,o,r,g,blue,a);
    }
}
