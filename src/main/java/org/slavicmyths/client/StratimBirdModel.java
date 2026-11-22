package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.marine.StratimBirdEntity;

/** Procedural articulated voxel seabird: volumetric chest, beak, feathered wings and talons. */
public final class StratimBirdModel extends EntityModel<StratimBirdEntity> {
    private static final int NAVY=0xFF1D3543,DARK=0xFF284956,SLATE=0xFF395D6D,
        BLUE=0xFF557D8A,PALE=0xFFBAC9C2,WHITE=0xFFDDE6DA,
        FEATHER=0xFF678D9A,EDGE=0xFF91AFB5,BEAK=0xFFD5B16D,
        TIP=0xFF473B36,BLACK=0xFF131D23,IRIS=0xFFDEBE69,
        CLAW=0xFF292B30,CREST=0xFF7DB7B5;
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final Group root,body,neck,head,tail,leftWing,rightWing,leftHand,rightHand;
    private final Group[] outer=new Group[2],feet=new Group[2];
    private final class Group {
        final ModelPart pivot=geometry.part(512,512);
        final Map<Integer,ModelPart> colored=new LinkedHashMap<>();
        final List<Group> children=new ArrayList<>();
        final float x,y,z;
        Group(Group parent,float x,float y,float z){
            this.x=x;this.y=y;this.z=z;
            if(parent==null)pivot.setPos(x,y,z);
            else{pivot.setPos(x-parent.x,y-parent.y,z-parent.z);parent.children.add(this);}
        }
        void cube(int color,float x,float y,float z,float w,float h,float d){
            ModelPart mesh=colored.computeIfAbsent(color,c->geometry.part(512,512));
            geometry.box(mesh,x-this.x,y-this.y,z-this.z,w,h,d);
        }
        void draw(PoseStack stack,VertexConsumer out,int light,int overlay){
            stack.pushPose();pivot.translateAndRotate(stack);
            for(var c:colored.entrySet())c.getValue().render(stack,out,light,overlay,c.getKey());
            for(Group child:children)child.draw(stack,out,light,overlay);
            stack.popPose();
        }
    }
    public StratimBirdModel(){
        root=new Group(null,0,0,0);
        body=new Group(root,0,-71,0);
        neck=new Group(body,0,-94,-38);
        head=new Group(neck,0,-113,-51);
        tail=new Group(body,0,-65,48);
        leftWing=new Group(body,-27,-81,-5);
        rightWing=new Group(body,27,-81,-5);
        outer[0]=new Group(leftWing,-112,-76,-7);
        outer[1]=new Group(rightWing,112,-76,-7);
        leftHand=new Group(outer[0],-181,-71,-17);
        rightHand=new Group(outer[1],181,-71,-17);
        feet[0]=new Group(body,-12,-27,17);
        feet[1]=new Group(body,12,-27,17);
        buildBody();buildHead();buildWings();buildTail();buildFeet();
    }
    private void buildBody(){
        // Non-monolithic overlapping oval cross-sections: breast to tapered rump.
        for(float z=-48;z<=58;z+=5.2F){
            float t=(z+48)/106F;
            float rx=24F*(.76F+.24F*Mth.sin(Mth.PI*t));
            float ry=31F*(.73F+.27F*Mth.sin(Mth.PI*t));
            float cy=-69F+8F*t;
            for(float x=-rx;x<=rx;x+=5.2F)
                for(float y=-ry;y<=ry;y+=5.1F){
                    if(x*x/(rx*rx)+y*y/(ry*ry)>1.03F)continue;
                    int color=y>ry*.24?PALE:y>0?BLUE:
                        (Math.floorMod((int)(x+z*3+y),9)==0?SLATE:NAVY);
                    body.cube(color,x-2.7F,cy+y-2.7F,z,5.4F,5.4F,5.3F);
                }
        }
        for(int side:new int[]{-1,1}){
            // Layered scapulars: tapered feathers overlap the shoulder.
            for(int i=0;i<12;i++){
                float z=-34+i*6F;
                float x=side*(17F+4F*Mth.sin(i*.42F));
                body.cube(i%4==0?EDGE:SLATE,x-3,-95+i*.85F,z,6,5.2F,11);
            }
        }
        for(int i=0;i<16;i++){
            float z=-39+i*5.5F;
            float x=5+10F*Mth.sin(i*.36F);
            body.cube(i%3==0?WHITE:PALE,x-6,-49+6F*Mth.sin(i*.3F),z,11,4.2F,7);
            body.cube(i%3==0?WHITE:PALE,-x-5,-49+6F*Mth.sin(i*.3F),z,11,4.2F,7);
        }
    }
    private void buildHead(){
        // Segmented tapered neck with feather rings.
        for(int i=0;i<12;i++){
            float t=i/11F,z=-33-i*2.1F,y=-90-i*2.1F;
            float r=13-3.3F*t;
            for(float x=-r;x<=r;x+=3.7F)
                for(float dy=-r;dy<=r;dy+=3.7F){
                    if(x*x+dy*dy>r*r)continue;
                    neck.cube(dy>2?PALE:(i%3==0?FEATHER:SLATE),
                        x-1.9F,y+dy-1.9F,z,3.8F,3.8F,3.5F);
                }
        }
        // Solid rounded skull and brows, three-dimensional hooked beak.
        for(float z=-72;z<=-38;z+=3.1F){
            float t=(z+72)/34F;
            float rx=11+5F*Mth.sin(Mth.PI*t),ry=12+3F*Mth.sin(Mth.PI*t);
            for(float x=-rx;x<=rx;x+=3.2F)
                for(float dy=-ry;dy<=ry;dy+=3.2F){
                    if(x*x/(rx*rx)+dy*dy/(ry*ry)>1)continue;
                    head.cube(dy>3?PALE:SLATE,x-1.6F,-116+dy-1.6F,z,3.4F,3.4F,3.5F);
                }
        }
        for(int sign:new int[]{-1,1}){
            float x=sign*12.3F;
            head.cube(DARK,x-3.2F,-124,-60,6.4F,8.2F,6.8F);
            head.cube(IRIS,x-2.5F,-122.7F,-60,5,5.5F,5.2F);
            head.cube(BLACK,x-1.7F,-121.8F,-60,3.4F,3.5F,3.5F);
            head.cube(WHITE,x-1F,-122F,-59.4F,1.5F,1.3F,1.5F);
        }
        for(float z=-95;z< -69;z+=2.5F){
            float t=(z+95)/26F;
            float r=3+7F*t;
            for(float x=-r;x<=r;x+=2.7F){
                float y=-111+5F*(1-t);
                head.cube(z< -87?TIP:BEAK,x-1.4F,y-2.3F,z,2.9F,5F+3*t,3);
            }
        }
        head.cube(TIP,-2.5F,-103,-97,5,8.5F,5);
        for(int i=0;i<11;i++){
            float z=-45+i*2.6F;
            float x=5*Mth.sin(i*.4F);
            head.cube(i%3==0?CREST:EDGE,x-2.5F,-137-i*.5F,z,5,9+i*.45F,4.3F);
        }
    }
    private void buildWings(){
        for(int sign:new int[]{-1,1}){
            Group inner=sign<0?leftWing:rightWing;
            Group fore=sign<0?outer[0]:outer[1];
            Group wrist=sign<0?leftHand:rightHand;
            // Rigid shoulder bones with flight feathers on both surfaces.
            for(int j=0;j<20;j++){
                float t=j/19F,x=sign*(27+85*t);
                float back=-5+7*Mth.sin(t*Mth.PI),y=-82+6*t;
                inner.cube(j%4==0?BLUE:SLATE,x-4,y-5,back-11,8,10,23);
                for(int k=0;k<2;k++){
                    float z=back+10+k*7;
                    inner.cube(k==0?PALE:FEATHER,x-4,y+1,z,8,4,12);
                }
                if(j%2==0){
                    for(int k=0;k<3;k++){
                        float z=back+19+k*7;
                        inner.cube(k==2?EDGE:FEATHER,x-4,y+1,z,8,3.5F,18-k*2);
                    }
                }
            }
            for(int j=0;j<18;j++){
                float t=j/17F,x=sign*(112+69*t);
                float z=-7-11*t,y=-76-7*t;
                fore.cube(j%4==0?BLUE:DARK,x-3.8F,y-4.5F,z-8,7.8F,9,19);
                // Long separate primary feathers have thickness in all axes.
                for(int k=0;k<3;k++){
                    float length=24+16*Mth.sin(Mth.PI*t)+k*9;
                    float angle=(j+k*3)*.21F;
                    float zz=z+10+k*8;
                    for(int q=0;q<5;q++){
                        float f=q/5F;
                        fore.cube(q==4?EDGE:FEATHER,x-3+sign*f*(3+3*k),
                            y+2+f*2,zz+f*length,
                            6.5F*(1-.6F*f),2.7F,Math.max(4,length/5F));
                    }
                }
            }
            for(int j=0;j<10;j++){
                float x=sign*(181+j*7.3F);
                float len=45-j*2F;
                for(int k=0;k<7;k++){
                    float t=k/7F;
                    wrist.cube(k>5?EDGE:j%3==0?BLUE:FEATHER,
                        x-3.5F,-74+t*(10+j*.4F),-19+t*len,
                        7.4F*(1-.45F*t),3.4F,8.1F);
                }
            }
        }
    }
    private void buildTail(){
        for(int i=0;i<17;i++){
            float u=(i-8)/8F;
            float start=47+2F*Math.abs(u);
            for(int j=0;j<10;j++){
                float t=j/10F;
                float x=u*(13+22*t);
                float z=start+75*t*(1-.28F*Math.abs(u));
                tail.cube(j==9?EDGE:i%3==0?BLUE:FEATHER,
                    x-3.8F,-66+8*t,z,7.6F*(1-.35F*t),4.1F,9.8F);
            }
        }
    }
    private void buildFeet(){
        for(int i=0;i<2;i++){
            int sign=i==0?-1:1;
            Group foot=feet[i];
            foot.cube(DARK,sign*12-6,-44,17,12,25,13);
            foot.cube(CLAW,sign*12-4,-20,10,8,15,13);
            for(int toe=-1;toe<=1;toe++){
                float x=sign*12+toe*7;
                for(int j=0;j<5;j++)
                    foot.cube(CLAW,x-3,-9+j*1.1F,-1-j*5,6,5,8);
                foot.cube(TIP,x-2.2F,-3,-28,4.4F,8,8);
            }
        }
    }
    @Override public void setupAnim(StratimBirdEntity bird,float limb,float amount,
        float age,float yaw,float pitch){
        float wave=Mth.sin(age*.13F);
        int state=bird.action();
        float power=state==StratimBirdEntity.STORM?1.0F:
            state==StratimBirdEntity.DIVE?.55F:.22F;
        leftWing.pivot.zRot=wave*power;
        rightWing.pivot.zRot=-wave*power;
        leftWing.pivot.xRot=state==StratimBirdEntity.DIVE?-.45F:.03F;
        rightWing.pivot.xRot=state==StratimBirdEntity.DIVE?-.45F:.03F;
        outer[0].pivot.zRot=state==StratimBirdEntity.DIVE?.42F:wave*.15F;
        outer[1].pivot.zRot=state==StratimBirdEntity.DIVE?-.42F:-wave*.15F;
        leftHand.pivot.zRot=state==StratimBirdEntity.STORM?.20F*Mth.sin(age*.4F):.04F*wave;
        rightHand.pivot.zRot=-leftHand.pivot.zRot;
        feet[0].pivot.xRot=state==StratimBirdEntity.TALONS?-.85F:.18F+wave*.04F;
        feet[1].pivot.xRot=feet[0].pivot.xRot;
        neck.pivot.xRot=state==StratimBirdEntity.BEAK?-.34F:.03F*Mth.sin(age*.08F);
        head.pivot.xRot=state==StratimBirdEntity.BEAK?-.2F:.04F*Mth.sin(age*.09F);
        float elapsed=bird.actionTicks();
        if(state==StratimBirdEntity.BEAK){
            float strike=elapsed<11?-Mth.clamp(elapsed/11F,0,1):Mth.clamp(1-(elapsed-11)/10F,0,1)*1.8F;
            neck.pivot.xRot=strike*.48F;head.pivot.xRot=strike*.32F;
        }
        if(state==StratimBirdEntity.TALONS){
            float reach=Mth.clamp(elapsed/13F,0,1);feet[0].pivot.xRot=feet[1].pivot.xRot=-reach*1.25F;
            leftWing.pivot.zRot=-.48F;rightWing.pivot.zRot=.48F;
        }
        if(state==StratimBirdEntity.GUST){
            float sweep=elapsed<15?Mth.clamp(elapsed/15F,0,1):Math.max(0,1-(elapsed-15)/14F);
            leftWing.pivot.zRot=-sweep*.95F;rightWing.pivot.zRot=sweep*.95F;
            neck.pivot.xRot=-.25F*sweep;
        }
        if(state==StratimBirdEntity.DIVE){
            root.pivot.xRot=.35F;neck.pivot.xRot=.4F;head.pivot.xRot=.2F;
        }else root.pivot.xRot=0;
        tail.pivot.yRot=.10F*Mth.sin(age*.07F);
        root.pivot.zRot=bird.hurtTime>0?.15F*Mth.sin(age):0;
        if(bird.deathTime>0){
            float fall=Mth.clamp(bird.deathTime/20F,0,1);
            root.pivot.zRot=fall*1.15F;leftWing.pivot.zRot=fall*.9F;
            rightWing.pivot.zRot=-fall*.9F;head.pivot.xRot=fall*.6F;
        }
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer out,int light,int overlay,int tint){
        root.draw(stack,out,light,overlay);
    }
    public void renderTrophy(PoseStack stack,VertexConsumer out,int light,int overlay,float age){
        // Same head, neck and beak geometry as the living creature.
        stack.pushPose();
        // The detached head retains only its neck-relative pivot (in model pixels).
        // Keep it above the trophy base; the living body offset does not apply here.
        stack.translate(0,0,1.5);
        head.pivot.xRot=.02F*Mth.sin(age*.035F);
        // Render independent from neck/world position, preserving the actual skull voxels.
        head.draw(stack,out,light,overlay);
        stack.popPose();
    }
}
