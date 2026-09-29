package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.PolevikEntity;
public final class PolevikModel extends EntityModel<PolevikEntity> {
    private final ModelRenderer head, body, rightArm, leftArm, rightLeg, leftLeg;
    public PolevikModel() {texWidth=256;texHeight=256;
        head=new ModelRenderer(this);head.setPos(0.0F,-3.0F,0.0F);
        head.texOffs(0,0).addBox(-2.5F,-6.0F,-2.0F,5.0F,6.0F,4.0F);
        head.texOffs(19,0).addBox(-3.0F,-10.0F,0.0F,1.0F,4.0F,1.0F);
        head.texOffs(24,0).addBox(-1.0F,-11.0F,0.0F,1.0F,5.0F,1.0F);
        head.texOffs(29,0).addBox(1.0F,-9.0F,0.0F,1.0F,3.0F,1.0F);
        head.texOffs(34,0).addBox(3.0F,-10.0F,0.0F,1.0F,4.0F,1.0F);
        head.texOffs(39,0).addBox(-2.0F,-1.0F,-3.0F,1.0F,7.0F,1.0F);
        head.texOffs(44,0).addBox(-1.0F,-1.0F,-3.0F,1.0F,10.0F,1.0F);
        head.texOffs(49,0).addBox(0.0F,-1.0F,-3.0F,1.0F,11.0F,1.0F);
        head.texOffs(54,0).addBox(1.0F,-1.0F,-3.0F,1.0F,9.0F,1.0F);
        head.texOffs(59,0).addBox(2.0F,-1.0F,-3.0F,1.0F,6.0F,1.0F);
        body=new ModelRenderer(this);body.setPos(0.0F,-2.0F,0.0F);
        body.texOffs(64,0).addBox(-2.0F,0.0F,-1.0F,1.0F,12.0F,2.0F);
        body.texOffs(71,0).addBox(-1.0F,0.0F,-1.0F,1.0F,12.0F,2.0F);
        body.texOffs(78,0).addBox(0.0F,0.0F,-1.0F,1.0F,12.0F,2.0F);
        body.texOffs(85,0).addBox(1.0F,0.0F,-1.0F,1.0F,12.0F,2.0F);
        body.texOffs(92,0).addBox(2.0F,0.0F,-1.0F,1.0F,12.0F,2.0F);
        body.texOffs(99,0).addBox(-2.0F,3.0F,-2.0F,1.0F,5.0F,1.0F);
        body.texOffs(104,0).addBox(1.0F,3.0F,-2.0F,1.0F,5.0F,1.0F);
        rightArm=new ModelRenderer(this);rightArm.setPos(-3.5F,-1.0F,0.0F);
        rightArm.texOffs(109,0).addBox(-0.7F,0.0F,-0.7F,1.4F,16.0F,1.4F);
        rightArm.texOffs(118,0).addBox(-1.0F,15.0F,-1.0F,0.5F,4.0F,0.5F);
        rightArm.texOffs(123,0).addBox(0.0F,15.0F,-1.0F,0.5F,4.0F,0.5F);
        rightArm.texOffs(128,0).addBox(1.0F,15.0F,-1.0F,0.5F,4.0F,0.5F);
        leftArm=new ModelRenderer(this);leftArm.setPos(3.5F,-1.0F,0.0F);
        leftArm.texOffs(133,0).addBox(-0.7F,0.0F,-0.7F,1.4F,16.0F,1.4F);
        leftArm.texOffs(142,0).addBox(-1.0F,15.0F,-1.0F,0.5F,4.0F,0.5F);
        leftArm.texOffs(147,0).addBox(0.0F,15.0F,-1.0F,0.5F,4.0F,0.5F);
        leftArm.texOffs(152,0).addBox(1.0F,15.0F,-1.0F,0.5F,4.0F,0.5F);
        rightLeg=new ModelRenderer(this);rightLeg.setPos(-2.0F,10.0F,0.0F);
        rightLeg.texOffs(157,0).addBox(-1.0F,0.0F,-0.5F,0.7F,13.0F,1.3F);
        rightLeg.texOffs(164,0).addBox(0.0F,0.0F,-0.5F,0.7F,13.0F,1.3F);
        rightLeg.texOffs(171,0).addBox(1.0F,0.0F,-0.5F,0.7F,13.0F,1.3F);
        rightLeg.texOffs(178,0).addBox(-1.5F,12.0F,-2.0F,3.0F,2.0F,4.0F);
        leftLeg=new ModelRenderer(this);leftLeg.setPos(2.0F,10.0F,0.0F);
        leftLeg.texOffs(193,0).addBox(-1.0F,0.0F,-0.5F,0.7F,13.0F,1.3F);
        leftLeg.texOffs(200,0).addBox(0.0F,0.0F,-0.5F,0.7F,13.0F,1.3F);
        leftLeg.texOffs(207,0).addBox(1.0F,0.0F,-0.5F,0.7F,13.0F,1.3F);
        leftLeg.texOffs(214,0).addBox(-1.5F,12.0F,-2.0F,3.0F,2.0F,4.0F);
    }
    @Override public void setupAnim(PolevikEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        head.y=-3.0F;head.xRot=head.yRot=head.zRot=0;
        body.y=-2.0F;body.xRot=body.yRot=body.zRot=0;
        rightArm.y=-1.0F;rightArm.xRot=rightArm.yRot=rightArm.zRot=0;
        leftArm.y=-1.0F;leftArm.xRot=leftArm.yRot=leftArm.zRot=0;
        rightLeg.y=10.0F;rightLeg.xRot=rightLeg.yRot=rightLeg.zRot=0;
        leftLeg.y=10.0F;leftLeg.xRot=leftLeg.yRot=leftLeg.zRot=0;
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        rightLeg.xRot=MathHelper.cos(walk*.6F)*amount*.6F;leftLeg.xRot=-rightLeg.xRot;rightArm.zRot=-.09F-MathHelper.sin(age*.05F)*.08F;leftArm.zRot=-rightArm.zRot;rightArm.xRot=-MathHelper.sin(attackTime*3.14159F)*1.5F;if(entity.state()==1){head.y+=8;body.y+=8;rightArm.y+=8;leftArm.y+=8;rightLeg.xRot=-1.15F;leftLeg.xRot=-1.15F;}if(entity.state()==2){rightArm.xRot=leftArm.xRot=-1;}
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
