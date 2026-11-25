package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.desert.*;
public final class IndrikRenderer extends EntityRenderer<IndrikEntity> {
 private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 private static final IndrikGeometry GEO=IndrikGeometry.INSTANCE;
 private final float[] bonePose=new float[GEO.groups.length*12],cubeVertices=new float[24];
 public IndrikRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=2.4F;}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(IndrikContent.BOSS.get(),IndrikRenderer::new);}
 @Override public ResourceLocation getTextureLocation(IndrikEntity e){return WHITE;}
 @Override public boolean shouldRender(IndrikEntity e,Frustum f,double x,double y,double z){return f.isVisible(e.getBoundingBox().inflate(9,7,9));}
 @Override public void render(IndrikEntity e,float yaw,float partial,PoseStack stack,MultiBufferSource buffer,int light){
  stack.pushPose();stack.mulPose(Axis.YP.rotationDegrees(-yaw));int a=e.visualAction();float progress=e.visualProgress(partial);float[] pose=bonePose,v=cubeVertices;GEO.sampleInto(a,progress,pose);
  var out=buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));int overlay=e.hurtTime>0?OverlayTexture.pack(0,OverlayTexture.v(true)):OverlayTexture.NO_OVERLAY;
  for(int i=0;i<GEO.count;i++){GEO.cube(i,pose,v);if(!GEO.visible(i,a,progress,v))continue;int packed=(GEO.flags[i]&1)!=0?LightTexture.FULL_BRIGHT:light;
   float cx=0,cy=0,cz=0;for(int k=0;k<8;k++){cx+=v[3*k]/8;cy+=v[3*k+1]/8;cz+=v[3*k+2]/8;}
   for(int j=0;j<6;j++)if((GEO.mask[i]&(1<<j))!=0)face(stack.last(),out,v,IndrikGeometry.FACES[j],cx,cy,cz,GEO.colors[i],packed,overlay);
  }stack.popPose();super.render(e,yaw,partial,stack,buffer,light);
 }
 private static void face(PoseStack.Pose pose,VertexConsumer out,float[] v,int[] f,float cx,float cy,float cz,int color,int light,int overlay){
  int a=3*f[0],b=3*f[1],c=3*f[2];float ux=v[b]-v[a],uy=v[b+1]-v[a+1],uz=v[b+2]-v[a+2],vx=v[c]-v[a],vy=v[c+1]-v[a+1],vz=v[c+2]-v[a+2];float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;boolean reverse=nx*(v[a]-cx)+ny*(v[a+1]-cy)+nz*(v[a+2]-cz)<0;float len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(len<1e-7)return;float scale=(reverse?-1:1)/len;nx*=scale;ny*=scale;nz*=scale;
  for(int k=0;k<4;k++){int at=3*f[reverse?3-k:k];out.addVertex(pose.pose(),v[at],v[at+1],v[at+2]).setColor(color).setUv(k==0||k==3?0:1,k<2?0:1).setOverlay(overlay).setLight(light).setNormal(pose,nx,ny,nz);}
 }
}
