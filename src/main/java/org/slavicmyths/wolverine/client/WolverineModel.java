package org.slavicmyths.wolverine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.client.FolkModelGeometry;
import org.slavicmyths.wolverine.NorthernWolverine;

/** Authored cuboid anatomy: long mustelid torso, articulated legs, muzzle, fangs and icy dorsal spines. */
public final class WolverineModel extends EntityModel<NorthernWolverine> {
    private final FolkModelGeometry geo=new FolkModelGeometry();
    private ModelPart part(ModelPart parent,float x,float y,float z){
        ModelPart result=geo.part(128,128);result.setPos(x,y,z);
        if(parent!=null)geo.attach(parent,result);
        return result;
    }
    private void box(ModelPart part,int material,float x,float y,float z,float w,float h,float d){
        geo.box(part,(material%4)*32,(material/4)*32,x,y,z,w,h,d);
    }
    private final ModelPart root=part(null,0,18,0);
    private final ModelPart torso=part(root,0,-23,0);
    private final ModelPart shoulder=part(torso,0,-3,-22);
    private final ModelPart head=part(shoulder,0,-7,-12);
    private final ModelPart jaw=part(head,0,6,-14);
    private final ModelPart[] upper=new ModelPart[4],lower=new ModelPart[4];
    private final ModelPart tail=part(torso,0,-3,30),tailTip=part(tail,0,1,12);

    public WolverineModel(){
        box(torso,0,-13,-10,-27,26,20,58);
        box(torso,1,-12,-13,-10,24,7,42);
        box(torso,0,-12,-7,18,24,19,18);
        box(shoulder,0,-15,-12,-11,30,25,27);
        box(shoulder,1,-16,-14,-7,32,8,19);
        box(shoulder,2,-12,3,-13,24,12,20);
        for(int i=-3;i<=3;i++){
            box(shoulder,i%2==0?0:1,i*4-2,-14,-10+(Math.abs(i)%3)*4,4,10,9);
        }
        box(head,0,-10,-10,-16,20,18,22);
        box(head,2,-8,-1,-24,16,11,15);
        box(head,3,-6,3,-30,12,7,12);
        box(head,3,-4,2,-31,8,4,4);
        box(jaw,3,-7,0,-17,14,5,17);
        for(int side:new int[]{-1,1}){
            box(head,0,side*8-3,-16,-8,6,8,7);
            box(head,3,side*8-2,-14,-9,4,5,3);
            box(head,4,side*6-2,-3,-16.5F,4,3,.8F);
            box(head,5,side*6-1,-2,-17.3F,2,2,.85F);
            box(head,6,side*5-2,5,-24,3,9,4);
            box(head,6,side*5-2,7,-18,3,6,3);
        }
        for(int i=0;i<4;i++){
            boolean front=i<2;int side=i%2==0?-1:1;
            upper[i]=part(root,side*11,-19,front?-21:24);
            lower[i]=part(upper[i],0,10,front?1:0);
            box(upper[i],front?0:1,-6,0,-7,12,13,14);
            box(lower[i],2,-5,0,-6,10,11,12);
            box(lower[i],3,-7,8,-11,14,4,19);
            for(int toe=-1;toe<=1;toe++)box(lower[i],6,toe*4-1,10,-14,3,3,7);
        }
        box(tail,1,-7,-7,0,14,13,20);
        box(tailTip,3,-6,-6,0,12,11,12);
        for(int z=-26;z<=25;z+=7){
            int index=(z+26)/7;
            box(torso,index%3==0?7:2,-3,-16,z,6,7+index%3*2,6);
        }
        for(int side:new int[]{-1,1})for(int z=-9;z<23;z+=8)
            box(torso,1,side*11-3,-10,z,5,9,7);
    }

    public ModelPart trophyHead(){return head;}
    @Override public void setupAnim(NorthernWolverine entity,float walk,float amount,float age,float yaw,float pitch){
        torso.xRot=torso.yRot=0;torso.zRot=Mth.sin(walk*.5F)*amount*.045F;
        shoulder.xRot=shoulder.yRot=0;
        head.setPos(0,-7,-12);
        head.xRot=Mth.clamp(pitch,-25,25)*Mth.DEG_TO_RAD*.5F;
        head.yRot=Mth.clamp(yaw,-35,35)*Mth.DEG_TO_RAD*.7F;
        jaw.xRot=0;
        tail.yRot=Mth.sin(age*.1F)*.2F;
        tailTip.yRot=Mth.sin(age*.1F-.7F)*.25F;
        for(int i=0;i<4;i++){
            float cycle=walk*.65F+(i==0||i==3?0:Mth.PI);
            upper[i].xRot=Mth.cos(cycle)*Math.min(1,amount)*.6F;
            lower[i].xRot=Math.max(0,Mth.sin(cycle))*Math.min(1,amount)*.75F;
        }
        int move=entity.action();
        float t=entity.actionTicks()+(age-entity.tickCount);
        float pulse=Math.max(0,Math.min(1,(t-6)/10F))*Math.max(0,Math.min(1,(35-t)/12F));
        if(move==NorthernWolverine.ROAR){
            float rise=Math.max(0,Math.min(1,(t-5)/20F))*Math.max(0,Math.min(1,(50-t)/12F));
            torso.xRot=-.7F*rise;
            shoulder.xRot=-.25F*rise;
            head.xRot=-.4F*rise;
            jaw.xRot=.8F*rise;
            for(int i=0;i<4;i++){
                upper[i].xRot=i<2?-.65F*rise:0;
                lower[i].xRot=i<2?.8F*rise:.1F*rise;
            }
        }else if(move==NorthernWolverine.CLAWS){
            upper[0].xRot=-1.1F*pulse;
            lower[0].xRot=.6F*pulse;
            float second=Math.max(0,Math.min(1,(t-16)/8F))*Math.max(0,Math.min(1,(33-t)/10F));
            upper[1].xRot=-1.1F*second;
            lower[1].xRot=.6F*second;
            shoulder.yRot=.17F*(second-pulse);
        }else if(move==NorthernWolverine.BITE||move==NorthernWolverine.GRIP){
            head.xRot+=.35F*pulse;head.z-=3*pulse;jaw.xRot=.72F*pulse;
        }else if(move==NorthernWolverine.SWEEP||move==NorthernWolverine.SPIKES){
            torso.yRot=.24F*pulse;
            shoulder.yRot=-.32F*pulse;
            upper[0].xRot=-.6F*pulse;
        }else if(move==NorthernWolverine.BURROW){
            torso.xRot=.25F*pulse;head.xRot=.25F*pulse;
            for(int i=0;i<4;i++)upper[i].xRot=.3F*pulse;
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer consumer,int light,int overlay,int color){
        root.render(pose,consumer,light,overlay,color);
    }
}
