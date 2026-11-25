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
 private static final class MeshHolder{static final Mesh INSTANCE=new Mesh();}
 private float[] vertices,previous;
 private static final class Mesh{
  final int vertices,faces;final int[] idx,col;final boolean[] emissive;final short[][][] frames;
  Mesh(){try(var in=LamyaGeometry.open("mesh.bin.gz")){
   if(in.readInt()!=0x4c414d36)throw new IOException("Lamya mesh magic");
   vertices=in.readInt();faces=in.readInt();int a=in.readInt(),n=in.readInt();
   if(vertices<1000||vertices>300000||faces<1000||faces>150000||a!=LamyaRules.LENGTH.length||n!=9)throw new IOException("Lamya mesh header");
   idx=new int[faces*4];col=new int[faces];emissive=new boolean[faces];for(int i=0;i<idx.length;i++){int index=in.readInt();if(index<0||index>=vertices)throw new IOException("Lamya vertex index");idx[i]=index*3;}for(int i=0;i<faces;i++){int color=col[i]=in.readInt();emissive[i]=((color>>>16)&255)>210&&((color>>>8)&255)>105&&(color&255)<110;}
   frames=new short[a][n][vertices*3];for(var act:frames)for(var f:act)for(int i=0;i<f.length;i++)f[i]=in.readShort();
   if(in.read()!=-1)throw new IOException("Trailing Lamya mesh");
  }catch(IOException e){throw new IllegalStateException("Cannot load Lamya V6 mesh",e);}}
  void pose(int a,float progress,float[] r){var f=frames[a];float x=Math.clamp(progress,0,1)*(f.length-1);int i=(int)x,j=Math.min(i+1,f.length-1);float t=x-i;for(int k=0;k<r.length;k++)r[k]=(f[i][k]+(f[j][k]-f[i][k])*t)/1024F;}
 }
 public LamyaRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=2.1F;}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(LamyaContent.BOSS.get(),LamyaRenderer::new);}
 @Override public ResourceLocation getTextureLocation(LamyaEntity e){return WHITE;}
 @Override public boolean shouldRender(LamyaEntity e,Frustum f,double x,double y,double z){return f.isVisible(e.getBoundingBox().inflate(15,8,15));}
 @Override public void render(LamyaEntity e,float yaw,float partial,PoseStack stack,MultiBufferSource buffer,int light){
  Mesh M=MeshHolder.INSTANCE;if(vertices==null){vertices=new float[M.vertices*3];previous=new float[M.vertices*3];}
  float[] v=vertices;M.pose(e.visualAction(),e.visualProgress(partial),v);float blend=e.blend(partial);
  if(blend<1){M.pose(e.previousAction(),e.previousProgress(),previous);for(int k=0;k<v.length;k++)v[k]=previous[k]+(v[k]-previous[k])*blend;}
  stack.pushPose();stack.mulPose(Axis.YP.rotationDegrees(-yaw));var out=buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));var pose=stack.last();int overlay=e.hurtTime>0?OverlayTexture.pack(0,OverlayTexture.v(true)):OverlayTexture.NO_OVERLAY;
  for(int i=0;i<M.faces;i++){
   int a=M.idx[4*i],b=M.idx[4*i+1],c=M.idx[4*i+2];
   float ux=v[b]-v[a],uy=v[b+1]-v[a+1],uz=v[b+2]-v[a+2],vx=v[c]-v[a],vy=v[c+1]-v[a+1],vz=v[c+2]-v[a+2];
   float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx,len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(len<1e-8)continue;
   nx/=len;ny/=len;nz/=len;
   int color=M.col[i],packed=M.emissive[i]?LightTexture.FULL_BRIGHT:light;
   for(int k=0;k<4;k++){int p=M.idx[i*4+k];out.addVertex(pose.pose(),v[p],v[p+1],v[p+2]).setColor(color).setUv(k==0||k==3?0:1,k<2?0:1).setOverlay(overlay).setLight(packed).setNormal(pose,nx,ny,nz);}
  }
  stack.popPose();super.render(e,yaw,partial,stack,buffer,light);
 }
}
