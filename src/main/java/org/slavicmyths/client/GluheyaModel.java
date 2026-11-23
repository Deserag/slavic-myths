package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.GluheyaBoss;

/**
 * 60-voxel (3.75-block) articulated model.
 * Full-perimeter crown, continuous side and rear hair, short twin daggers.
 * Phase 1 dress/bell and phase 2 assassin armor use the same head and face.
 */
public final class GluheyaModel extends EntityModel<GluheyaBoss> {
    private final FolkModelGeometry geo=new FolkModelGeometry();
    private final ModelPart root=geo.part(512,512),head=geo.part(512,512);
    private final ModelPart robe=geo.part(512,512),assassin=geo.part(512,512);
    private final ModelPart leftLeg=geo.part(512,512),rightLeg=geo.part(512,512);
    private final ModelPart leftArm=geo.part(512,512),rightArm=geo.part(512,512);
    private final ModelPart leftForearm=geo.part(512,512),rightForearm=geo.part(512,512);
    private final ModelPart leftKnife=geo.part(512,512),rightKnife=geo.part(512,512);
    private final ModelPart bell=geo.part(512,512),longSword=geo.part(512,512);

    private void b(ModelPart part,int mat,float x,float y,float z,float w,float h,float d){
        geo.box(part,(mat%4)*128+3,(mat/4)*128+3,x,-y-h,z,w,h,d);
    }
    private void band(ModelPart p,int mat,float x,float y,float z,float w,float h,float d){b(p,mat,x,y,z,w,h,d);}
    public GluheyaModel(){
        root.setPos(0,24,0);
        // Torso: shoulders taper to a visible waist instead of one rectangular pillar.
        b(root,0,-5.6F,34,-2.8F,11.2F,9,5.6F);
        b(root,1,-4.2F,30,-2.65F,8.4F,5.3F,5.3F);
        b(root,0,-3.2F,26,-2.25F,6.4F,5.2F,4.5F);
        b(root,11,-5.3F,25,-2.7F,10.6F,2.1F,5.4F);
        b(root,7,-1.6F,26.1F,-3.0F,3.2F,1.1F,.6F);
        // A single curved bust panel, angular layered clothing rather than split cubes.
        b(root,1,-4.7F,36.7F,-3.35F,9.4F,4.4F,.9F);
        b(root,2,-3.0F,31.0F,-3.0F,6.0F,4.6F,.7F);
        b(root,12,-.45F,38.7F,-3.65F,.9F,1.9F,.4F);
        // Neck remains short and visually hidden below the jaw.
        b(root,3,-1.5F,42.8F,-1.45F,3.0F,2.1F,2.8F);

        robe.setPos(0,0,0);
        // Phase 1: tapered tiered dress, broad lower hem and separate front panels.
        for(int j=0;j<9;j++){
            float y=j*3.0F,half=12.0F-j*.72F;
            b(robe,j%3==0?1:0,-half,y,-4.4F-j*.03F,half*2,3.1F,9.0F+j*.15F);
            b(robe,2,-1.6F,y,-4.95F-j*.03F,3.2F,3.0F,.55F);
            if(j%2==0)b(robe,7,-half-.5F,y,0,1.0F,3F,.65F);
        }
        geo.attach(root,robe);

        assassin.setPos(0,0,0);
        // Phase 2: combat tunic with uncovered legs, plates and fitted corset.
        b(assassin,10,-5.5F,20,-3.0F,11.0F,7F,6F);
        b(assassin,2,-4.2F,24,-3.4F,8.4F,12F,.8F);
        for(int s:new int[]{-1,1}){
            b(assassin,11,s*5.1F-1.3F,20,-3.55F,2.6F,9.0F,6.3F);
            b(assassin,7,s*3.8F-.6F,25,-3.9F,1.2F,4.0F,.6F);
        }
        geo.attach(root,assassin);

        // Independent legs pivot at the hips. Lower legs flex gently while running.
        for(int s:new int[]{-1,1}){
            ModelPart leg=s<0?leftLeg:rightLeg;
            leg.setPos(s*3.15F,-25,0);
            b(leg,2,-1.6F,-22,-2.0F,3.2F,22.0F,4.0F);
            b(leg,11,-1.9F,-25,-2.2F,3.8F,11.0F,4.4F);
            b(leg,15,-2.0F,-25,-4.4F,4.0F,4.0F,6.8F);
            b(leg,12,-1.5F,-23,-4.5F,3.0F,.7F,.5F);
            geo.attach(root,leg);
        }
        // Arms, real elbow pivots, narrow shoulders and hanging sleeves.
        for(int s:new int[]{-1,1}){
            ModelPart arm=s<0?leftArm:rightArm;
            ModelPart fore=s<0?leftForearm:rightForearm;
            arm.setPos(s*7F,-41,0);
            b(arm,1,-2.0F,-9,-2.4F,4.0F,10.0F,5.0F);
            b(arm,7,-2.15F,-1.5F,-2.7F,4.3F,1.3F,5.5F);
            fore.setPos(0,9.0F,0);
            b(fore,0,-1.8F,-8,-2.2F,3.6F,8.1F,4.4F);
            b(fore,3,-1.4F,-11.0F,-2.05F,2.8F,3.2F,3.5F);
            geo.attach(arm,fore);
            geo.attach(root,arm);
        }

        // Short STRAIGHT phase-2 daggers, about 10 model voxels with small hilts.
        for(ModelPart knife:new ModelPart[]{leftKnife,rightKnife}){
            knife.setPos(0,11.0F,-2.1F);
            b(knife,15,-.55F,-2.0F,-.4F,1.1F,3.0F,.8F);
            b(knife,9,-2.0F,-3.0F,-.6F,4.0F,.8F,1.2F);
            b(knife,8,-.75F,-9.6F,-.65F,1.5F,6.5F,1.3F);
            b(knife,14,-.4F,-10.6F,-.35F,.8F,1.0F,.7F);
        }
        geo.attach(leftForearm,leftKnife);
        geo.attach(rightForearm,rightKnife);

        bell.setPos(0,11.5F,-2.0F);
        for(int i=0;i<5;i++)b(bell,9,-.6F,-4.2F-i*1.4F,-.6F,1.2F,1.3F,1.2F);
        for(int i=0;i<5;i++){
            float w=3.0F+i*.7F;
            b(bell,i%2==0?9:7,-w/2,-16.4F+i*1.7F,-w/2,w,1.75F,w);
        }
        b(bell,14,-.8F,-17.5F,-.8F,1.6F,1.8F,1.6F);
        geo.attach(rightForearm,bell);

        // The large execution sword materializes only during the pull finisher.
        longSword.setPos(0,11,-2.0F);
        b(longSword,15,-.7F,-3.0F,-.7F,1.4F,4.0F,1.4F);
        b(longSword,9,-4.0F,-4.5F,-1.0F,8.0F,1.5F,2.0F);
        b(longSword,8,-1.4F,-24,-.7F,2.8F,19.5F,1.4F);
        b(longSword,14,-.65F,-26,-.65F,1.3F,2.0F,1.3F);
        geo.attach(rightForearm,longSword);

        // The head is fixed at the NECK, not floating on a long hinge.
        head.setPos(0,-45,0);
        b(head,3,-3.25F,0,-3.25F,6.5F,3.0F,6.5F);
        b(head,3,-4.4F,2.3F,-3.6F,8.8F,6.8F,7.2F);
        b(head,3,-3.7F,8.2F,-3.35F,7.4F,1.2F,6.7F);
        for(int s:new int[]{-1,1}){
            float x=s*2.05F;
            b(head,15,x-.75F,5.9F,-3.93F,1.5F,1.0F,.35F);
            b(head,6,x-.48F,6.0F,-4.02F,.96F,.72F,.35F);
            b(head,5,x-.5F,7.0F,-3.9F,1.0F,.26F,.34F);
        }
        // Sewn mouth, readable on the front of the face.
        b(head,13,-2.0F,2.55F,-3.93F,4.0F,.33F,.4F);
        for(int i=0;i<6;i++)b(head,13,-1.75F+i*.68F,2.18F,-4.0F,.2F,1.2F,.42F);

        // Continuous hair cap + BOTH temples + rear volume, no bald side patches.
        b(head,4,-5.1F,9.0F,-4.2F,10.2F,1.4F,8.5F);
        for(int s:new int[]{-1,1}){
            b(head,4,s*4.45F-.75F,-2.0F,-3.65F,1.5F,11.6F,8.0F);
            for(int l=0;l<4;l++){
                float x=s*(5.15F+l*.48F);
                b(head,l%2==0?5:4,x-.67F,-8.5F-l*2.0F,-3.2F+l*.82F,1.34F,16.0F-l*.35F,1.55F);
            }
            b(head,5,s*3.7F-.85F,6.2F,-4.1F,1.7F,3.8F,1.0F);
        }
        for(int j=0;j<10;j++){
            float x=-5.2F+j*1.16F;
            b(head,j%2==0?4:5,x-.62F,-19.0F+(j%3)*1.9F,3.55F+(j%3)*.24F,
                1.24F,27.0F-(j%3)*1.2F,1.2F);
        }

        // Full icy crown around front, back and both sides of the skull.
        for(float z:new float[]{-4.55F,4.3F})b(head,7,-5.4F,9.9F,z,10.8F,1.4F,.95F);
        for(float x:new float[]{-5.45F,4.48F})b(head,7,x,9.9F,-4.5F,.97F,1.4F,8.8F);
        for(float z:new float[]{-4.37F,4.16F}){
            for(int k=-2;k<=2;k++){
                float spike=(k==0?4.5F:k==1||k==-1?3.1F:2.2F);
                b(head,12,k*2.1F-.48F,11.2F,z,.96F,spike,.95F);
                b(head,14,k*2.1F-.27F,11.2F+spike,z+.15F,.55F,.7F,.55F);
            }
        }
        for(float x:new float[]{-5.2F,4.4F})for(int k=-1;k<=1;k++)
            b(head,12,x,11.2F,k*2.7F-.48F,.85F,k==0?3.0F:2.1F,.96F);
        b(head,14,-.85F,9.7F,-5.08F,1.7F,1.9F,.75F);
        geo.attach(root,head);
        // Preserve each joint's authored pivot when setupAnim resets its pose.
        root.getAllParts().forEach(part -> part.setInitialPose(part.storePose()));
    }

    @Override public void setupAnim(GluheyaBoss boss,float walk,float stride,float age,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean phase2=boss.secondPhase();
        robe.visible=!phase2;
        assassin.visible=phase2;
        bell.visible=!phase2;
        leftKnife.visible=phase2;
        rightKnife.visible=phase2;
        longSword.visible=phase2&&boss.action()==GluheyaBoss.PULL&&boss.actionFrame()>=24;
        if(longSword.visible)rightKnife.visible=false;
        float gait=Mth.clamp(stride,0,1);
        float phase=walk*.67F;
        float swing=Mth.sin(phase)*gait;
        float speed=phase2?.82F:.38F;
        leftLeg.xRot=swing*speed;
        rightLeg.xRot=-swing*speed;
        leftArm.xRot=-swing*(phase2?.48F:.28F);
        rightArm.xRot=swing*(phase2?.48F:.28F);
        leftArm.zRot=-.055F*Mth.sin(phase)*gait;
        rightArm.zRot=.055F*Mth.sin(phase)*gait;
        leftForearm.xRot=phase2?-.16F:-.08F;
        rightForearm.xRot=phase2?-.16F:-.08F;
        robe.zRot=Mth.sin(phase)*gait*.025F;
        root.y=24F-Mth.abs(Mth.sin(phase))*(phase2?.6F:.2F)*gait;
        root.zRot=.022F*Mth.sin(phase)*gait;
        root.xRot=phase2?.09F:-.015F;
        head.yRot=yaw*Mth.DEG_TO_RAD*.13F;
        head.xRot=pitch*Mth.DEG_TO_RAD*.13F;
        float f=boss.actionFrame(),attack=Mth.clamp(f/12F,0,1);
        switch(boss.action()){
            case GluheyaBoss.FAST_WAVE,GluheyaBoss.LONG_BELL,GluheyaBoss.SILENCE_FIELD,
                 GluheyaBoss.SILENCE_RING,GluheyaBoss.BELL_STORM->{
                rightArm.xRot=-1.15F*attack;
                rightForearm.xRot=-.55F*attack;
                bell.zRot=.22F*Mth.sin(f*.29F);
                root.xRot=-.025F*attack;
            }
            case GluheyaBoss.COMBO->{
                float cut=Mth.sin(f*.38F);
                leftArm.xRot=-.8F*Math.max(0,cut);
                rightArm.xRot=-.8F*Math.max(0,-cut);
                leftForearm.xRot=-.75F*Math.max(0,cut);
                rightForearm.xRot=-.75F*Math.max(0,-cut);
                root.yRot=.22F*cut;
            }
            case GluheyaBoss.DAGGER_THROW,GluheyaBoss.HUNT->{
                rightArm.xRot=-1.1F*attack;leftArm.xRot=-.75F*attack;
                rightForearm.xRot=-.4F;leftForearm.xRot=-.7F;
            }
            case GluheyaBoss.BACKSTAB->{
                rightArm.xRot=-1.5F*attack;rightForearm.xRot=-.4F*attack;root.yRot=.25F*attack;
            }
            case GluheyaBoss.PULL->{
                rightArm.xRot=-1.7F*attack;
                rightForearm.xRot=-.85F*attack;
                longSword.zRot=-.85F*Mth.clamp((f-27)/11F,0,1);
            }
            case GluheyaBoss.SHIFT->{
                root.yRot=.12F*Mth.sin(f*.2F);
                leftArm.xRot=-.6F;rightArm.xRot=-.6F;
            }
            default->{}
        }
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer out,int light,int overlay,int color){
        root.render(stack,out,light,overlay,color);
    }
}
