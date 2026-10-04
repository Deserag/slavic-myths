package org.slavicmyths.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.kurgan.*;
import static org.slavicmyths.kurgan.KurganFighter.*;

/** Own anatomy, articulated long arms/ribbons, stepped helmets and actual held silhouettes. */
public final class KurganCreatureModel extends EntityModel<KurganCreature> {
    private final Kind kind;private final ModelRenderer root,body,head,eyes,left,right,leftFore,rightFore,leftLeg,rightLeg,cape,jaw,weapon,shield;
    private final ModelRenderer[] ribbons=new ModelRenderer[6];
    private float opacity=1;
    private ModelRenderer part(ModelRenderer parent,int material,float x,float y,float z){ModelRenderer p=new ModelRenderer(this,(material%4)*64,(material/4)*64);p.setPos(x,y,z);if(parent!=null)parent.addChild(p);return p;}
    private ModelRenderer box(ModelRenderer parent,int mat,float x,float y,float z,float w,float h,float d){ModelRenderer p=part(parent,mat,0,0,0);p.addBox(x,y,z,w,h,d);return p;}
    public KurganCreatureModel(Kind kind){this.kind=kind;texWidth=256;texHeight=256;boolean upyr=kind==Kind.UPYR,nav=kind==Kind.NAV,volkhv=kind==Kind.VOLKHV;
        float legs=upyr?10:nav?14:volkhv?12:kind==Kind.PRINCE?16:kind==Kind.VOEVODA?14:12;
        float torso=upyr?11:nav?11:kind==Kind.PRINCE?15:kind==Kind.VOEVODA?13:12;
        float width=upyr?9:nav?6:volkhv?7:kind==Kind.PRINCE?12:kind==Kind.VOEVODA?11:9;
        root=part(null,0,0,24,0);body=part(root,nav?13:upyr?1:volkhv?12:2,0,-legs,0);
        body.addBox(-width/2,-torso,-3,width,torso,6);
        head=part(body,0,0,-torso-1,upyr?-2.8F:0);head.addBox(-4,-7,-4,8,7,7);
        jaw=part(head,upyr?0:3,0,0,0);jaw.addBox(-3.5F,-.5F,-4.4F,7,2.3F,6);
        box(head,7,-3.5F,-4.4F,-4.1F,2.6F,1.8F,.25F);box(head,7,.9F,-4.4F,-4.1F,2.6F,1.8F,.25F);
        eyes=part(head,8,0,0,0);eyes.addBox(-3,-4,-4.4F,1.5F,1,.4F);eyes.addBox(1.5F,-4,-4.4F,1.5F,1,.4F);
        if(nav){box(head,13,-4.4F,-7.3F,-3.9F,8.8F,1.2F,7.8F);box(head,7,-.9F,-1.8F,-4.1F,1.8F,1.8F,.3F);jaw.visible=false;}
        else{box(head,7,-3,-.3F,-4.7F,6,1.4F,.5F);for(int i=0;i<5;i++)box(jaw,3,-2.8F+i*1.15F,-.3F,-4.9F,.65F,.9F+(i%2)*.5F,.6F);box(head,0,-.75F,-2.8F,-5,1.5F,2,1);}
        left=part(body,upyr?1:nav?13:volkhv?12:2,width/2+1,-torso+1,0);right=part(body,upyr?1:nav?13:volkhv?12:2,-width/2-1,-torso+1,0);
        float arm=upyr?7.5F:nav?8:kind==Kind.PRINCE?8:6.5F;
        left.addBox(-.5F,-1,-2,4,arm,4);right.addBox(-3.5F,-1,-2,4,arm,4);
        leftFore=part(left,0,1.5F,arm-1,0);rightFore=part(right,0,-1.5F,arm-1,0);
        for(ModelRenderer fore:new ModelRenderer[]{leftFore,rightFore}){fore.addBox(-2,0,-2,4,upyr?8:7,4);if(upyr){box(fore,0,-2.7F,7,-2.3F,5.4F,3,5);for(int finger=0;finger<3;finger++){ModelRenderer claw=box(fore,4,-2+finger*1.6F,9,-3,1.1F,4,1.1F);claw.xRot=-.35F;}box(fore,0,-3,7,-2,1,4,1);}else if(!nav){box(fore,volkhv?12:6,-2.2F,4,-2.2F,4.4F,1.2F,4.4F);box(fore,0,-2.1F,6,-2.1F,4.2F,2.5F,4.2F);}}
        leftLeg=part(root,upyr?0:nav?13:5,2.5F,-legs,0);rightLeg=part(root,upyr?0:nav?13:5,-2.5F,-legs,0);
        if(!nav)for(ModelRenderer leg:new ModelRenderer[]{leftLeg,rightLeg}){leg.addBox(-2,0,-2,4,legs-2,4);box(leg,upyr?0:5,-2.2F,legs-4,-3.4F,4.4F,4,6);if(upyr)for(int toe=0;toe<3;toe++)box(leg,0,-1.8F+toe*1.3F,legs-1,-4.6F,1,1,1.5F);}
        cape=part(body,4,0,-torso,3.2F);weapon=part(rightFore,10,0,7,0);shield=part(leftFore,14,2,5,-2.5F);
        if(upyr){box(body,0,-5,-torso+1,1,10,6,4);box(body,5,-4.8F,-1.5F,-3.4F,9.6F,1.5F,6.8F);for(int i=0;i<5;i++)box(body,1,-4.5F+i*1.8F,-1,-3.6F,1.4F,3+(i%3),.5F);}
        else if(nav){for(int i=0;i<6;i++){ribbons[i]=part(body,13,(i-2.5F)*1.25F,-1,(i%2)*2-1);ribbons[i].addBox(-.7F,0,-.4F,1.4F,10+(i%3)*2,.8F);}weapon.visible=shield.visible=false;}
        else if(volkhv){box(head,12,-4.8F,-8,-4.5F,9.6F,1.5F,9);box(head,12,-4.8F,-7,-4,1.3F,9,8);box(head,12,3.5F,-7,-4,1.3F,9,8);box(head,12,-4,-6,2.5F,8,6,2);
            for(int i=0;i<6;i++){ModelRenderer skirt=box(body,12,-6+i*2,-1,-3.6F,1.7F,legs-1-(i%2),7.2F);skirt.zRot=(i-2.5F)*.025F;}
            for(int i=0;i<3;i++){box(body,6,-3+i*2.8F,-torso+3+i,-3.6F,.6F,5,.5F);box(body,6,-3.8F+i*2.8F,-torso+7+i,-3.9F,2.2F,2.6F,.6F);box(body,7,-3.2F+i*2.8F,-torso+7.7F+i,-4.1F,1,1.2F,.2F);}
            staff();shield.visible=false;
        }else{
            // Stepped conical helmet for retainers/voevoda; open princely circlet for the boss.
            if(kind==Kind.PRINCE){box(head,6,-4.5F,-7.2F,-4.5F,9,1.2F,8.5F);for(int i=0;i<5;i++)box(head,6,-4+i*1.8F,-9.2F-(i%2),-4.7F,1.2F,3+(i%2),.8F);}
            else{for(int layer=0;layer<3;layer++){float size=9-layer*2.2F;box(head,2,-size/2,-7.7F-layer*1.6F,-size/2,size,1.8F,size);}box(head,2,-.5F,-8,-5,.9F,4,1);}
            box(body,5,-width/2-.2F,-1.8F,-3.3F,width+.4F,1.8F,6.6F);box(body,6,-1.2F,-2,-3.7F,2.4F,2,.5F);
            for(int side:new int[]{-1,1}){box(body,2,side<0?-width/2-1:width/2-2,-torso-1,-3.7F,3.5F,3.5F,7);box(body,4,side<0?-width/2:1,-.8F,-3.5F,width/2-1,5,.6F);}
            if(kind==Kind.VOEVODA||kind==Kind.PRINCE){for(int i=0;i<5;i++)box(cape,11,-width/2+i*width/5,0,0,width/5-.15F,torso+legs-3+(i%2)*2,.6F);box(cape,5,-width/2-1,-.3F,-.3F,width+2,3.2F,1.3F);if(kind==Kind.PRINCE)seal(cape,0,torso, -.8F,5);}
            roundShield(kind==Kind.PRINCE?7:kind==Kind.VOEVODA?6.5F:5.5F);
            if(kind==Kind.VOEVODA)axe();else sword(kind==Kind.PRINCE?19:13);
        }
    }
    private void sword(float length){box(weapon,5,-.7F,-3,-.6F,1.4F,4,1.2F);box(weapon,6,-3,1,-.8F,6,1.2F,1.6F);box(weapon,2,-1.2F,2,-.45F,2.4F,length, .9F);box(weapon,3,-1.3F,2,-.55F,.4F,length,1.1F);box(weapon,2,-.7F,length+2,-.4F,1.4F,2,.8F);box(weapon,6,-1.2F,-4,-.9F,2.4F,1.5F,1.8F);}
    private void axe(){box(weapon,10,-.6F,-4,-.7F,1.2F,18,1.4F);box(weapon,6,-1,-2,-1,2,1.2F,2);box(weapon,2,-2,4,-1.1F,4,3,2.2F);box(weapon,2,2,3,-1,4,6,2);box(weapon,3,5.5F,2,-1.2F,1,8,2.4F);box(weapon,2,-4,4.3F,-.8F,2,1.7F,1.6F);}
    private void staff(){box(weapon,10,-.7F,-18,-.7F,1.4F,32,1.4F);for(int side:new int[]{-1,1}){ModelRenderer branch=box(weapon,10,side<0?-1:0,-25,-.7F,1,8,1.4F);branch.zRot=side*.6F;}box(weapon,3,-2.5F,-21,-2.7F,5,5,3);box(weapon,7,-1.6F,-19.5F,-2.9F,1,1,.3F);box(weapon,7,.6F,-19.5F,-2.9F,1,1,.3F);box(weapon,6,2,-18,0,.5F,7,.5F);box(weapon,6,1.4F,-11,-.5F,2,2,1);}
    private void roundShield(float radius){for(int band=-3;band<=3;band++){float w=(float)Math.sqrt(Math.max(0,radius*radius-band*band*2.6F));box(shield,kind==Kind.PRINCE?6:2,-w,band*2,-1,2*w,2.2F,2);box(shield,14,-w+.6F,band*2+.2F,-1.25F,2*w-1.2F,1.8F,.4F);}box(shield,2,-1.8F,-1.8F,-2,3.6F,3.6F,.9F);seal(shield,0,0,-1.8F,kind==Kind.PRINCE?4:3.4F);}
    private void seal(ModelRenderer p,float x,float y,float z,float size){ModelRenderer center=part(p,6,x,y,z);for(int i=0;i<8;i++){double angle=i*Math.PI/4;ModelRenderer ray=box(center,6,-.3F,-1.5F-size/2,0,.6F,size/2,.3F);ray.zRot=(float)angle;}}
    @Override public void setupAnim(KurganCreature e,float stride,float amount,float age,float yaw,float pitch){float walk=MathHelper.cos(stride*.65F)*amount,breath=MathHelper.sin(age*.09F)*.025F;root.y=24;root.xRot=0;body.xRot=kind==Kind.UPYR?.26F:kind==Kind.NAV?.03F:breath;body.yRot=0;body.zRot=0;head.yRot=yaw*.017453F;head.xRot=pitch*.017453F;jaw.xRot=0;left.xRot=walk*.25F;right.xRot=-walk*.25F;left.zRot=-.08F;right.zRot=.08F;leftFore.xRot=rightFore.xRot=-.12F;leftLeg.xRot=-walk*.65F;rightLeg.xRot=walk*.65F;cape.xRot=.08F+amount*.12F;weapon.xRot=0;shield.yRot=0;
        if(kind==Kind.NAV){root.y=22+MathHelper.sin(age*.08F)*.6F;for(int i=0;i<6;i++)ribbons[i].xRot=MathHelper.sin(age*.075F+i)*.14F;left.xRot=right.xRot=.1F;}
        if(kind==Kind.VOLKHV){right.xRot=-.15F;weapon.xRot=.05F;}
        if(e.guarding()){left.xRot=-1.1F;leftFore.xRot=-.25F;shield.yRot=-.2F;body.yRot=-.16F;}
        Move m=e.action();float t=e.elapsed(age-e.tickCount),prep=Math.min(1,t/Math.max(1,m.windup));opacity=1;
        if(m!=Move.IDLE){boolean release=t>=m.windup;float swing=release?Math.min(1,(t-m.windup)/5):0;
            if(m==Move.CLAW||m==Move.BITE){body.xRot=.35F;right.xRot=-prep*1.7F+swing*2.6F;left.xRot=-prep*.9F+swing*1.6F;jaw.xRot=m==Move.BITE?prep*.6F:0;}
            else if(m==Move.BOLT||m==Move.SEAL||m==Move.CLONES||m==Move.SUMMON){right.xRot=-.5F-prep*.9F;rightFore.xRot=-.3F;left.xRot=-prep*1.35F;left.zRot=-prep*.3F;head.xRot=-prep*.12F;weapon.xRot=-.15F;}
            else if(m==Move.SHIFT){float fade=MathHelper.sin(Math.min(1,t/14)*(float)Math.PI);root.y-=fade*2;opacity=1-fade*.8F;body.zRot=prep*.14F;}
            else if(m==Move.LEAP||m==Move.RUSH||m==Move.CHARGE||m==Move.BASH){body.xRot=prep*.3F;left.xRot=-1.3F;right.xRot=-.4F;leftLeg.xRot=prep*.5F;rightLeg.xRot=-prep*.45F;}
            else if(m==Move.GRAB){body.xRot=.4F;left.xRot=right.xRot=-prep*1.6F;jaw.xRot=.4F;}
            else{float cycle=m==Move.COMBO&&release?((t-m.windup)%10)/6:swing;right.xRot=-prep*(m==Move.HEAVY?2.5F:1.5F)+cycle*2.6F;body.yRot=m==Move.SWEEP?-.55F+cycle*1.2F:prep*.2F;left.xRot=-.65F;}
        }
        if(e.phase()==2){body.xRot+=.18F;cape.xRot+=.13F;}
        float reaction=e.phaseElapsed(age-e.tickCount);if(e.phase()>0&&reaction<12){float flare=MathHelper.sin(reaction*(float)Math.PI/12);body.zRot+=flare*.12F;head.xRot-=flare*.25F;cape.xRot+=flare*.35F;}
    }
    public void renderEyes(MatrixStack pose,IVertexBuilder out){pose.pushPose();root.translateAndRotate(pose);body.translateAndRotate(pose);head.translateAndRotate(pose);eyes.render(pose,out,15728640,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();}
    @Override public void renderToBuffer(MatrixStack p,IVertexBuilder out,int light,int overlay,float r,float g,float b,float alpha){root.render(p,out,light,overlay,r,g,b,alpha*opacity);}
}
