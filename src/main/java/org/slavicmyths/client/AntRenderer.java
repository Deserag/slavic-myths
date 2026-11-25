package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.desert.*;
public final class AntRenderer extends EntityRenderer<AntEntity> {
 private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 private static final int[][] FACES={{0,1,2,3},{4,7,6,5},{0,4,5,1},{1,5,6,2},{2,6,7,3},{3,7,4,0}};
 public AntRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.5F;}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(AntContent.QUEEN.get(),AntRenderer::new);e.registerEntityRenderer(AntContent.WORKER.get(),AntRenderer::new);e.registerEntityRenderer(AntContent.FLYING.get(),AntRenderer::new);}
 @Override public ResourceLocation getTextureLocation(AntEntity e){return WHITE;}
 @Override public boolean shouldRender(AntEntity e,Frustum frustum,double x,double y,double z){return frustum.isVisible(e.getBoundingBox().inflate(e.kind()==AntRules.QUEEN?4:1));}
 @Override public void render(AntEntity e,float yaw,float partial,PoseStack stack,MultiBufferSource buffer,int light){
  stack.pushPose();stack.mulPose(Axis.YP.rotationDegrees(-yaw));
  var boxes=e.pose(partial);
  int overlay=e.hurtTime>0?OverlayTexture.pack(0,OverlayTexture.v(true)):OverlayTexture.NO_OVERLAY;
  for(var box:boxes){int rgb=box.color(),color=0xff000000|rgb,packed=(rgb==AntGeometry.E||rgb==AntGeometry.G)?LightTexture.FULL_BRIGHT:light;float[] vertices=box.vertices();var out=buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));int base=0;float cx=0,cy=0,cz=0;for(int k=0;k<8;k++){cx+=vertices[3*k]/8;cy+=vertices[3*k+1]/8;cz+=vertices[3*k+2]/8;}for(int[] f:FACES)face(stack.last(),out,vertices,base,f,cx,cy,cz,color,packed,overlay);}
  stack.popPose();super.render(e,yaw,partial,stack,buffer,light);
 }
 private static void face(PoseStack.Pose pose,VertexConsumer out,float[] v,int base,int[] f,float cx,float cy,float cz,int color,int light,int overlay){
  int a=base+3*f[0],b=base+3*f[1],c=base+3*f[2];
  float ux=v[b]-v[a],uy=v[b+1]-v[a+1],uz=v[b+2]-v[a+2],vx=v[c]-v[a],vy=v[c+1]-v[a+1],vz=v[c+2]-v[a+2];
  float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;boolean reverse=nx*(v[a]-cx)+ny*(v[a+1]-cy)+nz*(v[a+2]-cz)<0;float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(length<1e-7)return;float scale=(reverse?-1:1)/length;nx*=scale;ny*=scale;nz*=scale;
  for(int k=0;k<4;k++){int at=base+3*f[reverse?3-k:k];float u=k==0||k==3?0:1,tex=k<2?0:1;out.addVertex(pose.pose(),v[at],v[at+1],v[at+2]).setColor(color).setUv(u,tex).setOverlay(overlay).setLight(light).setNormal(pose,nx,ny,nz);}
 }
}
