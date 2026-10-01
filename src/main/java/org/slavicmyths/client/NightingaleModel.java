package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.bandit.NightingaleEntity;

/** 37.5 model units: broad human torso, articulated arms, layered hair and beard.
 * Each material occupies its own 64x64 UV cell in the authored atlas. */
public final class NightingaleModel extends EntityModel<NightingaleEntity> {
    private final BreathingChest chest;
    private final ModelRenderer root,body,head,jaw,hair,beard,left,right,leftFore,rightFore,leftLeg,rightLeg,dagger,sheath,sheathedHilt;
    private ModelRenderer part(ModelRenderer parent,int material,float x,float y,float z){ModelRenderer p=new ModelRenderer(this,(material%4)*64,(material/4)*64);p.setPos(x,y,z);if(parent!=null)parent.addChild(p);return p;}
    private ModelRenderer box(ModelRenderer parent,int mat,float x,float y,float z,float w,float h,float d){ModelRenderer p=part(parent,mat,0,0,0);p.addBox(x,y,z,w,h,d);return p;}
    public NightingaleModel(){texWidth=256;texHeight=256;
        root=part(null,0,0,24,0);body=part(root,1,0,-15,0);body.addBox(-7,-13,-4,14,13,8);
        chest=new BreathingChest();body.addChild(chest);chest.addBox(-8,-14,-4.5F,16,9,9);box(body,2,-7.3F,-2.5F,-4.3F,14.6F,3,8.6F);
        box(body,5,-2,-2.2F,-4.8F,3.5F,2.6F,.7F);box(body,2,4,-.5F,-4.7F,3,4,2);box(body,5,-5,-1,-5,2,2,.6F);
        ModelRenderer strap=box(body,2,-1,-14,-4.9F,2,14,.7F);strap.zRot=-.5F;
        box(body,3,-6,-10,-4.7F,3,4,.4F);box(body,3,3,-6,-4.6F,3,2,.3F);
        box(body,0,-3,-16,-2.5F,6,3,5);
        head=part(body,0,0,-16,0);head.addBox(-4.6F,-6.5F,-4,9.2F,7,7.5F);
        box(head,0,-1.6F,-3.4F,-5.8F,3.2F,2.7F,2); // wide nose
        box(head,0,-5.3F,-3,-1,1,2.5F,2);box(head,0,4.3F,-3,-1,1,2.5F,2);
        for(int side:new int[]{-1,1}){box(head,7,side<0?-3.8F:1.2F,-4.5F,-4.15F,2.6F,1.5F,.2F);box(head,6,side<0?-3.1F:1.8F,-4.1F,-4.4F,1.2F,.6F,.3F);ModelRenderer brow=box(head,4,side<0?-4:1,-5.2F,-4.5F,3,1,.8F);brow.zRot=side*.08F;}
        jaw=part(head,0,0,0,0);jaw.addBox(-3.8F,-.4F,-4.3F,7.6F,2,6);
        box(jaw,7,-1.5F,-.65F,-4.65F,3,.6F,.4F);box(jaw,4,-3.5F,.5F,-4.5F,7,1.2F,.5F);
        beard=part(jaw,4,0,1,-3.3F);beard.addBox(-3.3F,0,-1,6.6F,2.7F,2);
        for(int i=0;i<3;i++)box(beard,4,-2.8F+i*2,2,-.8F,1.6F,2+(i==1?1:0),1.6F);
        hair=part(head,4,0,0,0);hair.addBox(-4.9F,-7,-3.3F,9.8F,2,7.5F);
        for(int i=0;i<5;i++)box(hair,4,-4.5F+i*1.8F,-5,3,1.9F,9+(i%2),2);
        for(int side:new int[]{-1,1}){box(hair,4,side<0?-5.2F:3.8F,-5,-2.5F,1.4F,8,4);box(hair,4,side<0?-5.6F:4.2F,1,-1.5F,1.4F,4,3);}
        left=part(body,1,8,-12,0);left.addBox(-.2F,-1,-3.2F,5.2F,6.5F,6.4F);
        right=part(body,1,-8,-12,0);right.addBox(-5,-1,-3.2F,5.2F,6.5F,6.4F);
        leftFore=part(left,0,2.5F,5,0);leftFore.addBox(-2.5F,0,-2.8F,5,6,5.6F);box(leftFore,2,-2.7F,4,-3,5.4F,2,6);box(leftFore,0,-2.7F,6,-3,5.4F,3,6);
        rightFore=part(right,0,-2.5F,5,0);rightFore.addBox(-2.5F,0,-2.8F,5,6,5.6F);box(rightFore,2,-2.7F,4,-3,5.4F,2,6);box(rightFore,0,-2.7F,6,-3,5.4F,3,6);
        dagger=part(rightFore,2,0,7,-1);dagger.addBox(-.7F,-2,-.6F,1.4F,4,1.2F);box(dagger,5,-2.5F,2,-.8F,5,1,1.6F);box(dagger,5,-1.5F,3,-.5F,3,7,1);box(dagger,6,-1.5F,3,-.65F,.5F,7,1.3F);box(dagger,5,-.8F,10,-.4F,1.6F,2,.8F);box(dagger,5,-.4F,12,-.3F,.8F,1,.6F);
        sheath=part(body,2,-6,0,0);sheath.zRot=.18F;sheath.addBox(-1.5F,-1,-1,3,10,2);box(sheath,5,-2,-1,-1.3F,4,1,2.6F);sheathedHilt=box(sheath,2,-.7F,-4,-.7F,1.4F,3,1.4F);
        leftLeg=part(root,3,3.6F,-15,0);rightLeg=part(root,3,-3.6F,-15,0);
        for(ModelRenderer leg:new ModelRenderer[]{leftLeg,rightLeg}){leg.addBox(-3,0,-3,6,9,6);box(leg,2,-3.2F,8,-3.2F,6.4F,7,6.4F);box(leg,2,-3.3F,12,-5,6.6F,3,8.4F);box(leg,5,-3.4F,9,-3.4F,1,1,1);}
    }
    @Override public void setupAnim(NightingaleEntity e,float stride,float amount,float age,float yaw,float pitch){
        float t=e.elapsed(age-e.tickCount),breath=MathHelper.sin(age*.09F)*.025F,walk=MathHelper.cos(stride*.64F)*amount;
        root.xRot=0;root.y=24;body.xRot=breath;body.zRot=walk*.025F;body.y=-15;chest.xRot=breath;chest.z=-breath*3;chest.expansion=breath;
        head.yRot=yaw*.017453F;head.xRot=pitch*.017453F;head.z=0;jaw.xRot=0;hair.xRot=breath;beard.xRot=-breath;
        left.xRot=walk*.22F;right.xRot=-walk*.22F;left.zRot=-.08F;right.zRot=.08F;leftFore.xRot=rightFore.xRot=-.12F;
        leftLeg.xRot=-walk*.62F;rightLeg.xRot=walk*.62F;leftLeg.zRot=rightLeg.zRot=0;
        int a=e.action();dagger.visible=e.drawn()||a==11;sheathedHilt.visible=!dagger.visible;
        if(a==1&&t>23){right.xRot=-.8F;right.zRot=-.3F;rightFore.xRot=-1.1F;}
        if(a==2||a==3){float prep=Math.min(1,t/(a==3?23:11));right.xRot=-.4F-prep*(a==3?2.2F:1.3F);right.zRot=.15F+prep*.25F;rightFore.xRot=-.5F;body.yRot=-prep*.25F;}else body.yRot=0;
        if(a==12||a==13){float swing=Math.min(1,t/8);right.xRot=-1.4F+swing*2.4F;right.zRot=.25F;rightFore.xRot=-.2F;body.yRot=.25F*swing;body.xRot+=.15F;}
        if(a==14){body.xRot=.32F;left.xRot=-1.7F;leftFore.xRot=0;}
        if(a==4){body.xRot=.25F;left.xRot=-1.3F;leftFore.xRot=-.6F;}
        if(a==5){body.xRot=-.15F;leftLeg.xRot=.3F;rightLeg.xRot=-.25F;}
        if(a==6||a==7||a==8){float p=Math.min(1,t/(a==7?40:a==8?16:27));body.xRot=-.08F-p*.12F;body.y=-15+p*.6F;chest.z=-p*.3F;chest.expansion=p;chest.xRot=-p*.07F;head.xRot=-p*.24F;left.zRot=-.2F-p*.15F;right.zRot=.2F+p*.15F;leftFore.xRot=rightFore.xRot=-.45F;jaw.xRot=p*.2F;leftLeg.zRot=-.06F;rightLeg.zRot=.06F;head.yRot=0;}
        if(a==9||a==10){chest.expansion=Math.max(0,1-t/5);body.xRot=.3F;head.xRot=.12F;head.z=-.4F;jaw.xRot=.12F;left.xRot=right.xRot=-.35F;hair.xRot=.12F;beard.xRot=.2F;}
        if(e.hurtTime>0&&a!=11){body.zRot+=MathHelper.sin(e.hurtTime*.5F)*(.04F+e.impact()*.14F);head.xRot-=.06F;}
        if(a==11){float p=Math.min(1,t/55);right.xRot=p*.5F;left.xRot=p*.3F;body.xRot=.2F;head.xRot=t<30?-.2F:.35F;jaw.xRot=t<30?.2F:0;root.xRot=Math.max(0,(p-.48F)/.52F)*1.5F;root.y=24-p*2;leftLeg.xRot=rightLeg.xRot=.1F;}
    }
    private final class BreathingChest extends ModelRenderer {
        float expansion;
        BreathingChest(){super(NightingaleModel.this,64,0);}
        @Override public void render(MatrixStack pose,IVertexBuilder out,int light,int overlay,float r,float g,float b,float a){pose.pushPose();pose.scale(1+expansion*.035F,1+expansion*.015F,1+expansion*.1F);super.render(pose,out,light,overlay,r,g,b,a);pose.popPose();}
    }
    @Override public void renderToBuffer(MatrixStack pose,IVertexBuilder out,int light,int overlay,float r,float g,float b,float a){root.render(pose,out,light,overlay,r,g,b,a);}
}
