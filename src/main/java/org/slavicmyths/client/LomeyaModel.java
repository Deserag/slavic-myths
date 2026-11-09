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
        head.setPos(0,-87,0);
        float[] jaw={2.55F,3.35F,4.25F,4.65F,4.60F,4.20F,3.72F};
        float[] front={-3.52F,-3.65F,-3.72F,-3.66F,-3.54F,-3.35F,-3.14F};
        for(int row=0;row<jaw.length;row++) {
            float width=jaw[row],y=-1.8F-row*2.22F;
            box(head,2,-width,y,front[row],width,2.29F,7.0F-(row>4?(row-4)*.45F:0F));
            box(head,0,0,y,front[row],width,2.29F,7.0F-(row>4?(row-4)*.45F:0F));
        }
        float[] crownWidth={3.30F,2.47F,1.40F};
        for(int row=0;row<crownWidth.length;row++) {
            float w=crownWidth[row],y=-17.3F-row*1.15F;
            box(head,2,-w,y,-2.70F+row*.37F,w,1.35F,5.88F-row*.65F);
            box(head,0,0,y,-2.70F+row*.37F,w,1.35F,5.88F-row*.65F);
        }
        for(int side:new int[]{-1,1}) {
            float x=side*3.08F;
            box(head,side<0?2:0,x-.60F,-9.8F,-3.91F,1.20F,4.40F,1.36F);
            box(head,side<0?2:0,x-.42F,-14.4F,-3.66F,.84F,3.25F,1.24F);
        }
        box(head,11,-3.20F,-10.5F,-4.13F,2.65F,2.25F,.55F);
        box(head,1,-2.92F,-9.8F,-4.70F,1.56F,1.0F,.36F);
        box(head,10,1.06F,-10.2F,-4.53F,2.58F,1.5F,.25F);
        box(head,1,1.46F,-9.9F,-4.81F,1.30F,1.0F,.35F);
        for(int j=0;j<4;j++)box(head,1,-3.27F+j*.75F,-3.5F,-4.10F,.53F,1.53F,.59F);
        for(int layer=0;layer<4;layer++) {
            float y=-18.9F-layer*1.18F;
            float half=4.55F-layer*.78F;
            for(int strand=-5;strand<=5;strand++) {
                float x=strand*half/5F,z=-2.95F+.28F*Mth.sin(strand*.9F+layer);
                box(head,strand%3==0?7:6,x-.55F,y-.12F*Mth.cos(strand*.76F),z,1.12F,1.56F,5.92F-layer*.45F);
            }
        }
        for(int lane=-5;lane<=5;lane++) {
            float x=lane*.78F;
            for(int section=0;section<5;section++) {
                float z=-3.08F+section*1.35F;
                float crownY=-21.6F-1.80F*Mth.sin((section+.5F)*Mth.PI/5F)+.12F*Math.abs(lane);
                box(head,(lane+section)%4==0?7:6,x-.48F,crownY,z,.96F,1.48F,1.55F);
            }
        }
        for(int side:new int[]{-1,1})for(int lock=0;lock<5;lock++) {
            float anchor=side*(2.70F+lock*.36F);
            int length=lock%2==0?49:40;
            for(int j=0;j<length;j+=3) {
                float t=j/(float)length;
                float x=anchor+side*(.65F*Mth.sin(t*4.4F+lock*.55F)+t*.54F);
                float z=lock<2?-3.0F+lock*.72F:2.45F+(lock-2)*.67F;
                box(head,lock%3==0?7:6,x-.48F,-18.25F+j,z-.55F,1.02F,3.35F,1.20F);
            }
        }
        for(int j=-3;j<=3;j++) {
            float x=j*.91F;
            box(head,j%3==0?7:6,x-.50F,-20.45F+.20F*Math.abs(j),-3.65F,1.06F,3.35F,.95F);
        }
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
        root.getAllParts().forEach(part -> part.setInitialPose(part.storePose()));
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
                int swing=f<17?0:f<25?1:f<33?2:3;
                boolean useLeft=(swing&1)==0;
                float start=swing==0?0:swing==1?17:swing==2?25:33;
                float length=swing==0?17:swing==3?13:8;
                float t=Mth.sin(Mth.clamp((f-start)/length,0F,1F)*Mth.PI);
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
        arm(true,eL,wL);arm(false,eR,wR);
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer out,int light,int overlay,int color) {
        root.render(stack,out,light,overlay,color);
    }
}
