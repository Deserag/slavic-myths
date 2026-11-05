package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.LomeyaBoss;

/** 2.5.1 articulated cuboid rig. Local Y is down, 16 units = one Minecraft block. */
public final class LomeyaModel extends EntityModel<LomeyaBoss> {
    private final FolkModelGeometry geo = new FolkModelGeometry();
    private final ModelPart root=geo.part(512,512),head=geo.part(512,512),giant=geo.part(512,512);
    private final ModelPart leftUpper=geo.part(512,512),leftLower=geo.part(512,512),leftHand=geo.part(512,512);
    private final ModelPart rightUpper=geo.part(512,512),rightLower=geo.part(512,512),rightHand=geo.part(512,512);
    private final ModelPart smallL=geo.part(512,512),smallR=geo.part(512,512);
    private final ModelPart leftLeg=geo.part(512,512),rightLeg=geo.part(512,512);
    private static final float[] SHOULDER_L={-7.7F,78F,0F},SHOULDER_R={7.7F,78F,0F};

    private void box(ModelPart p,int material,float x,float y,float z,float w,float h,float d) {
        geo.box(p, (material%4)*128+4,(material/4)*128+4,x,y,z,w,h,d);
    }
    private void hammer(ModelPart part,boolean heavy) {
        float shaft=heavy?63F:29F,below=heavy?7F:5F;
        float headY=heavy?-53F:-21F;
        float halfLength=heavy?14.5F:9F,headW=heavy?18F:9F,headH=heavy?11F:6.5F;
        float grip=heavy?2.8F:1.8F;
        box(part,2,-grip/2,-shaft,-grip/2,grip,shaft+below,grip);
        for(int i=0;i<4;i++) box(part,10,-grip/2-.2F,heavy?-27+i*6:-3+i*2,-grip/2-.2F,grip+.4F,heavy?2.6F:1.5F,grip+.4F);
        // Long striking head is along Z (front/back of boss). The shaft lies on its exact center.
        box(part,8,-headW/2,headY-headH/2,-halfLength,headW,headH,2*halfLength);
        for(int sign:new int[]{-1,1}) {
            float faceZ=sign*(halfLength+.65F);
            box(part,9,-headW/2-.75F,headY-headH/2-.5F,faceZ-.85F,headW+1.5F,headH+1.0F,1.7F);
            box(part,13,-.55F,headY-headH/2+.9F,faceZ+(sign>0?.9F:-.9F),1.1F,headH-1.8F,.4F);
            box(part,13,-headW*.30F,headY-.6F,faceZ+(sign>0?.9F:-.9F),headW*.6F,1.2F,.4F);
            for(int f=-1;f<=1;f++) box(part,2,f*(headW*.24F)-.7F,headY-headH/2-1F,sign*(halfLength-2.5F),1.4F,2F,2.2F);
        }
        box(part,14,-headW/2-.8F,headY-headH/2-.8F,-3F,headW+1.6F,headH+1.6F,6F);
    }
    public LomeyaModel() {
        root.setPos(0,24,0);
        // Separate walking legs with folded cloth panels and grounded boots.
        for(int side:new int[]{-1,1}) {
            ModelPart leg=side<0?leftLeg:rightLeg;
            leg.setPos(side*3.4F,-49F,0);
            box(leg,3,-2.1F,0,-2.7F,4.2F,27,5.4F);
            box(leg,5,-2F,26,-2.5F,4F,14F,5F);
            box(leg,11,-2.5F,39,-6.5F,5F,10F,9F);
            geo.attach(root,leg);
        }
        for(int tier=0;tier<3;tier++) {
            float y=-55+tier*7,wide=13+2.4F*tier,depth=9+2.4F*tier;
            box(root,3,-wide/2,y,depth/2-1.1F,wide,7,1.1F);
            box(root,4,-wide/2,y,-depth/2,wide/2-.6F,7,1.0F);
            box(root,4,.6F,y,-depth/2,wide/2-.6F,7,1.0F);
            box(root,3,-wide/2,y,-depth/2,1.1F,7,depth);
            box(root,3,wide/2-1.1F,y,-depth/2,1.1F,7,depth);
        }
        box(root,3,-6.6F,-78,-4.1F,13.2F,10,8.2F);
        box(root,4,-5.8F,-70,-4.5F,11.6F,8,9F);
        box(root,5,-4.85F,-62,-3.7F,9.7F,10,7.4F);
        box(root,4,-5.7F,-77,-6F,11.4F,4,2.5F);
        box(root,1,-2,-87,-2,4,5,4);
        box(root,13,-5,-61,-4.1F,10,1,1F);
        // A sculpted split face: left side exposed bone, right side flesh.
        head.setPos(0,-87,0);
        for(int row=0;row<7;row++) {
            float width=new float[]{2.9F,3.9F,4.6F,4.9F,4.7F,4F,3.2F}[row];
            box(head,2,-width,-2-row*2.25F,-3.7F,width,2.35F,7.4F);
            box(head,0,0,-2-row*2.25F,-3.7F,width,2.35F,7.4F);
        }
        box(head,11,-3.8F,-10.5F,-4.1F,3.3F,2.5F,.55F);
        box(head,1,-3F,-9.8F,-4.7F,1.6F,1.0F,.36F);
        box(head,10,1.1F,-10.2F,-4.5F,2.65F,1.5F,.25F);
        box(head,1,1.5F,-9.9F,-4.75F,1.3F,1.0F,.35F);
        for(int j=0;j<4;j++) box(head,1,-3.6F+j*.8F,-3.5F,-4.2F,.57F,1.6F,.6F);
        // Hair deliberately covers the entire fleshed half and reaches below waist; not the skull half.
        for(int j=0;j<8;j++) {
            float x=.05F+j*.64F,tail=37+j%3*3;
            box(head,j%3==0?7:6,x,-18F,2.9F, .65F,18+tail,2.3F);
        }
        for(int j=0;j<6;j++) {
            float x=3.5F+j*.3F;
            box(head,j%2==0?6:12,x,-17F,-2.8F,.65F,51-j*3,2F);
        }
        for(int j=0;j<5;j++) box(head,6,j*.93F,-19F,-4.0F,.95F,4+j%2,1.5F);
        geo.attach(root,head);
        // Arms are independent, re-aimed every frame from shoulders to elbows to wrists.
        for(ModelPart upper:new ModelPart[]{leftUpper,rightUpper})box(upper,0,-2.2F,0,-2.1F,4.4F,10,4.2F);
        box(leftUpper,2,-1.7F,0,-1.6F,3.4F,10,3.2F);
        box(leftLower,2,-1F,0,-.9F,2F,10,1.8F);
        box(rightLower,1,-2.05F,0,-1.9F,4.1F,10,3.8F);
        box(leftHand,2,-1.55F,-1.5F,-1.45F,3.1F,3F,2.9F);
        box(rightHand,1,-1.9F,-1.65F,-1.8F,3.8F,3.3F,3.6F);
        hammer(smallL,false);hammer(smallR,false);
        geo.attach(leftHand,smallL);geo.attach(rightHand,smallR);
        for(ModelPart limb:new ModelPart[]{leftUpper,leftLower,leftHand,rightUpper,rightLower,rightHand})geo.attach(root,limb);
        hammer(giant,true);
        giant.setPos(0,-58,-11);
        geo.attach(root,giant);
    }
    private static float[] interpolate(float[] start,float[] end,float t) {
        return new float[]{Mth.lerp(t,start[0],end[0]),Mth.lerp(t,start[1],end[1]),Mth.lerp(t,start[2],end[2])};
    }
    private static void aimSegment(ModelPart part,float[] a,float[] b) {
        float dx=b[0]-a[0],dy=a[1]-b[1],dz=b[2]-a[2];
        float length=Mth.sqrt(dx*dx+dy*dy+dz*dz);
        part.setPos(a[0],-a[1],a[2]);
        part.xRot=(float)Math.asin(Mth.clamp(dz/length,-1,1));
        part.zRot=-(float)Math.atan2(dx,dy);
        part.yRot=0;
        part.yScale=length/10F;
    }
    private void arm(boolean left,float[] elbow,float[] wrist) {
        ModelPart upper=left?leftUpper:rightUpper,lower=left?leftLower:rightLower,hand=left?leftHand:rightHand;
        aimSegment(upper,left?SHOULDER_L:SHOULDER_R,elbow);
        aimSegment(lower,elbow,wrist);
        hand.setPos(wrist[0],-wrist[1],wrist[2]);
        hand.xRot=0;hand.yRot=0;hand.zRot=0;
    }
    private static float[] giantGrip(float offset,float angle) {
        // Giant pivot is at world Y=58, Z=-11. Both hands follow the same rigid bone shaft.
        return new float[]{0,58+offset*(float)Math.cos(angle),-11-offset*(float)Math.sin(angle)};
    }
    @Override public void setupAnim(LomeyaBoss boss,float walk,float stride,float age,float yaw,float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.setPos(0,24,0);
        float step=Mth.cos(walk*.67F)*Mth.clamp(stride,0,1)*.50F;
        leftLeg.xRot=step;rightLeg.xRot=-step;
        head.yRot=yaw*Mth.DEG_TO_RAD*.18F;
        head.xRot=pitch*Mth.DEG_TO_RAD*.18F;
        float f=boss.actionFrame();
        float[] eL={-10,60,-5},wL={-12,59,-17};
        float[] eR={10,60,-5},wR={12,59,-17};
        boolean second=boss.phaseTwo();
        smallL.visible=!second;smallR.visible=!second;giant.visible=second;
        if(!second) {
            int action=boss.action();
            if(action==LomeyaBoss.COMBO) {
                boolean useLeft=f<24;
                float time=useLeft?f/24F:(f-24F)/22F;
                float t=time<.4F?time/.4F:1F-(time-.4F)/.6F;
                t=Mth.clamp(t,0,1);
                if(useLeft) {eL=interpolate(eL,new float[]{-12,73,-6},t);wL=interpolate(wL,new float[]{-13,68,-26},t);}
                else {eR=interpolate(eR,new float[]{12,73,-6},t);wR=interpolate(wR,new float[]{13,68,-26},t);}
            }
            if(action==LomeyaBoss.GRAB) {
                float t=Mth.sin(Mth.clamp(f/38F,0,1)*Mth.PI);
                eL=interpolate(eL,new float[]{-10,70,-9},t);
                wL=interpolate(wL,new float[]{-5,68,-24},t);
                smallL.visible=false;
            }
            if(action==LomeyaBoss.SLAM) {
                float t=Mth.sin(Mth.clamp(f/46F,0,1)*Mth.PI);
                eL=interpolate(eL,new float[]{-12,69,-8},t);wL=interpolate(wL,new float[]{-12,67,-25},t);
                eR=interpolate(eR,new float[]{12,69,-8},t);wR=interpolate(wR,new float[]{12,67,-25},t);
            }
        } else {
            float angle=0;
            if(boss.action()==LomeyaBoss.GIANT_WINDUP) angle=-.32F*Mth.sin(Mth.clamp(f/40F,0,1)*Mth.PI);
            if(boss.action()==LomeyaBoss.GIANT_STRIKE) {
                if(f<15) angle=-.25F*f/15F;
                else if(f<36) angle=Mth.lerp((f-15)/21F,-.25F,3.02F);
                else angle=3.02F*(1-Mth.clamp((f-36)/22F,0,1));
            }
            giant.xRot=angle;
            wL=giantGrip(-2,angle);wR=giantGrip(14,angle);
            eL=interpolate(SHOULDER_L,wL,.52F);eL[0]-=5;
            eR=interpolate(SHOULDER_R,wR,.52F);eR[0]+=5;
        }
        if(!second) {wL[1]=eL[1];wR[1]=eR[1];}
        arm(true,eL,wL);arm(false,eR,wR);
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer out,int light,int overlay,int color) {
        root.render(stack,out,light,overlay,color);
    }
}
