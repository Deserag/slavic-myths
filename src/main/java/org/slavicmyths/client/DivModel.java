package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.savanna.Div;

final class DivModel extends EntityModel<Div> {
    private static final int DARK=0,PURPLE=1,MID=2,LIGHT=3,RED=4,RED2=5,BONE=6,SKIN=7,CLAW=8,HAIR=9,Hock=10,Shin=11,Foot=12,Joint=13,Band=14;
    private final FolkModelGeometry g=new FolkModelGeometry();
    private final ModelPart root,body,head,tail;
    private final ModelPart[] arms=new ModelPart[2],fore=new ModelPart[2],legs=new ModelPart[2],closed=new ModelPart[2],spread=new ModelPart[2];
    private static Vec3 v(double x,double y,double z){return new Vec3(x,y,z);}
    private static Vec3 mc(Vec3 p){return v(p.x,-p.z,p.y);}
    private ModelPart part(ModelPart parent,Vec3 pivot){ModelPart p=g.part(256,256);Vec3 q=mc(pivot);p.setPos((float)q.x,(float)q.y,(float)q.z);if(parent!=null)g.attach(parent,p);return p;}
    private void box(ModelPart p,Vec3 c,double w,double d,double h,int col){Vec3 q=mc(c);g.box(p,(col%4)*64+1,(col/4)*64+1,(float)(q.x-w/2),(float)(q.y-h/2),(float)(q.z-d/2),(float)w,(float)h,(float)d);}
    private void limb(ModelPart p,Vec3 a,Vec3 b,double w,double dep,int col){Vec3 z=b.subtract(a).normalize(),q=v(0,1,0);q=q.subtract(z.scale(q.dot(z)));if(q.length()<.1){q=v(1,0,0);q=q.subtract(z.scale(q.dot(z)));}Vec3 y=q.normalize(),x=y.cross(z);Vec3 cx=mc(x),cy=mc(y),cz=mc(z);ModelPart child=part(p,a.add(b).scale(.5));float yr=(float)Math.asin(Mth.clamp(-cx.z,-1,1));child.yRot=yr;if(Math.abs(Math.cos(yr))>1e-5){child.xRot=(float)Math.atan2(cy.z,cz.z);child.zRot=(float)Math.atan2(cx.y,cx.x);}else{child.xRot=0;child.zRot=(float)Math.atan2(-cy.x,cy.y);}
        float l=(float)a.distanceTo(b);g.box(child,(col%4)*64+1,(col/4)*64+1,(float)(-w/2),(float)(-dep/2),-l/2,(float)w,(float)dep,l);}
    private void feather(ModelPart p,Vec3 a,Vec3 b,double w,double d,int c,int tip){Vec3 delta=b.subtract(a);limb(p,a,a.add(delta.scale(.70)),w,d,c);limb(p,a.add(delta.scale(.67)),a.add(delta.scale(.9)),w*.77,d*.87,tip);limb(p,a.add(delta.scale(.88)),b,w*.49,d*.68,tip);}
    private void f(ModelPart p,Vec3 a,Vec3 b,double w,double d,int c){feather(p,a,b,w,d,c,c);}
    private static int palette(int i,int... values){return values[Math.floorMod(i,values.length)];}
    DivModel(){root=part(null,Vec3.ZERO);root.y=24;body=part(root,Vec3.ZERO);
        double[][] rows={{-5.4,28,6.8,9,DARK},{-3.7,28,9.3,12,PURPLE},{-1,27,11.4,14,DARK},{2,26,11,13,PURPLE},{5,24,9.3,11,DARK},{8,22,7.2,8,PURPLE},{10,21,5,6,PURPLE}};
        for(double[] r:rows)box(body,v(0,r[0],r[1]),r[2],3.2,r[3],(int)r[4]);
        limb(body,v(0,-3,31),v(0,-7,36.8),6.1,6.1,DARK);
        for(int i=0;i<10;i++){double t=2*Math.PI*i/10;f(body,v(3*Math.cos(t),-5.8+3*Math.sin(t),35.2),v(4.8*Math.cos(t),-4.9+4*Math.sin(t),29.5-(i%2)*.9),2.1,1.35,palette(i,PURPLE,MID,DARK));}
        double[] rx={5,6.2,6.5,5.7,4.2},ry={5.7,7.6,8.9,9.1,8.2};
        for(int j=0;j<5;j++)for(int i=0;i<13;i++){double t=2*Math.PI*i/13,z=32-j*2.7,cy=-1.2+j*1.15;int col=palette(j+i,DARK,PURPLE,MID,PURPLE);if(Math.sin(t)<-.65&&j==1)col=BONE;else if(Math.sin(t)<-.55&&j==2)col=RED2;f(body,v(rx[j]*Math.cos(t),cy+ry[j]*Math.sin(t),z),v((rx[j]+.45)*Math.cos(t),cy+ry[j]*Math.sin(t)+.5,z-4.7-(i%2)*.6),2.55,1.5,col);}
        Vec3 tp=v(0,8,23);tail=part(body,tp);
        for(int i=0;i<9;i++)feather(tail,v((i-4),8,23).subtract(tp),v((i-4)*2.1,25-Math.abs(i-4)*1.1,8+Math.abs(i-4)*.7).subtract(tp),2.6,1.6,palette(i,DARK,PURPLE,MID),i%2==0?RED:RED2);
        for(int i=0;i<7;i++)f(tail,v((i-3)*1.25,8,24).subtract(tp),v((i-3)*1.7,17,17).subtract(tp),2.1,1.2,palette(i,MID,PURPLE,DARK));
        for(int j=0;j<2;j++){int s=j==0?-1:1;
            Vec3 hip=v(s*4.1,2.7,23),knee=v(s*5.7,1.1,16).subtract(hip),hock=v(s*6.4,4.4,10).subtract(hip),ankle=v(s*6.8,-1.4,3.1).subtract(hip),foot=v(s*6.8,-1.8,2).subtract(hip);
            legs[j]=part(body,hip);ModelPart leg=legs[j];limb(leg,Vec3.ZERO,knee,4.7,4.4,PURPLE);
            for(int i=0;i<4;i++)f(leg,v(s*(3.3+i*.7),1,23).subtract(hip),v(s*(4.4+i*.7),2.6,15.2-(i%2)).subtract(hip),1.6,1.4,palette(i,DARK,MID));
            limb(leg,knee,hock,2.8,2.9,BONE);box(leg,hock,2.7,2.8,2.5,Hock);limb(leg,hock,ankle,2.25,2.35,Shin);
            for(double t:new double[]{.18,.43,.68,.9})box(leg,hock.lerp(ankle,t),2.45,2.6,.65,BONE);
            box(leg,foot,4,4.6,2.2,Foot);
            for(int i=-1;i<=1;i++){Vec3 a=foot.add(i*1.05,-1.2,0),b=foot.add(i*2.15,-4.7-(i==0?.8:0),-.25),c=b.add(i*.25,-1.8,-1.1);limb(leg,a,b,1.25,1.35,BONE);limb(leg,b,c,1.3,1.2,CLAW);}
            limb(leg,foot.add(0,1,0),foot.add(0,3.7,-.5),1.25,1.1,BONE);limb(leg,foot.add(0,3.7,-.5),foot.add(0,4.6,-1.4),1,1,CLAW);
            Vec3 sh=v(s*5.35,-3.9,30.1),el=v(s*8.15,-3.4,25.9),wr=v(s*8.7,-8.4,21.5).subtract(el),palm=v(s*8.7,-8.6,20.7).subtract(el);
            arms[j]=part(body,sh);ModelPart arm=arms[j];box(arm,Vec3.ZERO,3.6,4,4.1,DARK);limb(arm,Vec3.ZERO,el.subtract(sh),2.8,3,PURPLE);
            for(int i=0;i<3;i++)f(arm,v(s*(5.2+i*.65),-5.25,30.8-i*.8).subtract(sh),v(s*(7.8+i*.35),-5.1,25.8-i*.55).subtract(sh),1.35,1.15,palette(i,MID,PURPLE,DARK));
            fore[j]=part(arm,el.subtract(sh));ModelPart lower=fore[j];box(lower,Vec3.ZERO,2.6,2.8,2.5,Joint);limb(lower,Vec3.ZERO,wr,2.15,2.25,BONE);
            for(double t:new double[]{.25,.5,.75})box(lower,wr.scale(t),2.3,2.5,.7,Band);
            box(lower,palm,3,2.4,2.1,Foot);
            for(int i=-1;i<=1;i++){Vec3 a=palm.add(i*.9,-.3,-.6),b=palm.add(i*1.5,-1.15,-2.5-(i==0?.5:0)),c=b.add(-i*.25,-1.15,-1.2),d=c.add(-i*.15,-.7,.1);limb(lower,a,b,1.05,1.15,BONE);limb(lower,b,c,.9,.95,CLAW);limb(lower,c,d,.65,.7,CLAW);}
            limb(lower,palm.add(-s*1.1,.2,0),palm.add(-s*2.2,-.8,-1.3),1,1,BONE);limb(lower,palm.add(-s*2.2,-.8,-1.3),palm.add(-s*1.9,-2,-2),.85,.85,CLAW);
            Vec3 rt=v(s*4.7,1.8,32);closed[j]=part(body,rt);spread[j]=part(body,rt);wing(closed[j],s,rt,false);wing(spread[j],s,rt,true);
        }
        Vec3 hp=v(0,-7.2,36.8);head=part(body,hp);Vec3 hc=v(0,-7.2,39.7).subtract(hp);Vec3 q=mc(hc);
        g.box(head,192,192,(float)q.x-4,(float)q.y-4,(float)q.z-4,8,8,8);
        for(int i=0;i<5;i++){double x=(i-2)*1.3;f(head,v(x,-3.6,42.4).subtract(hp),v(x*1.5,1.4,43.2+(i%2==0?.6:0)).subtract(hp),1.55,1.3,palette(i,DARK,MID,RED2));}
    }
    private void wing(ModelPart p,int s,Vec3 root,boolean open){Vec3 bend=open?v(s*16,3.8,38.5):v(s*11.7,3.3,38.4),wr=open?v(s*29,5.5,37.4):v(s*20.8,6.5,30),outer=open?v(s*43,9,32):v(s*27,11.5,10.5);
        limb(p,Vec3.ZERO,bend.subtract(root),4.2,3.3,DARK);limb(p,bend.subtract(root),wr.subtract(root),3.5,2.9,PURPLE);limb(p,wr.subtract(root),outer.subtract(root),2.5,2.4,DARK);
        for(int i=0;i<11;i++){double t=i/10.0;Vec3 a=root.lerp(bend,t),b=a.add(open?s*(3.2+t*3):s*(3.7+t*4),open?10.5:6.5,open?-12.8-t*3.2:-20.4-t*2.7);int c=palette(i,PURPLE,DARK,MID);feather(p,a.add(0,-.8,-.2).subtract(root),b.subtract(root),3,1.9,c,i%3==0?RED2:c);}
        for(int i=0;i<12;i++){double t=i/11.0;Vec3 a=bend.lerp(wr,t),b=open?v(s*(19+24*t),15+4.3*t,18.5+12*t+Math.sin(t*Math.PI)+(i%3)*.65):v(s*(14+15*t),12+5*t,7-2*Math.sin(Math.PI*t)+3.6*t+(i%3));feather(p,a.subtract(root),b.subtract(root),2.65,1.8,palette(i,DARK,PURPLE,MID,PURPLE),i%3==0?RED:i%3==1?RED2:PURPLE);}
        for(int row=0;row<3;row++)for(int i=0;i<12;i++){double t=i/11.0;Vec3 a=(t<=.5?root.lerp(bend,t*2):bend.lerp(wr,t*2-1)).add(s*row*.7,-2+row*.35,-row*2.1);int c=row==0&&i>=2&&i<=8?palette(i,BONE,Hock,MID):palette(i+row,PURPLE,MID,DARK);f(p,a.subtract(root),a.add(s*(2.5+row*.35),3.7,-5.5-row*.9).subtract(root),2.5,1.15,c);}
    }
    private static float smooth(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
    private static float env(float t,float a,float b,float c,float d){return smooth((t-a)/(b-a))*(1-smooth((t-c)/(d-c)));}
    @Override public void setupAnim(Div e,float walk,float amount,float age,float yaw,float pitch){int a=e.action();float t=Mth.clamp((e.actionAge()+Mth.clamp(age-e.tickCount,0,1))/Math.max(1,Div.duration(a)),0,1),wind=env(t,.02F,.28F,.29F,.45F),hit=env(t,.27F,.43F,.48F,.84F),pulse=env(t,.05F,.33F,.56F,.92F);
        body.xRot=body.yRot=0;head.xRot=pitch*.012F;head.yRot=yaw*.012F;tail.yRot=Mth.sin(age*.1F)*.08F;
        boolean open=e.flying()||a==Div.WING||a==Div.FAN||a==Div.FEATHER||a==Div.SCREAM||a==Div.GRAB||a==Div.DIVE;
        for(int i=0;i<2;i++){int s=i==0?-1:1;arms[i].xRot=fore[i].xRot=0;legs[i].xRot=Mth.sin(walk*.6F+i*Mth.PI)*Math.min(amount,1)*.25F;closed[i].visible=!open;spread[i].visible=open;closed[i].zRot=0;spread[i].zRot=s*(e.flying()?Mth.sin(age*.5F)*.32F:(wind-hit)*.28F);}
        switch(a){case Div.CLAW,Div.CURSED_CLAW->{arms[1].xRot=.65F*wind-1.24F*hit;fore[1].xRot=.38F*wind-.2F*hit;body.xRot=.12F*wind-.16F*hit;}
            case Div.DOUBLE->{arms[1].xRot=-1.2F*env(t,.13F,.29F,.32F,.49F);arms[0].xRot=-1.2F*env(t,.42F,.58F,.63F,.85F);}
            case Div.WING->{body.yRot=-.48F*wind+.55F*hit;}
            case Div.GRAB->{for(int i=0;i<2;i++){arms[i].xRot=-1.05F*pulse;fore[i].xRot=.4F*pulse;}}
            case Div.TAIL->{tail.yRot=1.15F*hit;body.yRot=-.8F*wind+.95F*hit;}
            case Div.FEATHER,Div.FAN->{head.xRot=-.14F*pulse;}
            case Div.SCREAM->{head.xRot=-.32F*wind+.23F*hit;}
            case Div.GAZE,Div.GROUND,Div.FATE->{head.xRot=.18F*pulse;}
            case Div.DIVE->{body.xRot=-.48F*env(t,.33F,.47F,.52F,.70F);arms[0].xRot=arms[1].xRot=-.9F*hit;}
            default->{}
        }}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){root.render(pose,out,light,overlay,color);}
}
