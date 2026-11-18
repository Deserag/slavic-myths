package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.NeveyaBoss;

/** Procedural 2.5.2 voxel geometry. One optimized 3D skirt, body, separate hair and a straight two-ended scythe. */
public final class NeveyaModel extends EntityModel<NeveyaBoss>{
    private final FolkModelGeometry geo=new FolkModelGeometry();
    private final ModelPart root=geo.part(512,512),head=geo.part(512,512),scythe=geo.part(512,512);
    private final ModelPart legL=geo.part(512,512),legR=geo.part(512,512),rightArm=geo.part(512,512);
    private final ModelPart hair=geo.part(512,512),veil=geo.part(512,512);
    private void b(ModelPart p,int mat,float x,float y,float z,float w,float h,float d){
        geo.box(p,(mat%4)*128+5,(mat/4)*128+5,x,-y-h,z,w,h,d);
    }
    private void line(ModelPart p,int mat,float ax,float ay,float az,float bx,float by,float bz,float thickness){
        float len=(float)Math.sqrt((ax-bx)*(ax-bx)+(ay-by)*(ay-by)+(az-bz)*(az-bz));
        int count=Math.max(2,(int)(len/1.45F));
        for(int i=0;i<=count;i++){
            float t=i/(float)count;
            b(p,mat,Mth.lerp(t,ax,bx)-thickness/2,Mth.lerp(t,ay,by)-thickness/2,Mth.lerp(t,az,bz)-thickness/2,
                thickness,thickness,thickness);
        }
    }
    public NeveyaModel(){
        root.setPos(0,24,0);
        for(int side:new int[]{-1,1}){
            ModelPart leg=side<0?legL:legR;
            leg.setPos(side*2.6F,-42,0);
            b(leg,1,-2,-34,-2.6F,4,37,5.2F);
            b(leg,0,-2.5F,-39,-5.5F,5,6,8.5F);
            geo.attach(root,leg);
        }
        // Volumetric continuous skirt: one narrowing oval envelope with texture-based blue trim.
        for(int j=0;j<=20;j++){
            float y=1.0F+j*3.1F,t=y/64F;
            float rx=17.5F-12F*(float)Math.pow(t,.81);
            float rz=11F-7.5F*(float)Math.pow(t,.91);
            for(int i=0;i<26;i++){
                float a=i*(Mth.TWO_PI/26F),fold=1+.022F*Mth.sin(a*7F+j*.3F);
                float x=rx*fold*Mth.sin(a),z=rz*fold*Mth.cos(a);
                boolean front=Mth.cos(a)<-.45F;
                int mat=front&&(i%9==2||i%9==3)?2:(i%6==0?1:0);
                b(root,mat,x-1.45F,y,z-1.55F,2.9F,3.5F,3.1F);
            }
            b(root,0,-rx*.70F,y,-rz*.65F,rx*1.4F,3.55F,rz*1.30F);
            float ribbonZ=-rz-.9F;
            b(root,2,-1.9F,y,ribbonZ,3.8F,3.6F,.66F);
            if(j%4==1)b(root,3,-.72F,y+.9F,ribbonZ-.2F,1.44F,1.0F,.38F);
        }
        // Corset silhouette (wider bust, narrower waist), one unified chest plate.
        for(int j=0;j<11;j++){
            float y=63+j*2.7F;
            float radius=y<73?5.2F-(y-63)*.18F:y<83?3.4F+(y-73)*.42F:7.6F-(y-83)*.27F;
            float rz=y<75?3.4F:4.5F;
            for(int i=0;i<18;i++){
                float a=i*Mth.TWO_PI/18F;
                b(root,j%4==0?1:0,radius*Mth.sin(a)-.82F,y,rz*Mth.cos(a)-.78F,1.64F,2.85F,1.56F);
            }
            b(root,0,-radius*.76F,y,-rz*.68F,radius*1.52F,2.87F,rz*1.36F);
        }
        for(int j=0;j<6;j++){
            float y=80+j*1.7F,radius=(float)Math.sqrt(Math.max(.10,1-Math.pow((y-84.3F)/6.5F,2)));
            b(root,0,-7*radius,y,-5.4F-2.5F*radius,14*radius,1.95F,1.65F);
        }
        // Stronger blue frontal embroidery, still integrated into the black dress.
        for(int j=0;j<9;j++){
            float y=68+j*2.25F;
            b(root,j%3==0?3:2,-1.3F,y,-6.25F,2.6F,2.4F,.65F);
        }
        for(int side:new int[]{-1,1}){
            for(int j=0;j<7;j++){
                float y=72+j*2.5F,x=side*(3.3F+j*.24F);
                b(root,11,x-.44F,y,-5.75F,.88F,2.15F,.48F);
            }
            // Puffy mourning sleeve with decorative thin lace.
            float sx=side*7.4F;
            b(root,1,sx-2.1F,84F,-3.4F,4.2F,6.2F,6.2F);
            b(root,0,sx-2.25F,78F,-3.2F,4.5F,9.6F,5.3F);
            b(root,11,sx-2.3F,75F,-3.5F,4.6F,3.8F,5.6F);
        }
        // Exposed neck and arms, with the left palm actually touching the staff grip.
        b(root,4,-1.45F,91,-2.1F,2.9F,5.0F,4.0F);
        line(root,0,-7,87,-2,-12.2F,79,-5.5F,3.65F);
        line(root,4,-12.2F,79,-5.5F,-18,75,-8,2.65F);
        b(root,4,-19.2F,73.7F,-9,2.5F,3.3F,2.5F);
        line(root,0,7,87,-2,10,75,-3.8F,3.8F);
        line(root,4,10,75,-3.8F,9.0F,63,-6.5F,2.75F);
        b(root,4,7.8F,61,-7.3F,2.8F,3.0F,2.8F);
        // Right forearm moves independently during attacks (without disconnecting at the shoulder).
        rightArm.setPos(7F,-87,-2F);
        b(rightArm,0,-2F,-12F,-2.0F,4,12.5F,4.0F);
        geo.attach(root,rightArm);
        // Small oval face: narrow chin, wider midface, gentle cheek contour.
        head.setPos(0,-94,0);
        float[] h={3.0F,3.7F,4.4F,4.6F,4.5F,3.95F,3.25F,2.65F};
        for(int j=0;j<h.length;j++){
            float y=j*2.05F,rx=h[j],rz=rx*.83F;
            b(head,4,-rx,y,-rz,rx*2F,2.18F,rz*2F);
        }
        // Eyes are in front of the face surface and stay uncovered by the bangs.
        for(int side:new int[]{-1,1}){
            float x=side*1.90F;
            b(head,12,x-.85F,8.4F,-4.25F,1.7F,1.18F,.4F);
            b(head,14,x-.53F,8.5F,-4.57F,1.06F,.95F,.35F);
            b(head,8,x-.17F,8.75F,-4.80F,.34F,.46F,.23F);
            b(head,15,x-.84F,10.25F,-4.4F,1.68F,.40F,.38F);
        }
        b(head,4,-.32F,5.9F,-4.6F,.64F,1.8F,.66F);
        b(head,13,-.85F,3.8F,-4.35F,1.7F,.34F,.4F);
        geo.attach(root,head);
        // Thick complete silver scalp cap, a forehead fringe, distinct long volumetric locks.
        for(int j=0;j<14;j++){
            float x=-5.15F+j*.79F;
            b(hair,j%3==0?6:5,x-.50F,109.1F-.10F*Math.abs(x),-2.8F,1.25F,5.0F,5.4F);
            if(j%2==0&&j>2&&j<12)
                b(hair,5,x-.43F,106.5F,-4.55F,.98F,4.2F,1.15F);
        }
        for(int side:new int[]{-1,1}){
            for(int j=0;j<5;j++){
                float baseX=side*(5.5F+j*1.05F),length=33+j*4;
                for(float k=0;k<length;k+=3.7F){
                    float t=k/length;
                    float x=baseX+side*(1.6F*Mth.sin(t*2.9F+j*.25F)+t);
                    float z=-2.35F+t*-.60F+.8F*Mth.sin(t*3.4F+j);
                    b(hair,j%3==0?5:6,x-.85F,108.5F-k,z-.90F,1.7F,4.05F,1.85F);
                }
            }
        }
        for(int j=0;j<8;j++){
            float x=-5.6F+j*1.48F;
            for(float k=0;k<53;k+=4.1F){
                float t=k/53F;
                b(hair,j%2==0?5:6,x+1.5F*t*Mth.sin(3.0F*t+j*.22F)-1F,109-k,
                    4.0F+2.2F*t,2,4.4F,2.0F);
            }
        }
        geo.attach(root,hair);
        // Veil: thin, low-polygon drapes behind the head, no opaque panel over the face.
        for(int j=0;j<7;j++){
            float x=-6.2F+j*2.0F;
            b(veil,7,x,74,8.4F,2.0F,37.5F,.6F);
            b(veil,7,x,111.0F,3.6F,2.0F,1.35F,1.0F);
        }
        for(int j=0;j<11;j++){
            float x=-5.3F+j*1.05F;
            b(veil,8,x-.48F,112F,-.7F,.96F,1.8F,1.15F);
            if(j%3==0)b(veil,10,x-.52F,112.9F,-1.05F,1.05F,1.1F,.6F);
        }
        geo.attach(root,veil);
        // Straight dark staff: pivot exactly in the grasping left palm (local 0,0,0).
        scythe.setPos(-18,-75,-8);
        b(scythe,8,-.9F,-37,-.85F,1.8F,84F,1.7F);
        for(int i=0;i<8;i++)b(scythe,11,-1.04F,-4+i*1.5F,-1.02F,2.08F,.7F,2.03F);
        for(int center:new int[]{-35,41}){
            b(scythe,8,-2.1F,center-2.5F,-2.1F,4.2F,5F,4.2F);
            b(scythe,10,-.55F,center-1.6F,-2.7F,1.1F,3.2F,.45F);
            b(scythe,3,-.85F,center-.25F,-2.85F,1.7F,1.3F,.40F);
        }
        // Upper blade curves LEFT, lower blade curves RIGHT; no 'M' or horn-shaped silhouette.
        for(int i=0;i<=34;i++){
            float t=i/34F,x=-26*t,yy=41+5.5F*Mth.sin(t*Mth.PI)-14.2F*(float)Math.pow(t,1.5);
            float thick=3.5F*(float)Math.pow(1-t,.87)+.3F;
            b(scythe,i%5==0?9:12,x-.7F,yy-thick/2,-.94F,1.4F,thick,1.9F);
            if(i<30)b(scythe,9,x-.72F,yy+thick*.41F,-1.02F,1.44F,.44F,2.04F);
        }
        for(int i=0;i<=28;i++){
            float t=i/28F,x=21*t,yy=-35-3.0F*Mth.sin(t*Mth.PI)+13.0F*(float)Math.pow(t,1.6);
            float thick=3.1F*(float)Math.pow(1-t,.86)+.28F;
            b(scythe,i%4==0?9:12,x-.67F,yy-thick/2,-.94F,1.34F,thick,1.9F);
            if(i<25)b(scythe,9,x-.70F,yy-thick*.50F,-1.02F,1.40F,.4F,2.06F);
        }
        geo.attach(root,scythe);
        root.getAllParts().forEach(p->p.setInitialPose(p.storePose()));
    }
    @Override public void setupAnim(NeveyaBoss boss,float walk,float stride,float age,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);
        root.setPos(0,24,0);
        float sway=Mth.sin(age*.09F);
        legL.xRot=Mth.cos(walk*.67F)*stride*.25F;
        legR.xRot=-legL.xRot;
        if(boss.flying())root.y-=1.7F+.9F*sway;
        head.yRot=yaw*Mth.DEG_TO_RAD*.16F;
        head.xRot=pitch*Mth.DEG_TO_RAD*.17F;
        hair.zRot=sway*.018F;
        veil.zRot=-sway*.024F;
        float f=boss.actionFrame(),t=boss.action()==NeveyaBoss.IDLE?0:Mth.clamp(f/18F,0,1);
        int action=boss.action();
        if(action==NeveyaBoss.COMBO||action==NeveyaBoss.SWEEP||action==NeveyaBoss.HARVEST){
            scythe.zRot=.45F*Mth.sin(f*.20F);
            scythe.xRot=.22F*Mth.sin(f*.18F);
            rightArm.xRot=.15F*Mth.sin(f*.17F);
        }else if(action==NeveyaBoss.DASH||action==NeveyaBoss.DIVE){
            scythe.xRot=-.65F*t;
            scythe.zRot=-.32F*t;
            root.xRot=.10F*Mth.sin(f*.08F);
        }else if(action==NeveyaBoss.VERDICT||action==NeveyaBoss.CIRCLES||action==NeveyaBoss.BOLT||action==NeveyaBoss.MARK){
            rightArm.xRot=-.6F*t;
            scythe.zRot=.09F*Mth.sin(f*.10F);
        }
    }
    @Override public void renderToBuffer(PoseStack stack,VertexConsumer out,int light,int overlay,int color){
        root.render(stack,out,light,overlay,color);
    }
}
