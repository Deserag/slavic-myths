package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.savanna.Polkan;
import static org.slavicmyths.client.PolkanWeapons.*;

/** Four independent horse legs, hierarchical knees/elbows and hand-mounted weapons. */
final class PolkanModel extends EntityModel<Polkan> {
    private final FolkModelGeometry g=new FolkModelGeometry();
    private final ModelPart root,body,torso,head,tail,right,left,rightFore,leftFore,spear,stowedSpear,arrow;
    private final ModelPart[] legs=new ModelPart[4],shins=new ModelPart[4];
    private final PolkanWeapons.Bow bow,stowedBow;
    PolkanModel(){
        root=part(g,null,0,24,0);
        body=part(g,root,0,-15,0);
        box(g,body,COAT,-5,-10,-11,10,10,22);
        box(g,body,DARK,-4,-9,10,8,9,2);
        box(g,body,RED,-5.5F,-11,-5,11,1,15);
        for(int s:new int[]{-1,1}){
            float x=s<0?-6:5;
            box(g,body,RED,x,-10,-6,1,8,17);
            box(g,body,GOLD,x,-3,-6,1,1,17);
            for(int j=0;j<4;j++){
                box(g,body,ARMOR,x-.1F,-9,-5+j*4,1.2F,5,3);
                box(g,body,GOLD,x-.2F,-5,-5+j*4,1.4F,1,3);
            }
        }
        box(g,body,ARMOR,-4,-8,-12,8,6,2);
        box(g,body,GOLD,-4,-5,-12.3F,8,1,.5F);
        for(int i=0;i<4;i++){
            int side=i%2==0?-1:1;
            legs[i]=part(g,body,side*3.4F,-1,i<2?-8:8);
            box(g,legs[i],COAT,-1.7F,0,-2,3.4F,8,4);
            shins[i]=part(g,legs[i],0,7,0);
            box(g,shins[i],COAT,-1.55F,-1,-1.6F,3.1F,3,3.2F);
            box(g,shins[i],DARK,-1.45F,1,-1.5F,2.9F,5,3);
            box(g,shins[i],BEARD,-1.8F,6,-2,3.6F,3,4);
        }
        tail=part(g,body,0,-8,11);
        box(g,tail,BEARD,-1.5F,0,0,3,10,3);box(g,tail,DARK,-1,9,1,2,4,2);
        torso=part(g,body,0,-10,-8);
        box(g,torso,RED,-4,-12,-2,8,13,4);
        box(g,torso,ARMOR,-4.5F,-11,-3,9,10,5.5F);
        box(g,torso,GOLD,-4.5F,-2,-3.3F,9,2,6);
        for(int i=0;i<3;i++){
            box(g,torso,GOLD,-4,-10+i*3,-3.6F,8,.7F,.6F);
            for(int s=-3;s<=3;s+=3)box(g,torso,STEEL,s,-11+i*3,-3.7F,.6F,2,.7F);
        }
        box(g,torso,GOLD,-1,-1.5F,-4,2,1.5F,.6F);
        head=part(g,torso,0,-12,0);
        box(g,head,SKIN,-4,-8,-4,8,8,8);
        box(g,head,BEARD,-4,-2,-4.3F,8,2.5F,.7F);
        box(g,head,BEARD,-2,-.5F,-4,4,2,1.2F);
        for(int s:new int[]{-1,1}){
            box(g,head,WHITE,s<0?-3:1,-5,-4.1F,2,1,.2F);
            box(g,head,EYE,s<0?-2:1,-5,-4.3F,1,1,.2F);
            box(g,head,BEARD,s<0?-3:1,-6,-4.1F,2,.6F,.3F);
            box(g,head,ARMOR,s<0?-5:3,-8,-3.8F,2,6,8);
        }
        box(g,head,ARMOR,-5,-9,-5,10,2,10);
        box(g,head,GOLD,-5,-8,-5.2F,10,1,.5F);
        box(g,head,ARMOR,-3,-11,-3,6,2,6);box(g,head,ARMOR,-2,-13,-2,4,2,4);box(g,head,GOLD,-.8F,-15,-.8F,1.6F,2,1.6F);
        box(g,head,GOLD,-.65F,-8,-4.8F,1.3F,4,.7F);
        right=part(g,torso,-6,-10,0);left=part(g,torso,6,-10,0);
        for(ModelPart p:new ModelPart[]{right,left}){
            box(g,p,SKIN,-2,-1,-2,4,7,4);box(g,p,RED,-2,-1,-2.1F,4,4,4.2F);
            box(g,p,ARMOR,-2.6F,-2,-3,5.2F,3,6);box(g,p,GOLD,-2.6F,-2.5F,-2,5.2F,1,4);
        }
        rightFore=part(g,right,0,5.5F,0);leftFore=part(g,left,0,5.5F,0);
        for(ModelPart p:new ModelPart[]{rightFore,leftFore}){
            box(g,p,SKIN,-2,-1.6F,-2,4,7,4);box(g,p,ARMOR,-2.2F,-1.6F,-2.2F,4.4F,4.5F,4.4F);
            box(g,p,GOLD,-2.2F,2.5F,-2.2F,4.4F,.7F,4.4F);
        }
        spear=spear(g,rightFore,0,4,-2);
        stowedSpear=spear(g,body,6,-9,2);stowedSpear.xRot=1.57F;
        bow=new PolkanWeapons.Bow(g,leftFore,0,4,-2);
        stowedBow=new PolkanWeapons.Bow(g,body,-6,-7,4);stowedBow.root.xRot=1.25F;
        arrow=arrow(g,rightFore,0,4,-2);
        ModelPart quiver=part(g,body,5,-10,5);quiver.xRot=.25F;
        box(g,quiver,WOOD,-2,-8,-2,4,11,4);box(g,quiver,GOLD,-2.2F,-8,-2.2F,4.4F,1,4.4F);
        for(int i=0;i<4;i++){box(g,quiver,WOOD,-1.5F+i, -13+i%2,-.5F,.45F,6,.45F);box(g,quiver,RED,-1.7F+i,-13+i%2,-.8F,.85F,2,1);}
    }
    @Override public void setupAnim(Polkan e,float walk,float amount,float age,float yaw,float pitch){
        float move=Math.min(amount,1),t=walk*(e.running()?1.15F:.75F),a=e.actionAge()+Mth.clamp(age-e.tickCount,0,1);
        body.y=-15;body.xRot=0;torso.xRot=torso.yRot=0;head.yRot=yaw*.012F;head.xRot=pitch*.012F;
        tail.xRot=.1F+move*.2F;tail.yRot=Mth.sin(age*.12F)*.15F;
        for(int i=0;i<4;i++){
            float phase=t+(e.running()?(i<2?0:2.4F)+(i%2)*.5F:(i==0||i==3?0:Mth.PI));
            legs[i].xRot=Mth.sin(phase)*move*(e.running()?.95F:.55F);
            shins[i].xRot=Math.max(0,-legs[i].xRot)*.85F*(i<2?1:-1);
        }
        if(e.running())body.y-=Math.abs(Mth.sin(t))*.55F*move;
        right.xRot=-.15F;right.yRot=right.zRot=0;rightFore.xRot=-.6F;rightFore.yRot=rightFore.zRot=0;
        left.xRot=.15F;left.yRot=left.zRot=0;leftFore.xRot=-.2F;leftFore.yRot=leftFore.zRot=0;
        spear.xRot=.75F;spear.yRot=spear.zRot=0;
        bow.root.xRot=bow.root.yRot=bow.root.zRot=0;bow.draw(0);
        int action=e.action();boolean shooting=action==Polkan.BOW||action==Polkan.VOLLEY||action==Polkan.RUNE_BOW;
        spear.visible=!shooting&&!e.spearOut();stowedSpear.visible=shooting;bow.root.visible=shooting;stowedBow.root.visible=!shooting;arrow.visible=false;
        if(shooting){
            float cycle=action==Polkan.VOLLEY&&a>22?(a-22)%17:a;
            float draw=Mth.clamp(cycle/(action==Polkan.VOLLEY&&a>22?14:22),0,1);
            if(action!=Polkan.VOLLEY&&a>22)draw=Math.max(0,1-(a-22)/8);
            left.xRot=-1.52F;left.yRot=.12F;leftFore.xRot=-.10F;
            right.xRot=-1.25F;right.yRot=-.55F*draw;rightFore.xRot=-.45F-1.0F*draw;
            torso.yRot=-.30F*draw;bow.root.xRot=1.62F;bow.draw(draw);
            arrow.visible=cycle<22;arrow.xRot=1.57F;
        }else if(action==Polkan.CHARGE){
            float p=Mth.clamp(a/12,0,1);torso.xRot=.25F*p;right.xRot=-1.25F*p;rightFore.xRot=-.15F;spear.xRot=1.65F+1.25F*p;
        }else if(action==Polkan.THRUST){
            float p=Mth.sin(Mth.PI*Mth.clamp(a/24,0,1));right.xRot=-1.3F*p;rightFore.xRot=-.6F*(1-p);spear.xRot=.75F+2.1F*p;torso.xRot=.15F*p;
        }else if(action==Polkan.SWEEP){
            float p=Mth.clamp(a/30,0,1);torso.yRot=.9F*Mth.cos(p*Mth.PI);right.xRot=-1.25F;right.yRot=1.15F*Mth.cos(p*Mth.PI);rightFore.xRot=-.18F;spear.xRot=2.8F;
        }else if(action==Polkan.THROW){
            float p=Mth.clamp(a/16,0,1);right.xRot=e.spearOut()?-1.05F:-2.7F+1.65F*p;rightFore.xRot=e.spearOut()?-.2F:-.8F+.6F*p;spear.xRot=e.spearOut()?0:1.55F;torso.xRot=.15F*p;
        }else if(action==Polkan.STOMP){
            float p=Mth.sin(Mth.PI*Mth.clamp(a/30,0,1));body.xRot=-.42F*p;body.y-=2*p;legs[0].xRot=legs[1].xRot=-.75F*p;shins[0].xRot=shins[1].xRot=.8F*p;torso.xRot=.22F*p;
        }else if(action==Polkan.KICK){
            float p=Mth.sin(Mth.PI*Mth.clamp(a/25,0,1));body.xRot=.28F*p;legs[2].xRot=legs[3].xRot=1.45F*p;shins[2].xRot=shins[3].xRot=-.3F*p;
        }else if(action==Polkan.COMMAND||action==Polkan.GUARD){
            float p=Mth.sin(Mth.PI*Mth.clamp(a/35,0,1));right.xRot=-2.7F*p;rightFore.xRot=-.4F;spear.xRot=2.5F*p+.7F;
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){root.render(pose,out,light,overlay,color);}
}