package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.slavicmyths.mor.MorZone;
/** Small surface tiles follow loaded collision surfaces; no client terrain writes. */
public final class MorZoneRenderer extends EntityRenderer<MorZone>{
 public MorZoneRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public ResourceLocation getTextureLocation(MorZone e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/mor/"+(e.style()%2==0?"ground_likho":"ground_ovinnik")+".png");}
 @Override public void render(MorZone e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){p.pushPose();VertexConsumer b=out.getBuffer(RenderType.entityTranslucent(getTextureLocation(e)));int r=(int)e.radius();for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){if(x*x+z*z>r*r)continue;BlockPos at=BlockPos.containing(e.position()).offset(x,-1,z);if(!e.level().hasChunkAt(at))continue;var shape=e.level().getBlockState(at).getCollisionShape(e.level(),at);if(shape.isEmpty())continue;double top=shape.max(net.minecraft.core.Direction.Axis.Y);float y=(float)(at.getY()+top-e.getY()+.015);float tx=(float)(at.getX()+.5-e.getX()),tz=(float)(at.getZ()+.5-e.getZ());var pose=p.last();v(b,pose,tx-.5F,y,tz-.5F,0,0,light);v(b,pose,tx-.5F,y,tz+.5F,0,1,light);v(b,pose,tx+.5F,y,tz+.5F,1,1,light);v(b,pose,tx+.5F,y,tz-.5F,1,0,light);}
 if(e.cloud()){var fog=out.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/mor/cloud.png")));for(int i=0;i<9;i++){p.pushPose();double a=i*Math.PI/4;p.translate(i==8?0:Math.cos(a)*3.2,1.1,i==8?0:Math.sin(a)*3.2);p.mulPose(entityRenderDispatcher.cameraOrientation());var frame=p.last();v(fog,frame,-1.6F,-1.1F,0,0,1,light);v(fog,frame,1.6F,-1.1F,0,1,1,light);v(fog,frame,1.6F,1.1F,0,1,0,light);v(fog,frame,-1.6F,1.1F,0,0,0,light);p.popPose();}}
 p.popPose();super.render(e,yaw,partial,p,out,light);}
 private static void v(VertexConsumer b,PoseStack.Pose p,float x,float y,float z,float u,float v,int light){b.addVertex(p,x,y,z).setColor(255,255,255,210).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,1,0);}
}
