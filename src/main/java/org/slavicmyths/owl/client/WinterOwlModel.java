package org.slavicmyths.owl.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.client.FolkModelGeometry;
import org.slavicmyths.owl.WinterOwl;
public final class WinterOwlModel extends EntityModel<WinterOwl>{
 private final FolkModelGeometry g=new FolkModelGeometry();
 private ModelPart part(ModelPart parent,float x,float y,float z){ModelPart p=g.part(256,256);p.setPos(x,y,z);if(parent!=null)g.attach(parent,p);return p;}
 private void box(ModelPart p,int mat,float x,float y,float z,float w,float h,float d){for(float i=0;i<w;i+=15)for(float k=0;k<h;k+=15)for(float l=0;l<d;l+=15)g.box(p,mat%4*64,mat/4*64,x+i,y+k,z+l,Math.min(15,w-i),Math.min(15,h-k),Math.min(15,d-l));}
 private final ModelPart root=part(null,0,24,0),body=part(root,0,-29,0),head=part(body,0,-24,-4),beak=part(head,0,3,-13),tail=part(body,0,9,12);
 private final ModelPart[] wing=new ModelPart[2],tip=new ModelPart[2],leg=new ModelPart[2];
 public WinterOwlModel(){
 box(body,0,-15,-22,-12,30,40,26);box(body,1,-12,-18,-15,24,33,9);
 for(int row=0;row<6;row++)for(int col=-2;col<=2;col++)box(body,row>=2&&Math.abs(col)<2?12:(row+col+4)%3,col*5.3F-2.6F,-16+row*5,-16-(2-Math.abs(col))*.75F,5.2F,7,3);
 box(head,0,-14,-12,-12,28,24,25);box(head,1,-12,-14,-10,24,6,22);
 for(int s:new int[]{-1,1}){float cx=s*7;
 for(int row=-2;row<=2;row++)for(int col=-2;col<=2;col++)if(row*row+col*col<=6)box(head,Math.abs(row)+Math.abs(col)>=3?2:1,cx+col*3-1.5F,row*3-2,-14,3,3,3);
 box(head,3,cx-4,-5,-16,8,7,2);box(head,5,cx-2.7F,-4,-18.1F,5.4F,4.5F,2);box(head,6,cx-1,-3,-20.2F,2,2,1);box(head,2,cx-6,-8,-17,11,3,3);
 for(int n=0;n<5;n++){box(head,7,s*(12+n*4)-(s<0?4:0),-12-n*2,1,4,4,4);box(head,2,s*(12+n*4)-(s<0?4:0),-13-n*2,1,4,1,4);}
 for(int n=0;n<3;n++)box(head,7,s*(21+n*2)-(s<0?3:0),-19-n*3,1,3,4,3);
 box(head,8,s*27-1,-12,1,2,6,2);box(head,9,s*27-2,-6,0,4,4,4);}
 box(beak,3,-3,-3,-7,6,6,7);box(beak,3,-2,3,-6,4,4,4);
 for(int i=0;i<2;i++){int s=i==0?-1:1;wing[i]=part(body,s*13,-17,1);tip[i]=part(wing[i],s*36,0,0);
 box(wing[i],0,s<0?-36:0,-4,-8,36,9,22);
 for(int k=0;k<8;k++){box(wing[i],k%3,s*(k*4.4F)-(s<0?5:0),0,3,5,4,23+k*1.5F);box(wing[i],k%2==0?2:1,s*(k*4.4F)-(s<0?5:0),-5,2,5,3,18);}
 box(tip[i],0,s<0?-25:0,-3,-4,25,7,15);
 for(int k=0;k<8;k++){box(tip[i],k%3,s*k*6.1F-(s<0?6.5F:0),0,0,6.5F,3,32-k*2.5F);box(tip[i],2,s*k*6.1F-(s<0?6.5F:0),-3,0,6.5F,3,14);if(k%2==0){float rx=s*k*6.1F-(s<0?3:0);box(tip[i],10,rx,-3.2F,4,1.5F,.25F,10);box(tip[i],10,rx-1.5F,-3.25F,8,4.5F,.25F,1.5F);}}
 leg[i]=part(root,s*8,-13,-2);box(leg[i],1,-5,0,-5,10,9,10);box(leg[i],4,-4,8,-4,8,5,8);for(int k=-1;k<=1;k++){box(leg[i],4,k*3-1.3F,10,-8,2.6F,3,7);box(leg[i],3,k*3-1.3F,10.5F,-10,2.6F,2.5F,3);}}
 for(int k=-3;k<=3;k++){ModelPart v=part(tail,k*3,0,0);v.yRot=k*.12F;box(v,Math.abs(k)%3,-3,0,0,6,4,25-Math.abs(k)*2);}
 for(int i=-3;i<=3;i++){box(body,7,i*4-2,-12-Math.abs(i),-19,4,2,2);if(i!=0)box(body,8,i*4-1,-9,-19,2,7,2);}
 box(body,7,-5,-10,-21,10,10,2);box(body,10,-1,-8,-23.1F,2,6,.4F);box(body,10,-4,-5,-23.1F,8,1,.4F);
 }
 public ModelPart trophyHead(){return head;}
 @Override public void setupAnim(WinterOwl e,float walk,float amount,float age,float yaw,float pitch){
 int a=e.action();float t=e.frame()+age-e.tickCount;boolean ground=a==WinterOwl.GROUND||a==WinterOwl.LAND&&e.onGround();float fold=ground?1F:0F;
 if(a==WinterOwl.DIVE||a==WinterOwl.MULTI_DIVE||a==WinterOwl.GRAB){float cycle=a==WinterOwl.MULTI_DIVE?t%42:t;if(cycle>14&&cycle<33)fold=.75F;}
 if(a==WinterOwl.LAND)fold=Math.max(fold,Mth.clamp(t/85F,0,.9F));if(a==WinterOwl.TAKEOFF)fold=Math.max(0,1-t/22F);
 float frequency=a==WinterOwl.TAKEOFF?.42F:a==WinterOwl.GUST?.36F:.19F;float flap=ground?0:Mth.sin(age*frequency)*.30F;if(a==WinterOwl.ORBIT||a==WinterOwl.TRAIL||a==WinterOwl.ICICLES)flap*=.42F;
 float charge=Mth.clamp(t/18F,0,1)*Mth.clamp((44-t)/12F,0,1);
 if(a==WinterOwl.GUST)flap+=Mth.sin(t*.27F)*charge*.55F;
 if(a==WinterOwl.FEATHERS)flap+=Mth.sin(t*.52F)*.2F;
 if(a==WinterOwl.SUMMON||a==WinterOwl.AWAKEN)flap-=charge*.32F;
 root.xRot=0;root.zRot=ground?0:e.bank();body.xRot=ground?0:.15F;if(a==WinterOwl.DIVE||a==WinterOwl.MULTI_DIVE)body.xRot=.48F*(1-fold*.4F);
 if(a==WinterOwl.SCREAM||a==WinterOwl.AWAKEN)body.xRot=-charge*.22F;
 if(a==WinterOwl.TORNADO||a==WinterOwl.BLIZZARD)root.zRot+=Mth.sin(age*.13F)*.15F;
 head.xRot=Mth.clamp(pitch,-35,35)*Mth.DEG_TO_RAD*.65F-body.xRot*.65F;head.yRot=Mth.clamp(yaw,-55,55)*Mth.DEG_TO_RAD;
 beak.xRot=(a==WinterOwl.SCREAM||a==WinterOwl.AWAKEN||a==WinterOwl.SUMMON)&&t>10&&t<39?.45F:0;tail.xRot=ground?-.95F:.13F+Mth.sin(age*.17F)*.14F;
 for(int i=0;i<2;i++){int s=i==0?-1:1;wing[i].zRot=s*(fold*.48F+flap);wing[i].yRot=s*(-fold*1.10F);wing[i].xRot=0;tip[i].zRot=s*(fold*.14F+flap*.5F);tip[i].yRot=s*(-fold*.55F);leg[i].xRot=ground?Mth.cos(walk*.6F+i*Mth.PI)*amount*.45F:-.75F;if(a==WinterOwl.GRAB||a==WinterOwl.DIVE||a==WinterOwl.LAND)leg[i].xRot=.2F;}
 }
 @Override public void renderToBuffer(PoseStack p,VertexConsumer c,int light,int overlay,int color){root.render(p,c,light,overlay,color);}
}