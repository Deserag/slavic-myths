package org.slavicmyths.cubeaudit;
import com.mojang.blaze3d.vertex.*;
/** Frozen main fb2994e9 implementation; test only. */
public final class ReferenceCube {
 public static void cube(PoseStack pose,VertexConsumer out,double x,double y,double z,double w,double h,double d,int color){var m=pose.last();float x0=(float)x,y0=(float)y,z0=(float)z,x1=(float)(x+w),y1=(float)(y+h),z1=(float)(z+d);float[][][] faces={{{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y0,z0}},{{x1,y0,z1},{x1,y1,z1},{x0,y1,z1},{x0,y0,z1}},{{x0,y0,z1},{x0,y1,z1},{x0,y1,z0},{x0,y0,z0}},{{x1,y0,z0},{x1,y1,z0},{x1,y1,z1},{x1,y0,z1}},{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}},{{x0,y0,z1},{x0,y0,z0},{x1,y0,z0},{x1,y0,z1}}};float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};for(int f=0;f<6;f++)for(int i=0;i<4;i++){var v=faces[f][i];out.addVertex(m,v[0],v[1],v[2]).setColor(color).setUv(i==0||i==1?0:1,i==0||i==3?1:0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(m,normals[f][0],normals[f][1],normals[f][2]);}}

}
