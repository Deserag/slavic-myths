package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.BannikEntity;
public final class BannikModel extends EntityModel<BannikEntity> {
    private final ModelRenderer head, body, rightArm, leftArm, rightLeg, leftLeg;
    public BannikModel() {texWidth=256;texHeight=256;
        head=new ModelRenderer(this);head.setPos(0.0F,5.0F,-3.0F);
        head.texOffs(0,0).addBox(-4.0F,-7.0F,-3.0F,8.0F,7.0F,6.0F);
        head.texOffs(29,0).addBox(-4.0F,-5.0F,-3.7F,8.0F,1.5F,1.0F);
        head.texOffs(48,0).addBox(-1.5F,-3.0F,-5.0F,3.0F,2.0F,2.0F);
        head.texOffs(59,0).addBox(-3.0F,0.0F,-4.0F,6.0F,5.0F,3.0F);
        head.texOffs(78,0).addBox(-2.0F,5.0F,-3.0F,4.0F,3.0F,2.0F);
        head.texOffs(91,0).addBox(1.0F,2.0F,-4.2F,1.0F,2.0F,1.0F);
        body=new ModelRenderer(this);body.setPos(0.0F,6.0F,0.0F);
        body.texOffs(96,0).addBox(-6.0F,0.0F,-3.0F,12.0F,10.0F,7.0F);
        body.texOffs(135,0).addBox(-6.0F,8.0F,-3.3F,12.0F,5.0F,7.6F);
        rightArm=new ModelRenderer(this);rightArm.setPos(-7.0F,6.0F,0.0F);
        rightArm.texOffs(176,0).addBox(-2.0F,0.0F,-2.0F,4.0F,12.0F,4.0F);
        rightArm.texOffs(193,0).addBox(-2.5F,10.0F,-2.5F,5.0F,4.0F,5.0F);
        rightArm.texOffs(214,0).addBox(-2.0F,13.0F,-2.0F,1.0F,2.0F,1.0F);
        rightArm.texOffs(219,0).addBox(0.0F,13.0F,-2.0F,1.0F,2.0F,1.0F);
        rightArm.texOffs(224,0).addBox(2.0F,13.0F,-2.0F,1.0F,2.0F,1.0F);
        leftArm=new ModelRenderer(this);leftArm.setPos(7.0F,6.0F,0.0F);
        leftArm.texOffs(229,0).addBox(-2.0F,0.0F,-2.0F,4.0F,12.0F,4.0F);
        leftArm.texOffs(0,18).addBox(-2.5F,10.0F,-2.5F,5.0F,4.0F,5.0F);
        leftArm.texOffs(21,18).addBox(-2.0F,13.0F,-2.0F,1.0F,2.0F,1.0F);
        leftArm.texOffs(26,18).addBox(0.0F,13.0F,-2.0F,1.0F,2.0F,1.0F);
        leftArm.texOffs(31,18).addBox(2.0F,13.0F,-2.0F,1.0F,2.0F,1.0F);
        rightLeg=new ModelRenderer(this);rightLeg.setPos(-3.0F,17.0F,0.0F);
        rightLeg.texOffs(36,18).addBox(-2.0F,0.0F,-2.0F,4.0F,7.0F,4.0F);
        leftLeg=new ModelRenderer(this);leftLeg.setPos(3.0F,17.0F,0.0F);
        leftLeg.texOffs(53,18).addBox(-2.0F,0.0F,-2.0F,4.0F,7.0F,4.0F);
    }
    @Override public void setupAnim(BannikEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        head.y=5.0F;head.xRot=head.yRot=head.zRot=0;
        body.y=6.0F;body.xRot=body.yRot=body.zRot=0;
        rightArm.y=6.0F;rightArm.xRot=rightArm.yRot=rightArm.zRot=0;
        leftArm.y=6.0F;leftArm.xRot=leftArm.yRot=leftArm.zRot=0;
        rightLeg.y=17.0F;rightLeg.xRot=rightLeg.yRot=rightLeg.zRot=0;
        leftLeg.y=17.0F;leftLeg.xRot=leftLeg.yRot=leftLeg.zRot=0;
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        body.xRot=.15F;head.xRot+=.1F;rightLeg.xRot=MathHelper.cos(walk*.7F)*amount*.5F;leftLeg.xRot=-rightLeg.xRot;rightArm.xRot=-.15F-MathHelper.sin(attackTime*3.14159F)*1.2F;leftArm.xRot=-.15F;if(entity.state()==4){head.xRot=-.35F;rightArm.xRot=leftArm.xRot=-1.1F;}
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
