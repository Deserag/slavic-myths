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
public final class HalaRenderer extends EntityRenderer<HalaEntity> {
 private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 private static final int[][] FACES={{0,1,2,3},{4,7,6,5},{0,4,5,1},{1,5,6,2},{2,6,7,3},{3,7,4,0}};
 private static final HalaGeometry GEO=HalaGeometry.INSTANCE;
 private final float[] vertices=new float[GEO.count*24];
 public HalaRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=2.4F;}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(HalaContent.BOSS.get(),HalaRenderer::new);}
 @Override public ResourceLocation getTextureLocation(HalaEntity e){return WHITE;}
 @Override public boolean shouldRender(HalaEntity e,Frustum frustum,double x,double y,double z){return frustum.isVisible(e.getBoundingBox().inflate(10,8,10));}
 @Override public void render(HalaEntity e,float yaw,float partial,PoseStack stack,MultiBufferSource buffer,int light){
  stack.pushPose();stack.mulPose(Axis.YP.rotationDegrees(-yaw));
  int action=e.visualAction();float progress=e.visualProgress(partial);GEO.sampleInto(action,progress,vertices);
  float dy=HalaEntity.poseElevation(action,progress);if(dy!=0)for(int i=1;i<vertices.length;i+=3)vertices[i]-=dy;
  var out=buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));
  int overlay=e.hurtTime>0?OverlayTexture.pack(0,OverlayTexture.v(true)):OverlayTexture.NO_OVERLAY;
  for(int i=0;i<GEO.count;i++){int color=GEO.colors[i],packed=light;if(color==0xffffb52d||color==0xffffe75c||color==0xfffff080||color==0xffff571b)packed=LightTexture.FULL_BRIGHT;int base=i*24;float cx=0,cy=0,cz=0;for(int k=0;k<8;k++){cx+=vertices[base+3*k]/8;cy+=vertices[base+3*k+1]/8;cz+=vertices[base+3*k+2]/8;}for(int[] f:FACES)face(stack.last(),out,vertices,base,f,cx,cy,cz,color,packed,overlay);}
  stack.popPose();super.render(e,yaw,partial,stack,buffer,light);
 }
 private static void face(PoseStack.Pose pose,VertexConsumer out,float[] v,int base,int[] f,float cx,float cy,float cz,int color,int light,int overlay){
  int a=base+3*f[0],b=base+3*f[1],c=base+3*f[2];
  float ux=v[b]-v[a],uy=v[b+1]-v[a+1],uz=v[b+2]-v[a+2],vx=v[c]-v[a],vy=v[c+1]-v[a+1],vz=v[c+2]-v[a+2];
  float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;boolean reverse=nx*(v[a]-cx)+ny*(v[a+1]-cy)+nz*(v[a+2]-cz)<0;float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(length<1e-7)return;float scale=(reverse?-1:1)/length;nx*=scale;ny*=scale;nz*=scale;
  for(int k=0;k<4;k++){int at=base+3*f[reverse?3-k:k];float u=k==0||k==3?0:1,tex=k<2?0:1;out.addVertex(pose.pose(),v[at],v[at+1],v[at+2]).setColor(color).setUv(u,tex).setOverlay(overlay).setLight(light).setNormal(pose,nx,ny,nz);}
 }
}
