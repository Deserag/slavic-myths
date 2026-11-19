package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.marine.StratimEntity;

/** Segmented volumetric marine-bird ModelParts, independent hinge and feather roots. */
public final class StratimModel extends EntityModel<StratimEntity> {
    private static final int DARK=0xFF243749,BLUE=0xFF3F6078,SLATE=0xFF628194,
        SILVER=0xFFB1C8C8,PALE=0xFFD7DFD3,BEAK=0xFFD4B379,TIP=0xFF594D46,
        EYE=0xFF101822,IRIS=0xFFF1D799,LEG=0xFF677780;
    private final FolkModelGeometry g=new FolkModelGeometry();
    private final Part root,neck,head,tail,leftWing,rightWing,leftHand,rightHand;
    private final Part[] primaryL=new Part[10],primaryR=new Part[10];
    private final class Part{
        final ModelPart pivot=g.part(256,256);
        final Map<Integer,ModelPart> meshes=new LinkedHashMap<>();
        final List<Part> children=new ArrayList<>();
        final float x,y,z;
        Part(Part parent,float x,float y,float z){
            this.x=x;this.y=y;this.z=z;
            pivot.setPos(parent==null?x:x-parent.x,parent==null?y:y-parent.y,parent==null?z:z-parent.z);
            if(parent!=null)parent.children.add(this);
        }
        void cube(int color,float x,float y,float z,float w,float h,float d){
            g.box(meshes.computeIfAbsent(color,k->g.part(256,256)),x-this.x,y-this.y,z-this.z,w,h,d);
        }
        void draw(PoseStack ps,VertexConsumer vc,int light,int overlay){
            ps.pushPose();pivot.translateAndRotate(ps);
            for(var e:meshes.entrySet())e.getValue().render(ps,vc,light,overlay,e.getKey());
            for(Part part:children)part.draw(ps,vc,light,overlay);
            ps.popPose();
        }
    }
    public StratimModel(){
        root=new Part(null,0,0,0);
        neck=new Part(root,0,-45,-39);
        head=new Part(neck,0,-70,-59);
        tail=new Part(root,0,-17,34);
        leftWing=new Part(root,19,-38,-7);
        rightWing=new Part(root,-19,-38,-7);
        leftHand=new Part(root,12,5,10);
        rightHand=new Part(root,-12,5,10);
        body();
        makeNeck();
        makeWings();
        makeFeet();
        makeTail();
    }
    private void ellipsoid(Part p,int top,int bottom,float cx,float cy,float cz,float rx,float ry,float rz,float step){
        for(float z=-rz;z<=rz;z+=step){
            for(float y=-ry;y<=ry;y+=step){
                for(float x=-rx;x<=rx;x+=step){
                    float n=x*x/(rx*rx)+y*y/(ry*ry)+z*z/(rz*rz);
                    if(n>1.05F)continue;
                    p.cube(y<0?top:bottom,cx+x-step*.5F,cy+y-step*.5F,cz+z-step*.5F,
                        step+.15F,step+.15F,step+.15F);
                }
            }
        }
    }
    private void body(){
        ellipsoid(root,DARK,SLATE,0,-26,0,22,24,40,5);
        ellipsoid(root,BLUE,SILVER,0,-15,-25,17,19,19,4.5F);
        for(int i=0;i<9;i++){
            float x=-14+i*3.5F;
            root.cube(i%3==0?PALE:SILVER,x,-5,-40,2.8F,7,15);
        }
        for(int sign:new int[]{-1,1})
            for(int i=0;i<8;i++){
                float x=sign*(13+i*.95F);
                root.cube(i%2==0?BLUE:DARK,x-2,-35+i*2,4+i*2,4,6,14);
            }
    }
    private void makeNeck(){
        for(int i=0;i<13;i++){
            float t=i/12F;
            float z=-39-26*t;
            float y=-45-24*t;
            float r=9-3.0F*t;
            ellipsoid(neck,i%3==0?SLATE:BLUE,i%3==0?SILVER:SLATE,
                0,y,z,r,r*1.07F,6.5F,3.3F);
            for(int sign:new int[]{-1,1})
                neck.cube(SILVER,sign*(r-2.3F),y+2.0F,z-3,2.5F,6,5);
        }
        ellipsoid(head,DARK,SLATE,0,-76,-65,12,11,13,3.1F);
        ellipsoid(head,BLUE,SILVER,0,-72,-77,8,6,10,2.8F);
        // Hooked four-stage bill, not a flat panel.
        for(int i=0;i<9;i++){
            float t=i/8F;
            float w=7-5*t;
            head.cube(i>=7?TIP:BEAK,-w,-77+2*t*t,-85-3.4F*i,w*2,5.2F-2.1F*t,5.1F);
        }
        head.cube(TIP,-2,-75,-115,4,6,5);
        for(int side:new int[]{-1,1}){
            head.cube(DARK,side*9-2,-82,-72,4,7,8);
            head.cube(IRIS,side*10-1.5F,-80,-72,3,5,5);
            head.cube(EYE,side*11-1,-79,-71,2,3,3);
            for(int i=0;i<6;i++)
                head.cube(i%2==0?SILVER:BLUE,side*(3+i*1.5F)-2,-90-i*1.1F,-62+i*.6F,3,8,3.5F);
        }
    }
    private void makeWings(){
        for(int side:new int[]{-1,1}){
            Part wing=side==1?leftWing:rightWing;
            // Real wedge in three spatial dimensions from shoulder to wrist to primaries.
            for(int section=0;section<18;section++){
                float t=section/17F;
                float base=19+section*10.1F;
                float z=-10+15*t+10*Mth.sin(t*Mth.PI);
                float depth=30-17*t;
                float thickness=10-6.0F*t;
                for(int row=0;row<5;row++){
                    float rz=z-depth*.5F+row*depth/5F;
                    float yy=-38+3*Mth.sin(t*Mth.PI)+row*.6F;
                    wing.cube(row==0?DARK:row==4?SILVER:BLUE,
                        side*base-5,yy-thickness*.5F,rz,
                        10,thickness,depth/4.5F);
                }
            }
            for(int i=0;i<10;i++){
                float u=i/9F;
                float span=194+8*u;
                Part primary=new Part(wing,side*(155+i*4.3F),-36,-3+i*4);
                if(side==1)primaryL[i]=primary;else primaryR[i]=primary;
                for(int j=0;j<10;j++){
                    float t=j/9F;
                    float x=side*(153+i*4.3F+t*(40-i*2.2F));
                    float z=-8+i*5+t*(7+i*.7F);
                    float width=(8-i*.38F)*(1-.6F*t);
                    primary.cube(j>=8?PALE:j%3==0?SLATE:DARK,
                        x-width*.5F,-39-t*2,z,Math.max(2.1F,width),3.1F-t,9.1F-5*t);
                }
            }
            // Secondary feathers along entire trailing edge: independently visible 3D quills.
            for(int i=0;i<19;i++){
                float u=i/18F,x=side*(28+u*166);
                float z=16+12*Mth.sin(u*Mth.PI);
                for(int j=0;j<7;j++){
                    float v=j/6F;
                    wing.cube(j>4?PALE:SLATE,x-3.5F,-32+v*2,
                        z+v*(21-12*u),7-u*3,2.7F,6-u*2);
                }
            }
        }
    }
    private void makeFeet(){
        for(int sign:new int[]{-1,1}){
            Part foot=sign==1?leftHand:rightHand;
            foot.cube(LEG,sign*11-3,4,7,6,19,8);
            foot.cube(LEG,sign*12-3,22,0,6,17,8);
            for(int claw=0;claw<3;claw++){
                float x=sign*13+(claw-1)*5;
                foot.cube(LEG,x-2,37,-4,4,4,12);
                foot.cube(TIP,x-1.5F,39,-14,3,4,12);
                foot.cube(TIP,x-1,42,-18,2,4,7);
            }
            foot.cube(TIP,sign*14-2,37,9,4,5,10);
        }
    }
    private void makeTail(){
        for(int i=0;i<11;i++){
            float side=(i-5)*5.8F;
            for(int j=0;j<14;j++){
                float t=j/13F;
                float x=side*(.55F+t*.9F);
                tail.cube(j>11?PALE:i%2==0?BLUE:SLATE,x-3,
                    -18+t*8,34+t*(70-3*Math.abs(i-5)),6-2*t,3.2F,7-3*t);
            }
        }
    }
    @Override public void setupAnim(StratimEntity bird,float walk,float amount,float age,float yaw,float pitch){
        float flap=Mth.sin(age*.11F);
        int a=bird.action();
        float force=a==StratimEntity.STORM?1.4F:a==StratimEntity.DIVE?.35F:.60F;
        float angle=a==StratimEntity.DIVE?-.8F:force*flap*.30F;
        leftWing.pivot.zRot=angle;rightWing.pivot.zRot=-angle;
        leftWing.pivot.xRot=a==StratimEntity.DIVE?-.35F:.05F*Mth.sin(age*.07F);
        rightWing.pivot.xRot=leftWing.pivot.xRot;
        for(int i=0;i<10;i++){
            float feather=Mth.sin(age*.12F+i*.27F)*.04F;
            primaryL[i].pivot.yRot=feather;
            primaryR[i].pivot.yRot=-feather;
        }
        neck.pivot.xRot=.045F*Mth.sin(age*.07F)+(a==StratimEntity.PECK?-.3F:0);
        head.pivot.xRot=a==StratimEntity.PECK?-.28F:.02F*Mth.sin(age*.09F);
        tail.pivot.xRot=.08F*Mth.sin(age*.08F);
        leftHand.pivot.xRot=rightHand.pivot.xRot=a==StratimEntity.TALONS?-.8F:.12F;
        root.pivot.zRot=a==StratimEntity.DIVE?.07F:0;
        if(bird.deathTime>0){
            float d=Mth.clamp(bird.deathTime/20F,0,1);
            root.pivot.zRot=d*.9F;
            leftWing.pivot.zRot=-1.1F*d;rightWing.pivot.zRot=1.1F*d;
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int tint){
        root.draw(pose,out,light,overlay);
    }
    public void renderTrophy(PoseStack pose,VertexConsumer out,int light,int overlay,float age){
        head.pivot.xRot=.035F*Mth.sin(age*.07F);
        head.draw(pose,out,light,overlay);
    }
}
