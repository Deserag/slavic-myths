package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.ViyBoss;

/** Actual articulated 64-unit = four-block cuboid model, based on approved procedural 3D prototype. */
public final class ViyModel extends EntityModel<ViyBoss> {
    private final FolkModelGeometry geo=new FolkModelGeometry();
    private final ModelPart root=geo.part(512,512),head=geo.part(512,512);
    private final ModelPart armL=geo.part(512,512),armR=geo.part(512,512);
    private final ModelPart legL=geo.part(512,512),legR=geo.part(512,512);
    private final ModelPart lidL=geo.part(512,512),lidR=geo.part(512,512);
    private void b(ModelPart p,int material,float x,float y,float z,float w,float h,float d){
        geo.box(p,(material%4)*128+4,(material/4)*128+4,x,-y-h,z,w,h,d);
    }
    private void path(ModelPart p,int material,float x,float y,float z,float dx,float dy,float dz,float size){
        int count=Math.max(2,(int)(Math.sqrt(dx*dx+dy*dy+dz*dz)/1.8));
        for(int i=0;i<=count;i++){
            float t=i/(float)count;
            b(p,material,x+dx*t-size/2,y+dy*t-size/2,z+dz*t-size/2,size,size,size);
        }
    }
    public ViyModel(){
        root.setPos(0,24,0);
        // Wide barrel torso: rings and internal fill instead of a single block.
        for(int row=0;row<12;row++){
            float y=15+row*2.75F,t=row/11F;
            float rx=18.5F+3.2F*Mth.sin(Mth.PI*t)-2.8F*t*t*t;
            float rz=11.4F+2.4F*Mth.sin(Mth.PI*(t+.12F));
            for(int k=0;k<22;k++){
                float angle=k*Mth.TWO_PI/22F;
                float warp=1+.027F*Mth.sin(6*angle+row*.9F);
                float x=rx*Mth.sin(angle)*warp,z=rz*Mth.cos(angle)-.5F-t;
                int mat=(row+k)%9==0?3:(row+k)%6<3?0:1;
                b(root,mat,x-1.55F,y,z-1.6F,3.1F,3.2F,3.2F);
            }
            b(root,1,-rx*.80F,y,-rz*.74F-1,rx*1.60F,3.3F,rz*1.48F);
        }
        // Huge raised hunch, fused earth, clods and roots.
        for(int row=0;row<6;row++){
            float v=-.8F+row*.32F,s=(float)Math.sqrt(Math.max(.02F,1-v*v));
            for(int k=0;k<16;k++){
                float a=k*Mth.TWO_PI/16F;
                b(root,k%5==0?4:1,18*s*Mth.sin(a)-1.7F,44+13*v,8+10*s*Mth.cos(a)-1.7F,3.4F,4.3F,3.4F);
            }
        }
        for(int side:new int[]{-1,1}){
            for(int j=0;j<4;j++){
                float x=side*(3.5F+j*4F);
                path(root,j%2==0?7:8,x,58+j%2,6,x*.18F,-9,7.2F,1.65F);
                path(root,7,x+side*1.8F,49+j%2,13,x*.15F,-6,-2.6F,1.3F);
            }
            for(int j=0;j<4;j++){
                float x=side*(8+j*.7F);
                b(root,j%2==0?4:3,x-1,34-j*3.1F,-12.8F+j*.5F,2.0F,1.6F,1.3F);
            }
        }
        // Short bowed legs with separate hip pivots for weighty walk.
        for(int side:new int[]{-1,1}){
            ModelPart leg=side<0?legL:legR;
            leg.setPos(side*9F,-17,0);
            for(int j=0;j<6;j++){
                float y=-16.8F+j*3.3F,rad=6.55F-j*.24F;
                b(leg,j%2==0?0:4,-rad,y,-5.25F,rad*2F,3.75F,10.6F);
            }
            b(leg,1,-6.6F,-18F,-9.3F,13.2F,6.0F,16.7F);
            for(int j=0;j<3;j++){
                b(leg,5,-5.7F+j*3.8F,-19,-13.0F,3.25F,3.4F,7.4F);
                b(leg,6,-5.5F+j*3.8F,-18,-13.7F,2.9F,1.5F,2.1F);
            }
            geo.attach(root,leg);
        }
        // Very long iron-like fingers, each attached to its own animated arm parent.
        for(int side:new int[]{-1,1}){
            ModelPart arm=side<0?armL:armR;
            arm.setPos(side*23,-45,0);
            for(int j=0;j<7;j++){
                float y=-4.5F-j*3.6F,r=6.5F-j*.36F+.45F*Mth.sin(j);
                b(arm,j%3==0?4:0,-r,y,-5.3F,r*2F,4.5F,11.1F);
                for(int k=0;k<4;k++)
                    b(arm,3,-4.3F+k*2.35F,y+.6F,-5.9F,1.7F,2.7F,.95F);
            }
            b(arm,5,-5.9F,-30,-6.5F,11.8F,6.6F,12.5F);
            b(arm,4,-5.7F,-32.5F,-6.8F,11.4F,4.6F,13.0F);
            for(int j=0;j<4;j++){
                float x=-5.5F+j*3.0F;
                b(arm,5,x,-40.0F,-6.3F,2.5F,9.5F,4.4F);
                b(arm,6,x+.15F,-40.7F,-6.5F,2.1F,2.0F,4.3F);
                b(arm,6,x+.45F,-32.2F,-6.7F,1.8F,4.2F,1.2F);
            }
            geo.attach(root,arm);
        }
        // Low broad skull and iron-like face, actual facial geometry in a movable head parent.
        head.setPos(0,-39,0);
        float[] radii={9.2F,10.6F,11.6F,12.3F,13.0F,12.7F,11.8F,9.3F};
        for(int j=0;j<8;j++){
            float y=.2F+j*2.35F,rx=radii[j],rz=9.3F+(j%4)*.45F;
            for(int k=0;k<20;k++){
                float a=k*Mth.TWO_PI/20F;
                b(head,(k+j)%6==1?9:j%3==0?3:0,
                    rx*Mth.sin(a)-1.5F,y,rz*Mth.cos(a)-5.1F,3.0F,2.75F,2.9F);
            }
            b(head,1,-rx*.76F,y,-rz*.72F-3.6F,rx*1.52F,2.6F,rz*1.44F);
        }
        for(int side:new int[]{-1,1}){
            b(head,9,side*8.2F-3.2F,2,-14.2F,6.4F,7.2F,2.65F);
            b(head,3,side*7.5F-3.2F,9.1F,-13.35F,6.2F,4.2F,1.6F);
            b(head,12,side*6.5F-2.55F,10F,-15.8F,5.1F,4.8F,1.45F);
            b(head,14,side*6.5F-1.20F,11F,-16.9F,2.40F,2.2F,1.0F);
            b(head,15,side*6.5F-.50F,11.4F,-17.2F,1.0F,1.25F,.44F);
        }
        b(head,12,-7.7F,0,-14.6F,15.4F,5.9F,2.05F);
        b(head,9,-7,-1.1F,-15.7F,14,1.6F,2.25F);
        for(int i=0;i<5;i++)b(head,13,-6.4F+i*2.65F,2.2F,-16.3F,1.65F,2.6F,1.2F);
        b(head,9,-2.25F,4.2F,-15.55F,4.5F,11.6F,3.4F);
        b(head,3,-1.6F,9.0F,-17.3F,3.2F,3.6F,2.9F);
        // Two independent *block-built* curtain eyelids, closed almost down to ground level.
        for(int side:new int[]{-1,1}){
            ModelPart lid=side<0?lidL:lidR;
            lid.setPos(side*6.75F,-15F,-16.5F);
            for(int layer=0;layer<9;layer++){
                float width=9.6F-3.2F*layer/8F,y=-5.45F*layer;
                int mat=layer%3==0?11:layer%3==2?10:2;
                b(lid,mat,-width/2,y,-.25F,width,5.85F,3.6F);
                b(lid,10,-width/2+.55F,y+.42F,-1.05F,width-1.1F,1.05F,.95F);
                for(int q=0;q<3;q++){
                    float x=-width/2+.6F+q*(width-1.2F)/3F;
                    b(lid,11,x,y+.48F,-1.25F,.48F,1.65F,.72F);
                }
            }
            b(head,10,side*6.8F-5.9F,16F,-14.9F,11.8F,3.25F,3.0F);
            geo.attach(head,lid);
        }
        geo.attach(root,head);
        // Only ragged black waist-cloth, never a cape or armor.
        for(int j=0;j<9;j++){
            float x=-14+j*3.5F,h=9+(j*7%7);
            b(root,j%2==0?10:12,x,14,-12.9F,2.8F,h,1.45F);
            b(root,10,x+.45F,14+h-1.5F,-13F,1F,2.8F,1.6F);
        }
        for(int j=0;j<7;j++)b(root,10,-12.5F+j*3.8F,13,11.3F,3F,11+(j%4)*1.2F,1.1F);
        root.getAllParts().forEach(part->part.setInitialPose(part.storePose()));
    }
    @Override public void setupAnim(ViyBoss boss,float walk,float stride,float age,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);
        root.setPos(0,24,0);
        float step=Mth.cos(walk*.62F)*Mth.clamp(stride,0,1)*.36F;
        legL.xRot=step;legR.xRot=-step;
        armL.xRot=-step*.60F;armR.xRot=step*.60F;
        root.y-=Mth.abs(Mth.sin(walk*.62F))*stride*.6F;
        head.yRot=Mth.clamp(yaw,-35,35)*Mth.DEG_TO_RAD*.22F;
        float openness=boss.eyesOpen()?1F:0F;
        if(boss.action()==ViyBoss.OPEN_EYES)openness=Mth.clamp(boss.actionFrame()/22F,0,1);
        lidL.yScale=1-.87F*openness;lidR.yScale=1-.87F*openness;
        lidL.y=-15-6F*openness;lidR.y=-15-6F*openness;
        float f=boss.actionFrame();
        float wind=Mth.sin(Mth.clamp(f/24F,0,1)*Mth.PI);
        switch(boss.action()){
            case ViyBoss.FIST->{armR.xRot=-1.35F*wind;root.zRot=-.055F*wind;}
            case ViyBoss.STOMP->{legR.xRot=-.65F*Mth.sin(Mth.clamp(f/40F,0,1)*Mth.PI);}
            case ViyBoss.RIFT->{armL.xRot=-1.0F*wind;armR.xRot=-1F*wind;}
            case ViyBoss.IRON_HAND->{armL.xRot=-1.23F*Mth.clamp(f/24F,0,1);armL.zRot=-.18F;}
            case ViyBoss.SUMMON->{armR.xRot=-.85F*Mth.clamp(f/32F,0,1);}
            case ViyBoss.FINGER->{armR.xRot=-1.15F*Mth.clamp(f/27F,0,1);}
            case ViyBoss.EYE_WAVE->{head.xRot=-.16F;armL.xRot=-.45F;armR.xRot=-.45F;}
            case ViyBoss.VERDICT->{armL.xRot=-.7F;armR.xRot=-.7F;root.y-=.35F*Mth.sin(f*.14F);}
            default->{}
        }
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer consumer,int light,int overlay,int color){
        root.render(stack,consumer,light,overlay,color);
    }
}
