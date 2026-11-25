package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.desert.*;
import java.io.*;
public final class LamyaRenderer extends EntityRenderer<LamyaEntity>{
 private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 private static final Mesh M=new Mesh();
 private static final class Mesh{
  final int vertices,faces;final int[] idx,col;final short[][][] frames;
  Mesh(){try(var in=LamyaGeometry.open("mesh.bin.gz")){
   if(in.readInt()!=0x4c414d36)throw new IOException("Lamya mesh magic");
   vertices=in.readInt();faces=in.readInt();int a=in.readInt(),n=in.readInt();
   if(vertices<1000||vertices>300000||faces<1000||faces>150000||a!=LamyaRules.LENGTH.length||n!=9)throw new IOException("Lamya mesh header");
   idx=new int[faces*4];col=new int[faces];for(int i=0;i<idx.length;i++){idx[i]=in.readInt();if(idx[i]<0||idx[i]>=vertices)throw new IOException("Lamya vertex index");}for(int i=0;i<faces;i++)col[i]=in.readInt();
   frames=new short[a][n][vertices*3];for(var act:frames)for(var f:act)for(int i=0;i<f.length;i++)f[i]=in.readShort();
   if(in.read()!=-1)throw new IOException("Trailing Lamya mesh");
  }catch(IOException e){throw new IllegalStateException("Cannot load Lamya V6 mesh",e);}}
  float[] pose(int a,float progress){var f=frames[a];float x=Math.clamp(progress,0,1)*(f.length-1);int i=(int)x,j=Math.min(i+1,f.length-1);float[] r=new float[vertices*3];for(int k=0;k<r.length;k++)r[k]=(f[i][k]+(f[j][k]-f[i][k])*(x-i))/1024F;return r;}
 }
 public LamyaRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=2.1F;}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(LamyaContent.BOSS.get(),LamyaRenderer::new);}
 @Override public ResourceLocation getTextureLocation(LamyaEntity e){return WHITE;}
 @Override public boolean shouldRender(LamyaEntity e,Frustum f,double x,double y,double z){return f.isVisible(e.getBoundingBox().inflate(15,8,15));}
 @Override public void render(LamyaEntity e,float yaw,float partial,PoseStack stack,MultiBufferSource buffer,int light){
  float[] v=M.pose(e.visualAction(),e.visualProgress(partial));stack.pushPose();stack.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));var out=buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));var pose=stack.last();int overlay=e.hurtTime>0?OverlayTexture.pack(0,OverlayTexture.v(true)):OverlayTexture.NO_OVERLAY;
  for(int i=0;i<M.faces;i++){
   int a=M.idx[4*i]*3,b=M.idx[4*i+1]*3,c=M.idx[4*i+2]*3;
   float ux=v[b]-v[a],uy=v[b+1]-v[a+1],uz=v[b+2]-v[a+2],vx=v[c]-v[a],vy=v[c+1]-v[a+1],vz=v[c+2]-v[a+2];
   float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx,len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(len<1e-8)continue;
   int color=M.col[i];int r=(color>>>16)&255,g=(color>>>8)&255,bl=color&255;int packed=r>210&&g>105&&bl<110?LightTexture.FULL_BRIGHT:light;
   for(int k=0;k<4;k++){int p=M.idx[i*4+k]*3;out.addVertex(pose.pose(),v[p],v[p+1],v[p+2]).setColor(color).setUv(k==0||k==3?0:1,k<2?0:1).setOverlay(overlay).setLight(packed).setNormal(pose,nx/len,ny/len,nz/len);}
  }
  stack.popPose();super.render(e,yaw,partial,stack,buffer,light);
 }
}
