package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.KikimoraEntity;
public final class KikimoraModel extends EntityModel<KikimoraEntity> {
    private final ModelRenderer head, body, rightArm, leftArm, rightLeg, leftLeg, bonnet, cap;
    public KikimoraModel() {texWidth=256;texHeight=256;
        head=new ModelRenderer(this);head.setPos(0.0F,-5.0F,-2.0F);
        head.texOffs(0,0).addBox(-2.0F,-6.0F,-2.0F,4.0F,7.0F,4.0F);
        head.texOffs(17,0).addBox(-0.6F,-3.0F,-6.0F,1.0F,4.0F,4.0F);
        head.texOffs(28,0).addBox(-2.5F,-5.0F,-1.0F,1.0F,11.0F,1.0F);
        head.texOffs(33,0).addBox(1.8F,-5.0F,0.0F,1.0F,13.0F,1.0F);
        head.texOffs(38,0).addBox(-1.0F,-5.0F,2.0F,1.0F,12.0F,1.0F);
        body=new ModelRenderer(this);body.setPos(0.0F,-4.0F,0.0F);
        body.texOffs(43,0).addBox(-2.5F,0.0F,-1.7F,5.0F,9.0F,4.0F);
        body.texOffs(62,0).addBox(-3.0F,7.0F,-2.0F,2.0F,4.0F,4.0F);
        body.texOffs(75,0).addBox(1.0F,7.0F,-2.0F,2.0F,3.0F,4.0F);
        rightArm=new ModelRenderer(this);rightArm.setPos(-3.0F,-3.0F,0.0F);
        rightArm.texOffs(88,0).addBox(-0.7F,0.0F,-0.7F,1.5F,13.0F,1.5F);
        rightArm.texOffs(97,0).addBox(-1.0F,12.0F,-1.0F,2.0F,3.0F,2.0F);
        rightArm.texOffs(106,0).addBox(-1.0F,14.0F,-1.0F,0.5F,3.0F,0.6F);
        rightArm.texOffs(111,0).addBox(0.0F,14.0F,-1.0F,0.5F,3.0F,0.6F);
        rightArm.texOffs(116,0).addBox(1.0F,14.0F,-1.0F,0.5F,3.0F,0.6F);
        leftArm=new ModelRenderer(this);leftArm.setPos(3.0F,-3.0F,0.0F);
        leftArm.texOffs(121,0).addBox(-0.7F,0.0F,-0.7F,1.5F,13.0F,1.5F);
        leftArm.texOffs(130,0).addBox(-1.0F,12.0F,-1.0F,2.0F,3.0F,2.0F);
        leftArm.texOffs(139,0).addBox(-1.0F,14.0F,-1.0F,0.5F,3.0F,0.6F);
        leftArm.texOffs(144,0).addBox(0.0F,14.0F,-1.0F,0.5F,3.0F,0.6F);
        leftArm.texOffs(149,0).addBox(1.0F,14.0F,-1.0F,0.5F,3.0F,0.6F);
        rightLeg=new ModelRenderer(this);rightLeg.setPos(-1.5F,5.0F,0.0F);
        rightLeg.texOffs(154,0).addBox(-0.8F,0.0F,-0.8F,1.6F,7.0F,2.0F);
        rightLeg.texOffs(163,0).addBox(-0.5F,7.0F,0.6F,1.0F,10.0F,1.0F);
        rightLeg.texOffs(168,0).addBox(-0.8F,15.0F,-0.3F,1.6F,2.0F,2.0F);
        rightLeg.texOffs(177,0).addBox(-1.0F,17.0F,-3.0F,0.5F,1.0F,4.0F);
        rightLeg.texOffs(188,0).addBox(0.0F,17.0F,-3.0F,0.5F,1.0F,4.0F);
        rightLeg.texOffs(199,0).addBox(1.0F,17.0F,-3.0F,0.5F,1.0F,4.0F);
        rightLeg.texOffs(210,0).addBox(-0.3F,17.0F,1.0F,0.6F,1.0F,2.0F);
        leftLeg=new ModelRenderer(this);leftLeg.setPos(1.5F,5.0F,0.0F);
        leftLeg.texOffs(217,0).addBox(-0.8F,0.0F,-0.8F,1.6F,7.0F,2.0F);
        leftLeg.texOffs(226,0).addBox(-0.5F,7.0F,0.6F,1.0F,10.0F,1.0F);
        leftLeg.texOffs(231,0).addBox(-0.8F,15.0F,-0.3F,1.6F,2.0F,2.0F);
        leftLeg.texOffs(240,0).addBox(-1.0F,17.0F,-3.0F,0.5F,1.0F,4.0F);
        leftLeg.texOffs(0,16).addBox(0.0F,17.0F,-3.0F,0.5F,1.0F,4.0F);
        leftLeg.texOffs(11,16).addBox(1.0F,17.0F,-3.0F,0.5F,1.0F,4.0F);
        leftLeg.texOffs(22,16).addBox(-0.3F,17.0F,1.0F,0.6F,1.0F,2.0F);
        bonnet=new ModelRenderer(this);bonnet.setPos(0.0F,-5.0F,-2.0F);
        bonnet.texOffs(29,16).addBox(-3.0F,-7.0F,-2.5F,6.0F,2.0F,5.0F);
        bonnet.texOffs(52,16).addBox(-3.0F,-5.0F,1.0F,6.0F,3.0F,2.0F);
        cap=new ModelRenderer(this);cap.setPos(0.0F,-5.0F,-2.0F);
        cap.texOffs(69,16).addBox(-2.5F,-7.0F,-2.5F,5.0F,2.0F,5.0F);
    }
    @Override public void setupAnim(KikimoraEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        head.y=-5.0F;head.xRot=head.yRot=head.zRot=0;
        body.y=-4.0F;body.xRot=body.yRot=body.zRot=0;
        rightArm.y=-3.0F;rightArm.xRot=rightArm.yRot=rightArm.zRot=0;
        leftArm.y=-3.0F;leftArm.xRot=leftArm.yRot=leftArm.zRot=0;
        rightLeg.y=5.0F;rightLeg.xRot=rightLeg.yRot=rightLeg.zRot=0;
        leftLeg.y=5.0F;leftLeg.xRot=leftLeg.yRot=leftLeg.zRot=0;
        bonnet.y=-5.0F;bonnet.xRot=bonnet.yRot=bonnet.zRot=0;
        cap.y=-5.0F;cap.xRot=cap.yRot=cap.zRot=0;
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        head.xRot += .16F + MathHelper.sin(age*.035F)*.06F; body.xRot=.13F; float step=MathHelper.cos(walk*(entity.state()==4?1.5F:1))*amount;rightLeg.xRot=step*.9F;leftLeg.xRot=-step*.9F;rightArm.xRot=-.3F-step*.25F-MathHelper.sin(attackTime*3.14159F)*1.7F;leftArm.xRot=-.3F+step*.25F;bonnet.visible=(entity.variant()/4)==0;cap.visible=!bonnet.visible;bonnet.xRot=head.xRot;bonnet.yRot=head.yRot;cap.xRot=head.xRot;cap.yRot=head.yRot;if(entity.state()==1){rightLeg.xRot=leftLeg.xRot=0;}
    }
    @Override public void renderToBuffer(MatrixStack p,IVertexBuilder b,int l,int o,float r,float g,float blue,float a){
        head.render(p,b,l,o,r,g,blue,a);
        body.render(p,b,l,o,r,g,blue,a);
        rightArm.render(p,b,l,o,r,g,blue,a);
        leftArm.render(p,b,l,o,r,g,blue,a);
        rightLeg.render(p,b,l,o,r,g,blue,a);
        leftLeg.render(p,b,l,o,r,g,blue,a);
        bonnet.render(p,b,l,o,r,g,blue,a);
        cap.render(p,b,l,o,r,g,blue,a);
    }
}
