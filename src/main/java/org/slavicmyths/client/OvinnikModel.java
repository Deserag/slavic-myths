package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.OvinnikEntity;
public final class OvinnikModel extends EntityModel<OvinnikEntity> {
    private final ModelRenderer head, body, rightArm, leftArm, rightLeg, leftLeg, tail, jaw;
    public OvinnikModel() {texWidth=256;texHeight=256;
        head=new ModelRenderer(this);head.setPos(0.0F,7.0F,-11.0F);
        head.texOffs(0,0).addBox(-5.0F,-5.0F,-4.0F,10.0F,9.0F,8.0F);
        head.texOffs(37,0).addBox(-4.0F,0.0F,-7.0F,8.0F,3.0F,4.0F);
        head.texOffs(62,0).addBox(-7.0F,-8.0F,-1.0F,3.0F,5.0F,2.0F);
        head.texOffs(73,0).addBox(4.0F,-8.0F,-1.0F,3.0F,5.0F,2.0F);
        body=new ModelRenderer(this);body.setPos(0.0F,7.0F,0.0F);
        body.texOffs(84,0).addBox(-6.0F,-5.0F,-10.0F,12.0F,12.0F,23.0F);
        body.texOffs(155,0).addBox(-5.0F,-7.0F,-5.0F,10.0F,3.0F,14.0F);
        body.texOffs(204,0).addBox(-7.0F,-3.0F,-10.0F,14.0F,8.0F,6.0F);
        rightArm=new ModelRenderer(this);rightArm.setPos(-5.0F,12.0F,-8.0F);
        rightArm.texOffs(0,36).addBox(-2.5F,0.0F,-2.5F,5.0F,12.0F,5.0F);
        leftArm=new ModelRenderer(this);leftArm.setPos(5.0F,12.0F,-8.0F);
        leftArm.texOffs(21,36).addBox(-2.3F,0.0F,-2.5F,4.6F,11.0F,5.0F);
        leftArm.texOffs(42,36).addBox(-3.0F,9.0F,-4.0F,6.0F,3.0F,6.0F);
        leftArm.texOffs(67,36).addBox(-2.0F,11.0F,-6.0F,1.0F,1.0F,3.0F);
        leftArm.texOffs(76,36).addBox(0.0F,11.0F,-6.0F,1.0F,1.0F,3.0F);
        leftArm.texOffs(85,36).addBox(2.0F,11.0F,-6.0F,1.0F,1.0F,3.0F);
        rightLeg=new ModelRenderer(this);rightLeg.setPos(-4.5F,13.0F,9.0F);
        rightLeg.texOffs(94,36).addBox(-2.3F,0.0F,-2.5F,4.6F,11.0F,5.0F);
        leftLeg=new ModelRenderer(this);leftLeg.setPos(4.5F,13.0F,9.0F);
        leftLeg.texOffs(115,36).addBox(-2.3F,0.0F,-2.5F,4.6F,11.0F,5.0F);
        tail=new ModelRenderer(this);tail.setPos(0.0F,7.0F,12.0F);
        tail.texOffs(136,36).addBox(-1.5F,-1.5F,0.0F,3.0F,3.0F,11.0F);
        tail.texOffs(165,36).addBox(-2.0F,-2.0F,9.0F,4.0F,4.0F,5.0F);
        jaw=new ModelRenderer(this);jaw.setPos(0.0F,7.0F,-11.0F);
        jaw.texOffs(184,36).addBox(-3.0F,3.0F,-6.0F,6.0F,2.0F,4.0F);
        jaw.texOffs(205,36).addBox(-3.0F,2.0F,-6.0F,1.0F,2.0F,1.0F);
        jaw.texOffs(210,36).addBox(2.0F,2.0F,-6.0F,1.0F,2.0F,1.0F);
    }
    @Override public void setupAnim(OvinnikEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        head.y=7.0F;head.xRot=head.yRot=head.zRot=0;
        body.y=7.0F;body.xRot=body.yRot=body.zRot=0;
        rightArm.y=12.0F;rightArm.xRot=rightArm.yRot=rightArm.zRot=0;
        leftArm.y=12.0F;leftArm.xRot=leftArm.yRot=leftArm.zRot=0;
        rightLeg.y=13.0F;rightLeg.xRot=rightLeg.yRot=rightLeg.zRot=0;
        leftLeg.y=13.0F;leftLeg.xRot=leftLeg.yRot=leftLeg.zRot=0;
        tail.y=7.0F;tail.xRot=tail.yRot=tail.zRot=0;
        jaw.y=7.0F;jaw.xRot=jaw.yRot=jaw.zRot=0;
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        rightArm.xRot=MathHelper.cos(walk*.65F)*amount*.7F;leftArm.xRot=-rightArm.xRot;rightLeg.xRot=-rightArm.xRot;leftLeg.xRot=rightArm.xRot;tail.yRot=MathHelper.sin(age*.06F)*.25F;tail.xRot=entity.state()==0?.35F:-.35F;jaw.visible=entity.state()!=0;jaw.yRot=head.yRot;if(entity.state()==0){body.y+=3;head.y+=3;rightArm.xRot=leftArm.xRot=-.65F;rightLeg.xRot=leftLeg.xRot=-.8F;}if(entity.state()==4)leftArm.xRot=-1.6F;if(entity.state()==5){body.y+=2;head.xRot=-.25F;}
    }
    @Override public void renderToBuffer(MatrixStack p,IVertexBuilder b,int l,int o,float r,float g,float blue,float a){
        head.render(p,b,l,o,r,g,blue,a);
        body.render(p,b,l,o,r,g,blue,a);
        rightArm.render(p,b,l,o,r,g,blue,a);
        leftArm.render(p,b,l,o,r,g,blue,a);
        rightLeg.render(p,b,l,o,r,g,blue,a);
        leftLeg.render(p,b,l,o,r,g,blue,a);
        tail.render(p,b,l,o,r,g,blue,a);
        jaw.render(p,b,l,o,r,g,blue,a);
    }
}
