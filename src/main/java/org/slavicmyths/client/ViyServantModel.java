package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.ViyServant;

/** Three weak and visually distinct cuboid shapes with Viy's palette. */
public final class ViyServantModel extends EntityModel<ViyServant>{
    private final FolkModelGeometry geo=new FolkModelGeometry();
    private final ModelPart root=geo.part(512,512),left=geo.part(512,512),right=geo.part(512,512);
    private final ViyServant.Kind kind;
    private void b(ModelPart p,int material,float x,float y,float z,float w,float h,float d){
        geo.box(p,(material%4)*128+4,(material/4)*128+4,x,-y-h,z,w,h,d);
    }
    public ViyServantModel(ViyServant.Kind kind){
        this.kind=kind;root.setPos(0,24,0);
        switch(kind){
            case UPYR->{
                for(int j=0;j<7;j++){
                    float y=9+j*2.5F,r=6.0F+Mth.sin(j*.52F)*1.5F;
                    b(root,j%2==0?0:1,-r,y,-4.0F,r*2F,2.8F,8.5F);
                }
                b(root,9,-5.3F,25.6F,-5.3F,10.6F,8.4F,10F);
                b(root,12,-3.8F,27.3F,-5.9F,7.6F,2.7F,1.9F);
                for(int s:new int[]{-1,1}){
                    b(root,15,s*2.4F-.8F,29,-6.2F,1.6F,1.1F,1F);
                    b(root,6,s*5F-2.4F,8,-6.5F,4.8F,15,6.8F);
                    b(root,5,s*5F-2.4F,3,-8F,4.8F,5,7F);
                }
                b(left,5,-2.4F,-8,-3.2F,4.8F,10,6.4F);
                b(right,5,-2.4F,-8,-3.2F,4.8F,10,6.4F);
                left.setPos(-4.3F,-8,0);right.setPos(4.3F,-8,0);
            }
            case NAV->{
                for(int j=0;j<8;j++){
                    float y=3+j*2.6F,r=6.2F-(j*.28F);
                    b(root,j%3==0?12:10,-r,y,-2.1F,r*2F,2.8F,4.2F);
                }
                for(int s:new int[]{-1,1}){
                    b(root,10,s*5F-1.7F,6,-2.2F,3.4F,20,4.4F);
                    for(int j=0;j<6;j++)
                        b(root,12,s*5F-.8F,4+j*3,-3.5F,1.6F,2.8F,.8F);
                }
                b(root,12,-4.2F,23.5F,-3F,8.4F,8.3F,6F);
                for(int s:new int[]{-1,1})b(root,14,s*2F-.7F,27.4F,-3.8F,1.4F,1.4F,.7F);
                b(left,12,-1.4F,-10,-2F,2.8F,12,4F);
                b(right,12,-1.4F,-10,-2F,2.8F,12,4F);
                left.setPos(-5.5F,-22,0);right.setPos(5.5F,-22,0);
            }
            case KORNELAP->{
                for(int j=0;j<5;j++){
                    float y=5+j*1.35F,r=6.0F-j*.4F;
                    b(root,j%2==0?1:7,-r,y,-4.2F,r*2F,1.5F,9.1F);
                }
                b(root,3,-4.5F,9.7F,-8.0F,9,4.2F,7.6F);
                for(int s:new int[]{-1,1}){
                    b(root,14,s*2.4F-.7F,11.0F,-8.7F,1.4F,1.3F,.9F);
                    for(int j=0;j<4;j++)b(root,7,s*3.8F-1,8.5F+j*1.5F,-4+j*2,2F,2F,2.2F);
                }
                b(left,7,-1.9F,-5,-1.4F,3.8F,6,2.8F);
                b(right,7,-1.9F,-5,-1.4F,3.8F,6,2.8F);
                left.setPos(-4F,-7,-2.7F);right.setPos(4F,-7,-2.7F);
            }
        }
        geo.attach(root,left);geo.attach(root,right);
        root.getAllParts().forEach(p->p.setInitialPose(p.storePose()));
    }
    @Override public void setupAnim(ViyServant mob,float walk,float stride,float age,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);
        root.setPos(0,24,0);
        float motion=Mth.cos(walk*.77F)*Mth.clamp(stride,0,1)*.57F;
        left.xRot=motion;right.xRot=-motion;
        if(kind==ViyServant.Kind.NAV)root.y-=.6F+Mth.sin(age*.12F)*.55F;
        else root.y-=Mth.abs(Mth.sin(walk*.77F))*stride*.25F;
    }
    @Override public void renderToBuffer(PoseStack p,VertexConsumer c,int light,int overlay,int color){
        root.render(p,c,light,overlay,color);
    }
}
