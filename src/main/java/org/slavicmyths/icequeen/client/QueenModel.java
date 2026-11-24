package org.slavicmyths.icequeen.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.slavicmyths.client.FolkModelGeometry;
import org.slavicmyths.icequeen.*;

/** Native cuboids in 1/16 block units, matching the two technical prototype silhouettes. */
public final class QueenModel<T extends LivingEntity> extends EntityModel<T>{
 private static final int NAVY=0x202e58,DARK=0x293b70,BLUE=0x3d598c,MID=0x557caf,LIGHT=0x85abd4,TRIM=0xc0d0e5,FUR=0xe2e4ed,FURS=0xb9c3d8,HAIR=0xcbd5e8,HAIRD=0xaabbd4,HAIRL=0xe6eaf3,SKIN=0xe4dce2,SHADOW=0x938fa8,EYE=0x79f7ff,BROW=0x273653,ICE=0x63c9ee,ICE2=0xa4e8fa,ICE3=0x398bc5,CORE=0xd0ffff,METAL=0x6082a7;
 private record Cube(ModelPart part,Quaternionf rotation,int rgb){}
 private final FolkModelGeometry geo=new FolkModelGeometry();
 private final List<Cube> first=new ArrayList<>(),second=new ArrayList<>(),shield=new ArrayList<>();
 private final List<Cube>[][][] arms;
 private boolean transformed,copy; private int action,frame,shieldCount,reactive;private float age,impact;
 @SuppressWarnings("unchecked")
 public QueenModel(){arms=(List<Cube>[][][])new List<?>[2][12][9];body(first,false);body(second,true);
  for(int p=0;p<2;p++)for(int a=0;a<12;a++)for(int f=0;f<9;f++){var l=new ArrayList<Cube>();arms[p][a][f]=l;limbs(l,p==1,a,f/8F);}
  for(int j=0;j<5;j++)box(shield,0,-12,23+(j-2)*1.4F,new float[]{2,4,5,5,4}[j],.55F,1.5F,j%2==0?ICE:ICE2);
 }
 private void box(List<Cube> l,float x,float depth,float z,float w,float d,float h,int c){add(l,x,depth,z,w,d,h,c,null);}
 private void add(List<Cube> l,float x,float depth,float z,float w,float d,float h,int c,Quaternionf rotation){
  ModelPart part=geo.part(128,128);geo.box(part,0,0,-w/2,-h/2,-d/2,w,h,d);part.setPos(x,-z,depth);l.add(new Cube(part,rotation,c));
 }
 private void seg(List<Cube> l,float x,float y,float z,float x2,float y2,float z2,float w,float d,int c){
  float dx=x2-x,dy=z-z2,dz=y2-y,len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);if(len<.01F)return;
  Quaternionf q=new Quaternionf().rotationTo(0,1,0,dx/len,dy/len,dz/len);
  add(l,(x+x2)/2,(y+y2)/2,(z+z2)/2,w,d,len,c,q);
 }
 private void crystal(List<Cube> l,float x,float y,float z,float x2,float y2,float z2,float width){
  float[] f={.78F,1,.78F,.49F,.25F};int[] colors={ICE3,ICE,ICE,ICE2,CORE};
  for(int i=0;i<5;i++){float a=i/5F,b=(i+1)/5F;seg(l,Mth.lerp(a,x,x2),Mth.lerp(a,y,y2),Mth.lerp(a,z,z2),Mth.lerp(b,x,x2),Mth.lerp(b,y,y2),Mth.lerp(b,z,z2),width*f[i],width*f[i],colors[i]);}
 }
 private void body(List<Cube> l,boolean p){
  for(int i=0;i<10;i++){float z=1+i*1.9F,t=i/9F,w=17-10.5F*t+(p?1.7F*(1-t):0),d=10-5.5F*t+(p?.4F:0);
   box(l,0,.25F,z,w,d,2,i%3==0?DARK:NAVY);box(l,0,-d/2-.03F,z,Math.max(2.2F,5.3F-3.1F*t),.55F,2,i%2==0?MID:BLUE);
   for(int s:new int[]{-1,1}){box(l,s*(w/2-1),-d/2+.1F,z,1.3F,.85F,2,p?LIGHT:TRIM);box(l,s*(w/2-.42F),.25F,z,.9F,d-.45F,2,DARK);}
   box(l,0,d/2+.15F,z,w-.55F,.65F,2,DARK);if(i==0)box(l,0,.25F,.45F,w+.5F,d+.5F,.9F,p?ICE3:TRIM);
  }
  for(int s:new int[]{-1,1}){
   for(int i=0;i<8;i++){float z=2.7F+i*2.05F,t=i/8F;box(l,s*(4.8F-2.45F*t),-(5-2.65F*t)-.42F,z,.72F,.48F,2.2F,TRIM);}
   if(!p)for(int i=0;i<8;i++){float z=1+i*2.6F,t=i/8F,w=8.5F-4.5F*t,d=5.65F-2.9F*t;box(l,s*w*.55F,d,z,w,1,2.7F,NAVY);box(l,s*w,d-.12F,z,.65F,1.1F,2.7F,TRIM);}
  }
  box(l,0,0,19.9F,6.9F,4.7F,2.7F,DARK);box(l,0,-.05F,19.7F,7.05F,5.05F,1.05F,p?TRIM:LIGHT);
  box(l,0,0,23.1F,8.1F,5.2F,5.3F,DARK);box(l,0,-2.55F,23.7F,6.8F,.85F,3.9F,p?BLUE:NAVY);
  for(int s:new int[]{-1,1})box(l,s*2.9F,-2.8F,23.1F,.68F,.5F,5.2F,p?TRIM:BLUE);
  box(l,0,0,27,2.7F,2.6F,2.8F,SKIN);box(l,0,-2.95F,25.5F,2,1,1.8F,METAL);box(l,0,-3.51F,25.5F,1.1F,.5F,1.15F,EYE);
  box(l,0,0,32,7.6F,6.8F,7.6F,SKIN);box(l,0,.42F,35.3F,7.9F,7.05F,1.3F,HAIRD);
  for(int s:new int[]{-1,1}){box(l,s*1.88F,-3.51F,32.4F,2.1F,.26F,1.43F,BROW);box(l,s*1.88F,-3.66F,32.3F,1.48F,.18F,.96F,EYE);box(l,s*1.6F,-3.77F,32.44F,.51F,.12F,.72F,CORE);box(l,s*1.9F,-3.58F,33.37F,2.2F,.2F,.47F,BROW);}
  box(l,0,-3.56F,30.15F,1.25F,.19F,.24F,SHADOW);
  box(l,0,.8F,35.9F,8.6F,6.8F,1.65F,HAIRL);box(l,0,2.65F,32.7F,8.5F,2.35F,6.8F,HAIRD);
  for(int j=0;j<6;j++){float x=-3.65F+j*1.43F,len=5+(j==2||j==3?3.4F:0)+(j%2)*1.2F-(p?1.2F:0);box(l,x,3.45F+(j%2)*.38F,34-len/2,1.55F,1.5F,len,j%2==0?HAIRL:HAIR);}
  for(int s:new int[]{-1,1}){box(l,s*3.85F,-.05F,32,1.35F,5.4F,7.1F,HAIR);box(l,s*3.55F,-3.44F,29.8F,1.16F,1.2F,p?4.3F:6,HAIRL);
   for(int j=0;j<3;j++)box(l,s*(3.5F+j*.48F),1.6F+j*.53F,29.5F-j*.85F,1.55F,2,2.8F,j%2==0?HAIRD:HAIR);}
  for(int j=0;j<5;j++){float h=j==0?2.5F:1.15F+(4-j)*.27F;box(l,-3.1F+j*1.5F,-3.58F,35.3F-h/2,1.65F,.85F,h,j%2==0?HAIR:HAIRL);}
  if(!p){
   for(int j=0;j<9;j++)box(l,-2.55F+.38F*Mth.sin(j*1.65F),-3.7F-.16F*Mth.sin(j*.7F),29-j*1.33F,1.7F-j/9F*.55F,1.5F,1.65F,j%2==0?HAIR:HAIRL);
   box(l,-2.55F,-3.7F,17.5F,.85F,1,1.05F,LIGHT);
   for(int s:new int[]{-1,1}){for(int j=0;j<4;j++){box(l,s*(2.15F+j*1.12F),.05F,26-.23F*j,1.8F,5.2F-.32F*j,2.45F-.13F*j,j%2==0?FURS:FUR);box(l,s*(2.1F+j*1.1F),-2.48F,25.2F-.35F*j,1.45F,1.3F,1.75F,FUR);}for(int j=0;j<3;j++)box(l,s*(1.4F+j*1.55F),2.9F,24.7F-j*.35F,2,1.1F,2.5F,FUR);}
   for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(x==0||z==0||Math.abs(x)==1&&Math.abs(z)==1)box(l,x*.72F,5.85F,10+z*.72F,.72F,.18F,.72F,x==0&&z==0?CORE:LIGHT);
  }else{
   for(int s:new int[]{-1,1}){
    box(l,s*3.45F,0,25.5F,3.6F,4.5F,2.2F,TRIM);box(l,s*2.45F,-3.15F,23.5F,2.6F,.95F,2.9F,MID);
    for(int j=0;j<3;j++){crystal(l,s*(3.4F+j*.75F),-.8F+j*.8F,25.3F,s*(7.1F+j*.78F),-.8F+j*.8F,29+j*.6F,2.5F);float z=18-j*4.6F,x=3.2F+j*1.65F,y=-2.6F-j*.9F;
     seg(l,s*x,y,z,s*(x+1.8F),y-.6F,z-4.8F,2.45F,.85F,ICE3);seg(l,s*x,y-.5F,z,s*(x+1.8F),y-1.1F,z-4.8F,1.65F,.38F,ICE);crystal(l,s*(x+1.8F),y-.65F,z-3.7F,s*(x+2.1F),y-.9F,z-6,1.55F);
     z=18-j*5;x=3.3F+j*1.55F;y=3.2F+j*.86F;
     seg(l,s*x,y,z,s*(x+1.8F),y+.95F,z-5.5F,2.85F,1.15F,ICE3);seg(l,s*x,y+.65F,z,s*(x+1.8F),y+1.55F,z-5.5F,2.1F,.55F,ICE2);seg(l,s*(x+1.4F),.3F,z,s*(x+3),.3F,z-5.6F,1.4F,2.7F,ICE);
    }
    seg(l,s*3,2.8F,18,s*6.8F,4.9F,3,2.5F,.85F,ICE3);seg(l,s*3,3.3F,18,s*6.8F,5.2F,3,1.25F,.5F,ICE2);
   }
   crystal(l,0,-4.8F,2,0,-4.8F,11,2.8F);box(l,0,-3.4F,19,1.7F,.8F,1.6F,ICE2);
   for(int j=1;j<15;j++){float a=(float)(-.06+(Math.PI+.12)*(j-1)/14),b=(float)(-.06+(Math.PI+.12)*j/14);seg(l,6.6F*Mth.cos(a),3.8F,33.8F+6.6F*Mth.sin(a),6.6F*Mth.cos(b),3.8F,33.8F+6.6F*Mth.sin(b),.72F,.66F,METAL);}
   for(float a:new float[]{0,.64F,1.57F,2.50F,Mth.PI})crystal(l,6.5F*Mth.cos(a),3.8F,33.8F+6.5F*Mth.sin(a),9*Mth.cos(a),3.8F,33.8F+9*Mth.sin(a),2.2F);
  }
  for(int s:new int[]{-1,1}){box(l,0,s*3.65F,36.6F,8.8F,1,1,p?METAL:NAVY);box(l,s*4,0,36.6F,1,6.8F,1,p?METAL:NAVY);}
  for(float[] v:new float[][]{{-3.8F,-3.4F,2.4F},{0,-3.65F,3.3F},{3.8F,-3.4F,2.4F},{-3.8F,2.8F,1.9F},{0,3.6F,2.4F},{3.8F,2.8F,1.9F}}){float x=v[0],y=v[1],h=v[2];box(l,x,y,37+h/2,1.15F,1.15F,h,ICE3);box(l,x,y-.12F,37+h*.72F,.76F,1,h*.51F,ICE2);}
 }
 private static float[] mix(float[] a,float[] b,float t){return new float[]{Mth.lerp(t,a[0],b[0]),Mth.lerp(t,a[1],b[1]),Mth.lerp(t,a[2],b[2])};}
 private void limbs(List<Cube> l,boolean p,int a,float t){
  float[][] idle={{-5.6F,-.9F,21.4F},{5.6F,.1F,21.4F},{-7.15F,-3.05F,19.6F},{6.8F,-.3F,17.4F}};
  float[][] dst=idle;float[] direction={0,0,1};
  if(a==IceQueen.SHOT||a==IceQueen.VOLLEY){dst=new float[][]{{-5.8F,-2.2F,22.5F},{5.7F,.1F,21.8F},{-5.8F,-6.5F,23.5F},{6.5F,-.8F,18.6F}};direction=new float[]{0,-1,.1F};}
  else if(a==IceQueen.THRUST||a==IceQueen.COMBO){dst=new float[][]{{-5.5F,-3,24},{6,-.1F,22},{-5.6F,-7.3F,24},{6.8F,-2,19}};direction=new float[]{0,-1,0};}
  else if(a==IceQueen.SWEEP||a==IceQueen.SPIN){dst=new float[][]{{-7,-2,24},{6.7F,-.8F,23},{-10,-2.6F,24},{7.5F,-2.4F,21}};direction=new float[]{-1,-.3F,.1F};}
  else if(a==IceQueen.SUMMON||a==IceQueen.TRANSFORM){dst=new float[][]{{-6,-1,22},{6.7F,-1,25.5F},{-7,-1.5F,20},{8,-3,27.6F}};}
  else if(a==IceQueen.STORM||a==IceQueen.GUARD||a==IceQueen.YIELD)dst=new float[][]{{-6,-1.5F,22},{6,-2,24},{-7,-3,20},{7,-5,25}};
  float[][] v=new float[4][];for(int i=0;i<4;i++)v[i]=mix(idle[i],dst[i],t);
  for(int i=0;i<2;i++){float[] e=v[i],h=v[i+2];float dx=h[0]-e[0],dy=h[1]-e[1],dz=h[2]-e[2],len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);dx/=len;dy/=len;dz/=len;
   seg(l,i==0?-4.5F:4.5F,0,25,e[0],e[1],e[2],2.8F,3,DARK);seg(l,e[0],e[1],e[2],h[0],h[1],h[2],3.4F,3.3F,p?BLUE:DARK);
   seg(l,h[0]-dx*.95F,h[1]-dy*.95F,h[2]-dz*.95F,h[0]-dx*.1F,h[1]-dy*.1F,h[2]-dz*.1F,3.45F,3.4F,p?TRIM:LIGHT);seg(l,h[0]-dx*.2F,h[1]-dy*.2F,h[2]-dz*.2F,h[0]+dx*1.22F,h[1]+dy*1.22F,h[2]+dz*1.22F,2.2F,2.4F,SKIN);
  }
  float[] d=mix(new float[]{0,0,1},direction,t);float len=(float)Math.sqrt(d[0]*d[0]+d[1]*d[1]+d[2]*d[2]);for(int i=0;i<3;i++)d[i]/=len;
  float[] h=v[2];float back=17-10*t*(1-direction[2]),front=p?13:11;
  float x=h[0],y=h[1],z=h[2];seg(l,x-d[0]*back,y-d[1]*back,z-d[2]*back,x+d[0]*front,y+d[1]*front,z+d[2]*front,.93F,.93F,NAVY);
  for(float k:new float[]{-back+.5F,-back*.36F,front*.35F,front-.5F})seg(l,x+d[0]*(k-.45F),y+d[1]*(k-.45F),z+d[2]*(k-.45F),x+d[0]*(k+.45F),y+d[1]*(k+.45F),z+d[2]*(k+.45F),1.35F,1.35F,METAL);
  float cx=x+d[0]*(front+.7F),cy=y+d[1]*(front+.7F),cz=z+d[2]*(front+.7F);
  box(l,cx,cy,cz,2.1F,2.1F,2.1F,EYE);box(l,cx,cy-.72F,cz,1.2F,1,1.2F,CORE);
  float ex=d[2],ez=-d[0],el=(float)Math.sqrt(ex*ex+ez*ez);if(el<.05F){ex=1;ez=0;}else{ex/=el;ez/=el;}
  for(int s:new int[]{-1,1}){seg(l,cx+s*ex*.7F,cy-d[1]*.7F,cz+s*ez*.7F,cx+s*ex*2.1F,cy,cz+s*ez*2.1F,.7F,.7F,METAL);crystal(l,cx+s*ex*1.5F,cy,cz+s*ez*1.5F,cx+s*ex*3+d[0]*1.25F,cy+d[1]*1.25F,cz+s*ez*3+d[2]*1.25F,1.25F);}
  crystal(l,cx+d[0]*.8F,cy+d[1]*.8F,cz+d[2]*.8F,cx+d[0]*(p?5.3F:2.5F),cy+d[1]*(p?5.3F:2.5F),cz+d[2]*(p?5.3F:2.5F),p?2:1.3F);
 }
 @Override public void setupAnim(T e,float walk,float amount,float age,float yaw,float pitch){
  this.age=age;copy=e instanceof QueenMinion;transformed=copy||e instanceof IceQueen q&&q.phase()==2;
  if(e instanceof IceQueen q){action=q.action();frame=q.frame();shieldCount=q.shields();reactive=q.reactive();impact=q.impactYaw()-q.getYRot();}
  else{action=e instanceof QueenMinion m&&m.kind()==QueenContent.Kind.RANGED_COPY?IceQueen.SHOT:IceQueen.THRUST;frame=e.tickCount%45;shieldCount=reactive=0;}
 }
 private void draw(List<Cube> l,PoseStack pose,VertexConsumer out,int light,int overlay,int color,boolean icy){
  for(Cube c:l){int rgb=c.rgb;if(icy){int r=(rgb>>16)&255,g=(rgb>>8)&255,b=rgb&255;rgb=((r+110)/2<<16)|((g+215)/2<<8)|((b+255)/2);}
   int tint=(color&0xff000000)|rgb;int lit=c.rgb==EYE||c.rgb==CORE?15728880:light;
   if(c.rotation==null)c.part.render(pose,out,lit,overlay,tint);else{pose.pushPose();pose.translate(c.part.x/16,c.part.y/16,c.part.z/16);pose.mulPose(c.rotation);pose.translate(-c.part.x/16,-c.part.y/16,-c.part.z/16);c.part.render(pose,out,lit,overlay,tint);pose.popPose();}
  }
 }
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
  pose.pushPose();pose.translate(0,1.5,0);
  if(action==IceQueen.SPIN)pose.mulPose(Axis.YP.rotationDegrees(frame*14F));
  if(action==IceQueen.YIELD)pose.mulPose(Axis.XP.rotationDegrees(5*Mth.sin(frame*Mth.PI/50F)));
  if(transformed)pose.translate(0,-.035F*Mth.sin(age*.1F),0);
  draw(transformed?second:first,pose,out,light,overlay,color,copy);
  float power=action==IceQueen.IDLE?0:Math.min(1,Math.min(frame/12F,Math.max(0,(25-frame)/8F)));
  if(action==IceQueen.COMBO)power=Math.max(0,Mth.sin(frame*Mth.PI/10F));
  draw(arms[transformed?1:0][Math.clamp(action,0,11)][Math.clamp(Math.round(power*8),0,8)],pose,out,light,overlay,color,copy);
  for(int i=0;i<shieldCount;i++){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(i*72+age*3));draw(shield,pose,out,15728880,overlay,color,false);pose.popPose();}
  if(reactive>0){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-impact));draw(shield,pose,out,15728880,overlay,color,false);pose.popPose();}
  pose.popPose();
 }
}
