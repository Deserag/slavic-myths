package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.entity.LeshyEntity;

public final class LeshyModel extends EntityModel<LeshyEntity> {
    private final boolean mor;
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final ModelPart head, body, leftArm, rightArm, leftLeg, rightLeg;
    public LeshyModel(){this(false);}
    public LeshyModel(boolean mor) {
        this.mor=mor;texWidth = 64; texHeight = 64;
        head = part(0, 0, 0, -3, 0, -3, -8, -3, 6, 8, 6);
        geometry.box(head,40, 0,-5, -16, -1, 2, 10, 2);
        geometry.box(head,40, 0,3, -17, -1, 2, 11, 2);
        geometry.box(head,40, 26,-8, -13, -1, 4, 2, 2);
        geometry.box(head,40, 26,4, -14, -1, 4, 2, 2);
        body = part(0, 16, 0, -3, 0, -3, 0, -2, 6, 13, 4);
        rightArm = part(24, 16, -4.5F, -2, 0, -1.5F, -1, -1.5F, 3, 19, 3);
        leftArm = part(24, 16, 4.5F, -2, 0, -1.5F, -1, -1.5F, 3, 19, 3);
        rightLeg = part(0, 36, -2, 10, 0, -1.5F, 0, -2, 3, 14, 4);
        leftLeg = part(0, 36, 2, 10, 0, -1.5F, 0, -2, 3, 14, 4);if(mor)morParts();
    }
    private void morParts(){
        for(int sign:new int[]{-1,1}){
            geometry.box(head,40,0,sign<0?-11:4,-12,-1,7,2,2);
            geometry.box(head,40,0,sign<0?-12:10,-17,-1,2,6,2);
            geometry.box(head,40,0,sign<0?-9:7,-20,-1,2,5,2);
            geometry.box(head,40,0,sign<0?-15:11,-12,-1,4,2,2);
            geometry.box(head,40,0,sign<0?-15:13,-15,-1,2,4,2);
            ModelPart arm=sign<0?rightArm:leftArm,leg=sign<0?rightLeg:leftLeg;
            geometry.box(arm,24,16,-2,-2,-2,4,6,4);
            for(int i=0;i<3;i++)geometry.box(arm,40,0,-1.5F+i,17,-2,1,5,1);
            geometry.box(leg,0,36,-2,12,-4,4,2,7);
            geometry.box(leg,40,0,sign<0?-4:1,12,-2,3,2,3);
            for(int i=0;i<3;i++)geometry.box(arm,48,48,-1.7F+i*1.2F,1,-2.2F,.7F,4+i%2*2,.6F);
            geometry.box(leg,48,48,-1.7F,6,-2.3F,1,4,.6F);
        }
        geometry.box(body,0,16,-3.3F,0,-2.4F,6.6F,8,1);
        for(int i=0;i<3;i++)geometry.box(body,48,48,-2.8F+i*2,7,-2.8F,1.5F,3+i%2,.6F);
    }
    private ModelPart part(int u, int v, float x, float y, float z, float bx, float by, float bz, int w, int h, int d) {
        ModelPart part = geometry.part(texWidth,texHeight,u, v);
        part.setPos(x,y,z); geometry.box(part,bx,by,bz,w,h,d); return part;
    }
    @Override public void setupAnim(LeshyEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        head.yRot = yaw * ((float)Math.PI / 180F); head.xRot = pitch * ((float)Math.PI / 180F);
        rightLeg.xRot = Mth.cos(walk * 0.5F) * amount * 0.7F; leftLeg.xRot = -rightLeg.xRot;
        float threat = -Mth.sin(attackTime * (float)Math.PI) * 1.2F;
        rightArm.xRot = threat + leftLeg.xRot * 0.5F; leftArm.xRot = threat + rightLeg.xRot * 0.5F;
        leftArm.zRot = 0.05F + Mth.sin(age * 0.04F) * 0.025F; rightArm.zRot = -leftArm.zRot;
        if(!mor&&entity.morState()==1){head.xRot=Mth.sin(age*1.4F)*.14F;rightArm.xRot=leftArm.xRot=.1F;return;}
 if(mor){poseMor(age,entity.morSkill(),entity.morElapsed(age-entity.tickCount),MorVisual.transform(entity));return;}
        switch(entity.attackPhase()){case TELEGRAPH->{rightArm.xRot=-1.6F;leftArm.xRot=-1.2F;}case ACTIVE->{rightArm.xRot=-.4F;leftArm.xRot=-.5F;}case RECOVERY->{rightArm.xRot=.3F;leftArm.xRot=.3F;}default->{}}
    }
    public void poseMor(float age,int skill,float elapsed,float transform){head.xRot=-.087F;head.yRot=Mth.sin(age*.105F)*.07F;float t=elapsed;if(transform>0){head.xRot-=transform*.17F;rightArm.xRot=leftArm.xRot=Mth.sin(age*1.4F)*.14F;}else if(skill==1){rightArm.xRot=leftArm.xRot=t<24?-.61F:.7F;}else if(skill==2){head.xRot=-.3F;rightArm.zRot=-.65F;leftArm.zRot=.65F;}}
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,int color) {
        head.render(pose,buffer,light,overlay,color); body.render(pose,buffer,light,overlay,color);
        leftArm.render(pose,buffer,light,overlay,color); rightArm.render(pose,buffer,light,overlay,color);
        leftLeg.render(pose,buffer,light,overlay,color); rightLeg.render(pose,buffer,light,overlay,color);
    }
}
