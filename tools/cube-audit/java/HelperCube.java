package org.slavicmyths.cubeaudit;
import com.mojang.blaze3d.vertex.*;
/** Frozen helper candidate used by the crossover, diagnostic only. */
public final class HelperCube {
 /** Emit the original six quads in their original order without per-cube face/normal arrays. */
 public static void cube(PoseStack pose,VertexConsumer out,double x,double y,double z,double w,double h,double d,int color){
  var m=pose.last();
  float x0=(float)x,y0=(float)y,z0=(float)z,x1=(float)(x+w),y1=(float)(y+h),z1=(float)(z+d);
  face(m,out,color,0,0,-1,x0,y0,z0,x0,y1,z0,x1,y1,z0,x1,y0,z0);
  face(m,out,color,0,0,1,x1,y0,z1,x1,y1,z1,x0,y1,z1,x0,y0,z1);
  face(m,out,color,-1,0,0,x0,y0,z1,x0,y1,z1,x0,y1,z0,x0,y0,z0);
  face(m,out,color,1,0,0,x1,y0,z0,x1,y1,z0,x1,y1,z1,x1,y0,z1);
  face(m,out,color,0,1,0,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0);
  face(m,out,color,0,-1,0,x0,y0,z1,x0,y0,z0,x1,y0,z0,x1,y0,z1);
 }
 private static void face(PoseStack.Pose m,VertexConsumer out,int color,float nx,float ny,float nz,
   float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz){
  vertex(m,out,color,ax,ay,az,0,1,nx,ny,nz);
  vertex(m,out,color,bx,by,bz,0,0,nx,ny,nz);
  vertex(m,out,color,cx,cy,cz,1,0,nx,ny,nz);
  vertex(m,out,color,dx,dy,dz,1,1,nx,ny,nz);
 }
 private static void vertex(PoseStack.Pose m,VertexConsumer out,int color,float x,float y,float z,float u,float v,float nx,float ny,float nz){
  out.addVertex(m,x,y,z).setColor(color).setUv(u,v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(m,nx,ny,nz);
 }

}
