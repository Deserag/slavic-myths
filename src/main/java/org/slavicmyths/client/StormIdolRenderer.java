package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.desert.*;
import static org.slavicmyths.client.StormIdolMesh.*;
import static org.slavicmyths.desert.StormIdolRules.*;
import static java.lang.Math.*;

public final class StormIdolRenderer extends EntityRenderer<StormIdolEntity>{
 private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 private static final int[] HAIR_COL={0xffc4cde5,0xff9ba9cf,0xff7889b3,0xffaab8dc};
 private static final int[] WIND_COL={0x755c6f9b,0x95839dc8,0xaba8c9eb,0x707393bd};
 // Scratch storage belongs to the renderer, used sequentially on the render thread.
 private static final int MAX_POINTS=Math.max(28,HAIR.stream().mapToInt(s->(s.points().length-1)*3+1).max().orElse(0));
 private final Vec3[] fists=new Vec3[4];
 private final float[] faceVertices=new float[12],ribbonVertices=new float[12];
 private final double[][] centers=new double[MAX_POINTS][3],points=new double[MAX_POINTS][3];
 private final double[] width=new double[MAX_POINTS],depth=new double[MAX_POINTS];
 private final double[][][] rings=new double[MAX_POINTS][4][3];
 private final double[] charmSin=new double[4],charmCos=new double[4];
 public StormIdolRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.8F;}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(StormIdolContent.BOSS.get(),StormIdolRenderer::new);}
 @Override public ResourceLocation getTextureLocation(StormIdolEntity e){return WHITE;}
 @Override public boolean shouldRender(StormIdolEntity e,Frustum f,double x,double y,double z){return f.isVisible(e.getBoundingBox().inflate(e.action()==IDLE?4:19,7,19));}
 @Override public void render(StormIdolEntity e,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light){
  float time=e.animationTime(partial);double t=time*.05,age=e.age()+partial;
  int a=e.action(),overlay=e.hurtTime>0?OverlayTexture.pack(0,OverlayTexture.v(true)):OverlayTexture.NO_OVERLAY;
  double speed=min(1,e.getDeltaMovement().horizontalDistance()*4),power=a==STORM?envelope(age,4,22,91,118):a==IDLE?0:.55;
  double flow=speed*.8,wind=1+power*1.4,tilt=.045*speed*sin(t*4)+.04*power,headYaw=.035*sin(t*2.5);
  stack.pushPose();stack.mulPose(Axis.YP.rotationDegrees(-yaw));stack.translate(0,bob(time),0);
  var out=buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE));
  // Fixed opaque mesh with local skeletal transforms. Independent fist pivots come from server pose.
  for(int i=0;i<4;i++){fists[i]=e.fistLocal(i,partial);double angle=.13*wind*sin(t*4+i)+flow*.15;charmSin[i]=sin(angle);charmCos[i]=cos(angle);}
  double hs=sin(headYaw),hc=cos(headYaw),ts=sin(tilt),tc=cos(tilt);
  float[] v=faceVertices;
  for(Face face:FACES){
   int bone=face.bone();float[] src=face.vertices();int color=face.color();
   for(int j=0;j<4;j++){
    double x=src[j*3],y=src[j*3+1],z=src[j*3+2];
    if(bone>=FIST&&bone<FIST+4){int i=bone-FIST;double[] c=FIST_CENTERS[i];Vec3 q=fists[i];x=x-c[0]+q.x/SCALE;y=y-c[1]+q.y/SCALE;z=z-c[2]-q.z/SCALE;}
    else if(bone==CLOTH){double u=max(0,min(1,(31-y)/16));x+=u*u*(.6*wind*sin(t*4-u*4+x*.15));z+=u*u*(.8*wind*cos(t*4-u*4+z*.15)+flow);}
    else if(bone>=CHARM){int i=bone-CHARM,s=i<2?-1:1;double px=s*(i%2==0?13:20),py=i%2==0?59:62;double dy=y-py,dz=z;y=py+dy*charmCos[i]-dz*charmSin[i];z=dy*charmSin[i]+dz*charmCos[i];}
    if(bone==HEAD||bone==CROWN||bone>=CHARM){double xx=x*hc+z*hs;z=-x*hs+z*hc;x=xx;}
    if(bone==BODY||bone==HEAD||bone==CROWN||bone>=CHARM){double dy=y-34;y=34+dy*tc-z*ts;z=dy*ts+z*tc;}
    v[3*j]=(float)(x*SCALE);v[3*j+1]=(float)(y*SCALE);v[3*j+2]=(float)(-z*SCALE);
   }
   boolean bright=(color&0xffffff)==CYAN||(color&0xffffff)==EYE;
   quad(stack.last(),out,v,color,bright?LightTexture.FULL_BRIGHT:light,overlay);
  }
  for(Strand strand:HAIR)drawHair(stack.last(),out,strand,t,wind,flow,tilt,headYaw,light,overlay);
  // Only a small, tapered cyclone underneath the robe. No arm tubes or large external gust rings.
  var trans=buffers.getBuffer(RenderType.entityTranslucent(WHITE));
  for(int k=0;k<3;k++){
   int n=28;
   for(int i=0;i<n;i++){double u=i/(n-1.),y=1.5+17*u,r=.45+2.35*pow(u,.85),ang=u*PI*4.4+k*2*PI/3+t*(4+power*2);points[i][0]=r*cos(ang)+.22*sin(y*.2+t);points[i][1]=y;points[i][2]=r*.8*sin(ang);width[i]=.3+1.1*u;depth[i]=.22+.18*u;}
   ribbon(stack.last(),trans,points,width,depth,n,WIND_COL,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
  }
  // Short disconnected crescents follow each gauntlet; never join it to the torso.
  for(int k=0;k<4;k++){Vec3 q=fists[k];int n=10;
   for(int i=0;i<n;i++){double u=i/(n-1.),ang=t*4+k+u*PI*1.15;points[i][0]=q.x/SCALE+5.2*cos(ang);points[i][1]=q.y/SCALE+.8+1.6*u;points[i][2]=-q.z/SCALE+4.7*sin(ang);width[i]=.12+.9*sin(PI*u);depth[i]=.22;}
   ribbon(stack.last(),trans,points,width,depth,n,WIND_COL,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
  }
  stack.popPose();super.render(e,yaw,partial,stack,buffers,light);
 }
 private void drawHair(PoseStack.Pose pose,VertexConsumer out,Strand strand,double t,double wind,double flow,double tilt,double headYaw,int light,int overlay){
  double[][] p=strand.points();int n=p.length;System.arraycopy(p[0],0,centers[0],0,3);
  for(int i=1;i<n;i++){
   double u=i/(n-1.),phase=t*5.2-u*5+strand.seed()*.69;
   double[] d=sub(p[i],p[i-1]);double ax=.16*u*wind*sin(phase)+flow*.22*u,ay=.17*u*wind*cos(phase*.91)+flow*.31*u,az=.1*u*wind*sin(phase*.86);
   double x=d[0]*cos(ay)+d[2]*sin(ay),z=-d[0]*sin(ay)+d[2]*cos(ay),y=d[1];double yy=y*cos(ax)-z*sin(ax),zz=y*sin(ax)+z*cos(ax);
   centers[i][0]=centers[i-1][0]+x*cos(az)-yy*sin(az);centers[i][1]=centers[i-1][1]+x*sin(az)+yy*cos(az);centers[i][2]=centers[i-1][2]+zz;
  }
  // Densify the segment chain without moving the anchored root.
  int m=(n-1)*3+1;double[][] pts=points;double[] w=width,d=depth;
  for(int j=0;j<m;j++){int i=min(n-2,j/3);double u=(j-i*3)/3.;double[] a=centers[max(0,i-1)],b=centers[i],c=centers[i+1],z=centers[min(n-1,i+2)];
   for(int k=0;k<3;k++)pts[j][k]=.5*(2*b[k]+(-a[k]+c[k])*u+(2*a[k]-5*b[k]+4*c[k]-z[k])*u*u+(-a[k]+3*b[k]-3*c[k]+z[k])*u*u*u);
   w[j]=strand.width()[i]*(1-u)+strand.width()[i+1]*u;d[j]=strand.depth()[i]*(1-u)+strand.depth()[i+1]*u;
  }
  double hs=sin(headYaw),hc=cos(headYaw),ts=sin(tilt),tc=cos(tilt);
  for(int j=0;j<m;j++){double[] q=pts[j];double x=q[0]*hc+q[2]*hs,z=-q[0]*hs+q[2]*hc,dy=q[1]-34;q[0]=x;q[1]=34+dy*tc-z*ts;q[2]=dy*ts+z*tc;}
  ribbon(pose,out,pts,w,d,m,HAIR_COL,light,overlay);
 }
 private void ribbon(PoseStack.Pose pose,VertexConsumer out,double[][] p,double[] widths,double[] depths,int n,int[] colors,int light,int overlay){
  float[] vertices=ribbonVertices;
  for(int i=0;i<n;i++){double[] tangent=unit(sub(p[min(n-1,i+1)],p[max(0,i-1)]));double[] u=cross(tangent,new double[]{0,0,1});if(dot(u,u)<.000001)u=cross(tangent,new double[]{0,1,0});u=unit(u);double[] v=cross(tangent,u);
   for(int j=0;j<4;j++){double sx=(j==0||j==3?-1:1)*widths[i]*.5,sy=(j<2?-1:1)*depths[i]*.5;for(int k=0;k<3;k++)rings[i][j][k]=p[i][k]+u[k]*sx+v[k]*sy;}
  }
  for(int i=0;i<n-1;i++)for(int j=0;j<4;j++){
   for(int k=0;k<4;k++){double[] point=rings[i+(k>=2?1:0)][(j+(k==1||k==2?1:0))%4];vertices[k*3]=(float)(point[0]*SCALE);vertices[k*3+1]=(float)(point[1]*SCALE);vertices[k*3+2]=(float)(-point[2]*SCALE);}
   quad(pose,out,vertices,colors[(j+(i%3==0?1:0))%colors.length],light,overlay);
  }
 }
 private static void quad(PoseStack.Pose pose,VertexConsumer out,float[] v,int color,int light,int overlay){
  float ux=v[3]-v[0],uy=v[4]-v[1],uz=v[5]-v[2],vx=v[6]-v[0],vy=v[7]-v[1],vz=v[8]-v[2];
  float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;float len=(float)sqrt(nx*nx+ny*ny+nz*nz);if(len<1e-8)return;
  // Z reflection reverses winding; reverse the vertex order and normal.
  float xx=-nx/len,yy=-ny/len,zz=-nz/len;
  for(int i=3;i>=0;i--)out.addVertex(pose.pose(),v[i*3],v[i*3+1],v[i*3+2]).setColor(color).setUv(i==0||i==3?0:1,i<2?0:1).setOverlay(overlay).setLight(light).setNormal(pose,xx,yy,zz);
 }
}
